package id.seria.gacha.editor.layouts.crate.animations.idle;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.LoreBuilder;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.SimplePaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XEnchantment;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.animations.idle.IdleEffect;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectType;
import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.inventory.ItemFlag;

public class EditorCrateIdleEffectShapeLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;
   private final IdleEffect effect;

   public EditorCrateIdleEffectShapeLayout(CrateType crateType, IdleEffect effect, EditorFacade.EditorLayout parent) {
      super(Translatable.literal("\u00a78Select Effect Shape"), parent);
      this.crateType = crateType;
      this.effect = effect;
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(view, new EditorCrateIdleEffectShapeLayout.ShapeGridComponent());
   }

   private class ShapeGridComponent extends SimplePaginatedList<IdleEffectType> {
      public ShapeGridComponent() {
         super(
            SlotCompound.fromGrid(10, 2, 7),
            new PagingData(PaginationSettings.ItemsAlign.LEFT, SlotCompound.from(1, 4), SlotCompound.from(9, 4)),
            __ -> new ArrayList<>(Arrays.asList(IdleEffectType.values()))
         );
         this.setOnItemRender(
            (context, index, item, type) -> {
               boolean active = EditorCrateIdleEffectShapeLayout.this.effect.getType() == type;
               XMaterial icon = type.getIcon();
               ItemBuilder display = ItemBuilder.of(icon.parseItem());
               if (active) {
                  display.addEnchant(XEnchantment.UNBREAKING.getEnchant(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS);
               }

               item.withItem(display)
                  .withDisplayName("\u00a7a" + StringUtil.humanize(type.name()) + (active ? " \u00a77(Selected)" : ""))
                  .withLore(
                     LoreBuilder.builder(EditorCrateIdleEffectShapeLayout.this)
                        .description("\u00a77" + type.getDescription())
                        .actions(new ClickAction(ClickAction.ClickType.LEFT, Translatable.literal("Select")))
                        .build()
                  );
            }
         );
         this.setOnItemClick((context, index, type) -> {
            if (context.isLeftClick()) {
               EditorCrateIdleEffectShapeLayout.this.effect.setType(type);
               EditorFacade.saveCrate(context.getContainerView(), EditorCrateIdleEffectShapeLayout.this.crateType);
               EditorCrateIdleEffectShapeLayout.this.getParent().open(context.getViewer());
            }
         });
      }
   }
}
