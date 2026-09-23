package id.seria.gacha.commands.subcommands;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class DetachCrateCommand extends CrateCommandExecutable {
   public DetachCrateCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) throws Exception {
      Actor issuer = context.getIssuer();
      if (!issuer.isPlayer()) {
         throw new CommandContextException(Translatable.key("only-ingame-players"));
      }

      Player player = (Player)issuer.raw();
      Block target = player.getTargetBlockExact(5);
      if (target != null && !target.getType().isAir()) {
         Crate crate = Crates.getCratesManager().getCrateByBlock(target);
         if (crate == null) {
            throw new CommandContextException(Translatable.literal("\u00a7cThere is no crate at the block you are looking at."));
         }

         String identifier = crate.getType().getIdentifier();
         Crates.getCratesManager().destroyCrate(crate);
         Translations.send(issuer, "\u00a7aCrate '\u00a7f" + identifier + "\u00a7a' has been detached from that location.");
      } else {
         throw new CommandContextException(Translatable.literal("\u00a7cYou must be looking at a block."));
      }
   }
}
