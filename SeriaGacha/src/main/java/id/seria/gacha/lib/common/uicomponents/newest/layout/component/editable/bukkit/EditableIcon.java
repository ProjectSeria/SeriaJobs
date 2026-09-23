package id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.bukkit;

import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.data.MutableData;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.bukkit.inventory.ItemStack;

public class EditableIcon extends EditableItem {
   @Deprecated
   public EditableIcon(ComponentData data, Supplier<ItemStack> object) {
      super(data, object);
   }

   public EditableIcon(SlotCompound slot, ComponentData data, ItemStack object) {
      super(slot, data, object);
   }

   public EditableIcon(SlotCompound slot, ComponentData data, MutableData<ItemStack> mutableData) {
      super(slot, data, mutableData);
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext context) {
      return Collections.emptyList();
   }
}
