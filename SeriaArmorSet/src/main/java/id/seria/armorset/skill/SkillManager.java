package id.seria.armorset.skill;

import id.seria.armorset.SeriaArmorSetPlugin;
import id.seria.armorset.model.ArmorSetDefinition;
import id.seria.armorset.skill.impl.TelepathySkill;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SkillManager {

    private final SeriaArmorSetPlugin plugin;
    private final Map<String, ArmorSkill> registeredSkills = new HashMap<>();

    public SkillManager(SeriaArmorSetPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        // Register default skills
        registerSkill(new TelepathySkill(plugin));
    }

    public void registerSkill(@NotNull ArmorSkill skill) {
        String id = skill.getId().toLowerCase(Locale.ROOT);
        registeredSkills.put(id, skill);
        skill.onEnable();
        plugin.getLogger().info("Registered armor skill: " + skill.getName() + " (" + id + ")");
    }

    public void unregisterAll() {
        for (ArmorSkill skill : registeredSkills.values()) {
            try {
                skill.onDisable();
            } catch (Throwable ignored) {}
        }
        registeredSkills.clear();
    }

    public @Nullable ArmorSkill getSkill(@NotNull String id) {
        return registeredSkills.get(id.toLowerCase(Locale.ROOT));
    }

    public @NotNull Collection<ArmorSkill> getRegisteredSkills() {
        return Collections.unmodifiableCollection(registeredSkills.values());
    }

    public void dispatchBlockDrop(@NotNull Player player, @NotNull BlockDropItemEvent event) {
        ArmorSetDefinition set = plugin.getMmoItemsHook().getActiveSetDefinition(player);
        if (set == null) return;

        for (Map.Entry<String, ConfigurationSection> entry : set.getSkills().entrySet()) {
            ArmorSkill skill = registeredSkills.get(entry.getKey());
            if (skill != null) {
                try {
                    skill.onBlockDrop(player, event, entry.getValue());
                } catch (Throwable t) {
                    plugin.getLogger().warning("Error executing skill '" + skill.getId() + "' for block drop: " + t.getMessage());
                }
            }
        }
    }

    public void dispatchMobDeath(@NotNull Player killer, @NotNull EntityDeathEvent event) {
        ArmorSetDefinition set = plugin.getMmoItemsHook().getActiveSetDefinition(killer);
        if (set == null) return;

        for (Map.Entry<String, ConfigurationSection> entry : set.getSkills().entrySet()) {
            ArmorSkill skill = registeredSkills.get(entry.getKey());
            if (skill != null) {
                try {
                    skill.onMobDeath(killer, event, entry.getValue());
                } catch (Throwable t) {
                    plugin.getLogger().warning("Error executing skill '" + skill.getId() + "' for mob death: " + t.getMessage());
                }
            }
        }
    }

    public void dispatchMythicMobDeath(@NotNull Player killer, @NotNull MythicMobDeathEvent event) {
        ArmorSetDefinition set = plugin.getMmoItemsHook().getActiveSetDefinition(killer);
        if (set == null) return;

        for (Map.Entry<String, ConfigurationSection> entry : set.getSkills().entrySet()) {
            ArmorSkill skill = registeredSkills.get(entry.getKey());
            if (skill != null) {
                try {
                    skill.onMythicMobDeath(killer, event, entry.getValue());
                } catch (Throwable t) {
                    plugin.getLogger().warning("Error executing skill '" + skill.getId() + "' for MythicMob death: " + t.getMessage());
                }
            }
        }
    }

    public void dispatchDamagedByEntity(@NotNull Player player, @NotNull EntityDamageByEntityEvent event) {
        ArmorSetDefinition set = plugin.getMmoItemsHook().getActiveSetDefinition(player);
        if (set == null) return;

        for (Map.Entry<String, ConfigurationSection> entry : set.getSkills().entrySet()) {
            ArmorSkill skill = registeredSkills.get(entry.getKey());
            if (skill != null) {
                try {
                    skill.onDamagedByEntity(player, event, entry.getValue());
                } catch (Throwable t) {
                    plugin.getLogger().warning("Error executing skill '" + skill.getId() + "' for damage event: " + t.getMessage());
                }
            }
        }
    }

    public void dispatchAttackEntity(@NotNull Player attacker, @NotNull EntityDamageByEntityEvent event) {
        ArmorSetDefinition set = plugin.getMmoItemsHook().getActiveSetDefinition(attacker);
        if (set == null) return;

        for (Map.Entry<String, ConfigurationSection> entry : set.getSkills().entrySet()) {
            ArmorSkill skill = registeredSkills.get(entry.getKey());
            if (skill != null) {
                try {
                    skill.onAttackEntity(attacker, event, entry.getValue());
                } catch (Throwable t) {
                    plugin.getLogger().warning("Error executing skill '" + skill.getId() + "' for attack event: " + t.getMessage());
                }
            }
        }
    }
}