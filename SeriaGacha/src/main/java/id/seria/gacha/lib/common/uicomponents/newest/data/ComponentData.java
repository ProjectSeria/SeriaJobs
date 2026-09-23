package id.seria.gacha.lib.common.uicomponents.newest.data;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.List;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;

public class ComponentData {
   @Deprecated
   private int slot;
   private ItemStack item;
   private final Translatable title;
   private final Translatable description;

   public ComponentData() {
      this.item = XMaterial.BARRIER.parseItem();
      this.title = Translatable.undefined();
      this.description = Translatable.empty();
   }

   @Deprecated
   public ComponentData(int slot, XMaterial material, String title, List<String> description) {
      this.slot = slot;
      this.item = material.parseItem();
      this.title = Translatable.literal(title);
      this.description = Translatable.literal(description);
   }

   @Deprecated
   public ComponentData(int slot, XMaterial material, Translatable title, Translatable description) {
      this.slot = slot;
      this.item = material.parseItem();
      this.title = title;
      this.description = description;
   }

   public ComponentData(int slot, ItemStack item, String title, List<String> description) {
      this.slot = slot;
      this.item = item;
      this.title = Translatable.literal(title);
      this.description = Translatable.literal(description);
   }

   public ComponentData(XMaterial material, Translatable title, Translatable description) {
      this.item = material.parseItem();
      this.title = title;
      this.description = description;
   }

   public ComponentData clone() {
      return new ComponentData(this.slot, this.item == null ? null : this.item.clone(), this.title, this.description);
   }

   @Deprecated
   public ComponentData withSlot(int slot) {
      ComponentData clone = this.clone();
      clone.slot = slot;
      return clone;
   }

   public ComponentData withItem(ItemStack item) {
      ComponentData clone = this.clone();
      clone.item = item == null ? null : item.clone();
      return clone;
   }

   public ComponentData withTitle(Translatable title) {
      ComponentData clone = this.clone();
      return new ComponentData(clone.slot, clone.item, title, clone.description);
   }

   public ComponentData withDescription(Translatable description) {
      ComponentData clone = this.clone();
      return new ComponentData(clone.slot, clone.item, clone.title, description);
   }

   @Deprecated
   @Generated
   public int getSlot() {
      return this.slot;
   }

   @Generated
   public ItemStack getItem() {
      return this.item;
   }

   @Generated
   public Translatable getTitle() {
      return this.title;
   }

   @Generated
   public Translatable getDescription() {
      return this.description;
   }

   @Generated
   public ComponentData(int slot, ItemStack item, Translatable title, Translatable description) {
      this.slot = slot;
      this.item = item;
      this.title = title;
      this.description = description;
   }

   @Generated
   public void setItem(ItemStack item) {
      this.item = item;
   }
}
