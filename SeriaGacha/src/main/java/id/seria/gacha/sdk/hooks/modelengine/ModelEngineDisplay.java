package id.seria.gacha.sdk.hooks.modelengine;

import com.ticxo.modelengine.api.animation.BlueprintAnimation;
import com.ticxo.modelengine.api.animation.BlueprintAnimation.LoopMode;
import com.ticxo.modelengine.api.animation.BlueprintAnimation.OverrideMode;
import com.ticxo.modelengine.api.animation.handler.AnimationHandler;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.animation.property.SimpleProperty;
import com.ticxo.modelengine.api.model.ActiveModel;
import org.jetbrains.annotations.Nullable;

public final class ModelEngineDisplay {
   private final ActiveModel activeModel;

   ModelEngineDisplay(ActiveModel activeModel) {
      this.activeModel = activeModel;
   }

   public void stopAnimation(@Nullable String animationId) {
      if (animationId != null) {
         this.activeModel.getAnimationHandler().stopAnimation(animationId);
      }
   }

   public void playLoopAnimation(@Nullable String animationId) {
      if (animationId != null && !animationId.equalsIgnoreCase("none")) {
         BlueprintAnimation blueprint = (BlueprintAnimation)this.activeModel.getBlueprint().getAnimations().get(animationId);
         if (blueprint != null) {
            IAnimationProperty property = new SimpleProperty(this.activeModel, blueprint);
            property.setForceLoopMode(LoopMode.LOOP);
            property.setForceOverride(OverrideMode.OVERRIDE);
            this.activeModel.getAnimationHandler().playAnimation(property, true);
         }
      }
   }

   public void playAnimationOnce(@Nullable String animationId) {
      if (animationId != null && !animationId.equalsIgnoreCase("none")) {
         this.activeModel.getAnimationHandler().forceStopAllAnimations();
         this.activeModel.getAnimationHandler().playAnimation(animationId, 0.0, 0.0, 1.0, true);
      }
   }

   public boolean isPlayingAnimation(String animationId) {
      if (animationId != null && !animationId.equalsIgnoreCase("none")) {
         AnimationHandler handler = this.activeModel.getAnimationHandler();
         IAnimationProperty property = handler.getAnimation(animationId);
         return property == null ? false : !property.isFinished();
      } else {
         return false;
      }
   }
}
