package id.seria.gacha.lib.common.nms.interfaces.particles.data.color;

import java.awt.Color;
import lombok.Generated;

public final class DustColorTransitionData extends DustData {
   private final int fadeRed;
   private final int fadeGreen;
   private final int fadeBlue;

   public DustColorTransitionData(Color color, Color fadeColor, float size) {
      super(color, size);
      this.fadeRed = fadeColor.getRed();
      this.fadeGreen = fadeColor.getGreen();
      this.fadeBlue = fadeColor.getBlue();
   }

   public DustColorTransitionData(int red, int green, int blue, int fadeRed, int fadeGreen, int fadeBlue, float size) {
      super(red, green, blue, size);
      this.fadeRed = fadeRed;
      this.fadeGreen = fadeGreen;
      this.fadeBlue = fadeBlue;
   }

   @Generated
   public int getFadeRed() {
      return this.fadeRed;
   }

   @Generated
   public int getFadeGreen() {
      return this.fadeGreen;
   }

   @Generated
   public int getFadeBlue() {
      return this.fadeBlue;
   }
}
