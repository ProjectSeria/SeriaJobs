package id.seria.gacha.registries.animation;

import id.seria.gacha.managers.crates.animations.opening.OpeningPhase;
import id.seria.gacha.managers.crates.type.list.OpeningAnimation;
import java.util.Locale;
import org.apache.commons.lang3.Validate;
import org.bukkit.inventory.ItemStack;

public final class OpeningAnimationPreset {
   private final String id;
   private final String displayName;
   private final ItemStack displayItem;
   private final OpeningPhase firstPhase;
   private final OpeningPhase secondPhase;
   private final OpeningPhase thirdPhase;

   public OpeningAnimationPreset(
      String id, String displayName, ItemStack displayItem, OpeningPhase firstPhase, OpeningPhase secondPhase, OpeningPhase thirdPhase
   ) {
      Validate.notEmpty(id, "Preset ID must not be empty", new Object[0]);
      Validate.notEmpty(displayName, "Preset display name must not be empty", new Object[0]);
      Validate.notNull(displayItem, "Preset display item must not be null", new Object[0]);
      Validate.notNull(firstPhase, "First phase must not be null", new Object[0]);
      Validate.notNull(secondPhase, "Second phase must not be null", new Object[0]);
      Validate.notNull(thirdPhase, "Third phase must not be null", new Object[0]);
      this.id = id.toUpperCase(Locale.ROOT);
      this.displayName = displayName;
      this.displayItem = displayItem.clone();
      this.firstPhase = firstPhase.clone();
      this.secondPhase = secondPhase.clone();
      this.thirdPhase = thirdPhase.clone();
   }

   public String getId() {
      return this.id;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public ItemStack getDisplayItem() {
      return this.displayItem.clone();
   }

   public OpeningPhase getFirstPhase() {
      return this.firstPhase.clone();
   }

   public OpeningPhase getSecondPhase() {
      return this.secondPhase.clone();
   }

   public OpeningPhase getThirdPhase() {
      return this.thirdPhase.clone();
   }

   public void applyTo(OpeningAnimation animation) {
      Validate.notNull(animation, "Animation must not be null", new Object[0]);
      animation.setPreOpenPhase(this.getFirstPhase());
      animation.setOpenPhase(this.getSecondPhase());
      animation.setDisplayPhase(this.getThirdPhase());
   }

   public boolean matches(OpeningAnimation animation) {
      return animation != null
         && this.samePhase(this.firstPhase, animation.getPreOpenPhase())
         && this.samePhase(this.secondPhase, animation.getOpenPhase())
         && this.samePhase(this.thirdPhase, animation.getDisplayPhase());
   }

   private boolean samePhase(OpeningPhase expected, OpeningPhase current) {
      return current != null
         && expected.getPhase().getId().equalsIgnoreCase(current.getPhase().getId())
         && expected.getProperties().map().equals(current.getProperties().map());
   }

   private OpeningAnimationPreset(String id, String displayName, ItemStack displayItem) {
      Validate.notEmpty(id, "Preset ID must not be empty", new Object[0]);
      Validate.notEmpty(displayName, "Preset display name must not be empty", new Object[0]);
      Validate.notNull(displayItem, "Preset display item must not be null", new Object[0]);
      this.id = id.toUpperCase(Locale.ROOT);
      this.displayName = displayName;
      this.displayItem = displayItem.clone();
      this.firstPhase = null;
      this.secondPhase = null;
      this.thirdPhase = null;
   }

   public static OpeningAnimationPreset custom(ItemStack displayItem) {
      return new OpeningAnimationPreset("_CUSTOM", "Custom", displayItem);
   }

   public boolean isCustomSentinel() {
      return this.firstPhase == null;
   }
}
