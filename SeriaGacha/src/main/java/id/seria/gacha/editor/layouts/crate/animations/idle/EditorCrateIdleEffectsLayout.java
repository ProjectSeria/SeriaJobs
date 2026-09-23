package id.seria.gacha.editor.layouts.crate.animations.idle;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.LoreBuilder;
import id.seria.gacha.lib.common.nms.interfaces.particles.PropertyType;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.SimplePaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.utility.ColorUtil;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.animations.idle.IdleEffect;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectData;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectType;
import java.util.ArrayList;
import org.bukkit.entity.Player;

public class EditorCrateIdleEffectsLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;

   public EditorCrateIdleEffectsLayout(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.idle-animation"), parent);
      this.crateType = crateType;
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(view, new EditorCrateIdleEffectsLayout.EffectsGridComponent());
   }

   private class EffectsGridComponent extends SimplePaginatedList<IdleEffect> {
      public EffectsGridComponent() {
         super(
            SlotCompound.fromGrid(10, 2, 7),
            new PagingData(PaginationSettings.ItemsAlign.LEFT, SlotCompound.from(1, 4), SlotCompound.from(9, 4)),
            __ -> new ArrayList<>(EditorCrateIdleEffectsLayout.this.crateType.getIdleEffects())
         );
         this.setOnItemRender(
            (context, index, item, effect) -> {
               IdleEffectData data = effect.getData();
               boolean colorable = data.getParticle().hasProperty(PropertyType.COLORABLE);
               XMaterial icon = effect.getType().getIcon();
               item.withItem(icon)
                  .withDisplayName("\u00a7a" + StringUtil.humanize(effect.getType().name()) + " \u00a77#" + (index + 1))
                  .withLore(
                     LoreBuilder.builder(EditorCrateIdleEffectsLayout.this)
                        .description(
                           "\u00a77" + effect.getType().getDescription(),
                           "",
                           "\u00a7fParticle: \u00a77" + StringUtil.humanize(data.getParticle().name()),
                           "\u00a7fColor: \u00a77" + (colorable ? ColorUtil.getColorDisplayName(data.getColor()) : "N/A"),
                           "\u00a7fAmount: \u00a77" + data.getAmount() + "  \u00a7fVelocity: \u00a77" + Constants.DECIMAL1_FORMAT.format(data.getVelocity())
                        )
                        .actions(
                           new ClickAction(ClickAction.ClickType.LEFT, Translatable.literal("Change Shape")),
                           new ClickAction(ClickAction.ClickType.RIGHT, Translatable.literal("Edit Settings")),
                           ClickAction.REMOVE(ClickAction.ClickType.SHIFT_LEFT, EditorCrateIdleEffectsLayout.this.label("effect"))
                        )
                        .build()
                  );
            }
         );
         this.setOnItemClick((context, index, effect) -> {
            Player player = context.getViewer();
            if (context.isShiftClick() && context.isLeftClick()) {
               EditorCrateIdleEffectsLayout.this.crateType.getIdleEffects().remove(effect);
               EditorFacade.saveCrate(context.getContainerView(), EditorCrateIdleEffectsLayout.this.crateType);
               context.getContainerView().updateView();
            } else if (context.isRightClick()) {
               new EditorCrateIdleEffectSettingsLayout(EditorCrateIdleEffectsLayout.this.crateType, effect, EditorCrateIdleEffectsLayout.this).open(player);
            } else {
               if (context.isLeftClick()) {
                  new EditorCrateIdleEffectShapeLayout(EditorCrateIdleEffectsLayout.this.crateType, effect, EditorCrateIdleEffectsLayout.this).open(player);
               }
            }
         });
         this.setAddItemSlot(
            VirtualItem.from(XMaterial.LIME_STAINED_GLASS_PANE)
               .withDisplayName("\u00a7aAdd Effect")
               .withLore(
                  LoreBuilder.builder(EditorCrateIdleEffectsLayout.this)
                     .description("\u00a77Add a new particle effect to this crate.")
                     .actions(ClickAction.ADD(ClickAction.ClickType.LEFT, EditorCrateIdleEffectsLayout.this.label("effect")))
                     .build()
               )
               .withFlag(Constants.ITEM_FLAGS)
               .whenClick(
                  context -> {
                     if (context.isLeftClick() && !context.isShiftClick()) {
                        IdleEffect newEffect = new IdleEffect(IdleEffectType.CIRCLE, new IdleEffectData());
                        EditorCrateIdleEffectsLayout.this.crateType.getIdleEffects().add(newEffect);
                        EditorFacade.saveCrate(context.getContainerView(), EditorCrateIdleEffectsLayout.this.crateType);
                        new EditorCrateIdleEffectShapeLayout(EditorCrateIdleEffectsLayout.this.crateType, newEffect, EditorCrateIdleEffectsLayout.this)
                           .open(context.getViewer());
                     }
                  }
               )
         );
      }
   }
}
