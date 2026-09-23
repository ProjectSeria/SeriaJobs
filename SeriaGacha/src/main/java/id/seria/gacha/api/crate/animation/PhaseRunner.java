package id.seria.gacha.api.crate.animation;

public interface PhaseRunner {
   OpeningAnimationHandler getHandler();

   AnimationPhase getPhase();

   AnimationProperties getProperties();

   void baseLoad();

   void baseTick();

   void baseUnload();

   void nextPhase();
}
