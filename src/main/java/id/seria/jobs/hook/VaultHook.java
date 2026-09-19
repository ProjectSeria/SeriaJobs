package id.seria.jobs.hook;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    private Economy economy;

    public boolean setup() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean deposit(Player player, double amount) {
        if (economy == null || player == null || amount <= 0) return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public double getBalance(Player player) {
        if (economy == null || player == null) return 0;
        return economy.getBalance(player);
    }

    public Economy getEconomy() {
        return economy;
    }
}
