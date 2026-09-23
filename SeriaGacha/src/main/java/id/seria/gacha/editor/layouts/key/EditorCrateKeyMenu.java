package id.seria.gacha.editor.layouts.key;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import id.seria.gacha.lib.common.utils.utility.BukkitUtil;
import id.seria.gacha.lib.common.utils.utility.InventoryUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.managers.keys.Key;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Deprecated
public class EditorCrateKeyMenu extends EditorFacade.LegacyEditorLayout {
   private static final int keyItemDragRawSlot = InventoryUtil.getPositionRaw(5, 2);
   private final Key key;
   private final EditorFacade.EditorLayout parent;

   public EditorCrateKeyMenu(Key key, EditorFacade.EditorLayout parent) {
      super(null, 4);
      this.setTitle(this.t("editor.titles.key.show")[0]);
      this.setCanPickupInventoryItems(true);
      this.key = key;
      this.parent = parent;
   }

   @Override
   public void setup(Player player, Inventory inventory) {
      ItemStack glass = ItemBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE).setDisplayName("\u00a72").build();
      this.fillBottomBorder(glass);
      this.setItem(
         5, this.getRows(), ItemBuilder.of(XMaterial.FEATHER).setDisplayName("\u00a76" + this.t("labels.go-back")[0]), event -> this.parent.open(player)
      );
      this.setItem(
         2,
         2,
         ItemBuilder.of(this.key.isEnabled() ? XMaterial.LIME_DYE : XMaterial.GRAY_DYE)
            .setDisplayName(this.t("editor.crate.key.enabled.title")[0])
            .setLore(
               this.t(
                  "editor.crate.key.enabled.description",
                  "%state%",
                  this.key.isEnabled() ? "\u00a7a" + this.t("labels.yes-word")[0] : "\u00a7c" + this.t("labels.no-word")[0]
               )
            ),
         event -> {
            this.key.setEnabled(!this.key.isEnabled());

            try {
               Crates.getKeysManager().saveKey(this.key);
               this.updateInventorySync(player);
            } catch (Exception e) {
               Translations.error(player, e);
               player.closeInventory();
            }
         }
      );
      this.setItem(
         3,
         2,
         ItemBuilder.of(XMaterial.ENCHANTED_BOOK)
            .setDisplayName("\u00a7aGlowing Key")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "&7By clicking here you will be able",
                        "&7to add glowing effect on the key.",
                        "",
                        "&fIs Enabled: %state%",
                        "",
                        "&6Left-Click: &eToggle state."
                     }
                  ),
                  "%state%",
                  this.key.isGlowing() ? "\u00a7a" + this.t("labels.yes-word")[0] : "\u00a7c" + this.t("labels.no-word")[0]
               )
            ),
         event -> {
            this.key.setGlowing(!this.key.isGlowing());

            try {
               Crates.getKeysManager().saveKey(this.key);
               this.updateInventorySync(player);
            } catch (Exception e) {
               Translations.error(player, e);
               player.closeInventory();
            }
         }
      );
      ItemBuilder keyItem = ItemBuilder.of(this.key.getPlainItem().build());
      this.setItem(
         5,
         2,
         keyItem.setDisplayName(this.t("editor.crate.key.item.title")[0])
            .setLore(
               this.t(
                  "editor.crate.key.item.description",
                  "%item_name%",
                  BukkitUtil.translateColorCodes(keyItem.getDisplayName()),
                  "%item_lore%",
                  keyItem.getLore().isEmpty() ? "\u00a77" + this.t("labels.empty")[0] : keyItem.getLore()
               )
            ),
         event -> {
            if (event.isLeftClick()) {
               Translations.send(player, this.t("editor.messages.write-displayname-chat"));
               player.closeInventory();
               TextReader.askForInput(player, input -> {
                  if (input != null) {
                     if (input.replace(" ", "").isEmpty()) {
                        Translations.send(player, this.t("editor.errors.invalid-input"));
                     } else {
                        this.key.getPlainItem().setCustomAmount(1);
                        this.key.getPlainItem().setCustomName(input);

                        try {
                           Crates.getKeysManager().saveKey(this.key);
                           Translations.send(player, this.t("editor.messages.displayname-updated", "%display_name%", BukkitUtil.translateColorCodes(input)));
                        } catch (Exception e) {
                           Translations.error(player, e);
                           player.closeInventory();
                        }
                     }
                  }

                  this.open(player);
               });
            } else if (event.isRightClick()) {
               if (event.isShiftClick()) {
                  this.key.getPlainItem().setCustomAmount(1);
                  this.key.getPlainItem().getCustomLore().clear();

                  try {
                     Crates.getKeysManager().saveKey(this.key);
                     Translations.send(player, this.t("editor.messages.lore-cleared"));
                     this.updateInventorySync(player);
                  } catch (Exception e) {
                     Translations.error(player, e);
                     player.closeInventory();
                  }
               } else {
                  Translations.send(player, this.t("editor.messages.write-lore-line-chat"));
                  player.closeInventory();
                  TextReader.askForInput(player, loreLine -> {
                     if (loreLine != null) {
                        this.key.getPlainItem().getCustomLore().add(loreLine);

                        try {
                           Crates.getKeysManager().saveKey(this.key);
                           Translations.send(player, this.t("editor.messages.lore-line-added"));
                        } catch (Exception e) {
                           Translations.error(player, e);
                           player.closeInventory();
                        }
                     }

                     this.open(player);
                  });
               }
            }
         }
      );
      this.setItem(
         7,
         2,
         ItemBuilder.of(XMaterial.EXPERIENCE_BOTTLE)
            .setDisplayName("\u00a7aVirtual Key")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "&7By clicking here you will be able",
                        "&7to set this key as virtual.",
                        "",
                        "&e&nThis means players will not",
                        "&e&nneed physical keys.",
                        "",
                        "&fIs Enabled: %state%",
                        "",
                        "&6Left-Click: &eToggle state."
                     }
                  ),
                  "%state%",
                  this.key.isVirtual() ? "\u00a7a" + this.t("labels.yes-word")[0] : "\u00a7c" + this.t("labels.no-word")[0]
               )
            ),
         event -> {
            this.key.setVirtual(!this.key.isVirtual());

            try {
               Crates.getKeysManager().saveKey(this.key);
               this.updateInventorySync(player);
            } catch (Exception e) {
               Translations.error(player, e);
               player.closeInventory();
            }
         }
      );
      this.setItem(8, 2, ItemBuilder.of(XMaterial.GRAY_STAINED_GLASS_PANE).setDisplayName("\u00a7a"));
   }

   @Override
   public void open(Player player) {
      this.open(player, new EditorFacade.EditorHolderWrapper(this));
   }

   @Override
   public void onMenuClick(InventoryClickEvent event) {
      Player player = (Player)event.getWhoClicked();
      int slot = event.getRawSlot();
      if (slot >= 0) {
         if (slot == keyItemDragRawSlot && event.getCursor().getType() != Material.AIR) {
            event.setCancelled(true);
            ItemStack newItem = event.getCursor().clone();
            if (newItem.getAmount() - 1 == 0) {
               newItem.setAmount(1);
               event.setCursor(new ItemStack(Material.AIR));
            } else {
               newItem.setAmount(newItem.getAmount() - 1);
               event.setCursor(newItem);
            }

            this.key.setPlainItem(BaseItem.from(newItem));

            try {
               if (this.key.isRegistered()) {
                  Crates.getKeysManager().saveKey(this.key);
               } else {
                  Crates.getKeysManager().registerKey(this.key, true);
               }

               this.updateInventorySync(player);
            } catch (Exception e) {
               Translations.error(player, e);
               player.closeInventory();
            }
         }
      }
   }
}
