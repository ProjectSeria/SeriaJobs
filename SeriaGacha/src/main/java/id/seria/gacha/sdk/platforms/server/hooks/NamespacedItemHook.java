package id.seria.gacha.sdk.platforms.server.hooks;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface NamespacedItemHook {
   String getNamespace();

   @Nullable
   ItemStack getItemFromId(String var1);

   @Nullable
   String getIdFromItem(ItemStack var1);
}
