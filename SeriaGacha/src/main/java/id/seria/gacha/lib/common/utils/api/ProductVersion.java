package id.seria.gacha.lib.common.utils.api;

public class ProductVersion implements Comparable<ProductVersion> {
   private final String version;

   public static int compare(String firstVersion, String secondVersion) {
      return new ProductVersion(firstVersion).compareTo(new ProductVersion(secondVersion));
   }

   public ProductVersion(String version) {
      if (version == null) {
         throw new IllegalArgumentException("Version can not be null");
      }

      if (version.equalsIgnoreCase("${project.version}")) {
         this.version = "1.0.0";
      } else {
         String cleanVersion = version.split("[^0-9.]")[0];
         if (!cleanVersion.matches("[0-9]+(\\.[0-9]+)*")) {
            throw new IllegalArgumentException("Invalid version format: " + version);
         }

         this.version = cleanVersion;
      }
   }

   public final String get() {
      return this.version;
   }

   public int compareTo(ProductVersion that) {
      if (that == null) {
         return 1;
      }

      String[] thisParts = this.get().split("\\.");
      String[] thatParts = that.get().split("\\.");
      int length = Math.max(thisParts.length, thatParts.length);

      for (int i = 0; i < length; i++) {
         int thisPart = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
         int thatPart = i < thatParts.length ? Integer.parseInt(thatParts[i]) : 0;
         if (thisPart < thatPart) {
            return -1;
         }

         if (thisPart > thatPart) {
            return 1;
         }
      }

      return 0;
   }

   @Override
   public boolean equals(Object that) {
      if (this == that) {
         return true;
      } else if (that == null) {
         return false;
      } else {
         return this.getClass() != that.getClass() ? false : this.compareTo((ProductVersion)that) == 0;
      }
   }

   @Override
   public String toString() {
      return this.version;
   }

   public ProductVersion clone() {
      return new ProductVersion(this.version);
   }
}
