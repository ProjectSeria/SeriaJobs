package id.seria.gacha.lib.xseries.art;

import id.seria.gacha.lib.xseries.AbstractReferencedClass;
import org.bukkit.Art;

public abstract class BukkitArt extends AbstractReferencedClass<Art> {
   public BukkitArt() {
   }

   public abstract int getBlockWidth();

   public abstract int getBlockHeight();

   public abstract String getKey();

   public abstract int getId();
}
