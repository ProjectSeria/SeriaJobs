package id.seria.gacha.sdk.hooks.modelengine;

import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ModeledEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class ModelEngineHook extends ServerHook {
   protected ModelEngineHook() {
   }

   @Override
   public void init() {
   }

   @Override
   public void load() {
   }

   @Override
   public void unload() {
   }

   @Override
   public void reload() {
      this.unload();
      this.load();
   }

   public ModelEngineHookProvider getProvider() {
      return (ModelEngineHookProvider)super.getProvider();
   }

   @Nullable
   public ModelEngineDisplay findDisplay(UUID entityUuid, String modelId) {
      ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(entityUuid);
      return modeledEntity == null ? null : modeledEntity.getModel(modelId).map(ModelEngineDisplay::new).orElse(null);
   }

   public List<String> animationKeys(String modelId) {
      ModelBlueprint blueprint = ModelEngineAPI.getBlueprint(modelId);
      return blueprint == null ? Collections.emptyList() : new ArrayList<>(blueprint.getAnimations().keySet());
   }
}
