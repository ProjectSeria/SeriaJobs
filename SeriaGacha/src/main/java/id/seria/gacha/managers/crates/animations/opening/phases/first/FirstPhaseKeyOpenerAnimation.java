package id.seria.gacha.managers.crates.animations.opening.phases.first;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.facades.KeyFacade;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeArmorStandEntity;
import id.seria.gacha.lib.common.services.Services;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.utility.BukkitUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.managers.crates.animations.opening.data.KeybasedOpeningPhaseData;
import id.seria.gacha.registries.animation.AnimationPhaseImpl;
import id.seria.gacha.registries.animation.PhaseRunnerImpl;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class FirstPhaseKeyOpenerAnimation extends PhaseRunnerImpl {
   private static final ItemStack KEY_ITEM = XMaterial.TRIPWIRE_HOOK.parseItem();
   private int step;
   private int alpha;
   private double delta;
   private FakeArmorStandEntity keyStand;
   private Location baseLocation;
   private boolean stepStarted;
   private double offsetHole;
   private double oldDelta;

   @Override
   public void load() {
      Vector offset = null;
      if (this.crate.getType().getBlockMaterial().equals(XMaterial.PLAYER_HEAD)) {
         this.offsetHole = -0.35;
         offset = new Vector(0.455, -1.59, 0.5);
      } else {
         this.offsetHole = -0.2;
         offset = new Vector(0.31, -1.29, 0.5);
      }

      this.baseLocation = this.centerLocation.clone().add(this.getKeyOffsetX(), this.getKeyOffsetY(), this.getKeyOffsetZ()).add(offset);
      this.keyStand = Utilities.getNMSFactory()
         .createFakeArmorStand(
            PhoenixCrates.getLegacyInstance(),
            this.r(this.baseLocation.clone().add(this.sin(-Math.PI / 2) * 0.5625, this.cos(-Math.PI / 2) * 0.5625, -this.delta))
         );
      this.keyStand.setInvisible(true);
      this.keyStand
         .setItemInHand(
            this.isUseKeyAsModel() && !this.crate.getType().getLinkedKeysIds().isEmpty()
               ? KeyFacade.getFirstKey(this.crate.getType()).getPlainItem().build()
               : KEY_ITEM
         );
      this.keyStand.setAudience(this.getAudience());
      Services.getWatcherService().startWatching(this.keyStand, false);
   }

   public FirstPhaseKeyOpenerAnimation(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      super(handler, animation, properties);
   }

   @Override
   public void tick() {
      switch (this.step) {
         case 0: {
            if (!this.stepStarted) {
               this.delta = 0.5;
               this.stepStarted = true;
            }

            this.delta -= 0.05;
            if (this.delta < this.offsetHole) {
               this.playSound(this.animationLocation, XSound.BLOCK_STONE_STEP, 2.0F, 0.5F);
               this.nextStep();
               this.delta = this.offsetHole;
            }

            double deltaOffset = this.getKeyAlignment().isHorizontal() ? -Math.PI / 2 : 0.0;
            double offsetAngle = 0.11065387457644;
            if (this.getKeyAlignment().isInverted()) {
               offsetAngle *= -1.0;
            }

            double wrappedDelta = deltaOffset + offsetAngle;
            this.keyStand
               .setPositionAndRotation(this.r(this.baseLocation.clone().add(this.sin(wrappedDelta) * 0.5625, this.cos(wrappedDelta) * 0.5625, this.delta)));
            this.keyStand.setRightArmPose(BukkitUtil.createEulerAngleFromRad(0.0, this.getKeyAlignment().isInverted() ? Math.PI : 0.0, deltaOffset));
            this.keyStand.setPositionChanged(false);
            this.keyStand.setMetadataChanged(false);
            this.keyStand.prepareTeleportPacket().sendPacket(this.getAudience());
            this.keyStand.prepareMetadataPacket().sendPacket(this.getAudience());
            break;
         }
         case 1: {
            this.alpha++;
            if (this.alpha <= 10) {
               return;
            }

            if (!this.stepStarted) {
               this.delta = this.getKeyAlignment().isHorizontal() ? -Math.PI / 2 : 0.0;
               this.oldDelta = this.getKeyAlignment().isHorizontal() ? -Math.PI / 2 : 0.0;
               this.stepStarted = true;
            }

            boolean finished = false;
            this.delta += 0.1;
            if (this.delta > (this.getKeyAlignment().isHorizontal() ? Math.PI / 2 : Math.PI)) {
               this.playSound(this.animationLocation, XSound.BLOCK_LEVER_CLICK, 2.0F, 1.0F);
               this.nextStep();
               this.delta = this.getKeyAlignment().isHorizontal() ? Math.PI / 2 : Math.PI;
               this.oldDelta = this.getKeyAlignment().isHorizontal() ? Math.PI / 2 : Math.PI;
               finished = true;
            }

            double offsetAngle = 0.11065387457644;
            if (this.getKeyAlignment().isInverted()) {
               offsetAngle *= -1.0;
            }

            double wrappedDelta = this.delta + offsetAngle;
            this.keyStand
               .setPositionAndRotation(this.r(this.baseLocation.clone().add(this.sin(wrappedDelta) * 0.5625, this.cos(wrappedDelta) * 0.5625, this.offsetHole)));
            this.keyStand
               .setRightArmPose(
                  BukkitUtil.createEulerAngleFromRad(
                     0.0, this.getKeyAlignment().isInverted() ? Math.PI : 0.0, lerp(this.oldDelta, this.delta, finished ? 1.0 : -1.33)
                  )
               );
            this.keyStand.setPositionChanged(false);
            this.keyStand.setMetadataChanged(false);
            this.keyStand.prepareTeleportPacket().sendPacket(this.getAudience());
            this.keyStand.prepareMetadataPacket().sendPacket(this.getAudience());
            this.oldDelta = this.delta;
            break;
         }
         case 2:
            this.alpha++;
            if (this.alpha <= 40) {
               ParticleEffect.SMOKE_NORMAL.display(this.animationLocation, 0.2F, 0.2F, 0.2F, 0.1F, 2, null, this.getAudience());
               return;
            }

            this.nextPhase();
      }
   }

   public static double lerp(double d1, double d2, double stage) {
      return d1 * (1.0 - stage) + d2 * stage;
   }

   @Override
   public void unload() {
      Services.getWatcherService().stopWatching(this.keyStand);
   }

   @Override
   public void nextPhase() {
      this.openCrate();
      super.nextPhase();
   }

   private KeybasedOpeningPhaseData.KeyAlignment getKeyAlignment() {
      return this.properties.getEnumOrDefault("alignment", KeybasedOpeningPhaseData.KeyAlignment.VERTICAL);
   }

   private boolean isUseKeyAsModel() {
      return this.properties.getPropertyOrDefault("useKeyAsModel", false);
   }

   private double getKeyOffsetX() {
      return this.properties.getPropertyOrDefault("offsetX", 0).intValue();
   }

   private double getKeyOffsetY() {
      return this.properties.getPropertyOrDefault("offsetY", 0).intValue();
   }

   private double getKeyOffsetZ() {
      return this.properties.getPropertyOrDefault("offsetZ", 0).intValue();
   }

   private void nextStep() {
      this.alpha = 0;
      this.delta = 0.0;
      this.stepStarted = false;
      this.step++;
   }
}
