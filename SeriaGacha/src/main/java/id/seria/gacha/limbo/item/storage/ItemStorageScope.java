package id.seria.gacha.limbo.item.storage;

import java.util.Map;
import org.bukkit.inventory.ItemStack;

public interface ItemStorageScope {
   String getStorageScopeId();

   Map<String, ItemStack> getInternalStorageItems();
}
