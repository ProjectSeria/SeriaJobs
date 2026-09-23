package id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.bukkit;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.data.MutableData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.EditableObject;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EditableItemAppearance extends EditableObject<ItemStack> {
   private XMaterial fillItem = XMaterial.BLACK_STAINED_GLASS_PANE;

   public EditableItemAppearance(SlotCompound slot, ComponentData data, ItemStack object) {
      super(slot, data, null, object);
      this.prepareActions();
   }

   public EditableItemAppearance(SlotCompound slot, ComponentData data, MutableData<ItemStack> mutableData) {
      super(slot, data, null, mutableData);
      this.prepareActions();
   }

   private void prepareActions() {
      this.setClickAction(ClickAction.REPLACE(ClickAction.ClickType.DRAG_DROP, Translatable.key("labels.material")), context -> {
         if (context.getCursor() != null && context.getCursor().getType() != Material.AIR) {
            ItemStack appearance = extractAppearance(context.getCursor());
            this.safeSetValue(context.getContainerView(), appearance, true);
         }
      });
   }

   private static ItemStack extractAppearance(ItemStack source) {
      ItemStack clone = source.clone();
      clone.setAmount(1);
      return clone;
   }

   @Override
   public void onPrepare(ContainerView view) {
      view.setCancelOnOtherInventoryClick(false);
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext context) {
      List<String> display = new ArrayList<>();
      ItemStack value = this.getValue().get();
      if (value != null && value.getType() != Material.AIR) {
         display.add("\u00a7fMaterial: \u00a77" + StringUtil.humanize(value.getType().name()));
         ItemBuilder builder = ItemBuilder.of(value);
         int cmd = builder.getCustomModelData();
         if (cmd > 0) {
            display.add("\u00a7fCustom Model Data: \u00a77" + cmd);
         }

         String itemModel = builder.getItemModel();
         if (itemModel != null && !itemModel.isEmpty()) {
            display.add("\u00a7fItem Model: \u00a77" + itemModel);
         }

         return display;
      } else {
         display.add("\u00a77None");
         return display;
      }
   }

   @Override
   public ComponentData getData() {
      ItemStack value = this.getValue().get();
      super.getData().setItem(value != null && value.getType() != Material.AIR ? value : this.fillItem.parseItem());
      return super.getData();
   }

   @Generated
   public XMaterial getFillItem() {
      return this.fillItem;
   }

   @Generated
   public EditableItemAppearance setFillItem(XMaterial fillItem) {
      this.fillItem = fillItem;
      return this;
   }
}
