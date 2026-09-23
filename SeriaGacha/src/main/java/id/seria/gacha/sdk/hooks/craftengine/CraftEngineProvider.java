package id.seria.gacha.sdk.hooks.craftengine;

import id.seria.gacha.sdk.platforms.server.hooks.NamespacedItemHook;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CraftEngineProvider extends ServerHook implements NamespacedItemHook {
   protected CraftEngineProvider() {
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
      return "craftengine";
   }

   @Nullable
   @Override
   public String getIdFromItem(ItemStack itemStack) {
      Key key = CraftEngineItems.getCustomItemId(itemStack);
      return key == null ? null : key.toString();
   }

   @Nullable
   @Override
   public ItemStack getItemFromId(String id) {
      BukkitItemDefinition item = CraftEngineItems.byId(id);
      return item == null ? null : item.buildBukkitItem();
   }
}
