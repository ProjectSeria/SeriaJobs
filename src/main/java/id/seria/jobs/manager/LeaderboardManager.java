package id.seria.jobs.manager;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import id.seria.jobs.model.LeaderboardEntry;
import id.seria.jobs.model.LeaderboardPeriod;
import id.seria.jobs.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderboardManager {

    private final SeriaJobs plugin;

    // Leaderboard caches
    private final Map<String, List<LeaderboardEntry>> weeklyJobLeaderboards = new ConcurrentHashMap<>();
    private final Map<String, List<LeaderboardEntry>> monthlyJobLeaderboards = new ConcurrentHashMap<>();
    private final Map<String, List<LeaderboardEntry>> allTimeJobLeaderboards = new ConcurrentHashMap<>();

    private volatile List<LeaderboardEntry> weeklyGlobalLeaderboard = Collections.emptyList();
    private volatile List<LeaderboardEntry> monthlyGlobalLeaderboard = Collections.emptyList();
    private volatile List<LeaderboardEntry> allTimeGlobalLeaderboard = Collections.emptyList();

    private BukkitTask updateTask;
    private BukkitTask payoutCheckerTask;

    public LeaderboardManager(SeriaJobs plugin) {
        this.plugin = plugin;
    }

    public void init() {
        if (!plugin.getConfig().getBoolean("leaderboards.enabled", true)) {
            return;
        }

        // Run initial update asynchronously
        updateAllLeaderboards();

        // Periodic update schedule
        int intervalMinutes = Math.max(1, plugin.getConfig().getInt("leaderboards.update-interval-minutes", 5));
        long intervalTicks = intervalMinutes * 60L * 20L;
        updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateAllLeaderboards, intervalTicks, intervalTicks);

        // Check payouts every 30 seconds
        payoutCheckerTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkScheduledPayouts, 20L * 30L, 20L * 30L);
    }

    public void shutdown() {
        if (updateTask != null) updateTask.cancel();
        if (payoutCheckerTask != null) payoutCheckerTask.cancel();
    }

    public void updateAllLeaderboards() {
        plugin.getPlayerDataManager().runAsync(conn -> {
            try {
                // 1. Global Leaderboards
                this.weeklyGlobalLeaderboard = fetchGlobalLeaderboard(conn, LeaderboardPeriod.WEEKLY);
                this.monthlyGlobalLeaderboard = fetchGlobalLeaderboard(conn, LeaderboardPeriod.MONTHLY);
                this.allTimeGlobalLeaderboard = fetchGlobalLeaderboard(conn, LeaderboardPeriod.ALL_TIME);

                // 2. Per-Job Leaderboards
                for (Job job : plugin.getJobRegistry().getAllJobs()) {
                    String jobId = job.getId().toLowerCase();
                    weeklyJobLeaderboards.put(jobId, fetchJobLeaderboard(conn, jobId, LeaderboardPeriod.WEEKLY));
                    monthlyJobLeaderboards.put(jobId, fetchJobLeaderboard(conn, jobId, LeaderboardPeriod.MONTHLY));
                    allTimeJobLeaderboards.put(jobId, fetchJobLeaderboard(conn, jobId, LeaderboardPeriod.ALL_TIME));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to update leaderboards: " + e.getMessage());
            }
        });
    }

    private List<LeaderboardEntry> fetchGlobalLeaderboard(Connection conn, LeaderboardPeriod period) {
        List<LeaderboardEntry> list = new ArrayList<>();
        String query;
        if (period == LeaderboardPeriod.WEEKLY) {
            query = "SELECT s.uuid, p.last_name, SUM(s.weekly_xp) AS total_xp, SUM(s.weekly_orders) AS total_orders " +
                    "FROM seriajobs_leaderboard_stats s " +
                    "LEFT JOIN seriajobs_players p ON s.uuid = p.uuid " +
                    "GROUP BY s.uuid, p.last_name " +
                    "HAVING total_xp > 0 OR total_orders > 0 " +
                    "ORDER BY total_xp DESC, total_orders DESC " +
                    "LIMIT 50;";
        } else if (period == LeaderboardPeriod.MONTHLY) {
            query = "SELECT s.uuid, p.last_name, SUM(s.monthly_xp) AS total_xp, SUM(s.monthly_orders) AS total_orders " +
                    "FROM seriajobs_leaderboard_stats s " +
                    "LEFT JOIN seriajobs_players p ON s.uuid = p.uuid " +
                    "GROUP BY s.uuid, p.last_name " +
                    "HAVING total_xp > 0 OR total_orders > 0 " +
                    "ORDER BY total_xp DESC, total_orders DESC " +
                    "LIMIT 50;";
        } else {
            query = "SELECT j.uuid, p.last_name, SUM(j.level) AS total_level, SUM(j.xp) AS total_xp, " +
                    "COALESCE(SUM(s.alltime_orders), 0) AS total_orders " +
                    "FROM seriajobs_player_jobs j " +
                    "LEFT JOIN seriajobs_players p ON j.uuid = p.uuid " +
                    "LEFT JOIN seriajobs_leaderboard_stats s ON j.uuid = s.uuid AND j.job_id = s.job_id " +
                    "GROUP BY j.uuid, p.last_name " +
                    "ORDER BY total_level DESC, total_xp DESC, total_orders DESC " +
                    "LIMIT 50;";
        }

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            int rank = 1;
            while (rs.next()) {
                String uuidStr = rs.getString("uuid");
                String name = rs.getString("last_name");
                if (name == null || name.isBlank()) name = "Unknown";
                UUID uuid = UUID.fromString(uuidStr);

                if (period == LeaderboardPeriod.ALL_TIME) {
                    int lvl = rs.getInt("total_level");
                    int xp = rs.getInt("total_xp");
                    int orders = rs.getInt("total_orders");
                    list.add(new LeaderboardEntry(uuid, name, rank++, lvl, lvl, xp, orders));
                } else {
                    int xp = rs.getInt("total_xp");
                    int orders = rs.getInt("total_orders");
                    list.add(new LeaderboardEntry(uuid, name, rank++, xp, 0, xp, orders));
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error querying global leaderboard (" + period + "): " + e.getMessage());
        }
        return Collections.unmodifiableList(list);
    }

    private List<LeaderboardEntry> fetchJobLeaderboard(Connection conn, String jobId, LeaderboardPeriod period) {
        List<LeaderboardEntry> list = new ArrayList<>();
        String query;
        if (period == LeaderboardPeriod.WEEKLY) {
            query = "SELECT s.uuid, p.last_name, s.weekly_xp AS xp, s.weekly_orders AS orders, COALESCE(j.level, 1) AS level " +
                    "FROM seriajobs_leaderboard_stats s " +
                    "LEFT JOIN seriajobs_players p ON s.uuid = p.uuid " +
                    "LEFT JOIN seriajobs_player_jobs j ON s.uuid = j.uuid AND s.job_id = j.job_id " +
                    "WHERE s.job_id = ? AND (s.weekly_xp > 0 OR s.weekly_orders > 0) " +
                    "ORDER BY s.weekly_xp DESC, s.weekly_orders DESC " +
                    "LIMIT 50;";
        } else if (period == LeaderboardPeriod.MONTHLY) {
            query = "SELECT s.uuid, p.last_name, s.monthly_xp AS xp, s.monthly_orders AS orders, COALESCE(j.level, 1) AS level " +
                    "FROM seriajobs_leaderboard_stats s " +
                    "LEFT JOIN seriajobs_players p ON s.uuid = p.uuid " +
                    "LEFT JOIN seriajobs_player_jobs j ON s.uuid = j.uuid AND s.job_id = j.job_id " +
                    "WHERE s.job_id = ? AND (s.monthly_xp > 0 OR s.monthly_orders > 0) " +
                    "ORDER BY s.monthly_xp DESC, s.monthly_orders DESC " +
                    "LIMIT 50;";
        } else {
            query = "SELECT j.uuid, p.last_name, j.level, j.xp, COALESCE(s.alltime_orders, 0) AS orders " +
                    "FROM seriajobs_player_jobs j " +
                    "LEFT JOIN seriajobs_players p ON j.uuid = p.uuid " +
                    "LEFT JOIN seriajobs_leaderboard_stats s ON j.uuid = s.uuid AND j.job_id = s.job_id " +
                    "WHERE j.job_id = ? " +
                    "ORDER BY j.level DESC, j.xp DESC, orders DESC " +
                    "LIMIT 50;";
        }

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    String uuidStr = rs.getString("uuid");
                    String name = rs.getString("last_name");
                    if (name == null || name.isBlank()) name = "Unknown";
                    UUID uuid = UUID.fromString(uuidStr);

                    int lvl = rs.getInt("level");
                    int xp = rs.getInt("xp");
                    int orders = rs.getInt("orders");
                    int score = period == LeaderboardPeriod.ALL_TIME ? lvl : xp;

                    list.add(new LeaderboardEntry(uuid, name, rank++, score, lvl, xp, orders));
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error querying job leaderboard (" + jobId + " " + period + "): " + e.getMessage());
        }
        return Collections.unmodifiableList(list);
    }

    // =========================================================================
    // Scheduled Payout System with Reset
    // =========================================================================

    public void checkScheduledPayouts() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Weekly Check
        if (plugin.getConfig().getBoolean("leaderboards.weekly.enabled", true)) {
            String targetDay = plugin.getConfig().getString("leaderboards.weekly.day-of-week", "SUNDAY").toUpperCase();
            String targetTime = plugin.getConfig().getString("leaderboards.weekly.time", "23:59");
            if (isTimeMatch(now, DayOfWeek.valueOf(targetDay), targetTime)) {
                String weekKey = now.get(IsoFields.WEEK_BASED_YEAR) + "-W" + now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                checkAndExecutePayout("weekly", weekKey, () -> executePayout(LeaderboardPeriod.WEEKLY));
            }
        }

        // 2. Monthly Check
        if (plugin.getConfig().getBoolean("leaderboards.monthly.enabled", true)) {
            String dayStr = plugin.getConfig().getString("leaderboards.monthly.day-of-month", "LAST").toUpperCase();
            String targetTime = plugin.getConfig().getString("leaderboards.monthly.time", "23:59");
            boolean isDay = false;
            if ("LAST".equalsIgnoreCase(dayStr)) {
                isDay = (now.getDayOfMonth() == now.toLocalDate().lengthOfMonth());
            } else {
                try {
                    int day = Integer.parseInt(dayStr);
                    isDay = (now.getDayOfMonth() == day);
                } catch (NumberFormatException ignored) {}
            }

            if (isDay && isHourMinuteMatch(now, targetTime)) {
                String monthKey = YearMonth.from(now).toString();
                checkAndExecutePayout("monthly", monthKey, () -> executePayout(LeaderboardPeriod.MONTHLY));
            }
        }
    }

    private boolean isTimeMatch(LocalDateTime now, DayOfWeek targetDay, String targetTime) {
        if (now.getDayOfWeek() != targetDay) return false;
        return isHourMinuteMatch(now, targetTime);
    }

    private boolean isHourMinuteMatch(LocalDateTime now, String targetTime) {
        String[] split = targetTime.split(":");
        if (split.length < 2) return false;
        try {
            int targetHour = Integer.parseInt(split[0].trim());
            int targetMinute = Integer.parseInt(split[1].trim());
            return now.getHour() == targetHour && now.getMinute() == targetMinute;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void checkAndExecutePayout(String payoutType, String cycleKey, Runnable payoutAction) {
        plugin.getPlayerDataManager().runAsync(conn -> {
            try {
                String metaKey = "last_payout_" + payoutType;
                String lastCycle = null;
                try (PreparedStatement ps = conn.prepareStatement("SELECT meta_value FROM seriajobs_leaderboard_meta WHERE meta_key = ?")) {
                    ps.setString(1, metaKey);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            lastCycle = rs.getString("meta_value");
                        }
                    }
                }

                if (cycleKey.equals(lastCycle)) {
                    return; // Already paid out this cycle
                }

                // Update meta to prevent duplicate payouts
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO seriajobs_leaderboard_meta (meta_key, meta_value) VALUES (?, ?) " +
                                "ON CONFLICT(meta_key) DO UPDATE SET meta_value = ?;")) {
                    ps.setString(1, metaKey);
                    ps.setString(2, cycleKey);
                    ps.setString(3, cycleKey);
                    ps.executeUpdate();
                } catch (SQLException sqliteFail) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO seriajobs_leaderboard_meta (meta_key, meta_value) VALUES (?, ?) " +
                                    "ON DUPLICATE KEY UPDATE meta_value = ?;")) {
                        ps.setString(1, metaKey);
                        ps.setString(2, cycleKey);
                        ps.setString(3, cycleKey);
                        ps.executeUpdate();
                    } catch (SQLException ignored) {}
                }

                // Run the payout on main thread
                Bukkit.getScheduler().runTask(plugin, payoutAction);

            } catch (Exception e) {
                plugin.getLogger().warning("Error checking payout for " + payoutType + ": " + e.getMessage());
            }
        });
    }

    public void executePayout(LeaderboardPeriod period) {
        String periodName = period == LeaderboardPeriod.WEEKLY ? "Mingguan" : "Bulanan";
        String sectionKey = period == LeaderboardPeriod.WEEKLY ? "leaderboards.weekly" : "leaderboards.monthly";

        plugin.getLogger().info("[SeriaJobs] Starting leaderboard payout and reset for " + periodName + "...");

        // 1. Payout Global Rewards
        ConfigurationSection globalRewardsSec = plugin.getConfig().getConfigurationSection(sectionKey + ".global-rewards");
        List<LeaderboardEntry> globalList = period == LeaderboardPeriod.WEEKLY ? weeklyGlobalLeaderboard : monthlyGlobalLeaderboard;

        if (globalRewardsSec != null && !globalList.isEmpty()) {
            for (String rankKey : globalRewardsSec.getKeys(false)) {
                try {
                    int rank = Integer.parseInt(rankKey);
                    if (rank <= globalList.size()) {
                        LeaderboardEntry winner = globalList.get(rank - 1);
                        List<Map<?, ?>> rewardsList = globalRewardsSec.getMapList(rankKey);
                        deliverRewards(winner.getUuid(), winner.getPlayerName(), rewardsList, "Juara " + rank + " Global " + periodName);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        // 2. Payout Per-Job Rewards
        ConfigurationSection jobRewardsSec = plugin.getConfig().getConfigurationSection(sectionKey + ".per-job-rewards");
        Map<String, List<LeaderboardEntry>> jobMap = period == LeaderboardPeriod.WEEKLY ? weeklyJobLeaderboards : monthlyJobLeaderboards;

        if (jobRewardsSec != null) {
            for (Map.Entry<String, List<LeaderboardEntry>> entry : jobMap.entrySet()) {
                String jobId = entry.getKey();
                List<LeaderboardEntry> jobEntries = entry.getValue();
                if (jobEntries.isEmpty()) continue;

                Job jobObj = plugin.getJobRegistry().getJob(jobId);
                String jobName = jobObj != null ? jobObj.getDisplayName() : jobId;

                for (String rankKey : jobRewardsSec.getKeys(false)) {
                    try {
                        int rank = Integer.parseInt(rankKey);
                        if (rank <= jobEntries.size()) {
                            LeaderboardEntry winner = jobEntries.get(rank - 1);
                            List<Map<?, ?>> rewardsList = jobRewardsSec.getMapList(rankKey);
                            deliverRewards(winner.getUuid(), winner.getPlayerName(), rewardsList, "Juara " + rank + " Job " + jobName + " (" + periodName + ")");
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // 3. Broadcast winner announcement
        if (plugin.getConfig().getBoolean(sectionKey + ".broadcast", true)) {
            Bukkit.broadcast(TextUtil.parse("<gradient:#f1c40f:#e67e22><bold>[SeriaJobs]</bold></gradient> " +
                    "<yellow>Pembagian Hadiah & Reset Leaderboard " + periodName + " telah selesai dilaksanakan!</yellow>"));
            if (!globalList.isEmpty()) {
                LeaderboardEntry top1 = globalList.get(0);
                Bukkit.broadcast(TextUtil.parse("<gray>Selamat kepada <yellow>" + top1.getPlayerName() + "</yellow> sebagai Juara 1 Global!</gray>"));
            }
        }

        // 4. RESET DATABASE STATS FOR THIS PERIOD
        plugin.getPlayerDataManager().runAsync(conn -> {
            try {
                String resetSql = period == LeaderboardPeriod.WEEKLY
                        ? "UPDATE seriajobs_leaderboard_stats SET weekly_xp = 0, weekly_orders = 0;"
                        : "UPDATE seriajobs_leaderboard_stats SET monthly_xp = 0, monthly_orders = 0;";

                try (PreparedStatement ps = conn.prepareStatement(resetSql)) {
                    ps.executeUpdate();
                }

                // Immediately refresh in-memory cache
                updateAllLeaderboards();
                plugin.getLogger().info("[SeriaJobs] Successfully reset " + periodName + " stats and refreshed leaderboards.");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to reset " + periodName + " leaderboard stats: " + e.getMessage());
            }
        });
    }

    private void deliverRewards(UUID uuid, String playerName, List<Map<?, ?>> rawRewards, String reason) {
        Player player = Bukkit.getPlayer(uuid);
        for (Map<?, ?> rMap : rawRewards) {
            String type = String.valueOf(rMap.get("type")).toUpperCase();
            double amount = 0;
            if (rMap.containsKey("amount")) {
                try {
                    amount = Double.parseDouble(String.valueOf(rMap.get("amount")));
                } catch (NumberFormatException ignored) {}
            }

            String extraData = "";
            if (rMap.containsKey("command")) {
                extraData = String.valueOf(rMap.get("command"));
            } else if (rMap.containsKey("currency")) {
                extraData = String.valueOf(rMap.get("currency"));
            } else if (rMap.containsKey("material")) {
                extraData = String.valueOf(rMap.get("material"));
            }

            if (player != null && player.isOnline()) {
                // Deliver directly online
                if ("VAULT".equals(type)) {
                    if (plugin.getHookManager().isVaultLoaded()) {
                        plugin.getHookManager().getVaultHook().deposit(player, amount);
                    } else if (plugin.getHookManager().isExcellentEconomyLoaded()) {
                        plugin.getHookManager().getExcellentEconomyHook().deposit(player, null, amount);
                    }
                } else if ("EXCELLENT_ECONOMY".equals(type) || "CURRENCY".equals(type)) {
                    if (plugin.getHookManager().isExcellentEconomyLoaded()) {
                        plugin.getHookManager().getExcellentEconomyHook().deposit(player, extraData, amount);
                    } else if (plugin.getHookManager().isVaultLoaded()) {
                        plugin.getHookManager().getVaultHook().deposit(player, amount);
                    }
                } else if ("COMMAND".equals(type)) {
                    String cmd = extraData.replace("%player%", player.getName()).replace("{player}", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
                player.sendMessage(TextUtil.parseForPlayer(player,
                        "<gold>[SeriaJobs]</gold> <green>Selamat! Anda menerima hadiah untuk <yellow>" + reason + "</yellow>!</green>"));
            } else {
                // Save pending reward for offline player
                plugin.getPlayerDataManager().addPendingReward(uuid, type, amount, extraData, reason);
            }
        }
    }

    public void checkAndDeliverPendingRewards(Player player) {
        plugin.getPlayerDataManager().claimPendingRewards(player);
    }

    public void recordOrderCompletion(UUID uuid, String jobId) {
        plugin.getPlayerDataManager().recordLeaderboardOrderCompletion(uuid, jobId);
    }

    // =========================================================================
    // Accessors for PAPI & Menus
    // =========================================================================

    @Nullable
    public LeaderboardEntry getGlobalTop(LeaderboardPeriod period, int rank) {
        List<LeaderboardEntry> list = period == LeaderboardPeriod.WEEKLY
                ? weeklyGlobalLeaderboard
                : (period == LeaderboardPeriod.MONTHLY ? monthlyGlobalLeaderboard : allTimeGlobalLeaderboard);

        if (rank >= 1 && rank <= list.size()) {
            return list.get(rank - 1);
        }
        return null;
    }

    @Nullable
    public LeaderboardEntry getJobTop(String jobId, LeaderboardPeriod period, int rank) {
        if (jobId == null) return null;
        Map<String, List<LeaderboardEntry>> map = period == LeaderboardPeriod.WEEKLY
                ? weeklyJobLeaderboards
                : (period == LeaderboardPeriod.MONTHLY ? monthlyJobLeaderboards : allTimeJobLeaderboards);

        List<LeaderboardEntry> list = map.get(jobId.toLowerCase());
        if (list != null && rank >= 1 && rank <= list.size()) {
            return list.get(rank - 1);
        }
        return null;
    }

    public int getPlayerGlobalRank(UUID uuid, LeaderboardPeriod period) {
        List<LeaderboardEntry> list = period == LeaderboardPeriod.WEEKLY
                ? weeklyGlobalLeaderboard
                : (period == LeaderboardPeriod.MONTHLY ? monthlyGlobalLeaderboard : allTimeGlobalLeaderboard);

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUuid().equals(uuid)) {
                return i + 1;
            }
        }
        return -1;
    }

    public int getPlayerJobRank(UUID uuid, String jobId, LeaderboardPeriod period) {
        if (jobId == null) return -1;
        Map<String, List<LeaderboardEntry>> map = period == LeaderboardPeriod.WEEKLY
                ? weeklyJobLeaderboards
                : (period == LeaderboardPeriod.MONTHLY ? monthlyJobLeaderboards : allTimeJobLeaderboards);

        List<LeaderboardEntry> list = map.get(jobId.toLowerCase());
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getUuid().equals(uuid)) {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    public String getWeeklyCountdown() {
        LocalDateTime now = LocalDateTime.now();
        String targetDayStr = plugin.getConfig().getString("leaderboards.weekly.day-of-week", "SUNDAY").toUpperCase();
        DayOfWeek targetDay = DayOfWeek.valueOf(targetDayStr);

        LocalDate nextDate = now.toLocalDate().with(TemporalAdjusters.nextOrSame(targetDay));
        String targetTime = plugin.getConfig().getString("leaderboards.weekly.time", "23:59");
        String[] split = targetTime.split(":");
        int hour = split.length > 0 ? Integer.parseInt(split[0].trim()) : 23;
        int minute = split.length > 1 ? Integer.parseInt(split[1].trim()) : 59;

        LocalDateTime target = LocalDateTime.of(nextDate, LocalTime.of(hour, minute));
        if (now.isAfter(target)) {
            target = target.plusWeeks(1);
        }

        Duration duration = Duration.between(now, target);
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        return days + "d " + hours + "h " + minutes + "m";
    }

    public String getMonthlyCountdown() {
        LocalDateTime now = LocalDateTime.now();
        String dayStr = plugin.getConfig().getString("leaderboards.monthly.day-of-month", "LAST").toUpperCase();

        LocalDate targetDate;
        if ("LAST".equalsIgnoreCase(dayStr)) {
            targetDate = now.toLocalDate().with(TemporalAdjusters.lastDayOfMonth());
        } else {
            int d = Integer.parseInt(dayStr);
            targetDate = now.toLocalDate().withDayOfMonth(Math.min(d, now.toLocalDate().lengthOfMonth()));
        }

        String targetTime = plugin.getConfig().getString("leaderboards.monthly.time", "23:59");
        String[] split = targetTime.split(":");
        int hour = split.length > 0 ? Integer.parseInt(split[0].trim()) : 23;
        int minute = split.length > 1 ? Integer.parseInt(split[1].trim()) : 59;

        LocalDateTime target = LocalDateTime.of(targetDate, LocalTime.of(hour, minute));
        if (now.isAfter(target)) {
            LocalDate nextMonth = now.toLocalDate().plusMonths(1);
            if ("LAST".equalsIgnoreCase(dayStr)) {
                targetDate = nextMonth.with(TemporalAdjusters.lastDayOfMonth());
            } else {
                int d = Integer.parseInt(dayStr);
                targetDate = nextMonth.withDayOfMonth(Math.min(d, nextMonth.lengthOfMonth()));
            }
            target = LocalDateTime.of(targetDate, LocalTime.of(hour, minute));
        }

        Duration duration = Duration.between(now, target);
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        return days + "d " + hours + "h " + minutes + "m";
    }
}
