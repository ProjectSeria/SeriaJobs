package id.seria.gacha.editor.layouts.crate.menus;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.actions.Actions;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionListComponent;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditablePopupList;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.menus.MenuFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class EditorCrateMenusLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;

   public EditorCrateMenusLayout(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.menus"), parent);
      this.crateType = crateType;
   }

   @Deprecated
   @Override
   public void onPrepare(ContainerView view) {
      List<String> rewardsMenuOptions = new ArrayList<>();
      rewardsMenuOptions.add("");
      rewardsMenuOptions.addAll(
         MenuFactory.getInstance()
            .getConfigurations()
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().getSettings().getType().equalsIgnoreCase("REWARDS_PREVIEW"))
            .map(Entry::getKey)
            .collect(Collectors.toList())
      );
      this.addComponent(
         view,
         new EditablePopupList<>(
               SlotCompound.from(2, 2),
               this.createData(
                  XMaterial.EMERALD, Translatable.key("editor.crate.rewards-menu.title"), Translatable.key("editor.crate.rewards-menu.description")
               ),
               this.label("menu"),
               rewardsMenuOptions,
               this.crateType.getRewardsMenuName()
            )
            .setParser(
               menu -> menu.isEmpty()
                  ? new EditablePopupList.ListItem<>(
                     menu, XMaterial.BARRIER, "\u00a7c" + this.t("labels.none")[0], Lists.newArrayList(new String[]{"\u00a77Disables the preview menu."})
                  )
                  : new EditablePopupList.ListItem<>(
                     menu, XMaterial.PAPER, StringUtil.humanize(menu), Lists.newArrayList(new String[]{"\u00a77" + menu + ".yml"})
                  )
            )
            .setFormatter(
               menu -> Translatable.literal(this.crateType.isPreviewMenuEnabled() ? this.crateType.getRewardsMenuName() : "\u00a7c" + this.t("labels.none")[0])
            )
            .setClickAction(
               ClickAction.REMOVE(ClickAction.ClickType.RIGHT, this.label("menu")),
               (editable, context) -> editable.safeSetValue(context.getContainerView(), "", true)
            )
            .setOnValueChange(value -> {
               this.crateType.setRewardsMenuName(value);
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
      this.addComponent(
         view,
         new ActionListComponent(
               SlotCompound.from(4, 2),
               this.createData(
                  XMaterial.REDSTONE, Translatable.key("editor.crate.preview-actions.title"), Translatable.key("editor.crate.preview-actions.description")
               ),
               Actions.getBuilders(),
               this.crateType.getPreviewActions()
            )
            .setOnValueChange(value -> {
               this.crateType.setPreviewActions(value);
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
      this.addComponent(
         view,
         new EditablePopupList<>(
               SlotCompound.from(6, 2),
               this.createData(
                  XMaterial.AMETHYST_SHARD.or(XMaterial.ENDER_EYE),
                  Translatable.key("editor.crate.selective-rewards-menu.title"),
                  Translatable.key("editor.crate.selective-rewards-menu.description")
               ),
               this.label("menu"),
               MenuFactory.getInstance()
                  .getConfigurations()
                  .entrySet()
                  .stream()
                  .filter(entry -> entry.getValue().getSettings().getType().equalsIgnoreCase("SELECT_REWARD"))
                  .map(Entry::getKey)
                  .collect(Collectors.toList()),
               this.crateType.getSelectRewardMenuName()
            )
            .setParser(
               menu -> new EditablePopupList.ListItem<>(
                  menu, XMaterial.PAPER, StringUtil.humanize(menu), Lists.newArrayList(new String[]{"\u00a77" + menu + ".yml"})
               )
            )
            .setClickAction(
               ClickAction.REMOVE(ClickAction.ClickType.RIGHT, this.label("menu")),
               (editable, context) -> editable.safeSetValue(context.getContainerView(), "", true)
            )
            .setOnValueChange(value -> {
               this.crateType.setSelectRewardMenuName(value);
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
      if (Crates.getPlugin().getEdition() == Edition.LITE) {
         this.addComponent(
            view,
            new EditablePopupList<>(
                  SlotCompound.from(8, 2),
                  this.createData(
                     XMaterial.NAUTILUS_SHELL, Translatable.key("editor.crate.rerolls-menu.title"), Translatable.key("editor.crate.rerolls-menu.description")
                  ),
                  this.label("menu"),
                  new ArrayList<>(),
                  "default_crate_reroll"
               )
               .setParser(
                  menu -> new EditablePopupList.ListItem<>(
                     menu, XMaterial.PAPER, StringUtil.humanize(menu), Lists.newArrayList(new String[]{"\u00a77" + menu + ".yml"})
                  )
               )
               .setClickAction(
                  ClickAction.REMOVE(ClickAction.ClickType.RIGHT, this.label("menu")),
                  (editable, context) -> editable.safeSetValue(context.getContainerView(), "", true)
               )
               .setOnValueChange(value -> {})
               .addError(() -> true, this.t("editor.errors.only-premium")[0])
               .setLocked(ctx -> true)
               .setFixedPosition(true)
         );
      } else {
         this.addComponent(
            view,
            new EditablePopupList<>(
                  SlotCompound.from(8, 2),
                  this.createData(
                     XMaterial.NAUTILUS_SHELL, Translatable.key("editor.crate.rerolls-menu.title"), Translatable.key("editor.crate.rerolls-menu.description")
                  ),
                  this.label("menu"),
                  MenuFactory.getInstance()
                     .getConfigurations()
                     .entrySet()
                     .stream()
                     .filter(entry -> entry.getValue().getSettings().getType().equalsIgnoreCase("CRATE_REROLL"))
                     .map(Entry::getKey)
                     .collect(Collectors.toList()),
                  this.crateType.getRerollsMenuName()
               )
               .setParser(
                  menu -> new EditablePopupList.ListItem<>(
                     menu, XMaterial.PAPER, StringUtil.humanize(menu), Lists.newArrayList(new String[]{"\u00a77" + menu + ".yml"})
                  )
               )
               .setClickAction(
                  ClickAction.REMOVE(ClickAction.ClickType.RIGHT, this.label("menu")),
                  (editable, context) -> editable.safeSetValue(context.getContainerView(), "", true)
               )
               .setOnValueChange(value -> {
                  this.crateType.setRerollsMenuName(value);
                  EditorFacade.saveCrate(view, this.crateType);
               })
               .setFixedPosition(true)
         );
      }
   }
}
