package id.seria.gacha.sdk.platforms.server.rich.impl;

import id.seria.gacha.api.sdk.text.Text;
import id.seria.gacha.api.sdk.utilities.Validate;
import id.seria.gacha.sdk.platforms.server.internal.RichTextConverter;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import id.seria.gacha.sdk.utilities.Reflections;
import id.seria.gacha.sdk.utilities.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdventureItemStack implements RItemStack {
   private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
   private final ItemStack item;

   public AdventureItemStack(@NonNull ItemStack item) {
      if (item == null) {
         throw new NullPointerException("item is marked non-null but is null");
      }

      this.item = item.clone();
      Validate.isTrue(RItemStack.MINIMESSAGE_SUPPORTED, "Mini Message not supported");
   }

   @Override
   public RItemStack displayName(Text text) {
      ItemMeta meta = this.item.getItemMeta();
      if (meta != null) {
         String plain = text.toPlainText();
         meta.displayName(this.deserializeKyori(plain));
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
            return Text.plain(this.serializeKyori(meta.displayName()));
         }

         if (Reflections.invokeOrDefault(meta, "hasItemName", false)) {
            Component itemName = Reflections.invokeOrNull(meta, "itemName", Component.class);
            if (itemName != null) {
               return Text.plain(this.serializeKyori(itemName));
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
         meta.lore(lore.stream().map(Text::toPlainText).map(this::deserializeKyori).collect(Collectors.toList()));
         this.item.setItemMeta(meta);
      }

      return this;
   }

   @Override
   public List<Text> lore() {
      ItemMeta meta = this.item.getItemMeta();
      return meta != null && meta.hasLore() ? meta.lore().stream().map(this::serializeKyori).map(Text::plain).collect(Collectors.toList()) : new ArrayList<>();
   }

   @Override
   public ItemStack bukkit() {
      return this.item;
   }

   @Override
   public RItemStack replaceLoreTokens(Map<String, String> tokens) {
      ItemMeta meta = this.item.getItemMeta();
      if (meta != null && meta.hasLore()) {
         List<Component> lore = new ArrayList<>(meta.lore());

         for (Entry<String, String> token : tokens.entrySet()) {
            lore.replaceAll(line -> line.replaceText(builder -> builder.matchLiteral(token.getKey()).replacement(token.getValue())));
         }

         meta.lore(lore);
         this.item.setItemMeta(meta);
      }

      return this;
   }

   private Component deserializeKyori(String plainText) {
      return RichTextConverter.deserialize(plainText);
   }

   private String serializeKyori(Component plainText) {
      return (String)MINI_MESSAGE.serialize(plainText);
   }
}
