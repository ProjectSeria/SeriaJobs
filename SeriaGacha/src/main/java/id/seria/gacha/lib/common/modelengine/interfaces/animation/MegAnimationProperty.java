package id.seria.gacha.lib.common.modelengine.interfaces.animation;

import id.seria.gacha.lib.common.modelengine.interfaces.enums.MegLoopMode;

public interface MegAnimationProperty {
   String getName();

   void setForceLoopMode(MegLoopMode var1);

   void setForceOverride(boolean var1);

   boolean isFinished();

   double getTime();

   MegBlueprintAnimation getBlueprintAnimation();

   boolean isSimpleProperty();
}
