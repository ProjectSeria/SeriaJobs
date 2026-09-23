package id.seria.gacha.limbo.item;

import id.seria.gacha.Crates;
import id.seria.gacha.lib.common.utils.configuration.adapter.Adaptable;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.limbo.item.adapter.BaseItemAdapter;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Adaptable(adapter = BaseItemAdapter.class)
public class CustomItem extends BaseItem {
   @NotNull
   protected Object value;

   private CustomItem(@NonNull Object value) {
      if (value == null) {
         throw new NullPointerException("value is marked non-null but is null");
      }

      this.value = value;
   }

   public CustomItem(@NonNull ItemStack itemStack) {
      if (itemStack == null) {
         throw new NullPointerException("itemStack is marked non-null but is null");
      }

      this.value = itemStack;
      this.setCustomAmount(itemStack.getAmount());
   }

   public CustomItem(@NonNull String storageId) {
      if (storageId == null) {
         throw new NullPointerException("storageId is marked non-null but is null");
      }

      this.value = storageId;
   }

   @Nullable
   @Override
   protected RItemStack createDefault() {
      Validate.notNull(this.value);
      if (!this.isItemStack()) {
         ItemStack extracted = Crates.getItemStorage().getItemByReference(this.value.toString());
         return extracted == null ? null : RItemStack.of(extracted);
      } else {
         return RItemStack.of((ItemStack)this.value);
      }
   }

   @NotNull
   @Override
   public BaseItem clone() {
      return new CustomItem(this.value instanceof ItemStack ? ((ItemStack)this.value).clone() : this.value);
   }

   @Override
   public String getNamespace() {
      return "custom";
   }

   public static CustomItem.Unsafe unsafe() {
      return CustomItem.Unsafe.INSTANCE;
   }

   public boolean isReference() {
      return this.value instanceof String;
   }

   @Nullable
   public String getReference() {
      return this.isReference() ? (String)this.value : null;
   }

   public boolean isItemStack() {
      return this.value instanceof ItemStack;
   }

   @Override
   public int getAmount() {
      return this.build().getAmount();
   }

   @Override
   public String getDisplayName() {
      ItemStack itemStack = this.build();
      return itemStack.getType() != Material.AIR && itemStack.getItemMeta() != null
         ? "\u00a77" + ItemBuilder.of(itemStack).getDisplayName()
         : StringUtil.humanize(itemStack.getType());
   }

   @NotNull
   @Override
   public List<String> getLore() {
      ItemStack itemStack = this.build();
      return itemStack.getItemMeta() != null && itemStack.getItemMeta().getLore() != null ? itemStack.getItemMeta().getLore() : new ArrayList<>();
   }

   public static final class Unsafe {
      private static final CustomItem.Unsafe INSTANCE = new CustomItem.Unsafe();

      private Unsafe() {
      }

      public void setStorageId(@NonNull CustomItem item, @NonNull String storageId) {
         if (item == null) {
            throw new NullPointerException("item is marked non-null but is null");
         }

         if (storageId == null) {
            throw new NullPointerException("storageId is marked non-null but is null");
         }

         if (item.value instanceof String) {
            throw new IllegalArgumentException("item already contains storage id");
         }

         item.value = storageId;
      }

      @NonNull
      public String getStorageId(@NonNull BaseItem rawItem) {
         if (rawItem == null) {
            throw new NullPointerException("rawItem is marked non-null but is null");
         } else if (!(rawItem instanceof CustomItem item)) {
            throw new IllegalArgumentException("base item not a custom item");
         } else if (!(item.value instanceof String)) {
            throw new IllegalArgumentException("stored value is not a storage id");
         } else {
            return (String)item.value;
         }
      }

      @NonNull
      public ItemStack getItemStack(@NonNull BaseItem rawItem) {
         if (rawItem == null) {
            throw new NullPointerException("rawItem is marked non-null but is null");
         } else if (!(rawItem instanceof CustomItem item)) {
            throw new IllegalArgumentException("base item not a custom item");
         } else if (!(item.value instanceof ItemStack)) {
            throw new IllegalArgumentException("stored value is not an ItemStack");
         } else {
            return (ItemStack)item.value;
         }
      }

      public void remapScope(@NonNull CustomItem item, @NonNull String oldScope, @NonNull String newScope) {
         if (item == null) {
            throw new NullPointerException("item is marked non-null but is null");
         }

         if (oldScope == null) {
            throw new NullPointerException("oldScope is marked non-null but is null");
         }

         if (newScope == null) {
            throw new NullPointerException("newScope is marked non-null but is null");
         }

         if (item.value instanceof String ref) {
            if (ref.startsWith(oldScope + ":")) {
               item.value = newScope + ref.substring(oldScope.length());
            }
         }
      }
   }
}
