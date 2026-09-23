package id.seria.gacha.lib.xseries.art;

import org.bukkit.Art;

class NewArt extends BukkitArt {
   private final Art art;

   public NewArt(Object art) {
      this.art = (Art)art;
   }

   @Override
   public int getBlockWidth() {
      return this.art.getBlockWidth();
   }

   @Override
   public int getBlockHeight() {
      return this.art.getBlockHeight();
   }

   @Override
   public String getKey() {
      return this.art.getKey().getKey();
   }

   @Override
   public int getId() {
      return this.art.getId();
   }

   public Art object() {
      return this.art;
   }
}
