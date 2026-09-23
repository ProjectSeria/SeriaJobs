package id.seria.gacha.lib.common.nms.interfaces.entity;

import org.bukkit.entity.EntityType;

public enum FakeEntityType {
   NOTSET(0),
   ELDER_GUARDIAN(0),
   WITHER_SKELETON(0),
   STRAY(0),
   HUSK(0),
   ZOMBIE_VILLAGER(0),
   EVOKER(0),
   VEX(0),
   VINDICATOR(0),
   ILLUSIONER(0),
   CREEPER(0),
   SKELETON(0),
   SPIDER(0),
   GIANT(0),
   ZOMBIE(0),
   SLIME(0),
   GHAST(0),
   ZOMBIFIED_PIGLIN(0),
   ENDERMAN(0),
   CAVE_SPIDER(0),
   SILVERFISH(0),
   BLAZE(0),
   MAGMA_CUBE(0),
   ENDER_DRAGON(0),
   WITHER(0),
   BAT(0),
   WITCH(0),
   ENDERMITE(0),
   GUARDIAN(0),
   SHULKER(0),
   PIG(0),
   SHEEP(0),
   COW(0),
   CHICKEN(0),
   SQUID(0),
   WOLF(0),
   MUSHROOM_COW(0),
   SNOWMAN(0),
   OCELOT(0),
   IRON_GOLEM(0),
   HORSE(0),
   RABBIT(0),
   POLAR_BEAR(0),
   VILLAGER(0),
   TURTLE(0),
   PHANTOM(0),
   DROWNED(0),
   DOLPHIN(0),
   PANDA(0),
   PILLAGER(0),
   RAVAGER(0),
   WANDERING_TRADER(0),
   FOX(0),
   BEE(0),
   HOGLIN(0),
   PIGLIN(0),
   STRIDER(0),
   ZOGLIN(0),
   PIGLIN_BRUTE(0),
   AXOLOTL(0),
   GLOW_SQUID(0),
   GOAT(0),
   ALLAY(0),
   FROG(0),
   WARDEN(0),
   SNIFFER(0),
   LLAMA(0),
   TADPOLE(0),
   PARROT(0),
   CAT(0),
   CAMEL(0),
   BREEZE(0),
   TRADER_LLAMA(0),
   SKELETON_HORSE(0),
   ZOMBIE_HORSE(0),
   DONKEY(0),
   MULE(0),
   ARMADILLO(0),
   DROPPED_ITEM(2),
   PRIMED_TNT(50),
   ENDER_CRYSTAL(51),
   LIGHTNING(59),
   ARROW(60),
   SNOWBALL(61),
   EGG(62),
   FALLING_BLOCK(70),
   ITEM_FRAME(71),
   ENDER_SIGNAL(72),
   SPLASH_POTION(73),
   THROWN_EXP_BOTTLE(75),
   FIREWORK(76),
   LEASH_HITCH(77),
   ARMOR_STAND(78),
   TEXT_DISPLAY(0),
   BLOCK_DISPLAY(0),
   ITEM_DISPLAY(0),
   INTERACTION(0),
   PLAYER(0);

   private int networkId;

   FakeEntityType(int networkId) {
      this.networkId = networkId;
   }

   public int getNetworkId() {
      return this.networkId;
   }

   public EntityType toBukkit() {
      try {
         return EntityType.valueOf(this.name());
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
         return null;
      }
   }

   public static FakeEntityType fromBukkit(EntityType type) {
      try {
         return valueOf(type.name());
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
         return null;
      }
   }
}
