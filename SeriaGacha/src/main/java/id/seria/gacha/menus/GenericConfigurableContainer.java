package id.seria.gacha.menus;

import id.seria.gacha.actions.Actions;
import id.seria.gacha.internal.Placeholders;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.configurable.ConfigurableLayout;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.ContainerConfiguration;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.stream.Collectors;

public class GenericConfigurableContainer extends ConfigurableLayout {
   public GenericConfigurableContainer(ServerPlugin plugin, ContainerConfiguration configuration) {
      super(plugin, Translations.getProvider(), configuration, Actions.getFactory());
   }

   @Override
   public void onPrepare(ContainerView containerView) {
   }

   @Override
   public void onRenderSlot(RenderViewContext context, VirtualItem item) {
      item.withDisplayName(Placeholders.setPlaceholders(item.getDisplayName(), context.getViewer()));
      item.withLore(item.getLore().stream().map(text -> Placeholders.setPlaceholders(text, context.getViewer())).collect(Collectors.toList()));
   }
}
