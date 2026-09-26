package id.seria.jobs.ui;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import id.seria.jobs.model.JobProfile;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Penangan interaksi klik langsung di dalam menu internal jobs.
 */
public class JobsMenuListener implements Listener {

    private final SeriaJobs plugin;

    public JobsMenuListener(@NotNull SeriaJobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof JobsMenuHolder holder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= 54) {
            return;
        }

        String jobId = holder.getJobId();
        Job job = plugin.getJobRegistry().getJob(jobId);
        if (job == null && !plugin.getJobRegistry().getJobs().isEmpty()) {
            job = plugin.getJobRegistry().getJobs().iterator().next();
        }
        if (job == null) return;

        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());

        // 1. Periksa apakah klik pada tombol reroll (slot index 1-5)
        for (int i = 0; i < JobsMenuService.REROLL_SLOTS.length; i++) {
            if (rawSlot == JobsMenuService.REROLL_SLOTS[i]) {
                int slotIndex = i + 1;
                handleRerollButtonClick(player, job, profile, slotIndex, event);
                return;
            }
        }

        // 2. Periksa apakah klik pada kartu order untuk turn-in atau reroll (slot index 1-5)
        for (int i = 0; i < JobsMenuService.ORDER_SLOTS.length; i++) {
            if (rawSlot == JobsMenuService.ORDER_SLOTS[i]) {
                int slotIndex = i + 1;
                handleOrderCardClick(player, job, profile, slotIndex, event);
                return;
            }
        }

        // 3. Tombol navigasi
        if (rawSlot == 48) {
            // Level Rewards Roadmap
            new JobProgressionMenu(plugin, player, job).open();
        } else if (rawSlot == 49 || rawSlot == 4) {
            // Refresh
            if (plugin.getJobsMenuService() != null) {
                plugin.getJobsMenuService().openJobMenu(player, job.getId());
            }
        } else if (rawSlot == 50) {
            // Close
            player.closeInventory();
        }
    }

    private void handleOrderCardClick(Player player, Job job, JobProfile profile, int slotIndex, InventoryClickEvent event) {
        org.bukkit.configuration.file.FileConfiguration cfg = plugin.getConfig();
        org.bukkit.configuration.ConfigurationSection sec = cfg.getConfigurationSection("order-clicks");

        java.util.List<String> actions = getClickActions(sec, event);
        if (actions.isEmpty()) {
            if (event.isRightClick()) {
                actions = java.util.List.of("[jobs_reroll]");
            } else {
                actions = java.util.List.of("[TURN_IN]");
            }
        }

        executeActionsForSlot(player, job, profile, slotIndex, actions);
    }

    private void handleRerollButtonClick(Player player, Job job, JobProfile profile, int slotIndex, InventoryClickEvent event) {
        org.bukkit.configuration.file.FileConfiguration cfg = plugin.getConfig();
        org.bukkit.configuration.ConfigurationSection sec = cfg.getConfigurationSection("reroll-button-clicks");

        java.util.List<String> actions = getClickActions(sec, event);
        if (actions.isEmpty()) {
            actions = java.util.List.of("[jobs_reroll]");
        }

        executeActionsForSlot(player, job, profile, slotIndex, actions);
    }

    private java.util.List<String> getClickActions(org.bukkit.configuration.ConfigurationSection sec, InventoryClickEvent event) {
        if (sec == null) return java.util.List.of();
        if (event.isShiftClick() && event.isRightClick()) {
            if (sec.contains("shift-right-click")) return sec.getStringList("shift-right-click");
            if (sec.contains("shift-right-click-actions")) return sec.getStringList("shift-right-click-actions");
        }
        if (event.isShiftClick() && event.isLeftClick()) {
            if (sec.contains("shift-left-click")) return sec.getStringList("shift-left-click");
            if (sec.contains("shift-left-click-actions")) return sec.getStringList("shift-left-click-actions");
        }
        if (event.getClick() == ClickType.MIDDLE) {
            if (sec.contains("middle-click")) return sec.getStringList("middle-click");
            if (sec.contains("middle-click-actions")) return sec.getStringList("middle-click-actions");
        }
        if (event.isRightClick()) {
            if (sec.contains("right-click")) return sec.getStringList("right-click");
            if (sec.contains("right-click-actions")) return sec.getStringList("right-click-actions");
        }
        if (event.isLeftClick()) {
            if (sec.contains("left-click")) return sec.getStringList("left-click");
            if (sec.contains("left-click-actions")) return sec.getStringList("left-click-actions");
        }
        if (sec.contains("click-actions")) return sec.getStringList("click-actions");
        if (sec.contains("actions")) return sec.getStringList("actions");
        return java.util.List.of();
    }

    private void executeActionsForSlot(Player player, Job job, JobProfile profile, int slotIndex, java.util.List<String> actions) {
        boolean shouldRefresh = false;
        for (String act : actions) {
            String trimmed = act.trim();
            if (trimmed.equalsIgnoreCase("[TURN_IN]") || trimmed.equalsIgnoreCase("[DEPOSIT]")) {
                if (plugin.getJobsManager().executeTurnIn(player, job.getId(), slotIndex)) {
                    shouldRefresh = true;
                }
            } else if (trimmed.equalsIgnoreCase("[jobs_reroll]") || trimmed.equalsIgnoreCase("[reroll]")) {
                if (plugin.getJobsManager().executeReroll(player, job.getId(), slotIndex)) {
                    shouldRefresh = true;
                }
            } else if (trimmed.equalsIgnoreCase("[REFRESH]")) {
                shouldRefresh = true;
            } else if (trimmed.equalsIgnoreCase("[CLOSE]")) {
                player.closeInventory();
                return;
            } else if (trimmed.toLowerCase().startsWith("[console]")) {
                String cmd = trimmed.substring("[console]".length()).trim()
                        .replace("%player%", player.getName())
                        .replace("%slot%", String.valueOf(slotIndex))
                        .replace("%job%", job.getId());
                if (!cmd.isEmpty()) {
                    org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), cmd);
                }
            } else if (trimmed.toLowerCase().startsWith("[player]")) {
                String cmd = trimmed.substring("[player]".length()).trim()
                        .replace("%player%", player.getName())
                        .replace("%slot%", String.valueOf(slotIndex))
                        .replace("%job%", job.getId());
                if (!cmd.isEmpty()) {
                    player.performCommand(cmd);
                }
            } else if (trimmed.toLowerCase().startsWith("[message]")) {
                String msg = trimmed.substring("[message]".length()).trim()
                        .replace("%player%", player.getName())
                        .replace("%slot%", String.valueOf(slotIndex))
                        .replace("%job%", job.getId());
                player.sendMessage(id.seria.jobs.utils.TextUtil.parseForPlayer(player, msg));
            } else if (trimmed.toLowerCase().startsWith("[sound]")) {
                String soundName = trimmed.substring("[sound]".length()).trim();
                try {
                    Sound snd = Sound.valueOf(soundName.toUpperCase());
                    player.playSound(player.getLocation(), snd, 1.0f, 1.0f);
                } catch (Exception ignored) {}
            }
        }

        if (shouldRefresh && plugin.getJobsMenuService() != null) {
            plugin.getJobsMenuService().openJobMenu(player, job.getId());
        }
    }
}
