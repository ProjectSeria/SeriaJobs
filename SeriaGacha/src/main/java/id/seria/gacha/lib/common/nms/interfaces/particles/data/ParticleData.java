package id.seria.gacha.lib.common.nms.interfaces.particles.data;

import id.seria.gacha.lib.common.nms.interfaces.particles.Particle;

public abstract class ParticleData {
   private Particle effect;

   public ParticleData() {
   }

   public void setEffect(Particle effect) {
      this.effect = effect;
   }

   public Particle getEffect() {
      return this.effect;
   }
}
