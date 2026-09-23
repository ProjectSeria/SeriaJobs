package id.seria.gacha.lib.xseries.reflection.jvm;

import id.seria.gacha.lib.xseries.reflection.ReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.XAccessFlag;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.ClassHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.DynamicClassHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.objects.ReflectedObject;
import id.seria.gacha.lib.xseries.reflection.jvm.objects.ReflectedObjectHandle;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftMapping;
import id.seria.gacha.lib.xseries.reflection.parser.ReflectionParser;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FieldMemberHandle extends FlaggedNamedMemberHandle {
   public static final MethodHandle MODIFIERS_FIELD;
   public static final DynamicClassHandle VarHandle = XReflection.classHandle().inPackage("java.lang.invoke").named("VarHandle");
   public static final MethodHandle VAR_HANDLE_SET = VarHandle.method().named("set").returns(void.class).parameters(Object[].class).reflectOrNull();
   private static final Object MODIFIERS_VAR_HANDLE;
   protected Boolean getter;

   public FieldMemberHandle(ClassHandle clazz) {
      super(clazz);
   }

   public boolean isGetter() {
      if (this.getter == null) {
         throw new IllegalStateException("Not specified whether the field handle is a getter or setter");
      } else {
         return this.getter;
      }
   }

   public FieldMemberHandle getter() {
      this.getter = true;
      return this;
   }

   public FieldMemberHandle asStatic() {
      super.asStatic();
      return this;
   }

   public FieldMemberHandle asFinal() {
      this.accessFlags.add(XAccessFlag.FINAL);
      return this;
   }

   public FieldMemberHandle makeAccessible() {
      super.makeAccessible();
      return this;
   }

   public FieldMemberHandle setter() {
      this.getter = false;
      return this;
   }

   public FieldMemberHandle returns(Class<?> clazz) {
      super.returns(clazz);
      return this;
   }

   public FieldMemberHandle returns(ClassHandle clazz) {
      super.returns(clazz);
      return this;
   }

   public FieldMemberHandle copy() {
      FieldMemberHandle handle = new FieldMemberHandle(this.clazz);
      handle.returnType = this.returnType;
      handle.getter = this.getter;
      handle.accessFlags.addAll(this.accessFlags);
      handle.names.addAll(this.names);
      return handle;
   }

   @Override
   public MethodHandle reflect() throws ReflectiveOperationException {
      Objects.requireNonNull(this.getter, "Not specified whether the field handle is a getter or setter");
      Field jvm = this.reflectJvm();
      return this.getter ? this.clazz.getNamespace().getLookup().unreflectGetter(jvm) : this.clazz.getNamespace().getLookup().unreflectSetter(jvm);
   }

   @Override
   public boolean exists() {
      try {
         this.reflectJvm();
         return true;
      } catch (ReflectiveOperationException ignored) {
         return false;
      }
   }

   public FieldMemberHandle signature(@Language(value = "Java", suffix = ";") String declaration) {
      return new ReflectionParser(declaration).imports(this.clazz.getNamespace()).parseField(this);
   }

   public FieldMemberHandle map(MinecraftMapping mapping, @Pattern("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") String name) {
      super.map(mapping, name);
      return this;
   }

   public FieldMemberHandle named(@Pattern("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") String... names) {
      super.named(names);
      return this;
   }

   @Override
   protected <T extends AccessibleObject & Member> T handleAccessible(T field) throws ReflectiveOperationException {
      field = super.handleAccessible(field);
      if (field == null) {
         return null;
      }

      if (this.getter != null && !this.getter && this.accessFlags.contains(XAccessFlag.FINAL) && this.accessFlags.contains(XAccessFlag.STATIC)) {
         try {
            int unfinalModifiers = XAccessFlag.FINAL.remove(field.getModifiers());
            if (MODIFIERS_VAR_HANDLE != null) {
               VAR_HANDLE_SET.invoke((Object)MODIFIERS_VAR_HANDLE, (AccessibleObject)field, (int)unfinalModifiers);
            } else {
               if (MODIFIERS_FIELD == null) {
                  throw new IllegalAccessException("Current Java version doesn't support modifying final fields. " + this);
               }

               MODIFIERS_FIELD.invoke((AccessibleObject)field, (int)unfinalModifiers);
            }
         } catch (Throwable e) {
            throw new ReflectiveOperationException("Cannot unfinal field " + this, e);
         }
      }

      return field;
   }

   @Nullable
   public Object get(Object instance) {
      try {
         return this.getter().reflectJvm().get(instance);
      } catch (ReflectiveOperationException ex) {
         throw XReflection.throwCheckedException(ex);
      }
   }

   @Nullable
   public Object getStatic() {
      try {
         return this.asStatic().getter().reflectJvm().get(null);
      } catch (ReflectiveOperationException ex) {
         throw XReflection.throwCheckedException(ex);
      }
   }

   @NotNull
   @Override
   public ReflectiveHandle<ReflectedObject> jvm() {
      return new ReflectedObjectHandle(() -> ReflectedObject.of(this.reflectJvm()));
   }

   public Field reflectJvm() throws ReflectiveOperationException {
      Objects.requireNonNull(this.returnType, "Return type not specified");
      if (this.names.isEmpty()) {
         throw new IllegalStateException("No names specified");
      }

      NoSuchFieldException errors = null;
      Field field = null;
      Class<?> clazz = this.clazz.reflect();
      Class<?> returnType = this.getReturnType();

      for (String name : this.names) {
         if (field != null) {
            break;
         }

         try {
            field = clazz.getDeclaredField(name);
            if (field.getType() != returnType) {
               throw new NoSuchFieldException("Field named '" + name + "' was found but the types don't match: " + field + " != " + this);
            }
         } catch (NoSuchFieldException ex) {
            field = null;
            if (errors == null) {
               errors = new NoSuchFieldException("None of the fields were found for " + this);
            }

            errors.addSuppressed(ex);
         }
      }

      if (field == null) {
         throw (NoSuchFieldException)XReflection.relativizeSuppressedExceptions(errors);
      } else {
         return this.handleAccessible(field);
      }
   }

   @Override
   public String toString() {
      String str = this.getClass().getSimpleName() + '{';
      str = str + this.accessFlags.stream().map(x -> x.name().toLowerCase(Locale.ENGLISH)).collect(Collectors.joining(" "));
      if (this.returnType != null) {
         str = str + this.returnType + " ";
      }

      str = str + String.join("/", this.names);
      return str + '}';
   }

   static {
      Object modVarHandle = null;
      MethodHandle modifierFieldJvm = null;

      try {
         modifierFieldJvm = XReflection.of(Field.class).field().setter().named("modifiers").returns(int.class).unreflect();
      } catch (Exception var6) {
      }

      try {
         VarHandle.reflect();
         MethodHandle PRIVATE_LOOKUP_IN = XReflection.of(MethodHandles.class)
            .method()
            .named("privateLookupIn")
            .returns(Lookup.class)
            .parameters(Class.class, Lookup.class)
            .reflect();
         MethodHandle FIND_VAR_HANDLE = VarHandle.method()
            .named("findVarHandle")
            .returns(Lookup.class)
            .parameters(Class.class, String.class, Class.class)
            .reflect();
         Lookup lookup = (Lookup)PRIVATE_LOOKUP_IN.invoke((Class)Field.class, (Lookup)MethodHandles.lookup());
         FIND_VAR_HANDLE.invoke((Lookup)lookup, (Class)Field.class, (String)"modifiers", (Class)int.class);
      } catch (Throwable var5) {
      }

      MODIFIERS_VAR_HANDLE = modVarHandle;
      MODIFIERS_FIELD = modifierFieldJvm;
   }
}
