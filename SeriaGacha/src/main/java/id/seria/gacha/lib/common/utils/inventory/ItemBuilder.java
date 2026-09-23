package id.seria.gacha.lib.common.utils.inventory;

import id.seria.gacha.lib.common.utils.version.ClassNMSLoader;
import id.seria.gacha.lib.xseries.XItemFlag;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.sdk.utilities.Reflections;
import java.util.List;
import lombok.NonNull;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public interface ItemBuilder {
   static ItemBuilder of(XMaterial material) {
      return of(material.parseItem());
   }

   static ItemBuilder of(@NonNull ItemStack item) {
      if (item == null) {
         throw new NullPointerException("item is marked non-null but is null");
      } else {
         return ClassNMSLoader.load(ItemBuilder.class, "item", ItemStack.class, item.clone());
      }
   }

   static ItemBuilder wrap(@NonNull ItemStack item) {
      if (item == null) {
         throw new NullPointerException("item is marked non-null but is null");
      } else {
         return ClassNMSLoader.load(ItemBuilder.class, "item", ItemStack.class, item);
      }
   }

   static ItemBuilder of(@NonNull Material material) {
      if (material == null) {
         throw new NullPointerException("material is marked non-null but is null");
      } else {
         return ClassNMSLoader.load(ItemBuilder.class, "item", Material.class, material);
      }
   }

   static ItemBuilder of(@NonNull Material material, byte data) {
      if (material == null) {
         throw new NullPointerException("material is marked non-null but is null");
      } else {
         return ClassNMSLoader.load(ItemBuilder.class, "item", new Class[]{Material.class, byte.class}, new Object[]{material, data});
      }
   }

   ItemBuilder setType(Material var1);

   ItemBuilder setAmount(int var1);

   ItemBuilder setPotionColor(Color var1);

   ItemBuilder addEnchant(Enchantment var1, int var2);

   ItemBuilder removeEnchant(Enchantment var1);

   ItemBuilder setCustomModelData(int var1);

   int getCustomModelData();

   default ItemBuilder setItemModel(String itemModel) {
      return this;
   }

   default String getItemModel() {
      return null;
   }

   default boolean isItemModelSupported() {
      return false;
   }

   ItemBuilder setDisplayName(String var1);

   String getDisplayName();

   ItemBuilder changeColorDisplayName(ChatColor var1);

   ItemBuilder setLore(List<String> var1);

   ItemBuilder setLore(String... var1);

   ItemBuilder removeLoreLine(String var1);

   ItemBuilder removeLastLoreLine();

   ItemBuilder removeLoreLine(int var1);

   ItemBuilder addLoreLines(String... var1);

   ItemBuilder addLoreLine(String var1);

   ItemBuilder addLoreLine(int var1, String var2);

   ItemBuilder addLoreLine(String var1, int var2);

   ItemBuilder setLeatherColor(Color var1);

   default ItemBuilder setArmorTrim(String materialNamespace, String patternNamespace) {
      return this;
   }

   default ItemBuilder setHideTooltip(boolean hideTooltip) {
      ItemStack item = this.build();
      ItemMeta meta = item.getItemMeta();
      if (meta != null && Reflections.invokeIfExists(meta, "setHideTooltip", new Class[]{boolean.class}, hideTooltip)) {
         item.setItemMeta(meta);
      }

      return this;
   }

   default ItemBuilder setArmorTrim(ArmorTrimMaterial material, ArmorTrimPattern pattern) {
      return material != null && pattern != null ? this.setArmorTrim(material.getNamespace(), pattern.getNamespace()) : this;
   }

   default String getArmorTrimMaterial() {
      return null;
   }

   default String getArmorTrimPattern() {
      return null;
   }

   default boolean hasArmorTrim() {
      return this.getArmorTrimMaterial() != null && this.getArmorTrimPattern() != null;
   }

   default ItemBuilder clearArmorTrim() {
      return this;
   }

   default NamespacedKey getArmorTrimPatternKey() {
      return null;
   }

   default NamespacedKey getArmorTrimMaterialKey() {
      return null;
   }

   default boolean isArmorMeta() {
      return false;
   }

   default ItemBuilder setArmorTrimMaterial(String materialNamespace) {
      String pattern = this.getArmorTrimPattern();
      return materialNamespace != null && pattern != null ? this.setArmorTrim(materialNamespace, pattern) : this;
   }

   default ItemBuilder setArmorTrimMaterial(ArmorTrimMaterial material) {
      return material == null ? this : this.setArmorTrimMaterial(material.getNamespace());
   }

   default ItemBuilder setArmorTrimPattern(String patternNamespace) {
      String material = this.getArmorTrimMaterial();
      return patternNamespace != null && material != null ? this.setArmorTrim(material, patternNamespace) : this;
   }

   default ItemBuilder setArmorTrimPattern(ArmorTrimPattern pattern) {
      return pattern == null ? this : this.setArmorTrimPattern(pattern.getNamespace());
   }

   ItemBuilder setHeadOwner(String var1);

   ItemBuilder setHeadOwner(OfflinePlayer var1);

   ItemBuilder setInfinityDurability();

   ItemBuilder addItemFlag(ItemFlag var1);

   ItemBuilder addItemFlags(ItemFlag... var1);

   default ItemBuilder addItemFlags(List<XItemFlag> flags) {
      if (flags != null && !flags.isEmpty()) {
         for (XItemFlag flag : flags) {
            if (flag != null) {
               ItemFlag bukkitFlag = flag.get();
               if (bukkitFlag != null) {
                  this.addItemFlag(bukkitFlag);
               }
            }
         }

         return this;
      } else {
         return this;
      }
   }

   List<String> getLore();

   ItemBuilder addTag(String var1);

   ItemBuilder setNbtTag(String var1, String var2);

   String getNbtTagString(String var1);

   ItemBuilder setNbtTag(String var1, int var2);

   int getNbtTagInt(String var1);

   ItemBuilder setBookTitle(String var1);

   ItemBuilder setBookAuthor(String var1);

   ItemBuilder addBookPage(BaseComponent[] var1);

   default ItemBuilder setBookPages(BaseComponent[]... content) {
      return this;
   }

   default int getMaxStackSize() {
      return this.build().getMaxStackSize();
   }

   ItemStack build();

   default ItemBuilder addPotionEffect(PotionEffect effect, boolean overwrite) {
      ItemStack item = this.build();
      if (item.hasItemMeta() && item.getItemMeta() instanceof PotionMeta) {
         PotionMeta meta = (PotionMeta)item.getItemMeta();
         meta.addCustomEffect(effect, overwrite);
         item.setItemMeta(meta);
      }

      return this;
   }

   default ItemBuilder removePotionEffect(PotionEffectType type) {
      ItemStack item = this.build();
      if (item.hasItemMeta() && item.getItemMeta() instanceof PotionMeta) {
         PotionMeta meta = (PotionMeta)item.getItemMeta();
         meta.removeCustomEffect(type);
         item.setItemMeta(meta);
      }

      return this;
   }

   default ItemBuilder setMainEffect(PotionEffectType type) {
      ItemStack item = this.build();
      if (item.hasItemMeta() && item.getItemMeta() instanceof PotionMeta) {
         PotionMeta meta = (PotionMeta)item.getItemMeta();
         meta.setMainEffect(type);
         item.setItemMeta(meta);
      }

      return this;
   }

   default ItemBuilder addBannerPattern(Pattern pattern) {
      if (pattern == null) {
         return this;
      }

      ItemStack item = this.build();
      ItemMeta meta = item.getItemMeta();
      if (meta instanceof BannerMeta) {
         BannerMeta bannerMeta = (BannerMeta)meta;
         bannerMeta.addPattern(pattern);
         item.setItemMeta(bannerMeta);
      } else if (meta instanceof BlockStateMeta) {
         BlockStateMeta blockStateMeta = (BlockStateMeta)meta;
         BlockState blockState = blockStateMeta.getBlockState();
         if (blockState instanceof Banner) {
            Banner banner = (Banner)blockState;
            banner.addPattern(pattern);
            banner.update();
            blockStateMeta.setBlockState(banner);
            item.setItemMeta(blockStateMeta);
         }
      }

      return this;
   }

   default ItemBuilder setColor(Color color) {
      if (color == null) {
         return this;
      }

      ItemStack item = this.build();
      ItemMeta meta = item.getItemMeta();
      if (meta instanceof LeatherArmorMeta) {
         ((LeatherArmorMeta)meta).setColor(color);
         item.setItemMeta(meta);
      } else if (meta instanceof PotionMeta) {
         ((PotionMeta)meta).setColor(color);
         item.setItemMeta(meta);
      } else if (meta instanceof MapMeta) {
         ((MapMeta)meta).setColor(color);
         item.setItemMeta(meta);
      } else if (meta instanceof FireworkEffectMeta) {
         FireworkEffectMeta fireworkMeta = (FireworkEffectMeta)meta;
         FireworkEffect existing = fireworkMeta.getEffect();
         Builder builder = FireworkEffect.builder().withColor(color);
         if (existing != null) {
            builder.flicker(existing.hasFlicker()).trail(existing.hasTrail()).with(existing.getType()).withFade(existing.getFadeColors());
         }

         fireworkMeta.setEffect(builder.build());
         item.setItemMeta(fireworkMeta);
      }

      return this;
   }
}
