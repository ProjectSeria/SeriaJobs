package id.seria.gacha.lib.xseries;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Banner;
import org.bukkit.block.Bed;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.Cake;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Colorable;
import org.bukkit.material.FlowerPot;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;

public final class XBlock {
   @Deprecated
   public static final Set<XMaterial> CROPS = Collections.unmodifiableSet(
      EnumSet.of(
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
      )
   );
   @Deprecated
   public static final Set<XMaterial> DANGEROUS = Collections.unmodifiableSet(
      EnumSet.of(XMaterial.MAGMA_BLOCK, XMaterial.LAVA, XMaterial.CAMPFIRE, XMaterial.FIRE, XMaterial.SOUL_FIRE)
   );
   public static final byte CAKE_SLICES = 6;
   private static final boolean ISFLAT = XMaterial.supports(1, 13);
   private static final Map<XMaterial, XMaterial> ITEM_TO_BLOCK = new EnumMap<>(XMaterial.class);

   private XBlock() {
   }

   public static boolean isLit(Block block) {
      if (ISFLAT) {
         if (!(block.getBlockData() instanceof Lightable)) {
            return false;
         }

         Lightable lightable = (Lightable)block.getBlockData();
         return lightable.isLit();
      } else {
         return isMaterial(
            block, XBlock.LegacyBlockMaterial.REDSTONE_LAMP_ON, XBlock.LegacyBlockMaterial.REDSTONE_TORCH_ON, XBlock.LegacyBlockMaterial.BURNING_FURNACE
         );
      }
   }

   public static boolean isContainer(@Nullable Block block) {
      return block != null && block.getState() instanceof InventoryHolder;
   }

   public static void setLit(Block block, boolean lit) {
      if (ISFLAT) {
         if (block.getBlockData() instanceof Lightable) {
            BlockData data = block.getBlockData();
            Lightable lightable = (Lightable)data;
            lightable.setLit(lit);
            block.setBlockData(data, false);
         }
      } else {
         String name = block.getType().name();
         if (name.endsWith("FURNACE")) {
            block.setType(XBlock.LegacyBlockMaterial.BURNING_FURNACE.material);
         } else if (name.startsWith("REDSTONE_LAMP")) {
            block.setType(XBlock.LegacyBlockMaterial.REDSTONE_LAMP_ON.material);
         } else {
            block.setType(XBlock.LegacyBlockMaterial.REDSTONE_TORCH_ON.material);
         }
      }
   }

   @Deprecated
   public static boolean isCrop(XMaterial material) {
      return CROPS.contains(material);
   }

   @Deprecated
   public static boolean isDangerous(XMaterial material) {
      return DANGEROUS.contains(material);
   }

   public static DyeColor getColor(Block block) {
      if (ISFLAT) {
         if (!(block.getBlockData() instanceof Colorable)) {
            return null;
         }

         Colorable colorable = (Colorable)block.getBlockData();
         return colorable.getColor();
      } else {
         BlockState state = block.getState();
         MaterialData data = state.getData();
         if (data instanceof Wool) {
            Wool wool = (Wool)data;
            return wool.getColor();
         } else {
            return null;
         }
      }
   }

   @Deprecated
   public static boolean isCake(@Nullable Material material) {
      return !ISFLAT ? material == XBlock.LegacyBlockMaterial.CAKE_BLOCK.material : material == Material.CAKE;
   }

   @Deprecated
   public static boolean isWheat(@Nullable Material material) {
      return !ISFLAT ? material == XBlock.LegacyBlockMaterial.CROPS.material : material == Material.WHEAT;
   }

   @Deprecated
   public static boolean isSugarCane(@Nullable Material material) {
      return !ISFLAT ? material == XBlock.LegacyBlockMaterial.SUGAR_CANE_BLOCK.material : material == Material.SUGAR_CANE;
   }

   @Deprecated
   public static boolean isBeetroot(@Nullable Material material) {
      return ISFLAT ? material == Material.BEETROOTS : material != null && material == XBlock.LegacyBlockMaterial.BEETROOT_BLOCK.material;
   }

   @Deprecated
   public static boolean isNetherWart(@Nullable Material material) {
      return !ISFLAT ? material == XBlock.LegacyBlockMaterial.NETHER_WARTS.material : material == Material.NETHER_WART;
   }

   @Deprecated
   public static boolean isCarrot(@Nullable Material material) {
      return !ISFLAT ? material == Material.CARROT : material == Material.CARROTS;
   }

   @Deprecated
   public static boolean isMelon(@Nullable Material material) {
      return !ISFLAT ? material == XBlock.LegacyBlockMaterial.MELON_BLOCK.material : material == Material.MELON;
   }

   @Deprecated
   public static boolean isPotato(@Nullable Material material) {
      return !ISFLAT ? material == Material.POTATO : material == Material.POTATOES;
   }

   public static BlockFace getDirection(Block block) {
      if (ISFLAT) {
         if (!(block.getBlockData() instanceof Directional)) {
            return BlockFace.SELF;
         }

         Directional direction = (Directional)block.getBlockData();
         return direction.getFacing();
      } else {
         BlockState state = block.getState();
         MaterialData data = state.getData();
         return data instanceof org.bukkit.material.Directional ? ((org.bukkit.material.Directional)data).getFacing() : BlockFace.SELF;
      }
   }

