package id.seria.gacha.api.crate.engine;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public interface EngineType {
   int getOrdinal();

   String getName();

   ItemStack getDisplayItem();
}
