package id.seria.gacha.sdk.platforms.server.registries;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import id.seria.gacha.api.sdk.plugin.addons.Addon;
import id.seria.gacha.api.sdk.plugin.addons.AddonDescription;
import id.seria.gacha.api.sdk.plugin.addons.AddonRegistry;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import lombok.Generated;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

public class ServerAddonRegistry implements AddonRegistry {
   private final ServerPlugin plugin;
   private final Map<Class<? extends Addon>, ServerAddonRegistry.AddonRegistration> loadedAddons = new ConcurrentHashMap<>();
   private final Map<String, Addon> addonsLookupMap = new HashMap<>();
   private static final String DEFAULT_ADDON_COLOR = "#AAAAAA";

   public ServerAddonRegistry(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   private static String nameColorOf(ServerAddonRegistry.AddonDescriptionFile description) {
      String hex = description.getColor();

      try {
         return ChatColor.of(hex != null && !hex.isEmpty() ? hex : "#AAAAAA").toString();
      } catch (IllegalArgumentException ex) {
         return ChatColor.of("#AAAAAA").toString();
      }
   }

   @Override
   public boolean initAddon(File file) {
      try {
         Validate.isTrue(file.getName().endsWith("jar"), "File is not a jar");
         ServerAddonRegistry.AddonDescriptionFile description = this.getAddonDescription(file);
         Bukkit.getConsoleSender().sendMessage("Loading addon " + nameColorOf(description) + description.getName() + ChatColor.RESET);
         ServerAddonRegistry.AddonClassLoader classLoader = new ServerAddonRegistry.AddonClassLoader(file, description, this.plugin.getClass().getClassLoader());
         Addon addon = classLoader.getAddon();
         if (addon == null) {
            classLoader.close();
            throw new IllegalStateException("Failed to initiate main class");
         }

         String requiredVersion = addon.getRequiredPluginVersion();
         if (requiredVersion != null && compareVersion(requiredVersion, this.plugin.getDescription().getVersion()) > 0) {
            classLoader.close();
            throw new IllegalStateException("This addon requires " + this.plugin.getName() + " v" + requiredVersion + " or higher!");
         }

         Class<?> classToLoad = Class.forName(description.getClassLoaderOf(), true, classLoader);
         if (classToLoad.isAssignableFrom(Addon.class)) {
            classLoader.close();
            throw new IllegalStateException("Specified class loader dont contain an addon");
         }

         addon.unsafe().initialize(this, this.plugin, file, description);
         addon.init();
         ServerAddonRegistry.AddonRegistration registration = new ServerAddonRegistry.AddonRegistration(addon, classLoader);
         synchronized (this.loadedAddons) {
            this.loadedAddons.put((Class<? extends Addon>)addon.getClass(), registration);
         }

         synchronized (this.addonsLookupMap) {
            this.addonsLookupMap.put(description.getName(), addon);
         }

         return true;
      } catch (Exception e) {
         this.plugin.getLogger().error("Could not load addon " + file.getName() + ": " + e.getMessage(), e);
         return false;
      }
   }

   private ServerAddonRegistry.AddonDescriptionFile getAddonDescription(File file) throws IOException {
      JarFile jar = new JarFile(file);

      ServerAddonRegistry.AddonDescriptionFile var24;
      try {
         JarEntry entry = jar.getJarEntry("addon.yml");
         if (entry == null) {
            throw new RuntimeException("No addon.yml found");
         }

         InputStreamReader reader = new InputStreamReader(jar.getInputStream(entry));

         try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(reader);
            ServerAddonRegistry.AddonDescriptionFile description = new ServerAddonRegistry.AddonDescriptionFile();
            Object mutable = null;
            mutable = yaml.get("name");
            Validate.notNull(mutable, "Name can not be null");
            description.setName(String.valueOf(mutable).replace(" ", "_"));
            description.setDisplayName(String.valueOf(mutable));
            mutable = yaml.get("name");
            Validate.notNull(mutable, "Plugin can not be null");
            description.setPlugin(String.valueOf(mutable).replace(" ", "_"));
            Validate.isTrue(Bukkit.getPluginManager().isPluginEnabled(this.plugin.getOriginal()), this.plugin + " is not loaded or enabled");
            mutable = yaml.get("main");
            Validate.notNull(mutable, "can not be null");
            Validate.isTrue(mutable instanceof String, "can not have space");
            description.setClassLoaderOf(String.valueOf(mutable).replace(" ", ""));
            mutable = yaml.get("version");
            Validate.notNull(mutable, "can not be null");
            description.setVersion(String.valueOf(mutable));
            mutable = yaml.get("depend");
            if (mutable != null) {
               if (mutable instanceof List) {
                  List<String> dependList = ((List<?>)mutable).stream().map(String::valueOf).collect(Collectors.toList());
                  description.setDepend(Collections.unmodifiableList(dependList));
               } else {
                  description.setDepend(ImmutableList.of((String)mutable));
               }
            }

            mutable = yaml.get("soft-depend");
            if (mutable != null) {
               if (mutable instanceof List) {
                  List<String> dependList = ((List<?>)mutable).stream().map(String::valueOf).collect(Collectors.toList());
                  description.setDepend(Collections.unmodifiableList(dependList));
               } else {
                  description.setDepend(ImmutableList.of((String)mutable));
               }
            }

            mutable = yaml.get("description");
            if (mutable != null) {
               description.setDescription(String.valueOf(mutable));
            }

            mutable = yaml.get("authors");
            if (mutable != null) {
               if (mutable instanceof List) {
                  List<String> dependList = ((List<?>)mutable).stream().map(String::valueOf).collect(Collectors.toList());
                  description.setDepend(Collections.unmodifiableList(dependList));
               } else {
                  description.setDepend(ImmutableList.of((String)mutable));
               }
            }

            mutable = yaml.get("color");
            if (mutable != null) {
               description.setColor(String.valueOf(mutable));
            }

            var24 = description;
         } catch (Throwable var11) {
            try {
               reader.close();
            } catch (Throwable var10) {
               var11.addSuppressed(var10);
            }

            throw var11;
         }

         reader.close();
      } catch (Throwable var12) {
         try {
            jar.close();
         } catch (Throwable var9) {
            var12.addSuppressed(var9);
         }

         throw var12;
      }

      jar.close();
      return var24;
   }

