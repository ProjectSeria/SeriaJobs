package id.seria.gacha.editor.layouts.crate.rewards.reward;

import id.seria.gacha.api.internal.Item;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.NavigableComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.bukkit.EditableItem;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableStringList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableString;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Operations;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomReward;
import java.util.ArrayList;
import java.util.Optional;
import lombok.Generated;

public class EditorCrateRewardDisplayItemLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;
   private final CustomReward reward;

   public EditorCrateRewardDisplayItemLayout(CrateType crateType, CustomReward reward, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.reward"), parent);
      this.crateType = crateType;
      this.reward = reward;
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(
         view,
         new EditableItem(
               SlotCompound.from(5, 2),
               this.createData(
                  XMaterial.AIR, Translatable.key("editor.crate.reward.display-item.title"), Translatable.key("editor.crate.reward.display-item.description")
               ),
               Operations.o(this.reward.getDisplayItem()).map(Item::build).orElse(XMaterial.AIR.parseItem())
            )
            .setOnValueChange(value -> {
               this.crateType.invalidateRewardsCache();
               this.reward.setDisplayItem(BaseItem.from(value));
               if (this.reward.getWinItems().isEmpty()) {
                  this.reward.getWinItems().add(BaseItem.from(value));
               }

               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
      );
      this.addComponent(
         view,
         new EditableString(
               SlotCompound.from(2, 2),
               this.createData(
                  XMaterial.PAPER,
                  Translatable.key("editor.crate.reward.display-item.name.title"),
                  Translatable.key("editor.crate.reward.display-item.name.description")
               ),
               this.label("display-name"),
               Optional.ofNullable(this.reward.getDisplayItem()).map(BaseItem::getCustomName).orElse("")
            )
            .setFormatter(
               value -> value.isEmpty() ? Translatable.key("labels.default") : Translatable.literal(Operations.safeName(this.reward.getDisplayItem()))
            )
            .setOnValueChange(value -> {
               this.reward.getDisplayItem().setCustomName(value);
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
            .setLocked(ctx -> this.reward.getDisplayItem() == null)
      );
      this.addComponent(
         view,
         new EditableStringList(
               SlotCompound.from(3, 2),
               this.createData(
                  ItemBuilder.of(XMaterial.GLOBE_BANNER_PATTERN.or(XMaterial.PAPER)).addItemFlags(Constants.ITEM_FLAGS),
                  Translatable.key("editor.crate.reward.display-item.lore.title"),
                  Translatable.key("editor.crate.reward.display-item.lore.description")
               ),
               this.label("line"),
               Optional.ofNullable(this.reward.getDisplayItem()).map(BaseItem::getCustomLore).orElseGet(ArrayList::new)
            )
            .setFormatter(
               value -> value.isEmpty() ? Translatable.key("labels.default") : Translatable.literal(Operations.safeLore(this.reward.getDisplayItem()))
            )
            .setOnValueChange(value -> {
               this.reward.getDisplayItem().setCustomLore(value);
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
            .setLocked(ctx -> this.reward.getDisplayItem() == null)
      );
      this.addComponent(
         view,
         new NavigableComponent(
            SlotCompound.from(7, 2),
            this.createData(
               XMaterial.COMPARATOR,
               Translatable.literal("Item Properties"),
               Translatable.literal(
                  "Edit RGB, item flags, banner patterns,", "enchantments, model data, potion effects,", "armor trim, tooltip style, and more."
               )
            ),
            new EditorCrateRewardDisplayItemAdvancedLayout(this.crateType, this.reward, this)
         )
      );
      this.addComponent(
         view,
         this.createComponent(
            SlotCompound.from(8, 2), this.createData(XMaterial.GRAY_STAINED_GLASS_PANE, Translatable.literal(""), Translatable.literal(new ArrayList()))
         )
      );
   }

   @Generated
   public CrateType getCrateType() {
      return this.crateType;
   }

   @Generated
   public CustomReward getReward() {
      return this.reward;
   }
}
