package id.seria.gacha.lib.xseries.particles;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

public final class Particles {
   public static final double PII = Math.PI * 2;
   public static final double R270 = Math.toRadians(270.0);
   public static final double R90 = Math.PI / 2;

   private Particles() {
   }

   public static Optional<XParticle> randomParticle(String... particles) {
      int rand = randInt(0, particles.length - 1);
      return XParticle.of(particles[rand]);
   }

   public static double random(double min, double max) {
      return ThreadLocalRandom.current().nextDouble(min, max);
   }

   public static int randInt(int min, int max) {
      return ThreadLocalRandom.current().nextInt(min, max + 1);
   }

   public static Color randomColor() {
      ThreadLocalRandom gen = ThreadLocalRandom.current();
      int randR = gen.nextInt(0, 256);
      int randG = gen.nextInt(0, 256);
      int randB = gen.nextInt(0, 256);
      return Color.fromRGB(randR, randG, randB);
   }

   public static DustOptions randomDust() {
      float size = randInt(5, 10) / 10.0F;
      return new DustOptions(randomColor(), size);
   }

   public static void blackSun(double radius, double radiusRate, double rate, double rateChange, ParticleDisplay display) {
      double j = 0.0;
      double i = 10.0;

      while (i > 0.0) {
         j += rateChange;
         circle(radius + i, rate - j, display);
         i -= radiusRate;
      }
   }

   public static void circle(double radius, double rate, ParticleDisplay display) {
      circle(radius, radius, 1.0, rate, 0.0, display);
   }

   public static void circle(double radius, double radius2, double extension, double rate, double limit, ParticleDisplay display) {
      double rateDiv = Math.PI / Math.abs(rate);
      if (limit == 0.0) {
         limit = Math.PI * 2;
      } else if (limit == -1.0) {
         limit = (Math.PI * 2) / Math.abs(extension);
      }

      for (double theta = 0.0; theta <= limit; theta += rateDiv) {
         double x = radius * Math.cos(extension * theta);
         double z = radius2 * Math.sin(extension * theta);
         if (display.isDirectional()) {
            double phi = Math.atan2(z, x);
            double directionX = Math.cos(extension * phi);
            double directionZ = Math.sin(extension * phi);
            display.particleDirection(directionX, display.getOffset().getY(), directionZ);
         }

         display.spawn(x, 0.0, z);
      }
   }

   public static void diamond(double radiusRate, double rate, double height, ParticleDisplay display) {
      double count = 0.0;
      double y = 0.0;

      while (y < height * 2.0) {
         if (y < height) {
            count += radiusRate;
         } else {
            count -= radiusRate;
         }

         for (double x = -count; x < count; x += rate) {
            display.spawn(x, y, 0.0);
         }

         y += rate;
      }
   }

   public static Runnable circularBeam(final double maxRadius, final double rate, final double radiusRate, final double extend, final ParticleDisplay display) {
      return new Runnable() {
         final double rateDiv = Math.PI / rate;
         final double radiusDiv = Math.PI / radiusRate;
         final Vector dir = display.getLocation().getDirection().normalize().multiply(extend);
         double dynamicRadius = 0.0;

         @Override
         public void run() {
            double radius = maxRadius * Math.sin(this.dynamicRadius);

            for (double theta = 0.0; theta < Math.PI * 2; theta += this.rateDiv) {
               double x = radius * Math.sin(theta);
               double z = radius * Math.cos(theta);
               display.spawn(x, 0.0, z);
            }

            this.dynamicRadius = this.dynamicRadius + this.radiusDiv;
            if (this.dynamicRadius > Math.PI) {
               this.dynamicRadius = 0.0;
            }

            display.getLocation().add(this.dir);
         }
      };
   }

