package id.seria.gacha.managers.crates.animations.opening.phases.second;

import com.google.common.collect.Maps;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.managers.crates.type.CrateBlockType;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CrateSkullFetcher {
   private static final ItemStack DEFAULT_ITEM = Utilities.getNMSFactory()
      .createTexturedSkullFromUrl("http://textures.minecraft.net/texture/6b0255b686edff78ff87326441ed8af8ada153821d83f0158e5735c6b839daf8");
   private static final Map<Material, ItemStack> SKULLS_ITEM = Maps.newHashMap();

   public CrateSkullFetcher() {
   }

   public static ItemStack fetchSkull(CrateBlockType blockType) {
      ItemStack skull = SKULLS_ITEM.get(blockType.getBukkitMaterial());
      if (skull == null) {
         return blockType.isDataBase64() ? Utilities.getNMSFactory().createTexturedSkullFromBase64(blockType.getTexture()) : DEFAULT_ITEM;
      } else {
         return skull;
      }
   }

   static {
      String path = "http://textures.minecraft.net/texture/";
      SKULLS_ITEM.put(
         Material.CHEST,
         Utilities.getNMSFactory()
            .createTexturedSkullFromUrl("http://textures.minecraft.net/texture/d5c6dc2bbf51c36cfc7714585a6a5683ef2b14d47d8ff714654a893f5da622")
      );
      SKULLS_ITEM.put(
         Material.ENDER_CHEST,
         Utilities.getNMSFactory()
            .createTexturedSkullFromUrl("http://textures.minecraft.net/texture/a6cc486c2be1cb9dfcb2e53dd9a3e9a883bfadb27cb956f1896d602b4067")
      );
   }
}
