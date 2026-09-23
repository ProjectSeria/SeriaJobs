package id.seria.gacha.lib.common.modelengine.interfaces.model;

import id.seria.gacha.lib.common.modelengine.interfaces.animation.MegAnimationHandler;
import org.bukkit.entity.Player;

public interface MegActiveModel {
   MegModelBlueprint getBlueprint();

   MegAnimationHandler getAnimationHandler();

   void setHitboxVisible(boolean var1);

   default void setScale(double scale) {
   }

   default void setGlow(boolean glowing, int rgb) {
   }

   void showToPlayer(Player var1);

   void hideFromPlayer(Player var1);

   void destroy();
}
