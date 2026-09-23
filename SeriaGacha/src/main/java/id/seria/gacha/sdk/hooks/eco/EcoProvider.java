package id.seria.gacha.sdk.hooks.eco;

import id.seria.gacha.sdk.platforms.server.hooks.NamespacedItemHook;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import com.willfp.eco.core.items.CustomItem;
import com.willfp.eco.core.items.Items;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EcoProvider extends ServerHook implements NamespacedItemHook {
   protected EcoProvider() {
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
      return "eco";
   }

   @Nullable
   @Override
   public String getIdFromItem(ItemStack itemStack) {
      CustomItem customItem = Items.getCustomItem(itemStack);
      return customItem == null ? null : customItem.getKey().getKey();
   }

   @Nullable
   @Override
   public ItemStack getItemFromId(String id) {
      return Items.lookup(id).getItem();
   }
}
