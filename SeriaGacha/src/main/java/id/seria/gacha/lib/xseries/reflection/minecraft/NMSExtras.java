package id.seria.gacha.lib.xseries.reflection.minecraft;

import id.seria.gacha.lib.xseries.reflection.XReflection;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class NMSExtras {
   public static final Class<?> EntityLiving = XReflection.ofMinecraft()
      .inPackage(MinecraftPackage.NMS, "world.entity")
      .map(MinecraftMapping.MOJANG, "LivingEntity")
      .map(MinecraftMapping.SPIGOT, "EntityLiving")
      .unreflect();
   private static final MethodHandle GET_ENTITY_HANDLE;
   public static final MethodHandle EXP_PACKET;
   public static final MethodHandle ENTITY_PACKET;
   public static final MethodHandle WORLD_HANDLE;
   public static final MethodHandle ENTITY_HANDLE;
   public static final MethodHandle LIGHTNING_ENTITY;
   public static final MethodHandle VEC3D;
   public static final MethodHandle GET_DATA_WATCHER;
   public static final MethodHandle DATA_WATCHER_GET_ITEM;
   public static final MethodHandle DATA_WATCHER_SET_ITEM;
   public static final MethodHandle PACKET_PLAY_OUT_OPEN_SIGN_EDITOR;
   public static final MethodHandle PACKET_PLAY_OUT_BLOCK_CHANGE;
   public static final MethodHandle ANIMATION_PACKET;
   public static final MethodHandle ANIMATION_TYPE;
   public static final MethodHandle ANIMATION_ENTITY_ID;
   public static final MethodHandle PLAY_OUT_MULTI_BLOCK_CHANGE_PACKET;
   public static final MethodHandle MULTI_BLOCK_CHANGE_INFO;
   public static final MethodHandle CHUNK_WRAPPER_SET;
   public static final MethodHandle CHUNK_WRAPPER;
   public static final MethodHandle SHORTS_OR_INFO;
   public static final MethodHandle SET_BlockState;
   public static final MethodHandle BLOCK_POSITION;
   public static final MethodHandle PLAY_BLOCK_ACTION;
   public static final MethodHandle GET_BUKKIT_ENTITY;
   public static final MethodHandle GET_BLOCK_TYPE;
   public static final MethodHandle GET_BLOCK;
   public static final MethodHandle GET_IBlockState;
   public static final MethodHandle SANITIZE_LINES;
   public static final MethodHandle TILE_ENTITY_SIGN;
   public static final MethodHandle TILE_ENTITY_SIGN__GET_UPDATE_PACKET;
   public static final MethodHandle TILE_ENTITY_SIGN__SET_LINE;
   public static final MethodHandle SIGN_TEXT;
   public static final Class<?> BlockState = XReflection.ofMinecraft()
      .inPackage(MinecraftPackage.NMS, "world.level.block.state")
      .map(MinecraftMapping.MOJANG, "BlockState")
      .map(MinecraftMapping.SPIGOT, "IBlockData")
      .unreflect();
   public static final Class<?> MULTI_BLOCK_CHANGE_INFO_CLASS = null;

   private NMSExtras() {
   }

   public static void setExp(Player player, float bar, int lvl, int exp) {
      try {
         Object packet = (Object)EXP_PACKET.invoke((float)bar, (int)lvl, (int)exp);
         MinecraftConnection.sendPacket(player, packet);
      } catch (Throwable ex) {
         ex.printStackTrace();
      }
   }

   public static void lightning(Player player, Location location, boolean sound) {
      lightning(Collections.singletonList(player), location, sound);
   }

   public static void lightning(Collection<Player> players, Location location, boolean sound) {
      try {
         Object world = (Object)WORLD_HANDLE.invoke((World)location.getWorld());
         if (!XReflection.supports(16)) {
            Object lightningBolt = (Object)LIGHTNING_ENTITY.invoke(
               (Object)world, (double)location.getX(), (double)location.getY(), (double)location.getZ(), (boolean)false, (boolean)false
            );
            Object packet = (Object)ENTITY_PACKET.invoke((Object)lightningBolt);

            for (Player player : players) {
               MinecraftConnection.sendPacket(player, packet);
            }
         } else {
            Class<?> nmsEntityType = XReflection.ofMinecraft()
               .inPackage(MinecraftPackage.NMS, "world.entity")
               .map(MinecraftMapping.MOJANG, "EntityType")
               .map(MinecraftMapping.SPIGOT, "EntityTypes")
               .unreflect();
            Object lightningType = nmsEntityType.getField(XReflection.supports(17) ? "U" : "LIGHTNING_BOLT").get(nmsEntityType);
            Object lightningBolt = (Object)LIGHTNING_ENTITY.invoke((Object)lightningType, (Object)world);
            Object lightningBoltID = lightningBolt.getClass().getMethod("getId").invoke(lightningBolt);
            Object lightningBoltUUID = lightningBolt.getClass().getMethod("getUniqueID").invoke(lightningBolt);
            Object vec3D = (Object)VEC3D.invoke((double)0.0, (double)0.0, (double)0.0);
            Object packet = (Object)ENTITY_PACKET.invoke(
               (Object)lightningBoltID,
               (Object)lightningBoltUUID,
               (double)location.getX(),
               (double)location.getY(),
               (double)location.getZ(),
               (float)0.0F,
               (float)0.0F,
               (Object)lightningType,
               (int)0,
               (Object)vec3D
            );

            for (Player player : players) {
               MinecraftConnection.sendPacket(player, packet);
            }
         }
      } catch (Throwable throwable) {
         throwable.printStackTrace();
      }
   }

   public static Object getData(Object dataWatcher, Object dataWatcherObject) {
      try {
         return (Object)DATA_WATCHER_GET_ITEM.invoke((Object)dataWatcher, (Object)dataWatcherObject);
      } catch (Throwable e) {
         throw new IllegalArgumentException("Failed to create data watcher", e);
      }
   }

   @Nullable
   public static Object getEntityHandle(Entity entity) {
      Objects.requireNonNull(entity, "Cannot get handle of null entity");

      try {
         return (Object)GET_ENTITY_HANDLE.invoke((Entity)entity);
      } catch (Throwable throwable) {
         throwable.printStackTrace();
         return null;
      }
   }

   public static Object getDataWatcher(Object handle) {
      try {
         return (Object)GET_DATA_WATCHER.invoke((Object)handle);
      } catch (Throwable e) {
         throw new IllegalArgumentException("Failed to get data watcher", e);
      }
   }

   public static Object setData(Object dataWatcher, Object dataWatcherObject, Object value) {
      try {
         return (Object)DATA_WATCHER_SET_ITEM.invoke((Object)dataWatcher, (Object)dataWatcherObject, (Object)value);
      } catch (Throwable e) {
         throw new IllegalArgumentException("Failed to set data watcher item", e);
      }
   }

   public static Object getStaticFieldIgnored(Class<?> clazz, String name) {
      return getStaticField(clazz, name, true);
   }

   public static Object getStaticField(Class<?> clazz, String name, boolean silent) {
      try {
         Field field = clazz.getDeclaredField(name);
         field.setAccessible(true);
         return field.get(null);
      } catch (Throwable e) {
         if (!silent) {
            throw new IllegalArgumentException("Failed to get static field of " + clazz + " named " + name, e);
         } else {
            return null;
         }
      }
   }

   public static void spinEntity(LivingEntity entity, boolean enabled) {
      if (!NMSExtras.EntityPose.SPIN_ATTACK.isSupported()) {
         throw new UnsupportedOperationException("Spin attacks are not supported in " + XReflection.getVersionInformation());
      }

      setLivingEntityFlag(entity, NMSExtras.LivingEntityFlags.SPIN_ATTACK.getBit(), enabled);
   }

   public static void setLivingEntityFlag(Entity entity, int index, boolean flag) {
      Object handle = getEntityHandle(entity);
      Object dataWatcher = getDataWatcher(handle);
      Object flagItem = NMSExtras.DataWatcherItemType.DATA_LIVING_ENTITY_FLAGS.getId();
      byte currentFlags = (Byte)getData(dataWatcher, flagItem);
      int newFlags;
      if (flag) {
         newFlags = currentFlags | index;
      } else {
         newFlags = currentFlags & ~index;
      }

      setData(dataWatcher, flagItem, (byte)newFlags);
   }

   public static boolean hasLivingEntityFlag(Entity entity, int index) {
      Object handle = getEntityHandle(entity);
      Object dataWatcher = getDataWatcher(handle);
      byte flags = (Byte)getData(dataWatcher, NMSExtras.DataWatcherItemType.DATA_LIVING_ENTITY_FLAGS.getId());
      return (flags & index) != 0;
   }

   public boolean isAutoSpinAttack(LivingEntity entity) {
      return hasLivingEntityFlag(entity, NMSExtras.LivingEntityFlags.SPIN_ATTACK.getBit());
   }

   public static void animation(Collection<? extends Player> players, LivingEntity entity, NMSExtras.Animation animation) {
      try {
         Object packet;
         if (XReflection.supports(17)) {
            packet = (Object)ANIMATION_PACKET.invoke((Object)(Object)ENTITY_HANDLE.invoke((LivingEntity)entity), (int)animation.ordinal());
         } else {
            packet = (Object)ANIMATION_PACKET.invoke();
            ANIMATION_TYPE.invoke((Object)packet, (int)animation.ordinal());
            ANIMATION_ENTITY_ID.invoke((Object)packet, (int)entity.getEntityId());
         }

         for (Player player : players) {
            MinecraftConnection.sendPacket(player, packet);
         }
      } catch (Throwable throwable) {
         throwable.printStackTrace();
      }
   }

   public static void chest(Block chest, boolean open) {
      Location location = chest.getLocation();

      try {
         Object world = (Object)WORLD_HANDLE.invoke((World)location.getWorld());
         Object position = XReflection.v(19, () -> {
            try {
               return (Object)BLOCK_POSITION.invoke((int)location.getBlockX(), (int)location.getBlockY(), (int)location.getBlockZ());
            } catch (Throwable e) {
               throw new IllegalArgumentException("Failed to set block position", e);
            }
         }).orElse(() -> {
            try {
               return (Object)BLOCK_POSITION.invoke((double)location.getX(), (double)location.getY(), (double)location.getZ());
            } catch (Throwable e) {
               throw new IllegalArgumentException("Failed to set block position", e);
            }
         });
         Object block = (Object)GET_BLOCK.invoke((Object)(Object)GET_BLOCK_TYPE.invoke((Object)world, (Object)position));
         PLAY_BLOCK_ACTION.invoke((Object)world, (Object)position, (Object)block, (int)1, (int)(open ? 1 : 0));
      } catch (Throwable throwable) {
         throwable.printStackTrace();
      }
   }

   @Deprecated
   protected static void sendBlockChange(Player player, Chunk chunk, Map<NMSExtras.WorldlessBlockWrapper, Object> blocks) {
      try {
         Object packet = (Object)PLAY_OUT_MULTI_BLOCK_CHANGE_PACKET.invoke();
         if (XReflection.supports(16)) {
            Object wrapper = (Object)CHUNK_WRAPPER.invoke((int)chunk.getX(), (int)chunk.getZ());
            CHUNK_WRAPPER_SET.invoke((Object)wrapper);
            Object dataArray = Array.newInstance(BlockState, blocks.size());
            Object shortArray = Array.newInstance(short.class, blocks.size());
            int i = 0;

            for (Entry<NMSExtras.WorldlessBlockWrapper, Object> entry : blocks.entrySet()) {
               Block loc = entry.getKey().block;
               int x = loc.getX() & 15;
               int y = loc.getY() & 15;
               int z = loc.getZ() & 15;
               i++;
            }

            SHORTS_OR_INFO.invoke((Object)packet, (Object)shortArray);
            SET_BlockState.invoke((Object)packet, (Object)dataArray);
         } else {
            Object wrapper = (Object)CHUNK_WRAPPER.invoke((int)chunk.getX(), (int)chunk.getZ());
            CHUNK_WRAPPER_SET.invoke((Object)wrapper);
            Object array = Array.newInstance(MULTI_BLOCK_CHANGE_INFO_CLASS, blocks.size());
            int i = 0;

            for (Entry<NMSExtras.WorldlessBlockWrapper, Object> entry : blocks.entrySet()) {
               Block loc = entry.getKey().block;
               int x = loc.getX() & 15;
               int z = loc.getZ() & 15;
               i++;
            }

            SHORTS_OR_INFO.invoke((Object)packet, (Object)array);
         }

         MinecraftConnection.sendPacket(player, packet);
      } catch (Throwable throwable) {
         throwable.printStackTrace();
      }
   }

   public static void openSign(Player player, DyeColor textColor, String[] lines, boolean frontSide) {
      try {
         Location loc = player.getLocation();
         Object position = (Object)BLOCK_POSITION.invoke((int)loc.getBlockX(), (int)1, (int)loc.getBlockY());
         Object signBlockData = (Object)GET_IBlockState.invoke((Material)Material.OAK_SIGN, (byte)0);
         Object blockChangePacket = (Object)PACKET_PLAY_OUT_BLOCK_CHANGE.invoke((Object)position, (Object)signBlockData);
         Object components = (Object)SANITIZE_LINES.invoke(lines);
         Object tileSign = (Object)TILE_ENTITY_SIGN.invoke((Object)position, (Object)signBlockData);
         if (!XReflection.supports(20)) {
            for (int i = 0; i < lines.length; i++) {
               Object component = Array.get(components, i);
               TILE_ENTITY_SIGN__SET_LINE.invoke((Object)tileSign, (int)i, (Object)component, (Object)component);
            }
         } else {
            Class<?> EnumColor = XReflection.ofMinecraft()
               .inPackage(MinecraftPackage.NMS, "world.item")
               .map(MinecraftMapping.MOJANG, "DyeColor")
               .map(MinecraftMapping.SPIGOT, "EnumColor")
               .unreflect();
            Object enumColor = null;
            Field[] signText = EnumColor.getFields();
            int var13 = signText.length;
            int var14 = 0;

            while (true) {
               if (var14 < var13) {
                  Field field = signText[var14];
                  Object color = field.get(null);
                  String colorName = (String)EnumColor.getDeclaredMethod("b").invoke(color);
                  if (!textColor.name().equalsIgnoreCase(colorName)) {
                     var14++;
                     continue;
                  }

                  enumColor = color;
               }

               Object signTextx = (Object)SIGN_TEXT.invoke((Object)components, (Object)components, (Object)enumColor, (boolean)frontSide);
               TILE_ENTITY_SIGN__SET_LINE.invoke((Object)signTextx, (boolean)true);
               break;
            }
         }

         Object signLinesUpdatePacket = (Object)TILE_ENTITY_SIGN__GET_UPDATE_PACKET.invoke((Object)tileSign);
         Object signPacket = XReflection.v(20, (Object)PACKET_PLAY_OUT_OPEN_SIGN_EDITOR.invoke((Object)position, (boolean)true))
            .orElse((Object)PACKET_PLAY_OUT_OPEN_SIGN_EDITOR.invoke((Object)position));
         MinecraftConnection.sendPacket(player, blockChangePacket, signLinesUpdatePacket, signPacket);
      } catch (Throwable x) {
         x.printStackTrace();
      }
   }

   static {
      Lookup lookup = MethodHandles.lookup();
      MethodHandle expPacket = null;
      MethodHandle entityPacket = null;
      MethodHandle worldHandle = null;
      MethodHandle entityHandle = null;
      MethodHandle lightning = null;
      MethodHandle vec3D = null;
      MethodHandle signEditorPacket = null;
      MethodHandle packetPlayOutBlockChange = null;
      MethodHandle animationPacket = null;
      MethodHandle animationType = null;
      MethodHandle animationEntityId = null;
      MethodHandle getBukkitEntity = null;
      MethodHandle blockPosition = null;
      MethodHandle playBlockAction = null;
      MethodHandle getBlockType = null;
      MethodHandle getBlock = null;
      MethodHandle getIBlockData = null;
      MethodHandle sanitizeLines = null;
      MethodHandle tileEntitySign = null;
      MethodHandle tileEntitySign_getUpdatePacket = null;
      MethodHandle tileEntitySign_setLine = null;
      MethodHandle signText = null;
      MethodHandle playOutMultiBlockChange = null;
      MethodHandle multiBlockChangeInfo = null;
      MethodHandle chunkWrapper = null;
      MethodHandle chunkWrapperSet = null;
      MethodHandle shortsOrInfo = null;
      MethodHandle setBlockData = null;
      MethodHandle getDataWatcher = null;
      MethodHandle dataWatcherGetItem = null;
      MethodHandle dataWatcherSetItem = null;
      MethodHandle getHandle = null;

      try {
         Class<?> CraftEntityClass = XReflection.ofMinecraft().inPackage(MinecraftPackage.CB, "entity").named("CraftEntity").unreflect();
         Class<?> nmsEntityType = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "world.entity")
            .map(MinecraftMapping.MOJANG, "EntityType")
            .map(MinecraftMapping.SPIGOT, "EntityTypes")
            .unreflect();
         Class<?> nmsEntity = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "world.entity").named("Entity").unreflect();
         Class<?> craftEntity = XReflection.ofMinecraft().inPackage(MinecraftPackage.CB, "entity").named("CraftEntity").unreflect();
         Class<?> nmsVec3D = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "world.phys")
            .map(MinecraftMapping.MOJANG, "Vec3")
            .map(MinecraftMapping.SPIGOT, "Vec3D")
            .unreflect();
         Class<?> world = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "world.level")
            .map(MinecraftMapping.MOJANG, "Level")
            .map(MinecraftMapping.SPIGOT, "World")
            .unreflect();
         Class<?> signOpenPacket = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "network.protocol.game")
            .map(MinecraftMapping.MOJANG, "ClientboundOpenSignEditorPacket")
            .map(MinecraftMapping.SPIGOT, "PacketPlayOutOpenSignEditor")
            .unreflect();
         Class<?> packetPlayOutBlockChangeClass = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "network.protocol.game")
            .map(MinecraftMapping.MOJANG, "ClientboundBlockUpdatePacket")
            .map(MinecraftMapping.SPIGOT, "PacketPlayOutBlockChange")
            .unreflect();
         Class<?> CraftMagicNumbers = XReflection.ofMinecraft().inPackage(MinecraftPackage.CB, "util").named("CraftMagicNumbers").unreflect();
         Class<?> CraftSign = XReflection.ofMinecraft().inPackage(MinecraftPackage.CB, "block").named("CraftSign").unreflect();
         Class<?> IChatBaseComponent = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "network.chat")
            .map(MinecraftMapping.MOJANG, "Component")
            .map(MinecraftMapping.SPIGOT, "IChatBaseComponent")
            .unreflect();
         Class<?> TileEntitySign = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "world.level.block.entity")
            .map(MinecraftMapping.MOJANG, "SignBlockEntity")
            .map(MinecraftMapping.SPIGOT, "TileEntitySign")
            .unreflect();
         Class<?> PacketPlayOutTileEntityData = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "network.protocol.game")
            .map(MinecraftMapping.MOJANG, "ClientboundBlockEntityDataPacket")
            .map(MinecraftMapping.SPIGOT, "PacketPlayOutTileEntityData")
            .unreflect();
         Class<?> DataWatcherClass = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "network.syncher")
            .map(MinecraftMapping.MOJANG, "SynchedEntityData")
            .map(MinecraftMapping.SPIGOT, "DataWatcher")
            .unreflect();
         Class<?> DataWatcherItemClass = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "network.syncher")
            .map(MinecraftMapping.MOJANG, "SynchedEntityData$DataItem")
            .map(MinecraftMapping.SPIGOT, "DataWatcher$Item")
            .unreflect();
         Class<?> DataWatcherObject = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "network.syncher")
            .map(MinecraftMapping.MOJANG, "EntityDataAccessor")
            .map(MinecraftMapping.SPIGOT, "DataWatcherObject")
            .unreflect();
         getHandle = lookup.findVirtual(CraftEntityClass, "getHandle", MethodType.methodType(nmsEntity));
         getDataWatcher = XReflection.of(nmsEntity)
            .method()
            .returns(DataWatcherClass)
            .map(MinecraftMapping.MOJANG, "getEntityData")
            .map(
               MinecraftMapping.OBFUSCATED,
               XReflection.v(21, 11, "aD")
                  .v(21, 9, "aC")
                  .v(21, 6, "au")
                  .v(21, 5, "ar")
                  .v(21, 3, "au")
                  .v(21, "ar")
                  .v(20, 5, "ap")
                  .v(20, 4, "an")
                  .v(20, 2, "al")
                  .v(19, "aj")
                  .v(18, "ai")
                  .orElse("getDataWatcher")
            )
            .unreflect();
         dataWatcherGetItem = XReflection.of(DataWatcherClass)
            .method()
            .returns(Object.class)
            .parameters(DataWatcherObject)
            .map(MinecraftMapping.MOJANG, "get")
            .map(MinecraftMapping.SPIGOT, XReflection.v(20, 5, "a").v(20, "b").v(18, "a").orElse("get"))
            .unreflect();
         dataWatcherSetItem = XReflection.of(DataWatcherClass)
            .method()
            .returns(void.class)
            .parameters(DataWatcherObject, Object.class)
            .map(MinecraftMapping.MOJANG, "set")
            .map(MinecraftMapping.SPIGOT, XReflection.v(20, 5, "a").v(18, "b").orElse("set"))
            .unreflect();
         getBukkitEntity = lookup.findVirtual(nmsEntity, "getBukkitEntity", MethodType.methodType(craftEntity));
         entityHandle = lookup.findVirtual(craftEntity, "getHandle", MethodType.methodType(nmsEntity));
         expPacket = lookup.findConstructor(
            XReflection.ofMinecraft()
               .inPackage(MinecraftPackage.NMS, "network.protocol.game")
               .map(MinecraftMapping.MOJANG, "ClientboundSetExperiencePacket")
               .map(MinecraftMapping.SPIGOT, "PacketPlayOutExperience")
               .unreflect(),
            MethodType.methodType(void.class, float.class, int.class, int.class)
         );
         if (!XReflection.supports(16)) {
            entityPacket = XReflection.ofMinecraft()
               .inPackage(MinecraftPackage.NMS)
               .named("PacketPlayOutSpawnEntityWeather")
               .constructor()
               .parameters(nmsEntity)
               .unreflect();
         } else {
            vec3D = lookup.findConstructor(nmsVec3D, MethodType.methodType(void.class, double.class, double.class, double.class));
            List<Class<?>> spawnTypes = new ArrayList<>(
               Arrays.asList(int.class, UUID.class, double.class, double.class, double.class, float.class, float.class, nmsEntityType, int.class, nmsVec3D)
            );
            if (XReflection.supports(19)) {
               spawnTypes.add(double.class);
            }

            entityPacket = lookup.findConstructor(
               XReflection.ofMinecraft()
                  .inPackage(MinecraftPackage.NMS, "network.protocol.game")
                  .map(MinecraftMapping.MOJANG, "ClientboundAddEntityPacket")
                  .map(MinecraftMapping.SPIGOT, "PacketPlayOutSpawnEntity")
                  .unreflect(),
               MethodType.methodType(void.class, spawnTypes)
            );
         }

         worldHandle = lookup.findVirtual(
            XReflection.ofMinecraft().inPackage(MinecraftPackage.CB).named("CraftWorld").unreflect(),
            "getHandle",
            MethodType.methodType(
               XReflection.ofMinecraft()
                  .inPackage(MinecraftPackage.NMS, "server.level")
                  .map(MinecraftMapping.MOJANG, "ServerLevel")
                  .map(MinecraftMapping.SPIGOT, "WorldServer")
                  .unreflect()
            )
         );
         MinecraftClassHandle entityLightning = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "world.entity")
            .map(MinecraftMapping.MOJANG, "LightningBolt")
            .map(MinecraftMapping.SPIGOT, "EntityLightning");
         if (!XReflection.supports(16)) {
            lightning = lookup.findConstructor(
               entityLightning.unreflect(), MethodType.methodType(void.class, world, double.class, double.class, double.class, boolean.class, boolean.class)
            );
         } else {
            lightning = lookup.findConstructor(entityLightning.unreflect(), MethodType.methodType(void.class, nmsEntityType, world));
         }

         Class<?> playOutMultiBlockChangeClass = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "network.protocol.game")
            .map(MinecraftMapping.MOJANG, "ClientboundSectionBlocksUpdatePacket")
            .map(MinecraftMapping.SPIGOT, "PacketPlayOutMultiBlockChange")
            .unreflect();
         Class<?> chunkCoordIntPairClass = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "world.level")
            .map(MinecraftMapping.MOJANG, "ChunkPos")
            .map(MinecraftMapping.SPIGOT, "ChunkCoordIntPair")
            .unreflect();

         try {
            if (!XReflection.supports(16)) {
               chunkWrapper = lookup.findConstructor(chunkCoordIntPairClass, MethodType.methodType(void.class, int.class, int.class));
            }
         } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
         }

         Class<?> animation = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "network.protocol.game")
            .map(MinecraftMapping.MOJANG, "ClientboundAnimatePacket")
            .map(MinecraftMapping.SPIGOT, "PacketPlayOutAnimation")
            .unreflect();
         animationPacket = lookup.findConstructor(
            animation, XReflection.supports(17) ? MethodType.methodType(void.class, nmsEntity, int.class) : MethodType.methodType(void.class)
         );
         if (!XReflection.supports(17)) {
            Field field = animation.getDeclaredField("a");
            field.setAccessible(true);
            animationEntityId = lookup.unreflectSetter(field);
            field = animation.getDeclaredField("b");
            field.setAccessible(true);
            animationType = lookup.unreflectSetter(field);
         }

         Class<?> blockPos = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "core")
            .map(MinecraftMapping.MOJANG, "BlockPos")
            .map(MinecraftMapping.SPIGOT, "BlockPosition")
            .unreflect();
         Class<?> block = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "world.level.block").named("Block").unreflect();
         blockPosition = lookup.findConstructor(
            blockPos,
            XReflection.v(19, MethodType.methodType(void.class, int.class, int.class, int.class))
               .orElse(MethodType.methodType(void.class, double.class, double.class, double.class))
         );
         getBlockType = XReflection.of(world)
            .method()
            .returns(BlockState)
            .parameters(blockPos)
            .map(MinecraftMapping.MOJANG, "getBlockState")
            .map(MinecraftMapping.SPIGOT, XReflection.v(18, "a_").orElse("getType"))
            .unreflect();
         if (XReflection.supports(21)) {
            getBlock = XReflection.ofMinecraft()
               .inPackage(MinecraftPackage.NMS, "world.level.block.state")
               .map(MinecraftMapping.MOJANG, "BlockBehaviour")
               .map(MinecraftMapping.SPIGOT, "BlockBase")
               .inner(XReflection.ofMinecraft().map(MinecraftMapping.MOJANG, "BlockStateBase").map(MinecraftMapping.SPIGOT, "BlockData"))
               .method()
               .returns(block)
               .map(MinecraftMapping.MOJANG, "getBlock")
               .map(MinecraftMapping.SPIGOT, "b")
               .unreflect();
         } else {
            getBlock = XReflection.of(BlockState)
               .method()
               .returns(block)
               .map(MinecraftMapping.MOJANG, "getBlock")
               .map(MinecraftMapping.SPIGOT, XReflection.v(18, "b").orElse("getBlock"))
               .unreflect();
         }

         playBlockAction = XReflection.of(world)
            .method()
            .returns(void.class)
            .parameters(blockPos, block, int.class, int.class)
            .map(MinecraftMapping.MOJANG, "blockEvent")
            .map(MinecraftMapping.SPIGOT, XReflection.v(18, "a").orElse("playBlockAction"))
            .unreflect();
         signEditorPacket = lookup.findConstructor(
            signOpenPacket, XReflection.v(20, MethodType.methodType(void.class, blockPos, boolean.class)).orElse(MethodType.methodType(void.class, blockPos))
         );
         if (XReflection.supports(17)) {
            packetPlayOutBlockChange = lookup.findConstructor(packetPlayOutBlockChangeClass, MethodType.methodType(void.class, blockPos, BlockState));
            getIBlockData = lookup.findStatic(CraftMagicNumbers, "getBlock", MethodType.methodType(BlockState, Material.class, byte.class));
            sanitizeLines = lookup.findStatic(
               CraftSign,
               XReflection.v(17, "sanitizeLines").orElse("SANITIZE_LINES"),
               MethodType.methodType(XReflection.toArrayClass(IChatBaseComponent), String[].class)
            );
            tileEntitySign = lookup.findConstructor(TileEntitySign, MethodType.methodType(void.class, blockPos, BlockState));
            tileEntitySign_getUpdatePacket = XReflection.of(TileEntitySign)
               .method()
               .returns(PacketPlayOutTileEntityData)
               .map(MinecraftMapping.MOJANG, "getUpdatePacket")
               .map(
                  MinecraftMapping.SPIGOT,
                  XReflection.v(21, 9, "l")
                     .v(21, 6, "u")
                     .v(21, 4, "s")
                     .v(21, 3, "t")
                     .v(20, 5, "l")
                     .v(20, 4, "m")
                     .v(20, "j")
                     .v(19, "f")
                     .v(18, "c")
                     .orElse("getUpdatePacket")
               )
               .unreflect();
            if (XReflection.supports(20)) {
               Class<?> SignText = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "world.level.block.entity").named("SignText").unreflect();
               if (!XReflection.supports(20, 6)) {
                  tileEntitySign_setLine = lookup.findVirtual(TileEntitySign, "a", MethodType.methodType(boolean.class, SignText, boolean.class));
               }

               Class<?> IChatBaseComponentArray = XReflection.of(IChatBaseComponent).asArray().unreflect();
               Class<?> EnumColor = XReflection.ofMinecraft()
                  .inPackage(MinecraftPackage.NMS, "world.item")
                  .map(MinecraftMapping.MOJANG, "DyeColor")
                  .map(MinecraftMapping.SPIGOT, "EnumColor")
                  .unreflect();
               signText = lookup.findConstructor(
                  SignText, MethodType.methodType(void.class, IChatBaseComponentArray, IChatBaseComponentArray, EnumColor, boolean.class)
               );
            } else {
               tileEntitySign_setLine = lookup.findVirtual(
                  TileEntitySign, "a", MethodType.methodType(void.class, int.class, IChatBaseComponent, IChatBaseComponent)
               );
            }
         }
      } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException ex) {
         ex.printStackTrace();
      }

      GET_ENTITY_HANDLE = getHandle;
      GET_DATA_WATCHER = getDataWatcher;
      DATA_WATCHER_GET_ITEM = dataWatcherGetItem;
      DATA_WATCHER_SET_ITEM = dataWatcherSetItem;
      EXP_PACKET = expPacket;
      ENTITY_PACKET = entityPacket;
      WORLD_HANDLE = worldHandle;
      ENTITY_HANDLE = entityHandle;
      LIGHTNING_ENTITY = lightning;
      VEC3D = vec3D;
      PACKET_PLAY_OUT_OPEN_SIGN_EDITOR = signEditorPacket;
      PACKET_PLAY_OUT_BLOCK_CHANGE = packetPlayOutBlockChange;
      ANIMATION_PACKET = animationPacket;
      ANIMATION_TYPE = animationType;
      ANIMATION_ENTITY_ID = animationEntityId;
      BLOCK_POSITION = blockPosition;
      PLAY_BLOCK_ACTION = playBlockAction;
      GET_BLOCK_TYPE = getBlockType;
      GET_BLOCK = getBlock;
      GET_IBlockState = getIBlockData;
      SANITIZE_LINES = sanitizeLines;
      TILE_ENTITY_SIGN = tileEntitySign;
      TILE_ENTITY_SIGN__GET_UPDATE_PACKET = tileEntitySign_getUpdatePacket;
      TILE_ENTITY_SIGN__SET_LINE = tileEntitySign_setLine;
      GET_BUKKIT_ENTITY = getBukkitEntity;
      PLAY_OUT_MULTI_BLOCK_CHANGE_PACKET = playOutMultiBlockChange;
      MULTI_BLOCK_CHANGE_INFO = multiBlockChangeInfo;
      CHUNK_WRAPPER = chunkWrapper;
      CHUNK_WRAPPER_SET = chunkWrapperSet;
      SHORTS_OR_INFO = shortsOrInfo;
      SET_BlockState = setBlockData;
      SIGN_TEXT = signText;
   }

   public enum Animation {
      SWING_MAIN_ARM,
      HURT,
      LEAVE_BED,
      SWING_OFF_HAND,
      CRITICAL_EFFECT,
      MAGIC_CRITICAL_EFFECT;

      Animation() {
      }
   }

   public enum DataWatcherItemType {
      DATA_LIVING_ENTITY_FLAGS(NMSExtras.getStaticFieldIgnored(NMSExtras.EntityLiving, "t"));

      private final Object id;
      private final boolean supported;

      DataWatcherItemType(Object DataWatcherObject) {
         boolean supported = true;
         Object id = null;

         try {
            id = DataWatcherObject;
         } catch (Throwable e) {
            supported = false;
         }

         this.supported = supported;
         this.id = id;
      }

      public boolean isSupported() {
         return this.supported;
      }

      public Object getId() {
         return this.id;
      }
   }

   public enum EntityPose {
      STANDING("a"),
      FALL_FLYING("b"),
      SLEEPING("c"),
      SWIMMING("d"),
      SPIN_ATTACK("e"),
      CROUCHING("f"),
      LONG_JUMPING("g"),
      DYING("h"),
      CROAKING("i"),
      USING_TONGUE("j"),
      SITTING("k"),
      ROARING("l"),
      SNIFFING("m"),
      EMERGING("n"),
      DIGGING("o");

      public final Object enumValue;
      private final boolean supported;

      EntityPose(String fieldName) {
         boolean supported = true;
         Object enumValue = null;

         try {
            Class<?> EntityPose = XReflection.ofMinecraft()
               .inPackage(MinecraftPackage.NMS, "world.entity")
               .map(MinecraftMapping.MOJANG, "Pose")
               .map(MinecraftMapping.SPIGOT, "EntityPose")
               .reflect();
            enumValue = EntityPose.getDeclaredField(XReflection.v(17, fieldName).orElse(this.name())).get(null);
         } catch (Throwable e) {
            supported = false;
         }

         this.supported = supported;
         this.enumValue = enumValue;
      }

      public boolean isSupported() {
         return this.supported;
      }

      public Object getEnumValue() {
         return this.enumValue;
      }
   }

   public enum LivingEntityFlags {
      SPIN_ATTACK(4);

      private final byte bit;

      LivingEntityFlags(int bit) {
         this.bit = (byte)bit;
      }

      public byte getBit() {
         return this.bit;
      }
   }

   public static class WorldlessBlockWrapper {
      public final Block block;

      public WorldlessBlockWrapper(Block block) {
         this.block = block;
      }

      @Override
      public int hashCode() {
         return (this.block.getY() + this.block.getZ() * 31) * 31 + this.block.getX();
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         }

         if (!(obj instanceof Block)) {
            return false;
         }

         Block other = (Block)obj;
         return this.block.getX() == other.getX() && this.block.getY() == other.getY() && this.block.getZ() == other.getZ();
      }
   }
}
