package id.seria.gacha.lib.xseries.reflection.proxy;

import id.seria.gacha.lib.xseries.reflection.XAccessFlag;
import id.seria.gacha.lib.xseries.reflection.jvm.objects.ReflectedObject;
import id.seria.gacha.lib.xseries.reflection.proxy.processors.MappedType;
import id.seria.gacha.lib.xseries.reflection.proxy.processors.ProxyMethodInfo;
import id.seria.gacha.lib.xseries.reflection.proxy.processors.ReflectiveAnnotationProcessor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class ReflectiveProxy<T extends ReflectiveProxyObject> implements InvocationHandler {
   private static final Map<Class<?>, ReflectiveProxy<?>> PROXIFIED_CLASS_LOADER0 = new IdentityHashMap<>();
   private static final ClassLoader CLASS_LOADER = ReflectiveProxy.class.getClassLoader();
   private final Class<?> targetClass;
   private final Class<T> proxyClass;
   private T proxy;
   private final Object instance;
   private final Map<Method, ReflectiveProxy.ProxifiedObject> handles;
   private final ClassOverloadedMethods<ReflectiveProxy.ProxifiedObject> nameMapped;

   public static <T extends ReflectiveProxyObject> ReflectiveProxy<T> proxify(Class<T> interfaceClass) {
      ReflectiveProxy<?> proxified = PROXIFIED_CLASS_LOADER0.get(interfaceClass);
      if (proxified != null) {
         return (ReflectiveProxy<T>)proxified;
      }

      ReflectiveAnnotationProcessor processor = new ReflectiveAnnotationProcessor(interfaceClass);
      processor.process(ReflectiveProxy::descriptorProcessor);
      Set<Entry<String, OverloadedMethod<ProxyMethodInfo>>> entries = processor.getMapped().mappings().entrySet();
      Map<Method, ReflectiveProxy.ProxifiedObject> handles = new IdentityHashMap<>(entries.size());
      OverloadedMethod.Builder<ReflectiveProxy.ProxifiedObject> nameMapped = new OverloadedMethod.Builder<>(ReflectiveProxy::descriptorProcessor);
      ReflectiveProxy<T> proxy = new ReflectiveProxy<>(processor.getTargetClass(), interfaceClass, null, handles, nameMapped.build());
      PROXIFIED_CLASS_LOADER0.put(interfaceClass, proxy);

      for (Entry<String, OverloadedMethod<ProxyMethodInfo>> mapping : entries) {
         for (ProxyMethodInfo overload : mapping.getValue().getOverloads()) {
            ReflectedObject jvm = overload.handle.jvm().unreflect();
            MethodHandle methodHandle = (MethodHandle)overload.handle.unreflect();
            methodHandle = createDynamicProxy(null, methodHandle);
            ReflectiveProxy.ProxifiedObject proxifiedObj = new ReflectiveProxy.ProxifiedObject(
               methodHandle,
               overload,
               jvm.accessFlags().contains(XAccessFlag.STATIC),
               jvm.type() == ReflectedObject.Type.CONSTRUCTOR,
               overload.rType.isDifferent() ? (ReflectiveProxy)proxify((Class)overload.rType.synthetic) : null,
               Arrays.stream(overload.pTypes).anyMatch(MappedType::isDifferent)
                  ? Arrays.stream(overload.pTypes).map(x -> x.isDifferent() ? (ReflectiveProxy)proxify((Class)x.synthetic) : null).toArray(ReflectiveProxy[]::new)
                  : null
            );
            handles.put(overload.interfaceMethod, proxifiedObj);
            nameMapped.add(proxifiedObj, mapping.getKey());
         }
      }

      nameMapped.build(proxy.nameMapped.mappings());
      proxy.proxy = proxy.createProxy();

      for (Method proxyMethod : proxy.proxy.getClass().getDeclaredMethods()) {
         ReflectiveProxy.ProxifiedObject matched = proxy.nameMapped.get(proxyMethod.getName(), () -> descriptorProcessor(proxyMethod), true);
         if (matched != null) {
            handles.put(proxyMethod, matched);
         }
      }

      for (Class<?> proxyInterfaces : proxy.proxy.getClass().getInterfaces()) {
         for (Method proxyMethod : proxyInterfaces.getDeclaredMethods()) {
            ReflectiveProxy.ProxifiedObject matched = proxy.nameMapped.get(proxyMethod.getName(), () -> descriptorProcessor(proxyMethod), true);
            if (matched != null) {
               handles.put(proxyMethod, matched);
            }
         }
      }

      return proxy;
   }

   private static MethodHandle createDynamicProxy(@Nullable Object bindInstance, MethodHandle methodHandle) {
      int parameterCount = methodHandle.type().parameterCount();
      int requireArgs = bindInstance != null ? 1 : 0;
      if (bindInstance != null) {
         methodHandle = methodHandle.bindTo(bindInstance);
      }

      return parameterCount == requireArgs
         ? methodHandle.asType(MethodType.methodType(Object.class))
         : methodHandle.asSpreader(Object[].class, parameterCount - requireArgs).asType(MethodType.methodType(Object.class, Object[].class));
   }

   private static String descriptorProcessor(ReflectiveProxy.ProxifiedObject obj) {
      return OverloadedMethod.getParameterDescriptor(MappedType.getRealTypes(obj.proxyMethodInfo.pTypes));
   }

   private static String descriptorProcessor(ProxyMethodInfo obj) {
      return OverloadedMethod.getParameterDescriptor(MappedType.getRealTypes(obj.pTypes));
   }

   private static String descriptorProcessor(Method method) {
      return OverloadedMethod.getParameterDescriptor(method.getParameterTypes());
   }

   private ReflectiveProxy(
      Class<?> targetClass,
      Class<T> proxyClass,
      Object instance,
      Map<Method, ReflectiveProxy.ProxifiedObject> handles,
      ClassOverloadedMethods<ReflectiveProxy.ProxifiedObject> nameMapped
   ) {
      this.targetClass = targetClass;
      this.proxyClass = proxyClass;
      this.instance = instance;
      this.handles = handles;
      this.nameMapped = nameMapped;
   }

   public static void checkInterfaceClass(Class<?> interfaceClass) {
      Objects.requireNonNull(interfaceClass, "Interface class is null");
      if (!interfaceClass.isInterface()) {
         throw new IllegalArgumentException("Cannot proxify non-interface class: " + interfaceClass);
      }

      if (!ReflectiveProxyObject.class.isAssignableFrom(interfaceClass)) {
         throw new IllegalArgumentException("The provided interface class must extend ReflectiveProxyObject interface");
      }
   }

   @Internal
   @NotNull
   public T createProxy() {
      return (T)Proxy.newProxyInstance(CLASS_LOADER, new Class[]{this.proxyClass}, this);
   }

   @NotNull
   public T proxy() {
      return this.proxy;
   }

   @Nullable
   public Object instance() {
      return this.instance;
   }

   @NotNull
   public T bindTo(@NotNull Object instance) {
      if (this.instance != null) {
         throw new IllegalStateException("This proxy object already has an instance bound to it: " + this);
      }

      Objects.requireNonNull(instance, "Instance cannot be null");
      if (!this.targetClass.isAssignableFrom(instance.getClass())) {
         throw new IllegalArgumentException("The given instance doesn't match the target class: " + instance + " -> " + this);
      }

      Map<Method, ReflectiveProxy.ProxifiedObject> handles = new IdentityHashMap<>(this.handles.size());
      Map<ReflectiveProxy.ProxifiedObject, ReflectiveProxy.ProxifiedObject> boundStates = new IdentityHashMap<>(this.nameMapped.mappings().size());
      OverloadedMethod.Builder<ReflectiveProxy.ProxifiedObject> nameMapped = new OverloadedMethod.Builder<>(ReflectiveProxy::descriptorProcessor);

      for (Entry<String, OverloadedMethod<ReflectiveProxy.ProxifiedObject>> entry : this.nameMapped.mappings().entrySet()) {
         for (ReflectiveProxy.ProxifiedObject overload : entry.getValue().getOverloads()) {
            ReflectiveProxy.ProxifiedObject unbound = overload;
            ReflectiveProxy.ProxifiedObject bound = boundStates.get(unbound);
            if (bound == null) {
               if (unbound.isStatic || unbound.isConstructor) {
                  nameMapped.add(unbound, entry.getKey());
                  continue;
               }

               MethodHandle insert;
               try {
                  insert = (MethodHandle)unbound.proxyMethodInfo.handle.unreflect();
                  if (insert.type().parameterCount() == 0) {
                     throw new IllegalStateException("Non-static, non-constructor with 0 arguments found: " + insert);
                  }

                  insert = createDynamicProxy(instance, insert);
               } catch (Exception e) {
                  throw new IllegalStateException(
                     "Failed to bind "
                        + instance
                        + " to "
                        + entry.getKey()
                        + " -> "
                        + unbound.handle
                        + " (static="
                        + unbound.isStatic
                        + ", constructor="
                        + unbound.isConstructor
                        + ')',
                     e
                  );
               }

               bound = new ReflectiveProxy.ProxifiedObject(
                  insert, unbound.proxyMethodInfo, unbound.isStatic, unbound.isConstructor, unbound.rType, unbound.pTypes
               );
               boundStates.put(unbound, bound);
            }

            nameMapped.add(bound, entry.getKey());
         }
      }

      for (Entry<Method, ReflectiveProxy.ProxifiedObject> entry : this.handles.entrySet()) {
         ReflectiveProxy.ProxifiedObject unbound = entry.getValue();
         if (!unbound.isStatic && !unbound.isConstructor) {
            ReflectiveProxy.ProxifiedObject bound = boundStates.get(unbound);
            if (bound == null) {
               throw new IllegalStateException(
                  "Cannot find bound method for "
                     + entry.getKey()
                     + " ("
                     + unbound
                     + "::"
                     + unbound.hashCode()
                     + ") in "
                     + nameMapped.build()
                     + " - "
                     + boundStates.entrySet().stream().map(x -> x.getKey() + "::" + x.hashCode()).collect(Collectors.toList())
               );
            }

            handles.put(entry.getKey(), bound);
         } else {
            handles.put(entry.getKey(), unbound);
         }
      }

      ReflectiveProxy<T> bound = new ReflectiveProxy<>(this.targetClass, this.proxyClass, instance, handles, nameMapped.build());
      return bound.createProxy();
   }

   private static String getMethodList(Class<?> clazz, boolean declaredOnly) {
      return Arrays.stream(declaredOnly ? clazz.getDeclaredMethods() : clazz.getMethods())
         .map(x -> x.getName() + "::" + System.identityHashCode(x))
         .collect(Collectors.toList())
         .toString();
   }

   @Override
   public Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
      int paramCount = method.getParameterCount();
      String name = method.getName();
      if (paramCount == 0) {
         switch (name) {
            case "instance":
               return this.instance;
            case "toString":
               return this.instance == null ? this.proxyClass.toString() : this.instance.toString();
            case "hashCode":
               return this.instance == null ? this.proxyClass.hashCode() : this.instance.hashCode();
            case "notify":
               if (this.instance == null) {
                  this.proxyClass.notify();
               } else {
                  this.instance.notify();
               }

               return null;
            case "notifyAll":
               if (this.instance == null) {
                  this.proxyClass.notifyAll();
               } else {
                  this.instance.notifyAll();
               }

               return null;
            case "wait":
               if (this.instance == null) {
                  this.proxyClass.wait();
               } else {
                  this.instance.wait();
               }

               return null;
            case "getTargetClass":
               return this.targetClass;
         }
      } else if (paramCount == 1) {
         switch (name) {
            case "bindTo":
               return this.bindTo(args[0]);
            case "isInstance":
               return this.targetClass.isInstance(args[0]);
            case "equals":
               return this.instance == null ? this.proxyClass == args[0] : this.instance.equals(args[0]);
            case "wait":
               if (this.instance == null) {
                  this.proxyClass.wait((Long)args[0]);
               } else {
                  this.instance.wait((Long)args[0]);
               }

               return null;
         }
      } else if (paramCount == 2 && name.equals("wait")) {
         if (this.instance == null) {
            this.proxyClass.wait((Long)args[0], (Integer)args[1]);
         } else {
            this.instance.wait((Long)args[0], (Integer)args[1]);
         }

         return null;
      }

      ReflectiveProxy.ProxifiedObject reflectedHandle = this.handles.get(method);
      if (reflectedHandle == null) {
         reflectedHandle = this.nameMapped.get(method.getName(), () -> descriptorProcessor(method));
         this.handles.put(method, reflectedHandle);
      }

      if (!reflectedHandle.isStatic && !reflectedHandle.isConstructor && this.instance == null) {
         throw new IllegalStateException("Cannot invoke non-static non-constructor member handle with when no instance is set");
      }

      if (reflectedHandle.isConstructor && this.instance != null) {
         throw new IllegalStateException("Cannot invoke constructor twice");
      }

      if (reflectedHandle.pTypes != null && args != null) {
         for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof ReflectiveProxyObject) {
               args[i] = ((ReflectiveProxyObject)arg).instance();
            }
         }
      }

      Object result;
      try {
         if (args == null) {
            result = (Object)reflectedHandle.handle.invokeExact();
         } else {
            result = (Object)reflectedHandle.handle.invoke(args);
         }
      } catch (Throwable ex) {
         throw new IllegalStateException(
            "Failed to execute "
               + method
               + " -> "
               + reflectedHandle.handle
               + " with args "
               + (args == null ? "null" : Arrays.stream(args).<String>map(x -> x == null ? "null" : x + " (" + x.getClass().getSimpleName() + ')')),
            ex
         );
      }

      if (reflectedHandle.rType != null) {
         result = reflectedHandle.rType.bindTo(result);
      }

      return result;
   }

   @Override
   public String toString() {
      return "ReflectiveProxy(proxyClass="
         + this.proxyClass
         + ", proxy="
         + this.proxy
         + ", instance="
         + this.instance
         + ", nameMapped="
         + this.nameMapped
         + ')';
   }

   public static final class ProxifiedObject {
      private final MethodHandle handle;
      private final ProxyMethodInfo proxyMethodInfo;
      private final boolean isStatic;
      private final boolean isConstructor;
      private final ReflectiveProxy<?> rType;
      private final ReflectiveProxy<?>[] pTypes;

      public ProxifiedObject(
         MethodHandle handle, ProxyMethodInfo proxyMethodInfo, boolean isStatic, boolean isConstructor, ReflectiveProxy<?> rType, ReflectiveProxy<?>[] pTypes
      ) {
         this.handle = handle;
         this.proxyMethodInfo = proxyMethodInfo;
         this.isStatic = isStatic;
         this.isConstructor = isConstructor;
         this.rType = rType;
         this.pTypes = pTypes;
      }

      @Override
      public String toString() {
         return this.getClass().getSimpleName() + '(' + this.proxyMethodInfo.interfaceMethod + ')';
      }
   }
}
