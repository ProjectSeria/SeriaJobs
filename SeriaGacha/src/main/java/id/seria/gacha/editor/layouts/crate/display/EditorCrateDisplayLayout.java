package id.seria.gacha.editor.layouts.crate.display;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.editor.layouts.crate.animations.idle.EditorCrateIdleEffectsLayout;
import id.seria.gacha.editor.layouts.crate.animations.opening.EditorCrateOpeningAnimationLayout;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.NavigableComponent;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.CrateType;

public class EditorCrateDisplayLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;

   public EditorCrateDisplayLayout(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.display"), parent);
      this.crateType = crateType;
   }

   @Override
   public void onPrepare(ContainerView view) {
      view.setCancelOnOtherInventoryClick(false);
      this.addComponent(
         view,
         new NavigableComponent(
            SlotCompound.from(4, 2),
            this.createData(
               XMaterial.SLIME_BALL,
               Translatable.key("editor.crate.display-settings.idle-animation.title"),
               Translatable.key("editor.crate.display-settings.idle-animation.description")
            ),
            new EditorCrateIdleEffectsLayout(this.crateType, this)
         )
      );
      this.addComponent(
         view,
         new NavigableComponent(
            SlotCompound.from(6, 2),
            this.createData(
               XMaterial.MAGMA_CREAM,
               Translatable.key("editor.crate.display-settings.opening-animation.title"),
               Translatable.key("editor.crate.display-settings.opening-animation.description")
            ),
            new EditorCrateOpeningAnimationLayout(this.crateType, this)
         )
      );
   }
}
