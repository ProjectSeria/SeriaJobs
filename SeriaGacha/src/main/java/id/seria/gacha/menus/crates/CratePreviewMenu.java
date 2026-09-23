package id.seria.gacha.menus.crates;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.api.internal.Item;
import id.seria.gacha.api.reward.RewardsMode;
import id.seria.gacha.facades.KeyFacade;
import id.seria.gacha.internal.Addons;
import id.seria.gacha.internal.Placeholders;
import id.seria.gacha.internal.menus.GenericConfigurableLayout;
import id.seria.gacha.lib.common.uicomponents.newest.Ui;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.ContainerConfiguration;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.ContainerItemsConfiguration;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.PaginatedContainerConfiguration;
import id.seria.gacha.lib.common.uicomponents.newest.container.ContainerType;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.CloseViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.OpenViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.SimpleComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.SimplePaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.objects.ComplexMaterial;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.utility.PlaceholderUtil;
import id.seria.gacha.lib.common.utils.utility.TimeUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Operations;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.rewards.GuaranteedRewardCalculator;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.managers.players.PlayerData;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.menus.MenuFactory;
import id.seria.gacha.placeholders.Placeholder;
import id.seria.gacha.registries.InformationRegistry;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Generated;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CratePreviewMenu extends GenericConfigurableLayout<CratePreviewMenu.CratePreviewMenuConfiguration> {
   public static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
   private final Optional<Crate> crate;
   private final CrateType crateType;
   private final String filterRarityId;
   private Runnable onCloseAction;
   private boolean ignoreNextClose;
   private double cachedMaxPercentage;

   public CratePreviewMenu(CratePreviewMenu.CratePreviewMenuConfiguration configuration, @NonNull Crate crate) {
      this(configuration, crate, null);
      if (crate == null) {
         throw new NullPointerException("crate is marked non-null but is null");
      }
   }

   public CratePreviewMenu(CratePreviewMenu.CratePreviewMenuConfiguration configuration, @NonNull Crate crate, String filterRarityId) {
      super(configuration);
      if (crate == null) {
         throw new NullPointerException("crate is marked non-null but is null");
      }

      this.crate = Optional.of(crate);
      this.crateType = crate.getType();
      this.filterRarityId = filterRarityId;
      this.cachedMaxPercentage = new ArrayList<>(crate.getType().getAvailableRewards()).stream().mapToDouble(CustomReward::getWeight).sum();
   }

   public CratePreviewMenu(CratePreviewMenu.CratePreviewMenuConfiguration configuration, CrateType crateType) {
      this(configuration, crateType, null);
   }

   public CratePreviewMenu(CratePreviewMenu.CratePreviewMenuConfiguration configuration, CrateType crateType, String filterRarityId) {
      super(configuration);
      this.crate = Optional.empty();
      this.crateType = crateType;
      this.filterRarityId = filterRarityId;
      this.cachedMaxPercentage = new ArrayList<>(crateType.getAvailableRewards()).stream().mapToDouble(CustomReward::getWeight).sum();
   }

   @Override
   public void onOpen(OpenViewContext context) {
      super.onOpen(context);
      context.setContainerTitle(
         Placeholders.setPlaceholders(
            PlaceholderUtil.replacePlaceholdersAndColors(
               this.getPlugin(), this.getConfiguration().getSettings().getTitle(), "%crate%", this.crateType.getFormattedDisplayName(context.getViewer())
            ),
            context.getViewer()
         )
      );
      context.setContainerType(ContainerType.fromRows(this.getConfiguration().getSettings().getRows()));
   }

   @Override
   public void onClose(CloseViewContext context) {
      if (this.ignoreNextClose) {
         this.ignoreNextClose = false;
      } else {
         if (this.onCloseAction != null && Utilities.isPlayerOnline(context.getViewer())) {
            Runnable action = this.onCloseAction;
            Crates.getScheduler().runSync(context.getViewer(), () -> {
               if (Utilities.isPlayerOnline(context.getViewer())) {
                  action.run();
               }
            });
         }
      }
   }

   @Override
   public void onPrepare(ContainerView view) {
      SimplePaginatedList<CustomReward> rewards = view.computeMetadataIfAbsent("preview-rewards.list", this::createPaginationComponent);
      this.addComponent(view, rewards);
      this.addComponent(view, this.createAvailableKeysComponent());
   }

   private SimpleComponent createAvailableKeysComponent() {
      return new SimpleComponent(this.getConfiguration().getAvailableKeysItem().getSlot(), null) {
         @Override
         public void onRender(RenderViewContext context) {
            Player player = context.getViewer();
            ContainerItemsConfiguration.NonActionsItemConfiguration availableKeysConfig = CratePreviewMenu.this.getConfiguration()
               .getAvailableKeysItem()
               .clone();
            List<String> keys = new ArrayList<>();
            int position = 1;

            for (Key key : KeyFacade.getCrateKeys(CratePreviewMenu.this.crateType)) {
               keys.add(
                  CratePreviewMenu.this.r(
                     CratePreviewMenu.this.getConfiguration().getKeyDisplay(),
                     "%position%",
                     position,
                     "%name%",
                     key.getFormattedDisplayName(player),
                     "%amount%",
                     KeyFacade.countTotalAvailableKeys(key, player)
                  )
               );
               position++;
            }

            int totalAvailableKeys = KeyFacade.countTotalAvailableKeys(CratePreviewMenu.this.crateType, player);
            availableKeysConfig.setDisplayName(
               CratePreviewMenu.this.r(
                  availableKeysConfig.getDisplayName(),
                  "%available_keys%",
                  totalAvailableKeys,
                  "%crate%",
                  CratePreviewMenu.this.crateType.getFormattedDisplayName(player)
               )
            );
            availableKeysConfig.setLore(
               CratePreviewMenu.this.r(
                  availableKeysConfig.getLore(),
                  "%keys%",
                  keys,
                  "%available_keys%",
                  totalAvailableKeys,
                  "%crate%",
                  CratePreviewMenu.this.crateType.getFormattedDisplayName(player)
               )
            );
            CratePreviewMenu.this.setItem(context, availableKeysConfig, click -> {});
         }
      };
   }

   private List<CustomReward> getAvailableRewards() {
      List<CustomReward> rewards;
      if (this.crate.isPresent()) {
         rewards = this.crate.get().getType().getRegisteredRewards();
      } else {
         rewards = Objects.requireNonNull(this.crateType).getAvailableRewards();
      }

      return this.filterRarityId != null
         ? rewards.stream()
            .filter(r -> this.filterRarityId.equals(InformationRegistry.getValue(r, Addons.REWARD_RARITY_IDENTIFIER, String.class)))
            .collect(Collectors.toList())
         : rewards;
   }

   private Map<String, String> computeGroupRarities() {
      Map<String, String> groupRarities = new HashMap<>();
      if (this.filterRarityId != null) {
         return groupRarities;
      }

      for (CustomReward reward : this.getAvailableRewards()) {
         String groupId = InformationRegistry.getValue(reward, Addons.RARITY_GROUP_REWARDS, String.class);
         if (groupId != null) {
            groupRarities.putIfAbsent(groupId, groupId);
         }
      }

      return groupRarities;
   }

   private Set<String> computeGroupRepresentatives(Map<String, String> groupRarities) {
      Set<String> representatives = new HashSet<>();
      if (groupRarities.isEmpty()) {
         return representatives;
      }

      Set<String> seenRarityIds = new HashSet<>();

      for (CustomReward reward : this.getAvailableRewards()) {
         String rarityId = InformationRegistry.getValue(reward, Addons.REWARD_RARITY_IDENTIFIER, String.class);
         if (rarityId != null && groupRarities.containsKey(rarityId) && seenRarityIds.add(rarityId)) {
            representatives.add(reward.getIdentifier());
         }
      }

      return representatives;
   }

   private long countRewardsInGroup(String rarityId, Player viewer) {
      return this.getAvailableRewards()
         .stream()
         .filter(r -> rarityId.equals(InformationRegistry.getValue(r, Addons.REWARD_RARITY_IDENTIFIER, String.class)))
         .filter(r -> !this.isObtained(r, viewer))
         .count();
   }

   private boolean isObtained(CustomReward reward, Player viewer) {
      return this.getConfiguration().isSkipObtainedRewards() && viewer != null && reward.isObtainedBy(viewer, this.crateType.getIdentifier());
   }

   private SimplePaginatedList<CustomReward> createPaginationComponent() {
      Map<String, String> groupRarities = this.computeGroupRarities();
      Set<String> groupRepresentatives = this.computeGroupRepresentatives(groupRarities);
      return (SimplePaginatedList<CustomReward>)this.<CustomReward>createSimplePagination(
            this.getConfiguration(),
            CustomReward.class,
            view -> {
               List<CustomReward> rewardsList = this.getAvailableRewards();
               if (this.getConfiguration().isSkipObtainedRewards()) {
                  Player viewer = view.getViewer();
                  rewardsList = rewardsList.stream()
                     .filter(rewardx -> groupRepresentatives.contains(rewardx.getIdentifier()) || !this.isObtained(rewardx, viewer))
                     .collect(Collectors.toList());
               }

               this.cachedMaxPercentage = new ArrayList<>(rewardsList).stream().mapToDouble(CustomReward::getWeight).sum();
               if (!groupRepresentatives.isEmpty()) {
                  List<CustomReward> groups = new ArrayList<>();
                  List<CustomReward> ungrouped = new ArrayList<>();

                  for (CustomReward reward : rewardsList) {
                     if (groupRepresentatives.contains(reward.getIdentifier())) {
                        groups.add(reward);
                     } else {
                        String rarityId = InformationRegistry.getValue(reward, Addons.REWARD_RARITY_IDENTIFIER, String.class);
                        if (rarityId == null || !groupRarities.containsKey(rarityId)) {
                           ungrouped.add(reward);
                        }
                     }
                  }

                  List<CustomReward> ordered = new ArrayList<>(groups);
                  ordered.addAll(ungrouped);
                  rewardsList = ordered;
               }

               return rewardsList;
            }
         )
         .setOnItemRender(
            (context, index, item, reward) -> {
               if (groupRepresentatives.contains(reward.getIdentifier())) {
                  String rarityId = InformationRegistry.getValue(reward, Addons.REWARD_RARITY_IDENTIFIER, String.class);
                  if (rarityId != null && groupRarities.containsKey(rarityId)) {
                     this.renderGroupItem(context, item, reward, rarityId);
                     return;
                  }
               }

               CratePreviewMenu.CratePreviewMenuConfiguration containerConfiguration = this.getConfiguration();
               long now = System.currentTimeMillis();
               boolean isTimeLimited = reward.getTimeLimitedExpiry() > 0L && reward.getTimeLimitedExpiry() > now;
               boolean timeLimitedExpired = reward.getTimeLimitedExpiry() > 0L && !isTimeLimited;
               CratePreviewMenu.RewardItemConfiguration reviewConfiguration = isTimeLimited
                  ? containerConfiguration.getRewardTimeLimitedItem()
                  : (
                     this.crateType.getRewardsMode() == RewardsMode.RANDOM
                        ? containerConfiguration.getRewardRandomModeItem()
                        : containerConfiguration.getRewardSelectiveModeItem()
                  );
               RItemStack rItem = RItemStack.of(Operations.o(reward.getDisplayItem()).map(Item::build).orElse(CustomReward.INVALID_REWARD));
               List<CustomReward> allRewards = this.getAvailableRewards();
               Placeholder.Context<CustomReward> placeholderContext = Placeholder.context(reward)
                  .with("viewer", context.getViewer())
                  .with("rewards", allRewards)
                  .with("crate", this.crateType.getIdentifier());
               if (isTimeLimited) {
                  placeholderContext.with(
                     "time",
                     TimeUtil.formatLargestUnit(
                        reward.getTimeLimitedExpiry() - now,
                        containerConfiguration.getDaysFormat(),
                        containerConfiguration.getHoursFormat(),
                        containerConfiguration.getMinutesFormat(),
                        containerConfiguration.getSecondsFormat()
                     )
                  );
               }

               List<String> lore = placeholderContext.parse(reviewConfiguration.getLore());
               PlayerData playerData = Crates.getPlayersManager().getCachedDataNow(context.getViewer());
               int totalOpenings = playerData.getOpenedCratesAmount(this.crateType.getIdentifier());
               int amountsLeft = 0;
               int remainingOpenings = GuaranteedRewardCalculator.getRemainingOpenings(this.crateType, reward, totalOpenings);
               if (remainingOpenings > 0 && remainingOpenings != Integer.MAX_VALUE) {
                  amountsLeft = remainingOpenings;
               }

               if (amountsLeft > 0) {
                  lore.add(this.t("crate.guaranteed-win-openings", "%openings%", amountsLeft)[0]);
               }

               int winAmount = playerData.getRewardWinAmount(reward.getIdentifier());
               int globalWinAmount = Crates.getGlobalRewardStatsTracker().getWinAmount(this.crateType.getIdentifier(), reward.getIdentifier());
               long winCooldownRemaining = reward.getWinLimitCooldown() > 0L
                  ? Math.max(0L, reward.getWinLimitCooldown() * 1000L - (now - playerData.getRewardCooldown(reward.getIdentifier())))
                  : 0L;
               boolean winCooldownActive = winCooldownRemaining > 0L;
               long lastGlobalWin = Crates.getGlobalRewardStatsTracker().getLastWinTimestamp(this.crateType.getIdentifier(), reward.getIdentifier());
               long globalCooldownRemaining = reward.getGlobalWinLimitCooldown() > 0L
                  ? Math.max(0L, reward.getGlobalWinLimitCooldown() * 1000L - (now - lastGlobalWin))
                  : 0L;
               boolean globalWinCooldownActive = globalCooldownRemaining > 0L;
               int effectiveWinAmount = reward.getWinLimitCooldown() > 0L
                     && !winCooldownActive
                     && reward.getWinLimits() > 0
                     && winAmount >= reward.getWinLimits()
                  ? 0
                  : winAmount;
               int effectiveGlobalWinAmount = reward.getGlobalWinLimitCooldown() > 0L
                     && !globalWinCooldownActive
                     && reward.getGlobalWinLimit() > 0
                     && globalWinAmount >= reward.getGlobalWinLimit()
                  ? 0
                  : globalWinAmount;
               placeholderContext.with("win_amount", effectiveWinAmount)
                  .with("win_limit", reward.getWinLimits() < 0 ? "\u221e" : String.valueOf(reward.getWinLimits()))
                  .with("global_win_amount", effectiveGlobalWinAmount)
                  .with("global_limit", reward.getGlobalWinLimit() < 0 ? "\u221e" : String.valueOf(reward.getGlobalWinLimit()))
                  .with(
                     "cooldown",
                     TimeUtil.formatLargestUnit(
                        winCooldownRemaining,
                        containerConfiguration.getDaysFormat(),
                        containerConfiguration.getHoursFormat(),
                        containerConfiguration.getMinutesFormat(),
                        containerConfiguration.getSecondsFormat()
                     )
                  )
                  .with(
                     "global_cooldown",
                     TimeUtil.formatLargestUnit(
                        globalCooldownRemaining,
                        containerConfiguration.getDaysFormat(),
                        containerConfiguration.getHoursFormat(),
                        containerConfiguration.getMinutesFormat(),
                        containerConfiguration.getSecondsFormat()
                     )
                  );
               boolean isBlacklistRestricted = reward.isBlacklistRestricted(context.getViewer());
               boolean isRequiredPermMissing = reward.isRequiredPermissionMissing(context.getViewer());
               boolean isDisabled = !reward.isEnabled();
               List<String> lockReasons = new ArrayList<>();
               if (isDisabled) {
                  lockReasons.addAll(placeholderContext.parse(containerConfiguration.getDisabledLore()));
               }

               if (reward.getWinLimits() > 0 && !isDisabled && !timeLimitedExpired) {
                  lockReasons.addAll(placeholderContext.parse(containerConfiguration.getWinLimitLore()));
               }

               if (winCooldownActive) {
                  lockReasons.addAll(placeholderContext.parse(containerConfiguration.getWinLimitCooldownLore()));
               }

               if (reward.getGlobalWinLimit() > 0 && !isDisabled && !timeLimitedExpired) {
                  lockReasons.addAll(placeholderContext.parse(containerConfiguration.getGlobalWinLimitLore()));
               }

               if (globalWinCooldownActive) {
                  lockReasons.addAll(placeholderContext.parse(containerConfiguration.getGlobalWinLimitCooldownLore()));
               }

               if (timeLimitedExpired) {
                  lockReasons.addAll(placeholderContext.parse(containerConfiguration.getTimeLimitedExpiredLore()));
               }

               if (isBlacklistRestricted) {
                  lockReasons.addAll(placeholderContext.parse(containerConfiguration.getRestrictedPermissionLore()));
               }

               if (isRequiredPermMissing) {
                  lockReasons.addAll(placeholderContext.parse(containerConfiguration.getRequiredPermissionLore()));
               }

               if (isDisabled) {
                  ContainerItemsConfiguration.NonSlotItemConfiguration lockedConfig = containerConfiguration.getLockedRewardItem();
                  List<String> lockedLore = new ArrayList<>(placeholderContext.parse(lockedConfig.getLore()));
                  if (!lockReasons.isEmpty() && !lockedLore.isEmpty()) {
                     lockedLore.add("");
                  }

                  lockedLore.addAll(lockReasons);
                  item.withItem(lockedConfig.getMaterial()).withDisplayName(placeholderContext.parse(lockedConfig.getDisplayName())).withLore(lockedLore);
               } else {
                  if (!lockReasons.isEmpty() && !lore.isEmpty()) {
                     lore.add("");
                  }

                  lore.addAll(lockReasons);
                  item.withItem(rItem).withDisplayName(placeholderContext.parse(reviewConfiguration.getName())).withLore(lore);
               }
            }
         );
   }

   private void renderGroupItem(RenderViewContext context, VirtualItem item, CustomReward groupRepresentative, String rarityId) {
      Item groupDisplayItem = InformationRegistry.getValue(groupRepresentative, Addons.RARITY_DISPLAY_ITEM, Item.class);
      String rarityDisplayName = InformationRegistry.getValue(groupRepresentative, Addons.RARITY_DISPLAY_NAME, String.class);
      long groupSize = this.countRewardsInGroup(rarityId, context.getViewer());
      ItemStack displayStack = groupDisplayItem != null ? groupDisplayItem.build() : XMaterial.CHEST.parseItem();
      boolean hasCustomName = groupDisplayItem instanceof BaseItem && ((BaseItem)groupDisplayItem).getCustomName() != null;
      boolean hasCustomLore = groupDisplayItem instanceof BaseItem
         && ((BaseItem)groupDisplayItem).getCustomLore() != null
         && !((BaseItem)groupDisplayItem).getCustomLore().isEmpty();
      item.withItem(displayStack)
         .withDisplayName(
            hasCustomName ? ((BaseItem)groupDisplayItem).getCustomName() : "&a" + (rarityDisplayName != null ? rarityDisplayName : "Rewards Group")
         )
         .withLore(
            hasCustomLore
               ? ((BaseItem)groupDisplayItem).getCustomLore()
               : Lists.newArrayList(new String[]{"&7Click to view rewards in this category.", "", "&7Rewards: &e" + groupSize})
         )
         .whenClick(click -> this.openGroupPreview(click.getViewer(), groupRepresentative, rarityId));
   }

   private void openGroupPreview(Player player, CustomReward representative, String rarityId) {
      CratePreviewMenu.CratePreviewMenuConfiguration config = this.getConfiguration();
      String customMenuName = InformationRegistry.getValue(representative, Addons.RARITY_PREVIEW_MENU, String.class);
      if (customMenuName != null && !customMenuName.isEmpty()) {
         try {
            ContainerConfiguration resolved = MenuFactory.getInstance().getConfigurationOrNull(customMenuName);
            if (resolved instanceof CratePreviewMenu.CratePreviewMenuConfiguration) {
               config = (CratePreviewMenu.CratePreviewMenuConfiguration)resolved;
            }
         } catch (Exception var7) {
         }
      }

      CratePreviewMenu groupMenu = this.crate.isPresent()
         ? new CratePreviewMenu(config, this.crate.get(), rarityId)
         : new CratePreviewMenu(config, this.crateType, rarityId);
      groupMenu.onCloseAction = () -> this.open(player);
      this.ignoreNextClose = true;
      groupMenu.open(player);
   }

   @Generated
   public Optional<Crate> getCrate() {
      return this.crate;
   }

   @Generated
   public CrateType getCrateType() {
      return this.crateType;
   }

   @Generated
   public String getFilterRarityId() {
      return this.filterRarityId;
   }

   @Generated
   public Runnable getOnCloseAction() {
      return this.onCloseAction;
   }

   @Generated
   public boolean isIgnoreNextClose() {
      return this.ignoreNextClose;
   }

   @Generated
   public double getCachedMaxPercentage() {
      return this.cachedMaxPercentage;
   }

   public static class CratePreviewMenuConfiguration extends PaginatedContainerConfiguration {
      @ConfigField(legacyPaths = "review", path = "reward.random-mode")
      protected final CratePreviewMenu.RewardItemConfiguration rewardRandomModeItem;
      @ConfigField(legacyPaths = "review", path = "reward.selective-mode")
      protected final CratePreviewMenu.RewardItemConfiguration rewardSelectiveModeItem;
      @ConfigField(path = "reward.extra.time-limited-reward")
      protected final CratePreviewMenu.RewardItemConfiguration rewardTimeLimitedItem;
      @ConfigField(path = "available-keys")
      protected final ContainerItemsConfiguration.NonActionsItemConfiguration availableKeysItem;
      @ConfigField(path = "available-keys.key-display")
      protected final String keyDisplay;
      @ConfigField(path = "reward.locked.item")
      protected final ContainerItemsConfiguration.NonSlotItemConfiguration lockedRewardItem;
      @ConfigField(path = "reward.locked.win-limit")
      protected final List<String> winLimitLore;
      @ConfigField(path = "reward.locked.global-win-limit")
      protected final List<String> globalWinLimitLore;
      @ConfigField(path = "reward.locked.restricted-permission")
      protected final List<String> restrictedPermissionLore;
      @ConfigField(path = "reward.locked.required-permission")
      protected final List<String> requiredPermissionLore;
      @ConfigField(path = "time-format.days")
      protected final String daysFormat;
      @ConfigField(path = "time-format.hours")
      protected final String hoursFormat;
      @ConfigField(path = "time-format.minutes")
      protected final String minutesFormat;
      @ConfigField(path = "time-format.seconds")
      protected final String secondsFormat;
      @ConfigField(path = "reward.locked.disabled")
      protected final List<String> disabledLore;
      @ConfigField(path = "reward.locked.win-limit-cooldown")
      protected final List<String> winLimitCooldownLore;
      @ConfigField(path = "reward.locked.global-win-limit-cooldown")
      protected final List<String> globalWinLimitCooldownLore;
      @ConfigField(path = "reward.locked.time-limited-expired")
      protected final List<String> timeLimitedExpiredLore;
      @ConfigField(path = "skip-obtained-rewards")
      protected boolean skipObtainedRewards;

      public CratePreviewMenuConfiguration() {
         this.settings.setType("REWARDS_PREVIEW");
         this.settings.setTitle("%crate% Rewards");
         this.settings.setRows(6);
         this.settings.setRefreshEnabled(true);
         this.settings.setRefreshRate(20);
         this.items.clear();
         ContainerItemsConfiguration.ItemConfiguration downFiller = new ContainerItemsConfiguration.ItemConfiguration(
            new SlotCompound(45, 53),
            new ComplexMaterial(XMaterial.BLACK_STAINED_GLASS_PANE),
            1,
            0,
            "",
            false,
            "&e",
            Lists.newArrayList(),
            Lists.newArrayList()
         );
         downFiller.setHideTooltip(true);
         this.items.put("down-filler", downFiller);
         this.items
            .put(
               "close-menu",
               new ContainerItemsConfiguration.ItemConfiguration(
                  new SlotCompound(49, 49),
                  new ComplexMaterial(XMaterial.BARRIER),
                  1,
                  0,
                  "",
                  false,
                  "&aClose Menu",
                  Lists.newArrayList(new String[]{"&7Click here to close this menu.", "", Ui.smallCaps("&6Left-Click: &eClose menu.")}),
                  Lists.newArrayList(new String[]{"[CLOSE_INVENTORY]"})
               )
            );
         this.pagination.setAlignItems(PaginationSettings.ItemsAlign.LEFT);
         this.pagination.setSlots(SlotCompound.fromGrid(10, 3, 7));
         this.pagination
            .setFillItem(
               new ContainerItemsConfiguration.NonSlotItemConfiguration(
                  new ComplexMaterial(XMaterial.AIR), 0, "", false, "", Lists.newArrayList(), Lists.newArrayList()
               )
            );
         this.pagination
            .setPreviousItem(
               new ContainerItemsConfiguration.ItemConfiguration(
                  new SlotCompound(18),
                  new ComplexMaterial(XMaterial.ARROW),
                  1,
                  0,
                  "",
                  false,
                  "&6Previous page - Page %page%",
                  Lists.newArrayList(new String[]{"&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<"}),
                  Lists.newArrayList()
               )
            );
         this.pagination
            .setNextItem(
               new ContainerItemsConfiguration.ItemConfiguration(
                  new SlotCompound(26),
                  new ComplexMaterial(XMaterial.ARROW),
                  1,
                  0,
                  "",
                  false,
                  "&6Next page - Page %page%",
                  Lists.newArrayList(new String[]{"&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>"}),
                  Lists.newArrayList()
               )
            );
         this.rewardRandomModeItem = new CratePreviewMenu.RewardItemConfiguration(
            "&7%reward_name%",
            Lists.newArrayList(new String[]{"%reward_lore%", "", "&eYour chance: &6%chance%%", "", "&7(This lore is configured at the menus folder)"})
         );
         this.rewardSelectiveModeItem = new CratePreviewMenu.RewardItemConfiguration(
            "&7%reward_name%",
            Lists.newArrayList(
               new String[]{
                  "%reward_lore%",
                  "",
                  "&eRequired Keys: &6%required_keys%",
                  "&eAmount Limit: &6%amount_limit%",
                  "",
                  "&7(This lore is configured at the menus folder)"
               }
            )
         );
         this.rewardTimeLimitedItem = new CratePreviewMenu.RewardItemConfiguration(
            "&7%reward_name%",
            Lists.newArrayList(new String[]{"%reward_lore%", "", "&eTime Left: &6%time%", "", "&7(This lore is configured at the menus folder)"})
         );
         this.daysFormat = "%value%D";
         this.hoursFormat = "%value%H";
         this.minutesFormat = "%value%M";
         this.secondsFormat = "%value%S";
         this.lockedRewardItem = new ContainerItemsConfiguration.NonSlotItemConfiguration(
            new ComplexMaterial(XMaterial.RED_STAINED_GLASS_PANE),
            0,
            "",
            false,
            "&7%reward_name% &c(Disabled)",
            Lists.newArrayList(new String[]{"&8(This reward is currently disabled.)", "", "&7(This lore is configured at the menus folder)"}),
            Lists.newArrayList()
         );
         this.winLimitLore = Lists.newArrayList(new String[]{"&fWin Limit: &e%win_amount%&7/&e%win_limit%"});
         this.globalWinLimitLore = Lists.newArrayList(new String[]{"&fGlobal Win Limit: &e%global_win_amount%&7/&e%global_limit%"});
         this.winLimitCooldownLore = Lists.newArrayList(new String[]{"&cWin limit cooldown: &e%cooldown%"});
         this.globalWinLimitCooldownLore = Lists.newArrayList(new String[]{"&cGlobal win limit cooldown: &e%global_cooldown%"});
         this.timeLimitedExpiredLore = Lists.newArrayList(new String[]{"&cThis reward has expired."});
         this.restrictedPermissionLore = Lists.newArrayList(new String[]{"&cRestricted from obtaining this reward."});
         this.requiredPermissionLore = Lists.newArrayList(new String[]{"&cNo permission for this reward."});
         this.disabledLore = Lists.newArrayList(new String[]{"&cThis reward is currently disabled."});
         this.availableKeysItem = new ContainerItemsConfiguration.NonActionsItemConfiguration(
            SlotCompound.from(7, 6),
            new ComplexMaterial(XMaterial.NETHER_STAR),
            1,
            0,
            "",
            false,
            true,
            "&aAvailable Keys &ex%available_keys%",
            Lists.newArrayList(new String[]{"&7All the available keys, that", "&7you can use on this crate. ", "", "&fKeys:", "%keys%"})
         );
         this.keyDisplay = "&7%position%. %name% &ex%amount%";
      }

      public CratePreviewMenu.CratePreviewMenuConfiguration clone() {
         return new CratePreviewMenu.CratePreviewMenuConfiguration();
      }

      @Generated
      public CratePreviewMenu.RewardItemConfiguration getRewardRandomModeItem() {
         return this.rewardRandomModeItem;
      }

      @Generated
      public CratePreviewMenu.RewardItemConfiguration getRewardSelectiveModeItem() {
         return this.rewardSelectiveModeItem;
      }

      @Generated
      public CratePreviewMenu.RewardItemConfiguration getRewardTimeLimitedItem() {
         return this.rewardTimeLimitedItem;
      }

      @Generated
      public ContainerItemsConfiguration.NonActionsItemConfiguration getAvailableKeysItem() {
         return this.availableKeysItem;
      }

      @Generated
      public String getKeyDisplay() {
         return this.keyDisplay;
      }

      @Generated
      public ContainerItemsConfiguration.NonSlotItemConfiguration getLockedRewardItem() {
         return this.lockedRewardItem;
      }

      @Generated
      public List<String> getWinLimitLore() {
         return this.winLimitLore;
      }

      @Generated
      public List<String> getGlobalWinLimitLore() {
         return this.globalWinLimitLore;
      }

      @Generated
      public List<String> getRestrictedPermissionLore() {
         return this.restrictedPermissionLore;
      }

      @Generated
      public List<String> getRequiredPermissionLore() {
         return this.requiredPermissionLore;
      }

      @Generated
      public String getDaysFormat() {
         return this.daysFormat;
      }

      @Generated
      public String getHoursFormat() {
         return this.hoursFormat;
      }

      @Generated
      public String getMinutesFormat() {
         return this.minutesFormat;
      }

      @Generated
      public String getSecondsFormat() {
         return this.secondsFormat;
      }

      @Generated
      public List<String> getDisabledLore() {
         return this.disabledLore;
      }

      @Generated
      public List<String> getWinLimitCooldownLore() {
         return this.winLimitCooldownLore;
      }

      @Generated
      public List<String> getGlobalWinLimitCooldownLore() {
         return this.globalWinLimitCooldownLore;
      }

      @Generated
      public List<String> getTimeLimitedExpiredLore() {
         return this.timeLimitedExpiredLore;
      }

      @Generated
      public boolean isSkipObtainedRewards() {
         return this.skipObtainedRewards;
      }
   }

   public static class RewardItemConfiguration {
      @ConfigField
      protected String name;
      @ConfigField
      protected List<String> lore;

      @Generated
      public String getName() {
         return this.name;
      }

      @Generated
      public List<String> getLore() {
         return this.lore;
      }

      @Generated
      public RewardItemConfiguration() {
      }

      @Generated
      public RewardItemConfiguration(String name, List<String> lore) {
         this.name = name;
         this.lore = lore;
      }
   }
}
