package id.seria.gacha.lib.xseries;

import id.seria.gacha.lib.xseries.base.XModule;
import id.seria.gacha.lib.xseries.base.XRegistry;
import java.util.Collection;
import java.util.Optional;
import org.bukkit.Registry;
import org.bukkit.block.banner.PatternType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class XPatternType extends XModule<XPatternType, PatternType> {
   public static final XRegistry<XPatternType, PatternType> REGISTRY = new XRegistry<>(
      PatternType.class, XPatternType.class, () -> Registry.BANNER_PATTERN, XPatternType::new, x$0 -> new XPatternType[x$0]
   );
   public static final XPatternType BASE = std("base");
   public static final XPatternType SQUARE_BOTTOM_LEFT = std("square_bottom_left");
   public static final XPatternType SQUARE_BOTTOM_RIGHT = std("square_bottom_right");
   public static final XPatternType SQUARE_TOP_LEFT = std("square_top_left");
   public static final XPatternType SQUARE_TOP_RIGHT = std("square_top_right");
   public static final XPatternType STRIPE_BOTTOM = std("stripe_bottom");
   public static final XPatternType STRIPE_TOP = std("stripe_top");
   public static final XPatternType STRIPE_LEFT = std("stripe_left");
   public static final XPatternType STRIPE_RIGHT = std("stripe_right");
   public static final XPatternType STRIPE_CENTER = std("stripe_center");
   public static final XPatternType STRIPE_MIDDLE = std("stripe_middle");
   public static final XPatternType STRIPE_DOWNRIGHT = std("stripe_downright");
   public static final XPatternType STRIPE_DOWNLEFT = std("stripe_downleft");
   public static final XPatternType SMALL_STRIPES = std("small_stripes", "STRIPE_SMALL");
   public static final XPatternType CROSS = std("cross");
   public static final XPatternType STRAIGHT_CROSS = std("straight_cross");
   public static final XPatternType TRIANGLE_BOTTOM = std("triangle_bottom");
   public static final XPatternType TRIANGLE_TOP = std("triangle_top");
   public static final XPatternType TRIANGLES_BOTTOM = std("triangles_bottom");
   public static final XPatternType TRIANGLES_TOP = std("triangles_top");
   public static final XPatternType DIAGONAL_LEFT = std("diagonal_left");
   public static final XPatternType DIAGONAL_UP_RIGHT = std("diagonal_up_right", "DIAGONAL_RIGHT_MIRROR");
   public static final XPatternType DIAGONAL_UP_LEFT = std("diagonal_up_left", "DIAGONAL_LEFT_MIRROR");
   public static final XPatternType DIAGONAL_RIGHT = std("diagonal_right");
   public static final XPatternType CIRCLE = std("circle", "CIRCLE_MIDDLE");
   public static final XPatternType RHOMBUS = std("rhombus", "RHOMBUS_MIDDLE");
   public static final XPatternType HALF_VERTICAL = std("half_vertical");
   public static final XPatternType HALF_HORIZONTAL = std("half_horizontal");
   public static final XPatternType HALF_VERTICAL_RIGHT = std("half_vertical_right", "HALF_VERTICAL_MIRROR");
   public static final XPatternType HALF_HORIZONTAL_BOTTOM = std("half_horizontal_bottom", "HALF_HORIZONTAL_MIRROR");
   public static final XPatternType BORDER = std("border");
   public static final XPatternType CURLY_BORDER = std("curly_border");
   public static final XPatternType CREEPER = std("creeper");
   public static final XPatternType GRADIENT = std("gradient");
   public static final XPatternType GRADIENT_UP = std("gradient_up");
   public static final XPatternType BRICKS = std("bricks");
   public static final XPatternType SKULL = std("skull");
   public static final XPatternType FLOWER = std("flower");
   public static final XPatternType MOJANG = std("mojang");
   public static final XPatternType GLOBE = std("globe");
   public static final XPatternType PIGLIN = std("piglin");
   public static final XPatternType FLOW = std("flow");
   public static final XPatternType GUSTER = std("guster");

   private XPatternType(PatternType patternType, String[] names) {
      super(patternType, names);
   }

   public static XPatternType of(PatternType patternType) {
      return REGISTRY.getByBukkitForm(patternType);
   }

   public static Optional<XPatternType> of(String patternType) {
      return REGISTRY.getByName(patternType);
   }

   @NotNull
   public static @Unmodifiable Collection<XPatternType> getValues() {
      return REGISTRY.getValues();
   }

   private static XPatternType std(String... names) {
      return REGISTRY.std(names);
   }

   static {
      REGISTRY.discardMetadata();
   }
}
