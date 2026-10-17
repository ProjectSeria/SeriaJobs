package id.seria.armorset.skill;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ArmorSkill {

    /**
     * Unique identifier for this skill (e.g. "telepathy", "eclipse_vacuum", "shadow_decoy").
     */
    @NotNull String getId();

    /**
     * Human-readable display name.
     */
    @NotNull String getName();

    /**
     * Called when the plugin or skill system is enabled/reloaded.
     */
    default void onEnable() {}

    /**
     * Called when the plugin or skill system is disabled.
     */
    default void onDisable() {}

    /**
     * Triggered when a player breaks a block producing item drops.
     */
    default void onBlockDrop(@NotNull Player player, @NotNull BlockDropItemEvent event, @Nullable ConfigurationSection config) {}

    /**
     * Triggered when a player kills any living entity producing death drops.
     */
    default void onMobDeath(@NotNull Player killer, @NotNull EntityDeathEvent event, @Nullable ConfigurationSection config) {}

    /**
     * Triggered when a player kills a MythicMobs mob that fires MythicMobDeathEvent.
     */
    default void onMythicMobDeath(@NotNull Player killer, @NotNull MythicMobDeathEvent event, @Nullable ConfigurationSection config) {}

    /**
     * Triggered when a player wearing the armor set receives damage from another entity.
     * (E.g. for decoy summons, damage reflection, damage mitigation, retaliations).
     */
    default void onDamagedByEntity(@NotNull Player player, @NotNull EntityDamageByEntityEvent event, @Nullable ConfigurationSection config) {}

    /**
     * Triggered when a player wearing the armor set hits another entity.
     * (E.g. for life steal, armor penetration, bonus on-hit effects).
     */
    default void onAttackEntity(@NotNull Player attacker, @NotNull EntityDamageByEntityEvent event, @Nullable ConfigurationSection config) {}
}