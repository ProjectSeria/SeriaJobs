package id.seria.gacha.internal;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.Ui;
import id.seria.gacha.lib.common.utils.inventory.ItemStackUtil;
import id.seria.gacha.lib.xseries.XItemFlag;
import id.seria.gacha.lib.xseries.XMaterial;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;

public class Constants {
   public static String ADDON_TAG = " " + Ui.hex("&#E99411", "&e") + Ui.smallCaps("(ADDON)");
   public static DecimalFormat DECIMAL1_FORMAT = new DecimalFormat("#.#");
   public static DecimalFormat DECIMAL2_FORMAT = new DecimalFormat("#.##");
   public static final List<XItemFlag> ITEM_FLAGS = ItemStackUtil.DEFAULT_HIDE_FLAGS;
   public static final XMaterial[] CHEST_MATERIALS = new XMaterial[]{
      XMaterial.CHEST,
      XMaterial.ENDER_CHEST,
      XMaterial.SHULKER_BOX,
      XMaterial.WHITE_SHULKER_BOX,
      XMaterial.ORANGE_SHULKER_BOX,
      XMaterial.MAGENTA_SHULKER_BOX,
      XMaterial.LIGHT_BLUE_SHULKER_BOX,
      XMaterial.YELLOW_SHULKER_BOX,
      XMaterial.LIME_SHULKER_BOX,
      XMaterial.PINK_SHULKER_BOX,
      XMaterial.GRAY_SHULKER_BOX,
      XMaterial.LIGHT_GRAY_SHULKER_BOX,
      XMaterial.CYAN_SHULKER_BOX,
      XMaterial.PURPLE_SHULKER_BOX,
      XMaterial.BLUE_SHULKER_BOX,
      XMaterial.BROWN_SHULKER_BOX,
      XMaterial.GREEN_SHULKER_BOX,
      XMaterial.RED_SHULKER_BOX,
      XMaterial.BLACK_SHULKER_BOX,
      XMaterial.TRAPPED_CHEST
   };
   public static List<XMaterial> AVAILABLE_MATERIALS = new ArrayList<>();

   public Constants() {
   }

   public static Translatable addon(String name) {
      return Translatable.concatenate(Translatable.literal(Ui.hex("&#FFD700", "&e") + name), ADDON_TAG);
   }

   public static void load() {
   }

   static {
      List<String> prohibitedKeywords = Arrays.asList(
         "CAKE",
         "SIGN",
         "FAN",
         "BANNER",
         "BED",
         "BUTTON",
         "DRIPSTONE",
         "BUD",
         "CAMPFIRE",
         "LANTERN",
         "BELL",
         "DOOR",
         "PLATE",
         "GATE",
         "CORAL",
         "EGG",
         "GLASS_PANE",
         "MELON",
         "CHAIN",
         "BARS",
         "FENCE",
         "BAMBOO",
         "CONDUIT",
         "ROD",
         "STAND",
         "PISTON",
         "CLUSTER",
         "WATER_CAULDRON",
         "LAVA_CAULDRON",
         "POWDER_SNOW_CAULDRON",
         "FROSTED_ICE",
         "CACTUS",
         "STAIR",
         "SLAB"
      );
      List<XMaterial> allowedMaterials = Arrays.asList(XMaterial.SEA_LANTERN);
      AVAILABLE_MATERIALS.add(XMaterial.PLAYER_HEAD);
      AVAILABLE_MATERIALS.addAll(Arrays.asList(CHEST_MATERIALS));
      AVAILABLE_MATERIALS.add(XMaterial.BARRIER);
      AVAILABLE_MATERIALS.add(XMaterial.DRAGON_EGG);

      label44:
      for (Material material : Material.values()) {
         if (material.isBlock() && material.isSolid()) {
            try {
               XMaterial displayMaterial = XMaterial.matchXMaterial(material);
               if (!AVAILABLE_MATERIALS.contains(displayMaterial)) {
                  if (!allowedMaterials.contains(displayMaterial)) {
                     for (String keyword : prohibitedKeywords) {
                        if (material.name().contains(keyword)) {
                           continue label44;
                        }
                     }
                  }

                  AVAILABLE_MATERIALS.add(displayMaterial);
               }
            } catch (IllegalArgumentException var9) {
            }
         }
      }
   }
}
