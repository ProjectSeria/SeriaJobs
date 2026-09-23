package id.seria.gacha.editor.layouts;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.editor.layouts.crate.EditorCrateMainSettingsLayout;
import id.seria.gacha.facades.CrateFacade;
import id.seria.gacha.internal.Addons;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.LoreBuilder;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.Ui;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickActionsFacade;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.SimplePaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableListOption;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.engine.vanilla_model.VanillaModelEngineData;
import id.seria.gacha.registries.InformationRegistry;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EditorCratesLayout extends EditorFacade.EditorLayout {
   private static final int LITE_CRATE_LIMIT = 5;
   public static ItemStack MODEL_ENGINE_SKULL = Utilities.getNMSFactory()
      .createTexturedSkullFromBase64(
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTFhNTEwM2FmMzgxZjc4OThjNTFlNGQwOTMwZWZmMzNlOWRiMTAxYjkyOWQ4MWVlY2M0NGUzNGNhMTgxNzEwNCJ9fX0="
      );
   public static ItemStack BETTER_MODEL_SKULL = Utilities.getNMSFactory()
      .createTexturedSkullFromBase64(
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTZjMzhjOTllMWRkNDJkNDEzYTFjYzY5MmE1YTJjOTZiYzQ3MzkzNDI1NDFmNGVkZmU0ZDMxZTk2YjNlY2Q4In19fQ=="
      );
   private static final String FILTER_TYPE_KEY = "editor.crates.filter.type";

   public EditorCratesLayout(EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crates"), parent);
   }

   private EditorCratesLayout.FilterType getFilterType(ContainerView view) {
      return view.getMetadata("editor.crates.filter.type", EditorCratesLayout.FilterType.ALL);
   }

   private void setFilterType(ContainerView view, EditorCratesLayout.FilterType filterType) {
      view.setMetadata("editor.crates.filter.type", filterType, true);
   }

   private List<CrateType> getFilteredCrates(ContainerView view) {
      EditorCratesLayout.FilterType filterType = this.getFilterType(view);
      return Crates.getCratesManager().getRegisteredTypes().stream().filter(crateType -> {
         switch (filterType) {
            case ALL:
            default:
               return true;
            case ONLY_CRATES:
               return !InformationRegistry.isEnabled(crateType, Addons.LOOTBOX);
            case ONLY_LOOTBOXES:
               return InformationRegistry.isEnabled(crateType, Addons.LOOTBOX);
         }
      }).collect(Collectors.toList());
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(
         view,
         new EditableListOption<>(
               SlotCompound.from(3, 6),
               this.createData(XMaterial.HOPPER, Constants.addon("Filter"), Translatable.key("editor.crates.type.description")),
               Arrays.asList(EditorCratesLayout.FilterType.values()),
               this.getFilterType(view)
            )
            .setItemFormatter(option -> Translatable.literal(option.getDisplayName()))
            .setOnOptionChange(value -> {
               this.setFilterType(view, value);
               view.updateView();
            })
            .addError(!InformationRegistry.isEnabled(Addons.LOOTBOX), Translatable.key("unlock-addon", "%addon%", "Lootbox"))
            .setFixedPosition(true)
      );
      this.addComponent(view, new EditorCratesLayout.CratesListComponent());
   }

   public class CratesListComponent extends SimplePaginatedList<CrateType> {
      public CratesListComponent() {
         super(
            SlotCompound.fromGrid(10, 3, 7),
            new PagingData(PaginationSettings.ItemsAlign.LEFT, SlotCompound.from(1, 3), SlotCompound.from(9, 3)),
            view -> EditorCratesLayout.this.getFilteredCrates(view)
         );
         this.setOnItemRender(
            (context, index, item, crateType) -> item.withItem(this.getDisplayItem(crateType))
               .withDisplayName(
                  EditorCratesLayout.this.t(
                        "editor.crates.type.title", "%name%", crateType.getFormattedDisplayName(context.getViewer()), "%identifier%", crateType.getIdentifier()
                     )[0]
                     + (InformationRegistry.isEnabled(crateType, Addons.LOOTBOX) ? " \u00a7e" + Ui.smallCaps("(Lootbox \u2b50)") : "")
               )
               .withLore(
                  LoreBuilder.builder(EditorCratesLayout.this)
                     .description(Translatable.key("editor.crates.type.description"))
                     .attribute(EditorCratesLayout.this.label("enabled"), crateType.isEnabled())
                     .actions(ClickAction.NAVIGATE, new ClickAction(ClickAction.ClickType.SHIFT_LEFT, EditorCratesLayout.this.label("receive-crate")))
                     .build()
               )
         );
         this.setOnItemClick((context, index, crateType) -> {
            Player viewer = context.getViewer();
            if (context.isShiftClick()) {
               if (context.isLeftClick()) {
                  CrateFacade.giveCrate(crateType, viewer, 1, true);
                  Translations.send(context.getViewer(), Translations.t("crate-given", "%amount%", 1, "%player%", viewer.getName()));
               }
            } else if (context.isLeftClick()) {
               try {
                  new EditorCrateMainSettingsLayout(crateType, EditorCratesLayout.this).open(viewer);
               } catch (Exception e) {
                  Translations.error(viewer, e);
                  viewer.closeInventory();
               }
            }
         });
      }

      private ItemStack getDisplayItem(CrateType type) {
         switch (type.getEngineType().getName()) {
            case "BETTER_MODEL":
               return EditorCratesLayout.BETTER_MODEL_SKULL.clone();
            case "MODEL_ENGINE":
               return EditorCratesLayout.MODEL_ENGINE_SKULL.clone();
            case "VANILLA_MODEL":
               VanillaModelEngineData data = (VanillaModelEngineData)type.getEngineData();
               return ItemBuilder.of(data.getMaterial()).setCustomModelData(data.getCustomModelData()).setItemModel(data.getItemModel()).build();
            default:
               return type.getItem().build();
         }
      }

      @Override
      public VirtualItem onPrepareAddItem(RenderViewContext context) {
         boolean lite = Crates.getPlugin().getEdition() == Edition.LITE;
         boolean limitReached = lite && Crates.getCratesManager().countRegisteredTypes() >= 5;
         return lite
            ? VirtualItem.from(XMaterial.LIME_STAINED_GLASS_PANE)
               .withDisplayName(
                  EditorCratesLayout.this.t("editor.crates.add.title")[0] + " \u00a78(" + Crates.getCratesManager().countRegisteredTypes() + "/5)"
               )
               .withLore(
                  LoreBuilder.builder(EditorCratesLayout.this)
                     .description(Translatable.key("editor.crates.add.description"))
                     .actions(ClickAction.CREATE(ClickAction.ClickType.LEFT, EditorCratesLayout.this.label("crate")))
                     .warning(Translatable.literal("Lite version only allows 5 crates."))
                     .error(() -> Crates.getCratesManager().countRegisteredTypes() >= 5, Translatable.literal("Maximum limit reached for lite version."))
                     .build()
               )
               .whenClick(click -> {
                  if (!limitReached) {
                     this.createCrate(click.getViewer());
                  }
               })
            : VirtualItem.from(XMaterial.LIME_STAINED_GLASS_PANE)
               .withDisplayName(EditorCratesLayout.this.t("editor.crates.add.title")[0])
               .withLore(
                  ClickActionsFacade.applyActions(
                     EditorCratesLayout.this,
                     Arrays.asList(EditorCratesLayout.this.t("editor.crates.add.description")),
                     ClickAction.CREATE(ClickAction.ClickType.LEFT, EditorCratesLayout.this.label("crate"))
                  )
               )
               .whenClick(click -> this.createCrate(click.getViewer()));
      }

      private void createCrate(Player viewer) {
         try {
            CrateType newCrateType = CrateFacade.buildDefault(null);
            new EditorCrateMainSettingsLayout(newCrateType, EditorCratesLayout.this).open(viewer);
         } catch (Exception e) {
            Translations.error(viewer, e);
            viewer.closeInventory();
         }
      }
   }

   private enum FilterType {
      ALL("All"),
      ONLY_CRATES("Crates"),
      ONLY_LOOTBOXES("Lootboxes");

      private final String displayName;

      FilterType(final String displayName) {
         this.displayName = displayName;
      }

      public String getDisplayName() {
         return this.displayName;
      }
   }
}
