package id.seria.gacha.lib.common.modelengine.v4_0_9;

import id.seria.gacha.lib.common.modelengine.interfaces.MegAPI;
import id.seria.gacha.lib.common.modelengine.interfaces.animation.MegBlueprintAnimation;
import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegDummy;
import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegEntityCompound;
import id.seria.gacha.lib.common.modelengine.interfaces.model.MegActiveModel;
import id.seria.gacha.lib.common.modelengine.interfaces.model.MegModelBlueprint;
import id.seria.gacha.lib.common.modelengine.interfaces.model.MegModelGenerator;
import id.seria.gacha.lib.common.modelengine.v4_0_9.animation.MegAnimationPropertyImpl;
import id.seria.gacha.lib.common.modelengine.v4_0_9.animation.MegBlueprintAnimationImpl;
import id.seria.gacha.lib.common.modelengine.v4_0_9.entity.MegDummyImpl;
import id.seria.gacha.lib.common.modelengine.v4_0_9.entity.MegEntityCompoundImpl;
import id.seria.gacha.lib.common.modelengine.v4_0_9.entity.MegModeledEntityImpl;
import id.seria.gacha.lib.common.modelengine.v4_0_9.model.MegActiveModelImpl;
import id.seria.gacha.lib.common.modelengine.v4_0_9.model.MegModelBlueprintImpl;
import id.seria.gacha.lib.common.modelengine.v4_0_9.model.MegModelGeneratorImpl;
import id.seria.gacha.lib.common.modelengine.v4_0_9.model.MegModelRegistryImpl;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.property.SimpleProperty;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class MegAPIImpl implements MegAPI {
   public MegAPIImpl() {
   }

   public MegModelRegistryImpl getModelRegistry() {
      try {
         return new MegModelRegistryImpl(ModelEngineAPI.getAPI().getModelRegistry());
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public MegModelGenerator getModelGenerator() {
      return new MegModelGeneratorImpl(ModelEngineAPI.getAPI().getModelGenerator());
   }

   @Override
   public MegActiveModel createActiveModel(String modeId) {
      ActiveModel model = ModelEngineAPI.createActiveModel(modeId);
      return model == null ? null : new MegActiveModelImpl(model);
   }

   public MegActiveModelImpl createActiveModel(MegModelBlueprint blueprint) {
      ActiveModel model = ModelEngineAPI.createActiveModel(((MegModelBlueprintImpl)blueprint).getObject());
      return model == null ? null : new MegActiveModelImpl(model);
   }

   public MegModeledEntityImpl createModeledEntity(Entity entity) {
      ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(entity);
      return modeledEntity == null ? null : new MegModeledEntityImpl(modeledEntity);
   }

   public MegModeledEntityImpl createModeledEntity(MegDummy dummy) {
      ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(((MegDummyImpl)dummy).getObject());
      return modeledEntity == null ? null : new MegModeledEntityImpl(modeledEntity);
   }

   public MegAnimationPropertyImpl createAnimationProperty(MegActiveModel activeModel, MegBlueprintAnimation animation) {
      return new MegAnimationPropertyImpl(new SimpleProperty(((MegActiveModelImpl)activeModel).getObject(), ((MegBlueprintAnimationImpl)animation).getObject()));
   }

   public MegAnimationPropertyImpl createAnimationProperty(
      MegActiveModel activeModel, MegBlueprintAnimation animation, double lerpIn, double lerpOut, double speed
   ) {
      return new MegAnimationPropertyImpl(
         new SimpleProperty(((MegActiveModelImpl)activeModel).getObject(), ((MegBlueprintAnimationImpl)animation).getObject(), lerpIn, lerpOut, speed)
      );
   }

   @Override
   public MegEntityCompound createStaticEntity(@NonNull String blueprintId, @NonNull Location rawLocation, Player viewer) {
      if (blueprintId == null) {
         throw new NullPointerException("blueprintId is marked non-null but is null");
      }

      if (rawLocation == null) {
         throw new NullPointerException("rawLocation is marked non-null but is null");
      }

      Location location = rawLocation.clone();
      ActiveModel activeModel = ModelEngineAPI.createActiveModel(blueprintId);
      Dummy dummy = new Dummy();
      dummy.setLocation(location);
      dummy.getBodyRotationController().setYBodyRot(location.getYaw());
      dummy.setYHeadRot(location.getYaw());
      dummy.setXHeadRot(location.getPitch());
      ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(dummy);
      modeledEntity.addModel(activeModel, true);
      if (viewer != null) {
         dummy.setDetectingPlayers(false);
         dummy.setForceViewing(viewer, true);
      }

      return new MegEntityCompoundImpl(new MegActiveModelImpl(activeModel), new MegModeledEntityImpl(modeledEntity));
   }

   public MegDummyImpl createDummy() {
      return new MegDummyImpl(new Dummy());
   }
}
