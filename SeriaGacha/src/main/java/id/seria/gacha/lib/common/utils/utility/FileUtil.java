package id.seria.gacha.lib.common.utils.utility;

public class FileUtil {
   public FileUtil() {
   }

   public static String getNameWithoutExtension(String fileName) {
      int dotIndex = fileName.lastIndexOf(46);
      return dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
   }

   public static String getExtensionWithoutName(String fileName) {
      int dotIndex = fileName.lastIndexOf(46);
      return dotIndex == -1 ? fileName : fileName.substring(dotIndex + 1);
   }
}
