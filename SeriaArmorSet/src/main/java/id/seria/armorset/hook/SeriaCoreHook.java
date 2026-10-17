package id.seria.armorset.hook;

import id.seria.armorset.SeriaArmorSetPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SeriaCoreHook {

    private final SeriaArmorSetPlugin plugin;
    private boolean seriaCoreAvailable = false;
    private TelepathyPlaceholderExpansion placeholderExpansion;

    public SeriaCoreHook(SeriaArmorSetPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        Plugin seriaCore = Bukkit.getPluginManager().getPlugin("SeriaCore");
        if (seriaCore != null && seriaCore.isEnabled()) {
            seriaCoreAvailable = true;
            plugin.getLogger().info("Hooked into SeriaCore successfully!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderExpansion = new TelepathyPlaceholderExpansion(plugin);
            placeholderExpansion.register();
            plugin.getLogger().info("Registered PlaceholderAPI expansion for SetTelepathy (%settelepathy_...%)");
        }
    }

    public void unregister() {
        if (placeholderExpansion != null) {
            try {
                placeholderExpansion.unregister();
            } catch (Throwable ignored) {}
            placeholderExpansion = null;
        }
    }

    public boolean isAvailable() {
        return seriaCoreAvailable;
    }

    private static class TelepathyPlaceholderExpansion extends PlaceholderExpansion {

        private final SeriaArmorSetPlugin plugin;

        public TelepathyPlaceholderExpansion(SeriaArmorSetPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public @NotNull String getIdentifier() {
            return "settelepathy";
        }

        @Override
        public @NotNull String getAuthor() {
            return "Seria";
        }

        @Override
        public @NotNull String getVersion() {
            return plugin.getDescription().getVersion();
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
            if (player == null) return "";

            MMOItemsHook hook = plugin.getMmoItemsHook();
            if (hook == null) return "";

            switch (params.toLowerCase()) {
                case "has_set":
                    return String.valueOf(hook.hasActiveSet(player));
                case "active_set": {
                    String activeSet = hook.getActiveSet(player);
                    return activeSet != null ? activeSet : "None";
                }
                case "pieces_worn": {
                    String activeSet = hook.getActiveSet(player);
                    if (activeSet == null) return "0";
                    return String.valueOf(hook.getPieceCount(player, activeSet));
                }
                case "pieces_required": {
                    String activeSet = hook.getActiveSet(player);
                    if (activeSet == null) return "0";
                    return String.valueOf(plugin.getConfiguration().getRequiredPieces(activeSet));
                }
                default:
                    return null;
            }
        }
    }
}