package id.seria.gacha.sdk.hooks.vault;

import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook extends ServerHook {
   private Economy defaultEconomy;

   protected VaultHook() {
   }

   @Override
   public void init() {
   }

   @Override
   public void load() {
      RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
      this.defaultEconomy = rsp != null ? (Economy)rsp.getProvider() : null;
   }

   @Override
   public void unload() {
      if (this.defaultEconomy != null) {
         this.defaultEconomy = null;
      }
   }

   @Override
   public void reload() {
      this.unload();
      this.load();
   }

   public VaultHookProvider getProvider() {
      return (VaultHookProvider)super.getProvider();
   }

   public void addMoney(OfflinePlayer player, double amount) {
      Validate.isTrue(this.defaultEconomy != null, "Vault not found or initialized.");
      this.defaultEconomy.depositPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), amount);
   }

   public void remMoney(OfflinePlayer player, double amount) {
      Validate.isTrue(this.defaultEconomy != null, "Vault not found or initialized.");
      this.defaultEconomy.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), amount);
   }

   public boolean hasMoney(OfflinePlayer player, double amount) {
      Validate.isTrue(this.defaultEconomy != null, "Vault not found or initialized.");
      return this.getBalance(player) >= amount;
   }

   public double getBalance(OfflinePlayer player) {
      Validate.isTrue(this.defaultEconomy != null, "Vault not found or initialized.");
      return this.defaultEconomy.getBalance(Bukkit.getOfflinePlayer(player.getUniqueId()));
   }

   public String format(double value) {
      Validate.isTrue(this.defaultEconomy != null, "Vault not found or initialized.");
      return this.defaultEconomy.format(value);
   }
}
