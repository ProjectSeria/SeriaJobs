package id.seria.gacha.lib.common.uicomponents.newest.container.context;

import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ClickViewContext {
   private final Player viewer;
   private final ContainerView containerView;
   private final InventoryClickEvent event;

   public Inventory getInventory() {
      return this.event.getInventory();
   }

   public Inventory getClickedInventory() {
      return this.event.getClickedInventory();
   }

   public SlotType getSlotType() {
      return this.event.getSlotType();
   }

   public ItemStack getCursor() {
      return this.event.getCursor();
   }

   public ItemStack getCurrentItem() {
      return this.event.getCurrentItem();
   }

   public boolean isRightClick() {
      return this.event.isRightClick();
   }

   public boolean isLeftClick() {
      return this.event.isLeftClick();
   }

   public boolean isShiftClick() {
      return this.event.isShiftClick();
   }

   @Deprecated
   public void setCursor(ItemStack stack) {
      this.event.setCursor(stack);
   }

   public void setCurrentItem(ItemStack stack) {
      this.event.setCurrentItem(stack);
   }

   public int getSlot() {
      return this.event.getSlot();
   }

   public int getRawSlot() {
      return this.event.getRawSlot();
   }

   public int getHotbarButton() {
      return this.event.getHotbarButton();
   }

   public InventoryAction getAction() {
      return this.event.getAction();
   }

   public ClickType getClick() {
      return this.event.getClick();
   }

   public void setCancelled(boolean toCancel) {
      this.event.setCancelled(toCancel);
   }

   public boolean isCancelled() {
      return this.event.isCancelled();
   }

   @Generated
   public Player getViewer() {
      return this.viewer;
   }

   @Generated
   public ContainerView getContainerView() {
      return this.containerView;
   }

   @Generated
   public InventoryClickEvent getEvent() {
      return this.event;
   }

   @Generated
   public ClickViewContext(Player viewer, ContainerView containerView, InventoryClickEvent event) {
      this.viewer = viewer;
      this.containerView = containerView;
      this.event = event;
   }
}
