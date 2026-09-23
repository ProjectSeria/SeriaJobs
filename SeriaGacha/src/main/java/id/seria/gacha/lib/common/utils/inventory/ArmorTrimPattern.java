package id.seria.gacha.lib.common.utils.inventory;

import java.util.Locale;

public enum ArmorTrimPattern {
   SENTRY,
   DUNE,
   COAST,
   WILD,
   WARD,
   EYE,
   VEX,
   TIDE,
   SNOUT,
   RIB,
   SPIRE,
   WAYFINDER,
   SHAPER,
   SILENCE,
   RAISER,
   HOST,
   FLOW,
   BOLT;

   ArmorTrimPattern() {
   }

   public String getNamespace() {
      return "minecraft:" + this.name().toLowerCase(Locale.ROOT);
   }
}
