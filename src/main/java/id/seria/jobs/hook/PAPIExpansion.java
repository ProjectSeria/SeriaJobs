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
        // Format: top_global_[period_]<pos>_<field>
        if (lower.startsWith("top_global_") && !lower.contains("player_rank")) {
            return handleGlobalLeaderboard(lower.substring("top_global_".length()));
        }

        // 3. Per-Job Leaderboard Top Entries (Server-wide)
        // Format: top_job_<job_id>_[period_]<pos>_<field>
        if (lower.startsWith("top_job_") && !lower.contains("player_rank")) {
            return handleJobLeaderboard(lower.substring("top_job_".length()));
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
        // Format: top_global_player_rank or top_global_player_rank_<period>
        if (lower.startsWith("top_global_player_rank")) {
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
        // Format: top_job_<job_id>_player_rank or top_job_<job_id>_player_rank_<period>
        if (lower.startsWith("top_job_") && lower.contains("_player_rank")) {
            String sub = lower.substring("top_job_".length());
            LeaderboardPeriod period = LeaderboardPeriod.WEEKLY;
            if (sub.endsWith("_weekly")) {
                period = LeaderboardPeriod.WEEKLY;
                sub = sub.substring(0, sub.length() - "_weekly".length());
            } else if (sub.endsWith("_monthly")) {
                period = LeaderboardPeriod.MONTHLY;
                sub = sub.substring(0, sub.length() - "_monthly".length());
            } else if (sub.endsWith("_alltime")) {
                period = LeaderboardPeriod.ALL_TIME;
                sub = sub.substring(0, sub.length() - "_alltime".length());
            }

            if (sub.endsWith("_player_rank")) {
                String jobId = sub.substring(0, sub.length() - "_player_rank".length());
                if (plugin.getLeaderboardManager() != null) {
                    int rank = plugin.getLeaderboardManager().getPlayerJobRank(player.getUniqueId(), jobId, period);
                    return rank == -1 ? "-" : String.valueOf(rank);
                }
            }
            return "-";
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

        int lastUnderscore = sub.lastIndexOf('_');
        if (lastUnderscore == -1) return null;
        String field = sub.substring(lastUnderscore + 1);
        String rem = sub.substring(0, lastUnderscore);

        int prevUnderscore = rem.lastIndexOf('_');
        if (prevUnderscore == -1) return null;

        int rank;
        try {
            rank = Integer.parseInt(rem.substring(prevUnderscore + 1));
        } catch (NumberFormatException e) {
            return null;
        }

        String jobPart = rem.substring(0, prevUnderscore);
        LeaderboardPeriod period = LeaderboardPeriod.WEEKLY;
        if (jobPart.endsWith("_weekly")) {
            period = LeaderboardPeriod.WEEKLY;
            jobPart = jobPart.substring(0, jobPart.length() - "_weekly".length());
        } else if (jobPart.endsWith("_monthly")) {
            period = LeaderboardPeriod.MONTHLY;
            jobPart = jobPart.substring(0, jobPart.length() - "_monthly".length());
        } else if (jobPart.endsWith("_alltime")) {
            period = LeaderboardPeriod.ALL_TIME;
            jobPart = jobPart.substring(0, jobPart.length() - "_alltime".length());
        }

        LeaderboardEntry entry = plugin.getLeaderboardManager().getJobTop(jobPart, period, rank);
        return formatEntryField(entry, field);
    }

    private String formatEntryField(@Nullable LeaderboardEntry entry, String field) {
        if (entry == null) {
            return field.equalsIgnoreCase("name") ? "-" : "0";
        }
        return switch (field.toLowerCase()) {
            case "name" -> entry.getPlayerName();
            case "xp" -> String.valueOf(entry.getXp());
            case "orders" -> String.valueOf(entry.getOrdersCompleted());
            case "level" -> String.valueOf(entry.getLevel());
            default -> "-";
        };
    }
}
