package id.seria.armorset.listener;

import id.seria.armorset.SeriaArmorSetPlugin;
import id.seria.armorset.config.ArmorSetConfig;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BlockDropListener implements Listener {

    private final SeriaArmorSetPlugin plugin;

    public BlockDropListener(SeriaArmorSetPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        ArmorSetConfig config = plugin.getConfiguration();
        if (!config.isBlocksEnabled()) return;

        Player player = event.getPlayer();
        if (player == null) return;

        if (!plugin.getMmoItemsHook().hasActiveSet(player)) return;

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

        if (anyAdded && config.isPlaySound()) {
            player.playSound(player.getLocation(), config.getSound(), config.getSoundVolume(), config.getSoundPitch());
        }
    }
}