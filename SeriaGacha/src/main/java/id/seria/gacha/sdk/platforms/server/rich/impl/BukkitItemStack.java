package id.seria.gacha.sdk.platforms.server.rich.impl;

import id.seria.gacha.api.sdk.text.PlainText;
import id.seria.gacha.api.sdk.text.Text;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import id.seria.gacha.sdk.utilities.Colors;
import id.seria.gacha.sdk.utilities.Reflections;
import id.seria.gacha.sdk.utilities.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BukkitItemStack implements RItemStack {
   private final ItemStack item;

   public BukkitItemStack(@NonNull ItemStack item) {
      if (item == null) {
         throw new NullPointerException("item is marked non-null but is null");
      }

      this.item = item.clone();
   }

   @Override
   public RItemStack displayName(Text component) {
      ItemMeta meta = this.item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(this.applyBukkitStyle(component.toPlainText()));
         this.item.setItemMeta(meta);
      }

      return this;
   }

   @Override
   public boolean hasDisplayName() {
      ItemMeta meta = this.item.getItemMeta();
      return meta == null ? false : meta.hasDisplayName() || Reflections.invokeOrDefault(meta, "hasItemName", false);
   }

   @Override
   public Text displayName() {
      ItemMeta meta = this.item.getItemMeta();
      if (meta != null) {
         if (meta.hasDisplayName()) {
            return Text.plain(meta.getDisplayName());
         }

         if (Reflections.invokeOrDefault(meta, "hasItemName", false)) {
            String itemName = Reflections.invokeOrNull(meta, "getItemName", String.class);
            if (itemName != null) {
               return Text.plain(itemName);
            }
         }
      }

      return Text.plain(Strings.humanize(this.item.getType()));
   }

   @Override
   public boolean hasLore() {
      ItemMeta meta = this.item.getItemMeta();
      return meta == null ? false : meta.hasLore();
   }

   @Override
   public RItemStack lore(List<? extends Text> lore) {
      ItemMeta meta = this.item.getItemMeta();
      if (meta != null) {
         meta.setLore(lore.stream().map(Text::toPlainText).map(this::applyBukkitStyle).collect(Collectors.toList()));
         this.item.setItemMeta(meta);
      }

      return this;
   }

   @Override
   public List<Text> lore() {
      ItemMeta meta = this.item.getItemMeta();
      return meta != null && meta.hasLore() ? meta.getLore().stream().map(PlainText::new).collect(Collectors.toList()) : new ArrayList<>();
   }

   @Override
   public ItemStack bukkit() {
      return this.item;
   }

   @Override
   public RItemStack replaceLoreTokens(Map<String, String> tokens) {
      ItemMeta meta = this.item.getItemMeta();
      if (meta != null && meta.hasLore()) {
         List<String> lore = new ArrayList<>(meta.getLore());

         for (Entry<String, String> token : tokens.entrySet()) {
            lore.replaceAll(line -> line.contains(token.getKey()) ? line.replace(token.getKey(), token.getValue()) : line);
         }

         meta.setLore(lore);
         this.item.setItemMeta(meta);
      }

      return this;
   }

   private String applyBukkitStyle(String plainText) {
      return Colors.replaceColorCodes(plainText);
   }
}
