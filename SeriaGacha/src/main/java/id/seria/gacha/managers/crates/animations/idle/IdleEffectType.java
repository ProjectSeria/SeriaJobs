package id.seria.gacha.managers.crates.animations.idle;

import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.animations.idle.effects.IdleAstroidEffect;
import id.seria.gacha.managers.crates.animations.idle.effects.IdleCircleEffect;
import id.seria.gacha.managers.crates.animations.idle.effects.IdleConicalSpiralEffect;
import id.seria.gacha.managers.crates.animations.idle.effects.IdleDefaultEffect;
import id.seria.gacha.managers.crates.animations.idle.effects.IdleDeltoidEffect;
import id.seria.gacha.managers.crates.animations.idle.effects.IdleDiamondEffect;
import id.seria.gacha.managers.crates.animations.idle.effects.IdleFlowerEffect;
import id.seria.gacha.managers.crates.animations.idle.effects.IdleNinjaStarEffect;
import id.seria.gacha.managers.crates.animations.idle.effects.IdlePulseEffect;
import id.seria.gacha.managers.crates.animations.idle.effects.IdleQuatrefoilEffect;
import id.seria.gacha.managers.crates.animations.idle.effects.IdleSpiralEffect;
import id.seria.gacha.managers.crates.animations.idle.effects.IdleSquareEffect;
import id.seria.gacha.managers.crates.animations.idle.effects.IdleStarEffect;
import lombok.Generated;

public enum IdleEffectType {
   DEFAULT(IdleDefaultEffect.class, "Simple particle spawn at offset", XMaterial.TORCH, false),
   DELTOID(IdleDeltoidEffect.class, "Triangular curved shape", XMaterial.ARROW, true),
   ASTROID(IdleAstroidEffect.class, "Four-cusped hypocycloid", XMaterial.FIREWORK_STAR, true),
   FLOWER(IdleFlowerEffect.class, "Petal-like flower pattern", XMaterial.POPPY, true),
   CIRCLE(IdleCircleEffect.class, "Particles orbit in a flat circle", XMaterial.ENDER_PEARL, false),
   QUATREFOIL(IdleQuatrefoilEffect.class, "Four-lobed clover shape", XMaterial.FERN, true),
   DIAMOND(IdleDiamondEffect.class, "Diamond-shaped outline", XMaterial.DIAMOND, true),
   STAR(IdleStarEffect.class, "Five-pointed star pattern", XMaterial.GLOWSTONE_DUST, false),
   NINJA_STAR(IdleNinjaStarEffect.class, "Spinning shuriken pattern", XMaterial.PRISMARINE_SHARD, true),
   SQUARE(IdleSquareEffect.class, "Square frame around the crate", XMaterial.IRON_BARS, false),
   SPIRAL(IdleSpiralEffect.class, "Ascending helix spiral", XMaterial.NETHER_STAR, true),
   CONICAL_SPIRAL(IdleConicalSpiralEffect.class, "Cone-shaped rising spiral", XMaterial.END_ROD, true),
   PULSE(IdlePulseEffect.class, "Expanding ring pulse", XMaterial.HEART_OF_THE_SEA, true);

   private Class<? extends IdleEffectAnimation> target;
   private String description;
   private XMaterial icon;
   private boolean premium;

   @Generated
   public Class<? extends IdleEffectAnimation> getTarget() {
      return this.target;
   }

   @Generated
   public String getDescription() {
      return this.description;
   }

   @Generated
   public XMaterial getIcon() {
      return this.icon;
   }

   @Generated
   public boolean isPremium() {
      return this.premium;
   }

   @Generated
   IdleEffectType(final Class<? extends IdleEffectAnimation> target, final String description, final XMaterial icon, final boolean premium) {
      this.target = target;
      this.description = description;
      this.icon = icon;
      this.premium = premium;
   }
}
