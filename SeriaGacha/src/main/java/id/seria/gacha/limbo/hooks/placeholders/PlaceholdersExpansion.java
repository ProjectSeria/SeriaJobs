package id.seria.gacha.limbo.hooks.placeholders;

import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.reward.GuaranteedRewardType;
import id.seria.gacha.facades.KeyFacade;
import id.seria.gacha.lib.common.utils.utility.TimeUtil;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.rerolling.CrateRerolling;
import id.seria.gacha.managers.crates.rerolling.RerollGroup;
import id.seria.gacha.managers.crates.rewards.GuaranteedRewardCalculator;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.managers.players.PlayerData;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.sdk.hooks.placeholderapi.expansions.PlaceholderRequester;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlaceholdersExpansion implements PlaceholderRequester {
   private final PhoenixCrates plugin;

   public PlaceholdersExpansion(PhoenixCrates plugin) {
      this.plugin = plugin;
   }

   @Override
   public String onRequest(OfflinePlayer player, String params) {
      if (player == null) {
         return null;
      }

      if (params.startsWith("keys")) {
         int seperatorIndex = params.indexOf("_");
         if (seperatorIndex != -1 && seperatorIndex < params.length() - 1) {
            String keyIdentifier = params.substring(seperatorIndex + 1);
            Key key = this.plugin.getKeysManager().getKeyByIdentifier(keyIdentifier);
            if (key != null && key.isEnabled()) {
               PlayerData data = this.plugin.getPlayersManager().getPlayerIfCached(player, true);
               if (data == null) {
                  return "N/A";
               }

               int count = data.getVirtualKeys(keyIdentifier);
               if (player.isOnline()) {
                  Player online = player.getPlayer();

                  for (ItemStack item : online.getInventory().getContents()) {
                     if (item != null && KeyFacade.getKeysMaterial().contains(item.getType())) {
                        String extractedType = KeyFacade.extractKey(item);
                        if (keyIdentifier.equalsIgnoreCase(extractedType)) {
                           count += item.getAmount();
                        }
                     }
                  }
               }

               return String.valueOf(count);
            }

            return "0";
         }
      } else if (params.startsWith("cooldown")) {
         int separatorIndex = params.indexOf("_");
         if (separatorIndex != -1 && separatorIndex < params.length() - 1) {
            String crateIdentifier = params.substring(separatorIndex + 1);
            CrateType crateType = this.plugin.getCratesManager().getTypeByIdentifier(crateIdentifier);
            if (crateType != null) {
               PlayerData data = this.plugin.getPlayersManager().getPlayerIfCached(player, true);
               if (data == null) {
                  return "N/A";
               }

               long currentCooldown = data.getCrateCooldown(crateType.getIdentifier()) + (crateType.getOpenCooldownSeconds() + 1) * 1000;
               long leftMillis = Math.max(0L, currentCooldown - System.currentTimeMillis());
               if (leftMillis == 0L) {
                  return "0s";
               }

               int seconds = (int)(leftMillis / 1000.0);
               if (seconds == 0) {
                  return "0s";
               }

               return TimeUtil.formatSecondsShort(seconds);
            }
         }
      } else if (params.startsWith("totalopened")) {
         int separatorIndex = params.indexOf("_");
         if (separatorIndex == -1 || separatorIndex >= params.length() - 1) {
            PlayerData data = this.plugin.getPlayersManager().getPlayerIfCached(player, true);
            if (data == null) {
               return "N/A";
            }

            return String.valueOf(
               data.getOpenedCratesAmount()
                  .entrySet()
                  .stream()
                  .filter(entry -> this.plugin.getCratesManager().getTypeByIdentifier(entry.getKey()) != null)
                  .map(entry -> entry.getValue())
                  .mapToInt(Integer::intValue)
                  .sum()
            );
         }

         String crateIdentifier = params.substring(separatorIndex + 1);
         CrateType crateType = this.plugin.getCratesManager().getTypeByIdentifier(crateIdentifier);
         if (crateType != null) {
            PlayerData data = this.plugin.getPlayersManager().getPlayerIfCached(player, true);
            if (data == null) {
               return "N/A";
            }

            return String.valueOf(data.getOpenedCratesAmount().getOrDefault(crateIdentifier, 0));
         }
      } else if (params.startsWith("rerolls_available")) {
         if (!player.isOnline()) {
            return "N/A";
         }

         int firstSeparatorIndex = params.indexOf("_");
         int secondSeparatorIndex = params.indexOf("_", firstSeparatorIndex + 1);
         if (secondSeparatorIndex != -1 && secondSeparatorIndex < params.length() - 1) {
            String crateIdentifier = params.substring(secondSeparatorIndex + 1);
            CrateType crateType = this.plugin.getCratesManager().getTypeByIdentifier(crateIdentifier);
            if (crateType != null) {
               CrateRerolling rerolling = crateType.getRerolling();
               Player online = player.getPlayer();
               int rerollsAvailable = 0;
               if (rerolling.isEnabled()) {
                  rerollsAvailable = rerolling.getFreeRolls()
                     + rerolling.getGroups()
                        .stream()
                        .filter(group -> online.hasPermission(group.getPermission()))
                        .mapToInt(RerollGroup::getRerolls)
                        .max()
                        .orElse(0);
               }

               PlayerData data = this.plugin.getPlayersManager().getPlayerIfCached(player, true);
               if (data == null) {
                  return "N/A";
               }

               rerollsAvailable += data.getCrateRerolls(crateIdentifier);
               return String.valueOf(rerollsAvailable);
            }
         }
      } else {
         if (params.startsWith("guaranteed_amount_")) {
            String crateIdentifier = params.substring("guaranteed_amount_".length());
            CrateType crateType = this.plugin.getCratesManager().getTypeByIdentifier(crateIdentifier);
            if (crateType != null && crateType.getGuaranteedRewardType() != GuaranteedRewardType.DISABLED) {
               PlayerData data = this.plugin.getPlayersManager().getPlayerIfCached(player, true);
               if (data == null) {
                  return "N/A";
               }

               int remaining = GuaranteedRewardCalculator.getMinRemaining(crateType, data.getOpenedCratesAmount(crateIdentifier));
               return remaining == Integer.MAX_VALUE ? "N/A" : String.valueOf(remaining);
            }

            return "N/A";
         }

         if (params.startsWith("guaranteed_reward_")) {
            String crateIdentifier = params.substring("guaranteed_reward_".length());
            CrateType crateType = this.plugin.getCratesManager().getTypeByIdentifier(crateIdentifier);
            if (crateType != null && crateType.getGuaranteedRewardType() != GuaranteedRewardType.DISABLED) {
               PlayerData data = this.plugin.getPlayersManager().getPlayerIfCached(player, true);
               if (data == null) {
                  return "N/A";
               }

               CustomReward closest = GuaranteedRewardCalculator.getClosestReward(crateType, data.getOpenedCratesAmount(crateIdentifier));
               if (closest == null) {
                  return "N/A";
               }

               return closest.getDisplayItem() != null ? closest.getDisplayItem().getDisplayName() : closest.getIdentifier();
            }

            return "N/A";
         }

         if (params.startsWith("winLimit_")) {
            CrateRewardMatch match = CrateRewardMatch.resolve(this.plugin, params.substring("winLimit_".length()));
            if (match == null) {
               return null;
            }

            PlayerData data = this.plugin.getPlayersManager().getPlayerIfCached(player, true);
            if (data == null) {
               return "N/A";
            }

            int winAmount = data.getRewardWinAmount(match.reward.getIdentifier());
            String winLimit = match.reward.getWinLimits() < 0 ? "\u221e" : String.valueOf(match.reward.getWinLimits());
            return winAmount + "/" + winLimit;
         }

         if (params.startsWith("globalWinLimit_")) {
            CrateRewardMatch match = CrateRewardMatch.resolve(this.plugin, params.substring("globalWinLimit_".length()));
            if (match == null) {
               return null;
            }

            int globalWinAmount = Crates.getCratesManager()
               .getGlobalRewardStatsTracker()
               .getWinAmount(match.crateType.getIdentifier(), match.reward.getIdentifier());
            String globalLimit = match.reward.getGlobalWinLimit() < 0 ? "\u221e" : String.valueOf(match.reward.getGlobalWinLimit());
            return globalWinAmount + "/" + globalLimit;
         }

         if (params.startsWith("rewardTimeLimit_")) {
            CrateRewardMatch match = CrateRewardMatch.resolve(this.plugin, params.substring("rewardTimeLimit_".length()));
            if (match == null) {
               return null;
            }

            long expiry = match.reward.getTimeLimitedExpiry();
            if (expiry <= 0L) {
               return "N/A";
            }

            long remaining = Math.max(0L, expiry - System.currentTimeMillis());
            if (remaining == 0L) {
               return "0s";
            }

            return TimeUtil.formatSecondsShort((int)(remaining / 1000L));
         }

         if (params.startsWith("cooldownGlobalWin_")) {
            CrateRewardMatch match = CrateRewardMatch.resolve(this.plugin, params.substring("cooldownGlobalWin_".length()));
            if (match == null) {
               return null;
            }

            if (match.reward.getGlobalWinLimitCooldown() <= 0L) {
               return "N/A";
            }

            long lastGlobalWin = Crates.getCratesManager()
               .getGlobalRewardStatsTracker()
               .getLastWinTimestamp(match.crateType.getIdentifier(), match.reward.getIdentifier());
            long remaining = Math.max(0L, match.reward.getGlobalWinLimitCooldown() * 1000L - (System.currentTimeMillis() - lastGlobalWin));
            if (remaining == 0L) {
               return "0s";
            }

            return TimeUtil.formatSecondsShort((int)(remaining / 1000L));
         }

         if (params.startsWith("cooldownWinLimit_")) {
            CrateRewardMatch match = CrateRewardMatch.resolve(this.plugin, params.substring("cooldownWinLimit_".length()));
            if (match == null) {
               return null;
            }

            if (match.reward.getWinLimitCooldown() <= 0L) {
               return "N/A";
            }

            PlayerData data = this.plugin.getPlayersManager().getPlayerIfCached(player, true);
            if (data == null) {
               return "N/A";
            }

            long remaining = Math.max(
               0L, match.reward.getWinLimitCooldown() * 1000L - (System.currentTimeMillis() - data.getRewardCooldown(match.reward.getIdentifier()))
            );
            if (remaining == 0L) {
               return "0s";
            }

            return TimeUtil.formatSecondsShort((int)(remaining / 1000L));
         }
      }

      return null;
   }
}
