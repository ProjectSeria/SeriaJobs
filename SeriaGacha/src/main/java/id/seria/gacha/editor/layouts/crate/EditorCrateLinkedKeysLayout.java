package id.seria.gacha.editor.layouts.crate;

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
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableBoolean;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.menus.SelectOptionMenu;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.keys.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.Generated;
import org.bukkit.entity.Player;

public class EditorCrateLinkedKeysLayout extends EditorFacade.EditorLayout {
   private CrateType crateType;

   public EditorCrateLinkedKeysLayout(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.link-keys"), parent);
      this.crateType = crateType;
   }

   @Override
   public void onPrepare(ContainerView view) {
      EditorCrateLinkedKeysLayout.LinkedKeysListComponent rewardsList = view.computeMetadataIfAbsent(
         "linked-keys-list-component", () -> new EditorCrateLinkedKeysLayout.LinkedKeysListComponent()
      );
      this.addComponent(view, rewardsList);
      boolean lite = Crates.getPlugin().getEdition() == Edition.LITE;
      this.addComponent(
         view,
         (new EditableBoolean(
               SlotCompound.from(2, 6),
               this.createData(
                  XMaterial.REPEATER,
                  Translatable.literal(lite ? "\u00a7aPriority Reordering \u00a78(Premium)" : "\u00a7aPriority Reordering"),
                  Translatable.literal(
                     Lists.newArrayList(
                        new String[]{
                           "&7Click here to sort the keys", "&7in your preferred order.", "&7This will affect the priority", "&7in which they are used."
                        }
                     )
                  )
               ),
               this.label("enabled"),
               rewardsList.isReordering()
            ) {})
            .setOnValueChange(value -> {
               rewardsList.setReordering(value);
               view.updateView();
            })
            .addError(lite, Translatable.key("plans.unlock-premium"))
            .addError(lite, Translatable.key("plans.upgrade-at"))
            .setLocked(ctx -> lite)
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(3, 6),
               this.createData(
                  XMaterial.LIME_DYE,
                  Translatable.literal("\u00a7aKey Required"),
                  Translatable.literal(
                     Lists.newArrayList(new String[]{"&7By clicking here you will be able", "&7to change if this crate requires", "&7a key to be opened."})
                  )
               ),
               this.label("required"),
               this.crateType.isKeyRequired()
            )
            .setOnValueChange(value -> {
               this.crateType.setKeyRequired(value);
               EditorFacade.saveCrate(view, this.crateType);
            })
            .setFixedPosition(true)
      );
   }

   public class LinkedKeysListComponent extends SimplePaginatedList<String> {
      private boolean reordering;
      private Integer currentSelectionIndex;

      public void setReordering(boolean value) {
         this.reordering = value;
         if (!value) {
            this.currentSelectionIndex = null;
         }
      }

      public LinkedKeysListComponent() {
         super(
            SlotCompound.fromGrid(10, 3, 7),
            new PagingData(PaginationSettings.ItemsAlign.LEFT, SlotCompound.from(1, 3), SlotCompound.from(9, 3)),
            view -> new ArrayList<>(EditorCrateLinkedKeysLayout.this.crateType.getLinkedKeysIds())
         );
         this.setOnItemRender(
            (context, index, item, crateKeyId) -> {
               Key key = Crates.getKeysManager().getKeyByIdentifier(crateKeyId);
               if (this.currentSelectionIndex != null && index == this.currentSelectionIndex) {
                  item.withItem(XMaterial.YELLOW_STAINED_GLASS_PANE).withDisplayName("&eSelected");
               } else {
                  item.withItem(key == null ? XMaterial.BARRIER.parseItem() : key.getPlainItem().build())
                     .withDisplayName(
                        EditorCrateLinkedKeysLayout.this.t(
                           "editor.keys.key.title",
                           "%name%",
                           key == null ? "Invalid" : key.getFormattedDisplayName(context.getViewer()),
                           "%identifier%",
                           key == null ? crateKeyId : key.getIdentifier()
                        )[0]
                     )
                     .withLore(
                        LoreBuilder.builder(EditorCrateLinkedKeysLayout.this)
                           .description(Translatable.key("editor.keys.key.description"))
                           .attribute(EditorCrateLinkedKeysLayout.this.label("enabled"), key != null && key.isEnabled())
                           .error(() -> key == null, Translatable.literal("Linked Key doesn't exist."))
                           .error(() -> key != null && !key.isEnabled(), Translatable.literal("Linked Key not enabled."))
                           .actions(this.actions())
                           .build()
                     );
               }
            }
         );
         this.setOnItemClick((context, index, crateKeyId) -> {
            if (this.reordering) {
               if (this.currentSelectionIndex == null) {
                  this.currentSelectionIndex = index;
                  context.getContainerView().updateView();
               } else {
                  int selectedIndex = this.currentSelectionIndex;
                  this.currentSelectionIndex = null;
                  if (selectedIndex != index) {
                     List<String> keys = new ArrayList<>(EditorCrateLinkedKeysLayout.this.crateType.getLinkedKeysIds());
                     Collections.swap(keys, selectedIndex, index);
                     EditorCrateLinkedKeysLayout.this.crateType.setLinkedKeysIds(new LinkedHashSet<>(keys));
                     EditorFacade.saveCrate(context.getContainerView(), EditorCrateLinkedKeysLayout.this.crateType);
                  }

                  context.getContainerView().updateView();
               }
            } else {
               Key key = Crates.getKeysManager().getKeyByIdentifier(crateKeyId);
               Player viewer = context.getViewer();
               if (context.isLeftClick()) {
                  if (context.isShiftClick()) {
                     if (key != null) {
                        KeyFacade.giveKey(key, viewer, false);
                     }
                  } else {
                     EditorCrateLinkedKeysLayout.this.crateType.getLinkedKeysIds().remove(crateKeyId);
                     EditorFacade.saveCrate(context.getContainerView(), EditorCrateLinkedKeysLayout.this.crateType);
                     Crates.getScheduler().runSync(viewer, () -> EditorCrateLinkedKeysLayout.this.updateView(viewer), 1L);
                  }
               }
            }
         });
      }

      private List<ClickAction> actions() {
         return this.reordering
            ? Lists.newArrayList(new ClickAction[]{ClickAction.CUSTOM("reorder", ClickAction.ClickType.LEFT, EditorCrateLinkedKeysLayout.this.label("key"))})
            : Lists.newArrayList(
               new ClickAction[]{
                  ClickAction.CUSTOM("unlink", ClickAction.ClickType.LEFT, EditorCrateLinkedKeysLayout.this.label("key")),
                  new ClickAction(ClickAction.ClickType.SHIFT_LEFT, EditorCrateLinkedKeysLayout.this.label("receive-key"))
               }
            );
      }

      @Override
      public VirtualItem onPrepareAddItem(RenderViewContext context) {
         if (this.reordering) {
            return VirtualItem.from(XMaterial.AIR);
         }

         boolean lite = Crates.getPlugin().getEdition() == Edition.LITE;
         List<Key> nonLinkedKeys = KeyFacade.getNonLinkedKeys(EditorCrateLinkedKeysLayout.this.crateType);
         if (lite) {
            nonLinkedKeys.removeIf(candidate -> !KeyFacade.canLinkKey(EditorCrateLinkedKeysLayout.this.crateType, candidate));
         }

         boolean multipleKeysLocked = lite && EditorCrateLinkedKeysLayout.this.crateType.getLinkedKeysIds().size() >= 1;
         List<String> lore = LoreBuilder.builder(EditorCrateLinkedKeysLayout.this)
            .description("&7By clicking here you will be", "&7able to link a key to this crate.")
            .actions(ClickAction.CUSTOM("link", ClickAction.ClickType.LEFT, EditorCrateLinkedKeysLayout.this.label("key")))
            .error(() -> multipleKeysLocked, Translatable.key("plans.unlock-premium"))
            .error(() -> multipleKeysLocked, Translatable.key("plans.upgrade-at"))
            .error(() -> !multipleKeysLocked && nonLinkedKeys.isEmpty(), Translatable.literal("&cThere are no available keys to link."))
            .build();
         return VirtualItem.from(XMaterial.LIME_STAINED_GLASS_PANE)
            .withDisplayName(multipleKeysLocked ? "\u00a7aLink Key \u00a78(Premium)" : "\u00a7aLink Key")
            .withLore(lore)
            .whenClick(
               click -> {
                  if (!nonLinkedKeys.isEmpty() && !multipleKeysLocked) {
                     Player viewer = click.getViewer();

                     try {
                        List<SelectOptionMenu.OptionItem<Key>> items = Lists.newArrayList();

                        for (Key value : nonLinkedKeys) {
                           List<String> keyLore = LoreBuilder.builder(EditorCrateLinkedKeysLayout.this)
                              .description("\u00a77By clicking here, you will link", "\u00a77this key to the crate.")
                              .actions(ClickAction.CUSTOM("link", ClickAction.ClickType.LEFT, EditorCrateLinkedKeysLayout.this.label("key")))
                              .build();
                           items.add(
                              new SelectOptionMenu.OptionItem<>(
                                 value,
                                 value.createItemStack(),
                                 value.getFormattedDisplayName(viewer) + " \u00a77(Id: " + value.getIdentifier() + "\u00a77)",
                                 keyLore
                              )
                           );
                        }

                        SelectOptionMenu selection = new SelectOptionMenu<>(PhoenixCrates.getLegacyInstance(), "Link Keys", items, key -> {
                           try {
                              if (!KeyFacade.canLinkKey(EditorCrateLinkedKeysLayout.this.crateType, key)) {
                                 EditorCrateLinkedKeysLayout.this.open(viewer);
                                 return;
                              }

                              EditorCrateLinkedKeysLayout.this.crateType.getLinkedKeysIds().add(key.getIdentifier());
                              Crates.getCratesManager().saveCrateType(EditorCrateLinkedKeysLayout.this.crateType);
                              EditorCrateLinkedKeysLayout.this.open(viewer);
                           } catch (Exception e) {
                              Translations.error(viewer, e);
                           }
                        });
                        selection.setBackConsumer(event -> EditorCrateLinkedKeysLayout.this.open(viewer));
                        selection.open(viewer);
                     } catch (Exception e) {
                        Translations.error(viewer, e);
                        viewer.closeInventory();
                     }
                  }
               }
            );
      }

      @Generated
      public boolean isReordering() {
         return this.reordering;
      }

      @Generated
      public Integer getCurrentSelectionIndex() {
         return this.currentSelectionIndex;
      }
   }
}
