package id.seria.gacha.managers.crates.animations.idle;

import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.annotations.Serializable;
import lombok.Generated;

@Serializable
public class IdleEffect {
   @ConfigField
   private IdleEffectType type = IdleEffectType.CIRCLE;
   @ConfigField
   private IdleEffectData data = new IdleEffectData();

   public IdleEffect clone() {
      return new IdleEffect(this.type, this.data.clone());
   }

   @Generated
   public IdleEffectType getType() {
      return this.type;
   }

   @Generated
   public IdleEffectData getData() {
      return this.data;
   }

   @Generated
   public void setType(IdleEffectType type) {
      this.type = type;
   }

   @Generated
   public void setData(IdleEffectData data) {
      this.data = data;
   }

   @Generated
   public IdleEffect() {
   }

   @Generated
   public IdleEffect(IdleEffectType type, IdleEffectData data) {
      this.type = type;
      this.data = data;
   }
}
