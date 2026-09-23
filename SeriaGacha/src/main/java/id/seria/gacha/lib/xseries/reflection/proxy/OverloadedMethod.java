package id.seria.gacha.lib.xseries.reflection.proxy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public abstract class OverloadedMethod<T> {
   public OverloadedMethod() {
   }

   public abstract T get(Supplier<String> var1);

   public abstract Collection<T> getOverloads();

   static String getParameterDescriptor(Class<?>[] parameters) {
      StringBuilder stringBuilder = new StringBuilder(parameters.length * 10);

      for (Class<?> parameter : parameters) {
         Class<?> currentClass;
         for (currentClass = parameter; currentClass.isArray(); currentClass = currentClass.getComponentType()) {
            stringBuilder.append('[');
         }

         if (currentClass.isPrimitive()) {
            stringBuilder.append(getDescriptor(currentClass));
         } else if (currentClass == String.class) {
            stringBuilder.append('@');
         } else {
            stringBuilder.append(currentClass.getName());
         }
      }

      return stringBuilder.toString();
   }

   static char getDescriptor(Class<?> currentClass) {
      if (currentClass == int.class) {
         return 'I';
      } else if (currentClass == void.class) {
         return 'V';
      } else if (currentClass == boolean.class) {
         return 'Z';
      } else if (currentClass == byte.class) {
         return 'B';
      } else if (currentClass == char.class) {
         return 'C';
      } else if (currentClass == short.class) {
         return 'S';
      } else if (currentClass == double.class) {
         return 'D';
      } else if (currentClass == float.class) {
         return 'F';
      } else if (currentClass == long.class) {
         return 'J';
      } else {
         throw new AssertionError("Unknown primitive: " + currentClass);
      }
   }

   public static final class Builder<T> {
      private final Map<String, Map<String, T>> descriptorMap = new HashMap<>(10);
      private final Function<T, String> descritporProcessor;

      public Builder(Function<T, String> descritporProcessor) {
         this.descritporProcessor = descritporProcessor;
      }

      public void add(T method, String name) {
         Map<String, T> descriptors = this.descriptorMap.computeIfAbsent(name, k -> new HashMap<>(3));
         String descriptor = this.descritporProcessor.apply(method);
         if (descriptors.put(descriptor, method) != null) {
            throw new IllegalArgumentException("Method named '" + name + "' with descriptor '" + descriptor + "' was already added: " + descriptors);
         }
      }

      public ClassOverloadedMethods<T> build() {
         HashMap<String, OverloadedMethod<T>> nameMapped = new HashMap<>(this.descriptorMap.size());
         ClassOverloadedMethods<T> classOverloadedMethods = new ClassOverloadedMethods<>(nameMapped);
         this.build(nameMapped);
         return classOverloadedMethods;
      }

      public void build(Map<String, OverloadedMethod<T>> nameMapped) {
         for (Entry<String, Map<String, T>> method : this.descriptorMap.entrySet()) {
            OverloadedMethod<T> overload;
            if (method.getValue().size() == 1) {
               overload = new OverloadedMethod.Single<>(method.getValue().values().iterator().next());
            } else {
               overload = new OverloadedMethod.Multi<>(method.getValue());
            }

            nameMapped.put(method.getKey(), overload);
         }
      }
   }

   private static final class Multi<T> extends OverloadedMethod<T> {
      private final Map<String, T> descriptorMap;

      public Multi(Map<String, T> descriptorMap) {
         this.descriptorMap = descriptorMap;
      }

      @Override
      public T get(Supplier<String> descriptor) {
         return this.descriptorMap.get(descriptor.get());
      }

      @Override
      public Collection<T> getOverloads() {
         return this.descriptorMap.values();
      }

      @Override
      public String toString() {
         return "Multi(" + this.descriptorMap.keySet() + ')';
      }
   }

   private static final class Single<T> extends OverloadedMethod<T> {
      private final T object;

      public Single(T object) {
         this.object = object;
      }

      @Override
      public T get(Supplier<String> descriptor) {
         return this.object;
      }

      @Override
      public Collection<T> getOverloads() {
         return Collections.singleton(this.object);
      }

      @Override
      public String toString() {
         return "Single";
      }
   }
}
