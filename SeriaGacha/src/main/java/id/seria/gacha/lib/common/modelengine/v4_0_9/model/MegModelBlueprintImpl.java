package id.seria.gacha.lib.common.modelengine.v4_0_9.model;

import id.seria.gacha.lib.common.modelengine.interfaces.animation.MegBlueprintAnimation;
import id.seria.gacha.lib.common.modelengine.interfaces.model.MegModelBlueprint;
import id.seria.gacha.lib.common.modelengine.v4_0_9.animation.MegBlueprintAnimationImpl;
import id.seria.gacha.lib.common.modelengine.v4_0_9.entity.MegHitboxImpl;
import com.ticxo.modelengine.api.animation.BlueprintAnimation;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import lombok.Generated;

public class MegModelBlueprintImpl implements MegModelBlueprint {
   private final ModelBlueprint object;

   protected MegModelBlueprintImpl(ModelBlueprint object) {
      this.object = Objects.requireNonNull(object);
   }

   @Override
   public Map<String, MegBlueprintAnimation> getAnimations() {
      try {
         Map<String, MegBlueprintAnimation> rawMap = new HashMap<>();

         for (Entry<String, BlueprintAnimation> entry : this.object.getAnimations().entrySet()) {
            rawMap.put(entry.getKey().toString(), new MegBlueprintAnimationImpl(entry.getValue()));
         }

         return rawMap;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   public MegHitboxImpl getMainHitbox() {
      return this.object.getMainHitbox() == null ? null : new MegHitboxImpl(this.object.getMainHitbox());
   }

   @Generated
   public ModelBlueprint getObject() {
      return this.object;
   }
}
