package id.seria.armorset.skill.impl;

import id.seria.armorset.SeriaArmorSetPlugin;
import id.seria.armorset.skill.ArmorSkill;
import id.seria.armorset.util.TextUtil;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DreadDecoySkill implements ArmorSkill {

    private static final String COOLDOWN_KEY = "HALLOWEEN_DREAD_DECOY";
    private final SeriaArmorSetPlugin plugin;

    public DreadDecoySkill(SeriaArmorSetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getId() {
        return "dread_decoy";
    }

    @Override
    public @NotNull String getName() {
        return "Shadow Sentry (Dread Decoy)";
    }

    @Override
    public void onDamagedByEntity(@NotNull Player player, @NotNull EntityDamageByEntityEvent event, @Nullable ConfigurationSection config) {
        double cooldown = config != null ? config.getDouble("cooldown", 30.0) : 30.0;

        // 1. Cooldown check via MythicLib CooldownMap
        MMOPlayerData mmoPlayer = MMOPlayerData.get(player);
        if (mmoPlayer != null && mmoPlayer.getCooldownMap().isOnCooldown(COOLDOWN_KEY)) {
            return;
        }

        // Apply cooldown
        if (mmoPlayer != null) {
            mmoPlayer.getCooldownMap().applyCooldown(COOLDOWN_KEY, cooldown);
        }

        // 2. Cast MythicMobs skill if available
        boolean castViaMythic = false;
        String mythicSkillName = config != null ? config.getString("mythic-skill", "Halloween_DreadDecoy") : "Halloween_DreadDecoy";

        if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            try {
                castViaMythic = MythicBukkit.inst().getAPIHelper().castSkill(player, mythicSkillName);
            } catch (Throwable t) {
                if (plugin.getConfiguration().isDebug()) {
                    plugin.getLogger().warning("[Debug] Failed casting MythicMobs skill '" + mythicSkillName + "': " + t.getMessage());
                }
            }
        }

        // 3. Fallback effects if MythicMobs is not present or failed
        if (!castViaMythic) {
            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
            player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.8f);
            TextUtil.sendMessage(player, "&8[&4☠&8] &7Bayangan pelindung bangkit!");
        }

        if (plugin.getConfiguration().isDebug()) {
            plugin.getLogger().info("[Debug] Triggered DreadDecoy for " + player.getName() + " (Cooldown: " + cooldown + "s)");
        }
    }
}