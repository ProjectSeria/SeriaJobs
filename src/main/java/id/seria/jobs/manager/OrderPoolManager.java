package id.seria.jobs.manager;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import id.seria.jobs.model.JobOrder;
import id.seria.jobs.model.JobProfile;
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
    private final Map<String, JobOrder> activeEventOrders = new ConcurrentHashMap<>();
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

            if (config.isConfigurationSection("active-event-orders")) {
                for (String jobId : config.getConfigurationSection("active-event-orders").getKeys(false)) {
                    String orderId = config.getString("active-event-orders." + jobId);
                    Job job = plugin.getJobRegistry().getJob(jobId);
                    if (job != null && orderId != null) {
                        JobOrder order = job.getOrder(orderId);
                        if (order != null) {
                            activeEventOrders.put(jobId.toLowerCase(), order);
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

        for (Map.Entry<String, JobOrder> entry : activeEventOrders.entrySet()) {
            if (entry.getValue() != null) {
                config.set("active-event-orders." + entry.getKey(), entry.getValue().getId());
            }
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
                    new OrderRotationTask(plugin).run();
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
        activeEventOrders.clear();

        for (Job job : plugin.getJobRegistry().getJobs()) {
            rollJobOrders(job);
        }

        // Reset rerolls and active orders for online players
        for (org.bukkit.entity.Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            JobProfile profile = plugin.getPlayerDataManager().getProfile(onlinePlayer.getUniqueId());
            if (profile != null) {
                profile.resetRerolls();
                profile.clearActiveOrders();
            }
        }

        calculateNextRotation();
        saveState();
    }

    public void rollJobOrders(Job job) {
        String currentEvent = plugin.getConfigManager().getCurrentEvent();
        boolean eventActive = plugin.getConfigManager().isEventActive();

        List<JobOrder> regularPool = new ArrayList<>();
        List<JobOrder> eventPool = new ArrayList<>();

        for (JobOrder jo : job.getOrdersPool().values()) {
            if (jo.isEventOrder()) {
                if (eventActive && jo.getEvent() != null && jo.getEvent().equalsIgnoreCase(currentEvent)) {
                    eventPool.add(jo);
                }
            } else {
                regularPool.add(jo);
            }
        }

        Collections.shuffle(regularPool);
        Collections.shuffle(eventPool);

        // 1. Regular Order Pool (murni regular orders, tidak terganggu oleh event slot)
        int needed = Math.max(1, job.getActiveOrdersCount());
        List<JobOrder> selected = new ArrayList<>();
        for (int i = 0; i < needed; i++) {
            if (i < regularPool.size()) {
                selected.add(regularPool.get(i));
            } else if (!regularPool.isEmpty()) {
                selected.add(regularPool.get(i % regularPool.size()));
            }
        }
        activeOrders.put(job.getId().toLowerCase(), selected);

        // 2. Active Event Order terpisah
        if (eventActive && !eventPool.isEmpty()) {
            activeEventOrders.put(job.getId().toLowerCase(), eventPool.get(0));
        } else {
            activeEventOrders.remove(job.getId().toLowerCase());
        }
    }

    public List<JobOrder> getActiveOrders(String jobId) {
        return activeOrders.getOrDefault(jobId.toLowerCase(), Collections.emptyList());
    }

    public JobOrder getActiveEventOrder(String jobId) {
        return activeEventOrders.get(jobId.toLowerCase());
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
