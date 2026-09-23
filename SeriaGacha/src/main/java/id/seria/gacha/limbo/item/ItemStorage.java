package id.seria.gacha.limbo.item;

import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.workers.AsyncQueueConfigurationWorker;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.limbo.item.storage.ItemStorageScope;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.sdk.platforms.server.hooks.NamespacedItemHook;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class ItemStorage {
   private ItemStorage.StorageConfiguration configuration;
   private AsyncQueueConfigurationWorker configurationWorker;
   private final List<ItemStorageScope> additionalScopes = new ArrayList<>();
   private static final String[] HOOK_PRIORITY = new String[]{"nexo", "itemsadder", "craftengine", "mythicitem", "mmoitem", "hdb", "executableitem"};

   public ItemStorage(PhoenixCrates plugin) {
      this.configuration = new ItemStorage.StorageConfiguration();
      this.configurationWorker = new AsyncQueueConfigurationWorker(plugin, this.configuration, null, "internal_storage.yml");
   }

   public ItemStack getLegacyItemBy(String id) {
      return this.getLegacyItems().get(id);
   }

   public void registerScope(ItemStorageScope scope) {
      this.additionalScopes.add(scope);
   }

   public void unregisterScope(ItemStorageScope scope) {
      this.additionalScopes.remove(scope);
   }

   public ItemStack getItemByReference(String reference) {
      if (reference != null && !reference.trim().isEmpty()) {
         ItemStorage.Reference parsedReference = this.parseScopedReference(reference);
         if (parsedReference != null) {
            ItemStack crateScopedItem = this.findItemInCrateScope(parsedReference.scopeId, parsedReference.itemId);
            if (crateScopedItem != null) {
               return crateScopedItem;
            }

            ItemStack keyScopedItem = this.findItemInKeyScope(parsedReference.scopeId, parsedReference.itemId);
            if (keyScopedItem != null) {
               return keyScopedItem;
            }

            ItemStack additionalScopedItem = this.findItemInAdditionalScopes(parsedReference.scopeId, parsedReference.itemId);
            return additionalScopedItem != null ? additionalScopedItem : this.getLegacyItemBy(parsedReference.itemId);
         } else {
            return this.getLegacyItemBy(reference);
         }
      } else {
         return null;
      }
   }

   public void storeItem(CustomItem item, ItemStorageScope storage) {
      Validate.notNull(storage, "storage cannot be null", new Object[0]);
      Validate.isTrue(item.isItemStack(), "Item already contains storage id", new Object[0]);
      ItemStack stack = CustomItem.unsafe().getItemStack(item);
      String baseIdentifier = null;
      PhoenixCrates plugin = PhoenixCrates.getLegacyInstance();

      for (String namespace : HOOK_PRIORITY) {
         NamespacedItemHook hook = plugin.getHookRegistry().getNamespacedHook(namespace);
         if (hook != null) {
            String id = hook.getIdFromItem(stack);
            if (id != null) {
               baseIdentifier = namespace + ":" + id;
               break;
            }
         }
      }

      if (baseIdentifier == null) {
         baseIdentifier = ItemBuilder.of(stack).getDisplayName();
      }

      baseIdentifier = baseIdentifier.replace(" ", "_");
      baseIdentifier = ChatColor.stripColor(baseIdentifier);
      baseIdentifier = baseIdentifier.replaceAll("[^a-zA-Z0-9_:]", "");
      baseIdentifier = baseIdentifier.toLowerCase();
      if (this.isInvalidIdentifier(baseIdentifier)) {
         baseIdentifier = stack.getType().name().toLowerCase();
      }

      String reference = this.storeClone(storage, baseIdentifier, stack);
      CustomItem.unsafe().setStorageId(item, reference);
   }

   public boolean containsLegacyId(String internalId) {
      return this.configuration.legacyItems.containsKey(internalId);
   }

   public void load() throws Exception {
      this.configurationWorker.open();
      this.configurationWorker.validate();
   }

   public void reload() throws Exception {
      this.configurationWorker.open();
      this.configurationWorker.validate();
   }

   public void unload() {
      if (this.configurationWorker != null) {
         this.configurationWorker.close();
      }
   }

   public Map<String, ItemStack> getLegacyItems() {
      return Collections.unmodifiableMap(this.configuration.legacyItems);
   }

   private ItemStack findItemInCrateScope(String scopeId, String itemId) {
      CrateType crateType = Crates.getCratesManager().getTypeByStorageScopeId(scopeId);
      return this.getScopedItem(crateType, itemId);
   }

   private ItemStack findItemInAdditionalScopes(String scopeId, String itemId) {
      for (ItemStorageScope scope : this.additionalScopes) {
         if (scope.getStorageScopeId() != null && scope.getStorageScopeId().equals(scopeId)) {
            return this.getScopedItem(scope, itemId);
         }
      }

      return null;
   }

   private ItemStack findItemInKeyScope(String scopeId, String itemId) {
      Key key = Crates.getKeysManager().getKeyByStorageScopeId(scopeId);
      return this.getScopedItem(key, itemId);
   }

   private ItemStack getScopedItem(ItemStorageScope scope, String itemId) {
      return scope == null ? null : scope.getInternalStorageItems().get(itemId);
   }

   private String storeClone(ItemStorageScope storage, String baseIdentifier, ItemStack sourceStack) {
      Map<String, ItemStack> scopedItems = storage.getInternalStorageItems();
      String identifier = baseIdentifier;

      for (int counter = 1; scopedItems.containsKey(identifier); counter++) {
         ItemStack cached = scopedItems.get(identifier);
         if (cached != null && cached.isSimilar(sourceStack)) {
            return storage.getStorageScopeId() + ":" + identifier;
         }

         identifier = baseIdentifier + counter;
      }

      ItemStack clone = sourceStack.clone();
      clone.setAmount(1);
      scopedItems.put(identifier, clone);
      return storage.getStorageScopeId() + ":" + identifier;
   }

   private boolean isInvalidIdentifier(String identifier) {
      if (identifier != null && !identifier.isEmpty()) {
         String normalized = identifier.replace("_", "").replace(":", "");
         return normalized.isEmpty();
      } else {
         return true;
      }
   }

   private ItemStorage.Reference parseScopedReference(String reference) {
      int index = reference.indexOf(58);
      if (index > 0 && index < reference.length() - 1) {
         String scope = reference.substring(0, index).trim();
         String itemId = reference.substring(index + 1);
         return !scope.isEmpty() && !itemId.isEmpty() ? new ItemStorage.Reference(scope, itemId) : null;
      } else {
         return null;
      }
   }

   private static class Reference {
      private final String scopeId;
      private final String itemId;

      private Reference(String scopeId, String itemId) {
         this.scopeId = scopeId;
         this.itemId = itemId;
      }
   }

   public class StorageConfiguration extends Configuration {
      @ConfigField(path = "items")
      private Map<String, ItemStack> legacyItems = new LinkedHashMap<>();

      public StorageConfiguration() {
      }

      public ItemStorage.StorageConfiguration clone() {
         ItemStorage.StorageConfiguration clone = ItemStorage.this.new StorageConfiguration();

         for (Entry<String, ItemStack> entry : this.legacyItems.entrySet()) {
            clone.legacyItems.put(entry.getKey(), entry.getValue().clone());
         }

         return clone;
      }
   }
}
