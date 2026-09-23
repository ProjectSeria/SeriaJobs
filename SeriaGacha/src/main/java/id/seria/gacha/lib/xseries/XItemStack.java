package id.seria.gacha.lib.xseries;

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import id.seria.gacha.lib.xseries.paper.AdventureAPIFactory;
import id.seria.gacha.lib.xseries.profiles.builder.XSkull;
import id.seria.gacha.lib.xseries.profiles.objects.Profileable;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Axolotl.Variant;
import org.bukkit.entity.TropicalFish.Pattern;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.AxolotlBucketMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;
import org.bukkit.material.MaterialData;
import org.bukkit.material.SpawnEgg;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class XItemStack {
   @Deprecated
   private static final XMaterial DEFAULT_MATERIAL = XMaterial.BARRIER;
   private static final boolean SUPPORTS_UNBREAKABLE;
   private static final boolean SUPPORTS_POTION_COLOR;
   private static final boolean SUPPORTS_Inventory_getStorageContents;
   private static final boolean SUPPORTS_CUSTOM_MODEL_DATA;
   private static final boolean SUPPORTS_ADVANCED_CUSTOM_MODEL_DATA;
   private static final boolean SUPPORTS_ITEM_MODEL;
   private static final boolean SUPPORTS_ITEM_NAME;
   private static final Map<Class<? extends ItemMeta>, Optional<Function<XItemStack.Deserializer, XItemStack.MetaHandler<ItemMeta>>>> DESERIALIZE_META_HANDLERS;
   private static final Map<Class<? extends ItemMeta>, Optional<Function<XItemStack.Serializer, XItemStack.MetaHandler<ItemMeta>>>> SERIALIZE_META_HANDLERS;

   private static <M extends ItemMeta> void meta(
      Class<? extends M> clazz,
      Function<XItemStack.Deserializer, XItemStack.MetaHandler<M>> deserialize,
      Function<XItemStack.Serializer, XItemStack.MetaHandler<M>> serialize
   ) {
      DESERIALIZE_META_HANDLERS.put(clazz, Optional.of(cast(deserialize)));
      SERIALIZE_META_HANDLERS.put(clazz, Optional.of(cast(serialize)));
   }

   private static void onlyIf(String className, Runnable runnable) {
      try {
         Class.forName("org.bukkit.inventory.meta." + className);
         runnable.run();
      } catch (ClassNotFoundException var3) {
      }
   }

   private static <T extends XItemStack.SerialObject> void recursiveMetaHandle(
      T serialObject,
      Class<?> metaClass,
      ItemMeta meta,
      Map<Class<? extends ItemMeta>, Optional<Function<T, XItemStack.MetaHandler<ItemMeta>>>> map,
      List<Function<T, XItemStack.MetaHandler<ItemMeta>>> collectedHandlers
   ) {
      Optional<Function<T, XItemStack.MetaHandler<ItemMeta>>> handler = map.get(metaClass);
      if (handler != null) {
         if (handler.isPresent()) {
            if (collectedHandlers != null) {
               collectedHandlers.add(handler.get());
            }

            handler.get().apply(serialObject).handle(meta);
         }
      } else {
         List<Function<T, XItemStack.MetaHandler<ItemMeta>>> subCollectedHandlers = new ArrayList<>();
         Class<?> superclass = metaClass.getSuperclass();
         if (superclass != null) {
            recursiveMetaHandle(serialObject, superclass, meta, map, subCollectedHandlers);
         }

         for (Class<?> anInterface : metaClass.getInterfaces()) {
            recursiveMetaHandle(serialObject, anInterface, meta, map, subCollectedHandlers);
         }

         if (subCollectedHandlers.isEmpty()) {
            map.put((Class<? extends ItemMeta>)metaClass, Optional.empty());
         } else {
            map.put((Class<? extends ItemMeta>)metaClass, Optional.of(inst -> subMeta -> {
               T castedInst = cast(inst);

               for (Function<T, XItemStack.MetaHandler<ItemMeta>> subCollectedHandler : subCollectedHandlers) {
                  subCollectedHandler.apply(castedInst).handle(subMeta);
               }
            }));
            if (collectedHandlers != null) {
               collectedHandlers.addAll(subCollectedHandlers);
            }
         }
      }
   }

   private static <T> T cast(Object something) {
      return (T)something;
   }

   private XItemStack() {
   }

   private static BlockState safeBlockState(BlockStateMeta meta) {
      try {
         return meta.getBlockState();
      } catch (IllegalStateException ex) {
         if (ex.getMessage().toLowerCase(Locale.ENGLISH).contains("missing blockstate")) {
            return null;
         } else {
            throw ex;
         }
      } catch (ClassCastException ex) {
         return null;
      }
   }

   @Deprecated
   public static void serialize(@NotNull ItemStack item, @NotNull ConfigurationSection config) {
      serialize(item, config, Function.identity());
   }

   @Deprecated
   public static void serialize(@NotNull ItemStack item, @NotNull ConfigurationSection config, @NotNull Function<String, String> translator) {
      new XItemStack.Serializer().fromItem(item).toConfig(config).withTranslator(translator).serialize();
   }

   @Deprecated
   public static Map<String, Object> serialize(@NotNull ItemStack item) {
      Objects.requireNonNull(item, "Cannot serialize a null item");
      ConfigurationSection config = new MemoryConfiguration();
      serialize(item, config);
      return configSectionToMap(config);
   }

   @Deprecated
   @NotNull
   public static ItemStack deserialize(@NotNull ConfigurationSection config) {
      return edit(DEFAULT_MATERIAL.parseItem(), config, Function.identity(), null);
   }

   @Deprecated
   @NotNull
   public static ItemStack deserialize(@NotNull Map<String, Object> serializedItem) {
      Objects.requireNonNull(serializedItem, "serializedItem cannot be null.");
      return deserialize(mapToConfigSection(serializedItem));
   }

   @Deprecated
   @NotNull
   public static ItemStack deserialize(@NotNull ConfigurationSection config, @NotNull Function<String, String> translator) {
      return deserialize(config, translator, null);
   }

   @Deprecated
   @NotNull
   public static ItemStack deserialize(
      @NotNull ConfigurationSection config, @NotNull Function<String, String> translator, @Nullable Consumer<Exception> restart
   ) {
      return edit(DEFAULT_MATERIAL.parseItem(), config, translator, restart);
   }

   @Deprecated
   @NotNull
   public static ItemStack deserialize(@NotNull Map<String, Object> serializedItem, @NotNull Function<String, String> translator) {
      Objects.requireNonNull(serializedItem, "serializedItem cannot be null.");
      Objects.requireNonNull(translator, "translator cannot be null.");
      return deserialize(mapToConfigSection(serializedItem), translator);
   }

   private static int toInt(String str, int defaultValue) {
      try {
         return Integer.parseInt(str);
      } catch (NumberFormatException nfe) {
         return defaultValue;
      }
   }

   private static List<String> split(@NotNull String str, char separatorChar) {
      List<String> list = new ArrayList<>(5);
      boolean match = false;
      boolean lastMatch = false;
      int len = str.length();
      int start = 0;

      for (int i = 0; i < len; i++) {
         if (str.charAt(i) == separatorChar) {
            if (match) {
               list.add(str.substring(start, i));
               match = false;
               lastMatch = true;
            }

            start = i + 1;
         } else {
            lastMatch = false;
            match = true;
         }
      }

      if (match || lastMatch) {
         list.add(str.substring(start, len));
      }

      return list;
   }

   private static List<String> splitNewLine(String str) {
      int len = str.length();
      List<String> list = new ArrayList<>();
      int i = 0;
      int start = 0;
      boolean match = false;
      boolean lastMatch = false;

      while (i < len) {
         if (str.charAt(i) == '\n') {
            if (match) {
               list.add(str.substring(start, i));
               match = false;
               lastMatch = true;
            }

            start = ++i;
         } else {
            lastMatch = false;
            match = true;
            i++;
         }
      }

      if (match || lastMatch) {
         list.add(str.substring(start, i));
      }

      return list;
   }

   public static XItemStack.Serializer serializer() {
      return new XItemStack.Serializer();
   }

   public static XItemStack.Deserializer deserializer() {
      return new XItemStack.Deserializer();
   }

   @Deprecated
   @NotNull
   public static ItemStack edit(
      @NotNull ItemStack item, @NotNull ConfigurationSection config, @NotNull Function<String, String> translator, @Nullable Consumer<Exception> restart
   ) {
      return new XItemStack.Deserializer().modifyItem(item).fromConfig(config).withTranslator(translator).withRestart(restart).deserialize();
   }

   @NotNull
   private static ConfigurationSection mapToConfigSection(@NotNull Map<?, ?> map) {
      ConfigurationSection config = new MemoryConfiguration();

      for (Entry<?, ?> entry : map.entrySet()) {
         String key = entry.getKey().toString();
         Object value = entry.getValue();
         if (value != null) {
            if (value instanceof Map) {
               value = mapToConfigSection((Map<?, ?>)value);
            }

            config.set(key, value);
         }
      }

      return config;
   }

   @NotNull
   private static Map<String, Object> configSectionToMap(@NotNull ConfigurationSection config) {
      Map<String, Object> map = new LinkedHashMap<>();

      for (String key : config.getKeys(false)) {
         Object value = config.get(key);
         if (value != null) {
            if (value instanceof ConfigurationSection) {
               value = configSectionToMap((ConfigurationSection)value);
            }

            map.put(key, value);
         }
      }

      return map;
   }

   @NotNull
   @Internal
   public static Optional<Color> parseColor(@Nullable String str) {
      if (Strings.isNullOrEmpty(str)) {
         return Optional.empty();
      }

      List<String> rgb = split(str.replace(" ", ""), ',');
      if (rgb.size() == 3) {
         return Optional.of(Color.fromRGB(toInt(rgb.get(0), 0), toInt(rgb.get(1), 0), toInt(rgb.get(2), 0)));
      }

      try {
         return Optional.of(Color.fromRGB(Integer.parseInt(str)));
      } catch (NumberFormatException var4) {
         if (str.startsWith("#")) {
            str = str.substring(1);
         }

         try {
            return Optional.of(Color.fromRGB(Integer.parseInt(str, 16)));
         } catch (NumberFormatException e) {
            return Optional.empty();
         }
      }
   }

   private static <T> List<T> parseRawOrList(String singular, String plural, ConfigurationSection section, Function<String, T> convert) {
      List<String> list = section.getStringList(plural);
      if (!list.isEmpty()) {
         return list.stream().map(convert).collect(Collectors.toList());
      }

      list = section.getStringList(singular);
      if (!list.isEmpty()) {
         return list.stream().map(convert).collect(Collectors.toList());
      }

      String single = section.getString(singular);
      if (single != null && !single.isEmpty()) {
         return Collections.singletonList(convert.apply(single));
      }

      single = section.getString(plural);
      return single != null && !single.isEmpty() ? Collections.singletonList(convert.apply(single)) : Collections.emptyList();
   }

   private static <T> T tryNumber(String str, Function<String, T> convert) {
      try {
         return convert.apply(str);
      } catch (NumberFormatException ex) {
         return null;
      }
   }

   @NotNull
   @Contract(mutates = "param1")
   public static List<ItemStack> giveOrDrop(@NotNull Player player, @Nullable ItemStack... items) {
      return giveOrDrop(player, true, items);
   }

   @NotNull
   @Contract(mutates = "param1")
   public static List<ItemStack> giveOrDrop(@NotNull Player player, boolean split, @Nullable ItemStack... items) {
      if (items != null && items.length != 0) {
         List<ItemStack> leftOvers = addItems(player.getInventory(), split, items);
         World world = player.getWorld();
         Location location = player.getLocation();

         for (ItemStack drop : leftOvers) {
            world.dropItemNaturally(location, drop);
         }

         return leftOvers;
      } else {
         return new ArrayList<>();
      }
   }

   @Contract(mutates = "param1")
   public static List<ItemStack> addItems(@NotNull Inventory inventory, boolean split, @NotNull ItemStack... items) {
      return addItems(inventory, split, null, items);
   }

   @NotNull
   @Contract(mutates = "param1")
   public static List<ItemStack> addItems(
      @NotNull Inventory inventory, boolean split, @Nullable Predicate<Integer> modifiableSlots, @NotNull ItemStack... items
   ) {
      Objects.requireNonNull(inventory, "Cannot add items to null inventory");
      Objects.requireNonNull(items, "Cannot add null items to inventory");
      List<ItemStack> leftOvers = new ArrayList<>(items.length);
      int invSize = getStorageContents(inventory).length;
      int lastEmpty = 0;

      for (ItemStack item : items) {
         int lastPartial = 0;
         int maxAmount = split ? item.getMaxStackSize() : inventory.getMaxStackSize();

         while (true) {
            int firstPartial = lastPartial >= invSize ? -1 : firstPartial(inventory, item, lastPartial, modifiableSlots);
            if (firstPartial == -1) {
               if (lastEmpty != -1) {
                  lastEmpty = firstEmpty(inventory, lastEmpty, modifiableSlots);
               }

               if (lastEmpty == -1) {
                  leftOvers.add(item);
                  break;
               }

               lastPartial = Integer.MAX_VALUE;
               int amount = item.getAmount();
               if (amount <= maxAmount) {
                  inventory.setItem(lastEmpty, item);
                  break;
               }

               ItemStack copy = item.clone();
               copy.setAmount(maxAmount);
               inventory.setItem(lastEmpty, copy);
               item.setAmount(amount - maxAmount);
               if (++lastEmpty == invSize) {
                  lastEmpty = -1;
               }
            } else {
               ItemStack partialItem = inventory.getItem(firstPartial);
               int sum = item.getAmount() + partialItem.getAmount();
               if (sum <= maxAmount) {
                  partialItem.setAmount(sum);
                  inventory.setItem(firstPartial, partialItem);
                  break;
               }

               partialItem.setAmount(maxAmount);
               inventory.setItem(firstPartial, partialItem);
               item.setAmount(sum - maxAmount);
               lastPartial = firstPartial + 1;
            }
         }
      }

      return leftOvers;
   }

   @NotNull
   @Contract(pure = true)
   public static @Range(from = -1L, to = 2147483647L) int firstPartial(@NotNull Inventory inventory, @Nullable ItemStack item, int beginIndex) {
      return firstPartial(inventory, item, beginIndex, null);
   }

   @NotNull
   @Contract(pure = true)
   public static @Range(from = -1L, to = 2147483647L) int firstPartial(
      @NotNull Inventory inventory, @Nullable ItemStack item, int beginIndex, @Nullable Predicate<Integer> modifiableSlots
   ) {
      if (item != null) {
         ItemStack[] items = getStorageContents(inventory);
         int invSize = items.length;
         if (beginIndex < 0 || beginIndex >= invSize) {
            throw new IndexOutOfBoundsException("Begin Index: " + beginIndex + ", Inventory storage content size: " + invSize);
         }

         for (; beginIndex < invSize; beginIndex++) {
            if (modifiableSlots == null || modifiableSlots.test(beginIndex)) {
               ItemStack cItem = items[beginIndex];
               if (cItem != null && cItem.getAmount() < cItem.getMaxStackSize() && cItem.isSimilar(item)) {
                  return beginIndex;
               }
            }
         }
      }

      return -1;
   }

   @NotNull
   @Contract(pure = true)
   public static List<ItemStack> stack(@NotNull Collection<ItemStack> items) {
      return stack(items, ItemStack::isSimilar);
   }

   @NotNull
   @Contract(pure = true)
   public static List<ItemStack> stack(@NotNull Collection<ItemStack> items, @NotNull BiPredicate<ItemStack, ItemStack> similarity) {
      Objects.requireNonNull(items, "Cannot stack null items");
      Objects.requireNonNull(similarity, "Similarity check cannot be null");
      List<ItemStack> stacked = new ArrayList<>(items.size());

      for (ItemStack item : items) {
         if (item != null && item.getType() != Material.AIR) {
            boolean add = true;

            for (ItemStack stack : stacked) {
               if (similarity.test(item, stack)) {
                  stack.setAmount(stack.getAmount() + item.getAmount());
                  add = false;
                  break;
               }
            }

            if (add) {
               stacked.add(item.clone());
            }
         }
      }

      return stacked;
   }

   @Contract(pure = true)
   public static @Range(from = -1L, to = 2147483647L) int firstEmpty(@NotNull Inventory inventory, int beginIndex) {
      return firstEmpty(inventory, beginIndex, null);
   }

   @Contract(pure = true)
   public static @Range(from = -1L, to = 2147483647L) int firstEmpty(@NotNull Inventory inventory, int beginIndex, @Nullable Predicate<Integer> modifiableSlots) {
      ItemStack[] items = getStorageContents(inventory);
      int invSize = items.length;
      if (beginIndex >= 0 && beginIndex < invSize) {
         while (beginIndex < invSize) {
            if ((modifiableSlots == null || modifiableSlots.test(beginIndex)) && items[beginIndex] == null) {
               return beginIndex;
            }

            beginIndex++;
         }

         return -1;
      } else {
         throw new IndexOutOfBoundsException("Begin Index: " + beginIndex + ", Inventory storage content size: " + invSize);
      }
   }

   @Contract(pure = true)
   public static @Range(from = -1L, to = 2147483647L) int firstPartialOrEmpty(@NotNull Inventory inventory, @Nullable ItemStack item, int beginIndex) {
      if (item != null) {
         ItemStack[] items = getStorageContents(inventory);
         int len = items.length;
         if (beginIndex < 0 || beginIndex >= len) {
            throw new IndexOutOfBoundsException("Begin Index: " + beginIndex + ", Size: " + len);
         }

         while (beginIndex < len) {
            ItemStack cItem = items[beginIndex];
            if (cItem == null || cItem.getAmount() < cItem.getMaxStackSize() && cItem.isSimilar(item)) {
               return beginIndex;
            }

            beginIndex++;
         }
      }

      return -1;
   }

   @Contract(pure = true)
   public static ItemStack[] getStorageContents(Inventory inventory) {
      return SUPPORTS_Inventory_getStorageContents ? inventory.getStorageContents() : Arrays.copyOfRange(inventory.getContents(), 0, 36);
   }

   @Contract(pure = true)
   public static boolean notEmpty(@Nullable ItemStack item) {
      return !isEmpty(item);
   }

   @Contract(pure = true)
   public static boolean isEmpty(@Nullable ItemStack item) {
      return item == null || item.getType() == Material.AIR;
   }

   static {
      boolean supportsPotionColor = false;
      boolean supportsUnbreakable = false;
      boolean supportsGetStorageContents = false;
      boolean supportSCustomModelData = false;
      boolean supportsAdvancedCustomModelData = false;
      boolean supportsItemModel = false;
      boolean supportsItemName = false;

      try {
         ItemMeta.class.getDeclaredMethod("setUnbreakable", boolean.class);
         supportsUnbreakable = true;
      } catch (NoSuchMethodException var14) {
      }

      try {
         ItemMeta.class.getDeclaredMethod("hasCustomModelData");
         supportSCustomModelData = true;
      } catch (NoSuchMethodException var13) {
      }

      try {
         ItemMeta.class.getDeclaredMethod("getCustomModelDataComponent");
         supportsAdvancedCustomModelData = true;
      } catch (NoSuchMethodException var12) {
      }

      try {
         ItemMeta.class.getDeclaredMethod("getItemModel");
         supportsItemModel = true;
      } catch (NoSuchMethodException var11) {
      }

      try {
         ItemMeta.class.getDeclaredMethod("setItemName", String.class);
         supportsItemName = true;
      } catch (NoSuchMethodException var10) {
      }

      try {
         Class.forName("org.bukkit.inventory.meta.PotionMeta").getMethod("setColor", Color.class);
         supportsPotionColor = true;
      } catch (Throwable var9) {
      }

      try {
         Inventory.class.getDeclaredMethod("getStorageContents");
         supportsGetStorageContents = true;
      } catch (NoSuchMethodException var8) {
      }

      SUPPORTS_POTION_COLOR = supportsPotionColor;
      SUPPORTS_UNBREAKABLE = supportsUnbreakable;
      SUPPORTS_Inventory_getStorageContents = supportsGetStorageContents;
      SUPPORTS_CUSTOM_MODEL_DATA = supportSCustomModelData;
      SUPPORTS_ADVANCED_CUSTOM_MODEL_DATA = supportsAdvancedCustomModelData;
      SUPPORTS_ITEM_MODEL = supportsItemModel;
      SUPPORTS_ITEM_NAME = supportsItemName;
      DESERIALIZE_META_HANDLERS = new IdentityHashMap<>();
      SERIALIZE_META_HANDLERS = new IdentityHashMap<>();
      meta(SkullMeta.class, x -> {
         XItemStack.Deserializer rec$ = x;
         return x$0 -> rec$.handleSkullMeta(x$0);
      }, x -> {
         XItemStack.Serializer rec$ = x;
         return x$0 -> rec$.handleSkullMeta(x$0);
      });
      meta(LeatherArmorMeta.class, x -> {
         XItemStack.Deserializer rec$ = x;
         return x$0 -> rec$.handleLeatherArmorMeta(x$0);
      }, x -> {
         XItemStack.Serializer rec$ = x;
         return x$0 -> rec$.handleLeatherArmorMeta(x$0);
      });
      meta(PotionMeta.class, x -> {
         XItemStack.Deserializer rec$ = x;
         return x$0 -> rec$.handlePotionMeta(x$0);
      }, x -> {
         XItemStack.Serializer rec$ = x;
         return x$0 -> rec$.handlePotionMeta(x$0);
      });
      meta(BlockStateMeta.class, x -> {
         XItemStack.Deserializer rec$ = x;
         return x$0 -> rec$.handleBlockStateMeta(x$0);
      }, x -> {
         XItemStack.Serializer rec$ = x;
         return x$0 -> rec$.handleBlockStateMeta(x$0);
      });
      meta(FireworkMeta.class, x -> {
         XItemStack.Deserializer rec$ = x;
         return x$0 -> rec$.handleFireworkMeta(x$0);
      }, x -> {
         XItemStack.Serializer rec$ = x;
         return x$0 -> rec$.handleFireworkMeta(x$0);
      });
      meta(BookMeta.class, x -> {
         XItemStack.Deserializer rec$ = x;
         return x$0 -> rec$.handleBookMeta(x$0);
      }, x -> {
         XItemStack.Serializer rec$ = x;
         return x$0 -> rec$.handleBookMeta(x$0);
      });
      meta(BannerMeta.class, x -> {
         XItemStack.Deserializer rec$ = x;
         return x$0 -> rec$.handleBannerMeta(x$0);
      }, x -> {
         XItemStack.Serializer rec$ = x;
         return x$0 -> rec$.handleBannerMeta(x$0);
      });
      meta(MapMeta.class, x -> {
         XItemStack.Deserializer rec$ = x;
         return x$0 -> rec$.handleMapMeta(x$0);
      }, x -> {
         XItemStack.Serializer rec$ = x;
         return x$0 -> rec$.handleMapMeta(x$0);
      });
      meta(EnchantmentStorageMeta.class, x -> {
         XItemStack.Deserializer rec$ = x;
         return x$0 -> rec$.handleEnchantmentStorageMeta(x$0);
      }, x -> {
         XItemStack.Serializer rec$ = x;
         return x$0 -> rec$.handleEnchantmentStorageMeta(x$0);
      });
      onlyIf("SpawnEggMeta", () -> meta(SpawnEggMeta.class, x -> {
         XItemStack.Deserializer rec$ = x;
         return x$0 -> rec$.handleSpawnEggMeta(x$0);
      }, x -> {
         XItemStack.Serializer rec$ = x;
         return x$0 -> rec$.handleSpawnEggMeta(x$0);
      }));
      onlyIf("ArmorMeta", () -> meta(ArmorMeta.class, x -> {
         XItemStack.Deserializer rec$ = x;
         return x$0 -> rec$.handleArmorMeta(x$0);
      }, x -> {
         XItemStack.Serializer rec$ = x;
         return x$0 -> rec$.handleArmorMeta(x$0);
      }));
      onlyIf("AxolotlBucketMeta", () -> meta(AxolotlBucketMeta.class, x -> {
         XItemStack.Deserializer rec$ = x;
         return x$0 -> rec$.handleAxolotlBucketMeta(x$0);
      }, x -> {
         XItemStack.Serializer rec$ = x;
         return x$0 -> rec$.handleAxolotlBucketMeta(x$0);
      }));
      onlyIf("CompassMeta", () -> meta(CompassMeta.class, x -> {
         XItemStack.Deserializer rec$ = x;
         return x$0 -> rec$.handleCompassMeta(x$0);
      }, x -> {
         XItemStack.Serializer rec$ = x;
         return x$0 -> rec$.handleCompassMeta(x$0);
      }));
      onlyIf("SuspiciousStewMeta", () -> meta(SuspiciousStewMeta.class, x -> {
         XItemStack.Deserializer rec$ = x;
         return x$0 -> rec$.handleSuspiciousStewMeta(x$0);
      }, x -> {
         XItemStack.Serializer rec$ = x;
         return x$0 -> rec$.handleSuspiciousStewMeta(x$0);
      }));
      onlyIf("CrossbowMeta", () -> meta(CrossbowMeta.class, x -> {
         XItemStack.Deserializer rec$ = x;
         return x$0 -> rec$.handleCrossbowMeta(x$0);
      }, x -> {
         XItemStack.Serializer rec$ = x;
         return x$0 -> rec$.handleCrossbowMeta(x$0);
      }));
      onlyIf("TropicalFishBucketMeta", () -> meta(TropicalFishBucketMeta.class, x -> {
         XItemStack.Deserializer rec$ = x;
         return x$0 -> rec$.handleTropicalFishBucketMeta(x$0);
      }, x -> {
         XItemStack.Serializer rec$ = x;
         return x$0 -> rec$.handleTropicalFishBucketMeta(x$0);
      }));
   }

   public static final class Deserializer extends XItemStack.SerialObject {
      private Consumer<Exception> restart;
      private Function<List<String>, List<? extends Component>> miniMessageHandler;
      private static final boolean SPACE_EMPTY_LORE_LINES = true;
      private static final boolean SUPPORTS_PotionMeta_setBasePotionType = XReflection.of(PotionMeta.class)
         .method("public void setBasePotionType(org.bukkit.potion.PotionType potionType)")
         .exists();
      private static final boolean SUPPORTS_PotionEffectType_getKey = XReflection.of(PotionEffectType.class)
         .method("public org.bukkit.NamespacedKey getKey()")
         .exists();

      private Deserializer() {
      }

      public XItemStack.Deserializer modifyItem(ItemStack item) {
         this.item = item;
         return this;
      }

      public XItemStack.Deserializer fromConfig(ConfigurationSection config) {
         this.config = config;
         return this;
      }

      @NotNull
      public XItemStack.Deserializer fromConfig(Map<String, Object> serializedItem) {
         Objects.requireNonNull(serializedItem, "serializedItem cannot be null.");
         return this.fromConfig(XItemStack.mapToConfigSection(serializedItem));
      }

      public XItemStack.Deserializer withTranslator(Function<String, String> translator) {
         super.withTranslator(translator);
         return this;
      }

      public XItemStack.Deserializer withRestart(Consumer<Exception> restart) {
         this.restart = restart;
         return this;
      }

      @Experimental
      public XItemStack.Deserializer withMiniMessage(Function<List<String>, List<? extends Component>> miniMessageHandler) {
         this.miniMessageHandler = miniMessageHandler;
         return this;
      }

      public XItemStack.Deserializer copy() {
         return this.copySettings().modifyItem(this.item == null ? null : this.item.clone());
      }

      public XItemStack.Deserializer copySettings() {
         return new XItemStack.Deserializer()
            .fromConfig(this.config)
            .withTranslator(this.translator)
            .withRestart(this.restart)
            .withMiniMessage(this.miniMessageHandler);
      }

      public ItemStack deserialize() {
         Objects.requireNonNull(this.config, "Config not set");
         this.handleMaterial();
         this.handleDamage();
         this.getOrCreateMeta();
         this.handleDurability();
         this.displayName();
         this.itemName();
         this.unbreakable();
         this.customModelData();
         this.lore();
         this.enchants();
         this.itemFlags();
         this.attributes();
         this.legacySpawnEgg();
         XItemStack.recursiveMetaHandle(this, this.meta.getClass(), this.meta, XItemStack.DESERIALIZE_META_HANDLERS, null);
         this.item.setItemMeta(this.meta);
         return this.item;
      }

      private void attributes() {
         if (XMaterial.supports(1, 13)) {
            ConfigurationSection attributes = this.config.getConfigurationSection("attributes");
            if (attributes != null) {
               for (String attribute : attributes.getKeys(false)) {
                  Optional<XAttribute> attributeInst = XAttribute.of(attribute);
                  if (attributeInst.isPresent() && attributeInst.get().isSupported()) {
                     ConfigurationSection section = attributes.getConfigurationSection(attribute);
                     if (section != null) {
                        EquipmentSlot slot = section.getString("slot") != null
                           ? (EquipmentSlot)Enums.getIfPresent(EquipmentSlot.class, section.getString("slot")).or(EquipmentSlot.HAND)
                           : null;
                        String attrName = section.getString("name");
                        if (attrName == null) {
                           attrName = UUID.randomUUID().toString().toLowerCase(Locale.ENGLISH);
                        }

                        AttributeModifier modifier = XAttribute.createModifier(
                           attrName,
                           section.getDouble("amount"),
                           (Operation)Enums.getIfPresent(Operation.class, section.getString("operation")).or(Operation.ADD_NUMBER),
                           slot
                        );
                        this.meta.addAttributeModifier(attributeInst.get().get(), modifier);
                     }
                  }
               }
            }

            if (!this.meta.getItemFlags().isEmpty() && XReflection.supports(1, 20, 6) && !this.meta.hasAttributeModifiers()) {
               this.meta
                  .addAttributeModifier(
                     XAttribute.ATTACK_DAMAGE.get(), XAttribute.createModifier("xseries:itemflagdummy", 0.0, Operation.MULTIPLY_SCALAR_1, null)
                  );
            }
         }
      }

      private void legacySpawnEgg() {
         if (!XMaterial.supports(1, 11)) {
            MaterialData data = this.item.getData();
            if (data instanceof SpawnEgg) {
               String creatureName = this.config.getString("creature");
               if (!Strings.isNullOrEmpty(creatureName)) {
                  SpawnEgg spawnEgg = (SpawnEgg)data;
                  com.google.common.base.Optional<EntityType> creature = Enums.getIfPresent(EntityType.class, creatureName.toUpperCase(Locale.ENGLISH));
                  if (creature.isPresent()) {
                     spawnEgg.setSpawnedType((EntityType)creature.get());
                  }

                  this.item.setData(data);
               }
            }
         }
      }

      private void unbreakable() {
         if (XItemStack.SUPPORTS_UNBREAKABLE && this.config.isSet("unbreakable")) {
            this.meta.setUnbreakable(this.config.getBoolean("unbreakable"));
         }
      }

      private void customModelData() {
         if (XItemStack.SUPPORTS_ITEM_MODEL) {
            String itemModel = this.config.getString("item-model");
            if (itemModel != null && !itemModel.isEmpty()) {
               this.meta.setItemModel(NamespacedKey.fromString(itemModel));
            }
         }

         if (XItemStack.SUPPORTS_ADVANCED_CUSTOM_MODEL_DATA) {
            CustomModelDataComponent customModelData = this.meta.getCustomModelDataComponent();
            ConfigurationSection detailed = this.config.getConfigurationSection("custom-model-data");
            if (detailed != null) {
               customModelData.setStrings(XItemStack.parseRawOrList("string", "strings", detailed, x -> x));
               customModelData.setFlags(XItemStack.parseRawOrList("flag", "flags", detailed, Boolean::parseBoolean));
               customModelData.setFloats(XItemStack.parseRawOrList("float", "floats", detailed, Float::parseFloat));
               customModelData.setColors(
                  XItemStack.parseRawOrList(
                     "color",
                     "colors",
                     detailed,
                     x -> XItemStack.parseColor(x).orElseThrow(() -> new IllegalArgumentException("Unknown color for custom model data: " + x))
                  )
               );
            } else {
               List<String> listed = this.config.getStringList("custom-model-data");
               if (!listed.isEmpty()) {
                  String modelData = listed.get(0);
                  if (modelData != null && !modelData.isEmpty()) {
                     if (XItemStack.tryNumber(modelData, Float::parseFloat) != null) {
                        customModelData.setFloats(listed.stream().map(Float::parseFloat).collect(Collectors.toList()));
                     } else {
                        Optional<Color> color = XItemStack.parseColor(modelData);
                        if (color.isPresent()) {
                           customModelData.setColors(
                              listed.stream()
                                 .map(XItemStack::parseColor)
                                 .map(x -> x.orElseThrow(() -> new IllegalArgumentException("Unknown color for custom model data: " + x)))
                                 .collect(Collectors.toList())
                           );
                        } else {
                           customModelData.setStrings(listed);
                        }
                     }
                  }
               } else {
                  String modelData = this.config.getString("custom-model-data");
                  if (modelData != null && !modelData.isEmpty()) {
                     Float floatId = XItemStack.tryNumber(modelData, Float::parseFloat);
                     if (floatId != null) {
                        customModelData.setFloats(Collections.singletonList(floatId));
                     } else {
                        Optional<Color> color = XItemStack.parseColor(modelData);
                        if (color.isPresent()) {
                           customModelData.setColors(Collections.singletonList(color.get()));
                        } else {
                           customModelData.setStrings(Collections.singletonList(modelData));
                        }
                     }
                  }
               }
            }

            if (!customModelData.getColors().isEmpty()
               || !customModelData.getStrings().isEmpty()
               || !customModelData.getFlags().isEmpty()
               || !customModelData.getFloats().isEmpty()) {
               this.meta.setCustomModelDataComponent(customModelData);
            }
         } else if (XItemStack.SUPPORTS_CUSTOM_MODEL_DATA) {
            String modelData = this.config.getString("custom-model-data");
            if (modelData != null && !modelData.isEmpty()) {
               Integer intId = XItemStack.tryNumber(modelData, Integer::parseInt);
               if (intId != null) {
                  this.meta.setCustomModelData(intId);
               }
            }
         }
      }

      private void displayName() {
         String name = this.config.getString("name");
         if (!Strings.isNullOrEmpty(name)) {
            if (this.miniMessageHandler != null) {
               AdventureAPIFactory.setDisplayName(this.meta, this.miniMessageHandler.apply(Collections.singletonList(name)).get(0));
               return;
            }

            String translated = this.translator.apply(name);
            this.meta.setDisplayName(translated);
         } else if (name != null && name.isEmpty()) {
            this.meta.setDisplayName(" ");
         }
      }

      private void itemName() {
         if (XItemStack.SUPPORTS_ITEM_NAME) {
            String itemName = this.config.getString("item-name");
            if (!Strings.isNullOrEmpty(itemName)) {
               String translated = this.translator.apply(itemName);
               this.meta.setItemName(translated);
            } else if (itemName != null && itemName.isEmpty()) {
               this.meta.setItemName(" ");
            }
         }
      }

      private void itemFlags() {
         List<String> flags = this.config.getStringList("flags");
         if (!flags.isEmpty()) {
            for (String flag : flags) {
               flag = flag.toUpperCase(Locale.ENGLISH);
               if (flag.equals("ALL")) {
                  XItemFlag.decorationOnly(this.meta);
                  break;
               }

               XItemFlag.of(flag).ifPresent(itemFlag -> itemFlag.set(this.meta));
            }
         } else {
            String allFlags = this.config.getString("flags");
            if (!Strings.isNullOrEmpty(allFlags) && allFlags.equalsIgnoreCase("ALL")) {
               XItemFlag.decorationOnly(this.meta);
            }
         }
      }

      private void handleEnchantmentStorageMeta(EnchantmentStorageMeta meta) {
         ConfigurationSection enchantment = this.config.getConfigurationSection("stored-enchants");
         if (enchantment != null) {
            for (String ench : enchantment.getKeys(false)) {
               Optional<XEnchantment> enchant = XEnchantment.of(ench);
               enchant.ifPresent(xEnchantment -> meta.addStoredEnchant(xEnchantment.get(), enchantment.getInt(ench), true));
            }
         }
      }

      private void enchants() {
         ConfigurationSection enchants = this.config.getConfigurationSection("enchants");
         if (enchants != null) {
            for (String ench : enchants.getKeys(false)) {
               Optional<XEnchantment> enchant = XEnchantment.of(ench);
               enchant.ifPresent(xEnchantment -> this.meta.addEnchant(xEnchantment.get(), enchants.getInt(ench), true));
            }
         } else if (this.config.getBoolean("glow")) {
            this.meta.addEnchant(XEnchantment.UNBREAKING.get(), 1, false);
            XItemFlag.HIDE_ENCHANTS.set(this.meta);
         }
      }

      private void lore() {
         if (this.config.isSet("lore")) {
            List<String> lores = this.config.getStringList("lore");
            List<String> translatedLore;
            if (!lores.isEmpty()) {
               if (this.miniMessageHandler != null) {
                  AdventureAPIFactory.setLore(this.meta, this.miniMessageHandler.apply(lores));
                  return;
               }

               translatedLore = new ArrayList<>(lores.size());

               for (String lore : lores) {
                  if (lore.isEmpty()) {
                     translatedLore.add(" ");
                  } else {
                     for (String singleLore : XItemStack.splitNewLine(lore)) {
                        if (singleLore.isEmpty()) {
                           translatedLore.add(" ");
                        } else {
                           translatedLore.add(this.translator.apply(singleLore));
                        }
                     }
                  }
               }
            } else {
               String lore = this.config.getString("lore");
               translatedLore = new ArrayList<>(10);
               if (!Strings.isNullOrEmpty(lore)) {
                  if (this.miniMessageHandler != null) {
                     AdventureAPIFactory.setLore(this.meta, this.miniMessageHandler.apply(Collections.singletonList(lore)));
                     return;
                  }

                  for (String singleLore : XItemStack.splitNewLine(lore)) {
                     if (singleLore.isEmpty()) {
                        translatedLore.add(" ");
                     } else {
                        translatedLore.add(this.translator.apply(singleLore));
                     }
                  }
               }
            }

            this.meta.setLore(translatedLore);
         }
      }

      private void handleSpawnEggMeta(SpawnEggMeta spawnEgg) {
         String creatureName = this.config.getString("creature");
         if (!Strings.isNullOrEmpty(creatureName)) {
            com.google.common.base.Optional<EntityType> creature = Enums.getIfPresent(EntityType.class, creatureName.toUpperCase(Locale.ENGLISH));
            if (creature.isPresent()) {
               spawnEgg.setSpawnedType((EntityType)creature.get());
            }
         }
      }

      private void handleTropicalFishBucketMeta(TropicalFishBucketMeta tropical) {
         DyeColor color = (DyeColor)Enums.getIfPresent(DyeColor.class, this.config.getString("color")).or(DyeColor.WHITE);
         DyeColor patternColor = (DyeColor)Enums.getIfPresent(DyeColor.class, this.config.getString("pattern-color")).or(DyeColor.WHITE);
         Pattern pattern = (Pattern)Enums.getIfPresent(Pattern.class, this.config.getString("pattern")).or(Pattern.BETTY);
         tropical.setBodyColor(color);
         tropical.setPatternColor(patternColor);
         tropical.setPattern(pattern);
      }

      private void handleCrossbowMeta(CrossbowMeta crossbow) {
         ConfigurationSection projectiles = this.config.getConfigurationSection("projectiles");
         if (projectiles != null) {
            for (String projectile : projectiles.getKeys(false)) {
               ItemStack projectileItem = this.copySettings().fromConfig(this.config.getConfigurationSection("projectiles." + projectile)).deserialize();
               crossbow.addChargedProjectile(projectileItem);
            }
         }
      }

      private void handleSuspiciousStewMeta(SuspiciousStewMeta stew) {
         for (String effects : this.config.getStringList("effects")) {
            XPotion.Effect effect = XPotion.parseEffect(effects);
            if (effect.hasChance()) {
               stew.addCustomEffect(effect.getEffect(), true);
            }
         }
      }

      private void handleCompassMeta(CompassMeta compass) {
         compass.setLodestoneTracked(this.config.getBoolean("tracked"));
         ConfigurationSection lodestone = this.config.getConfigurationSection("lodestone");
         if (lodestone != null) {
            World world = Bukkit.getWorld(lodestone.getString("world"));
            double x = lodestone.getDouble("x");
            double y = lodestone.getDouble("y");
            double z = lodestone.getDouble("z");
            compass.setLodestone(new Location(world, x, y, z));
         }
      }

      private void handleAxolotlBucketMeta(AxolotlBucketMeta bucket) {
         String variantStr = this.config.getString("color");
         if (variantStr != null) {
            Variant variant = (Variant)Enums.getIfPresent(Variant.class, variantStr.toUpperCase(Locale.ENGLISH)).or(Variant.BLUE);
            bucket.setVariant(variant);
         }
      }

      private void handleArmorMeta(ArmorMeta armor) {
         ConfigurationSection trim = this.config.getConfigurationSection("trim");
         if (trim != null) {
            TrimMaterial trimMaterial = (TrimMaterial)Registry.TRIM_MATERIAL.get(NamespacedKey.fromString(trim.getString("material")));
            TrimPattern trimPattern = (TrimPattern)Registry.TRIM_PATTERN.get(NamespacedKey.fromString(trim.getString("pattern")));
            armor.setTrim(new ArmorTrim(trimMaterial, trimPattern));
         }
      }

      private void handleMapMeta(MapMeta map) {
         ConfigurationSection mapSection = this.config.getConfigurationSection("map");
         if (mapSection != null) {
            map.setScaling(mapSection.getBoolean("scaling"));
            if (XMaterial.supports(1, 11)) {
               if (mapSection.isSet("location")) {
                  map.setLocationName(mapSection.getString("location"));
               }

               if (mapSection.isSet("color")) {
                  XItemStack.parseColor(mapSection.getString("color")).ifPresent(map::setColor);
               }
            }

            if (XMaterial.supports(1, 14)) {
               ConfigurationSection view = mapSection.getConfigurationSection("view");
               if (view != null) {
                  World world = Bukkit.getWorld(view.getString("world"));
                  if (world != null) {
                     MapView mapView = Bukkit.createMap(world);
                     mapView.setWorld(world);
                     mapView.setScale((Scale)Enums.getIfPresent(Scale.class, view.getString("scale")).or(Scale.NORMAL));
                     mapView.setLocked(view.getBoolean("locked"));
                     mapView.setTrackingPosition(view.getBoolean("tracking-position"));
                     mapView.setUnlimitedTracking(view.getBoolean("unlimited-tracking"));
                     ConfigurationSection centerSection = view.getConfigurationSection("center");
                     if (centerSection != null) {
                        mapView.setCenterX(centerSection.getInt("x"));
                        mapView.setCenterZ(centerSection.getInt("z"));
                     }

                     map.setMapView(mapView);
                  }
               }
            }
         }
      }

      private void handleBookMeta(BookMeta book) {
         ConfigurationSection bookInfo = this.config.getConfigurationSection("book");
         if (bookInfo != null) {
            book.setTitle(bookInfo.getString("title"));
            book.setAuthor(bookInfo.getString("author"));
            book.setPages(bookInfo.getStringList("pages"));
            if (XMaterial.supports(1, 9)) {
               String generationValue = bookInfo.getString("generation");
               if (generationValue != null) {
                  Generation generation = (Generation)Enums.getIfPresent(Generation.class, generationValue).orNull();
                  book.setGeneration(generation);
               }
            }
         }
      }

      private void handleFireworkMeta(FireworkMeta firework) {
         firework.setPower(this.config.getInt("power"));
         ConfigurationSection fireworkSection = this.config.getConfigurationSection("firework");
         if (fireworkSection != null) {
            Builder builder = FireworkEffect.builder();

            for (String fws : fireworkSection.getKeys(false)) {
               ConfigurationSection fw = this.config.getConfigurationSection("firework." + fws);
               builder.flicker(fw.getBoolean("flicker"));
               builder.trail(fw.getBoolean("trail"));
               builder.with((Type)Enums.getIfPresent(Type.class, fw.getString("type").toUpperCase(Locale.ENGLISH)).or(Type.STAR));
               ConfigurationSection colorsSection = fw.getConfigurationSection("colors");
               if (colorsSection != null) {
                  List<String> fwColors = colorsSection.getStringList("base");
                  List<Color> colors = new ArrayList<>(fwColors.size());

                  for (String colorStr : fwColors) {
                     Optional<Color> color = XItemStack.parseColor(colorStr);
                     if (color.isPresent()) {
                        colors.add(color.get());
                     }
                  }

                  builder.withColor(colors);
                  fwColors = colorsSection.getStringList("fade");
                  colors = new ArrayList<>(fwColors.size());

                  for (String colorStr : fwColors) {
                     Optional<Color> color = XItemStack.parseColor(colorStr);
                     if (color.isPresent()) {
                        colors.add(color.get());
                     }
                  }

                  builder.withFade(colors);
               }

               firework.addEffect(builder.build());
            }
         }
      }

      private void handleBlockStateMeta(BlockStateMeta bsm) {
         BlockState state = XItemStack.safeBlockState(bsm);
         if (state instanceof CreatureSpawner) {
            CreatureSpawner spawner = (CreatureSpawner)state;
            String spawnerStr = this.config.getString("spawner");
            if (!Strings.isNullOrEmpty(spawnerStr)) {
               spawner.setSpawnedType((EntityType)Enums.getIfPresent(EntityType.class, spawnerStr.toUpperCase(Locale.ENGLISH)).orNull());
               spawner.update(true);
               bsm.setBlockState(spawner);
            }
         } else if (XMaterial.supports(1, 11) && state instanceof ShulkerBox) {
            ConfigurationSection shulkerSection = this.config.getConfigurationSection("contents");
            if (shulkerSection != null) {
               ShulkerBox box = (ShulkerBox)state;

               for (String key : shulkerSection.getKeys(false)) {
                  ItemStack boxItem = this.copySettings().fromConfig(shulkerSection.getConfigurationSection(key)).deserialize();
                  int slot = XItemStack.toInt(key, 0);
                  box.getInventory().setItem(slot, boxItem);
               }

               box.update(true);
               bsm.setBlockState(box);
            }
         } else if (state instanceof Banner) {
            Banner banner = (Banner)state;
            ConfigurationSection patterns = this.config.getConfigurationSection("patterns");
            if (!XMaterial.supports(1, 14)) {
               banner.setBaseColor(DyeColor.WHITE);
            }

            if (patterns != null) {
               for (String pattern : patterns.getKeys(false)) {
                  Optional<XPatternType> patternType = XPatternType.of(pattern);
                  if (patternType.isPresent() && patternType.get().isSupported()) {
                     DyeColor color = (DyeColor)Enums.getIfPresent(DyeColor.class, patterns.getString(pattern).toUpperCase(Locale.ENGLISH)).or(DyeColor.WHITE);
                     banner.addPattern(new org.bukkit.block.banner.Pattern(color, patternType.get().get()));
                  }
               }

               banner.update(true);
               bsm.setBlockState(banner);
            }
         }
      }

      private void handlePotionMeta(ItemMeta meta) {
         if (XMaterial.supports(1, 9)) {
            PotionMeta potion = (PotionMeta)meta;

            for (String effects : this.config.getStringList("effects")) {
               XPotion.Effect effect = XPotion.parseEffect(effects);
               if (effect.hasChance()) {
                  potion.addCustomEffect(effect.getEffect(), true);
               }
            }

            String baseType = this.config.getString("base-type");
            if (!Strings.isNullOrEmpty(baseType)) {
               boolean isNewType = !baseType.contains(",");
               if (SUPPORTS_PotionMeta_setBasePotionType) {
                  if (isNewType) {
                     PotionType potionType;
                     try {
                        potionType = PotionType.valueOf(baseType);
                     } catch (IllegalArgumentException ex) {
                        potionType = PotionType.AWKWARD;
                     }

                     potion.setBasePotionType(potionType);
                  } else {
                     String[] components = baseType.split(",");
                     XPotion effect = XPotion.of(components[0]).orElse(XPotion.HEALTH_BOOST);
                     boolean extended = Boolean.parseBoolean(components[1]);
                     boolean upgraded = Boolean.parseBoolean(components[2]);
                     String potionTypeStr = effect.getPotionType().name();
                     if (extended) {
                        potionTypeStr = "STRONG_" + potionTypeStr;
                     } else if (upgraded) {
                        potionTypeStr = "LONG_" + potionTypeStr;
                     }

                     PotionType potionType;
                     try {
                        potionType = PotionType.valueOf(potionTypeStr);
                     } catch (IllegalArgumentException ex) {
                        potionType = PotionType.AWKWARD;
                     }

                     potion.setBasePotionType(potionType);
                  }
               } else {
                  boolean extended = false;
                  boolean upgraded = false;
                  PotionType effect;
                  if (isNewType) {
                     if (baseType.startsWith("STRONG_")) {
                        extended = true;
                        baseType = baseType.substring(7);
                     } else if (baseType.startsWith("LONG_")) {
                        upgraded = true;
                        baseType = baseType.substring(5);
                     }

                     effect = XPotion.of(baseType).orElse(XPotion.HEALTH_BOOST).getPotionType();
                  } else {
                     String[] components = baseType.split(",");
                     effect = XPotion.of(components[0]).orElse(XPotion.HEALTH_BOOST).getPotionType();
                     extended = Boolean.parseBoolean(components[1]);
                     upgraded = Boolean.parseBoolean(components[2]);
                  }

                  potion.setBasePotionData(new PotionData(effect, extended, upgraded));
               }
            }

            if (XItemStack.SUPPORTS_POTION_COLOR && this.config.contains("color")) {
               potion.setColor(Color.fromRGB(this.config.getInt("color")));
            }
         }
      }

      private void handleLeatherArmorMeta(LeatherArmorMeta leather) {
         String colorStr = this.config.getString("color");
         if (colorStr != null) {
            XItemStack.parseColor(colorStr).ifPresent(leather::setColor);
         }
      }

      private void handleBannerMeta(BannerMeta banner) {
         ConfigurationSection patterns = this.config.getConfigurationSection("patterns");
         if (patterns != null) {
            for (String pattern : patterns.getKeys(false)) {
               Optional<XPatternType> patternType = XPatternType.of(pattern);
               if (patternType.isPresent() && patternType.get().isSupported()) {
                  DyeColor color = (DyeColor)Enums.getIfPresent(DyeColor.class, patterns.getString(pattern).toUpperCase(Locale.ENGLISH)).or(DyeColor.WHITE);
                  banner.addPattern(new org.bukkit.block.banner.Pattern(color, patternType.get().get()));
               }
            }
         }
      }

      private void handleSkullMeta(SkullMeta meta) {
         String skull = this.config.getString("skull");
         if (skull != null) {
            if (skull.isEmpty()) {
               XSkull.of(meta).profile(Profileable.detect(skull)).removeProfile();
            } else {
               XSkull.of(meta).profile(Profileable.detect(skull)).lenient().apply();
            }
         }
      }

      private void handleDurability() {
         if (XMaterial.supports(1, 13)) {
            if (this.meta instanceof Damageable) {
               int damage = this.config.getInt("damage");
               if (damage > 0) {
                  ((Damageable)this.meta).setDamage(damage);
               }
            }
         } else {
            int damage = this.config.getInt("damage");
            if (damage > 0) {
               this.item.setDurability((short)damage);
            }
         }
      }

      private void handleDamage() {
         int amount = this.config.getInt("amount");
         if (amount > 1) {
            this.item.setAmount(amount);
         }
      }

      private void getOrCreateMeta() {
         this.meta = this.item.getItemMeta();
         if (this.meta == null) {
            this.meta = Bukkit.getItemFactory().getItemMeta(XMaterial.STONE.get());
         }
      }

      private void handleMaterial() {
         String materialName = this.config.getString("material");
         XMaterial material = null;
         if (!Strings.isNullOrEmpty(materialName)) {
            Optional<XMaterial> materialOpt = XMaterial.matchXMaterial(materialName);
            if (materialOpt.isPresent()) {
               material = materialOpt.get();
            } else {
               material = this.solutionOrThrow(new XItemStack.UnknownMaterialCondition(materialName));
            }

            if (!material.isSupported()) {
               material = this.solutionOrThrow(
                  new XItemStack.UnAcceptableMaterialCondition(material, XItemStack.UnAcceptableMaterialCondition.Reason.UNSUPPORTED)
               );
            }

            if (XTag.INVENTORY_NOT_DISPLAYABLE.isTagged(material)) {
               material = this.solutionOrThrow(
                  new XItemStack.UnAcceptableMaterialCondition(material, XItemStack.UnAcceptableMaterialCondition.Reason.NOT_DISPLAYABLE)
               );
            }
         } else {
            String skull = this.config.getString("skull");
            if (skull != null) {
               material = XMaterial.PLAYER_HEAD;
            }
         }

         if (material == null) {
            material = this.solutionOrThrow(new XItemStack.UnsetMaterialCondition());
         }

         if (this.item == null) {
            this.item = material.parseItem();
         } else {
            material.setType(this.item);
         }
      }

      private XMaterial solutionOrThrow(XItemStack.MaterialCondition condition) {
         if (this.restart == null) {
            throw condition;
         } else {
            this.restart.accept(condition);
            if (condition.hasSolution()) {
               return condition.solution;
            } else {
               throw condition;
            }
         }
      }
   }

   public static class MaterialCondition extends RuntimeException {
      protected XMaterial solution;

      public MaterialCondition(String message) {
         super(message);
      }

      public void setSolution(XMaterial solution) {
         this.solution = solution;
      }

      public boolean hasSolution() {
         return this.solution != null;
      }
   }

   private interface MetaHandler<M extends ItemMeta> {
      void handle(M var1);
   }

   private abstract static class SerialObject {
      @Nullable
      protected ItemStack item;
      @Nullable
      protected ConfigurationSection config;
      @Nullable
      protected Function<String, String> translator = Function.identity();
      protected ItemMeta meta;

      private SerialObject() {
      }

      public abstract XItemStack.SerialObject copy();

      public abstract XItemStack.SerialObject copySettings();

      public XItemStack.SerialObject withTranslator(Function<String, String> translator) {
         this.translator = translator;
         return this;
      }
   }

   public static final class Serializer extends XItemStack.SerialObject {
      private Function<List<? extends Component>, List<String>> miniMessageHandler;
      private static final boolean SUPPORTS_PotionMeta_getBasePotionType = XReflection.of(PotionMeta.class)
         .method("public org.bukkit.potion.PotionType getBasePotionType()")
         .exists();
      private static final boolean SUPPORTS_PotionEffectType_getKey = XReflection.of(PotionEffectType.class)
         .method("public org.bukkit.NamespacedKey getKey()")
         .exists();

      private Serializer() {
      }

      public XItemStack.Serializer fromItem(ItemStack item) {
         this.item = Objects.requireNonNull(item, "Cannot serialize null item");
         return this;
      }

      public XItemStack.Serializer toConfig(ConfigurationSection config) {
         this.config = config;
         return this;
      }

      public XItemStack.Serializer withTranslator(Function<String, String> translator) {
         super.withTranslator(translator);
         return this;
      }

      @Experimental
      public XItemStack.Serializer withMiniMessage(Function<List<? extends Component>, List<String>> miniMessageHandler) {
         this.miniMessageHandler = miniMessageHandler;
         return this;
      }

      @NotNull
      public Map<String, Object> writeToMap() {
         if (this.config == null) {
            this.config = new MemoryConfiguration();
         }

         this.serialize();
         return XItemStack.configSectionToMap(this.config);
      }

      public XItemStack.Serializer copy() {
         return this.copySettings().fromItem(this.item == null ? null : this.item.clone());
      }

      public XItemStack.Serializer copySettings() {
         return new XItemStack.Serializer().toConfig(this.config).withTranslator(this.translator).withMiniMessage(this.miniMessageHandler);
      }

      public void serialize() {
         Objects.requireNonNull(this.item, "Item not set");
         Objects.requireNonNull(this.config, "Config not set");
         this.config.set("material", XMaterial.matchXMaterial(this.item).name());
         if (this.item.getAmount() > 1) {
            this.config.set("amount", this.item.getAmount());
         }

         if (this.item.hasItemMeta()) {
            this.meta = this.item.getItemMeta();
            if (this.meta != null) {
               this.handleDurability(this.meta);
               if (this.meta.hasDisplayName()) {
                  if (this.miniMessageHandler != null) {
                     this.config.set("name", AdventureAPIFactory.displayName(this.meta, this.miniMessageHandler));
                  } else {
                     this.config.set("name", this.translator.apply(this.meta.getDisplayName()));
                  }
               }

               if (this.meta.hasLore()) {
                  if (this.miniMessageHandler != null) {
                     this.config.set("lore", AdventureAPIFactory.lore(this.meta, this.miniMessageHandler));
                  } else {
                     this.config.set("lore", this.meta.getLore().stream().map(this.translator).collect(Collectors.toList()));
                  }
               }

               if (XItemStack.SUPPORTS_ITEM_NAME && this.meta.hasItemName()) {
                  String itemName = this.meta.getItemName();
                  this.config.set("item-name", this.translator.apply(itemName));
               }

               this.customModelData();
               if (XItemStack.SUPPORTS_UNBREAKABLE && this.meta.isUnbreakable()) {
                  this.config.set("unbreakable", true);
               }

               this.handleEnchants();
               this.handleItemFlags(this.meta);
               this.handleAttributes(this.meta);
               this.legacySpawnEgg();
               XItemStack.recursiveMetaHandle(this, this.meta.getClass(), this.meta, XItemStack.SERIALIZE_META_HANDLERS, null);
            }
         }
      }

      private void customModelData() {
         if (XItemStack.SUPPORTS_ITEM_MODEL && this.meta.hasItemModel()) {
            this.config.set("item-model", this.meta.getItemModel().toString());
         }

         if (XItemStack.SUPPORTS_CUSTOM_MODEL_DATA) {
            if (XItemStack.SUPPORTS_ADVANCED_CUSTOM_MODEL_DATA) {
               CustomModelDataComponent customModelData = this.meta.getCustomModelDataComponent();
               List<String> strings = customModelData.getStrings();
               List<Float> floats = customModelData.getFloats();
               List<Boolean> flags = customModelData.getFlags();
               List<Color> colors = customModelData.getColors();
               int idCount = (int)Stream.of(strings, floats, flags, colors).filter(x -> !x.isEmpty()).count();
               if (idCount == 0) {
                  return;
               }

               if (idCount == 1) {
                  if (!strings.isEmpty()) {
                     this.config.set("custom-model-data", singleOrList(strings));
                  }

                  if (!floats.isEmpty()) {
                     this.config.set("custom-model-data", singleOrList(floats));
                  }

                  if (!flags.isEmpty()) {
                     this.config.set("custom-model-data", singleOrList(flags));
                  }

                  if (!colors.isEmpty()) {
                     this.config.set("custom-model-data", singleOrList(colors.stream().map(XItemStack.Serializer::colorString).collect(Collectors.toList())));
                  }
               } else {
                  ConfigurationSection cfgCustomModelData = this.config.createSection("custom-model-data");
                  if (!strings.isEmpty()) {
                     cfgCustomModelData.set("strings", strings);
                  }

                  if (!floats.isEmpty()) {
                     cfgCustomModelData.set("floats", floats);
                  }

                  if (!flags.isEmpty()) {
                     cfgCustomModelData.set("flags", flags);
                  }

                  if (!colors.isEmpty()) {
                     cfgCustomModelData.set("colors", colors.stream().map(XItemStack.Serializer::colorString).collect(Collectors.toList()));
                  }
               }
            } else if (this.meta.hasCustomModelData()) {
               this.config.set("custom-model-data", this.meta.getCustomModelData());
            }
         }
      }

      private static <T> Object singleOrList(List<T> list) {
         return list.size() == 1 ? list.get(0) : list;
      }

      private void legacySpawnEgg() {
         if (!XMaterial.supports(1, 11)) {
            MaterialData data = this.item.getData();
            if (data instanceof SpawnEgg) {
               SpawnEgg spawnEgg = (SpawnEgg)data;
               this.config.set("creature", spawnEgg.getSpawnedType().getName());
            }
         }
      }

      private void handleSpawnEggMeta(SpawnEggMeta spawnEgg) {
         this.config.set("creature", spawnEgg.getSpawnedType().getName());
      }

      private void handleSuspiciousStewMeta(SuspiciousStewMeta stew) {
         List<PotionEffect> customEffects = stew.getCustomEffects();
         List<String> effects = new ArrayList<>(customEffects.size());

         for (PotionEffect effect : customEffects) {
            effects.add(XPotion.of(effect.getType()).name() + ", " + effect.getDuration() + ", " + effect.getAmplifier());
         }

         this.config.set("effects", effects);
      }

      private void handleTropicalFishBucketMeta(TropicalFishBucketMeta tropical) {
         this.config.set("pattern", tropical.getPattern().name());
         this.config.set("color", tropical.getBodyColor().name());
         this.config.set("pattern-color", tropical.getPatternColor().name());
      }

      private void handleCrossbowMeta(CrossbowMeta crossbow) {
         int i = 0;

         for (ItemStack projectiles : crossbow.getChargedProjectiles()) {
            this.copySettings().fromItem(projectiles).toConfig(this.config.getConfigurationSection("projectiles." + i)).serialize();
            i++;
         }
      }

      private void handleCompassMeta(CompassMeta compass) {
         ConfigurationSection subSection = this.config.createSection("lodestone");
         subSection.set("tracked", compass.isLodestoneTracked());
         if (compass.hasLodestone()) {
            Location location = compass.getLodestone();
            subSection.set("location.world", location.getWorld().getName());
            subSection.set("location.x", location.getX());
            subSection.set("location.y", location.getY());
            subSection.set("location.z", location.getZ());
         }
      }

      private void handleAxolotlBucketMeta(AxolotlBucketMeta bucket) {
         if (bucket.hasVariant()) {
            this.config.set("color", bucket.getVariant().toString());
         }
      }

      private void handleArmorMeta(ArmorMeta armorMeta) {
         if (armorMeta.hasTrim()) {
            ArmorTrim trim = armorMeta.getTrim();
            ConfigurationSection trimConfig = this.config.createSection("trim");
            trimConfig.set("material", trim.getMaterial().getKey().getNamespace() + ':' + trim.getMaterial().getKey().getKey());
            trimConfig.set("pattern", trim.getPattern().getKey().getNamespace() + ':' + trim.getPattern().getKey().getKey());
         }
      }

      private void handleMapMeta(MapMeta map) {
         ConfigurationSection mapSection = this.config.createSection("map");
         mapSection.set("scaling", map.isScaling());
         if (XMaterial.supports(1, 11)) {
            if (map.hasLocationName()) {
               mapSection.set("location", map.getLocationName());
            }

            if (map.hasColor()) {
               Color color = map.getColor();
               mapSection.set("color", colorString(color));
            }
         }

         if (XMaterial.supports(1, 14) && map.hasMapView()) {
            MapView mapView = map.getMapView();
            ConfigurationSection view = mapSection.createSection("view");
            view.set("scale", mapView.getScale().toString());
            view.set("world", mapView.getWorld().getName());
            ConfigurationSection centerSection = view.createSection("center");
            centerSection.set("x", mapView.getCenterX());
            centerSection.set("z", mapView.getCenterZ());
            view.set("locked", mapView.isLocked());
            view.set("tracking-position", mapView.isTrackingPosition());
            view.set("unlimited-tracking", mapView.isUnlimitedTracking());
         }
      }

      private void handleBookMeta(BookMeta book) {
         if (book.getTitle() != null || book.getAuthor() != null || book.getGeneration() != null || !book.getPages().isEmpty()) {
            ConfigurationSection bookInfo = this.config.createSection("book");
            if (book.getTitle() != null) {
               bookInfo.set("title", book.getTitle());
            }

            if (book.getAuthor() != null) {
               bookInfo.set("author", book.getAuthor());
            }

            if (XMaterial.supports(1, 9)) {
               Generation generation = book.getGeneration();
               if (generation != null) {
                  bookInfo.set("generation", book.getGeneration().toString());
               }
            }

            if (!book.getPages().isEmpty()) {
               bookInfo.set("pages", book.getPages());
            }
         }
      }

      private void handleFireworkMeta(FireworkMeta firework) {
         this.config.set("power", firework.getPower());
         int i = 0;

         for (FireworkEffect fw : firework.getEffects()) {
            this.config.set("firework." + i + ".type", fw.getType().name());
            ConfigurationSection fwc = this.config.getConfigurationSection("firework." + i);
            fwc.set("flicker", fw.hasFlicker());
            fwc.set("trail", fw.hasTrail());
            List<Color> fwBaseColors = fw.getColors();
            List<Color> fwFadeColors = fw.getFadeColors();
            List<String> baseColors = new ArrayList<>(fwBaseColors.size());
            List<String> fadeColors = new ArrayList<>(fwFadeColors.size());
            ConfigurationSection colors = fwc.createSection("colors");

            for (Color color : fwBaseColors) {
               baseColors.add(colorString(color));
            }

            colors.set("base", baseColors);

            for (Color color : fwFadeColors) {
               fadeColors.add(colorString(color));
            }

            colors.set("fade", fadeColors);
            i++;
         }
      }

      @NotNull
      private static String colorString(Color color) {
         return color.getRed() + ", " + color.getGreen() + ", " + color.getBlue();
      }

      private void handlePotionMeta(PotionMeta meta) {
         if (XMaterial.supports(1, 9)) {
            if (SUPPORTS_PotionMeta_getBasePotionType) {
               PotionType basePotionType = meta.getBasePotionType();
               if (basePotionType != null) {
                  this.config.set("base-type", basePotionType.name());
               }
            } else {
               PotionData potionData = meta.getBasePotionData();
               this.config.set("base-effect", potionData.getType().name() + ", " + potionData.isExtended() + ", " + potionData.isUpgraded());
            }

            List<PotionEffect> customEffects = meta.getCustomEffects();
            if (!customEffects.isEmpty()) {
               this.config.set("effects", customEffects.stream().map(x -> {
                  String typeStr;
                  if (SUPPORTS_PotionEffectType_getKey) {
                     NamespacedKey type = x.getType().getKey();
                     typeStr = type.getNamespace() + ':' + type.getKey();
                  } else {
                     typeStr = x.getType().getName();
                  }

                  int seconds = x.getDuration() / 20;
                  int level = x.getAmplifier() + 1;
                  return typeStr + ", " + seconds + ", " + level;
               }).collect(Collectors.toList()));
            }

            if (XItemStack.SUPPORTS_POTION_COLOR && meta.hasColor()) {
               this.config.set("color", meta.getColor().asRGB());
            }
         }
      }

      private void handleLeatherArmorMeta(LeatherArmorMeta meta) {
         Color color = meta.getColor();
         this.config.set("color", colorString(color));
      }

      private void handleBannerMeta(BannerMeta meta) {
         ConfigurationSection patterns = this.config.createSection("patterns");

         for (org.bukkit.block.banner.Pattern pattern : meta.getPatterns()) {
            patterns.set(XPatternType.of(pattern.getPattern()).name(), pattern.getColor().name());
         }
      }

      private void handleSkullMeta(ItemMeta meta) {
         String skull = XSkull.of(meta).getProfileValue();
         if (skull != null) {
            this.config.set("skull", skull);
         }
      }

      private void handleEnchantmentStorageMeta(EnchantmentStorageMeta meta) {
         for (Entry<Enchantment, Integer> enchant : meta.getStoredEnchants().entrySet()) {
            String entry = "stored-enchants." + XEnchantment.of(enchant.getKey()).name();
            this.config.set(entry, enchant.getValue());
         }
      }

      private void handleBlockStateMeta(BlockStateMeta meta) {
         BlockState state = XItemStack.safeBlockState(meta);
         if (XMaterial.supports(1, 11) && state instanceof ShulkerBox) {
            ShulkerBox box = (ShulkerBox)state;
            ConfigurationSection shulker = this.config.createSection("contents");
            int i = 0;

            for (ItemStack itemInBox : box.getInventory().getContents()) {
               if (itemInBox != null) {
                  this.copySettings().fromItem(itemInBox).toConfig(shulker.createSection(Integer.toString(i))).serialize();
               }

               i++;
            }
         } else if (state instanceof CreatureSpawner) {
            CreatureSpawner cs = (CreatureSpawner)state;
            if (cs.getSpawnedType() != null) {
               this.config.set("spawner", cs.getSpawnedType().name());
            }
         }
      }

      private void handleAttributes(ItemMeta meta) {
         if (XMaterial.supports(1, 13)) {
            Multimap<Attribute, AttributeModifier> attributes = meta.getAttributeModifiers();
            if (attributes != null) {
               for (Entry<Attribute, AttributeModifier> attribute : attributes.entries()) {
                  String path = "attributes." + XAttribute.of(attribute.getKey()).name() + '.';
                  AttributeModifier modifier = attribute.getValue();
                  this.config.set(path + "name", modifier.getName());
                  this.config.set(path + "amount", modifier.getAmount());
                  this.config.set(path + "operation", modifier.getOperation().name());
                  if (modifier.getSlot() != null) {
                     this.config.set(path + "slot", modifier.getSlot().name());
                  }
               }
            }
         }
      }

      private void handleItemFlags(ItemMeta meta) {
         if (!meta.getItemFlags().isEmpty()) {
            Set<ItemFlag> flags = meta.getItemFlags();
            List<String> flagNames = new ArrayList<>(flags.size());

            for (ItemFlag flag : flags) {
               flagNames.add(flag.name());
            }

            this.config.set("flags", flagNames);
         }
      }

      private void handleEnchants() {
         for (Entry<Enchantment, Integer> enchant : this.meta.getEnchants().entrySet()) {
            String entry = "enchants." + XEnchantment.of(enchant.getKey()).name();
            this.config.set(entry, enchant.getValue());
         }
      }

      private void handleDurability(ItemMeta meta) {
         if (XMaterial.supports(1, 13)) {
            if (meta instanceof Damageable) {
               Damageable damageable = (Damageable)meta;
               if (damageable.hasDamage()) {
                  this.config.set("damage", damageable.getDamage());
               }
            }
         } else {
            this.config.set("damage", this.item.getDurability());
         }
      }
   }

   public static final class UnAcceptableMaterialCondition extends XItemStack.MaterialCondition {
      private final XMaterial material;
      private final XItemStack.UnAcceptableMaterialCondition.Reason reason;

      public UnAcceptableMaterialCondition(XMaterial material, XItemStack.UnAcceptableMaterialCondition.Reason reason) {
         super("Unacceptable material: " + material.name() + " (" + reason.name() + ')');
         this.material = material;
         this.reason = reason;
      }

      public XItemStack.UnAcceptableMaterialCondition.Reason getReason() {
         return this.reason;
      }

      public XMaterial getMaterial() {
         return this.material;
      }

      public enum Reason {
         UNSUPPORTED,
         NOT_DISPLAYABLE;

         Reason() {
         }
      }
   }

   public static final class UnknownMaterialCondition extends XItemStack.MaterialCondition {
      private final String material;

      public UnknownMaterialCondition(String material) {
         super("Unknown material: " + material);
         this.material = material;
      }

      public String getMaterial() {
         return this.material;
      }
   }

   public static final class UnsetMaterialCondition extends XItemStack.MaterialCondition {
      public UnsetMaterialCondition() {
         super("No material is not set or could be derived");
      }
   }
}
