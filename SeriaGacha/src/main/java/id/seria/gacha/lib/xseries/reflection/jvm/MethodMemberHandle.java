package id.seria.gacha.lib.xseries.reflection.jvm;

import id.seria.gacha.lib.xseries.reflection.ReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.XAccessFlag;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.ClassHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.objects.ReflectedObject;
import id.seria.gacha.lib.xseries.reflection.jvm.objects.ReflectedObjectHandle;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftMapping;
import id.seria.gacha.lib.xseries.reflection.parser.ReflectionParser;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Experimental;

public class MethodMemberHandle extends FlaggedNamedMemberHandle {
   protected ClassHandle[] parameterTypes = new ClassHandle[0];

   public MethodMemberHandle(ClassHandle clazz) {
      super(clazz);
   }

   public MethodMemberHandle parameters(ClassHandle... parameterTypes) {
      this.parameterTypes = parameterTypes;
      return this;
   }

   public MethodMemberHandle returns(Class<?> clazz) {
      super.returns(clazz);
      return this;
   }

   public MethodMemberHandle returns(ClassHandle clazz) {
      super.returns(clazz);
      return this;
   }

   public MethodMemberHandle asStatic() {
      super.asStatic();
      return this;
   }

   public MethodMemberHandle parameters(Class<?>... parameterTypes) {
      this.parameterTypes = Arrays.stream(parameterTypes).map(XReflection::of).toArray(ClassHandle[]::new);
      return this;
   }

   public MethodMemberHandle signature(@Language(value = "Java", suffix = ";") String declaration) {
      return new ReflectionParser(declaration).imports(this.clazz.getNamespace()).parseMethod(this);
   }

   public MethodMemberHandle map(MinecraftMapping mapping, @Pattern("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") String name) {
      super.map(mapping, name);
      return this;
   }

   public MethodMemberHandle named(@Pattern("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*") String... names) {
      super.named(names);
      return this;
   }

   public MethodType getMethodType() {
      return MethodType.methodType(this.getReturnType(), FlaggedNamedMemberHandle.getParameters(this, this.parameterTypes));
   }

   @Experimental
   public CallSite toLambda(Class<?> proxy, String interfaceMethod) throws ReflectiveOperationException, LambdaConversionException {
      MethodType factoryType = this.getMethodType();
      MethodType implType;
      if (this.accessFlags.contains(XAccessFlag.STATIC)) {
         implType = MethodType.methodType(proxy);
      } else {
         implType = MethodType.methodType(proxy, this.clazz.reflect());
      }

      return LambdaMetafactory.metafactory(this.clazz.getNamespace().getLookup(), interfaceMethod, implType, factoryType, this.reflect(), factoryType);
   }

   @Override
   public MethodHandle reflect() throws ReflectiveOperationException {
      return this.clazz.getNamespace().getLookup().unreflect(this.reflectJvm());
   }

   @NotNull
   @Override
   public ReflectiveHandle<ReflectedObject> jvm() {
      return new ReflectedObjectHandle(() -> ReflectedObject.of(this.reflectJvm()));
   }

   public Method reflectJvm() throws ReflectiveOperationException {
      Objects.requireNonNull(this.returnType, "Return type not specified");
      if (this.names.isEmpty()) {
         throw new IllegalStateException("No names specified");
      }

      NoSuchMethodException errors = null;
      Method method = null;
      Class<?> clazz = this.clazz.reflect();
      Class<?>[] parameterTypes = FlaggedNamedMemberHandle.getParameters(this, this.parameterTypes);
      Class<?> returnType = this.getReturnType();

      for (String name : this.names) {
         if (method != null) {
            break;
         }

         try {
            method = clazz.getDeclaredMethod(name, parameterTypes);
            if (method.getReturnType() != returnType) {
               throw new NoSuchMethodException(
                  "Method named '" + name + "' was found but the return types don't match: " + this.returnType + " != " + method.getReturnType()
               );
            }
         } catch (NoSuchMethodException ignored) {
            try {
               method = clazz.getMethod(name, parameterTypes);
               if (method.getReturnType() != returnType) {
                  throw new NoSuchMethodException(
                     "Method named '" + name + "' was found but the return types don't match: " + this.returnType + " != " + method.getReturnType()
                  );
               }
            } catch (NoSuchMethodException realEx) {
               method = null;
               if (errors == null) {
                  errors = new NoSuchMethodException("None of the methods were found for " + this);
               }

               errors.addSuppressed(realEx);
            }
         }
      }

      if (method == null) {
         throw (NoSuchMethodException)XReflection.relativizeSuppressedExceptions(errors);
      } else {
         return this.handleAccessible(method);
      }
   }

   public MethodMemberHandle copy() {
      MethodMemberHandle handle = new MethodMemberHandle(this.clazz);
      handle.returnType = this.returnType;
      handle.parameterTypes = this.parameterTypes;
      handle.accessFlags.addAll(this.accessFlags);
      handle.names.addAll(this.names);
      return handle;
   }

   @Override
   public String toString() {
      String str = this.getClass().getSimpleName() + '{';
      str = str + this.accessFlags.stream().map(x -> x.name().toLowerCase(Locale.ENGLISH)).collect(Collectors.joining(" "));
      if (this.returnType != null) {
         str = str + this.returnType + " ";
      }

      str = str + String.join("/", this.names);
      str = str + '(' + Arrays.stream(this.parameterTypes).map(Object::toString).collect(Collectors.joining(", ")) + ')';
      return str + '}';
   }
}