   public static BukkitTask circularBeam(Plugin plugin, double maxRadius, double rate, double radiusRate, double extend, ParticleDisplay display) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, circularBeam(maxRadius, rate, radiusRate, extend, display), 0L, 1L);
   }

   public static void flower(int count, double radius, ParticleDisplay display, Runnable runnable) {
      double theta = 0.0;

      while (theta < Math.PI * 2) {
         double x = radius * Math.cos(theta);
         double z = radius * Math.sin(theta);
         display.getLocation().add(x, 0.0, z);
         runnable.run();
         display.getLocation().subtract(x, 0.0, z);
         theta += (Math.PI * 2) / count;
      }
   }

   public static void filledCircle(double radius, double rate, double radiusRate, ParticleDisplay display) {
      double dynamicRate = 0.0;
      double i = 0.1;

      while (i < radius) {
         if (i > radius) {
            i = radius;
         }

         dynamicRate += rate / (radius / radiusRate);
         circle(i, dynamicRate, display);
         i += radiusRate;
      }
   }

   public static Runnable chaoticDoublePendulum(
      final double radius,
      final double gravity,
      final double length,
      final double length2,
      final double mass1,
      final double mass2,
      final boolean dimension3,
      final int speed,
      final ParticleDisplay display
   ) {
      return new Runnable() {
         double theta = Math.PI / 2;
         double theta2 = Math.PI / 2;
         double thetaPrime = 0.0;
         double thetaPrime2 = 0.0;

         @Override
         public void run() {
            int repeat = speed;

            while (repeat-- != 0) {
               if (dimension3) {
                  display.rotate(0.09519977738150888, 0.07139983303613166, 0.057119866428905326);
               }

               double totalMass = mass1 + mass2;
               double totalMassDouble = 2.0 * totalMass;
               double deltaTheta = this.theta - this.theta2;
               double lenLunar = totalMassDouble - mass2 * Math.cos(2.0 * this.theta - 2.0 * this.theta2);
               double deltaCosTheta = Math.cos(deltaTheta);
               double deltaSinTheta = Math.sin(deltaTheta);
               double phi = this.thetaPrime * this.thetaPrime * length;
               double phi2 = this.thetaPrime2 * this.thetaPrime2 * length2;
               double num1 = -gravity * totalMassDouble * Math.sin(this.theta);
               double num2 = -mass2 * gravity * Math.sin(this.theta - 2.0 * this.theta2);
               double num3 = -2.0 * deltaSinTheta * mass2;
               double num4 = phi2 + phi * deltaCosTheta;
               double len = length * lenLunar;
               double thetaDoublePrime = (num1 + num2 + num3 * num4) / len;
               num1 = 2.0 * deltaSinTheta;
               num2 = phi * totalMass;
               num3 = gravity * totalMass * Math.cos(this.theta);
               num4 = phi2 * mass2 * deltaCosTheta;
               len = length2 * lenLunar;
               double thetaDoublePrime2 = num1 * (num2 + num3 + num4) / len;
               this.thetaPrime += thetaDoublePrime;
               this.thetaPrime2 += thetaDoublePrime2;
               this.theta = this.theta + this.thetaPrime;
               this.theta2 = this.theta2 + this.thetaPrime2;
               double x = radius * Math.sin(this.theta);
               double y = radius * Math.cos(this.theta);
               double x2 = x + radius * Math.sin(this.theta2);
               double y2 = y + radius * Math.cos(this.theta2);
               display.spawn(x2, y2, 0.0);
            }
         }
      };
   }

   public static BukkitTask chaoticDoublePendulum(
      Plugin plugin,
      double radius,
      double gravity,
      double length,
      double length2,
      double mass1,
      double mass2,
      boolean dimension3,
      int speed,
      ParticleDisplay display
   ) {
      return Bukkit.getScheduler()
         .runTaskTimerAsynchronously(plugin, chaoticDoublePendulum(radius, gravity, length, length2, mass1, mass2, dimension3, speed, display), 0L, 1L);
   }

   public static Runnable magicCircles(final double radius, final double rate, final double radiusRate, final double distance, final ParticleDisplay display) {
      return new Runnable() {
         final double radiusDiv = Math.PI / radiusRate;
         final Vector dir = display.getLocation().getDirection().normalize().multiply(distance);
         double dynamicRadius = radius;

         @Override
         public void run() {
            double rateDiv = Math.PI / (rate * this.dynamicRadius);

            for (double theta = 0.0; theta < Math.PI * 2; theta += rateDiv) {
               double x = this.dynamicRadius * Math.sin(theta);
               double z = this.dynamicRadius * Math.cos(theta);
               display.spawn(x, 0.0, z);
            }

            this.dynamicRadius = this.dynamicRadius + this.radiusDiv;
            display.getLocation().add(this.dir);
         }
      };
   }

   public static BukkitTask magicCircles(Plugin plugin, double radius, double rate, double radiusRate, double distance, ParticleDisplay display) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, magicCircles(radius, rate, radiusRate, distance, display), 0L, 1L);
   }

   public static void infinity(double radius, double rate, ParticleDisplay display) {
      double rateDiv = Math.PI / rate;

      for (double i = 0.0; i < Math.PI * 2; i += rateDiv) {
         double x = Math.sin(i);
         double smooth = Math.pow(x, 2.0) + 1.0;
         double curve = radius * Math.cos(i);
         double z = curve / smooth;
         double y = curve * x / smooth;
         circle(1.0, rate, display.cloneWithLocation(x, y, z));
      }
   }

   public static void cone(double height, double radius, double rate, double circleRate, ParticleDisplay display) {
      double radiusDiv = radius / (height / rate);

      for (double i = 0.0; i < height; i += rate) {
         radius -= radiusDiv;
         if (radius < 0.0) {
            radius = 0.0;
         }

         circle(radius, circleRate - i, display.cloneWithLocation(0.0, i, 0.0));
      }
   }

   public static void slash(double size, boolean useWideSide, ParticleDisplay display) {
      double start = useWideSide ? Math.PI / 2 : 0.0;
      double end = useWideSide ? R270 : Math.PI;
      ellipse(start, end, 0.10471975511965977, size, size + 2.0, display);
   }

   public static void slash(
      Plugin plugin, final double distance, final boolean useWideSide, final Supplier<Double> size, final Supplier<Double> speed, final ParticleDisplay display
   ) {
      (new BukkitRunnable() {
         double distanceTraveled = 0.0;

         public void run() {
            Particles.slash(size.get(), useWideSide, display);
            double speedConst = speed.get();
            this.distanceTraveled += speedConst;
            if (this.distanceTraveled >= distance) {
               this.cancel();
            } else {
               display.advanceInDirection(speedConst);
            }
         }
      }).runTaskTimerAsynchronously(plugin, 1L, 1L);
   }

   public static void ellipse(double start, double end, double rate, double radius, double otherRadius, ParticleDisplay display) {
      double theta = start;

      while (theta <= end) {
         double x = radius * Math.cos(theta);
         double z = otherRadius * Math.sin(theta);
         display.spawn(x, 0.0, z);
         theta += rate;
      }
   }

   public static BooleanSupplier blackhole(
      final int points, final double radius, final double rate, final int mode, final int time, final ParticleDisplay display
   ) {
      display.extra = 0.1;
      return new BooleanSupplier() {
         final double rateDiv = Math.PI / rate;
         int timer = time;
         double theta = 0.0;
         boolean done = false;

         @Override
         public boolean getAsBoolean() {
            if (this.done) {
               return false;
            }

            for (int i = 0; i < points; i++) {
               double angle = (Math.PI * 2) * ((double)i / points);
               double x = radius * Math.cos(this.theta + angle);
               double z = radius * Math.sin(this.theta + angle);
               double phi = Math.atan2(z, x);
               double xDirection = -Math.cos(phi);
               double zDirection = -Math.sin(phi);
               display.particleDirection(xDirection, 0.0, zDirection);
               display.spawn(x, 0.0, z);
               if (mode > 1) {
                  x = radius * Math.cos(-this.theta + angle);
                  z = radius * Math.sin(-this.theta + angle);
                  if (mode == 2) {
                     phi = Math.atan2(z, x);
                  } else if (mode == 3) {
                     phi = Math.atan2(x, z);
                  } else if (mode == 4) {
                     Math.atan2(Math.log(x), Math.log(z));
                  }

                  xDirection = -Math.cos(phi);
                  zDirection = -Math.sin(phi);
                  display.particleDirection(xDirection, 0.0, zDirection);
                  display.spawn(x, 0.0, z);
               }
            }

            this.theta = this.theta + this.rateDiv;
            if (--this.timer <= 0) {
               this.done = true;
               return false;
            } else {
               return true;
            }
         }
      };
   }

   public static BukkitTask blackhole(Plugin plugin, int points, double radius, double rate, int mode, int time, ParticleDisplay display) {
      final BooleanSupplier blackhole = blackhole(points, radius, rate, mode, time, display);
      return (new BukkitRunnable() {
         public void run() {
            if (!blackhole.getAsBoolean()) {
               this.cancel();
            }
         }
      }).runTaskTimerAsynchronously(plugin, 0L, 1L);
   }

   public static void rainbow(double radius, double rate, double curve, double layers, double compact, ParticleDisplay display) {
      int[][] rainbow = new int[][]{{128, 0, 128}, {75, 0, 130}, {0, 0, 255}, {0, 255, 0}, {255, 255, 0}, {255, 140, 0}, {255, 0, 0}};
      double secondRadius = radius * curve;

      for (int i = 0; i < 7; i++) {
         int[] rgb = rainbow[i];
         display = ParticleDisplay.of(XParticle.DUST).withLocation(display.getLocation()).withColor(new java.awt.Color(rgb[0], rgb[1], rgb[2]), 1.0F);

         for (int layer = 0; layer < layers; layer++) {
            double rateDiv = Math.PI / (rate * (i + 2));

            for (double theta = 0.0; theta <= Math.PI; theta += rateDiv) {
               double x = radius * Math.cos(theta);
               double y = secondRadius * Math.sin(theta);
               display.spawn(x, y, 0.0);
            }

            radius += compact;
         }
      }
   }

   public static void crescent(double radius, double rate, ParticleDisplay display) {
      double rateDiv = Math.PI / rate;
      double end = Math.toRadians(325.0);

      for (double theta = Math.toRadians(45.0); theta <= end; theta += rateDiv) {
         double x = Math.cos(theta);
         double z = Math.sin(theta);
         display.spawn(radius * x, 0.0, radius * z);
         double smallerRadius = radius / 1.3;
         display.spawn(smallerRadius * x + 0.8, 0.0, smallerRadius * z);
      }
   }

   public static void waveFunction(double extend, double heightRange, double size, double rate, ParticleDisplay display) {
      double height = heightRange / 2.0;
      boolean increase = true;
      double increaseRandomizer = random(heightRange / 2.0, heightRange);
      double rateDiv = Math.PI / rate;
      size *= Math.PI * 2;

      for (double x = 0.0; x <= size; x += rateDiv) {
         double xx = extend * x;
         double y1 = Math.sin(x);
         if (y1 == 1.0) {
            increase = !increase;
            if (increase) {
               increaseRandomizer = random(heightRange / 2.0, heightRange);
            } else {
               increaseRandomizer = random(-heightRange, -heightRange / 2.0);
            }
         }

         height += increaseRandomizer;

         for (double z = 0.0; z <= size; z += rateDiv) {
            double y2 = Math.cos(z);
            double yy = height * y1 * y2;
            double zz = extend * z;
            display.spawn(xx, yy, zz);
         }
      }
   }

   public static Runnable vortex(final int points, double rate, final ParticleDisplay display) {
      final double rateDiv = Math.PI / rate;
      return new Runnable() {
         double theta = 0.0;

         @Override
         public void run() {
            this.theta = this.theta + rateDiv;

            for (int i = 0; i < points; i++) {
               double multiplier = (Math.PI * 2) * ((double)i / points);
               double x = Math.cos(this.theta + multiplier);
               double z = Math.sin(this.theta + multiplier);
               double angle = Math.atan2(z, x);
               double xDirection = Math.cos(angle);
               double zDirection = Math.sin(angle);
               display.particleDirection(xDirection, 0.0, zDirection);
               display.spawn(x, 0.0, z);
            }
         }
      };
   }

   public static BukkitTask vortex(Plugin plugin, int points, double rate, ParticleDisplay display) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, vortex(points, rate, display), 0L, 1L);
   }

   public static void cylinder(double height, double radius, double rate, ParticleDisplay display) {
      filledCircle(radius, rate, 3.0, display);
      filledCircle(radius, rate, 3.0, display.cloneWithLocation(0.0, height, 0.0));

      for (double y = 0.0; y < height; y += 0.1) {
         circle(radius, rate, display.cloneWithLocation(0.0, y, 0.0));
      }
   }

   public static Runnable moveRotatingAround(
      final double rate, final double offsetx, final double offsety, final double offsetz, final Runnable runnable, final ParticleDisplay... displays
   ) {
      return new Runnable() {
         double rotation = 180.0;

         @Override
         public void run() {
            this.rotation = this.rotation + rate;
            double x = Math.toRadians(90.0 + this.rotation);
            double y = Math.toRadians(60.0 + this.rotation);
            double z = Math.toRadians(30.0 + this.rotation);
            Vector vector = new Vector(offsetx * Math.PI, offsety * Math.PI, offsetz * Math.PI);
            if (offsetx != 0.0) {
               ParticleDisplay.rotateAround(vector, ParticleDisplay.Axis.X, x);
            }

            if (offsety != 0.0) {
               ParticleDisplay.rotateAround(vector, ParticleDisplay.Axis.Y, y);
            }

            if (offsetz != 0.0) {
               ParticleDisplay.rotateAround(vector, ParticleDisplay.Axis.Z, z);
            }

            for (ParticleDisplay display : displays) {
               display.getLocation().add(vector);
            }

            runnable.run();

            for (ParticleDisplay display : displays) {
               display.getLocation().subtract(vector);
            }
         }
      };
   }

   public static BukkitTask moveRotatingAround(
      Plugin plugin, long update, double rate, double offsetx, double offsety, double offsetz, Runnable runnable, ParticleDisplay... displays
   ) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, moveRotatingAround(rate, offsetx, offsety, offsetz, runnable, displays), 0L, update);
   }

   public static Runnable moveAround(
      final double rate,
      final double endRate,
      final double offsetx,
      final double offsety,
      final double offsetz,
      final Runnable runnable,
      final ParticleDisplay... displays
   ) {
      return new Runnable() {
         double multiplier = 0.0;
         boolean opposite = false;

         @Override
         public void run() {
            if (this.opposite) {
               this.multiplier = this.multiplier - rate;
            } else {
               this.multiplier = this.multiplier + rate;
            }

            double x = this.multiplier * offsetx;
            double y = this.multiplier * offsety;
            double z = this.multiplier * offsetz;

            for (ParticleDisplay display : displays) {
               display.getLocation().add(x, y, z);
            }

            runnable.run();

            for (ParticleDisplay display : displays) {
               display.getLocation().subtract(x, y, z);
            }

            if (this.opposite) {
               if (this.multiplier <= 0.0) {
                  this.opposite = false;
               }
            } else if (this.multiplier >= endRate) {
               this.opposite = true;
            }
         }
      };
   }

   public static BukkitTask moveAround(
      Plugin plugin, long update, double rate, double endRate, double offsetx, double offsety, double offsetz, Runnable runnable, ParticleDisplay... displays
   ) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, moveAround(rate, endRate, offsetx, offsety, offsetz, runnable, displays), 0L, update);
   }

   public static BukkitTask testDisplay(Plugin plugin, Runnable runnable) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, 0L, 1L);
   }

   public static Runnable rotateAround(
      final double rate, final double offsetx, final double offsety, final double offsetz, final Runnable runnable, final ParticleDisplay... displays
   ) {
      return new Runnable() {
         double rotation = 180.0;

         @Override
         public void run() {
            this.rotation = this.rotation + rate;
            double x = Math.toRadians((90.0 + this.rotation) * offsetx);
            double y = Math.toRadians((60.0 + this.rotation) * offsety);
            double z = Math.toRadians((30.0 + this.rotation) * offsetz);

            for (ParticleDisplay display : displays) {
               display.rotate(x, y, z);
            }

            runnable.run();
         }
      };
   }

   public static BukkitTask rotateAround(
      Plugin plugin, long update, double rate, double offsetx, double offsety, double offsetz, Runnable runnable, ParticleDisplay... displays
   ) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, rotateAround(rate, offsetx, offsety, offsetz, runnable, displays), 0L, update);
   }

   public static Runnable guard(
      final double rate, final double offsetx, final double offsety, final double offsetz, final Runnable runnable, final ParticleDisplay... displays
   ) {
      return new Runnable() {
         double rotation = 180.0;

         @Override
         public void run() {
            this.rotation = this.rotation + rate;
            double x = Math.toRadians((90.0 + this.rotation) * offsetx);
            double y = Math.toRadians((60.0 + this.rotation) * offsety);
            double z = Math.toRadians((30.0 + this.rotation) * offsetz);
            Vector vector = new Vector(offsetx * Math.PI, offsety * Math.PI, offsetz * Math.PI);
            ParticleDisplay.rotateAround(vector, x, y, z);

            for (ParticleDisplay display : displays) {
               display.rotate(x, y, z);
               display.getLocation().add(vector);
            }

            runnable.run();

            for (ParticleDisplay display : displays) {
               display.getLocation().subtract(vector);
            }
         }
      };
   }

   public static BukkitTask guard(
      Plugin plugin, long update, double rate, double offsetx, double offsety, double offsetz, Runnable runnable, ParticleDisplay... displays
   ) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, guard(rate, offsetx, offsety, offsetz, runnable, displays), 0L, update);
   }

   public static void sphere(double radius, double rate, ParticleDisplay display) {
      double rateDiv = Math.PI / rate;

      for (double phi = 0.0; phi <= Math.PI; phi += rateDiv) {
         double y1 = radius * Math.cos(phi);
         double y2 = radius * Math.sin(phi);

         for (double theta = 0.0; theta <= Math.PI * 2; theta += rateDiv) {
            double x = Math.cos(theta) * y2;
            double z = Math.sin(theta) * y2;
            if (display.isDirectional()) {
               double omega = Math.atan2(z, x);
               double directionX = Math.cos(omega);
               double directionY = Math.sin(Math.atan2(y2, y1));
               double directionZ = Math.sin(omega);
               display.particleDirection(directionX, directionY, directionZ);
            }

            display.spawn(x, y1, z);
         }
      }
   }

   public static void spikeSphere(double radius, double rate, int chance, double minRandomDistance, double maxRandomDistance, ParticleDisplay display) {
      double rateDiv = Math.PI / rate;

      for (double phi = 0.0; phi <= Math.PI; phi += rateDiv) {
         double y = radius * Math.cos(phi);
         double sinPhi = radius * Math.sin(phi);

         for (double theta = 0.0; theta <= Math.PI * 2; theta += rateDiv) {
            double x = Math.cos(theta) * sinPhi;
            double z = Math.sin(theta) * sinPhi;
            if (chance == 0 || randInt(0, chance) == 1) {
               Location start = display.cloneLocation(x, y, z);
               Vector endVect = start.clone().subtract(display.getLocation()).toVector().multiply(random(minRandomDistance, maxRandomDistance));
               Location end = start.clone().add(endVect);
               line(start, end, 0.1, display);
            }
         }
      }
   }

   public static void ring(double rate, double radius, double tubeRadius, ParticleDisplay display) {
      double rateDiv = Math.PI / rate;
      double tubeDiv = Math.PI / tubeRadius;

      for (double theta = 0.0; theta <= Math.PI * 2; theta += rateDiv) {
         double cos = Math.cos(theta);
         double sin = Math.sin(theta);

         for (double phi = 0.0; phi <= Math.PI * 2; phi += tubeDiv) {
            double finalRadius = radius + tubeRadius * Math.cos(phi);
            double x = finalRadius * cos;
            double y = finalRadius * sin;
            double z = tubeRadius * Math.sin(phi);
            display.spawn(x, y, z);
         }
      }
   }

   public static BooleanSupplier spread(
      final int amount,
      final int rate,
      final Location start,
      final Location originEnd,
      final double offsetx,
      final double offsety,
      final double offsetz,
      final ParticleDisplay display
   ) {
      return new BooleanSupplier() {
         int count = amount;
         boolean done = false;

         @Override
         public boolean getAsBoolean() {
            if (this.done) {
               return false;
            }

            int frame = rate;

            while (frame-- != 0) {
               double x = Particles.random(-offsetx, offsetx);
               double y = Particles.random(-offsety, offsety);
               double z = Particles.random(-offsetz, offsetz);
               Location end = originEnd.clone().add(x, y, z);
               Particles.line(start, end, 0.1, display);
            }

            if (this.count-- <= 0) {
               this.done = true;
               return false;
            } else {
               return true;
            }
         }
      };
   }

   public static BukkitTask spread(
      Plugin plugin, int amount, int rate, Location start, Location originEnd, double offsetx, double offsety, double offsetz, ParticleDisplay display
   ) {
      final BooleanSupplier spread = spread(amount, rate, start, originEnd, offsetx, offsety, offsetz, display);
      return (new BukkitRunnable() {
         public void run() {
            if (!spread.getAsBoolean()) {
               this.cancel();
            }
         }
      }).runTaskTimerAsynchronously(plugin, 0L, 1L);
   }

   public static void heart(double cut, double cutAngle, double depth, double compressHeight, double rate, ParticleDisplay display) {
      double theta = 0.0;

      while (theta <= Math.PI * 2) {
         double phi = theta / cut;
         double cos = Math.cos(phi);
         double sin = Math.sin(phi);
         double omega = Math.pow(Math.abs(Math.sin(2.0 * cutAngle * phi)) + depth * Math.abs(Math.sin(cutAngle * phi)), 1.0 / compressHeight);
         double y = omega * (sin + cos);
         double z = omega * (cos - sin);
         display.spawn(0.0, y, z);
         theta += Math.PI / rate;
      }
   }

   public static Runnable atomic(final int orbits, final double radius, final double rate, final ParticleDisplay orbit) {
      return new Runnable() {
         final double rateDiv = Math.PI / rate;
         final double dist = Math.PI / orbits;
         double theta = 0.0;

         @Override
         public void run() {
            int orbital = orbits;
            this.theta = this.theta + this.rateDiv;
            double x = radius * Math.cos(this.theta);
            double z = radius * Math.sin(this.theta);

            for (double angle = 0.0; orbital > 0; angle += this.dist) {
               orbit.rotate(ParticleDisplay.Rotation.of(angle, ParticleDisplay.Axis.Z));
               orbit.spawn(x, 0.0, z);
               orbital--;
            }
         }
      };
   }

   public static BukkitTask atomic(Plugin plugin, int orbits, double radius, double rate, ParticleDisplay orbit) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, atomic(orbits, radius, rate, orbit), 0L, 1L);
   }

   public static BooleanSupplier helix(
      final int strings,
      final double radius,
      final double rate,
      final double extension,
      final double length,
      final double speed,
      final double rotationRate,
      final boolean fadeUp,
      final boolean fadeDown,
      final ParticleDisplay display
   ) {
      return new BooleanSupplier() {
         final double distanceBetweenEachCirclePoints = (Math.PI * 2) / strings;
         final double radiusDiv = radius / (length / rate);
         final double radiusDiv2 = fadeUp && fadeDown ? this.radiusDiv * 2.0 : this.radiusDiv;
         double dynamicRadius = fadeDown ? 0.0 : radius;
         boolean center = !fadeDown;
         final double calculatedRotRate = this.distanceBetweenEachCirclePoints / rotationRate;
         double rotation = 0.0;
         double currentDistance = 0.0;

         @Override
         public boolean getAsBoolean() {
            if (this.currentDistance >= length) {
               return false;
            }

            if (!this.center) {
               this.dynamicRadius = this.dynamicRadius + this.radiusDiv2;
               if (this.dynamicRadius >= radius) {
                  this.center = true;
               }
            } else if (fadeUp) {
               this.dynamicRadius = this.dynamicRadius - this.radiusDiv2;
            }

            for (double i = 0.0; i < strings; i++) {
               double angle = i * this.distanceBetweenEachCirclePoints * extension + this.rotation;
               double x = this.dynamicRadius * Math.cos(angle);
               double z = this.dynamicRadius * Math.sin(angle);
               display.spawn(x, 0.0, z);
            }

            this.currentDistance = this.currentDistance + speed;
            if (this.currentDistance < length) {
               display.advanceInDirection(speed);
            } else {
               display.advanceInDirection(speed - (this.currentDistance - length));
            }

            this.rotation = this.rotation + this.calculatedRotRate;
            return true;
         }
      };
   }

   public static BukkitTask helix(
      Plugin plugin,
      int strings,
      double radius,
      double rate,
      double extension,
      double height,
      double speed,
      double rotationRate,
      boolean fadeUp,
      boolean fadeDown,
      ParticleDisplay display
   ) {
      final BooleanSupplier helix = helix(strings, radius, rate, extension, height, speed, rotationRate, fadeUp, fadeDown, display);
      return (new BukkitRunnable() {
         public void run() {
            if (!helix.getAsBoolean()) {
               this.cancel();
            }
         }
      }).runTaskTimerAsynchronously(plugin, 0L, 1L);
   }

   public static void lightning(
      Location start,
      Vector direction,
      int entries,
      int branches,
      double radius,
      double offset,
      double offsetRate,
      double length,
      double lengthRate,
      double branch,
      double branchRate,
      ParticleDisplay display
   ) {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      if (entries > 0) {
         boolean inRange = true;

         while (random.nextDouble() < branch || inRange) {
            Vector randomizer = new Vector(random.nextDouble(-radius, radius), random.nextDouble(-radius, radius), random.nextDouble(-radius, radius))
               .normalize()
               .multiply(random.nextDouble(-radius, radius) * offset);
            Vector endVector = start.clone().toVector().add(direction.clone().multiply(length)).add(randomizer);
            Location end = endVector.toLocation(start.getWorld());
            if (end.distance(start) <= length) {
               inRange = true;
            } else {
               inRange = false;
               int rate = (int)(start.distance(end) / 0.1);
               Vector rateDir = endVector.clone().subtract(start.toVector()).normalize().multiply(0.1);

               for (int i = 0; i < rate; i++) {
                  Location loc = start.clone().add(rateDir.clone().multiply(i));
                  display.spawn(loc);
               }

               lightning(
                  end.clone(),
                  direction,
                  entries - 1,
                  branches - 1,
                  radius,
                  offset * offsetRate,
                  offsetRate,
                  length * lengthRate,
                  lengthRate,
                  branch * branchRate,
                  branchRate,
                  display
               );
               if (branches <= 0) {
                  break;
               }
            }
         }
      }
   }

   public static void dna(
      double radius, double rate, double extension, int height, int hydrogenBondDist, ParticleDisplay display, ParticleDisplay hydrogenBondDisplay
   ) {
      int nucleotideDist = 0;
      double y = 0.0;

      while (y <= height) {
         nucleotideDist++;
         double x = radius * Math.cos(extension * y);
         double z = radius * Math.sin(extension * y);
         Location nucleotide1 = display.getLocation().clone().add(x, y, z);
         display.spawn(x, y, z);
         Location nucleotide2 = display.getLocation().clone().subtract(x, -y, z);
         display.spawn(-x, y, -z);
         if (nucleotideDist >= hydrogenBondDist) {
            nucleotideDist = 0;
            line(nucleotide1, nucleotide2, rate * 2.0, hydrogenBondDisplay);
         }

         y += rate;
      }
   }

   public static BooleanSupplier dnaReplication(
      final double radius,
      final double rate,
      final int speed,
      final double extension,
      final int height,
      final int hydrogenBondDist,
      final ParticleDisplay display
   ) {
      final ParticleDisplay adenine = ParticleDisplay.of(XParticle.DUST).withColor(java.awt.Color.BLUE, 1.0F);
      final ParticleDisplay thymine = ParticleDisplay.of(XParticle.DUST).withColor(java.awt.Color.YELLOW, 1.0F);
      final ParticleDisplay guanine = ParticleDisplay.of(XParticle.DUST).withColor(java.awt.Color.GREEN, 1.0F);
      final ParticleDisplay cytosine = ParticleDisplay.of(XParticle.DUST).withColor(java.awt.Color.RED, 1.0F);
      return new BooleanSupplier() {
         double y = 0.0;
         int nucleotideDist = 0;
         boolean done = false;

         @Override
         public boolean getAsBoolean() {
            if (this.done) {
               return false;
            }

            int repeat = speed;

            while (repeat-- != 0) {
               this.y = this.y + rate;
               this.nucleotideDist++;
               double x = radius * Math.cos(extension * this.y);
               double z = radius * Math.sin(extension * this.y);
               Location nucleotide1 = display.getLocation().clone().add(x, this.y, z);
               Particles.circle(0.1, 10.0, display.cloneWithLocation(x, this.y, z));
               Location nucleotide2 = display.getLocation().clone().subtract(x, -this.y, z);
               Particles.circle(0.1, 10.0, display.cloneWithLocation(-x, this.y, -z));
               Location midPointBond = nucleotide1.toVector().midpoint(nucleotide2.toVector()).toLocation(nucleotide1.getWorld());
               if (this.nucleotideDist >= hydrogenBondDist) {
                  this.nucleotideDist = 0;
                  if (Particles.randInt(0, 1) == 1) {
                     Particles.line(nucleotide1, midPointBond, rate - 0.1, adenine);
                     Particles.line(nucleotide2, midPointBond, rate - 0.1, thymine);
                  } else {
                     Particles.line(nucleotide1, midPointBond, rate - 0.1, cytosine);
                     Particles.line(nucleotide2, midPointBond, rate - 0.1, guanine);
                  }
               }

               if (this.y > height) {
                  this.done = true;
                  return false;
               }
            }

            return true;
         }
      };
   }

   public static BukkitTask dnaReplication(
      Plugin plugin, double radius, double rate, int speed, double extension, int height, int hydrogenBondDist, ParticleDisplay display
   ) {
      final BooleanSupplier dnaReplication = dnaReplication(radius, rate, speed, extension, height, hydrogenBondDist, display);
      return (new BukkitRunnable() {
         public void run() {
            if (!dnaReplication.getAsBoolean()) {
               this.cancel();
            }
         }
      }).runTaskTimerAsynchronously(plugin, 0L, 1L);
   }

   public static void drawLine(Player player, double length, double rate, ParticleDisplay display) {
      Location eye = player.getEyeLocation();
      line(eye, eye.clone().add(eye.getDirection().multiply(length)), rate, display);
   }

   public static Runnable cloud(ParticleDisplay cloud, ParticleDisplay rain) {
      return () -> {
         cloud.spawn();
         rain.spawn();
      };
   }

   public static BukkitTask cloud(Plugin plugin, ParticleDisplay cloud, ParticleDisplay rain) {
      return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, cloud(cloud, rain), 0L, 1L);
   }

   public static void line(Location start, Location end, double rate, ParticleDisplay display) {
      rate = Math.abs(rate);
      double x = end.getX() - start.getX();
      double y = end.getY() - start.getY();
      double z = end.getZ() - start.getZ();
      double length = Math.sqrt(NumberConversions.square(x) + NumberConversions.square(y) + NumberConversions.square(z));
      x /= length;
      y /= length;
      z /= length;
      ParticleDisplay clone = display.copy();
      clone.withLocation(start);

      for (double i = 0.0; i < length; i += rate) {
         if (i > length) {
            i = length;
         }

         clone.spawn(x * i, y * i, z * i);
      }
   }

   public static void rectangle(Location start, Location end, double rate, ParticleDisplay display) {
      display.withLocation(start);
      double maxX = Math.max(start.getX(), end.getX());
      double minX = Math.min(start.getX(), end.getX());
      double maxY = Math.max(start.getY(), end.getY());
      double minY = Math.min(start.getY(), end.getY());
      double x = minX;

      while (x <= maxX) {
         for (double y = minY; y <= maxY; y += rate) {
            display.spawn(x - minX, y - minY, 0.0);
         }

         x += rate;
      }
   }

   public static void cage(Location start, Location end, double rate, double barRate, ParticleDisplay display) {
      double maxX = Math.max(start.getX(), end.getX());
      double minX = Math.min(start.getX(), end.getX());
      double maxZ = Math.max(start.getZ(), end.getZ());
      double minZ = Math.min(start.getZ(), end.getZ());
      double barChance = 0.0;
      double x = minX;

      while (x <= maxX) {
         for (double z = minZ; z <= maxZ; z += rate) {
            Location barStart = display.spawn(x - minX, 0.0, z - minZ);
            Location barEnd = display.spawn(x - minX, 3.0, z - minZ);
            if (x == minX || x + rate > maxX || z == minZ || z + rate > maxZ) {
               if (++barChance >= barRate) {
                  barChance = 0.0;
                  line(barStart, barEnd, rate, display);
               }
            }
         }

         x += rate;
      }
   }

   public static void filledCube(Location start, Location end, double rate, ParticleDisplay display) {
      display.withLocation(start);
      double maxX = Math.max(start.getX(), end.getX());
      double minX = Math.min(start.getX(), end.getX());
      double maxY = Math.max(start.getY(), end.getY());
      double minY = Math.min(start.getY(), end.getY());
      double maxZ = Math.max(start.getZ(), end.getZ());
      double minZ = Math.min(start.getZ(), end.getZ());
      double x = minX;

      while (x <= maxX) {
         for (double y = minY; y <= maxY; y += rate) {
            for (double z = minZ; z <= maxZ; z += rate) {
               display.spawn(x - minX, y - minY, z - minZ);
            }
         }

         x += rate;
      }
   }

   public static void cube(Location start, Location end, double rate, ParticleDisplay display) {
      display.withLocation(start);
      double maxX = Math.max(start.getX(), end.getX());
      double minX = Math.min(start.getX(), end.getX());
      double maxY = Math.max(start.getY(), end.getY());
      double minY = Math.min(start.getY(), end.getY());
      double maxZ = Math.max(start.getZ(), end.getZ());
      double minZ = Math.min(start.getZ(), end.getZ());
      double x = minX;

      while (x <= maxX) {
         for (double y = minY; y <= maxY; y += rate) {
            for (double z = minZ; z <= maxZ; z += rate) {
               if (y == minY || y + rate > maxY || x == minX || x + rate > maxX || z == minZ || z + rate > maxZ) {
                  display.spawn(x - minX, y - minY, z - minZ);
               }
            }
         }

         x += rate;
      }
   }

   public static void structuredCube(Location start, Location end, double rate, ParticleDisplay display) {
      display.withLocation(start);
      double maxX = Math.max(start.getX(), end.getX());
      double minX = Math.min(start.getX(), end.getX());
      double maxY = Math.max(start.getY(), end.getY());
      double minY = Math.min(start.getY(), end.getY());
      double maxZ = Math.max(start.getZ(), end.getZ());
      double minZ = Math.min(start.getZ(), end.getZ());
      double x = minX;

      while (x <= maxX) {
         for (double y = minY; y <= maxY; y += rate) {
            for (double z = minZ; z <= maxZ; z += rate) {
               int components = 0;
               if (x == minX || x + rate > maxX) {
                  components++;
               }

               if (y == minY || y + rate > maxY) {
                  components++;
               }

               if (z == minZ || z + rate > maxZ) {
                  components++;
               }

               if (components >= 2) {
                  display.spawn(x - minX, y - minY, z - minZ);
               }
            }
         }

         x += rate;
      }
   }

   public static void hypercube(Location startOrigin, Location endOrigin, double rate, double sizeRate, int cubes, ParticleDisplay display) {
      List<Location> previousPoints = null;

      for (int i = 0; i < cubes + 1; i++) {
         List<Location> points = new ArrayList<>(8);
         Location start = startOrigin.clone().subtract(i * sizeRate, i * sizeRate, i * sizeRate);
         Location end = endOrigin.clone().add(i * sizeRate, i * sizeRate, i * sizeRate);
         display.withLocation(start);
         double maxX = Math.max(start.getX(), end.getX());
         double minX = Math.min(start.getX(), end.getX());
         double maxY = Math.max(start.getY(), end.getY());
         double minY = Math.min(start.getY(), end.getY());
         double maxZ = Math.max(start.getZ(), end.getZ());
         double minZ = Math.min(start.getZ(), end.getZ());
         points.add(new Location(start.getWorld(), maxX, maxY, maxZ));
         points.add(new Location(start.getWorld(), minX, minY, minZ));
         points.add(new Location(start.getWorld(), maxX, minY, maxZ));
         points.add(new Location(start.getWorld(), minX, maxY, minZ));
         points.add(new Location(start.getWorld(), minX, minY, maxZ));
         points.add(new Location(start.getWorld(), maxX, minY, minZ));
         points.add(new Location(start.getWorld(), maxX, maxY, minZ));
         points.add(new Location(start.getWorld(), minX, maxY, maxZ));
         if (previousPoints != null) {
            for (int p = 0; p < 8; p++) {
               Location current = points.get(p);
               Location previous = previousPoints.get(p);
               line(previous, current, rate, display);
            }
         }

         previousPoints = points;

         for (double x = minX; x <= maxX; x += rate) {
            for (double y = minY; y <= maxY; y += rate) {
               for (double z = minZ; z <= maxZ; z += rate) {
                  int components = 0;
                  if (x == minX || x + rate > maxX) {
                     components++;
                  }

                  if (y == minY || y + rate > maxY) {
                     components++;
                  }

                  if (z == minZ || z + rate > maxZ) {
                     components++;
                  }

                  if (components >= 2) {
                     display.spawn(x - minX, y - minY, z - minZ);
                  }
               }
            }
         }
      }
   }

   public static BooleanSupplier tesseract(final double size, final double rate, final double speed, final long ticks, final ParticleDisplay display) {
      final double[][] positions = new double[][]{
         {-1.0, -1.0, -1.0, 1.0},
         {1.0, -1.0, -1.0, 1.0},
         {1.0, 1.0, -1.0, 1.0},
         {-1.0, 1.0, -1.0, 1.0},
         {-1.0, -1.0, 1.0, 1.0},
         {1.0, -1.0, 1.0, 1.0},
         {1.0, 1.0, 1.0, 1.0},
         {-1.0, 1.0, 1.0, 1.0},
         {-1.0, -1.0, -1.0, -1.0},
         {1.0, -1.0, -1.0, -1.0},
         {1.0, 1.0, -1.0, -1.0},
         {-1.0, 1.0, -1.0, -1.0},
         {-1.0, -1.0, 1.0, -1.0},
         {1.0, -1.0, 1.0, -1.0},
         {1.0, 1.0, 1.0, -1.0},
         {-1.0, 1.0, 1.0, -1.0}
      };
      final List<int[]> connections = new ArrayList<>();
      int level = 1;

      for (int h = 0; h <= level; h++) {
         int start = 8 * h;

         for (int i = start; i < start + 4; i++) {
            connections.add(new int[]{i, (i + 1) % 4 + start});
            connections.add(new int[]{i + 4, (i + 1) % 4 + 4 + start});
            connections.add(new int[]{i, i + 4});
         }
      }

      for (int i = 0; i < (level + 1) * 4; i++) {
         connections.add(new int[]{i, i + 8});
      }

      return new BooleanSupplier() {
         double angle = 0.0;
         long repeat = 0L;
         boolean done = false;

         @Override
         public boolean getAsBoolean() {
            if (this.done) {
               return false;
            }

            double cos = Math.cos(this.angle);
            double sin = Math.sin(this.angle);
            double[][] rotationXY = new double[][]{{cos, -sin, 0.0, 0.0}, {sin, cos, 0.0, 0.0}, {0.0, 0.0, 1.0, 0.0}, {0.0, 0.0, 0.0, 1.0}};
            double[][] rotationZW = new double[][]{{1.0, 0.0, 0.0, 0.0}, {0.0, 1.0, 0.0, 0.0}, {0.0, 0.0, cos, -sin}, {0.0, 0.0, sin, cos}};
            double[][] projected3D = new double[positions.length][4];

            for (int i = 0; i < positions.length; i++) {
               double[] point = positions[i];
               double[] rotated = Particles.matrix(rotationXY, point);
               rotated = Particles.matrix(rotationZW, rotated);
               int distance = 2;
               double w = 1.0 / (distance - rotated[3]);
               double[][] projection = new double[][]{{w, 0.0, 0.0, 0.0}, {0.0, w, 0.0, 0.0}, {0.0, 0.0, w, 0.0}};
               double[] projected = Particles.matrix(projection, rotated);

               for (int proj = 0; proj < projected.length; proj++) {
                  projected[proj] *= size;
               }

               projected3D[i] = projected;
               display.spawn(projected[0], projected[1], projected[2]);
            }

            for (int[] connection : connections) {
               double[] pointA = projected3D[connection[0]];
               double[] pointB = projected3D[connection[1]];
               Location start = display.cloneLocation(pointA[0], pointA[1], pointA[2]);
               Location end = display.cloneLocation(pointB[0], pointB[1], pointB[2]);
               Particles.line(start, end, rate, display);
            }

            if (++this.repeat > ticks) {
               this.done = true;
               return false;
            } else {
               this.angle = this.angle + speed;
               return true;
            }
         }
      };
   }

   public static BukkitTask tesseract(Plugin plugin, double size, double rate, double speed, long ticks, ParticleDisplay display) {
      final BooleanSupplier tesseract = tesseract(size, rate, speed, ticks, display);
      return (new BukkitRunnable() {
         public void run() {
            if (!tesseract.getAsBoolean()) {
               this.cancel();
            }
         }
      }).runTaskTimerAsynchronously(plugin, 0L, 1L);
   }

   private static double[] matrix(double[][] a, double[] m) {
      double[][] b = new double[4][1];
      b[0][0] = m[0];
      b[1][0] = m[1];
      b[2][0] = m[2];
      b[3][0] = m[3];
      int colsA = a[0].length;
      int rowsA = a.length;
      int colsB = b[0].length;
      int rowsB = b.length;
      double[][] result = new double[rowsA][rowsB];

      for (int i = 0; i < rowsA; i++) {
         for (int j = 0; j < colsB; j++) {
            float sum = 0.0F;

            for (int k = 0; k < colsA; k++) {
               sum += (float)(a[i][k] * b[k][j]);
            }

            result[i][j] = sum;
         }
      }

      double[] v = new double[]{result[0][0], result[1][0], result[2][0], 0.0};
      if (result.length > 3) {
         v[3] = result[3][0];
      }

      return v;
   }

   public static void mandelbrot(double size, double zoom, double rate, double x0, double y0, int color, ParticleDisplay display) {
      double y = -size;

      while (y < size) {
         for (double x = -size; x < size; x += rate) {
            double zy = 0.0;
            double zx = 0.0;
            double cX = (x - x0) / zoom;
            double cY = (y - y0) / zoom;

            int iteration;
            for (iteration = color; zx * zx + zy * zy <= 4.0 && iteration > 0; iteration--) {
               double xtemp = zx * zx - zy * zy + cX;
               zy = 2.0 * zx * zy + cY;
               zx = xtemp;
            }

            if (iteration == 0) {
               display.spawn(x, y, 0.0);
            }
         }

         y += rate;
      }
   }

   public static void julia(double size, double zoom, int colorScheme, double moveX, double moveY, ParticleDisplay display) {
      double cx = -0.7;
      double cy = 0.27015;

      for (double x = -size; x < size; x += 0.1) {
         for (double y = -size; y < size; y += 0.1) {
            double zx = 1.5 * (size - size / 2.0) / (0.5 * zoom * size) + moveX;
            double zy = (y - size / 2.0) / (0.5 * zoom * size) + moveY;

            int i;
            for (i = colorScheme; zx * zx + zy * zy < 4.0 && i > 0; i--) {
               double xtemp = zx * zx - zy * zy + cx;
               zy = 2.0 * zx * zy + cy;
               zx = xtemp;
            }

            java.awt.Color color = new java.awt.Color((i << 21) + (i << 10) + i * 8);
            display.withColor(color, 0.8F).spawn(x, y, 0.0);
         }
      }
   }

   public static List<BooleanSupplier> star(
      final int points,
      int spikes,
      double rate,
      final double spikeLength,
      final double coreRadius,
      final double neuron,
      final boolean prototype,
      final int speed,
      final ParticleDisplay display
   ) {
      final double pointsRate = (Math.PI * 2) / points;
      final double rateDiv = Math.PI / rate;
      final ThreadLocalRandom random = prototype ? null : ThreadLocalRandom.current();
      List<BooleanSupplier> tasks = new ArrayList<>();

      for (int i = 0; i < spikes * 2; i++) {
         final double spikeAngle = i * Math.PI / spikes;
         tasks.add(new BooleanSupplier() {
            double vein = 0.0;
            double theta = 0.0;
            boolean done = false;

            @Override
            public boolean getAsBoolean() {
               if (this.done) {
                  return false;
               }

               int repeat = speed;

               while (repeat-- != 0) {
                  this.theta = this.theta + rateDiv;
                  double height = (prototype ? this.vein : random.nextDouble(0.0, neuron)) * spikeLength;
                  if (prototype) {
                     this.vein = this.vein + neuron;
                  }

                  Vector vector = new Vector(Math.cos(this.theta), 0.0, Math.sin(this.theta));
                  vector.multiply((spikeLength - height) * coreRadius / spikeLength);
                  vector.setY(coreRadius + height);
                  ParticleDisplay.rotateAround(vector, ParticleDisplay.Axis.X, spikeAngle);

                  for (int j = 0; j < points; j++) {
                     ParticleDisplay.rotateAround(vector, ParticleDisplay.Axis.Y, pointsRate);
                     display.spawn(vector);
                  }
               }

               if (this.theta >= Math.PI * 2) {
                  this.done = true;
                  return false;
               } else {
                  return true;
               }
            }
         });
      }

      return tasks;
   }

   public static List<BukkitTask> star(
      Plugin plugin,
      int points,
      int spikes,
      double rate,
      double spikeLength,
      double coreRadius,
      double neuron,
      boolean prototype,
      int speed,
      ParticleDisplay display
   ) {
      List<BukkitTask> tasks = new ArrayList<>();

      for (final BooleanSupplier task : star(points, spikes, rate, spikeLength, coreRadius, neuron, prototype, speed, display)) {
         tasks.add((new BukkitRunnable() {
            public void run() {
               if (!task.getAsBoolean()) {
                  this.cancel();
               }
            }
         }).runTaskTimerAsynchronously(plugin, 0L, 1L));
      }

      return tasks;
   }

   public static void eye(double radius, double radius2, double rate, double extension, ParticleDisplay display) {
      double rateDiv = Math.PI / rate;
      double limit = Math.PI / extension;
      double x = 0.0;

      for (double i = 0.0; i < limit; i += rateDiv) {
         double y = radius * Math.sin(extension * i);
         double y2 = radius2 * Math.sin(extension * -i);
         display.spawn(x, y, 0.0);
         display.spawn(x, y2, 0.0);
         x += 0.1;
      }
   }

   public static void illuminati(double size, double extension, ParticleDisplay display) {
      polygon(3, 1, size, 1.0 / (size * 30.0), 0.0, display);
      eye(size / 4.0, size / 4.0, 30.0, extension, display.cloneWithLocation(0.3, 0.0, size / 1.8).rotate(Math.PI / 2, Math.PI / 2, 0.0));
      circle(size / 5.0, size * 5.0, display.cloneWithLocation(0.3, 0.0, 0.0));
   }

   public static void polygon(int points, int connection, double size, double rate, double extend, ParticleDisplay display) {
      for (int point = 0; point < points; point++) {
         double angle = Math.toRadians(360.0 / points * point);
         double nextAngle = Math.toRadians(360.0 / points * (point + connection));
         double x = Math.cos(angle) * size;
         double z = Math.sin(angle) * size;
         double x2 = Math.cos(nextAngle) * size;
         double z2 = Math.sin(nextAngle) * size;
         double deltaX = x2 - x;
         double deltaZ = z2 - z;
         double pos = 0.0;

         while (pos < 1.0 + extend) {
            double x1 = x + deltaX * pos;
            double z1 = z + deltaZ * pos;
            display.spawn(x1, 0.0, z1);
            pos += rate;
         }
      }
   }

   public static void neopaganPentagram(double size, double rate, double extend, ParticleDisplay star, ParticleDisplay circle) {
      polygon(5, 2, size, rate, extend, star);
      circle(size + 0.5, rate * 1000.0, circle);
   }

   public static void atom(int orbits, double radius, double rate, ParticleDisplay orbit, ParticleDisplay nucleus) {
      double dist = Math.PI / orbits;

      for (double angle = 0.0; orbits > 0; angle += dist) {
         orbit.rotate(ParticleDisplay.Rotation.of(angle, ParticleDisplay.Axis.Z));
         circle(radius, rate, orbit);
         orbits--;
      }

      sphere(radius / 3.0, rate / 2.0, nucleus);
   }

   public static BooleanSupplier meguminExplosion(final double size, final ParticleDisplay display) {
      final BooleanSupplier spread = spread(30, 2, display.getLocation(), display.getLocation().clone().add(0.0, 10.0, 0.0), 5.0, 5.0, 5.0, display);
      return new BooleanSupplier() {
         boolean first = true;

         @Override
         public boolean getAsBoolean() {
            if (this.first) {
               this.first = false;
               Particles.polygon(10, 4, size, 0.02, 0.3, display);
               Particles.polygon(10, 3, size / (size - 1.0), 0.5, 0.0, display);
               Particles.circle(size, 40.0, display);
            }

            return spread.getAsBoolean();
         }
      };
   }

   public static BukkitTask meguminExplosion(Plugin plugin, double size, ParticleDisplay display) {
      final BooleanSupplier explosion = meguminExplosion(size, display);
      return (new BukkitRunnable() {
         public void run() {
            if (!explosion.getAsBoolean()) {
               this.cancel();
            }
         }
      }).runTaskTimerAsynchronously(plugin, 0L, 1L);
   }

   public static BooleanSupplier explosionWave(final double rate, final ParticleDisplay display, final ParticleDisplay secDisplay) {
      return new BooleanSupplier() {
         static final double addition = Math.PI / 10;
         final double rateDiv = Math.PI / rate;
         double times = Math.PI / 4;
         boolean done = false;

         @Override
         public boolean getAsBoolean() {
            if (this.done) {
               return false;
            }

            this.times += Math.PI / 10;
            double theta = 0.0;

            while (theta <= Math.PI * 2) {
               double x = this.times * Math.cos(theta);
               double y = 2.0 * Math.exp(-0.1 * this.times) * Math.sin(this.times) + 1.5;
               double z = this.times * Math.sin(theta);
               display.spawn(x, y, z);
               theta += 0.04908738521234052;
               x = this.times * Math.cos(theta);
               z = this.times * Math.sin(theta);
               secDisplay.spawn(x, y, z);
               theta += this.rateDiv;
            }

            if (this.times > 20.0) {
               this.done = true;
               return false;
            } else {
               return true;
            }
         }
      };
   }

   public static BukkitTask explosionWave(Plugin plugin, double rate, ParticleDisplay display, ParticleDisplay secDisplay) {
      final BooleanSupplier explosionWave = explosionWave(rate, display, secDisplay);
      return (new BukkitRunnable() {
         public void run() {
            if (!explosionWave.getAsBoolean()) {
               this.cancel();
            }
         }
      }).runTaskTimerAsynchronously(plugin, 0L, 1L);
   }

   private static BufferedImage getImage(Path path) {
      if (!Files.exists(path)) {
         return null;
      }

      try {
         return ImageIO.read(Files.newInputStream(path, StandardOpenOption.READ));
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }

   private static CompletableFuture<BufferedImage> getScaledImage(Path path, int width, int height) {
      return CompletableFuture.supplyAsync(() -> {
         BufferedImage image = getImage(path);
         if (image == null) {
            return null;
         }

         int finalHeight = height;
         int finalWidth = width;
         if (image.getWidth() > image.getHeight()) {
            finalHeight = width * image.getHeight() / image.getWidth();
         } else {
            finalWidth = height * image.getWidth() / image.getHeight();
         }

         BufferedImage resizedImg = new BufferedImage(width, height, 2);
         Graphics2D graphics = resizedImg.createGraphics();
         graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         graphics.drawImage(image, 0, 0, finalWidth, finalHeight, null);
         graphics.dispose();
         return resizedImg;
      });
   }

   public static CompletableFuture<Map<double[], Color>> renderImage(Path path, int resizedWidth, int resizedHeight, double compact) {
      return getScaledImage(path, resizedWidth, resizedHeight).thenCompose(image -> renderImage(image, resizedWidth, resizedHeight, compact));
   }

   public static CompletableFuture<Map<double[], Color>> renderImage(BufferedImage image, int resizedWidth, int resizedHeight, double compact) {
      return CompletableFuture.supplyAsync(() -> {
         if (image == null) {
            return null;
         }

         int width = image.getWidth();
         int height = image.getHeight();
         double centerX = width / 2.0;
         double centerY = height / 2.0;
         Map<double[], Color> rendered = new HashMap<>();

         for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
               int pixel = image.getRGB(x, y);
               if (pixel >> 24 != 0) {
                  java.awt.Color color = new java.awt.Color(pixel);
                  int r = color.getRed();
                  int g = color.getGreen();
                  int b = color.getBlue();
                  double[] coords = new double[]{(x - centerX) * compact, (y - centerY) * compact};
                  Color bukkitColor = Color.fromRGB(r, g, b);
                  rendered.put(coords, bukkitColor);
               }
            }
         }

         return rendered;
      });
   }

   public static BooleanSupplier displayRenderedImage(
      final Map<double[], Color> render, final Callable<Location> location, final int repeat, final int quality, final int speed, final float size
   ) {
      return new BooleanSupplier() {
         int times = repeat;
         boolean done = false;

         @Override
         public boolean getAsBoolean() {
            if (this.done) {
               return false;
            }

            try {
               Particles.displayRenderedImage(render, location.call(), quality, speed, size);
            } catch (Exception e) {
               e.printStackTrace();
            }

            if (this.times-- <= 0) {
               this.done = true;
               return false;
            } else {
               return true;
            }
         }
      };
   }

   public static BukkitTask displayRenderedImage(
      Plugin plugin, Map<double[], Color> render, Callable<Location> location, int repeat, long period, int quality, int speed, float size
   ) {
      final BooleanSupplier displayRenderedImage = displayRenderedImage(render, location, repeat, quality, speed, size);
      return (new BukkitRunnable() {
         public void run() {
            if (!displayRenderedImage.getAsBoolean()) {
               this.cancel();
            }
         }
      }).runTaskTimerAsynchronously(plugin, 0L, period);
   }

   public static void displayRenderedImage(Map<double[], Color> render, Location location, int quality, int speed, float size) {
      World world = location.getWorld();
      double rotation = location.getYaw();
      BlockFace facing;
      if (rotation >= 135.0 || rotation < -135.0) {
         facing = BlockFace.NORTH;
      } else if (rotation >= -135.0 && rotation < -45.0) {
         facing = BlockFace.EAST;
      } else if (rotation >= -45.0 && rotation < 45.0) {
         facing = BlockFace.SOUTH;
      } else {
         if (!(rotation >= 45.0) || !(rotation < 135.0)) {
            throw new IllegalArgumentException("Unknown rotation yaw: " + rotation);
         }

         facing = BlockFace.WEST;
      }

      for (Entry<double[], Color> pixel : render.entrySet()) {
         DustOptions data = new DustOptions(pixel.getValue(), size);
         double[] pixelLoc = pixel.getKey();
         double x;
         double y;
         double z;
         switch (facing) {
            case NORTH:
               x = location.getX() + pixelLoc[0];
               y = location.getY() - pixelLoc[1];
               z = location.getZ();
               break;
            case EAST:
               x = location.getX();
               y = location.getY() - pixelLoc[1];
               z = location.getZ() + pixelLoc[0];
               break;
            case SOUTH:
               x = location.getX() - pixelLoc[0];
               y = location.getY() - pixelLoc[1];
               z = location.getZ();
               break;
            case WEST:
               x = location.getX();
               y = location.getY() - pixelLoc[1];
               z = location.getZ() - pixelLoc[0];
               break;
            default:
               throw new AssertionError("Invalid facing: " + facing);
         }

         Location loc = new Location(world, x, y, z);
         world.spawnParticle(XParticle.DUST.get(), loc, quality, 0.0, 0.0, 0.0, speed, data);
      }
   }

   public static void saveImage(BufferedImage image, Path path) {
      try {
         ImageIO.write(image, "png", Files.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static CompletableFuture<BufferedImage> stringToImage(Font font, java.awt.Color color, String str) {
      return CompletableFuture.supplyAsync(() -> {
         BufferedImage image = new BufferedImage(1, 1, 2);
         Graphics2D graphics = image.createGraphics();
         graphics.setFont(font);
         FontRenderContext context = graphics.getFontMetrics().getFontRenderContext();
         Rectangle2D frame = font.getStringBounds(str, context);
         graphics.dispose();
         image = new BufferedImage((int)Math.ceil(frame.getWidth()), (int)Math.ceil(frame.getHeight()), 2);
         graphics = image.createGraphics();
         graphics.setColor(color);
         graphics.setFont(font);
         graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
         graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
         graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
         graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
         graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
         FontMetrics metrics = graphics.getFontMetrics();
         graphics.drawString(str, 0, metrics.getAscent());
         graphics.dispose();
         return image;
      });
   }
}
