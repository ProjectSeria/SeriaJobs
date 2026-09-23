package id.seria.gacha.lib.xseries.reflection.proxy.processors;

import id.seria.gacha.lib.xseries.reflection.ReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.StaticReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.XAccessFlag;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import id.seria.gacha.lib.xseries.reflection.aggregate.VersionHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.FieldMemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.MemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.MethodMemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.NameableReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.ClassHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.DynamicClassHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.objects.ReflectedObject;
import id.seria.gacha.lib.xseries.reflection.proxy.ClassOverloadedMethods;
import id.seria.gacha.lib.xseries.reflection.proxy.OverloadedMethod;
import id.seria.gacha.lib.xseries.reflection.proxy.ReflectiveProxy;
import id.seria.gacha.lib.xseries.reflection.proxy.ReflectiveProxyObject;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.Field;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.Final;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.Ignore;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.MappedMinecraftName;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.Private;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.Protected;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.Proxify;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.ReflectMinecraftPackage;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.ReflectName;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.Static;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class ReflectiveAnnotationProcessor {
   private final Class<? extends ReflectiveProxyObject> interfaceClass;
   private ClassOverloadedMethods<ProxyMethodInfo> mapped;
   private OverloadedMethod.Builder<ProxyMethodInfo> mappedHandles;
   private Class<?> targetClass;

   public ReflectiveAnnotationProcessor(Class<? extends ReflectiveProxyObject> interfaceClass) {
      ReflectiveProxy.checkInterfaceClass(interfaceClass);
      this.interfaceClass = interfaceClass;
   }

   private void error(String msg) {
      this.error(msg, null);
   }

   private void error(String msg, Throwable ex) {
      throw new IllegalStateException(msg + " (Proxified Interface: " + this.interfaceClass + ')', ex);
   }

   protected static boolean isAnnotationInherited(Class<?> clazz, Method method, Class<? extends Annotation> annotation) {
      try {
         Method superMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
         if (superMethod.isAnnotationPresent(annotation)) {
            return true;
         }
      } catch (NoSuchMethodException var7) {
      }

      for (Class<?> superInterface : clazz.getInterfaces()) {
         if (isAnnotationInherited(superInterface, method, annotation)) {
            return true;
         }
      }

      return false;
   }

   public void loadDependencies(Function<Class<?>, Boolean> isLoaded) {
      for (OverloadedMethod<ProxyMethodInfo> overloads : this.mapped.mappings().values()) {
         for (ProxyMethodInfo overload : overloads.getOverloads()) {
            this.loadDependency(overload.rType, isLoaded);

            for (MappedType pType : overload.pTypes) {
               this.loadDependency(pType, isLoaded);
            }
         }
      }
   }

   private void loadDependency(MappedType type, Function<Class<?>, Boolean> isLoaded) {
      if (ReflectiveProxyObject.class.isAssignableFrom(type.synthetic) && type.synthetic != this.interfaceClass && !isLoaded.apply(type.synthetic)) {
         XReflection.proxify((Class)type.synthetic);
      }
   }

   public void process(Function<ProxyMethodInfo, String> descriptorProcessor) {
      ClassHandle classHandle = this.processTargetClass();
      Method[] interfaceMethods = this.interfaceClass.getMethods();
      this.mappedHandles = new OverloadedMethod.Builder<>(descriptorProcessor);

      for (Method method : interfaceMethods) {
         if (!isAnnotationInherited(this.interfaceClass, method, Ignore.class)) {
            boolean asStatic = method.isAnnotationPresent(Static.class);
            boolean asFinal = method.isAnnotationPresent(Final.class);
            MappedType[] pTypes = new MappedType[0];
            MappedType rType;
            MemberHandle handle;
            if (method.isAnnotationPresent(id.seria.gacha.lib.xseries.reflection.proxy.annotations.Constructor.class)) {
               Class<?> returnType = method.getReturnType();
               if (returnType != this.targetClass && returnType != this.interfaceClass && returnType != Object.class) {
                  this.error("Method marked with @Constructor must return Object.class, " + this.targetClass + " or " + this.interfaceClass);
               }

               rType = this.unwrap(returnType);
               pTypes = this.unwrap(method.getParameterTypes());
               handle = classHandle.constructor(MappedType.getRealTypes(pTypes));
               if (asStatic) {
                  this.error("Constructor cannot be static: " + method);
               }

               if (asFinal) {
                  this.error("Constructor cannot be final: " + method);
               }
            } else if (method.isAnnotationPresent(Field.class)) {
               FieldMemberHandle field = classHandle.field();
               if (method.getReturnType() == void.class) {
                  field.setter();
                  if (method.getParameterCount() != 1) {
                     this.error("Field setter method must have only one parameter: " + method);
                  }

                  MappedType fieldType = this.unwrap(method.getParameterTypes()[0]);
                  rType = new MappedType(void.class, void.class);
                  pTypes = new MappedType[]{fieldType};
                  field.returns(fieldType.real);
               } else {
                  field.getter();
                  if (method.getParameterCount() != 0) {
                     this.error("Field getter method must not have any parameters: " + method);
                  }

                  rType = this.unwrap(method.getReturnType());
                  field.returns(rType.real);
               }

               if (asStatic) {
                  field.asStatic();
               }

               if (asFinal) {
                  field.asFinal();
               }

               handle = field;
            } else {
               rType = this.unwrap(method.getReturnType());
               pTypes = this.unwrap(method.getParameterTypes());
               MethodMemberHandle methHandle = classHandle.method().returns(rType.real).parameters(MappedType.getRealTypes(pTypes));
               if (asStatic) {
                  methHandle = methHandle.asStatic();
               }

               if (asFinal) {
                  this.error("Declaring method as final has no effect: " + method);
               }

               handle = methHandle;
            }

            boolean visibilitySet = false;
            if (method.isAnnotationPresent(Private.class)) {
               visibilitySet = true;
               handle.getAccessFlags().add(XAccessFlag.PRIVATE);
            }

            if (method.isAnnotationPresent(Protected.class)) {
               if (visibilitySet) {
                  this.error("Cannot have two visibility modifier private and protected for " + method);
               }

               handle.getAccessFlags().add(XAccessFlag.PRIVATE);
            }

            if (handle instanceof NameableReflectiveHandle) {
               ((NameableReflectiveHandle)handle).named(method.getName());
               this.reflectNames((NameableReflectiveHandle)handle, method);
            }

            ReflectiveHandle<MethodHandle> cached = handle.cached();

            try {
               cached.reflect();
            } catch (ReflectiveOperationException e) {
               this.error("Failed to map " + method, e);
            }

            ProxyMethodInfo methodInfo = new ProxyMethodInfo(cached, method, rType, pTypes);
            this.mappedHandles.add(methodInfo, method.getName());
         }
      }

      this.mapped = this.mappedHandles.build();
   }

   @NotNull
   public ClassHandle processTargetClass() {
      Proxify reflectClass = this.interfaceClass.getAnnotation(Proxify.class);
      ReflectMinecraftPackage mcClass = this.interfaceClass.getAnnotation(ReflectMinecraftPackage.class);
      if (reflectClass == null && mcClass == null) {
         this.error("Proxy interface is not annotated with @Class or @ReflectMinecraftPackage");
      }

      if (reflectClass != null && mcClass != null) {
         this.error("Proxy interface cannot contain both @Class or @ReflectMinecraftPackage");
      }

      ClassHandle classHandle;
      boolean ignoreCurrentName;
      if (reflectClass != null) {
         if (reflectClass.target() != void.class) {
            classHandle = XReflection.of(reflectClass.target());
            ignoreCurrentName = true;
         } else {
            DynamicClassHandle dynClassHandle = XReflection.classHandle();
            classHandle = dynClassHandle;
            dynClassHandle.inPackage(reflectClass.packageName());
            ignoreCurrentName = reflectClass.ignoreCurrentName();
         }
      } else {
         DynamicClassHandle dynClassHandle = XReflection.ofMinecraft();
         classHandle = dynClassHandle;
         dynClassHandle.inPackage(mcClass.type(), mcClass.packageName());
         ignoreCurrentName = mcClass.ignoreCurrentName();
      }

      if (classHandle instanceof DynamicClassHandle) {
         DynamicClassHandle dynamicClassHandle = (DynamicClassHandle)classHandle;
         if (!ignoreCurrentName) {
            dynamicClassHandle.named(this.interfaceClass.getSimpleName());
         }

         this.reflectNames(dynamicClassHandle, this.interfaceClass);
      }

      this.targetClass = classHandle.unreflect();
      MappedType.LOOK_AHEAD.put(this.interfaceClass, this.targetClass);
      return classHandle;
   }

   public Class<?> getTargetClass() {
      return this.targetClass;
   }

   public ClassOverloadedMethods<ProxyMethodInfo> getMapped() {
      return this.mapped;
   }

   private void reflectNames(NameableReflectiveHandle handle, AnnotatedElement annotated) {
      MappedMinecraftName[] mapped = annotated.getDeclaredAnnotationsByType(MappedMinecraftName.class);
      ReflectName[] rawNames = annotated.getDeclaredAnnotationsByType(ReflectName.class);

      for (MappedMinecraftName mcMapped : mapped) {
         this.reflectNames0(handle, mcMapped.names());
      }

      this.reflectNames0(handle, rawNames);
   }

   private void reflectDefaults() {
      try {
         MethodHandle privateLookupIn = XReflection.of(MethodHandles.class)
            .method("public static Lookup privateLookupIn(Class<?> targetClass, Lookup caller) throws IllegalAccessException")
            .reflectOrNull();
         if (privateLookupIn == null) {
            Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
            constructor.setAccessible(true);
            Lookup lookup = constructor.newInstance(this.interfaceClass).in(this.interfaceClass);

            for (Method method : this.interfaceClass.getMethods()) {
               if (method.isDefault()) {
                  this.handleDefaultMethod(method, lookup.unreflectSpecial(method, this.interfaceClass));
               }
            }
         }

         Lookup lookup = (Lookup)privateLookupIn.invokeExact((Class)this.interfaceClass, (Lookup)MethodHandles.lookup());

         for (Method method : this.interfaceClass.getMethods()) {
            if (method.isDefault()) {
               MethodHandle meth = lookup.findSpecial(
                  this.interfaceClass, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()), this.interfaceClass
               );
               this.handleDefaultMethod(method, meth);
            }
         }
      } catch (Throwable ex) {
         ex.printStackTrace();
      }
   }

   private void handleDefaultMethod(Method method, MethodHandle defaultHandle) {
      this.mappedHandles
         .add(
            new ProxyMethodInfo(
               new StaticReflectiveHandle<>(defaultHandle, ReflectedObject.of(method)),
               method,
               this.unwrap(method.getReturnType()),
               this.unwrap(method.getParameterTypes())
            ),
            method.getName()
         );
   }

   private void reflectNames0(NameableReflectiveHandle handle, ReflectName[] reflectedNames) {
      if (reflectedNames.length != 0) {
         VersionHandle<String[]> versionControl = null;
         String[] chosen = null;
         int index = 0;

         for (ReflectName name : reflectedNames) {
            index++;
            if (chosen != null) {
               this.error("Cannot contain more tha one @ReflectName if no version is specified");
            }

            if (!name.version().isEmpty()) {
               if (index == reflectedNames.length) {
                  this.error("Last @ReflectName should not contain version");
               }

               int[] semVer = Arrays.stream(name.version().split("\\.")).mapToInt(Integer::parseInt).toArray();
               if (versionControl == null) {
                  if (semVer.length == 1) {
                     versionControl = XReflection.v(semVer[0], name.value());
                  }

                  if (semVer.length == 2) {
                     versionControl = XReflection.v(semVer[1], name.value());
                  }

                  if (semVer.length == 3) {
                     versionControl = XReflection.v(semVer[1], semVer[2], name.value());
                  }
               } else {
                  if (semVer.length == 1) {
                     versionControl.v(semVer[0], name.value());
                  }

                  if (semVer.length == 2) {
                     versionControl.v(semVer[1], name.value());
                  }

                  if (semVer.length == 3) {
                     versionControl.v(semVer[1], semVer[2], name.value());
                  }
               }
            } else if (versionControl != null) {
               if (index != reflectedNames.length) {
                  this.error("One of @ReflectName doesn't contain a version.");
               } else {
                  chosen = versionControl.orElse(name.value());
               }
            } else {
               chosen = name.value();
            }
         }

         handle.named(chosen);
      }
   }

   private MappedType[] unwrap(Class<?>[] classes) {
      MappedType[] unwrapped = new MappedType[classes.length];

      for (int i = 0; i < classes.length; i++) {
         Class<?> clazz = classes[i];
         unwrapped[i] = this.unwrap(clazz);
      }

      return unwrapped;
   }

   private MappedType unwrap(Class<?> clazz) {
      if (clazz == this.interfaceClass || clazz == ReflectiveProxyObject.class) {
         return new MappedType(this.interfaceClass, this.targetClass);
      } else if (ReflectiveProxyObject.class.isAssignableFrom(clazz)) {
         Class<?> real = MappedType.getMappedTypeOrCreate((Class<? extends ReflectiveProxyObject>)clazz);
         return new MappedType(clazz, real);
      } else {
         return new MappedType(clazz, clazz);
      }
   }
}
