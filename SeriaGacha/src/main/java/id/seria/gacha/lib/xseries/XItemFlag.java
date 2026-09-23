package id.seria.gacha.lib.xseries;

import id.seria.gacha.lib.xseries.base.XBase;
import id.seria.gacha.lib.xseries.base.XRegistry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public enum XItemFlag implements XBase<XItemFlag, ItemFlag> {
   HIDE_ADDITIONAL_TOOLTIP("HIDE_POTION_EFFECTS"),
   HIDE_ARMOR_TRIM(),
   HIDE_ATTRIBUTES(),
   HIDE_DESTROYS(),
   HIDE_DYE(),
   HIDE_ENCHANTS(),
   HIDE_PLACED_ON(),
   HIDE_STORED_ENCHANTMENTS("HIDE_STORED_ENCHANTS"),
   HIDE_UNBREAKABLE(),
   HIDE_CUSTOM_DATA(),
   HIDE_MAX_STACK_SIZE(),
   HIDE_MAX_DAMAGE(),
   HIDE_DAMAGE(),
   HIDE_CUSTOM_NAME(),
   HIDE_ITEM_NAME(),
   HIDE_ITEM_MODEL(),
   HIDE_LORE(),
   HIDE_RARITY(),
   HIDE_ENCHANTMENTS(),
   HIDE_CAN_PLACE_ON(),
   HIDE_CAN_BREAK(),
   HIDE_ATTRIBUTE_MODIFIERS(),
   HIDE_CUSTOM_MODEL_DATA(),
   HIDE_TOOLTIP_DISPLAY(),
   HIDE_REPAIR_COST(),
   HIDE_CREATIVE_SLOT_LOCK(),
   HIDE_ENCHANTMENT_GLINT_OVERRIDE(),
   HIDE_INTANGIBLE_PROJECTILE(),
   HIDE_FOOD(),
   HIDE_CONSUMABLE(),
   HIDE_USE_REMAINDER(),
   HIDE_USE_COOLDOWN(),
   HIDE_DAMAGE_RESISTANT(),
   HIDE_TOOL(),
   HIDE_WEAPON(),
   HIDE_ENCHANTABLE(),
   HIDE_EQUIPPABLE(),
   HIDE_REPAIRABLE(),
   HIDE_GLIDER(),
   HIDE_TOOLTIP_STYLE(),
   HIDE_DEATH_PROTECTION(),
   HIDE_BLOCKS_ATTACKS(),
   HIDE_DYED_COLOR(),
   HIDE_MAP_COLOR(),
   HIDE_MAP_ID(),
   HIDE_MAP_DECORATIONS(),
   HIDE_MAP_POST_PROCESSING(),
   HIDE_CHARGED_PROJECTILES(),
   HIDE_BUNDLE_CONTENTS(),
   HIDE_POTION_CONTENTS(),
   HIDE_POTION_DURATION_SCALE(),
   HIDE_SUSPICIOUS_STEW_EFFECTS(),
   HIDE_WRITABLE_BOOK_CONTENT(),
   HIDE_WRITTEN_BOOK_CONTENT(),
   HIDE_TRIM(),
   HIDE_DEBUG_STICK_STATE(),
   HIDE_ENTITY_DATA(),
   HIDE_BUCKET_ENTITY_DATA(),
   HIDE_BLOCK_ENTITY_DATA(),
   HIDE_INSTRUMENT(),
   HIDE_PROVIDES_TRIM_MATERIAL(),
   HIDE_OMINOUS_BOTTLE_AMPLIFIER(),
   HIDE_JUKEBOX_PLAYABLE(),
   HIDE_PROVIDES_BANNER_PATTERNS(),
   HIDE_RECIPES(),
   HIDE_LODESTONE_TRACKER(),
   HIDE_FIREWORK_EXPLOSION(),
   HIDE_FIREWORKS(),
   HIDE_PROFILE(),
   HIDE_NOTE_BLOCK_SOUND(),
   HIDE_BANNER_PATTERNS(),
   HIDE_BASE_COLOR(),
   HIDE_POT_DECORATIONS(),
   HIDE_CONTAINER(),
   HIDE_BLOCK_STATE(),
   HIDE_BEES(),
   HIDE_LOCK(),
   HIDE_CONTAINER_LOOT(),
   HIDE_BREAK_SOUND(),
   HIDE_VILLAGER_VARIANT(),
   HIDE_WOLF_VARIANT(),
   HIDE_WOLF_SOUND_VARIANT(),
   HIDE_WOLF_COLLAR(),
   HIDE_FOX_VARIANT(),
   HIDE_SALMON_SIZE(),
   HIDE_PARROT_VARIANT(),
   HIDE_TROPICAL_FISH_PATTERN(),
   HIDE_TROPICAL_FISH_BASE_COLOR(),
   HIDE_TROPICAL_FISH_PATTERN_COLOR(),
   HIDE_MOOSHROOM_VARIANT(),
   HIDE_RABBIT_VARIANT(),
   HIDE_PIG_VARIANT(),
   HIDE_COW_VARIANT(),
   HIDE_CHICKEN_VARIANT(),
   HIDE_FROG_VARIANT(),
   HIDE_HORSE_VARIANT(),
   HIDE_PAINTING_VARIANT(),
   HIDE_LLAMA_VARIANT(),
   HIDE_AXOLOTL_VARIANT(),
   HIDE_CAT_VARIANT(),
   HIDE_CAT_COLLAR(),
   HIDE_SHEEP_COLOR(),
   HIDE_SHULKER_COLOR();

   public static final XRegistry<XItemFlag, ItemFlag> REGISTRY = XItemFlag.Data.REGISTRY;
   private static final ItemFlag[] NONE_DECORATION_FLAGS = Arrays.stream(values())
      .filter(x -> x != HIDE_LORE && x != HIDE_ITEM_NAME && x != HIDE_CUSTOM_NAME)
      .filter(XBase::isSupported)
      .map(XItemFlag::get)
      .toArray(ItemFlag[]::new);
   private final ItemFlag itemFlag;

   XItemFlag(String... names) {
      this.itemFlag = XItemFlag.Data.REGISTRY.stdEnum(this, names);
   }

   @NotNull
   public static XItemFlag of(@NotNull ItemFlag itemFlag) {
      return REGISTRY.getByBukkitForm(itemFlag);
   }

   @NotNull
   public static Optional<XItemFlag> of(@NotNull String itemFlag) {
      return REGISTRY.getByName(itemFlag);
   }

   @NotNull
   public static @Unmodifiable Collection<XItemFlag> getValues() {
      return REGISTRY.getValues();
   }

   @Override
   public String[] getNames() {
      return new String[]{this.name()};
   }

   @Nullable
   public ItemFlag get() {
      return this.itemFlag;
   }

   @Contract(mutates = "param1")
   public void set(@NotNull ItemStack item) {
      ItemMeta meta = item.getItemMeta();
      meta.addItemFlags(new ItemFlag[]{this.itemFlag});
      item.setItemMeta(meta);
   }

   @Contract(mutates = "param1")
   public void set(@NotNull ItemMeta meta) {
      meta.addItemFlags(new ItemFlag[]{this.itemFlag});
   }

   @Contract(mutates = "param1")
   public void removeFrom(@NotNull ItemStack item) {
      ItemMeta meta = item.getItemMeta();
      this.removeFrom(meta);
      item.setItemMeta(meta);
   }

   @Contract(mutates = "param1")
   public void removeFrom(@NotNull ItemMeta meta) {
      meta.removeItemFlags(new ItemFlag[]{this.itemFlag});
   }

   @Contract(value = "_ -> new", pure = true)
   @NotNull
   public static Set<XItemFlag> getFlags(@NotNull ItemStack item) {
      return getFlags(item.getItemMeta());
   }

   @Contract(value = "_ -> new", pure = true)
   @NotNull
   public static Set<XItemFlag> getFlags(@NotNull ItemMeta meta) {
      return meta.getItemFlags().stream().map(XItemFlag::of).collect(Collectors.toSet());
   }

   @Contract(mutates = "param1")
   public boolean has(@NotNull ItemStack item) {
      return this.has(item.getItemMeta());
   }

   @Contract(mutates = "param1")
   public boolean has(@NotNull ItemMeta meta) {
      return meta.getItemFlags().contains(this.itemFlag);
   }

   @Deprecated
   @Contract(mutates = "param1")
   public static void hideEverything(@NotNull ItemMeta meta) {
      decorationOnly(meta);
   }

   @Contract(mutates = "param1")
   public static void decorationOnly(@NotNull ItemMeta meta) {
      meta.addItemFlags(NONE_DECORATION_FLAGS);
   }

   static {
      REGISTRY.discardMetadata();
   }

   private static final class Data {
      private static final XRegistry<XItemFlag, ItemFlag> REGISTRY = new XRegistry<>(ItemFlag.class, XItemFlag.class, x$0 -> new XItemFlag[x$0]);

      private Data() {
      }
   }
}
