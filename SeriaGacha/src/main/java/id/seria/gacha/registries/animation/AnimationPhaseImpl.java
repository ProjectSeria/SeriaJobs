package id.seria.gacha.registries.animation;

import id.seria.gacha.api.crate.animation.AnimationEnvironment;
import id.seria.gacha.api.crate.animation.AnimationPhase;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.crate.animation.OpeningAnimationHandler;
import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.lib.common.utils.version.ServerVersion;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.managers.crates.animations.opening.data.OpeningPhaseData;
import java.util.function.Supplier;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;

public class AnimationPhaseImpl implements AnimationPhase {
   private Edition edition;
   private PhaseType type;
   private String id;
   private String displayName;
   private ItemStack displayItem;
   private ServerVersion requiredBukkitVersion;
   @Deprecated
   private Class<? extends OpeningPhaseData> legacyDataClass;
   private AnimationPhaseImpl.OpeningPhaseSupplier unsafePhaseSupplier;
   private Supplier<OpeningPhaseData> legacyDataSupplier;
   private AnimationEnvironment environment;
   private String description;

   public AnimationPhaseImpl(
      Edition edition,
      PhaseType type,
      String id,
      String displayName,
      ItemStack displayItem,
      ServerVersion requiredVersion,
      AnimationPhaseImpl.OpeningPhaseSupplier unsafePhaseSupplier,
      Supplier<OpeningPhaseData> legacyDataSupplier
   ) {
      this.edition = edition;
      this.type = type;
      this.id = id;
      this.displayName = displayName;
      this.displayItem = displayItem;
      this.requiredBukkitVersion = requiredVersion;
      this.unsafePhaseSupplier = unsafePhaseSupplier;
      this.legacyDataSupplier = legacyDataSupplier;
      this.legacyDataClass = (Class<? extends OpeningPhaseData>)legacyDataSupplier.get().getClass();
      this.environment = AnimationEnvironment.WORLD;
   }

   @Override
   public AnimationEnvironment getEnvironment() {
      return this.environment;
   }

   public AnimationPhaseImpl setEnvironment(AnimationEnvironment environment) {
      this.environment = environment == null ? AnimationEnvironment.WORLD : environment;
      return this;
   }

   @Override
   public String getDescription() {
      return this.description;
   }

   public AnimationPhaseImpl setDescription(String description) {
      this.description = description;
      return this;
   }

   @Override
   public boolean isVersionCompatible() {
      return ServerVersion.isGreaterEqualThan(this.requiredBukkitVersion);
   }

   @Override
   public String getRequiredVersion() {
      return this.requiredBukkitVersion.displayName();
   }

   public PhaseRunnerImpl createRunner(OpeningAnimationHandler session, AnimationProperties properties) {
      return this.unsafePhaseSupplier.create(session, this, properties);
   }

   @Deprecated
   public OpeningPhaseData createDefaultLegacyData() {
      return this.legacyDataSupplier.get();
   }

   @Deprecated
   @Override
   public Class<?> getLegacyDataClass() {
      return this.legacyDataClass;
   }

   @Deprecated
   @Override
   public boolean isLite() {
      return this.edition.equals(Edition.LITE);
   }

   @Deprecated
   @Override
   public boolean isPremium() {
      return this.edition.equals(Edition.PREMIUM);
   }

   @Deprecated
   @Override
   public boolean isPro() {
      return this.edition.equals(Edition.PRO);
   }

   @Generated
   @Override
   public PhaseType getType() {
      return this.type;
   }

   @Generated
   @Override
   public String getId() {
      return this.id;
   }

   @Generated
   @Override
   public String getDisplayName() {
      return this.displayName;
   }

   @Generated
   @Override
   public ItemStack getDisplayItem() {
      return this.displayItem;
   }

   public interface OpeningPhaseSupplier {
      PhaseRunnerImpl create(OpeningAnimationHandler var1, AnimationPhaseImpl var2, AnimationProperties var3) throws ClassCastException;
   }
}
