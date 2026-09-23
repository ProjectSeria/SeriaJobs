package id.seria.gacha.lib.xseries.reflection.jvm.classes;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface PackageHandle {
   @Language("RegExp")
   @Internal
   String JAVA_PACKAGE_PATTERN = "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
   @Language("RegExp")
   @Internal
   String JAVA_IDENTIFIER_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

   @NotNull
   String packageId();

   @NotNull
   String getBasePackageName();

   @NotNull
   String getPackage(@NotNull String var1);
}
