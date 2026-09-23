package id.seria.gacha.registries.animation;

import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.lib.common.utils.version.ServerVersion;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.managers.crates.animations.opening.data.OpeningPhaseData;
import java.util.function.Supplier;
import lombok.Generated;
import org.bukkit.inventory.ItemStack;

public class AddonAnimationPhaseImpl extends AnimationPhaseImpl {
   private final String addonName;
   private final String addonUrl;

   public AddonAnimationPhaseImpl(
      Edition edition,
      PhaseType type,
      String id,
      String displayName,
      ItemStack displayItem,
      ServerVersion requiredVersion,
      AnimationPhaseImpl.OpeningPhaseSupplier unsafePhaseSupplier,
      Supplier<OpeningPhaseData> legacyDataSupplier,
      String addonName,
      String addonUrl
   ) {
      super(edition, type, id, displayName, displayItem, requiredVersion, unsafePhaseSupplier, legacyDataSupplier);
      this.addonName = addonName;
      this.addonUrl = addonUrl;
   }

   @Generated
   public String getAddonName() {
      return this.addonName;
   }

   @Generated
   public String getAddonUrl() {
      return this.addonUrl;
   }
}
