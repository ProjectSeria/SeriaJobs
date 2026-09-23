package id.seria.gacha.limbo.menus;

import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.container.AbstractContainer;
import id.seria.gacha.lib.common.uicomponents.newest.container.ContainerType;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.CloseViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.OpenViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Deprecated
public class ConfirmationMenu extends AbstractContainer {
   private String containerTitle;
   private final Runnable onAccept;
   private final Runnable onDecline;
   private ItemStack displayItem;
   private boolean done;

   public ConfirmationMenu(ServerPlugin plugin, String title, Runnable onAccept, Runnable onDecline, ItemStack displayItem) {
      super(plugin);
      this.containerTitle = title;
      this.onAccept = onAccept;
      this.onDecline = onDecline;
      if (displayItem != null) {
         this.displayItem = displayItem.clone();
      }
   }

   public ConfirmationMenu(ServerPlugin plugin, String title, Runnable onAccept, Runnable onDecline) {
      super(plugin);
      this.containerTitle = title;
      this.onAccept = onAccept;
      this.onDecline = onDecline;
   }

   @Override
   public void onInitialize(ContainerView containerView) {
   }

   @Override
   public void onOpen(OpenViewContext openContext) {
      openContext.setContainerTitle(this.containerTitle);
      openContext.setContainerType(ContainerType.CONTAINER_9X3);
   }

   @Override
   public void onRender(RenderViewContext renderContext) {
      Player player = renderContext.getViewer();

      for (int x = 1; x <= 3; x++) {
         for (int y = 1; y <= 3; y++) {
            renderContext.setSlot(
               x, y, ItemBuilder.of(XMaterial.RED_STAINED_GLASS_PANE).setDisplayName("\u00a7c" + Translations.t("labels.decline")[0]), __ -> {
                  this.done = true;
                  this.onDecline.run();
                  player.closeInventory();
               }
            );
         }
      }

      if (this.displayItem != null) {
         renderContext.setSlot(5, 2, ItemBuilder.of(this.displayItem));
      }

      for (int x = 7; x <= 9; x++) {
         for (int y = 1; y <= 3; y++) {
            renderContext.setSlot(
               x, y, ItemBuilder.of(XMaterial.GREEN_STAINED_GLASS_PANE).setDisplayName("\u00a7a" + Translations.t("labels.confirm")[0]), __ -> {
                  this.done = true;
                  this.onAccept.run();
                  player.closeInventory();
               }
            );
         }
      }
   }

   @Override
   public void onClose(CloseViewContext closeContext) {
      if (!this.done) {
         this.onDecline.run();
         this.done = true;
      }
   }
}
