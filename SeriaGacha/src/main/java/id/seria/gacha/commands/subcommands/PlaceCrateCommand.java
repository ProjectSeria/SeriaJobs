package id.seria.gacha.commands.subcommands;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.internal.FacingDirection;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class PlaceCrateCommand extends CrateCommandExecutable {
   public PlaceCrateCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) throws Exception {
      Actor issuer = context.getIssuer();
      CrateType crateType = context.getComputedParameter("crate", CrateType.class);
      String worldName = context.getComputedParameter("world", String.class);
      int x = context.getComputedParameter("x", Integer.class);
      int y = context.getComputedParameter("y", Integer.class);
      int z = context.getComputedParameter("z", Integer.class);
      World world = Bukkit.getWorld(worldName);
      if (world == null) {
         throw new CommandContextException(Translatable.literal("\u00a7cWorld '" + worldName + "' not found."));
      }

      String facingInput = context.getComputedParameter("facing", String.class);

      FacingDirection facing;
      try {
         facing = FacingDirection.valueOf(facingInput.toUpperCase());
      } catch (IllegalArgumentException e) {
         throw new CommandContextException(Translatable.literal("\u00a7cInvalid facing direction: " + facingInput));
      }

      Location location = new Location(world, x, y, z);
      if (Crates.getCratesManager().getCrateByBlock(location.getBlock()) != null) {
         throw new CommandContextException(Translatable.literal("\u00a7cThere is already a crate at that location."));
      }

      Crate crate = new Crate(Crates.getCratesManager(), crateType, location, facing);
      Crates.getCratesManager().createCrate(crate, true);
      Translations.send(issuer, "\u00a7aCrate '\u00a7f" + crateType.getIdentifier() + "\u00a7a' placed at " + worldName + " " + x + " " + y + " " + z + ".");
   }
}
