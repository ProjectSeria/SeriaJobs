package id.seria.gacha.managers.rarities;

import id.seria.gacha.internal.Placeholders;
import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.annotations.LoadFirst;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.limbo.item.storage.ItemStorageScope;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import lombok.Generated;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class RarityType extends Configuration implements ItemStorageScope {
   private boolean registered = false;
   @ConfigField
   private boolean enabled = true;
   @ConfigField
   private String identifier = "random_identifier_" + System.currentTimeMillis();
   @ConfigField
   private String displayName = "Rarity Example";
   @ConfigField
   private double weight = 100.0;
   @ConfigField(path = "required-keys")
   private int requiredKeys = 1;
   @ConfigField(path = "group-rewards")
   private boolean groupRewards = false;
   @ConfigField(path = "display-item")
   private BaseItem displayItem = null;
   @ConfigField(path = "preview-menu")
   private String previewMenu = null;
   @LoadFirst
   @ConfigField(path = "internal-storage.scope-id")
   private String storageScopeId = UUID.randomUUID().toString().replace("-", "");
   @ConfigField(path = "internal-storage.items")
   private Map<String, ItemStack> internalStorageItems = new LinkedHashMap<>();

   public RarityType() {
   }

   public RarityType(boolean registered, boolean enabled, String identifier, String displayName, double weight) {
      this(registered, enabled, identifier, displayName, weight, 1);
   }

   public RarityType(boolean registered, boolean enabled, String identifier, String displayName, double weight, int requiredKeys) {
      this(registered, enabled, identifier, displayName, weight, requiredKeys, false);
   }

   public RarityType(boolean registered, boolean enabled, String identifier, String displayName, double weight, int requiredKeys, boolean groupRewards) {
      this(registered, enabled, identifier, displayName, weight, requiredKeys, groupRewards, null, null);
   }

   public RarityType(
      boolean registered,
      boolean enabled,
      String identifier,
      String displayName,
      double weight,
      int requiredKeys,
      boolean groupRewards,
      BaseItem displayItem,
      String previewMenu
   ) {
      this.registered = registered;
      this.enabled = enabled;
      this.identifier = identifier;
      this.displayName = displayName;
      this.weight = weight;
      this.requiredKeys = requiredKeys;
      this.groupRewards = groupRewards;
      this.displayItem = displayItem;
      this.previewMenu = previewMenu;
      this.storageScopeId = UUID.randomUUID().toString().replace("-", "");
      this.internalStorageItems = new LinkedHashMap<>();
   }

   public String getFormattedDisplayName(@Nullable Player player) {
      return Placeholders.setPlaceholders(this.displayName, player);
   }

   @Override
   public void onLoad() {
      this.registered = true;
      if (this.storageScopeId == null || this.storageScopeId.trim().isEmpty()) {
         this.storageScopeId = UUID.randomUUID().toString().replace("-", "");
      }

      if (this.internalStorageItems == null) {
         this.internalStorageItems = new LinkedHashMap<>();
      }
   }

   public void setIdentifier(String newIdentifier) {
      this.identifier = ChatColor.stripColor(newIdentifier.replace(" ", "_"));
   }

   @Internal
   public void markRegistered() {
      this.registered = true;
   }

   @Internal
   public void unmarkRegistered() {
      this.registered = false;
   }

   @Override
   public Configuration clone() {
      RarityType clone = new RarityType(
         this.registered,
         this.enabled,
         this.identifier,
         this.displayName,
         this.weight,
         this.requiredKeys,
         this.groupRewards,
         this.displayItem,
         this.previewMenu
      );
      clone.storageScopeId = this.storageScopeId;
      clone.internalStorageItems = new LinkedHashMap<>();

      for (Entry<String, ItemStack> entry : this.internalStorageItems.entrySet()) {
         clone.internalStorageItems.put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().clone());
      }

      return clone;
   }

   @Generated
   public boolean isRegistered() {
      return this.registered;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public String getIdentifier() {
      return this.identifier;
   }

   @Generated
   public String getDisplayName() {
      return this.displayName;
   }

   @Generated
   public double getWeight() {
      return this.weight;
   }

   @Generated
   public int getRequiredKeys() {
      return this.requiredKeys;
   }

   @Generated
   public boolean isGroupRewards() {
      return this.groupRewards;
   }

   @Generated
   public BaseItem getDisplayItem() {
      return this.displayItem;
   }

   @Generated
   public String getPreviewMenu() {
      return this.previewMenu;
   }

   @Generated
   @Override
   public String getStorageScopeId() {
      return this.storageScopeId;
   }

   @Generated
   @Override
   public Map<String, ItemStack> getInternalStorageItems() {
      return this.internalStorageItems;
   }

   @Generated
   public void setRegistered(boolean registered) {
      this.registered = registered;
   }

   @Generated
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   @Generated
   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }

   @Generated
   public void setWeight(double weight) {
      this.weight = weight;
   }

   @Generated
   public void setRequiredKeys(int requiredKeys) {
      this.requiredKeys = requiredKeys;
   }

   @Generated
   public void setGroupRewards(boolean groupRewards) {
      this.groupRewards = groupRewards;
   }

   @Generated
   public void setDisplayItem(BaseItem displayItem) {
      this.displayItem = displayItem;
   }

   @Generated
   public void setPreviewMenu(String previewMenu) {
      this.previewMenu = previewMenu;
   }

   @Generated
   public void setStorageScopeId(String storageScopeId) {
      this.storageScopeId = storageScopeId;
   }

   @Generated
   public void setInternalStorageItems(Map<String, ItemStack> internalStorageItems) {
      this.internalStorageItems = internalStorageItems;
   }
}
