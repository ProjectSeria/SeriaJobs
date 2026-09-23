package id.seria.gacha.internal;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import id.seria.gacha.PhoenixCrates;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class StringKeyStore {
   private static PhoenixCrates plugin;
   private static File file;
   private static final Gson gson = new Gson();
   private static Set<String> keys;
   private static boolean loaded = false;

   public StringKeyStore() {
   }

   public static void initialize(PhoenixCrates pluginInstance) {
      plugin = pluginInstance;
      file = new File(plugin.getDataFolder(), "internal");
      keys = new HashSet<>();
   }

   private static void lazyLoad() {
      if (!loaded) {
         if (file.exists()) {
            try {
               String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
               keys = (Set<String>)gson.fromJson(content, (new TypeToken<Set<String>>() {}).getType());
            } catch (IOException e) {
               plugin.getLogger().error("Could not load keys from file: " + e.getMessage());
               keys = new HashSet<>();
            }
         } else {
            keys = new HashSet<>();
         }

         loaded = true;
      }
   }

   public static boolean contains(String key) {
      lazyLoad();
      return keys.contains(key);
   }

   public static void add(String key) {
      lazyLoad();
      if (keys.add(key)) {
         save();
      }
   }

   public static void remove(String key) {
      lazyLoad();
      if (keys.remove(key)) {
         save();
      }
   }

   private static void save() {
      try {
         if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
         }

         String content = gson.toJson(keys);
         java.nio.file.Files.write(file.toPath(), content.getBytes());
      } catch (IOException e) {
         plugin.getLogger().error("Could not save keys to file: " + e.getMessage());
      }
   }
}
