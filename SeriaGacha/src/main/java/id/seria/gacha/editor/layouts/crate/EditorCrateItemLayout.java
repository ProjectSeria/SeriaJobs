package id.seria.gacha.editor.layouts.crate;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.bukkit.EditableItem;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableStringList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableString;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Operations;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.managers.crates.CrateType;
import java.util.ArrayList;
import java.util.Optional;

public class EditorCrateItemLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;

   public EditorCrateItemLayout(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.show"), parent);
      this.crateType = crateType;
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(
         view,
         new EditableItem(
               SlotCompound.from(3, 2),
               this.createData(XMaterial.AIR, Translatable.key("editor.crate.item.title"), Translatable.key("editor.crate.item.description")),
               Operations.safeBuild(this.crateType.getItem())
            )
            .setOnValueChange(value -> {
               this.crateType.setItem(BaseItem.from(value));
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
      this.addComponent(
         view,
         new EditableString(
               SlotCompound.from(5, 2),
               this.createData(XMaterial.PAPER, Translatable.key("editor.crate.item.name.title"), Translatable.key("editor.crate.item.name.description")),
               this.label("display-name"),
               Optional.ofNullable(this.crateType.getItem()).map(BaseItem::getCustomName).orElse("")
            )
            .setFormatter(value -> value.isEmpty() ? Translatable.key("labels.default") : Translatable.literal(Operations.safeName(this.crateType.getItem())))
            .setOnValueChange(value -> {
               this.crateType.setDisplayName(value);
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
      this.addComponent(
         view,
         new EditableStringList(
               SlotCompound.from(7, 2),
               this.createData(
                  ItemBuilder.of(XMaterial.GLOBE_BANNER_PATTERN.or(XMaterial.PAPER)).addItemFlags(Constants.ITEM_FLAGS),
                  Translatable.key("editor.crate.item.lore.title"),
                  Translatable.key("editor.crate.item.lore.description")
               ),
               this.label("line"),
               Optional.ofNullable(this.crateType.getItem()).map(BaseItem::getCustomLore).orElseGet(ArrayList::new)
            )
            .setFormatter(value -> value.isEmpty() ? Translatable.key("labels.default") : Translatable.literal(Operations.safeLore(this.crateType.getItem())))
            .setOnValueChange(value -> {
               this.crateType.getItem().setCustomLore(value);
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
   }
}
