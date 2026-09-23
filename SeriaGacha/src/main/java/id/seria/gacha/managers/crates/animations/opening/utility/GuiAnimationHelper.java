package id.seria.gacha.managers.crates.animations.opening.utility;

import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public final class GuiAnimationHelper {
   public static final String DEFAULT_TITLE = "&8&lOpening...";
   public static final String DEFAULT_FILLER = "BLACK_STAINED_GLASS_PANE";
   public static final String DEFAULT_MARKER_NORMAL = "YELLOW_STAINED_GLASS_PANE";
   public static final String DEFAULT_MARKER_FLICKER = "ORANGE_STAINED_GLASS_PANE";
   public static final List<String> DEFAULT_CHANCE_LORE = Arrays.asList("", "&eYour chance: &6%chance%%");
   public static final XMaterial[] RAINBOW_PALETTE = new XMaterial[]{
      XMaterial.PURPLE_STAINED_GLASS_PANE,
      XMaterial.BLUE_STAINED_GLASS_PANE,
      XMaterial.CYAN_STAINED_GLASS_PANE,
      XMaterial.LIME_STAINED_GLASS_PANE,
      XMaterial.YELLOW_STAINED_GLASS_PANE,
      XMaterial.ORANGE_STAINED_GLASS_PANE,
      XMaterial.RED_STAINED_GLASS_PANE,
      XMaterial.PINK_STAINED_GLASS_PANE,
      XMaterial.MAGENTA_STAINED_GLASS_PANE
   };

   private GuiAnimationHelper() {
   }

   public static ItemStack buildRainbowPane(XMaterial material) {
      return ItemBuilder.of(material.parseItem()).setDisplayName(Translations.r("&r")).setHideTooltip(true).build();
   }

   public static String getTitle(AnimationProperties properties) {
      return properties.getPropertyOrDefault("title", "&8&lOpening...");
   }

   public static List<String> getChanceLore(AnimationProperties properties) {
      Object raw = properties.getProperty("chanceLore");
      return raw instanceof List ? (List)raw : DEFAULT_CHANCE_LORE;
   }

   public static ItemStack buildMaterial(AnimationProperties properties, String prefix, String defaultMaterial) {
      String matName = properties.getPropertyOrDefault(prefix, defaultMaterial);

      try {
         XMaterial xMat = XMaterial.valueOf(matName);
         ItemStack item = xMat.parseItem();
         if (item == null) {
            return XMaterial.BLACK_STAINED_GLASS_PANE.parseItem();
         }

         ItemBuilder builder = ItemBuilder.of(item).setDisplayName(Translations.r("&r")).setHideTooltip(true);
         int cmd = properties.getPropertyOrDefault(prefix + "Cmd", 0);
         if (cmd > 0) {
            builder.setCustomModelData(cmd);
         }

         String model = properties.getPropertyOrDefault(prefix + "Model", "");
         if (model != null && !model.isEmpty()) {
            builder.setItemModel(model);
         }

         return builder.build();
      } catch (Exception e) {
         return ItemBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem()).setDisplayName(Translations.r("&r")).setHideTooltip(true).build();
      }
   }

   public static ItemStack buildFiller(AnimationProperties properties) {
      return buildMaterial(properties, "filler", "BLACK_STAINED_GLASS_PANE");
   }

   public static ItemStack buildMarkerNormal(AnimationProperties properties) {
      return buildMaterial(properties, "markerNormal", "YELLOW_STAINED_GLASS_PANE");
   }

   public static ItemStack buildMarkerFlicker(AnimationProperties properties) {
      return buildMaterial(properties, "markerFlicker", "ORANGE_STAINED_GLASS_PANE");
   }

   public static ItemStack buildBlackGlass(AnimationProperties properties) {
      return buildMaterial(properties, "blackGlass", "BLACK_STAINED_GLASS_PANE");
   }

   public static ItemStack buildYellowGlass(AnimationProperties properties) {
      return buildMaterial(properties, "yellowGlass", "YELLOW_STAINED_GLASS_PANE");
   }

   public static ItemStack buildWhiteGlass(AnimationProperties properties) {
      return buildMaterial(properties, "whiteGlass", "WHITE_STAINED_GLASS_PANE");
   }

   public static ItemStack buildGrayGlass(AnimationProperties properties) {
      return buildMaterial(properties, "grayGlass", "GRAY_STAINED_GLASS_PANE");
   }

   public static ItemStack buildPurpleGlass(AnimationProperties properties) {
      return buildMaterial(properties, "purpleGlass", "PURPLE_STAINED_GLASS_PANE");
   }

   public static List<BaseReward> buildDedupedReel(List<? extends BaseReward> availableRewards, int size, int winnerIndex, BaseReward winner) {
      List<BaseReward> reel = new ArrayList<>(size);

      for (int i = 0; i < size; i++) {
         reel.add(nextDistinctReward(availableRewards, i > 0 ? reel.get(i - 1) : null, null));
      }

      reel.set(winnerIndex, winner);
      if (winnerIndex - 1 >= 0) {
         reel.set(winnerIndex - 1, nextDistinctReward(availableRewards, winner, winnerIndex - 2 >= 0 ? reel.get(winnerIndex - 2) : null));
      }

      if (winnerIndex + 1 < size) {
         reel.set(winnerIndex + 1, nextDistinctReward(availableRewards, winner, winnerIndex + 2 < size ? reel.get(winnerIndex + 2) : null));
      }

      return reel;
   }

   private static BaseReward nextDistinctReward(List<? extends BaseReward> availableRewards, BaseReward avoid1, BaseReward avoid2) {
      if (availableRewards.size() <= 1) {
         return MathUtil.random(availableRewards);
      }

      int attempts = 0;

      BaseReward candidate;
      do {
         candidate = MathUtil.random(availableRewards);
         attempts++;
      } while (attempts < 20 && (candidate == avoid1 || candidate == avoid2));

      return candidate;
   }
}
