package id.seria.jobs.model;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JobProfile {

    private final UUID uuid;
    private final Set<String> activeJobs = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, Integer> jobLevels = new ConcurrentHashMap<>();
    private final Map<String, Integer> jobXp = new ConcurrentHashMap<>();

    // Key format: "shiftId:jobId:orderId"
    private final Map<String, Integer> orderProgress = new ConcurrentHashMap<>();
    private final Set<String> completedOrders = Collections.synchronizedSet(new HashSet<>());

    // Cooldown on leave: jobId -> expireTimestamp (millis)
    private final Map<String, Long> leaveCooldowns = new ConcurrentHashMap<>();

    public JobProfile(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    @NotNull
    public Set<String> getActiveJobs() {
        return activeJobs;
    }

    public boolean hasJob(String jobId) {
        return activeJobs.contains(jobId.toLowerCase());
    }

    public void addActiveJob(String jobId) {
        activeJobs.add(jobId.toLowerCase());
        jobLevels.putIfAbsent(jobId.toLowerCase(), 1);
        jobXp.putIfAbsent(jobId.toLowerCase(), 0);
    }

    public void removeActiveJob(String jobId) {
        activeJobs.remove(jobId.toLowerCase());
    }

    public int getJobLevel(String jobId) {
        return jobLevels.getOrDefault(jobId.toLowerCase(), 1);
    }

    public void setJobLevel(String jobId, int level) {
        jobLevels.put(jobId.toLowerCase(), Math.max(1, level));
    }

    public int getJobXp(String jobId) {
        return jobXp.getOrDefault(jobId.toLowerCase(), 0);
    }

    public void setJobXp(String jobId, int xp) {
        jobXp.put(jobId.toLowerCase(), Math.max(0, xp));
    }

    public void addJobXp(String jobId, int amount) {
        int current = getJobXp(jobId);
        jobXp.put(jobId.toLowerCase(), current + amount);
    }

    public int getOrderProgress(String shiftId, String jobId, String orderId) {
        String key = makeKey(shiftId, jobId, orderId);
        return orderProgress.getOrDefault(key, 0);
    }

    public void setOrderProgress(String shiftId, String jobId, String orderId, int amount) {
        String key = makeKey(shiftId, jobId, orderId);
        orderProgress.put(key, Math.max(0, amount));
    }

    public void addOrderProgress(String shiftId, String jobId, String orderId, int amount) {
        String key = makeKey(shiftId, jobId, orderId);
        int current = orderProgress.getOrDefault(key, 0);
        orderProgress.put(key, current + amount);
    }

    public boolean isOrderCompleted(String shiftId, String jobId, String orderId) {
        return completedOrders.contains(makeKey(shiftId, jobId, orderId));
    }

    public void markOrderCompleted(String shiftId, String jobId, String orderId) {
        completedOrders.add(makeKey(shiftId, jobId, orderId));
    }

    public long getLeaveCooldown(String jobId) {
        return leaveCooldowns.getOrDefault(jobId.toLowerCase(), 0L);
    }

    public void setLeaveCooldown(String jobId, long expireTimestamp) {
        leaveCooldowns.put(jobId.toLowerCase(), expireTimestamp);
    }

    public boolean isLeaveOnCooldown(String jobId) {
        long expire = getLeaveCooldown(jobId);
        return System.currentTimeMillis() < expire;
    }

    private String makeKey(String shiftId, String jobId, String orderId) {
        return shiftId + ":" + jobId.toLowerCase() + ":" + orderId.toLowerCase();
    }

    public Map<String, Integer> getJobLevels() {
        return jobLevels;
    }

    public Map<String, Integer> getJobXpMap() {
        return jobXp;
    }

    public Map<String, Integer> getOrderProgressMap() {
        return orderProgress;
    }

    public Set<String> getCompletedOrdersSet() {
        return completedOrders;
    }

    public Map<String, Long> getLeaveCooldowns() {
        return leaveCooldowns;
    }
}
