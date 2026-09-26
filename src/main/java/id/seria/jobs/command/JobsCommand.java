package id.seria.jobs.command;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.api.JobJoinEvent;
import id.seria.jobs.api.JobLeaveEvent;
import id.seria.jobs.model.Job;
import id.seria.jobs.model.JobProfile;
import id.seria.jobs.model.LeaderboardEntry;
import id.seria.jobs.model.LeaderboardPeriod;
import id.seria.jobs.ui.MainJobsMenu;
import id.seria.jobs.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JobsCommand implements CommandExecutor, TabCompleter {

    private final SeriaJobs plugin;

    public JobsCommand(SeriaJobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("seriajobs.use")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(sender instanceof Player p ? p : null, "general.no-permission"));
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLanguageManager().getMessage(null, "general.player-only"));
                return true;
            }
            new MainJobsMenu(plugin, player).open();
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "join": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(null, "general.player-only"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(player, "general.unknown-subcommand"));
                    return true;
                }
                handleJoin(player, args[1]);
                return true;
            }

            case "leave": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(null, "general.player-only"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(player, "general.unknown-subcommand"));
                    return true;
                }
                handleLeave(player, args[1]);
                return true;
            }

            case "rewards":
            case "progression": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(null, "general.player-only"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(player, "general.unknown-subcommand"));
                    return true;
                }
                Job job = plugin.getJobRegistry().getJob(args[1]);
                if (job == null) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player, "general.job-not-found", "{job}", args[1]));
                    return true;
                }
                new id.seria.jobs.ui.JobProgressionMenu(plugin, player, job).open();
                return true;
            }

            case "order":
            case "orders":
            case "market":
            case "bursa": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(null, "general.player-only"));
                    return true;
                }
                String jobId = args.length >= 2 ? args[1] : "";
                if (jobId.isEmpty()) {
                    JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
                    if (!profile.getActiveJobs().isEmpty()) {
                        jobId = profile.getActiveJobs().iterator().next();
                    }
                }
                if (plugin.getJobsMenuService() != null) {
                    plugin.getJobsMenuService().openJobMenu(player, jobId);
                }
                return true;
            }

            case "reroll": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(null, "general.player-only"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§eGunakan: §f/jobs reroll <slot 1-5> [gins|serium] [jobId]");
                    return true;
                }
                int slotIndex;
                try {
                    slotIndex = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cNomor slot harus berupa angka antara 1 sampai 5.");
                    return true;
                }
                if (slotIndex < 1 || slotIndex > 5) {
                    player.sendMessage("§cNomor slot harus antara 1 sampai 5.");
                    return true;
                }

                String currency = args.length >= 3 ? args[2].toLowerCase() : "gins";
                String jobId = args.length >= 4 ? args[3] : "";
                if (jobId.isEmpty()) {
                    JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
                    if (!profile.getActiveJobs().isEmpty()) {
                        jobId = profile.getActiveJobs().iterator().next();
                    }
                }
                if (jobId.isEmpty()) {
                    player.sendMessage("§cKamu belum bergabung ke pekerjaan apa pun!");
                    return true;
                }

                if (currency.equals("serium")) {
                    plugin.getJobsManager().executeRerollSerium(player, jobId, slotIndex);
                } else {
                    plugin.getJobsManager().executeRerollGins(player, jobId, slotIndex);
                }
                return true;
            }

            case "admin": {
                if (!sender.hasPermission("seriajobs.admin")) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(sender instanceof Player p ? p : null, "general.no-permission"));
                    return true;
                }
                handleAdmin(sender, args);
                return true;
            }

            case "reload": {
                if (!sender.hasPermission("seriajobs.admin")) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(sender instanceof Player p ? p : null, "general.no-permission"));
                    return true;
                }
                handleReload(sender);
                return true;
            }

            case "top":
            case "leaderboard": {
                handleLeaderboard(sender, args);
                return true;
            }

            default: {
                sender.sendMessage(plugin.getLanguageManager().getMessage(sender instanceof Player p ? p : null, "general.unknown-subcommand"));
                return true;
            }
        }
    }

    private void handleJoin(Player player, String jobId) {
        Job job = plugin.getJobRegistry().getJob(jobId);
        if (job == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "general.job-not-found", "{job}", jobId));
            return;
        }

        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
        if (profile.hasJob(job.getId())) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "jobs.already-joined", "{job}", job.getDisplayName()));
            return;
        }

        if (!plugin.getSlotPermissionManager().canJoinJob(player, profile)) {
            int max = plugin.getSlotPermissionManager().getMaxSlots(player);
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "jobs.slot-full",
                    "{active}", String.valueOf(profile.getActiveJobs().size()),
                    "{max}", max == -1 ? "∞" : String.valueOf(max)));
            return;
        }

        JobJoinEvent event = new JobJoinEvent(player, job);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        profile.addActiveJob(job.getId());
        plugin.getPlayerDataManager().saveProfileAsync(profile);

        player.sendMessage(plugin.getLanguageManager().getMessage(player, "jobs.join-success", "{job}", job.getDisplayName()));
    }

    private void handleLeave(Player player, String jobId) {
        Job job = plugin.getJobRegistry().getJob(jobId);
        if (job == null) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "general.job-not-found", "{job}", jobId));
            return;
        }

        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
        if (!profile.hasJob(job.getId())) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "jobs.not-joined"));
            return;
        }

        if (!plugin.getConfigManager().isLeaveJobEnabled()) return;

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
        if (event.isCancelled()) return;

        profile.removeActiveJob(job.getId());
        long cooldownHours = plugin.getConfigManager().getLeaveCooldownHours();
        profile.setLeaveCooldown(job.getId(), System.currentTimeMillis() + (cooldownHours * 3600 * 1000L));

        if (plugin.getConfigManager().isResetProgressOnLeave()) {
            profile.setJobLevel(job.getId(), 1);
            profile.setJobXp(job.getId(), 0);
        }

        plugin.getPlayerDataManager().saveProfileAsync(profile);
        player.sendMessage(plugin.getLanguageManager().getMessage(player, "jobs.leave-success", "{job}", job.getDisplayName()));
    }

    private void handleAdmin(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getLanguageManager().getMessage(sender instanceof Player p ? p : null, "general.unknown-subcommand"));
            return;
        }

        String adminSub = args[1].toLowerCase();
        switch (adminSub) {
            case "reload":
                handleReload(sender);
                break;

            case "forceroll":
                if (args.length >= 3) {
                    String jobId = args[2];
                    Job job = plugin.getJobRegistry().getJob(jobId);
                    if (job == null) {
                        sender.sendMessage(plugin.getLanguageManager().getMessage(sender instanceof Player p ? p : null, "general.job-not-found", "{job}", jobId));
                        return;
                    }
                    plugin.getOrderPoolManager().rollJobOrders(job);
                    plugin.getOrderPoolManager().saveState();
                    sender.sendMessage(plugin.getLanguageManager().getMessage(sender instanceof Player p ? p : null, "admin.forceroll-job", "{job}", job.getDisplayName()));
                } else {
                    plugin.getOrderPoolManager().rollAllOrders();
                    sender.sendMessage(plugin.getLanguageManager().getMessage(sender instanceof Player p ? p : null, "admin.forceroll-all"));
                }
                break;

            case "setlevel":
                if (args.length < 5) {
                    sender.sendMessage("§cUsage: /seriajobs admin setlevel <player> <job_id> <level>");
                    return;
                }
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found or offline!");
                    return;
                }
                String jobId = args[3];
                Job job = plugin.getJobRegistry().getJob(jobId);
                if (job == null) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage(sender instanceof Player p ? p : null, "general.job-not-found", "{job}", jobId));
                    return;
                }
                try {
                    int level = Integer.parseInt(args[4]);
                    JobProfile profile = plugin.getPlayerDataManager().getProfile(target.getUniqueId());
                    profile.setJobLevel(job.getId(), level);
                    plugin.getPlayerDataManager().saveProfileAsync(profile);
                    sender.sendMessage(plugin.getLanguageManager().getMessage(sender instanceof Player p ? p : null, "admin.setlevel-success",
                            "{player}", target.getName(),
                            "{job}", job.getDisplayName(),
                            "{level}", String.valueOf(level)));
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid level number!");
                }
                break;

            case "leaderboard":
            case "top":
                if (args.length < 3) {
                    sender.sendMessage("§eUsage: /seriajobs admin leaderboard <forcepayout|reset|update> [weekly|monthly]");
                    return;
                }
                String lbAction = args[2].toLowerCase();
                LeaderboardPeriod lbPeriod = LeaderboardPeriod.WEEKLY;
                if (args.length >= 4 && args[3].equalsIgnoreCase("monthly")) {
                    lbPeriod = LeaderboardPeriod.MONTHLY;
                }

                if (lbAction.equalsIgnoreCase("forcepayout")) {
                    sender.sendMessage("§a[SeriaJobs] Memulai eksekusi pembagian hadiah dan reset " + lbPeriod.name() + "...");
                    plugin.getLeaderboardManager().executePayout(lbPeriod);
                } else if (lbAction.equalsIgnoreCase("reset")) {
                    sender.sendMessage("§c[SeriaJobs] Mereset database leaderboard periode " + lbPeriod.name() + "...");
                    LeaderboardPeriod finalPeriod = lbPeriod;
                    plugin.getPlayerDataManager().runAsync(conn -> {
                        try {
                            String resetSql = finalPeriod == LeaderboardPeriod.WEEKLY
                                    ? "UPDATE seriajobs_leaderboard_stats SET weekly_xp = 0, weekly_orders = 0;"
                                    : "UPDATE seriajobs_leaderboard_stats SET monthly_xp = 0, monthly_orders = 0;";
                            try (var ps = conn.prepareStatement(resetSql)) {
                                ps.executeUpdate();
                            }
                            plugin.getLeaderboardManager().updateAllLeaderboards();
                            sender.sendMessage("§a[SeriaJobs] Berhasil mereset leaderboard " + finalPeriod.name() + "!");
                        } catch (Exception e) {
                            sender.sendMessage("§c[SeriaJobs] Gagal mereset leaderboard: " + e.getMessage());
                        }
                    });
                } else if (lbAction.equalsIgnoreCase("update")) {
                    plugin.getLeaderboardManager().updateAllLeaderboards();
                    sender.sendMessage("§a[SeriaJobs] Pembaruan cache leaderboard dipicu.");
                }
                break;

            default:
                sender.sendMessage(plugin.getLanguageManager().getMessage(sender instanceof Player p ? p : null, "general.unknown-subcommand"));
                break;
        }
    }

    private void handleLeaderboard(CommandSender sender, String[] args) {
        if (plugin.getLeaderboardManager() == null) {
            sender.sendMessage("§cSistem Leaderboard sedang dinonaktifkan.");
            return;
        }

        // /seriajobs leaderboard [job|global] [weekly|monthly|alltime]
        String target = args.length > 1 ? args[1].toLowerCase() : "global";
        LeaderboardPeriod period = LeaderboardPeriod.WEEKLY;
        if (args.length > 2) {
            String pStr = args[2].toLowerCase();
            if (pStr.startsWith("m")) period = LeaderboardPeriod.MONTHLY;
            else if (pStr.startsWith("a")) period = LeaderboardPeriod.ALL_TIME;
        }

        String periodName = period == LeaderboardPeriod.WEEKLY ? "Mingguan" : (period == LeaderboardPeriod.MONTHLY ? "Bulanan" : "Sepanjang Waktu");

        if (target.equals("global")) {
            sender.sendMessage(TextUtil.parse("<gradient:#f1c40f:#e67e22>======= <bold>Peringkat Global (" + periodName + ")</bold> =======</gradient>"));
            boolean found = false;
            for (int rank = 1; rank <= 10; rank++) {
                LeaderboardEntry entry = plugin.getLeaderboardManager().getGlobalTop(period, rank);
                if (entry != null) {
                    found = true;
                    sender.sendMessage(TextUtil.parse("<yellow>#" + rank + "</yellow> <white>" + entry.getPlayerName() + "</white> - <green>" + entry.getXp() + " XP</green> <gray>(" + entry.getOrdersCompleted() + " pesanan)</gray>"));
                }
            }
            if (!found) {
                sender.sendMessage(TextUtil.parse("<gray>Belum ada data pada periode ini.</gray>"));
            }
            if (period == LeaderboardPeriod.WEEKLY) {
                sender.sendMessage(TextUtil.parse("<gray>Reset & Hadiah dalam: <yellow>" + plugin.getLeaderboardManager().getWeeklyCountdown() + "</yellow></gray>"));
            } else if (period == LeaderboardPeriod.MONTHLY) {
                sender.sendMessage(TextUtil.parse("<gray>Reset & Hadiah dalam: <yellow>" + plugin.getLeaderboardManager().getMonthlyCountdown() + "</yellow></gray>"));
            }
        } else {
            Job job = plugin.getJobRegistry().getJob(target);
            if (job == null) {
                sender.sendMessage(plugin.getLanguageManager().getMessage(sender instanceof Player p ? p : null, "general.job-not-found", "{job}", target));
                return;
            }

            sender.sendMessage(TextUtil.parse("<gradient:#f1c40f:#e67e22>======= <bold>Peringkat " + job.getDisplayName() + " (" + periodName + ")</bold> =======</gradient>"));
            boolean found = false;
            for (int rank = 1; rank <= 10; rank++) {
                LeaderboardEntry entry = plugin.getLeaderboardManager().getJobTop(job.getId(), period, rank);
                if (entry != null) {
                    found = true;
                    sender.sendMessage(TextUtil.parse("<yellow>#" + rank + "</yellow> <white>" + entry.getPlayerName() + "</white> - <green>" + entry.getXp() + " XP</green> <gray>(" + entry.getOrdersCompleted() + " pesanan)</gray>"));
                }
            }
            if (!found) {
                sender.sendMessage(TextUtil.parse("<gray>Belum ada data pada periode ini.</gray>"));
            }
        }
    }

    private void handleReload(CommandSender sender) {
        long start = System.currentTimeMillis();
        plugin.reload();
        long elapsed = System.currentTimeMillis() - start;
        sender.sendMessage(plugin.getLanguageManager().getMessage(sender instanceof Player p ? p : null, "general.reload-success", "{ms}", String.valueOf(elapsed)));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("join");
            completions.add("leave");
            completions.add("orders");
            completions.add("market");
            completions.add("rewards");
            completions.add("progression");
            completions.add("leaderboard");
            completions.add("top");
            completions.add("reroll");
            if (sender.hasPermission("seriajobs.admin")) {
                completions.add("admin");
                completions.add("reload");
            }
            return filterCompletions(completions, args[0]);
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reroll")) {
                completions.addAll(List.of("1", "2", "3", "4", "5"));
                return filterCompletions(completions, args[1]);
            }
            if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("leave")
                    || args[0].equalsIgnoreCase("rewards") || args[0].equalsIgnoreCase("progression")
                    || args[0].equalsIgnoreCase("orders") || args[0].equalsIgnoreCase("order")
                    || args[0].equalsIgnoreCase("market")) {
                completions.addAll(plugin.getJobRegistry().getJobs().stream().map(Job::getId).toList());
                return filterCompletions(completions, args[1]);
            }
            if (args[0].equalsIgnoreCase("leaderboard") || args[0].equalsIgnoreCase("top")) {
                completions.add("global");
                completions.addAll(plugin.getJobRegistry().getJobs().stream().map(Job::getId).toList());
                return filterCompletions(completions, args[1]);
            }
            if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("seriajobs.admin")) {
                completions.add("reload");
                completions.add("forceroll");
                completions.add("setlevel");
                completions.add("leaderboard");
                return filterCompletions(completions, args[1]);
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("reroll")) {
                completions.add("gins");
                completions.add("serium");
                return filterCompletions(completions, args[2]);
            }
            if (args[0].equalsIgnoreCase("leaderboard") || args[0].equalsIgnoreCase("top")) {
                completions.add("weekly");
                completions.add("monthly");
                completions.add("alltime");
                return filterCompletions(completions, args[2]);
            }
            if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("seriajobs.admin")) {
                if (args[1].equalsIgnoreCase("forceroll")) {
                    completions.addAll(plugin.getJobRegistry().getJobs().stream().map(Job::getId).toList());
                    return filterCompletions(completions, args[2]);
                }
                if (args[1].equalsIgnoreCase("leaderboard") || args[1].equalsIgnoreCase("top")) {
                    completions.add("forcepayout");
                    completions.add("reset");
                    completions.add("update");
                    return filterCompletions(completions, args[2]);
                }
                if (args[1].equalsIgnoreCase("setlevel")) {
                    return null; // suggest player names
                }
            }
        }

        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("reroll")) {
                completions.addAll(plugin.getJobRegistry().getJobs().stream().map(Job::getId).toList());
                return filterCompletions(completions, args[3]);
            }
            if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("seriajobs.admin")) {
                if (args[1].equalsIgnoreCase("leaderboard") || args[1].equalsIgnoreCase("top")) {
                    completions.add("weekly");
                    completions.add("monthly");
                    return filterCompletions(completions, args[3]);
                }
                if (args[1].equalsIgnoreCase("setlevel")) {
                    completions.addAll(plugin.getJobRegistry().getJobs().stream().map(Job::getId).toList());
                    return filterCompletions(completions, args[3]);
                }
            }
        }

        return Collections.emptyList();
    }

    private List<String> filterCompletions(List<String> list, String input) {
        String lower = input.toLowerCase();
        return list.stream().filter(s -> s.toLowerCase().startsWith(lower)).collect(Collectors.toList());
    }
}
