package id.seria.gacha.lib.common.nms.interfaces.particles.data;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public final class VibrationData extends ParticleData {
   private final Location start;
   private final Location blockDestination;
   private final Entity entitydestination;
   private final int ticks;

   public VibrationData(Location start, Location destination, int ticks) {
      this.start = Objects.requireNonNull(start);
      this.blockDestination = Objects.requireNonNull(destination);
      this.entitydestination = null;
      this.ticks = ticks;
   }

   public VibrationData(Location destination, int ticks) {
      this.start = null;
      this.blockDestination = Objects.requireNonNull(destination);
      this.entitydestination = null;
      this.ticks = ticks;
   }

   public VibrationData(Location start, Entity destination, int ticks) {
      this.start = Objects.requireNonNull(start);
      this.entitydestination = Objects.requireNonNull(destination);
      this.blockDestination = null;
      this.ticks = ticks;
   }

   public VibrationData(Entity destination, int ticks) {
      this.start = null;
      this.entitydestination = Objects.requireNonNull(destination);
      this.blockDestination = null;
      this.ticks = ticks;
   }

   public Location getStart() {
      return this.start;
   }

   public Location getBlockDestination() {
      return this.blockDestination;
   }

   public Entity getEntityDestination() {
      return this.entitydestination;
   }

   public int getTicks() {
      return this.ticks;
   }
}
