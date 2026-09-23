package id.seria.gacha.managers.crates.engine.vanilla_block;

import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.managers.crates.engine.EngineData;
import id.seria.gacha.managers.crates.engine.EngineType;
import lombok.Generated;

public class VanillaBlockEngineData extends EngineData implements Cloneable {
   @ConfigField
   private boolean blockOpeningAnimationEnabled = true;

   @Override
   public EngineType getType() {
      return EngineType.valueOf("VANILLA_BLOCK");
   }

   @Override
   public EngineData clone() {
      return new VanillaBlockEngineData(this.blockOpeningAnimationEnabled);
   }

   @Generated
   public boolean isBlockOpeningAnimationEnabled() {
      return this.blockOpeningAnimationEnabled;
   }

   @Generated
   public void setBlockOpeningAnimationEnabled(boolean blockOpeningAnimationEnabled) {
      this.blockOpeningAnimationEnabled = blockOpeningAnimationEnabled;
   }

   @Generated
   public VanillaBlockEngineData() {
   }

   @Generated
   public VanillaBlockEngineData(boolean blockOpeningAnimationEnabled) {
      this.blockOpeningAnimationEnabled = blockOpeningAnimationEnabled;
   }
}
