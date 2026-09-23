package id.seria.gacha.editor.layouts.crate.rewards.reward.alternative;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.CloseViewContext;
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
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomAlternativeReward;
import id.seria.gacha.managers.reward.CustomReward;
import lombok.Generated;

public class EditorCustomAlternativeRewardItemLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;
   private final CustomReward parentReward;
   private final CustomAlternativeReward reward;

   public EditorCustomAlternativeRewardItemLayout(CrateType crateType, CustomReward parentReward, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.reward"), parent);
      this.crateType = crateType;
      this.parentReward = parentReward;
      this.reward = parentReward.getAlternative();
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(
         view,
         new EditableItem(
               SlotCompound.from(3, 2),
               this.createData(
                  XMaterial.AIR,
                  Translatable.key("editor.crate.reward.alternative-item.title"),
                  Translatable.key("editor.crate.reward.alternative-item.description")
               ),
               Operations.safeBuild(this.reward.getItem())
            )
            .setOnValueChange(value -> {
               this.reward.setItem(BaseItem.from(value));
               EditorFacade.saveCrate(view, this.crateType, this.parentReward);
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
               Operations.safeName(this.reward.getItem())
            )
            .setFormatter(value -> value.isEmpty() ? Translatable.key("labels.default") : Translatable.literal(BukkitUtil.translateColorCodes(value)))
            .setOnValueChange(value -> {
               this.reward.getItem().setCustomName(value);
               EditorFacade.saveCrate(view, this.crateType, this.parentReward);
            })
            .setLocked(ctx -> this.reward.getDisplayItem() == null)
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
               Operations.safeLore(this.reward.getItem())
            )
            .setOnValueChange(value -> {
               this.reward.getItem().setCustomLore(value);
               EditorFacade.saveCrate(view, this.crateType, this.parentReward);
            })
            .setLocked(ctx -> this.reward.getItem() == null)
      );
   }

   @Override
   public void onClose(CloseViewContext context) {
      EditorFacade.saveCrate(context.getHolder(), this.crateType, this.parentReward);
   }

   @Generated
   public CrateType getCrateType() {
      return this.crateType;
   }

   @Generated
   public CustomReward getParentReward() {
      return this.parentReward;
   }

   @Generated
   public CustomAlternativeReward getReward() {
      return this.reward;
   }
}
