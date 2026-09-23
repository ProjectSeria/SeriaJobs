package id.seria.gacha.commands.subcommands;

import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class SummonRewardCommand extends CrateCommandExecutable {
   public SummonRewardCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      CustomReward reward = context.getComputedParameter("reward", CustomReward.class);
      String worldName = context.getComputedParameter("world", String.class);
      int x = context.getComputedParameter("x", Integer.class);
      int y = context.getComputedParameter("y", Integer.class);
      int z = context.getComputedParameter("z", Integer.class);
      if (!reward.isValid()) {
         throw new CommandContextException(Translatable.literal("\u00a7cThis reward is not valid. (Empty display item or content items)"));
      }

      World world = Bukkit.getWorld(worldName);
      if (world == null) {
         throw new CommandContextException(Translatable.literal("\u00a7cWorld '" + worldName + "' not found."));
      }

      ItemStack item = reward.getDisplayItem().build();
      Location location = new Location(world, x + 0.5, y, z + 0.5);
      world.dropItemNaturally(location, item);
      issuer.sendMessage("\u00a7aReward item for '\u00a7f" + reward.getIdentifier() + "\u00a7a' summoned at " + worldName + " " + x + " " + y + " " + z + ".");
   }
}
