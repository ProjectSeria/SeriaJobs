package id.seria.gacha.lib.common.nms.interfaces.particles.data;

public final class SculkChargeData extends ParticleData {
   private final float roll;

   public SculkChargeData(float roll) {
      this.roll = roll;
   }

   public float getRoll() {
      return this.roll;
   }
}
