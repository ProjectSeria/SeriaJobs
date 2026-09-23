package id.seria.gacha.lib.common.utils.configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Supplier;

public abstract class Configuration {
   private static final Map<Class<? extends Configuration>, Map<String, Supplier<? extends Configuration>>> REGISTERED_MODULES = new LinkedHashMap<>();
   private final Map<String, Configuration> loadedModules = new LinkedHashMap<>();

   protected Configuration() {
      this.reloadModules();
   }

   private static String normalizeModuleKey(String key) {
      if (key == null) {
         throw new IllegalArgumentException("module key cannot be null");
      } else {
         String normalized = key.trim().toLowerCase();
         if (normalized.isEmpty()) {
            throw new IllegalArgumentException("module key cannot be empty");
         } else {
            return normalized;
         }
      }
   }

   private static Class<? extends Configuration> resolveCallerConfigurationClass() {
      StackTraceElement[] trace = Thread.currentThread().getStackTrace();

      for (StackTraceElement element : trace) {
         try {
            Class<?> clazz = Class.forName(element.getClassName());
            if (clazz != Configuration.class && Configuration.class.isAssignableFrom(clazz)) {
               return (Class<? extends Configuration>)clazz;
            }
         } catch (ClassNotFoundException var6) {
         }
      }

      throw new IllegalStateException("Unable to resolve caller configuration class");
   }

   public static synchronized <M extends Configuration> void registerModule(String key, Supplier<M> supplier) {
      registerModule(resolveCallerConfigurationClass(), key, supplier);
   }

   public static synchronized <M extends Configuration> void registerExtension(String key, Supplier<M> supplier) {
      registerModule(resolveCallerConfigurationClass(), key, supplier);
   }

   public static synchronized <O extends Configuration, M extends Configuration> void registerModule(Class<O> ownerClass, String key, Supplier<M> supplier) {
      Objects.requireNonNull(ownerClass, "ownerClass cannot be null");
      Objects.requireNonNull(supplier, "supplier cannot be null");
      REGISTERED_MODULES.computeIfAbsent(ownerClass, ignored -> new LinkedHashMap<>()).put(normalizeModuleKey(key), supplier);
   }

   public static synchronized <O extends Configuration> boolean unregisterModule(Class<O> ownerClass, String key) {
      Objects.requireNonNull(ownerClass, "ownerClass cannot be null");
      Map<String, Supplier<? extends Configuration>> modules = REGISTERED_MODULES.get(ownerClass);
      return modules == null ? false : modules.remove(normalizeModuleKey(key)) != null;
   }

   public synchronized void reloadModules() {
      this.loadedModules.clear();
      synchronized (Configuration.class) {
         for (Class<?> target = this.getClass(); target != null && Configuration.class.isAssignableFrom(target); target = target.getSuperclass()) {
            Class<? extends Configuration> configClass = (Class<? extends Configuration>)target;
            Map<String, Supplier<? extends Configuration>> modules = REGISTERED_MODULES.get(configClass);
            if (modules != null) {
               for (Entry<String, Supplier<? extends Configuration>> entry : modules.entrySet()) {
                  Configuration module = Objects.requireNonNull(entry.getValue().get(), "module supplier returned null for key \"" + entry.getKey() + "\"");
                  this.loadedModules.put(entry.getKey(), module);
               }
            }
         }
      }
   }

   public synchronized List<Configuration> getRegisteredModules() {
      return new ArrayList<>(this.loadedModules.values());
   }

   public synchronized Optional<Configuration> getModule(String key) {
      return Optional.ofNullable(this.loadedModules.get(normalizeModuleKey(key)));
   }

   public synchronized <M extends Configuration> Optional<M> getModule(String key, Class<M> moduleClass) {
      Objects.requireNonNull(moduleClass, "moduleClass cannot be null");
      return this.getModule(key).filter(moduleClass::isInstance).map(moduleClass::cast);
   }

   public synchronized <M extends Configuration> M getModule(Class<M> moduleClass) {
      Objects.requireNonNull(moduleClass, "moduleClass cannot be null");

      for (Configuration module : this.loadedModules.values()) {
         if (moduleClass.isInstance(module)) {
            return moduleClass.cast(module);
         }
      }

      throw new IllegalStateException("No module registered for class: " + moduleClass.getName());
   }

   public synchronized boolean hasModule(String key) {
      return this.loadedModules.containsKey(normalizeModuleKey(key));
   }

   protected final synchronized void copyLoadedModulesTo(Configuration target) {
      Objects.requireNonNull(target, "target cannot be null");
      synchronized (target) {
         for (Entry<String, Configuration> entry : this.loadedModules.entrySet()) {
            String key = entry.getKey();
            Configuration module = entry.getValue();
            if (module != null && target.loadedModules.containsKey(key)) {
               target.loadedModules.put(key, module.clone());
            }
         }
      }
   }

   public abstract Configuration clone();

   public void onLoad() {
   }
}
