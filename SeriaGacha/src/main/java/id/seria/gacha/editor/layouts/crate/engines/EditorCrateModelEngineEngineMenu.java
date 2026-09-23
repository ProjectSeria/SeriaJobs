package id.seria.gacha.editor.layouts.crate.engines;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.modelengine.interfaces.model.MegModelBlueprint;
import id.seria.gacha.lib.common.utils.inventory.CustomInventory;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.hooks.modelengine.ModelEngine;
import id.seria.gacha.limbo.menus.SelectOptionMenu;
import id.seria.gacha.managers.CratesManager;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.engine.modelengine.ModelEngineEngineData;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Deprecated
public class EditorCrateModelEngineEngineMenu extends CustomInventory {
   private final CrateType crateType;
   private final EditorFacade.EditorLayout parent;

   public EditorCrateModelEngineEngineMenu(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(parent.getPlugin(), Translations.t("editor.titles.crate.engine", "%crate%", StringUtil.humanize(crateType.getEngineType().getName()))[0], 5);
      Validate.isTrue(crateType.getEngineData() instanceof ModelEngineEngineData);
      this.crateType = crateType;
      this.parent = parent;
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
      ModelEngineEngineData data = (ModelEngineEngineData)this.crateType.getEngineData();
      this.setItem(
         2,
         2,
         ItemBuilder.of(XMaterial.SLIME_BALL)
            .setDisplayName(Translations.t("editor.crate-engine.model-engine.model-name.title")[0])
            .setLore(
               Translations.t(
                  "editor.crate-engine.model-engine.model-name.description",
                  "%name%",
                  data.getModelId().equalsIgnoreCase("empty") ? "?????" : data.getModelId()
               )
            ),
         event -> {
            if (event.isLeftClick()) {
               if (event.isShiftClick()) {
                  return;
               }

               List<SelectOptionMenu.OptionItem<String>> options = Lists.newArrayList();

               for (String model : ModelEngine.getApi().getModelRegistry().getAllBlueprintId().stream().sorted().collect(Collectors.toList())) {
                  options.add(
                     new SelectOptionMenu.OptionItem<>(
                        model,
                        XMaterial.PAPER,
                        Translations.r("&a%name%", "%name%", model),
                        Translations.r(Lists.newArrayList(new String[]{"", "&6Left-Click: &eSelect model."}))
                     )
                  );
               }

               SelectOptionMenu<String> optionSelector = new SelectOptionMenu<>(this.plugin, this.getTitle(), options, 3, menuName -> {
                  data.setModelId(menuName);
                  this.save(player);
               });
               optionSelector.setBackConsumer(__ -> this.open(player));
               optionSelector.open(player);
            }
         }
      );
      this.setItem(
         2,
         3,
         ItemBuilder.of(XMaterial.ARMOR_STAND)
            .setDisplayName(Translations.t("editor.crate-engine.model-engine.scale.title")[0])
            .setLore(Translations.t("editor.crate-engine.model-engine.scale.description")),
         event -> {
            double scale = data.getScale();
            if (event.isLeftClick()) {
               scale += event.isShiftClick() ? 1.0 : 0.1;
            } else if (event.isRightClick()) {
               scale -= event.isShiftClick() ? 1.0 : 0.1;
            }

            data.setScale(Math.min(10.0, Math.max(0.1, Math.round(scale * 10.0) / 10.0)));
            this.save(player);
         }
      );
      this.setItem(
         4,
         2,
         ItemBuilder.of(XMaterial.REDSTONE)
            .setDisplayName("\u00a7aIdle Animation")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "&7By clicking here you will be able",
                        "&7to define the idle animation for this model.",
                        "",
                        "&fCurrent Animation: &7%animation%",
                        "",
                        "&6Left-Click: &eEdit animation.",
                        "&6Right-Click: &eRemove animation."
                     }
                  ),
                  "%animation%",
                  data.getModelId().equalsIgnoreCase("none") ? "None" : data.getIdleAnimation()
               )
            ),
         event -> {
            if (event.isLeftClick()) {
               MegModelBlueprint blueprint = ModelEngine.getApi().getModelRegistry().getBlueprint(data.getModelId());
               if (blueprint == null) {
                  return;
               }

               List<SelectOptionMenu.OptionItem<String>> options = Lists.newArrayList();

               for (String animation : blueprint.getAnimations().keySet()) {
                  options.add(
                     new SelectOptionMenu.OptionItem<>(
                        animation,
                        XMaterial.PAPER,
                        Translations.r("&a%name%", "%name%", animation),
                        Translations.r(Lists.newArrayList(new String[]{"", "&6Left-Click: &eSelect model."}))
                     )
                  );
               }

               SelectOptionMenu<String> optionSelector = new SelectOptionMenu<>(this.plugin, this.getTitle(), options, 3, menuName -> {
                  data.setIdleAnimation(menuName);
                  this.save(player);
               });
               optionSelector.setBackConsumer(__ -> this.open(player));
               optionSelector.open(player);
            } else if (event.isRightClick()) {
               data.setIdleAnimation("none");
               this.save(player);
            }
         }
      );
      this.setItem(
         5,
         2,
         ItemBuilder.of(XMaterial.GLOWSTONE_DUST)
            .setDisplayName("\u00a7aFirst Opening Phase")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "&7By clicking here you will be able",
                        "&7to change the animation that will be played",
                        "&7on the first phase of the opening animation.",
                        "",
                        "&fCurrent Animation: &7%animation%",
                        "",
                        "&6Left-Click: &eEdit animation.",
                        "&6Right-Click: &eRemove animation."
                     }
                  ),
                  "%animation%",
                  data.getModelId().equalsIgnoreCase("none") ? "None" : data.getFirstPhaseAnimation()
               )
            ),
         event -> {
            if (event.isLeftClick()) {
               MegModelBlueprint blueprint = ModelEngine.getApi().getModelRegistry().getBlueprint(data.getModelId());
               if (blueprint == null) {
                  return;
               }

               List<SelectOptionMenu.OptionItem<String>> options = Lists.newArrayList();

               for (String animation : blueprint.getAnimations().keySet()) {
                  options.add(
                     new SelectOptionMenu.OptionItem<>(
                        animation,
                        XMaterial.PAPER,
                        Translations.r("&a%name%", "%name%", animation),
                        Translations.r(Lists.newArrayList(new String[]{"", "&6Left-Click: &eSelect model."}))
                     )
                  );
               }

               SelectOptionMenu<String> optionSelector = new SelectOptionMenu<>(this.plugin, this.getTitle(), options, 3, menuName -> {
                  data.setFirstPhaseAnimation(menuName);
                  this.save(player);
               });
               optionSelector.setBackConsumer(__ -> this.open(player));
               optionSelector.open(player);
            } else if (event.isRightClick()) {
               data.setFirstPhaseAnimation("none");
               this.save(player);
            }
         }
      );
      this.setItem(
         6,
         2,
         ItemBuilder.of(XMaterial.GLOWSTONE_DUST)
            .setDisplayName("\u00a7aSecond Opening Phase")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "&7By clicking here you will be able",
                        "&7to change the animation that will be played",
                        "&7on the first phase of the opening animation.",
                        "",
                        "&fCurrent Animation: &7%animation%",
                        "",
                        "&6Left-Click: &eEdit animation.",
                        "&6Right-Click: &eRemove animation."
                     }
                  ),
                  "%animation%",
                  data.getModelId().equalsIgnoreCase("none") ? "None" : data.getSecondPhaseAnimation()
               )
            ),
         event -> {
            if (event.isLeftClick()) {
               MegModelBlueprint blueprint = ModelEngine.getApi().getModelRegistry().getBlueprint(data.getModelId());
               if (blueprint == null) {
                  return;
               }

               List<SelectOptionMenu.OptionItem<String>> options = Lists.newArrayList();

               for (String animation : blueprint.getAnimations().keySet()) {
                  options.add(
                     new SelectOptionMenu.OptionItem<>(
                        animation,
                        XMaterial.PAPER,
                        Translations.r("&a%name%", "%name%", animation),
                        Translations.r(Lists.newArrayList(new String[]{"", "&6Left-Click: &eSelect model."}))
                     )
                  );
               }

               SelectOptionMenu<String> optionSelector = new SelectOptionMenu<>(this.plugin, this.getTitle(), options, 3, menuName -> {
                  data.setSecondPhaseAnimation(menuName);
                  this.save(player);
               });
               optionSelector.setBackConsumer(__ -> this.open(player));
               optionSelector.open(player);
            } else if (event.isRightClick()) {
               data.setSecondPhaseAnimation("none");
               this.save(player);
            }
         }
      );
      this.setItem(
         7,
         2,
         ItemBuilder.of(XMaterial.GLOWSTONE_DUST)
            .setDisplayName("\u00a7aThird Opening Phase")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "&7By clicking here you will be able",
                        "&7to change the animation that will be played",
                        "&7on the third phase of the opening animation.",
                        "",
                        "&fCurrent Animation: &7%animation%",
                        "",
                        "&6Left-Click: &eEdit animation.",
                        "&6Right-Click: &eRemove animation."
                     }
                  ),
                  "%animation%",
                  data.getModelId().equalsIgnoreCase("none") ? "None" : data.getThirdPhaseAnimation()
               )
            ),
         event -> {
            if (event.isLeftClick()) {
               MegModelBlueprint blueprint = ModelEngine.getApi().getModelRegistry().getBlueprint(data.getModelId());
               if (blueprint == null) {
                  return;
               }

               List<SelectOptionMenu.OptionItem<String>> options = Lists.newArrayList();

               for (String animation : blueprint.getAnimations().keySet()) {
                  options.add(
                     new SelectOptionMenu.OptionItem<>(
                        animation,
                        XMaterial.PAPER,
                        Translations.r("&a%name%", "%name%", animation),
                        Translations.r(Lists.newArrayList(new String[]{"", "&6Left-Click: &eSelect model."}))
                     )
                  );
               }

               SelectOptionMenu<String> optionSelector = new SelectOptionMenu<>(this.plugin, this.getTitle(), options, 3, menuName -> {
                  data.setThirdPhaseAnimation(menuName);
                  this.save(player);
               });
               optionSelector.setBackConsumer(__ -> this.open(player));
               optionSelector.open(player);
            } else if (event.isRightClick()) {
               data.setThirdPhaseAnimation("none");
               this.save(player);
            }
         }
      );
      this.setItem(
         8,
         2,
         ItemBuilder.of(XMaterial.REDSTONE)
            .setDisplayName("\u00a7aClose Animation")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "&7By clicking here you will be able",
                        "&7to define the idle animation for this model.",
                        "",
                        "&fCurrent Animation: &7%animation%",
                        "",
                        "&6Left-Click: &eEdit animation.",
                        "&6Right-Click: &eRemove animation."
                     }
                  ),
                  "%animation%",
                  data.getModelId().equalsIgnoreCase("none") ? "None" : data.getCloseAnimation()
               )
            ),
         event -> {
            if (event.isLeftClick()) {
               MegModelBlueprint blueprint = ModelEngine.getApi().getModelRegistry().getBlueprint(data.getModelId());
               if (blueprint == null) {
                  return;
               }

               List<SelectOptionMenu.OptionItem<String>> options = Lists.newArrayList();

               for (String animation : blueprint.getAnimations().keySet()) {
                  options.add(
                     new SelectOptionMenu.OptionItem<>(
                        animation,
                        XMaterial.PAPER,
                        Translations.r("&a%name%", "%name%", animation),
                        Translations.r(Lists.newArrayList(new String[]{"", "&6Left-Click: &eSelect model."}))
                     )
                  );
               }

               SelectOptionMenu<String> optionSelector = new SelectOptionMenu<>(this.plugin, this.getTitle(), options, 3, menuName -> {
                  data.setCloseAnimation(menuName);
                  this.save(player);
               });
               optionSelector.setBackConsumer(__ -> this.open(player));
               optionSelector.open(player);
            } else if (event.isRightClick()) {
               data.setCloseAnimation("none");
               this.save(player);
            }
         }
      );
      this.setItem(
         5,
         3,
         ItemBuilder.of(XMaterial.CLOCK)
            .setDisplayName("\u00a7aFirst Phase Start Delay")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "&7By clicking here you will be able",
                        "&7to set the delay the phase animation will wait to start.",
                        "",
                        "&e&nThis delay don't apply to the model",
                        "&e&nanimation and only the phase animation.",
                        "",
                        "&fStart Delay: &7%delay%s",
                        "",
                        "&6Left-Click: &eAdd 0.1.",
                        "&6Shift + Left-Click: &eAdd 1.",
                        "&6Right-Click: &eRemove 0.1.",
                        "&6Shift + Right-Click: &eRemove 1."
                     }
                  ),
                  "%delay%",
                  Constants.DECIMAL1_FORMAT.format(data.getFirstPhaseStartDelay())
               )
            ),
         event -> {
            double offset = data.getFirstPhaseStartDelay();
            if (event.isLeftClick()) {
               offset += event.isShiftClick() ? 1.0 : 0.1;
            } else if (event.isRightClick()) {
               offset -= event.isShiftClick() ? 1.0 : 0.1;
            }

            data.setFirstPhaseStartDelay(Math.max(offset, 0.0));
            this.save(player);
         }
      );
      this.setItem(
         6,
         3,
         ItemBuilder.of(XMaterial.CLOCK)
            .setDisplayName("\u00a7aSecond Phase Start Delay")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "&7By clicking here you will be able",
                        "&7to set the delay the phase animation will wait to start.",
                        "",
                        "&e&nThis delay don't apply to the model",
                        "&e&nanimation and only the phase animation.",
                        "",
                        "&fStart Delay: &7%delay%s",
                        "",
                        "&6Left-Click: &eAdd 0.1.",
                        "&6Shift + Left-Click: &eAdd 1.",
                        "&6Right-Click: &eRemove 0.1.",
                        "&6Shift + Right-Click: &eRemove 1."
                     }
                  ),
                  "%delay%",
                  Constants.DECIMAL1_FORMAT.format(data.getSecondPhaseStartDelay())
               )
            ),
         event -> {
            double offset = data.getSecondPhaseStartDelay();
            if (event.isLeftClick()) {
               offset += event.isShiftClick() ? 1.0 : 0.1;
            } else if (event.isRightClick()) {
               offset -= event.isShiftClick() ? 1.0 : 0.1;
            }

            data.setSecondPhaseStartDelay(Math.max(offset, 0.0));
            this.save(player);
         }
      );
      this.setItem(
         7,
         3,
         ItemBuilder.of(XMaterial.CLOCK)
            .setDisplayName("\u00a7aThird Phase Start Delay")
            .setLore(
               Translations.r(
                  Lists.newArrayList(
                     new String[]{
                        "&7By clicking here you will be able",
                        "&7to set the delay the phase animation will wait to start.",
                        "",
                        "&e&nThis delay don't apply to the model",
                        "&e&nanimation and only the phase animation.",
                        "",
                        "&fStart Delay: &7%delay%s",
                        "",
                        "&6Left-Click: &eAdd 0.1.",
                        "&6Shift + Left-Click: &eAdd 1.",
                        "&6Right-Click: &eRemove 0.1.",
                        "&6Shift + Right-Click: &eRemove 1."
                     }
                  ),
                  "%delay%",
                  Constants.DECIMAL1_FORMAT.format(data.getThirdPhaseStartDelay())
               )
            ),
         event -> {
            double offset = data.getThirdPhaseStartDelay();
            if (event.isLeftClick()) {
               offset += event.isShiftClick() ? 1.0 : 0.1;
            } else if (event.isRightClick()) {
               offset -= event.isShiftClick() ? 1.0 : 0.1;
            }

            data.setThirdPhaseStartDelay(Math.max(offset, 0.0));
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
}