   public static boolean setDirection(Block block, BlockFace facing) {
      if (ISFLAT) {
         if (!(block.getBlockData() instanceof Directional)) {
            return false;
         }

         BlockData data = block.getBlockData();
         Directional direction = (Directional)data;
         direction.setFacing(facing);
         block.setBlockData(data, false);
         return true;
      } else {
         BlockState state = block.getState();
         MaterialData data = state.getData();
         if (data instanceof org.bukkit.material.Directional) {
            if (XMaterial.matchXMaterial(block.getType()) == XMaterial.LADDER) {
               facing = facing.getOppositeFace();
            }

            ((org.bukkit.material.Directional)data).setFacingDirection(facing);
            state.update(true);
            return true;
         } else {
            return false;
         }
      }
   }

   @Experimental
   public static XMaterial getType(Block block) {
      if (ISFLAT) {
         return XMaterial.matchXMaterial(block.getType());
      }

      Material mat = block.getType();
      XBlock.LegacyMaterialGroup legacyMaterial = XBlock.LegacyMaterialGroup.getMaterial(mat.name());
      if (legacyMaterial == null) {
         return XMaterial.matchXMaterial(block.getType());
      }

      byte data = block.getData();
      switch (legacyMaterial) {
         case STANDING_BANNER:
         case BANNER: {
            Banner banner = (Banner)block.getState();
            DyeColor baseColor = banner.getBaseColor();
            switch (baseColor) {
               case WHITE:
                  return XMaterial.WHITE_BANNER;
               case ORANGE:
                  return XMaterial.ORANGE_BANNER;
               case MAGENTA:
                  return XMaterial.MAGENTA_BANNER;
               case LIGHT_BLUE:
                  return XMaterial.LIGHT_BLUE_BANNER;
               case YELLOW:
                  return XMaterial.YELLOW_BANNER;
               case LIME:
                  return XMaterial.LIME_BANNER;
               case PINK:
                  return XMaterial.PINK_BANNER;
               case GRAY:
                  return XMaterial.GRAY_BANNER;
               case LIGHT_GRAY:
                  return XMaterial.LIGHT_GRAY_BANNER;
               case CYAN:
                  return XMaterial.CYAN_BANNER;
               case PURPLE:
                  return XMaterial.PURPLE_BANNER;
               case BLUE:
                  return XMaterial.BLUE_BANNER;
               case BROWN:
                  return XMaterial.BROWN_BANNER;
               case GREEN:
                  return XMaterial.GREEN_BANNER;
               case RED:
                  return XMaterial.RED_BANNER;
               case BLACK:
                  return XMaterial.BLACK_BANNER;
               default:
                  throw new AssertionError("Unknown " + legacyMaterial + " type: " + baseColor);
            }
         }
         case WALL_BANNER: {
            Banner banner = (Banner)block.getState();
            DyeColor baseColor = banner.getBaseColor();
            switch (baseColor) {
               case WHITE:
                  return XMaterial.WHITE_WALL_BANNER;
               case ORANGE:
                  return XMaterial.ORANGE_WALL_BANNER;
               case MAGENTA:
                  return XMaterial.MAGENTA_WALL_BANNER;
               case LIGHT_BLUE:
                  return XMaterial.LIGHT_BLUE_WALL_BANNER;
               case YELLOW:
                  return XMaterial.YELLOW_WALL_BANNER;
               case LIME:
                  return XMaterial.LIME_WALL_BANNER;
               case PINK:
                  return XMaterial.PINK_WALL_BANNER;
               case GRAY:
                  return XMaterial.GRAY_WALL_BANNER;
               case LIGHT_GRAY:
                  return XMaterial.LIGHT_GRAY_WALL_BANNER;
               case CYAN:
                  return XMaterial.CYAN_WALL_BANNER;
               case PURPLE:
                  return XMaterial.PURPLE_WALL_BANNER;
               case BLUE:
                  return XMaterial.BLUE_WALL_BANNER;
               case BROWN:
                  return XMaterial.BROWN_WALL_BANNER;
               case GREEN:
                  return XMaterial.GREEN_WALL_BANNER;
               case RED:
                  return XMaterial.RED_WALL_BANNER;
               case BLACK:
                  return XMaterial.BLACK_WALL_BANNER;
               default:
                  throw new AssertionError("Unknown " + legacyMaterial + " type: " + baseColor);
            }
         }
         case CARPET:
         case WOOL:
         case STAINED_CLAY:
         case STAINED_GLASS:
         case STAINED_GLASS_PANE:
         case THIN_GLASS:
         case STONE:
         case RED_ROSE:
         case LONG_GRASS:
         case DIRT:
         case SAND:
         case SANDSTONE:
         case RED_SANDSTONE:
         case SPONGE:
         case PRISMARINE:
         case CONCRETE:
         case CONCRETE_POWDER:
         case SMOOTH_BRICK:
         case COBBLE_WALL:
         case MONSTER_EGGS:
         case WOOD:
         default:
            break;
         case QUARTZ_BLOCK:
            switch (data) {
               case 0:
                  return XMaterial.QUARTZ_BLOCK;
               case 1:
                  return XMaterial.CHISELED_QUARTZ_BLOCK;
               case 2:
               case 3:
               case 4:
                  return XMaterial.QUARTZ_PILLAR;
               default:
                  throw new AssertionError("Unknown QUARTZ_BLOCK type: " + data);
            }
         case SKULL:
            Skull skull = (Skull)block.getState();
            switch (skull.getSkullType()) {
               case SKELETON:
                  return XMaterial.SKELETON_SKULL;
               case WITHER:
                  return XMaterial.WITHER_SKELETON_SKULL;
               case ZOMBIE:
                  return XMaterial.ZOMBIE_HEAD;
               case PLAYER:
                  return XMaterial.PLAYER_HEAD;
               case CREEPER:
                  return XMaterial.CREEPER_HEAD;
               case DRAGON:
                  return XMaterial.DRAGON_HEAD;
               default:
                  throw new AssertionError("Unknown SKULL type: " + skull);
            }
         case FLOWER_POT:
            BlockState state = block.getState();
            FlowerPot pot = (FlowerPot)state.getData();
            MaterialData contents = pot.getContents();
            if (contents == null) {
               return XMaterial.FLOWER_POT;
            }

            Material contentType = contents.getItemType();
            byte contentData = contents.getData();
            switch (contentType.name()) {
               case "RED_ROSE":
                  switch (contentData) {
                     case 0:
                        return XMaterial.POTTED_POPPY;
                     case 1:
                        return XMaterial.POTTED_BLUE_ORCHID;
                     case 2:
                        return XMaterial.POTTED_ALLIUM;
                     case 3:
                        return XMaterial.POTTED_AZURE_BLUET;
                     case 4:
                        return XMaterial.POTTED_RED_TULIP;
                     case 5:
                        return XMaterial.POTTED_ORANGE_TULIP;
                     case 6:
                        return XMaterial.POTTED_WHITE_TULIP;
                     case 7:
                        return XMaterial.POTTED_PINK_TULIP;
                     case 8:
                        return XMaterial.POTTED_OXEYE_DAISY;
                  }
               case "YELLOW_FLOWER":
                  return XMaterial.POTTED_DANDELION;
               case "RED_MUSHROOM":
                  return XMaterial.POTTED_RED_MUSHROOM;
               case "BROWN_MUSHROOM":
                  return XMaterial.POTTED_BROWN_MUSHROOM;
               case "CACTUS":
                  return XMaterial.POTTED_CACTUS;
               case "DEAD_BUSH":
                  return XMaterial.POTTED_DEAD_BUSH;
               case "SAPLING":
                  switch (contentData) {
                     case 0:
                        return XMaterial.POTTED_OAK_SAPLING;
                     case 1:
                        return XMaterial.POTTED_SPRUCE_SAPLING;
                     case 2:
                        return XMaterial.POTTED_BIRCH_SAPLING;
                     case 3:
                        return XMaterial.POTTED_JUNGLE_SAPLING;
                     case 4:
                        return XMaterial.POTTED_ACACIA_SAPLING;
                     case 5:
                        return XMaterial.POTTED_DARK_OAK_SAPLING;
                  }
               case "LONG_GRASS":
                  if (contentData == 2) {
                     return XMaterial.POTTED_FERN;
                  }
               default:
                  throw new AssertionError("Unknown potted flower type: " + pot + " | " + contentType + " | " + contentData);
            }
         case DOUBLE_PLANT:
            if (data == 10) {
               Block targetBlock = block.getRelative(0, -1, 0);
               if (targetBlock.getType().name().equals("DOUBLE_PLANT")) {
                  data = targetBlock.getData();
               }
            }

            data = (byte)(data & 7);
            break;
         case ANVIL:
            data = block.getData();
            data = (byte)(data >> 2 & 3);
            break;
         case BED:
         case BED_BLOCK:
            if (!XMaterial.supports(1, 12)) {
               return XMaterial.RED_BED;
            }

            Bed bed = (Bed)block.getState();
            DyeColor dyeColor = bed.getColor();
            switch (dyeColor) {
               case WHITE:
                  return XMaterial.WHITE_BED;
               case ORANGE:
                  return XMaterial.ORANGE_BED;
               case MAGENTA:
                  return XMaterial.MAGENTA_BED;
               case LIGHT_BLUE:
                  return XMaterial.LIGHT_BLUE_BED;
               case YELLOW:
                  return XMaterial.YELLOW_BED;
               case LIME:
                  return XMaterial.LIME_BED;
               case PINK:
                  return XMaterial.PINK_BED;
               case GRAY:
                  return XMaterial.GRAY_BED;
               case LIGHT_GRAY:
                  return XMaterial.LIGHT_GRAY_BED;
               case CYAN:
                  return XMaterial.CYAN_BED;
               case PURPLE:
                  return XMaterial.PURPLE_BED;
               case BLUE:
                  return XMaterial.BLUE_BED;
               case BROWN:
                  return XMaterial.BROWN_BED;
               case GREEN:
                  return XMaterial.GREEN_BED;
               case RED:
                  return XMaterial.RED_BED;
               case BLACK:
                  return XMaterial.BLACK_BED;
               default:
                  throw new AssertionError("Unkonwn " + legacyMaterial + " type: " + dyeColor);
            }
         case WOOD_STEP:
            data = (byte)(data & 7);
            break;
         case WOOD_DOUBLE_STEP:
            switch (data) {
               case 0:
                  return XMaterial.OAK_PLANKS;
               case 1:
                  return XMaterial.SPRUCE_PLANKS;
               case 2:
                  return XMaterial.BIRCH_PLANKS;
               case 3:
                  return XMaterial.JUNGLE_PLANKS;
               case 4:
                  return XMaterial.ACACIA_PLANKS;
               case 5:
                  return XMaterial.DARK_OAK_PLANKS;
               default:
                  throw new AssertionError("Unknown WOOD_DOUBLE_STEP type: " + data);
            }
         case LEAVES:
         case LEAVES_2:
         case LOG:
         case LOG_2:
            data = (byte)(data & 3);
            break;
         case SAPLING:
            data = (byte)(data & 7);
            break;
         case BRICK:
            return XMaterial.BRICKS;
         case STEP:
            data = (byte)(data & 7);
            if (data == 2) {
               return XMaterial.OAK_SLAB;
            }
            break;
         case DOUBLE_STEP:
            switch (data) {
               case 0:
                  return XMaterial.SMOOTH_STONE_SLAB;
               case 1:
                  return XMaterial.SANDSTONE;
               case 2:
                  return XMaterial.OAK_PLANKS;
               case 3:
                  return XMaterial.COBBLESTONE;
               case 4:
                  return XMaterial.BRICKS;
               case 5:
                  return XMaterial.STONE_BRICKS;
               case 6:
                  return XMaterial.NETHER_BRICKS;
               case 7:
                  return XMaterial.QUARTZ_BLOCK;
               default:
                  throw new AssertionError("Unknown DOUBLE_STEP type: " + data);
            }
         case DOUBLE_STONE_SLAB2:
            if (data == 0) {
               return XMaterial.RED_SANDSTONE;
            }

            throw new AssertionError("Unknown DOUBLE_STONE_SLAB2 type: " + data);
      }

      byte finalData = data;
      return XMaterial.matchDefinedXMaterial(mat.name(), data)
         .orElseThrow(() -> new AssertionError("Unknown legacy block type:  | " + finalData + " | " + mat + " | " + legacyMaterial));
   }

