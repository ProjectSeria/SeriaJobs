package id.seria.jobs.hook;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import id.seria.jobs.model.JobOrder;
import id.seria.jobs.model.JobProfile;
import id.seria.jobs.model.LeaderboardEntry;
import id.seria.jobs.model.LeaderboardPeriod;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PAPIExpansion extends PlaceholderExpansion {

    private final SeriaJobs plugin;

    public PAPIExpansion(SeriaJobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "seriajobs";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Seria Network";
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
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        String lower = params.toLowerCase();
        Player onlinePlayer = (offlinePlayer != null && offlinePlayer.isOnline()) ? offlinePlayer.getPlayer() : null;

        // Custom placeholder support via %seriajobs_custom_<name>%
        if (plugin.getCustomPlaceholderManager() != null) {
            if (lower.startsWith("custom_")) {
                String res = plugin.getCustomPlaceholderManager().evaluate(onlinePlayer, params.substring(7));
                if (res != null) return res;
            }
        }

        // 1. Countdown Placeholders (Server-wide)
        if (lower.equals("rotation_countdown")) {
            return plugin.getOrderPoolManager().getFormattedCountdown();
        }
        if (lower.equals("leaderboard_weekly_countdown")) {
            return plugin.getLeaderboardManager() != null ? plugin.getLeaderboardManager().getWeeklyCountdown() : "-";
        }
        if (lower.equals("leaderboard_monthly_countdown")) {
            return plugin.getLeaderboardManager() != null ? plugin.getLeaderboardManager().getMonthlyCountdown() : "-";
        }

        // 2. Global Leaderboard Top Entries (Server-wide)
        // Format: top_global_[period_]<pos>_<field> or leaderboard_global_[period_]<pos>_<field>
        if ((lower.startsWith("top_global_") || lower.startsWith("leaderboard_global_")) && !lower.contains("player_rank")) {
            String sub = lower.startsWith("top_global_")
                    ? lower.substring("top_global_".length())
                    : lower.substring("leaderboard_global_".length());
            return handleGlobalLeaderboard(sub);
        }

        // 3. Per-Job Leaderboard Top Entries (Server-wide)
        // Format: top_job_<job_id>_[period_]<pos>_<field> or leaderboard_job_<job_id>_[period_]<pos>_<field>
        if ((lower.startsWith("top_job_") || lower.startsWith("leaderboard_job_")) && !lower.contains("player_rank")) {
            String sub = lower.startsWith("top_job_")
                    ? lower.substring("top_job_".length())
                    : lower.substring("leaderboard_job_".length());
            return handleJobLeaderboard(sub);
        }

        // 4. Offline/Null Player Check for Player-Specific Placeholders
        if (offlinePlayer == null || !offlinePlayer.isOnline()) {
            return "";
        }

        Player player = offlinePlayer.getPlayer();
        if (player == null) return "";

        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());

        // 5. LuckPerms Placeholders
        if (lower.startsWith("luckperms_") || lower.startsWith("lp_")) {
            if (plugin.getHookManager().isLuckPermsLoaded()) {
                LuckPermsHook lp = plugin.getHookManager().getLuckPermsHook();
                if (lower.startsWith("luckperms_meta_")) {
                    String key = params.substring("luckperms_meta_".length());
                    String val = lp.getUserMeta(player, key);
                    return val != null ? val : "";
                }
                if (lower.startsWith("lp_meta_")) {
                    String key = params.substring("lp_meta_".length());
                    String val = lp.getUserMeta(player, key);
                    return val != null ? val : "";
                }
                if (lower.equals("luckperms_group") || lower.equals("lp_group")) {
                    return lp.getPrimaryGroup(player);
                }
                if (lower.equals("luckperms_prefix") || lower.equals("lp_prefix")) {
                    return lp.getPrefix(player);
                }
                if (lower.equals("luckperms_extra_items") || lower.equals("lp_extra_items")) {
                    return String.valueOf(lp.getExtraJobItems(player));
                }
            }
            return "";
        }

        // 6. Player Global Rank
        // Format: top_global_player_rank or player_rank_global or leaderboard_player_rank [_<period>]
        if (lower.startsWith("top_global_player_rank") || lower.startsWith("player_rank_global") || lower.equals("leaderboard_player_rank")) {
            LeaderboardPeriod period = LeaderboardPeriod.WEEKLY;
            if (lower.endsWith("_monthly")) period = LeaderboardPeriod.MONTHLY;
            else if (lower.endsWith("_alltime")) period = LeaderboardPeriod.ALL_TIME;

            if (plugin.getLeaderboardManager() != null) {
                int rank = plugin.getLeaderboardManager().getPlayerGlobalRank(player.getUniqueId(), period);
                return rank == -1 ? "-" : String.valueOf(rank);
            }
            return "-";
        }

        // 7. Player Job Rank
        // Format: top_job_<job_id>_player_rank or player_rank_job_<job_id> [_<period>]
        if ((lower.startsWith("top_job_") && lower.contains("_player_rank")) || lower.startsWith("player_rank_job_")) {
            LeaderboardPeriod period = LeaderboardPeriod.WEEKLY;
            String clean = lower;
            if (clean.endsWith("_monthly")) {
                period = LeaderboardPeriod.MONTHLY;
                clean = clean.substring(0, clean.length() - "_monthly".length());
            } else if (clean.endsWith("_alltime")) {
                period = LeaderboardPeriod.ALL_TIME;
                clean = clean.substring(0, clean.length() - "_alltime".length());
            } else if (clean.endsWith("_weekly")) {
                period = LeaderboardPeriod.WEEKLY;
                clean = clean.substring(0, clean.length() - "_weekly".length());
            }

            String jobId = null;
            if (clean.startsWith("top_job_") && clean.endsWith("_player_rank")) {
                jobId = clean.substring("top_job_".length(), clean.length() - "_player_rank".length());
            } else if (clean.startsWith("player_rank_job_")) {
                jobId = clean.substring("player_rank_job_".length());
            }

            if (jobId != null && plugin.getLeaderboardManager() != null) {
                int rank = plugin.getLeaderboardManager().getPlayerJobRank(player.getUniqueId(), jobId, period);
                return rank == -1 ? "-" : String.valueOf(rank);
            }
            return "-";
        }

        // 7b. Player Leaderboard Stats in Current Cycle
        // Format: player_leaderboard_[xp|orders][_period]
        if (lower.startsWith("player_leaderboard_")) {
            LeaderboardPeriod period = LeaderboardPeriod.WEEKLY;
            if (lower.endsWith("_monthly")) period = LeaderboardPeriod.MONTHLY;
            else if (lower.endsWith("_alltime")) period = LeaderboardPeriod.ALL_TIME;

            if (plugin.getLeaderboardManager() != null) {
                LeaderboardEntry entry = plugin.getLeaderboardManager().getPlayerGlobalEntry(player.getUniqueId(), period);
                if (lower.contains("_xp_formatted") || lower.contains("_formatted_xp")) {
                    return entry != null ? String.format(java.util.Locale.US, "%,d", entry.getXp()) : "0";
                }
                if (lower.contains("_xp")) {
                    return entry != null ? String.valueOf(entry.getXp()) : "0";
                }
                if (lower.contains("_orders_formatted") || lower.contains("_formatted_orders")) {
                    return entry != null ? String.format(java.util.Locale.US, "%,d", entry.getOrdersCompleted()) : "0";
                }
                if (lower.contains("_orders")) {
                    return entry != null ? String.valueOf(entry.getOrdersCompleted()) : "0";
                }
            }
            return "0";
        }

        // 8. General Job Placeholders
        if (params.equalsIgnoreCase("max_slots")) {
            int max = plugin.getSlotPermissionManager().getMaxSlots(player);
            return max == -1 ? "Tak Terbatas" : String.valueOf(max);
        }

        if (params.equalsIgnoreCase("active_jobs_count")) {
            return String.valueOf(profile.getActiveJobs().size());
        }

        // seriajobs_has_job_<job_id>
        if (params.toLowerCase().startsWith("has_job_")) {
            String jobId = params.substring("has_job_".length());
            return String.valueOf(profile.hasJob(jobId));
        }

        // seriajobs_job_name_<job_id>
        if (params.toLowerCase().startsWith("job_name_")) {
            String jobId = params.substring("job_name_".length());
            Job job = plugin.getJobRegistry().getJob(jobId);
            return job != null ? job.getDisplayName() : jobId;
        }

        // seriajobs_primary_job_name / seriajobs_job_name
        if (params.equalsIgnoreCase("job_name") || params.equalsIgnoreCase("primary_job_name")) {
            if (!profile.getActiveJobs().isEmpty()) {
                String firstJobId = profile.getActiveJobs().iterator().next();
                Job job = plugin.getJobRegistry().getJob(firstJobId);
                return job != null ? job.getDisplayName() : firstJobId;
            }
            return "-";
        }

        // seriajobs_primary_job_id / seriajobs_job_id
        if (params.equalsIgnoreCase("job_id") || params.equalsIgnoreCase("primary_job_id")) {
            if (!profile.getActiveJobs().isEmpty()) {
                return profile.getActiveJobs().iterator().next();
            }
            return "-";
        }

        // seriajobs_primary_job_level / seriajobs_level
        if (params.equalsIgnoreCase("level") || params.equalsIgnoreCase("primary_job_level")) {
            if (!profile.getActiveJobs().isEmpty()) {
                String firstJobId = profile.getActiveJobs().iterator().next();
                return String.valueOf(profile.getJobLevel(firstJobId));
            }
            return "1";
        }

        // seriajobs_primary_job_xp / seriajobs_xp
        if (params.equalsIgnoreCase("xp") || params.equalsIgnoreCase("primary_job_xp")) {
            if (!profile.getActiveJobs().isEmpty()) {
                String firstJobId = profile.getActiveJobs().iterator().next();
                return String.valueOf(profile.getJobXp(firstJobId));
            }
            return "0";
        }

        // seriajobs_primary_job_required_xp / seriajobs_required_xp
        if (params.equalsIgnoreCase("required_xp") || params.equalsIgnoreCase("primary_job_required_xp")) {
            if (!profile.getActiveJobs().isEmpty()) {
                String firstJobId = profile.getActiveJobs().iterator().next();
                Job job = plugin.getJobRegistry().getJob(firstJobId);
                if (job != null) {
                    return String.valueOf(job.getRequiredXp(profile.getJobLevel(firstJobId)));
                }
            }
            return "100";
        }

        // seriajobs_job_level_<job_id>
        if (params.toLowerCase().startsWith("job_level_")) {
            String jobId = params.substring("job_level_".length());
            return String.valueOf(profile.getJobLevel(jobId));
        }

        // seriajobs_job_xp_<job_id>
        if (params.toLowerCase().startsWith("job_xp_")) {
            String jobId = params.substring("job_xp_".length());
            return String.valueOf(profile.getJobXp(jobId));
        }

        // seriajobs_job_required_xp_<job_id>
        if (params.toLowerCase().startsWith("job_required_xp_")) {
            String jobId = params.substring("job_required_xp_".length());
            Job job = plugin.getJobRegistry().getJob(jobId);
            if (job == null) return "0";
            int level = profile.getJobLevel(jobId);
            return String.valueOf(job.getRequiredXp(level));
        }

        // seriajobs_order_progress_<job_id>_<index>
        if (params.toLowerCase().startsWith("order_progress_")) {
            String sub = params.substring("order_progress_".length());
            int lastUnderscore = sub.lastIndexOf('_');
            if (lastUnderscore != -1) {
                String jobId = sub.substring(0, lastUnderscore);
                try {
                    int index = Integer.parseInt(sub.substring(lastUnderscore + 1));
                    List<JobOrder> active = plugin.getOrderPoolManager().getActiveOrders(jobId);
                    if (index >= 0 && index < active.size()) {
                        JobOrder order = active.get(index);
                        String shiftId = plugin.getOrderPoolManager().getCurrentShiftId();
                        return String.valueOf(profile.getOrderProgress(shiftId, jobId, order.getId()));
                    }
                } catch (NumberFormatException ignored) {}
            }
            return "0";
        }

        // seriajobs_primary_job_resource_multiplier / seriajobs_resource_multiplier
        if (params.equalsIgnoreCase("resource_multiplier") || params.equalsIgnoreCase("primary_job_resource_multiplier")) {
            if (!profile.getActiveJobs().isEmpty()) {
                String firstJobId = profile.getActiveJobs().iterator().next();
                int level = profile.getJobLevel(firstJobId);
                return String.format(java.util.Locale.US, "%.2f", plugin.getConfigManager().getResourceMultiplier(level));
            }
            return "1.00";
        }

        // seriajobs_resource_multiplier_<job_id>
        if (params.toLowerCase().startsWith("resource_multiplier_")) {
            String jobId = params.substring("resource_multiplier_".length());
            int level = profile.getJobLevel(jobId);
            return String.format(java.util.Locale.US, "%.2f", plugin.getConfigManager().getResourceMultiplier(level));
        }

        // seriajobs_order_required_<job_id>_<index>
        if (params.toLowerCase().startsWith("order_required_")) {
            String sub = params.substring("order_required_".length());
            int lastUnderscore = sub.lastIndexOf('_');
            if (lastUnderscore != -1) {
                String jobId = sub.substring(0, lastUnderscore);
                try {
                    int index = Integer.parseInt(sub.substring(lastUnderscore + 1));
                    List<JobOrder> active = plugin.getOrderPoolManager().getActiveOrders(jobId);
                    if (index >= 0 && index < active.size()) {
                        int level = profile.getJobLevel(jobId);
                        return String.valueOf(active.get(index).getRequiredAmount(level));
                    }
                } catch (NumberFormatException ignored) {}
            }
            return "0";
        }

        // seriajobs_order_base_required_<job_id>_<index>
        if (params.toLowerCase().startsWith("order_base_required_")) {
            String sub = params.substring("order_base_required_".length());
            int lastUnderscore = sub.lastIndexOf('_');
            if (lastUnderscore != -1) {
                String jobId = sub.substring(0, lastUnderscore);
                try {
                    int index = Integer.parseInt(sub.substring(lastUnderscore + 1));
                    List<JobOrder> active = plugin.getOrderPoolManager().getActiveOrders(jobId);
                    if (index >= 0 && index < active.size()) {
                        return String.valueOf(active.get(index).getAmount());
                    }
                } catch (NumberFormatException ignored) {}
            }
            return "0";
        }

        if (plugin.getCustomPlaceholderManager() != null) {
            String customRes = plugin.getCustomPlaceholderManager().evaluate(onlinePlayer, params);
            if (customRes != null) return customRes;
        }

        return null;
    }

    private @Nullable String handleGlobalLeaderboard(String sub) {
        if (plugin.getLeaderboardManager() == null) return "-";

        LeaderboardPeriod period = LeaderboardPeriod.WEEKLY;
        String remaining = sub;
        if (sub.startsWith("weekly_")) {
            period = LeaderboardPeriod.WEEKLY;
            remaining = sub.substring("weekly_".length());
        } else if (sub.startsWith("monthly_")) {
            period = LeaderboardPeriod.MONTHLY;
            remaining = sub.substring("monthly_".length());
        } else if (sub.startsWith("alltime_")) {
            period = LeaderboardPeriod.ALL_TIME;
            remaining = sub.substring("alltime_".length());
        }

        int underscore = remaining.indexOf('_');
        if (underscore == -1) return null;

        int rank;
        try {
            rank = Integer.parseInt(remaining.substring(0, underscore));
        } catch (NumberFormatException e) {
            return null;
        }

        String field = remaining.substring(underscore + 1);
        LeaderboardEntry entry = plugin.getLeaderboardManager().getGlobalTop(period, rank);
        return formatEntryField(entry, field);
    }

    private @Nullable String handleJobLeaderboard(String sub) {
        if (plugin.getLeaderboardManager() == null) return "-";

        String[] parts = sub.split("_");
        if (parts.length < 3) return null;

        // Find the index of the rank (which is an integer)
        int rankIndex = -1;
        int rank = -1;
        for (int i = 0; i < parts.length; i++) {
            try {
                rank = Integer.parseInt(parts[i]);
                rankIndex = i;
                break;
            } catch (NumberFormatException ignored) {}
        }
        if (rankIndex == -1 || rank <= 0 || rankIndex == 0 || rankIndex == parts.length - 1) {
            return null;
        }

        // Everything before rankIndex is period + jobId
        LeaderboardPeriod period = LeaderboardPeriod.WEEKLY;
        List<String> jobTokens = new java.util.ArrayList<>();
        for (int i = 0; i < rankIndex; i++) {
            String token = parts[i];
            if (token.equalsIgnoreCase("weekly")) {
                period = LeaderboardPeriod.WEEKLY;
            } else if (token.equalsIgnoreCase("monthly")) {
                period = LeaderboardPeriod.MONTHLY;
            } else if (token.equalsIgnoreCase("alltime")) {
                period = LeaderboardPeriod.ALL_TIME;
            } else {
                jobTokens.add(token);
            }
        }
        String jobId = String.join("_", jobTokens);

        // Everything after rankIndex is field
        StringBuilder fieldBuilder = new StringBuilder();
        for (int i = rankIndex + 1; i < parts.length; i++) {
            if (fieldBuilder.length() > 0) fieldBuilder.append("_");
            fieldBuilder.append(parts[i]);
        }
        String field = fieldBuilder.toString();

        LeaderboardEntry entry = plugin.getLeaderboardManager().getJobTop(jobId, period, rank);
        return formatEntryField(entry, field);
    }

    private String formatEntryField(@Nullable LeaderboardEntry entry, String field) {
        if (entry == null) {
            return (field.equalsIgnoreCase("name") || field.equalsIgnoreCase("player") || field.equalsIgnoreCase("player_name"))
                    ? "-" : "0";
        }
        return switch (field.toLowerCase()) {
            case "name", "player", "player_name" -> entry.getPlayerName();
            case "xp" -> String.valueOf(entry.getXp());
            case "xp_formatted", "formatted_xp" -> String.format(java.util.Locale.US, "%,d", entry.getXp());
            case "orders" -> String.valueOf(entry.getOrdersCompleted());
            case "orders_formatted", "formatted_orders" -> String.format(java.util.Locale.US, "%,d", entry.getOrdersCompleted());
            case "level" -> String.valueOf(entry.getLevel());
            case "rank" -> String.valueOf(entry.getRank());
            default -> "-";
        };
    }
}
