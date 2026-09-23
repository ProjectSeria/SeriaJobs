package id.seria.gacha.sdk.hooks.headdatabase;

import id.seria.gacha.sdk.platforms.server.hooks.NamespacedItemHook;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import lombok.NonNull;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class HeadDatabaseProvider extends ServerHook implements NamespacedItemHook {
   protected HeadDatabaseProvider() {
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
      return "hdb";
   }

   @Nullable
   @Override
   public String getIdFromItem(ItemStack itemStack) {
      return new HeadDatabaseAPI().getItemID(itemStack);
   }

   @Nullable
   @Override
   public ItemStack getItemFromId(@NonNull String id) {
      if (id == null) {
         throw new NullPointerException("id is marked non-null but is null");
      } else {
         return new HeadDatabaseAPI().getItemHead(id);
      }
   }
}
