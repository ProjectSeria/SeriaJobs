package id.seria.jobs.service;

import id.seria.jobs.SeriaJobs;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstraksi transaksi mata uang Gins berbasis Vault API.
 */
public class EconomyService {

    private final SeriaJobs plugin;
    private Economy vaultEconomy;

    public EconomyService(@NotNull SeriaJobs plugin) {
        this.plugin = plugin;
        initVault();
    }

    private void initVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.vaultEconomy = rsp.getProvider();
            }
        }
    }

    @Nullable
    public Economy getVaultEconomy() {
        if (this.vaultEconomy == null) {
            initVault();
        }
        return this.vaultEconomy;
    }

    public boolean has(@NotNull Player player, double amount) {
        if (amount <= 0) return true;
        Economy econ = getVaultEconomy();
        if (econ == null) return false;
        return econ.has(player, amount);
    }

    public void withdraw(@NotNull Player player, double amount) {
        if (amount <= 0) return;
        Economy econ = getVaultEconomy();
        if (econ != null) {
            econ.withdrawPlayer(player, amount);
        }
    }

    public void deposit(@NotNull Player player, double amount) {
        if (amount <= 0) return;
        Economy econ = getVaultEconomy();
        if (econ != null) {
            econ.depositPlayer(player, amount);
        }
    }

    public double getBalance(@NotNull Player player) {
        Economy econ = getVaultEconomy();
        if (econ == null) return 0.0;
        return econ.getBalance(player);
    }
}
