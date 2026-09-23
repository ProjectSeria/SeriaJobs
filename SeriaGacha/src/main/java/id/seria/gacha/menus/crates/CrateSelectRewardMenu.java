package id.seria.gacha.menus.crates;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.api.crate.events.CrateSelectiveConfirmEvent;
import id.seria.gacha.api.crate.exceptions.CrateOpeningException;
import id.seria.gacha.api.internal.Item;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.api.session.OpeningContext;
import id.seria.gacha.facades.KeyFacade;
import id.seria.gacha.internal.Addons;
import id.seria.gacha.internal.Placeholders;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.internal.menus.GenericConfigurableLayout;
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
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.managers.players.PlayerData;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.menus.MenuFactory;
import id.seria.gacha.placeholders.Placeholder;
import id.seria.gacha.registries.InformationRegistry;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Generated;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CrateSelectRewardMenu extends GenericConfigurableLayout<CrateSelectRewardMenu.CrateSelectRewardMenuConfiguration> {
   private final Crate crate;
   private final String filterRarityId;
   private final boolean virtual;
   private Runnable onCloseAction;
   private boolean ignoreNextClose;

   public CrateSelectRewardMenu(CrateSelectRewardMenu.CrateSelectRewardMenuConfiguration configuration, @NonNull Crate crate) {
      this(configuration, crate, null, false);
      if (crate == null) {
         throw new NullPointerException("crate is marked non-null but is null");
      }
   }

   public CrateSelectRewardMenu(CrateSelectRewardMenu.CrateSelectRewardMenuConfiguration configuration, @NonNull Crate crate, String filterRarityId) {
      this(configuration, crate, filterRarityId, false);
      if (crate == null) {
         throw new NullPointerException("crate is marked non-null but is null");
      }
   }

   public CrateSelectRewardMenu(
      CrateSelectRewardMenu.CrateSelectRewardMenuConfiguration configuration, @NonNull Crate crate, String filterRarityId, boolean virtual
   ) {
      super(configuration);
      if (crate == null) {
         throw new NullPointerException("crate is marked non-null but is null");
      }

      this.crate = crate;
      this.filterRarityId = filterRarityId;
      this.virtual = virtual;
   }

   @Override
   public void onPrepare(ContainerView view) {
      view.setMetadata("available-keys", KeyFacade.countTotalAvailableKeys(this.crate.getType(), view.getViewer()), true);
      view.setMetadata(
         "has-permission", !this.crate.getType().isPermissionRequired() || view.getViewer().hasPermission(this.crate.getType().getPermission()), true
      );
      SimplePaginatedList<CustomReward> rewards = view.computeMetadataIfAbsent("selective-rewards.list", this::createPaginationComponent);
      this.addComponent(view, rewards);
      this.addComponent(view, this.createAvailableKeysComponent());
   }

   private SimpleComponent createAvailableKeysComponent() {
      return new SimpleComponent(this.getConfiguration().getAvailableKeysItem().getSlot(), null) {
         @Override
         public void onRender(RenderViewContext context) {
            Player player = context.getViewer();
            ContainerItemsConfiguration.NonActionsItemConfiguration availableKeysConfig = CrateSelectRewardMenu.this.getConfiguration()
               .getAvailableKeysItem()
               .clone();
            List<String> keys = new ArrayList<>();
            int position = 1;

            for (Key key : KeyFacade.getCrateKeys(CrateSelectRewardMenu.this.crate.getType())) {
               keys.add(
                  CrateSelectRewardMenu.this.r(
                     CrateSelectRewardMenu.this.getConfiguration().getKeyDisplay(),
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

            int totalAvailableKeys = KeyFacade.countTotalAvailableKeys(CrateSelectRewardMenu.this.crate.getType(), player);
            availableKeysConfig.setDisplayName(
               CrateSelectRewardMenu.this.r(
                  availableKeysConfig.getDisplayName(),
                  "%available_keys%",
                  totalAvailableKeys,
                  "%crate%",
                  CrateSelectRewardMenu.this.crate.getType().getFormattedDisplayName(player)
               )
            );
            availableKeysConfig.setLore(
               CrateSelectRewardMenu.this.r(
                  availableKeysConfig.getLore(),
                  "%keys%",
                  keys,
                  "%available_keys%",
                  totalAvailableKeys,
                  "%crate%",
                  CrateSelectRewardMenu.this.crate.getType().getFormattedDisplayName(player)
               )
            );
            CrateSelectRewardMenu.this.setItem(context, availableKeysConfig, click -> {});
         }
      };
   }

   private List<CustomReward> getRewardsList() {
      List<CustomReward> rewards = this.crate.getType().getRegisteredRewards();
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

      for (CustomReward reward : this.getRewardsList()) {
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

      for (CustomReward reward : this.getRewardsList()) {
         String rarityId = InformationRegistry.getValue(reward, Addons.REWARD_RARITY_IDENTIFIER, String.class);
         if (rarityId != null && groupRarities.containsKey(rarityId) && seenRarityIds.add(rarityId)) {
            representatives.add(reward.getIdentifier());
         }
      }

      return representatives;
   }

   private long countRewardsInGroup(String rarityId) {
      return this.getRewardsList()
         .stream()
         .filter(r -> rarityId.equals(InformationRegistry.getValue(r, Addons.REWARD_RARITY_IDENTIFIER, String.class)))
         .count();
   }

   private void renderGroupItem(RenderViewContext context, VirtualItem item, CustomReward groupRepresentative, String rarityId) {
      Item groupDisplayItem = InformationRegistry.getValue(groupRepresentative, Addons.RARITY_DISPLAY_ITEM, Item.class);
      String rarityDisplayName = InformationRegistry.getValue(groupRepresentative, Addons.RARITY_DISPLAY_NAME, String.class);
      long groupSize = this.countRewardsInGroup(rarityId);
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
               : Lists.newArrayList(new String[]{"&7Click to select from this category.", "", "&7Rewards: &e" + groupSize})
         )
         .whenClick(click -> {
            CrateSelectRewardMenu groupMenu = new CrateSelectRewardMenu(this.getConfiguration(), this.crate, rarityId, this.virtual);
            groupMenu.onCloseAction = () -> this.open(click.getViewer());
            this.ignoreNextClose = true;
            groupMenu.open(click.getViewer());
         });
   }

   private SimplePaginatedList<CustomReward> createPaginationComponent() {
      Map<String, String> groupRarities = this.computeGroupRarities();
      Set<String> groupRepresentatives = this.computeGroupRepresentatives(groupRarities);
      return (SimplePaginatedList<CustomReward>)this.<CustomReward>createSimplePagination(this.getConfiguration(), CustomReward.class, view -> {
            List<CustomReward> rewardsList = this.getRewardsList();
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

               rewardsList = new ArrayList<>(groups);
               rewardsList.addAll(ungrouped);
            }

            return rewardsList;
         })
         .setOnItemRender(
            (context, index, item, reward) -> {
               if (groupRepresentatives.contains(reward.getIdentifier())) {
                  String rarityId = InformationRegistry.getValue(reward, Addons.REWARD_RARITY_IDENTIFIER, String.class);
                  if (rarityId != null && groupRarities.containsKey(rarityId)) {
                     this.renderGroupItem(context, item, reward, rarityId);
                     return;
                  }
               }

               Player player = context.getViewer();
               boolean hasPermission = context.getContainerView().getMetadata("has-permission", true);
               int availableKeys = context.getContainerView().getMetadata("available-keys", 0);
               List<String> availableRewards = this.crate.getAvailableRewards(player).stream().map(BaseReward::getIdentifier).collect(Collectors.toList());
               CrateSelectRewardMenu.CrateSelectRewardMenuConfiguration containerConfiguration = this.getConfiguration();
               String rarityDisplay = InformationRegistry.getValue(reward, Addons.RARITY_REWARD_DISPLAY, String.class);
               boolean hasRarity = rarityDisplay != null && !rarityDisplay.trim().isEmpty();
               String rarityName = hasRarity ? rarityDisplay : "";
               if (availableRewards.contains(reward.getIdentifier())) {
                  int requiredKeys = this.crate.getType().isKeyRequired() ? reward.getRequiredKeys() : 0;
                  boolean notEnoughKeys = availableKeys < requiredKeys;
                  CrateSelectRewardMenu.RewardItemConfiguration rewardConfiguration = containerConfiguration.getUnlockedRewardItem();
                  Placeholder.Context<CustomReward> placeholderContext = Placeholder.context(reward)
                     .with("viewer", player)
                     .with("required_keys", requiredKeys)
                     .with("rarity_name", rarityName);
                  List<String> loreTemplate = new ArrayList<>(rewardConfiguration.getLore());
                  if (!hasRarity) {
                     loreTemplate.removeIf(line -> line.contains("%rarity_name%"));
                  }

                  List<String> lore = placeholderContext.parse(loreTemplate);
                  if (reward.getWinLimits() > 0 || reward.getGlobalWinLimit() > 0) {
                     PlayerData playerData = Crates.getPlayersManager().getCachedDataNow(player);
                     int winAmount = playerData.getRewardWinAmount(reward.getIdentifier());
                     int globalWinAmount = Crates.getGlobalRewardStatsTracker().getWinAmount(this.crate.getType().getIdentifier(), reward.getIdentifier());
                     int effectiveWinAmount = reward.getWinLimitCooldown() > 0L && reward.getWinLimits() > 0 && winAmount >= reward.getWinLimits()
                        ? 0
                        : winAmount;
                     int effectiveGlobalWinAmount = reward.getGlobalWinLimitCooldown() > 0L
                           && reward.getGlobalWinLimit() > 0
                           && globalWinAmount >= reward.getGlobalWinLimit()
                        ? 0
                        : globalWinAmount;
                     placeholderContext.with("win_amount", effectiveWinAmount)
                        .with("win_limit", reward.getWinLimits() < 0 ? "\u221e" : String.valueOf(reward.getWinLimits()))
                        .with("global_win_amount", effectiveGlobalWinAmount)
                        .with("global_limit", reward.getGlobalWinLimit() < 0 ? "\u221e" : String.valueOf(reward.getGlobalWinLimit()));
                     List<String> winLimitInfo = new ArrayList<>();
                     if (reward.getWinLimits() > 0) {
                        winLimitInfo.addAll(placeholderContext.parse(containerConfiguration.getWinLimitLore()));
                     }

                     if (reward.getGlobalWinLimit() > 0) {
                        winLimitInfo.addAll(placeholderContext.parse(containerConfiguration.getGlobalWinLimitLore()));
                     }

                     if (!winLimitInfo.isEmpty()) {
                        if (!lore.isEmpty()) {
                           lore.add("");
                        }

                        lore.addAll(winLimitInfo);
                     }
                  }

                  if (!hasPermission) {
                     lore.add(this.t("crate.no-permission", "%permission%", this.crate.getType().getPermission())[0]);
                  }

                  item.withItem(RItemStack.of(Operations.o(reward.getDisplayItem()).map(Item::build).orElse(CustomReward.INVALID_REWARD)))
                     .withDisplayName(placeholderContext.parse(rewardConfiguration.getDisplayName()))
                     .withLore(lore);
                  if (!notEnoughKeys && hasPermission) {
                     item.whenClick(
                        event -> {
                           this.ignoreNextClose = true;
                           Runnable onConfirm = () -> {
                              try {
                                 CrateSelectiveConfirmEvent confirmEvent = new CrateSelectiveConfirmEvent(this.crate, context.getViewer(), reward);
                                 Bukkit.getPluginManager().callEvent(confirmEvent);
                                 if (confirmEvent.isCancelled()) {
                                    return;
                                 }

                                 this.crate.openCrate(OpeningContext.selective(context.getViewer(), reward));
                              } catch (CrateOpeningException e) {
                                 Translations.send(context.getViewer(), e.getMessage());
                              }
                           };
                           if (Crates.getConfiguration().isDisableConfirmationMenus()) {
                              onConfirm.run();
                           } else {
                              ConfirmationMenu.ConfirmationMenuConfiguration confirmConfig = (ConfirmationMenu.ConfirmationMenuConfiguration)MenuFactory.getInstance()
                                 .getConfiguration("confirmation_menu");
                              new ConfirmationMenu(
                                    confirmConfig,
                                    onConfirm,
                                    () -> this.open(context.getViewer()),
                                    Operations.o(reward.getDisplayItem()).map(Item::build).orElse(CustomReward.INVALID_REWARD),
                                    placeholderContext.parse(rewardConfiguration.getDisplayName())
                                 )
                                 .open(context.getViewer());
                           }
                        }
                     );
                  } else if (notEnoughKeys) {
                     item.whenClick(
                        event -> {
                           this.ignoreNextClose = true;
                           Player viewer = context.getViewer();
                           viewer.closeInventory();
                           if (!this.virtual && Crates.getConfiguration().getCratesKnockback() > 0.0) {
                              Location crateLocation = this.crate.getLocation().getBlock().getLocation().add(0.5, 0.5, 0.5);
                              viewer.setVelocity(
                                 viewer.getLocation()
                                    .subtract(crateLocation)
                                    .toVector()
                                    .normalize()
                                    .setY(0.1)
                                    .multiply(Crates.getConfiguration().getCratesKnockback())
                              );
                              viewer.playSound(crateLocation, "minecraft:block.note_block.bass", 1.0F, 1.0F);
                           }

                           Translations.send(viewer, this.t("crate.redeem-no-keys", "%required_keys%", reward.getRequiredKeys()));
                        }
                     );
                  }
               } else {
                  ContainerItemsConfiguration.NonSlotItemConfiguration rewardConfiguration = containerConfiguration.getLockedRewardItem();
                  Placeholder.Context<CustomReward> placeholderContext = Placeholder.context(reward)
                     .with("viewer", player)
                     .with("required_keys", reward.getRequiredKeys())
                     .with("rarity_name", rarityName);
                  List<String> lockedLoreTemplate = new ArrayList<>(rewardConfiguration.getLore());
                  if (!hasRarity) {
                     lockedLoreTemplate.removeIf(line -> line.contains("%rarity_name%"));
                  }

                  List<String> lore = new ArrayList<>(placeholderContext.parse(lockedLoreTemplate));
                  long now = System.currentTimeMillis();
                  PlayerData playerData = Crates.getPlayersManager().getCachedDataNow(player);
                  int winAmount = playerData.getRewardWinAmount(reward.getIdentifier());
                  long winCooldownRemaining = reward.getWinLimitCooldown() > 0L
                     ? Math.max(0L, reward.getWinLimitCooldown() * 1000L - (now - playerData.getRewardCooldown(reward.getIdentifier())))
                     : 0L;
                  boolean winCooldownActive = winCooldownRemaining > 0L;
                  int globalWinAmount = Crates.getGlobalRewardStatsTracker().getWinAmount(this.crate.getType().getIdentifier(), reward.getIdentifier());
                  long lastGlobalWin = Crates.getGlobalRewardStatsTracker().getLastWinTimestamp(this.crate.getType().getIdentifier(), reward.getIdentifier());
                  long globalCooldownRemaining = reward.getGlobalWinLimitCooldown() > 0L
                     ? Math.max(0L, reward.getGlobalWinLimitCooldown() * 1000L - (now - lastGlobalWin))
                     : 0L;
                  boolean globalWinCooldownActive = globalCooldownRemaining > 0L;
                  boolean timeLimitedExpired = reward.getTimeLimitedExpiry() > 0L && now > reward.getTimeLimitedExpiry();
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
                  List<String> lockReasons = new ArrayList<>();
                  if (!reward.isEnabled()) {
                     lockReasons.addAll(placeholderContext.parse(containerConfiguration.getDisabledLore()));
                  }

                  if (reward.getWinLimits() > 0 && reward.isEnabled() && !timeLimitedExpired) {
                     lockReasons.addAll(placeholderContext.parse(containerConfiguration.getWinLimitLore()));
                  }

                  if (winCooldownActive) {
                     lockReasons.addAll(placeholderContext.parse(containerConfiguration.getWinLimitCooldownLore()));
                  }

                  if (reward.getGlobalWinLimit() > 0 && reward.isEnabled() && !timeLimitedExpired) {
                     lockReasons.addAll(placeholderContext.parse(containerConfiguration.getGlobalWinLimitLore()));
                  }

                  if (globalWinCooldownActive) {
                     lockReasons.addAll(placeholderContext.parse(containerConfiguration.getGlobalWinLimitCooldownLore()));
                  }

                  if (timeLimitedExpired) {
                     lockReasons.addAll(placeholderContext.parse(containerConfiguration.getTimeLimitedExpiredLore()));
                  }

                  if (reward.isBlacklistRestricted(player)) {
                     lockReasons.addAll(placeholderContext.parse(containerConfiguration.getRestrictedPermissionLore()));
                  }

                  if (reward.isRequiredPermissionMissing(player)) {
                     lockReasons.addAll(placeholderContext.parse(containerConfiguration.getRequiredPermissionLore()));
                  }

                  if (!lockReasons.isEmpty() && !lore.isEmpty()) {
                     lore.add("");
                  }

                  lore.addAll(lockReasons);
                  item.withItem(rewardConfiguration.getMaterial())
                     .withDisplayName(placeholderContext.parse(rewardConfiguration.getDisplayName()))
                     .withLore(lore)
                     .whenClick(event -> {});
               }
            }
         );
   }

   private String getRewardRarityIdentifier(BaseReward reward) {
      return InformationRegistry.getValue(reward, Addons.REWARD_RARITY_IDENTIFIER, String.class);
   }

   private Set<String> getKeyRarityIdentifiers(Key key) {
      Set<?> identifiers = InformationRegistry.getValue(key, Addons.KEY_RARITY_IDENTIFIERS, Set.class);
      if (identifiers != null && !identifiers.isEmpty()) {
         Set<String> resolved = new HashSet<>();

         for (Object identifier : identifiers) {
            if (identifier instanceof String) {
               resolved.add((String)identifier);
            }
         }

         return resolved;
      } else {
         return Collections.emptySet();
      }
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
   public void onOpen(OpenViewContext context) {
      super.onOpen(context);
      context.setContainerTitle(
         Placeholders.setPlaceholders(
            PlaceholderUtil.replacePlaceholdersAndColors(
               this.getPlugin(), this.getConfiguration().getSettings().getTitle(), "%crate%", this.crate.getType().getFormattedDisplayName(context.getViewer())
            ),
            context.getViewer()
         )
      );
      context.setContainerType(ContainerType.fromRows(this.getConfiguration().getSettings().getRows()));
   }

   @Generated
   public Crate getCrate() {
      return this.crate;
   }

   @Generated
   public String getFilterRarityId() {
      return this.filterRarityId;
   }

   @Generated
   public boolean isVirtual() {
      return this.virtual;
   }

   @Generated
   public Runnable getOnCloseAction() {
      return this.onCloseAction;
   }

   @Generated
   public boolean isIgnoreNextClose() {
      return this.ignoreNextClose;
   }

   public static class CrateSelectRewardMenuConfiguration extends PaginatedContainerConfiguration {
      @ConfigField(path = "reward.unlocked")
      protected final CrateSelectRewardMenu.RewardItemConfiguration unlockedRewardItem;
      @ConfigField(path = "reward.locked")
      protected final ContainerItemsConfiguration.NonSlotItemConfiguration lockedRewardItem;
      @ConfigField(path = "reward.rarity-locked")
      protected final ContainerItemsConfiguration.NonSlotItemConfiguration rarityLockedRewardItem;
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
      @ConfigField(path = "available-keys")
      protected final ContainerItemsConfiguration.NonActionsItemConfiguration availableKeysItem;
      @ConfigField(path = "available-keys.key-display")
      protected final String keyDisplay;

      public CrateSelectRewardMenuConfiguration() {
         this.settings.setType("SELECT_REWARD");
         this.settings.setTitle("%crate% &r&7Rewards");
         this.settings.setRows(6);
         this.settings.setRefreshEnabled(true);
         this.settings.setRefreshRate(20);
         this.items.clear();
         ContainerItemsConfiguration.ItemConfiguration downFiller = new ContainerItemsConfiguration.ItemConfiguration(
            SlotCompound.fromRange(1, 6, 9, 6),
            new ComplexMaterial(XMaterial.BLACK_STAINED_GLASS_PANE),
            1,
            0,
            "",
            false,
            "\u00a7e",
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
                  new ComplexMaterial(XMaterial.ARROW),
                  1,
                  0,
                  "",
                  false,
                  "&aClose Menu",
                  Lists.newArrayList(new String[]{"&7Click here to close this menu.", "", "&6Left-Click: &eClose menu."}),
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
         this.unlockedRewardItem = new CrateSelectRewardMenu.RewardItemConfiguration(
            "&7%reward_name%",
            Lists.newArrayList(
               new String[]{
                  "%reward_lore%",
                  "",
                  "&fRarity: &6%rarity_name%",
                  "&fRequired Keys: &7%required_keys%",
                  "",
                  "&7(This lore is configured at the menus folder)",
                  "",
                  "&6Left-Click: &eClick here to redeem."
               }
            )
         );
         this.lockedRewardItem = new ContainerItemsConfiguration.NonSlotItemConfiguration(
            new ComplexMaterial(XMaterial.RED_STAINED_GLASS_PANE),
            0,
            "",
            false,
            "&7%reward_name% &c(Locked)",
            Lists.newArrayList(
               new String[]{
                  "&8(This reward is locked)",
                  "",
                  "&fRarity: %rarity_name%",
                  "&fRequired Keys: &7%required_keys%",
                  "",
                  "&7(This lore is configured at the menus folder)"
               }
            ),
            Lists.newArrayList()
         );
         this.daysFormat = "%value%D";
         this.hoursFormat = "%value%H";
         this.minutesFormat = "%value%M";
         this.secondsFormat = "%value%S";
         this.winLimitLore = Lists.newArrayList(new String[]{"&fWin Limit: &e%win_amount%&7/&e%win_limit%"});
         this.globalWinLimitLore = Lists.newArrayList(new String[]{"&fGlobal Win Limit: &e%global_win_amount%&7/&e%global_limit%"});
         this.winLimitCooldownLore = Lists.newArrayList(new String[]{"&cWin limit cooldown: &e%cooldown%"});
         this.globalWinLimitCooldownLore = Lists.newArrayList(new String[]{"&cGlobal win limit cooldown: &e%global_cooldown%"});
         this.timeLimitedExpiredLore = Lists.newArrayList(new String[]{"&cThis reward has expired."});
         this.restrictedPermissionLore = Lists.newArrayList(new String[]{"&cRestricted from obtaining this reward."});
         this.requiredPermissionLore = Lists.newArrayList(new String[]{"&cNo permission for this reward."});
         this.disabledLore = Lists.newArrayList(new String[]{"&cThis reward is currently disabled."});
         this.rarityLockedRewardItem = new ContainerItemsConfiguration.NonSlotItemConfiguration(
            new ComplexMaterial(XMaterial.RED_STAINED_GLASS_PANE),
            0,
            "",
            false,
            "&7%reward_name% &c(Locked)",
            Lists.newArrayList(
               new String[]{
                  "&8(This reward is locked)",
                  "",
                  "&fRarity: %rarity_name%",
                  "&fRequired Keys: &7%required_keys%",
                  "",
                  "&7(This lore is configured at the menus folder)",
                  "",
                  "&6Left-Click: &eClick here to redeem.",
                  "&cYour key's rarity can't redeem this reward."
               }
            ),
            Lists.newArrayList()
         );
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

      public CrateSelectRewardMenu.CrateSelectRewardMenuConfiguration clone() {
         return new CrateSelectRewardMenu.CrateSelectRewardMenuConfiguration();
      }

      @Generated
      public CrateSelectRewardMenu.RewardItemConfiguration getUnlockedRewardItem() {
         return this.unlockedRewardItem;
      }

      @Generated
      public ContainerItemsConfiguration.NonSlotItemConfiguration getLockedRewardItem() {
         return this.lockedRewardItem;
      }

      @Generated
      public ContainerItemsConfiguration.NonSlotItemConfiguration getRarityLockedRewardItem() {
         return this.rarityLockedRewardItem;
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
      public ContainerItemsConfiguration.NonActionsItemConfiguration getAvailableKeysItem() {
         return this.availableKeysItem;
      }

      @Generated
      public String getKeyDisplay() {
         return this.keyDisplay;
      }
   }

   public static class RewardItemConfiguration {
      @ConfigField
      protected String displayName;
      @ConfigField
      protected List<String> lore;

      @Generated
      public String getDisplayName() {
         return this.displayName;
      }

      @Generated
      public List<String> getLore() {
         return this.lore;
      }

      @Generated
      public RewardItemConfiguration() {
      }

      @Generated
      public RewardItemConfiguration(String displayName, List<String> lore) {
         this.displayName = displayName;
         this.lore = lore;
      }
   }
}
