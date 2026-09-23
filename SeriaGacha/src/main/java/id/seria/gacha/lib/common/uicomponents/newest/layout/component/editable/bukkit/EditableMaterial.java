package id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.bukkit;

import com.google.common.collect.Lists;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditablePopupList;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.List;
import java.util.function.Supplier;
import org.bukkit.Material;

public class EditableMaterial extends EditablePopupList<XMaterial> {
   @Deprecated
   public EditableMaterial(ComponentData data, List<XMaterial> options, Supplier<XMaterial> object) {
      this(new SlotCompound(data.getSlot()), data, options, object.get());
   }

   public EditableMaterial(SlotCompound slot, ComponentData data, List<XMaterial> options, XMaterial object) {
      super(slot, data, Translatable.key("labels.material"), options, object);
      this.options.removeIf(material -> material.parseMaterial() == null || material.parseMaterial() == Material.AIR);
      this.setParser(material -> new EditablePopupList.ListItem<>(material, material, StringUtil.humanize(material), Lists.newArrayList()));
   }

   @Override
   public ComponentData getData() {
      super.getData().setItem(this.getValue().get().parseItem());
      return super.getData();
   }

   public void setValue(XMaterial object) throws Exception {
      this.setValue(object, true);
   }

   public void setValue(XMaterial object, boolean applyModifiers) throws Exception {
      XMaterial processedObject = applyModifiers ? this.applyValueModifiers(object) : object;
      this.setRawValue(processedObject);
      if (this.onValueChange != null) {
         this.onValueChange.accept(this.getValue().get());
      }
   }
}
