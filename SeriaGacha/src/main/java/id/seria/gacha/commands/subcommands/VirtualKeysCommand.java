package id.seria.gacha.commands.subcommands;

import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.menus.MenuFactory;
import id.seria.gacha.menus.keys.VirtualKeysMenu;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.entity.Player;

public class VirtualKeysCommand extends CrateCommandExecutable {
   public VirtualKeysCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) throws Exception {
      Actor issuer = context.getIssuer();
      if (!issuer.isPlayer()) {
         throw new CommandContextException(Translatable.literal("&cOnly players can execute this command."));
      }

      Player player = (Player)issuer.raw();
      VirtualKeysMenu.VirtualKeysMenuConfiguration configuration = (VirtualKeysMenu.VirtualKeysMenuConfiguration)MenuFactory.getInstance()
         .getConfiguration("virtual_keys");
      if (configuration == null) {
         throw new CommandContextException(Translatable.literal("&cVirtual keys menu is not available."));
      }

      new VirtualKeysMenu(configuration).open(player);
   }
}
