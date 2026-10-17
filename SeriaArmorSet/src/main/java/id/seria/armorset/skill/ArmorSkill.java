package id.seria.armorset.skill;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
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
     * Called when a player breaks a block that produces drops.
     */
    default void onBlockDrop(@NotNull Player player, @NotNull BlockDropItemEvent event, @Nullable ConfigurationSection config) {}

    /**
     * Called when a player kills any living entity that produces death drops.
     */
    default void onMobDeath(@NotNull Player killer, @NotNull EntityDeathEvent event, @Nullable ConfigurationSection config) {}

    /**
     * Called when a player kills a MythicMobs mob that triggers MythicMobDeathEvent.
     */
    default void onMythicMobDeath(@NotNull Player killer, @NotNull MythicMobDeathEvent event, @Nullable ConfigurationSection config) {}
}