   public static void setType(@NotNull Block block, @Nullable XMaterial material, boolean applyPhysics) {
      Objects.requireNonNull(block, "Cannot set type of null block");
      if (material == null) {
         material = XMaterial.AIR;
      }

      XMaterial smartConversion = ITEM_TO_BLOCK.get(material);
      if (smartConversion != null) {
         material = smartConversion;
      }

      Material parsedMat = material.get();
      if (parsedMat == null) {
         throw new XBlock.BlockTypeChangeException(XBlock.BlockTypeChangeException.Reason.UNSUPPORTED_MATERIAL, material, "");
      }

      String parsedName = parsedMat.name();
      SkullType skullType = getSkullType(material);
      if (!ISFLAT && (parsedName.equals("SKULL_ITEM") || skullType != null)) {
         parsedMat = Material.valueOf("SKULL");
      }

      if (ISFLAT && parsedMat.isBlock()) {
         throw new XBlock.BlockTypeChangeException(XBlock.BlockTypeChangeException.Reason.NOT_A_BLOCK, material, parsedMat);
      }

      block.setType(parsedMat, applyPhysics);
      if (!ISFLAT) {
         XBlock.LegacyBlockMaterial blockMaterial = null;
         switch (material) {
            case CAKE:
               blockMaterial = XBlock.LegacyBlockMaterial.CAKE_BLOCK;
               break;
            case SUGAR_CANE:
               blockMaterial = XBlock.LegacyBlockMaterial.SUGAR_CANE_BLOCK;
               break;
            case POTATOES:
            case POTATO:
               blockMaterial = XBlock.LegacyBlockMaterial.POTATO;
               break;
            case CARROT:
            case CARROTS:
               blockMaterial = XBlock.LegacyBlockMaterial.CARROT;
               break;
            case WHEAT_SEEDS:
            case WHEAT:
               blockMaterial = XBlock.LegacyBlockMaterial.CROPS;
               break;
            case NETHER_WART:
               blockMaterial = XBlock.LegacyBlockMaterial.NETHER_WARTS;
         }

         if (blockMaterial != null) {
            Material legacyMat = blockMaterial.material;
            if (legacyMat == null) {
               throw new XBlock.BlockTypeChangeException(
                  XBlock.BlockTypeChangeException.Reason.UNEXPECTED_LOGIC, material, ", Legacy material '" + blockMaterial.name() + "' not found"
               );
            }

            block.setType(legacyMat, applyPhysics);
         } else {
            XBlock.LegacyMaterialGroup legacyMaterial = XBlock.LegacyMaterialGroup.getMaterial(parsedName);
            if (legacyMaterial == XBlock.LegacyMaterialGroup.BANNER) {
               block.setType(XBlock.LegacyMaterialGroup.STANDING_BANNER.material, applyPhysics);
            }

            XBlock.LegacyMaterialGroup.Handling handling = legacyMaterial == null ? null : legacyMaterial.handling;
            BlockState state = block.getState();
            boolean update = false;
            if (handling == XBlock.LegacyMaterialGroup.Handling.COLORABLE) {
               if (state instanceof Banner) {
                  Banner banner = (Banner)state;
                  String xName = material.name();
                  int colorIndex = xName.indexOf(95);
                  String color = xName.substring(0, colorIndex);
                  if (color.equals("LIGHT")) {
                     color = xName.substring(0, "LIGHT_".length() + 4);
                  }

                  banner.setBaseColor(DyeColor.valueOf(color));
               } else {
                  state.setRawData(material.getData());
               }

               update = true;
            } else if (handling == XBlock.LegacyMaterialGroup.Handling.WOOD_SPECIES) {
               String name = material.name();
               int firstIndicator = name.indexOf(95);
               if (firstIndicator < 0) {
                  throw new XBlock.BlockTypeChangeException(
                     XBlock.BlockTypeChangeException.Reason.UNEXPECTED_LOGIC, material, ", Wood species material not found: '" + name + '\''
                  );
               }

               String woodType = name.substring(0, firstIndicator);
               TreeSpecies species;
               switch (woodType) {
                  case "OAK":
                     species = TreeSpecies.GENERIC;
                     break;
                  case "DARK":
                     species = TreeSpecies.DARK_OAK;
                     break;
                  case "SPRUCE":
                     species = TreeSpecies.REDWOOD;
                     break;
                  default:
                     try {
                        species = TreeSpecies.valueOf(woodType);
                     } catch (IllegalArgumentException ex) {
                        throw new AssertionError("Unknown material " + legacyMaterial + " for wood species");
                     }
               }

               boolean firstType = false;
               switch (legacyMaterial) {
                  case WOOD:
                  case WOOD_DOUBLE_STEP:
                     state.setRawData(species.getData());
                     update = true;
                     break;
                  case WOOD_STEP:
                  case SAPLING:
                     state.setRawData((byte)(state.getRawData() & 8 | species.getData()));
                     update = true;
                     break;
                  case LEAVES:
                  case LOG:
                     firstType = true;
                  case LEAVES_2:
                  case LOG_2:
                     switch (species) {
                        case GENERIC:
                        case REDWOOD:
                        case BIRCH:
                        case JUNGLE:
                           if (!firstType) {
                              throw new AssertionError("Invalid tree species " + species + " for block type" + legacyMaterial + ", use block type 2 instead");
                           }
                           break;
                        case ACACIA:
                        case DARK_OAK:
                           if (firstType) {
                              throw new AssertionError("Invalid tree species " + species + " for block type 2 " + legacyMaterial + ", use block type instead");
                           }
                     }

                     state.setRawData((byte)(state.getRawData() & 12 | species.getData() & 3));
                     update = true;
                     break;
                  default:
                     throw new AssertionError("Unknown block type " + legacyMaterial + " for tree species: " + species);
               }
            } else if (material.getData() != 0) {
               if (skullType != null) {
                  boolean isWallSkull = material.name().contains("WALL");
                  state.setRawData((byte)(isWallSkull ? 0 : 1));
               } else {
                  state.setRawData(material.getData());
               }

               update = true;
            }

            if (skullType != null) {
               Skull skull = (Skull)state;
               skull.setSkullType(skullType);
               update = true;
            }

            if (update) {
               state.update(true, applyPhysics);
            }

            if (!update) {
               throw new XBlock.BlockTypeChangeException(
                  XBlock.BlockTypeChangeException.Reason.STATE_CHANGE_FAILURE, material, ", Failed to change state of: '" + state + '\''
               );
            }
         }
      }
   }

