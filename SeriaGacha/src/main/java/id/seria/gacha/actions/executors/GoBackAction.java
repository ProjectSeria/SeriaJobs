package id.seria.gacha.actions.executors;

import id.seria.gacha.lib.common.uicomponents.newest.UiComponents;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.ContainerLayout;
import id.seria.gacha.lib.common.utils.actions.Action;
import id.seria.gacha.lib.common.utils.actions.ActionExecutor;

public class GoBackAction extends Action {
   public GoBackAction() {
      super("GOBACK", new String[0]);
   }

   @Override
   public void execute(ActionExecutor executor) {
      ContainerView menuView = UiComponents.getPlayerView(executor.getPlayer().getUniqueId());
      if (menuView != null && menuView.getContainer() instanceof ContainerLayout) {
         ContainerLayout layout = ((ContainerLayout)menuView.getContainer()).getParent();
         if (layout == null) {
            executor.getPlayer().closeInventory();
         } else {
            layout.open(executor.getPlayer());
         }
      }
   }

   @Override
   public String getArgumentsSyntax() {
      return "";
   }
}
