package id.seria.gacha.lib.xseries.reflection.proxy.processors;

import id.seria.gacha.lib.xseries.reflection.ReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.XAccessFlag;
import id.seria.gacha.lib.xseries.reflection.jvm.ConstructorMemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.FieldMemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.MethodMemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.ClassHandle;
import id.seria.gacha.lib.xseries.reflection.proxy.ReflectiveProxy;
import id.seria.gacha.lib.xseries.reflection.proxy.ReflectiveProxyObject;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.Ignore;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class ReflectiveHandleProxyProcessor {
   public ReflectiveHandleProxyProcessor() {
   }

   public static <T extends ReflectiveProxyObject> T proxify(
      @NotNull Class<T> interfaceClass, @NotNull ClassHandle targetClass, @NotNull ReflectiveHandle<?>... handles
   ) {
      Set<ReflectiveHandle<?>> remainingHandles = Collections.newSetFromMap(new IdentityHashMap<>());
      remainingHandles.addAll(Arrays.asList(handles));
      Method[] interfaceMethods = interfaceClass.getMethods();
      Map<Method, ReflectiveProxy.ProxifiedObject> mappedHandles = new IdentityHashMap<>(interfaceMethods.length);

      label76:
      for (Method method : interfaceMethods) {
         if (!ReflectiveAnnotationProcessor.isAnnotationInherited(interfaceClass, method, Ignore.class)) {
            String name = method.getName();
            Iterator<ReflectiveHandle<?>> iter = remainingHandles.iterator();

            while (iter.hasNext()) {
               ReflectiveHandle<?> next = iter.next();
               if (next instanceof FieldMemberHandle) {
                  FieldMemberHandle field = (FieldMemberHandle)next;
                  if (field.getPossibleNames().stream().anyMatch(x -> x.equals(name))) {
                     iter.remove();
                     mappedHandles.put(
                        method,
                        new ReflectiveProxy.ProxifiedObject(field.unreflect(), null, field.getAccessFlags().contains(XAccessFlag.STATIC), false, null, null)
                     );
                     break;
                  }
               } else if (next instanceof MethodMemberHandle) {
                  MethodMemberHandle methodHandle = (MethodMemberHandle)next;
                  if (methodHandle.getPossibleNames().stream().anyMatch(x -> x.equals(name))) {
                     iter.remove();
                     mappedHandles.put(
                        method,
                        new ReflectiveProxy.ProxifiedObject(
                           methodHandle.unreflect(), null, methodHandle.getAccessFlags().contains(XAccessFlag.STATIC), false, null, null
                        )
                     );
                     break;
                  }
               } else if (next instanceof ConstructorMemberHandle && method.getReturnType() == interfaceClass && name.equals(interfaceClass.getName())) {
                  ConstructorMemberHandle ctor = (ConstructorMemberHandle)next;
                  if (ctor.getParameterTypes().length == method.getParameterCount()) {
                     Constructor<?> constructor;
                     try {
                        constructor = ctor.reflectJvm();
                     } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("Failed to map " + method, e);
                     }

                     int index = 0;
                     Parameter[] var16 = method.getParameters();
                     int var17 = var16.length;
                     int var18 = 0;

                     while (true) {
                        if (var18 < var17) {
                           Parameter parameter = var16[var18];
                           if (constructor.getParameters()[index].getType() == parameter.getType()) {
                              index++;
                              var18++;
                              continue;
                           }

                           index = -1;
                        }

                        if (index != -1) {
                           iter.remove();
                           mappedHandles.put(method, new ReflectiveProxy.ProxifiedObject(ctor.unreflect(), null, false, true, null, null));
                           continue label76;
                        }
                        break;
                     }
                  }
               }
            }
         }
      }

      return null;
   }
}
