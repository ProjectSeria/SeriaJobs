package id.seria.gacha.editor.layouts.crate.animations.opening;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.animation.AnimationEnvironment;
import id.seria.gacha.api.crate.animation.AnimationPhase;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.editor.layouts.crate.animations.opening.data.ColoredSkullPhaseDataBuilderMenu;
import id.seria.gacha.editor.layouts.crate.animations.opening.data.KeybasedPhaseDataBuilderMenu;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.LoreBuilder;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.NavigableComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.SimpleComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.SimplePaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableBoolean;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.ColorUtil;
import id.seria.gacha.lib.xseries.XEnchantment;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.menus.SelectOptionMenu;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.animations.opening.OpeningPhase;
import id.seria.gacha.managers.crates.animations.opening.data.ColoredOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.data.ColoredSkullOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.data.EmptyOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.data.KeybasedOpeningPhaseData;
import id.seria.gacha.managers.crates.type.list.OpeningAnimation;
import id.seria.gacha.registries.AnimationsRegistry;
import id.seria.gacha.registries.animation.AddonAnimationPhaseImpl;
import id.seria.gacha.registries.animation.AddonLockedAnimationPhaseImpl;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class EditorCrateOpeningAnimationLayout extends EditorFacade.EditorLayout {
   private static final String[] PHASE_LABELS = new String[]{"Pre Open", "Post Open", "Reward Display"};
   private final CrateType crateType;
   private final EditorFacade.EditorLayout originalParent;
   private final PhaseType activePhaseType;

   public EditorCrateOpeningAnimationLayout(CrateType crateType, EditorFacade.EditorLayout parent) {
      this(crateType, parent, PhaseType.PRE_OPENING);
   }

   public EditorCrateOpeningAnimationLayout(CrateType crateType, EditorFacade.EditorLayout parent, PhaseType activePhaseType) {
      super(Translatable.key("editor.titles.crate.opening-animations"), parent);
      this.crateType = crateType;
      this.originalParent = parent;
      this.activePhaseType = activePhaseType;
   }

   @Override
   public void onPrepare(ContainerView view) {
      OpeningAnimation animation = this.getCurrentAnimation();
      this.addPhaseTab(view, PhaseType.PRE_OPENING, 2, animation.getPreOpenPhase(), PHASE_LABELS[0]);
      this.addPhaseTab(view, PhaseType.OPENING, 3, animation.getOpenPhase(), PHASE_LABELS[1]);
      this.addPhaseTab(view, PhaseType.DISPLAYING, 4, animation.getDisplayPhase(), PHASE_LABELS[2]);

      for (int row = 2; row <= 5; row++) {
         this.addComponent(
            view,
            this.createComponent(
               SlotCompound.from(2, row), this.createData(XMaterial.GRAY_STAINED_GLASS_PANE, Translatable.literal(""), Translatable.literal(new ArrayList()))
            )
         );
      }

      this.addComponent(view, new EditorCrateOpeningAnimationLayout.AnimationGridComponent());

      for (int row = 2; row <= 5; row++) {
         this.addComponent(
            view,
            this.createComponent(
               SlotCompound.from(9, row), this.createData(XMaterial.BLACK_STAINED_GLASS_PANE, Translatable.literal(""), Translatable.literal(new ArrayList()))
            )
         );
      }

      for (int col = 1; col <= 9; col++) {
         this.addComponent(
            view,
            this.createComponent(
                  SlotCompound.from(col, 1),
                  this.createData(XMaterial.BLACK_STAINED_GLASS_PANE, Translatable.literal(""), Translatable.literal(new ArrayList()))
               )
               .setFixedPosition(true)
         );
      }

      this.addComponent(
         view,
         new NavigableComponent(
            SlotCompound.from(1, 5),
            this.createData(XMaterial.BOOKSHELF, Translatable.literal("\u00a7aPresets"), Translatable.literal("\u00a77Click to browse animation presets.")),
            new EditorCrateAnimationPresetsLayout(this.crateType, this)
         )
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(8, 6),
               this.createData(
                  XMaterial.OAK_DOOR,
                  Translatable.literal("\u00a7aSkip on ESC"),
                  Translatable.literal("\u00a77When enabled, closing the GUI", "\u00a77animation will skip it instead", "\u00a77of re-opening the menu.")
               ),
               this.label("enabled"),
               this.crateType.getExtraSettings().isSkipOnEsc()
            )
            .setOnValueChange(value -> {
               this.crateType.getExtraSettings().setSkipOnEsc(value);
               EditorFacade.saveCrate(view, this.crateType);
            })
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         new NavigableComponent(
               SlotCompound.from(7, 6),
               this.createData(
                  XMaterial.ITEM_FRAME,
                  Translatable.literal("\u00a7aDisplay Settings"),
                  Translatable.literal("\u00a77Configure the position of the", "\u00a77reward display item and its name.")
               ),
               new EditorCrateDisplaySettingsLayout(this.crateType, this)
            )
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         this.createComponent(
               SlotCompound.from(5, 6),
               this.createData(XMaterial.FEATHER, this.label("go-back"), Translatable.literal(new ArrayList())),
               Lists.newArrayList(new ClickAction[]{ClickAction.legacy(ClickAction.ClickType.LEFT, "Go Back")}),
               context -> this.originalParent.open(context.getViewer())
            )
            .setFixedPosition(true)
      );
   }

   private void addPhaseTab(ContainerView view, PhaseType phaseType, int row, OpeningPhase currentPhase, String phaseLabel) {
      boolean active = this.activePhaseType == phaseType;
      String phaseName = currentPhase != null && currentPhase.getPhase() != null ? currentPhase.getPhase().getDisplayName() : this.t("labels.none")[0];
      boolean phaseCustomizable = currentPhase != null && currentPhase.getPhase() != null && isCustomizable(currentPhase.getPhase());
      ItemBuilder item = ItemBuilder.of(
         currentPhase != null && currentPhase.getPhase() != null ? currentPhase.getPhase().getDisplayItem() : XMaterial.IRON_BARS.parseItem()
      );
      if (active) {
         item.addEnchant(XEnchantment.UNBREAKING.getEnchant(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS);
      }

      List<String> loreLines = new ArrayList<>();
      loreLines.add("\u00a77Current: \u00a7f" + phaseName);
      if (currentPhase != null && currentPhase.getProperties() != null) {
         Color color = currentPhase.getProperties().getColorOrDefault("color", null);
         if (color != null) {
            loreLines.add("\u00a77Color: \u00a7f" + ColorUtil.getColorDisplayName(color) + " \u00a77(" + ColorUtil.getHexColor(color) + ")");
         }
      }

      SimpleComponent component = new SimpleComponent(
         SlotCompound.from(1, row),
         this.createData(
            item, Translatable.literal("\u00a7a" + phaseLabel + (active ? " \u00a7e(Active)" : "")), Translatable.literal(loreLines.toArray(new String[0]))
         )
      );
      component.setClickAction(ClickAction.legacy(ClickAction.ClickType.LEFT, "Select Phase"), (editable, context) -> {
         if (!active) {
            new EditorCrateOpeningAnimationLayout(this.crateType, this.originalParent, phaseType).open(context.getViewer());
         }
      });
      if (phaseCustomizable) {
         component.setClickAction(new ClickAction(ClickAction.ClickType.RIGHT, Translatable.literal("Edit Phase")), (editable, context) -> {
            OpeningAnimation anim = this.getCurrentAnimation();
            this.openDataBuilderForPhase(context.getViewer(), currentPhase.getPhase(), anim, phaseType);
         });
      }

      this.addComponent(view, component);
   }

   private static boolean isCustomizable(AnimationPhase phase) {
      if ("FLASH_CYCLE_GUI".equals(phase.getId())) {
         return false;
      }

      if (phase.getEnvironment() == AnimationEnvironment.GUI) {
         return true;
      }

      Class<?> dataClass = phase.getLegacyDataClass();
      return dataClass != null && !dataClass.equals(EmptyOpeningPhaseData.class);
   }

   private static String getDescription(AnimationPhase phase) {
      String desc = phase.getDescription();
      return desc != null ? desc : "Custom animation";
   }

   private static boolean isPremiumLocked(AnimationPhase phase) {
      return Crates.getPlugin().getEdition() == Edition.LITE && phase.isPremium();
   }

   private void openDataBuilderForPhase(Player player, AnimationPhase phase, OpeningAnimation animation, PhaseType targetPhaseType) {
      if (phase.getEnvironment() == AnimationEnvironment.GUI) {
         new EditorGuiPhaseSettingsLayout(this.crateType, targetPhaseType, this).open(player);
      } else if (!phase.getLegacyDataClass().equals(ColoredOpeningPhaseData.class)) {
         if (phase.getLegacyDataClass().equals(ColoredSkullOpeningPhaseData.class)) {
            new ColoredSkullPhaseDataBuilderMenu(PhoenixCrates.getLegacyInstance(), this.crateType, null, target -> this.open(target), data -> {
               setPhaseForType(animation, targetPhaseType, new OpeningPhase(phase, data));
               EditorFacade.saveCrate(null, this.crateType);
               Translations.send(player, this.t("editor.opening-animation.phase-updated"));
               this.open(player);
            }).open(player);
         } else {
            if (phase.getLegacyDataClass().equals(KeybasedOpeningPhaseData.class)) {
               new KeybasedPhaseDataBuilderMenu(PhoenixCrates.getLegacyInstance(), this.crateType, null, target -> this.open(target), data -> {
                  setPhaseForType(animation, targetPhaseType, new OpeningPhase(phase, data));
                  EditorFacade.saveCrate(null, this.crateType);
                  Translations.send(player, this.t("editor.opening-animation.phase-updated"));
                  this.open(player);
               }).open(player);
            }
         }
      } else {
         List<SelectOptionMenu.OptionItem<Color>> colorOptions = Lists.newArrayList();

         for (Color color : ColorUtil.getColors()) {
            colorOptions.add(
               new SelectOptionMenu.OptionItem<>(
                  color,
                  XMaterial.BOOK,
                  this.t("editor.edit-standby-effect.color-select.title", "%name%", ColorUtil.getColorDisplayName(color))[0],
                  this.t("editor.edit-standby-effect.color-select.description")
               )
            );
         }

         SelectOptionMenu<Color> colorSelector = new SelectOptionMenu<>(PhoenixCrates.getLegacyInstance(), "Select Color", colorOptions, selectedColor -> {
            setPhaseForType(animation, targetPhaseType, new OpeningPhase(phase, new ColoredOpeningPhaseData(selectedColor)));
            EditorFacade.saveCrate(null, this.crateType);
            Translations.send(player, this.t("editor.opening-animation.phase-updated"));
            this.open(player);
         });
         colorSelector.setBackConsumer(__ -> this.open(player));
         colorSelector.open(player);
      }
   }

   private OpeningAnimation getCurrentAnimation() {
      return this.crateType.getOpeningAnimations().list().get(0);
   }

   private static OpeningPhase getPhaseForType(OpeningAnimation animation, PhaseType type) {
      switch (type) {
         case PRE_OPENING:
            return animation.getPreOpenPhase();
         case OPENING:
            return animation.getOpenPhase();
         case DISPLAYING:
            return animation.getDisplayPhase();
         default:
            return null;
      }
   }

   private static void setPhaseForType(OpeningAnimation animation, PhaseType type, OpeningPhase phase) {
      switch (type) {
         case PRE_OPENING:
            animation.setPreOpenPhase(phase);
            break;
         case OPENING:
            animation.setOpenPhase(phase);
            break;
         case DISPLAYING:
            animation.setDisplayPhase(phase);
      }
   }

   private class AnimationGridComponent extends SimplePaginatedList<AnimationPhase> {
      public AnimationGridComponent() {
         super(
            SlotCompound.fromGrid(11, 4, 6),
            new PagingData(PaginationSettings.ItemsAlign.LEFT, SlotCompound.from(4, 6), SlotCompound.from(6, 6)),
            __ -> new ArrayList<>(AnimationsRegistry.getInstance().getPhases(EditorCrateOpeningAnimationLayout.this.activePhaseType))
         );
         this.setOnItemRender(
            (context, index, item, phase) -> {
               OpeningAnimation animation = EditorCrateOpeningAnimationLayout.this.getCurrentAnimation();
               OpeningPhase currentPhase = EditorCrateOpeningAnimationLayout.getPhaseForType(animation, EditorCrateOpeningAnimationLayout.this.activePhaseType);
               AddonAnimationPhaseImpl addonPhase = phase instanceof AddonAnimationPhaseImpl ? (AddonAnimationPhaseImpl)phase : null;
               AddonLockedAnimationPhaseImpl addonLockedPhase = phase instanceof AddonLockedAnimationPhaseImpl ? (AddonLockedAnimationPhaseImpl)phase : null;
               boolean premiumLocked = addonLockedPhase == null && EditorCrateOpeningAnimationLayout.isPremiumLocked(phase);
               boolean compatible = phase.isVersionCompatible();
               boolean selected = currentPhase != null && currentPhase.getPhase().equals(phase);
               boolean customizable = EditorCrateOpeningAnimationLayout.isCustomizable(phase);
               ItemBuilder displayItem;
               if (addonLockedPhase == null && !premiumLocked) {
                  displayItem = ItemBuilder.of(phase.getDisplayItem());
               } else {
                  displayItem = ItemBuilder.of(XMaterial.BARRIER.parseItem());
               }

               if (selected) {
                  displayItem.addEnchant(XEnchantment.UNBREAKING.getEnchant(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS);
               }

               String phaseDisplayName = addonPhase == null
                  ? phase.getDisplayName()
                  : EditorCrateOpeningAnimationLayout.this.t(Constants.addon(phase.getDisplayName()))[0];
               LoreBuilder lore = LoreBuilder.builder(EditorCrateOpeningAnimationLayout.this)
                  .description("\u00a77" + EditorCrateOpeningAnimationLayout.getDescription(phase));
               if (addonLockedPhase != null) {
                  lore.error(() -> true, Translatable.key("unlock-addon", "%addon%", addonLockedPhase.getAddonName()))
                     .actions(new ClickAction(ClickAction.ClickType.LEFT, Translatable.literal("Purchase at phoenixplugins.com")));
               } else if (premiumLocked) {
                  lore.error(() -> true, Translatable.key("plans.unlock-premium")).error(() -> true, Translatable.key("plans.upgrade-at"));
               } else {
                  boolean showVersion = !phase.getRequiredVersion().startsWith("1.20")
                     && !phase.getRequiredVersion().startsWith("1.8")
                     && !phase.getRequiredVersion().startsWith("1.9")
                     && !phase.getRequiredVersion().startsWith("1.1")
                     && !compatible;
                  if (showVersion) {
                     lore.attribute(Translatable.literal("Required Version"), phase.getRequiredVersion() + "+ \u00a7c\u274c");
                  }

                  lore.attribute(Translatable.literal("Customizable"), customizable ? "\u00a7aYes" : "\u00a7cNo");
                  lore.attribute(Translatable.literal("Virtual Open"), phase.getEnvironment() == AnimationEnvironment.GUI ? "\u00a7aYes" : "\u00a7cNo");
                  List<ClickAction> actions = new ArrayList<>();
                  if (!selected) {
                     actions.add(new ClickAction(ClickAction.ClickType.LEFT, Translatable.literal("Select")));
                  }

                  if (selected && customizable) {
                     actions.add(new ClickAction(ClickAction.ClickType.RIGHT, Translatable.literal("Edit Phase")));
                  }

                  lore.actions(actions);
               }

               item.withItem(displayItem)
                  .withDisplayName(
                     (addonLockedPhase == null && !premiumLocked ? "\u00a7a" : "\u00a7c") + phaseDisplayName + (selected ? " \u00a77(Selected)" : "")
                  )
                  .withLore(lore.build());
            }
         );
         this.setOnItemClick(
            (context, index, phase) -> {
               Player player = context.getViewer();
               if (phase.isVersionCompatible()) {
                  AddonLockedAnimationPhaseImpl addonLockedPhase = phase instanceof AddonLockedAnimationPhaseImpl ? (AddonLockedAnimationPhaseImpl)phase : null;
                  if (addonLockedPhase != null) {
                     addonLockedPhase.sendUnlockMessage(player);
                  } else if (EditorCrateOpeningAnimationLayout.isPremiumLocked(phase)) {
                     Translations.send(player, EditorCrateOpeningAnimationLayout.this.t("plans.unlock-premium"));
                  } else {
                     OpeningAnimation animation = EditorCrateOpeningAnimationLayout.this.getCurrentAnimation();
                     OpeningPhase currentPhase = EditorCrateOpeningAnimationLayout.getPhaseForType(
                        animation, EditorCrateOpeningAnimationLayout.this.activePhaseType
                     );
                     boolean selected = currentPhase != null && currentPhase.getPhase().equals(phase);
                     if (selected && context.isRightClick() && EditorCrateOpeningAnimationLayout.isCustomizable(phase)) {
                        EditorCrateOpeningAnimationLayout.this.openDataBuilderForPhase(
                           player, phase, animation, EditorCrateOpeningAnimationLayout.this.activePhaseType
                        );
                     } else if (context.isLeftClick() && !selected) {
                        if (EditorCrateOpeningAnimationLayout.isCustomizable(phase)) {
                           EditorCrateOpeningAnimationLayout.setPhaseForType(
                              animation, EditorCrateOpeningAnimationLayout.this.activePhaseType, new OpeningPhase(phase, AnimationProperties.empty())
                           );
                           EditorFacade.saveCrate(context.getContainerView(), EditorCrateOpeningAnimationLayout.this.crateType);
                           Translations.send(player, EditorCrateOpeningAnimationLayout.this.t("editor.opening-animation.phase-updated"));
                           context.getContainerView().updateView();
                        } else {
                           if (phase.getLegacyDataClass().equals(EmptyOpeningPhaseData.class)) {
                              EditorCrateOpeningAnimationLayout.setPhaseForType(
                                 animation, EditorCrateOpeningAnimationLayout.this.activePhaseType, new OpeningPhase(phase, AnimationProperties.empty())
                              );
                              EditorFacade.saveCrate(context.getContainerView(), EditorCrateOpeningAnimationLayout.this.crateType);
                              Translations.send(player, EditorCrateOpeningAnimationLayout.this.t("editor.opening-animation.phase-updated"));
                              context.getContainerView().updateView();
                           }
                        }
                     }
                  }
               }
            }
         );
      }
   }
}
