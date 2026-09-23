package id.seria.gacha.sdk.hooks.itemsadder;

import id.seria.gacha.sdk.platforms.server.hooks.NamespacedItemHook;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.ItemsAdder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemsAdderProvider extends ServerHook implements NamespacedItemHook {
   protected ItemsAdderProvider() {
   }

   @Override
   public void init() {
   }

   @Override
   public void load() {
   }

   @Override
   public void unload() {
   }

   @Override
   public void reload() {
      this.unload();
      this.load();
   }

   @Override
   public String getNamespace() {
      return "itemsadder";
   }

   @Nullable
   @Override
   public String getIdFromItem(ItemStack item) {
      CustomStack stack = CustomStack.byItemStack(item);
      return stack == null ? null : stack.getNamespacedID();
   }

   @Nullable
   @Override
   public ItemStack getItemFromId(String id) {
      return ItemsAdder.getCustomItem(id);
   }
}
