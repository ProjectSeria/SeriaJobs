package id.seria.gacha.managers.crates.cost;

import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.sdk.hooks.excellenteconomy.ExcellentEconomyHook;
import id.seria.gacha.sdk.hooks.playerpoints.PlayerPointsHook;
import id.seria.gacha.sdk.hooks.vault.VaultHook;
import id.seria.gacha.sdk.hooks.votingplugin.VotingPluginHook;
import org.bukkit.entity.Player;

public enum CostEngineType implements id.seria.gacha.api.crate.cost.CostEngineType {
   NONE {
      @Override
      public boolean isInstalled() {
         return true;
      }

      @Override
      public boolean isWholeNumber() {
         return true;
      }

      @Override
      public boolean hasEnough(Player player, double amount) {
         return true;
      }

      @Override
      public void withdraw(Player player, double amount) {
      }

      @Override
      public String format(double amount) {
         return "None";
      }
   },
   LEVEL {
      @Override
      public boolean isInstalled() {
         return true;
      }

      @Override
      public boolean isWholeNumber() {
         return true;
      }

      @Override
      public boolean hasEnough(Player player, double amount) {
         return player.getLevel() >= (int)amount;
      }

      @Override
      public void withdraw(Player player, double amount) {
         player.setLevel(Math.max(0, player.getLevel() - (int)amount));
      }

      @Override
      public String format(double amount) {
         return (int)amount + " Levels";
      }
   },
   VAULT {
      @Override
      public boolean isInstalled() {
         return Crates.isVaultInstalled();
      }

      @Override
      public boolean isWholeNumber() {
         return false;
      }

      @Override
      public boolean hasEnough(Player player, double amount) {
         return this.getHook().hasMoney(player, amount);
      }

      @Override
      public void withdraw(Player player, double amount) {
         this.getHook().remMoney(player, amount);
      }

      @Override
      public String format(double amount) {
         return this.getHook().format(amount);
      }

      private VaultHook getHook() {
         return PhoenixCrates.getLegacyInstance().getHookRegistry().getHook(VaultHook.class);
      }
   },
   PLAYERPOINTS {
      @Override
      public boolean isInstalled() {
         return Crates.isPlayerPointsInstalled();
      }

      @Override
      public boolean isPremium() {
         return true;
      }

      @Override
      public boolean isWholeNumber() {
         return true;
      }

      @Override
      public boolean hasEnough(Player player, double amount) {
         return this.getHook().has(player, (int)amount);
      }

      @Override
      public void withdraw(Player player, double amount) {
         this.getHook().take(player, (int)amount);
      }

      @Override
      public String format(double amount) {
         return this.getHook().format((int)amount);
      }

      private PlayerPointsHook getHook() {
         return PhoenixCrates.getLegacyInstance().getHookRegistry().getHook(PlayerPointsHook.class);
      }
   },
   VOTINGPLUGIN {
      @Override
      public boolean isInstalled() {
         return Crates.isVotingPluginInstalled();
      }

      @Override
      public boolean isPremium() {
         return true;
      }

      @Override
      public boolean isWholeNumber() {
         return true;
      }

      @Override
      public boolean hasEnough(Player player, double amount) {
         return this.getHook().has(player, (int)amount);
      }

      @Override
      public void withdraw(Player player, double amount) {
         this.getHook().removePoints(player, (int)amount);
      }

      @Override
      public String format(double amount) {
         return this.getHook().format((int)amount);
      }

      private VotingPluginHook getHook() {
         return PhoenixCrates.getLegacyInstance().getHookRegistry().getHook(VotingPluginHook.class);
      }
   },
   EXCELLENTECONOMY {
      @Override
      public boolean isInstalled() {
         return Crates.isExcellentEconomyInstalled();
      }

      @Override
      public boolean isPremium() {
         return true;
      }

      @Override
      public boolean isWholeNumber() {
         return false;
      }

      @Override
      public boolean isWholeNumber(String currencyId) {
         return !this.getHook().isDecimal(currencyId);
      }

      @Override
      public boolean hasEnough(Player player, double amount) {
         return false;
      }

      @Override
      public boolean hasEnough(Player player, double amount, String currencyId) {
         return this.getHook().has(player, currencyId, amount);
      }

      @Override
      public void withdraw(Player player, double amount) {
      }

      @Override
      public void withdraw(Player player, double amount, String currencyId) {
         this.getHook().withdraw(player, currencyId, amount);
      }

      @Override
      public String format(double amount) {
         return String.valueOf(amount);
      }

      @Override
      public String format(double amount, String currencyId) {
         return this.getHook().format(currencyId, amount);
      }

      private ExcellentEconomyHook getHook() {
         return PhoenixCrates.getLegacyInstance().getHookRegistry().getHook(ExcellentEconomyHook.class);
      }
   };

   CostEngineType() {
   }

   @Override
   public int getOrdinal() {
      return this.ordinal();
   }

   @Override
   public String getName() {
      return this.name();
   }

   public abstract boolean isInstalled();

   public boolean isPremium() {
      return false;
   }

   public abstract boolean isWholeNumber();

   public boolean isWholeNumber(String currencyId) {
      return this.isWholeNumber();
   }

   public abstract boolean hasEnough(Player var1, double var2);

   public boolean hasEnough(Player player, double amount, String currencyId) {
      return this.hasEnough(player, amount);
   }

   public abstract void withdraw(Player var1, double var2);

   public void withdraw(Player player, double amount, String currencyId) {
      this.withdraw(player, amount);
   }

   public abstract String format(double var1);

   public String format(double amount, String currencyId) {
      return this.format(amount);
   }
}
