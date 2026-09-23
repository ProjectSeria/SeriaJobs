package id.seria.gacha.lib.common.utils.utility;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ColorUtil {
   private static final List<ColorUtil.ColorName> colors = new ArrayList<>();

   public ColorUtil() {
   }

   public static List<Color> getColors() {
      return colors.stream().map(c -> new Color(c.r, c.g, c.b)).collect(Collectors.toList());
   }

   public static String getHexColor(Color color) {
      int r = color.getRed();
      int g = color.getGreen();
      int b = color.getBlue();
      return String.format("#%02X%02X%02X", r, g, b);
   }

   public static String getHexColor(org.bukkit.Color color) {
      int r = color.getRed();
      int g = color.getGreen();
      int b = color.getBlue();
      return String.format("#%02X%02X%02X", r, g, b);
   }

   public static String getColorName(Color color) {
      return getColorNameFromRgb(color.getRed(), color.getGreen(), color.getBlue());
   }

   public static String getColorName(org.bukkit.Color color) {
      return getColorNameFromRgb(color.getRed(), color.getGreen(), color.getBlue());
   }

   public static String getColorDisplayName(Color color) {
      return StringUtil.humanizeCamelcase(getColorName(color));
   }

   public static String getColorDisplayName(org.bukkit.Color color) {
      return StringUtil.humanizeCamelcase(getColorName(color));
   }

   public static String getColorNameFromRgb(int r, int g, int b) {
      ColorUtil.ColorName closestMatch = null;
      int minMSE = Integer.MAX_VALUE;

      for (ColorUtil.ColorName c : colors) {
         int mse = c.computeMSE(r, g, b);
         if (mse < minMSE) {
            minMSE = mse;
            closestMatch = c;
         }
      }

      return closestMatch != null ? closestMatch.getName() : "Unknown.";
   }

   public static Color darkenColor(Color color, double amount) {
      org.apache.commons.lang.Validate.isTrue(amount >= 0.0 && amount <= 1.0, "Amount should be between 0 and 1");
      return new Color((int)(color.getRed() * amount), (int)(color.getGreen() * amount), (int)(color.getBlue() * amount));
   }

   public static String getColorNameFromHex(int hexColor) {
      int r = (hexColor & 0xFF0000) >> 16;
      int g = (hexColor & 0xFF00) >> 8;
      int b = hexColor & 0xFF;
      return getColorNameFromRgb(r, g, b);
   }

   public static int colorToHex(Color c) {
      return Integer.decode("0x" + Integer.toHexString(c.getRGB()).substring(2));
   }

   public static String getColorNameFromColor(Color color) {
      return getColorNameFromRgb(color.getRed(), color.getGreen(), color.getBlue());
   }

   public static String getColorNameFromColor(org.bukkit.Color color) {
      return getColorNameFromRgb(color.getRed(), color.getGreen(), color.getBlue());
   }

   static {
      colors.add(new ColorUtil.ColorName("AliceBlue", 240, 248, 255));
      colors.add(new ColorUtil.ColorName("AntiqueWhite", 250, 235, 215));
      colors.add(new ColorUtil.ColorName("Aqua", 0, 255, 255));
      colors.add(new ColorUtil.ColorName("Aquamarine", 127, 255, 212));
      colors.add(new ColorUtil.ColorName("Azure", 240, 255, 255));
      colors.add(new ColorUtil.ColorName("Beige", 245, 245, 220));
      colors.add(new ColorUtil.ColorName("Bisque", 255, 228, 196));
      colors.add(new ColorUtil.ColorName("Black", 0, 0, 0));
      colors.add(new ColorUtil.ColorName("BlanchedAlmond", 255, 235, 205));
      colors.add(new ColorUtil.ColorName("Blue", 0, 0, 255));
      colors.add(new ColorUtil.ColorName("BlueViolet", 138, 43, 226));
      colors.add(new ColorUtil.ColorName("Brown", 165, 42, 42));
      colors.add(new ColorUtil.ColorName("BurlyWood", 222, 184, 135));
      colors.add(new ColorUtil.ColorName("CadetBlue", 95, 158, 160));
      colors.add(new ColorUtil.ColorName("Chartreuse", 127, 255, 0));
      colors.add(new ColorUtil.ColorName("Chocolate", 210, 105, 30));
      colors.add(new ColorUtil.ColorName("Coral", 255, 127, 80));
      colors.add(new ColorUtil.ColorName("CornflowerBlue", 100, 149, 237));
      colors.add(new ColorUtil.ColorName("Cornsilk", 255, 248, 220));
      colors.add(new ColorUtil.ColorName("Crimson", 220, 20, 60));
      colors.add(new ColorUtil.ColorName("Cyan", 0, 255, 255));
      colors.add(new ColorUtil.ColorName("DarkBlue", 0, 0, 139));
      colors.add(new ColorUtil.ColorName("DarkCyan", 0, 139, 139));
      colors.add(new ColorUtil.ColorName("DarkGoldenRod", 184, 134, 11));
      colors.add(new ColorUtil.ColorName("DarkGray", 169, 169, 169));
      colors.add(new ColorUtil.ColorName("DarkGreen", 0, 100, 0));
      colors.add(new ColorUtil.ColorName("DarkKhaki", 189, 183, 107));
      colors.add(new ColorUtil.ColorName("DarkMagenta", 139, 0, 139));
      colors.add(new ColorUtil.ColorName("DarkOliveGreen", 85, 107, 47));
      colors.add(new ColorUtil.ColorName("DarkOrange", 255, 140, 0));
      colors.add(new ColorUtil.ColorName("DarkOrchid", 153, 50, 204));
      colors.add(new ColorUtil.ColorName("DarkRed", 139, 0, 0));
      colors.add(new ColorUtil.ColorName("DarkSalmon", 233, 150, 122));
      colors.add(new ColorUtil.ColorName("DarkSeaGreen", 143, 188, 143));
      colors.add(new ColorUtil.ColorName("DarkSlateBlue", 72, 61, 139));
      colors.add(new ColorUtil.ColorName("DarkSlateGray", 47, 79, 79));
      colors.add(new ColorUtil.ColorName("DarkTurquoise", 0, 206, 209));
      colors.add(new ColorUtil.ColorName("DarkViolet", 148, 0, 211));
      colors.add(new ColorUtil.ColorName("DeepPink", 255, 20, 147));
      colors.add(new ColorUtil.ColorName("DeepSkyBlue", 0, 191, 255));
      colors.add(new ColorUtil.ColorName("DimGray", 105, 105, 105));
      colors.add(new ColorUtil.ColorName("DodgerBlue", 30, 144, 255));
      colors.add(new ColorUtil.ColorName("FireBrick", 178, 34, 34));
      colors.add(new ColorUtil.ColorName("FloralWhite", 255, 250, 240));
      colors.add(new ColorUtil.ColorName("ForestGreen", 34, 139, 34));
      colors.add(new ColorUtil.ColorName("Fuchsia", 255, 0, 255));
      colors.add(new ColorUtil.ColorName("Gainsboro", 220, 220, 220));
      colors.add(new ColorUtil.ColorName("GhostWhite", 248, 248, 255));
      colors.add(new ColorUtil.ColorName("Gold", 255, 215, 0));
      colors.add(new ColorUtil.ColorName("GoldenRod", 218, 165, 32));
      colors.add(new ColorUtil.ColorName("Gray", 128, 128, 128));
      colors.add(new ColorUtil.ColorName("Green", 0, 128, 0));
      colors.add(new ColorUtil.ColorName("GreenYellow", 173, 255, 47));
      colors.add(new ColorUtil.ColorName("HoneyDew", 240, 255, 240));
      colors.add(new ColorUtil.ColorName("HotPink", 255, 105, 180));
      colors.add(new ColorUtil.ColorName("IndianRed", 205, 92, 92));
      colors.add(new ColorUtil.ColorName("Indigo", 75, 0, 130));
      colors.add(new ColorUtil.ColorName("Ivory", 255, 255, 240));
      colors.add(new ColorUtil.ColorName("Khaki", 240, 230, 140));
      colors.add(new ColorUtil.ColorName("Lavender", 230, 230, 250));
      colors.add(new ColorUtil.ColorName("LavenderBlush", 255, 240, 245));
      colors.add(new ColorUtil.ColorName("LawnGreen", 124, 252, 0));
      colors.add(new ColorUtil.ColorName("LemonChiffon", 255, 250, 205));
      colors.add(new ColorUtil.ColorName("LightBlue", 173, 216, 230));
      colors.add(new ColorUtil.ColorName("LightCoral", 240, 128, 128));
      colors.add(new ColorUtil.ColorName("LightCyan", 224, 255, 255));
      colors.add(new ColorUtil.ColorName("LightGoldenRodYellow", 250, 250, 210));
      colors.add(new ColorUtil.ColorName("LightGray", 211, 211, 211));
      colors.add(new ColorUtil.ColorName("LightGreen", 144, 238, 144));
      colors.add(new ColorUtil.ColorName("LightPink", 255, 182, 193));
      colors.add(new ColorUtil.ColorName("LightSalmon", 255, 160, 122));
      colors.add(new ColorUtil.ColorName("LightSeaGreen", 32, 178, 170));
      colors.add(new ColorUtil.ColorName("LightSkyBlue", 135, 206, 250));
      colors.add(new ColorUtil.ColorName("LightSlateGray", 119, 136, 153));
      colors.add(new ColorUtil.ColorName("LightSteelBlue", 176, 196, 222));
      colors.add(new ColorUtil.ColorName("LightYellow", 255, 255, 224));
      colors.add(new ColorUtil.ColorName("Lime", 0, 255, 0));
      colors.add(new ColorUtil.ColorName("LimeGreen", 50, 205, 50));
      colors.add(new ColorUtil.ColorName("Linen", 250, 240, 230));
      colors.add(new ColorUtil.ColorName("Magenta", 255, 0, 255));
      colors.add(new ColorUtil.ColorName("Maroon", 128, 0, 0));
      colors.add(new ColorUtil.ColorName("MediumAquaMarine", 102, 205, 170));
      colors.add(new ColorUtil.ColorName("MediumBlue", 0, 0, 205));
      colors.add(new ColorUtil.ColorName("MediumOrchid", 186, 85, 211));
      colors.add(new ColorUtil.ColorName("MediumPurple", 147, 112, 219));
      colors.add(new ColorUtil.ColorName("MediumSeaGreen", 60, 179, 113));
      colors.add(new ColorUtil.ColorName("MediumSlateBlue", 123, 104, 238));
      colors.add(new ColorUtil.ColorName("MediumSpringGreen", 0, 250, 154));
      colors.add(new ColorUtil.ColorName("MediumTurquoise", 72, 209, 204));
      colors.add(new ColorUtil.ColorName("MediumVioletRed", 199, 21, 133));
      colors.add(new ColorUtil.ColorName("MidnightBlue", 25, 25, 112));
      colors.add(new ColorUtil.ColorName("MintCream", 245, 255, 250));
      colors.add(new ColorUtil.ColorName("MistyRose", 255, 228, 225));
      colors.add(new ColorUtil.ColorName("Moccasin", 255, 228, 181));
      colors.add(new ColorUtil.ColorName("NavajoWhite", 255, 222, 173));
      colors.add(new ColorUtil.ColorName("Navy", 0, 0, 128));
      colors.add(new ColorUtil.ColorName("OldLace", 253, 245, 230));
      colors.add(new ColorUtil.ColorName("Olive", 128, 128, 0));
      colors.add(new ColorUtil.ColorName("OliveDrab", 107, 142, 35));
      colors.add(new ColorUtil.ColorName("Orange", 255, 165, 0));
      colors.add(new ColorUtil.ColorName("OrangeRed", 255, 69, 0));
      colors.add(new ColorUtil.ColorName("Orchid", 218, 112, 214));
      colors.add(new ColorUtil.ColorName("PaleGoldenRod", 238, 232, 170));
      colors.add(new ColorUtil.ColorName("PaleGreen", 152, 251, 152));
      colors.add(new ColorUtil.ColorName("PaleTurquoise", 175, 238, 238));
      colors.add(new ColorUtil.ColorName("PaleVioletRed", 219, 112, 147));
      colors.add(new ColorUtil.ColorName("PapayaWhip", 255, 239, 213));
      colors.add(new ColorUtil.ColorName("PeachPuff", 255, 218, 185));
      colors.add(new ColorUtil.ColorName("Peru", 205, 133, 63));
      colors.add(new ColorUtil.ColorName("Pink", 255, 192, 203));
      colors.add(new ColorUtil.ColorName("Plum", 221, 160, 221));
      colors.add(new ColorUtil.ColorName("PowderBlue", 176, 224, 230));
      colors.add(new ColorUtil.ColorName("Purple", 128, 0, 128));
      colors.add(new ColorUtil.ColorName("Red", 255, 0, 0));
      colors.add(new ColorUtil.ColorName("RosyBrown", 188, 143, 143));
      colors.add(new ColorUtil.ColorName("RoyalBlue", 65, 105, 225));
      colors.add(new ColorUtil.ColorName("SaddleBrown", 139, 69, 19));
      colors.add(new ColorUtil.ColorName("Salmon", 250, 128, 114));
      colors.add(new ColorUtil.ColorName("SandyBrown", 244, 164, 96));
      colors.add(new ColorUtil.ColorName("SeaGreen", 46, 139, 87));
      colors.add(new ColorUtil.ColorName("SeaShell", 255, 245, 238));
      colors.add(new ColorUtil.ColorName("Sienna", 160, 82, 45));
      colors.add(new ColorUtil.ColorName("Silver", 192, 192, 192));
      colors.add(new ColorUtil.ColorName("SkyBlue", 135, 206, 235));
      colors.add(new ColorUtil.ColorName("SlateBlue", 106, 90, 205));
      colors.add(new ColorUtil.ColorName("SlateGray", 112, 128, 144));
      colors.add(new ColorUtil.ColorName("Snow", 255, 250, 250));
      colors.add(new ColorUtil.ColorName("SpringGreen", 0, 255, 127));
      colors.add(new ColorUtil.ColorName("SteelBlue", 70, 130, 180));
      colors.add(new ColorUtil.ColorName("Tan", 210, 180, 140));
      colors.add(new ColorUtil.ColorName("Teal", 0, 128, 128));
      colors.add(new ColorUtil.ColorName("Thistle", 216, 191, 216));
      colors.add(new ColorUtil.ColorName("Tomato", 255, 99, 71));
      colors.add(new ColorUtil.ColorName("Turquoise", 64, 224, 208));
      colors.add(new ColorUtil.ColorName("Violet", 238, 130, 238));
      colors.add(new ColorUtil.ColorName("Wheat", 245, 222, 179));
      colors.add(new ColorUtil.ColorName("White", 255, 255, 255));
      colors.add(new ColorUtil.ColorName("WhiteSmoke", 245, 245, 245));
      colors.add(new ColorUtil.ColorName("Yellow", 255, 255, 0));
      colors.add(new ColorUtil.ColorName("YellowGreen", 154, 205, 50));
   }

   private static class ColorName {
      public int r;
      public int g;
      public int b;
      public String name;

      public ColorName(String name, int r, int g, int b) {
         this.r = r;
         this.g = g;
         this.b = b;
         this.name = name;
      }

      public int computeMSE(int pixR, int pixG, int pixB) {
         return ((pixR - this.r) * (pixR - this.r) + (pixG - this.g) * (pixG - this.g) + (pixB - this.b) * (pixB - this.b)) / 3;
      }

      public int getR() {
         return this.r;
      }

      public int getG() {
         return this.g;
      }

      public int getB() {
         return this.b;
      }

      public String getName() {
         return this.name;
      }
   }
}
