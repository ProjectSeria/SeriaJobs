package id.seria.gacha.lib.common.nms.v26_R2.item;

import id.seria.gacha.lib.common.nms.v26_R2.NMSFactoryImpl;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;

public class ItemBuilderImpl implements ItemBuilder {
   private ItemStack item;

   public ItemBuilderImpl(ItemStack item) {
      this.item = item.clone();
   }

   public ItemBuilderImpl(Material material) {
      this.item = new ItemStack(material, 1);
   }

   public ItemBuilderImpl(Material material, byte data) {
      this.item = new ItemStack(material, 1, (short)0, data);
   }

   @Override
   public ItemBuilder setType(Material material) {
      this.item.setType(material);
      return this;
   }

   @Override
   public ItemBuilder setAmount(int amount) {
      this.item.setAmount(amount);
      return this;
   }

   @Override
   public ItemBuilder setPotionColor(Color color) {
      ItemMeta meta = this.item.getItemMeta();
      if (meta instanceof PotionMeta) {
         ((PotionMeta)meta).setColor(color);
      }

      this.item.setItemMeta(meta);
      return this;
   }

   @Override
   public ItemBuilder addEnchant(Enchantment enchantment, int level) {
      this.item.addUnsafeEnchantment(enchantment, level);
      return this;
   }

   @Override
   public ItemBuilder removeEnchant(Enchantment enchantment) {
      this.item.removeEnchantment(enchantment);
      return this;
   }

   @Override
   public ItemBuilder setCustomModelData(int customModelData) {
      ItemMeta meta = this.item.getItemMeta();
      if (meta != null) {
         meta.setCustomModelData(customModelData);
         this.item.setItemMeta(meta);
      }

      return this;
   }

   @Override
   public int getCustomModelData() {
      ItemMeta meta = this.item.getItemMeta();
      return meta != null && meta.hasCustomModelData() ? meta.getCustomModelData() : 0;
   }

   @Override
   public ItemBuilder setItemModel(String itemModel) {
      ItemMeta meta = this.item.getItemMeta();
      if (meta != null) {
         meta.setItemModel(itemModel == null ? null : NamespacedKey.fromString(itemModel));
         this.item.setItemMeta(meta);
      }

      return this;
   }

   @Override
   public String getItemModel() {
      ItemMeta meta = this.item.getItemMeta();
      return meta != null && meta.getItemModel() != null ? meta.getItemModel().toString() : null;
   }

   @Override
   public boolean isItemModelSupported() {
      return true;
   }

   @Override
   public ItemBuilder setDisplayName(String name) {
      ItemMeta meta = this.item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(name);
         this.item.setItemMeta(meta);
      }

