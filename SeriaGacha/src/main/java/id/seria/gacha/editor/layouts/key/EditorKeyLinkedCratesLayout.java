package id.seria.gacha.editor.layouts.key;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.facades.KeyFacade;
import id.seria.gacha.internal.LoreBuilder;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.SimplePaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.menus.SelectOptionMenu;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.engine.vanilla_model.VanillaModelEngineData;
import id.seria.gacha.managers.keys.Key;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EditorKeyLinkedCratesLayout extends EditorFacade.EditorLayout {
   public static ItemStack MODEL_ENGINE_SKULL = Utilities.getNMSFactory()
      .createTexturedSkullFromBase64(
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTFhNTEwM2FmMzgxZjc4OThjNTFlNGQwOTMwZWZmMzNlOWRiMTAxYjkyOWQ4MWVlY2M0NGUzNGNhMTgxNzEwNCJ9fX0="
      );
   public static ItemStack BETTER_MODEL_SKULL = Utilities.getNMSFactory()
      .createTexturedSkullFromBase64(
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTZjMzhjOTllMWRkNDJkNDEzYTFjYzY5MmE1YTJjOTZiYzQ3MzkzNDI1NDFmNGVkZmU0ZDMxZTk2YjNlY2Q4In19fQ=="
      );
   private final Key key;

   public EditorKeyLinkedCratesLayout(Key key, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.key.linked-crates"), parent);
      this.key = key;
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(view, new EditorKeyLinkedCratesLayout.CratesListComponent());
   }

   public class CratesListComponent extends SimplePaginatedList<CrateType> {
      public CratesListComponent() {
         super(
            SlotCompound.fromGrid(10, 3, 7),
            new PagingData(PaginationSettings.ItemsAlign.LEFT, SlotCompound.from(1, 3), SlotCompound.from(9, 3)),
            view -> KeyFacade.getLinkedCrates(EditorKeyLinkedCratesLayout.this.key)
         );
         this.setOnItemRender(
            (context, index, item, crateType) -> item.withItem(this.getDisplayItem(crateType))
               .withDisplayName(
                  EditorKeyLinkedCratesLayout.this.t(
                     "editor.crates.type.title", "%name%", crateType.getFormattedDisplayName(context.getViewer()), "%identifier%", crateType.getIdentifier()
                  )[0]
               )
               .withLore(
                  LoreBuilder.builder(EditorKeyLinkedCratesLayout.this)
                     .description(Translatable.key("editor.crates.type.description"))
                     .attribute(EditorKeyLinkedCratesLayout.this.label("enabled"), crateType.isEnabled())
                     .actions(ClickAction.CUSTOM("unlink", ClickAction.ClickType.LEFT, EditorKeyLinkedCratesLayout.this.label("crate")))
                     .build()
               )
         );
         this.setOnItemClick((context, index, crateType) -> {
            Player viewer = context.getViewer();
            if (context.isLeftClick()) {
               crateType.getLinkedKeysIds().remove(EditorKeyLinkedCratesLayout.this.key.getIdentifier());

               try {
                  Crates.getCratesManager().saveCrateType(crateType);
               } catch (Exception e) {
                  Translations.error(viewer, e);
                  Crates.getLogger().error("Error saving crate", e);
               }

               Crates.getScheduler().runSync(viewer, () -> EditorKeyLinkedCratesLayout.this.updateView(viewer), 1L);
            }
         });
      }

      private ItemStack getDisplayItem(CrateType type) {
         switch (type.getEngineType().getName()) {
            case "BETTER_MODEL":
               return EditorKeyLinkedCratesLayout.BETTER_MODEL_SKULL.clone();
            case "MODEL_ENGINE":
               return EditorKeyLinkedCratesLayout.MODEL_ENGINE_SKULL.clone();
            case "VANILLA_MODEL":
               VanillaModelEngineData data = (VanillaModelEngineData)type.getEngineData();
               return ItemBuilder.of(data.getMaterial()).setCustomModelData(data.getCustomModelData()).setItemModel(data.getItemModel()).build();
            case "VANILLA_BLOCK":
            default:
               return type.getBlockMaterial().parseItem();
         }
      }

      @Override
      public VirtualItem onPrepareAddItem(RenderViewContext context) {
         boolean lite = Crates.getPlugin().getEdition() == Edition.LITE;
         List<CrateType> nonLinkedCrates = KeyFacade.getNonLinkedCrates(EditorKeyLinkedCratesLayout.this.key);
         if (lite) {
            nonLinkedCrates.removeIf(candidate -> !KeyFacade.canLinkKey(candidate, EditorKeyLinkedCratesLayout.this.key));
         }

         boolean limitReached = lite && KeyFacade.getLinkedCrates(EditorKeyLinkedCratesLayout.this.key).size() >= 1;
         LoreBuilder loreBuilder = LoreBuilder.builder(EditorKeyLinkedCratesLayout.this)
            .description("&7By clicking here you will be", "&7able to link a crate to this key.")
            .actions(ClickAction.CUSTOM("link", ClickAction.ClickType.LEFT, EditorKeyLinkedCratesLayout.this.label("crate")));
         if (lite) {
            loreBuilder.error(
               () -> KeyFacade.getLinkedCrates(EditorKeyLinkedCratesLayout.this.key).size() >= 1,
               Translatable.literal("\u00a7cMultiple keys only available on Premium version.", EditorKeyLinkedCratesLayout.this.t("plans.upgrade-at")[0])
            );
         }

         loreBuilder.error(() -> nonLinkedCrates.isEmpty(), Translatable.literal("\u00a7cThere are no available crates to link."));
         return VirtualItem.from(XMaterial.LIME_STAINED_GLASS_PANE)
            .withDisplayName("\u00a7aLink Crate")
            .withLore(loreBuilder.build())
            .whenClick(
               click -> {
                  if (!nonLinkedCrates.isEmpty() && !limitReached) {
                     Player viewer = click.getViewer();

                     try {
                        List<SelectOptionMenu.OptionItem<CrateType>> items = Lists.newArrayList();

                        for (CrateType value : nonLinkedCrates) {
                           List<String> crateLore = LoreBuilder.builder(EditorKeyLinkedCratesLayout.this)
                              .description("\u00a77By clicking here, you will link", "\u00a77this crate to the key.")
                              .actions(ClickAction.CUSTOM("select", ClickAction.ClickType.LEFT, EditorKeyLinkedCratesLayout.this.label("crate")))
                              .build();
                           items.add(
                              new SelectOptionMenu.OptionItem<>(
                                 value,
                                 this.getDisplayItem(value),
                                 "\u00a7a" + value.getFormattedDisplayName(viewer) + " \u00a77(Id: " + value.getIdentifier() + "\u00a77)",
                                 crateLore
                              )
                           );
                        }

                        SelectOptionMenu selection = new SelectOptionMenu<>(PhoenixCrates.getLegacyInstance(), "Link Crates", items, crateType -> {
                           try {
                              if (!KeyFacade.canLinkKey(crateType, EditorKeyLinkedCratesLayout.this.key)) {
                                 EditorKeyLinkedCratesLayout.this.open(viewer);
                                 return;
                              }

                              crateType.getLinkedKeysIds().add(EditorKeyLinkedCratesLayout.this.key.getIdentifier());
                              Crates.getCratesManager().saveCrateType(crateType);
                              EditorKeyLinkedCratesLayout.this.open(viewer);
                           } catch (Exception e) {
                              Translations.error(viewer, e);
                           }
                        });
                        selection.setBackConsumer(event -> EditorKeyLinkedCratesLayout.this.open(viewer));
                        selection.open(viewer);
                     } catch (Exception e) {
                        Translations.error(viewer, e);
                        viewer.closeInventory();
                     }
                  }
               }
            );
      }
   }
}
