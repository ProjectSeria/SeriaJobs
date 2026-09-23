package id.seria.gacha;

import id.seria.gacha.configurations.SettingsConfiguration;
import id.seria.gacha.lib.common.services.services.database.DatabaseProvider;
import id.seria.gacha.limbo.debug.DebugExecutorMonitor;
import id.seria.gacha.limbo.item.ItemStorage;
import id.seria.gacha.managers.CratesManager;
import id.seria.gacha.managers.KeysManager;
import id.seria.gacha.managers.PlayersManager;
import id.seria.gacha.managers.crates.rewards.GlobalRewardStatsTracker;
import id.seria.gacha.sdk.hooks.bettermodel.BetterModelHookProvider;
import id.seria.gacha.sdk.hooks.excellenteconomy.ExcellentEconomyHookProvider;
import id.seria.gacha.sdk.hooks.playerpoints.PlayerPointsHookProvider;
import id.seria.gacha.sdk.hooks.vault.VaultHookProvider;
import id.seria.gacha.sdk.hooks.votingplugin.VotingPluginHookProvider;
import id.seria.gacha.sdk.platforms.server.components.ServerLogger;
import id.seria.gacha.sdk.platforms.server.components.ServerScheduler;

public class Crates {
   private static DebugExecutorMonitor debugHandler;

   public Crates() {
   }

   public static PhoenixCrates getPlugin() {
      return PhoenixCrates.getLegacyInstance();
   }

   public static PlayersManager getPlayersManager() {
      return PhoenixCrates.getLegacyInstance().getPlayersManager();
   }

   public static ServerScheduler getScheduler() {
      return PhoenixCrates.getLegacyInstance().getScheduler();
   }

   public static KeysManager getKeysManager() {
      return PhoenixCrates.getLegacyInstance().getKeysManager();
   }

   public static CratesManager getCratesManager() {
      return PhoenixCrates.getLegacyInstance().getCratesManager();
   }

   public static GlobalRewardStatsTracker getGlobalRewardStatsTracker() {
      return PhoenixCrates.getLegacyInstance().getCratesManager().getGlobalRewardStatsTracker();
   }

   public static ItemStorage getItemStorage() {
      return PhoenixCrates.getLegacyInstance().getItemStorage();
   }

   public static boolean isVaultInstalled() {
      return PhoenixCrates.getLegacyInstance().getHookRegistry().isHookEnabled(VaultHookProvider.class);
   }

   public static boolean isPlayerPointsInstalled() {
      return PhoenixCrates.getLegacyInstance().getHookRegistry().isHookEnabled(PlayerPointsHookProvider.class);
   }

   public static boolean isVotingPluginInstalled() {
      return PhoenixCrates.getLegacyInstance().getHookRegistry().isHookEnabled(VotingPluginHookProvider.class);
   }

   public static boolean isExcellentEconomyInstalled() {
      return PhoenixCrates.getLegacyInstance().getHookRegistry().isHookEnabled(ExcellentEconomyHookProvider.class);
   }

   public static boolean isBetterModelInstalled() {
      return PhoenixCrates.getLegacyInstance().getHookRegistry().isHookEnabled(BetterModelHookProvider.class);
   }

   public static ServerLogger getLogger() {
      return PhoenixCrates.getLegacyInstance().getLogger();
   }

   public static SettingsConfiguration getConfiguration() {
      return PhoenixCrates.getLegacyInstance().getConfiguration();
   }

   public static boolean isDebugEnabled() {
      return PhoenixCrates.getLegacyInstance().getConfiguration().isDebug();
   }

   public static DatabaseProvider getDatabaseProvider() {
      return PhoenixCrates.getLegacyInstance().getDatabaseProvider();
   }

   public static void debug(String message) {
      if (isDebugEnabled()) {
         if (debugHandler == null) {
            debugHandler = new DebugExecutorMonitor(PhoenixCrates.getLegacyInstance());
            debugHandler.startPeriodicLogging();
         }

         debugHandler.debug(message);
      }
   }
}
