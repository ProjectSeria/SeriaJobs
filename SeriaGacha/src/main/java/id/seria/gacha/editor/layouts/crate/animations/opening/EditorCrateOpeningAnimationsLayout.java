package id.seria.gacha.editor.layouts.crate.animations.opening;

import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
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
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.CratesManager;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.animations.opening.OpeningPhase;
import id.seria.gacha.managers.crates.type.list.OpeningAnimation;
import id.seria.gacha.registries.AnimationsRegistry;
import id.seria.gacha.registries.animation.OpeningAnimationPreset;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

@Deprecated
public class EditorCrateOpeningAnimationsLayout extends EditorFacade.EditorLayout {
   private static final OpeningAnimationPreset CUSTOM_PRESET = OpeningAnimationPreset.custom(XMaterial.WRITABLE_BOOK.parseItem());
   private final CrateType crateType;

   public EditorCrateOpeningAnimationsLayout(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.opening-animations"), parent);
      this.crateType = crateType;
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(
         view,
         new SimplePaginatedList<>(
               SlotCompound.fromGrid(10, 2, 7), new PagingData(PaginationSettings.ItemsAlign.LEFT, SlotCompound.from(1, 4), SlotCompound.from(9, 4)), __ -> {
                  List<OpeningAnimationPreset> allPresets = new ArrayList<>(AnimationsRegistry.getInstance().getPresets());
                  List<OpeningAnimationPreset> list = new ArrayList<>();
                  if (!allPresets.isEmpty()) {
                     list.add(allPresets.get(0));
                  }

                  list.add(CUSTOM_PRESET);
                  if (allPresets.size() > 1) {
                     list.addAll(allPresets.subList(1, allPresets.size()));
                  }

                  return list;
               }
            )
            .setOnItemRender(
               (context, index, item, preset) -> {
                  if (preset.isCustomSentinel()) {
                     boolean selected = !AnimationsRegistry.getInstance().getPresetByAnimation(this.getCurrentAnimation()).isPresent();
                     OpeningAnimation current = this.getCurrentAnimation();
                     String firstLabel = selected
                        ? "\u00a77First: \u00a7f" + this.getPhaseDisplayName(current.getPreOpenPhase())
                        : "\u00a77First: \u00a7fCustom";
                     String secondLabel = selected
                        ? "\u00a77Second: \u00a7f" + this.getPhaseDisplayName(current.getOpenPhase())
                        : "\u00a77Second: \u00a7fCustom";
                     String thirdLabel = selected
                        ? "\u00a77Third: \u00a7f" + this.getPhaseDisplayName(current.getDisplayPhase())
                        : "\u00a77Third: \u00a7fCustom";
                     item.withItem(XMaterial.WRITABLE_BOOK.parseItem())
                        .withDisplayName("\u00a7aCustom" + (selected ? " \u00a77(Selected)" : ""))
                        .withLore(
                           LoreBuilder.builder(this)
                              .description(firstLabel, secondLabel, thirdLabel)
                              .attribute(Translatable.literal("Selected"), selected)
                              .actions(new ClickAction(ClickAction.ClickType.LEFT, Translatable.literal("Select Preset")))
                              .build()
                        );
                  } else {
                     boolean selected = preset.matches(this.getCurrentAnimation());
                     item.withItem(preset.getDisplayItem())
                        .withDisplayName("\u00a7a" + preset.getDisplayName() + (selected ? " \u00a77(Selected)" : ""))
                        .withLore(
                           LoreBuilder.builder(this)
                              .description(
                                 "\u00a77First: \u00a7f" + preset.getFirstPhase().getPhase().getDisplayName(),
                                 "\u00a77Second: \u00a7f" + preset.getSecondPhase().getPhase().getDisplayName(),
                                 "\u00a77Third: \u00a7f" + preset.getThirdPhase().getPhase().getDisplayName()
                              )
                              .attribute(Translatable.literal("Selected"), selected)
                              .actions(
                                 new ClickAction(
                                    ClickAction.ClickType.LEFT, selected ? Translatable.literal("Unselect Preset") : Translatable.literal("Select Preset")
                                 )
                              )
                              .build()
                        );
                  }
               }
            )
            .setOnItemClick(
               (context, index, preset) -> {
                  Player viewer = context.getViewer();
                  if (context.isLeftClick()) {
                     if (preset.isCustomSentinel()) {
                        try {
                           new EditorCrateCreateOpeningAnimation(PhoenixCrates.getLegacyInstance(), this.crateType, this, this.getCurrentAnimation())
                              .open(viewer);
                        } catch (Exception e) {
                           Translations.error(viewer, e);
                           viewer.closeInventory();
                        }

                        return;
                     }

                     try {
                        if (preset.matches(this.getCurrentAnimation())) {
                           OpeningAnimationPreset nonePreset = AnimationsRegistry.getInstance()
                              .getPresetById("NONE")
                              .orElseThrow(() -> new IllegalStateException("Missing NONE opening animation preset."));
                           nonePreset.applyTo(this.getCurrentAnimation());
                           this.save(context.getContainerView());
                           context.getContainerView().updateView();
                           Translations.send(viewer, "\u00a7aNone preset selected.");
                           return;
                        }

                        preset.applyTo(this.getCurrentAnimation());
                        this.save(context.getContainerView());
                        context.getContainerView().updateView();
                        Translations.send(viewer, "\u00a7aOpening animation preset applied.");
                     } catch (Exception e) {
                        Translations.error(viewer, e);
                        viewer.closeInventory();
                     }
                  }
               }
            )
      );
   }

   private OpeningAnimation getCurrentAnimation() {
      return this.crateType.getOpeningAnimations().list().get(0);
   }

   private String getPhaseDisplayName(OpeningPhase phase) {
      return phase != null && phase.getPhase() != null && phase.getPhase().getDisplayName() != null && !phase.getPhase().getDisplayName().isEmpty()
         ? phase.getPhase().getDisplayName()
         : "Custom";
   }

   private void save(ContainerView view) {
      Player player = view.getViewer();

      try {
         CratesManager manager = Crates.getCratesManager();
         if (this.crateType.isRegistered()) {
            manager.saveCrateType(this.crateType);
         } else {
            manager.registerCrateType(this.crateType, true);
         }
      } catch (Exception e) {
         Translations.error(player, e);
         player.closeInventory();
      }
   }
}
