package id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.EditableObject;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class EditableBoolean extends EditableObject<Boolean> {
   @Deprecated
   public EditableBoolean(ComponentData data, Translatable displayType, Supplier<Boolean> object) {
      this(new SlotCompound(data.getSlot()), data, displayType, object.get());
   }

   public EditableBoolean(SlotCompound slot, ComponentData data, Translatable displayType, Boolean object) {
      super(slot, data, displayType, object);
      this.setFormatter(value -> value ? Translatable.key("labels.yes-word") : Translatable.key("labels.no-word"));
      this.setClickAction(
         new ClickAction(ClickAction.ClickType.LEFT, Translatable.key("labels.switch-state")),
         context -> this.safeSetValue(context.getContainerView(), !this.getValue().get(), true)
      );
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext context) {
      return Arrays.asList(
         this._t(Translatable.key("layout.displays.state", "%type%", this.displayType, "%state%", this.formatter.apply(this.getValue().get())))
      );
   }

   @Override
   public ComponentData getData() {
      switch (XMaterial.matchXMaterial(super.getData().getItem().getType())) {
         case LIME_DYE:
         case GRAY_DYE:
            super.getData().setItem((this.getValue().get() ? XMaterial.LIME_DYE : XMaterial.GRAY_DYE).parseItem());
            break;
         case RED_STAINED_GLASS_PANE:
         case LIME_STAINED_GLASS_PANE:
            super.getData().setItem((this.getValue().get() ? XMaterial.LIME_STAINED_GLASS_PANE : XMaterial.RED_STAINED_GLASS_PANE).parseItem());
      }

      return super.getData();
   }
}
