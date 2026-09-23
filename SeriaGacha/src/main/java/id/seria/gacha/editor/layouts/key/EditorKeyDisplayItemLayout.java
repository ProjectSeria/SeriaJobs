package id.seria.gacha.editor.layouts.key;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.bukkit.EditableItem;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableStringList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableString;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.BukkitUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Operations;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.managers.keys.Key;
import java.util.ArrayList;
import java.util.Optional;
import lombok.Generated;

public class EditorKeyDisplayItemLayout extends EditorFacade.EditorLayout {
   private final Key key;

   public EditorKeyDisplayItemLayout(Key key, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.key.show"), parent);
      this.key = key;
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(
         view,
         new EditableItem(
               SlotCompound.from(3, 2),
               this.createData(
                  XMaterial.AIR, Translatable.key("editor.crate.reward.display-item.title"), Translatable.key("editor.crate.reward.display-item.description")
               ),
               Operations.safeBuild(this.key.getPlainItem())
            )
            .setOnValueChange(value -> {
               this.key.setPlainItem(BaseItem.from(value));
               EditorFacade.saveKey(view, this.key);
            })
      );
      this.addComponent(
         view,
         new EditableString(
               SlotCompound.from(5, 2),
               this.createData(
                  XMaterial.PAPER,
                  Translatable.key("editor.crate.reward.display-item.name.title"),
                  Translatable.key("editor.crate.reward.display-item.name.description")
               ),
               this.label("display-name"),
               Optional.ofNullable(this.key.getPlainItem()).map(BaseItem::getCustomName).orElse("")
            )
            .setFormatter(value -> value.isEmpty() ? Translatable.key("labels.default") : Translatable.literal(BukkitUtil.translateColorCodes(value)))
            .setOnValueChange(value -> {
               this.key.getPlainItem().setCustomName(value);
               EditorFacade.saveKey(view, this.key);
            })
      );
      this.addComponent(
         view,
         new EditableStringList(
               SlotCompound.from(7, 2),
               this.createData(
                  ItemBuilder.of(XMaterial.GLOBE_BANNER_PATTERN.or(XMaterial.PAPER)).addItemFlags(Constants.ITEM_FLAGS),
                  Translatable.key("editor.crate.reward.display-item.lore.title"),
                  Translatable.key("editor.crate.reward.display-item.lore.description")
               ),
               this.label("line"),
               Optional.ofNullable(this.key.getPlainItem()).map(BaseItem::getCustomLore).orElseGet(ArrayList::new)
            )
            .setOnValueChange(value -> {
               this.key.getPlainItem().setCustomLore(value);
               EditorFacade.saveKey(view, this.key);
            })
      );
   }

   @Generated
   public Key getKey() {
      return this.key;
   }
}
