package id.seria.gacha.editor.layouts.crate.animations.opening;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.LoreBuilder;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.SimplePaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.xseries.XEnchantment;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.type.list.OpeningAnimation;
import id.seria.gacha.registries.AnimationsRegistry;
import id.seria.gacha.registries.animation.OpeningAnimationPreset;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class EditorCrateAnimationPresetsLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;

   public EditorCrateAnimationPresetsLayout(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.opening-animations"), parent);
      this.crateType = crateType;
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(view, new EditorCrateAnimationPresetsLayout.PresetsListComponent());
   }

   private OpeningAnimation getCurrentAnimation() {
      return this.crateType.getOpeningAnimations().list().get(0);
   }

   private static boolean isPremiumLocked(OpeningAnimationPreset preset) {
      return Crates.getPlugin().getEdition() != Edition.LITE
         ? false
         : preset.getFirstPhase().getPhase().isPremium() || preset.getSecondPhase().getPhase().isPremium() || preset.getThirdPhase().getPhase().isPremium();
   }

   private class PresetsListComponent extends SimplePaginatedList<OpeningAnimationPreset> {
      public PresetsListComponent() {
         super(
            SlotCompound.fromGrid(10, 2, 7),
            new PagingData(PaginationSettings.ItemsAlign.LEFT, SlotCompound.from(1, 4), SlotCompound.from(9, 4)),
            __ -> new ArrayList<>(AnimationsRegistry.getInstance().getPresets())
         );
         this.setOnItemRender(
            (context, index, item, preset) -> {
               OpeningAnimation animation = EditorCrateAnimationPresetsLayout.this.getCurrentAnimation();
               boolean selected = preset.matches(animation);
               boolean premiumLocked = EditorCrateAnimationPresetsLayout.isPremiumLocked(preset);
               ItemBuilder displayItem = premiumLocked ? ItemBuilder.of(XMaterial.BARRIER.parseItem()) : ItemBuilder.of(preset.getDisplayItem());
               if (selected) {
                  displayItem.addEnchant(XEnchantment.UNBREAKING.getEnchant(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS);
               }

               LoreBuilder lore = LoreBuilder.builder(EditorCrateAnimationPresetsLayout.this)
                  .description(
                     "\u00a77Pre Open: \u00a7f" + preset.getFirstPhase().getPhase().getDisplayName(),
                     "\u00a77Post Open: \u00a7f" + preset.getSecondPhase().getPhase().getDisplayName(),
                     "\u00a77Reward Display: \u00a7f" + preset.getThirdPhase().getPhase().getDisplayName()
                  );
               if (premiumLocked) {
                  lore.error(() -> true, Translatable.key("plans.unlock-premium")).error(() -> true, Translatable.key("plans.upgrade-at"));
               } else {
                  lore.attribute(Translatable.literal("Selected"), selected)
                     .actions(new ClickAction(ClickAction.ClickType.LEFT, Translatable.literal(selected ? "Unselect Preset" : "Select Preset")));
               }

               item.withItem(displayItem)
                  .withDisplayName((premiumLocked ? "\u00a7c" : "\u00a7a") + preset.getDisplayName() + (selected ? " \u00a77(Selected)" : ""))
                  .withLore(lore.build());
            }
         );
         this.setOnItemClick(
            (context, index, preset) -> {
               if (context.isLeftClick()) {
                  Player player = context.getViewer();
                  if (EditorCrateAnimationPresetsLayout.isPremiumLocked(preset)) {
                     Translations.send(player, EditorCrateAnimationPresetsLayout.this.t("plans.unlock-premium"));
                  } else {
                     OpeningAnimation animation = EditorCrateAnimationPresetsLayout.this.getCurrentAnimation();

                     try {
                        if (preset.matches(animation)) {
                           OpeningAnimationPreset nonePreset = AnimationsRegistry.getInstance()
                              .getPresetById("NONE")
                              .orElseThrow(() -> new IllegalStateException("Missing NONE opening animation preset."));
                           nonePreset.applyTo(animation);
                           EditorFacade.saveCrate(context.getContainerView(), EditorCrateAnimationPresetsLayout.this.crateType);
                           context.getContainerView().updateView();
                           Translations.send(player, EditorCrateAnimationPresetsLayout.this.t("editor.opening-animation.preset-removed"));
                           return;
                        }

                        preset.applyTo(animation);
                        EditorFacade.saveCrate(context.getContainerView(), EditorCrateAnimationPresetsLayout.this.crateType);
                        context.getContainerView().updateView();
                        Translations.send(player, EditorCrateAnimationPresetsLayout.this.t("editor.opening-animation.preset-applied"));
                     } catch (Exception e) {
                        Translations.error(player, e);
                        player.closeInventory();
                     }
                  }
               }
            }
         );
      }
   }
}
