package id.seria.gacha.editor.layouts.crate.engines;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.dialog.DialogUtil;
import id.seria.gacha.lib.common.utils.inventory.CustomInventory;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.menus.SelectOptionMenu;
import id.seria.gacha.managers.CratesManager;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.engine.vanilla_model.VanillaModelDisplayComponent;
import id.seria.gacha.managers.crates.engine.vanilla_model.VanillaModelEngineData;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Deprecated
public class EditorCrateVanillaModelEngineMenu extends CustomInventory {
   private final CrateType crateType;
   private final EditorFacade.EditorLayout parent;
   private EditorCrateVanillaModelEngineMenu.CustomDataType customDataType;

   public EditorCrateVanillaModelEngineMenu(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(parent.getPlugin(), Translations.t("editor.titles.crate.engine", "%crate%", StringUtil.humanize(crateType.getEngineType().getName()))[0], 4);
      Validate.isTrue(crateType.getEngineData() instanceof VanillaModelEngineData);
      this.crateType = crateType;
      this.parent = parent;
      String itemModel = ((VanillaModelEngineData)crateType.getEngineData()).getItemModel();
      this.customDataType = VanillaModelDisplayComponent.ITEM_MODEL_SUPPORTED && itemModel != null && !itemModel.isEmpty()
         ? EditorCrateVanillaModelEngineMenu.CustomDataType.ITEM_MODEL
         : EditorCrateVanillaModelEngineMenu.CustomDataType.CUSTOM_MODEL_DATA;
   }

