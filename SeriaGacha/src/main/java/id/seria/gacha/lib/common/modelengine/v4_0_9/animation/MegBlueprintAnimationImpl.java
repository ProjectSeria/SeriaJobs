package id.seria.gacha.lib.common.modelengine.v4_0_9.animation;

import id.seria.gacha.lib.common.modelengine.interfaces.animation.MegBlueprintAnimation;
import id.seria.gacha.lib.common.modelengine.interfaces.enums.MegLoopMode;
import com.ticxo.modelengine.api.animation.BlueprintAnimation;
import java.util.Objects;
import lombok.Generated;

public class MegBlueprintAnimationImpl implements MegBlueprintAnimation {
   private final BlueprintAnimation object;

   public MegBlueprintAnimationImpl(BlueprintAnimation object) {
      this.object = Objects.requireNonNull(object);
   }

   @Override
   public double getLength() {
      return this.object.getLength();
   }

   @Override
   public MegLoopMode getLoopMode() {
      return MegLoopMode.valueOf(this.object.getLoopMode().toString());
   }

   @Generated
   public BlueprintAnimation getObject() {
      return this.object;
   }
}