      return this;
   }

   @Override
   public String getDisplayName() {
      ItemMeta meta = this.item.getItemMeta();
      if (meta != null) {
         if (meta.hasDisplayName()) {
            return meta.getDisplayName();
         }

         if (meta.hasItemName()) {
            return meta.getItemName();
         }
      }

      return StringUtil.humanize(this.item.getType());
   }

   @Override
   public ItemBuilder changeColorDisplayName(ChatColor color) {
      ItemMeta meta = this.item.getItemMeta();
      meta.setDisplayName(color + ChatColor.stripColor(meta.getDisplayName()));
      this.item.setItemMeta(meta);
      return this;
   }

   @Override
   public ItemBuilder setLore(List<String> lore) {
      try {
         ItemMeta meta = this.item.getItemMeta();
         if (meta != null) {
            meta.setLore(lore);
            this.item.setItemMeta(meta);
         }
      } catch (IllegalArgumentException var3) {
      }

      return this;
   }

   @Override
   public ItemBuilder setLore(String... lore) {
      try {
         ItemMeta meta = this.item.getItemMeta();
         if (meta != null) {
            meta.setLore(Arrays.asList(lore));
            this.item.setItemMeta(meta);
         }
      } catch (IllegalArgumentException var3) {
      }

      return this;
   }

   @Override
   public ItemBuilder removeLoreLine(String line) {
      try {
         ItemMeta meta = this.item.getItemMeta();
         if (meta != null) {
            List<String> lore = new ArrayList<>(meta.getLore());
            if (!lore.contains(line)) {
               return this;
            }

            lore.remove(line);
            meta.setLore(lore);
            this.item.setItemMeta(meta);
         }
      } catch (IllegalArgumentException var4) {
      }

      return this;
   }

   @Override
   public ItemBuilder removeLastLoreLine() {
      try {
         ItemMeta meta = this.item.getItemMeta();
         if (meta != null) {
            List<String> lore = new ArrayList<>(meta.getLore());
            lore.remove(lore.size() - 1);
            meta.setLore(lore);
            this.item.setItemMeta(meta);
         }
      } catch (IllegalArgumentException var3) {
      }

      return this;
   }

   @Override
   public ItemBuilder removeLoreLine(int index) {
      try {
         ItemMeta meta = this.item.getItemMeta();
         if (meta != null) {
            List<String> lore = new ArrayList<>(meta.getLore());
            if (index < 0 || index > lore.size()) {
               return this;
            }

            lore.remove(index);
            meta.setLore(lore);
            this.item.setItemMeta(meta);
         }
      } catch (IllegalArgumentException var4) {
      }

      return this;
   }

   @Override
   public ItemBuilder addLoreLines(String... lines) {
      for (String s : lines) {
         this.addLoreLine(s);
      }

      return this;
   }

   @Override
   public ItemBuilder addLoreLine(String line) {
      try {
         ItemMeta meta = this.item.getItemMeta();
         if (meta != null) {
            List<String> lore = new ArrayList<>();
            if (meta.hasLore()) {
               lore = new ArrayList<>(meta.getLore());
            }

            lore.add(line);
            meta.setLore(lore);
            this.item.setItemMeta(meta);
         }
      } catch (IllegalArgumentException var4) {
      }

      return this;
   }

   @Override
   public ItemBuilder addLoreLine(int index, String line) {
      try {
         ItemMeta meta = this.item.getItemMeta();
         if (meta != null) {
            List<String> lore = new ArrayList<>();
            if (meta.hasLore()) {
               lore = new ArrayList<>(meta.getLore());
            }

            lore.add(index, line);
            meta.setLore(lore);
            this.item.setItemMeta(meta);
         }
      } catch (IllegalArgumentException var5) {
      }

      return this;
   }

   @Override
   public ItemBuilder addLoreLine(String line, int pos) {
      try {
         ItemMeta meta = this.item.getItemMeta();
         if (meta != null) {
            List<String> lore = new ArrayList<>(meta.getLore());
            lore.set(pos, line);
            meta.setLore(lore);
            this.item.setItemMeta(meta);
         }
      } catch (IllegalArgumentException var5) {
      }

      return this;
   }

   @Override
   public ItemBuilder setLeatherColor(Color color) {
      if (!(this.item.getItemMeta() instanceof LeatherArmorMeta)) {
         return this;
      }

      LeatherArmorMeta meta = (LeatherArmorMeta)this.item.getItemMeta();
      meta.setColor(color);
      this.item.setItemMeta(meta);
      return this;
   }

   @Override
   public ItemBuilder setArmorTrim(String materialNamespace, String patternNamespace) {
      if (materialNamespace != null && patternNamespace != null) {
         ItemMeta itemMeta = this.item.getItemMeta();
         if (!(itemMeta instanceof ArmorMeta)) {
            return this;
         }

         NamespacedKey materialKey = NamespacedKey.fromString(materialNamespace);
         if (materialKey == null && !materialNamespace.contains(":")) {
            materialKey = NamespacedKey.minecraft(materialNamespace.toLowerCase(Locale.ROOT));
         }

         NamespacedKey patternKey = NamespacedKey.fromString(patternNamespace);
         if (patternKey == null && !patternNamespace.contains(":")) {
            patternKey = NamespacedKey.minecraft(patternNamespace.toLowerCase(Locale.ROOT));
         }

         if (materialKey != null && patternKey != null) {
            TrimMaterial trimMaterial = (TrimMaterial)Registry.TRIM_MATERIAL.get(materialKey);
            TrimPattern trimPattern = (TrimPattern)Registry.TRIM_PATTERN.get(patternKey);
            if (trimMaterial != null && trimPattern != null) {
               ArmorMeta armorMeta = (ArmorMeta)itemMeta;
               armorMeta.setTrim(new ArmorTrim(trimMaterial, trimPattern));
               this.item.setItemMeta(armorMeta);
               return this;
            } else {
               return this;
            }
         } else {
            return this;
         }
      } else {
         return this;
      }
   }

   @Override
   public ItemBuilder setHeadOwner(String owner) {
      if (!(this.item.getItemMeta() instanceof SkullMeta)) {
         return this;
      }

      SkullMeta meta = (SkullMeta)this.item.getItemMeta();
      meta.setOwner(owner);
      this.item.setItemMeta(meta);
      return this;
   }

   @Override
   public ItemBuilder setHeadOwner(OfflinePlayer owner) {
      if (!(this.item.getItemMeta() instanceof SkullMeta)) {
         return this;
      }

      SkullMeta meta = (SkullMeta)this.item.getItemMeta();
      meta.setOwningPlayer(owner);
      this.item.setItemMeta(meta);
      return this;
   }

   @Override
   public ItemBuilder setInfinityDurability() {
      this.item.setDurability((short)32767);
      return this;
   }

   @Override
   public ItemBuilder addItemFlag(ItemFlag flag) {
      if (this.item.getType() == Material.AIR) {
         return this;
      }

      ItemMeta im = this.item.getItemMeta();
      im.addItemFlags(new ItemFlag[]{flag});
      this.item.setItemMeta(im);
      return this;
   }

   @Override
   public ItemBuilder addItemFlags(ItemFlag... flags) {
      if (this.item.getType() == Material.AIR) {
         return this;
      }

      ItemMeta im = this.item.getItemMeta();

      for (ItemFlag flag : flags) {
         im.addItemFlags(new ItemFlag[]{flag});
      }

      this.item.setItemMeta(im);
      return this;
   }

   @Override
   public List<String> getLore() {
      return this.item.getItemMeta() != null && this.item.getItemMeta().getLore() != null ? this.item.getItemMeta().getLore() : new ArrayList<>();
   }

   @Override
   public ItemBuilder addTag(String tag) {
      return this.setNbtTag(tag, tag);
   }

   @Override
   public ItemBuilder setNbtTag(String tag, String value) {
      if (this.item.getType() != Material.AIR && this.item.getItemMeta() != null) {
         ItemMeta meta = this.item.getItemMeta();
         NamespacedKey key;
         if (tag.contains(":")) {
            String[] parts = tag.split(":", 2);
            key = new NamespacedKey(parts[0], parts[1]);
         } else {
            key = new NamespacedKey(NMSFactoryImpl.PLUGIN_INSTANCE.getOriginal(), tag);
         }

         meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
         this.item.setItemMeta(meta);
         return this;
      } else {
         return this;
      }
   }

   @Override
   public String getNbtTagString(String tag) {
      if (this.item.getType() != Material.AIR && this.item.getItemMeta() != null) {
         try {
            ItemMeta meta = this.item.getItemMeta();
            NamespacedKey key;
            if (tag.contains(":")) {
               String[] parts = tag.split(":", 2);
               key = new NamespacedKey(parts[0], parts[1]);
            } else {
               key = new NamespacedKey(NMSFactoryImpl.PLUGIN_INSTANCE.getOriginal(), tag);
            }

            return (String)meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
         } catch (Exception ex) {
            ex.printStackTrace();
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public ItemBuilder setNbtTag(String tag, int value) {
      if (this.item.getType() != Material.AIR && this.item.getItemMeta() != null) {
         try {
            ItemMeta meta = this.item.getItemMeta();
            NamespacedKey key;
            if (tag.contains(":")) {
               String[] parts = tag.split(":", 2);
               key = new NamespacedKey(parts[0], parts[1]);
            } else {
               key = new NamespacedKey(NMSFactoryImpl.PLUGIN_INSTANCE.getOriginal(), tag);
            }

            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, value);
            this.item.setItemMeta(meta);
         } catch (Exception ex) {
            ex.printStackTrace();
         }

         return this;
      } else {
         return this;
      }
   }

   @Override
   public int getNbtTagInt(String tag) {
      if (this.item.getType() != Material.AIR && this.item.getItemMeta() != null) {
         try {
            ItemMeta meta = this.item.getItemMeta();
            Integer value = (Integer)meta.getPersistentDataContainer()
               .get(new NamespacedKey(NMSFactoryImpl.PLUGIN_INSTANCE.getOriginal(), tag), PersistentDataType.INTEGER);
            return value == null ? -1 : value;
         } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
         }
      } else {
         return -1;
      }
   }

   @Override
   public ItemBuilder setBookTitle(String title) {
      if (this.item.getType() != Material.AIR && this.item.getItemMeta() != null) {
         if (this.item.getItemMeta() instanceof BookMeta bookMeta) {
            bookMeta.setTitle(title);
            this.item.setItemMeta(bookMeta);
         }

         return this;
      } else {
         return this;
      }
   }

   @Override
   public ItemBuilder setBookAuthor(String author) {
      if (this.item.getType() != Material.AIR && this.item.getItemMeta() != null) {
         if (this.item.getItemMeta() instanceof BookMeta bookMeta) {
            bookMeta.setAuthor(author);
            this.item.setItemMeta(bookMeta);
         }

         return this;
      } else {
         return this;
      }
   }

   @Override
   public ItemBuilder addBookPage(BaseComponent[] content) {
      if (this.item.getType() != Material.AIR && this.item.getItemMeta() != null) {
         if (this.item.getItemMeta() instanceof BookMeta bookMeta) {
            bookMeta.spigot().addPage(new BaseComponent[][]{content});
            this.item.setItemMeta(bookMeta);
         }

         return this;
      } else {
         return this;
      }
   }

   @Override
   public ItemStack build() {
      return this.item;
   }
}
