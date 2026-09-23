package id.seria.gacha.managers.crates.animations.idle;

import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.annotations.Serializable;
import java.awt.Color;
import lombok.Generated;

@Serializable
public class IdleEffectData {
   @ConfigField
   protected ParticleEffect particle = ParticleEffect.REDSTONE;
   @ConfigField
   protected Color color = Color.RED;
   @ConfigField
   protected double offsetX = 0.0;
   @ConfigField
   protected double offsetY = 1.0;
   @ConfigField
   protected double offsetZ = 2.0;
   @ConfigField
   protected double spread = 0.0;
   @ConfigField
   protected double velocity = 0.0;
   @ConfigField
   protected int amount = 1;

   public IdleEffectData clone() {
      return new IdleEffectData(
         this.particle,
         new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.color.getAlpha()),
         this.offsetX,
         this.offsetY,
         this.offsetZ,
         this.spread,
         this.velocity,
         this.amount
      );
   }

   @Generated
   public ParticleEffect getParticle() {
      return this.particle;
   }

   @Generated
   public Color getColor() {
      return this.color;
   }

   @Generated
   public double getOffsetX() {
      return this.offsetX;
   }

   @Generated
   public double getOffsetY() {
      return this.offsetY;
   }

   @Generated
   public double getOffsetZ() {
      return this.offsetZ;
   }

   @Generated
   public double getSpread() {
      return this.spread;
   }

   @Generated
   public double getVelocity() {
      return this.velocity;
   }

   @Generated
   public int getAmount() {
      return this.amount;
   }

   @Generated
   public void setParticle(ParticleEffect particle) {
      this.particle = particle;
   }

   @Generated
   public void setColor(Color color) {
      this.color = color;
   }

   @Generated
   public void setOffsetX(double offsetX) {
      this.offsetX = offsetX;
   }

   @Generated
   public void setOffsetY(double offsetY) {
      this.offsetY = offsetY;
   }

   @Generated
   public void setOffsetZ(double offsetZ) {
      this.offsetZ = offsetZ;
   }

   @Generated
   public void setSpread(double spread) {
      this.spread = spread;
   }

   @Generated
   public void setVelocity(double velocity) {
      this.velocity = velocity;
   }

   @Generated
   public void setAmount(int amount) {
      this.amount = amount;
   }

   @Generated
   public IdleEffectData(ParticleEffect particle, Color color, double offsetX, double offsetY, double offsetZ, double spread, double velocity, int amount) {
      this.particle = particle;
      this.color = color;
      this.offsetX = offsetX;
      this.offsetY = offsetY;
      this.offsetZ = offsetZ;
      this.spread = spread;
      this.velocity = velocity;
      this.amount = amount;
   }

   @Generated
   public IdleEffectData() {
   }
}
