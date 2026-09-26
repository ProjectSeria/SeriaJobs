package id.seria.jobs.ui;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.api.JobJoinEvent;
import id.seria.jobs.api.JobLeaveEvent;
import id.seria.jobs.model.Job;
import id.seria.jobs.model.JobProfile;
import id.seria.jobs.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MainJobsMenu extends BaseMenu {

    public MainJobsMenu(SeriaJobs plugin, Player player) {
        super(plugin, player, "main_jobs_menu.yml");
    }

    @Override
    protected String replacePlaceholders(String text) {
        if (text == null) return null;
        int activeCount = 0;
        int maxSlots = -1;
        String activeJobsList = "-";
        String activeJobLevel = "-";
        int ordersCompleted = 0;

        if (player != null && plugin.getPlayerDataManager() != null) {
            JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
            if (profile != null) {
                activeCount = profile.getActiveJobs().size();
                if (!profile.getActiveJobs().isEmpty()) {
                    List<String> names = new ArrayList<>();
                    List<String> levels = new ArrayList<>();
                    for (String jId : profile.getActiveJobs()) {
                        Job j = plugin.getJobRegistry().getJob(jId);
                        names.add(j != null ? j.getDisplayName() : jId);
                        levels.add(String.valueOf(profile.getJobLevel(jId)));
                    }
                    activeJobsList = String.join("<gray>, </gray>", names);
                    activeJobLevel = String.join("<gray>, </gray>", levels);
                }
            }
            if (plugin.getSlotPermissionManager() != null) {
                maxSlots = plugin.getSlotPermissionManager().getMaxSlots(player);
            }
            if (plugin.getLeaderboardManager() != null) {
                var entry = plugin.getLeaderboardManager().getPlayerGlobalEntry(player.getUniqueId(), id.seria.jobs.model.LeaderboardPeriod.ALL_TIME);
                if (entry != null) {
                    ordersCompleted = entry.getOrdersCompleted();
                }
            }
        }
        String maxStr = maxSlots == -1 ? "Tak Terbatas" : String.valueOf(maxSlots);
        String ordersCompletedStr = String.valueOf(ordersCompleted);

        return text.replace("{seriajobs_active_jobs_list}%", activeJobsList)
                .replace("{seriajobs_active_jobs_list}", activeJobsList)
                .replace("%seriajobs_active_jobs_list%", activeJobsList)
                .replace("{seriajobs_active_jobs}%", activeJobsList)
                .replace("{seriajobs_active_jobs}", activeJobsList)
                .replace("%seriajobs_active_jobs%", activeJobsList)
                .replace("{active_jobs_list}", activeJobsList)
                .replace("%active_jobs_list%", activeJobsList)
                .replace("{seriajobs_level}%", activeJobLevel)
                .replace("{seriajobs_level}", activeJobLevel)
                .replace("%seriajobs_level%", activeJobLevel)
                .replace("{seriajobs_job_level}%", activeJobLevel)
                .replace("{seriajobs_job_level}", activeJobLevel)
                .replace("%seriajobs_job_level%", activeJobLevel)
                .replace("{seriajobs_active_job_level}%", activeJobLevel)
                .replace("{seriajobs_active_job_level}", activeJobLevel)
                .replace("%seriajobs_active_job_level%", activeJobLevel)
                .replace("{seriajobs_active_jobs_level}%", activeJobLevel)
                .replace("{seriajobs_active_jobs_level}", activeJobLevel)
                .replace("%seriajobs_active_jobs_level%", activeJobLevel)
                .replace("{seriajobs_order_completed}%", ordersCompletedStr)
                .replace("{seriajobs_order_completed}", ordersCompletedStr)
                .replace("%seriajobs_order_completed%", ordersCompletedStr)
                .replace("{seriajobs_orders_completed}", ordersCompletedStr)
                .replace("%seriajobs_orders_completed%", ordersCompletedStr)
                .replace("{order_completed}", ordersCompletedStr)
                .replace("%order_completed%", ordersCompletedStr)
                .replace("{orders_completed}", ordersCompletedStr)
                .replace("%orders_completed%", ordersCompletedStr)
                .replace("{active_jobs_count}", String.valueOf(activeCount))
                .replace("%active_jobs_count%", String.valueOf(activeCount))
                .replace("{max_slots}", maxStr)
                .replace("%max_slots%", maxStr);
    }

    @Override
    public void build() {
        createInventory(54, "<gradient:#1e3a8a:#3b82f6>Bursa Pekerjaan Seria</gradient>");
        applyFillers();
        applyStaticItems();

        List<Integer> slots = menuConfig != null
                ? menuConfig.getIntegerList("job-slots")
                : List.of(20, 21, 22, 23, 24, 29, 30, 31, 32, 33);

        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
        List<Job> allJobs = new ArrayList<>(plugin.getJobRegistry().getJobs());

        int slotIndex = 0;
        for (Job job : allJobs) {
            if (slotIndex >= slots.size()) break;
            int slot = slots.get(slotIndex++);

            boolean isJoined = profile.hasJob(job.getId());
            int level = profile.getJobLevel(job.getId());
            int xp = profile.getJobXp(job.getId());
            int requiredXp = job.getRequiredXp(level);

            ItemStack item = new ItemStack(job.getIcon() != null ? job.getIcon() : Material.STONE);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                String templateSection = isJoined ? "job-item-joined" : "job-item-available";
                String nameFormat = menuConfig != null
                        ? menuConfig.getString(templateSection + ".name", job.getDisplayName())
                        : job.getDisplayName();

                nameFormat = nameFormat.replace("{display_name}", job.getDisplayName())
                        .replace("{job_id}", job.getId())
                        .replace("{level}", String.valueOf(level));

                meta.displayName(TextUtil.parseForPlayer(player, nameFormat));

                List<String> rawLore = menuConfig != null
                        ? menuConfig.getStringList(templateSection + ".lore")
                        : new ArrayList<>();

                List<Component> finalLore = new ArrayList<>();
                for (String line : rawLore) {
                    if (line.contains("{description}")) {
                        for (String descLine : job.getDescription()) {
                            finalLore.add(TextUtil.parseForPlayer(player, descLine));
                        }
                    } else {
                        String replaced = line.replace("{display_name}", job.getDisplayName())
                                .replace("{job_id}", job.getId())
                                .replace("{level}", String.valueOf(level))
                                .replace("{max_level}", String.valueOf(job.getMaxLevel()))
                                .replace("{xp}", String.valueOf(xp))
                                .replace("{required_xp}", String.valueOf(requiredXp));
                        finalLore.add(TextUtil.parseForPlayer(player, replaced));
                    }
                }
                meta.lore(finalLore);
                item.setItemMeta(meta);
            }

            inventory.setItem(slot, item);

            clickActions.put(slot, e -> {
                e.setCancelled(true);
                if (isJoined) {
                    if (e.isRightClick()) {
                        handleLeaveJob(job, profile);
                    } else {
                        new OrderDepositMenu(plugin, player, job).open();
                    }
                } else {
                    handleJoinJob(job, profile);
                }
            });
        }
    }

    private void handleJoinJob(Job job, JobProfile profile) {
        if (!plugin.getSlotPermissionManager().canJoinJob(player, profile)) {
            int max = plugin.getSlotPermissionManager().getMaxSlots(player);
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "jobs.slot-full",
                    "{active}", String.valueOf(profile.getActiveJobs().size()),
                    "{max}", max == -1 ? "∞" : String.valueOf(max)));
            return;
        }

        JobJoinEvent event = new JobJoinEvent(player, job);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        profile.addActiveJob(job.getId());
        plugin.getPlayerDataManager().saveProfileAsync(profile);

        player.sendMessage(plugin.getLanguageManager().getMessage(player, "jobs.join-success",
                "{job}", job.getDisplayName()));

        // Refresh UI
        refresh();
    }

    private void handleLeaveJob(Job job, JobProfile profile) {
        if (!plugin.getConfigManager().isLeaveJobEnabled()) {
            return;
        }

        if (profile.isLeaveOnCooldown(job.getId())) {
            long remainingMillis = profile.getLeaveCooldown(job.getId()) - System.currentTimeMillis();
            long hours = remainingMillis / (1000 * 60 * 60);
            long minutes = (remainingMillis % (1000 * 60 * 60)) / (1000 * 60);
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "jobs.leave-cooldown",
                    "{time}", hours + " jam " + minutes + " menit"));
            return;
        }

        JobLeaveEvent event = new JobLeaveEvent(player, job);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        profile.removeActiveJob(job.getId());
        long cooldownHours = plugin.getConfigManager().getLeaveCooldownHours();
        profile.setLeaveCooldown(job.getId(), System.currentTimeMillis() + (cooldownHours * 3600 * 1000L));

        if (plugin.getConfigManager().isResetProgressOnLeave()) {
            profile.setJobLevel(job.getId(), 1);
            profile.setJobXp(job.getId(), 0);
        }

        plugin.getPlayerDataManager().saveProfileAsync(profile);

        player.sendMessage(plugin.getLanguageManager().getMessage(player, "jobs.leave-success",
                "{job}", job.getDisplayName()));

        // Refresh UI
        refresh();
    }
}
