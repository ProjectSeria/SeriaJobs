package id.seria.gacha.lib.common.modelengine.interfaces.animation;

public interface MegAnimationHandler {
   MegAnimationProperty getAnimation(String var1);

   void playAnimation(MegAnimationProperty var1, boolean var2);

   void playAnimation(String var1, double var2, double var4, double var6, boolean var8);

   void stopAnimation(String var1);

   void forceStopAllAnimations();
}
