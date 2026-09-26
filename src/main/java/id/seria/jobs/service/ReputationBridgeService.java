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

    public ReputationBridgeService(@NotNull SeriaJobs plugin) {
        this.plugin = plugin;
        this.reputationPlaceholder = plugin.getConfig().getString("reputation.placeholder", "%excelenteconomy_balance_reputasi%");
    }

    public void reload() {
        this.reputationPlaceholder = plugin.getConfig().getString("reputation.placeholder", "%excelenteconomy_balance_reputasi%");
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
        if (plugin.getHookManager() != null && plugin.getHookManager().isExcellentEconomyLoaded()) {
            try {
                double bal = plugin.getHookManager().getExcellentEconomyHook().getBalance(player, "reputasi");
                if (bal > 0) {
                    return (int) Math.round(bal);
                }
            } catch (Throwable ignored) {}
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
