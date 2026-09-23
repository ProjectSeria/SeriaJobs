package id.seria.gacha.lib.common.nms.interfaces.particles.data.color;

import id.seria.gacha.lib.common.nms.interfaces.particles.data.ParticleData;

public abstract class ParticleColor extends ParticleData {
   private final int red;
   private final int green;
   private final int blue;

   ParticleColor(int red, int green, int blue) {
      this.red = red;
      this.green = green;
      this.blue = blue;
   }

   public float getRed() {
      return this.red;
   }

   public float getGreen() {
      return this.green;
   }

   public float getBlue() {
      return this.blue;
   }
}
