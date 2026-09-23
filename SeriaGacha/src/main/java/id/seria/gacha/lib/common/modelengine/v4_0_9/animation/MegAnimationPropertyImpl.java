package id.seria.gacha.lib.common.modelengine.v4_0_9.animation;

import id.seria.gacha.lib.common.modelengine.interfaces.animation.MegAnimationProperty;
import id.seria.gacha.lib.common.modelengine.interfaces.enums.MegLoopMode;
import com.ticxo.modelengine.api.animation.BlueprintAnimation.LoopMode;
import com.ticxo.modelengine.api.animation.BlueprintAnimation.OverrideMode;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.animation.property.SimpleProperty;
import java.util.Objects;
import lombok.Generated;

public class MegAnimationPropertyImpl implements MegAnimationProperty {
   private final IAnimationProperty object;

   public MegAnimationPropertyImpl(IAnimationProperty object) {
      this.object = Objects.requireNonNull(object);
   }

   @Override
   public String getName() {
      return this.object.getName();
   }

   @Override
   public void setForceLoopMode(MegLoopMode loopMode) {
      this.object.setForceLoopMode(LoopMode.valueOf(loopMode.name()));
   }

   @Override
   public void setForceOverride(boolean forceOverride) {
      this.object.setForceOverride(forceOverride ? OverrideMode.OVERRIDE : OverrideMode.NONE);
   }

   @Override
   public boolean isFinished() {
      return this.object.isFinished();
   }

   @Override
   public double getTime() {
      return this.object.getTime();
   }

   public MegBlueprintAnimationImpl getBlueprintAnimation() {
      if (!(this.object instanceof SimpleProperty)) {
         throw new IllegalStateException("Only SimpleProperty objects have access to this method, founded " + this.object.getClass().getSimpleName());
      } else {
         return this.object.getBlueprintAnimation() == null ? null : new MegBlueprintAnimationImpl(this.object.getBlueprintAnimation());
      }
   }

   @Override
   public boolean isSimpleProperty() {
      return this.object instanceof SimpleProperty;
   }

   @Generated
   public IAnimationProperty getObject() {
      return this.object;
   }
}
