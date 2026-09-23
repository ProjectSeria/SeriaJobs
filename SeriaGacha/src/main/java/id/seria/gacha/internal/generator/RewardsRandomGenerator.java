package id.seria.gacha.internal.generator;

import com.google.common.collect.Iterables;
import id.seria.gacha.Crates;
import id.seria.gacha.api.crate.events.CrateRewardSelectionEvent;
import id.seria.gacha.api.key.Key;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.api.reward.GuaranteedRewardType;
import id.seria.gacha.api.reward.RewardsGenerator;
import id.seria.gacha.api.session.OpeningContext;
import id.seria.gacha.facades.RewardFacade;
import id.seria.gacha.lib.common.utils.utility.HashUtil;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.players.PlayerData;
import id.seria.gacha.managers.reward.CustomReward;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;

public class RewardsRandomGenerator implements RewardsGenerator {
   private final Crate crate;
   private final OpeningContext.RandomOpeningContext data;
   private Set<String> excludedIdentifiers = Collections.emptySet();

   public RewardsRandomGenerator(Crate crate, OpeningContext.RandomOpeningContext data) {
      this.crate = crate;
      this.data = data;
   }

   public void setExcludedIdentifiers(Set<String> excludedIdentifiers) {
      this.excludedIdentifiers = excludedIdentifiers;
   }

   @Override
   public List<? extends BaseReward> generateRewards() {
      int maxRewardsPerOpen = this.crate.getType().getMaxWinRewards();
      List<BaseReward> rewards = new ArrayList<>();
      PlayerData record = this.crate.getPlugin().getPlayersManager().getCachedDataNow(this.data.getPlayer());
      boolean skipSameReward = this.crate.getType().getRerolling().isSkipSameReward();
      if (skipSameReward) {
         this.excludedIdentifiers = new HashSet<>(this.excludedIdentifiers);
      }

      if (this.data.getKeysAmount().isEmpty()) {
         int openedAmounts = record.getOpenedCratesAmount(this.crate.getType().getIdentifier());

         for (int j = 0; j < maxRewardsPerOpen; j++) {
            BaseReward reward = this.generateReward(null, openedAmounts);
            rewards.add(reward);
            if (skipSameReward) {
               this.excludedIdentifiers.add(reward.getIdentifier());
            }
         }

         return rewards;
      } else {
         int openedAmounts = record.getOpenedCratesAmount(this.crate.getType().getIdentifier()) - this.data.getRequiredKeys();

         for (Entry<? extends Key, Integer> entry : this.data.getKeysAmount().entrySet()) {
            Key key = entry.getKey();
            int amount = entry.getValue();

            for (int i = 0; i < amount; i++) {
               openedAmounts++;

               for (int j = 0; j < maxRewardsPerOpen; j++) {
                  BaseReward reward = this.generateReward(key, openedAmounts);
                  rewards.add(reward);
                  if (skipSameReward) {
                     this.excludedIdentifiers.add(reward.getIdentifier());
                  }
               }
            }
         }

         return rewards;
      }
   }

   @Override
   public BaseReward generateReward(Key key) {
      int openedAmounts = this.crate
         .getPlugin()
         .getPlayersManager()
         .getCachedDataNow(this.data.getPlayer())
         .getOpenedCratesAmount(this.crate.getType().getIdentifier());
      return this.generateReward(key, openedAmounts);
   }

   private BaseReward generateReward(Key key, int openedAmounts) {
      BaseReward guaranteed = this.checkGuaranteedReward(openedAmounts);
      if (guaranteed == null || this.crate.getType().getRerolling().isSkipSameReward() && this.excludedIdentifiers.contains(guaranteed.getIdentifier())) {
         List<? extends BaseReward> rewards = this.crate.getAvailableRewards(this.data.getPlayer());
         boolean skipSameReward = this.crate.getType().getRerolling().isSkipSameReward();
         if (skipSameReward && !this.excludedIdentifiers.isEmpty()) {
            List<? extends BaseReward> filtered = rewards.stream()
               .filter(reward -> !this.excludedIdentifiers.contains(reward.getIdentifier()))
               .collect(Collectors.toList());
            if (!filtered.isEmpty()) {
               rewards = filtered;
            }
         }

         CrateRewardSelectionEvent selectionEvent = new CrateRewardSelectionEvent(this.crate, this.data.getPlayer(), key, rewards);
         Bukkit.getPluginManager().callEvent(selectionEvent);
         if (selectionEvent.isHandled() && selectionEvent.getSelectedReward() != null) {
            return selectionEvent.getSelectedReward();
         }

         rewards = selectionEvent.getCandidates();
         return this.selectByWeight(rewards);
      } else {
         return guaranteed;
      }
   }

   private BaseReward checkGuaranteedReward(int openedAmounts) {
      CrateType type = this.crate.getType();
      if (type.getGuaranteedRewardType() == GuaranteedRewardType.DISABLED) {
         return null;
      }

      PlayerData record = this.crate.getPlugin().getPlayersManager().getCachedDataNow(this.data.getPlayer());

      for (CustomReward reward : type.getRegisteredRewards()) {
         if (reward.getGuaranteedWin() > 0) {
            if (switch (type.getGuaranteedRewardType()) {
                  case SEQUENTIAL -> reward.getGuaranteedWin() == openedAmounts;
                  case REPETITIVE -> openedAmounts % reward.getGuaranteedWin() == 0;
                  default -> false;
               }
               && reward.isValid()
               && !reward.isRestricted(this.data.getPlayer())
               && (reward.getWinLimits() < 0 || record.getRewardWinAmount(reward.getIdentifier()) < reward.getWinLimits())
               && (
                  reward.getGlobalWinLimit() < 0
                     || Crates.getGlobalRewardStatsTracker().getWinAmount(this.crate.getType().getIdentifier(), reward.getIdentifier())
                        < reward.getGlobalWinLimit()
               )) {
               return RewardFacade.wrap(reward);
            }
         }
      }

      return null;
   }

   private BaseReward selectByWeight(List<? extends BaseReward> items) {
      LinkedHashMap<? extends BaseReward, Double> ordered = HashUtil.sortByValue(
         (HashMap)items.stream().collect(Collectors.toMap(i -> (BaseReward)i, BaseReward::getWeight))
      );
      int max = (int)ordered.values().stream().mapToDouble(f -> f * 100.0).sum();
      int random = MathUtil.random(0, max);
      int acc = 0;

      for (Entry<? extends BaseReward, Double> entry : ordered.entrySet()) {
         acc += (int)(entry.getValue() * 100.0);
         if (random <= acc) {
            return entry.getKey();
         }
      }

      return (BaseReward)Iterables.getLast(ordered.keySet());
   }
}
