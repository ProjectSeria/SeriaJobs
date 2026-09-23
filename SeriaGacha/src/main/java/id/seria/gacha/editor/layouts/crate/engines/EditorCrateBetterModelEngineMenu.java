package id.seria.gacha.editor.layouts.crate.engines;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.SimpleComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableNumber;
import id.seria.gacha.lib.common.uicomponents.newest.layout.modifiers.ObjectModifiers;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.slf4j.Logger;
import id.seria.gacha.lib.slf4j.LoggerFactory;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.menus.SelectOptionMenu;
import id.seria.gacha.managers.CratesManager;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.engine.bettermodel.BetterModelEngineData;
import id.seria.gacha.sdk.hooks.bettermodel.BetterModelProvider;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.entity.Player;

public class EditorCrateBetterModelEngineMenu extends EditorFacade.EditorLayout {
   @Generated
   private static final Logger log = LoggerFactory.getLogger(EditorCrateBetterModelEngineMenu.class);
   private final CrateType crateType;

   public EditorCrateBetterModelEngineMenu(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.engines.bettermodel"), parent);
      this.crateType = crateType;
   }

   @Override
   public void onPrepare(ContainerView view) {
      BetterModelEngineData data = (BetterModelEngineData)this.crateType.getEngineData();
      this.addComponent(
         view,
         new SimpleComponent(
               SlotCompound.from(2, 2),
               this.createData(
                  XMaterial.SLIME_BALL,
                  this.t("editor.crate-engine.better-model.model-name.title")[0],
                  this.t(
                     "editor.crate-engine.model-engine.model-name.description",
                     "%name%",
                     data.getModelId().equalsIgnoreCase("empty") ? "?????" : data.getModelId()
                  )
               )
            )
            .setClickAction(
               new ClickAction(ClickAction.ClickType.LEFT, this.label("select")),
               context -> {
                  if (!context.isShiftClick()) {
                     if (!Crates.isBetterModelInstalled()) {
                        throw new IllegalStateException("BetterModel not available");
                     }

                     List<String> modelKeys;
                     try {
                        modelKeys = PhoenixCrates.getLegacyInstance()
                           .getHookRegistry()
                           .getHook(BetterModelProvider.class)
                           .modelKeys()
                           .stream()
                           .sorted(String::compareToIgnoreCase)
                           .collect(Collectors.toList());
                     } catch (Throwable t) {
                        context.getViewer().sendMessage("\u00a7cBetterModel is not ready yet. Please wait a few seconds after startup and try again. cause: ");
                        return;
                     }

                     List<SelectOptionMenu.OptionItem<String>> options = Lists.newArrayList();

                     for (String modelKey : modelKeys) {
                        options.add(
                           new SelectOptionMenu.OptionItem<>(
                              modelKey,
                              XMaterial.PAPER,
                              Translations.r("&a%name%", "%name%", modelKey),
                              Translations.r(Lists.newArrayList(new String[]{"", "&6Left-Click: &eSelect model."}))
                           )
                        );
                     }

                     SelectOptionMenu<String> selector = new SelectOptionMenu<>(
                        this.plugin, this.t("editor.titles.crate.engine", "%crate%", this.crateType.getIdentifier())[0], options, 3, menuName -> {
                           data.setModelId(menuName);
                           this.save(context.getViewer());
                        }
                     );
                     selector.setBackConsumer(__ -> this.open(context.getViewer()));
                     selector.open(context.getViewer());
                  }
               }
            )
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(2, 3),
               this.createData(
                  XMaterial.ARMOR_STAND,
                  Translatable.key("editor.crate-engine.better-model.scale.title"),
                  Translatable.key("editor.crate-engine.better-model.scale.description")
               ),
               data.getScale()
            )
            .addModifier(ObjectModifiers.CLAMP(0.1, 10.0))
            .setOnValueChange(value -> {
               data.setScale(value.doubleValue());
               this.save(view.getViewer());
            })
      );
      this.addComponent(
         view,
         new SimpleComponent(
               SlotCompound.from(4, 2),
               this.createData(
                  XMaterial.REDSTONE,
                  "\u00a7aIdle Animation",
                  Translations.r(
                        Lists.newArrayList(
                           new String[]{
                              "&7By clicking here you will be able", "&7to define the idle animation for this model.", "", "&fCurrent Animation: &7%animation%"
                           }
                        ),
                        "%animation%",
                        data.getModelId().equalsIgnoreCase("none") ? "None" : data.getIdleAnimation()
                     )
                     .toArray(new String[0])
               )
            )
            .setClickAction(
               new ClickAction(ClickAction.ClickType.LEFT, this.label("edit")), context -> this.openAnimationSelector(context.getViewer(), data, menuName -> {
                  data.setIdleAnimation(menuName);
                  this.save(context.getViewer());
               })
            )
            .setClickAction(new ClickAction(ClickAction.ClickType.RIGHT, this.label("remove")), context -> {
               data.setIdleAnimation("none");
               this.save(context.getViewer());
            })
      );
      this.addComponent(
         view,
         new SimpleComponent(
               SlotCompound.from(5, 2),
               this.createData(
                  XMaterial.GLOWSTONE_DUST,
                  "\u00a7aFirst Opening Phase",
                  Translations.r(
                        Lists.newArrayList(
                           new String[]{
                              "&7By clicking here you will be able",
                              "&7to change the animation that will be played",
                              "&7on the first phase of the opening animation.",
                              "",
                              "&fCurrent Animation: &7%animation%"
                           }
                        ),
                        "%animation%",
                        data.getModelId().equalsIgnoreCase("none") ? "None" : data.getFirstPhaseAnimation()
                     )
                     .toArray(new String[0])
               )
            )
            .setClickAction(
               new ClickAction(ClickAction.ClickType.LEFT, this.label("edit")), context -> this.openAnimationSelector(context.getViewer(), data, menuName -> {
                  data.setFirstPhaseAnimation(menuName);
                  this.save(context.getViewer());
               })
            )
            .setClickAction(new ClickAction(ClickAction.ClickType.RIGHT, this.label("remove")), context -> {
               data.setFirstPhaseAnimation("none");
               this.save(context.getViewer());
            })
      );
      this.addComponent(
         view,
         new SimpleComponent(
               SlotCompound.from(6, 2),
               this.createData(
                  XMaterial.GLOWSTONE_DUST,
                  "\u00a7aSecond Opening Phase",
                  Translations.r(
                        Lists.newArrayList(
                           new String[]{
                              "&7By clicking here you will be able",
                              "&7to change the animation that will be played",
                              "&7on the second phase of the opening animation.",
                              "",
                              "&fCurrent Animation: &7%animation%"
                           }
                        ),
                        "%animation%",
                        data.getModelId().equalsIgnoreCase("none") ? "None" : data.getSecondPhaseAnimation()
                     )
                     .toArray(new String[0])
               )
            )
            .setClickAction(
               new ClickAction(ClickAction.ClickType.LEFT, this.label("edit")), context -> this.openAnimationSelector(context.getViewer(), data, menuName -> {
                  data.setSecondPhaseAnimation(menuName);
                  this.save(context.getViewer());
               })
            )
            .setClickAction(new ClickAction(ClickAction.ClickType.RIGHT, this.label("remove")), context -> {
               data.setSecondPhaseAnimation("none");
               this.save(context.getViewer());
            })
      );
      this.addComponent(
         view,
         new SimpleComponent(
               SlotCompound.from(7, 2),
               this.createData(
                  XMaterial.GLOWSTONE_DUST,
                  "\u00a7aThird Opening Phase",
                  Translations.r(
                        Lists.newArrayList(
                           new String[]{
                              "&7By clicking here you will be able",
                              "&7to change the animation that will be played",
                              "&7on the third phase of the opening animation.",
                              "",
                              "&fCurrent Animation: &7%animation%"
                           }
                        ),
                        "%animation%",
                        data.getModelId().equalsIgnoreCase("none") ? "None" : data.getThirdPhaseAnimation()
                     )
                     .toArray(new String[0])
               )
            )
            .setClickAction(
               new ClickAction(ClickAction.ClickType.LEFT, this.label("edit")), context -> this.openAnimationSelector(context.getViewer(), data, menuName -> {
                  data.setThirdPhaseAnimation(menuName);
                  this.save(context.getViewer());
               })
            )
            .setClickAction(new ClickAction(ClickAction.ClickType.RIGHT, this.label("remove")), context -> {
               data.setThirdPhaseAnimation("none");
               this.save(context.getViewer());
            })
      );
      this.addComponent(
         view,
         new SimpleComponent(
               SlotCompound.from(8, 2),
               this.createData(
                  XMaterial.REDSTONE,
                  "\u00a7aClose Animation",
                  Translations.r(
                        Lists.newArrayList(
                           new String[]{
                              "&7By clicking here you will be able",
                              "&7to define the close animation for this model.",
                              "",
                              "&fCurrent Animation: &7%animation%"
                           }
                        ),
                        "%animation%",
                        data.getModelId().equalsIgnoreCase("none") ? "None" : data.getCloseAnimation()
                     )
                     .toArray(new String[0])
               )
            )
            .setClickAction(
               new ClickAction(ClickAction.ClickType.LEFT, this.label("edit")), context -> this.openAnimationSelector(context.getViewer(), data, menuName -> {
                  data.setCloseAnimation(menuName);
                  this.save(context.getViewer());
               })
            )
            .setClickAction(new ClickAction(ClickAction.ClickType.RIGHT, this.label("remove")), context -> {
               data.setCloseAnimation("none");
               this.save(context.getViewer());
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(5, 3),
               this.createData(
                  XMaterial.CLOCK,
                  Translatable.key("editor.crate-engine.first-phase-delay.title"),
                  Translatable.key("editor.crate-engine.first-phase-delay.description", "%delay%", String.valueOf(data.getFirstPhaseStartDelay()))
               ),
               data.getFirstPhaseStartDelay()
            )
            .addModifier(ObjectModifiers.POSITIVE_NUMBER)
            .setOnValueChange(value -> {
               data.setFirstPhaseStartDelay(value.intValue());
               this.save(view.getViewer());
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(6, 3),
               this.createData(
                  XMaterial.CLOCK,
                  Translatable.key("editor.crate-engine.second-phase-delay.title"),
                  Translatable.key("editor.crate-engine.second-phase-delay.description", "%delay%", String.valueOf(data.getSecondPhaseStartDelay()))
               ),
               data.getSecondPhaseStartDelay()
            )
            .addModifier(ObjectModifiers.POSITIVE_NUMBER)
            .setOnValueChange(value -> {
               data.setSecondPhaseStartDelay(value.intValue());
               this.save(view.getViewer());
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(7, 3),
               this.createData(
                  XMaterial.CLOCK,
                  Translatable.key("editor.crate-engine.third-phase-delay.title"),
                  Translatable.key("editor.crate-engine.third-phase-delay.description", "%delay%", String.valueOf(data.getThirdPhaseStartDelay()))
               ),
               data.getThirdPhaseStartDelay()
            )
            .addModifier(ObjectModifiers.POSITIVE_NUMBER)
            .setOnValueChange(value -> {
               data.setThirdPhaseStartDelay(value.intValue());
               this.save(view.getViewer());
            })
      );
   }

   private void openAnimationSelector(Player player, BetterModelEngineData data, Consumer<String> onSelect) {
      List<String> blueprint = PhoenixCrates.getLegacyInstance()
         .getHookRegistry()
         .getHook(BetterModelProvider.class)
         .animationKeys(data.getModelId())
         .stream()
         .sorted(String::compareToIgnoreCase)
         .collect(Collectors.toList());
      List<SelectOptionMenu.OptionItem<String>> options = Lists.newArrayList();

      for (String animation : blueprint) {
         options.add(
            new SelectOptionMenu.OptionItem<>(
               animation,
               XMaterial.PAPER,
               Translations.r("&a%name%", "%name%", animation),
               Translations.r(Lists.newArrayList(new String[]{"", "&6Left-Click: &eSelect animation."}))
            )
         );
      }

      SelectOptionMenu<String> selector = new SelectOptionMenu<>(
         this.plugin, this.t("editor.titles.engines.bettermodel", "%crate%", this.crateType.getIdentifier())[0], options, 3, onSelect
      );
      selector.setBackConsumer(__ -> this.open(player));
      selector.open(player);
   }

   private void save(Player player) {
      try {
         CratesManager manager = Crates.getCratesManager();
         if (this.crateType.isRegistered()) {
            manager.saveCrateType(this.crateType);
         } else {
            manager.registerCrateType(this.crateType, true);
         }

         if (this.getViewersContext().containsKey(player.getUniqueId())) {
            this.updateView(player);
         } else {
            this.open(player);
         }
      } catch (Exception e) {
         Translations.error(player, e);
         player.closeInventory();
      }
   }
}
