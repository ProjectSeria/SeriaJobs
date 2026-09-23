package id.seria.gacha.lib.xseries;

import id.seria.gacha.lib.xseries.reflection.XReflection;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftClassHandle;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftConnection;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftMapping;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftPackage;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class XWorldBorder {
   public static final int ABSOLUTE_MAX_SIZE = 29999984;
   public static final double MAX_SIZE = 5.999997E7F;
   public static final double MAX_CENTER_COORDINATE = 2.9999984E7;
   private static final boolean SUPPORTS_NATIVE_WORLDBORDERS = XReflection.of(Player.class)
      .method()
      .named("setWorldBorder")
      .returns(void.class)
      .parameters(WorldBorder.class)
      .exists();
   protected XWorldBorder.BorderBounds borderBounds;

   public XWorldBorder() {
   }

   public abstract double getDamageBuffer();

   public abstract double getSizeLerpTarget();

   public abstract double getSize();

   public abstract boolean isWithinBorder(Location var1);

   public abstract int getWarningDistance();

   public abstract Duration getWarningTime();

   public abstract Location getCenter();

   public abstract void setFor(Collection<Player> var1, boolean var2);

   public final XWorldBorder.BorderBounds getBorderBounds() {
      return this.borderBounds;
   }

   public abstract XWorldBorder copy();

   public abstract XWorldBorder setDamageBuffer(double var1);

   public abstract XWorldBorder setWarningTime(Duration var1);

   public abstract XWorldBorder setWarningDistance(int var1);

   public XWorldBorder setSize(double newSize) {
      return this.setSize(newSize, Duration.ZERO);
   }

   public XWorldBorder setSize(double newSize, @NotNull Duration duration) {
      Objects.requireNonNull(duration, "Size change duration cannot be null");
      if (newSize > 5.999997E7F) {
         throw new IllegalArgumentException("Border size is bigger than max border size: " + newSize + " > " + 5.999997E7F);
      } else {
         return this;
      }
   }

   public XWorldBorder setSizeLerpTarget(double sizeLerpTarget) {
      if (sizeLerpTarget > 5.999997E7F) {
         throw new IllegalArgumentException("Size lerp target size is bigger than max border size: " + sizeLerpTarget + " > " + 5.999997E7F);
      } else {
         return this;
      }
   }

   public abstract XWorldBorder setCenter(Location var1);

   public XWorldBorder setCenter(double x, double z) {
      if (!Double.isNaN(x) && !Double.isNaN(z)) {
         return this;
      } else {
         throw new IllegalArgumentException("Invalid coordinates: " + x + ", " + z);
      }
   }

   public XWorldBorder update(Player... players) {
      if (players == null) {
         throw new IllegalArgumentException("Player array is null");
      } else {
         return this;
      }
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName()
         + "(size: "
         + this.getSize()
         + ", warningDistance: "
         + this.getWarningDistance()
         + ", warningTime: "
         + this.getWarningTime()
         + ", center: "
         + this.getCenter()
         + ", damageBuffer: "
         + this.getDamageBuffer()
         + ')';
   }

   public final double getDistanceToBorder(Location location) {
      if (this.borderBounds == null) {
         return this.getCenter().distanceSquared(location);
      }

      double x = location.getX();
      double z = location.getZ();
      double d2 = z - this.borderBounds.minZ;
      double d3 = this.borderBounds.maxZ - z;
      double d4 = x - this.borderBounds.minX;
      double d5 = this.borderBounds.maxX - x;
      double d6 = Math.min(d4, d5);
      d6 = Math.min(d6, d2);
      return Math.min(d6, d3);
   }

   protected void updateBorderBounds(Location center) {
      this.borderBounds = new XWorldBorder.BorderBounds(center.getWorld(), center.getX(), center.getZ(), this.getSize());
   }

   public static XWorldBorder create() {
      return SUPPORTS_NATIVE_WORLDBORDERS ? new XWorldBorder.BukkitWorldBorder(Bukkit.createWorldBorder()) : new XWorldBorder.NMSWorldBorder();
   }

   public static XWorldBorder getOrCreate(Player player) {
      XWorldBorder wb = get(player);
      if (wb != null) {
         return wb;
      }

      wb = create();
      wb.setFor(Collections.singleton(player), true);
      if (!SUPPORTS_NATIVE_WORLDBORDERS) {
         XWorldBorder.NMSWorldBorder.WORLD_BORDERS.put(player.getUniqueId(), wb);
      }

      return wb;
   }

   @Nullable
   public static XWorldBorder get(Player player) {
      if (SUPPORTS_NATIVE_WORLDBORDERS) {
         WorldBorder worldBorder = player.getWorldBorder();
         return worldBorder == null ? null : new XWorldBorder.BukkitWorldBorder(worldBorder);
      } else {
         return XWorldBorder.NMSWorldBorder.WORLD_BORDERS.get(player.getUniqueId());
      }
   }

   @Nullable
   public static XWorldBorder remove(Player player) {
      if (SUPPORTS_NATIVE_WORLDBORDERS) {
         WorldBorder worldBorder = player.getWorldBorder();
         if (worldBorder == null) {
            return null;
         }

         player.setWorldBorder(player.getWorld().getWorldBorder());
         return new XWorldBorder.BukkitWorldBorder(worldBorder);
      } else {
         return XWorldBorder.NMSWorldBorder.WORLD_BORDERS.remove(player.getUniqueId());
      }
   }

   public static XWorldBorder from(WorldBorder bukkitWb) {
      if (SUPPORTS_NATIVE_WORLDBORDERS) {
         return new XWorldBorder.BukkitWorldBorder(bukkitWb).copy();
      }

      XWorldBorder.NMSWorldBorder wb = new XWorldBorder.NMSWorldBorder();
      wb.world = bukkitWb.getCenter().getWorld();
      wb.centerX = bukkitWb.getCenter().getX();
      wb.centerZ = bukkitWb.getCenter().getZ();
      wb.size = bukkitWb.getSize();
      wb.sizeLerpTime = Duration.ZERO;
      wb.damagePerBlock = bukkitWb.getDamageAmount();
      wb.damageSafeZone = bukkitWb.getDamageBuffer();
      wb.warningTime = Duration.ofSeconds(bukkitWb.getWarningTime());
      wb.warningBlocks = bukkitWb.getWarningDistance();
      wb.handle = wb.createHandle();
      return wb;
   }

   public static final class BorderBounds {
      protected final World lastCenterWorld;
      protected final double lastCenterX;
      protected final double lastCenterZ;
      public final double minX;
      public final double minZ;
      public final double maxX;
      public final double maxZ;

      private static double clamp(double var0, double var2, double var4) {
         return var0 < var2 ? var2 : Math.min(var0, var4);
      }

      public boolean isCenterSame(World world, double centerX, double centerZ) {
         return this.lastCenterWorld == world && this.lastCenterX == centerX && this.lastCenterZ == centerZ;
      }

      public BorderBounds(World centerWorld, double centerX, double centerZ, double size) {
         this.lastCenterWorld = centerWorld;
         this.lastCenterX = centerX;
         this.lastCenterZ = centerZ;
         this.minX = clamp(centerX - size / 2.0, -2.9999984E7, 2.9999984E7);
         this.minZ = clamp(centerZ - size / 2.0, -2.9999984E7, 2.9999984E7);
         this.maxX = clamp(centerX + size / 2.0, -2.9999984E7, 2.9999984E7);
         this.maxZ = clamp(centerZ + size / 2.0, -2.9999984E7, 2.9999984E7);
      }
   }

   private static final class BukkitWorldBorder extends XWorldBorder {
      private final WorldBorder worldBorder;

      private BukkitWorldBorder(WorldBorder worldBorder) {
         this.worldBorder = worldBorder;
      }

      @Override
      public double getDamageBuffer() {
         return this.worldBorder.getDamageBuffer();
      }

      @Override
      public double getSizeLerpTarget() {
         return 0.0;
      }

      @Override
      public double getSize() {
         return this.worldBorder.getSize();
      }

      @Override
      public int getWarningDistance() {
         return this.worldBorder.getWarningDistance();
      }

      @Override
      public Duration getWarningTime() {
         return Duration.ofSeconds(this.worldBorder.getWarningTime());
      }

      @Override
      public XWorldBorder setDamageBuffer(double blocks) {
         this.worldBorder.setDamageBuffer(blocks);
         return this;
      }

      @Override
      public XWorldBorder setWarningDistance(int blocks) {
         this.worldBorder.setWarningDistance(blocks);
         return this;
      }

      @Override
      public XWorldBorder setWarningTime(Duration time) {
         this.worldBorder.setWarningTime((int)time.getSeconds());
         return this;
      }

      @Override
      public XWorldBorder setSize(double newSize, @NotNull Duration duration) {
         this.worldBorder.setSize(newSize, TimeUnit.MILLISECONDS, duration.toMillis());
         return this;
      }

      @Override
      public XWorldBorder setCenter(Location location) {
         this.worldBorder.setCenter(location);
         return this;
      }

      @Override
      public XWorldBorder setCenter(double x, double z) {
         this.worldBorder.setCenter(x, z);
         return this;
      }

      @Override
      public XWorldBorder update(Player... players) {
         return this;
      }

      @Override
      public XWorldBorder setSizeLerpTarget(double sizeLerpTarget) {
         this.worldBorder.setSize(sizeLerpTarget);
         return this;
      }

      @Override
      public void setFor(Collection<Player> players, boolean forceInit) {
         for (Player player : players) {
            player.setWorldBorder(this.worldBorder);
         }
      }

      @Override
      public Location getCenter() {
         Location center = this.worldBorder.getCenter();
         if (this.borderBounds == null || this.borderBounds.isCenterSame(center.getWorld(), center.getX(), center.getZ())) {
            this.updateBorderBounds(center);
         }

         return center;
      }

      @Override
      public boolean isWithinBorder(Location location) {
         return this.worldBorder.isInside(location);
      }

      @Override
      public XWorldBorder copy() {
         WorldBorder border = Bukkit.createWorldBorder();
         border.setCenter(this.worldBorder.getCenter());
         border.setSize(this.worldBorder.getSize());
         border.setDamageBuffer(this.worldBorder.getDamageBuffer());
         border.setDamageAmount(this.worldBorder.getDamageAmount());
         border.setWarningDistance(this.worldBorder.getWarningDistance());
         border.setWarningTime(this.worldBorder.getWarningTime());
         return new XWorldBorder.BukkitWorldBorder(border);
      }
   }

   public static final class Events implements Listener {
      public Events() {
      }

      @EventHandler
      public void onJoin(PlayerMoveEvent event) {
         XWorldBorder wb = XWorldBorder.get(event.getPlayer());
         if (wb != null) {
            Player p = event.getPlayer();
            Location loc = p.getLocation();
            if (!wb.isWithinBorder(loc)) {
               double distance = wb.getDistanceToBorder(loc);
               if (!(distance < wb.getDamageBuffer())) {
                  p.damage(wb.getDamageBuffer() * distance);
               }
            }
         }
      }

      @EventHandler
      public void onJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
         XWorldBorder wb = XWorldBorder.get(player);
         if (wb != null) {
            wb.setFor(Collections.singleton(player), true);
         }
      }

      @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
      public void onWorldChange(PlayerChangedWorldEvent event) {
         Player player = event.getPlayer();
         XWorldBorder wb = XWorldBorder.get(player);
         if (wb != null) {
            wb.setFor(Collections.singleton(player), true);
         }
      }
   }

   private static final class NMSWorldBorder extends XWorldBorder {
      private static final MethodHandle WORLD_HANDLE;
      private static final MethodHandle WORLDBORDER;
      private static final MethodHandle WORLDBORDER_WORLD;
      private static final MethodHandle CENTER;
      private static final MethodHandle WARNING_DISTANCE;
      private static final MethodHandle WARNING_TIME;
      private static final MethodHandle SIZE;
      private static final MethodHandle WorldBorder_lerpSizeBetween;
      private static final MethodHandle PACKET_WARNING_DISTANCE;
      private static final MethodHandle PACKET_WARNING_DELAY;
      private static final MethodHandle PACKET_LERP_SIZE;
      private static final MethodHandle PACKET_INIT;
      private static final MethodHandle PACKET_CENTER;
      private static final MethodHandle PACKET_SIZE;
      private static final Object INITIALIZE;
      private static final boolean SUPPORTS_SEPARATE_PACKETS;
      private static final Map<UUID, XWorldBorder> WORLD_BORDERS = new HashMap<>();
      private Object handle;
      private double damagePerBlock = 0.2;
      private double damageSafeZone = 5.0;
      private double size = 100.0;
      private double sizeLerpTarget = 0.0;
      private Duration warningTime = Duration.ofSeconds(15L);
      private Duration sizeLerpTime = Duration.ZERO;
      private int warningBlocks = 5;
      private World world;
      private double centerX;
      private double centerZ;
      private final Set<XWorldBorder.NMSWorldBorder.Component> updateRequired = EnumSet.noneOf(XWorldBorder.NMSWorldBorder.Component.class);
      private boolean init = true;

      private NMSWorldBorder() {
      }

      public XWorldBorder.NMSWorldBorder copy() {
         XWorldBorder.NMSWorldBorder wb = new XWorldBorder.NMSWorldBorder();
         wb.world = this.world;
         wb.centerX = this.centerX;
         wb.centerZ = this.centerZ;
         wb.size = this.size;
         wb.sizeLerpTime = this.sizeLerpTime;
         wb.damagePerBlock = this.damagePerBlock;
         wb.damageSafeZone = this.damageSafeZone;
         wb.warningTime = this.warningTime;
         wb.warningBlocks = this.warningBlocks;
         wb.handle = wb.createHandle();
         return wb;
      }

      public XWorldBorder setDamageAmount(double damage) {
         this.damagePerBlock = damage;
         return this;
      }

      @Override
      public double getSize() {
         return this.size;
      }

      public double getDamageAmount() {
         return this.damagePerBlock;
      }

      @Override
      public XWorldBorder setDamageBuffer(double blocks) {
         this.damageSafeZone = blocks;
         return this;
      }

      @Override
      public double getDamageBuffer() {
         return this.damageSafeZone;
      }

      @Override
      public XWorldBorder setWarningTime(Duration time) {
         if (this.warningTime == time) {
            return this;
         }

         this.warningTime = time;
         this.update(XWorldBorder.NMSWorldBorder.Component.WARNING_DELAY);
         return this;
      }

      @Override
      public Duration getWarningTime() {
         return this.warningTime;
      }

      @Override
      public XWorldBorder setWarningDistance(int blocks) {
         if (this.warningBlocks == blocks) {
            return this;
         }

         this.warningBlocks = blocks;
         this.update(XWorldBorder.NMSWorldBorder.Component.WARNING_DISTANCE);
         return this;
      }

      @Override
      public double getSizeLerpTarget() {
         return this.sizeLerpTarget;
      }

      @Override
      public XWorldBorder setSizeLerpTarget(double sizeLerpTarget) {
         super.setSizeLerpTarget(sizeLerpTarget);
         if (this.sizeLerpTarget == sizeLerpTarget) {
            return this;
         }

         this.sizeLerpTarget = sizeLerpTarget;
         this.update(XWorldBorder.NMSWorldBorder.Component.SIZE_LERP);
         return this;
      }

      @Override
      public XWorldBorder update(Player... players) {
         this.setFor(Arrays.asList(players), false);
         return this;
      }

      @Override
      public int getWarningDistance() {
         return this.warningBlocks;
      }

      @Override
      public XWorldBorder setCenter(Location location) {
         this.setCenter(location.getX(), location.getZ());
         this.world = location.getWorld();
         return this;
      }

      @Override
      public XWorldBorder setCenter(double x, double z) {
         super.setCenter(x, z);
         if (this.centerX == x && this.centerZ == z) {
            return this;
         }

         this.centerX = x;
         this.centerZ = z;
         this.updateBorderBounds(this.getCenter());
         this.update(XWorldBorder.NMSWorldBorder.Component.CENTER);
         return this;
      }

      @Override
      public Location getCenter() {
         return new Location(this.world, this.centerX, 0.0, this.centerZ);
      }

      @Override
      public XWorldBorder setSize(double newSize, @NotNull Duration duration) {
         super.setSize(newSize, duration);
         if (this.size == newSize && this.sizeLerpTime.equals(duration)) {
            return this;
         }

         this.size = newSize;
         this.sizeLerpTime = duration;
         this.updateBorderBounds(this.getCenter());
         this.update(XWorldBorder.NMSWorldBorder.Component.SIZE);
         if (!duration.isZero()) {
            this.update(XWorldBorder.NMSWorldBorder.Component.SIZE_LERP);
         }

         return this;
      }

      private void update(XWorldBorder.NMSWorldBorder.Component comp) {
         if (SUPPORTS_SEPARATE_PACKETS) {
            this.updateRequired.add(comp);
         }
      }

      @Override
      public boolean isWithinBorder(Location location) {
         if (this.borderBounds == null) {
            return false;
         } else {
            return this.world != null && this.world != location.getWorld()
               ? false
               : location.getX() + 1.0 > this.borderBounds.minX
                  && location.getX() < this.borderBounds.maxX
                  && location.getZ() + 1.0 > this.borderBounds.minZ
                  && location.getZ() < this.borderBounds.maxZ;
         }
      }

      @Override
      public void setFor(Collection<Player> players, boolean forceInit) {
         boolean init = forceInit || this.init;
         this.init = false;

         try {
            for (XWorldBorder.NMSWorldBorder.Component component : this.updateRequired) {
               component.setHandle(this);
            }

            Object[] packets;
            if (SUPPORTS_SEPARATE_PACKETS && !init) {
               packets = new Object[this.updateRequired.size()];
               int i = 0;

               for (XWorldBorder.NMSWorldBorder.Component component : this.updateRequired) {
                  packets[i++] = component.createPacket(this);
               }
            } else {
               Object packet = XReflection.supports(17)
                  ? (Object)PACKET_INIT.invoke((Object)this.handle)
                  : (Object)PACKET_INIT.invoke((Object)this.handle, (Object)INITIALIZE);
               packets = new Object[]{packet};
            }

            for (Player player : players) {
               MinecraftConnection.sendPacket(player, packets);
            }
         } catch (Throwable throwable) {
            throwable.printStackTrace();
         } finally {
            this.updateRequired.clear();
         }
      }

      private Object createHandle() {
         Objects.requireNonNull(this.world, "No world specified");

         try {
            Object worldBorder = (Object)WORLDBORDER.invoke();
            Object world = (Object)WORLD_HANDLE.invoke((World)this.world);
            WORLDBORDER_WORLD.invoke((Object)worldBorder, (Object)world);
            return worldBorder;
         } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
         }
      }

      static {
         if (!XWorldBorder.SUPPORTS_NATIVE_WORLDBORDERS) {
            Object initialize = null;
            MethodHandle packetInit = null;
            MethodHandle packetWarnDist = null;
            MethodHandle packetWarnDelay = null;
            MethodHandle packetLerpSize = null;
            MethodHandle packetCenter = null;
            MethodHandle packetSize = null;
            Lookup lookup = MethodHandles.lookup();
            MinecraftClassHandle wb = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "world.level.border").named("WorldBorder");
            MinecraftClassHandle worldServer = XReflection.ofMinecraft()
               .inPackage(MinecraftPackage.NMS, "server.level")
               .map(MinecraftMapping.MOJANG, "ServerLevel")
               .map(MinecraftMapping.SPIGOT, "WorldServer");
            MinecraftClassHandle craftWorld = XReflection.ofMinecraft().inPackage(MinecraftPackage.CB).named("CraftWorld");

            try {
               if (!XReflection.supports(17)) {
                  Class<?> wbType;
                  try {
                     wbType = Class.forName("EnumWorldBorderAction");
                  } catch (ClassNotFoundException e) {
                     wbType = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS).named("PacketPlayOutWorldBorder$EnumWorldBorderAction").unreflect();
                  }

                  packetInit = lookup.findConstructor(
                     XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS).named("PacketPlayOutWorldBorder").unreflect(),
                     MethodType.methodType(void.class, wb.reflect(), wbType)
                  );

                  for (Object type : wbType.getEnumConstants()) {
                     if (type.toString().equals("INITIALIZE")) {
                        initialize = type;
                        break;
                     }
                  }
               }
            } catch (Exception ex) {
               ex.printStackTrace();
            }

            boolean supportsSeperatePackets;
            try {
               Function<String, MethodHandle> getPacket = packet -> XReflection.ofMinecraft()
                  .inPackage(MinecraftPackage.NMS, "network.protocol.game")
                  .named(packet)
                  .constructor(wb)
                  .unreflect();
               packetWarnDist = getPacket.apply("ClientboundSetBorderWarningDistancePacket");
               packetWarnDelay = getPacket.apply("ClientboundSetBorderWarningDelayPacket");
               packetLerpSize = getPacket.apply("ClientboundSetBorderLerpSizePacket");
               packetInit = getPacket.apply("ClientboundInitializeBorderPacket");
               packetCenter = getPacket.apply("ClientboundSetBorderCenterPacket");
               packetSize = getPacket.apply("ClientboundSetBorderSizePacket");
               supportsSeperatePackets = true;
            } catch (Throwable ignored) {
               supportsSeperatePackets = false;
            }

            PACKET_INIT = packetInit;
            PACKET_SIZE = packetSize;
            PACKET_CENTER = packetCenter;
            PACKET_LERP_SIZE = packetLerpSize;
            PACKET_WARNING_DELAY = packetWarnDelay;
            PACKET_WARNING_DISTANCE = packetWarnDist;
            SUPPORTS_SEPARATE_PACKETS = supportsSeperatePackets;
            WORLD_HANDLE = craftWorld.method().named("getHandle").returns(worldServer).unreflect();
            INITIALIZE = initialize;
            WORLDBORDER = wb.constructor().unreflect();
            WORLDBORDER_WORLD = wb.field().setter().named("world").returns(worldServer).unreflect();
            CENTER = wb.method()
               .map(MinecraftMapping.OBFUSCATED, XReflection.v(21, 5, "d").v(18, "c").orElse("setCenter"))
               .map(MinecraftMapping.MOJANG, "setCenter")
               .returns(void.class)
               .parameters(double.class, double.class)
               .unreflect();
            SIZE = wb.method().named(XReflection.v(18, "a").orElse("setSize")).returns(void.class).parameters(double.class).unreflect();
            WARNING_TIME = wb.method().named(XReflection.v(18, "b").orElse("setWarningTime")).returns(void.class).parameters(int.class).unreflect();
            WARNING_DISTANCE = wb.method()
               .map(MinecraftMapping.OBFUSCATED, XReflection.v(20, "c").v(18, "b").orElse("setWarningDistance"))
               .map(MinecraftMapping.MOJANG, "setWarningBlocks")
               .returns(void.class)
               .parameters(int.class)
               .unreflect();
            WorldBorder_lerpSizeBetween = wb.method()
               .map(MinecraftMapping.OBFUSCATED, XReflection.v(18, "a").orElse("transitionSizeBetween"))
               .map(MinecraftMapping.MOJANG, XReflection.v(21, "lerpSizeBetween").orElse("transitionSizeBetween"))
               .returns(void.class)
               .parameters(double.class, double.class, long.class)
               .unreflect();
         } else {
            PACKET_SIZE = null;
            PACKET_CENTER = null;
            PACKET_INIT = null;
            PACKET_LERP_SIZE = null;
            PACKET_WARNING_DELAY = null;
            PACKET_WARNING_DISTANCE = null;
            WorldBorder_lerpSizeBetween = null;
            SIZE = null;
            WARNING_TIME = null;
            WARNING_DISTANCE = null;
            CENTER = null;
            WORLDBORDER_WORLD = null;
            WORLDBORDER = null;
            WORLD_HANDLE = null;
            INITIALIZE = null;
            SUPPORTS_SEPARATE_PACKETS = true;
         }
      }

      private enum Component {
         SIZE {
            @Override
            protected void setHandle(XWorldBorder.NMSWorldBorder wb) throws Throwable {
               XWorldBorder.NMSWorldBorder.SIZE.invoke((Object)wb.handle, (double)wb.size);
            }

            @Override
            protected Object createPacket(XWorldBorder.NMSWorldBorder wb) throws Throwable {
               return (Object)XWorldBorder.NMSWorldBorder.PACKET_SIZE.invoke((Object)wb.handle);
            }
         },
         SIZE_LERP {
            @Override
            protected void setHandle(XWorldBorder.NMSWorldBorder wb) throws Throwable {
               XWorldBorder.NMSWorldBorder.WorldBorder_lerpSizeBetween
                  .invoke((Object)wb.handle, (double)wb.sizeLerpTarget, (double)wb.size, (long)wb.sizeLerpTime.toMillis());
            }

            @Override
            protected Object createPacket(XWorldBorder.NMSWorldBorder wb) throws Throwable {
               return (Object)XWorldBorder.NMSWorldBorder.PACKET_LERP_SIZE.invoke((Object)wb.handle);
            }
         },
         WARNING_DISTANCE {
            @Override
            protected void setHandle(XWorldBorder.NMSWorldBorder wb) throws Throwable {
               XWorldBorder.NMSWorldBorder.WARNING_DISTANCE.invoke((Object)wb.handle, (int)wb.warningBlocks);
            }

            @Override
            protected Object createPacket(XWorldBorder.NMSWorldBorder wb) throws Throwable {
               return (Object)XWorldBorder.NMSWorldBorder.PACKET_WARNING_DISTANCE.invoke((Object)wb.handle);
            }
         },
         WARNING_DELAY {
            @Override
            protected void setHandle(XWorldBorder.NMSWorldBorder wb) throws Throwable {
               XWorldBorder.NMSWorldBorder.WARNING_TIME.invoke((Object)wb.handle, (int)wb.warningBlocks);
            }

            @Override
            protected Object createPacket(XWorldBorder.NMSWorldBorder wb) throws Throwable {
               return (Object)XWorldBorder.NMSWorldBorder.PACKET_WARNING_DELAY.invoke((Object)wb.handle);
            }
         },
         CENTER {
            @Override
            protected void setHandle(XWorldBorder.NMSWorldBorder wb) throws Throwable {
               XWorldBorder.NMSWorldBorder.CENTER.invoke((Object)wb.handle, (double)wb.centerX, (double)wb.centerZ);
            }

            @Override
            protected Object createPacket(XWorldBorder.NMSWorldBorder wb) throws Throwable {
               return (Object)XWorldBorder.NMSWorldBorder.PACKET_CENTER.invoke((Object)wb.handle);
            }
         };

         Component() {
         }

         protected abstract void setHandle(XWorldBorder.NMSWorldBorder var1) throws Throwable;

         protected abstract Object createPacket(XWorldBorder.NMSWorldBorder var1) throws Throwable;
      }
   }
}
