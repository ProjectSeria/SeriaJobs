package id.seria.gacha.limbo.hooks;

import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

@Deprecated
public class VaultHook {
   private static Economy ECONOMY;

   public VaultHook() {
   }

   private static Economy getEconomy() {
      if (ECONOMY == null) {
         RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
         ECONOMY = rsp != null ? (Economy)rsp.getProvider() : null;
      }

      Validate.isTrue(ECONOMY != null, "Vault not found or initialized.", new Object[0]);
      return ECONOMY;
   }

   public static void addMoney(OfflinePlayer player, double amount) {
      getEconomy().depositPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), amount);
   }

   public static void remMoney(OfflinePlayer player, double amount) {
      getEconomy().withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), amount);
   }

   public static boolean hasMoney(OfflinePlayer player, double amount) {
      return getBalance(player) >= amount;
   }

   public static double getBalance(OfflinePlayer player) {
      return getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getUniqueId()));
   }

   public static String format(double value) {
      return getEconomy().format(value);
   }
}
