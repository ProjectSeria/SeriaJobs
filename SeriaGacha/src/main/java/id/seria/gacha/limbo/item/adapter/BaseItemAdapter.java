package id.seria.gacha.limbo.item.adapter;

import id.seria.gacha.Crates;
import id.seria.gacha.api.internal.Item;
import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.inventory.ArmorTrimMaterial;
import id.seria.gacha.lib.common.utils.inventory.ArmorTrimPattern;
import id.seria.gacha.lib.xseries.XEnchantment;
import id.seria.gacha.lib.xseries.XItemFlag;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.limbo.item.CustomItem;
import id.seria.gacha.limbo.item.VanillaItem;
import id.seria.gacha.limbo.item.hooks.HookItem;
import id.seria.gacha.limbo.item.storage.ItemStorageScope;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseItemAdapter extends ConfigAdapter<Item, Object> {
   public BaseItemAdapter() {
   }

   public Item deserialize(ConfigAdapter.ReaderContext context) throws ErrorException, WarningException {
      Object rootObject = context.getRootObject();
      if (!(rootObject instanceof ItemStorageScope scopedStorage)) {
         throw new ErrorException("Root object must implement ItemStorageScope, found \"" + rootObject.getClass().getSimpleName() + "\"");
      } else {
         ConfigurationReader reader = context.getReader();
         Object object = context.getObject();
         if (object instanceof ItemStack) {
            return new CustomItem((ItemStack)object);
         } else if (!(object instanceof ConfigurationSection section)) {
            throw new WarningException("Expected \"Yaml Section\", found \"" + object.getClass().getSimpleName() + "\"");
         } else {
            String material = this.mapStr(section.get("material"));
            String displayName = this.mapStr(section.get("display-name"));
            List<String> lore = this.mapList(section.get("lore"));
            Integer modelData = this.mapInt(section.get("model-data"));
            String itemModel = this.mapStr(section.get("item-model"));
            Integer amount = this.mapInt(section.get("amount"));
            List<String> enchants = this.mapList(section.get("enchants"));
            List<String> flags = this.mapList(section.get("flags"));
            if (flags == null) {
               flags = this.mapList(section.get("item-flags"));
            }

            String tooltipStyle = this.mapStr(section.get("tooltip-style"));
            List<String> bannerMeta = this.mapList(section.get("banner-meta"));
            String trimMaterial = this.mapStr(section.get("trim-material"));
            String trimPattern = this.mapStr(section.get("trim-pattern"));
            List<String> potionEffects = this.mapList(section.get("potion-effects"));
            String rgb = this.mapStr(section.get("rgb"));
            Boolean enchantmentGlintOverride = this.mapBool(section.get("enchantment-glint-override"));
            String[] typeParts = material.split(":", 2);
            BaseItem item;
            if (typeParts.length == 1) {
               item = new VanillaItem(XMaterial.matchXMaterial(material).orElse(XMaterial.DIRT));
            } else {
               String type = typeParts[0].toLowerCase();
               if (HookItem.NAMESPACES.contains(type)) {
                  item = new HookItem(type, typeParts[1]);
               } else if (type.equals("custom")) {
                  String reference = typeParts[1];
                  item = new CustomItem(scopedStorage.getStorageScopeId() + ":" + reference);
               } else if (type.equals("vanilla")) {
                  item = new VanillaItem(XMaterial.matchXMaterial(typeParts[1]).orElse(XMaterial.AIR));
               } else {
                  reader.getLogger().warningAndFlush(reader.getCachedLoggedPath(), "Material type \"" + type + "\" doesn't exist. Using default value.");
                  item = new VanillaItem(XMaterial.matchXMaterial(material).orElse(XMaterial.DIRT));
               }
            }

            if (displayName != null) {
               item.setCustomName(displayName);
            }

            if (lore != null) {
               item.setCustomLore(lore);
            }

            if (modelData != null) {
               item.setCustomModelData(modelData);
            }

            if (itemModel != null) {
               item.setItemModel(itemModel);
            }

            if (amount != null) {
               item.setCustomAmount(amount);
            }

            if (enchants != null) {
               item.setCustomEnchants(this.mapEnchant(enchants));
            }

            if (flags != null) {
               item.setCustomFlags(this.formatFlags(flags));
            }

            if (tooltipStyle != null) {
               item.setCustomTooltipStyle(this.formatNamespacedKey(tooltipStyle));
            }

            if (bannerMeta != null) {
               item.setCustomBannerPatterns(this.formatBannerPatterns(bannerMeta, reader));
            }

            if (trimMaterial != null) {
               this.formatTrimMaterial(trimMaterial, reader).ifPresent(item::setCustomTrimMaterial);
            }

            if (trimPattern != null) {
               this.formatTrimPattern(trimPattern, reader).ifPresent(item::setCustomTrimPattern);
            }

            if (potionEffects != null) {
               item.setCustomPotionEffects(this.formatPotionEffects(potionEffects, reader));
            }

            if (rgb != null) {
               this.formatColor(rgb).ifPresent(item::setCustomRgb);
            }

            if (enchantmentGlintOverride != null) {
               item.setCustomEnchantmentGlintOverride(enchantmentGlintOverride);
            }

            return item;
         }
      }
   }

   @Override
   public Object serialize(ConfigAdapter.WriterContext<Item> context) throws ErrorException {
      Object rootObject = context.getRootObject();
      if (!(rootObject instanceof ItemStorageScope scopedStorage)) {
         throw new ErrorException("Root object must implement ItemStorageScope, found \"" + rootObject.getClass().getSimpleName() + "\"");
      } else {
         Item rawItem = context.getObject();
         if (!(rawItem instanceof BaseItem item)) {
            throw new ErrorException("Expected \"BaseItem\", found \"" + rawItem.getClass().getSimpleName() + "\"");
         } else {
            if (rawItem instanceof CustomItem customItem) {
               if (customItem.isItemStack()) {
                  Crates.getItemStorage().storeItem(customItem, scopedStorage);
               } else if (customItem.isReference()) {
                  String reference = CustomItem.unsafe().getStorageId(customItem);
                  if (reference.indexOf(58) == -1) {
                     throw new ErrorException("Unscoped custom item reference is not allowed: " + reference);
                  }

                  this.migrateLegacyReferenceIfNeeded(scopedStorage, reference);
               }
            }

            String namespace = item.getNamespace();
            String material;
            if (item instanceof CustomItem) {
               String ref = CustomItem.unsafe().getStorageId(item);
               int idx = ref.indexOf(58);
               String strippedRef = idx != -1 ? ref.substring(idx + 1) : ref;
               material = namespace + ":" + strippedRef;
            } else if (item instanceof HookItem) {
               material = namespace + ":" + ((HookItem)item).getId();
            } else {
               material = XMaterial.matchXMaterial(item.build()).or(XMaterial.DIRT).name();
               if (!(item instanceof VanillaItem)) {
                  material = namespace + ":" + material;
               }
            }

            Map<String, Object> values = new LinkedHashMap<>();
            values.put("material", material);
            if (item.getCustomName() != null) {
               values.put("display-name", item.getCustomName());
            }

            if (item.getCustomLore() != null) {
               values.put("lore", item.getCustomLore());
            }

            if (item.getCustomModelData() != null) {
               values.put("model-data", item.getCustomModelData());
            }

            if (item.getItemModel() != null) {
               values.put("item-model", item.getItemModel());
            }

            if (item.getCustomAmount() >= 1) {
               values.put("amount", item.getCustomAmount());
            }

            if (item.getCustomEnchants() != null) {
               List<String> enchants = new ArrayList<>();

               for (Entry<XEnchantment, Integer> entry : item.getCustomEnchants().entrySet()) {
                  enchants.add(entry.getKey().name() + ":" + entry.getValue());
               }

               values.put("enchants", enchants);
            }

            if (item.getCustomFlags() != null) {
               List<String> flagNames = new ArrayList<>();

               for (XItemFlag flag : item.getCustomFlags()) {
                  flagNames.add(flag.name());
               }

               values.put("flags", flagNames);
            }

            if (item.getCustomTooltipStyle() != null) {
               values.put("tooltip-style", item.getCustomTooltipStyle().toString());
            }

            if (item.getCustomBannerPatterns() != null) {
               List<String> bannerMeta = new ArrayList<>();

               for (Pattern pattern : item.getCustomBannerPatterns()) {
                  bannerMeta.add(pattern.getColor().name() + ";" + pattern.getPattern().name());
               }

               values.put("banner-meta", bannerMeta);
            }

            if (item.getCustomTrimMaterial() != null) {
               values.put("trim-material", item.getCustomTrimMaterial().name());
            }

            if (item.getCustomTrimPattern() != null) {
               values.put("trim-pattern", item.getCustomTrimPattern().name());
            }

            if (item.getCustomPotionEffects() != null) {
               List<String> potionEffects = new ArrayList<>();

               for (PotionEffect effect : item.getCustomPotionEffects()) {
                  potionEffects.add(effect.getType().getKey().getKey() + ";" + effect.getDuration() + ";" + effect.getAmplifier());
               }

               values.put("potion-effects", potionEffects);
            }

            if (item.getCustomRgb() != null) {
               Color color = item.getCustomRgb();
               values.put("rgb", color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
            }

            if (item.getCustomEnchantmentGlintOverride() != null) {
               values.put("enchantment-glint-override", item.getCustomEnchantmentGlintOverride());
            }

            return values;
         }
      }
   }

   @NotNull
   private Map<XEnchantment, Integer> mapEnchant(List<String> rawEnchants) {
      Map<XEnchantment, Integer> enchants = new HashMap<>();

      for (String rawEnchant : rawEnchants) {
         String[] parts = rawEnchant.split(":");
         String name = parts[0];
         int level = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
         XEnchantment xEnchant = XEnchantment.of(name).orElse(XEnchantment.UNBREAKING);
         enchants.put(xEnchant, level);
      }

      return enchants;
   }

   @NotNull
   private List<XItemFlag> formatFlags(List<String> rawFlags) {
      List<XItemFlag> flags = new ArrayList<>();

      for (String flag : rawFlags) {
         try {
            flags.add(XItemFlag.valueOf(flag.toUpperCase().replace(" ", "_")));
         } catch (IllegalArgumentException var6) {
         }
      }

      return flags;
   }

   @NotNull
   private NamespacedKey formatNamespacedKey(String raw) {
      String[] parts = raw.split(":", 2);
      return parts.length == 2 ? new NamespacedKey(parts[0], parts[1]) : NamespacedKey.minecraft(raw);
   }

   @NotNull
   private List<Pattern> formatBannerPatterns(List<String> rawPatterns, ConfigurationReader reader) {
      List<Pattern> patterns = new ArrayList<>();

      for (String entry : rawPatterns) {
         String[] parts = entry.split(";", 2);
         if (parts.length != 2) {
            reader.getLogger()
               .warningAndFlush(
                  reader.getCachedLoggedPath(), "Invalid banner pattern \"" + entry + "\". Expected format \"<dyecolor>;<patterntype>\". Ignoring."
               );
         } else {
            try {
               DyeColor color = DyeColor.valueOf(parts[0].trim().toUpperCase());
               PatternType type = PatternType.valueOf(parts[1].trim().toUpperCase());
               patterns.add(new Pattern(color, type));
            } catch (IllegalArgumentException e) {
               reader.getLogger().warningAndFlush(reader.getCachedLoggedPath(), "Invalid banner pattern \"" + entry + "\". Ignoring.");
            }
         }
      }

      return patterns;
   }

   @NotNull
   private Optional<ArmorTrimMaterial> formatTrimMaterial(String raw, ConfigurationReader reader) {
      String name = raw.contains(":") ? raw.substring(raw.indexOf(58) + 1) : raw;

      try {
         return Optional.of(ArmorTrimMaterial.valueOf(name.trim().toUpperCase()));
      } catch (IllegalArgumentException e) {
         reader.getLogger().warningAndFlush(reader.getCachedLoggedPath(), "Invalid trim material \"" + raw + "\". Ignoring.");
         return Optional.empty();
      }
   }

   @NotNull
   private Optional<ArmorTrimPattern> formatTrimPattern(String raw, ConfigurationReader reader) {
      String name = raw.contains(":") ? raw.substring(raw.indexOf(58) + 1) : raw;

      try {
         return Optional.of(ArmorTrimPattern.valueOf(name.trim().toUpperCase()));
      } catch (IllegalArgumentException e) {
         reader.getLogger().warningAndFlush(reader.getCachedLoggedPath(), "Invalid trim pattern \"" + raw + "\". Ignoring.");
         return Optional.empty();
      }
   }

   @NotNull
   private List<PotionEffect> formatPotionEffects(List<String> rawEffects, ConfigurationReader reader) {
      List<PotionEffect> effects = new ArrayList<>();

      for (String entry : rawEffects) {
         String[] parts = entry.split(";", 3);
         if (parts.length != 3) {
            reader.getLogger()
               .warningAndFlush(
                  reader.getCachedLoggedPath(), "Invalid potion effect \"" + entry + "\". Expected format \"<type>;<duration>;<amplifier>\". Ignoring."
               );
         } else {
            try {
               PotionEffectType type = (PotionEffectType)Registry.EFFECT.get(NamespacedKey.minecraft(parts[0].trim().toLowerCase()));
               if (type == null) {
                  throw new IllegalArgumentException("Unknown potion effect type: " + parts[0]);
               }

               int duration = Integer.parseInt(parts[1].trim());
               int amplifier = Integer.parseInt(parts[2].trim());
               effects.add(new PotionEffect(type, duration, amplifier));
            } catch (IllegalArgumentException e) {
               reader.getLogger().warningAndFlush(reader.getCachedLoggedPath(), "Invalid potion effect \"" + entry + "\". Ignoring.");
            }
         }
      }

      return effects;
   }

   @NotNull
   private Optional<Color> formatColor(String raw) {
      String[] parts = raw.split(",");
      if (parts.length != 3) {
         return Optional.empty();
      }

      try {
         int red = Integer.parseInt(parts[0].trim());
         int green = Integer.parseInt(parts[1].trim());
         int blue = Integer.parseInt(parts[2].trim());
         return Optional.of(Color.fromRGB(red, green, blue));
      } catch (IllegalArgumentException e) {
         return Optional.empty();
      }
   }

   @Nullable
   private Integer mapInt(Object o) {
      return o instanceof Integer ? (Integer)o : null;
   }

   @Nullable
   private String mapStr(Object obj) {
      return obj instanceof String ? (String)obj : null;
   }

   @Nullable
   private Boolean mapBool(Object obj) {
      return obj instanceof Boolean ? (Boolean)obj : null;
   }

   @Nullable
   private List<String> mapList(Object obj) {
      return obj instanceof List ? (List)obj : null;
   }

   private void migrateLegacyReferenceIfNeeded(ItemStorageScope scopedStorage, String reference) {
      int separator = reference.indexOf(58);
      if (separator > 0 && separator < reference.length() - 1) {
         String itemId = reference.substring(separator + 1);
         Map<String, ItemStack> scopedItems = scopedStorage.getInternalStorageItems();
         if (!scopedItems.containsKey(itemId)) {
            ItemStack legacy = Crates.getItemStorage().getItemByReference(reference);
            if (legacy != null) {
               ItemStack clone = legacy.clone();
               clone.setAmount(1);
               scopedItems.put(itemId, clone);
            }
         }
      }
   }
}
