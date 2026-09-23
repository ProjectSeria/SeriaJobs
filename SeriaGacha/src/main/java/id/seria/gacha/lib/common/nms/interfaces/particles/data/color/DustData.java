package id.seria.gacha.lib.common.nms.interfaces.particles.data.color;

import java.awt.Color;

public class DustData extends RegularColor {
   private final float size;

   public DustData(Color color, float size) {
      super(color);
      this.size = size;
   }

   public DustData(int red, int green, int blue, float size) {
      super(red, green, blue);
      this.size = size;
   }

   public float getSize() {
      return this.size;
   }
}
