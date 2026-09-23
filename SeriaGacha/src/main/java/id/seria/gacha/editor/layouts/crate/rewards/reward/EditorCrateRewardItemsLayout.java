package id.seria.gacha.editor.layouts.crate.rewards.reward;

import id.seria.gacha.Crates;
import id.seria.gacha.api.internal.Item;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.LoreBuilder;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.CloseViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.SimplePaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableNumber;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemStackUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.managers.CratesManager;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomReward;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EditorCrateRewardItemsLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;
   private final CustomReward reward;

   public EditorCrateRewardItemsLayout(CrateType crateType, CustomReward reward, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.reward-items"), parent);
      this.crateType = crateType;
      this.reward = reward;
   }

   @Override
   public void onPrepare(ContainerView view) {
      view.setCancelOnOtherInventoryClick(false);
      EditorCrateRewardItemsLayout.ItemsListComponent itemsList = view.computeMetadataIfAbsent(
         "items-list", () -> new EditorCrateRewardItemsLayout.ItemsListComponent()
      );
      this.addComponent(view, itemsList);
      int itemsMax = this.reward.getWinItems().size();
      int itemAmountNormalized = itemsMax == 0 ? -1 : Math.min(Math.max(-1, this.reward.getMaxWinItemAmount()), itemsMax);
      this.reward.setMaxWinItemAmount(itemAmountNormalized);
      boolean lite = Crates.getPlugin().getEdition() == Edition.LITE;
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(2, 6),
               this.createData(
                  XMaterial.ORANGE_SHULKER_BOX.or(XMaterial.CHEST),
                  Translatable.key("editor.crate.reward.max-win-item-amount.title"),
                  Translatable.key("editor.crate.reward.max-win-item-amount.description")
               ),
               this.label("amount"),
               itemAmountNormalized
            )
            .setFormatter(value -> Translatable.literal(value.intValue() < 0 ? "\u221e" : value.intValue()))
            .setClickAction(ClickAction.REMOVE(ClickAction.ClickType.RIGHT, Translatable.literal("1")), (editable, context) -> {
               int current = editable.getValue().get().intValue();
               if (current > -1) {
                  editable.safeSetValue(context.getContainerView(), current == 1 ? -1 : current - 1, true, false);
               }
            })
            .setClickAction(ClickAction.ADD(ClickAction.ClickType.LEFT, Translatable.literal("1")), (editable, context) -> {
               int current = editable.getValue().get().intValue();
               int max = this.reward.getWinItems().size();
               if (current < max) {
                  editable.safeSetValue(context.getContainerView(), current < 0 ? 1 : current + 1, true, false);
               }
            })
            .setClickAction(
               ClickAction.SET(ClickAction.ClickType.SHIFT_LEFT, this.label("unlimited")),
               (editable, context) -> editable.safeSetValue(context.getContainerView(), -1, true, false)
            )
            .removeClickAction(ClickAction.ClickType.SHIFT_RIGHT)
            .setOnValueChange(value -> {
               int max = this.reward.getWinItems().size();
               int clamped = max == 0 ? -1 : Math.min(Math.max(-1, value.intValue()), max);
               this.reward.setMaxWinItemAmount(clamped);
               this.save(view);
            })
            .addWarning(() -> lite, Translatable.key("plans.unlock-premium"))
            .addWarning(() -> lite, Translatable.key("plans.upgrade-at"))
            .setLocked(value -> lite)
      );
   }

   @Override
   public void onClick(ClickViewContext context) {
      if (context.getClickedInventory().equals(context.getViewer().getOpenInventory().getBottomInventory())) {
         EditorCrateRewardItemsLayout.ItemsListComponent itemsList = context.getContainerView()
            .computeMetadataIfAbsent("items-list", () -> new EditorCrateRewardItemsLayout.ItemsListComponent());
         itemsList.selected = context.getCurrentItem() != null;
         Crates.getScheduler().runSync(context.getViewer(), () -> context.getContainerView().updateView(), 1L);
      }

      super.onClick(context);
   }

   @Override
   public void onClose(CloseViewContext context) {
      this.save(context.getHolder());
   }

   private void save(ContainerView view) {
      Player player = view.getViewer();

      try {
         if (!this.reward.isRegistered()) {
            this.crateType.addReward(this.reward);
         }

         CratesManager manager = Crates.getCratesManager();
         if (this.crateType.isRegistered()) {
            manager.saveCrateType(this.crateType);
         } else {
            manager.registerCrateType(this.crateType, true);
         }
      } catch (Exception e) {
         Translations.error(player, e);
         player.closeInventory();
      }
   }

   @Generated
   public CrateType getCrateType() {
      return this.crateType;
   }

   @Generated
   public CustomReward getReward() {
      return this.reward;
   }

   private class ItemsListComponent extends SimplePaginatedList<Item> {
      private boolean selected;

      public ItemsListComponent() {
         super(
            SlotCompound.fromGrid(10, 3, 7),
            new PagingData(PaginationSettings.ItemsAlign.LEFT, SlotCompound.from(1, 3), SlotCompound.from(9, 3)),
            EditorCrateRewardItemsLayout.this.reward.getWinItems()
         );
         this.setOnItemRender(
            (context, index, item, customItem) -> {
               ItemStack itemStack = customItem.build();
               item.withItem(itemStack)
                  .withLore(
                     LoreBuilder.builder(EditorCrateRewardItemsLayout.this)
                        .description(ItemStackUtil.extractRawLore(itemStack))
                        .actions(
                           ClickAction.REMOVE(ClickAction.ClickType.LEFT, Translatable.key("labels.item")),
                           ClickAction.RECEIVE(ClickAction.ClickType.RIGHT, Translatable.key("labels.item"))
                        )
                        .build()
                  );
            }
         );
         this.setOnItemClick((context, index, customItem) -> {
            if (context.isRightClick()) {
               context.getViewer().getInventory().addItem(new ItemStack[]{customItem.build().clone()});
            } else if (context.isLeftClick()) {
               EditorCrateRewardItemsLayout.this.reward.getWinItems().remove(customItem);
               EditorCrateRewardItemsLayout.this.save(context.getContainerView());
               context.getViewer().getInventory().addItem(new ItemStack[]{customItem.build().clone()});
               context.getContainerView().updateView();
            }
         });
         this.setAddItemSlot(
            VirtualItem.from(XMaterial.LIME_STAINED_GLASS_PANE)
               .withDisplayName(EditorCrateRewardItemsLayout.this.t("editor.crate.reward.win-items.add-item.title")[0])
               .withLore(EditorCrateRewardItemsLayout.this.t("editor.crate.reward.win-items.add-item.description"))
         );
      }

      @Override
      public void onRender(RenderViewContext context) {
         if (this.selected) {
            this.getSlot().forEach(slot -> VirtualItem.from(XMaterial.GREEN_STAINED_GLASS_PANE).withSlot(slot).withDisplayName("\u00a7a").render(context));
         } else {
            super.onRender(context);
         }
      }

      @Override
      public void onClick(ClickViewContext context) {
         context.setCancelled(true);
         super.onClick(context);
         if (context.getCursor() != null && context.getCursor().getType() != Material.AIR) {
            if (this.selected) {
               EditorCrateRewardItemsLayout.this.reward.getWinItems().add(BaseItem.from(context.getCursor().clone()));
               if (!EditorCrateRewardItemsLayout.this.getReward().isDisplayValid()) {
                  Translations.send(context.getViewer(), "\u00a77Added item as display item as well.");
                  EditorCrateRewardItemsLayout.this.reward.setDisplayItem(BaseItem.from(context.getCursor().clone()));
               }

               context.setCursor(new ItemStack(Material.AIR));
               this.selected = false;
               context.getContainerView().updateView();
            }
         } else {
            this.selected = false;
         }
      }

      @Generated
      public boolean isSelected() {
         return this.selected;
      }

      @Generated
      public void setSelected(boolean selected) {
         this.selected = selected;
      }
   }
}