   public void initializeAddons() {
      File folder = new File(this.plugin.getDataFolder(), "addons");
      if (!folder.exists()) {
         folder.mkdirs();
      }

      if (folder.isDirectory()) {
         File[] files = folder.listFiles((dir, name) -> name.endsWith(".jar"));
         if (files != null) {
            for (File file : files) {
               this.initAddon(file);
            }
         }
      }
   }

   public void loadAddons() {
      for (ServerAddonRegistry.AddonRegistration registration : this.loadedAddons.values()) {
         this.loadAddon(registration.addon);
      }
   }

   public void disableAddons() {
      for (ServerAddonRegistry.AddonRegistration registration : this.loadedAddons.values()) {
         this.unloadAddon(registration.addon);
      }
   }

   public void unloadAddons() throws IOException {
      for (ServerAddonRegistry.AddonRegistration registration : this.loadedAddons.values()) {
         this.unregisterAddon(registration.addon);
      }
   }

   @Override
   public void loadAddon(Addon addon) {
      if (!addon.isEnabled()) {
         Bukkit.getConsoleSender()
            .sendMessage(
               "Enabling addon "
                  + nameColorOf((ServerAddonRegistry.AddonDescriptionFile)addon.getDescription())
                  + addon.getDescription().getName()
                  + ChatColor.RESET
            );

         try {
            addon.load();
            addon.unsafe().setEnabled(true);
         } catch (Exception e) {
            e.printStackTrace();
            this.unloadAddon(addon);
         }
      }
   }

   @Override
   public void unloadAddon(Addon addon) {
      if (addon.isEnabled()) {
         try {
            addon.unload();
         } catch (Exception e) {
            e.printStackTrace();
         } finally {
            addon.unsafe().setEnabled(false);
         }
      }
   }

