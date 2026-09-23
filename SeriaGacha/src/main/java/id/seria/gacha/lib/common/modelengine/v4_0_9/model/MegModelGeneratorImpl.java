package id.seria.gacha.lib.common.modelengine.v4_0_9.model;

import id.seria.gacha.lib.common.modelengine.interfaces.model.MegModelGenerator;
import com.ticxo.modelengine.api.generator.ModelGenerator;
import java.util.Objects;

public class MegModelGeneratorImpl implements MegModelGenerator {
   private final ModelGenerator generator;

   public MegModelGeneratorImpl(ModelGenerator generator) {
      this.generator = Objects.requireNonNull(generator);
   }

   @Override
   public boolean isInitialized() {
      return this.generator.isInitialized();
   }
}
