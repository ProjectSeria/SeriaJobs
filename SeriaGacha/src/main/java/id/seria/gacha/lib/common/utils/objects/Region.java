package id.seria.gacha.lib.common.utils.objects;

import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.utility.BukkitUtil;
import lombok.Generated;
import lombok.NonNull;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Region implements Cloneable {
   @ConfigField
   private World world;
   @ConfigField(path = "minimum")
   private Vector minimumPosition;
   @ConfigField(path = "maximum")
   private Vector maximumPosition;
   private boolean normalized;

   public static Region from(@NonNull Block first, @NonNull Block second) {
      if (first == null) {
         throw new NullPointerException("first is marked non-null but is null");
      }

      if (second == null) {
         throw new NullPointerException("second is marked non-null but is null");
      }

      Validate.isTrue(first.getWorld().equals(second.getWorld()), "World for both blocks are not the same");
      Vector minimum = new Vector(Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()), Math.min(first.getZ(), second.getZ()));
      Vector maximum = new Vector(Math.max(first.getX(), second.getX()), Math.max(first.getY(), second.getY()), Math.max(first.getZ(), second.getZ()));
      return new Region(first.getWorld(), minimum, maximum);
   }

   public Region(World world, Vector first, Vector second) {
      this.set(first.toLocation(world), second.toLocation(world));
   }

   public Region(Location first, Location second) {
      this.set(first, second);
   }

   public void set(Location first, Location second) {
      Validate.isTrue(first.getWorld().equals(second.getWorld()), "First and Second location don't share the same world");
      this.world = first.getWorld();
      this.set(first.toVector(), second.toVector());
   }

   public void set(Vector first, Vector second) {
      if (!BukkitUtil.hasDecimal(first) && !BukkitUtil.hasDecimal(second)) {
         this.minimumPosition = new Vector(Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()), Math.min(first.getZ(), second.getZ()));
         this.maximumPosition = new Vector(Math.max(first.getX(), second.getX()), Math.max(first.getY(), second.getY()), Math.max(first.getZ(), second.getZ()));
         this.normalized = true;
      } else {
         throw new IllegalArgumentException("Both vectors must have integer values for X, Y, and Z.");
      }
   }

   public void normalize() {
      if (!this.normalized) {
         this.set(this.minimumPosition, this.maximumPosition);
      }
   }

   public boolean intersects(Location location) {
      if (location.getWorld().equals(this.world) && this.minimumPosition != null && this.maximumPosition != null) {
         this.normalize();
         int locX = location.getBlockX();
         int locY = location.getBlockY();
         int locZ = location.getBlockZ();
         return locX >= this.minimumPosition.getX()
            && locX <= this.maximumPosition.getX()
            && locY >= this.minimumPosition.getY()
            && locY <= this.maximumPosition.getY()
            && locZ >= this.minimumPosition.getZ()
            && locZ <= this.maximumPosition.getZ();
      } else {
         return false;
      }
   }

   public boolean intersects(Block block) {
      if (block.getWorld().equals(this.world) && this.minimumPosition != null && this.maximumPosition != null) {
         this.normalize();
         int locX = block.getX();
         int locY = block.getY();
         int locZ = block.getZ();
         return locX >= this.minimumPosition.getX()
            && locX <= this.maximumPosition.getX()
            && locY >= this.minimumPosition.getY()
            && locY <= this.maximumPosition.getY()
            && locZ >= this.minimumPosition.getZ()
            && locZ <= this.maximumPosition.getZ();
      } else {
         return false;
      }
   }

   public boolean intersects(Player player) {
      return this.intersects(player.getLocation()) || this.intersects(player.getLocation().add(0.0, 1.79, 0.0));
   }

   public Location adjustToBoundary(Location location) {
      return this.adjustToBoundary(location, 0.0);
   }

   public Location adjustToBoundary(Location location, double inset) {
      if (!this.intersects(location)) {
         double x = location.getX();
         double y = location.getY();
         double z = location.getZ();
         x = Math.min(Math.max(x, this.minimumPosition.getX() + inset), this.maximumPosition.getX() - inset);
         y = Math.min(Math.max(y, this.minimumPosition.getY() + inset), this.maximumPosition.getY() - inset);
         z = Math.min(Math.max(z, this.minimumPosition.getZ() + inset), this.maximumPosition.getZ() - inset);
         return new Location(this.world, x, y, z, location.getYaw(), location.getPitch());
      } else {
         return location;
      }
   }

   public Region subtract(Vector vec) {
      this.minimumPosition.setX(this.minimumPosition.getX() - vec.getX());
      this.minimumPosition.setY(this.minimumPosition.getY() - vec.getY());
      this.minimumPosition.setZ(this.minimumPosition.getZ() - vec.getZ());
      this.maximumPosition.setX(this.maximumPosition.getX() - vec.getX());
      this.maximumPosition.setY(this.maximumPosition.getY() - vec.getY());
      this.maximumPosition.setZ(this.maximumPosition.getZ() - vec.getZ());
      return this;
   }

   public Region add(Vector vec) {
      this.minimumPosition.setX(this.minimumPosition.getX() + vec.getX());
      this.minimumPosition.setY(this.minimumPosition.getY() + vec.getY());
      this.minimumPosition.setZ(this.minimumPosition.getZ() + vec.getZ());
      this.maximumPosition.setX(this.maximumPosition.getX() + vec.getX());
      this.maximumPosition.setY(this.maximumPosition.getY() + vec.getY());
      this.maximumPosition.setZ(this.maximumPosition.getZ() + vec.getZ());
      return this;
   }

   public Vector getCenterPosition() {
      return this.minimumPosition.clone().add(this.maximumPosition).multiply(0.5);
   }

   public Region clone() {
      return new Region(this.minimumPosition.toLocation(this.world), this.maximumPosition.toLocation(this.world));
   }

   @Generated
   public World getWorld() {
      return this.world;
   }

   @Generated
   public Vector getMinimumPosition() {
      return this.minimumPosition;
   }

   @Generated
   public Vector getMaximumPosition() {
      return this.maximumPosition;
   }

   @Generated
   public boolean isNormalized() {
      return this.normalized;
   }

   @Generated
   public Region() {
   }

   @Generated
   public void setWorld(World world) {
      this.world = world;
   }
}
