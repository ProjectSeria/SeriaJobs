package id.seria.jobs.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomPAPIExpansion extends PlaceholderExpansion {

    private final CustomPlaceholderManager manager;

    public CustomPAPIExpansion(CustomPlaceholderManager manager) {
        this.manager = manager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "custom";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Seria";
    }

    @Override
    public @NotNull String getVersion() {
        return "2.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        Player player = offlinePlayer != null && offlinePlayer.isOnline() ? offlinePlayer.getPlayer() : null;
        return manager.evaluate(player, params);
    }
}
