package id.seria.gacha.lib.xseries.reflection.minecraft;

import id.seria.gacha.lib.xseries.reflection.XReflection;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.PackageHandle;
import org.intellij.lang.annotations.Pattern;

public enum MinecraftPackage implements PackageHandle {
   NMS(XReflection.NMS_PACKAGE),
   CB(XReflection.CRAFTBUKKIT_PACKAGE),
   BUKKIT("org.bukkit"),
   SPIGOT("org.spigotmc");

   private final String packageId;

   MinecraftPackage(String packageName) {
      this.packageId = packageName;
   }

   @Override
   public String packageId() {
      return this.name();
   }

   @Override
   public String getBasePackageName() {
      return this.packageId;
   }

   @Override
   public String getPackage(
      @Pattern("(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") String packageName
   ) {
      if (!packageName.startsWith(".") && !packageName.endsWith(".")) {
         return packageName.isEmpty() || this == NMS && !XReflection.supports(17) ? this.packageId : this.packageId + '.' + packageName;
      } else {
         throw new IllegalArgumentException("Package name must not start or end with a dot: " + packageName + " (" + this + ')');
      }
   }
}
