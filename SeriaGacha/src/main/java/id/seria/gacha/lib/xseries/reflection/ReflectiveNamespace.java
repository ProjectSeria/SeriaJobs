package id.seria.gacha.lib.xseries.reflection;

import id.seria.gacha.lib.xseries.reflection.jvm.classes.ClassHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.DynamicClassHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.StaticClassHandle;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftClassHandle;
import id.seria.gacha.lib.xseries.reflection.parser.ReflectionParser;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

public class ReflectiveNamespace {
   private final Map<String, Class<?>> imports = new HashMap<>();
   private final Lookup lookup = MethodHandles.lookup();
   private final Set<ClassHandle> handles = Collections.newSetFromMap(new IdentityHashMap<>());

   protected ReflectiveNamespace() {
   }

   public ReflectiveNamespace imports(@NotNull Class<?>... classes) {
      for (Class<?> clazz : classes) {
         this.imports(clazz.getSimpleName(), clazz);
      }

      return this;
   }

   public ReflectiveNamespace imports(@NotNull String name, @NotNull Class<?> clazz) {
      Objects.requireNonNull(name);
      Objects.requireNonNull(clazz);
      this.imports.put(name, clazz);
      return this;
   }

   @NotNull
   @Internal
   public Map<String, Class<?>> getImports() {
      for (ClassHandle handle : this.handles) {
         Class<?> clazz = handle.reflectOrNull();
         if (clazz != null) {
            for (String className : handle.getPossibleNames()) {
               this.imports.put(className, clazz);
            }
         }
      }

      return this.imports;
   }

   @Internal
   public void link(ClassHandle handle) {
      if (handle.getNamespace() != this) {
         throw new IllegalArgumentException("Not the same namespace");
      }

      this.handles.add(handle);
   }

   @NotNull
   @Internal
   public Lookup getLookup() {
      return this.lookup;
   }

   public StaticClassHandle of(Class<?> clazz) {
      this.imports(clazz);
      return new StaticClassHandle(this, clazz);
   }

   public DynamicClassHandle classHandle(@Language(value = "Java", suffix = "{}") String declaration) {
      DynamicClassHandle classHandle = new DynamicClassHandle(this);
      return new ReflectionParser(declaration).imports(this).parseClass(classHandle);
   }

   public DynamicClassHandle classHandle() {
      return new DynamicClassHandle(this);
   }

   public MinecraftClassHandle ofMinecraft(@Language(value = "Java", suffix = "{}") String declaration) {
      MinecraftClassHandle classHandle = new MinecraftClassHandle(this);
      return new ReflectionParser(declaration).imports(this).parseClass(classHandle);
   }

   public MinecraftClassHandle ofMinecraft() {
      return new MinecraftClassHandle(this);
   }
}
