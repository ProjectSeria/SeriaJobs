package id.seria.jobs.manager;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import id.seria.jobs.model.JobOrder;
import id.seria.jobs.model.JobProfile;
import id.seria.jobs.model.WorkOrder;
import id.seria.jobs.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Scheduler berkala siklus 4–6 jam untuk rotasi order pasar,
 * reset counter reroll, kalkulasi kapasitas slot, dan broadcast pembaruan pasar.
 */
public class OrderRotationTask extends BukkitRunnable {

    private final SeriaJobs plugin;

    public OrderRotationTask(@NotNull SeriaJobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getLogger().info("Executing periodic OrderRotationTask cycle...");

        // 1. Reset orders in OrderPoolManager
        plugin.getOrderPoolManager().rollAllOrders();

        String broadcastMsg = plugin.getConfig().getString(
                "market-shifts.broadcast-message",
                "<gold><bold>[PASAR KERJA]</bold> <yellow>Bursa kerja telah diperbarui! Semua pesanan pasar dan kuota reroll telah di-reset.</yellow>"
        );

        int thresholdSlot4 = plugin.getConfig().getInt("reputation.slot4-threshold", 500);
        String donorPerm = plugin.getConfig().getString("slots.paid-rank-permission", "seriajobs.rank.paid");

        // 2. Iterate online and cached player profiles
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            JobProfile profile = plugin.getPlayerDataManager().getProfile(onlinePlayer.getUniqueId());
            if (profile != null) {
                // Reset counter reroll
                profile.resetRerolls();

                // Hitung kapasitas slot
                int allowedSlots = profile.getMaxAllowedSlots(onlinePlayer);

                // Re-populate active WorkOrders for online player's active jobs
                for (String jobId : profile.getActiveJobs()) {
                    Job job = plugin.getJobRegistry().getJob(jobId);
                    if (job == null) continue;

                    List<JobOrder> activeTemplates = plugin.getOrderPoolManager().getActiveOrders(jobId);
                    double orderMult = plugin.getReputationBridgeService() != null
                            ? plugin.getReputationBridgeService().getOrderMultiplier(onlinePlayer)
                            : 1.0;

                    for (int slot = 1; slot <= activeTemplates.size(); slot++) {
                        if (!profile.isSlotUnlocked(onlinePlayer, slot)) {
                            continue;
                        }
                        JobOrder template = activeTemplates.get(slot - 1);
                        if (template != null) {
                            int jobLevel = profile.getJobLevel(jobId);
                            WorkOrder workOrder = plugin.getOrderGenerationService().generateOrder(template, jobLevel, orderMult);
                            profile.setActiveOrder(slot, workOrder);
                        }
                    }

                    // Re-populate event order to slot 100 if event is active
                    if (plugin.getConfigManager().isEventActive()) {
                        JobOrder eventTemplate = plugin.getOrderPoolManager().getActiveEventOrder(jobId);
                        if (eventTemplate != null) {
                            int jobLevel = profile.getJobLevel(jobId);
                            WorkOrder eventWorkOrder = plugin.getOrderGenerationService().generateOrder(eventTemplate, jobLevel, orderMult);
                            profile.setActiveOrder(100, eventWorkOrder);
                        }
                    } else {
                        profile.setActiveOrder(100, null);
                    }
                }

                plugin.getPlayerDataManager().saveProfileAsync(profile);
            }
        }

        // 3. Broadcast market update to all players on server
        if (broadcastMsg != null && !broadcastMsg.isBlank()) {
            Bukkit.broadcast(TextUtil.parse(broadcastMsg));
        }
    }
}
