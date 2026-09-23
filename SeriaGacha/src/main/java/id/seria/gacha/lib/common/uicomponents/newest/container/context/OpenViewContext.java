package id.seria.gacha.lib.common.uicomponents.newest.container.context;

import id.seria.gacha.lib.common.uicomponents.newest.container.AbstractContainer;
import id.seria.gacha.lib.common.uicomponents.newest.container.ContainerType;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import lombok.Generated;
import org.bukkit.entity.Player;

public class OpenViewContext {
   private final AbstractContainer container;
   private final ContainerView containerView;
   private String containerTitle = "Default Container Title";
   private ContainerType containerType = ContainerType.CONTAINER_9X6;
   private boolean cancelled;

   public Player getViewer() {
      return this.containerView.getViewer();
   }

   @Generated
   public void setContainerTitle(String containerTitle) {
      this.containerTitle = containerTitle;
   }

   @Generated
   public void setContainerType(ContainerType containerType) {
      this.containerType = containerType;
   }

   @Generated
   public AbstractContainer getContainer() {
      return this.container;
   }

   @Generated
   public ContainerView getContainerView() {
      return this.containerView;
   }

   @Generated
   public String getContainerTitle() {
      return this.containerTitle;
   }

   @Generated
   public ContainerType getContainerType() {
      return this.containerType;
   }

   @Generated
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Generated
   public OpenViewContext(AbstractContainer container, ContainerView containerView) {
      this.container = container;
      this.containerView = containerView;
   }

   @Generated
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}
