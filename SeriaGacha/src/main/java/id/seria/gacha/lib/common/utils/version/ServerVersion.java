package id.seria.gacha.lib.common.utils.version;

import lombok.Generated;

public enum ServerVersion {
   v1_20_R4(20, "v1_20_R3"),
   v1_20_R6(20, "v1_20_R4"),
   v1_21_R1(21, "v1_21_R1"),
   v1_21_R2(21, "v1_21_R2"),
   v1_21_R3(21, "v1_21_R3"),
   v1_21_R4(21, "v1_21_R3"),
   v1_21_R5(21, "v1_21_R4"),
   v1_21_R6(21, "v1_21_R5"),
   v1_21_R7(21, "v1_21_R5"),
   v1_21_R9(21, "v1_21_R6"),
   v1_21_R10(21, "v1_21_R6"),
   v1_21_R11(21, "v1_21_R7"),
   v26_R1(26, "v26_R1"),
   v26_R2(26, "v26_R2");

   private final int numeric;
   private final String packageName;
   private static final ServerVersion CURRENT_VERSION = extractCurrentVersion();
   public static final int MINECRAFT_VERSION = CURRENT_VERSION.getNumeric();

   public ClientVersion toClientVersion() {
      switch (this.numeric) {
         case 20:
            return ClientVersion.v1_20;
         case 21:
            return ClientVersion.v1_21;
         case 26:
            return ClientVersion.v26_1;
         default:
            return null;
      }
   }

   public String displayName() {
      String name = this.name().substring(1).replace("_", ".");
      int index = name.indexOf("R");
      return index != -1 ? name.substring(0, index - 1) : name;
   }

   private static ServerVersion extractCurrentVersion() {
      String nmsVersionName = VersionUtils.extractNMSVersion();

      try {
         return valueOf(nmsVersionName);
      } catch (IllegalArgumentException e) {
         return null;
      }
   }

   public static boolean isValid() {
      return CURRENT_VERSION != null;
   }

   public static ServerVersion getCurrent() {
      if (CURRENT_VERSION == null) {
         throw new IllegalStateException(
            "This plugin does not support \"" + VersionUtils.extractNMSVersion() + "\" version. Notify plugin author [Splitrox_] about this error."
         );
      } else {
         return CURRENT_VERSION;
      }
   }

   public static boolean isGreaterEqualThan(ServerVersion other) {
      return getCurrent().ordinal() >= other.ordinal();
   }

   public static boolean isBetween(ServerVersion from, ServerVersion to) {
      return from.ordinal() <= getCurrent().ordinal() && getCurrent().ordinal() <= to.ordinal();
   }

   @Generated
   public int getNumeric() {
      return this.numeric;
   }

   @Generated
   public String getPackageName() {
      return this.packageName;
   }

   @Generated
   ServerVersion(final int numeric, final String packageName) {
      this.numeric = numeric;
      this.packageName = packageName;
   }
}
