package id.seria.gacha.managers.crates;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.CrateInstance;
import id.seria.gacha.api.crate.events.CrateOpenEvent;
import id.seria.gacha.api.crate.exceptions.CrateOpeningException;
import id.seria.gacha.api.crate.hologram.HologramType;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.api.reward.RewardsMode;
import id.seria.gacha.api.session.OpeningContext;
import id.seria.gacha.facades.KeyFacade;
import id.seria.gacha.facades.RewardFacade;
import id.seria.gacha.internal.FacingDirection;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.objects.ListMap;
import id.seria.gacha.lib.common.utils.objects.Pair;
import id.seria.gacha.lib.common.utils.utility.BukkitUtil;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.managers.CratesManager;
import id.seria.gacha.managers.crates.animations.IdleCrateAnimation;
import id.seria.gacha.managers.crates.animations.idle.IdleEffect;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectAnimation;
import id.seria.gacha.managers.crates.cost.CostEngineType;
import id.seria.gacha.managers.crates.engine.EngineDisplayComponent;
import id.seria.gacha.managers.crates.hologram.CrateHologram;
import id.seria.gacha.managers.crates.session.CrateOpeningSession;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.managers.players.PlayerData;
import id.seria.gacha.managers.reward.CustomAlternativeReward;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.managers.reward.WrappedReward;
import id.seria.gacha.managers.tasks.TaskSubscriber;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Crate implements CrateInstance, TaskSubscriber {
   public static final ListMap<UUID, CrateOpeningSession> OPENING_SESSIONS = new ListMap<>();
   private final CratesManager manager;
   private final CrateType type;
   protected final Location location;
   protected FacingDirection facingDirection;
   protected EngineDisplayComponent displayComponent;
   private CrateHologram hologram;
   private final PreviewItem previewItem;
   private IdleCrateAnimation idleAnimation;
   private final List<Player> nearbyPlayers;
   private final Map<UUID, CrateOpeningSession> currentSessions = new HashMap<>();
   private final Map<String, Object> metadata;
   private boolean loaded;
   protected int delta;

   public Crate(CratesManager manager, CrateType type, Location location, FacingDirection facingDirection) throws Exception {
      this.nearbyPlayers = new ArrayList<>();
      this.metadata = new HashMap<>();
      this.manager = manager;
      this.type = type;
      this.location = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
      this.facingDirection = facingDirection;
      this.displayComponent = type.getEngineType().createDisplayComponent(this);
      this.loadStandByAnimations();
      this.previewItem = new PreviewItem(this);
   }

   private void refreshNearbyPlayers() {
      synchronized (this.nearbyPlayers) {
         this.nearbyPlayers.clear();
         this.nearbyPlayers.addAll(BukkitUtil.getNearbyPlayers(this.location, Crates.getConfiguration().getCratesRenderRadius()));
      }
   }

   public List<Player> getNearbyPlayers() {
      synchronized (this.nearbyPlayers) {
         return this.nearbyPlayers;
      }
   }

   public void load(boolean updateBlock) {
      BukkitUtil.runSync(PhoenixCrates.getLegacyInstance(), this.location, () -> {
         this.loadDisplay(updateBlock, true, true);
         this.manager.subscribeTask(this);
      });
   }

   protected void loadDisplay(boolean updateBlock, boolean displayHologram, boolean displayPreviewItem) {
      if (updateBlock) {
         this.refreshNearbyPlayers();
      }

      if (this.facingDirection == FacingDirection.UNKNOWN) {
         BlockFace bukkitFace = Utilities.getNMSFactory().getBlockFace(this.location.getBlock());
         this.facingDirection = FacingDirection.valueOf(FacingDirection.fromBukkit(bukkitFace).name());
      }

      this.location.setYaw((float)this.facingDirection.getDirection());
      if (displayHologram) {
         if (this.hologram == null) {
            this.hologram = this.type.getHologramProperties().createHologram(this);
         }

         this.hologram.moveTo(this.getHologramLocation());
      }

      this.displayComponent.create(updateBlock);
      if (displayPreviewItem && this.type.isPreviewItemEnabled()) {
         this.previewItem.load();
      }
   }

   @Override
   public boolean isFinished() {
      return false;
   }

   @Override
   public void tick() {
      this.currentSessions.entrySet().removeIf(entry -> entry.getValue().isFinished());
      if (this.hologram != null
         && this.delta++ % Crates.getConfiguration().getHologramRefreshRate() == 0
         && this.type.getHologramType() == HologramType.BUILTIN) {
         this.hologram.refresh();
      }

      List<UUID> toRemove = Lists.newArrayList();
      List<Player> inSession = Lists.newArrayList();

      for (Entry<UUID, CrateOpeningSession> entry : this.currentSessions.entrySet()) {
         CrateOpeningSession session = entry.getValue();
         if (!session.isUnloaded() && session.getPlayer().isOnline()) {
            inSession.add(session.getPlayer());
         } else {
            toRemove.add(entry.getKey());
         }
      }

      for (UUID u : toRemove) {
         this.unloadSession(u, true);
      }

      this.idleAnimation.setIgnoredPlayers(inSession);
      if (this.type.isSimultaneousOpeningsEnabled() || this.currentSessions.isEmpty()) {
         this.idleAnimation.tick();
      }

      if (this.delta % 30 == 0) {
         this.refreshNearbyPlayers();
      }

      this.previewItem.tick();
   }

   public boolean hasActiveSession() {
      return !this.currentSessions.isEmpty();
   }

   public void unloadSession(UUID playerId, boolean force) {
      CrateOpeningSession session = this.currentSessions.remove(playerId);
      if (session != null) {
         this.unloadSession(session, force);
      }
   }

   public void unloadSession(CrateOpeningSession session, boolean force) {
      if (session != null) {
         session.unload(force);
         UUID playerId = session.getContext().getPlayerId();
         OPENING_SESSIONS.remove(playerId, session);
         if (this.currentSessions.get(playerId) == session) {
            this.currentSessions.remove(playerId);
         }
      }
   }

   public void unload(boolean updateBlock) {
      this.manager.unsubscribeTask(this);

      for (CrateOpeningSession session : new ArrayList<>(this.currentSessions.values())) {
         this.unloadSession(session, true);
      }

      this.currentSessions.clear();
      this.displayComponent.destroy(updateBlock);
      if (this.hologram != null) {
         this.hologram.despawn();
      }

      this.previewItem.unload();
   }

   public void reload() throws Exception {
      this.unload(false);
      this.displayComponent = this.type.getEngineType().createDisplayComponent(this);
      this.hologram = this.type.getHologramProperties().createHologram(this);
      this.hologram.moveTo(this.getHologramLocation());
      this.load(false);
      this.loadStandByAnimations();
   }

   @Override
   public void openCrate(OpeningContext data) throws CrateOpeningException {
      Player player = data.getPlayer();
      if (this.type.isSimultaneousOpeningsEnabled()) {
         CrateOpeningSession playerSession = this.currentSessions.get(player.getUniqueId());
         if (playerSession != null) {
            if (playerSession.isClaimed()) {
               this.unloadSession(playerSession, true);
            } else {
               if (!Crates.getConfiguration().isCratesQuickOpen()) {
                  return;
               }

               if (!player.isSneaking()) {
                  throw new CrateOpeningException(Translations.t("crate.quick-open-available")[0], false);
               }

               this.unloadSession(playerSession, true);
            }
         }
      } else if (!this.currentSessions.isEmpty()) {
         if (this.currentSessions.containsKey(player.getUniqueId())) {
            return;
         }

         throw new CrateOpeningException(Translations.t("crate.already-opening")[0]);
      }

      if (this.type.isPermissionRequired() && !player.hasPermission(this.type.getPermission())) {
         throw new CrateOpeningException(Translations.t("crate.no-permission", "%permission%", this.type.getPermission())[0]);
      }

      if (this.getType().getRegisteredRewards().isEmpty()) {
         throw new CrateOpeningException(Translations.t("crate.no-rewards")[0]);
      }

      if (this.getType().getAvailableRewards().isEmpty()) {
         throw new CrateOpeningException(Translations.t("crate.no-available-rewards")[0]);
      }

      PlayerData playerRecord = Crates.getPlayersManager().getPlayerIfCached(player, true);
      if (playerRecord == null) {
         throw new CrateOpeningException(Translations.t("crate.data-not-available")[0]);
      }

      if (this.getAvailableRewards(player).isEmpty()) {
         throw new CrateOpeningException(Translations.t("crate.no-available-rewards")[0]);
      }

      int requiredKeys = data.getRequiredKeys();
      int missingKeys = requiredKeys;
      boolean requireKeyInMainHand = data instanceof OpeningContext.RandomOpeningContext
         && ((OpeningContext.RandomOpeningContext)data).isRequireKeyInMainHand();
      Map<Integer, Pair<Key, Integer>> keySlots = new HashMap<>();
      if (this.type.isKeyRequired()) {
         List<Key> keys = KeyFacade.getCrateKeys(this.type);
         if (keys.isEmpty()) {
            throw new CrateOpeningException(Translations.t("crate.no-linked-key")[0]);
         }

         int x = 0;

         for (Key key : keys) {
            x++;
            if (missingKeys == 0) {
               break;
            }

            if (key.isVirtual()) {
               int virtualKeys = playerRecord.getVirtualKeys(key.getIdentifier());
               int usedKeys = Math.min(virtualKeys, missingKeys);
               missingKeys = Math.max(0, missingKeys - usedKeys);
               keySlots.put(-x, new Pair<>(key, usedKeys));
            } else if (requireKeyInMainHand) {
               ItemStack playerKey = player.getItemInHand();
               int playerKeySlot = player.getInventory().getHeldItemSlot();
               String keyId = KeyFacade.extractKey(playerKey);
               if (keyId != null && keyId.equals(key.getIdentifier())) {
                  missingKeys = Math.max(0, missingKeys);
                  if (missingKeys - playerKey.getAmount() >= 0) {
                     keySlots.put(playerKeySlot, new Pair<>(key, playerKey.getAmount()));
                  } else {
                     keySlots.put(playerKeySlot, new Pair<>(key, missingKeys));
                  }

                  missingKeys = Math.max(0, missingKeys - playerKey.getAmount());
               }
            } else {
               ItemStack[] armor = player.getInventory().getArmorContents();

               for (int a = 0; a < armor.length && missingKeys > 0; a++) {
                  ItemStack piece = armor[a];
                  if (piece != null && piece.getItemMeta() != null) {
                     String crateKey = KeyFacade.extractKey(piece);
                     if (crateKey != null && crateKey.equals(key.getIdentifier())) {
                        int slot = 36 + a;
                        if (missingKeys - piece.getAmount() >= 0) {
                           keySlots.put(slot, new Pair<>(key, piece.getAmount()));
                        } else {
                           keySlots.put(slot, new Pair<>(key, missingKeys));
                        }

                        missingKeys = Math.max(0, missingKeys - piece.getAmount());
                     }
                  }
               }

               if (missingKeys > 0) {
                  ItemStack offhand = Utilities.getNMSFactory().wrapPlayer(player).getItemInOffHand();
                  if (offhand != null && offhand.getItemMeta() != null) {
                     String crateKey = KeyFacade.extractKey(offhand);
                     if (crateKey != null && crateKey.equals(key.getIdentifier())) {
                        if (missingKeys - offhand.getAmount() >= 0) {
                           keySlots.put(40, new Pair<>(key, offhand.getAmount()));
                        } else {
                           keySlots.put(40, new Pair<>(key, missingKeys));
                        }

                        missingKeys = Math.max(0, missingKeys - offhand.getAmount());
                     }
                  }
               }

               int i = -1;

               for (ItemStack content : player.getInventory().getContents()) {
                  i++;
                  if (content != null && content.getItemMeta() != null) {
                     if (missingKeys == 0) {
                        break;
                     }

                     String crateKey = KeyFacade.extractKey(content);
                     if (crateKey != null && crateKey.equals(key.getIdentifier())) {
                        missingKeys = Math.max(0, missingKeys);
                        if (missingKeys - content.getAmount() >= 0) {
                           keySlots.put(i, new Pair<>(key, content.getAmount()));
                        } else {
                           keySlots.put(i, new Pair<>(key, missingKeys));
                        }

                        missingKeys = Math.max(0, missingKeys - content.getAmount());
                     }
                  }
               }
            }
         }

         if (missingKeys == requiredKeys) {
            String keysDisplays = keys.stream().map(keyx -> keyx.getFormattedDisplayName(player)).collect(Collectors.joining(", "));
            throw new CrateOpeningException(Translations.t("crate.no-key", "%key%", keysDisplays)[0]);
         }

         if (missingKeys > 0) {
            throw new CrateOpeningException(Translations.t("crate.no-keys")[0]);
         }
      }

      long currentCooldown = playerRecord.getCrateCooldown(this.type.getIdentifier()) + this.type.getOpenCooldownSeconds() * 1000L;
      if (System.currentTimeMillis() < currentCooldown) {
         double leftMillis = currentCooldown - System.currentTimeMillis();
         throw new CrateOpeningException(Translations.t("crate.cooldown-wait", "%cooldown%", Translations.formatTime((int)(leftMillis / 1000.0) + 1))[0]);
      }

      CostEngineType costEngine = this.type.getOpenCostEngine();
      String costCurrency = this.type.getOpenCostCurrency();
      if (this.type.getOpenMoneyCost() > 0.0) {
         if (costEngine.isPremium() && Crates.getPlugin().getEdition() == Edition.LITE) {
            throw new CrateOpeningException(Translations.t("plans.unlock-premium")[0], false);
         }

         if (!costEngine.isInstalled()) {
            throw new CrateOpeningException(Translations.t("crate.cost-engine-not-installed")[0], false);
         }

         if (!costEngine.hasEnough(player, this.type.getOpenMoneyCost(), costCurrency)) {
            throw new CrateOpeningException(
               Translations.t("crate.no-money-" + costEngine.name().toLowerCase(), "%money%", costEngine.format(this.type.getOpenMoneyCost(), costCurrency))[0],
               false
            );
         }

         costEngine.withdraw(player, this.type.getOpenMoneyCost(), costCurrency);
         Translations.send(
            player,
            Translations.t(
               "crate.money-used-" + costEngine.name().toLowerCase(), "%money_consumed%", costEngine.format(this.type.getOpenMoneyCost(), costCurrency)
            )[0]
         );
      }

      if (this.type.getOpenCooldownSeconds() > 0) {
         playerRecord.addCrateCooldown(this.type.getIdentifier(), System.currentTimeMillis());
      }

      if (this.type.isKeyRequired()) {
         Map<Key, Integer> keysAmount = new HashMap<>();
         if (!keySlots.isEmpty()) {
            for (Entry<Integer, Pair<Key, Integer>> entry : keySlots.entrySet()) {
               Key key = (Key)entry.getValue().first;
               int amount = (Integer)entry.getValue().second;
               keysAmount.put(key, keysAmount.getOrDefault(key, 0) + amount);
               if (key.isVirtual()) {
                  playerRecord.setVirtualKeys(key.getIdentifier(), Math.max(0, playerRecord.getVirtualKeys(key.getIdentifier()) - amount));
               } else {
                  ItemStack item = player.getInventory().getItem(entry.getKey());
                  if (item.getAmount() - amount <= 0) {
                     player.getInventory().setItem(entry.getKey(), new ItemStack(Material.AIR));
                  } else {
                     item.setAmount(item.getAmount() - amount);
                  }
               }
            }
         }

         data.setKeysAmount(keysAmount);
      } else {
         data.setKeysAmount(new HashMap<>());
      }

      Bukkit.getPluginManager().callEvent(new CrateOpenEvent(this, player));
      CrateOpeningSession session = new CrateOpeningSession(this, data);
      session.load();
      this.currentSessions.put(player.getUniqueId(), session);
      this.manager.registerOpeningSession(session);
      playerRecord.incrementOpenedCratesAmount(this.type.getIdentifier(), data.getRequiredKeys());
   }

   public List<? extends BaseReward> getAvailableRewards(Player player) {
      PlayerData playerData = this.getPlugin().getPlayersManager().getCachedDataNow(player);
      List<BaseReward> availableRewards = new ArrayList<>();

      for (CustomReward reward : this.type.getAvailableRewards()) {
         if (reward.isValid()
            && (this.type.getRewardsMode() != RewardsMode.RANDOM || reward.getWeight() != 0.0)
            && reward.getWinLimits() != 0
            && reward.getGlobalWinLimit() != 0) {
            long now = System.currentTimeMillis();
            boolean winLimitCooldownPassed = reward.getWinLimitCooldown() <= 0L
               || now - playerData.getRewardCooldown(reward.getIdentifier()) >= reward.getWinLimitCooldown() * 1000L;
            boolean globalWinLimitCooldownPassed = reward.getGlobalWinLimitCooldown() <= 0L
               || now - Crates.getGlobalRewardStatsTracker().getLastWinTimestamp(this.type.getIdentifier(), reward.getIdentifier())
                  >= reward.getGlobalWinLimitCooldown() * 1000L;
            int winAmount = playerData.getRewardWinAmount(reward.getIdentifier());
            int globalWinCount = Crates.getGlobalRewardStatsTracker().getWinAmount(this.type.getIdentifier(), reward.getIdentifier());
            if (reward.getWinLimitCooldown() > 0L && winLimitCooldownPassed && reward.getWinLimits() > 0 && winAmount >= reward.getWinLimits()) {
               playerData.resetRewardWinAmount(reward.getIdentifier());
               winAmount = 0;
            }

            if (reward.getGlobalWinLimitCooldown() > 0L
               && globalWinLimitCooldownPassed
               && reward.getGlobalWinLimit() > 0
               && globalWinCount >= reward.getGlobalWinLimit()) {
               Crates.getGlobalRewardStatsTracker().resetWinAmount(this.type.getIdentifier(), reward.getIdentifier());
               globalWinCount = 0;
            }

            boolean canWin = (reward.getWinLimits() < 0 || winAmount < reward.getWinLimits())
               && (reward.getGlobalWinLimit() < 0 || globalWinCount < reward.getGlobalWinLimit())
               && winLimitCooldownPassed
               && globalWinLimitCooldownPassed;
            boolean isRestricted = reward.isRestricted(player);
            if (canWin && !isRestricted) {
               availableRewards.add(RewardFacade.wrap(reward));
            } else if (reward.getAlternative().isEnabled()) {
               CustomAlternativeReward alternative = reward.getAlternative();
               availableRewards.add(
                  new WrappedReward(
                     reward.getIdentifier() + "_alternative",
                     alternative.getItem(),
                     alternative.isVirtual() ? new ArrayList<>() : Lists.newArrayList(new BaseItem[]{alternative.getItem()}),
                     alternative.getWinCommands(),
                     reward.getWeight(),
                     alternative.isBroadcast(),
                     alternative.isSharePlayersOnline(),
                     alternative.isAllowWinMessage(),
                     reward.getRewardMessageType(),
                     true
                  )
               );
            }
         }
      }

      return availableRewards;
   }

   public Block getBlock() {
      return this.location.getBlock();
   }

   @Deprecated
   public PhoenixCrates getPlugin() {
      return this.manager.getPlugin();
   }

   public World getWorld() {
      return this.location.getWorld();
   }

   public void setMetadata(String key, Object value) {
      String checkedKey = Objects.requireNonNull(key, "key");
      if (value == null) {
         this.metadata.remove(checkedKey);
      } else {
         this.metadata.put(checkedKey, value);
      }
   }

   public boolean hasMetadata(String key) {
      return this.metadata.containsKey(Objects.requireNonNull(key, "key"));
   }

   public <T> T getMetadataOrNull(String key, Class<T> type) {
      Object value = this.metadata.get(Objects.requireNonNull(key, "key"));
      return value != null && type.isInstance(value) ? type.cast(value) : null;
   }

   public void setFacingDirection(FacingDirection face) {
      this.facingDirection = face;
      this.location.setYaw((float)face.getDirection());
      this.displayComponent.update();
   }

   public int getBlockX() {
      return this.location.getBlockX();
   }

   public int getBlockY() {
      return this.location.getBlockY();
   }

   public int getBlockZ() {
      return this.location.getBlockZ();
   }

   public void showHologram(Player player) {
      if (this.hologram != null) {
         this.hologram.show(player);
      }
   }

   public void hideHologram(Player player) {
      if (this.hologram != null) {
         this.hologram.hide(player);
      }
   }

   public Location getHologramLocation() {
      return this.location
         .clone()
         .add(
            0.5,
            this.displayComponent.getDisplayHeight() + 0.3 + this.type.getHologramProperties().getOffset() + (this.type.isPreviewItemEnabled() ? 0.75 : 0.0),
            0.5
         );
   }

   private void loadStandByAnimations() throws Exception {
      this.idleAnimation = new IdleCrateAnimation(this);

      for (IdleEffect effect : this.type.getIdleEffects()) {
         this.idleAnimation.addEffect(IdleEffectAnimation.createEffect(this.idleAnimation, effect));
      }
   }

   @Generated
   public CratesManager getManager() {
      return this.manager;
   }

   @Generated
   public CrateType getType() {
      return this.type;
   }

   @Generated
   @Override
   public Location getLocation() {
      return this.location;
   }

   @Generated
   public FacingDirection getFacingDirection() {
      return this.facingDirection;
   }

   @Generated
   public EngineDisplayComponent getDisplayComponent() {
      return this.displayComponent;
   }

   @Generated
   public CrateHologram getHologram() {
      return this.hologram;
   }

   @Generated
   public PreviewItem getPreviewItem() {
      return this.previewItem;
   }

   @Generated
   public IdleCrateAnimation getIdleAnimation() {
      return this.idleAnimation;
   }

   @Generated
   public Map<UUID, CrateOpeningSession> getCurrentSessions() {
      return this.currentSessions;
   }

   @Generated
   public Map<String, Object> getMetadata() {
      return this.metadata;
   }

   @Generated
   public boolean isLoaded() {
      return this.loaded;
   }

   @Generated
   public int getDelta() {
      return this.delta;
   }
}
