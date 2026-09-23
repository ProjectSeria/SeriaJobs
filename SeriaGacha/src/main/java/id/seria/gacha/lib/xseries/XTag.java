package id.seria.gacha.lib.xseries;

import id.seria.gacha.lib.xseries.base.XBase;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class XTag<T extends XBase<?, ?>> {
   @NotNull
   public static final XTag<XMaterial> AIR = XTag.TagBuilder.simple(XMaterial.AIR, XMaterial.CAVE_AIR, XMaterial.VOID_AIR);
   @NotNull
   public static final XTag<XMaterial> INVENTORY_NOT_DISPLAYABLE = XTag.TagBuilder.of(
         XMaterial.FROSTED_ICE,
         XMaterial.MOVING_PISTON,
         XMaterial.PISTON_HEAD,
         XMaterial.BUBBLE_COLUMN,
         XMaterial.POWDER_SNOW,
         XMaterial.REDSTONE_WIRE,
         XMaterial.TRIPWIRE,
         XMaterial.BIG_DRIPLEAF_STEM,
         XMaterial.SWEET_BERRY_BUSH,
         XMaterial.TORCHFLOWER_CROP,
         XMaterial.TWISTING_VINES_PLANT,
         XMaterial.WEEPING_VINES_PLANT,
         XMaterial.BAMBOO_SAPLING,
         XMaterial.CARROTS,
         XMaterial.POTATOES,
         XMaterial.BAMBOO_SAPLING,
         XMaterial.CHORUS_PLANT,
         XMaterial.KELP_PLANT,
         XMaterial.COCOA,
         XMaterial.TALL_SEAGRASS,
         XMaterial.MELON_STEM,
         XMaterial.PUMPKIN_STEM,
         XMaterial.ATTACHED_MELON_STEM,
         XMaterial.ATTACHED_PUMPKIN_STEM
      )
      .inheritFrom(
         AIR,
         XTag.CAVE_VINES,
         XTag.FILLED_CAULDRONS,
         XTag.FIRE,
         XTag.FLUID,
         XTag.PORTALS,
         XTag.WALL_SIGNS,
         XTag.WALL_HANGING_SIGNS,
         XTag.WALL_TORCHES,
         XTag.ALIVE_CORAL_WALL_FANS,
         XTag.DEAD_CORAL_WALL_FANS,
         XTag.WALL_HEADS,
         XTag.CANDLE_CAKES,
         XTag.WALL_BANNERS,
         XTag.FLOWER_POTS.without(XMaterial.FLOWER_POT)
      )
      .build();
   @NotNull
   public static final XTag<XMaterial> ACACIA_LOGS = XTag.TagBuilder.simple(
      XMaterial.STRIPPED_ACACIA_LOG, XMaterial.ACACIA_LOG, XMaterial.ACACIA_WOOD, XMaterial.STRIPPED_ACACIA_WOOD
   );
   @NotNull
   public static final XTag<XMaterial> CORAL_FANS = XTag.TagBuilder.simple(
      XTag.ALIVE_CORAL_FANS, XTag.ALIVE_CORAL_WALL_FANS, XTag.DEAD_CORAL_WALL_FANS, XTag.DEAD_CORAL_FANS
   );
   @NotNull
   public static final XTag<XMaterial> ALIVE_CORAL_BLOCKS = XTag.TagBuilder.simple(findAllCorals(true, true, false, false));
   @NotNull
   public static final XTag<XMaterial> ALIVE_CORAL_FANS = XTag.TagBuilder.simple(findAllCorals(true, false, true, false));
   @NotNull
   public static final XTag<XMaterial> ALIVE_CORAL_PLANTS = XTag.TagBuilder.simple(findAllCorals(true, false, false, false));
   @NotNull
   public static final XTag<XMaterial> ALIVE_CORAL_WALL_FANS = XTag.TagBuilder.simple(findAllCorals(true, false, true, true));
   @NotNull
   public static final XTag<XMaterial> SPAWN_EGGS = XTag.TagBuilder.of(
         Arrays.stream(XMaterial.values()).filter(x -> x.name().endsWith("_SPAWN_EGG")).toArray(XMaterial[]::new)
      )
      .build();
   @NotNull
   public static final XTag<XMaterial> ANIMALS_SPAWNABLE_ON = XTag.TagBuilder.simple(XMaterial.GRASS_BLOCK);
   @NotNull
   public static final XTag<XMaterial> ANVIL = XTag.TagBuilder.simple(XMaterial.ANVIL, XMaterial.CHIPPED_ANVIL, XMaterial.DAMAGED_ANVIL);
   @NotNull
   public static final XTag<XMaterial> AXOLOTL_TEMPT_ITEMS = XTag.TagBuilder.simple(XMaterial.TROPICAL_FISH_BUCKET);
   @NotNull
   public static final XTag<XMaterial> AXOLOTLS_SPAWNABLE_ON = XTag.TagBuilder.simple(XMaterial.CLAY);
   @NotNull
   public static final XTag<XMaterial> AZALEA_GROWS_ON = XTag.TagBuilder.of(XMaterial.SNOW_BLOCK, XMaterial.POWDER_SNOW)
      .inheritFrom(XTag.TERRACOTTA, XTag.SAND, XTag.DIRT)
      .build();
   @NotNull
   public static final XTag<XMaterial> AZALEA_ROOT_REPLACEABLE = XTag.TagBuilder.of(XMaterial.CLAY, XMaterial.GRAVEL)
      .inheritFrom(AZALEA_GROWS_ON, XTag.CAVE_VINES, XTag.BASE_STONE_OVERWORLD)
      .build();
   @NotNull
   public static final XTag<XMaterial> BAMBOO_LOGS = XTag.TagBuilder.simple(
      XMaterial.STRIPPED_BAMBOO_BLOCK, XMaterial.BAMBOO_BLOCK, XMaterial.BAMBOO_MOSAIC, XMaterial.BAMBOO_PLANKS
   );
   @NotNull
   public static final XTag<XMaterial> BAMBOO_PLANTABLE_ON = XTag.TagBuilder.of(XMaterial.GRAVEL, XMaterial.BAMBOO_SAPLING, XMaterial.BAMBOO)
      .inheritFrom(XTag.DIRT, XTag.SAND)
      .build();
   @NotNull
   public static final XTag<XMaterial> BANNERS = XTag.TagBuilder.simple(XTag.ITEMS_BANNERS, XTag.WALL_BANNERS);
   @NotNull
   public static final XTag<XMaterial> BASE_STONE_NETHER = XTag.TagBuilder.simple(XMaterial.NETHERRACK, XMaterial.BASALT, XMaterial.BLACKSTONE);
   @NotNull
   public static final XTag<XMaterial> BASE_STONE_OVERWORLD = XTag.TagBuilder.simple(
      XMaterial.TUFF, XMaterial.DIORITE, XMaterial.DEEPSLATE, XMaterial.ANDESITE, XMaterial.GRANITE, XMaterial.STONE
   );
   @NotNull
   public static final XTag<XMaterial> BEACON_BASE_BLOCKS = XTag.TagBuilder.simple(
      XMaterial.NETHERITE_BLOCK, XMaterial.GOLD_BLOCK, XMaterial.IRON_BLOCK, XMaterial.EMERALD_BLOCK, XMaterial.DIAMOND_BLOCK
   );
   @NotNull
   public static final XTag<XMaterial> BEDS = XTag.TagBuilder.simple(findAllColors("BED"));
   @NotNull
   public static final XTag<XMaterial> BEE_GROWABLES = XTag.TagBuilder.of(XMaterial.SWEET_BERRY_BUSH).inheritFrom(XTag.CROPS, XTag.CAVE_VINES).build();
   @NotNull
   public static final XTag<XMaterial> BIG_DRIPLEAF_PLACEABLE = XTag.TagBuilder.of(XMaterial.CLAY, XMaterial.FARMLAND).inheritFrom(XTag.DIRT).build();
   @NotNull
   public static final XTag<XMaterial> BIRCH_LOGS = XTag.TagBuilder.simple(
      XMaterial.STRIPPED_BIRCH_LOG, XMaterial.BIRCH_LOG, XMaterial.BIRCH_WOOD, XMaterial.STRIPPED_BIRCH_WOOD
   );
   @NotNull
   public static final XTag<XMaterial> BUTTONS = XTag.TagBuilder.of(XMaterial.STONE_BUTTON, XMaterial.POLISHED_BLACKSTONE_BUTTON)
      .inheritFrom(XTag.WOODEN_BUTTONS)
      .build();
   @NotNull
   public static final XTag<XMaterial> CAMPFIRES = XTag.TagBuilder.simple(XMaterial.CAMPFIRE, XMaterial.SOUL_CAMPFIRE);
   @NotNull
   public static final XTag<XMaterial> CANDLE_CAKES = XTag.TagBuilder.simple(findAllColors("CANDLE_CAKE"));
   @NotNull
   public static final XTag<XMaterial> CANDLES = XTag.TagBuilder.simple(findAllColors("CANDLE"));
   @NotNull
   public static final XTag<XMaterial> CARPETS = XTag.TagBuilder.simple(findAllColors("CARPET"));
   @NotNull
   public static final XTag<XMaterial> CAULDRONS = XTag.TagBuilder.simple(
      XMaterial.CAULDRON, XMaterial.LAVA_CAULDRON, XMaterial.POWDER_SNOW_CAULDRON, XMaterial.WATER_CAULDRON
   );
   @NotNull
   public static final XTag<XMaterial> CAVE_VINES = XTag.TagBuilder.simple(XMaterial.CAVE_VINES, XMaterial.CAVE_VINES_PLANT);
   @NotNull
   public static final XTag<XMaterial> CHERRY_LOGS = XTag.TagBuilder.simple(
      XMaterial.STRIPPED_CHERRY_LOG, XMaterial.CHERRY_LOG, XMaterial.CHERRY_WOOD, XMaterial.STRIPPED_CHERRY_WOOD
   );
   @NotNull
   public static final XTag<XMaterial> CLIMBABLE = XTag.TagBuilder.of(
         XMaterial.SCAFFOLDING,
         XMaterial.WEEPING_VINES_PLANT,
         XMaterial.WEEPING_VINES,
         XMaterial.TWISTING_VINES,
         XMaterial.TWISTING_VINES_PLANT,
         XMaterial.VINE,
         XMaterial.LADDER
      )
      .inheritFrom(CAVE_VINES)
      .build();
   @NotNull
   public static final XTag<XMaterial> CLUSTER_MAX_HARVESTABLES = XTag.TagBuilder.simple(
      XMaterial.DIAMOND_PICKAXE,
      XMaterial.GOLDEN_PICKAXE,
      XMaterial.STONE_PICKAXE,
      XMaterial.NETHERITE_PICKAXE,
      XMaterial.WOODEN_PICKAXE,
      XMaterial.IRON_PICKAXE
   );
   @NotNull
   public static final XTag<XMaterial> COAL_ORES = XTag.TagBuilder.simple(XMaterial.COAL_ORE, XMaterial.DEEPSLATE_COAL_ORE);
   @NotNull
   public static final XTag<XMaterial> CONCRETE = XTag.TagBuilder.simple(findAllColors("CONCRETE"));
   @NotNull
   public static final XTag<XMaterial> CONCRETE_POWDER = XTag.TagBuilder.simple(findAllColors("CONCRETE_POWDER"));
   @NotNull
   public static final XTag<XMaterial> COPPER_ORES = XTag.TagBuilder.simple(XMaterial.COPPER_ORE, XMaterial.DEEPSLATE_COPPER_ORE);
   @NotNull
   public static final XTag<XMaterial> CORALS = XTag.TagBuilder.simple(
      ALIVE_CORAL_WALL_FANS,
      ALIVE_CORAL_FANS,
      ALIVE_CORAL_BLOCKS,
      ALIVE_CORAL_PLANTS,
      XTag.DEAD_CORAL_WALL_FANS,
      XTag.DEAD_CORAL_FANS,
      XTag.DEAD_CORAL_BLOCKS,
      XTag.DEAD_CORAL_PLANTS
   );
   @NotNull
   public static final XTag<XMaterial> CRIMSON_STEMS = XTag.TagBuilder.simple(
      XMaterial.CRIMSON_HYPHAE, XMaterial.STRIPPED_CRIMSON_STEM, XMaterial.CRIMSON_STEM, XMaterial.STRIPPED_CRIMSON_HYPHAE
   );
   @NotNull
   public static final XTag<XMaterial> CROPS = XTag.TagBuilder.simple(
      XMaterial.CARROT,
      XMaterial.CARROTS,
      XMaterial.POTATO,
      XMaterial.POTATOES,
      XMaterial.NETHER_WART,
      XMaterial.PUMPKIN_SEEDS,
      XMaterial.WHEAT_SEEDS,
      XMaterial.WHEAT,
      XMaterial.MELON_SEEDS,
      XMaterial.BEETROOT_SEEDS,
      XMaterial.BEETROOTS,
      XMaterial.SUGAR_CANE,
      XMaterial.BAMBOO_SAPLING,
      XMaterial.BAMBOO,
      XMaterial.CHORUS_PLANT,
      XMaterial.KELP,
      XMaterial.KELP_PLANT,
      XMaterial.SEA_PICKLE,
      XMaterial.BROWN_MUSHROOM,
      XMaterial.RED_MUSHROOM,
      XMaterial.MELON_STEM,
      XMaterial.PUMPKIN_STEM,
      XMaterial.COCOA,
      XMaterial.COCOA_BEANS
   );
   @NotNull
   public static final XTag<XMaterial> CRYSTAL_SOUND_BLOCKS = XTag.TagBuilder.simple(XMaterial.AMETHYST_BLOCK, XMaterial.BUDDING_AMETHYST);
   @NotNull
   public static final XTag<XMaterial> DARK_OAK_LOGS = XTag.TagBuilder.simple(
      XMaterial.STRIPPED_DARK_OAK_LOG, XMaterial.DARK_OAK_LOG, XMaterial.DARK_OAK_WOOD, XMaterial.STRIPPED_DARK_OAK_WOOD
   );
   @NotNull
   public static final XTag<XMaterial> DEAD_CORAL_BLOCKS = XTag.TagBuilder.simple(findAllCorals(false, true, false, false));
   @NotNull
   public static final XTag<XMaterial> DEAD_CORAL_FANS = XTag.TagBuilder.simple(findAllCorals(false, false, true, false));
   @NotNull
   public static final XTag<XMaterial> DEAD_CORAL_PLANTS = XTag.TagBuilder.simple(findAllCorals(false, false, false, false));
   @NotNull
   public static final XTag<XMaterial> DEAD_CORAL_WALL_FANS = XTag.TagBuilder.simple(findAllCorals(false, false, true, true));
   @NotNull
   public static final XTag<XMaterial> DEEPSLATE_ORE_REPLACEABLES = XTag.TagBuilder.simple(XMaterial.TUFF, XMaterial.DEEPSLATE);
   @NotNull
   public static final XTag<XMaterial> DIAMOND_ORES = XTag.TagBuilder.simple(XMaterial.DIAMOND_ORE, XMaterial.DEEPSLATE_DIAMOND_ORE);
   @NotNull
   public static final XTag<XMaterial> DIRT = XTag.TagBuilder.simple(
      XMaterial.MOSS_BLOCK, XMaterial.COARSE_DIRT, XMaterial.PODZOL, XMaterial.DIRT, XMaterial.ROOTED_DIRT, XMaterial.MYCELIUM, XMaterial.GRASS_BLOCK
   );
   @NotNull
   public static final XTag<XMaterial> DOORS = XTag.TagBuilder.of(XMaterial.IRON_DOOR).inheritFrom(XTag.WOODEN_DOORS).build();
   @NotNull
   public static final XTag<XMaterial> DRAGON_IMMUNE = XTag.TagBuilder.of(
         XMaterial.IRON_BARS, XMaterial.OBSIDIAN, XMaterial.RESPAWN_ANCHOR, XMaterial.END_STONE, XMaterial.CRYING_OBSIDIAN
      )
      .inheritFrom(XTag.WITHER_IMMUNE)
      .build();
   @NotNull
   public static final XTag<XMaterial> DRIPSTONE_REPLACEABLE = XTag.TagBuilder.of(XMaterial.DIRT).inheritFrom(BASE_STONE_OVERWORLD).build();
   @NotNull
   public static final XTag<XMaterial> WALL_HEADS = XTag.TagBuilder.simple(
      XTag.TagBuilder.simple(findMaterialsEndingWith("WALL_HEAD")), XTag.TagBuilder.simple(XMaterial.WITHER_SKELETON_WALL_SKULL, XMaterial.SKELETON_WALL_SKULL)
   );
   @NotNull
   public static final XTag<XMaterial> EMERALD_ORES = XTag.TagBuilder.simple(XMaterial.EMERALD_ORE, XMaterial.DEEPSLATE_EMERALD_ORE);
   @NotNull
   public static final XTag<XMaterial> ENDERMAN_HOLDABLE = XTag.TagBuilder.of(
         XMaterial.TNT,
         XMaterial.PUMPKIN,
         XMaterial.CARVED_PUMPKIN,
         XMaterial.MELON,
         XMaterial.CRIMSON_FUNGUS,
         XMaterial.WARPED_FUNGUS,
         XMaterial.WARPED_ROOTS,
         XMaterial.CRIMSON_ROOTS,
         XMaterial.RED_MUSHROOM,
         XMaterial.BROWN_MUSHROOM,
         XMaterial.CACTUS,
         XMaterial.GRAVEL,
         XMaterial.CLAY
      )
      .inheritFrom(DIRT, XTag.NYLIUM, XTag.SAND, XTag.SMALL_FLOWERS)
      .build();
   @NotNull
   public static final XTag<XMaterial> FEATURES_CANNOT_REPLACE = XTag.TagBuilder.simple(
      XMaterial.SPAWNER, XMaterial.END_PORTAL_FRAME, XMaterial.BEDROCK, XMaterial.CHEST
   );
   @NotNull
   public static final XTag<XMaterial> FENCE_GATES = XTag.TagBuilder.simple(XTag.WOODEN_FENCE_GATES);
   @NotNull
   public static final XTag<XMaterial> FENCES = XTag.TagBuilder.of(XMaterial.NETHER_BRICK_FENCE).inheritFrom(XTag.WOODEN_FENCES).build();
   @NotNull
   public static final XTag<XMaterial> FILLED_CAULDRONS = XTag.TagBuilder.simple(
      XMaterial.LAVA_CAULDRON, XMaterial.POWDER_SNOW_CAULDRON, XMaterial.WATER_CAULDRON
   );
   @NotNull
   public static final XTag<XMaterial> FIRE = XTag.TagBuilder.simple(XMaterial.FIRE, XMaterial.SOUL_FIRE);
   @NotNull
   public static final XTag<XMaterial> FLOWER_POTS = XTag.TagBuilder.simple(
      XMaterial.POTTED_OAK_SAPLING,
      XMaterial.POTTED_WITHER_ROSE,
      XMaterial.POTTED_ACACIA_SAPLING,
      XMaterial.POTTED_LILY_OF_THE_VALLEY,
      XMaterial.POTTED_WARPED_FUNGUS,
      XMaterial.POTTED_WARPED_ROOTS,
      XMaterial.POTTED_ALLIUM,
      XMaterial.POTTED_BROWN_MUSHROOM,
      XMaterial.POTTED_WHITE_TULIP,
      XMaterial.POTTED_ORANGE_TULIP,
      XMaterial.POTTED_DANDELION,
      XMaterial.POTTED_AZURE_BLUET,
      XMaterial.POTTED_FLOWERING_AZALEA_BUSH,
      XMaterial.POTTED_PINK_TULIP,
      XMaterial.POTTED_CORNFLOWER,
      XMaterial.POTTED_CRIMSON_FUNGUS,
      XMaterial.POTTED_RED_MUSHROOM,
      XMaterial.POTTED_BLUE_ORCHID,
      XMaterial.POTTED_FERN,
      XMaterial.POTTED_POPPY,
      XMaterial.POTTED_CRIMSON_ROOTS,
      XMaterial.POTTED_RED_TULIP,
      XMaterial.POTTED_OXEYE_DAISY,
      XMaterial.POTTED_AZALEA_BUSH,
      XMaterial.POTTED_BAMBOO,
      XMaterial.POTTED_CACTUS,
      XMaterial.FLOWER_POT,
      XMaterial.POTTED_DEAD_BUSH,
      XMaterial.POTTED_DARK_OAK_SAPLING,
      XMaterial.POTTED_PALE_OAK_SAPLING,
      XMaterial.POTTED_SPRUCE_SAPLING,
      XMaterial.POTTED_JUNGLE_SAPLING,
      XMaterial.POTTED_BIRCH_SAPLING,
      XMaterial.POTTED_MANGROVE_PROPAGULE,
      XMaterial.POTTED_CHERRY_SAPLING,
      XMaterial.POTTED_TORCHFLOWER
   );
   @NotNull
   public static final XTag<XMaterial> FLOWERS = XTag.TagBuilder.of(XMaterial.FLOWERING_AZALEA, XMaterial.FLOWERING_AZALEA_LEAVES)
      .inheritFrom(XTag.SMALL_FLOWERS, XTag.TALL_FLOWERS)
      .build();
   @NotNull
   public static final XTag<XMaterial> FOX_FOOD = XTag.TagBuilder.simple(XMaterial.GLOW_BERRIES, XMaterial.SWEET_BERRIES);
   @NotNull
   public static final XTag<XMaterial> FOXES_SPAWNABLE_ON = XTag.TagBuilder.simple(
      XMaterial.SNOW, XMaterial.SNOW_BLOCK, XMaterial.PODZOL, XMaterial.GRASS_BLOCK, XMaterial.COARSE_DIRT
   );
   @NotNull
   public static final XTag<XMaterial> FREEZE_IMMUNE_WEARABLES = XTag.TagBuilder.simple(
      XMaterial.LEATHER_BOOTS, XMaterial.LEATHER_CHESTPLATE, XMaterial.LEATHER_HELMET, XMaterial.LEATHER_LEGGINGS, XMaterial.LEATHER_HORSE_ARMOR
   );
   @NotNull
   public static final XTag<XMaterial> GEODE_INVALID_BLOCKS = XTag.TagBuilder.simple(
      XMaterial.BEDROCK, XMaterial.WATER, XMaterial.LAVA, XMaterial.ICE, XMaterial.PACKED_ICE, XMaterial.BLUE_ICE
   );
   @NotNull
   public static final XTag<XMaterial> GLASS = XTag.TagBuilder.of(findAllColors("GLASS")).inheritFrom(XTag.TagBuilder.simple(XMaterial.TINTED_GLASS)).build();
   @NotNull
   public static final XTag<XMaterial> GLAZED_TERRACOTTA = XTag.TagBuilder.simple(findAllColors("GLAZED_TERRACOTTA"));
   @NotNull
   public static final XTag<XMaterial> GOATS_SPAWNABLE_ON = XTag.TagBuilder.of(XMaterial.GRAVEL, XMaterial.STONE, XMaterial.PACKED_ICE)
      .inheritFrom(XTag.SNOW)
      .build();
   @NotNull
   public static final XTag<XMaterial> GOLD_ORES = XTag.TagBuilder.simple(XMaterial.GOLD_ORE, XMaterial.DEEPSLATE_GOLD_ORE, XMaterial.NETHER_GOLD_ORE);
   @NotNull
   public static final XTag<XMaterial> GUARDED_BY_PIGLINS = XTag.TagBuilder.of(
         XMaterial.GOLD_BLOCK,
         XMaterial.ENDER_CHEST,
         XMaterial.RAW_GOLD_BLOCK,
         XMaterial.GILDED_BLACKSTONE,
         XMaterial.CHEST,
         XMaterial.BARREL,
         XMaterial.TRAPPED_CHEST
      )
      .inheritFrom(XTag.SHULKER_BOXES, GOLD_ORES)
      .build();
   @NotNull
   public static final XTag<XMaterial> HANGING_SIGNS = XTag.TagBuilder.simple(findAllWoodTypes("HANGING_SIGN"));
   @NotNull
   public static final XTag<XMaterial> HOGLIN_REPELLENTS = XTag.TagBuilder.simple(
      XMaterial.WARPED_FUNGUS, XMaterial.NETHER_PORTAL, XMaterial.POTTED_WARPED_FUNGUS, XMaterial.RESPAWN_ANCHOR
   );
   @NotNull
   public static final XTag<XMaterial> ICE = XTag.TagBuilder.simple(XMaterial.ICE, XMaterial.PACKED_ICE, XMaterial.BLUE_ICE, XMaterial.FROSTED_ICE);
   @NotNull
   public static final XTag<XMaterial> IGNORED_BY_PIGLIN_BABIES = XTag.TagBuilder.simple(XMaterial.LEATHER);
   @NotNull
   public static final XTag<XMaterial> IMPERMEABLE = XTag.TagBuilder.simple(GLASS);
   @NotNull
   public static final XTag<XMaterial> INFINIBURN_END = XTag.TagBuilder.simple(XMaterial.BEDROCK, XMaterial.NETHERRACK, XMaterial.MAGMA_BLOCK);
   @NotNull
   public static final XTag<XMaterial> INFINIBURN_NETHER = XTag.TagBuilder.simple(XMaterial.NETHERRACK, XMaterial.MAGMA_BLOCK);
   @NotNull
   public static final XTag<XMaterial> INFINIBURN_OVERWORLD = XTag.TagBuilder.simple(XMaterial.NETHERRACK, XMaterial.MAGMA_BLOCK);
   @NotNull
   public static final XTag<XMaterial> INSIDE_STEP_SOUND_BLOCKS = XTag.TagBuilder.simple(XMaterial.SNOW, XMaterial.POWDER_SNOW);
   @NotNull
   public static final XTag<XMaterial> IRON_ORES = XTag.TagBuilder.simple(XMaterial.IRON_ORE, XMaterial.DEEPSLATE_IRON_ORE);
   @NotNull
   public static final XTag<XMaterial> ITEMS_ARROWS = XTag.TagBuilder.simple(XMaterial.ARROW, XMaterial.SPECTRAL_ARROW, XMaterial.TIPPED_ARROW);
   @NotNull
   public static final XTag<XMaterial> ITEMS_BANNERS = XTag.TagBuilder.simple(findAllColors("BANNER"));
   @NotNull
   public static final XTag<XMaterial> ITEMS_BEACON_PAYMENT_ITEMS = XTag.TagBuilder.simple(
      XMaterial.EMERALD, XMaterial.DIAMOND, XMaterial.NETHERITE_INGOT, XMaterial.IRON_INGOT, XMaterial.GOLD_INGOT
   );
   @NotNull
   public static final XTag<XMaterial> ITEMS_BOATS = XTag.TagBuilder.simple(
      XMaterial.OAK_BOAT,
      XMaterial.ACACIA_BOAT,
      XMaterial.DARK_OAK_BOAT,
      XMaterial.PALE_OAK_BOAT,
      XMaterial.BIRCH_BOAT,
      XMaterial.SPRUCE_BOAT,
      XMaterial.JUNGLE_BOAT,
      XMaterial.MANGROVE_BOAT,
      XMaterial.CHERRY_BOAT,
      XMaterial.BAMBOO_RAFT
   );
   @NotNull
   public static final XTag<XMaterial> ITEMS_COALS = XTag.TagBuilder.simple(XMaterial.COAL, XMaterial.CHARCOAL);
   @NotNull
   public static final XTag<XMaterial> ITEMS_CREEPER_DROP_MUSIC_DISCS = XTag.TagBuilder.simple(
      XMaterial.MUSIC_DISC_BLOCKS,
      XMaterial.MUSIC_DISC_11,
      XMaterial.MUSIC_DISC_WAIT,
      XMaterial.MUSIC_DISC_MELLOHI,
      XMaterial.MUSIC_DISC_STAL,
      XMaterial.MUSIC_DISC_WARD,
      XMaterial.MUSIC_DISC_13,
      XMaterial.MUSIC_DISC_CAT,
      XMaterial.MUSIC_DISC_CHIRP,
      XMaterial.MUSIC_DISC_MALL,
      XMaterial.MUSIC_DISC_FAR,
      XMaterial.MUSIC_DISC_STRAD
   );
   @NotNull
   public static final XTag<XMaterial> ITEMS_FISHES = XTag.TagBuilder.simple(
      XMaterial.TROPICAL_FISH, XMaterial.SALMON, XMaterial.PUFFERFISH, XMaterial.COOKED_COD, XMaterial.COD, XMaterial.COOKED_SALMON
   );
   @NotNull
   public static final XTag<XMaterial> ITEMS_FURNACE_MATERIALS = XTag.TagBuilder.of(XMaterial.COAL, XMaterial.CHARCOAL, XMaterial.COAL_BLOCK)
      .inheritFrom(XTag.LOGS, XTag.PLANKS)
      .build();
   @NotNull
   public static final XTag<XMaterial> ITEMS_LECTERN_BOOKS = XTag.TagBuilder.simple(XMaterial.WRITABLE_BOOK, XMaterial.WRITTEN_BOOK);
   @NotNull
   public static final XTag<XMaterial> ITEMS_MUSIC_DISCS = XTag.TagBuilder.of(XMaterial.MUSIC_DISC_OTHERSIDE, XMaterial.MUSIC_DISC_PIGSTEP)
      .inheritFrom(ITEMS_CREEPER_DROP_MUSIC_DISCS)
      .build();
   @NotNull
   public static final XTag<XMaterial> ITEMS_PIGLIN_LOVED = XTag.TagBuilder.of(
         XMaterial.GOLD_BLOCK,
         XMaterial.RAW_GOLD,
         XMaterial.GLISTERING_MELON_SLICE,
         XMaterial.GOLDEN_HORSE_ARMOR,
         XMaterial.LIGHT_WEIGHTED_PRESSURE_PLATE,
         XMaterial.GOLDEN_SWORD,
         XMaterial.GOLDEN_AXE,
         XMaterial.BELL,
         XMaterial.ENCHANTED_GOLDEN_APPLE,
         XMaterial.RAW_GOLD_BLOCK,
         XMaterial.GILDED_BLACKSTONE,
         XMaterial.CLOCK,
         XMaterial.GOLDEN_CARROT,
         XMaterial.GOLDEN_APPLE,
         XMaterial.GOLDEN_SHOVEL,
         XMaterial.GOLDEN_PICKAXE,
         XMaterial.GOLDEN_HOE,
         XMaterial.GOLD_INGOT
      )
      .inheritFrom(GOLD_ORES, XTag.GOLDEN_ARMOR_PIECES)
      .build();
   @NotNull
   public static final XTag<XMaterial> ITEMS_STONE_TOOL_MATERIALS = XTag.TagBuilder.simple(
      XMaterial.COBBLED_DEEPSLATE, XMaterial.BLACKSTONE, XMaterial.COBBLESTONE
   );
   @NotNull
   public static final XTag<XMaterial> WALL_BANNERS = XTag.TagBuilder.simple(findAllColors("WALL_BANNER"));
   @NotNull
   public static final XTag<XMaterial> JUNGLE_LOGS = XTag.TagBuilder.simple(
      XMaterial.STRIPPED_JUNGLE_LOG, XMaterial.JUNGLE_LOG, XMaterial.JUNGLE_WOOD, XMaterial.STRIPPED_JUNGLE_WOOD
   );
   @NotNull
   public static final XTag<XMaterial> LAPIS_ORES = XTag.TagBuilder.simple(XMaterial.LAPIS_ORE, XMaterial.DEEPSLATE_LAPIS_ORE);
   @NotNull
   public static final XTag<XMaterial> LAVA_POOL_STONE_CANNOT_REPLACE = XTag.TagBuilder.simple(
      XMaterial.DARK_OAK_LEAVES,
      XMaterial.STRIPPED_DARK_OAK_WOOD,
      XMaterial.STRIPPED_PALE_OAK_WOOD,
      XMaterial.OAK_WOOD,
      XMaterial.CRIMSON_HYPHAE,
      XMaterial.JUNGLE_LEAVES,
      XMaterial.MANGROVE_LEAVES,
      XMaterial.CHERRY_LEAVES,
      XMaterial.DARK_OAK_WOOD,
      XMaterial.STRIPPED_ACACIA_LOG,
      XMaterial.DARK_OAK_LOG,
      XMaterial.STRIPPED_DARK_OAK_LOG,
      XMaterial.AZALEA_LEAVES,
      XMaterial.SPAWNER,
      XMaterial.JUNGLE_LOG,
      XMaterial.SPRUCE_LOG,
      XMaterial.MANGROVE_LOG,
      XMaterial.CHERRY_LOG,
      XMaterial.STRIPPED_CRIMSON_HYPHAE,
      XMaterial.SPRUCE_LEAVES,
      XMaterial.STRIPPED_BIRCH_LOG,
      XMaterial.PALE_OAK_WOOD,
      XMaterial.PALE_OAK_LOG,
      XMaterial.STRIPPED_PALE_OAK_LOG,
      XMaterial.ACACIA_LOG,
      XMaterial.STRIPPED_ACACIA_WOOD,
      XMaterial.CRIMSON_STEM,
      XMaterial.BIRCH_WOOD,
      XMaterial.STRIPPED_JUNGLE_WOOD,
      XMaterial.STRIPPED_MANGROVE_LOG,
      XMaterial.STRIPPED_CHERRY_LOG,
      XMaterial.WARPED_HYPHAE,
      XMaterial.CHEST,
      XMaterial.FLOWERING_AZALEA_LEAVES,
      XMaterial.STRIPPED_OAK_LOG,
      XMaterial.ACACIA_WOOD,
      XMaterial.BEDROCK,
      XMaterial.BIRCH_LEAVES,
      XMaterial.STRIPPED_CRIMSON_STEM,
      XMaterial.OAK_LEAVES,
      XMaterial.STRIPPED_BIRCH_WOOD,
      XMaterial.STRIPPED_MANGROVE_WOOD,
      XMaterial.STRIPPED_CHERRY_WOOD,
      XMaterial.STRIPPED_JUNGLE_LOG,
      XMaterial.WARPED_STEM,
      XMaterial.END_PORTAL_FRAME,
      XMaterial.SPRUCE_WOOD,
      XMaterial.STRIPPED_SPRUCE_LOG,
      XMaterial.STRIPPED_SPRUCE_WOOD,
      XMaterial.JUNGLE_WOOD,
      XMaterial.MANGROVE_WOOD,
      XMaterial.CHERRY_WOOD,
      XMaterial.STRIPPED_OAK_WOOD,
      XMaterial.STRIPPED_WARPED_STEM,
      XMaterial.OAK_LOG,
      XMaterial.ACACIA_LEAVES,
      XMaterial.STRIPPED_WARPED_HYPHAE,
      XMaterial.BIRCH_LOG
   );
   @NotNull
   public static final XTag<XMaterial> LEAVES = XTag.TagBuilder.simple(
      XMaterial.SPRUCE_LEAVES,
      XMaterial.ACACIA_LEAVES,
      XMaterial.DARK_OAK_LEAVES,
      XMaterial.AZALEA_LEAVES,
      XMaterial.JUNGLE_LEAVES,
      XMaterial.FLOWERING_AZALEA_LEAVES,
      XMaterial.BIRCH_LEAVES,
      XMaterial.OAK_LEAVES,
      XMaterial.MANGROVE_LEAVES,
      XMaterial.CHERRY_LEAVES
   );
   @NotNull
   public static final XTag<XMaterial> LOGS = XTag.TagBuilder.simple(XTag.LOGS_THAT_BURN, CRIMSON_STEMS, XTag.WARPED_STEMS);
   @NotNull
   public static final XTag<XMaterial> LOGS_THAT_BURN = XTag.TagBuilder.simple(
      ACACIA_LOGS, XTag.OAK_LOGS, DARK_OAK_LOGS, XTag.PALE_OAK_LOGS, XTag.SPRUCE_LOGS, JUNGLE_LOGS, BIRCH_LOGS, XTag.MANGROVE_LOGS, CHERRY_LOGS
   );
   @NotNull
   public static final XTag<XMaterial> LUSH_GROUND_REPLACEABLE = XTag.TagBuilder.of(XMaterial.GRAVEL, XMaterial.SAND, XMaterial.CLAY)
      .inheritFrom(CAVE_VINES, DIRT, BASE_STONE_OVERWORLD)
      .build();
   @NotNull
   public static final XTag<XMaterial> MANGROVE_LOGS = XTag.TagBuilder.simple(
      XMaterial.STRIPPED_MANGROVE_LOG, XMaterial.MANGROVE_LOG, XMaterial.MANGROVE_WOOD, XMaterial.STRIPPED_MANGROVE_WOOD
   );
   @NotNull
   public static final XTag<XMaterial> MINEABLE_AXE = XTag.TagBuilder.of(
         XMaterial.COMPOSTER,
         XMaterial.COCOA,
         XMaterial.RED_MUSHROOM_BLOCK,
         XMaterial.CRAFTING_TABLE,
         XMaterial.TALL_GRASS,
         XMaterial.BIG_DRIPLEAF_STEM,
         XMaterial.RED_MUSHROOM,
         XMaterial.JUKEBOX,
         XMaterial.WARPED_FUNGUS,
         XMaterial.DEAD_BUSH,
         XMaterial.NOTE_BLOCK,
         XMaterial.CRIMSON_FUNGUS,
         XMaterial.MUSHROOM_STEM,
         XMaterial.CHORUS_PLANT,
         XMaterial.BEE_NEST,
         XMaterial.BROWN_MUSHROOM_BLOCK,
         XMaterial.JACK_O_LANTERN,
         XMaterial.FERN,
         XMaterial.NETHER_WART,
         XMaterial.CARTOGRAPHY_TABLE,
         XMaterial.CHEST,
         XMaterial.SWEET_BERRY_BUSH,
         XMaterial.BROWN_MUSHROOM,
         XMaterial.CARVED_PUMPKIN,
         XMaterial.SMITHING_TABLE,
         XMaterial.GLOW_LICHEN,
         XMaterial.SMALL_DRIPLEAF,
         XMaterial.LOOM,
         XMaterial.BEEHIVE,
         XMaterial.SHORT_GRASS,
         XMaterial.HANGING_ROOTS,
         XMaterial.CHORUS_FLOWER,
         XMaterial.ATTACHED_PUMPKIN_STEM,
         XMaterial.BIG_DRIPLEAF,
         XMaterial.DAYLIGHT_DETECTOR,
         XMaterial.SPORE_BLOSSOM,
         XMaterial.LILY_PAD,
         XMaterial.TRAPPED_CHEST,
         XMaterial.BARREL,
         XMaterial.LARGE_FERN,
         XMaterial.LECTERN,
         XMaterial.SUGAR_CANE,
         XMaterial.MELON,
         XMaterial.ATTACHED_MELON_STEM,
         XMaterial.PUMPKIN,
         XMaterial.BAMBOO,
         XMaterial.FLETCHING_TABLE,
         XMaterial.BOOKSHELF
      )
      .inheritFrom(
         XTag.WOODEN_STAIRS,
         XTag.WOODEN_SLABS,
         XTag.WOODEN_PRESSURE_PLATES,
         XTag.WOODEN_FENCES,
         XTag.WOODEN_FENCE_GATES,
         XTag.WOODEN_TRAPDOORS,
         XTag.WOODEN_DOORS,
         XTag.WOODEN_BUTTONS,
         BANNERS,
         XTag.SIGNS,
         CAVE_VINES,
         CROPS,
         LOGS,
         XTag.PLANKS,
         XTag.SAPLINGS,
         CLIMBABLE,
         CAMPFIRES
      )
      .build();
   @NotNull
   public static final XTag<XMaterial> MINEABLE_HOE = XTag.TagBuilder.simple(
      XMaterial.FLOWERING_AZALEA_LEAVES,
      XMaterial.DARK_OAK_LEAVES,
      XMaterial.PALE_OAK_LEAVES,
      XMaterial.SHROOMLIGHT,
      XMaterial.BIRCH_LEAVES,
      XMaterial.DRIED_KELP_BLOCK,
      XMaterial.JUNGLE_LEAVES,
      XMaterial.OAK_LEAVES,
      XMaterial.MOSS_CARPET,
      XMaterial.WET_SPONGE,
      XMaterial.AZALEA_LEAVES,
      XMaterial.NETHER_WART_BLOCK,
      XMaterial.WARPED_WART_BLOCK,
      XMaterial.SPONGE,
      XMaterial.SPRUCE_LEAVES,
      XMaterial.SCULK_SENSOR,
      XMaterial.HAY_BLOCK,
      XMaterial.TARGET,
      XMaterial.ACACIA_LEAVES,
      XMaterial.MANGROVE_LEAVES,
      XMaterial.CHERRY_LEAVES,
      XMaterial.MOSS_BLOCK
   );
   @NotNull
   public static final XTag<XMaterial> MINEABLE_PICKAXE = XTag.TagBuilder.of(
         XMaterial.OXIDIZED_CUT_COPPER,
         XMaterial.GOLD_BLOCK,
         XMaterial.SMOOTH_SANDSTONE,
         XMaterial.IRON_DOOR,
         XMaterial.COBBLESTONE,
         XMaterial.DRIPSTONE_BLOCK,
         XMaterial.CHISELED_SANDSTONE,
         XMaterial.INFESTED_STONE_BRICKS,
         XMaterial.QUARTZ_BLOCK,
         XMaterial.COPPER_BLOCK,
         XMaterial.STONE_BRICKS,
         XMaterial.CHISELED_POLISHED_BLACKSTONE,
         XMaterial.DISPENSER,
         XMaterial.DEEPSLATE_BRICKS,
         XMaterial.HEAVY_WEIGHTED_PRESSURE_PLATE,
         XMaterial.OBSIDIAN,
         XMaterial.EXPOSED_CUT_COPPER,
         XMaterial.SMOOTH_QUARTZ,
         XMaterial.SMOOTH_RED_SANDSTONE,
         XMaterial.STONE,
         XMaterial.INFESTED_COBBLESTONE,
         XMaterial.WAXED_CUT_COPPER,
         XMaterial.PRISMARINE,
         XMaterial.PISTON,
         XMaterial.CUT_COPPER,
         XMaterial.CHISELED_QUARTZ_BLOCK,
         XMaterial.MOSSY_STONE_BRICKS,
         XMaterial.EMERALD_BLOCK,
         XMaterial.BELL,
         XMaterial.AMETHYST_BLOCK,
         XMaterial.GILDED_BLACKSTONE,
         XMaterial.CHISELED_NETHER_BRICKS,
         XMaterial.WAXED_COPPER_BLOCK,
         XMaterial.IRON_BLOCK,
         XMaterial.BUDDING_AMETHYST,
         XMaterial.POLISHED_DEEPSLATE,
         XMaterial.HOPPER,
         XMaterial.CUT_RED_SANDSTONE,
         XMaterial.QUARTZ_BRICKS,
         XMaterial.CHISELED_STONE_BRICKS,
         XMaterial.ENDER_CHEST,
         XMaterial.END_STONE_BRICKS,
         XMaterial.NETHERRACK,
         XMaterial.REDSTONE_BLOCK,
         XMaterial.WAXED_OXIDIZED_CUT_COPPER,
         XMaterial.LIGHT_WEIGHTED_PRESSURE_PLATE,
         XMaterial.WAXED_WEATHERED_CUT_COPPER,
         XMaterial.IRON_CHAIN,
         XMaterial.MAGMA_BLOCK,
         XMaterial.STONE_PRESSURE_PLATE,
         XMaterial.DARK_PRISMARINE,
         XMaterial.MEDIUM_AMETHYST_BUD,
         XMaterial.LANTERN,
         XMaterial.ICE,
         XMaterial.DIORITE,
         XMaterial.DROPPER,
         XMaterial.CRACKED_NETHER_BRICKS,
         XMaterial.BREWING_STAND,
         XMaterial.CHISELED_RED_SANDSTONE,
         XMaterial.CALCITE,
         XMaterial.CUT_SANDSTONE,
         XMaterial.POLISHED_BASALT,
         XMaterial.DEEPSLATE_TILES,
         XMaterial.QUARTZ_PILLAR,
         XMaterial.LODESTONE,
         XMaterial.POLISHED_GRANITE,
         XMaterial.POLISHED_ANDESITE,
         XMaterial.OBSERVER,
         XMaterial.CHISELED_DEEPSLATE,
         XMaterial.RAW_GOLD_BLOCK,
         XMaterial.CRACKED_POLISHED_BLACKSTONE_BRICKS,
         XMaterial.WAXED_EXPOSED_CUT_COPPER,
         XMaterial.SMALL_AMETHYST_BUD,
         XMaterial.OXIDIZED_COPPER,
         XMaterial.POLISHED_BLACKSTONE,
         XMaterial.RAW_IRON_BLOCK,
         XMaterial.POLISHED_BLACKSTONE_BRICKS,
         XMaterial.INFESTED_DEEPSLATE,
         XMaterial.RAW_COPPER_BLOCK,
         XMaterial.BLACKSTONE,
         XMaterial.AMETHYST_CLUSTER,
         XMaterial.GRINDSTONE,
         XMaterial.WAXED_EXPOSED_COPPER,
         XMaterial.RED_SANDSTONE,
         XMaterial.LIGHTNING_ROD,
         XMaterial.SOUL_LANTERN,
         XMaterial.POLISHED_BLACKSTONE_PRESSURE_PLATE,
         XMaterial.IRON_BARS,
         XMaterial.PURPUR_BLOCK,
         XMaterial.FURNACE,
         XMaterial.CONDUIT,
         XMaterial.SPAWNER,
         XMaterial.COAL_BLOCK,
         XMaterial.BONE_BLOCK,
         XMaterial.WARPED_NYLIUM,
         XMaterial.WEATHERED_COPPER,
         XMaterial.WEATHERED_CUT_COPPER,
         XMaterial.MOSSY_COBBLESTONE,
         XMaterial.SMOKER,
         XMaterial.COBBLED_DEEPSLATE,
         XMaterial.SMOOTH_BASALT,
         XMaterial.STONE_BUTTON,
         XMaterial.NETHER_BRICKS,
         XMaterial.BRICKS,
         XMaterial.RED_NETHER_BRICKS,
         XMaterial.SMOOTH_STONE,
         XMaterial.ANDESITE,
         XMaterial.BASALT,
         XMaterial.TUFF,
         XMaterial.END_STONE,
         XMaterial.WAXED_OXIDIZED_COPPER,
         XMaterial.INFESTED_CHISELED_STONE_BRICKS,
         XMaterial.PRISMARINE_BRICKS,
         XMaterial.CRYING_OBSIDIAN,
         XMaterial.CRACKED_DEEPSLATE_TILES,
         XMaterial.INFESTED_STONE,
         XMaterial.IRON_TRAPDOOR,
         XMaterial.INFESTED_MOSSY_STONE_BRICKS,
         XMaterial.RESPAWN_ANCHOR,
         XMaterial.BLUE_ICE,
         XMaterial.POLISHED_DIORITE,
         XMaterial.NETHER_BRICK_FENCE,
         XMaterial.INFESTED_CRACKED_STONE_BRICKS,
         XMaterial.SANDSTONE,
         XMaterial.EXPOSED_COPPER,
         XMaterial.WAXED_WEATHERED_COPPER,
         XMaterial.CRACKED_DEEPSLATE_BRICKS,
         XMaterial.LARGE_AMETHYST_BUD,
         XMaterial.PISTON_HEAD,
         XMaterial.NETHERITE_BLOCK,
         XMaterial.PURPUR_PILLAR,
         XMaterial.GRANITE,
         XMaterial.STONECUTTER,
         XMaterial.BLAST_FURNACE,
         XMaterial.ENCHANTING_TABLE,
         XMaterial.LAPIS_BLOCK,
         XMaterial.PACKED_ICE,
         XMaterial.CRACKED_STONE_BRICKS,
         XMaterial.DEEPSLATE,
         XMaterial.CRIMSON_NYLIUM,
         XMaterial.STICKY_PISTON,
         XMaterial.DIAMOND_BLOCK,
         XMaterial.POINTED_DRIPSTONE
      )
      .inheritFrom(
         XTag.TERRACOTTA,
         GLAZED_TERRACOTTA,
         XTag.WALLS,
         CORALS,
         XTag.SHULKER_BOXES,
         XTag.RAILS,
         DIAMOND_ORES,
         GOLD_ORES,
         IRON_ORES,
         EMERALD_ORES,
         COPPER_ORES,
         ANVIL,
         CONCRETE,
         XTag.NON_WOODEN_STAIRS,
         XTag.NON_WOODEN_SLABS,
         CAULDRONS
      )
      .build();
   @NotNull
   public static final XTag<XMaterial> MINEABLE_SHOVEL = XTag.TagBuilder.of(
         XMaterial.FARMLAND,
         XMaterial.DIRT_PATH,
         XMaterial.SNOW,
         XMaterial.SNOW_BLOCK,
         XMaterial.RED_SAND,
         XMaterial.COARSE_DIRT,
         XMaterial.SOUL_SAND,
         XMaterial.GRAVEL,
         XMaterial.SAND,
         XMaterial.PODZOL,
         XMaterial.DIRT,
         XMaterial.CLAY,
         XMaterial.ROOTED_DIRT,
         XMaterial.MYCELIUM,
         XMaterial.SOUL_SOIL,
         XMaterial.GRASS_BLOCK
      )
      .inheritFrom(CONCRETE_POWDER)
      .build();
   @NotNull
   public static final XTag<XMaterial> MOOSHROOMS_SPAWNABLE_ON = XTag.TagBuilder.simple(XMaterial.MYCELIUM);
   @NotNull
   public static final XTag<XMaterial> MOSS_REPLACEABLE = XTag.TagBuilder.simple(CAVE_VINES, DIRT, BASE_STONE_OVERWORLD);
   @NotNull
   public static final XTag<XMaterial> MUSHROOM_GROW_BLOCK = XTag.TagBuilder.of(XMaterial.PODZOL, XMaterial.MYCELIUM).inheritFrom(XTag.NYLIUM).build();
   @NotNull
   public static final XTag<XMaterial> NEEDS_DIAMOND_TOOL = XTag.TagBuilder.simple(
      XMaterial.OBSIDIAN, XMaterial.NETHERITE_BLOCK, XMaterial.ANCIENT_DEBRIS, XMaterial.RESPAWN_ANCHOR, XMaterial.CRYING_OBSIDIAN
   );
   @NotNull
   public static final XTag<XMaterial> NEEDS_IRON_TOOL = XTag.TagBuilder.simple(
      XMaterial.GOLD_ORE,
      XMaterial.GOLD_BLOCK,
      XMaterial.REDSTONE_ORE,
      XMaterial.RAW_GOLD_BLOCK,
      XMaterial.EMERALD_BLOCK,
      XMaterial.DIAMOND_BLOCK,
      XMaterial.DIAMOND_ORE,
      XMaterial.DEEPSLATE_EMERALD_ORE,
      XMaterial.DEEPSLATE_GOLD_ORE,
      XMaterial.EMERALD_ORE,
      XMaterial.DEEPSLATE_REDSTONE_ORE,
      XMaterial.DEEPSLATE_DIAMOND_ORE
   );
   @NotNull
   public static final XTag<XMaterial> NEEDS_STONE_TOOL = XTag.TagBuilder.simple(
      XMaterial.OXIDIZED_CUT_COPPER,
      XMaterial.DEEPSLATE_COPPER_ORE,
      XMaterial.EXPOSED_CUT_COPPER_SLAB,
      XMaterial.WAXED_OXIDIZED_CUT_COPPER_SLAB,
      XMaterial.WAXED_OXIDIZED_CUT_COPPER,
      XMaterial.OXIDIZED_CUT_COPPER_SLAB,
      XMaterial.WAXED_WEATHERED_CUT_COPPER,
      XMaterial.WAXED_WEATHERED_CUT_COPPER_STAIRS,
      XMaterial.WEATHERED_COPPER,
      XMaterial.WEATHERED_CUT_COPPER_STAIRS,
      XMaterial.EXPOSED_CUT_COPPER,
      XMaterial.DEEPSLATE_LAPIS_ORE,
      XMaterial.COPPER_ORE,
      XMaterial.WEATHERED_CUT_COPPER,
      XMaterial.WAXED_CUT_COPPER_STAIRS,
      XMaterial.WAXED_EXPOSED_CUT_COPPER,
      XMaterial.OXIDIZED_COPPER,
      XMaterial.WAXED_COPPER_BLOCK,
      XMaterial.RAW_IRON_BLOCK,
      XMaterial.LAPIS_BLOCK,
      XMaterial.DEEPSLATE_IRON_ORE,
      XMaterial.CUT_COPPER_STAIRS,
      XMaterial.COPPER_BLOCK,
      XMaterial.WAXED_WEATHERED_CUT_COPPER_SLAB,
      XMaterial.IRON_BLOCK,
      XMaterial.WAXED_EXPOSED_CUT_COPPER_STAIRS,
      XMaterial.RAW_COPPER_BLOCK,
      XMaterial.LAPIS_ORE,
      XMaterial.WEATHERED_CUT_COPPER_SLAB,
      XMaterial.CUT_COPPER_SLAB,
      XMaterial.IRON_ORE,
      XMaterial.EXPOSED_COPPER,
      XMaterial.WAXED_EXPOSED_COPPER,
      XMaterial.EXPOSED_CUT_COPPER_STAIRS,
      XMaterial.WAXED_CUT_COPPER_SLAB,
      XMaterial.WAXED_EXPOSED_CUT_COPPER_SLAB,
      XMaterial.OXIDIZED_CUT_COPPER_STAIRS,
      XMaterial.WAXED_OXIDIZED_COPPER,
      XMaterial.WAXED_CUT_COPPER,
      XMaterial.WAXED_WEATHERED_COPPER,
      XMaterial.LIGHTNING_ROD,
      XMaterial.WAXED_OXIDIZED_CUT_COPPER_STAIRS,
      XMaterial.CUT_COPPER
   );
   @NotNull
   public static final XTag<XMaterial> NON_FLAMMABLE_WOOD = XTag.TagBuilder.simple(
      XMaterial.CRIMSON_PLANKS,
      XMaterial.WARPED_WALL_SIGN,
      XMaterial.CRIMSON_FENCE_GATE,
      XMaterial.WARPED_HYPHAE,
      XMaterial.CRIMSON_HYPHAE,
      XMaterial.WARPED_STEM,
      XMaterial.WARPED_TRAPDOOR,
      XMaterial.STRIPPED_CRIMSON_HYPHAE,
      XMaterial.CRIMSON_PRESSURE_PLATE,
      XMaterial.WARPED_STAIRS,
      XMaterial.CRIMSON_SIGN,
      XMaterial.CRIMSON_STAIRS,
      XMaterial.STRIPPED_WARPED_STEM,
      XMaterial.CRIMSON_FENCE,
      XMaterial.WARPED_FENCE,
      XMaterial.CRIMSON_TRAPDOOR,
      XMaterial.STRIPPED_WARPED_HYPHAE,
      XMaterial.WARPED_DOOR,
      XMaterial.WARPED_PRESSURE_PLATE,
      XMaterial.WARPED_PLANKS,
      XMaterial.STRIPPED_CRIMSON_STEM,
      XMaterial.CRIMSON_STEM,
      XMaterial.CRIMSON_SLAB,
      XMaterial.CRIMSON_WALL_SIGN,
      XMaterial.WARPED_FENCE_GATE,
      XMaterial.WARPED_BUTTON,
      XMaterial.WARPED_SLAB,
      XMaterial.CRIMSON_DOOR,
      XMaterial.CRIMSON_BUTTON,
      XMaterial.WARPED_SIGN
   );
   @NotNull
   public static final XTag<XMaterial> NON_WOODEN_STAIRS = XTag.TagBuilder.simple(
      XMaterial.STONE_BRICK_STAIRS,
      XMaterial.STONE_STAIRS,
      XMaterial.POLISHED_BLACKSTONE_BRICK_STAIRS,
      XMaterial.RED_SANDSTONE_STAIRS,
      XMaterial.PRISMARINE_STAIRS,
      XMaterial.GRANITE_STAIRS,
      XMaterial.WAXED_WEATHERED_CUT_COPPER_STAIRS,
      XMaterial.POLISHED_DIORITE_STAIRS,
      XMaterial.WEATHERED_CUT_COPPER_STAIRS,
      XMaterial.NETHER_BRICK_STAIRS,
      XMaterial.RED_NETHER_BRICK_STAIRS,
      XMaterial.PRISMARINE_BRICK_STAIRS,
      XMaterial.WAXED_CUT_COPPER_STAIRS,
      XMaterial.DEEPSLATE_TILE_STAIRS,
      XMaterial.POLISHED_ANDESITE_STAIRS,
      XMaterial.SMOOTH_RED_SANDSTONE_STAIRS,
      XMaterial.PURPUR_STAIRS,
      XMaterial.POLISHED_DEEPSLATE_STAIRS,
      XMaterial.QUARTZ_STAIRS,
      XMaterial.MOSSY_COBBLESTONE_STAIRS,
      XMaterial.BRICK_STAIRS,
      XMaterial.CUT_COPPER_STAIRS,
      XMaterial.SANDSTONE_STAIRS,
      XMaterial.ANDESITE_STAIRS,
      XMaterial.WAXED_EXPOSED_CUT_COPPER_STAIRS,
      XMaterial.COBBLED_DEEPSLATE_STAIRS,
      XMaterial.COBBLESTONE_STAIRS,
      XMaterial.DEEPSLATE_BRICK_STAIRS,
      XMaterial.DIORITE_STAIRS,
      XMaterial.SMOOTH_QUARTZ_STAIRS,
      XMaterial.EXPOSED_CUT_COPPER_STAIRS,
      XMaterial.DARK_PRISMARINE_STAIRS,
      XMaterial.OXIDIZED_CUT_COPPER_STAIRS,
      XMaterial.POLISHED_BLACKSTONE_STAIRS,
      XMaterial.POLISHED_GRANITE_STAIRS,
      XMaterial.MOSSY_STONE_BRICK_STAIRS,
      XMaterial.END_STONE_BRICK_STAIRS,
      XMaterial.WAXED_OXIDIZED_CUT_COPPER_STAIRS,
      XMaterial.SMOOTH_SANDSTONE_STAIRS,
      XMaterial.BLACKSTONE_STAIRS
   );
   @NotNull
   public static final XTag<XMaterial> NON_WOODEN_SLABS = XTag.TagBuilder.simple(
      XMaterial.MOSSY_COBBLESTONE_SLAB,
      XMaterial.EXPOSED_CUT_COPPER_SLAB,
      XMaterial.SMOOTH_QUARTZ_SLAB,
      XMaterial.COBBLESTONE_SLAB,
      XMaterial.POLISHED_BLACKSTONE_SLAB,
      XMaterial.OXIDIZED_CUT_COPPER_SLAB,
      XMaterial.POLISHED_ANDESITE_SLAB,
      XMaterial.RED_SANDSTONE_SLAB,
      XMaterial.BLACKSTONE_SLAB,
      XMaterial.STONE_SLAB,
      XMaterial.SMOOTH_SANDSTONE_SLAB,
      XMaterial.COBBLED_DEEPSLATE_SLAB,
      XMaterial.SMOOTH_RED_SANDSTONE_SLAB,
      XMaterial.POLISHED_DIORITE_SLAB,
      XMaterial.PRISMARINE_BRICK_SLAB,
      XMaterial.QUARTZ_SLAB,
      XMaterial.DIORITE_SLAB,
      XMaterial.NETHER_BRICK_SLAB,
      XMaterial.PRISMARINE_SLAB,
      XMaterial.WAXED_EXPOSED_CUT_COPPER_SLAB,
      XMaterial.RED_NETHER_BRICK_SLAB,
      XMaterial.POLISHED_BLACKSTONE_BRICK_SLAB,
      XMaterial.MOSSY_STONE_BRICK_SLAB,
      XMaterial.SMOOTH_STONE_SLAB,
      XMaterial.SANDSTONE_SLAB,
      XMaterial.WEATHERED_CUT_COPPER_SLAB,
      XMaterial.DEEPSLATE_BRICK_SLAB,
      XMaterial.POLISHED_DEEPSLATE_SLAB,
      XMaterial.GRANITE_SLAB,
      XMaterial.ANDESITE_SLAB,
      XMaterial.CUT_COPPER_SLAB,
      XMaterial.CUT_SANDSTONE_SLAB,
      XMaterial.END_STONE_BRICK_SLAB,
      XMaterial.WAXED_OXIDIZED_CUT_COPPER_SLAB,
      XMaterial.CUT_RED_SANDSTONE_SLAB,
      XMaterial.PURPUR_SLAB,
      XMaterial.STONE_BRICK_SLAB,
      XMaterial.WAXED_CUT_COPPER_SLAB,
      XMaterial.DEEPSLATE_TILE_SLAB,
      XMaterial.DARK_PRISMARINE_SLAB,
      XMaterial.PETRIFIED_OAK_SLAB,
      XMaterial.WAXED_WEATHERED_CUT_COPPER_SLAB,
      XMaterial.BRICK_SLAB,
      XMaterial.POLISHED_GRANITE_SLAB
   );
   @NotNull
   public static final XTag<XMaterial> NYLIUM = XTag.TagBuilder.simple(XMaterial.CRIMSON_NYLIUM, XMaterial.WARPED_NYLIUM);
   @NotNull
   public static final XTag<XMaterial> OAK_LOGS = XTag.TagBuilder.simple(
      XMaterial.STRIPPED_OAK_LOG, XMaterial.OAK_LOG, XMaterial.OAK_WOOD, XMaterial.STRIPPED_OAK_WOOD
   );
   @NotNull
   public static final XTag<XMaterial> OCCLUDES_VIBRATION_SIGNALS = XTag.TagBuilder.simple(XTag.WOOL);
   @NotNull
   public static final XTag<XMaterial> ORES = XTag.TagBuilder.of(XMaterial.ANCIENT_DEBRIS, XMaterial.NETHER_QUARTZ_ORE)
      .inheritFrom(COAL_ORES, IRON_ORES, COPPER_ORES, XTag.REDSTONE_ORES, LAPIS_ORES, GOLD_ORES, DIAMOND_ORES, EMERALD_ORES)
      .build();
   @NotNull
   public static final XTag<XMaterial> PALE_OAK_LOGS = XTag.TagBuilder.simple(
      XMaterial.STRIPPED_PALE_OAK_LOG, XMaterial.PALE_OAK_LOG, XMaterial.PALE_OAK_WOOD, XMaterial.STRIPPED_PALE_OAK_WOOD
   );
   @NotNull
   public static final XTag<XMaterial> PARROTS_SPAWNABLE_ON = XTag.TagBuilder.of(XMaterial.GRASS_BLOCK).inheritFrom(AIR, LEAVES, LOGS).build();
   @NotNull
   public static final XTag<XMaterial> PIGLIN_FOOD = XTag.TagBuilder.simple(XMaterial.COOKED_PORKCHOP, XMaterial.PORKCHOP);
   @NotNull
   public static final XTag<XMaterial> PIGLIN_REPELLENTS = XTag.TagBuilder.simple(
      XMaterial.SOUL_WALL_TORCH, XMaterial.SOUL_TORCH, XMaterial.SOUL_CAMPFIRE, XMaterial.SOUL_LANTERN, XMaterial.SOUL_FIRE
   );
   @NotNull
   public static final XTag<XMaterial> PLANKS = XTag.TagBuilder.simple(findAllWoodTypes("PLANKS"));
   @NotNull
   public static final XTag<XMaterial> POLAR_BEARS_SPAWNABLE_ON_IN_FROZEN_OCEAN = XTag.TagBuilder.simple(XMaterial.ICE);
   @NotNull
   public static final XTag<XMaterial> PORTALS = XTag.TagBuilder.simple(XMaterial.END_GATEWAY, XMaterial.END_PORTAL, XMaterial.NETHER_PORTAL);
   @NotNull
   public static final XTag<XMaterial> POTTERY_SHERDS = XTag.TagBuilder.simple(
      XMaterial.ANGLER_POTTERY_SHERD,
      XMaterial.ARCHER_POTTERY_SHERD,
      XMaterial.ARMS_UP_POTTERY_SHERD,
      XMaterial.BLADE_POTTERY_SHERD,
      XMaterial.BREWER_POTTERY_SHERD,
      XMaterial.BURN_POTTERY_SHERD,
      XMaterial.DANGER_POTTERY_SHERD,
      XMaterial.EXPLORER_POTTERY_SHERD,
      XMaterial.FRIEND_POTTERY_SHERD,
      XMaterial.HEART_POTTERY_SHERD,
      XMaterial.HEARTBREAK_POTTERY_SHERD,
      XMaterial.HOWL_POTTERY_SHERD,
      XMaterial.MINER_POTTERY_SHERD,
      XMaterial.MOURNER_POTTERY_SHERD,
      XMaterial.PLENTY_POTTERY_SHERD,
      XMaterial.PRIZE_POTTERY_SHERD,
      XMaterial.SHEAF_POTTERY_SHERD,
      XMaterial.SHELTER_POTTERY_SHERD,
      XMaterial.SKULL_POTTERY_SHERD,
      XMaterial.SNORT_POTTERY_SHERD
   );
   @NotNull
   public static final XTag<XMaterial> PRESSURE_PLATES = XTag.TagBuilder.of(XMaterial.LIGHT_WEIGHTED_PRESSURE_PLATE, XMaterial.HEAVY_WEIGHTED_PRESSURE_PLATE)
      .inheritFrom(XTag.STONE_PRESSURE_PLATES, XTag.WOODEN_PRESSURE_PLATES)
      .build();
   @NotNull
   public static final XTag<XMaterial> PREVENT_MOB_SPAWNING_INSIDE = XTag.TagBuilder.simple(XTag.RAILS);
   @NotNull
   public static final XTag<XMaterial> RABBITS_SPAWNABLE_ON = XTag.TagBuilder.simple(
      XMaterial.GRASS_BLOCK, XMaterial.SNOW, XMaterial.SNOW_BLOCK, XMaterial.SAND
   );
   @NotNull
   public static final XTag<XMaterial> RAILS = XTag.TagBuilder.simple(XMaterial.RAIL, XMaterial.ACTIVATOR_RAIL, XMaterial.DETECTOR_RAIL, XMaterial.POWERED_RAIL);
   @NotNull
   public static final XTag<XMaterial> REDSTONE_ORES = XTag.TagBuilder.simple(XMaterial.REDSTONE_ORE, XMaterial.DEEPSLATE_REDSTONE_ORE);
   @NotNull
   public static final XTag<XMaterial> REPLACEABLE_PLANTS = XTag.TagBuilder.simple(
      XMaterial.FERN,
      XMaterial.GLOW_LICHEN,
      XMaterial.DEAD_BUSH,
      XMaterial.PEONY,
      XMaterial.TALL_GRASS,
      XMaterial.HANGING_ROOTS,
      XMaterial.VINE,
      XMaterial.SUNFLOWER,
      XMaterial.LARGE_FERN,
      XMaterial.LILAC,
      XMaterial.ROSE_BUSH,
      XMaterial.SHORT_GRASS
   );
   @NotNull
   public static final XTag<XMaterial> SAND = XTag.TagBuilder.simple(XMaterial.SAND, XMaterial.RED_SAND);
   @NotNull
   public static final XTag<XMaterial> SAPLINGS = XTag.TagBuilder.simple(
      XMaterial.ACACIA_SAPLING,
      XMaterial.JUNGLE_SAPLING,
      XMaterial.SPRUCE_SAPLING,
      XMaterial.DARK_OAK_SAPLING,
      XMaterial.PALE_OAK_SAPLING,
      XMaterial.AZALEA,
      XMaterial.OAK_SAPLING,
      XMaterial.FLOWERING_AZALEA,
      XMaterial.BIRCH_SAPLING,
      XMaterial.MANGROVE_PROPAGULE,
      XMaterial.CHERRY_SAPLING
   );
   @NotNull
   public static final XTag<XMaterial> SHULKER_BOXES = XTag.TagBuilder.simple(findAllColors("SHULKER_BOX"));
   @NotNull
   public static final XTag<XMaterial> SIGNS = XTag.TagBuilder.simple(XTag.WALL_SIGNS, XTag.STANDING_SIGNS);
   @NotNull
   public static final XTag<XMaterial> SMALL_DRIPLEAF_PLACEABLE = XTag.TagBuilder.simple(XMaterial.CLAY, XMaterial.MOSS_BLOCK);
   @NotNull
   public static final XTag<XMaterial> SMALL_FLOWERS = XTag.TagBuilder.simple(
      XMaterial.RED_TULIP,
      XMaterial.AZURE_BLUET,
      XMaterial.OXEYE_DAISY,
      XMaterial.BLUE_ORCHID,
      XMaterial.PINK_TULIP,
      XMaterial.POPPY,
      XMaterial.WHITE_TULIP,
      XMaterial.DANDELION,
      XMaterial.ALLIUM,
      XMaterial.CORNFLOWER,
      XMaterial.ORANGE_TULIP,
      XMaterial.LILY_OF_THE_VALLEY,
      XMaterial.WITHER_ROSE
   );
   @NotNull
   public static final XTag<XMaterial> SMITHING_TEMPLATES = XTag.TagBuilder.simple(
      XMaterial.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
      XMaterial.COAST_ARMOR_TRIM_SMITHING_TEMPLATE,
      XMaterial.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE,
      XMaterial.EYE_ARMOR_TRIM_SMITHING_TEMPLATE,
      XMaterial.HOST_ARMOR_TRIM_SMITHING_TEMPLATE,
      XMaterial.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE,
      XMaterial.RIB_ARMOR_TRIM_SMITHING_TEMPLATE,
      XMaterial.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE,
      XMaterial.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE,
      XMaterial.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE,
      XMaterial.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE,
      XMaterial.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE,
      XMaterial.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE,
      XMaterial.VEX_ARMOR_TRIM_SMITHING_TEMPLATE,
      XMaterial.WARD_ARMOR_TRIM_SMITHING_TEMPLATE,
      XMaterial.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE,
      XMaterial.WILD_ARMOR_TRIM_SMITHING_TEMPLATE
   );
   @NotNull
   public static final XTag<XMaterial> SNOW = XTag.TagBuilder.simple(XMaterial.SNOW_BLOCK, XMaterial.SNOW, XMaterial.POWDER_SNOW);
   @NotNull
   public static final XTag<XMaterial> SOUL_FIRE_BASE_BLOCKS = XTag.TagBuilder.simple(XMaterial.SOUL_SOIL, XMaterial.SOUL_SAND);
   @NotNull
   public static final XTag<XMaterial> SOUL_SPEED_BLOCKS = XTag.TagBuilder.simple(XMaterial.SOUL_SOIL, XMaterial.SOUL_SAND);
   @NotNull
   public static final XTag<XMaterial> SPRUCE_LOGS = XTag.TagBuilder.simple(
      XMaterial.STRIPPED_SPRUCE_LOG, XMaterial.SPRUCE_LOG, XMaterial.SPRUCE_WOOD, XMaterial.STRIPPED_SPRUCE_WOOD
   );
   @NotNull
   public static final XTag<XMaterial> STAIRS = XTag.TagBuilder.simple(NON_WOODEN_STAIRS, XTag.WOODEN_STAIRS);
   @NotNull
   public static final XTag<XMaterial> STANDING_SIGNS = XTag.TagBuilder.simple(findAllWoodTypes("SIGN"));
   @NotNull
   public static final XTag<XMaterial> STONE_BRICKS = XTag.TagBuilder.simple(
      XMaterial.CHISELED_STONE_BRICKS, XMaterial.CRACKED_STONE_BRICKS, XMaterial.MOSSY_STONE_BRICKS, XMaterial.STONE_BRICKS
   );
   @NotNull
   public static final XTag<XMaterial> STONE_ORE_REPLACEABLES = XTag.TagBuilder.simple(
      XMaterial.STONE, XMaterial.DIORITE, XMaterial.ANDESITE, XMaterial.GRANITE
   );
   @NotNull
   public static final XTag<XMaterial> STONE_PRESSURE_PLATES = XTag.TagBuilder.simple(
      XMaterial.STONE_PRESSURE_PLATE, XMaterial.POLISHED_BLACKSTONE_PRESSURE_PLATE
   );
   @NotNull
   public static final XTag<XMaterial> STRIDER_WARM_BLOCKS = XTag.TagBuilder.simple(XMaterial.LAVA);
   @NotNull
   public static final XTag<XMaterial> TALL_FLOWERS = XTag.TagBuilder.simple(XMaterial.PEONY, XMaterial.SUNFLOWER, XMaterial.LILAC, XMaterial.ROSE_BUSH);
   @NotNull
   public static final XTag<XMaterial> TERRACOTTA = XTag.TagBuilder.simple(findAllColors("TERRACOTTA"));
   @NotNull
   public static final XTag<XMaterial> TRAPDOORS = XTag.TagBuilder.of(XMaterial.IRON_TRAPDOOR).inheritFrom(XTag.WOODEN_TRAPDOORS).build();
   @NotNull
   public static final XTag<XMaterial> UNDERWATER_BONEMEALS = XTag.TagBuilder.of(XMaterial.SEAGRASS).inheritFrom(CORALS, ALIVE_CORAL_WALL_FANS).build();
   @NotNull
   public static final XTag<XMaterial> UNSTABLE_BOTTOM_CENTER = XTag.TagBuilder.simple(FENCE_GATES);
   @NotNull
   public static final XTag<XMaterial> VALID_SPAWN = XTag.TagBuilder.simple(XMaterial.PODZOL, XMaterial.GRASS_BLOCK);
   @NotNull
   public static final XTag<XMaterial> WALL_HANGING_SIGNS = XTag.TagBuilder.simple(findAllWoodTypes("WALL_HANGING_SIGN"));
   @NotNull
   public static final XTag<XMaterial> WALL_POST_OVERRIDE = XTag.TagBuilder.of(
         XMaterial.TORCH, XMaterial.TRIPWIRE, XMaterial.REDSTONE_TORCH, XMaterial.SOUL_TORCH
      )
      .inheritFrom(SIGNS, BANNERS, PRESSURE_PLATES)
      .build();
   @NotNull
   public static final XTag<XMaterial> WALL_SIGNS = XTag.TagBuilder.simple(findAllWoodTypes("WALL_SIGN"));
   @NotNull
   public static final XTag<XMaterial> WALL_TORCHES = XTag.TagBuilder.simple(XMaterial.WALL_TORCH, XMaterial.SOUL_WALL_TORCH, XMaterial.REDSTONE_WALL_TORCH);
   @NotNull
   public static final XTag<XMaterial> WALLS = XTag.TagBuilder.simple(
      XMaterial.POLISHED_DEEPSLATE_WALL,
      XMaterial.NETHER_BRICK_WALL,
      XMaterial.POLISHED_BLACKSTONE_WALL,
      XMaterial.DEEPSLATE_BRICK_WALL,
      XMaterial.RED_SANDSTONE_WALL,
      XMaterial.BRICK_WALL,
      XMaterial.COBBLESTONE_WALL,
      XMaterial.POLISHED_BLACKSTONE_BRICK_WALL,
      XMaterial.PRISMARINE_WALL,
      XMaterial.SANDSTONE_WALL,
      XMaterial.GRANITE_WALL,
      XMaterial.DEEPSLATE_TILE_WALL,
      XMaterial.BLACKSTONE_WALL,
      XMaterial.STONE_BRICK_WALL,
      XMaterial.RED_NETHER_BRICK_WALL,
      XMaterial.DIORITE_WALL,
      XMaterial.MOSSY_COBBLESTONE_WALL,
      XMaterial.ANDESITE_WALL,
      XMaterial.MOSSY_STONE_BRICK_WALL,
      XMaterial.END_STONE_BRICK_WALL,
      XMaterial.COBBLED_DEEPSLATE_WALL
   );
   @NotNull
   public static final XTag<XMaterial> WARPED_STEMS = XTag.TagBuilder.simple(
      XMaterial.WARPED_HYPHAE, XMaterial.STRIPPED_WARPED_STEM, XMaterial.WARPED_STEM, XMaterial.STRIPPED_WARPED_HYPHAE
   );
   @NotNull
   public static final XTag<XMaterial> WITHER_IMMUNE = XTag.TagBuilder.simple(
      XMaterial.STRUCTURE_BLOCK,
      XMaterial.END_GATEWAY,
      XMaterial.BEDROCK,
      XMaterial.END_PORTAL,
      XMaterial.COMMAND_BLOCK,
      XMaterial.REPEATING_COMMAND_BLOCK,
      XMaterial.MOVING_PISTON,
      XMaterial.CHAIN_COMMAND_BLOCK,
      XMaterial.BARRIER,
      XMaterial.END_PORTAL_FRAME,
      XMaterial.JIGSAW
   );
   @NotNull
   public static final XTag<XMaterial> WITHER_SUMMON_BASE_BLOCKS = XTag.TagBuilder.simple(XMaterial.SOUL_SOIL, XMaterial.SOUL_SAND);
   @NotNull
   public static final XTag<XMaterial> WOLVES_SPAWNABLE_ON = XTag.TagBuilder.simple(XMaterial.GRASS_BLOCK, XMaterial.SNOW, XMaterial.SNOW_BLOCK);
   @NotNull
   public static final XTag<XMaterial> WOODEN_BUTTONS = XTag.TagBuilder.simple(findAllWoodTypes("BUTTON"));
   @NotNull
   public static final XTag<XMaterial> WOODEN_DOORS = XTag.TagBuilder.simple(findAllWoodTypes("DOOR"));
   @NotNull
   public static final XTag<XMaterial> WOODEN_FENCE_GATES = XTag.TagBuilder.simple(findAllWoodTypes("FENCE_GATE"));
   @NotNull
   public static final XTag<XMaterial> WOODEN_FENCES = XTag.TagBuilder.simple(findAllWoodTypes("FENCE"));
   @NotNull
   public static final XTag<XMaterial> WOODEN_PRESSURE_PLATES = XTag.TagBuilder.simple(findAllWoodTypes("PRESSURE_PLATE"));
   @NotNull
   public static final XTag<XMaterial> WOODEN_SLABS = XTag.TagBuilder.simple(findAllWoodTypes("SLAB"));
   @NotNull
   public static final XTag<XMaterial> WOODEN_STAIRS = XTag.TagBuilder.simple(findAllWoodTypes("STAIRS"));
   @NotNull
   public static final XTag<XMaterial> WOODEN_TRAPDOORS = XTag.TagBuilder.simple(findAllWoodTypes("TRAPDOOR"));
   @NotNull
   public static final XTag<XMaterial> WOOL = XTag.TagBuilder.simple(findAllColors("WOOL"));
   @NotNull
   public static final XTag<XMaterial> LEATHER_ARMOR_PIECES = XTag.TagBuilder.simple(
      XMaterial.LEATHER_HELMET, XMaterial.LEATHER_CHESTPLATE, XMaterial.LEATHER_LEGGINGS, XMaterial.LEATHER_BOOTS
   );
   @NotNull
   public static final XTag<XMaterial> IRON_ARMOR_PIECES = XTag.TagBuilder.simple(
      XMaterial.IRON_HELMET, XMaterial.IRON_CHESTPLATE, XMaterial.IRON_LEGGINGS, XMaterial.IRON_BOOTS
   );
   @NotNull
   public static final XTag<XMaterial> CHAINMAIL_ARMOR_PIECES = XTag.TagBuilder.simple(
      XMaterial.CHAINMAIL_HELMET, XMaterial.CHAINMAIL_CHESTPLATE, XMaterial.CHAINMAIL_LEGGINGS, XMaterial.CHAINMAIL_BOOTS
   );
   @NotNull
   public static final XTag<XMaterial> GOLDEN_ARMOR_PIECES = XTag.TagBuilder.simple(
      XMaterial.GOLDEN_HELMET, XMaterial.GOLDEN_CHESTPLATE, XMaterial.GOLDEN_LEGGINGS, XMaterial.GOLDEN_BOOTS
   );
   @NotNull
   public static final XTag<XMaterial> DIAMOND_ARMOR_PIECES = XTag.TagBuilder.simple(
      XMaterial.DIAMOND_HELMET, XMaterial.DIAMOND_CHESTPLATE, XMaterial.DIAMOND_LEGGINGS, XMaterial.DIAMOND_BOOTS
   );
   @NotNull
   public static final XTag<XMaterial> NETHERITE_ARMOR_PIECES = XTag.TagBuilder.simple(
      XMaterial.NETHERITE_HELMET, XMaterial.NETHERITE_CHESTPLATE, XMaterial.NETHERITE_LEGGINGS, XMaterial.NETHERITE_BOOTS
   );
   @NotNull
   public static final XTag<XMaterial> ARMOR_PIECES = XTag.TagBuilder.of(XMaterial.TURTLE_HELMET)
      .inheritFrom(LEATHER_ARMOR_PIECES, CHAINMAIL_ARMOR_PIECES, IRON_ARMOR_PIECES, GOLDEN_ARMOR_PIECES, DIAMOND_ARMOR_PIECES, NETHERITE_ARMOR_PIECES)
      .build();
   @NotNull
   public static final XTag<XMaterial> WOODEN_TOOLS = XTag.TagBuilder.simple(
      XMaterial.WOODEN_PICKAXE, XMaterial.WOODEN_AXE, XMaterial.WOODEN_HOE, XMaterial.WOODEN_SHOVEL, XMaterial.WOODEN_SWORD
   );
   @NotNull
   public static final XTag<XMaterial> FLUID = XTag.TagBuilder.simple(XMaterial.LAVA, XMaterial.WATER);
   @NotNull
   public static final XTag<XMaterial> STONE_TOOLS = XTag.TagBuilder.simple(
      XMaterial.STONE_PICKAXE, XMaterial.STONE_AXE, XMaterial.STONE_HOE, XMaterial.STONE_SHOVEL, XMaterial.STONE_SWORD
   );
   @NotNull
   public static final XTag<XMaterial> IRON_TOOLS = XTag.TagBuilder.simple(
      XMaterial.IRON_PICKAXE, XMaterial.IRON_AXE, XMaterial.IRON_HOE, XMaterial.IRON_SHOVEL, XMaterial.IRON_SWORD
   );
   @NotNull
   public static final XTag<XMaterial> DIAMOND_TOOLS = XTag.TagBuilder.simple(
      XMaterial.DIAMOND_PICKAXE, XMaterial.DIAMOND_AXE, XMaterial.DIAMOND_HOE, XMaterial.DIAMOND_SHOVEL, XMaterial.DIAMOND_SWORD
   );
   @NotNull
   public static final XTag<XMaterial> NETHERITE_TOOLS = XTag.TagBuilder.simple(
      XMaterial.NETHERITE_PICKAXE, XMaterial.NETHERITE_AXE, XMaterial.NETHERITE_HOE, XMaterial.NETHERITE_SHOVEL, XMaterial.NETHERITE_SWORD
   );
   @NotNull
   public static final XTag<XMaterial> SWORDS = XTag.TagBuilder.simple(
      XMaterial.WOODEN_SWORD, XMaterial.STONE_SWORD, XMaterial.IRON_SWORD, XMaterial.GOLDEN_SWORD, XMaterial.DIAMOND_SWORD, XMaterial.NETHERITE_SWORD
   );
   @NotNull
   public static final XTag<XMaterial> PICKAXES = XTag.TagBuilder.simple(
      XMaterial.WOODEN_PICKAXE,
      XMaterial.STONE_PICKAXE,
      XMaterial.IRON_PICKAXE,
      XMaterial.GOLDEN_PICKAXE,
      XMaterial.DIAMOND_PICKAXE,
      XMaterial.NETHERITE_PICKAXE
   );
   @NotNull
   public static final XTag<XMaterial> AXES = XTag.TagBuilder.simple(
      XMaterial.WOODEN_AXE, XMaterial.STONE_AXE, XMaterial.IRON_AXE, XMaterial.GOLDEN_AXE, XMaterial.DIAMOND_AXE, XMaterial.NETHERITE_AXE
   );
   @NotNull
   public static final XTag<XMaterial> SHOVELS = XTag.TagBuilder.simple(
      XMaterial.WOODEN_SHOVEL, XMaterial.STONE_SHOVEL, XMaterial.IRON_SHOVEL, XMaterial.GOLDEN_SHOVEL, XMaterial.DIAMOND_SHOVEL, XMaterial.NETHERITE_SHOVEL
   );
   @NotNull
   public static final XTag<XMaterial> HOES = XTag.TagBuilder.simple(
      XMaterial.WOODEN_HOE, XMaterial.STONE_HOE, XMaterial.IRON_HOE, XMaterial.GOLDEN_HOE, XMaterial.DIAMOND_HOE, XMaterial.NETHERITE_HOE
   );
   @NotNull
   public static final XTag<XMaterial> DANGEROUS_BLOCKS = XTag.TagBuilder.simple(
      XMaterial.MAGMA_BLOCK, XMaterial.LAVA, XMaterial.CAMPFIRE, XMaterial.FIRE, XMaterial.SOUL_FIRE
   );
   @NotNull
   public static final XTag<XEnchantment> ARMOR_ENCHANTS = XTag.TagBuilder.simple(
      XEnchantment.BLAST_PROTECTION,
      XEnchantment.BINDING_CURSE,
      XEnchantment.VANISHING_CURSE,
      XEnchantment.FIRE_PROTECTION,
      XEnchantment.MENDING,
      XEnchantment.PROJECTILE_PROTECTION,
      XEnchantment.PROTECTION,
      XEnchantment.THORNS,
      XEnchantment.UNBREAKING
   );
   @NotNull
   public static final XTag<XEnchantment> HELEMT_ENCHANTS = XTag.TagBuilder.of(XEnchantment.AQUA_AFFINITY, XEnchantment.RESPIRATION)
      .inheritFrom(ARMOR_ENCHANTS)
      .build();
   @NotNull
   public static final XTag<XEnchantment> CHESTPLATE_ENCHANTS = XTag.TagBuilder.simple(ARMOR_ENCHANTS);
   @NotNull
   public static final XTag<XEnchantment> LEGGINGS_ENCHANTS = XTag.TagBuilder.simple(ARMOR_ENCHANTS);
   @NotNull
   public static final XTag<XEnchantment> BOOTS_ENCHANTS = XTag.TagBuilder.of(
         XEnchantment.DEPTH_STRIDER, XEnchantment.FEATHER_FALLING, XEnchantment.FROST_WALKER
      )
      .inheritFrom(ARMOR_ENCHANTS)
      .build();
   @NotNull
   public static final XTag<XEnchantment> ELYTRA_ENCHANTS = XTag.TagBuilder.simple(
      XEnchantment.BINDING_CURSE, XEnchantment.VANISHING_CURSE, XEnchantment.MENDING, XEnchantment.UNBREAKING
   );
   @NotNull
   public static final XTag<XEnchantment> SWORD_ENCHANTS = XTag.TagBuilder.simple(
      XEnchantment.BANE_OF_ARTHROPODS,
      XEnchantment.VANISHING_CURSE,
      XEnchantment.FIRE_ASPECT,
      XEnchantment.KNOCKBACK,
      XEnchantment.LOOTING,
      XEnchantment.MENDING,
      XEnchantment.SHARPNESS,
      XEnchantment.SMITE,
      XEnchantment.SWEEPING_EDGE,
      XEnchantment.UNBREAKING
   );
   @NotNull
   public static final XTag<XEnchantment> AXE_ENCHANTS = XTag.TagBuilder.simple(
      XEnchantment.BANE_OF_ARTHROPODS,
      XEnchantment.VANISHING_CURSE,
      XEnchantment.EFFICIENCY,
      XEnchantment.FORTUNE,
      XEnchantment.MENDING,
      XEnchantment.SHARPNESS,
      XEnchantment.SILK_TOUCH,
      XEnchantment.SMITE,
      XEnchantment.UNBREAKING
   );
   @NotNull
   public static final XTag<XEnchantment> HOE_ENCHANTS = XTag.TagBuilder.simple(
      XEnchantment.VANISHING_CURSE, XEnchantment.EFFICIENCY, XEnchantment.FORTUNE, XEnchantment.MENDING, XEnchantment.SILK_TOUCH, XEnchantment.UNBREAKING
   );
   @NotNull
   public static final XTag<XEnchantment> PICKAXE_ENCHANTS = XTag.TagBuilder.simple(
      XEnchantment.VANISHING_CURSE, XEnchantment.EFFICIENCY, XEnchantment.FORTUNE, XEnchantment.MENDING, XEnchantment.SILK_TOUCH, XEnchantment.UNBREAKING
   );
   @NotNull
   public static final XTag<XEnchantment> SHOVEL_ENCHANTS = XTag.TagBuilder.simple(
      XEnchantment.VANISHING_CURSE, XEnchantment.EFFICIENCY, XEnchantment.FORTUNE, XEnchantment.MENDING, XEnchantment.SILK_TOUCH, XEnchantment.UNBREAKING
   );
   @NotNull
   public static final XTag<XEnchantment> SHEARS_ENCHANTS = XTag.TagBuilder.of(
         XEnchantment.VANISHING_CURSE, XEnchantment.EFFICIENCY, XEnchantment.MENDING, XEnchantment.UNBREAKING
      )
      .build();
   @NotNull
   public static final XTag<XEnchantment> BOW_ENCHANTS = XTag.TagBuilder.simple(
      XEnchantment.VANISHING_CURSE, XEnchantment.FLAME, XEnchantment.INFINITY, XEnchantment.MENDING, XEnchantment.PUNCH, XEnchantment.UNBREAKING
   );
   @NotNull
   public static final XTag<XEnchantment> CROSSBOW_ENCHANTS = XTag.TagBuilder.simple(
      XEnchantment.VANISHING_CURSE, XEnchantment.MENDING, XEnchantment.MULTISHOT, XEnchantment.PIERCING, XEnchantment.QUICK_CHARGE, XEnchantment.UNBREAKING
   );
   @NotNull
   public static final XTag<XEntityType> EFFECTIVE_SMITE_ENTITIES = XTag.TagBuilder.simple(
      XEntityType.ZOMBIE,
      XEntityType.SKELETON,
      XEntityType.WITHER,
      XEntityType.BEE,
      XEntityType.PHANTOM,
      XEntityType.DROWNED,
      XEntityType.WITHER_SKELETON,
      XEntityType.SKELETON_HORSE,
      XEntityType.STRAY,
      XEntityType.HUSK
   );
   @NotNull
   public static final XTag<XEntityType> EFFECTIVE_BANE_OF_ARTHROPODS_ENTITIES = XTag.TagBuilder.simple(
      XEntityType.SPIDER, XEntityType.CAVE_SPIDER, XEntityType.SILVERFISH, XEntityType.ENDERMITE
   );
   @NotNull
   public static final XTag<XPotion> DEBUFFS = XTag.TagBuilder.simple(
      XPotion.BAD_OMEN,
      XPotion.BLINDNESS,
      XPotion.NAUSEA,
      XPotion.INSTANT_DAMAGE,
      XPotion.HUNGER,
      XPotion.LEVITATION,
      XPotion.POISON,
      XPotion.SLOWNESS,
      XPotion.MINING_FATIGUE,
      XPotion.UNLUCK,
      XPotion.WEAKNESS,
      XPotion.WITHER
   );
   public static final Map<XMaterial, XEntityType> MATERIAL_TO_ENTITY = new HashMap<>();
   @NotNull
   private final Set<T> values;
   private static final Map<String, XTag<?>> TAGS;

   private XTag(@NotNull Set<T> values) {
      this.values = values;
   }

   public static <E> List<XTag.Matcher<E>> stringMatcher(@Nullable Collection<String> elements) {
      return stringMatcher(elements, null);
   }

   public static <E> List<XTag.Matcher<E>> stringMatcher(@Nullable Collection<String> elements, @Nullable Collection<XTag.Matcher.Error> errors) {
      if (elements != null && !elements.isEmpty()) {
         List<XTag.Matcher<E>> matchers = new ArrayList<>(elements.size());

         for (String comp : elements) {
            String checker = comp.toUpperCase(Locale.ENGLISH);
            if (checker.startsWith("CONTAINS:")) {
               comp = XMaterial.format(checker.substring(9));
               matchers.add(new XTag.Matcher.TextMatcher(comp, true));
            } else if (checker.startsWith("REGEX:")) {
               comp = comp.substring(6);

               try {
                  matchers.add(new XTag.Matcher.RegexMatcher(Pattern.compile(comp)));
               } catch (Throwable e) {
                  if (errors != null) {
                     errors.add(new XTag.Matcher.Error(comp, "REGEX", e));
                  }
               }
            } else {
               if (checker.startsWith("TAG:")) {
                  comp = XMaterial.format(comp.substring(4));
                  Optional<XTag<?>> tag = getTag(comp);
                  if (tag.isPresent()) {
                     matchers.add((XTag.Matcher<E>)(new XTag.Matcher.XTagMatcher<>(tag.get())));
                  } else {
                     errors.add(new XTag.Matcher.Error("Cannot find tag: " + comp, "TAG"));
                  }
               }

               matchers.add(new XTag.Matcher.TextMatcher(comp, false));
            }
         }

         return matchers;
      } else {
         return new ArrayList<>();
      }
   }

   public static <T> boolean anyMatchString(T target, Collection<String> matchers) {
      return anyMatch(target, stringMatcher(matchers));
   }

   public static <T> boolean anyMatch(T target, Collection<XTag.Matcher<T>> matchers) {
      return matchers.stream().anyMatch(x -> x.matches(target));
   }

   private static XMaterial[] findAllColors(String material) {
      String[] colorPrefixes = new String[]{
         "ORANGE", "LIGHT_BLUE", "GRAY", "BLACK", "MAGENTA", "PINK", "BLUE", "GREEN", "CYAN", "PURPLE", "YELLOW", "LIME", "LIGHT_GRAY", "WHITE", "BROWN", "RED"
      };
      List<XMaterial> list = new ArrayList<>();
      XMaterial.matchXMaterial(material).ifPresent(list::add);

      for (String color : colorPrefixes) {
         XMaterial.matchXMaterial(color + '_' + material).ifPresent(list::add);
      }

      return list.toArray(new XMaterial[0]);
   }

   private static XMaterial[] findAllWoodTypes(String material) {
      String[] woodPrefixes = new String[]{
         "ACACIA", "DARK_OAK", "PALE_OAK", "JUNGLE", "BIRCH", "WARPED", "OAK", "SPRUCE", "CRIMSON", "MANGROVE", "CHERRY", "BAMBOO"
      };
      List<XMaterial> list = new ArrayList<>();

      for (String wood : woodPrefixes) {
         XMaterial.matchXMaterial(wood + '_' + material).ifPresent(list::add);
      }

      return list.toArray(new XMaterial[0]);
   }

   private static XMaterial[] findMaterialsEndingWith(String material) {
      return Arrays.stream(XMaterial.VALUES).filter(x -> x.name().endsWith(material)).toArray(XMaterial[]::new);
   }

   private static XMaterial[] findMaterialsStartingWith(String material) {
      return Arrays.stream(XMaterial.VALUES).filter(x -> x.name().startsWith(material)).toArray(XMaterial[]::new);
   }

   private static XMaterial[] findAllCorals(boolean alive, boolean block, boolean fan, boolean wall) {
      String[] materials = new String[]{"FIRE", "TUBE", "BRAIN", "HORN", "BUBBLE"};
      List<XMaterial> list = new ArrayList<>();

      for (String material : materials) {
         StringBuilder builder = new StringBuilder();
         if (!alive) {
            builder.append("DEAD_");
         }

         builder.append(material).append("_CORAL");
         if (block) {
            builder.append("_BLOCK");
         }

         if (fan) {
            if (wall) {
               builder.append("_WALL");
            }

            builder.append("_FAN");
         }

         XMaterial.matchXMaterial(builder.toString()).ifPresent(list::add);
      }

      return list.toArray(new XMaterial[0]);
   }

   public static boolean isItem(XMaterial material) {
      if (!XMaterial.supports(1, 13)) {
         switch (material) {
            case ATTACHED_MELON_STEM:
            case ATTACHED_PUMPKIN_STEM:
            case BEETROOTS:
            case BLACK_WALL_BANNER:
            case BLUE_WALL_BANNER:
            case BROWN_WALL_BANNER:
            case CARROTS:
            case COCOA:
            case CREEPER_WALL_HEAD:
            case CYAN_WALL_BANNER:
            case DRAGON_WALL_HEAD:
            case END_GATEWAY:
            case END_PORTAL:
            case FIRE:
            case FIRE_CORAL_WALL_FAN:
            case FROSTED_ICE:
            case GRAY_WALL_BANNER:
            case GREEN_WALL_BANNER:
            case HORN_CORAL_WALL_FAN:
            case LAVA:
            case LIGHT_BLUE_WALL_BANNER:
            case LIGHT_GRAY_WALL_BANNER:
            case LIME_WALL_BANNER:
            case MAGENTA_WALL_BANNER:
            case MELON_STEM:
            case MOVING_PISTON:
            case NETHER_PORTAL:
            case ORANGE_WALL_BANNER:
            case PINK_WALL_BANNER:
            case PISTON_HEAD:
            case PLAYER_WALL_HEAD:
            case POTATOES:
            case POTTED_ACACIA_SAPLING:
            case POTTED_ALLIUM:
            case POTTED_AZURE_BLUET:
            case POTTED_BIRCH_SAPLING:
            case POTTED_BLUE_ORCHID:
            case POTTED_BROWN_MUSHROOM:
            case POTTED_CACTUS:
            case POTTED_DANDELION:
            case POTTED_DARK_OAK_SAPLING:
            case POTTED_PALE_OAK_SAPLING:
            case POTTED_DEAD_BUSH:
            case POTTED_FERN:
            case POTTED_JUNGLE_SAPLING:
            case POTTED_OAK_SAPLING:
            case POTTED_ORANGE_TULIP:
            case POTTED_OXEYE_DAISY:
            case POTTED_PINK_TULIP:
            case POTTED_POPPY:
            case POTTED_RED_MUSHROOM:
            case POTTED_RED_TULIP:
            case POTTED_SPRUCE_SAPLING:
            case POTTED_WHITE_TULIP:
            case PUMPKIN_STEM:
            case PURPLE_WALL_BANNER:
            case REDSTONE_WALL_TORCH:
            case REDSTONE_WIRE:
            case RED_WALL_BANNER:
            case SKELETON_WALL_SKULL:
            case TRIPWIRE:
            case ACACIA_WALL_SIGN:
            case OAK_WALL_SIGN:
            case BIRCH_WALL_SIGN:
            case JUNGLE_WALL_SIGN:
            case SPRUCE_WALL_SIGN:
            case DARK_OAK_WALL_SIGN:
            case PALE_OAK_WALL_SIGN:
            case WALL_TORCH:
            case WATER:
            case WHITE_WALL_BANNER:
            case WITHER_SKELETON_WALL_SKULL:
            case YELLOW_WALL_BANNER:
            case ZOMBIE_WALL_HEAD:
               return false;
            default:
               return true;
         }
      } else {
         Material mat = material.get();
         return mat != null && mat.isItem();
      }
   }

   public static boolean isInteractable(XMaterial material) {
      if (XMaterial.supports(1, 13)) {
         return material.get().isInteractable();
      }

      switch (material) {
         case MOVING_PISTON:
         case POTTED_ACACIA_SAPLING:
         case POTTED_ALLIUM:
         case POTTED_AZURE_BLUET:
         case POTTED_BIRCH_SAPLING:
         case POTTED_BLUE_ORCHID:
         case POTTED_BROWN_MUSHROOM:
         case POTTED_CACTUS:
         case POTTED_DANDELION:
         case POTTED_DARK_OAK_SAPLING:
         case POTTED_PALE_OAK_SAPLING:
         case POTTED_DEAD_BUSH:
         case POTTED_FERN:
         case POTTED_JUNGLE_SAPLING:
         case POTTED_OAK_SAPLING:
         case POTTED_ORANGE_TULIP:
         case POTTED_OXEYE_DAISY:
         case POTTED_PINK_TULIP:
         case POTTED_POPPY:
         case POTTED_RED_MUSHROOM:
         case POTTED_RED_TULIP:
         case POTTED_SPRUCE_SAPLING:
         case POTTED_WHITE_TULIP:
         case ACACIA_WALL_SIGN:
         case OAK_WALL_SIGN:
         case BIRCH_WALL_SIGN:
         case JUNGLE_WALL_SIGN:
         case SPRUCE_WALL_SIGN:
         case DARK_OAK_WALL_SIGN:
         case PALE_OAK_WALL_SIGN:
         case ACACIA_BUTTON:
         case ACACIA_DOOR:
         case ACACIA_FENCE:
         case ACACIA_FENCE_GATE:
         case ACACIA_STAIRS:
         case ACACIA_TRAPDOOR:
         case ANVIL:
         case BEACON:
         case BIRCH_BUTTON:
         case BIRCH_DOOR:
         case BIRCH_FENCE:
         case PALE_OAK_BUTTON:
         case PALE_OAK_DOOR:
         case PALE_OAK_FENCE:
         case PALE_OAK_FENCE_GATE:
         case PALE_OAK_STAIRS:
         case PALE_OAK_TRAPDOOR:
         case BIRCH_FENCE_GATE:
         case BIRCH_STAIRS:
         case BIRCH_TRAPDOOR:
         case BLACK_BED:
         case BLACK_SHULKER_BOX:
         case BLUE_BED:
         case BLUE_SHULKER_BOX:
         case BREWING_STAND:
         case BRICK_STAIRS:
         case BROWN_BED:
         case BROWN_SHULKER_BOX:
         case CAKE:
         case CAULDRON:
         case CHAIN_COMMAND_BLOCK:
         case CHEST:
         case CHIPPED_ANVIL:
         case COBBLESTONE_STAIRS:
         case COMMAND_BLOCK:
         case COMPARATOR:
         case CRAFTING_TABLE:
         case CYAN_BED:
         case CYAN_SHULKER_BOX:
         case DAMAGED_ANVIL:
         case DARK_OAK_BUTTON:
         case DARK_OAK_DOOR:
         case DARK_OAK_FENCE:
         case DARK_OAK_FENCE_GATE:
         case DARK_OAK_STAIRS:
         case DARK_OAK_TRAPDOOR:
         case DARK_PRISMARINE_STAIRS:
         case DAYLIGHT_DETECTOR:
         case DISPENSER:
         case DRAGON_EGG:
         case DROPPER:
         case ENCHANTING_TABLE:
         case ENDER_CHEST:
         case FLOWER_POT:
         case FURNACE:
         case GRAY_BED:
         case GRAY_SHULKER_BOX:
         case GREEN_BED:
         case GREEN_SHULKER_BOX:
         case HOPPER:
         case IRON_DOOR:
         case IRON_TRAPDOOR:
         case JUKEBOX:
         case JUNGLE_BUTTON:
         case JUNGLE_DOOR:
         case JUNGLE_FENCE:
         case JUNGLE_FENCE_GATE:
         case JUNGLE_STAIRS:
         case JUNGLE_TRAPDOOR:
         case LEVER:
         case LIGHT_BLUE_BED:
         case LIGHT_BLUE_SHULKER_BOX:
         case LIGHT_GRAY_BED:
         case LIGHT_GRAY_SHULKER_BOX:
         case LIME_BED:
         case LIME_SHULKER_BOX:
         case MAGENTA_BED:
         case MAGENTA_SHULKER_BOX:
         case NETHER_BRICK_FENCE:
         case NETHER_BRICK_STAIRS:
         case NOTE_BLOCK:
         case OAK_BUTTON:
         case OAK_DOOR:
         case OAK_FENCE:
         case OAK_FENCE_GATE:
         case OAK_STAIRS:
         case OAK_TRAPDOOR:
         case ORANGE_BED:
         case ORANGE_SHULKER_BOX:
         case PINK_BED:
         case PINK_SHULKER_BOX:
         case PRISMARINE_BRICK_STAIRS:
         case PRISMARINE_STAIRS:
         case PUMPKIN:
         case PURPLE_BED:
         case PURPLE_SHULKER_BOX:
         case PURPUR_STAIRS:
         case QUARTZ_STAIRS:
         case REDSTONE_ORE:
         case RED_BED:
         case RED_SANDSTONE_STAIRS:
         case RED_SHULKER_BOX:
         case REPEATER:
         case REPEATING_COMMAND_BLOCK:
         case SANDSTONE_STAIRS:
         case SHULKER_BOX:
         case ACACIA_SIGN:
         case BIRCH_SIGN:
         case DARK_OAK_SIGN:
         case JUNGLE_SIGN:
         case OAK_SIGN:
         case SPRUCE_SIGN:
         case SPRUCE_BUTTON:
         case SPRUCE_DOOR:
         case SPRUCE_FENCE:
         case SPRUCE_FENCE_GATE:
         case SPRUCE_STAIRS:
         case SPRUCE_TRAPDOOR:
         case STONE_BRICK_STAIRS:
         case STONE_BUTTON:
         case STRUCTURE_BLOCK:
         case TNT:
         case TRAPPED_CHEST:
         case WHITE_BED:
         case WHITE_SHULKER_BOX:
         case YELLOW_BED:
         case YELLOW_SHULKER_BOX:
            return true;
         case NETHER_PORTAL:
         case ORANGE_WALL_BANNER:
         case PINK_WALL_BANNER:
         case PISTON_HEAD:
         case PLAYER_WALL_HEAD:
         case POTATOES:
         case PUMPKIN_STEM:
         case PURPLE_WALL_BANNER:
         case REDSTONE_WALL_TORCH:
         case REDSTONE_WIRE:
         case RED_WALL_BANNER:
         case SKELETON_WALL_SKULL:
         case TRIPWIRE:
         case WALL_TORCH:
         case WATER:
         case WHITE_WALL_BANNER:
         case WITHER_SKELETON_WALL_SKULL:
         case YELLOW_WALL_BANNER:
         case ZOMBIE_WALL_HEAD:
         default:
            return false;
      }
   }

   @NotNull
   public Set<T> getValues() {
      return this.values;
   }

   public boolean isTagged(@Nullable T value) {
      return value != null && this.values.contains(value);
   }

   @SafeVarargs
   private final XTag<T> without(T... without) {
      Set<T> newSet = new HashSet<>(this.values);

      for (T value : without) {
         newSet.remove(value);
      }

      return new XTag<>(newSet);
   }

   public static Optional<XTag<?>> getTag(String name) {
      return Optional.ofNullable(TAGS.get(name));
   }

   static {
      MATERIAL_TO_ENTITY.put(XMaterial.MINECART, XEntityType.MINECART);
      MATERIAL_TO_ENTITY.put(XMaterial.CHEST_MINECART, XEntityType.CHEST_MINECART);
      MATERIAL_TO_ENTITY.put(XMaterial.COMMAND_BLOCK_MINECART, XEntityType.COMMAND_BLOCK_MINECART);
      MATERIAL_TO_ENTITY.put(XMaterial.TNT_MINECART, XEntityType.TNT_MINECART);
      MATERIAL_TO_ENTITY.put(XMaterial.FURNACE_MINECART, XEntityType.FURNACE_MINECART);
      MATERIAL_TO_ENTITY.put(XMaterial.HOPPER_MINECART, XEntityType.HOPPER_MINECART);
      MATERIAL_TO_ENTITY.put(XMaterial.END_CRYSTAL, XEntityType.END_CRYSTAL);
      MATERIAL_TO_ENTITY.put(XMaterial.PAINTING, XEntityType.PAINTING);
      MATERIAL_TO_ENTITY.put(XMaterial.ITEM_FRAME, XEntityType.ITEM_FRAME);
      MATERIAL_TO_ENTITY.put(XMaterial.GLOW_ITEM_FRAME, XEntityType.GLOW_ITEM_FRAME);
      MATERIAL_TO_ENTITY.put(XMaterial.WIND_CHARGE, XEntityType.WIND_CHARGE);
      MATERIAL_TO_ENTITY.put(XMaterial.EGG, XEntityType.EGG);
      MATERIAL_TO_ENTITY.put(XMaterial.SNOWBALL, XEntityType.SNOWBALL);
      MATERIAL_TO_ENTITY.put(XMaterial.ENDER_PEARL, XEntityType.ENDER_PEARL);
      MATERIAL_TO_ENTITY.put(XMaterial.ENDER_EYE, XEntityType.EYE_OF_ENDER);

      for (XMaterial boat : ITEMS_BOATS.values) {
         XEntityType entityType = XEntityType.of(boat.name()).orElseThrow(() -> new IllegalStateException("Cannot find entity type for boat: " + boat));
         MATERIAL_TO_ENTITY.put(boat, entityType);
      }

      for (XMaterial spawnEgg : SPAWN_EGGS.values) {
         String name = spawnEgg.name().substring(0, spawnEgg.name().length() - "_SPAWN_EGG".length());
         XEntityType entityType = XEntityType.of(name)
            .orElseThrow(() -> new IllegalStateException("Cannot find entity type for spawn egg: " + spawnEgg + " named " + name));
         MATERIAL_TO_ENTITY.put(spawnEgg, entityType);
      }

      TAGS = new HashMap<>(30);

      for (Field field : XTag.class.getDeclaredFields()) {
         try {
            if (field.getType() == XTag.class) {
               TAGS.put(field.getName(), (XTag<?>)field.get(null));
            }
         } catch (IllegalAccessException ex) {
            new IllegalStateException("Failed to get XTag " + field, ex).printStackTrace();
         }
      }
   }

   public abstract static class Matcher<T> {
      public Matcher() {
      }

      public abstract boolean matches(T var1);

      public static final class Error extends RuntimeException {
         public final String matcher;

         public Error(String matcher, String message) {
            super(message);
            this.matcher = matcher;
         }

         public Error(String matcher, String message, Throwable cause) {
            super(message, cause);
            this.matcher = matcher;
         }
      }

      public static final class RegexMatcher<T> extends XTag.Matcher<T> {
         public final Pattern regex;

         public RegexMatcher(Pattern regex) {
            this.regex = regex;
         }

         @Override
         public boolean matches(T object) {
            String name = object instanceof Enum ? ((Enum)object).name() : object.toString();
            return this.regex.matcher(name).matches();
         }
      }

      public static final class TextMatcher<T> extends XTag.Matcher<T> {
         public final String text;
         public final boolean contains;

         public TextMatcher(String text, boolean contains) {
            this.text = text;
            this.contains = contains;
         }

         @Override
         public boolean matches(T object) {
            String name = object instanceof Enum ? ((Enum)object).name() : object.toString();
            return this.contains ? name.contains(this.text) : name.equals(this.text);
         }
      }

      public static final class XTagMatcher<T extends XBase<?, ?>> extends XTag.Matcher<T> {
         public final XTag<T> matcher;

         public XTagMatcher(XTag<T> matcher) {
            this.matcher = matcher;
         }

         public boolean matches(T object) {
            return this.matcher.isTagged(object);
         }
      }
   }

   private static final class TagBuilder<T extends XBase<?, ?>> {
      private final Set<T> values;

      private TagBuilder(Collection<T> values) {
         if (values.isEmpty()) {
            this.values = Collections.newSetFromMap(new IdentityHashMap<>());
         } else if (values.iterator().next() instanceof Enum) {
            Iterator<T> iter = values.iterator();
            T first = iter.next();
            this.values = EnumSet.of((Enum)first);

            while (iter.hasNext()) {
               this.values.add(iter.next());
            }
         } else {
            this.values = Collections.newSetFromMap(new IdentityHashMap<>(values.size()));
            this.values.addAll(values);
         }
      }

      @SafeVarargs
      private static <T extends XBase<?, ?>> XTag<T> simple(T... values) {
         return of(values).build();
      }

      @SafeVarargs
      private static <T extends XBase<?, ?>> XTag<T> simple(XTag<T>... values) {
         return new XTag.TagBuilder<>(Collections.singletonList(values[0].values.iterator().next())).inheritFrom(values).build();
      }

      @SafeVarargs
      private static <T extends XBase<?, ?>> XTag.TagBuilder<T> of(T... values) {
         return new XTag.TagBuilder<>(Arrays.asList(values));
      }

      @SafeVarargs
      private final XTag.TagBuilder<T> inheritFrom(@NotNull XTag<T>... values) {
         for (XTag<T> value : values) {
            this.values.addAll(value.values);
         }

         return this;
      }

      private XTag<T> build() {
         return new XTag<>(Collections.unmodifiableSet(this.values));
      }
   }
}
