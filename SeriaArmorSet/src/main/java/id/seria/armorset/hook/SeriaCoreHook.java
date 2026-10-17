package id.seria.armorset.hook;

import id.seria.armorset.SeriaArmorSetPlugin;
import id.seria.armorset.model.ArmorSetDefinition;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SeriaCoreHook {

    private final SeriaArmorSetPlugin plugin;
    private boolean seriaCoreAvailable = false;
    private ArmorSetPlaceholderExpansion armorSetExpansion;
    private TelepathyPlaceholderExpansion telepathyExpansion;

    public SeriaCoreHook(SeriaArmorSetPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        Plugin seriaCore = Bukkit.getPluginManager().getPlugin("SeriaCore");
        if (seriaCore != null && seriaCore.isEnabled()) {
            seriaCoreAvailable = true;
            plugin.getLogger().info("Hooked into SeriaCore backbone successfully!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            armorSetExpansion = new ArmorSetPlaceholderExpansion(plugin);
            armorSetExpansion.register();

            telepathyExpansion = new TelepathyPlaceholderExpansion(plugin);
            telepathyExpansion.register();

            plugin.getLogger().info("Registered PlaceholderAPI expansions (%seriaarmorset_...% and %settelepathy_...%)");
        }
    }

    public void unregister() {
        if (armorSetExpansion != null) {
            try { armorSetExpansion.unregister(); } catch (Throwable ignored) {}
            armorSetExpansion = null;
        }
        if (telepathyExpansion != null) {
            try { telepathyExpansion.unregister(); } catch (Throwable ignored) {}
            telepathyExpansion = null;
        }
    }

    public boolean isAvailable() {
        return seriaCoreAvailable;
    }

    private static class ArmorSetPlaceholderExpansion extends PlaceholderExpansion {

        private final SeriaArmorSetPlugin plugin;

        public ArmorSetPlaceholderExpansion(SeriaArmorSetPlugin plugin) {
            this.plugin = plugin;
        }

        @Override public @NotNull String getIdentifier() { return "seriaarmorset"; }
        @Override public @NotNull String getAuthor() { return "Seria"; }
        @Override public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }
        @Override public boolean persist() { return true; }

        @Override
        public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
            if (player == null) return "";

            MMOItemsHook hook = plugin.getMmoItemsHook();
            if (hook == null) return "";

            ArmorSetDefinition def = hook.getActiveSetDefinition(player);

            switch (params.toLowerCase()) {
                case "has_set":
                    return String.valueOf(def != null);
                case "active_set":
                    return def != null ? def.getId() : "None";
                case "pieces_worn": {
                    if (def == null) return "0";
                    return String.valueOf(hook.getPieceCount(player, def.getId()));
                }
                case "pieces_required": {
                    if (def == null) return "0";
                    return String.valueOf(def.getPiecesRequired());
                }
                case "active_skills": {
                    if (def == null) return "None";
                    return String.join(", ", def.getSkillIds());
                }
                default:
                    return null;
            }
        }
    }

    private static class TelepathyPlaceholderExpansion extends PlaceholderExpansion {

        private final SeriaArmorSetPlugin plugin;

        public TelepathyPlaceholderExpansion(SeriaArmorSetPlugin plugin) {
            this.plugin = plugin;
        }

        @Override public @NotNull String getIdentifier() { return "settelepathy"; }
        @Override public @NotNull String getAuthor() { return "Seria"; }
        @Override public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }
        @Override public boolean persist() { return true; }

        @Override
        public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
            if (player == null) return "";

            MMOItemsHook hook = plugin.getMmoItemsHook();
            if (hook == null) return "";

            ArmorSetDefinition def = hook.getActiveSetDefinition(player);

            switch (params.toLowerCase()) {
                case "has_set":
                    return String.valueOf(def != null && def.hasSkill("telepathy"));
                case "active_set":
                    return def != null ? def.getId() : "None";
                case "pieces_worn": {
                    if (def == null) return "0";
                    return String.valueOf(hook.getPieceCount(player, def.getId()));
                }
                case "pieces_required": {
                    if (def == null) return "0";
                    return String.valueOf(def.getPiecesRequired());
                }
                default:
                    return null;
            }
        }
    }
}