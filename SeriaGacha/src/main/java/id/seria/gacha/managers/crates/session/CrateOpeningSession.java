package id.seria.gacha.managers.crates.session;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.events.CratePreRerollEvent;
import id.seria.gacha.api.crate.events.CrateRerollEvent;
import id.seria.gacha.api.crate.events.CrateRewardPlayerEvent;
import id.seria.gacha.api.key.Key;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.api.reward.RewardsGenerator;
import id.seria.gacha.api.sdk.utilities.Validate;
import id.seria.gacha.api.session.OpeningContext;
import id.seria.gacha.api.session.OpeningSession;
import id.seria.gacha.facades.RewardFacade;
import id.seria.gacha.internal.Addons;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.internal.generator.RewardsMassSelectiveGenerator;
import id.seria.gacha.internal.generator.RewardsRandomGenerator;
import id.seria.gacha.internal.generator.RewardsSelectiveGenerator;
import id.seria.gacha.lib.common.modelengine.interfaces.model.MegActiveModel;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.ContainerConfiguration;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.Operations;
import id.seria.gacha.limbo.hooks.ModelEngineHook;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.managers.crates.engine.EngineDisplayComponent;
import id.seria.gacha.managers.crates.engine.EngineType;
import id.seria.gacha.managers.crates.engine.bettermodel.BetterModelDisplayComponent;
import id.seria.gacha.managers.crates.engine.modelengine.ModelEngineDisplayComponent;
import id.seria.gacha.managers.crates.rerolling.CrateRerolling;
import id.seria.gacha.managers.crates.rerolling.RerollGroup;
import id.seria.gacha.managers.crates.rewards.GlobalRewardStatsTracker;
import id.seria.gacha.managers.players.PlayerData;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.managers.reward.RewardMessageType;
import id.seria.gacha.managers.reward.WrappedReward;
import id.seria.gacha.managers.tasks.TaskSubscriber;
import id.seria.gacha.menus.MenuFactory;
import id.seria.gacha.menus.crates.CrateRerollMenu;
import id.seria.gacha.registries.InformationRegistry;
import id.seria.gacha.registries.ScopedValue;
import id.seria.gacha.sdk.hooks.bettermodel.BetterModelDisplay;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import lombok.Generated;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CrateOpeningSession implements OpeningSession, TaskSubscriber {
   private final Crate crate;
   private final OpeningContext context;
   private int rerollsLeft;
   private int extraRerolls;
   private final RewardsGenerator rewardsGenerator;
   private OpeningAnimationHandlerImpl animationHandler;
   private EngineDisplayComponent virtualDisplayComponent;
   private final Map<Integer, Key> rewardKeyMap;
   private final Set<String> wonRewardIdentifiers;
   private Runnable pendingAnimationCallback;
   private boolean replaying;
   private boolean rerolling;
   private boolean unloaded;
   private boolean worldChangeAllowed;
   @Deprecated
   private final List<BaseReward> rewardsGiven = new ArrayList<>();
   private boolean claimed;
   private boolean unloading;

   public CrateOpeningSession(Crate crate, OpeningContext context) {
      this.rewardKeyMap = new HashMap<>();
      this.wonRewardIdentifiers = new HashSet<>();
      this.extraRerolls = 0;
      this.rerollsLeft = 0;
      this.crate = crate;
      this.context = context;
      switch (crate.getType().getRewardsMode()) {
         case RANDOM:
            Validate.isTrue(context instanceof OpeningContext.RandomOpeningContext, "Wrong data \"" + context.getClass().getSimpleName() + "\" for selective");
            this.rewardsGenerator = new RewardsRandomGenerator(this.crate, (OpeningContext.RandomOpeningContext)context);
            Player player = context.getPlayer();
            CrateRerolling rerolling = this.crate.getType().getRerolling();
            if (rerolling.isEnabled()) {
               PlayerData playerData = Crates.getPlayersManager().getCachedDataNow(player);
               this.extraRerolls = playerData.getCrateRerolls(this.crate.getType().getIdentifier());
               this.rerollsLeft = (
                     rerolling.getFreeRolls()
                        + rerolling.getGroups()
                           .stream()
                           .filter(group -> player.hasPermission(group.getPermission()))
                           .mapToInt(RerollGroup::getRerolls)
                           .max()
                           .orElse(0)
                        + this.extraRerolls
                  )
                  * context.getRequiredKeys();
            }
            break;
         case SELECTIVE:
            if (context instanceof OpeningContext.MassSelectiveOpeningContext) {
               this.rewardsGenerator = new RewardsMassSelectiveGenerator(this.crate, (OpeningContext.MassSelectiveOpeningContext)context);
            } else {
               Validate.isTrue(
                  context instanceof OpeningContext.SelectiveOpeningContext, "Wrong data \"" + context.getClass().getSimpleName() + "\" for selective"
               );
               this.rewardsGenerator = new RewardsSelectiveGenerator(this.crate, (OpeningContext.SelectiveOpeningContext)context);
            }
            break;
         default:
            throw new NotImplementedException();
      }
   }

   public void load() {
      try {
         this.animationHandler = new OpeningAnimationHandlerImpl(this, this.getPlayer());

         for (Player player : this.animationHandler.getAudience()) {
            this.crate.hideHologram(player);
         }

         if (this.crate.getPreviewItem().getItemEntity() != null) {
            this.crate.getPreviewItem().getItemEntity().prepareDestroyPacket().sendPacket(this.getPlayer());
         }

         if (this.crate.getPreviewItem().getHolderEntity() != null) {
            this.crate.getPreviewItem().getHolderEntity().prepareDestroyPacket().sendPacket(this.getPlayer());
         }

         this.virtualDisplayComponent = this.crate.getType().getEngineType().createDisplayComponent(this.crate, this.getPlayer());
         this.virtualDisplayComponent.create(false);
         Crates.getScheduler()
            .runSync(
               this.crate.getLocation(),
               () -> {
                  if (!this.crate.getType().getEngineType().equals(EngineType.VANILLA_BLOCK)
                     && !this.crate.getType().getEngineType().equals(EngineType.VANILLA_MODEL)) {
                     this.crate.getDisplayComponent().hide(Lists.newArrayList(new Player[]{this.getPlayer()}));
                  }
               },
               1L
            );
         this.animationHandler.init();
      } catch (Exception e) {
         Crates.getLogger().error("Error preparing crate opening session", e);
      }
   }

   @Override
   public Location getLocation() {
      return this.crate.getLocation();
   }

   @Override
   public boolean isFinished() {
      return this.unloaded || !this.getPlayer().isOnline();
   }

   @Override
   public void tick() {
      if (this.animationHandler != null) {
         if (this.animationHandler.isDone()) {
            if (this.pendingAnimationCallback != null) {
               Runnable callback = this.pendingAnimationCallback;
               this.pendingAnimationCallback = null;
               this.replaying = false;
               callback.run();
            } else {
               this.unload(false);
            }
         } else {
            this.animationHandler.tick();
         }
      }
   }

   public void replayAnimation(Runnable onComplete) {
      this.pendingAnimationCallback = onComplete;
      this.replaying = true;
      this.unloaded = false;
      this.unloading = false;
      this.load();
      UUID playerId = this.context.getPlayerId();
      this.crate.getCurrentSessions().put(playerId, this);
      this.getPlugin().getCratesManager().registerOpeningSession(this);
   }

   public void unload(boolean force) {
      if (!this.unloaded && (!this.unloading || force)) {
         this.unloading = true;

         try {
            if (this.animationHandler != null) {
               this.animationHandler.unload();
               if (force || !this.rerolling) {
                  this.claim();
               }
            }

            if (!force && this.getPlugin().isEnabled() && this.playCloseAnimation()) {
               return;
            }

            this.finalizeUnload();
            this.animationHandler = null;
         } catch (Exception e) {
            Crates.getLogger().error("Error unloading crate opening session", e);
         }
      }
   }

   private boolean playCloseAnimation() {
      if (this.virtualDisplayComponent instanceof ModelEngineDisplayComponent displayComponent) {
         MegActiveModel activeModel = displayComponent.getActiveModel();
         return this.tryPlayCloseAnimation(
            displayComponent.getCloseAnimation(),
            activeModel == null
               ? null
               : (animationId, whenFinish) -> ModelEngineHook.playAnimationOnce(activeModel, this.crate.getLocation(), animationId, whenFinish)
         );
      } else if (this.virtualDisplayComponent instanceof BetterModelDisplayComponent displayComponent) {
         BetterModelDisplay activeModel = displayComponent.getActiveModel();
         return this.tryPlayCloseAnimation(displayComponent.getCloseAnimation(), activeModel == null ? null : activeModel::playAnimationOnce);
      } else {
         return false;
      }
   }

   private boolean tryPlayCloseAnimation(String closeAnimation, BiConsumer<String, Runnable> player) {
      if (player != null && closeAnimation != null && !closeAnimation.equalsIgnoreCase("none")) {
         player.accept(closeAnimation, () -> {
            if (!this.unloaded) {
               this.finalizeUnload();
            }
         });
         return true;
      } else {
         return false;
      }
   }

   private void finalizeUnload() {
      if (!this.crate.getType().getEngineType().equals(EngineType.VANILLA_BLOCK) && !this.crate.getType().getEngineType().equals(EngineType.VANILLA_MODEL)) {
         this.crate.getDisplayComponent().show(Lists.newArrayList(new Player[]{this.getPlayer()}));
      }

      if (!this.getPlugin().isEnabled()) {
         this.virtualDisplayComponent.destroy(false);
      } else {
         Crates.getScheduler().runSync(this.crate.getLocation(), () -> this.virtualDisplayComponent.destroy(false), 2L);
      }

      for (Player player : this.animationHandler.getAudience()) {
         this.crate.showHologram(player);
      }

      this.unloaded = true;
   }

   private void trackWonRewards() {
      for (BaseReward reward : this.rewardsGiven) {
         this.wonRewardIdentifiers.add(reward.getIdentifier());
      }
   }

   public void giveRewards(List<? extends BaseReward> rewards) {
      if (!this.claimed && !this.replaying) {
         this.rewardsGiven.clear();
         this.rewardsGiven.addAll(rewards);
         this.rewardKeyMap.clear();
         if (this.crate.getType().getRerolling().isSkipSameReward()) {
            this.trackWonRewards();
         }

         List<Key> keysUsed = new ArrayList<>();

         for (Entry<? extends Key, Integer> entry : this.context.getKeysAmount().entrySet()) {
            Key key = entry.getKey();
            int amount = entry.getValue();

            for (int i = 0; i < amount * this.crate.getType().getMaxWinRewards() && keysUsed.size() < rewards.size(); i++) {
               keysUsed.add(key);
            }
         }

         for (int i = 0; i < rewards.size() && i < keysUsed.size(); i++) {
            this.rewardKeyMap.put(i, keysUsed.get(i));
         }

         if (!this.crate.getType().getRerolling().isEnabled()) {
            this.claim();
         } else {
            CratePreRerollEvent preRerollEvent = new CratePreRerollEvent(this);
            Bukkit.getPluginManager().callEvent(preRerollEvent);
            if (preRerollEvent.isCancelled()) {
               this.claim();
            } else if (!preRerollEvent.isHandled()) {
               if (this.rewardsGiven.size() <= 1 && this.rerollsLeft > 0) {
                  this.openRerollMenu();
               } else {
                  this.claim();
               }
            }
         }
      }
   }

   public void claim() {
      if (!this.claimed) {
         this.claimed = true;
         this.rerolling = false;
         if (this.rewardsGiven.isEmpty()) {
            if (this.rewardsGenerator instanceof RewardsRandomGenerator && this.crate.getType().getRerolling().isSkipSameReward()) {
               ((RewardsRandomGenerator)this.rewardsGenerator).setExcludedIdentifiers(this.wonRewardIdentifiers);
            }

            List<? extends BaseReward> rewards = this.rewardsGenerator.generateRewards();
            this.rewardsGiven.clear();
            this.rewardsGiven.addAll(rewards);
            this.rewardKeyMap.clear();
            if (this.crate.getType().getRerolling().isSkipSameReward()) {
               this.trackWonRewards();
            }

            List<Key> keysUsed = new ArrayList<>();

            for (Entry<? extends Key, Integer> entry : this.context.getKeysAmount().entrySet()) {
               Key key = entry.getKey();
               int amount = entry.getValue();

               for (int i = 0; i < amount * this.crate.getType().getMaxWinRewards() && keysUsed.size() < rewards.size(); i++) {
                  keysUsed.add(key);
               }
            }

            for (int i = 0; i < rewards.size() && i < keysUsed.size(); i++) {
               this.rewardKeyMap.put(i, keysUsed.get(i));
            }
         }

         this.wonRewardIdentifiers.clear();
         Crates.getScheduler()
            .execute(
               this.getPlayer(),
               () -> {
                  PlayerData record = this.getPlugin().getPlayersManager().getCachedDataNow(this.getPlayer());

                  for (BaseReward item : this.rewardsGiven) {
                     CrateRewardPlayerEvent apiEvent = new CrateRewardPlayerEvent(this.crate, item, this.getPlayer());
                     Bukkit.getPluginManager().callEvent(apiEvent);
                     BaseReward reward = Objects.requireNonNull(apiEvent.getReward(), "Reward cannot be null");
                     int displayAmount = reward.getDisplayItem() == null ? 1 : reward.getDisplayItem().getAmount();
                     if (reward.isAllowWinMessage()) {
                        String[] rewardMessage = Translations.t(
                           "crate.reward-received",
                           "%item%",
                           Operations.safeName(reward.getDisplayItem()),
                           "%amount%",
                           displayAmount,
                           "%crate%",
                           this.crate.getType().getFormattedDisplayName(this.getPlayer())
                        );
                        RewardMessageType rewardMessageType;
                        if (reward instanceof WrappedReward) {
                           rewardMessageType = ((WrappedReward)reward).getRewardMessageType();
                        } else if (reward instanceof CustomReward) {
                           rewardMessageType = ((CustomReward)reward).getRewardMessageType();
                        } else {
                           rewardMessageType = RewardMessageType.CHAT;
                        }

                        if (rewardMessageType == RewardMessageType.ACTION_BAR) {
                           Utilities.getNMSFactory().sendActionBarPacket(Collections.singletonList(this.getPlayer()), rewardMessage[0]);
                        } else {
                           Translations.send(this.getPlayer(), rewardMessage);
                        }
                     }

                     if (reward.isBroadcast()) {
                        for (String broadcastMessage : Crates.getPlugin().getEdition() == Edition.LITE
                           ? Crates.getConfiguration().getBroadcastMessage()
                           : this.crate.getType().getBroadcastMessage()) {
                           Translations.send(
                              Bukkit.getOnlinePlayers(),
                              Translations.r(
                                 broadcastMessage,
                                 "%player%",
                                 this.getPlayer().getName(),
                                 "%reward%",
                                 Operations.safeName(reward.getDisplayItem()),
                                 "%crate%",
                                 this.crate.getType().getFormattedDisplayName(null)
                              )
                           );
                        }
                     }

                     if (reward.isSharePlayersOnline()) {
                        Player[] targets = InformationRegistry.getValue(
                           ScopedValue.of(Addons.ACTION_SHARE_PLAYERS_ONLINE, this.crate.getType(), Bukkit.getOnlinePlayers()), Addons.IP_LIMIT, Player[].class
                        );
                        if (targets == null) {
                           targets = Bukkit.getOnlinePlayers().toArray(new Player[0]);
                        }

                        for (Player other : targets) {
                           RewardFacade.giveReward(this.crate, reward, other);
                        }
                     } else {
                        RewardFacade.giveReward(this.crate, reward, this.getPlayer());
                     }

                     if (!reward.isAlternative()) {
                        record.incrementRewardWinAmount(reward.getIdentifier());
                        CustomReward registeredReward = this.crate.getType().getRewardByIdentifier(reward.getIdentifier());
                        if (registeredReward != null) {
                           if (registeredReward.getWinLimitCooldown() > 0L) {
                              boolean limitHit = registeredReward.getWinLimits() <= 0
                                 || record.getRewardWinAmount(reward.getIdentifier()) >= registeredReward.getWinLimits();
                              if (limitHit) {
                                 record.setRewardCooldown(reward.getIdentifier(), System.currentTimeMillis());
                              }
                           }

                           if (registeredReward.getGlobalWinLimit() >= 0) {
                              GlobalRewardStatsTracker gsm = Crates.getGlobalRewardStatsTracker();
                              String crateId = this.crate.getType().getIdentifier();
                              String rewardId = reward.getIdentifier();
                              gsm.incrementWinAmount(crateId, rewardId);
                              if (registeredReward.getGlobalWinLimitCooldown() > 0L
                                 && gsm.getWinAmount(crateId, rewardId) >= registeredReward.getGlobalWinLimit()) {
                                 gsm.markRewardWon(crateId, rewardId);
                              }
                           } else if (registeredReward.getGlobalWinLimitCooldown() > 0L) {
                              Crates.getGlobalRewardStatsTracker().markRewardWon(this.crate.getType().getIdentifier(), reward.getIdentifier());
                           }
                        }
                     }
                  }
               }
            );
      }
   }

   public Player getPlayer() {
      return this.context.getPlayer();
   }

   public void reroll(int currentRerollIndex, Key key) {
      if (!this.claimed) {
         this.rerolling = true;
         if (this.rerollsLeft <= 0) {
            throw new IllegalStateException("No rerolls left");
         }

         if (this.rerollsLeft <= this.extraRerolls) {
            PlayerData data = this.crate.getPlugin().getPlayersManager().getCachedDataNow(this.getPlayer());
            data.setCrateRerolls(this.crate.getType().getIdentifier(), data.getCrateRerolls(this.crate.getType().getIdentifier()) - 1);
         }

         this.rerollsLeft--;
         RewardsRandomGenerator randomGenerator = (RewardsRandomGenerator)this.rewardsGenerator;
         boolean skipSameReward = this.crate.getType().getRerolling().isSkipSameReward();
         if (skipSameReward) {
            randomGenerator.setExcludedIdentifiers(this.wonRewardIdentifiers);
         }

         if (currentRerollIndex >= 0) {
            Validate.isTrue(currentRerollIndex < this.rewardsGiven.size(), "Current reroll index out of range");
            BaseReward newReward = randomGenerator.generateReward(key);
            this.rewardsGiven.set(currentRerollIndex, newReward);
            this.rewardKeyMap.put(currentRerollIndex, key);
            if (skipSameReward) {
               this.trackWonRewards();
            }

            Bukkit.getPluginManager().callEvent(new CrateRerollEvent(this.crate, newReward, this.getPlayer()));
         } else {
            this.rewardsGiven.clear();
            this.rewardKeyMap.clear();
            List<? extends BaseReward> newRewards = randomGenerator.generateRewards();
            this.rewardsGiven.addAll(newRewards);
            if (skipSameReward) {
               this.trackWonRewards();
            }

            List<Key> keysUsed = new ArrayList<>();

            for (Entry<? extends Key, Integer> entry : this.context.getKeysAmount().entrySet()) {
               Key k = entry.getKey();
               int amount = entry.getValue();

               for (int i = 0; i < amount * this.crate.getType().getMaxWinRewards() && keysUsed.size() < newRewards.size(); i++) {
                  keysUsed.add(k);
               }
            }

            for (int i = 0; i < newRewards.size() && i < keysUsed.size(); i++) {
               this.rewardKeyMap.put(i, keysUsed.get(i));
            }

            for (BaseReward newReward : newRewards) {
               Bukkit.getPluginManager().callEvent(new CrateRerollEvent(this.crate, newReward, this.getPlayer()));
            }

            if (this.rerollsLeft == 0) {
               this.claim();
            }
         }
      }
   }

   public void reroll() {
      this.reroll(-1, null);
   }

   public void openRerollMenu() {
      if (!this.getCrate().getType().getRerolling().isEnabled()) {
         throw new IllegalStateException("Rerolling not enabled");
      }

      if (this.rewardsGiven.isEmpty()) {
         throw new IllegalStateException("Rewards not given");
      }

      ContainerConfiguration configuration = MenuFactory.getInstance().getConfigurationOrNull(this.crate.getType().getRerollsMenuName());
      if (configuration != null && configuration instanceof CrateRerollMenu.CrateRerollMenuConfiguration) {
         this.rerolling = true;
         new CrateRerollMenu(this.getPlugin(), (CrateRerollMenu.CrateRerollMenuConfiguration)configuration, this).open(this.getPlayer());
      } else {
         Crates.getLogger()
            .warn(
               "Reroll menu '"
                  + this.crate.getType().getRerollsMenuName()
                  + "' not found or invalid, skipping reroll for crate '"
                  + this.crate.getType().getIdentifier()
                  + "'"
            );
         this.claim();
      }
   }

   public boolean isMassOpening() {
      return this.context instanceof OpeningContext.RandomOpeningContext && ((OpeningContext.RandomOpeningContext)this.context).getOpeningQuantity() > 1
         || this.context instanceof OpeningContext.MassSelectiveOpeningContext;
   }

   private PhoenixCrates getPlugin() {
      return this.crate.getPlugin();
   }

   @Generated
   public Crate getCrate() {
      return this.crate;
   }

   @Generated
   @Override
   public OpeningContext getContext() {
      return this.context;
   }

   @Generated
   public int getRerollsLeft() {
      return this.rerollsLeft;
   }

   @Generated
   public int getExtraRerolls() {
      return this.extraRerolls;
   }

   @Generated
   @Override
   public RewardsGenerator getRewardsGenerator() {
      return this.rewardsGenerator;
   }

   @Generated
   public OpeningAnimationHandlerImpl getAnimationHandler() {
      return this.animationHandler;
   }

   @Generated
   public EngineDisplayComponent getVirtualDisplayComponent() {
      return this.virtualDisplayComponent;
   }

   @Generated
   public Map<Integer, Key> getRewardKeyMap() {
      return this.rewardKeyMap;
   }

   @Generated
   public Set<String> getWonRewardIdentifiers() {
      return this.wonRewardIdentifiers;
   }

   @Generated
   public Runnable getPendingAnimationCallback() {
      return this.pendingAnimationCallback;
   }

   @Generated
   public boolean isReplaying() {
      return this.replaying;
   }

   @Generated
   public boolean isRerolling() {
      return this.rerolling;
   }

   @Generated
   public boolean isUnloaded() {
      return this.unloaded;
   }

   @Generated
   public boolean isWorldChangeAllowed() {
      return this.worldChangeAllowed;
   }

   @Deprecated
   @Generated
   public List<BaseReward> getRewardsGiven() {
      return this.rewardsGiven;
   }

   @Generated
   public boolean isClaimed() {
      return this.claimed;
   }

   @Generated
   public boolean isUnloading() {
      return this.unloading;
   }

   @Generated
   public void setWorldChangeAllowed(boolean worldChangeAllowed) {
      this.worldChangeAllowed = worldChangeAllowed;
   }
}
