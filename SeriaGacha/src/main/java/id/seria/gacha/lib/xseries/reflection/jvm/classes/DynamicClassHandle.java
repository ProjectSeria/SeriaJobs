package id.seria.gacha.lib.xseries.reflection.jvm.classes;

import com.google.common.base.Strings;
import id.seria.gacha.lib.xseries.reflection.ReflectiveNamespace;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import id.seria.gacha.lib.xseries.reflection.jvm.NameableReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftMapping;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

public class DynamicClassHandle extends ClassHandle implements NameableReflectiveHandle {
   protected ClassHandle parent;
   protected String packageName;
   protected final Set<String> classNames = new HashSet<>(5);
   protected int array;

   public DynamicClassHandle(ReflectiveNamespace namespace) {
      super(namespace);
   }

   public DynamicClassHandle inPackage(
      @Pattern("(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") @NotNull String packageName
   ) {
      Objects.requireNonNull(packageName, "Null package name");
      this.packageName = packageName;
      return this;
   }

   public DynamicClassHandle inPackage(@NotNull PackageHandle packageHandle) {
      return this.inPackage(packageHandle, "");
   }

   public DynamicClassHandle inPackage(
      @NotNull PackageHandle packageHandle,
      @Pattern("(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") @NotNull String packageName
   ) {
      Objects.requireNonNull(packageHandle, "Null package handle type");
      Objects.requireNonNull(packageName, "Null package handle name");
      if (this.parent != null) {
         throw new IllegalStateException("Cannot change package of an inner class: " + packageHandle + " -> " + packageName);
      }

      this.packageName = packageHandle.getPackage(packageName);
      return this;
   }

   public DynamicClassHandle map(MinecraftMapping mapping, String name) {
      return this.named(name);
   }

   public DynamicClassHandle named(@Pattern("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") @NotNull String... classNames) {
      Objects.requireNonNull(classNames);

      for (String className : this.classNames) {
         Objects.requireNonNull(className, () -> "Cannot add null class name from: " + Arrays.toString(classNames) + " to " + this);
      }

      this.classNames.addAll(Arrays.asList(classNames));
      return this;
   }

   public String[] reflectClassNames() {
      if (this.parent == null) {
         Objects.requireNonNull(this.packageName, "Package name is null");
      }

      String[] classNames = new String[this.classNames.size()];
      Class<?> parent = this.parent == null ? null : XReflection.of(this.parent.unreflect()).asArray(0).unreflect();
      int i = 0;

      for (String className : this.classNames) {
         String clazz;
         if (parent == null) {
            clazz = this.packageName + '.' + className;
         } else {
            clazz = parent.getName() + '$' + className;
         }

         if (this.array != 0) {
            clazz = Strings.repeat("[", this.array) + 'L' + clazz + ';';
         }

         classNames[i++] = clazz;
      }

      return classNames;
   }

   public DynamicClassHandle copy() {
      DynamicClassHandle handle = new DynamicClassHandle(this.namespace);
      handle.array = this.array;
      handle.parent = this.parent;
      handle.packageName = this.packageName;
      handle.classNames.addAll(this.classNames);
      return handle;
   }

   public Class<?> reflect() throws ClassNotFoundException {
      String[] classNames = this.reflectClassNames();
      if (classNames.length == 0) {
         throw new IllegalStateException("No class name specified for " + this);
      }

      ClassNotFoundException errors = null;

      for (String className : classNames) {
         try {
            return this.checkConstraints(Class.forName(className));
         } catch (ClassNotFoundException ex) {
            if (errors == null) {
               errors = new ClassNotFoundException("None of the classes were found");
            }

            errors.addSuppressed(ex);
         }
      }

      throw (ClassNotFoundException)XReflection.relativizeSuppressedExceptions(errors);
   }

   public DynamicClassHandle asArray(int dimension) {
      if (dimension < 0) {
         throw new IllegalArgumentException("Array dimension cannot be negative: " + dimension);
      }

      this.array = dimension;
      return this;
   }

   @Override
   public boolean isArray() {
      return this.array > 0;
   }

   @Override
   public Set<String> getPossibleNames() {
      return this.classNames;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName()
         + '{'
         + (this.parent == null ? "" : this.parent + " -> ")
         + (this.parent == null ? this.packageName : (this.packageName == null ? "" : this.packageName))
         + '('
         + String.join("|", this.classNames)
         + ')'
         + (this.array == 0 ? "" : "[" + this.array + ']')
         + " }";
   }
}
