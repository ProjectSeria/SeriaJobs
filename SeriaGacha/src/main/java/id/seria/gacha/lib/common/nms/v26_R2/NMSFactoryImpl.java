package id.seria.gacha.lib.common.nms.v26_R2;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import id.seria.gacha.lib.common.nms.interfaces.BlockSounds;
import id.seria.gacha.lib.common.nms.interfaces.NMSFactory;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakePlayerEntity;
import id.seria.gacha.lib.common.nms.interfaces.wrappers.BossBarWrapper;
import id.seria.gacha.lib.common.nms.interfaces.wrappers.PlayerWrapper;
import id.seria.gacha.lib.common.nms.interfaces.wrappers.enums.BarColorWrapper;
import id.seria.gacha.lib.common.nms.interfaces.wrappers.enums.BarStyleWrapper;
import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeArmorStandEntityImpl;
import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeBatEntityImpl;
import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeBlockDisplayEntityImpl;
import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeEntityImpl;
import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeEquipableEntityImpl;
import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeFrogEntityImpl;
import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeInteractionEntityImpl;
import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeItemDisplayEntityImpl;
import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeItemEntityImpl;
import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeLivingEntityImpl;
import id.seria.gacha.lib.common.nms.v26_R2.entity.FakePlayerEntityImpl;
import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeTextDisplayEntityImpl;
import id.seria.gacha.lib.common.nms.v26_R2.inbound.InboundPacketHandler;
import id.seria.gacha.lib.common.nms.v26_R2.wrappers.BossBarWrapperImpl;
import id.seria.gacha.lib.common.nms.v26_R2.wrappers.PlayerWrapperImpl;
import id.seria.gacha.lib.common.utils.utility.Validate;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Display.BlockDisplay;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.entity.Display.TextDisplay;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.io.FileDeleteStrategy;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftSound;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class NMSFactoryImpl implements NMSFactory {
   public static ServerPlugin PLUGIN_INSTANCE;
   private String handlerName;
   private static final boolean[] COLOR_CODES = new boolean[128];

   public NMSFactoryImpl() {
   }

   @Override
   public void init(ServerPlugin plugin) {
      PLUGIN_INSTANCE = plugin;
      this.handlerName = String.format("%s_listener", plugin.getName().toLowerCase().replace(" ", "_"));
   }

   @Override
   public PlayerWrapper wrapPlayer(Player player) {
      return new PlayerWrapperImpl(this, player);
   }

   @Override
   public BossBarWrapper createBossBar(String title, BarColorWrapper color, BarStyleWrapper style) {
      return new BossBarWrapperImpl(this, Bukkit.createBossBar(title, BarColor.valueOf(color.name()), BarStyle.valueOf(style.name()), new BarFlag[0]));
   }

   @Override
   public void forceDeleteFile(File file) {
      FileDeleteStrategy.FORCE.deleteQuietly(file);
   }

   @Override
   public void injectFakeEntityPacketListener(Player player) {
      this.modifyPipeline(player, pipeline -> {
         ChannelHandler currentListener = pipeline.get(this.handlerName);
         if (currentListener != null) {
            pipeline.remove(this.handlerName);
         }

         try {
            pipeline.addBefore("packet_handler", this.handlerName, new InboundPacketHandler(PLUGIN_INSTANCE, player));
         } catch (NoSuchElementException e) {
            pipeline.addFirst(this.handlerName, new InboundPacketHandler(PLUGIN_INSTANCE, player));
         }
      });
   }

   @Override
   public void uninjectFakeEntityPacketListener(Player player) {
      this.modifyPipeline(player, pipeline -> {
         ChannelHandler currentListener = pipeline.get(this.handlerName);
         if (currentListener != null) {
            pipeline.remove(this.handlerName);
         }
      });
   }

   private void modifyPipeline(Player player, Consumer<ChannelPipeline> pipelineModifierTask) {
      ServerGamePacketListenerImpl playerConnection = ((CraftPlayer)player).getHandle().connection;
      Connection networkManager = null;

      try {
         Field field = ServerCommonPacketListenerImpl.class.getDeclaredField("connection");
         field.setAccessible(true);
         networkManager = (Connection)field.get(playerConnection);
      } catch (Exception ex) {
         ex.printStackTrace();
         return;
      }

      Channel channel = networkManager.channel;
      channel.eventLoop().execute(() -> {
         if (player.isOnline()) {
            try {
               pipelineModifierTask.accept(channel.pipeline());
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      });
   }

   @Override
   public FakeEntity createFakeEntity(JavaPlugin plugin, FakeEntityType type, Location location) {
      CraftWorld craftWorld = (CraftWorld)location.getWorld();
      Entity nmsEntity = null;
      if (type.equals(FakeEntityType.DROPPED_ITEM)) {
         nmsEntity = new ItemEntity(net.minecraft.world.entity.EntityType.ITEM, craftWorld.getHandle());
         nmsEntity.setPos(location.getX(), location.getY(), location.getZ());
      } else {
         nmsEntity = craftWorld.createEntity(location, type.toBukkit().getEntityClass(), true);
      }

      if (nmsEntity instanceof TextDisplay) {
         return new FakeTextDisplayEntityImpl(plugin, (TextDisplay)nmsEntity);
      } else if (nmsEntity instanceof BlockDisplay) {
         return new FakeBlockDisplayEntityImpl(plugin, (BlockDisplay)nmsEntity);
      } else if (nmsEntity instanceof ItemDisplay) {
         return new FakeItemDisplayEntityImpl(plugin, (ItemDisplay)nmsEntity);
      } else if (nmsEntity instanceof Interaction) {
         return new FakeInteractionEntityImpl(plugin, (Interaction)nmsEntity);
      } else if (nmsEntity instanceof ArmorStand) {
         return new FakeArmorStandEntityImpl(plugin, (ArmorStand)nmsEntity);
      } else if (nmsEntity instanceof ItemEntity) {
         return new FakeItemEntityImpl(plugin, (ItemEntity)nmsEntity);
      } else if (nmsEntity instanceof Frog) {
         return new FakeFrogEntityImpl(plugin, (Frog)nmsEntity);
      } else if (nmsEntity instanceof Bat) {
         return new FakeBatEntityImpl(plugin, (Bat)nmsEntity);
      } else if (nmsEntity instanceof Mob) {
         return new FakeEquipableEntityImpl(plugin, type, (Mob)nmsEntity);
      } else {
         return nmsEntity instanceof LivingEntity
            ? new FakeLivingEntityImpl(plugin, type, (LivingEntity)nmsEntity)
            : new FakeEntityImpl(plugin, type, nmsEntity);
      }
   }

   @Override
   public FakePlayerEntity createFakePlayer(JavaPlugin plugin, Location location, String name, UUID skinUniqueId, String skinValue, String skinSignature) {
      GameProfile profile = this.buildPlayerProfile(skinUniqueId, name, skinValue, skinSignature);
      MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
      ServerLevel level = ((CraftWorld)location.getWorld()).getHandle();
      ServerPlayer nmsEntity = new ServerPlayer(server, level, profile, ClientInformation.createDefault());
      nmsEntity.setPos(location.getX(), location.getY(), location.getZ());
      nmsEntity.setYRot(location.getYaw());
      nmsEntity.setXRot(location.getPitch());
      return new FakePlayerEntityImpl(plugin, nmsEntity);
   }

   private GameProfile buildPlayerProfile(UUID uuid, String name, String skinValue, String skinSignature) {
      GameProfile profile = new GameProfile(uuid != null ? uuid : UUID.randomUUID(), name);
      if (skinValue != null && !skinValue.isEmpty()) {
         profile.getProperties().put("textures", new Property("textures", skinValue, skinSignature));
         return profile;
      } else {
         return profile;
      }
   }

   @Override
   public FakeEntity modifyEntity(JavaPlugin plugin, org.bukkit.entity.Entity entity) {
      return new FakeEntityImpl(plugin, FakeEntityType.NOTSET, ((CraftEntity)entity).getHandle());
   }

   @Override
   public FakeEntity modifyEntity(org.bukkit.entity.Entity entity) {
      return new FakeEntityImpl(PLUGIN_INSTANCE.getOriginal(), FakeEntityType.NOTSET, ((CraftEntity)entity).getHandle());
   }

   @Override
   public FakeEntity spawnEntity(JavaPlugin plugin, FakeEntityType type, Location location) {
      CraftWorld craftWorld = (CraftWorld)location.getWorld();
      Entity nmsEntity = null;
      if (type.equals(FakeEntityType.DROPPED_ITEM)) {
         nmsEntity = new ItemEntity(net.minecraft.world.entity.EntityType.ITEM, craftWorld.getHandle());
         nmsEntity.setPos(location.getX(), location.getY(), location.getZ());
      } else {
         nmsEntity = craftWorld.createEntity(location, type.toBukkit().getEntityClass(), true);
      }

      if (nmsEntity instanceof TextDisplay) {
         return new FakeTextDisplayEntityImpl(plugin, (TextDisplay)nmsEntity);
      }

      if (nmsEntity instanceof BlockDisplay) {
         return new FakeBlockDisplayEntityImpl(plugin, (BlockDisplay)nmsEntity);
      }

      if (nmsEntity instanceof ItemDisplay) {
         return new FakeItemDisplayEntityImpl(plugin, (ItemDisplay)nmsEntity);
      }

      if (nmsEntity instanceof Interaction) {
         return new FakeInteractionEntityImpl(plugin, (Interaction)nmsEntity);
      }

      if (nmsEntity instanceof ArmorStand) {
         return new FakeArmorStandEntityImpl(plugin, (ArmorStand)nmsEntity);
      }

      if (nmsEntity instanceof ItemEntity) {
         return new FakeItemEntityImpl(plugin, (ItemEntity)nmsEntity);
      }

      if (nmsEntity instanceof Bat) {
         return new FakeBatEntityImpl(plugin, (Bat)nmsEntity);
      }

      if (nmsEntity instanceof Mob) {
         return new FakeEquipableEntityImpl(plugin, type, (Mob)nmsEntity);
      }

      if (nmsEntity instanceof LivingEntity) {
         return new FakeLivingEntityImpl(plugin, type, (LivingEntity)nmsEntity);
      }

      craftWorld.addEntityToWorld(nmsEntity, SpawnReason.CUSTOM);
      return new FakeEntityImpl(plugin, type, nmsEntity);
   }

   @Override
   public void disableBukkitLivingEntityAI(org.bukkit.entity.LivingEntity bukkitEntity) {
      bukkitEntity.setAI(false);
   }

   @Override
   public void disableBukkitLivingEntitySound(org.bukkit.entity.LivingEntity bukkitEntity) {
      bukkitEntity.setSilent(false);
   }

   @Override
   public void sendBlockBreakAnimationPacket(Collection<Player> players, Block block, int state) {
      BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ());
      ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(pos.hashCode(), pos, state);

      for (Player player : players) {
         ((CraftPlayer)player).getHandle().connection.send(packet);
      }
   }

   @Override
   public void sendBlockUpdatePacket(Collection<Player> players, Location location, Material newMaterial, byte data, BlockFace face) {
      CraftBlockData craftData = (CraftBlockData)Bukkit.createBlockData(newMaterial);
      if (craftData instanceof Directional) {
         ((Directional)craftData).setFacing(face);
      } else if (craftData instanceof Orientable) {
         ((Orientable)craftData).setAxis(convertBlockFaceToAxis(face));
      } else if (craftData instanceof Rotatable) {
         ((Rotatable)craftData).setRotation(face.getOppositeFace());
      }

      BlockState state = craftData.getState();
      ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(
         new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), state
      );

      for (Player player : players) {
         ((CraftPlayer)player).getHandle().connection.send(packet);
      }
   }

   @Override
   public void sendTexturedBlockUpdatePacket(Collection<Player> players, Location location, String base64, BlockFace face) {
      World world = location.getWorld();
      if (world != null) {
         BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
         UUID uuid = UUID.nameUUIDFromBytes(base64.getBytes());
         GameProfile profile = new GameProfile(uuid, this.clamp10(uuid.toString().replace("-", "")));
         profile.getProperties().put("textures", new Property("textures", base64));
         BlockState state = ((CraftBlockData)Bukkit.createBlockData(Material.PLAYER_HEAD)).getState();
         SkullBlockEntity skull = new SkullBlockEntity(pos, state);
         skull.setLevel(((CraftWorld)world).getHandle());
         skull.owner = new ResolvableProfile(profile);
         this.sendBlockUpdatePacket(players, location, Material.PLAYER_HEAD, (byte)0, face);

         for (Player player : players) {
            ((CraftPlayer)player).getHandle().connection.send(skull.getUpdatePacket());
         }
      }
   }

   private String clamp10(String input) {
      return input.length() > 10 ? input.substring(0, 10) : input;
   }

   @Override
   public void sendTablistPacket(Collection<Player> players, String header, String footer) {
      Object playerListHeader = CraftChatMessage.fromStringOrNull(header, true);
      Object playerListFooter = CraftChatMessage.fromStringOrNull(footer, true);
      ClientboundTabListPacket packet = new ClientboundTabListPacket(
         (Component)(playerListHeader == null ? Component.empty() : playerListHeader),
         (Component)(playerListFooter == null ? Component.empty() : playerListFooter)
      );

      for (Player player : players) {
         ((CraftPlayer)player).getHandle().connection.send(packet);
      }
   }

   @Override
   public void sendTitlePacket(Collection<Player> players, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
      ClientboundSetTitlesAnimationPacket packetTimes = new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);
      ClientboundSetTitleTextPacket packetTitle = new ClientboundSetTitleTextPacket(CraftChatMessage.fromString(title)[0]);
      ClientboundSetSubtitleTextPacket packetSubtitle = new ClientboundSetSubtitleTextPacket(CraftChatMessage.fromString(subtitle)[0]);

      for (Player player : players) {
         ((CraftPlayer)player).getHandle().connection.send(packetTimes);
         ((CraftPlayer)player).getHandle().connection.send(packetTitle);
         ((CraftPlayer)player).getHandle().connection.send(packetSubtitle);
      }
   }

   @Override
   public void sendActionBarPacket(Collection<Player> players, String message) {
      for (Player player : players) {
         player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
      }
   }

   @Override
   public String translateColorCodes(String rawMessage) {
      int length = rawMessage.length();
      char[] chars = rawMessage.toCharArray();
      StringBuilder translated = new StringBuilder(length);

      for (int i = 0; i < length; i++) {
         char current = chars[i];
         if (current == '&' && i + 7 < length && chars[i + 1] == '#') {
            boolean isValidHex = true;

            for (int j = i + 2; j < i + 8; j++) {
               char hexChar = chars[j];
               if ((hexChar < '0' || hexChar > '9') && (hexChar < 'a' || hexChar > 'f') && (hexChar < 'A' || hexChar > 'F')) {
                  isValidHex = false;
                  break;
               }
            }

            if (isValidHex) {
               String string = new String(chars, i + 1, 7);

               try {
                  int rgb = Integer.parseInt(string.substring(1), 16);
               } catch (NumberFormatException var7) {
                  throw new IllegalArgumentException("Illegal hex string " + string);
               }

               StringBuilder magic = new StringBuilder("\u00a7x");

               for (char c : string.substring(1).toCharArray()) {
                  magic.append('\u00a7').append(c);
               }

               translated.append(magic);
               i += 7;
               continue;
            }
         }

         if (current == '&' && i + 1 < length) {
            char next = chars[i + 1];
            if (next < 128 && COLOR_CODES[next]) {
               char lowerNext = next >= 'A' && next <= 'Z' ? (char)(next | 32) : next;
               translated.append('\u00a7').append(lowerNext);
               i++;
               continue;
            }
         }

         translated.append(current);
      }

      return translated.toString();
   }

   private static boolean isHexColorCode(String str, int start) {
      for (int i = start; i < start + 6; i++) {
         if (!isHexChar(str.charAt(i))) {
            return false;
         }
      }

      return true;
   }

   private static boolean isHexChar(char c) {
      return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
   }

   @Override
   public void changeChestState(Collection<Player> players, Location location, boolean open, boolean silent) {
      byte dataByte = (byte)(open ? 1 : 0);

      for (Player player : players) {
         if (!silent) {
            playCorrectChestSound(player, location, open);
         }

         BlockPos position = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
         net.minecraft.world.level.block.Block b = ((CraftBlockData)Bukkit.createBlockData(location.getBlock().getType())).getState().getBlock();
         ClientboundBlockEventPacket blockActionPacket = new ClientboundBlockEventPacket(position, b, 1, dataByte);
         ((CraftPlayer)player).getHandle().connection.send(blockActionPacket);
      }
   }

   @Override
   public void setBlockFace(Block block, BlockFace face) {
      BlockData blockData = block.getBlockData();
      if (blockData instanceof Directional) {
         ((Directional)blockData).setFacing(face);
         block.setBlockData(blockData);
      } else if (blockData instanceof Orientable) {
         ((Orientable)blockData).setAxis(convertBlockFaceToAxis(face));
         block.setBlockData(blockData);
      } else if (blockData instanceof Rotatable) {
         ((Rotatable)blockData).setRotation(face.getOppositeFace());
         block.setBlockData(blockData);
      }
   }

   @Override
   public void setBlockTexture(Block block, String base64) {
      block.setType(Material.PLAYER_HEAD);
      SkullBlockEntity skullTile = (SkullBlockEntity)((CraftWorld)block.getWorld())
         .getHandle()
         .getBlockEntity(new BlockPos(block.getX(), block.getY(), block.getZ()));
      if (skullTile != null) {
         UUID uuid = UUID.nameUUIDFromBytes(base64.getBytes());
         GameProfile profile = new GameProfile(uuid, this.clamp10(uuid.toString().replace("-", "")));
         profile.getProperties().put("textures", new Property("textures", base64));
         skullTile.owner = new ResolvableProfile(profile);
         block.getState().update(true);
      }
   }

   @Override
   public void setInventoryContents(PlayerInventory inventory, ItemStack[] items) {
      Validate.isTrue(items.length == 41, "Expected 41 array size, founded " + items.length);
      ItemStack[] storageContents = new ItemStack[36];
      ItemStack[] armorContents = new ItemStack[4];
      ItemStack[] extraContents = new ItemStack[1];

      for (int i = 0; i < 36; i++) {
         storageContents[i] = items[i];
      }

      for (int i = 0; i < 4; i++) {
         armorContents[i] = items[36 + i];
      }

      extraContents[0] = items[40];
      inventory.setStorageContents(storageContents);
      inventory.setArmorContents(armorContents);
      inventory.setExtraContents(extraContents);
   }

   @Override
   public BlockFace getBlockFace(Block block) {
      BlockData blockData = block.getBlockData();
      if (blockData instanceof Directional) {
         return ((Directional)blockData).getFacing();
      } else {
         return blockData instanceof Rotatable ? ((Rotatable)blockData).getRotation().getOppositeFace() : BlockFace.NORTH;
      }
   }

   @Override
   public ItemStack createTexturedSkullFromUrl(String url) {
      ItemStack item = new ItemStack(Material.PLAYER_HEAD);
      return this.createTexturedSkullFromUrl(item, url);
   }

   @Override
   public ItemStack createTexturedSkullFromBase64(String base64) {
      ItemStack item = new ItemStack(Material.PLAYER_HEAD);
      return this.createTexturedSkullFromBase64(item, base64);
   }

   @Override
   public ItemStack createTexturedSkullFromUrl(ItemStack skull, String url) {
      if (url != null && !url.isEmpty()) {
         byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
         return this.createTexturedSkullFromBase64(skull, new String(encodedData));
      } else {
         return skull;
      }
   }

   @Override
   public ItemStack createTexturedSkullFromBase64(ItemStack skull, String base64) {
      if (!id.seria.gacha.lib.common.utils.objects.Base64.isValid(base64)) {
         Bukkit.getLogger().warning("Textured Skull requested with an invalid base64: " + base64);
         return skull;
      }

      UUID uuid = UUID.nameUUIDFromBytes(Base64.getDecoder().decode(base64));
      SkullMeta skullMeta = (SkullMeta)skull.getItemMeta();
      GameProfile profile = new GameProfile(uuid, this.clamp10(uuid.toString().replace("-", "")));
      profile.getProperties().put("textures", new Property("textures", base64));
      Field profileField = null;

      try {
         profileField = skullMeta.getClass().getDeclaredField("profile");
         profileField.setAccessible(true);

         try {
            profileField.set(skullMeta, new ResolvableProfile(profile));
         } catch (Throwable ignored) {
            profileField.set(skullMeta, profile);
         }
      } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
         ex.printStackTrace();
      }

      skull.setItemMeta(skullMeta);
      return skull;
   }

   @Override
   public BlockSounds getBlockSounds(Block block) {
      BlockPos position = new BlockPos(block.getX(), block.getY(), block.getZ());
      BlockState blockData = ((CraftBlock)block).getNMS();
      SoundType type = blockData.getSoundType();
      BlockSoundsImpl sounds = new BlockSoundsImpl();
      sounds.setBreakSound(CraftSound.minecraftToBukkit(type.getBreakSound()));
      sounds.setHitSound(CraftSound.minecraftToBukkit(type.getHitSound()));
      sounds.setStepSound(CraftSound.minecraftToBukkit(type.getStepSound()));
      sounds.setPlaceSound(CraftSound.minecraftToBukkit(type.getPlaceSound()));
      sounds.setFallSound(CraftSound.minecraftToBukkit(type.getFallSound()));
      sounds.setVolume(type.getVolume());
      sounds.setPitch(type.getPitch());
      return sounds;
   }

   private static void playCorrectChestSound(Player player, Location location, boolean open) {
      Block block = location.getBlock();
      switch (block.getType()) {
         case CHEST:
            if (open) {
               player.playSound(location, Sound.BLOCK_CHEST_OPEN, 1.0F, 1.0F);
            } else {
               player.playSound(location, Sound.BLOCK_CHEST_CLOSE, 1.0F, 1.0F);
            }
            break;
         case ENDER_CHEST:
            if (open) {
               player.playSound(location, Sound.BLOCK_ENDER_CHEST_OPEN, 1.0F, 1.0F);
            } else {
               player.playSound(location, Sound.BLOCK_ENDER_CHEST_CLOSE, 1.0F, 1.0F);
            }
            break;
         case WHITE_SHULKER_BOX:
         case ORANGE_SHULKER_BOX:
         case MAGENTA_SHULKER_BOX:
         case LIGHT_BLUE_SHULKER_BOX:
         case YELLOW_SHULKER_BOX:
         case LIME_SHULKER_BOX:
         case PINK_SHULKER_BOX:
         case GRAY_SHULKER_BOX:
         case LIGHT_GRAY_SHULKER_BOX:
         case CYAN_SHULKER_BOX:
         case PURPLE_SHULKER_BOX:
         case BLUE_SHULKER_BOX:
         case BROWN_SHULKER_BOX:
         case GREEN_SHULKER_BOX:
         case RED_SHULKER_BOX:
         case BLACK_SHULKER_BOX:
         case SHULKER_BOX:
            if (open) {
               player.playSound(location, Sound.BLOCK_SHULKER_BOX_OPEN, 1.0F, 1.0F);
            } else {
               player.playSound(location, Sound.BLOCK_SHULKER_BOX_CLOSE, 1.0F, 1.0F);
            }
      }
   }

   private static Axis convertBlockFaceToAxis(BlockFace face) {
      switch (face) {
         case NORTH:
         case SOUTH:
            return Axis.Z;
         case EAST:
         case WEST:
            return Axis.X;
         case UP:
         case DOWN:
            return Axis.Y;
         default:
            return Axis.X;
      }
   }

   static {
      String validCodes = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

      for (char c : validCodes.toCharArray()) {
         COLOR_CODES[c] = true;
      }
   }
}
