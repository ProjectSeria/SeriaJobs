package id.seria.jobs.manager;

import id.seria.core.SeriaCorePlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import id.seria.jobs.SeriaJobs;
import id.seria.jobs.api.JobLevelUpEvent;
import id.seria.jobs.model.Job;
import id.seria.jobs.model.JobOrder;
import id.seria.jobs.model.JobProfile;
import id.seria.jobs.model.WorkOrder;
import id.seria.jobs.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PlayerDataManager {

    private final SeriaJobs plugin;
    private final Map<UUID, JobProfile> profileCache = new ConcurrentHashMap<>();
    private final ExecutorService dbExecutor;
    private HikariDataSource dataSource;
    private boolean useSeriaCoreDb = false;

    public PlayerDataManager(SeriaJobs plugin) {
        this.plugin = plugin;
        this.dbExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "SeriaJobs-DB-Worker");
            t.setDaemon(true);
            return t;
        });
    }

    public void init() {
        if (plugin.getConfigManager().isHookEnabled("seriacore.use-central-database")
                && SeriaCorePlugin.getInstance().getPlayerDataManager().getConnection() != null) {
            useSeriaCoreDb = true;
            plugin.getLogger().info("Using SeriaCore centralized database connection.");
        } else {
            initHikari();
        }
        createTables();
    }

    private void initHikari() {
        try {
            HikariConfig config = new HikariConfig();
            String storageType = plugin.getConfigManager().getStorageType();

            if ("MYSQL".equalsIgnoreCase(storageType)) {
                config.setJdbcUrl("jdbc:mysql://" + plugin.getConfigManager().getMySqlHost() + ":"
                        + plugin.getConfigManager().getMySqlPort() + "/" + plugin.getConfigManager().getMySqlDatabase()
                        + "?useSSL=false&characterEncoding=utf8&autoReconnect=true");
                config.setUsername(plugin.getConfigManager().getMySqlUser());
                config.setPassword(plugin.getConfigManager().getMySqlPassword());
                config.setMaximumPoolSize(plugin.getConfigManager().getMySqlPoolSize());
            } else {
                File dbFile = new File(plugin.getDataFolder(), "player_data.db");
                config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
                config.setDriverClassName("org.sqlite.JDBC");
                config.setMaximumPoolSize(1);
                config.addDataSourceProperty("journal_mode", "WAL");
                config.addDataSourceProperty("synchronous", "NORMAL");
            }

            config.setPoolName("SeriaJobs-Pool");
            config.setConnectionTimeout(30000);
            dataSource = new HikariDataSource(config);
            plugin.getLogger().info("Initialized internal HikariCP data pool for " + storageType);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        if (useSeriaCoreDb) {
            Connection conn = SeriaCorePlugin.getInstance().getPlayerDataManager().getConnection();
            if (conn != null && !conn.isClosed()) {
                return conn;
            }
        }
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        throw new SQLException("No active database connection available!");
    }

    public void runAsync(Consumer<Connection> action) {
        if (useSeriaCoreDb) {
            SeriaCorePlugin.getInstance().getPlayerDataManager().runAsync(action);
            return;
        }

        if (dbExecutor.isShutdown()) {
            return;
        }

        dbExecutor.submit(() -> {
            try (Connection conn = getConnection()) {
                action.accept(conn);
            } catch (Exception e) {
                plugin.getLogger().warning("Error running async database task: " + e.getMessage());
            }
        });
    }

    private void createTables() {
        runAsync(conn -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS seriajobs_players (" +
                        "uuid VARCHAR(36) PRIMARY KEY, " +
                        "last_name VARCHAR(64), " +
                        "orders_completed INT DEFAULT 0, " +
                        "updated_at BIGINT);");

                try {
                    stmt.execute("ALTER TABLE seriajobs_players ADD COLUMN orders_completed INT DEFAULT 0;");
                } catch (SQLException ignored) {}

                stmt.execute("CREATE TABLE IF NOT EXISTS seriajobs_player_jobs (" +
                        "uuid VARCHAR(36), " +
                        "job_id VARCHAR(64), " +
                        "is_active INT DEFAULT 0, " +
                        "level INT DEFAULT 1, " +
                        "xp INT DEFAULT 0, " +
                        "cooldown_until BIGINT DEFAULT 0, " +
                        "PRIMARY KEY (uuid, job_id));");

                stmt.execute("CREATE TABLE IF NOT EXISTS seriajobs_order_progress (" +
                        "uuid VARCHAR(36), " +
                        "shift_id VARCHAR(64), " +
                        "job_id VARCHAR(64), " +
                        "order_id VARCHAR(64), " +
                        "progress INT DEFAULT 0, " +
                        "is_completed INT DEFAULT 0, " +
                        "PRIMARY KEY (uuid, shift_id, job_id, order_id));");

                stmt.execute("CREATE TABLE IF NOT EXISTS seriajobs_leaderboard_stats (" +
                        "uuid VARCHAR(36), " +
                        "job_id VARCHAR(64), " +
                        "weekly_xp INT DEFAULT 0, " +
                        "monthly_xp INT DEFAULT 0, " +
                        "alltime_xp BIGINT DEFAULT 0, " +
                        "weekly_orders INT DEFAULT 0, " +
                        "monthly_orders INT DEFAULT 0, " +
                        "alltime_orders INT DEFAULT 0, " +
                        "PRIMARY KEY (uuid, job_id));");

                stmt.execute("CREATE TABLE IF NOT EXISTS seriajobs_pending_rewards (" +
                        "id VARCHAR(64) PRIMARY KEY, " +
                        "uuid VARCHAR(36), " +
                        "reward_type VARCHAR(64), " +
                        "amount DOUBLE, " +
                        "extra_data TEXT, " +
                        "reason VARCHAR(128), " +
                        "created_at BIGINT);");

                stmt.execute("CREATE TABLE IF NOT EXISTS seriajobs_leaderboard_meta (" +
                        "meta_key VARCHAR(64) PRIMARY KEY, " +
                        "meta_value VARCHAR(128));");

                stmt.execute("CREATE TABLE IF NOT EXISTS seriajobs_player_shifts (" +
                        "uuid VARCHAR(36), " +
                        "shift_id VARCHAR(64), " +
                        "reroll_count INT DEFAULT 0, " +
                        "PRIMARY KEY (uuid, shift_id));");

                stmt.execute("CREATE TABLE IF NOT EXISTS seriajobs_slot_orders (" +
                        "uuid VARCHAR(36), " +
                        "shift_id VARCHAR(64), " +
                        "job_id VARCHAR(64), " +
                        "slot_index INT, " +
                        "order_id VARCHAR(64), " +
                        "generated_reputation INT DEFAULT 0, " +
                        "is_completed INT DEFAULT 0, " +
                        "PRIMARY KEY (uuid, shift_id, job_id, slot_index));");
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to create tables: " + e.getMessage());
            }
        });
    }

    public JobProfile getProfile(UUID uuid) {
        return profileCache.computeIfAbsent(uuid, JobProfile::new);
    }

    public void loadProfileAsync(UUID uuid, String name, Consumer<JobProfile> callback) {
        JobProfile profile = getProfile(uuid);
        runAsync(conn -> {
            try {
                // Update player last known name
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO seriajobs_players (uuid, last_name, updated_at) VALUES (?, ?, ?) " +
                                "ON CONFLICT(uuid) DO UPDATE SET last_name = ?, updated_at = ?;")) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, name);
                    ps.setLong(3, System.currentTimeMillis());
                    ps.setString(4, name);
                    ps.setLong(5, System.currentTimeMillis());
                    ps.executeUpdate();
                } catch (SQLException fallbackSqlite) {
                    // Try mysql fallback if ON CONFLICT syntax differs
                    try (PreparedStatement psMySql = conn.prepareStatement(
                            "INSERT INTO seriajobs_players (uuid, last_name, updated_at) VALUES (?, ?, ?) " +
                                    "ON DUPLICATE KEY UPDATE last_name = ?, updated_at = ?;")) {
                        psMySql.setString(1, uuid.toString());
                        psMySql.setString(2, name);
                        psMySql.setLong(3, System.currentTimeMillis());
                        psMySql.setString(4, name);
                        psMySql.setLong(5, System.currentTimeMillis());
                        psMySql.executeUpdate();
                    } catch (SQLException ignored) {}
                }

                // Load jobs
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM seriajobs_player_jobs WHERE uuid = ?")) {
                    ps.setString(1, uuid.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String jobId = rs.getString("job_id");
                            int isActive = rs.getInt("is_active");
                            int level = rs.getInt("level");
                            int xp = rs.getInt("xp");
                            long cooldown = rs.getLong("cooldown_until");

                            if (isActive == 1) {
                                profile.addActiveJob(jobId);
                            }
                            profile.setJobLevel(jobId, level);
                            profile.setJobXp(jobId, xp);
                            if (cooldown > 0) {
                                profile.setLeaveCooldown(jobId, cooldown);
                            }
                        }
                    }
                }

                // Load current shift order progress
                String currentShift = plugin.getOrderPoolManager().getCurrentShiftId();
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM seriajobs_order_progress WHERE uuid = ? AND shift_id = ?")) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, currentShift);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String jobId = rs.getString("job_id");
                            String orderId = rs.getString("order_id");
                            int progress = rs.getInt("progress");
                            int isCompleted = rs.getInt("is_completed");

                            profile.setOrderProgress(currentShift, jobId, orderId, progress);
                            if (isCompleted == 1) {
                                profile.markOrderCompleted(currentShift, jobId, orderId);
                            }
                        }
                    }
                }

                // Load shift reroll count
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT reroll_count FROM seriajobs_player_shifts WHERE uuid = ? AND shift_id = ?")) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, currentShift);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            profile.setRerollCount(rs.getInt("reroll_count"));
                        } else {
                            profile.setRerollCount(0);
                        }
                    }
                }

                // Load slot orders
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM seriajobs_slot_orders WHERE uuid = ? AND shift_id = ?")) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, currentShift);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int slot = rs.getInt("slot_index");
                            String jobId = rs.getString("job_id");
                            String orderId = rs.getString("order_id");
                            int rep = rs.getInt("generated_reputation");
                            int completed = rs.getInt("is_completed");

                            Job job = plugin.getJobRegistry().getJob(jobId);
                            if (job != null) {
                                JobOrder jo = job.getOrder(orderId);
                                if (jo != null) {
                                    int level = profile.getJobLevel(jobId);
                                    WorkOrder wo = plugin.getOrderGenerationService().generateOrder(jo, level);
                                    wo.setCompleted(completed == 1);
                                    profile.setActiveOrder(slot, wo);
                                }
                            }
                        }
                    }
                }
                // Load total completed orders
                int totalOrders = 0;
                try (PreparedStatement ps = conn.prepareStatement("SELECT orders_completed FROM seriajobs_players WHERE uuid = ?")) {
                    ps.setString(1, uuid.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            totalOrders = rs.getInt("orders_completed");
                        }
                    }
                } catch (Exception ignored) {}

                if (totalOrders == 0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT COALESCE(SUM(alltime_orders), 0) AS lb_orders FROM seriajobs_leaderboard_stats WHERE uuid = ?")) {
                        ps.setString(1, uuid.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                totalOrders = Math.max(totalOrders, rs.getInt("lb_orders"));
                            }
                        }
                    } catch (Exception ignored) {}
                }

                if (totalOrders == 0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT COUNT(*) AS prog_orders FROM seriajobs_order_progress WHERE uuid = ? AND is_completed = 1")) {
                        ps.setString(1, uuid.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                totalOrders = Math.max(totalOrders, rs.getInt("prog_orders"));
                            }
                        }
                    } catch (Exception ignored) {}
                }

                profile.setOrdersCompleted(totalOrders);
            } catch (SQLException e) {
                plugin.getLogger().warning("Error loading data for player " + uuid + ": " + e.getMessage());
            }

            if (callback != null) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(profile));
            }
        });
    }

    public void saveProfileAsync(JobProfile profile) {
        runAsync(conn -> saveProfileDirect(conn, profile));
    }

    public void saveProfileSync(JobProfile profile) {
        try (Connection conn = getConnection()) {
            saveProfileDirect(conn, profile);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save profile synchronously: " + e.getMessage());
        }
    }

    private void saveProfileDirect(Connection conn, JobProfile profile) {
        String uuidStr = profile.getUuid().toString();
        try {
            // Save active and inactive jobs
            for (Map.Entry<String, Integer> entry : profile.getJobLevels().entrySet()) {
                String jobId = entry.getKey();
                int level = entry.getValue();
                int xp = profile.getJobXp(jobId);
                int isActive = profile.hasJob(jobId) ? 1 : 0;
                long cooldown = profile.getLeaveCooldown(jobId);

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO seriajobs_player_jobs (uuid, job_id, is_active, level, xp, cooldown_until) " +
                                "VALUES (?, ?, ?, ?, ?, ?) " +
                                "ON CONFLICT(uuid, job_id) DO UPDATE SET is_active = ?, level = ?, xp = ?, cooldown_until = ?;")) {
                    ps.setString(1, uuidStr);
                    ps.setString(2, jobId);
                    ps.setInt(3, isActive);
                    ps.setInt(4, level);
                    ps.setInt(5, xp);
                    ps.setLong(6, cooldown);

                    ps.setInt(7, isActive);
                    ps.setInt(8, level);
                    ps.setInt(9, xp);
                    ps.setLong(10, cooldown);
                    ps.executeUpdate();
                } catch (SQLException fallbackSqlite) {
                    try (PreparedStatement psMySql = conn.prepareStatement(
                            "INSERT INTO seriajobs_player_jobs (uuid, job_id, is_active, level, xp, cooldown_until) " +
                                    "VALUES (?, ?, ?, ?, ?, ?) " +
                                    "ON DUPLICATE KEY UPDATE is_active = ?, level = ?, xp = ?, cooldown_until = ?;")) {
                        psMySql.setString(1, uuidStr);
                        psMySql.setString(2, jobId);
                        psMySql.setInt(3, isActive);
                        psMySql.setInt(4, level);
                        psMySql.setInt(5, xp);
                        psMySql.setLong(6, cooldown);

                        psMySql.setInt(7, isActive);
                        psMySql.setInt(8, level);
                        psMySql.setInt(9, xp);
                        psMySql.setLong(10, cooldown);
                        psMySql.executeUpdate();
                    } catch (SQLException ignored) {}
                }
            }

            // Save order progress
            String shiftId = plugin.getOrderPoolManager().getCurrentShiftId();
            for (Map.Entry<String, Integer> entry : profile.getOrderProgressMap().entrySet()) {
                String[] parts = entry.getKey().split(":");
                if (parts.length < 3) continue;
                String orderShift = parts[0];
                String jobId = parts[1];
                String orderId = parts[2];
                int progress = entry.getValue();
                int isCompleted = profile.isOrderCompleted(orderShift, jobId, orderId) ? 1 : 0;

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO seriajobs_order_progress (uuid, shift_id, job_id, order_id, progress, is_completed) " +
                                "VALUES (?, ?, ?, ?, ?, ?) " +
                                "ON CONFLICT(uuid, shift_id, job_id, order_id) DO UPDATE SET progress = ?, is_completed = ?;")) {
                    ps.setString(1, uuidStr);
                    ps.setString(2, orderShift);
                    ps.setString(3, jobId);
                    ps.setString(4, orderId);
                    ps.setInt(5, progress);
                    ps.setInt(6, isCompleted);

                    ps.setInt(7, progress);
                    ps.setInt(8, isCompleted);
                    ps.executeUpdate();
                } catch (SQLException fallbackSqlite) {
                    try (PreparedStatement psMySql = conn.prepareStatement(
                            "INSERT INTO seriajobs_order_progress (uuid, shift_id, job_id, order_id, progress, is_completed) " +
                                    "VALUES (?, ?, ?, ?, ?, ?) " +
                                    "ON DUPLICATE KEY UPDATE progress = ?, is_completed = ?;")) {
                        psMySql.setString(1, uuidStr);
                        psMySql.setString(2, orderShift);
                        psMySql.setString(3, jobId);
                        psMySql.setString(4, orderId);
                        psMySql.setInt(5, progress);
                        psMySql.setInt(6, isCompleted);

                        psMySql.setInt(7, progress);
                        psMySql.setInt(8, isCompleted);
                        psMySql.executeUpdate();
                    } catch (SQLException ignored) {}
                }
            }

            // Save shift reroll count
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO seriajobs_player_shifts (uuid, shift_id, reroll_count) VALUES (?, ?, ?) " +
                            "ON CONFLICT(uuid, shift_id) DO UPDATE SET reroll_count = ?;")) {
                ps.setString(1, uuidStr);
                ps.setString(2, shiftId);
                ps.setInt(3, profile.getRerollCount());
                ps.setInt(4, profile.getRerollCount());
                ps.executeUpdate();
            } catch (SQLException fallbackSqlite) {
                try (PreparedStatement psMySql = conn.prepareStatement(
                        "INSERT INTO seriajobs_player_shifts (uuid, shift_id, reroll_count) VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE reroll_count = ?;")) {
                    psMySql.setString(1, uuidStr);
                    psMySql.setString(2, shiftId);
                    psMySql.setInt(3, profile.getRerollCount());
                    psMySql.setInt(4, profile.getRerollCount());
                    psMySql.executeUpdate();
                } catch (SQLException ignored) {}
            }

            // Save slot orders
            for (Map.Entry<Integer, WorkOrder> entry : profile.getActiveOrders().entrySet()) {
                int slot = entry.getKey();
                WorkOrder wo = entry.getValue();
                if (wo == null) continue;
                String jobId = wo.getTemplate().getOriginalJobOrder() != null
                        ? wo.getTemplate().getOriginalJobOrder().getJobId()
                        : (wo.getTemplate().getJobType() != null ? wo.getTemplate().getJobType().getId() : "default");
                String orderId = wo.getTemplate().getTemplateId();
                int rep = wo.getGeneratedReputationAmount();
                int completed = wo.isCompleted() ? 1 : 0;

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO seriajobs_slot_orders (uuid, shift_id, job_id, slot_index, order_id, generated_reputation, is_completed) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                                "ON CONFLICT(uuid, shift_id, job_id, slot_index) DO UPDATE SET order_id = ?, generated_reputation = ?, is_completed = ?;")) {
                    ps.setString(1, uuidStr);
                    ps.setString(2, shiftId);
                    ps.setString(3, jobId);
                    ps.setInt(4, slot);
                    ps.setString(5, orderId);
                    ps.setInt(6, rep);
                    ps.setInt(7, completed);

                    ps.setString(8, orderId);
                    ps.setInt(9, rep);
                    ps.setInt(10, completed);
                    ps.executeUpdate();
                } catch (SQLException fallbackSqlite) {
                    try (PreparedStatement psMySql = conn.prepareStatement(
                            "INSERT INTO seriajobs_slot_orders (uuid, shift_id, job_id, slot_index, order_id, generated_reputation, is_completed) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                                    "ON DUPLICATE KEY UPDATE order_id = ?, generated_reputation = ?, is_completed = ?;")) {
                        psMySql.setString(1, uuidStr);
                        psMySql.setString(2, shiftId);
                        psMySql.setString(3, jobId);
                        psMySql.setInt(4, slot);
                        psMySql.setString(5, orderId);
                        psMySql.setInt(6, rep);
                        psMySql.setInt(7, completed);

                        psMySql.setString(8, orderId);
                        psMySql.setInt(9, rep);
                        psMySql.setInt(10, completed);
                        psMySql.executeUpdate();
                    } catch (SQLException ignored) {}
                }
            }

            // Save total completed orders
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE seriajobs_players SET orders_completed = ? WHERE uuid = ?;")) {
                ps.setInt(1, profile.getOrdersCompleted());
                ps.setString(2, uuidStr);
                ps.executeUpdate();
            } catch (SQLException ignored) {}
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save player direct data: " + e.getMessage());
        }
    }

    public void addJobXp(Player player, String jobId, int amount) {
        Job job = plugin.getJobRegistry().getJob(jobId);
        if (job == null || player == null || amount <= 0) return;

        // Record XP in leaderboard stats regardless of level cap
        recordLeaderboardXpGain(player.getUniqueId(), jobId, amount);

        JobProfile profile = getProfile(player.getUniqueId());
        int oldLevel = profile.getJobLevel(jobId);
        if (oldLevel >= job.getMaxLevel()) {
            return;
        }

        int currentLevel = oldLevel;
        int currentXp = profile.getJobXp(jobId) + amount;
        int requiredXp = job.getRequiredXp(currentLevel);

        boolean leveledUp = false;
        while (currentXp >= requiredXp && currentLevel < job.getMaxLevel()) {
            currentXp -= requiredXp;
            currentLevel++;
            leveledUp = true;
            requiredXp = job.getRequiredXp(currentLevel);

            // Give milestone rewards for reached level
            if (job.hasRewardsForLevel(currentLevel)) {
                List<id.seria.jobs.model.reward.Reward> rewards = job.getRewardsForLevel(currentLevel);
                if (rewards != null) {
                    for (id.seria.jobs.model.reward.Reward r : rewards) {
                        r.apply(player, job);
                    }
                }
            }
        }

        profile.setJobLevel(jobId, currentLevel);
        profile.setJobXp(jobId, currentXp);
        saveProfileAsync(profile);

        if (leveledUp) {
            int finalLevel = currentLevel;
            Bukkit.getPluginManager().callEvent(new JobLevelUpEvent(player, job, oldLevel, finalLevel));
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "jobs.level-up",
                    "{job}", job.getDisplayName(),
                    "{level}", String.valueOf(finalLevel)));
        }
    }

    public void recordLeaderboardXpGain(UUID uuid, String jobId, int amount) {
        if (uuid == null || jobId == null || amount <= 0) return;
        runAsync(conn -> {
            try {
                // SQLite ON CONFLICT
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO seriajobs_leaderboard_stats (uuid, job_id, weekly_xp, monthly_xp, alltime_xp) " +
                                "VALUES (?, ?, ?, ?, ?) " +
                                "ON CONFLICT(uuid, job_id) DO UPDATE SET " +
                                "weekly_xp = weekly_xp + ?, monthly_xp = monthly_xp + ?, alltime_xp = alltime_xp + ?;")) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, jobId.toLowerCase());
                    ps.setInt(3, amount);
                    ps.setInt(4, amount);
                    ps.setLong(5, amount);
                    ps.setInt(6, amount);
                    ps.setInt(7, amount);
                    ps.setLong(8, amount);
                    ps.executeUpdate();
                } catch (SQLException sqliteFail) {
                    // MySQL ON DUPLICATE KEY
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO seriajobs_leaderboard_stats (uuid, job_id, weekly_xp, monthly_xp, alltime_xp) " +
                                    "VALUES (?, ?, ?, ?, ?) " +
                                    "ON DUPLICATE KEY UPDATE " +
                                    "weekly_xp = weekly_xp + ?, monthly_xp = monthly_xp + ?, alltime_xp = alltime_xp + ?;")) {
                        ps.setString(1, uuid.toString());
                        ps.setString(2, jobId.toLowerCase());
                        ps.setInt(3, amount);
                        ps.setInt(4, amount);
                        ps.setLong(5, amount);
                        ps.setInt(6, amount);
                        ps.setInt(7, amount);
                        ps.setLong(8, amount);
                        ps.executeUpdate();
                    } catch (SQLException ignored) {}
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to record leaderboard XP: " + e.getMessage());
            }
        });
    }

    public void recordLeaderboardOrderCompletion(UUID uuid, String jobId) {
        if (uuid == null || jobId == null) return;
        runAsync(conn -> {
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO seriajobs_leaderboard_stats (uuid, job_id, weekly_orders, monthly_orders, alltime_orders) " +
                                "VALUES (?, ?, 1, 1, 1) " +
                                "ON CONFLICT(uuid, job_id) DO UPDATE SET " +
                                "weekly_orders = weekly_orders + 1, monthly_orders = monthly_orders + 1, alltime_orders = alltime_orders + 1;")) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, jobId.toLowerCase());
                    ps.executeUpdate();
                } catch (SQLException sqliteFail) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO seriajobs_leaderboard_stats (uuid, job_id, weekly_orders, monthly_orders, alltime_orders) " +
                                    "VALUES (?, ?, 1, 1, 1) " +
                                    "ON DUPLICATE KEY UPDATE " +
                                    "weekly_orders = weekly_orders + 1, monthly_orders = monthly_orders + 1, alltime_orders = alltime_orders + 1;")) {
                        ps.setString(1, uuid.toString());
                        ps.setString(2, jobId.toLowerCase());
                        ps.executeUpdate();
                    } catch (SQLException ignored) {}
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to record leaderboard order: " + e.getMessage());
            }
        });
    }

    public void addPendingReward(UUID uuid, String rewardType, double amount, String extraData, String reason) {
        if (uuid == null || rewardType == null) return;
        String id = UUID.randomUUID().toString();
        runAsync(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO seriajobs_pending_rewards (id, uuid, reward_type, amount, extra_data, reason, created_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?);")) {
                ps.setString(1, id);
                ps.setString(2, uuid.toString());
                ps.setString(3, rewardType.toUpperCase());
                ps.setDouble(4, amount);
                ps.setString(5, extraData != null ? extraData : "");
                ps.setString(6, reason != null ? reason : "Leaderboard Reward");
                ps.setLong(7, System.currentTimeMillis());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to save pending reward: " + e.getMessage());
            }
        });
    }

    public void claimPendingRewards(Player player) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        runAsync(conn -> {
            try {
                List<String> claimedIds = new java.util.ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM seriajobs_pending_rewards WHERE uuid = ?")) {
                    ps.setString(1, uuid.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String id = rs.getString("id");
                            String type = rs.getString("reward_type");
                            double amount = rs.getDouble("amount");
                            String extraData = rs.getString("extra_data");
                            String reason = rs.getString("reason");

                            claimedIds.add(id);

                            Bukkit.getScheduler().runTask(plugin, () -> {
                                if (!player.isOnline()) return;
                                if ("VAULT".equalsIgnoreCase(type)) {
                                    plugin.getEconomyService().deposit(player, amount);
                                } else if ("EXCELLENT_ECONOMY".equalsIgnoreCase(type) || "CURRENCY".equalsIgnoreCase(type)) {
                                    plugin.getEconomyService().deposit(player, extraData, amount);
                                } else if ("COMMAND".equalsIgnoreCase(type)) {
                                    if (extraData != null && !extraData.isBlank()) {
                                        String cmd = extraData.replace("%player%", player.getName()).replace("{player}", player.getName());
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                                    }
                                }

                                player.sendMessage(TextUtil.parseForPlayer(player,
                                        "<gold>[SeriaJobs]</gold> <green>Hadiah tertunda (" + reason + ") telah berhasil diklaim!</green>"));
                            });
                        }
                    }
                }

                if (!claimedIds.isEmpty()) {
                    for (String claimId : claimedIds) {
                        try (PreparedStatement del = conn.prepareStatement("DELETE FROM seriajobs_pending_rewards WHERE id = ?")) {
                            del.setString(1, claimId);
                            del.executeUpdate();
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to claim pending rewards for " + player.getName() + ": " + e.getMessage());
            }
        });
    }

    public void unloadProfile(UUID uuid) {
        JobProfile profile = profileCache.remove(uuid);
        if (profile != null) {
            saveProfileAsync(profile);
        }
    }

    public void shutdown() {
        for (JobProfile profile : profileCache.values()) {
            saveProfileSync(profile);
        }
        profileCache.clear();

        dbExecutor.shutdown();
        try {
            if (!dbExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                dbExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            dbExecutor.shutdownNow();
        }

        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
