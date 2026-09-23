package id.seria.gacha.api.events.items;

import id.seria.gacha.api.player.PlayerData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemDeliveryEvent extends Event {
   private static final HandlerList HANDLER_LIST = new HandlerList();
   private final PlayerData playerData;
   private final Player player;
   private final ItemDeliverySource source;
   private final List<ItemStack> remainingItems;

   public ItemDeliveryEvent(@NotNull PlayerData playerData, Player player, ItemDeliverySource source, Collection<ItemStack> items) {
      this.playerData = Objects.requireNonNull(playerData, "playerData");
      this.player = player;
      this.source = source;
      this.remainingItems = new ArrayList<>(normalize(items));
   }

   @NotNull
   public PlayerData getPlayerData() {
      return this.playerData;
   }

   public Player getPlayer() {
      return this.player;
   }

   public ItemDeliverySource getSource() {
      return this.source;
   }

   public List<ItemStack> getRemainingItems() {
      return this.remainingItems;
   }

   public List<ItemStack> getRemainingItemsSnapshot() {
      return Collections.unmodifiableList(new ArrayList<>(this.remainingItems));
   }

   public void setRemainingItems(Collection<ItemStack> items) {
      this.remainingItems.clear();
      this.remainingItems.addAll(normalize(items));
   }

   public void consumeAll() {
      this.remainingItems.clear();
   }

   public HandlerList getHandlers() {
      return HANDLER_LIST;
   }

   public static HandlerList getHandlerList() {
      return HANDLER_LIST;
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
}
