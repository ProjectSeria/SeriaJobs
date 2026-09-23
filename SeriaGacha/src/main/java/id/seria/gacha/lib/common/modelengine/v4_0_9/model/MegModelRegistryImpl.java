package id.seria.gacha.lib.common.modelengine.v4_0_9.model;

import id.seria.gacha.lib.common.modelengine.interfaces.model.MegModelRegistry;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ModelRegistry;
import java.util.Objects;
import java.util.Set;

public class MegModelRegistryImpl implements MegModelRegistry {
   private final ModelRegistry modelRegistry;

   public MegModelRegistryImpl(ModelRegistry modelRegistry) {
      this.modelRegistry = Objects.requireNonNull(modelRegistry);
   }

   @Override
   public Set<String> getAllBlueprintId() {
      return this.modelRegistry.getKeys();
   }

   public MegModelBlueprintImpl getBlueprint(String modelId) {
      ModelBlueprint blueprint = (ModelBlueprint)this.modelRegistry.get(modelId);
      return blueprint == null ? null : new MegModelBlueprintImpl(blueprint);
   }
}