   public static SkullType getSkullType(XMaterial material) {
      switch (material) {
         case PLAYER_HEAD:
         case PLAYER_WALL_HEAD:
            return SkullType.PLAYER;
         case DRAGON_HEAD:
         case DRAGON_WALL_HEAD:
            return SkullType.DRAGON;
         case ZOMBIE_HEAD:
         case ZOMBIE_WALL_HEAD:
            return SkullType.ZOMBIE;
         case CREEPER_HEAD:
         case CREEPER_WALL_HEAD:
            return SkullType.CREEPER;
         case SKELETON_SKULL:
         case SKELETON_WALL_SKULL:
            return SkullType.SKELETON;
         case WITHER_SKELETON_SKULL:
         case WITHER_SKELETON_WALL_SKULL:
            return SkullType.WITHER;
         case PIGLIN_HEAD:
         case PIGLIN_WALL_HEAD:
            return SkullType.PIGLIN;
         default:
            return null;
      }
   }

   public static void setType(@NotNull Block block, @Nullable XMaterial material) {
      setType(block, material, true);
   }

   public static int getAge(Block block) {
      if (ISFLAT) {
         if (!(block.getBlockData() instanceof Ageable)) {
            return 0;
         }

         Ageable ageable = (Ageable)block.getBlockData();
         return ageable.getAge();
      } else {
         BlockState state = block.getState();
         MaterialData data = state.getData();
         return data.getData();
      }
   }

