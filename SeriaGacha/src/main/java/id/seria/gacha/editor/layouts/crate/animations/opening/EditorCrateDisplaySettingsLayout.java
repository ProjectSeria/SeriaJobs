package id.seria.gacha.editor.layouts.crate.animations.opening;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableNumber;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.CrateType;
import org.bukkit.util.Vector;

public class EditorCrateDisplaySettingsLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;

   public EditorCrateDisplaySettingsLayout(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(Translatable.literal("\u00a78Display Settings"), parent);
      this.crateType = crateType;
   }

   @Override
   public void onPrepare(ContainerView view) {
      Vector itemPosition = this.crateType.getExtraSettings().getDisplayItemPosition();
      Vector namePosition = this.crateType.getExtraSettings().getDisplayItemNamePosition();
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(4, 2),
               this.createData(
                  XMaterial.ITEM_FRAME,
                  Translatable.literal("\u00a7aItem Position X"),
                  Translatable.literal("\u00a77Horizontal offset of the", "\u00a77reward display item.")
               ),
               this.label("offset-x"),
               itemPosition.getX()
            )
            .setOnValueChange(value -> {
               itemPosition.setX(value.doubleValue());
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(5, 2),
               this.createData(
                  XMaterial.ITEM_FRAME,
                  Translatable.literal("\u00a7aItem Position Y"),
                  Translatable.literal("\u00a77Vertical offset of the", "\u00a77reward display item.")
               ),
               this.label("offset-y"),
               itemPosition.getY()
            )
            .setOnValueChange(value -> {
               itemPosition.setY(value.doubleValue());
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(6, 2),
               this.createData(
                  XMaterial.ITEM_FRAME,
                  Translatable.literal("\u00a7aItem Position Z"),
                  Translatable.literal("\u00a77Depth offset of the", "\u00a77reward display item.")
               ),
               this.label("offset-z"),
               itemPosition.getZ()
            )
            .setOnValueChange(value -> {
               itemPosition.setZ(value.doubleValue());
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(4, 3),
               this.createData(
                  XMaterial.NAME_TAG,
                  Translatable.literal("\u00a7aName Position X"),
                  Translatable.literal("\u00a77Horizontal offset of the", "\u00a77display item's name.")
               ),
               this.label("offset-x"),
               namePosition.getX()
            )
            .setOnValueChange(value -> {
               namePosition.setX(value.doubleValue());
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(5, 3),
               this.createData(
                  XMaterial.NAME_TAG,
                  Translatable.literal("\u00a7aName Position Y"),
                  Translatable.literal("\u00a77Vertical offset of the", "\u00a77display item's name.")
               ),
               this.label("offset-y"),
               namePosition.getY()
            )
            .setOnValueChange(value -> {
               namePosition.setY(value.doubleValue());
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(6, 3),
               this.createData(
                  XMaterial.NAME_TAG,
                  Translatable.literal("\u00a7aName Position Z"),
                  Translatable.literal("\u00a77Depth offset of the", "\u00a77display item's name.")
               ),
               this.label("offset-z"),
               namePosition.getZ()
            )
            .setOnValueChange(value -> {
               namePosition.setZ(value.doubleValue());
               EditorFacade.saveCrate(view, this.crateType);
            })
      );
   }
}
