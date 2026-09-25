package id.seria.jobs.hook;

import id.seria.core.SeriaCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.util.UUID;
import java.util.function.Consumer;

public class SeriaCoreHook {

    private boolean available = false;

    public boolean setup() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("SeriaCore");
        if (plugin != null && plugin.isEnabled()) {
            available = true;
            return true;
        }
        return false;
    }

    public boolean isAvailable() {
        return available;
    }

    public Connection getConnection() {
        if (!available) return null;
        try {
            return SeriaCorePlugin.getInstance().getPlayerDataManager().getConnection();
        } catch (Throwable t) {
            return null;
        }
    }

    public void runAsync(Consumer<Connection> action) {
        if (!available) return;
        try {
            SeriaCorePlugin.getInstance().getPlayerDataManager().runAsync(action);
        } catch (Throwable t) {
            Bukkit.getLogger().warning("[SeriaJobs] Failed to run async DB task via SeriaCore: " + t.getMessage());
        }
    }

    public boolean addDelivery(UUID playerUuid, ItemStack item, String reason) {
        if (!available || item == null) return false;
        try {
            SeriaCorePlugin.getInstance().getDeliveryManager().addDelivery(playerUuid, item, reason);
            return true;
        } catch (Throwable t) {
            Bukkit.getLogger().warning("[SeriaJobs] Failed to stash undelivered item to SeriaCore: " + t.getMessage());
            return false;
        }
    }

    public void registerToSeriaCore(id.seria.jobs.SeriaJobs jobsPlugin, PAPIExpansion expansion) {
        if (!available) return;
        try {
            // 1. Register internal SeriaJobs provider with SeriaCore
            SeriaCorePlugin.getInstance().getPlaceholderManager().registerProvider(new id.seria.core.placeholder.SeriaPlaceholderProvider() {
                @Override
                public String getPrefix() {
                    return "seriajobs";
                }

                @Override
                public String onRequest(org.bukkit.entity.Player player, String params) {
                    if (expansion != null) {
                        return expansion.onRequest(player, params);
                    }
                    return null;
                }
            });

            // 2. Register SeriaJobs placeholder folder with SeriaCore
            java.io.File folder = new java.io.File(jobsPlugin.getDataFolder(), "placeholder");
            if (folder.exists() && folder.isDirectory()) {
                SeriaCorePlugin.getInstance().getPlaceholderManager().registerFolder(folder);
            }
        } catch (Throwable t) {
            Bukkit.getLogger().warning("[SeriaJobs] Failed to register placeholders with SeriaCore: " + t.getMessage());
        }
    }
}