   @Override
   public void unregisterAddon(Addon addon) throws IOException {
      if (addon.isEnabled()) {
         this.unloadAddon(addon);
      }

      ServerAddonRegistry.AddonRegistration registration = this.loadedAddons.remove(addon.getClass());
      this.loadedAddons.remove(addon.getClass());
      if (registration != null) {
         registration.classLoader.close();
      }
   }

   @Override
   public <T extends Addon> T getAddon(Class<T> addonClazz) {
      ServerAddonRegistry.AddonRegistration registration = this.loadedAddons.get(addonClazz);
      return (T)(registration == null ? null : registration.addon);
   }

   @Override
   public Addon getAddon(String name) {
      return this.addonsLookupMap.get(name);
   }

   @Override
   public boolean isAddonEnabled(String name) {
      return this.getAddon(name) != null;
   }

   private static int compareVersion(String firstVersion, String secondVersion) {
      if (firstVersion != null && secondVersion != null) {
         if (firstVersion.equalsIgnoreCase("${project.version}")) {
            firstVersion = "1.0.0";
         }

         if (secondVersion.equalsIgnoreCase("${project.version}")) {
            secondVersion = "1.0.0";
         }

         firstVersion = firstVersion.split("[^0-9.]")[0];
         if (!firstVersion.matches("[0-9]+(\\.[0-9]+)*")) {
            throw new IllegalArgumentException("Invalid version format: " + firstVersion);
         }

         secondVersion = secondVersion.split("[^0-9.]")[0];
         if (!secondVersion.matches("[0-9]+(\\.[0-9]+)*")) {
            throw new IllegalArgumentException("Invalid version format: " + firstVersion);
         }

         String[] firstParts = firstVersion.split("\\.");
         String[] secondParts = secondVersion.split("\\.");
         int length = Math.max(firstParts.length, secondParts.length);

         for (int i = 0; i < length; i++) {
            int firstPart = i < firstParts.length ? Integer.parseInt(firstParts[i]) : 0;
            int secondPart = i < secondParts.length ? Integer.parseInt(secondParts[i]) : 0;
            if (firstPart < secondPart) {
               return -1;
            }

            if (firstPart > secondPart) {
               return 1;
            }
         }

         return 0;
      } else {
         throw new IllegalArgumentException("Version cannot be null");
      }
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public Map<Class<? extends Addon>, ServerAddonRegistry.AddonRegistration> getLoadedAddons() {
      return this.loadedAddons;
   }

   @Generated
   public Map<String, Addon> getAddonsLookupMap() {
      return this.addonsLookupMap;
   }

   public class AddonClassLoader extends URLClassLoader {
      private final JarFile jar;
      private final Manifest manifest;
      private final URL url;
      private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
      private final Addon addon;

      public AddonClassLoader(final File file, final ServerAddonRegistry.AddonDescriptionFile description, final ClassLoader parent) throws Exception {
         super(new URL[]{file.toURI().toURL()}, parent);
         this.jar = new JarFile(file);
         this.manifest = this.jar.getManifest();
         this.url = file.toURI().toURL();
         Class<?> mainClass = Class.forName(description.getClassLoaderOf(), true, this);
         if (!Addon.class.isAssignableFrom(mainClass)) {
            throw new RuntimeException(mainClass.getName() + " does not extend Addon");
         }

         this.addon = mainClass.asSubclass(Addon.class).newInstance();
         this.addon.unsafe().setClassLoader(this);
      }

      @Override
      protected Class<?> findClass(String name) throws ClassNotFoundException {
         Class<?> result = this.classes.get(name);
         if (result == null) {
            String path = name.replace('.', '/').concat(".class");
            JarEntry entry = this.jar.getJarEntry(path);
            if (entry != null) {
               byte[] classBytes;
               try {
                  InputStream inputStream = this.jar.getInputStream(entry);

                  try {
                     classBytes = ByteStreams.toByteArray(inputStream);
                  } catch (Throwable var10) {
                     if (inputStream != null) {
                        try {
                           inputStream.close();
                        } catch (Throwable var9) {
                           var10.addSuppressed(var9);
                        }
                     }

                     throw var10;
                  }

                  if (inputStream != null) {
                     inputStream.close();
                  }
               } catch (IOException ex) {
                  throw new ClassNotFoundException(name, ex);
               }

               int dot = name.lastIndexOf(46);
               if (dot != -1) {
                  String pkgName = name.substring(0, dot);
                  if (this.getPackage(pkgName) == null) {
                     try {
                        if (this.manifest != null) {
                           this.definePackage(pkgName, this.manifest, this.url);
                        } else {
                           this.definePackage(pkgName, null, null, null, null, null, null, null);
                        }
                     } catch (IllegalArgumentException ex) {
                        if (this.getPackage(pkgName) == null) {
                           throw new IllegalStateException("Cannot find package " + pkgName);
                        }
                     }
                  }
               }

               CodeSigner[] signers = entry.getCodeSigners();
               CodeSource source = new CodeSource(this.url, signers);
               result = this.defineClass(name, classBytes, 0, classBytes.length, source);
               if (result == null) {
                  result = super.findClass(name);
               }

               this.classes.put(name, result);
            }
         }

         return result;
      }

      @Override
      public URL getResource(String name) {
         return this.findResource(name);
      }

      @Override
      public Enumeration<URL> getResources(String name) throws IOException {
         return this.findResources(name);
      }

      @Override
      public void close() throws IOException {
         try {
            super.close();
         } finally {
            this.jar.close();
         }
      }

      @Generated
      public Addon getAddon() {
         return this.addon;
      }
   }

   public class AddonDescriptionFile implements AddonDescription {
      private String name;
      private String displayName;
      private String plugin;
      private String classLoaderOf;
      private List<String> depend = ImmutableList.of();
      private List<String> softDepend = ImmutableList.of();
      private String version;
      private String description;
      private List<String> authors = ImmutableList.of();
      private String color;

      @Generated
      @Override
      public String getName() {
         return this.name;
      }

      @Generated
      @Override
      public String getDisplayName() {
         return this.displayName;
      }

      @Generated
      @Override
      public String getPlugin() {
         return this.plugin;
      }

      @Generated
      @Override
      public String getClassLoaderOf() {
         return this.classLoaderOf;
      }

      @Generated
      @Override
      public List<String> getDepend() {
         return this.depend;
      }

      @Generated
      @Override
      public List<String> getSoftDepend() {
         return this.softDepend;
      }

      @Generated
      @Override
      public String getVersion() {
         return this.version;
      }

      @Generated
      @Override
      public String getDescription() {
         return this.description;
      }

      @Generated
      @Override
      public List<String> getAuthors() {
         return this.authors;
      }

      @Generated
      public String getColor() {
         return this.color;
      }

      @Generated
      public void setName(String name) {
         this.name = name;
      }

      @Generated
      public void setDisplayName(String displayName) {
         this.displayName = displayName;
      }

      @Generated
      public void setPlugin(String plugin) {
         this.plugin = plugin;
      }

      @Generated
      public void setClassLoaderOf(String classLoaderOf) {
         this.classLoaderOf = classLoaderOf;
      }

      @Generated
      public void setDepend(List<String> depend) {
         this.depend = depend;
      }

      @Generated
      public void setSoftDepend(List<String> softDepend) {
         this.softDepend = softDepend;
      }

      @Generated
      public void setVersion(String version) {
         this.version = version;
      }

      @Generated
      public void setDescription(String description) {
         this.description = description;
      }

      @Generated
      public void setAuthors(List<String> authors) {
         this.authors = authors;
      }

      @Generated
      public void setColor(String color) {
         this.color = color;
      }

      @Generated
      public AddonDescriptionFile() {
      }
   }

   public static class AddonRegistration {
      private final Addon addon;
      private final ServerAddonRegistry.AddonClassLoader classLoader;

      @Generated
      public Addon getAddon() {
         return this.addon;
      }

      @Generated
      public ServerAddonRegistry.AddonClassLoader getClassLoader() {
         return this.classLoader;
      }

      @Generated
      public AddonRegistration(Addon addon, ServerAddonRegistry.AddonClassLoader classLoader) {
         this.addon = addon;
         this.classLoader = classLoader;
      }
   }
}
