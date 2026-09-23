package id.seria.gacha.lib.common.utils.inventory;

import java.util.Locale;

public enum ArmorTrimMaterial {
   QUARTZ,
   IRON,
   NETHERITE,
   REDSTONE,
   COPPER,
   GOLD,
   EMERALD,
   DIAMOND,
   LAPIS,
   AMETHYST,
   RESIN;

   ArmorTrimMaterial() {
   }

   public String getNamespace() {
      return "minecraft:" + this.name().toLowerCase(Locale.ROOT);
   }
}
