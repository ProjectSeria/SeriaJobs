package id.seria.gacha.commands;

import id.seria.gacha.Crates;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.sdk.platforms.server.commands.BukkitCommandResolvers;
import id.seria.gacha.sdk.platforms.server.commands.ServerCommandManager;

public class CrateCommandResolvers extends BukkitCommandResolvers {
   public CrateCommandResolvers(ServerCommandManager manager) {
      super(manager);
      this.registerResolver(CrateType.class, (context, stack) -> {
         String crateIdentifier = stack.pop();
         return Crates.getCratesManager().getTypeByIdentifier(crateIdentifier);
      });
      this.registerResolver(Key.class, (context, stack) -> {
         String keyIdentifier = stack.pop();
         return Crates.getKeysManager().getKeyByIdentifier(keyIdentifier);
      });
      this.registerResolver(CustomReward.class, (context, stack) -> {
         String rewardIdentifier = stack.pop();
         CrateType crate = context.getComputedParameter("crate", CrateType.class);
         return crate != null ? crate.getRewardByIdentifier(rewardIdentifier) : null;
      });
   }
}
