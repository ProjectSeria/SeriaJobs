package id.seria.armorset.skill.impl;

import id.seria.armorset.SeriaArmorSetPlugin;
import id.seria.armorset.skill.ArmorSkill;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TelepathySkill implements ArmorSkill {

    private final SeriaArmorSetPlugin plugin;

    public TelepathySkill(SeriaArmorSetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getId() {
        return "telepathy";
    }

    @Override
    public @NotNull String getName() {
        return "Telepathy Drop Collector";
    }

    @Override
    public void onBlockDrop(@NotNull Player player, @NotNull BlockDropItemEvent event, @Nullable ConfigurationSection config) {
        boolean blocksEnabled = config == null || config.getBoolean("blocks", true);
        if (!blocksEnabled) return;

        List<Item> items = event.getItems();
        if (items.isEmpty()) return;

        Iterator<Item> it = items.iterator();
        boolean anyAdded = false;

        while (it.hasNext()) {
            Item itemEntity = it.next();
            ItemStack stack = itemEntity.getItemStack();
            if (stack == null || stack.getType().isAir()) {
                it.remove();
                continue;
            }

            int originalAmount = stack.getAmount();
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);

            if (leftover.isEmpty()) {
                it.remove();
                anyAdded = true;
            } else {
                Iterator<ItemStack> remIt = leftover.values().iterator();
                ItemStack primaryRem = remIt.next();
                if (primaryRem.getAmount() < originalAmount) {
                    anyAdded = true;
                }
                itemEntity.setItemStack(primaryRem);

                while (remIt.hasNext()) {
                    itemEntity.getWorld().dropItemNaturally(itemEntity.getLocation(), remIt.next());
                }
            }
        }

        if (anyAdded && isPlaySound(config)) {
            playSound(player, config);
        }
    }

    @Override
    public void onMobDeath(@NotNull Player killer, @NotNull EntityDeathEvent event, @Nullable ConfigurationSection config) {
        boolean mobsEnabled = config == null || config.getBoolean("mobs", true);
        if (!mobsEnabled) return;

        List<ItemStack> drops = event.getDrops();
        if (drops != null && !drops.isEmpty()) {
            List<ItemStack> leftovers = new ArrayList<>();
            boolean anyAdded = false;

            for (ItemStack drop : drops) {
                if (drop == null || drop.getType().isAir()) continue;

                int originalAmount = drop.getAmount();
                Map<Integer, ItemStack> remaining = killer.getInventory().addItem(drop);

                if (remaining.isEmpty()) {
                    anyAdded = true;
                } else {
                    leftovers.addAll(remaining.values());
                    if (remaining.values().iterator().next().getAmount() < originalAmount) {
                        anyAdded = true;
                    }
                }
            }

            drops.clear();
            drops.addAll(leftovers);

            if (anyAdded && isPlaySound(config)) {
                playSound(killer, config);
            }
        }

        boolean xpEnabled = config == null || config.getBoolean("xp", true);
        if (xpEnabled && event.getDroppedExp() > 0) {
            killer.giveExp(event.getDroppedExp());
            event.setDroppedExp(0);
        }
    }

    @Override
    public void onMythicMobDeath(@NotNull Player killer, @NotNull MythicMobDeathEvent event, @Nullable ConfigurationSection config) {
        boolean mobsEnabled = config == null || config.getBoolean("mobs", true);
        if (!mobsEnabled) return;

        List<ItemStack> drops = event.getDrops();
        if (drops == null || drops.isEmpty()) return;

        List<ItemStack> leftovers = new ArrayList<>();
        boolean anyAdded = false;

        for (ItemStack drop : drops) {
            if (drop == null || drop.getType().isAir()) continue;

            int originalAmount = drop.getAmount();
            Map<Integer, ItemStack> remaining = killer.getInventory().addItem(drop);

            if (remaining.isEmpty()) {
                anyAdded = true;
            } else {
                leftovers.addAll(remaining.values());
                if (remaining.values().iterator().next().getAmount() < originalAmount) {
                    anyAdded = true;
                }
            }
        }

        event.setDrops(leftovers);

        if (anyAdded && isPlaySound(config)) {
            playSound(killer, config);
        }
    }

    private boolean isPlaySound(@Nullable ConfigurationSection config) {
        if (config != null && config.contains("play-sound")) {
            return config.getBoolean("play-sound");
        }
        return plugin.getConfiguration().isPlaySound();
    }

    private void playSound(Player player, @Nullable ConfigurationSection config) {
        Sound sound = plugin.getConfiguration().getSound();
        float volume = plugin.getConfiguration().getSoundVolume();
        float pitch = plugin.getConfiguration().getSoundPitch();

        if (config != null) {
            String soundName = config.getString("sound-name");
            if (soundName != null) {
                try {
                    sound = Sound.valueOf(soundName.toUpperCase());
                } catch (IllegalArgumentException ignored) {}
            }
            volume = (float) config.getDouble("sound-volume", volume);
            pitch = (float) config.getDouble("sound-pitch", pitch);
        }

        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}