package id.seria.gacha.lib.common.uicomponents.newest.container.holders;

import id.seria.gacha.lib.common.uicomponents.newest.container.AbstractContainer;
import id.seria.gacha.lib.common.uicomponents.newest.container.ContainerType;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.events.EventBus;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.Generated;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ContainerView implements InventoryHolder {
   protected AbstractContainer container;
   protected final Player viewer;
   protected ContainerType containerType;
   protected String containerTitle;
   protected Inventory inventory;
   protected Consumer<ClickViewContext>[] consumers = new Consumer[0];
   protected boolean cancelOnContainerClick = true;
   protected boolean cancelOnOtherInventoryClick = true;
   protected volatile boolean closed;
   private Map<String, Object> metadata = new HashMap<>();
   private Set<String> persistentMetadataKeys = new HashSet<>();
   private EventBus<ContainerView> eventBus = new EventBus<>();

   public ContainerView(AbstractContainer container, Player viewer) {
      this.container = container;
      this.viewer = viewer;
   }

   public Object getMetadata(String key) {
      synchronized (this.metadata) {
         return this.metadata.get(key);
      }
   }

   public <T> T getMetadataOrNull(String key, Class<T> clazz) {
      synchronized (this.metadata) {
         Object data = this.metadata.get(key);
         if (data == null) {
            return null;
         }

         Validate.isTrue(clazz.isInstance(data), String.format("Cannot cast %s to %s", data.getClass().getSimpleName(), clazz.getSimpleName()));
         return clazz.cast(data);
      }
   }

   public <T> T computeMetadataIfAbsent(String key, Supplier<T> supplier) {
      synchronized (this.metadata) {
         Object data = this.metadata.get(key);
         if (data == null) {
            T newValue = supplier.get();
            this.setMetadata(key, newValue, true);
            return newValue;
         } else {
            Validate.isTrue(
               supplier.get().getClass().isInstance(data),
               String.format("Cannot cast %s to %s", data.getClass().getSimpleName(), supplier.get().getClass().getSimpleName())
            );
            return (T)data;
         }
      }
   }

   public <T> T getMetadata(String key, Class<T> clazz) {
      synchronized (this.metadata) {
         Object data = this.metadata.get(key);
         Validate.isTrue(data != null, "Data is null");
         Validate.isTrue(clazz.isInstance(data), String.format("Cannot cast %s to %s", data.getClass().getSimpleName(), clazz.getSimpleName()));
         return clazz.cast(data);
      }
   }

   public <T> T getMetadata(String key, T defaultObject) {
      synchronized (this.metadata) {
         Object data = this.metadata.get(key);
         if (data == null && defaultObject == null) {
            return null;
         }

         if (data != null) {
            Validate.isTrue(
               defaultObject.getClass().isInstance(data),
               String.format("Cannot cast %s to %s", data.getClass().getSimpleName(), defaultObject.getClass().getSimpleName())
            );
         }

         return (T)(data == null ? defaultObject : data);
      }
   }

   public void setMetadata(String key, Object value, boolean persistent) {
      synchronized (this.metadata) {
         this.metadata.put(key, value);
         if (persistent) {
            this.persistentMetadataKeys.add(key);
         }
      }
   }

   public Object removeMetadata(String key) {
      synchronized (this.metadata) {
         Object removed = this.metadata.remove(key);
         if (removed != null) {
            this.persistentMetadataKeys.remove(key);
         }

         return removed;
      }
   }

   public boolean containsMetadata(String key) {
      synchronized (this.metadata) {
         return this.metadata.containsKey(key);
      }
   }

   public ServerPlugin getPlugin() {
      return this.container.getPlugin();
   }

   public void updateView() {
      if (this.container.getViewersContext().containsKey(this.viewer.getUniqueId())) {
         this.container.updateView(this);
      } else {
         this.container.open(this);
      }
   }

   @Generated
   public AbstractContainer getContainer() {
      return this.container;
   }

   @Generated
   public Player getViewer() {
      return this.viewer;
   }

   @Generated
   public ContainerType getContainerType() {
      return this.containerType;
   }

   @Generated
   public String getContainerTitle() {
      return this.containerTitle;
   }

   @Generated
   public Inventory getInventory() {
      return this.inventory;
   }

   @Generated
   public Consumer<ClickViewContext>[] getConsumers() {
      return this.consumers;
   }

   @Generated
   public boolean isCancelOnContainerClick() {
      return this.cancelOnContainerClick;
   }

   @Generated
   public boolean isCancelOnOtherInventoryClick() {
      return this.cancelOnOtherInventoryClick;
   }

   @Generated
   public boolean isClosed() {
      return this.closed;
   }

   @Generated
   public Map<String, Object> getMetadata() {
      return this.metadata;
   }

   @Generated
   public Set<String> getPersistentMetadataKeys() {
      return this.persistentMetadataKeys;
   }

   @Generated
   public EventBus<ContainerView> getEventBus() {
      return this.eventBus;
   }

   @Generated
   public void setContainer(AbstractContainer container) {
      this.container = container;
   }

   @Generated
   public void setContainerType(ContainerType containerType) {
      this.containerType = containerType;
   }

   @Generated
   public void setContainerTitle(String containerTitle) {
      this.containerTitle = containerTitle;
   }

   @Generated
   public void setInventory(Inventory inventory) {
      this.inventory = inventory;
   }

   @Generated
   public void setConsumers(Consumer<ClickViewContext>[] consumers) {
      this.consumers = consumers;
   }

   @Generated
   public void setCancelOnContainerClick(boolean cancelOnContainerClick) {
      this.cancelOnContainerClick = cancelOnContainerClick;
   }

   @Generated
   public void setCancelOnOtherInventoryClick(boolean cancelOnOtherInventoryClick) {
      this.cancelOnOtherInventoryClick = cancelOnOtherInventoryClick;
   }

   @Generated
   public void setClosed(boolean closed) {
      this.closed = closed;
   }
}
