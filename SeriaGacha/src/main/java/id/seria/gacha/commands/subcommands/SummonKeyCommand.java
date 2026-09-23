package id.seria.gacha.commands.subcommands;

import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class SummonKeyCommand extends CrateCommandExecutable {
   public SummonKeyCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      Key key = context.getComputedParameter("key", Key.class);
      String worldName = context.getComputedParameter("world", String.class);
      int x = context.getComputedParameter("x", Integer.class);
      int y = context.getComputedParameter("y", Integer.class);
      int z = context.getComputedParameter("z", Integer.class);
      World world = Bukkit.getWorld(worldName);
      if (world == null) {
         throw new CommandContextException(Translatable.literal("\u00a7cWorld '" + worldName + "' not found."));
      }

      ItemStack item = key.createItemStack();
      Location location = new Location(world, x + 0.5, y, z + 0.5);
      world.dropItemNaturally(location, item);
      issuer.sendMessage("\u00a7aKey item for '\u00a7f" + key.getIdentifier() + "\u00a7a' summoned at " + worldName + " " + x + " " + y + " " + z + ".");
   }
}
