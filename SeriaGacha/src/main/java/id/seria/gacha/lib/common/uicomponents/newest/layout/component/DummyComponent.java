package id.seria.gacha.lib.common.uicomponents.newest.layout.component;

import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;

@Deprecated
public class DummyComponent extends SimpleComponent {
   @Deprecated
   public DummyComponent(ComponentData data) {
      this(new SlotCompound(data.getSlot()), data);
   }

   public DummyComponent(SlotCompound slot, ComponentData data) {
      super(slot, data);
   }
}
