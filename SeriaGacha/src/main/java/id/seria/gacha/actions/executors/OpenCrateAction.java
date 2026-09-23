package id.seria.gacha.actions.executors;

import id.seria.gacha.api.crate.exceptions.CrateOpeningException;
import id.seria.gacha.api.crate.exceptions.CratePreviewException;
import id.seria.gacha.facades.CrateFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.UiComponents;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.utils.actions.Action;
import id.seria.gacha.lib.common.utils.actions.ActionExecutor;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.menus.crates.CratePreviewMenu;

public class OpenCrateAction extends Action {
   public OpenCrateAction() {
      super("OPEN", new String[0]);
   }

   @Override
   public void execute(ActionExecutor executor) {
      ContainerView menuView = UiComponents.getPlayerView(executor.getPlayer().getUniqueId());
      if (menuView != null && menuView.getContainer() instanceof CratePreviewMenu) {
         CratePreviewMenu previewMenu = (CratePreviewMenu)menuView.getContainer();
         if (!previewMenu.getCrate().isPresent()) {
            Translations.send(executor.getPlayer(), "\u00a7cFast crate opening is not available on this menu.");
         } else {
            Crate crate = previewMenu.getCrate().get();
            executor.getPlayer().closeInventory();

            try {
               CrateFacade.openCrate(crate, executor.getPlayer());
            } catch (CrateOpeningException e) {
               Translations.send(executor.getPlayer(), e.getMessage());
            } catch (CratePreviewException e) {
               throw new RuntimeException(e);
            }
         }
      } else {
         Translations.send(executor.getPlayer(), "\u00a7cNo crate preview menu opened.");
      }
   }

   @Override
   public String getArgumentsSyntax() {
      return "";
   }
}
