package id.seria.gacha.api.session;

import id.seria.gacha.api.crate.CrateInstance;
import id.seria.gacha.api.crate.animation.OpeningAnimationHandler;
import id.seria.gacha.api.reward.RewardsGenerator;

public interface OpeningSession {
   CrateInstance getCrate();

   <T extends OpeningContext> T getContext();

   <T extends RewardsGenerator> T getRewardsGenerator();

   OpeningAnimationHandler getAnimationHandler();
}
