package id.seria.gacha.facades;

import id.seria.gacha.api.events.items.ItemDeliveryEvent;
import id.seria.gacha.api.events.items.ItemDeliverySource;
import id.seria.gacha.managers.players.PlayerData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ItemDeliveryFacade {
   private ItemDeliveryFacade() {
   }

   public static ItemDeliveryFacade.DeliveryResult deliver(PlayerData playerData, Player player, Collection<ItemStack> items, ItemDeliverySource source) {
      List<ItemStack> normalizedItems = normalize(items);
      if (normalizedItems.isEmpty()) {
         return ItemDeliveryFacade.DeliveryResult.empty();
      }

      ItemDeliveryEvent event = new ItemDeliveryEvent(playerData, player, source, normalizedItems);
      Bukkit.getPluginManager().callEvent(event);
      List<ItemStack> postEventItems = normalize(event.getRemainingItems());
      if (postEventItems.isEmpty()) {
         return new ItemDeliveryFacade.DeliveryResult(0, 0);
      }

      if (player == null) {
         return new ItemDeliveryFacade.DeliveryResult(countItems(postEventItems), 0);
      }

      Map<Integer, ItemStack> left = player.getInventory().addItem(postEventItems.toArray(new ItemStack[0]));
      if (left.isEmpty()) {
         return new ItemDeliveryFacade.DeliveryResult(0, 0);
      }

      int dropped = 0;

      for (ItemStack item : left.values()) {
         if (item != null && item.getAmount() > 0) {
            dropped += item.getAmount();
            player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
         }
      }

      return new ItemDeliveryFacade.DeliveryResult(0, dropped);
   }

   private static int countItems(Collection<ItemStack> items) {
      int amount = 0;

      for (ItemStack item : items) {
         if (item != null && item.getAmount() > 0) {
            amount += item.getAmount();
         }
      }

      return amount;
   }

   private static List<ItemStack> normalize(Collection<ItemStack> items) {
      if (items != null && !items.isEmpty()) {
         List<ItemStack> out = new ArrayList<>();

         for (ItemStack item : items) {
            if (item != null && item.getType() != null && item.getAmount() > 0) {
               out.add(item.clone());
            }
         }

         return out;
      } else {
         return Collections.emptyList();
      }
   }

   public static final class DeliveryResult {
      private static final ItemDeliveryFacade.DeliveryResult EMPTY = new ItemDeliveryFacade.DeliveryResult(0, 0);
      private final int unresolvedAmount;
      private final int droppedAmount;

      public DeliveryResult(int unresolvedAmount, int droppedAmount) {
         this.unresolvedAmount = unresolvedAmount;
         this.droppedAmount = droppedAmount;
      }

      public boolean hasDroppedItems() {
         return this.droppedAmount > 0;
      }

      public boolean hasUnresolvedItems() {
         return this.unresolvedAmount > 0;
      }

      public static ItemDeliveryFacade.DeliveryResult empty() {
         return EMPTY;
      }

      @Generated
      public int getUnresolvedAmount() {
         return this.unresolvedAmount;
      }

      @Generated
      public int getDroppedAmount() {
         return this.droppedAmount;
      }
   }
}
