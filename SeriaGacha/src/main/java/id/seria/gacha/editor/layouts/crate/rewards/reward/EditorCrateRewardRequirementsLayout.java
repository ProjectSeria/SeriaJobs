package id.seria.gacha.editor.layouts.crate.rewards.reward;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.CloseViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableStringList;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomReward;
import lombok.Generated;

public class EditorCrateRewardRequirementsLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;
   private final CustomReward reward;

   public EditorCrateRewardRequirementsLayout(CrateType crateType, CustomReward reward, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.reward-requirements"), parent);
      this.crateType = crateType;
      this.reward = reward;
      this.setViewPersistentOnGoBack(true);
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(
         view,
         new EditableStringList(
               SlotCompound.from(4, 2),
               this.createData(
                  XMaterial.GLOBE_BANNER_PATTERN.or(XMaterial.PAPER),
                  Translatable.key("editor.crate.reward.restricted-permissions.title"),
                  Translatable.key("editor.crate.reward.restricted-permissions.description")
               ),
               this.label("permission"),
               this.reward.getRestrictedPermissions()
            )
            .setOnValueChange(value -> {
               this.reward.setRestrictedPermissions(value);
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
      );
      this.addComponent(
         view,
         new EditableStringList(
               SlotCompound.from(6, 2),
               this.createData(
                  XMaterial.TRIPWIRE_HOOK,
                  Translatable.key("editor.crate.reward.requirements.required-permission.title"),
                  Translatable.key("editor.crate.reward.requirements.required-permission.description")
               ),
               this.label("permission"),
               this.reward.getRequiredPermissions()
            )
            .setOnValueChange(value -> {
               this.reward.setRequiredPermissions(value);
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
      );
   }

   @Override
   public void onClose(CloseViewContext context) {
      EditorFacade.saveCrate(context.getHolder(), this.crateType, this.reward);
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
