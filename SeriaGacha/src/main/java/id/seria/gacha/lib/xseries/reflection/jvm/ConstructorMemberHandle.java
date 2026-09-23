package id.seria.gacha.lib.xseries.reflection.jvm;

import id.seria.gacha.lib.xseries.reflection.ReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.XAccessFlag;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.ClassHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.objects.ReflectedObject;
import id.seria.gacha.lib.xseries.reflection.jvm.objects.ReflectedObjectHandle;
import id.seria.gacha.lib.xseries.reflection.parser.ReflectionParser;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

public class ConstructorMemberHandle extends MemberHandle {
   protected ClassHandle[] parameterTypes = new ClassHandle[0];

   public ConstructorMemberHandle(ClassHandle clazz) {
      super(clazz);
   }

   @Internal
   public ClassHandle[] getParameterTypes() {
      return this.parameterTypes;
   }

   public ConstructorMemberHandle parameters(Class<?>... parameterTypes) {
      this.parameterTypes = Arrays.stream(parameterTypes).map(XReflection::of).toArray(ClassHandle[]::new);
      return this;
   }

   public ConstructorMemberHandle parameters(ClassHandle... parameterTypes) {
      this.parameterTypes = parameterTypes;
      return this;
   }

   @Override
   public MethodHandle reflect() throws ReflectiveOperationException {
      if (this.accessFlags.contains(XAccessFlag.FINAL)) {
         throw new UnsupportedOperationException("Constructor cannot be final: " + this);
      }

      if (this.accessFlags.contains(XAccessFlag.PRIVATE)) {
         return this.clazz.getNamespace().getLookup().unreflectConstructor(this.reflectJvm());
      }

      Class<?>[] parameterTypes = FlaggedNamedMemberHandle.getParameters(this, this.parameterTypes);
      return this.clazz.getNamespace().getLookup().findConstructor(this.clazz.unreflect(), MethodType.methodType(void.class, parameterTypes));
   }

   public ConstructorMemberHandle signature(@Language(value = "Java", suffix = ";") String declaration) {
      return new ReflectionParser(declaration).imports(this.clazz.getNamespace()).parseConstructor(this);
   }

   @NotNull
   @Override
   public ReflectiveHandle<ReflectedObject> jvm() {
      return new ReflectedObjectHandle(() -> ReflectedObject.of(this.reflectJvm()));
   }

   public Constructor<?> reflectJvm() throws ReflectiveOperationException {
      Class<?>[] parameterTypes = FlaggedNamedMemberHandle.getParameters(this, this.parameterTypes);
      return this.handleAccessible(this.clazz.unreflect().getDeclaredConstructor(parameterTypes));
   }

   public ConstructorMemberHandle copy() {
      ConstructorMemberHandle handle = new ConstructorMemberHandle(this.clazz);
      handle.parameterTypes = this.parameterTypes;
      handle.accessFlags.addAll(this.accessFlags);
      return handle;
   }

   @Override
   public String toString() {
      String str = this.getClass().getSimpleName() + '{';
      str = str + this.accessFlags.stream().map(x -> x.name().toLowerCase(Locale.ENGLISH)).collect(Collectors.joining(" "));
      str = str + this.clazz.toString() + ' ';
      str = str + '(' + Arrays.stream(this.parameterTypes).map(Object::toString).collect(Collectors.joining(", ")) + ')';
      return str + '}';
   }
}
