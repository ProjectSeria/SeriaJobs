package id.seria.gacha.lib.common.uicomponents.newest.container.context;

import id.seria.gacha.lib.common.uicomponents.newest.container.AbstractContainer;
import id.seria.gacha.lib.common.uicomponents.newest.container.ContainerType;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.InventoryUtil;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import id.seria.gacha.sdk.platforms.server.internal.RichTextConverter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RenderViewContext {
   private final AbstractContainer container;
   private final ContainerView containerView;
   private final ItemStack[] slots;
   private final Consumer<ClickViewContext>[] consumers;
   private boolean cancelled;

   public RenderViewContext(AbstractContainer container, ContainerView containerView) {
      this.container = container;
      this.containerView = containerView;
      int size = containerView.getContainerType().getSize();
      this.slots = new ItemStack[size];
      this.consumers = new Consumer[size + 36];
   }

   public ContainerType getContainerType() {
      return this.containerView.getContainerType();
   }

   public String getContainerTitle() {
      return this.containerView.getContainerTitle();
   }

   public Inventory prepareInventory(ContainerView view) throws InterruptedException, ExecutionException, TimeoutException {
      String t = this.containerView.getContainerTitle();
      String containerTitle = RichTextConverter.toLegacyText(t);
      int size = this.slots.length;
      if (Bukkit.isPrimaryThread()) {
         return Bukkit.createInventory(view, size, containerTitle);
      }

      CompletableFuture<Inventory> future = new CompletableFuture<>();
      this.container.getPlugin().getScheduler().runSync(() -> {
         try {
            Inventory inv = Bukkit.createInventory(view, size, containerTitle);
            future.complete(inv);
         } catch (Exception e) {
            future.completeExceptionally(e);
         }
      });
      return future.get(2L, TimeUnit.SECONDS);
   }

   public void setSlot(int x, int y, ItemBuilder builder, Consumer<ClickViewContext> consumer) {
      this.setSlot(InventoryUtil.getPositionRaw(x, y), builder.build(), consumer);
   }

   public void setSlot(int x, int y, ItemStack item, Consumer<ClickViewContext> consumer) {
      this.setSlot(InventoryUtil.getPositionRaw(x, y), item, consumer);
   }

   public void setSlot(int x, int y, ItemBuilder builder) {
      this.setSlot(InventoryUtil.getPositionRaw(x, y), builder.build(), null);
   }

   public void setSlot(int x, int y, ItemStack item) {
      this.setSlot(InventoryUtil.getPositionRaw(x, y), item, null);
   }

   public void setSlot(int slot, ItemBuilder builder, Consumer<ClickViewContext> consumer) {
      this.setSlot(slot, builder.build(), consumer);
   }

   public void setSlot(int slot, ItemStack item) {
      this.setSlot(slot, item, null);
   }

   public void setSlot(int slot, ItemBuilder builder) {
      this.setSlot(slot, builder, null);
   }

   public void setSlot(int slot, ItemStack item, Consumer<ClickViewContext> consumer) {
      if (slot >= 0 && slot < this.consumers.length && slot < this.slots.length && item != null && !item.getType().equals(Material.AIR)) {
         this.consumers[slot] = consumer;
         this.slots[slot] = item;
      }
   }

   public void fillSlots(ItemBuilder item) {
      this.fillSlots(item.build(), null);
   }

   public void fillSlots(ItemBuilder item, Consumer<ClickViewContext> consumer) {
      this.fillSlots(item.build(), consumer);
   }

   public void fillSlots(ItemStack item) {
      this.fillSlots(item, null);
   }

   public void fillSlots(ItemStack item, Consumer<ClickViewContext> consumer) {
      for (int slot = 0; slot < this.slots.length; slot++) {
         if (this.slots[slot] == null) {
            this.consumers[slot] = consumer;
            this.slots[slot] = item;
         }
      }
   }

   public Player getViewer() {
      return this.containerView.getViewer();
   }

   public ServerPlugin getPlugin() {
      return this.containerView.getPlugin();
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
   public ItemStack[] getSlots() {
      return this.slots;
   }

   @Generated
   public Consumer<ClickViewContext>[] getConsumers() {
      return this.consumers;
   }

   @Generated
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Generated
   public RenderViewContext(
      AbstractContainer container, ContainerView containerView, ItemStack[] slots, Consumer<ClickViewContext>[] consumers, boolean cancelled
   ) {
      this.container = container;
      this.containerView = containerView;
      this.slots = slots;
      this.consumers = consumers;
      this.cancelled = cancelled;
   }
}
