package id.seria.jobs.service;

import id.seria.core.hooks.economy.EconomyHook;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Transaksi mata uang SeriaJobs di atas {@link EconomyHook} SeriaCore.
 * Tanpa currency = Gins (Vault). Dengan currency = mata uang ExcellentEconomy (serium, reputasi, ...).
 */
public class EconomyService {

    private static final String VAULT = "vault";

    private EconomyHook hook() {
        return EconomyHook.getInstance();
    }

    public boolean isVaultAvailable() {
        return hook().isVaultAvailable();
    }

    public boolean isCurrencyEngineAvailable() {
        return hook().isExcellentEconomyAvailable();
    }

    // ---- Gins (Vault) ----

    public boolean has(@NotNull Player player, double amount) {
        if (amount <= 0) return true;
        return hook().hasBalance(player, VAULT, amount);
    }

    public void withdraw(@NotNull Player player, double amount) {
        hook().withdraw(player, VAULT, amount);
    }

    public void deposit(@NotNull Player player, double amount) {
        deposit(player, null, amount);
    }

    public double getBalance(@NotNull Player player) {
        return hook().getBalance(player, VAULT);
    }

    // ---- Currency tertentu ----

    /**
     * Saldo currency ExcellentEconomy; 0 bila currency tidak terdaftar (tanpa jatuh ke saldo Vault).
     */
    public double getBalance(@NotNull Player player, @NotNull String currencyId) {
        if (!hook().hasCurrency(currencyId)) return 0.0;
        return hook().getBalance(player, currencyId);
    }

    public boolean withdraw(@NotNull Player player, @NotNull String currencyId, double amount) {
        if (!hook().hasCurrency(currencyId)) return false;
        return hook().withdraw(player, currencyId, amount);
    }

    /**
     * Deposit ke currency tertentu, atau ke Gins (Vault, lalu currency utama ExcellentEconomy) bila currency kosong.
     * Bila currency yang diminta tidak terdaftar, saldo masuk ke Vault.
     */
    public boolean deposit(@NotNull Player player, @Nullable String currencyId, double amount) {
        if (amount <= 0) return false;
        if (currencyId != null && !currencyId.isBlank() && hook().hasCurrency(currencyId)) {
            return hook().deposit(player, currencyId, amount);
        }
        if (hook().isVaultAvailable()) {
            return hook().deposit(player, VAULT, amount);
        }
        String primary = hook().getPrimaryCurrencyId();
        return primary != null && hook().deposit(player, primary, amount);
    }
}
