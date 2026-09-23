package id.seria.gacha.lib.common.modelengine.v4_0_9.animation;

import id.seria.gacha.lib.common.modelengine.interfaces.animation.MegAnimationHandler;
import id.seria.gacha.lib.common.modelengine.interfaces.animation.MegAnimationProperty;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import java.util.Objects;

public class MegAnimationHandlerImpl implements MegAnimationHandler {
   private final AnimationHandler object;

   public MegAnimationHandlerImpl(AnimationHandler object) {
      this.object = Objects.requireNonNull(object);
   }

   public MegAnimationPropertyImpl getAnimation(String animation) {
      IAnimationProperty property = this.object.getAnimation(animation);
      return property == null ? null : new MegAnimationPropertyImpl(property);
   }

   @Override
   public void playAnimation(MegAnimationProperty property, boolean force) {
      this.object.playAnimation(((MegAnimationPropertyImpl)property).getObject(), force);
   }

   @Override
   public void playAnimation(String animation, double lerpIn, double lerpOut, double speed, boolean force) {
      this.object.playAnimation(animation, lerpIn, lerpOut, speed, force);
   }

   @Override
   public void stopAnimation(String animation) {
      this.object.stopAnimation(animation);
   }

   @Override
   public void forceStopAllAnimations() {
      this.object.forceStopAllAnimations();
   }
}
