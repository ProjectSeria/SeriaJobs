package id.seria.gacha.lib.common.services.services;

import id.seria.gacha.lib.common.services.factory.ServicesFactory;
import id.seria.gacha.lib.common.services.services.animator.BlockbenchAnimator;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;

public class AnimatorService implements ServicesFactory.ServiceLifecycle {
   private final ServerPlugin plugin;

   public AnimatorService(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public void load() {
   }

   public BlockbenchAnimator newBlockbenchAnimator() {
      return new BlockbenchAnimator(this.plugin);
   }

   @Override
   public void unload() {
   }
}
