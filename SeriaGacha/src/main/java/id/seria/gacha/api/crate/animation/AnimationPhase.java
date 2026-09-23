package id.seria.gacha.api.crate.animation;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface AnimationPhase {
   PhaseType getType();

   String getId();

   String getDisplayName();

   ItemStack getDisplayItem();

   boolean isVersionCompatible();

   String getRequiredVersion();

   default AnimationEnvironment getEnvironment() {
      return AnimationEnvironment.WORLD;
   }

   default String getDescription() {
      return null;
   }

   PhaseRunner createRunner(OpeningAnimationHandler var1, AnimationProperties var2);

   @Deprecated
   default Class<?> getLegacyDataClass() {
      return null;
   }

   @Deprecated
   @Internal
   default boolean isLite() {
      return true;
   }

   @Deprecated
   @Internal
   default boolean isPremium() {
      return true;
   }

   @Deprecated
   @Internal
   default boolean isPro() {
      return true;
   }
}
