package id.seria.jobs.hook;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.JobProfile;
import id.seria.jobs.model.WorkOrder;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mengekspos data internal pesanan bursa kerja dan sistem reroll ke PlaceholderAPI
 * dengan identifier %jobsorder% untuk DeluxeMenus shop, Hologram, atau TAB.
 */
public class JobsPlaceholderExpansion extends PlaceholderExpansion {

    private final SeriaJobs plugin;

    public JobsPlaceholderExpansion(@NotNull SeriaJobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "jobsorder";
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
        if (offlinePlayer == null || !offlinePlayer.isOnline()) {
            return "";
        }

        Player player = offlinePlayer.getPlayer();
        if (player == null) return "";

        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
        if (profile == null) return "";

        String lower = params.toLowerCase();

        // %jobsorder_order_reputation_<slot>%
        if (lower.startsWith("order_reputation_")) {
            String slotStr = lower.substring("order_reputation_".length());
            try {
                int slot = Integer.parseInt(slotStr);
                WorkOrder wo = profile.getActiveOrder(slot);
                if (wo != null) {
                    return String.valueOf(wo.getGeneratedReputationAmount());
                }
            } catch (NumberFormatException ignored) {}
            return "0";
        }

        int maxReroll = plugin.getConfig().getInt("reroll.max-count", 10);

        // %jobsorder_reroll_current% / %jobsorder_reroll_count%
        if (lower.equals("reroll_current") || lower.equals("reroll_count")) {
            return String.valueOf(profile.getRerollCount());
        }

        // %jobsorder_reroll_max% / %jobsorder_reroll_max_count%
        if (lower.equals("reroll_max") || lower.equals("reroll_max_count")) {
            return String.valueOf(maxReroll);
        }

        // %jobsorder_reroll_remaining%
        if (lower.equals("reroll_remaining")) {
            return String.valueOf(Math.max(0, maxReroll - profile.getRerollCount()));
        }

        // %jobsorder_reroll_ratio% / %jobsorder_reroll_quota% -> (e.g. "1/10")
        if (lower.equals("reroll_ratio") || lower.equals("reroll_quota")) {
            return profile.getRerollCount() + "/" + maxReroll;
        }

        // %jobsorder_can_reroll%
        if (lower.equals("can_reroll")) {
            return String.valueOf(profile.canReroll(maxReroll));
        }

        // %jobsorder_reroll_cost_gins%
        if (lower.equals("reroll_cost_gins")) {
            int cost = plugin.getOrderGenerationService().calculateRerollCost(profile.getRerollCount());
            return String.valueOf(cost);
        }

        // %jobsorder_reroll_cost_serium%
        if (lower.equals("reroll_cost_serium")) {
            return String.valueOf(plugin.getConfig().getInt("reroll.serium-cost", 1));
        }

        // %jobsorder_has_gins%
        if (lower.equals("has_gins")) {
            int cost = plugin.getOrderGenerationService().calculateRerollCost(profile.getRerollCount());
            boolean has = plugin.getJobsManager().getEconomyService() != null
                    && plugin.getJobsManager().getEconomyService().has(player, cost);
            return String.valueOf(has);
        }

        // %jobsorder_balance% / %jobsorder_gins_balance%
        if (lower.equals("balance") || lower.equals("gins_balance")) {
            double bal = plugin.getJobsManager().getEconomyService() != null
                    ? plugin.getJobsManager().getEconomyService().getBalance(player)
                    : 0.0;
            return String.valueOf((long) bal);
        }

        // %jobsorder_has_serium%
        if (lower.equals("has_serium")) {
            int seriumCost = plugin.getConfig().getInt("reroll.serium-cost", 1);
            if (plugin.getHookManager() != null && plugin.getHookManager().isExcellentEconomyLoaded()) {
                double current = plugin.getHookManager().getExcellentEconomyHook().getBalance(player, "serium");
                return String.valueOf(current >= seriumCost);
            }
            return "false";
        }

        // %jobsorder_serium_balance%
        if (lower.equals("serium_balance")) {
            if (plugin.getHookManager() != null && plugin.getHookManager().isExcellentEconomyLoaded()) {
                double current = plugin.getHookManager().getExcellentEconomyHook().getBalance(player, "serium");
                return String.valueOf((long) current);
            }
            return "0";
        }

        // %jobsorder_reroll_choice%
        if (lower.equals("reroll_choice")) {
            if (!profile.canReroll()) return "none";
            int cost = plugin.getOrderGenerationService().calculateRerollCost(profile.getRerollCount());
            if (plugin.getJobsManager().getEconomyService() != null && plugin.getJobsManager().getEconomyService().has(player, cost)) {
                return "gins";
            }
            int seriumCost = plugin.getConfig().getInt("reroll.serium-cost", 1);
            if (plugin.getHookManager() != null && plugin.getHookManager().isExcellentEconomyLoaded()) {
                double current = plugin.getHookManager().getExcellentEconomyHook().getBalance(player, "serium");
                if (current >= seriumCost) return "serium";
            }
            return "none";
        }

        // %jobsorder_max_slots%
        if (lower.equals("max_slots")) {
            int rep = plugin.getReputationBridgeService() != null
                    ? plugin.getReputationBridgeService().getPlayerReputation(player)
                    : 0;
            int threshold = plugin.getConfig().getInt("reputation.slot4-threshold", 500);
            boolean hasPaidRank = player.hasPermission("seriajobs.rank.paid")
                    || player.hasPermission("seriajobs.donor")
                    || player.hasPermission("seriajobs.vip")
                    || player.isOp();
            int slots = profile.getMaxAllowedSlots(rep, threshold, hasPaidRank);
            return String.valueOf(slots);
        }

        // %jobsorder_reputation%
        if (lower.equals("reputation")) {
            int rep = plugin.getReputationBridgeService() != null
                    ? plugin.getReputationBridgeService().getPlayerReputation(player)
                    : 0;
            return String.valueOf(rep);
        }

        // %jobsorder_reputation_tier%
        if (lower.equals("reputation_tier") || lower.equals("tier_name")) {
            if (plugin.getReputationBridgeService() != null) {
                return plugin.getReputationBridgeService().getPlayerTier(player).getDisplayName();
            }
            return "Default";
        }

        // %jobsorder_reward_multiplier%
        if (lower.equals("reward_multiplier")) {
            double mult = plugin.getReputationBridgeService() != null
                    ? plugin.getReputationBridgeService().getRewardMultiplier(player)
                    : 1.0;
            return String.format(java.util.Locale.US, "%.2f", mult);
        }

        // %jobsorder_order_multiplier%
        if (lower.equals("order_multiplier")) {
            double mult = plugin.getReputationBridgeService() != null
                    ? plugin.getReputationBridgeService().getOrderMultiplier(player)
                    : 1.0;
            return String.format(java.util.Locale.US, "%.2f", mult);
        }

        return null;
    }
}
