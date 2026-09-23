package id.seria.gacha.sdk.hooks.excellenteconomy;

import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import su.nightexpress.excellenteconomy.EconomyPlugin;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;

public class ExcellentEconomyHook extends ServerHook {
   private ExcellentEconomyAPI api;

   protected ExcellentEconomyHook() {
   }

   @Override
   public void init() {
   }

   @Override
   public void load() {
      Plugin plugin = Bukkit.getPluginManager().getPlugin("ExcellentEconomy");
      this.api = plugin instanceof EconomyPlugin ? ((EconomyPlugin)plugin).getAPI() : null;
   }

   @Override
   public void unload() {
      if (this.api != null) {
         this.api = null;
      }
   }

   @Override
   public void reload() {
      this.unload();
      this.load();
   }

   public ExcellentEconomyHookProvider getProvider() {
      return (ExcellentEconomyHookProvider)super.getProvider();
   }

   private ExcellentEconomyAPI getApi() {
      Validate.isTrue(this.api != null, "ExcellentEconomy not found or initialized.");
      return this.api;
   }

   public Set<String> getCurrencyIds() {
      Set<String> ids = new HashSet<>();

      for (ExcellentCurrency currency : this.getApi().getCurrencies()) {
         ids.add(currency.getId());
      }

      return ids;
   }

   public boolean hasCurrency(String currencyId) {
      return this.getApi().hasCurrency(currencyId);
   }

   public ExcellentCurrency getCurrency(String currencyId) {
      return this.getApi().getCurrency(currencyId);
   }

   public double getBalance(Player player, String currencyId) {
      return this.getApi().getBalance(player, currencyId);
   }

   public boolean has(Player player, String currencyId, double amount) {
      return this.getBalance(player, currencyId) >= amount;
   }

   public boolean deposit(Player player, String currencyId, double amount) {
      return this.getApi().deposit(player, currencyId, amount);
   }

   public boolean withdraw(Player player, String currencyId, double amount) {
      return this.getApi().withdraw(player, currencyId, amount);
   }

   public boolean setBalance(Player player, String currencyId, double amount) {
      return this.getApi().setBalance(player, currencyId, amount);
   }

   public String format(String currencyId, double amount) {
      ExcellentCurrency currency = this.getCurrency(currencyId);
      return currency != null ? currency.format(amount) : String.valueOf(amount);
   }

   public boolean isDecimal(String currencyId) {
      ExcellentCurrency currency = this.getCurrency(currencyId);
      return currency == null || currency.isDecimal();
   }
}
