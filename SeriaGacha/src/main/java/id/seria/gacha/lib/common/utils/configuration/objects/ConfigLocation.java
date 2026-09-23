package id.seria.gacha.lib.common.utils.configuration.objects;

import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import lombok.Generated;

public class ConfigLocation {
   @ConfigField
   private String world;
   @ConfigField
   private double x;
   @ConfigField
   private double y;
   @ConfigField
   private double z;
   @ConfigField
   private float yaw;
   @ConfigField
   private float pitch;

   @Generated
   public String getWorld() {
      return this.world;
   }

   @Generated
   public double getX() {
      return this.x;
   }

   @Generated
   public double getY() {
      return this.y;
   }

   @Generated
   public double getZ() {
      return this.z;
   }

   @Generated
   public float getYaw() {
      return this.yaw;
   }

   @Generated
   public float getPitch() {
      return this.pitch;
   }

   @Generated
   public void setWorld(String world) {
      this.world = world;
   }

   @Generated
   public void setX(double x) {
      this.x = x;
   }

   @Generated
   public void setY(double y) {
      this.y = y;
   }

   @Generated
   public void setZ(double z) {
      this.z = z;
   }

   @Generated
   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   @Generated
   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   @Generated
   public ConfigLocation(String world, double x, double y, double z, float yaw, float pitch) {
      this.world = world;
      this.x = x;
      this.y = y;
      this.z = z;
      this.yaw = yaw;
      this.pitch = pitch;
   }

   @Generated
   public ConfigLocation() {
   }
}
