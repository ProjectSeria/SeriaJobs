package id.seria.gacha.sdk.platforms.server.rich;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import id.seria.gacha.api.sdk.text.Text;
import id.seria.gacha.lib.xseries.XEnchantment;
import id.seria.gacha.lib.xseries.XItemFlag;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.sdk.platforms.server.rich.impl.AdventureItemStack;
import id.seria.gacha.sdk.platforms.server.rich.impl.BukkitItemStack;
import id.seria.gacha.sdk.utilities.Cache;
import id.seria.gacha.sdk.utilities.Reflections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public interface RItemStack {
   boolean MINIMESSAGE_SUPPORTED = Cache.fetch("paper-minimessage-supported", () -> {
      try {
         Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
         Class<?> itemMetaClass = Class.forName("org.bukkit.inventory.meta.ItemMeta");
         itemMetaClass.getMethod("displayName", componentClass);
         itemMetaClass.getMethod("displayName");
         itemMetaClass.getMethod("lore", List.class);
         itemMetaClass.getMethod("lore");
         Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
         return true;
      } catch (ClassNotFoundException | NoSuchMethodException e) {
         return false;
      }
   });

   static RItemStack of(@NonNull ItemStack item) {
      if (item == null) {
         throw new NullPointerException("item is marked non-null but is null");
      } else {
         return MINIMESSAGE_SUPPORTED ? new AdventureItemStack(item) : new BukkitItemStack(item);
      }
   }

   static RItemStack of(@NonNull Material material) {
      if (material == null) {
         throw new NullPointerException("material is marked non-null but is null");
      } else {
         return of(new ItemStack(material));
      }
   }

   static RItemStack of(@NonNull XMaterial material) {
      if (material == null) {
         throw new NullPointerException("material is marked non-null but is null");
      } else {
         return of(material.parseItem());
      }
   }

   boolean hasDisplayName();

   RItemStack displayName(Text var1);

   Text displayName();

   boolean hasLore();

   RItemStack lore(List<? extends Text> var1);

   List<Text> lore();

   ItemStack bukkit();

   default RItemStack replaceLoreTokens(Map<String, String> tokens) {
      if (tokens != null && !tokens.isEmpty() && this.hasLore()) {
         List<Text> replaced = new ArrayList<>();

         for (Text line : this.lore()) {
            for (Entry<String, String> token : tokens.entrySet()) {
               if (token.getKey() != null) {
                  String value = token.getValue() == null ? "" : token.getValue();
                  line = line.replaceText(Pattern.quote(token.getKey()), Matcher.quoteReplacement(value));
               }
            }

            replaced.add(line);
         }

         return this.lore(replaced);
      } else {
         return this;
      }
   }

   default RItemStack lore(Text... lore) {
      return this.lore(Lists.newArrayList(lore));
   }

   default XMaterial material() {
      return XMaterial.matchXMaterial(this.bukkit());
   }

   default int amount() {
      return this.bukkit().getAmount();
   }

   default RItemStack amount(int amount) {
      this.bukkit().setAmount(amount);
      return this;
   }

   default int modelData() {
      ItemMeta meta = this.bukkit().getItemMeta();
      return meta != null ? Reflections.invokeOrDefault(meta, "getCustomModelData", 0) : 0;
   }

   default String itemModel() {
      ItemMeta meta = this.bukkit().getItemMeta();
      return meta != null ? Reflections.invokeOrNull(meta, "getItemModel", null) : null;
   }

   default RItemStack modelData(int modelData) {
      ItemMeta meta = this.bukkit().getItemMeta();
      if (meta != null && Reflections.invokeIfExists(meta, "setCustomModelData", new Class[]{Integer.class}, modelData)) {
         this.bukkit().setItemMeta(meta);
      }

      return this;
   }

   default RItemStack itemModel(String itemModel) {
      ItemMeta meta = this.bukkit().getItemMeta();
      if (meta != null && Reflections.invokeIfExists(meta, "setItemModel", new Class[]{Integer.class}, itemModel)) {
         this.bukkit().setItemMeta(meta);
      }

      return this;
   }

   default RItemStack enchant(XEnchantment enchantment, int level) {
      ItemMeta meta = this.bukkit().getItemMeta();
      if (meta != null) {
         Enchantment bukkit = enchantment.get();
         if (bukkit != null) {
            meta.addEnchant(bukkit, level, true);
            this.bukkit().setItemMeta(meta);
         }
      }

      return this;
   }

   default Map<XEnchantment, Integer> enchants() {
      Map<XEnchantment, Integer> enchants = Maps.newLinkedHashMap();
      ItemMeta meta = this.bukkit().getItemMeta();
      if (meta != null) {
         for (Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
            enchants.put(XEnchantment.of(entry.getKey()), entry.getValue());
         }
      }

      return enchants;
   }

   default RItemStack flags(XItemFlag... flags) {
      return this.flags(Lists.newArrayList(flags));
   }

   default RItemStack flags(List<XItemFlag> flags) {
      ItemMeta meta = this.bukkit().getItemMeta();
      if (meta != null) {
         meta.addItemFlags(flags.stream().map(XItemFlag::get).filter(Objects::nonNull).toArray(ItemFlag[]::new));
         this.bukkit().setItemMeta(meta);
      }

      return this;
   }

   default Set<XItemFlag> flags() {
      Set<XItemFlag> flags = Sets.newLinkedHashSet();
      ItemMeta meta = this.bukkit().getItemMeta();
      if (meta != null) {
         for (ItemFlag itemFlag : meta.getItemFlags()) {
            flags.add(XItemFlag.of(itemFlag));
         }
      }

      return flags;
   }

   default boolean hideTooltip() {
      ItemMeta meta = this.bukkit().getItemMeta();
      return meta != null ? Reflections.invokeOrDefault(meta, "isHideTooltip", false) : false;
   }

   default RItemStack hideTooltip(boolean hideTooltip) {
      ItemMeta meta = this.bukkit().getItemMeta();
      if (meta != null && Reflections.invokeIfExists(meta, "setHideTooltip", new Class[]{boolean.class}, hideTooltip)) {
         this.bukkit().setItemMeta(meta);
      }

      return this;
   }

   default Boolean enchantmentGlintOverride() {
      ItemMeta meta = this.bukkit().getItemMeta();
      return meta != null ? Reflections.invokeOrNull(meta, "getEnchantmentGlintOverride", Boolean.class) : null;
   }

   default RItemStack enchantmentGlintOverride(Boolean glint) {
      ItemMeta meta = this.bukkit().getItemMeta();
      if (meta != null && Reflections.invokeIfExists(meta, "setEnchantmentGlintOverride", new Class[]{Boolean.class}, glint)) {
         this.bukkit().setItemMeta(meta);
      }

      return this;
   }

   default NamespacedKey tooltipStyle() {
      ItemMeta meta = this.bukkit().getItemMeta();
      return meta != null ? Reflections.invokeOrNull(meta, "getTooltipStyle", NamespacedKey.class) : null;
   }

   default RItemStack tooltipStyle(NamespacedKey tooltipStyle) {
      ItemMeta meta = this.bukkit().getItemMeta();
      if (meta != null && Reflections.invokeIfExists(meta, "setTooltipStyle", new Class[]{NamespacedKey.class}, tooltipStyle)) {
         this.bukkit().setItemMeta(meta);
      }

      return this;
   }
}
