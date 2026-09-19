package id.seria.jobs.manager;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import id.seria.jobs.model.JobOrder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OrderPoolManager {

    private final SeriaJobs plugin;
    private final Map<String, List<JobOrder>> activeOrders = new ConcurrentHashMap<>();
    private String currentShiftId;
    private long nextRotationTimestamp;
    private BukkitTask timerTask;

    public OrderPoolManager(SeriaJobs plugin) {
        this.plugin = plugin;
    }

    public void init() {
        loadState();
        calculateNextRotation();
        startTimer();
    }

    private void loadState() {
        File stateFile = new File(plugin.getDataFolder(), "data/active_shift.yml");
        if (stateFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(stateFile);
            currentShiftId = config.getString("shift-id");
            nextRotationTimestamp = config.getLong("next-rotation", 0L);

            if (config.isConfigurationSection("active-orders")) {
                for (String jobId : config.getConfigurationSection("active-orders").getKeys(false)) {
                    List<String> orderIds = config.getStringList("active-orders." + jobId);
                    Job job = plugin.getJobRegistry().getJob(jobId);
                    if (job != null) {
                        List<JobOrder> orders = new ArrayList<>();
                        for (String orderId : orderIds) {
                            JobOrder order = job.getOrder(orderId);
                            if (order != null) {
                                orders.add(order);
                            }
                        }
                        if (!orders.isEmpty()) {
                            activeOrders.put(jobId.toLowerCase(), orders);
                        }
                    }
                }
            }
        }

        // If no active shift or expired, generate now
        if (currentShiftId == null || activeOrders.isEmpty() || System.currentTimeMillis() >= nextRotationTimestamp) {
            rollAllOrders();
        }
    }

    public void saveState() {
        File dataDir = new File(plugin.getDataFolder(), "data");
        if (!dataDir.exists()) dataDir.mkdirs();

        File stateFile = new File(dataDir, "active_shift.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("shift-id", currentShiftId);
        config.set("next-rotation", nextRotationTimestamp);

        for (Map.Entry<String, List<JobOrder>> entry : activeOrders.entrySet()) {
            List<String> ids = new ArrayList<>();
            for (JobOrder order : entry.getValue()) {
                ids.add(order.getId());
            }
            config.set("active-orders." + entry.getKey(), ids);
        }

        try {
            config.save(stateFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save active shift state: " + e.getMessage());
        }
    }

    private void calculateNextRotation() {
        long now = System.currentTimeMillis();
        if (plugin.getConfigManager().isUseFixedTimes()) {
            List<String> fixedTimes = plugin.getConfigManager().getFixedResetTimes();
            if (fixedTimes != null && !fixedTimes.isEmpty()) {
                ZonedDateTime currentZdt = ZonedDateTime.now();
                LocalTime currentLocal = currentZdt.toLocalTime();

                List<LocalTime> times = new ArrayList<>();
                for (String ft : fixedTimes) {
                    try {
                        times.add(LocalTime.parse(ft, DateTimeFormatter.ofPattern("HH:mm")));
                    } catch (Exception ignored) {}
                }
                Collections.sort(times);

                LocalTime nextTime = null;
                for (LocalTime t : times) {
                    if (t.isAfter(currentLocal)) {
                        nextTime = t;
                        break;
                    }
                }

                ZonedDateTime targetZdt;
                if (nextTime != null) {
                    targetZdt = currentZdt.withHour(nextTime.getHour()).withMinute(nextTime.getMinute()).withSecond(0);
                } else {
                    // Next day's first time
                    LocalTime firstTime = times.get(0);
                    targetZdt = currentZdt.plusDays(1).withHour(firstTime.getHour()).withMinute(firstTime.getMinute()).withSecond(0);
                }
                nextRotationTimestamp = targetZdt.toInstant().toEpochMilli();
                return;
            }
        }

        // Interval-based fallback
        long intervalMillis = plugin.getConfigManager().getMarketShiftIntervalSeconds() * 1000L;
        if (nextRotationTimestamp <= now) {
            nextRotationTimestamp = now + intervalMillis;
        }
    }

    private void startTimer() {
        if (timerTask != null) {
            timerTask.cancel();
        }

        timerTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (System.currentTimeMillis() >= nextRotationTimestamp) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    rollAllOrders();
                    calculateNextRotation();
                    saveState();
                    plugin.getLogger().info("Market shift rotated! New shift: " + currentShiftId);
                });
            }
        }, 20L, 20L);
    }

    public void rollAllOrders() {
        currentShiftId = "shift_" + System.currentTimeMillis();
        activeOrders.clear();

        for (Job job : plugin.getJobRegistry().getJobs()) {
            rollJobOrders(job);
        }
        calculateNextRotation();
        saveState();
    }

    public void rollJobOrders(Job job) {
        List<JobOrder> pool = new ArrayList<>(job.getOrdersPool().values());
        Collections.shuffle(pool);

        int count = Math.min(job.getActiveOrdersCount(), pool.size());
        List<JobOrder> selected = new ArrayList<>(pool.subList(0, count));
        activeOrders.put(job.getId().toLowerCase(), selected);
    }

    public List<JobOrder> getActiveOrders(String jobId) {
        return activeOrders.getOrDefault(jobId.toLowerCase(), Collections.emptyList());
    }

    public String getCurrentShiftId() {
        return currentShiftId != null ? currentShiftId : "shift_default";
    }

    public long getNextRotationTimestamp() {
        return nextRotationTimestamp;
    }

    public String getFormattedCountdown() {
        long diff = Math.max(0, nextRotationTimestamp - System.currentTimeMillis());
        Duration duration = Duration.ofMillis(diff);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void shutdown() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        saveState();
    }
}
