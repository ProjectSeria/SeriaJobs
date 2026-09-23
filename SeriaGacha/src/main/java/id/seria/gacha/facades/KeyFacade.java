package id.seria.gacha.facades;

import id.seria.gacha.Crates;
import id.seria.gacha.api.events.items.ItemDeliverySource;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.managers.players.PlayerData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KeyFacade {
   public static final int LITE_LINK_LIMIT = 1;
   private static final Set<Material> cachedKeysMaterial = new HashSet<>();

   public KeyFacade() {
   }

   public static Set<Material> getKeysMaterial() {
      if (cachedKeysMaterial.size() != Crates.getKeysManager().countRegisteredKeys()) {
         cachedKeysMaterial.clear();
         cachedKeysMaterial.addAll(Crates.getKeysManager().getRegisteredKeys().stream().map(r -> r.getPlainItem().getType()).collect(Collectors.toSet()));
      }

      return cachedKeysMaterial;
   }

   public static int countInventoryKeys(Key type, Player player) {
      int i = 0;

      for (ItemStack content : player.getInventory().getContents()) {
         if (content != null && content.hasItemMeta() && type.getPlainItem().getType().equals(content.getType())) {
            String crateKey = extractKey(content);
            if (crateKey != null && crateKey.equals(type.getIdentifier()) && type.getIdentifier().equalsIgnoreCase(crateKey)) {
               i += content.getAmount();
            }
         }
      }

      return i;
   }

   public static int countTotalAvailableKeys(Key key, Player player) {
      if (!key.isEnabled()) {
         return 0;
      } else if (key.isVirtual()) {
         PlayerData playerData = Crates.getPlayersManager().getCachedDataNow(player);
         return playerData.getVirtualKeys(key.getIdentifier());
      } else {
         return countInventoryKeys(key, player);
      }
   }

   public static int countTotalAvailableKeys(CrateType type, Player player) {
      int amount = 0;

      for (String keyId : type.getLinkedKeysIds()) {
         Key key = Crates.getKeysManager().getKeyByIdentifier(keyId);
         if (key != null && key.isEnabled()) {
            if (key.isVirtual()) {
               PlayerData playerData = Crates.getPlayersManager().getCachedDataNow(player);
               amount += playerData.getVirtualKeys(key.getIdentifier());
            } else {
               amount += countInventoryKeys(key, player);
            }
         }
      }

      return amount;
   }

   public static List<Key> getCrateKeys(CrateType type) {
      LinkedHashSet<Key> keys = new LinkedHashSet<>();

      for (String keyId : type.getLinkedKeysIds()) {
         Key key = Crates.getKeysManager().getKeyByIdentifier(keyId);
         if (key != null && key.isEnabled()) {
            keys.add(key);
         }
      }

      return new ArrayList<>(keys);
   }

   public static List<CrateType> getLinkedCrates(Key key) {
      LinkedHashSet<CrateType> crates = new LinkedHashSet<>();

      for (CrateType crateType : Crates.getCratesManager().getRegisteredTypes()) {
         if (crateType.getLinkedKeysIds().contains(key.getIdentifier())) {
            crates.add(crateType);
         }
      }

      return new ArrayList<>(crates);
   }

   public static List<CrateType> getNonLinkedCrates(Key key) {
      LinkedHashSet<CrateType> crates = new LinkedHashSet<>();

      for (CrateType crateType : Crates.getCratesManager().getRegisteredTypes()) {
         if (!crateType.getLinkedKeysIds().contains(key.getIdentifier())) {
            crates.add(crateType);
         }
      }

      return new ArrayList<>(crates);
   }

   public static List<Key> getNonLinkedKeys(CrateType crateType) {
      LinkedHashSet<Key> keys = new LinkedHashSet<>();

      for (Key key : Crates.getKeysManager().getRegisteredKeys()) {
         if (key.isEnabled() && !crateType.getLinkedKeysIds().contains(key.getIdentifier())) {
            keys.add(key);
         }
      }

      return new ArrayList<>(keys);
   }

   public static boolean canLinkKey(CrateType crateType, Key key) {
      if (crateType.getLinkedKeysIds().contains(key.getIdentifier())) {
         return true;
      } else {
         return Crates.getPlugin().getEdition() != Edition.LITE ? true : crateType.getLinkedKeysIds().size() < 1 && getLinkedCrates(key).size() < 1;
      }
   }

   public static void giveKey(@NonNull Key key, @NonNull Player player, boolean silent) {
      if (key == null) {
         throw new NullPointerException("key is marked non-null but is null");
      }

      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      giveKey(key, player, 1, silent);
   }

   public static void giveKey(@NonNull Key key, @NonNull Player player) {
      if (key == null) {
         throw new NullPointerException("key is marked non-null but is null");
      }

      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      giveKey(key, player, false);
   }

   public static void giveKey(@NonNull Key key, @NonNull Player player, int amount, boolean silent) {
      if (key == null) {
         throw new NullPointerException("key is marked non-null but is null");
      }

      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      giveKey(key, player, amount, silent, () -> {}, e -> Crates.getLogger().error("An error occurred while processing player data", e));
   }

   public static void giveKey(
      @NonNull Key key, @NonNull Player player, int amount, boolean silent, @NonNull Runnable onSuccess, @NonNull Consumer<Exception> onFailure
   ) {
      if (key == null) {
         throw new NullPointerException("key is marked non-null but is null");
      }

      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (onSuccess == null) {
         throw new NullPointerException("onSuccess is marked non-null but is null");
      }

      if (onFailure == null) {
         throw new NullPointerException("onFailure is marked non-null but is null");
      }

      if (key.isVirtual()) {
         PlayerFacade.processPlayerData(player, data -> {
            data.setVirtualKeys(key.getIdentifier(), data.getVirtualKeys(key.getIdentifier()) + amount);
            if (!silent) {
               Translations.send(player, Translations.t("virtual-key-received", "%display_name%", key.getFormattedDisplayName(player), "%amount%", amount)[0]);
            }

            onSuccess.run();
         }, onFailure);
      } else {
         PlayerFacade.processPlayerData(player, data -> {
            List<ItemStack> keyItems = splitIntoStacks(key.createItemStack(), amount);
            ItemDeliveryFacade.DeliveryResult result = ItemDeliveryFacade.deliver(data, player, keyItems, ItemDeliverySource.KEY_GRANT);
            if (result.hasDroppedItems()) {
               Translations.send(player, Translations.t("editor.errors.inventory-full-key"));
            }

            if (!silent) {
               Translations.send(player, Translations.t("key-received", "%display_name%", key.getFormattedDisplayName(player), "%amount%", amount)[0]);
            }

            onSuccess.run();
         }, onFailure);
      }
   }

   public static void giveKey(@NonNull Key key, @NonNull String playerName, int amount, boolean silent) {
      if (key == null) {
         throw new NullPointerException("key is marked non-null but is null");
      }

      if (playerName == null) {
         throw new NullPointerException("playerName is marked non-null but is null");
      }

      giveKey(key, playerName, amount, silent, () -> {}, e -> Crates.getLogger().error("An error occurred while processing player data", e));
   }

   public static void giveKey(
      @NonNull Key key, @NonNull String playerName, int amount, boolean silent, @NonNull Runnable onSuccess, @NonNull Consumer<Exception> onFailure
   ) {
      if (key == null) {
         throw new NullPointerException("key is marked non-null but is null");
      }

      if (playerName == null) {
         throw new NullPointerException("playerName is marked non-null but is null");
      }

      if (onSuccess == null) {
         throw new NullPointerException("onSuccess is marked non-null but is null");
      }

      if (onFailure == null) {
         throw new NullPointerException("onFailure is marked non-null but is null");
      }

      Player onlinePlayer = Bukkit.getPlayer(playerName);
      if (onlinePlayer != null) {
         giveKey(key, onlinePlayer, amount, silent, onSuccess, onFailure);
      } else {
         if (key.isVirtual()) {
            PlayerFacade.processPlayerData(playerName, data -> {
               data.setVirtualKeys(key.getIdentifier(), data.getVirtualKeys(key.getIdentifier()) + amount);
               onSuccess.run();
            }, onFailure);
         } else {
            PlayerFacade.processPlayerData(
               playerName,
               data -> {
                  ItemDeliveryFacade.DeliveryResult result = ItemDeliveryFacade.deliver(
                     data, null, splitIntoStacks(key.createItemStack(), amount), ItemDeliverySource.KEY_GRANT
                  );
                  if (result.hasUnresolvedItems()) {
                     Crates.getLogger()
                        .warn(
                           "Could not deliver "
                              + result.getUnresolvedAmount()
                              + " physical key items to offline player "
                              + playerName
                              + ". No consumption of the delivery event."
                        );
                  }

                  onSuccess.run();
               },
               onFailure
            );
         }
      }
   }

   private static List<ItemStack> splitIntoStacks(ItemStack source, int amount) {
      List<ItemStack> out = new ArrayList<>();

      while (amount > 0) {
         int stackAmount = Math.min(amount, source.getMaxStackSize());
         out.add(ItemBuilder.of(source).setAmount(stackAmount).build());
         amount -= stackAmount;
      }

      return out;
   }

   public static String extractKey(ItemStack item) {
      Objects.requireNonNull(item);
      ItemBuilder builder = ItemBuilder.of(item);
      String value = builder.getNbtTagString("phoenixcrates:key");
      if (value == null) {
         value = builder.getNbtTagString("phoenixcrates:crate-type");
      }

      if (value == null) {
         value = builder.getNbtTagString("crate-type");
      }

      return value;
   }

   public static Key getFirstKey(CrateType type) {
      for (String keyId : type.getLinkedKeysIds()) {
         Key key = Crates.getKeysManager().getKeyByIdentifier(keyId);
         if (key != null) {
            return key;
         }
      }

      return null;
   }

   public static Key getFirstAvailableKey(CrateType type, Player player) {
      for (String keyId : type.getLinkedKeysIds()) {
         Key key = Crates.getKeysManager().getKeyByIdentifier(keyId);
         if (key != null && !key.isVirtual() && key.isEnabled() && countInventoryKeys(key, player) > 0) {
            return key;
         }
      }

      return null;
   }
}
