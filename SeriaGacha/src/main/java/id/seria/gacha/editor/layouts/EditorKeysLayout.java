package id.seria.gacha.editor.layouts;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.editor.layouts.key.EditorKeyLayout;
import id.seria.gacha.facades.KeyFacade;
import id.seria.gacha.internal.LoreBuilder;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickActionsFacade;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.SimplePaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.managers.keys.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;

public class EditorKeysLayout extends EditorFacade.EditorLayout {
   private static final int LITE_KEY_LIMIT = 5;

   public EditorKeysLayout(EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.keys"), parent);
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(view, new EditorKeysLayout.KeysListComponent());
   }

   public class KeysListComponent extends SimplePaginatedList<Key> {
      public KeysListComponent() {
         super(
            SlotCompound.fromGrid(10, 3, 7),
            new PagingData(PaginationSettings.ItemsAlign.LEFT, SlotCompound.from(1, 3), SlotCompound.from(9, 3)),
            view -> Crates.getKeysManager().getRegisteredKeys()
         );
         this.setOnItemRender(
            (context, index, item, key) -> item.withItem(key.getPlainItem().build())
               .withDisplayName(
                  EditorKeysLayout.this.t(
                     "editor.keys.key.title", "%name%", key.getFormattedDisplayName(context.getViewer()), "%identifier%", key.getIdentifier()
                  )[0]
               )
               .withLore(
                  LoreBuilder.builder(EditorKeysLayout.this)
                     .description(Translatable.key("editor.keys.key.description"))
                     .attribute(EditorKeysLayout.this.label("enabled"), key.isEnabled())
                     .attribute(EditorKeysLayout.this.label("crates-linked"), KeyFacade.getLinkedCrates(key).size())
                     .actions(ClickAction.NAVIGATE, new ClickAction(ClickAction.ClickType.SHIFT_LEFT, EditorKeysLayout.this.label("receive-key")))
                     .build()
               )
         );
         this.setOnItemClick((context, index, crateKey) -> {
            Player viewer = context.getViewer();
            if (context.isShiftClick()) {
               if (context.isLeftClick()) {
                  KeyFacade.giveKey(crateKey, viewer, false);
               }
            } else if (context.isLeftClick()) {
               try {
                  new EditorKeyLayout(crateKey, EditorKeysLayout.this).open(viewer);
               } catch (Exception e) {
                  Translations.error(viewer, e);
                  viewer.closeInventory();
               }
            }
         });
      }

      @Override
      public VirtualItem onPrepareAddItem(RenderViewContext context) {
         boolean lite = Crates.getPlugin().getEdition() == Edition.LITE;
         boolean limitReached = lite && Crates.getKeysManager().countRegisteredKeys() >= 5;
         List<String> description = new ArrayList<>(Arrays.asList(EditorKeysLayout.this.t("editor.keys.add.description")));
         if (limitReached) {
            description.add("");
            description.add("\u00a7cMaximum limit reached for lite version.");
         }

         String suffix = lite ? " \u00a78(" + Crates.getKeysManager().countRegisteredKeys() + "/5)" : "";
         return VirtualItem.from(XMaterial.LIME_STAINED_GLASS_PANE)
            .withDisplayName(EditorKeysLayout.this.t("editor.keys.add.title")[0] + suffix)
            .withLore(
               ClickActionsFacade.applyActions(
                  EditorKeysLayout.this, description, ClickAction.CREATE(ClickAction.ClickType.LEFT, EditorKeysLayout.this.label("key"))
               )
            )
            .whenClick(click -> {
               if (!lite || Crates.getKeysManager().countRegisteredKeys() < 5) {
                  Player viewer = click.getViewer();

                  try {
                     Key newKey = Key.buildDefault(null);
                     new EditorKeyLayout(newKey, EditorKeysLayout.this).open(viewer);
                  } catch (Exception e) {
                     Translations.error(viewer, e);
                     viewer.closeInventory();
                  }
               }
            });
      }
   }
}