   public static void setAge(Block block, int age) {
      if (ISFLAT) {
         if (!(block.getBlockData() instanceof Ageable)) {
            return;
         }

         BlockData data = block.getBlockData();
         Ageable ageable = (Ageable)data;
         ageable.setAge(age);
         block.setBlockData(data, false);
      }

      BlockState state = block.getState();
      MaterialData data = state.getData();
      data.setData((byte)age);
      state.update(true);
   }

   public static boolean setColor(Block block, DyeColor color) {
      if (ISFLAT) {
         String type = block.getType().name();
         int index = type.indexOf(95);
         if (index == -1) {
            return false;
         }

         String realType = type.substring(index + 1);
         Material material = Material.getMaterial(color.name() + '_' + realType);
         if (material == null) {
            return false;
         }

         block.setType(material);
         return true;
      } else {
         BlockState state = block.getState();
         state.setRawData(color.getWoolData());
         state.update(true);
         return false;
      }
   }

   public static boolean setFluidLevel(Block block, int level) {
      if (ISFLAT) {
         if (!(block.getBlockData() instanceof Levelled)) {
            return false;
         }

         BlockData data = block.getBlockData();
         Levelled levelled = (Levelled)data;
         levelled.setLevel(level);
         block.setBlockData(data, false);
         return true;
      } else {
         BlockState state = block.getState();
         MaterialData data = state.getData();
         data.setData((byte)level);
         state.update(true);
         return false;
      }
   }

   public static int getFluidLevel(Block block) {
      if (ISFLAT) {
         if (!(block.getBlockData() instanceof Levelled)) {
            return -1;
         }

         Levelled levelled = (Levelled)block.getBlockData();
         return levelled.getLevel();
      } else {
         BlockState state = block.getState();
         MaterialData data = state.getData();
         return data.getData();
      }
   }

   public static boolean isWaterStationary(Block block) {
      return ISFLAT ? getFluidLevel(block) < 7 : block.getType() == XBlock.LegacyBlockMaterial.STATIONARY_WATER.material;
   }

