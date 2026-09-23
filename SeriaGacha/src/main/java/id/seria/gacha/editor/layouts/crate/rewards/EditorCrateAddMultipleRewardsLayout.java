package id.seria.gacha.editor.layouts.crate.rewards;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.facades.RewardFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.ContainerType;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.CloseViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.OpenViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomReward;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EditorCrateAddMultipleRewardsLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;
   protected final EditorFacade.EditorLayout parent;

   public EditorCrateAddMultipleRewardsLayout(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.multiple-rewards"), null);
      this.parent = parent;
      this.crateType = crateType;
   }

   @Override
   public void onPrepare(ContainerView view) {
      view.setCancelOnContainerClick(false);
      view.setCancelOnOtherInventoryClick(false);
      this.addComponent(
         view,
         this.createComponent(
            SlotCompound.from(4, 6),
            this.createData(XMaterial.GRAY_DYE, Translatable.concatenate("\u00a7c", this.label("cancel")), Translatable.empty()),
            Lists.newArrayList(new ClickAction[]{new ClickAction(ClickAction.ClickType.LEFT, this.label("cancel"))}),
            context -> this.parent.open(context.getViewer())
         )
      );
      this.addComponent(
         view,
         this.createComponent(
            SlotCompound.from(6, 6),
            this.createData(
               XMaterial.LIME_DYE,
               Translatable.concatenate("\u00a7a", Translatable.key("layout.actions.description.add", "%value%", this.label("items"))),
               Translatable.empty()
            ),
            Lists.newArrayList(
               new ClickAction[]{
                  new ClickAction(ClickAction.ClickType.LEFT, Translatable.key("layout.actions.description.add", "%value%", this.label("items")))
               }
            ),
            context -> {
               Inventory inventory = context.getInventory();

               for (ItemStack itemStack : this.getInventoryItems(inventory)) {
                  CustomReward reward = RewardFacade.buildDefault(itemStack);
                  this.crateType.addReward(reward);
               }

               try {
                  Crates.getCratesManager().saveCrateType(this.crateType);
                  this.clearInventoryItems(inventory);
                  this.parent.open(context.getViewer());
               } catch (Exception e) {
                  Translations.error(context.getViewer(), e);
                  context.getViewer().closeInventory();
               }
            }
         )
      );
   }

   @Override
   public void onOpen(OpenViewContext context) {
      context.setContainerTitle(this.t(this.getTitle())[0]);
      context.setContainerType(ContainerType.CONTAINER_9X6);
   }

   @Override
   public void onRender(RenderViewContext context) {
      for (int x = 1; x <= 9; x++) {
         context.setSlot(x, context.getContainerType().getRows(), ItemBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE).setDisplayName("\u00a72"));
      }

      super.onRender(context);
   }

   @Override
   public void onClick(ClickViewContext context) {
      if (context.getClickedInventory().equals(context.getViewer().getOpenInventory().getTopInventory())) {
         int slot = context.getRawSlot();
         context.setCancelled(slot < 0 || slot >= 45);
      }

      super.onClick(context);
   }

   @Override
   public void onClose(CloseViewContext context) {
      Player viewer = context.getViewer();
      Inventory menuInventory = viewer.getOpenInventory().getTopInventory();
      viewer.getInventory().addItem(this.getInventoryItems(menuInventory).toArray(new ItemStack[0]));
   }

   private List<ItemStack> getInventoryItems(Inventory inventory) {
      List<ItemStack> items = Lists.newArrayList();

      for (int i = 0; i < 45; i++) {
         ItemStack item = inventory.getItem(i);
         if (item != null && item.getType() != Material.AIR) {
            items.add(item);
         }
      }

      return items;
   }

   private void clearInventoryItems(Inventory inventory) {
      for (int i = 0; i < 45; i++) {
         inventory.setItem(i, new ItemStack(Material.AIR));
      }
   }
}
