package id.seria.gacha.lib.common.nms.interfaces.particles.data.color;

import java.awt.Color;
import lombok.Generated;

public class NoteColor extends RegularColor {
   private static final Color[] COLORS = new Color[]{
      Color.decode("#77D700"),
      Color.decode("#95C000"),
      Color.decode("#B2A500"),
      Color.decode("#CC8600"),
      Color.decode("#E26500"),
      Color.decode("#F34100"),
      Color.decode("#FC1E00"),
      Color.decode("#FE000F"),
      Color.decode("#F70033"),
      Color.decode("#E8005A"),
      Color.decode("#CF0083"),
      Color.decode("#AE00A9"),
      Color.decode("#8600CC"),
      Color.decode("#5B00E7"),
      Color.decode("#2D00F9"),
      Color.decode("#020AFE"),
      Color.decode("#0037F6"),
      Color.decode("#0068E0"),
      Color.decode("#009ABC"),
      Color.decode("#00C68D"),
      Color.decode("#00E958"),
      Color.decode("#00FC21"),
      Color.decode("#1FFC00"),
      Color.decode("#59E800"),
      Color.decode("#94C100")
   };
   private final int note;

   public static int calculateNote(Color targetColor) {
      int closestIndex = 0;
      double minDistance = Double.MAX_VALUE;

      for (int i = 0; i < COLORS.length; i++) {
         double distance = calculateColorDistance(targetColor, COLORS[i]);
         if (distance < minDistance) {
            minDistance = distance;
            closestIndex = i;
         }
      }

      return closestIndex;
   }

   private static double calculateColorDistance(Color color1, Color color2) {
      int r1 = color1.getRed();
      int g1 = color1.getGreen();
      int b1 = color1.getBlue();
      int r2 = color2.getRed();
      int g2 = color2.getGreen();
      int b2 = color2.getBlue();
      return Math.sqrt((r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1));
   }

   public NoteColor(Color color) {
      super(color);
      this.note = calculateNote(color);
   }

   public NoteColor(int red, int green, int blue) {
      super(red, green, blue);
      this.note = calculateNote(new Color(red, green, blue));
   }

   @Generated
   public int getNote() {
      return this.note;
   }
}
