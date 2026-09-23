package id.seria.gacha.commands.subcommands;

import id.seria.gacha.commands.validations.CrateValidations;
import id.seria.gacha.editor.layouts.EditorKeysLayout;
import id.seria.gacha.editor.layouts.EditorMainLayout;
import id.seria.gacha.editor.layouts.key.EditorKeyLayout;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.entity.Player;

public class EditKeyCommand extends CrateCommandExecutable {
   public EditKeyCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      CrateValidations.requirePlayerSender(context);
      Player player = (Player)context.getIssuer().raw();
      Key key = context.getComputedParameter("key", Key.class);
      EditorMainLayout mainLayout = new EditorMainLayout();
      EditorKeysLayout keysLayout = new EditorKeysLayout(mainLayout);
      EditorKeyLayout layout = new EditorKeyLayout(key, keysLayout);

      try {
         layout.open(player);
      } catch (Exception e) {
         Translations.error(player, e);
         player.closeInventory();
      }
   }
}
