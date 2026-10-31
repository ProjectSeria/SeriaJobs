package id.seria.jobs.service;

import id.seria.jobs.SeriaJobs;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Membaca nilai reputasi dari plugin eksternal (ExcellentEconomy) melalui
 * PlaceholderAPI untuk kalkulasi pembukaan slot ke-4.
 */
public class ReputationBridgeService {

    private final SeriaJobs plugin;
    private String reputationPlaceholder;
    private final java.util.List<id.seria.jobs.model.ReputationTier> tiers = new java.util.ArrayList<>();

    public ReputationBridgeService(@NotNull SeriaJobs plugin) {
        this.plugin = plugin;
        this.reputationPlaceholder = plugin.getConfig().getString("reputation.placeholder", "%excellenteconomy_balance_reputasi%");
        loadTiers();
    }

    public void reload() {
        this.reputationPlaceholder = plugin.getConfig().getString("reputation.placeholder", "%excellenteconomy_balance_reputasi%");
        loadTiers();
    }

    private void loadTiers() {
        tiers.clear();
        org.bukkit.configuration.file.FileConfiguration cfg = plugin.getConfig();

        if (cfg.isList("reputation.tiers")) {
            java.util.List<?> rawList = cfg.getList("reputation.tiers");
            if (rawList != null) {
                for (Object item : rawList) {
                    if (item instanceof java.util.Map<?, ?> map) {
                        int req = getIntFromMap(map, "required", "reputation", "threshold", "min-reputation", "min_reputation", "amount");
                        double rewardMult = getDoubleFromMap(map, "reward-multiplier", "reward_multiplier", "reward", "rewards");
                        double orderMult = getDoubleFromMap(map, "order-multiplier", "order_multiplier", "amount-multiplier", "amount_multiplier", "order");
                        int slots = getIntFromMap(map, "slots", "allowed-slots", "allowed_slots", "slot");
                        String name = getStringFromMap(map, "display-name", "display_name", "name");

                        tiers.add(new id.seria.jobs.model.ReputationTier(req, rewardMult, orderMult, slots, name));
                    }
                }
            }
        } else if (cfg.isConfigurationSection("reputation.tiers")) {
            org.bukkit.configuration.ConfigurationSection sec = cfg.getConfigurationSection("reputation.tiers");
            if (sec != null) {
                for (String key : sec.getKeys(false)) {
                    org.bukkit.configuration.ConfigurationSection sub = sec.getConfigurationSection(key);
                    if (sub != null) {
                        int req = sub.getInt("required", sub.getInt("reputation", sub.getInt("threshold", 0)));
                        double rewardMult = sub.getDouble("reward-multiplier", sub.getDouble("reward_multiplier", sub.getDouble("reward", 1.0)));
                        double orderMult = sub.getDouble("order-multiplier", sub.getDouble("order_multiplier", sub.getDouble("order", 1.0)));
                        int slots = sub.getInt("slots", sub.getInt("allowed-slots", 0));
                        String name = sub.getString("display-name", sub.getString("display_name", sub.getString("name", key)));

                        tiers.add(new id.seria.jobs.model.ReputationTier(req, rewardMult, orderMult, slots, name));
                    }
                }
            }
        }

        // Sort ascending by required reputation
        tiers.sort(java.util.Comparator.comparingInt(id.seria.jobs.model.ReputationTier::getRequiredReputation));
        plugin.getLogger().info("Loaded " + tiers.size() + " reputation tier(s) from config.");
    }

    private int getIntFromMap(java.util.Map<?, ?> map, String... keys) {
        for (String k : keys) {
            Object val = map.get(k);
            if (val instanceof Number n) return n.intValue();
            if (val instanceof String s) {
                try { return Integer.parseInt(s.trim()); } catch (NumberFormatException ignored) {}
            }
        }
        return 0;
    }

    private double getDoubleFromMap(java.util.Map<?, ?> map, String... keys) {
        for (String k : keys) {
            Object val = map.get(k);
            if (val instanceof Number n) return n.doubleValue();
            if (val instanceof String s) {
                try { return Double.parseDouble(s.trim()); } catch (NumberFormatException ignored) {}
            }
        }
        return 1.0;
    }

    private String getStringFromMap(java.util.Map<?, ?> map, String... keys) {
        for (String k : keys) {
            Object val = map.get(k);
            if (val != null) return String.valueOf(val);
        }
        return null;
    }

    @NotNull
    public java.util.List<id.seria.jobs.model.ReputationTier> getTiers() {
        return java.util.Collections.unmodifiableList(tiers);
    }

    @NotNull
    public id.seria.jobs.model.ReputationTier getTierForReputation(int reputation) {
        id.seria.jobs.model.ReputationTier matched = null;
        for (id.seria.jobs.model.ReputationTier tier : tiers) {
            if (reputation >= tier.getRequiredReputation()) {
                matched = tier;
            } else {
                break;
            }
        }
        if (matched != null) {
            return matched;
        }
        return new id.seria.jobs.model.ReputationTier(0, 1.0, 1.0, 0, "Default");
    }

    @NotNull
    public id.seria.jobs.model.ReputationTier getPlayerTier(@Nullable Player player) {
        int rep = getPlayerReputation(player);
        return getTierForReputation(rep);
    }

    public double getRewardMultiplier(@Nullable Player player) {
        if (player == null) return 1.0;
        return getPlayerTier(player).getRewardMultiplier();
    }

    public double getOrderMultiplier(@Nullable Player player) {
        if (player == null) return 1.0;
        return getPlayerTier(player).getOrderMultiplier();
    }

    @NotNull
    public String getReputationPlaceholder() {
        return reputationPlaceholder;
    }

    public void setReputationPlaceholder(@NotNull String reputationPlaceholder) {
        this.reputationPlaceholder = reputationPlaceholder;
    }

    /**
     * Parsing nilai placeholder via PlaceholderAPI.
     * Konversi hasil parsing ke integer (fallback 0 jika parsing gagal).
     */
    public int getPlayerReputation(@Nullable Player player) {
        if (player == null) return 0;

        // 1. Direct ExcellentEconomy Hook check if available
        double bal = plugin.getEconomyService().getBalance(player, "reputasi");
        if (bal > 0) {
            return (int) Math.round(bal);
        }

        // 2. PlaceholderAPI parsing
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                String parsed = PlaceholderAPI.setPlaceholders(player, reputationPlaceholder);
                if (parsed != null && !parsed.isEmpty() && !parsed.equals(reputationPlaceholder)) {
                    // Strip commas and formatting
                    String clean = parsed.replace(",", "").replace(".", "").trim();
                    // If it was a decimal like 50.00, handle appropriately
                    if (parsed.contains(".")) {
                        try {
                            double d = Double.parseDouble(parsed.replace(",", "").trim());
                            return (int) Math.round(d);
                        } catch (NumberFormatException ignored) {}
                    }
                    return Integer.parseInt(clean);
                }
            } catch (Throwable ignored) {}
        }

        return 0;
    }
}
