package id.seria.gacha.commands.subcommands;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.internal.FacingDirection;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.utils.utility.FaceUtil;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class AttachCrateCommand extends CrateCommandExecutable {
   public AttachCrateCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) throws Exception {
      Actor issuer = context.getIssuer();
      if (!issuer.isPlayer()) {
         throw new CommandContextException(Translatable.key("only-ingame-players"));
      }

      Player player = (Player)issuer.raw();
      CrateType crateType = context.getComputedParameter("crate", CrateType.class);
      Block target = player.getTargetBlockExact(5);
      if (target != null && !target.getType().isAir()) {
         if (Crates.getCratesManager().getCrateByBlock(target) != null) {
            throw new CommandContextException(Translatable.literal("\u00a7cThere is already a crate at that location."));
         }

         float yaw = player.getLocation().getYaw();
         BlockFace blockFace = FaceUtil.yawToFace(yaw, false);
         FacingDirection facingDirection = FacingDirection.fromBukkit(blockFace);
         Crate crate = new Crate(Crates.getCratesManager(), crateType, target.getLocation(), facingDirection);
         Crates.getCratesManager().createCrate(crate, false);
         Translations.send(issuer, "\u00a7aCrate '\u00a7f" + crateType.getIdentifier() + "\u00a7a' attached to the block you are looking at.");
      } else {
         throw new CommandContextException(Translatable.literal("\u00a7cYou must be looking at a block."));
      }
   }
}
