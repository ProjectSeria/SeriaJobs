package id.seria.gacha.api.crate;

import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public interface CrateBlockType {
   Material getBukkitMaterial();

   Object getData();

   boolean isDataBase64();

   boolean isDataByte();
}