   @Override
   public void setup(Player player, Inventory inventory) {
      ItemStack glass = ItemBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE).setDisplayName("\u00a7r").build();
      this.fillBottomBorder(glass);
      this.setItem(
         5,
         this.getRows(),
         ItemBuilder.of(XMaterial.FEATHER).setDisplayName("\u00a76" + Translations.t("labels.go-back")[0]),
         event -> this.parent.open(player)
      );
      VanillaModelEngineData data = (VanillaModelEngineData)this.crateType.getEngineData();
      this.setItem(
         2,
         2,
         ItemBuilder.of(data.getMaterial())
            .setDisplayName("\u00a7aMaterial")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "&7By clicking here you will be able",
                        "&7to change the material of this model.",
                        "",
                        "\u00a7fMaterial: \u00a77%material%",
                        "",
                        "\u00a76Left-Click: \u00a7eChange Material."
                     }
                  ),
                  "%material%",
                  StringUtil.humanize(data.getMaterial())
               )
            ),
         event -> {
            List<SelectOptionMenu.OptionItem<XMaterial>> options = Lists.newArrayList();
            options.add(
               new SelectOptionMenu.OptionItem<>(
                  XMaterial.PAPER,
                  XMaterial.PAPER,
                  "\u00a7aPaper",
                  Lists.newArrayList(Translations.t("editor.crate-engine.vanilla-model.material-select.description"))
               )
            );
            options.add(
               new SelectOptionMenu.OptionItem<>(
                  XMaterial.FEATHER,
                  XMaterial.FEATHER,
                  "\u00a7aFeather",
                  Lists.newArrayList(Translations.t("editor.crate-engine.vanilla-model.material-select.description"))
               )
            );

            for (XMaterial material : XMaterial.VALUES) {
               if (material.isSupported() && material != XMaterial.PAPER && material != XMaterial.FEATHER) {
                  try {
                     material.parseItem();
                  } catch (IllegalArgumentException ignored) {
                     continue;
                  }

                  String displayName = material.equals(XMaterial.PLAYER_HEAD) ? "Textured Skull" : StringUtil.humanize(material);
                  options.add(
                     new SelectOptionMenu.OptionItem<>(
                        material,
                        material,
                        Translations.t("editor.crate-engine.vanilla-model.material-select.title", "%name%", displayName)[0],
                        Translations.t("editor.crate-engine.vanilla-model.material-select.description")
                     )
                  );
               }
            }

            SelectOptionMenu<XMaterial> optionSelector = new SelectOptionMenu<>(this.plugin, this.getTitle(), options, 5, materialx -> {
               data.setMaterial(materialx);
               this.save(player);
            });
            optionSelector.setBackConsumer(__ -> this.open(player));
            optionSelector.open(player);
         }
      );
      boolean itemModelSupported = VanillaModelDisplayComponent.ITEM_MODEL_SUPPORTED;
      List<String> customDataLore = Lists.newArrayList(Translations.t("editor.crate-engine.vanilla-model.custom-data.description"));
      if (itemModelSupported) {
         customDataLore.addAll(Arrays.asList(Translations.t("editor.crate-engine.vanilla-model.custom-data.item-model-description")));
      }

      customDataLore.add("");

      for (EditorCrateVanillaModelEngineMenu.CustomDataType type : EditorCrateVanillaModelEngineMenu.CustomDataType.values()) {
         if (type != EditorCrateVanillaModelEngineMenu.CustomDataType.ITEM_MODEL || itemModelSupported) {
            customDataLore.add(
               String.format(
                  type == this.customDataType ? "\u00a7a\u25aa %s" : "\u00a77\u25aa %s",
                  Translations.t("editor.crate-engine.vanilla-model.custom-data.options." + type.key)[0]
               )
            );
         }
      }

      customDataLore.addAll(Arrays.asList(Translations.t("editor.crate-engine.vanilla-model.custom-data.footer")));
      if (itemModelSupported) {
         customDataLore.addAll(Arrays.asList(Translations.t("editor.crate-engine.vanilla-model.custom-data.swap-hint")));
      }

      customDataLore.addAll(Arrays.asList(Translations.t("editor.crate-engine.vanilla-model.custom-data.set-hint")));
      String customDataInput = this.customDataType == EditorCrateVanillaModelEngineMenu.CustomDataType.ITEM_MODEL
         ? data.getItemModel()
         : String.valueOf(data.getCustomModelData());
      String customDataValue = customDataInput != null && !customDataInput.isEmpty() ? customDataInput : Translations.t("labels.empty")[0];
      String customDataOption = Translations.t("editor.crate-engine.vanilla-model.custom-data.options." + this.customDataType.key)[0];
      this.setItem(
         3,
         2,
         ItemBuilder.of(XMaterial.GLOWSTONE_DUST)
            .setDisplayName(Translations.t("editor.crate-engine.vanilla-model.custom-data.title")[0])
            .setLore(Translations.r(customDataLore, "%value%", customDataValue)),
         event -> {
            if (event.isLeftClick()) {
               if (itemModelSupported) {
                  if (this.customDataType == EditorCrateVanillaModelEngineMenu.CustomDataType.CUSTOM_MODEL_DATA) {
                     this.customDataType = EditorCrateVanillaModelEngineMenu.CustomDataType.ITEM_MODEL;
                     data.setCustomModelData(0);
                  } else {
                     this.customDataType = EditorCrateVanillaModelEngineMenu.CustomDataType.CUSTOM_MODEL_DATA;
                     data.setItemModel(null);
                  }

                  this.save(player);
               }
            } else if (event.isRightClick()) {
               Consumer<String> handler = input -> {
                  if (input != null) {
                     if (input.replace(" ", "").isEmpty()) {
                        Translations.send(player, Translations.t("editor.errors.invalid-input"));
                     } else {
                        if (this.customDataType == EditorCrateVanillaModelEngineMenu.CustomDataType.ITEM_MODEL) {
                           data.setItemModel(input);
                           data.setCustomModelData(0);
                           this.save(player);
                           return;
                        }

                        if (StringUtil.isInteger(input)) {
                           data.setCustomModelData(Integer.parseInt(input));
                           data.setItemModel(null);
                           this.save(player);
                           return;
                        }

                        Translations.send(player, Translations.t("editor.errors.invalid-input"));
                     }
                  }

                  this.open(player);
               };
               player.closeInventory();

               try {
                  DialogUtil.askForInput(
                     player,
                     Translations.t("editor.crate-engine.vanilla-model.custom-data.popup.title", "%option%", customDataOption)[0],
                     customDataOption,
                     Translations.t("editor.crate-engine.vanilla-model.custom-data.popup.prompt", "%option%", customDataOption.toLowerCase())[0],
                     customDataInput,
                     handler
                  );
               } catch (Throwable ignored) {
                  Translations.send(player, Translations.t("editor.messages.write-in-chat"));
                  TextReader.askForInput(player, handler);
               }
            }
         }
      );
      this.setItem(
         5,
         2,
         ItemBuilder.of(XMaterial.COMPASS)
            .setDisplayName("\u00a7aRotation Degrees")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "\u00a77This option allows you to adjust the",
                        "\u00a77rotation of the crate.",
                        "",
                        "\u00a7fRotation: \u00a77%degrees%\u00ba",
                        "",
                        "\u00a76Left-Click: \u00a7eAdd 10.",
                        "\u00a76Shift + Left-Click: \u00a7eAdd 90.",
                        "\u00a76Right-Click: \u00a7eRemove 10.",
                        "\u00a76Shift + Right-Click: \u00a7eRemove 90."
                     }
                  ),
                  "%degrees%",
                  data.getRotationDegrees()
               )
            ),
         event -> {
            int rotation = data.getRotationDegrees();
            if (event.isLeftClick()) {
               rotation += event.isShiftClick() ? 90 : 10;
            } else if (event.isRightClick()) {
               rotation -= event.isShiftClick() ? 90 : 10;
            }

            data.setRotationDegrees((rotation % 360 + 360) % 360);
            this.save(player);
         }
      );
      this.setItem(
         4,
         2,
         ItemBuilder.of(XMaterial.ARMOR_STAND)
            .setDisplayName("\u00a7aScale")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "\u00a77This option allows you to adjust the",
                        "\u00a77size of the crate display.",
                        "",
                        "\u00a7fScale: \u00a77%scale%",
                        "",
                        "\u00a76Left-Click: \u00a7eAdd 0.1.",
                        "\u00a76Shift + Left-Click: \u00a7eAdd 1.",
                        "\u00a76Right-Click: \u00a7eRemove 0.1.",
                        "\u00a76Shift + Right-Click: \u00a7eRemove 1."
                     }
                  ),
                  "%scale%",
                  Constants.DECIMAL1_FORMAT.format(data.getScale())
               )
            ),
         event -> {
            double scale = data.getScale();
            if (event.isLeftClick()) {
               scale += event.isShiftClick() ? 1.0 : 0.1;
            } else if (event.isRightClick()) {
               scale -= event.isShiftClick() ? 1.0 : 0.1;
            }

            data.setScale(MathUtil.clamp(scale, 0.1, 10.0));
            this.save(player);
         }
      );
      this.setItem(
         6,
         2,
         ItemBuilder.of(XMaterial.GUNPOWDER)
            .setDisplayName("\u00a7aOffset X")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "\u00a77This option allows you to adjust the",
                        "\u00a77key position based on the crate location.",
                        "",
                        "\u00a7fCurrent: \u00a77%offset%",
                        "",
                        "\u00a76Left-Click: \u00a7eAdd 0.1.",
                        "\u00a76Shift + Left-Click: \u00a7eAdd 1.",
                        "\u00a76Right-Click: \u00a7eRemove 0.1.",
                        "\u00a76Shift + Right-Click: \u00a7eRemove 1."
                     }
                  ),
                  "%offset%",
                  Constants.DECIMAL1_FORMAT.format(data.getOffsetX())
               )
            ),
         event -> {
            double offset = data.getOffsetX();
            if (event.isLeftClick()) {
               offset += event.isShiftClick() ? 1.0 : 0.1;
            } else if (event.isRightClick()) {
               offset -= event.isShiftClick() ? 1.0 : 0.1;
            }

            data.setOffsetX(MathUtil.clamp(offset, -5.0, 5.0));
            this.save(player);
         }
      );
      this.setItem(
         7,
         2,
         ItemBuilder.of(XMaterial.GUNPOWDER)
            .setDisplayName("\u00a7aOffset Y")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "\u00a77This option allows you to adjust the",
                        "\u00a77key position based on the crate location.",
                        "",
                        "\u00a7fCurrent: \u00a77%offset%",
                        "",
                        "\u00a76Left-Click: \u00a7eAdd 0.1.",
                        "\u00a76Shift + Left-Click: \u00a7eAdd 1.",
                        "\u00a76Right-Click: \u00a7eRemove 0.1.",
                        "\u00a76Shift + Right-Click: \u00a7eRemove 1."
                     }
                  ),
                  "%offset%",
                  Constants.DECIMAL1_FORMAT.format(data.getOffsetY())
               )
            ),
         event -> {
            double offset = data.getOffsetY();
            if (event.isLeftClick()) {
               offset += event.isShiftClick() ? 1.0 : 0.1;
            } else if (event.isRightClick()) {
               offset -= event.isShiftClick() ? 1.0 : 0.1;
            }

            data.setOffsetY(MathUtil.clamp(offset, -5.0, 5.0));
            this.save(player);
         }
      );
      this.setItem(
         8,
         2,
         ItemBuilder.of(XMaterial.GUNPOWDER)
            .setDisplayName("\u00a7aOffset Z")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "\u00a77This option allows you to adjust the",
                        "\u00a77key position based on the crate location.",
                        "",
                        "\u00a7fCurrent: \u00a77%offset%",
                        "",
                        "\u00a76Left-Click: \u00a7eAdd 0.1.",
                        "\u00a76Shift + Left-Click: \u00a7eAdd 1.",
                        "\u00a76Right-Click: \u00a7eRemove 0.1.",
                        "\u00a76Shift + Right-Click: \u00a7eRemove 1."
                     }
                  ),
                  "%offset%",
                  Constants.DECIMAL1_FORMAT.format(data.getOffsetZ())
               )
            ),
         event -> {
            double offset = data.getOffsetZ();
            if (event.isLeftClick()) {
               offset += event.isShiftClick() ? 1.0 : 0.1;
            } else if (event.isRightClick()) {
               offset -= event.isShiftClick() ? 1.0 : 0.1;
            }

            data.setOffsetZ(MathUtil.clamp(offset, -5.0, 5.0));
            this.save(player);
         }
      );
   }

   private void save(Player player) {
      try {
         CratesManager manager = Crates.getCratesManager();
         if (this.crateType.isRegistered()) {
            manager.saveCrateType(this.crateType);
         } else {
            manager.registerCrateType(this.crateType, true);
         }

         if (this.getViewers().contains(player)) {
            this.updateInventorySync(player);
         } else {
            this.open(player);
         }
      } catch (Exception e) {
         Translations.error(player, e);
         player.closeInventory();
      }
   }

   @Override
   public void open(Player player) {
      this.open(player, new EditorFacade.EditorHolderWrapper(this));
   }

   private enum CustomDataType {
      CUSTOM_MODEL_DATA("custom-model-data"),
      ITEM_MODEL("item-model");

      private final String key;

      CustomDataType(final String key) {
         this.key = key;
      }
   }
}
