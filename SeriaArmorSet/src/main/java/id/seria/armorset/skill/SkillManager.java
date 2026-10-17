package id.seria.armorset.skill;

import id.seria.armorset.SeriaArmorSetPlugin;
import id.seria.armorset.hook.MythicLibSkillHandler;
import id.seria.armorset.model.ArmorSetDefinition;
import id.seria.armorset.skill.impl.DreadDecoySkill;
import id.seria.armorset.skill.impl.TelepathySkill;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.lib.MythicLib;
import org.bukkit.Bukkit;
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
    private final Map<String, String> skillAliases = new HashMap<>();

    public SkillManager(SeriaArmorSetPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        // Register default skills
        registerSkill(new TelepathySkill(plugin), "telepathy", "halloween_eclipse_vacuum", "eclipse_vacuum", "seria_telepathy");
        registerSkill(new DreadDecoySkill(plugin), "dread_decoy", "halloween_dread_decoy", "shadow_decoy");
    }

    public void registerSkill(@NotNull ArmorSkill skill, String... aliases) {
        String primaryId = skill.getId().toLowerCase(Locale.ROOT);
        registeredSkills.put(primaryId, skill);
        skillAliases.put(primaryId, primaryId);

        // Register in MythicLib as native skill handler
        registerInMythicLib(primaryId.toUpperCase(Locale.ROOT), skill);

        for (String alias : aliases) {
            String lowerAlias = alias.toLowerCase(Locale.ROOT);
            skillAliases.put(lowerAlias, primaryId);
            registerInMythicLib(alias.toUpperCase(Locale.ROOT), skill);
        }

        skill.onEnable();
        plugin.getLogger().info("Registered armor skill: " + skill.getName() + " (" + primaryId + ")");
    }

    private void registerInMythicLib(String id, ArmorSkill skill) {
        if (Bukkit.getPluginManager().isPluginEnabled("MythicLib")) {
            try {
                MythicLib.plugin.getSkills().registerSkillHandler(new MythicLibSkillHandler(id, skill));
            } catch (Throwable t) {
                // Ignore if already registered
            }
        }
    }

    public void unregisterAll() {
        for (ArmorSkill skill : registeredSkills.values()) {
            try {
                skill.onDisable();
            } catch (Throwable ignored) {}
        }
        registeredSkills.clear();
        skillAliases.clear();
    }

    public @Nullable ArmorSkill getSkill(@NotNull String id) {
        String primaryId = skillAliases.get(id.toLowerCase(Locale.ROOT));
        if (primaryId == null) {
            // Check config ability-mapping
            String mapped = plugin.getConfiguration().getMappedSkill(id);
            primaryId = skillAliases.get(mapped.toLowerCase(Locale.ROOT));
        }
        return primaryId != null ? registeredSkills.get(primaryId) : null;
    }

    public @NotNull Collection<ArmorSkill> getRegisteredSkills() {
        return Collections.unmodifiableCollection(registeredSkills.values());
    }

    public void dispatchBlockDrop(@NotNull Player player, @NotNull BlockDropItemEvent event) {
        ArmorSetDefinition set = plugin.getMmoItemsHook().getActiveSetDefinition(player);
        if (set == null) return;

        for (Map.Entry<String, ConfigurationSection> entry : set.getSkills().entrySet()) {
            ArmorSkill skill = getSkill(entry.getKey());
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
            ArmorSkill skill = getSkill(entry.getKey());
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
            ArmorSkill skill = getSkill(entry.getKey());
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
            ArmorSkill skill = getSkill(entry.getKey());
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
            ArmorSkill skill = getSkill(entry.getKey());
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