package id.seria.jobs.hook;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuckPermsHook {

    private LuckPerms luckPerms;

    public boolean setup() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null || !Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            return false;
        }
        try {
            this.luckPerms = LuckPermsProvider.get();
            return this.luckPerms != null;
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean isLoaded() {
        if (luckPerms == null) {
            setup();
        }
        return luckPerms != null;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    @Nullable
    public String getMeta(@NotNull Player player, @NotNull String key) {
        if (isLoaded()) {
            try {
                User user = luckPerms.getUserManager().getUser(player.getUniqueId());
                if (user != null) {
                    String val = user.getCachedData().getMetaData().getMetaValue(key);
                    if (val != null) return val;
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    @Nullable
    public String getUserMeta(@NotNull Player player, @NotNull String key) {
        return getMeta(player, key);
    }

    public int getIntMeta(@NotNull Player player, @NotNull String key, int fallback) {
        String val = getMeta(player, key);
        if (val != null) {
            try {
                return Integer.parseInt(val.trim());
            } catch (NumberFormatException ignored) {}
        }
        return fallback;
    }

    public double getDoubleMeta(@NotNull Player player, @NotNull String key, double fallback) {
        String val = getMeta(player, key);
        if (val != null) {
            try {
                return Double.parseDouble(val.trim());
            } catch (NumberFormatException ignored) {}
        }
        return fallback;
    }

    @NotNull
    public String getPrimaryGroup(@NotNull Player player) {
        if (isLoaded()) {
            try {
                User user = luckPerms.getUserManager().getUser(player.getUniqueId());
                if (user != null) {
                    return user.getPrimaryGroup();
                }
            } catch (Throwable ignored) {}
        }
        return "default";
    }

    @Nullable
    public String getPrefix(@NotNull Player player) {
        if (isLoaded()) {
            try {
                User user = luckPerms.getUserManager().getUser(player.getUniqueId());
                if (user != null) {
                    return user.getCachedData().getMetaData().getPrefix();
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    /**
     * Calculates the extra item bonus for a player.
     * Looks up LuckPerms meta:
     * 1. Job-specific meta: "extra_job_item_<jobId>" or "extra_item_<jobId>"
     * 2. General meta: "extra_job_item", "extra_job_items", "job_extra_item", "extra_item"
     * 3. Permission node: seriajobs.extraitem.<number> or seriajobs.bonus.<number>
     */
    public int getExtraJobItems(@NotNull Player player) {
        return getExtraJobItems(player, null);
    }

    public int getExtraJobItems(@NotNull Player player, @Nullable String jobId) {
        int extra = 0;

        if (isLoaded()) {
            if (jobId != null) {
                extra = getIntMeta(player, "extra_job_item_" + jobId.toLowerCase(), 0);
                if (extra == 0) {
                    extra = getIntMeta(player, "extra_item_" + jobId.toLowerCase(), 0);
                }
            }

            if (extra == 0) {
                extra = getIntMeta(player, "extra_job_item", 0);
            }
            if (extra == 0) {
                extra = getIntMeta(player, "extra_job_items", 0);
            }
            if (extra == 0) {
                extra = getIntMeta(player, "job_extra_item", 0);
            }
            if (extra == 0) {
                extra = getIntMeta(player, "extra_item", 0);
            }
            if (extra == 0) {
                extra = getIntMeta(player, "seriajobs_extra_item", 0);
            }
        }

        // Permission fallback / additive check: seriajobs.extraitem.<number>
        int permExtra = getHighestPermissionNumber(player, "seriajobs.extraitem.");
        if (permExtra <= 0) {
            permExtra = getHighestPermissionNumber(player, "seriajobs.bonus.");
        }

        return Math.max(extra, permExtra);
    }

    public int getHighestPermissionNumber(@NotNull Player player, @NotNull String prefix) {
        int highest = 0;
        String lowerPrefix = prefix.toLowerCase();
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            if (!pai.getValue()) continue;
            String perm = pai.getPermission().toLowerCase();
            if (perm.startsWith(lowerPrefix)) {
                String numStr = perm.substring(lowerPrefix.length());
                try {
                    int val = Integer.parseInt(numStr);
                    if (val > highest) {
                        highest = val;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return highest;
    }
}
