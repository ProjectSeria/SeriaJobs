package id.seria.gacha.lib.common.uicomponents.newest.container;

import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.CloseViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.OpenViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractContainer {
   protected final ServerPlugin plugin;
   protected final Map<UUID, ContainerView> viewersContext = new ConcurrentHashMap<>();
   @Deprecated
   protected boolean skipNextOnCloseEvent;

   public AbstractContainer(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   public abstract void onInitialize(ContainerView var1);

   public abstract void onOpen(OpenViewContext var1);

   public abstract void onRender(RenderViewContext var1);

   public void onRenderSlot(RenderViewContext context, VirtualItem item) {
   }

   public void onClick(ClickViewContext context) {
   }

   public void onClose(CloseViewContext context) {
   }

   public void open(Player viewer) {
      this.open(new ContainerView(this, viewer));
   }

   public void open(ContainerView view) {
      Exception originalException = new Exception();
      view.setClosed(false);
      Utilities.getAsyncService().execute(() -> {
         try {
            if (!Utilities.isPlayerOnline(view.getViewer())) {
               return;
            }

            view.getContainer().getViewersContext().remove(view.getViewer().getUniqueId());
            view.setContainer(this);
            Map<String, Object> oldMetadata = new HashMap<>(view.getMetadata());
            Set<String> oldPersistentKeys = new HashSet<>(view.getPersistentMetadataKeys());
            this.clearNonPersistentMetadata(view);
            this.onInitialize(view);
            OpenViewContext openContext = new OpenViewContext(this, view);
            this.onOpen(openContext);
            if (openContext.isCancelled()) {
               for (Entry<String, Object> entry : oldMetadata.entrySet()) {
                  view.setMetadata(entry.getKey(), entry.getValue(), oldPersistentKeys.contains(entry.getKey()));
               }

               return;
            }

            view.setContainerType(openContext.getContainerType());
            view.setContainerTitle(openContext.getContainerTitle());
            RenderViewContext renderContext = new RenderViewContext(this, view);
            Inventory inventory = renderContext.prepareInventory(view);
            this.onRender(renderContext);

            for (int i = 0; i < renderContext.getSlots().length; i++) {
               if (renderContext.getSlots()[i] != null) {
                  VirtualItem item = new VirtualItem().withSlot(i).withItem(renderContext.getSlots()[i]);
                  this.onRenderSlot(renderContext, item);
                  item.render(inventory);
               }
            }

            view.setInventory(inventory);
            view.setConsumers(renderContext.getConsumers());
            this.viewersContext.put(view.getViewer().getUniqueId(), view);
            this.plugin.getScheduler().runSync(view.getViewer(), () -> {
               if (view.isClosed()) {
                  this.viewersContext.remove(view.getViewer().getUniqueId());
               } else {
                  view.getViewer().openInventory(inventory);
                  this.viewersContext.put(view.getViewer().getUniqueId(), view);
               }
            });
         } catch (Exception e) {
            e.initCause(originalException);
            this.plugin.getLogger().error("Error opening container", e);
         }
      });
   }

   public void updateViews() {
      for (ContainerView view : new ArrayList<>(this.viewersContext.values())) {
         this.updateView(view);
      }
   }

   public void updateView(Player viewer) {
      ContainerView view = this.viewersContext.get(viewer.getUniqueId());
      if (view != null) {
         this.updateView(view);
      }
   }

   public void updateView(ContainerView view) {
      if (!Utilities.isPlayerOnline(view.getViewer())) {
         this.viewersContext.remove(view.getViewer().getUniqueId());
      } else {
         Player viewer = view.getViewer();
         Inventory topInv = viewer.getOpenInventory().getTopInventory();
         if (topInv.getHolder() instanceof ContainerView && topInv.getHolder().equals(view)) {
            this.clearNonPersistentMetadata(view);
            this.viewersContext.remove(view.getViewer().getUniqueId());
            this.onInitialize(view);
            RenderViewContext renderContext = new RenderViewContext(this, view);
            this.onRender(renderContext);

            for (int i = 0; i < renderContext.getSlots().length; i++) {
               if (renderContext.getSlots()[i] == null) {
                  view.getInventory().setItem(i, new ItemStack(Material.AIR));
               } else {
                  VirtualItem item = new VirtualItem().withSlot(i).withItem(renderContext.getSlots()[i]);
                  this.onRenderSlot(renderContext, item);
                  item.render(view.getInventory());
               }
            }

            this.viewersContext.put(view.getViewer().getUniqueId(), view);
            view.setConsumers(renderContext.getConsumers());
            view.getViewer().updateInventory();
         } else {
            this.viewersContext.remove(viewer.getUniqueId());
         }
      }
   }

   protected void clearNonPersistentMetadata(ContainerView view) {
      for (String key : new HashSet<>(view.getMetadata().keySet())) {
         if (!view.getPersistentMetadataKeys().contains(key)) {
            view.removeMetadata(key);
         }
      }
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public Map<UUID, ContainerView> getViewersContext() {
      return this.viewersContext;
   }

   @Deprecated
   @Generated
   public boolean isSkipNextOnCloseEvent() {
      return this.skipNextOnCloseEvent;
   }

   @Deprecated
   @Generated
   public void setSkipNextOnCloseEvent(boolean skipNextOnCloseEvent) {
      this.skipNextOnCloseEvent = skipNextOnCloseEvent;
   }
}
