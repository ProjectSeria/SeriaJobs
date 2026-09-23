package id.seria.gacha.lib.common.utils.configuration.objects;

import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import lombok.Generated;

public class Size {
   @ConfigField
   public int width;
   @ConfigField
   public int height;

   @Generated
   public Size(int width, int height) {
      this.width = width;
      this.height = height;
   }
}
