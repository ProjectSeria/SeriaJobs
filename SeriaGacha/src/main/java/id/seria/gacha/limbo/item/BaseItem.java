package id.seria.gacha.limbo.item;

import id.seria.gacha.api.internal.Item;
import id.seria.gacha.api.sdk.text.Text;
import id.seria.gacha.lib.common.utils.inventory.ArmorTrimMaterial;
import id.seria.gacha.lib.common.utils.inventory.ArmorTrimPattern;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.xseries.XEnchantment;
import id.seria.gacha.lib.xseries.XItemFlag;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseItem extends Item implements Cloneable {
   @Nullable
   protected String customName;
   @Nullable
   protected List<String> customLore;
   @Nullable
   protected Integer customModelData;
   @Nullable
   protected String itemModel;
   @Nullable
   protected Integer customAmount;
   @Nullable
   protected Map<XEnchantment, Integer> customEnchants;
   @Nullable
   protected List<XItemFlag> customFlags;
   @Nullable
   protected NamespacedKey customTooltipStyle;
   @Nullable
   protected List<Pattern> customBannerPatterns;
   @Nullable
   protected ArmorTrimMaterial customTrimMaterial;
   @Nullable
   protected ArmorTrimPattern customTrimPattern;
   @Nullable
   protected List<PotionEffect> customPotionEffects;
   @Nullable
   protected Color customRgb;
   @Nullable
   protected Boolean customEnchantmentGlintOverride;

   public BaseItem() {
   }

   public static BaseItem vanilla(XMaterial material, String displayName) {
      VanillaItem item = new VanillaItem(material);
      item.setCustomName(displayName);
      return item;
   }

   public static BaseItem vanilla(XMaterial material, String displayName, String... lore) {
      VanillaItem item = new VanillaItem(material);
      item.setCustomName(displayName);
      item.setCustomLore(Arrays.asList(lore));
      return item;
   }

   public static BaseItem from(XMaterial material) {
      return new VanillaItem(material);
   }

   public static BaseItem from(ItemBuilder builder) {
      return from(builder.build());
   }

   public static BaseItem from(ItemStack stack) {
      return stack == null ? null : new CustomItem(stack.clone());
   }

   @NotNull
   @Override
   public ItemStack build() {
      RItemStack defaultItem = this.createDefault();
      if (defaultItem == null) {
         defaultItem = RItemStack.of(XMaterial.DIRT);
      }

      return this.override(defaultItem).bukkit();
   }

   protected RItemStack override(RItemStack stack) {
      if (this.customName != null) {
         stack.displayName(Text.plain(this.customName));
      }

      if (this.customLore != null) {
         stack.lore(Text.plain(this.customLore));
      } else if (!stack.hasLore()) {
         stack.lore(new ArrayList<>());
      }

      if (this.customModelData != null) {
         stack.modelData(this.customModelData);
      }

      if (this.itemModel != null) {
         stack.itemModel(this.itemModel);
      }

      if (this.customAmount != null) {
         stack.amount(this.customAmount);
      }

      if (this.customEnchants != null) {
         for (Entry<XEnchantment, Integer> entry : this.customEnchants.entrySet()) {
            stack.enchant(entry.getKey(), entry.getValue());
         }
      }

      if (this.customFlags != null) {
         stack.flags(this.customFlags);
      }

      if (this.customTooltipStyle != null) {
         stack.tooltipStyle(this.customTooltipStyle);
      }

      if (this.customEnchantmentGlintOverride != null) {
         stack.enchantmentGlintOverride(this.customEnchantmentGlintOverride);
      }

      if (this.customTrimMaterial != null && this.customTrimPattern != null) {
         ItemBuilder.wrap(stack.bukkit()).setArmorTrim(this.customTrimMaterial, this.customTrimPattern);
      }

      if (this.customBannerPatterns != null) {
         ItemBuilder builder = ItemBuilder.wrap(stack.bukkit());

         for (Pattern pattern : this.customBannerPatterns) {
            builder.addBannerPattern(pattern);
         }
      }

      if (this.customPotionEffects != null) {
         ItemBuilder builder = ItemBuilder.wrap(stack.bukkit());

         for (PotionEffect effect : this.customPotionEffects) {
            builder.addPotionEffect(effect, true);
         }
      }

      if (this.customRgb != null) {
         ItemBuilder.wrap(stack.bukkit()).setColor(this.customRgb);
      }

      return stack;
   }

   public BaseItem setCustomLore(@NotNull List<String> lore) {
      this.customLore = lore;
      return this;
   }

   public BaseItem setCustomLore(String... lore) {
      this.customLore = Arrays.asList(lore);
      return this;
   }

   @Nullable
   protected abstract RItemStack createDefault();

   @NotNull
   public abstract BaseItem clone();

   @Override
   public Material getType() {
      RItemStack stack = this.createDefault();
      return stack == null ? Material.DIRT : stack.bukkit().getType();
   }

   public void setCustomAmount(Integer value) {
      this.customAmount = value == null ? null : value <= 0 ? 1 : value;
   }

   public int getCustomAmount() {
      return this.customAmount == null ? 1 : Math.max(this.customAmount, 1);
   }

   @Override
   public String getDisplayName() {
      ItemStack itemStack = this.build();
      return ItemBuilder.of(itemStack).getDisplayName();
   }

   @NotNull
   @Override
   public List<String> getLore() {
      ItemStack itemStack = this.build();
      return ItemBuilder.of(itemStack).getLore();
   }

   @Override
   public int getAmount() {
      return this.build().getAmount();
   }

   @Nullable
   @Generated
   public String getCustomName() {
      return this.customName;
   }

   @Nullable
   @Generated
   public List<String> getCustomLore() {
      return this.customLore;
   }

   @Nullable
   @Generated
   public Integer getCustomModelData() {
      return this.customModelData;
   }

   @Nullable
   @Generated
   public String getItemModel() {
      return this.itemModel;
   }

   @Nullable
   @Generated
   public Map<XEnchantment, Integer> getCustomEnchants() {
      return this.customEnchants;
   }

   @Nullable
   @Generated
   public List<XItemFlag> getCustomFlags() {
      return this.customFlags;
   }

   @Nullable
   @Generated
   public NamespacedKey getCustomTooltipStyle() {
      return this.customTooltipStyle;
   }

   @Nullable
   @Generated
   public List<Pattern> getCustomBannerPatterns() {
      return this.customBannerPatterns;
   }

   @Nullable
   @Generated
   public ArmorTrimMaterial getCustomTrimMaterial() {
      return this.customTrimMaterial;
   }

   @Nullable
   @Generated
   public ArmorTrimPattern getCustomTrimPattern() {
      return this.customTrimPattern;
   }

   @Nullable
   @Generated
   public List<PotionEffect> getCustomPotionEffects() {
      return this.customPotionEffects;
   }

   @Nullable
   @Generated
   public Color getCustomRgb() {
      return this.customRgb;
   }

   @Nullable
   @Generated
   public Boolean getCustomEnchantmentGlintOverride() {
      return this.customEnchantmentGlintOverride;
   }

   @Generated
   public BaseItem setCustomName(@Nullable String customName) {
      this.customName = customName;
      return this;
   }

   @Generated
   public BaseItem setCustomModelData(@Nullable Integer customModelData) {
      this.customModelData = customModelData;
      return this;
   }

   @Generated
   public BaseItem setItemModel(@Nullable String itemModel) {
      this.itemModel = itemModel;
      return this;
   }

   @Generated
   public BaseItem setCustomEnchants(@Nullable Map<XEnchantment, Integer> customEnchants) {
      this.customEnchants = customEnchants;
      return this;
   }

   @Generated
   public BaseItem setCustomFlags(@Nullable List<XItemFlag> customFlags) {
      this.customFlags = customFlags;
      return this;
   }

   @Generated
   public BaseItem setCustomTooltipStyle(@Nullable NamespacedKey customTooltipStyle) {
      this.customTooltipStyle = customTooltipStyle;
      return this;
   }

   @Generated
   public BaseItem setCustomBannerPatterns(@Nullable List<Pattern> customBannerPatterns) {
      this.customBannerPatterns = customBannerPatterns;
      return this;
   }

   @Generated
   public BaseItem setCustomTrimMaterial(@Nullable ArmorTrimMaterial customTrimMaterial) {
      this.customTrimMaterial = customTrimMaterial;
      return this;
   }

   @Generated
   public BaseItem setCustomTrimPattern(@Nullable ArmorTrimPattern customTrimPattern) {
      this.customTrimPattern = customTrimPattern;
      return this;
   }

   @Generated
   public BaseItem setCustomPotionEffects(@Nullable List<PotionEffect> customPotionEffects) {
      this.customPotionEffects = customPotionEffects;
      return this;
   }

   @Generated
   public BaseItem setCustomRgb(@Nullable Color customRgb) {
      this.customRgb = customRgb;
      return this;
   }

   @Generated
   public BaseItem setCustomEnchantmentGlintOverride(@Nullable Boolean customEnchantmentGlintOverride) {
      this.customEnchantmentGlintOverride = customEnchantmentGlintOverride;
      return this;
   }
}
