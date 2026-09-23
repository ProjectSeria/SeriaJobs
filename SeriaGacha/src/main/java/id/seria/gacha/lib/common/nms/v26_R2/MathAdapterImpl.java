package id.seria.gacha.lib.common.nms.v26_R2;

import id.seria.gacha.lib.common.nms.interfaces.MathAdapter;
import net.minecraft.util.Mth;

public class MathAdapterImpl implements MathAdapter {
   public MathAdapterImpl() {
   }

   @Override
   public double sin(double radians) {
      return Mth.sin((float)radians);
   }

   @Override
   public double cos(double radians) {
      return Mth.cos((float)radians);
   }
}
