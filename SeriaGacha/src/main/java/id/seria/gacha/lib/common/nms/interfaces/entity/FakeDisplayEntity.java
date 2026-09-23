package id.seria.gacha.lib.common.nms.interfaces.entity;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FakeDisplayEntity extends FakeEntity {
   int getInterpolationDuration();

   void setInterpolationDuration(int var1);

   int getTeleportDuration();

   void setTeleportDuration(int var1);

   float getViewRange();

   void setViewRange(float var1);

   float getShadowRadius();

   void setShadowRadius(float var1);

   float getShadowStrength();

   void setShadowStrength(float var1);

   float getDisplayWidth();

   void setDisplayWidth(float var1);

   float getDisplayHeight();

   void setDisplayHeight(float var1);

   int getInterpolationDelay();

   void setInterpolationDelay(int var1);

   @NotNull
   FakeDisplayEntity.Billboard getBillboard();

   void setBillboard(@NotNull FakeDisplayEntity.Billboard var1);

   @Nullable
   Color getGlowColorOverride();

   void setGlowColorOverride(@Nullable Color var1);

   @Nullable
   FakeDisplayEntity.Brightness getBrightness();

   void setBrightness(@Nullable FakeDisplayEntity.Brightness var1);

   default DisplayTransformation getTransformation() {
      throw new NotImplementedException("Still not implemented on this version");
   }

   default void setTransformation(DisplayTransformation transformation) {
      throw new NotImplementedException("Still not implemented on this version");
   }

   enum Billboard {
      FIXED,
      VERTICAL,
      HORIZONTAL,
      CENTER;

      Billboard() {
      }
   }

   final class Brightness {
      private final int blockLight;
      private final int skyLight;

      public Brightness(int blockLight, int skyLight) {
         if (blockLight < 0 || blockLight > 15) {
            throw new IllegalArgumentException("Block brightness out of range: " + blockLight);
         }

         if (skyLight >= 0 && skyLight <= 15) {
            this.blockLight = blockLight;
            this.skyLight = skyLight;
         } else {
            throw new IllegalArgumentException("Sky brightness out of range: " + skyLight);
         }
      }

      public int getBlockLight() {
         return this.blockLight;
      }

      public int getSkyLight() {
         return this.skyLight;
      }

      @Override
      public int hashCode() {
         int hash = 7;
         hash = 47 * hash + this.blockLight;
         return 47 * hash + this.skyLight;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            FakeDisplayEntity.Brightness other = (FakeDisplayEntity.Brightness)obj;
            return this.blockLight == other.blockLight && this.skyLight == other.skyLight;
         } else {
            return false;
         }
      }

      @Override
      public String toString() {
         return "Brightness{blockLight=" + this.blockLight + ", skyLight=" + this.skyLight + "}";
      }
   }
}
