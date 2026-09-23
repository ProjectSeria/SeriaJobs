package id.seria.gacha.managers;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.workers.AsyncQueueMultiWorker;
import id.seria.gacha.lib.common.utils.configuration.workers.ConfigurationWorker;
import id.seria.gacha.lib.common.utils.utility.FileUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.limbo.item.adapter.BaseItemAdapter;
import id.seria.gacha.limbo.item.storage.ItemStorageScope;
import id.seria.gacha.managers.keys.CachedKey;
import id.seria.gacha.managers.keys.Key;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class KeysManager implements id.seria.gacha.api.managers.KeysManager {
   private final PhoenixCrates plugin;
   private final Set<CachedKey> keys = new LinkedHashSet<>();
   private final AsyncQueueMultiWorker filesWorker;

   public KeysManager(PhoenixCrates plugin) {
      this.plugin = plugin;
      this.filesWorker = new AsyncQueueMultiWorker(plugin);
   }

   public void load() throws Exception {
      this.filesWorker.open();
      this.loadKeysFromFolder();
   }

   public void reload() throws Exception {
      this.unload();
      this.filesWorker.open();
      this.loadKeysFromFolder();
   }

   public void unload() throws Exception {
      this.filesWorker.close();
      this.keys.clear();
   }

   @NotNull
   @Override
   public List<Key> getRegisteredKeys() {
      List<Key> registeredKeys = new ArrayList<>(this.keys.size());

      for (CachedKey cachedKey : this.keys) {
         registeredKeys.add(cachedKey.getKey());
      }

      return registeredKeys;
   }

   @Override
   public int countRegisteredKeys() {
      return this.keys.size();
   }

   @NotNull
   @Override
   public Set<String> getKeysIdentifier() {
      return this.keys.stream().map(c -> c.getKey().getIdentifier()).collect(Collectors.toSet());
   }

   public Key getKeyByIdentifier(String key) {
      return this.keys.stream().map(CachedKey::getKey).filter(t -> t.getIdentifier().equalsIgnoreCase(key)).findFirst().orElse(null);
   }

   public void saveKey(Key type) {
      this.filesWorker.queueSave(this.getCachedKeyOrNull(type.getIdentifier()).getConfigurationWorker());
   }

   public CachedKey getCachedKeyOrNull(String identifier) {
      synchronized (this.keys) {
         return this.keys.stream().filter(c -> c.getKey().getIdentifier().equalsIgnoreCase(identifier)).findFirst().orElse(null);
      }
   }

   public Key getKeyByStorageScopeId(String scopeId) {
      if (scopeId != null && !scopeId.trim().isEmpty()) {
         synchronized (this.keys) {
            return this.keys
               .stream()
               .map(CachedKey::getKey)
               .filter(key -> key instanceof ItemStorageScope)
               .filter(key -> scopeId.equalsIgnoreCase(key.getStorageScopeId()))
               .findFirst()
               .orElse(null);
         }
      } else {
         return null;
      }
   }

   public void registerKey(Key key, boolean createFile) throws Exception {
      if (this.getCachedKeyOrNull(key.getIdentifier()) == null) {
         this.keys.add(this.createCache(key, key.getIdentifier()));
         if (createFile) {
            this.saveKey(key);
         }

         key.markRegistered();
      }
   }

   @Deprecated
   private CachedKey createCache(Key key, String fileName) throws ErrorException {
      ConfigurationWorker configurationWorker = new ConfigurationWorker(this.plugin, key, null, "keys/" + fileName + ".yml");
      configurationWorker.registerAdapter(new BaseItemAdapter());
      return new CachedKey(configurationWorker, key);
   }

   public void unregisterCrateKey(Key key, boolean eraseFile) {
      CachedKey cache = this.getCachedKeyOrNull(key.getIdentifier());
      if (cache != null) {
         key.unmarkRegistered();
         this.keys.remove(cache);
         if (eraseFile) {
            cache.getConfigurationWorker().getConfigurationFile().delete();
         }
      }
   }

   private synchronized void loadKeysFromFolder() throws Exception {
      this.keys.clear();
      File keysFolder = new File(this.plugin.getDataFolder(), "keys");
      if (keysFolder.exists() && keysFolder.listFiles().length != 0) {
         boolean limited = this.plugin.getEdition() == Edition.LITE;
         int loaded = 0;

         for (File file : Objects.requireNonNull(keysFolder.listFiles())) {
            if (file.isFile() && FileUtil.getExtensionWithoutName(file.getName()).equalsIgnoreCase("yml")) {
               String name = FileUtil.getNameWithoutExtension(file.getName());
               if (limited) {
                  if (++loaded > 5) {
                     Bukkit.getConsoleSender()
                        .sendMessage(ChatColor.RED + "Key \"" + name + "\" was deleted. Maximum amount (5) allowed exceeded for the lite version.");
                     file.delete();
                     continue;
                  }
               }

               Key key = new Key();
               CachedKey cachedKey = this.createCache(key, name);
               cachedKey.getConfigurationWorker().load();
               key.markRegistered();
               this.keys.add(cachedKey);
            }
         }
      } else {
         keysFolder.mkdirs();
         this.registerKey(
            new Key(
               true,
               "example1",
               false,
               true,
               BaseItem.from(XMaterial.TRIPWIRE_HOOK)
                  .setCustomName("&aExample #1 Crate Key")
                  .setCustomLore("&7This is a lore line", "", "&eUse this key in &6%crate%&e!")
            ),
            true
         );
         this.registerKey(
            new Key(
               true,
               "example2",
               false,
               true,
               BaseItem.from(XMaterial.TRIPWIRE_HOOK)
                  .setCustomName("&aExample #2 Crate Key")
                  .setCustomLore("&7This is a lore line", "", "&eUse this key in &6%crate%&e!")
            ),
            true
         );
         this.registerKey(
            new Key(
               true,
               "example3",
               false,
               true,
               BaseItem.from(XMaterial.TRIPWIRE_HOOK)
                  .setCustomName("&aExample #3 Crate Key")
                  .setCustomLore("&7This is a lore line", "", "&eUse this key in &6%crate%&e!")
            ),
            true
         );
      }
   }

   @Generated
   public PhoenixCrates getPlugin() {
      return this.plugin;
   }

   @Generated
   public AsyncQueueMultiWorker getFilesWorker() {
      return this.filesWorker;
   }

   @Generated
   private Set<CachedKey> getKeys() {
      return this.keys;
   }
}
