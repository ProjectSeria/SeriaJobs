package id.seria.gacha.editor.layouts.crate;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.editor.layouts.crate.menus.EditorCrateMenusLayout;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.FacingDirection;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.NavigableComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.SimpleComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.bukkit.EditableLocation;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.complex.EditablePair;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableListOption;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableStringList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableBoolean;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableNumber;
import id.seria.gacha.lib.common.uicomponents.newest.layout.modifiers.ObjectModifiers;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import id.seria.gacha.lib.common.utils.utility.FaceUtil;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.common.utils.utility.TimeUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.PremiumEditorKeys;
import id.seria.gacha.limbo.menus.ConfirmationMenu;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.cost.CostEngineType;
import id.seria.gacha.managers.crates.engine.EngineData;
import id.seria.gacha.registries.EditorRegistry;
import id.seria.gacha.lib.xseries.XEnchantment;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class EditorCrateSecondarySettingsLayout extends EditorFacade.EditorLayout {
   private static final Table<CrateType, Class<? extends EngineData>, EngineData> CACHED_ENGINES_DATA = HashBasedTable.create();
   private final CrateType crateType;

   public EditorCrateSecondarySettingsLayout(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(Translatable.empty(), parent);
      this.crateType = crateType;
   }

   private static boolean isLiteAllowed(CostEngineType engine) {
      return !engine.isPremium();
   }

   @Override
   public void onPrepare(ContainerView view) {
      boolean lite = Crates.getPlugin().getEdition() == Edition.LITE;
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(2, 2),
               this.createData(
                  XMaterial.GOLDEN_APPLE, Translatable.key("editor.crate.preview-reward.title"), Translatable.key("editor.crate.preview-reward.description")
               ),
               this.label("enabled"),
               this.crateType.isPreviewItemEnabled()
            )
            .setOnValueChange(value -> {
               this.crateType.setPreviewItemEnabled(value);
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
      this.addComponent(
         view,
         new NavigableComponent(
            SlotCompound.from(3, 2),
            this.createData(XMaterial.ENDER_EYE, Translatable.key("editor.crate.menus.title"), Translatable.key("editor.crate.menus.description")),
            new EditorCrateMenusLayout(this.crateType, this)
         )
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(5, 2),
               this.createData(
                  XMaterial.COCOA_BEANS,
                  Translatable.key("editor.crate.simultaneous-openings.title"),
                  Translatable.key("editor.crate.simultaneous-openings.description")
               ),
               this.label("enabled"),
               this.crateType.isSimultaneousOpeningsEnabled()
            )
            .setOnValueChange(value -> {
               this.crateType.setSimultaneousOpeningsEnabled(value);
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
      this.addComponent(
         view,
         new EditablePair<>(
               SlotCompound.from(5, 3),
               this.createData(
                  XMaterial.OAK_FENCE,
                  Translatable.key("editor.crate.permission-required.title"),
                  Translatable.key("editor.crate.permission-required.description")
               ),
               this.label("required"),
               this.crateType.isPermissionRequired(),
               this.label("permission"),
               this.crateType.getPermission()
            )
            .addRightValidator(input -> !input.replace(" ", "").isEmpty(), Translatable.key("editor.errors.invalid-input"))
            .addRightValidator(input -> !input.contains(" "), Translatable.key("editor.errors.no-spaces"))
            .setOnLeftValueChange((__, value) -> {
               this.crateType.setPermissionRequired(value);
               EditorFacade.saveCrate(view, this.crateType);
            })
            .setOnRightValueChange((__, value) -> {
               this.crateType.setPermission(value);
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
      this.addComponent(
         view,
         new EditorCrateSecondarySettingsLayout.CostEngineComponent(
            SlotCompound.from(2, 3),
            this.createData(XMaterial.GOLD_INGOT, Translatable.key("editor.crate.open-cost.title"), Translatable.key("editor.crate.open-cost.description"))
         )
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(3, 3),
               this.createData(
                  XMaterial.HOPPER, Translatable.key("editor.crate.open-cooldown.title"), Translatable.key("editor.crate.open-cooldown.description")
               ),
               this.crateType.getOpenCooldownSeconds()
            )
            .setFormatter(value -> Translatable.literal(TimeUtil.formatSecondsShort(this.crateType.getOpenCooldownSeconds())))
            .addModifier(ObjectModifiers.POSITIVE_NUMBER)
            .setOnValueChange(value -> {
               this.crateType.setOpenCooldownSeconds(value.intValue());
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
      this.addComponent(
         view,
         new EditableStringList(
               SlotCompound.from(7, 2),
               this.createData(
                  XMaterial.ITEM_FRAME,
                  Translatable.key("editor.crate.broadcast-message.title"),
                  Translatable.key("editor.crate.broadcast-message.description")
               ),
               this.label("broadcast-message"),
               this.crateType.getBroadcastMessage()
            )
            .setOnValueChange(value -> {
               this.crateType.setBroadcastMessage(value);
               EditorFacade.saveCrate(view, this.crateType);
            })
            .addError(lite, Translatable.key("plans.unlock-premium"))
            .addError(lite, Translatable.key("plans.upgrade-at"))
            .setLocked(ctx -> lite)
      );
      if (lite) {
         this.addComponent(
            view,
            new EditableLocation(
                  SlotCompound.from(8, 2),
                  this.createData(
                     XMaterial.FLINT_AND_STEEL, Translatable.key("editor.crate.attach-block.title"), Translatable.key("editor.crate.attach-block.description")
                  ),
                  EditableLocation.ActionType.CLICK_TO_DEFINE,
                  (Location)null
               )
               .setOnValueChange(value -> {
                  BlockFace blockFace = FaceUtil.yawToFace(view.getViewer().getLocation().getYaw(), this.shouldUseSubCardinalDirections());
                  if (Crates.getCratesManager().getCrateByBlock(value.getBlock()) != null) {
                     Translations.send(view.getViewer(), Translations.t("crate.already-placed"));
                  } else {
                     try {
                        Crate crate = new Crate(Crates.getCratesManager(), this.crateType, value, FacingDirection.fromBukkit(blockFace));
                        Crates.getCratesManager().createCrate(crate, false);
                        Translations.send(view.getViewer(), Translations.t("crate.successfully-placed"));
                     } catch (Exception e) {
                        Translations.error(view.getViewer(), e);
                     }
                  }
               })
               .replaceDisplayAction(new ClickAction(ClickAction.ClickType.LEFT, this.label("attach-block")))
               .removeClickAction(ClickAction.ClickType.RIGHT)
               .setShowAttributes(false)
         );
      } else {
         this.addComponent(
            view,
            new EditableBoolean(
                  SlotCompound.from(8, 2),
                  this.createData(
                     XMaterial.PAPER,
                     Translatable.key("editor.crate.allow-open-command.title"),
                     Translatable.key("editor.crate.allow-open-command.description")
                  ),
                  this.label("enabled"),
                  this.crateType.isAllowOpenCommand()
               )
               .setOnValueChange(value -> {
                  this.crateType.setAllowOpenCommand(value);
                  EditorFacade.saveCrate(view, this.crateType);
               })
               .addWarning(
                  () -> Crates.getCratesManager().getCratesByType(this.crateType).isEmpty(),
                  "&cThis crate has no placed instances. Place it somewhere in the world for this to work."
               )
         );
         this.addComponent(
            view,
            new EditableLocation(
                  SlotCompound.from(3, 5),
                  this.createData(
                     XMaterial.FLINT_AND_STEEL, Translatable.key("editor.crate.attach-block.title"), Translatable.key("editor.crate.attach-block.description")
                  ),
                  EditableLocation.ActionType.CLICK_TO_DEFINE,
                  (Location)null
               )
               .setOnValueChange(value -> {
                  BlockFace blockFace = FaceUtil.yawToFace(view.getViewer().getLocation().getYaw(), this.shouldUseSubCardinalDirections());
                  if (Crates.getCratesManager().getCrateByBlock(value.getBlock()) != null) {
                     Translations.send(view.getViewer(), Translations.t("crate.already-placed"));
                  } else {
                     try {
                        Crate crate = new Crate(Crates.getCratesManager(), this.crateType, value, FacingDirection.fromBukkit(blockFace));
                        Crates.getCratesManager().createCrate(crate, false);
                        Translations.send(view.getViewer(), Translations.t("crate.successfully-placed"));
                     } catch (Exception e) {
                        Translations.error(view.getViewer(), e);
                     }
                  }
               })
               .replaceDisplayAction(new ClickAction(ClickAction.ClickType.LEFT, this.label("attach-block")))
               .removeClickAction(ClickAction.ClickType.RIGHT)
               .setShowAttributes(false)
               .setFixedPosition(true)
         );
      }

      this.addComponent(
         view,
         this.createComponent(
            SlotCompound.from(7, 3), this.createData(XMaterial.GRAY_STAINED_GLASS_PANE, Translatable.literal(""), Translatable.literal(new ArrayList()))
         )
      );
      this.addComponent(
         view,
         this.createComponent(
            SlotCompound.from(8, 3), this.createData(XMaterial.GRAY_STAINED_GLASS_PANE, Translatable.literal(""), Translatable.literal(new ArrayList()))
         )
      );
      this.addComponent(
         view,
         new NavigableComponent(
               SlotCompound.from(1, 4),
               this.createData(EditorCrateMainSettingsLayout.ARROW_LEFT, Translatable.literal("&aMain Settings"), Translatable.literal("&8(Previous page)")),
               new EditorCrateMainSettingsLayout(this.crateType, (EditorFacade.EditorLayout)this.getParent())
            )
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(2, 5),
               this.createData(XMaterial.LIME_DYE, Translatable.key("editor.crate.enabled.title"), Translatable.key("editor.crate.enabled.description")),
               this.label("enabled"),
               this.crateType.isEnabled()
            )
            .setOnValueChange(value -> {
               this.crateType.setEnabled(value);
               Crates.getCratesManager().recreateCrates();
               EditorFacade.saveCrate(view, this.crateType);
            })
            .setFixedPosition(true)
      );
      if (lite) {
         this.addComponent(
            view,
            new SimpleComponent(
                  SlotCompound.from(7, 5),
                  this.createData(
                     XMaterial.OAK_SIGN,
                     Translatable.literal("Clone Crate \u00a78(Premium)"),
                     Translatable.literal(Lists.newArrayList(new String[]{"By clicking here you will be", "able to clone this crate."}))
                  )
               )
               .setClickAction(ClickAction.legacy(ClickAction.ClickType.LEFT, "Duplicate crate"), (editable, context) -> {})
               .addError(Translatable.key("plans.unlock-premium"))
               .addError(Translatable.key("plans.upgrade-at"))
               .setFixedPosition(true)
         );
      } else {
         this.addComponent(
            view,
            new SimpleComponent(
                  SlotCompound.from(7, 5),
                  this.createData(
                     XMaterial.OAK_SIGN, Translatable.literal("Clone Crate"), Translatable.literal("By clicking here you will be", "able to clone this crate.")
                  )
               )
               .setClickAction(
                  ClickAction.legacy(ClickAction.ClickType.LEFT, "Duplicate crate"),
                  (editable, context) -> {
                     Player player = context.getViewer();
                     if (!this.crateType.isRegistered()) {
                        this.getParent().open(player);
                     } else {
                        ConfirmationMenu confirmation = new ConfirmationMenu(
                           this.plugin,
                           "Do you want to clone this crate?",
                           () -> {
                              CrateType clone = (CrateType)this.crateType.clone();
                              String newIdentifier = StringUtil.duplicate(this.crateType.getIdentifier());
                              int r = 1000;

                              for (Set<String> currentIdentifiers = PhoenixCrates.getLegacyInstance().getCratesManager().getCratesIdentifier();
                                 currentIdentifiers.contains(newIdentifier) && r > 0;
                                 r--
                              ) {
                                 newIdentifier = StringUtil.duplicate(newIdentifier);
                              }

                              if (r <= 0) {
                                 newIdentifier = newIdentifier + MathUtil.random(1, 9999);
                              }

                              clone.setIdentifier(newIdentifier);
                              clone.setDisplayName(clone.getDisplayName() + " &a(Copy)");

                              try {
                                 Crates.getCratesManager().registerCrateType(clone, true);
                                 Translations.send(context.getViewer(), "\u00a7aCrate successfully duplicated. Crate Identifier: " + clone.getIdentifier());
                                 this.getParent().open(player);
                              } catch (Exception e) {
                                 Translations.send(context.getViewer(), "\u00a7cAn error has occurred:", e.getMessage());
                              }
                           },
                           () -> this.open(player)
                        );
                        confirmation.open(player);
                     }
                  }
               )
               .setFixedPosition(true)
         );
      }

      this.addComponent(
         view,
         this.createComponent(
               SlotCompound.from(8, 5),
               this.createData(XMaterial.BARRIER, Translatable.key("editor.crate.delete.title"), Translatable.key("editor.crate.delete.description")),
               Lists.newArrayList(new ClickAction[]{ClickAction.DELETE(ClickAction.ClickType.LEFT, this.label("crate"))}),
               context -> {
                  Player player = context.getViewer();
                  if (!this.crateType.isRegistered()) {
                     this.getParent().open(player);
                  } else {
                     ConfirmationMenu confirmation = new ConfirmationMenu(this.plugin, "Do you want to delete this crate type?", () -> {
                        try {
                           Crates.getCratesManager().unregisterCrateType(this.crateType, true);
                           Translations.send(player, Translations.t("crate-type-deleted"));
                           player.closeInventory();
                           this.getParent().open(player);
                        } catch (Exception e) {
                           Translations.error(player, e);
                           player.closeInventory();
                        }
                     }, () -> this.open(player));
                     confirmation.open(player);
                  }
               }
            )
            .setFixedPosition(true)
      );
   }

   private ItemStack enchanted(XMaterial material) {
      return ItemBuilder.of(material).addEnchant(XEnchantment.DURABILITY.getEnchant(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).build();
   }

   private ItemStack enchanted(ItemStack item) {
      return ItemBuilder.of(item).addEnchant(XEnchantment.DURABILITY.getEnchant(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS).build();
   }

   @Override
   public Translatable getTitle() {
      return this.crateType.isRegistered() ? Translatable.key("editor.titles.crate.show") : Translatable.key("editor.titles.crate.new");
   }

   private boolean shouldUseSubCardinalDirections() {
      return this.crateType.getEngineType().getName().equalsIgnoreCase("VANILLA")
         ? !ArrayUtils.contains(Constants.CHEST_MATERIALS, this.crateType.getBlockType().getMaterial())
         : true;
   }

   private boolean isMalformedUrl(String input) {
      try {
         new URL(input);
         return false;
      } catch (MalformedURLException e) {
         return true;
      }
   }

   public class CostEngineComponent extends EditableListOption<CostEngineType> {
      public CostEngineComponent(final SlotCompound slot, final ComponentData data) {
         super(slot, data, Arrays.asList(CostEngineType.values()), EditorCrateSecondarySettingsLayout.this.crateType.getOpenCostEngine());
         this.removeClickAction(ClickAction.ClickType.LEFT_AND_RIGHT);
         boolean lite = Crates.getPlugin().getEdition() == Edition.LITE;
         this.setItemSelection(engine -> engine.isInstalled() && (!lite || EditorCrateSecondarySettingsLayout.isLiteAllowed(engine)));
         this.setOnOptionChange(value -> {
            EditorCrateSecondarySettingsLayout.this.crateType.setOpenCostEngine(value);
            EditorCrateSecondarySettingsLayout.this.crateType.setOpenMoneyCost(0.0);
         });
         this.setClickAction(new ClickAction(ClickAction.ClickType.LEFT, EditorCrateSecondarySettingsLayout.this.label("swap-cost-engine")), context -> {
            int i = 0;

            int index;
            for (index = this.normalizeIndex(this.getValue().get() + 1); i < this.options.size() && !this.itemSelection.apply(this.options.get(index)); i++) {
               index = this.normalizeIndex(index + 1);
            }

            if (i != this.options.size()) {
               this.setValue(index);
               EditorFacade.saveCrate(context.getContainerView(), EditorCrateSecondarySettingsLayout.this.crateType);
               context.getContainerView().updateView();
            }
         });
         this.setClickAction(
            new ClickAction(ClickAction.ClickType.RIGHT, EditorCrateSecondarySettingsLayout.this.label("set-cost-amount")),
            context -> {
               Player player = context.getViewer();
               CrateType crateType = EditorCrateSecondarySettingsLayout.this.crateType;
               if (crateType.getOpenCostEngine() == CostEngineType.EXCELLENTECONOMY) {
                  player.closeInventory();
                  EditorFacade.EditorLayout costMenu = EditorRegistry.get(
                     PremiumEditorKeys.EXCELLENT_ECONOMY_COST, crateType, EditorCrateSecondarySettingsLayout.this
                  );
                  if (costMenu != null) {
                     costMenu.open(player);
                  }
               } else {
                  player.sendMessage("\u00a7eWrite the new cost amount\u00a7e in the chat.");
                  player.closeInventory();
                  TextReader.askForInput(player, input -> {
                     if (input != null && NumberUtils.isNumber(input)) {
                        crateType.setOpenMoneyCost(this.normalizeAmount(crateType.getOpenCostEngine(), Math.max(0.0, Double.parseDouble(input))));
                        EditorFacade.saveCrate(context.getContainerView(), crateType);
                     }

                     context.getContainerView().updateView();
                  });
               }
            }
         );
      }

      protected String getFormattedOption(CostEngineType engine, boolean selected) {
         if (!engine.isInstalled()) {
            return StringUtil.humanize(engine) + " \u00a7c(" + EditorCrateSecondarySettingsLayout.this.t("labels.not-installed")[0] + ")";
         } else {
            return Crates.getPlugin().getEdition() == Edition.LITE && !EditorCrateSecondarySettingsLayout.isLiteAllowed(engine)
               ? StringUtil.humanize(engine) + " \u00a7c(Premium)"
               : StringUtil.humanize(engine);
         }
      }

      @Override
      public List<String> getRenderDisplay(RenderViewContext context) {
         CrateType crateType = EditorCrateSecondarySettingsLayout.this.crateType;
         CostEngineType costEngine = crateType.getOpenCostEngine();
         String amountDisplay;
         if (costEngine == CostEngineType.EXCELLENTECONOMY) {
            String currencyId = crateType.getOpenCostCurrency();
            double amount = crateType.getOpenMoneyCost();
            boolean wholeNumber = costEngine.isInstalled() ? costEngine.isWholeNumber(currencyId) : costEngine.isWholeNumber();
            amountDisplay = (wholeNumber ? String.valueOf((long)amount) : String.valueOf(amount))
               + " (Id: "
               + (currencyId != null && !currencyId.isEmpty() ? currencyId : "None")
               + ")";
         } else {
            amountDisplay = costEngine.isInstalled()
               ? costEngine.format(crateType.getOpenMoneyCost())
               : this.formatAmount(costEngine, crateType.getOpenMoneyCost());
         }

         List<String> lore = new ArrayList<>();
         lore.add(
            EditorCrateSecondarySettingsLayout.this.t(
               "layout.displays.value", "%type%", EditorCrateSecondarySettingsLayout.this.t("labels.cost-amount")[0], "%value%", amountDisplay
            )[0]
         );
         lore.add("");
         lore.add("\u00a7fEngines:");
         lore.addAll(super.getRenderDisplay(context));
         return lore;
      }

      private String formatAmount(CostEngineType engine, double amount) {
         return engine.isWholeNumber() ? String.valueOf((long)amount) : String.valueOf(amount);
      }

      private double normalizeAmount(CostEngineType engine, double amount) {
         return engine.isWholeNumber() ? Math.floor(amount) : amount;
      }
   }
}
