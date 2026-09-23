package id.seria.gacha.managers.crates.engine.bettermodel;

import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.managers.crates.engine.EngineData;
import id.seria.gacha.managers.crates.engine.EngineType;
import lombok.Generated;

public class BetterModelEngineData extends EngineData implements Cloneable {
   @ConfigField
   private String modelId = "empty";
   @ConfigField
   private String firstPhaseAnimation = "none";
   @ConfigField
   private String secondPhaseAnimation = "none";
   @ConfigField
   private String thirdPhaseAnimation = "none";
   @ConfigField
   private double firstPhaseStartDelay = 0.0;
   @ConfigField
   private double secondPhaseStartDelay = 0.0;
   @ConfigField
   private double thirdPhaseStartDelay = 0.0;
   @ConfigField
   private String idleAnimation = "none";
   @ConfigField
   private String closeAnimation = "none";
   @ConfigField
   private double scale = 1.0;

   @Override
   public EngineType getType() {
      return EngineType.valueOf("BETTER_MODEL");
   }

   public double getPhaseStartDelay(PhaseType type) {
      switch (type) {
         case PRE_OPENING:
            return this.firstPhaseStartDelay;
         case OPENING:
            return this.secondPhaseStartDelay;
         case DISPLAYING:
            return this.thirdPhaseStartDelay;
         default:
            throw new IllegalArgumentException("Unknown phase \"" + type.name() + "\"");
      }
   }

   public BetterModelEngineData clone() {
      return new BetterModelEngineData(
         this.modelId,
         this.firstPhaseAnimation,
         this.secondPhaseAnimation,
         this.thirdPhaseAnimation,
         this.firstPhaseStartDelay,
         this.secondPhaseStartDelay,
         this.thirdPhaseStartDelay,
         this.idleAnimation,
         this.closeAnimation,
         this.scale
      );
   }

   @Generated
   public String getModelId() {
      return this.modelId;
   }

   @Generated
   public String getFirstPhaseAnimation() {
      return this.firstPhaseAnimation;
   }

   @Generated
   public String getSecondPhaseAnimation() {
      return this.secondPhaseAnimation;
   }

   @Generated
   public String getThirdPhaseAnimation() {
      return this.thirdPhaseAnimation;
   }

   @Generated
   public double getFirstPhaseStartDelay() {
      return this.firstPhaseStartDelay;
   }

   @Generated
   public double getSecondPhaseStartDelay() {
      return this.secondPhaseStartDelay;
   }

   @Generated
   public double getThirdPhaseStartDelay() {
      return this.thirdPhaseStartDelay;
   }

   @Generated
   public String getIdleAnimation() {
      return this.idleAnimation;
   }

   @Generated
   public String getCloseAnimation() {
      return this.closeAnimation;
   }

   @Generated
   public double getScale() {
      return this.scale;
   }

   @Generated
   public void setModelId(String modelId) {
      this.modelId = modelId;
   }

   @Generated
   public void setFirstPhaseAnimation(String firstPhaseAnimation) {
      this.firstPhaseAnimation = firstPhaseAnimation;
   }

   @Generated
   public void setSecondPhaseAnimation(String secondPhaseAnimation) {
      this.secondPhaseAnimation = secondPhaseAnimation;
   }

   @Generated
   public void setThirdPhaseAnimation(String thirdPhaseAnimation) {
      this.thirdPhaseAnimation = thirdPhaseAnimation;
   }

   @Generated
   public void setFirstPhaseStartDelay(double firstPhaseStartDelay) {
      this.firstPhaseStartDelay = firstPhaseStartDelay;
   }

   @Generated
   public void setSecondPhaseStartDelay(double secondPhaseStartDelay) {
      this.secondPhaseStartDelay = secondPhaseStartDelay;
   }

   @Generated
   public void setThirdPhaseStartDelay(double thirdPhaseStartDelay) {
      this.thirdPhaseStartDelay = thirdPhaseStartDelay;
   }

   @Generated
   public void setIdleAnimation(String idleAnimation) {
      this.idleAnimation = idleAnimation;
   }

   @Generated
   public void setCloseAnimation(String closeAnimation) {
      this.closeAnimation = closeAnimation;
   }

   @Generated
   public void setScale(double scale) {
      this.scale = scale;
   }

   @Generated
   public BetterModelEngineData() {
   }

   @Generated
   public BetterModelEngineData(
      String modelId,
      String firstPhaseAnimation,
      String secondPhaseAnimation,
      String thirdPhaseAnimation,
      double firstPhaseStartDelay,
      double secondPhaseStartDelay,
      double thirdPhaseStartDelay,
      String idleAnimation,
      String closeAnimation,
      double scale
   ) {
      this.modelId = modelId;
      this.firstPhaseAnimation = firstPhaseAnimation;
      this.secondPhaseAnimation = secondPhaseAnimation;
      this.thirdPhaseAnimation = thirdPhaseAnimation;
      this.firstPhaseStartDelay = firstPhaseStartDelay;
      this.secondPhaseStartDelay = secondPhaseStartDelay;
      this.thirdPhaseStartDelay = thirdPhaseStartDelay;
      this.idleAnimation = idleAnimation;
      this.closeAnimation = closeAnimation;
      this.scale = scale;
   }
}
