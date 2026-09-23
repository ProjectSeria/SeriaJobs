package id.seria.gacha.lib.common.nms.interfaces;

import com.google.common.collect.Lists;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeArmorStandEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeBlockDisplayEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeInteractionEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeItemDisplayEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeItemEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakePlayerEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeTextDisplayEntity;
import id.seria.gacha.lib.common.nms.interfaces.wrappers.BossBarWrapper;
import id.seria.gacha.lib.common.nms.interfaces.wrappers.PlayerWrapper;
import id.seria.gacha.lib.common.nms.interfaces.wrappers.enums.BarColorWrapper;
import id.seria.gacha.lib.common.nms.interfaces.wrappers.enums.BarStyleWrapper;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.io.File;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public interface NMSFactory {
   default void test_frog(Entity frog) {
   }

   void init(ServerPlugin var1);

   PlayerWrapper wrapPlayer(Player var1);

   BossBarWrapper createBossBar(String var1, BarColorWrapper var2, BarStyleWrapper var3);

   void forceDeleteFile(File var1);

   void injectFakeEntityPacketListener(Player var1);

   void uninjectFakeEntityPacketListener(Player var1);

   FakeEntity createFakeEntity(JavaPlugin var1, FakeEntityType var2, Location var3);

   default FakeEntity modifyEntity(Entity entity) {
      throw new NotImplementedException("Still not implemented on this version");
   }

   default FakeEntity modifyEntity(JavaPlugin plugin, Entity entity) {
      throw new NotImplementedException("Not implemented on this version");
   }

   default FakeEntity spawnEntity(JavaPlugin plugin, FakeEntityType type, Location location) {
      throw new NotImplementedException("Still not implemented on this version");
   }

   void disableBukkitLivingEntityAI(LivingEntity var1);

   void disableBukkitLivingEntitySound(LivingEntity var1);

   void sendTablistPacket(Collection<Player> var1, String var2, String var3);

   void sendTitlePacket(Collection<Player> var1, String var2, String var3, int var4, int var5, int var6);

   void sendActionBarPacket(Collection<Player> var1, String var2);

   String translateColorCodes(String var1);

   void sendBlockBreakAnimationPacket(Collection<Player> var1, Block var2, int var3);

   void sendBlockUpdatePacket(Collection<Player> var1, Location var2, Material var3, byte var4, BlockFace var5);

   void sendTexturedBlockUpdatePacket(Collection<Player> var1, Location var2, String var3, BlockFace var4);

   void changeChestState(Collection<Player> var1, Location var2, boolean var3, boolean var4);

   void setBlockFace(Block var1, BlockFace var2);

   void setBlockTexture(Block var1, String var2);

   void setInventoryContents(PlayerInventory var1, ItemStack[] var2);

   BlockFace getBlockFace(Block var1);

   ItemStack createTexturedSkullFromUrl(String var1);

   ItemStack createTexturedSkullFromUrl(ItemStack var1, String var2);

   ItemStack createTexturedSkullFromBase64(String var1);

   ItemStack createTexturedSkullFromBase64(ItemStack var1, String var2);

   BlockSounds getBlockSounds(Block var1);

   default FakeArmorStandEntity createFakeArmorStand(ServerPlugin plugin, Location location) {
      return (FakeArmorStandEntity)this.createFakeEntity(plugin.getOriginal(), FakeEntityType.ARMOR_STAND, location);
   }

   default FakeEntity createFakeEntity(ServerPlugin plugin, FakeEntityType type, Location location) {
      return this.createFakeEntity(plugin.getOriginal(), type, location);
   }

   default FakeArmorStandEntity createFakeArmorStand(JavaPlugin plugin, Location location) {
      return (FakeArmorStandEntity)this.createFakeEntity(plugin, FakeEntityType.ARMOR_STAND, location);
   }

   default FakeItemEntity createFakeItem(JavaPlugin plugin, Location location) {
      return (FakeItemEntity)this.createFakeEntity(plugin, FakeEntityType.DROPPED_ITEM, location);
   }

   default FakeBlockDisplayEntity createFakeBlockDisplay(JavaPlugin plugin, Location location) {
      return (FakeBlockDisplayEntity)this.createFakeEntity(plugin, FakeEntityType.BLOCK_DISPLAY, location);
   }

   default FakeBlockDisplayEntity createFakeBlockDisplay(ServerPlugin plugin, Location location) {
      return this.createFakeBlockDisplay(plugin.getOriginal(), location);
   }

   default FakeItemDisplayEntity createFakeItemDisplay(JavaPlugin plugin, Location location) {
      return (FakeItemDisplayEntity)this.createFakeEntity(plugin, FakeEntityType.ITEM_DISPLAY, location);
   }

   default FakeItemDisplayEntity createFakeItemDisplay(ServerPlugin plugin, Location location) {
      return this.createFakeItemDisplay(plugin.getOriginal(), location);
   }

   default FakeTextDisplayEntity createFakeTextDisplay(JavaPlugin plugin, Location location) {
      return (FakeTextDisplayEntity)this.createFakeEntity(plugin, FakeEntityType.TEXT_DISPLAY, location);
   }

   default FakeTextDisplayEntity createFakeTextDisplay(ServerPlugin plugin, Location location) {
      return this.createFakeTextDisplay(plugin.getOriginal(), location);
   }

   default FakeInteractionEntity createFakeInteraction(JavaPlugin plugin, Location location) {
      return (FakeInteractionEntity)this.createFakeEntity(plugin, FakeEntityType.INTERACTION, location);
   }

   default FakeInteractionEntity createFakeInteraction(ServerPlugin plugin, Location location) {
      return this.createFakeInteraction(plugin.getOriginal(), location);
   }

   default FakePlayerEntity createFakePlayer(JavaPlugin plugin, Location location, String name, UUID skinUniqueId, String skinValue, String skinSignature) {
      throw new NotImplementedException("Still not implemented on this version");
   }

   default String[] translateColorCodes(String[] rawMessage) {
      String[] copy = Arrays.copyOf(rawMessage, rawMessage.length);

      for (int i = 0; i < copy.length; i++) {
         copy[i] = this.translateColorCodes(copy[i]);
      }

      return copy;
   }

   default List<String> translateColorCodes(List<String> rawMessage) {
      return Lists.newArrayList(this.translateColorCodes(rawMessage.toArray(new String[0])));
   }

   default void sendBlockBreakAnimationPacket(Player player, Block block, int state) {
      this.sendBlockBreakAnimationPacket(Arrays.asList(player), block, state);
   }

   default void sendBlockUpdatePacket(Player player, Block block, Material newMaterial, byte data, BlockFace face) {
      this.sendBlockUpdatePacket(Arrays.asList(player), block.getLocation(), newMaterial, data, face);
   }

   default void sendTexturedBlockUpdatePacket(Player player, Block block, String texture, BlockFace face) {
      this.sendTexturedBlockUpdatePacket(Arrays.asList(player), block.getLocation(), texture, face);
   }

   default void changeChestState(Location location, boolean open, boolean silent) {
      this.changeChestState(location.getWorld().getPlayers(), location, open, silent);
   }

   default void changeChestState(Location location, boolean open) {
      this.changeChestState(location, open, false);
   }

   default void changeChestState(Collection<Player> players, Location location, boolean open) {
      this.changeChestState(players, location, open, false);
   }

   default void changeChestState(Player player, Location location, boolean open) {
      this.changeChestState(Arrays.asList(player), location, open, false);
   }

   default String convertTextureUrlToBase64(String textureUrl) {
      return new String(Base64.getEncoder().encode(String.format("{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}", textureUrl).getBytes()));
   }

   default void spawnParticle(
      Object particle,
      Location location,
      float offsetX,
      float offsetY,
      float offsetZ,
      float speed,
      int amount,
      Object data,
      Collection<? extends Player> players
   ) {
   }
}
