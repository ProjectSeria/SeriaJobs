package id.seria.gacha.lib.common.nms.v26_R2;

import com.google.common.base.Preconditions;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Translatable;
import org.bukkit.World;
import org.bukkit.entity.Allay;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Camel;
import org.bukkit.entity.Cat;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.ChestBoat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cod;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Egg;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.GlowSquid;
import org.bukkit.entity.Goat;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Marker;
import org.bukkit.entity.Mule;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.PufferFish;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.Salmon;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.Slime;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Sniffer;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Stray;
import org.bukkit.entity.Strider;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tadpole;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.TraderLlama;
import org.bukkit.entity.Trident;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Vex;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.entity.Warden;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zoglin;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.entity.minecart.StorageMinecart;

public enum EntityType implements Keyed, Translatable {
   DROPPED_ITEM("item", Item.class, 1, false),
   EXPERIENCE_ORB("experience_orb", ExperienceOrb.class, 2),
   AREA_EFFECT_CLOUD("area_effect_cloud", AreaEffectCloud.class, 3),
   ELDER_GUARDIAN("elder_guardian", ElderGuardian.class, 4),
   WITHER_SKELETON("wither_skeleton", WitherSkeleton.class, 5),
   STRAY("stray", Stray.class, 6),
   EGG("egg", Egg.class, 7),
   LEASH_HITCH("leash_knot", LeashHitch.class, 8),
   PAINTING("painting", Painting.class, 9),
   ARROW("arrow", Arrow.class, 10),
   SNOWBALL("snowball", Snowball.class, 11),
   FIREBALL("fireball", LargeFireball.class, 12),
   SMALL_FIREBALL("small_fireball", SmallFireball.class, 13),
   ENDER_PEARL("ender_pearl", EnderPearl.class, 14),
   ENDER_SIGNAL("eye_of_ender", EnderSignal.class, 15),
   SPLASH_POTION("potion", ThrownPotion.class, 16, false),
   THROWN_EXP_BOTTLE("experience_bottle", ThrownExpBottle.class, 17),
   ITEM_FRAME("item_frame", ItemFrame.class, 18),
   WITHER_SKULL("wither_skull", WitherSkull.class, 19),
   PRIMED_TNT("tnt", TNTPrimed.class, 20),
   FALLING_BLOCK("falling_block", FallingBlock.class, 21, false),
   FIREWORK("firework_rocket", Firework.class, 22, false),
   HUSK("husk", Husk.class, 23),
   SPECTRAL_ARROW("spectral_arrow", SpectralArrow.class, 24),
   SHULKER_BULLET("shulker_bullet", ShulkerBullet.class, 25),
   DRAGON_FIREBALL("dragon_fireball", DragonFireball.class, 26),
   ZOMBIE_VILLAGER("zombie_villager", ZombieVillager.class, 27),
   SKELETON_HORSE("skeleton_horse", SkeletonHorse.class, 28),
   ZOMBIE_HORSE("zombie_horse", ZombieHorse.class, 29),
   ARMOR_STAND("armor_stand", ArmorStand.class, 30),
   DONKEY("donkey", Donkey.class, 31),
   MULE("mule", Mule.class, 32),
   EVOKER_FANGS("evoker_fangs", EvokerFangs.class, 33),
   EVOKER("evoker", Evoker.class, 34),
   VEX("vex", Vex.class, 35),
   VINDICATOR("vindicator", Vindicator.class, 36),
   ILLUSIONER("illusioner", Illusioner.class, 37),
   MINECART_COMMAND("command_block_minecart", CommandMinecart.class, 40),
   BOAT("boat", Boat.class, 41),
   MINECART("minecart", RideableMinecart.class, 42),
   MINECART_CHEST("chest_minecart", StorageMinecart.class, 43),
   MINECART_FURNACE("furnace_minecart", PoweredMinecart.class, 44),
   MINECART_TNT("tnt_minecart", ExplosiveMinecart.class, 45),
   MINECART_HOPPER("hopper_minecart", HopperMinecart.class, 46),
   MINECART_MOB_SPAWNER("spawner_minecart", SpawnerMinecart.class, 47),
   CREEPER("creeper", Creeper.class, 50),
   SKELETON("skeleton", Skeleton.class, 51),
   SPIDER("spider", Spider.class, 52),
   GIANT("giant", Giant.class, 53),
   ZOMBIE("zombie", Zombie.class, 54),
   SLIME("slime", Slime.class, 55),
   GHAST("ghast", Ghast.class, 56),
   ZOMBIFIED_PIGLIN("zombified_piglin", PigZombie.class, 57),
   ENDERMAN("enderman", Enderman.class, 58),
   CAVE_SPIDER("cave_spider", CaveSpider.class, 59),
   SILVERFISH("silverfish", Silverfish.class, 60),
   BLAZE("blaze", Blaze.class, 61),
   MAGMA_CUBE("magma_cube", MagmaCube.class, 62),
   ENDER_DRAGON("ender_dragon", EnderDragon.class, 63),
   WITHER("wither", Wither.class, 64),
   BAT("bat", Bat.class, 65),
   WITCH("witch", Witch.class, 66),
   ENDERMITE("endermite", Endermite.class, 67),
   GUARDIAN("guardian", Guardian.class, 68),
   SHULKER("shulker", Shulker.class, 69),
   PIG("pig", Pig.class, 90),
   SHEEP("sheep", Sheep.class, 91),
   COW("cow", Cow.class, 92),
   CHICKEN("chicken", Chicken.class, 93),
   SQUID("squid", Squid.class, 94),
   WOLF("wolf", Wolf.class, 95),
   MUSHROOM_COW("mooshroom", MushroomCow.class, 96),
   SNOWMAN("snow_golem", Snowman.class, 97),
   OCELOT("ocelot", Ocelot.class, 98),
   IRON_GOLEM("iron_golem", IronGolem.class, 99),
   HORSE("horse", Horse.class, 100),
   RABBIT("rabbit", Rabbit.class, 101),
   POLAR_BEAR("polar_bear", PolarBear.class, 102),
   LLAMA("llama", Llama.class, 103),
   LLAMA_SPIT("llama_spit", LlamaSpit.class, 104),
   PARROT("parrot", Parrot.class, 105),
   VILLAGER("villager", Villager.class, 120),
   ENDER_CRYSTAL("end_crystal", EnderCrystal.class, 200),
   TURTLE("turtle", Turtle.class, -1),
   PHANTOM("phantom", Phantom.class, -1),
   TRIDENT("trident", Trident.class, -1),
   COD("cod", Cod.class, -1),
   SALMON("salmon", Salmon.class, -1),
   PUFFERFISH("pufferfish", PufferFish.class, -1),
   TROPICAL_FISH("tropical_fish", TropicalFish.class, -1),
   DROWNED("drowned", Drowned.class, -1),
   DOLPHIN("dolphin", Dolphin.class, -1),
   CAT("cat", Cat.class, -1),
   PANDA("panda", Panda.class, -1),
   PILLAGER("pillager", Pillager.class, -1),
   RAVAGER("ravager", Ravager.class, -1),
   TRADER_LLAMA("trader_llama", TraderLlama.class, -1),
   WANDERING_TRADER("wandering_trader", WanderingTrader.class, -1),
   FOX("fox", Fox.class, -1),
   BEE("bee", Bee.class, -1),
   HOGLIN("hoglin", Hoglin.class, -1),
   PIGLIN("piglin", Piglin.class, -1),
   STRIDER("strider", Strider.class, -1),
   ZOGLIN("zoglin", Zoglin.class, -1),
   PIGLIN_BRUTE("piglin_brute", PiglinBrute.class, -1),
   AXOLOTL("axolotl", Axolotl.class, -1),
   GLOW_ITEM_FRAME("glow_item_frame", GlowItemFrame.class, -1),
   GLOW_SQUID("glow_squid", GlowSquid.class, -1),
   GOAT("goat", Goat.class, -1),
   MARKER("marker", Marker.class, -1),
   ALLAY("allay", Allay.class, -1),
   CHEST_BOAT("chest_boat", ChestBoat.class, -1),
   FROG("frog", Frog.class, -1),
   TADPOLE("tadpole", Tadpole.class, -1),
   WARDEN("warden", Warden.class, -1),
   CAMEL("camel", Camel.class, -1),
   BLOCK_DISPLAY("block_display", BlockDisplay.class, -1),
   INTERACTION("interaction", Interaction.class, -1),
   ITEM_DISPLAY("item_display", ItemDisplay.class, -1),
   SNIFFER("sniffer", Sniffer.class, -1),
   TEXT_DISPLAY("text_display", TextDisplay.class, -1),
   FISHING_HOOK("fishing_bobber", FishHook.class, -1, false),
   LIGHTNING("lightning_bolt", LightningStrike.class, -1, false),
   PLAYER("player", Player.class, -1, false),
   UNKNOWN(null, null, -1, false);

   private final String name;
   private final Class<? extends Entity> clazz;
   private final short typeId;
   public final boolean independent;
   public final boolean living;
   private final NamespacedKey key;

   EntityType(String name, Class<? extends Entity> clazz, int typeId) {
      this(name, clazz, typeId, true);
   }

   EntityType(String name, Class<? extends Entity> clazz, int typeId, boolean independent) {
      this.name = name;
      this.clazz = clazz;
      this.typeId = (short)typeId;
      this.independent = independent;
      this.living = clazz != null && LivingEntity.class.isAssignableFrom(clazz);
      this.key = null;
   }

   @Deprecated
   public String getName() {
      return this.name;
   }

   public NamespacedKey getKey() {
      Preconditions.checkArgument(this.key != null, "EntityType doesn't have key! Is it UNKNOWN?");
      return this.key;
   }

   public Class<? extends Entity> getEntityClass() {
      return this.clazz;
   }

   @Deprecated
   public short getTypeId() {
      return this.typeId;
   }

   public boolean isSpawnable() {
      return this.independent;
   }

   public boolean isAlive() {
      return this.living;
   }

   public String getTranslationKey() {
      return "";
   }

   public boolean isEnabledByFeature(World world) {
      return true;
   }
}
