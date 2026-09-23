package id.seria.gacha.lib.common.uicomponents.newest.container.context;

import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class CloseViewContext {
   private final Player viewer;
   private final ContainerView holder;
   private final InventoryCloseEvent event;

   public Inventory getInventory() {
      return this.event.getInventory();
   }

   @Generated
   public Player getViewer() {
      return this.viewer;
   }

   @Generated
   public ContainerView getHolder() {
      return this.holder;
   }

   @Generated
   public InventoryCloseEvent getEvent() {
      return this.event;
   }

   @Generated
   public CloseViewContext(Player viewer, ContainerView holder, InventoryCloseEvent event) {
      this.viewer = viewer;
      this.holder = holder;
      this.event = event;
   }
}
