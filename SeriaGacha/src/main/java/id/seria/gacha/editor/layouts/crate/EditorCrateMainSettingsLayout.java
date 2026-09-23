package id.seria.gacha.editor.layouts.crate;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.reward.RewardsMode;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.editor.layouts.EditorMainLayout;
import id.seria.gacha.editor.layouts.crate.display.EditorCrateDisplayLayout;
import id.seria.gacha.editor.layouts.crate.engines.EditorCrateBetterModelEngineMenu;
import id.seria.gacha.editor.layouts.crate.engines.EditorCrateModelEngineEngineMenu;
import id.seria.gacha.editor.layouts.crate.engines.EditorCrateVanillaBlockEngineMenu;
import id.seria.gacha.editor.layouts.crate.engines.EditorCrateVanillaModelEngineMenu;
import id.seria.gacha.editor.layouts.crate.hologram.EditorCrateHologramLayout;
import id.seria.gacha.editor.layouts.crate.rewards.EditorCrateRewardsLayout;
import id.seria.gacha.internal.Addons;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.FacingDirection;
import id.seria.gacha.internal.LoreBuilder;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.Ui;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.LinkedComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.NavigableComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.SimpleComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.bukkit.EditableItem;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.bukkit.EditableLocation;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableListOption;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableBoolean;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableString;
import id.seria.gacha.lib.common.uicomponents.newest.layout.modifiers.ObjectModifiers;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.FaceUtil;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.common.utils.utility.PlayerUtil;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.PremiumEditorKeys;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.limbo.menus.ConfirmationMenu;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.engine.EngineData;
import id.seria.gacha.managers.crates.engine.EngineType;
import id.seria.gacha.registries.EditorRegistry;
import id.seria.gacha.registries.InformationRegistry;
import id.seria.gacha.lib.xseries.XEnchantment;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class EditorCrateMainSettingsLayout extends EditorFacade.EditorLayout {
   public static ItemStack ARROW_LEFT = Utilities.getNMSFactory()
      .createTexturedSkullFromUrl("http://textures.minecraft.net/texture/86971dd881dbaf4fd6bcaa93614493c612f869641ed59d1c9363a3666a5fa6");
   public static ItemStack ARROW_RIGHT = Utilities.getNMSFactory()
      .createTexturedSkullFromUrl("http://textures.minecraft.net/texture/f32ca66056b72863e98f7f32bd7d94c7a0d796af691c9ac3a9136331352288f9");
   private static final Table<CrateType, Class<? extends EngineData>, EngineData> CACHED_ENGINES_DATA = HashBasedTable.create();
   private final CrateType crateType;

   public EditorCrateMainSettingsLayout(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(Translatable.empty(), parent);
      this.crateType = crateType;
   }

   @Override
   public void onPrepare(ContainerView view) {
      boolean lite = Crates.getPlugin().getEdition() == Edition.LITE;
      this.addComponent(
         view,
         new EditableString(
               SlotCompound.from(2, 2),
               this.createData(XMaterial.NAME_TAG, Translatable.key("editor.crate.identifier.title"), Translatable.key("editor.crate.identifier.description")),
               this.crateType.getIdentifier()
            )
            .addValidator(input -> !input.replace(" ", "").isEmpty(), Translatable.key("editor.errors.invalid-input"))
            .addValidator(input -> !input.contains(" "), Translatable.key("editor.errors.no-spaces"))
            .addModifier(input -> ObjectModifiers.ID_NORMALIZER.process(input))
            .addValidator(input -> Crates.getCratesManager().getTypeByIdentifier(input) == null, Translatable.key("editor.errors.identifier-already-in-use"))
            .setOnValueChange(value -> {
               this.crateType.setIdentifier(value);
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
      if (lite) {
         this.addComponent(
            view,
            new EditableString(
                  SlotCompound.from(3, 2),
                  this.createData(
                     XMaterial.PAPER, Translatable.key("editor.crate.display-name.title"), Translatable.key("editor.crate.display-name.description")
                  ),
                  this.label("display-name"),
                  this.crateType.getDisplayName()
               )
               .setOnValueChange(value -> {
                  this.crateType.setDisplayName(value);
                  EditorFacade.saveCrate(view, this.crateType);
               })
         );
      } else {
         this.addComponent(
            view,
            new EditableItem(
                  SlotCompound.from(3, 2),
                  this.createData(
                     this.crateType.getItem().build(), Translatable.key("editor.crate.item.title"), Translatable.key("editor.crate.item.description")
                  ),
                  this.crateType.getItem().build()
               )
               .setClickAction(ClickAction.EDIT(ClickAction.ClickType.LEFT, Translatable.key("labels.item")), (object, context) -> {
                  EditorFacade.EditorLayout itemLayout = EditorRegistry.get(PremiumEditorKeys.ITEM_EDITOR, this.crateType, this);
                  if (itemLayout != null) {
                     itemLayout.open(context.getViewer());
                  }
               })
               .setOnValueChange(value -> {
                  this.crateType.setItem(BaseItem.from(value));
                  EditorFacade.saveCrate(view, this.crateType);
               })
         );
      }

      this.addComponent(
         view,
         new NavigableComponent(
            SlotCompound.from(2, 3),
            this.createData(XMaterial.NETHER_STAR, Translatable.key("editor.crate.animations.title"), Translatable.key("editor.crate.animations.description")),
            new EditorCrateDisplayLayout(this.crateType, this)
         )
      );
      this.addComponent(
         view,
         new NavigableComponent(
               SlotCompound.from(3, 3),
               this.createData(
                  XMaterial.TRIPWIRE_HOOK, Translatable.key("editor.crate.link-keys.title"), Translatable.key("editor.crate.link-keys.description")
               ),
               new EditorCrateLinkedKeysLayout(this.crateType, this)
            )
            .addAttribute(this.label("keys-linked"), this.crateType.getLinkedKeysIds().size())
            .addAttribute(this.label("key-required"), this.crateType.isKeyRequired())
      );
      this.addComponent(
         view,
         new NavigableComponent(
               SlotCompound.from(5, 2),
               this.createData(XMaterial.EMERALD, Translatable.key("editor.crate.rewards.title"), Translatable.key("editor.crate.rewards.description")),
               new EditorCrateRewardsLayout(this.crateType, this)
            )
            .addWarning(this.crateType.getRegisteredRewards().isEmpty(), Translatable.key("editor.errors.no-rewards-configured"))
            .addWarning(
               this.crateType.getRegisteredRewards().size() != this.crateType.getAvailableRewards().size(),
               Translatable.key(
                  "editor.errors.invalid-rewards", "%amount%", this.crateType.getRegisteredRewards().size() - this.crateType.getAvailableRewards().size()
               )
            )
      );
      this.addComponent(
         view,
         new EditorCrateMainSettingsLayout.EngineComponent(
               SlotCompound.from(5, 3),
               this.createData(
                  XMaterial.EXPERIENCE_BOTTLE,
                  Translatable.key("editor.crate.display-settings.crate-engine.title"),
                  Translatable.key("editor.crate.display-settings.crate-engine.description")
               )
            )
            .setOnOptionChange(value -> {
               CACHED_ENGINES_DATA.put(this.crateType, this.crateType.getEngineData().getClass(), this.crateType.getEngineData());
               EngineData temp = value.createEmptyData();
               EngineData newData = (EngineData)CACHED_ENGINES_DATA.get(this.crateType, temp.getClass());
               if (newData == null) {
                  newData = temp;
               }

               this.crateType.setEngineData(newData);
               EditorFacade.saveCrate(view, this.crateType);
            })
            .setItemSelection(engine -> engine.isAvailable() && engine.isInstalled())
            .setClickAction(
               new ClickAction(ClickAction.ClickType.DROP, this.label("documentation")),
               context -> PlayerUtil.sendClickableLink(
                  context.getViewer(),
                  "&#ffd500&lPhoenixCrates &8\u00bb &fClick here for the crate engine docs.",
                  "https://www.phoenixplugins.com/docs/phoenix-crates/main-features/crate-engine-mode"
               )
            )
            .setClickAction(new ClickAction(ClickAction.ClickType.RIGHT, this.label("edit-engine")), context -> {
               Player viewer = context.getViewer();
               switch (this.crateType.getEngineType().getName()) {
                  case "VANILLA_BLOCK":
                     new EditorCrateVanillaBlockEngineMenu(this.crateType, this).open(viewer);
                     break;
                  case "VANILLA_MODEL":
                     new EditorCrateVanillaModelEngineMenu(this.crateType, this).open(viewer);
                     break;
                  case "MODEL_ENGINE":
                     new EditorCrateModelEngineEngineMenu(this.crateType, this).open(viewer);
                     break;
                  case "BETTER_MODEL":
                     new EditorCrateBetterModelEngineMenu(this.crateType, this).open(viewer);
                     break;
                  default:
                     Translations.send(viewer, this.t("error-processing", "%message%", "Engine Editor not configured"));
                     viewer.closeInventory();
               }
            })
      );
      if (lite) {
         this.addComponent(
            view,
            new NavigableComponent(
                  SlotCompound.from(7, 2),
                  this.createData(
                     XMaterial.RED_SHULKER_BOX.or(XMaterial.CHEST),
                     Translatable.key("editor.addons.lootboxes.title"),
                     Translatable.key("editor.addons.lootboxes.description")
                  ),
                  new EditorMainLayout.DummyLayout(this)
               )
               .addError(Translatable.key("plans.requires-addon"))
               .addWarning(Translatable.key("plans.upgrade-at"))
               .setLocked(ctx -> true)
         );
         this.addComponent(
            view,
            new NavigableComponent(
                  SlotCompound.from(8, 2),
                  this.createData(
                     XMaterial.STRING, Translatable.key("editor.addons.mass-opening.title"), Translatable.key("editor.addons.mass-opening.description")
                  ),
                  new EditorMainLayout.DummyLayout(this)
               )
               .addError(Translatable.key("plans.requires-addon"))
               .addWarning(Translatable.key("plans.upgrade-at"))
               .setLocked(ctx -> true)
         );
         this.addComponent(
            view,
            new NavigableComponent(
                  SlotCompound.from(8, 3),
                  this.createData(
                     XMaterial.NAUTILUS_SHELL.or(XMaterial.REDSTONE),
                     Translatable.key("editor.addons.reroll.title"),
                     Translatable.key("editor.addons.reroll.description")
                  ),
                  new EditorMainLayout.DummyLayout(this)
               )
               .addWarning(Translatable.key("plans.upgrade-at"))
               .setLocked(ctx -> true)
         );
      } else {
         this.addComponent(
            view,
            new LinkedComponent(
                  SlotCompound.from(7, 2),
                  this.createData(
                     XMaterial.RED_SHULKER_BOX.or(XMaterial.CHEST),
                     Translatable.literal(Ui.hex("&#FFD700", "&e") + "Lootbox" + Constants.ADDON_TAG),
                     LoreBuilder.builder(this)
                        .description("&7Enable this crate as a lootbox.", "&7If no world phase is configured,", "&7it can open directly from air.")
                        .attribute(this.label("enabled"), false)
                        .literal()
                  ),
                  "crate.lootbox.enabled"
               )
               .withMetadata("crateType", this.crateType)
               .setFallbackError(Translatable.key("unlock-addon", "%addon%", "Lootbox"))
               .setClickAction(new ClickAction(ClickAction.ClickType.LEFT, Translatable.key("labels.switch-state")))
         );
         this.addComponent(
            view,
            new NavigableComponent(
                  SlotCompound.from(8, 2),
                  this.createData(
                     XMaterial.RESIN_BRICK.or(XMaterial.STRING),
                     Constants.addon("Mass Opening"),
                     LoreBuilder.builder(this).description(Translatable.key("editor.crate.mass-opening.description")).literal()
                  ),
                  EditorRegistry.get(Addons.MASS_OPENING, this)
               )
               .addAttribute(this.label("enabled"), InformationRegistry.isEnabled(this.crateType, Addons.MASS_OPENING))
               .withMetadata("crateType", this.crateType)
               .addWarning(InformationRegistry.isEnabled(Addons.MASS_OPENING), Ui.hex("&#2ECC71", "&a") + "Mass Opening Addon is installed!")
               .addError(!InformationRegistry.isEnabled(Addons.MASS_OPENING), Translatable.key("unlock-addon", "%addon%", "Mass Opening"))
               .addError(!this.crateType.isKeyRequired(), "&cOnly available with key required \u00a7lactivated\u00a7c.")
         );
         this.addComponent(
            view,
            new NavigableComponent(
                  SlotCompound.from(8, 3),
                  this.createData(
                     ItemBuilder.of(XMaterial.OMINOUS_TRIAL_KEY.or(XMaterial.NAUTILUS_SHELL.or(XMaterial.REDSTONE))).addItemFlags(Constants.ITEM_FLAGS),
                     Translatable.literal("&aRerolls"),
                     LoreBuilder.builder(this)
                        .description("&7By clicking here you will be able", "&7to define the rerolls of this crate.")
                        .attribute(this.label("enabled"), this.crateType.getRerolling().isEnabled())
                        .literal()
                  ),
                  EditorRegistry.get(PremiumEditorKeys.REROLLING, this.crateType, this)
               )
               .addError(() -> !this.crateType.getRewardsMode().equals(RewardsMode.RANDOM), "&cOnly available with rewards \u00a7lrandom mode\u00a7c.")
         );
      }

      this.addComponent(
         view,
         new NavigableComponent(
            SlotCompound.from(7, 3),
            this.createData(
               XMaterial.BOOK,
               Translatable.key("editor.crate.display-settings.hologram.title"),
               Translatable.key("editor.crate.display-settings.hologram.description")
            ),
            new EditorCrateHologramLayout(this.crateType, this)
         )
      );
      this.addComponent(
         view,
         new NavigableComponent(
               SlotCompound.from(9, 4),
               this.createData(ARROW_RIGHT, Translatable.literal("&aNext Page"), Translatable.literal("&8(Next page)")),
               new EditorCrateSecondarySettingsLayout(this.crateType, (EditorFacade.EditorLayout)this.getParent())
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
      if (!lite) {
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

      if (lite) {
         this.addComponent(
            view,
            new SimpleComponent(
                  SlotCompound.from(7, 5),
                  this.createData(
                     XMaterial.OAK_SIGN,
                     Translatable.literal("&6Clone Crate \u00a78(Premium)"),
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

   public class EngineComponent extends EditableListOption<EngineType> {
      public EngineComponent(SlotCompound slot, final ComponentData data) {
         super(slot, data, EngineType.values(), EditorCrateMainSettingsLayout.this.crateType.getEngineType());
         this.removeClickAction(ClickAction.ClickType.LEFT_AND_RIGHT);
         this.setClickAction(new ClickAction(ClickAction.ClickType.LEFT, EditorCrateMainSettingsLayout.this.label("swap-engine")), context -> {
            int i = 0;

            int index;
            for (index = this.normalizeIndex(this.getValue().get() + 1); i < this.options.size() && !this.itemSelection.apply(this.options.get(index)); i++) {
               index = this.normalizeIndex(index + 1);
            }

            if (i != this.options.size()) {
               this.setValue(index);
               context.getContainerView().updateView();
            }
         });
      }

      protected String getFormattedOption(EngineType engine, boolean selected) {
         if (engine.isSoon()) {
            return StringUtil.humanize(engine) + " \u00a7c(Soon)";
         } else if (!engine.isInstalled()) {
            return StringUtil.humanize(engine) + " \u00a7c(" + EditorCrateMainSettingsLayout.this.t("labels.not-installed")[0] + ")";
         } else {
            return !engine.isAvailable()
               ? StringUtil.humanize(engine) + " \u00a7c(" + EditorCrateMainSettingsLayout.this.t("labels.not-available")[0] + ")"
               : StringUtil.humanize(engine);
         }
      }
   }
}
