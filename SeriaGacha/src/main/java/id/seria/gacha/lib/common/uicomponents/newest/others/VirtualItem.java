package id.seria.gacha.lib.common.uicomponents.newest.others;

import id.seria.gacha.api.sdk.text.Text;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.utils.configuration.objects.ComplexMaterial;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ArmorTrimMaterial;
import id.seria.gacha.lib.common.utils.inventory.ArmorTrimPattern;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.InventoryUtil;
import id.seria.gacha.lib.xseries.XEnchantment;
import id.seria.gacha.lib.xseries.XItemFlag;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class VirtualItem {
   public static int UNDEFINED = -1;
   private int[] slots = new int[]{UNDEFINED};
   private RItemStack rItemStack = RItemStack.of(XMaterial.BARRIER).displayName(Text.plain("&cNot defined"));
   private Consumer<ClickViewContext> clickConsumer;

   public VirtualItem() {
   }

   public static VirtualItem from(ItemBuilder item) {
      return new VirtualItem().withItem(item);
   }

   public static VirtualItem from(ItemStack item) {
      return new VirtualItem().withItem(item);
   }

   public static VirtualItem from(ComplexMaterial material) {
      return new VirtualItem().withItem(material);
   }

   public static VirtualItem from(XMaterial material) {
      return new VirtualItem().withMaterial(material);
   }

   public static VirtualItem air() {
      return new VirtualItem().withMaterial(XMaterial.AIR);
   }

   public static VirtualItem air(RenderViewContext context) {
      return new VirtualItem().withMaterial(XMaterial.AIR);
   }

   public VirtualItem withSlot(int slot) {
      this.slots = new int[]{slot};
      return this;
   }

   public VirtualItem withSlot(int x, int y) {
      this.slots = new int[]{InventoryUtil.getPositionRaw(x, y)};
      return this;
   }

   public VirtualItem withSlots(int[] slots) {
      if (slots.length == 0) {
         this.slots = new int[]{UNDEFINED};
      } else {
         this.slots = (int[])slots.clone();
      }

      return this;
   }

   public VirtualItem withSlots(SlotCompound compound) {
      this.withSlots(compound.asArray());
      return this;
   }

   public VirtualItem withItem(ComplexMaterial material) {
      return this.withItem(material.parseItem());
   }

   public VirtualItem withItem(ItemBuilder builder) {
      this.rItemStack = RItemStack.of(builder.build());
      return this;
   }

   public VirtualItem withItem(RItemStack item) {
      this.rItemStack = item;
      return this;
   }

   public VirtualItem withItem(ItemStack item) {
      this.rItemStack = RItemStack.of(item);
      return this;
   }

   public VirtualItem withMaterial(XMaterial material) {
      return this.withItem(material.or(XMaterial.BARRIER).parseItem());
   }

   @Deprecated
   public VirtualItem withItem(XMaterial material) {
      return this.withItem(material.or(XMaterial.BARRIER).parseItem());
   }

   @Deprecated
   public VirtualItem withGlow() {
      return this.withGlow(true);
   }

   public VirtualItem withGlow(boolean state) {
      if (state && this.getItem().getEnchantments().isEmpty()) {
         this.rItemStack.enchant(XEnchantment.UNBREAKING, 1).flags(XItemFlag.HIDE_ENCHANTS);
      }

      return this;
   }

   public VirtualItem withCustomModelData(int customModelData) {
      if (customModelData == 0) {
         return this;
      }

      ItemBuilder itemBuilder = ItemBuilder.wrap(this.rItemStack.bukkit());
      itemBuilder.setCustomModelData(customModelData);
      this.rItemStack = RItemStack.of(itemBuilder.build());
      return this;
   }

   public VirtualItem withItemModel(String itemModel) {
      if (itemModel != null && !itemModel.isEmpty()) {
         ItemBuilder itemBuilder = ItemBuilder.wrap(this.rItemStack.bukkit());
         itemBuilder.setItemModel(itemModel);
         this.rItemStack = RItemStack.of(itemBuilder.build());
         return this;
      } else {
         return this;
      }
   }

   public VirtualItem withHideTooltip(Boolean hideTooltip) {
      if (hideTooltip == null) {
         return this;
      }

      this.rItemStack.hideTooltip(hideTooltip);
      return this;
   }

   public VirtualItem withTooltipStyle(String tooltipStyle) {
      if (tooltipStyle != null && !tooltipStyle.isEmpty()) {
         this.rItemStack.tooltipStyle(parseNamespacedKey(tooltipStyle));
         return this;
      } else {
         return this;
      }
   }

   public VirtualItem withEnchantmentGlintOverride(Boolean enchantmentGlintOverride) {
      if (enchantmentGlintOverride == null) {
         return this;
      }

      this.rItemStack.enchantmentGlintOverride(enchantmentGlintOverride);
      return this;
   }

   public VirtualItem withArmorTrim(String trimMaterial, String trimPattern) {
      if (trimMaterial != null && trimPattern != null) {
         try {
            ArmorTrimMaterial material = ArmorTrimMaterial.valueOf(stripNamespace(trimMaterial).toUpperCase());
            ArmorTrimPattern pattern = ArmorTrimPattern.valueOf(stripNamespace(trimPattern).toUpperCase());
            ItemBuilder itemBuilder = ItemBuilder.wrap(this.rItemStack.bukkit());
            itemBuilder.setArmorTrim(material, pattern);
            this.rItemStack = RItemStack.of(itemBuilder.build());
         } catch (IllegalArgumentException var6) {
         }

         return this;
      } else {
         return this;
      }
   }

   public VirtualItem withBannerMeta(List<String> bannerMeta) {
      if (bannerMeta != null && !bannerMeta.isEmpty()) {
         ItemBuilder itemBuilder = ItemBuilder.wrap(this.rItemStack.bukkit());

         for (String entry : bannerMeta) {
            String[] parts = entry.split(";", 2);
            if (parts.length == 2) {
               try {
                  DyeColor color = DyeColor.valueOf(parts[0].trim().toUpperCase());
                  PatternType type = PatternType.valueOf(parts[1].trim().toUpperCase());
                  itemBuilder.addBannerPattern(new Pattern(color, type));
               } catch (IllegalArgumentException var8) {
               }
            }
         }

         this.rItemStack = RItemStack.of(itemBuilder.build());
         return this;
      } else {
         return this;
      }
   }

   public VirtualItem withPotionEffects(List<String> potionEffects) {
      if (potionEffects != null && !potionEffects.isEmpty()) {
         ItemBuilder itemBuilder = ItemBuilder.wrap(this.rItemStack.bukkit());

         for (String entry : potionEffects) {
            String[] parts = entry.split(";", 3);
            if (parts.length == 3) {
               try {
                  PotionEffectType type = (PotionEffectType)Registry.EFFECT.get(NamespacedKey.minecraft(parts[0].trim().toLowerCase()));
                  if (type != null) {
                     int duration = Integer.parseInt(parts[1].trim());
                     int amplifier = Integer.parseInt(parts[2].trim());
                     itemBuilder.addPotionEffect(new PotionEffect(type, duration, amplifier), true);
                  }
               } catch (IllegalArgumentException var9) {
               }
            }
         }

         this.rItemStack = RItemStack.of(itemBuilder.build());
         return this;
      } else {
         return this;
      }
   }

   public VirtualItem withRgb(String rgb) {
      if (rgb != null && !rgb.isEmpty()) {
         String[] parts = rgb.split(",");
         if (parts.length != 3) {
            return this;
         }

         try {
            int red = Integer.parseInt(parts[0].trim());
            int green = Integer.parseInt(parts[1].trim());
            int blue = Integer.parseInt(parts[2].trim());
            ItemBuilder itemBuilder = ItemBuilder.wrap(this.rItemStack.bukkit());
            itemBuilder.setColor(Color.fromRGB(red, green, blue));
            this.rItemStack = RItemStack.of(itemBuilder.build());
         } catch (IllegalArgumentException var7) {
         }

         return this;
      } else {
         return this;
      }
   }

   private static NamespacedKey parseNamespacedKey(String raw) {
      String[] parts = raw.split(":", 2);
      return parts.length == 2 ? new NamespacedKey(parts[0], parts[1]) : NamespacedKey.minecraft(raw);
   }

   private static String stripNamespace(String raw) {
      int separator = raw.indexOf(58);
      return separator == -1 ? raw.trim() : raw.substring(separator + 1).trim();
   }

   public VirtualItem withDisplayName(String name) {
      this.rItemStack.displayName(Text.plain(name));
      return this;
   }

   public VirtualItem withDisplayName(Text text) {
      this.rItemStack.displayName(text);
      return this;
   }

   public VirtualItem withLore(Text... description) {
      this.rItemStack.lore(description);
      return this;
   }

   public VirtualItem withLore(String... description) {
      this.rItemStack.lore(Text.plain(Arrays.asList(description)));
      return this;
   }

   public VirtualItem withLore(List<String> description) {
      this.rItemStack.lore(Text.plain(description));
      return this;
   }

   public VirtualItem withFlag(ItemFlag... flags) {
      this.rItemStack.flags(Arrays.stream(flags).map(XItemFlag::of).collect(Collectors.toList()));
      return this;
   }

   public VirtualItem withFlag(ItemFlag flag) {
      this.rItemStack.flags(XItemFlag.of(flag));
      return this;
   }

   public VirtualItem withFlag(XItemFlag xFlag) {
      this.rItemStack.flags(xFlag);
      return this;
   }

   public VirtualItem withFlag(XItemFlag... xFlags) {
      for (XItemFlag xFlag : xFlags) {
         this.withFlag(xFlag);
      }

      return this;
   }

   public VirtualItem withFlag(Collection<XItemFlag> xFlags) {
      for (XItemFlag xFlag : xFlags) {
         this.withFlag(xFlag);
      }

      return this;
   }

   public VirtualItem withAmount(int amount) {
      this.rItemStack.amount(amount);
      return this;
   }

   public VirtualItem withNbtTag(String tag, int value) {
      ItemBuilder itemBuilder = ItemBuilder.wrap(this.rItemStack.bukkit());
      itemBuilder.setNbtTag(tag, value);
      this.rItemStack = RItemStack.of(itemBuilder.build());
      return this;
   }

   public VirtualItem whenClick(Consumer<ClickViewContext> clickConsumer) {
      this.clickConsumer = clickConsumer;
      return this;
   }

   public Consumer<ClickViewContext> getClickConsumer() {
      return this.clickConsumer != null ? this.clickConsumer : ctx -> {};
   }

   public ItemStack getItem() {
      return this.rItemStack.bukkit();
   }

   public ItemBuilder getItemBuilder() {
      return ItemBuilder.wrap(this.rItemStack.bukkit());
   }

   public List<Text> lore() {
      return this.rItemStack.lore();
   }

   public List<String> getLore() {
      return this.rItemStack.lore().stream().map(Text::toPlainText).collect(Collectors.toList());
   }

   public Text displayName() {
      return this.rItemStack.displayName();
   }

   public String getDisplayName() {
      return this.rItemStack.displayName().toPlainText();
   }

   public void render(Inventory inventory) {
      for (int slot : this.slots) {
         if (slot >= 0 && slot <= inventory.getSize() - 1) {
            inventory.setItem(slot, this.getItem());
         }
      }
   }

   public void render(RenderViewContext context) {
      for (int slot : this.slots) {
         if (slot <= context.getContainerType().getSize() - 1) {
            context.setSlot(slot, this.getItem(), this.clickConsumer);
         }
      }
   }

   @Generated
   public int[] getSlots() {
      return this.slots;
   }

   @Generated
   public RItemStack getRItemStack() {
      return this.rItemStack;
   }

   public interface VirtualItemConsumer {
      void accept(RenderViewContext var1, VirtualItem var2);

      default void setItem(RenderViewContext context, int x, int y) {
         VirtualItem item = new VirtualItem();
         item.withSlot(x, y);
         this.accept(context, item);

         for (int slot : item.getSlots()) {
            context.setSlot(slot, item.getItem(), item.clickConsumer);
         }
      }
   }
}
