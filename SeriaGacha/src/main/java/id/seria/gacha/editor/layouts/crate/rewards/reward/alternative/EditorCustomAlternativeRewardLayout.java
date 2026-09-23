package id.seria.gacha.editor.layouts.crate.rewards.reward.alternative;

import id.seria.gacha.Crates;
import id.seria.gacha.api.internal.Item;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.NavigableComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.bukkit.EditableItem;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableBoolean;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.Operations;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomAlternativeReward;
import id.seria.gacha.managers.reward.CustomReward;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EditorCustomAlternativeRewardLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;
   private final CustomReward parentReward;
   private final CustomAlternativeReward reward;

   public EditorCustomAlternativeRewardLayout(CrateType crateType, CustomReward parentReward, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.reward"), parent);
      this.crateType = crateType;
      this.parentReward = parentReward;
      this.reward = parentReward.getAlternative();
   }

   @Override
   public void onPrepare(ContainerView view) {
      boolean lite = Crates.getPlugin().getEdition() == Edition.LITE;
      this.addComponent(
         view,
         new EditableItem(
               SlotCompound.from(3, 2),
               this.createData(
                  XMaterial.AIR,
                  Translatable.key("editor.crate.reward.alternative-item.title"),
                  Translatable.key("editor.crate.reward.alternative-item.description")
               ),
               Operations.o(this.reward.getDisplayItem()).map(Item::build).orElse(new ItemStack(Material.DIRT))
            )
            .setClickAction(
               ClickAction.EDIT(ClickAction.ClickType.LEFT, Translatable.key("labels.item")),
               (object, context) -> new EditorCustomAlternativeRewardItemLayout(this.crateType, this.parentReward, this).open(context.getViewer())
            )
            .setOnValueChange(value -> {
               this.reward.setItem(BaseItem.from(value));
               EditorFacade.saveCrate(view, this.crateType, this.parentReward);
            })
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(4, 2),
               this.createData(
                  XMaterial.END_PORTAL_FRAME,
                  Translatable.key("editor.crate.reward.virtual.title"),
                  Translatable.key("editor.crate.reward.virtual.description")
               ),
               this.label("virtual"),
               this.reward.isVirtual()
            )
            .setOnValueChange(value -> {
               this.reward.setVirtual(value);
               EditorFacade.saveCrate(view, this.crateType, this.parentReward);
            })
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         new NavigableComponent(
            SlotCompound.from(5, 2),
            this.createData(
               XMaterial.COMMAND_BLOCK,
               Translatable.key("editor.crate.reward.win-commands.title"),
               Translatable.key("editor.crate.reward.win-commands.description")
            ),
            new EditorFacade.LegacyEditorWrapper(new EditorCustomAlternativeRewardCommandsMenu(this.crateType, this.parentReward, this))
         )
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(6, 2),
               this.createData(
                  XMaterial.NETHER_STAR, Translatable.key("editor.crate.reward.broadcast.title"), Translatable.key("editor.crate.reward.broadcast.description")
               ),
               this.label("broadcast"),
               this.reward.isBroadcast()
            )
            .setOnValueChange(value -> {
               this.reward.setBroadcast(value);
               EditorFacade.saveCrate(view, this.crateType, this.parentReward);
            })
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(7, 2),
               this.createData(
                  XMaterial.EMERALD,
                  Translatable.key("editor.crate.reward.share-players-online.title"),
                  Translatable.key("editor.crate.reward.share-players-online.description")
               ),
               this.label("sharing"),
               this.reward.isSharePlayersOnline()
            )
            .setOnValueChange(value -> {
               this.reward.setSharePlayersOnline(value);
               EditorFacade.saveCrate(view, this.crateType, this.parentReward);
            })
            .addError(lite, Translatable.key("plans.unlock-premium"))
            .addError(lite, Translatable.key("plans.upgrade-at"))
            .setLocked(ctx -> lite)
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(3, 4),
               this.createData(
                  XMaterial.LIME_DYE,
                  Translatable.key("editor.alternative-reward.enabled.title"),
                  Translatable.key("editor.alternative-reward.enabled.description")
               ),
               this.label("enabled"),
               this.reward.isEnabled()
            )
            .setOnValueChange(value -> {
               this.reward.setEnabled(value);
               EditorFacade.saveCrate(view, this.crateType, this.parentReward);
            })
            .setFixedPosition(true)
      );
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
