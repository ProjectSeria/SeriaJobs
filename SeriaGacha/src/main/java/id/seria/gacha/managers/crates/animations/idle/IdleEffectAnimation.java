package id.seria.gacha.managers.crates.animations.idle;

import com.google.common.collect.Lists;
import id.seria.gacha.lib.common.nms.interfaces.particles.PropertyType;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.ParticleData;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.DustColorTransitionData;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.NoteColor;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.color.RegularColor;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.utility.ColorUtil;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.animations.IdleCrateAnimation;
import java.awt.Color;
import java.util.List;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class IdleEffectAnimation {
   protected final IdleCrateAnimation animation;
   protected final Crate crate;
   protected ParticleEffect particle;
   protected Color color;
   protected double offsetX;
   protected double offsetY;
   protected double offsetZ;
   protected double velocity;
   protected double spread;
   protected IdleEffectType type;
   protected int amount;
   protected final Location center;
   protected double steps;

   @Deprecated
   public static IdleEffectAnimation createEffect(IdleCrateAnimation animation, IdleEffect data) throws Exception {
      return data.getType().getTarget().getConstructor(IdleCrateAnimation.class, IdleEffect.class).newInstance(animation, data);
   }

   public IdleEffectAnimation(IdleCrateAnimation animation, IdleEffect effect) {
      this.animation = animation;
      this.crate = this.animation.getCrate();
      this.type = effect.getType();
      this.particle = effect.getData().getParticle();
      this.color = effect.getData().getColor();
      this.offsetX = effect.getData().getOffsetX();
      this.offsetY = effect.getData().getOffsetY();
      this.offsetZ = effect.getData().getOffsetZ();
      this.velocity = effect.getData().getVelocity();
      this.spread = effect.getData().getSpread();
      this.amount = effect.getData().getAmount();
      if (this.amount <= 0) {
         this.amount = 1;
      }

      if (this.amount > 10) {
         this.amount = 10;
      }

      this.steps = (Math.PI * 2) / this.amount;
      this.center = this.crate.getLocation().clone().add(0.5, 0.5, 0.5);
   }

   public void display(Location location) {
      ParticleData particleData = null;
      if (!this.particle.hasProperty(PropertyType.REQUIRES_BLOCK)) {
         if (this.particle == ParticleEffect.NOTE) {
            particleData = new NoteColor(this.color.getRed(), this.color.getGreen(), this.color.getBlue());
         } else if (!this.particle.equals(ParticleEffect.REDSTONE) && this.particle.hasProperty(PropertyType.DUST)) {
            particleData = new DustColorTransitionData(this.color, ColorUtil.darkenColor(this.color, 0.5), 1.0F);
         } else if (this.particle.hasProperty(PropertyType.COLORABLE)) {
            particleData = new RegularColor(this.color);
         }
      }

      List<Player> audience = Lists.newArrayList(this.crate.getNearbyPlayers());
      audience.removeAll(this.animation.getIgnoredPlayers());
      this.particle
         .display(
            location.clone().add(this.offsetX + this.randomSpread(), this.offsetY + this.randomSpread(), this.offsetZ + this.randomSpread()),
            0.0F,
            0.0F,
            0.0F,
            (float)this.velocity,
            0,
            particleData,
            audience
         );
   }

   private double randomSpread() {
      return MathUtil.random(-this.spread, this.spread);
   }

   public abstract void tick();

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
   public void setVelocity(double velocity) {
      this.velocity = velocity;
   }

   @Generated
   public void setSpread(double spread) {
      this.spread = spread;
   }

   @Generated
   public void setType(IdleEffectType type) {
      this.type = type;
   }

   @Generated
   public void setAmount(int amount) {
      this.amount = amount;
   }

   @Generated
   public void setSteps(double steps) {
      this.steps = steps;
   }

   @Generated
   public IdleCrateAnimation getAnimation() {
      return this.animation;
   }

   @Generated
   public Crate getCrate() {
      return this.crate;
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
   public double getVelocity() {
      return this.velocity;
   }

   @Generated
   public double getSpread() {
      return this.spread;
   }

   @Generated
   public IdleEffectType getType() {
      return this.type;
   }

   @Generated
   public int getAmount() {
      return this.amount;
   }

   @Generated
   public Location getCenter() {
      return this.center;
   }

   @Generated
   public double getSteps() {
      return this.steps;
   }
}