   @Deprecated
   public static boolean isWater(Material material) {
      return material == Material.WATER || material == XBlock.LegacyBlockMaterial.STATIONARY_WATER.material;
   }

   @Deprecated
   public static boolean isLava(Material material) {
      return material == Material.LAVA || material == XBlock.LegacyBlockMaterial.STATIONARY_LAVA.material;
   }

   @Deprecated
   public static boolean isOneOf(Block block, Collection<String> blocks) {
      if (blocks != null && !blocks.isEmpty()) {
         String name = block.getType().name();
         XMaterial matched = XMaterial.matchXMaterial(block.getType());

         for (String comp : blocks) {
            String checker = comp.toUpperCase(Locale.ENGLISH);
            if (checker.startsWith("CONTAINS:")) {
               comp = XMaterial.format(checker.substring(9));
               if (name.contains(comp)) {
                  return true;
               }
            } else if (checker.startsWith("REGEX:")) {
               comp = comp.substring(6);
               if (name.matches(comp)) {
                  return true;
               }
            } else {
               Optional<XMaterial> xMat = XMaterial.matchXMaterial(comp);
               if (xMat.isPresent() && isSimilar(block, xMat.get())) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static void setCakeSlices(Block block, int amount) {
      if (!isCake(block.getType())) {
         throw new IllegalArgumentException("Block is not a cake: " + block.getType());
      }

      if (ISFLAT) {
         BlockData data = block.getBlockData();
         Cake cake = (Cake)data;
         int remaining = cake.getMaximumBites() - (cake.getBites() + amount);
         if (remaining > 0) {
            cake.setBites(remaining);
            block.setBlockData(data);
         } else {
            block.breakNaturally();
         }
      } else {
         BlockState state = block.getState();
         org.bukkit.material.Cake cake = (org.bukkit.material.Cake)state.getData();
         if (amount > 0) {
            cake.setSlicesRemaining(amount);
            state.update(true);
         } else {
            block.breakNaturally();
         }
      }
   }

   public static int addCakeSlices(Block block, int slices) {
      if (!isCake(block.getType())) {
         throw new IllegalArgumentException("Block is not a cake: " + block.getType());
      }

      if (ISFLAT) {
         BlockData data = block.getBlockData();
         Cake cake = (Cake)data;
         int bites = cake.getBites() - slices;
         int remaining = cake.getMaximumBites() - bites;
         if (remaining > 0) {
            cake.setBites(bites);
            block.setBlockData(data);
            return remaining;
         } else {
            block.breakNaturally();
            return 0;
         }
      } else {
         BlockState state = block.getState();
         org.bukkit.material.Cake cake = (org.bukkit.material.Cake)state.getData();
         int remaining = cake.getSlicesRemaining() + slices;
         if (remaining > 0) {
            cake.setSlicesRemaining(remaining);
            state.update(true);
            return remaining;
         } else {
            block.breakNaturally();
            return 0;
         }
      }
   }

   public static void setEnderPearlOnFrame(Block endPortalFrame, boolean eye) {
      BlockState state = endPortalFrame.getState();
      if (ISFLAT) {
         BlockData data = state.getBlockData();
         EndPortalFrame frame = (EndPortalFrame)data;
         frame.setEye(eye);
         state.setBlockData(data);
      } else {
         state.setRawData((byte)(eye ? 4 : 0));
      }

      state.update(true);
   }

   public static boolean isSimilar(Block block, XMaterial material) {
      if (material == getType(block)) {
         return true;
      }

      Material mat = block.getType();
      if (material.name().endsWith("_BED") && !XMaterial.supports(1, 12)) {
         return mat == XBlock.LegacyBlockMaterial.BED_BLOCK.material || mat == XBlock.LegacyBlockMaterial.BED.material;
      }

      switch (material) {
         case CAKE:
            return isCake(mat);
         case SUGAR_CANE:
            if (!ISFLAT) {
               return mat == XBlock.LegacyBlockMaterial.SUGAR_CANE_BLOCK.material;
            }

            return mat == Material.SUGAR_CANE;
         case POTATOES:
         case POTATO:
            if (!ISFLAT) {
               return mat == Material.POTATO;
            }

            return mat == Material.POTATOES;
         case CARROT:
         case CARROTS:
            if (!ISFLAT) {
               return mat == Material.CARROT;
            }

            return mat == Material.CARROTS;
         case WHEAT_SEEDS:
         case WHEAT:
            if (!ISFLAT) {
               return mat == XBlock.LegacyBlockMaterial.CROPS.material;
            }

            return mat == Material.WHEAT;
         case NETHER_WART:
         case NETHER_WART_BLOCK:
            if (!ISFLAT) {
               return mat == XBlock.LegacyBlockMaterial.NETHER_WARTS.material;
            }

            return mat == Material.NETHER_WART;
         case PLAYER_HEAD:
         case PLAYER_WALL_HEAD:
         case DRAGON_HEAD:
         case DRAGON_WALL_HEAD:
         case ZOMBIE_HEAD:
         case ZOMBIE_WALL_HEAD:
         case CREEPER_HEAD:
         case CREEPER_WALL_HEAD:
         case SKELETON_SKULL:
         case SKELETON_WALL_SKULL:
         case WITHER_SKELETON_SKULL:
         case WITHER_SKELETON_WALL_SKULL:
         case PIGLIN_HEAD:
         case PIGLIN_WALL_HEAD:
         default:
            return false;
         case MELON:
         case MELON_SLICE:
            if (!ISFLAT) {
               return mat == XBlock.LegacyBlockMaterial.MELON_BLOCK.material;
            }

            return mat == Material.MELON;
         case BEETROOT:
         case BEETROOT_SEEDS:
         case BEETROOTS:
            if (!ISFLAT) {
               return mat == XBlock.LegacyBlockMaterial.BEETROOT_BLOCK.material;
            }

            return mat == Material.BEETROOTS;
         case WATER:
            return mat == Material.WATER || mat == XBlock.LegacyBlockMaterial.STATIONARY_WATER.material;
         case LAVA:
            return mat == Material.LAVA || mat == XBlock.LegacyBlockMaterial.STATIONARY_LAVA.material;
         case AIR:
         case CAVE_AIR:
         case VOID_AIR:
            return isAir(mat);
      }
   }

   public static boolean isAir(@Nullable Material material) {
      if (ISFLAT) {
         switch (material) {
            case AIR:
            case CAVE_AIR:
            case VOID_AIR:
               return true;
            default:
               return false;
         }
      } else {
         return material == Material.AIR;
      }
   }

   public static boolean isPowered(Block block) {
      if (ISFLAT) {
         if (!(block.getBlockData() instanceof Powerable)) {
            return false;
         }

         Powerable powerable = (Powerable)block.getBlockData();
         return powerable.isPowered();
      } else {
         String name = block.getType().name();
         return name.startsWith("REDSTONE_COMPARATOR") ? block.getType() == XBlock.LegacyBlockMaterial.REDSTONE_COMPARATOR_ON.material : false;
      }
   }

   public static void setPowered(Block block, boolean powered) {
      if (ISFLAT) {
         if (block.getBlockData() instanceof Powerable) {
            BlockData data = block.getBlockData();
            Powerable powerable = (Powerable)data;
            powerable.setPowered(powered);
            block.setBlockData(data, false);
         }
      } else {
         String name = block.getType().name();
         if (name.startsWith("REDSTONE_COMPARATOR")) {
            block.setType(XBlock.LegacyBlockMaterial.REDSTONE_COMPARATOR_ON.material);
         }
      }
   }

   public static boolean isOpen(Block block) {
      if (ISFLAT) {
         if (!(block.getBlockData() instanceof Openable)) {
            return false;
         }

         Openable openable = (Openable)block.getBlockData();
         return openable.isOpen();
      } else {
         BlockState state = block.getState();
         if (!(state instanceof org.bukkit.material.Openable)) {
            return false;
         }

         org.bukkit.material.Openable openable = (org.bukkit.material.Openable)state.getData();
         return openable.isOpen();
      }
   }

   public static void setOpened(Block block, boolean opened) {
      if (ISFLAT) {
         if (block.getBlockData() instanceof Openable) {
            BlockData data = block.getBlockData();
            Openable openable = (Openable)data;
            openable.setOpen(opened);
            block.setBlockData(data, false);
         }
      } else {
         BlockState state = block.getState();
         if (state instanceof org.bukkit.material.Openable) {
            org.bukkit.material.Openable openable = (org.bukkit.material.Openable)state.getData();
            openable.setOpen(opened);
            state.setData((MaterialData)openable);
            state.update(true, true);
         }
      }
   }

   public static BlockFace getRotation(Block block) {
      if (ISFLAT) {
         BlockData blockData = block.getBlockData();
         if (blockData instanceof Rotatable) {
            return ((Rotatable)blockData).getRotation();
         }

         if (blockData instanceof Directional) {
            return ((Directional)blockData).getFacing();
         }
      } else {
         BlockState state = block.getState();
         if (state instanceof Skull) {
            return ((Skull)state).getRotation();
         }

         MaterialData data = state.getData();
         if (data instanceof org.bukkit.material.Directional) {
            return ((org.bukkit.material.Directional)data).getFacing();
         }
      }

      return null;
   }

   public static void setRotation(Block block, BlockFace facing) {
      if (ISFLAT) {
         BlockData blockData = block.getBlockData();
         if (blockData instanceof Rotatable) {
            ((Rotatable)blockData).setRotation(facing);
         } else if (blockData instanceof Directional) {
            ((Directional)blockData).setFacing(facing);
         }

         block.setBlockData(blockData, false);
      } else {
         BlockState state = block.getState();
         if (state instanceof Skull) {
            ((Skull)state).setRotation(facing);
         } else {
            MaterialData data = state.getData();
            if (!(data instanceof org.bukkit.material.Directional)) {
               return;
            }

            org.bukkit.material.Directional directional = (org.bukkit.material.Directional)data;
            directional.setFacingDirection(facing);
         }

         state.update(true, true);
      }
   }

   private static boolean isMaterial(Block block, XBlock.LegacyBlockMaterial... materials) {
      Material type = block.getType();

      for (XBlock.LegacyBlockMaterial material : materials) {
         if (type == material.material) {
            return true;
         }
      }

      return false;
   }

   static {
      ITEM_TO_BLOCK.put(XMaterial.MELON_SLICE, XMaterial.MELON_STEM);
      ITEM_TO_BLOCK.put(XMaterial.MELON_SEEDS, XMaterial.MELON_STEM);
      ITEM_TO_BLOCK.put(XMaterial.CARROT_ON_A_STICK, XMaterial.CARROTS);
      ITEM_TO_BLOCK.put(XMaterial.GOLDEN_CARROT, XMaterial.CARROTS);
      ITEM_TO_BLOCK.put(XMaterial.CARROT, XMaterial.CARROTS);
      ITEM_TO_BLOCK.put(XMaterial.POTATO, XMaterial.POTATOES);
      ITEM_TO_BLOCK.put(XMaterial.BAKED_POTATO, XMaterial.POTATOES);
      ITEM_TO_BLOCK.put(XMaterial.POISONOUS_POTATO, XMaterial.POTATOES);
      ITEM_TO_BLOCK.put(XMaterial.PUMPKIN_SEEDS, XMaterial.PUMPKIN_STEM);
      ITEM_TO_BLOCK.put(XMaterial.PUMPKIN_PIE, XMaterial.PUMPKIN);
   }

   public static final class BlockTypeChangeException extends RuntimeException {
      private final XBlock.BlockTypeChangeException.Reason reason;
      private final XMaterial material;

      private BlockTypeChangeException(XBlock.BlockTypeChangeException.Reason reason, XMaterial material, Material finalMaterial) {
         super(reason.toString() + " - XMaterial: " + material + ", Finalized Material: " + finalMaterial);
         this.reason = reason;
         this.material = material;
      }

      private BlockTypeChangeException(XBlock.BlockTypeChangeException.Reason reason, XMaterial material, String extra) {
         super(reason.toString() + " - XMaterial: " + material + extra);
         this.reason = reason;
         this.material = material;
      }

      public XBlock.BlockTypeChangeException.Reason getReason() {
         return this.reason;
      }

      public XMaterial getMaterial() {
         return this.material;
      }

      public enum Reason {
         UNSUPPORTED_MATERIAL,
         NOT_A_BLOCK,
         STATE_CHANGE_FAILURE,
         UNEXPECTED_LOGIC;

         Reason() {
         }
      }
   }

   private enum LegacyBlockMaterial {
      CAKE_BLOCK,
      CROPS,
      SUGAR_CANE_BLOCK,
      BEETROOT_BLOCK,
      NETHER_WARTS,
      MELON_BLOCK,
      BED,
      BED_BLOCK,
      CARROT,
      POTATO,
      BURNING_FURNACE,
      STATIONARY_WATER,
      STATIONARY_LAVA,
      REDSTONE_LAMP_ON,
      REDSTONE_LAMP_OFF,
      REDSTONE_TORCH_ON,
      REDSTONE_TORCH_OFF,
      REDSTONE_COMPARATOR_ON,
      REDSTONE_COMPARATOR_OFF;

      @Nullable
      private final Material material = Material.getMaterial(this.name());

      LegacyBlockMaterial() {
      }
   }

   private enum LegacyMaterialGroup {
      STANDING_BANNER(XBlock.LegacyMaterialGroup.Handling.COLORABLE),
      WALL_BANNER(XBlock.LegacyMaterialGroup.Handling.COLORABLE),
      BANNER(XBlock.LegacyMaterialGroup.Handling.COLORABLE),
      CARPET(XBlock.LegacyMaterialGroup.Handling.COLORABLE),
      WOOL(XBlock.LegacyMaterialGroup.Handling.COLORABLE),
      STAINED_CLAY(XBlock.LegacyMaterialGroup.Handling.COLORABLE),
      STAINED_GLASS(XBlock.LegacyMaterialGroup.Handling.COLORABLE),
      STAINED_GLASS_PANE(XBlock.LegacyMaterialGroup.Handling.COLORABLE),
      THIN_GLASS(XBlock.LegacyMaterialGroup.Handling.COLORABLE),
      STONE,
      QUARTZ_BLOCK,
      SKULL,
      RED_ROSE,
      FLOWER_POT,
      DOUBLE_PLANT,
      LONG_GRASS,
      DIRT,
      SAND,
      SANDSTONE,
      RED_SANDSTONE,
      SPONGE,
      PRISMARINE,
      CONCRETE,
      CONCRETE_POWDER,
      ANVIL,
      SMOOTH_BRICK,
      COBBLE_WALL,
      BED,
      BED_BLOCK,
      MONSTER_EGGS,
      WOOD(XBlock.LegacyMaterialGroup.Handling.WOOD_SPECIES),
      WOOD_STEP(XBlock.LegacyMaterialGroup.Handling.WOOD_SPECIES),
      WOOD_DOUBLE_STEP(XBlock.LegacyMaterialGroup.Handling.WOOD_SPECIES),
      LEAVES(XBlock.LegacyMaterialGroup.Handling.WOOD_SPECIES),
      LEAVES_2(XBlock.LegacyMaterialGroup.Handling.WOOD_SPECIES),
      LOG(XBlock.LegacyMaterialGroup.Handling.WOOD_SPECIES),
      LOG_2(XBlock.LegacyMaterialGroup.Handling.WOOD_SPECIES),
      SAPLING(XBlock.LegacyMaterialGroup.Handling.WOOD_SPECIES),
      BRICK,
      STEP,
      DOUBLE_STEP,
      DOUBLE_STONE_SLAB2;

      private static final Map<String, XBlock.LegacyMaterialGroup> LOOKUP = new HashMap<>();
      private final Material material = Material.getMaterial(this.name());
      private final XBlock.LegacyMaterialGroup.Handling handling;

      LegacyMaterialGroup(XBlock.LegacyMaterialGroup.Handling handling) {
         this.handling = handling;
      }

      LegacyMaterialGroup() {
         this(null);
      }

      private static XBlock.LegacyMaterialGroup getMaterial(String name) {
         return LOOKUP.get(name);
      }

      static {
         for (XBlock.LegacyMaterialGroup legacyMaterial : values()) {
            LOOKUP.put(legacyMaterial.name(), legacyMaterial);
         }
      }

      private enum Handling {
         XMaterial,
         COLORABLE,
         WOOD_SPECIES;

         Handling() {
         }
      }
   }
}
