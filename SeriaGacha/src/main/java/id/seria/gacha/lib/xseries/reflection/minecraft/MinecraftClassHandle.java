package id.seria.gacha.lib.xseries.reflection.minecraft;

import id.seria.gacha.lib.xseries.reflection.ReflectiveNamespace;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.DynamicClassHandle;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Pattern;

public class MinecraftClassHandle extends DynamicClassHandle {
   public MinecraftClassHandle(ReflectiveNamespace namespace) {
      super(namespace);
   }

   public MinecraftClassHandle inPackage(MinecraftPackage minecraftPackage) {
      super.inPackage(minecraftPackage);
      return this;
   }

   public MinecraftClassHandle inPackage(
      MinecraftPackage minecraftPackage,
      @Pattern("(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") String packageName
   ) {
      super.inPackage(minecraftPackage, packageName);
      return this;
   }

   public MinecraftClassHandle inner(@Language(value = "Java", suffix = "{}") String declaration) {
      return this.inner(this.namespace.ofMinecraft(declaration));
   }

   public MinecraftClassHandle named(@Pattern("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") String... clazzNames) {
      super.named(clazzNames);
      return this;
   }

   public MinecraftClassHandle map(MinecraftMapping mapping, @Pattern("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") String className) {
      this.classNames.add(className);
      return this;
   }

   public MinecraftClassHandle copy() {
      MinecraftClassHandle handle = new MinecraftClassHandle(this.namespace);
      handle.array = this.array;
      handle.parent = this.parent;
      handle.packageName = this.packageName;
      handle.classNames.addAll(this.classNames);
      return handle;
   }
}
