package id.seria.gacha.commands.subcommands;

import id.seria.gacha.commands.validations.CrateValidations;
import id.seria.gacha.editor.layouts.EditorCratesLayout;
import id.seria.gacha.editor.layouts.EditorMainLayout;
import id.seria.gacha.editor.layouts.crate.EditorCrateMainSettingsLayout;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.entity.Player;

public class EditCrateCommand extends CrateCommandExecutable {
   public EditCrateCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      CrateValidations.requirePlayerSender(context);
      Player player = (Player)context.getIssuer().raw();
      CrateType crateType = context.getComputedParameter("crate", CrateType.class);
      EditorMainLayout mainLayout = new EditorMainLayout();
      EditorCratesLayout cratesLayout = new EditorCratesLayout(mainLayout);
      EditorCrateMainSettingsLayout layout = new EditorCrateMainSettingsLayout(crateType, cratesLayout);

      try {
         layout.open(player);
      } catch (Exception e) {
         Translations.error(player, e);
         player.closeInventory();
      }
   }
}
