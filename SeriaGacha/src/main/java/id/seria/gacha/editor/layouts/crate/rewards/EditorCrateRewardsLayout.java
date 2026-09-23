package id.seria.gacha.editor.layouts.crate.rewards;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.internal.Item;
import id.seria.gacha.api.reward.GuaranteedRewardType;
import id.seria.gacha.api.reward.RewardsMode;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.editor.layouts.crate.rewards.reward.EditorCrateRewardLayout;
import id.seria.gacha.facades.RewardFacade;
import id.seria.gacha.internal.Addons;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.LoreBuilder;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.SimplePaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableListOption;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableListSingleOption;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableBoolean;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableNumber;
import id.seria.gacha.lib.common.uicomponents.newest.layout.modifiers.ObjectModifiers;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemStackUtil;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.Operations;
import id.seria.gacha.limbo.PremiumEditorKeys;
import id.seria.gacha.limbo.menus.ConfirmationMenu;
import id.seria.gacha.managers.CratesManager;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.registries.EditorRegistry;
import id.seria.gacha.registries.InformationRegistry;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EditorCrateRewardsLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;

   public EditorCrateRewardsLayout(CrateType type, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.rewards"), parent);
      this.id = "crate-rewards-layout";
      this.crateType = type;
   }

   @Override
   public void onPrepare(ContainerView view) {
      boolean lite = Crates.getPlugin().getEdition() == Edition.LITE;
      EditorCrateRewardsLayout.RewardsListComponent rewardsList = view.computeMetadataIfAbsent(
         "rewards-list-component", () -> new EditorCrateRewardsLayout.RewardsListComponent()
      );
      this.addComponent(view, rewardsList);
      this.addComponent(
         view,
         (new EditableBoolean(
               SlotCompound.from(2, 6),
               this.createData(
                  XMaterial.REPEATER,
                  Translatable.key("editor.crate.rewards.reordering.title"),
                  Translatable.key("editor.crate.rewards.reordering.description")
               ),
               this.label("enabled"),
               rewardsList.isReordering()
            ) {})
            .setOnValueChange(value -> {
               rewardsList.setReordering(value);
               view.updateView();
            })
            .setFixedPosition(true)
      );
      int maxWinRewardsLimit = lite ? 5 : 30;
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(3, 6),
               this.createData(
                  XMaterial.NETHER_STAR,
                  Translatable.key("editor.crate.rewards.win-amount.title"),
                  Translatable.key("editor.crate.rewards.win-amount.description")
               ),
               this.label("amount"),
               this.crateType.getMaxWinRewards()
            )
            .addModifier(ObjectModifiers.CLAMP(1, maxWinRewardsLimit))
            .setOnValueChange(value -> {
               this.crateType.setMaxWinRewards(value.intValue());
               EditorFacade.saveCrate(view, this.crateType);
               view.updateView();
            })
            .removeClickAction(ClickAction.ClickType.SHIFT_LEFT)
            .removeClickAction(ClickAction.ClickType.SHIFT_RIGHT)
            .setFormatter(amount -> Translatable.literal(amount.intValue() + "/" + maxWinRewardsLimit))
            .addWarning(() -> !this.crateType.getRewardsMode().equals(RewardsMode.RANDOM), "&cOnly available with rewards \u00a7lrandom mode\u00a7c.")
            .setLocked(value -> !this.crateType.getRewardsMode().equals(RewardsMode.RANDOM))
      );
      this.addComponent(
         view,
         new EditableListOption<>(
               SlotCompound.from(7, 6),
               this.createData(
                  XMaterial.AMETHYST_SHARD.or(XMaterial.GOLD_INGOT),
                  Translatable.key("editor.crate.display-settings.rewards-mode.title"),
                  Translatable.key("editor.crate.display-settings.rewards-mode.description")
               ),
               Lists.newArrayList(RewardsMode.values()),
               this.crateType.getRewardsMode()
            )
            .setItemFormatter(
               option -> option == RewardsMode.SELECTIVE ? Translatable.literal("Selective &b&lNEW!") : Translatable.literal(StringUtil.humanize(option))
            )
            .setOnOptionChange(value -> {
               this.crateType.setRewardsMode(value);
               EditorFacade.saveCrate(view, this.crateType);
            })
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         new EditableListSingleOption<>(
               SlotCompound.from(8, 6),
               this.createData(
                  XMaterial.HEART_OF_THE_SEA.or(XMaterial.MELON_SLICE),
                  Translatable.literal("&aGuaranteed Mode"),
                  Translatable.literal(
                     "&7By clicking here you will be able",
                     "&7to define the type of guarantee rewards.",
                     "",
                     "&7By default no reward is guaranteed,",
                     "&7you can configure this on the reward."
                  )
               ),
               Arrays.asList(GuaranteedRewardType.values()),
               this.crateType.getGuaranteedRewardType()
            )
            .setOnOptionChange(value -> {
               this.crateType.setGuaranteedRewardType(value);
               EditorFacade.saveCrate(view, this.crateType);
            })
            .setItemFormatter(value -> Translatable.literal(StringUtil.humanize(value)))
            .setFixedPosition(true)
      );
   }

   private void save(ContainerView view) {
      try {
         CratesManager manager = Crates.getCratesManager();
         if (this.crateType.isRegistered()) {
            manager.saveCrateType(this.crateType);
         } else {
            manager.registerCrateType(this.crateType, true);
         }
      } catch (Exception e) {
         Translations.error(view.getViewer(), e);
         view.getViewer().closeInventory();
      }
   }

   @Generated
   public CrateType getCrateType() {
      return this.crateType;
   }

   private class RewardsListComponent extends SimplePaginatedList<CustomReward> {
      private boolean reordering;
      private Integer currentSelectionIndex;
      private final Set<String> groupRepresentativeIds = new LinkedHashSet<>();

      public void setReordering(boolean value) {
         this.reordering = value;
         if (!value) {
            this.currentSelectionIndex = null;
         }
      }

      private boolean isGroupRepresentative(CustomReward reward) {
         return this.groupRepresentativeIds.contains(reward.getIdentifier());
      }

      private String getGroupRarityId(CustomReward reward) {
         return InformationRegistry.getValue(reward, Addons.RARITY_GROUP_REWARDS, String.class);
      }

      private List<CustomReward> buildGroupedRewardsList() {
         List<CustomReward> all = EditorCrateRewardsLayout.this.crateType.getRegisteredRewards();
         List<CustomReward> groups = new ArrayList<>();
         List<CustomReward> ungrouped = new ArrayList<>();
         Set<String> seenGroupRarities = new LinkedHashSet<>();
         this.groupRepresentativeIds.clear();

         for (CustomReward reward : all) {
            String groupRarity = this.getGroupRarityId(reward);
            if (groupRarity != null) {
               if (!seenGroupRarities.contains(groupRarity)) {
                  seenGroupRarities.add(groupRarity);
                  this.groupRepresentativeIds.add(reward.getIdentifier());
                  groups.add(reward);
               }
            } else {
               ungrouped.add(reward);
            }
         }

         List<CustomReward> result = new ArrayList<>(groups);
         result.addAll(ungrouped);
         return result;
      }

      private int countRewardsInGroup(String rarityId) {
         int count = 0;

         for (CustomReward r : EditorCrateRewardsLayout.this.crateType.getRegisteredRewards()) {
            String rId = this.getGroupRarityId(r);
            if (rarityId.equals(rId)) {
               count++;
            }
         }

         return count;
      }

      public RewardsListComponent() {
         super(
            SlotCompound.fromGrid(10, 3, 7),
            new PagingData(PaginationSettings.ItemsAlign.LEFT, SlotCompound.from(1, 3), SlotCompound.from(9, 3)),
            EditorCrateRewardsLayout.this.crateType.getRegisteredRewards()
         );
         this.id = "crate-rewards";
         this.setItemsSupplier(view -> this.buildGroupedRewardsList());
         this.setOnItemRender(
               (context, index, item, reward) -> {
                  if (this.currentSelectionIndex != null && index == this.currentSelectionIndex) {
                     item.withItem(XMaterial.YELLOW_STAINED_GLASS_PANE)
                        .withDisplayName(EditorCrateRewardsLayout.this.t("editor.crate.rewards.reordering.selected.title")[0]);
                  } else if (this.isGroupRepresentative(reward)) {
                     String groupRarity = this.getGroupRarityId(reward);
                     String rarityDisplay = InformationRegistry.getValue(reward, Addons.RARITY_REWARD_DISPLAY, String.class);
                     String displayName = rarityDisplay != null && !rarityDisplay.trim().isEmpty() ? rarityDisplay : groupRarity;
                     int count = this.countRewardsInGroup(groupRarity);
                     item.withItem(XMaterial.CHEST)
                        .withAmount(Math.min(64, Math.max(1, count)))
                        .withDisplayName(displayName + " &7Rewards")
                        .withLore(
                           LoreBuilder.builder(EditorCrateRewardsLayout.this)
                              .description("&7This group contains &e" + count + " &7reward" + (count != 1 ? "s" : "") + ".")
                              .actions(ClickAction.NAVIGATE)
                              .build()
                        );
                  } else {
                     ItemStack displayItem = Operations.o(reward.getDisplayItem()).map(Item::build).orElse(CustomReward.INVALID_REWARD);
                     if (EditorCrateRewardsLayout.this.crateType.getRewardsMode() == RewardsMode.SELECTIVE) {
                        LoreBuilder builder = LoreBuilder.builder(EditorCrateRewardsLayout.this)
                           .description(ItemStackUtil.extractRawLore(displayItem))
                           .actions(this.getItemClickActions(reward))
                           .error(() -> !reward.isDisplayValid(), Translatable.key("editor.errors.invalid-reward-display"));
                        builder.attribute(EditorCrateRewardsLayout.this.label("required-keys"), reward.getRequiredKeys());
                        String rarityDisplay = InformationRegistry.getValue(reward, Addons.RARITY_REWARD_DISPLAY, String.class);
                        if (rarityDisplay != null && !rarityDisplay.trim().isEmpty()) {
                           builder.attribute(Translatable.literal("Rarity"), rarityDisplay);
                        }

                        String rewardName = RItemStack.of(displayItem).displayName().toPlainText();
                        item.withItem(displayItem).withDisplayName("\u00a7a" + (index + 1) + ". " + rewardName).withLore(builder.build());
                     } else {
                        double effectivePercentage = reward.getWeight();
                        double chance = RewardFacade.calculateChance(
                           reward, EditorCrateRewardsLayout.this.crateType.getRegisteredRewards(), context.getViewer()
                        );
                        LoreBuilder builder = LoreBuilder.builder(EditorCrateRewardsLayout.this)
                           .actions(this.getItemClickActions(reward))
                           .error(() -> !reward.isDisplayValid(), Translatable.key("editor.errors.invalid-reward-display"));
                        String rewardDisplayName = RItemStack.of(displayItem).displayName().toPlainText();
                        String displayName = "\u00a7a" + (index + 1) + ". " + rewardDisplayName;
                        boolean guaranteedWin = reward.getGuaranteedWin() > 0
                           && EditorCrateRewardsLayout.this.crateType.getGuaranteedRewardType() != GuaranteedRewardType.DISABLED;
                        if (guaranteedWin) {
                           builder.attribute(EditorCrateRewardsLayout.this.label("openings"), reward.getGuaranteedWin());
                           if (effectivePercentage > 0.0) {
                              displayName = "\u00a7e" + ChatColor.stripColor(displayName);
                           } else {
                              displayName = "\u00a7c" + ChatColor.stripColor(displayName);
                           }
                        }

                        if (!guaranteedWin || effectivePercentage > 0.0) {
                           builder.attribute(
                                 EditorCrateRewardsLayout.this.label("weight"), Constants.DECIMAL1_FORMAT.format(effectivePercentage), guaranteedWin
                              )
                              .attribute(EditorCrateRewardsLayout.this.label("chance"), Constants.DECIMAL2_FORMAT.format(chance) + "%");
                        }

                        String rarityDisplay = InformationRegistry.getValue(reward, Addons.RARITY_REWARD_DISPLAY, String.class);
                        if (rarityDisplay != null && !rarityDisplay.trim().isEmpty()) {
                           builder.attribute(Translatable.literal("Rarity"), rarityDisplay);
                        }

                        builder.description(ItemStackUtil.extractRawLore(displayItem));
                        item.withItem(displayItem).withDisplayName(displayName).withLore(builder.build());
                     }
                  }
               }
            )
            .setOnItemClick(
               (context, index, reward) -> {
                  if (this.reordering) {
                     if (this.currentSelectionIndex == null) {
                        this.currentSelectionIndex = index;
                        context.getContainerView().updateView();
                     } else {
                        int selectedIndex = this.currentSelectionIndex;
                        this.currentSelectionIndex = null;
                        if (selectedIndex != index) {
                           EditorCrateRewardsLayout.this.crateType.swapRewards(selectedIndex, index);
                           EditorFacade.saveCrate(context.getContainerView(), EditorCrateRewardsLayout.this.crateType);
                        }

                        context.getContainerView().updateView();
                     }
                  } else if (this.isGroupRepresentative(reward)) {
                     String groupRarity = this.getGroupRarityId(reward);
                     String rarityDisplay = InformationRegistry.getValue(reward, Addons.RARITY_REWARD_DISPLAY, String.class);
                     String displayName = rarityDisplay != null && !rarityDisplay.trim().isEmpty() ? rarityDisplay : groupRarity;
                     EditorFacade.EditorLayout groupLayout = EditorRegistry.getRarityGroup(
                        PremiumEditorKeys.RARITY_GROUP, EditorCrateRewardsLayout.this.crateType, groupRarity, displayName, EditorCrateRewardsLayout.this
                     );
                     if (groupLayout != null) {
                        groupLayout.open(context.getViewer());
                     }
                  } else if (context.isLeftClick()) {
                     new EditorCrateRewardLayout(EditorCrateRewardsLayout.this.crateType, reward, EditorCrateRewardsLayout.this)
                        .open(context.getContainerView());
                  } else if (context.isRightClick()) {
                     Player player = context.getViewer();
                     ConfirmationMenu confirmation = new ConfirmationMenu(PhoenixCrates.getLegacyInstance(), "Do you want to delete this reward?", () -> {
                        EditorCrateRewardsLayout.this.crateType.removeReward(reward);
                        EditorFacade.saveCrate(context.getContainerView(), EditorCrateRewardsLayout.this.crateType);
                        context.getContainerView().updateView();
                     }, () -> EditorCrateRewardsLayout.this.open(player), Operations.o(reward.getDisplayItem())
                        .map(Item::build)
                        .orElse(CustomReward.INVALID_REWARD));
                     confirmation.open(player);
                  }
               }
            );
      }

      @Override
      public VirtualItem onPrepareAddItem(RenderViewContext context) {
         if (this.reordering) {
            return VirtualItem.from(XMaterial.AIR);
         }

         boolean lite = Crates.getPlugin().getEdition() == Edition.LITE;
         LoreBuilder loreBuilder = LoreBuilder.builder(EditorCrateRewardsLayout.this)
            .description(Translatable.key("editor.crate.rewards.add.description"))
            .actions(
               ClickAction.ADD(ClickAction.ClickType.LEFT, EditorCrateRewardsLayout.this.label("reward")),
               ClickAction.ADD(ClickAction.ClickType.SHIFT_LEFT, EditorCrateRewardsLayout.this.label("multiple-rewards"))
            );
         if (lite) {
            loreBuilder.error(() -> true, Translatable.literal("&cMultiple Rewards only available on the premium version."));
         }

         return VirtualItem.from(XMaterial.LIME_STAINED_GLASS_PANE)
            .withDisplayName(EditorCrateRewardsLayout.this.t("editor.crate.rewards.add.title")[0])
            .withLore(loreBuilder.build())
            .withFlag(Constants.ITEM_FLAGS)
            .whenClick(
               click -> {
                  if (click.isShiftClick()) {
                     if (!lite) {
                        EditorFacade.EditorLayout addMultipleLayout = EditorRegistry.get(
                           PremiumEditorKeys.ADD_MULTIPLE_REWARDS, EditorCrateRewardsLayout.this.crateType, EditorCrateRewardsLayout.this
                        );
                        if (addMultipleLayout != null) {
                           addMultipleLayout.open(context.getViewer());
                        }
                     }
                  } else {
                     Player viewer = context.getViewer();

                     try {
                        new EditorCrateRewardLayout(EditorCrateRewardsLayout.this.crateType, RewardFacade.buildEmpty(), EditorCrateRewardsLayout.this)
                           .open(context.getContainerView());
                     } catch (Exception e) {
                        Translations.error(viewer, e);
                        viewer.closeInventory();
                     }
                  }
               }
            );
      }

      public List<ClickAction> getItemClickActions(CustomReward reward) {
         return this.reordering
            ? Lists.newArrayList(new ClickAction[]{new ClickAction(ClickAction.ClickType.LEFT, EditorCrateRewardsLayout.this.label("reorder-reward"))})
            : Lists.newArrayList(
               new ClickAction[]{
                  ClickAction.EDIT(ClickAction.ClickType.LEFT, EditorCrateRewardsLayout.this.label("reward")),
                  ClickAction.DELETE(ClickAction.ClickType.RIGHT, EditorCrateRewardsLayout.this.label("reward"))
               }
            );
      }

      @Generated
      public boolean isReordering() {
         return this.reordering;
      }

      @Generated
      public Integer getCurrentSelectionIndex() {
         return this.currentSelectionIndex;
      }

      @Generated
      public Set<String> getGroupRepresentativeIds() {
         return this.groupRepresentativeIds;
      }
   }
}
