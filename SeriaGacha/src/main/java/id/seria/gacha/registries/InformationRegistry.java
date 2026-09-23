package id.seria.gacha.registries;

import id.seria.gacha.internal.Key;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

public final class InformationRegistry {
   private static final Map<Key, Supplier<Boolean>> BOOLEAN_SUPPLIERS = new LinkedHashMap<>();
   private static final Map<Key, Map<Class<?>, Function<?, Boolean>>> BOOLEAN_FUNCTIONS = new LinkedHashMap<>();
   private static final Map<Key, Map<Class<?>, Function<?, ?>>> VALUE_FUNCTIONS = new LinkedHashMap<>();

   private InformationRegistry() {
   }

   public static synchronized void registerBoolean(Key identifier, Supplier<Boolean> supplier) {
      BOOLEAN_SUPPLIERS.put(Objects.requireNonNull(identifier, "identifier"), Objects.requireNonNull(supplier, "supplier"));
   }

   public static synchronized <T> void registerBoolean(Key identifier, Class<T> type, Function<T, Boolean> resolver) {
      Key checkedIdentifier = Objects.requireNonNull(identifier, "identifier");
      Class<T> checkedType = Objects.requireNonNull(type, "type");
      Function<T, Boolean> checkedResolver = Objects.requireNonNull(resolver, "resolver");
      BOOLEAN_FUNCTIONS.computeIfAbsent(checkedIdentifier, key -> new LinkedHashMap<>()).put(checkedType, checkedResolver);
   }

   public static synchronized void unregister(Key identifier) {
      if (identifier != null) {
         BOOLEAN_SUPPLIERS.remove(identifier);
         BOOLEAN_FUNCTIONS.remove(identifier);
         VALUE_FUNCTIONS.remove(identifier);
      }
   }

   public static synchronized void unregister(Key identifier, Class<?> type) {
      if (identifier != null && type != null) {
         Map<Class<?>, Function<?, Boolean>> resolvers = BOOLEAN_FUNCTIONS.get(identifier);
         if (resolvers != null) {
            resolvers.remove(type);
            if (resolvers.isEmpty()) {
               BOOLEAN_FUNCTIONS.remove(identifier);
            }

            Map<Class<?>, Function<?, ?>> valueResolvers = VALUE_FUNCTIONS.get(identifier);
            if (valueResolvers != null) {
               valueResolvers.remove(type);
               if (valueResolvers.isEmpty()) {
                  VALUE_FUNCTIONS.remove(identifier);
               }
            }
         }
      }
   }

   public static synchronized <T, R> void registerValue(Key identifier, Class<T> type, Function<T, R> resolver) {
      Key checkedIdentifier = Objects.requireNonNull(identifier, "identifier");
      Class<T> checkedType = Objects.requireNonNull(type, "type");
      Function<T, R> checkedResolver = Objects.requireNonNull(resolver, "resolver");
      VALUE_FUNCTIONS.computeIfAbsent(checkedIdentifier, key -> new LinkedHashMap<>()).put(checkedType, checkedResolver);
   }

   public static synchronized <T> T getValue(Object value, Key identifier, Class<T> expectedType) {
      if (value != null && identifier != null && expectedType != null) {
         Map<Class<?>, Function<?, ?>> resolvers = VALUE_FUNCTIONS.get(identifier);
         if (resolvers == null) {
            return null;
         }

         Function<Object, ?> resolver = (Function<Object, ?>)resolvers.get(value.getClass());
         if (resolver == null) {
            for (Entry<Class<?>, Function<?, ?>> entry : resolvers.entrySet()) {
               if (entry.getKey().isInstance(value)) {
                  resolver = (Function<Object, ?>)entry.getValue();
                  break;
               }
            }
         }

         if (resolver == null) {
            return null;
         }

         try {
            Object resolved = resolver.apply(value);
            return resolved != null && expectedType.isInstance(resolved) ? expectedType.cast(resolved) : null;
         } catch (Exception ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static synchronized boolean isEnabled(Key identifier) {
      Supplier<Boolean> supplier = BOOLEAN_SUPPLIERS.get(identifier);
      if (supplier == null) {
         return false;
      }

      try {
         return Boolean.TRUE.equals(supplier.get());
      } catch (Exception ignored) {
         return false;
      }
   }

   public static synchronized <T> boolean isEnabled(T value, Key identifier) {
      if (value != null && identifier != null) {
         Map<Class<?>, Function<?, Boolean>> resolvers = BOOLEAN_FUNCTIONS.get(identifier);
         if (resolvers == null) {
            return false;
         }

         Function<T, Boolean> resolver = (Function<T, Boolean>)resolvers.get(value.getClass());
         if (resolver == null) {
            for (Entry<Class<?>, Function<?, Boolean>> entry : resolvers.entrySet()) {
               if (entry.getKey().isInstance(value)) {
                  resolver = (Function<T, Boolean>)entry.getValue();
                  break;
               }
            }
         }

         if (resolver == null) {
            return false;
         }

         try {
            return Boolean.TRUE.equals(resolver.apply(value));
         } catch (Exception ignored) {
            return false;
         }
      } else {
         return false;
      }
   }
}
