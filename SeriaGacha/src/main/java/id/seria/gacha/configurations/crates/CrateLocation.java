package id.seria.gacha.configurations.crates;

import com.google.common.base.Objects;
import id.seria.gacha.internal.FacingDirection;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.annotations.Serializable;
import lombok.Generated;
import org.bukkit.Location;

@Serializable
public class CrateLocation {
   @ConfigField
   private String world;
   @ConfigField
   private int x;
   @ConfigField
   private int y;
   @ConfigField
   private int z;
   @ConfigField
   private FacingDirection face = FacingDirection.UNKNOWN;

   @Override
   public int hashCode() {
      return Objects.hashCode(new Object[]{this.world, this.x, this.y, this.z});
   }

   @Override
   public boolean equals(Object object) {
      return !(object instanceof CrateLocation casted)
         ? false
         : this.world.equalsIgnoreCase(casted.world) && this.x == casted.x && this.y == casted.y && this.z == casted.z;
   }

   public static CrateLocation from(Location location) {
      if (location != null && location.getWorld() != null) {
         FacingDirection facingDirection = FacingDirection.UNKNOWN;
         if (location.getYaw() >= -45.0F && location.getYaw() < 45.0F) {
            facingDirection = FacingDirection.SOUTH;
         } else if (location.getYaw() >= 45.0F && location.getYaw() < 135.0F) {
            facingDirection = FacingDirection.WEST;
         } else if (location.getYaw() >= -135.0F && location.getYaw() < -45.0F) {
            facingDirection = FacingDirection.EAST;
         } else {
            facingDirection = FacingDirection.NORTH;
         }

         return new CrateLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), facingDirection);
      } else {
         throw new IllegalArgumentException("Location or world cannot be null");
      }
   }

   @Generated
   public String getWorld() {
      return this.world;
   }

   @Generated
   public int getX() {
      return this.x;
   }

   @Generated
   public int getY() {
      return this.y;
   }

   @Generated
   public int getZ() {
      return this.z;
   }

   @Generated
   public FacingDirection getFace() {
      return this.face;
   }

   @Generated
   public void setWorld(String world) {
      this.world = world;
   }

   @Generated
   public void setX(int x) {
      this.x = x;
   }

   @Generated
   public void setY(int y) {
      this.y = y;
   }

   @Generated
   public void setZ(int z) {
      this.z = z;
   }

   @Generated
   public void setFace(FacingDirection face) {
      this.face = face;
   }

   @Generated
   public CrateLocation() {
   }

   @Generated
   public CrateLocation(String world, int x, int y, int z, FacingDirection face) {
      this.world = world;
      this.x = x;
      this.y = y;
      this.z = z;
      this.face = face;
   }
}
