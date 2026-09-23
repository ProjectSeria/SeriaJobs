package id.seria.gacha.lib.common.nms.interfaces.particles.data;

public final class ShriekData extends ParticleData {
   private final int delay;

   public ShriekData(int delay) {
      this.delay = delay;
   }

   public int getDelay() {
      return this.delay;
   }
}
