package id.seria.gacha.lib.xseries.art;

import org.bukkit.Art;

public class XArt {
   private static final boolean USE_INTERFACE = Art.class.isInterface();

   private XArt() {
   }

   public static BukkitArt of(Art art) {
      return USE_INTERFACE ? new NewArt(art) : new OldArt(art);
   }
}
