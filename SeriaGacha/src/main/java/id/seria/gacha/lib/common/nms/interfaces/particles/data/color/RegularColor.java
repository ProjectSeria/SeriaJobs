package id.seria.gacha.lib.common.nms.interfaces.particles.data.color;

import id.seria.gacha.lib.common.nms.interfaces.particles.utils.MathUtils;
import java.awt.Color;

public class RegularColor extends ParticleColor {
   public RegularColor(Color color) {
      super(color.getRed(), color.getGreen(), color.getBlue());
   }

   public RegularColor(int red, int green, int blue) {
      super(MathUtils.getMaxOrMin(red, 255, 0), MathUtils.getMaxOrMin(green, 255, 0), MathUtils.getMaxOrMin(blue, 255, 0));
   }

   public static RegularColor random() {
      return random(true);
   }

   public static RegularColor random(boolean highSaturation) {
      return highSaturation
         ? fromHSVHue(MathUtils.generateRandomInteger(0, 360))
         : new RegularColor(new Color(MathUtils.RANDOM.nextInt(256), MathUtils.RANDOM.nextInt(256), MathUtils.RANDOM.nextInt(256)));
   }

   public static RegularColor fromHSVHue(int hue) {
      return new RegularColor(new Color(Color.HSBtoRGB(hue / 360.0F, 1.0F, 1.0F)));
   }
}
