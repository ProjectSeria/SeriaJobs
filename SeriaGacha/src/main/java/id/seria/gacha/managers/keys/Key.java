package id.seria.gacha.managers.keys;

import id.seria.gacha.api.sdk.text.Text;
import id.seria.gacha.facades.KeyFacade;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.Placeholders;
import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.annotations.LoadFirst;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XEnchantment;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.limbo.item.storage.ItemStorageCleaner;
import id.seria.gacha.limbo.item.storage.ItemStorageScope;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Key extends Configuration implements id.seria.gacha.api.key.Key, ItemStorageScope, Cloneable {
   private static long ID_COUNTER = System.currentTimeMillis();
   private boolean registered = false;
   @ConfigField
   protected boolean enabled = true;
   @ConfigField
   protected String identifier = "key_" + ID_COUNTER++;
   @ConfigField
   private boolean virtual;
   @ConfigField(path = "glow")
   private boolean glowing;
   @ConfigField(path = "item")
   protected BaseItem plainItem = BaseItem.from(XMaterial.TRIPWIRE_HOOK)
      .setCustomName("&aCrate Key")
      .setCustomLore("&7This is a lore line!", "", "&eUse this key in \u00a76%crate%\u00a7e!");
   @LoadFirst
   @ConfigField(path = "internal-storage.scope-id")
   private String storageScopeId = UUID.randomUUID().toString().replace("-", "");
   @ConfigField(path = "internal-storage.items")
   private Map<String, ItemStack> internalStorageItems = new LinkedHashMap<>();

   public static Key buildDefault(String identifier) {
      if (identifier == null) {
         identifier = "key_" + ID_COUNTER++;
      }

      return new Key(
         false,
         true,
         identifier,
         false,
         false,
         BaseItem.from(XMaterial.TRIPWIRE_HOOK)
            .setCustomName("&aCrate Key")
            .setCustomLore("&7This is a lore line!", "", "&eUse this key in \u00a76%crate%\u00a7e!")
      );
   }

   public Key(boolean registered, boolean enabled, String identifier, boolean virtual, boolean glowing, BaseItem plainItem) {
      this.registered = registered;
      this.enabled = enabled;
      this.identifier = identifier;
      this.virtual = virtual;
      this.glowing = glowing;
      this.plainItem = plainItem;
      this.storageScopeId = UUID.randomUUID().toString().replace("-", "");
      this.internalStorageItems = new LinkedHashMap<>();
   }

   public Key(boolean enabled, String identifier, boolean virtual, boolean glowing, BaseItem plainItem) {
      this.enabled = enabled;
      this.identifier = identifier;
      this.virtual = virtual;
      this.glowing = glowing;
      this.plainItem = plainItem;
      this.storageScopeId = UUID.randomUUID().toString().replace("-", "");
      this.internalStorageItems = new LinkedHashMap<>();
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

      this.cleanupUnusedStorageItems();
   }

   public void cleanupUnusedStorageItems() {
      ItemStorageCleaner.removeUnused(this, Collections.singletonList(this.plainItem));
   }

   public String getFormattedDisplayName(Player player) {
      return Placeholders.setPlaceholders(this.getDisplayName(), player);
   }

   public ItemStack createItemStack() {
      RItemStack rItem = RItemStack.of(this.plainItem.build());
      String crates = KeyFacade.getLinkedCrates(this).stream().map(type -> type.getFormattedDisplayName(null)).collect(Collectors.joining("\u00a77, "));
      String humanizedKey = StringUtil.humanize(this.identifier);
      List<String> updatedLore = this.plainItem
         .getLore()
         .stream()
         .map(lore -> lore.replace("%crate%", crates).replace("%key%", humanizedKey))
         .collect(Collectors.toList());
      rItem.lore(Text.plain(updatedLore));
      if (this.glowing) {
         rItem.enchant(XEnchantment.UNBREAKING, 1);
      }

      rItem.flags(Constants.ITEM_FLAGS);
      ItemBuilder builder = ItemBuilder.of(rItem.bukkit());
      builder.setNbtTag("phoenixcrates:key", this.identifier);
      return builder.build();
   }

   public void markRegistered() {
      this.registered = true;
   }

   public void unmarkRegistered() {
      this.registered = false;
   }

   @Override
   public String getDisplayName() {
      return this.plainItem.getDisplayName();
   }

   public Key clone() {
      Key clone = new Key(this.enabled, this.identifier, this.virtual, this.glowing, this.plainItem.clone());
      clone.storageScopeId = this.storageScopeId;
      clone.internalStorageItems = new LinkedHashMap<>();

      for (Entry<String, ItemStack> entry : this.internalStorageItems.entrySet()) {
         clone.internalStorageItems.put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().clone());
      }

      this.copyLoadedModulesTo(clone);
      return clone;
   }

   @Generated
   public boolean isRegistered() {
      return this.registered;
   }

   @Generated
   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public String getIdentifier() {
      return this.identifier;
   }

   @Generated
   @Override
   public boolean isVirtual() {
      return this.virtual;
   }

   @Generated
   @Override
   public boolean isGlowing() {
      return this.glowing;
   }

   @Generated
   public BaseItem getPlainItem() {
      return this.plainItem;
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
   public void setIdentifier(String identifier) {
      this.identifier = identifier;
   }

   @Generated
   public void setVirtual(boolean virtual) {
      this.virtual = virtual;
   }

   @Generated
   public void setGlowing(boolean glowing) {
      this.glowing = glowing;
   }

   @Generated
   public void setPlainItem(BaseItem plainItem) {
      this.plainItem = plainItem;
   }

   @Generated
   public void setStorageScopeId(String storageScopeId) {
      this.storageScopeId = storageScopeId;
   }

   @Generated
   public void setInternalStorageItems(Map<String, ItemStack> internalStorageItems) {
      this.internalStorageItems = internalStorageItems;
   }

   @Generated
   public Key() {
   }
}
