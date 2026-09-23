package id.seria.gacha.lib.common.uicomponents.newest.layout.component;

import com.google.common.collect.Lists;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import java.util.List;

public class SimpleComponent extends ClickableComponent<SimpleComponent> {
   @Deprecated
   public SimpleComponent(ComponentData data) {
      this(new SlotCompound(data.getSlot()), data);
   }

   public SimpleComponent(SlotCompound slot, ComponentData data) {
      super(slot, data);
   }

   @Override
   public void onPrepare(ContainerView ignored) {
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext context) {
      return Lists.newArrayList();
   }
}
