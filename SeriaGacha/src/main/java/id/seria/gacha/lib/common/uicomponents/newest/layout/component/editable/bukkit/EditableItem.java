package id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.bukkit;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.api.sdk.text.Text;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.data.MutableData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.EditableObject;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EditableItem extends EditableObject<ItemStack> {
   private XMaterial fillItem = XMaterial.IRON_BARS;
   private boolean ignoreEmptyHand = true;

   @Deprecated
   public EditableItem(ComponentData data, Supplier<ItemStack> object) {
      this(new SlotCompound(data.getSlot()), data, object.get());
   }

   public EditableItem(SlotCompound slot, ComponentData data, ItemStack object) {
      super(slot, data, null, object);
      this.prepareActions();
   }

   public EditableItem(SlotCompound slot, ComponentData data, MutableData<ItemStack> mutableData) {
      super(slot, data, null, mutableData);
      this.prepareActions();
   }

   private void prepareActions() {
      this.setClickAction(ClickAction.REPLACE(ClickAction.ClickType.DRAG_DROP, Translatable.key("labels.item")), context -> {
         if (!this.ignoreEmptyHand || context.getCursor() != null && context.getCursor().getType() != Material.AIR) {
            this.safeSetValue(context.getContainerView(), context.getCursor().clone(), true);
         }
      });
   }

   @Override
   public void onPrepare(ContainerView view) {
      view.setCancelOnOtherInventoryClick(false);
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext context) {
      if (this.getValue().get() != null && this.getValue().get().getType() != Material.AIR) {
         RItemStack rItem = RItemStack.of(this.getValue().get());
         List<Text> lore = rItem.lore();
         return this.translate(
            Translatable.key(
               "layout.displays.item.populated",
               "%name%",
               rItem.displayName().toPlainText(),
               "%lore_if_empty%",
               lore.isEmpty() ? this._t("labels.empty") : "",
               "%lore%",
               lore.stream().map(Text::toPlainText).collect(Collectors.toList())
            )
         );
      } else {
         return this.translate(Translatable.key("layout.displays.item.empty"));
      }
   }

   protected List<String> translate(Translatable key) {
      return Arrays.asList(super._t(key));
   }

   @Override
   public ComponentData getData() {
      super.getData()
         .setItem(this.getValue().get() != null && this.getValue().get().getType() != Material.AIR ? this.getValue().get() : this.fillItem.parseItem());
      return super.getData();
   }

   @Generated
   public XMaterial getFillItem() {
      return this.fillItem;
   }

   @Generated
   public boolean isIgnoreEmptyHand() {
      return this.ignoreEmptyHand;
   }

   @Generated
   public EditableItem setFillItem(XMaterial fillItem) {
      this.fillItem = fillItem;
      return this;
   }

   @Generated
   public EditableItem setIgnoreEmptyHand(boolean ignoreEmptyHand) {
      this.ignoreEmptyHand = ignoreEmptyHand;
      return this;
   }
}
