package id.seria.gacha.editor.layouts.crate.rewards;

import com.google.common.collect.Lists;
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
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.LinkedComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.SimplePaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemStackUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Operations;
import id.seria.gacha.limbo.menus.ConfirmationMenu;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.registries.InformationRegistry;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EditorCrateRarityGroupLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;
   private final String rarityIdentifier;
   private final String rarityDisplayName;

   public EditorCrateRarityGroupLayout(CrateType crateType, String rarityIdentifier, String rarityDisplayName, EditorFacade.EditorLayout parent) {
      super(Translatable.literal("Editor > ... > " + ChatColor.stripColor(Translations.r(rarityDisplayName)) + " Rewards"), parent);
      this.crateType = crateType;
      this.rarityIdentifier = rarityIdentifier;
      this.rarityDisplayName = rarityDisplayName;
   }

   private List<CustomReward> getFilteredRewards() {
      return this.crateType.getRegisteredRewards().stream().filter(reward -> {
         String rewardRarity = InformationRegistry.getValue(reward, Addons.REWARD_RARITY_IDENTIFIER, String.class);
         return this.rarityIdentifier.equals(rewardRarity);
      }).collect(Collectors.toList());
   }

   @Override
   public void onPrepare(ContainerView view) {
      SimplePaginatedList<CustomReward> list = new SimplePaginatedList<>(
         SlotCompound.fromGrid(10, 3, 7),
         new PagingData(PaginationSettings.ItemsAlign.LEFT, SlotCompound.from(1, 3), SlotCompound.from(9, 3)),
         this.getFilteredRewards()
      );
      list.setItemsSupplier(v -> this.getFilteredRewards());
      list.setOnItemRender(
         (context, index, item, reward) -> {
            ItemStack displayItem = Operations.o(reward.getDisplayItem()).map(Item::build).orElse(CustomReward.INVALID_REWARD);
            LoreBuilder builder = LoreBuilder.builder(this)
               .description(ItemStackUtil.extractRawLore(displayItem))
               .actions(
                  Lists.newArrayList(
                     new ClickAction[]{
                        ClickAction.EDIT(ClickAction.ClickType.LEFT, this.label("reward")),
                        ClickAction.DELETE(ClickAction.ClickType.RIGHT, this.label("reward"))
                     }
                  )
               )
               .error(() -> !reward.isDisplayValid(), Translatable.literal("&cInvalid reward display"));
            if (this.crateType.getRewardsMode() == RewardsMode.SELECTIVE) {
               builder.attribute(this.label("required-keys"), reward.getRequiredKeys());
            } else {
               double chance = RewardFacade.calculateChance(reward, this.crateType.getRegisteredRewards(), context.getViewer());
               boolean guaranteedWin = reward.getGuaranteedWin() > 0 && this.crateType.getGuaranteedRewardType() != GuaranteedRewardType.DISABLED;
               if (guaranteedWin) {
                  builder.attribute(this.label("openings"), reward.getGuaranteedWin());
               }

               if (!guaranteedWin || reward.getWeight() > 0.0) {
                  builder.attribute(this.label("weight"), Constants.DECIMAL1_FORMAT.format(reward.getWeight()), guaranteedWin);
                  builder.attribute(this.label("chance"), Constants.DECIMAL2_FORMAT.format(chance) + "%");
               }
            }

            builder.attribute(this.label("enabled"), reward.isEnabled() && !reward.isTimeLimitedExpired());
            String rewardName = RItemStack.of(displayItem).displayName().toPlainText();
            item.withItem(displayItem).withDisplayName("\u00a7a" + (index + 1) + ". " + rewardName).withLore(builder.build());
         }
      );
      list.setOnItemClick((context, index, reward) -> {
         if (context.isLeftClick()) {
            new EditorCrateRewardLayout(this.crateType, reward, this).open(context.getContainerView());
         } else if (context.isRightClick()) {
            Player player = context.getViewer();
            new ConfirmationMenu(PhoenixCrates.getLegacyInstance(), "Do you want to delete this reward?", () -> {
               this.crateType.removeReward(reward);
               EditorFacade.saveCrate(context.getContainerView(), this.crateType);
               context.getContainerView().updateView();
            }, () -> this.open(player), Operations.o(reward.getDisplayItem()).map(Item::build).orElse(CustomReward.INVALID_REWARD)).open(player);
         }
      });
      this.addComponent(view, list);
      this.addComponent(
         view,
         new LinkedComponent(
               SlotCompound.from(3, 6),
               this.createData(
                  XMaterial.PAPER,
                  Translatable.literal("Preview Menu"),
                  Translatable.literal("&7Custom menu for previewing", "&7rewards in this group.", "", "&7(Default: crate preview menu)")
               ),
               "rarity.group.preview-menu"
            )
            .withMetadata("rarityIdentifier", this.rarityIdentifier)
            .setFallbackError(Translatable.literal("&cRarity addon required."))
            .setClickAction(ClickAction.CHOOSE(ClickAction.ClickType.LEFT, this.label("menu")))
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         new LinkedComponent(
               SlotCompound.from(7, 6),
               this.createData(
                  XMaterial.CHEST,
                  Translatable.literal("Display Item"),
                  Translatable.literal("&7Custom display item for this", "&7group on preview/selective menus.", "", "&7(Default: chest icon)")
               ),
               "rarity.group.display-item"
            )
            .withMetadata("rarityIdentifier", this.rarityIdentifier)
            .setFallbackError(Translatable.literal("&cRarity addon required."))
            .setFixedPosition(true)
      );
   }
}
