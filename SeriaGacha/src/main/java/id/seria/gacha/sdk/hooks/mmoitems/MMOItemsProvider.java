package id.seria.gacha.sdk.hooks.mmoitems;

import id.seria.gacha.sdk.platforms.server.hooks.NamespacedItemHook;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MMOItemsProvider extends ServerHook implements NamespacedItemHook {
   protected MMOItemsProvider() {
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
      return "mmoitem";
   }

   @Nullable
   @Override
   public String getIdFromItem(ItemStack itemStack) {
      Type customItem = MMOItems.getType(itemStack);
      return customItem == null ? null : customItem.getId();
   }

   @Nullable
   @Override
   public ItemStack getItemFromId(String id) {
      String[] split = id.split(":");
      return split.length != 2 ? null : MMOItems.plugin.getItem(split[0], split[1]);
   }
}
