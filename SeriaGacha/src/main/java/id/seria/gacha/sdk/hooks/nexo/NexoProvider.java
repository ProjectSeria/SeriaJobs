package id.seria.gacha.sdk.hooks.nexo;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import id.seria.gacha.sdk.platforms.server.hooks.NamespacedItemHook;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class NexoProvider extends ServerHook implements NamespacedItemHook {
   protected NexoProvider() {
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
      return "nexo";
   }

   @Nullable
   @Override
   public String getIdFromItem(ItemStack itemStack) {
      return NexoItems.idFromItem(itemStack);
   }

   @Nullable
   @Override
   public ItemStack getItemFromId(String id) {
      ItemBuilder itemBuilder = NexoItems.itemFromId(id);
      return itemBuilder == null ? null : itemBuilder.build();
   }
}
