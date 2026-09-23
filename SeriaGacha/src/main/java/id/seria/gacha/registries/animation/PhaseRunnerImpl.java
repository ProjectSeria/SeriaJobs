package id.seria.gacha.registries.animation;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.api.crate.animation.AnimationEnvironment;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.crate.animation.PhaseRunner;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.lib.common.utils.utility.PlayerUtil;
import id.seria.gacha.lib.common.utils.utility.RotationUtil;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.animations.OpeningAnimationHandlerImpl;
import id.seria.gacha.managers.crates.animations.opening.data.OpeningPhaseData;
import id.seria.gacha.managers.crates.engine.modelengine.ModelEngineEngineData;
import id.seria.gacha.managers.crates.session.CrateOpeningSession;
import java.util.List;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public abstract class PhaseRunnerImpl implements PhaseRunner {
   protected final OpeningAnimationHandlerImpl handler;
   protected final AnimationPhaseImpl phase;
   protected final AnimationProperties properties;
   protected final Crate crate;
   protected CrateOpeningSession session;
   protected final Location animationLocation;
   protected final Location centerLocation;
   protected final Location displayLocation;
   protected final Player player;
   private boolean waitingForAnimation;
   private boolean shouldTick;

   @Deprecated
   public PhaseRunnerImpl(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl phase, OpeningPhaseData data) {
      this(handler, phase, data.asProperties());
   }

   public PhaseRunnerImpl(OpeningAnimationHandlerImpl handler, AnimationPhaseImpl animation, AnimationProperties properties) {
      this.handler = handler;
      this.phase = animation;
      this.properties = properties;
      this.crate = handler.getCrate();
      this.session = handler.getSession();
      this.animationLocation = this.crate.getLocation().clone();
      this.animationLocation.setX(this.crate.getBlockX() + 0.5);
      this.animationLocation.setY(this.crate.getBlockY());
      this.animationLocation.setZ(this.crate.getBlockZ() + 0.5);
      this.centerLocation = this.animationLocation.clone().add(0.0, 0.5, 0.0);
      this.displayLocation = this.animationLocation
         .clone()
         .add(this.r(this.crate.getType().getExtraSettings().getDisplayItemPosition().clone()))
         .add(0.0, 0.6, 0.0);
      this.player = handler.getPlayer();
   }

   @Override
   public final void baseLoad() {
      if (this.crate.getType().getEngineData() instanceof ModelEngineEngineData) {
         ModelEngineEngineData castedData = (ModelEngineEngineData)this.crate.getType().getEngineData();
         double startDelay = castedData.getPhaseStartDelay(this.phase.getType());
         if (startDelay < 0.05) {
            this.executeCustomSound();
            this.load();
            this.shouldTick = true;
            return;
         }

         Crates.getScheduler().runSync(this.animationLocation, () -> {
            this.executeCustomSound();
            this.load();
            this.shouldTick = true;
         }, (int)(startDelay * 20.0));
      } else {
         this.executeCustomSound();
         this.load();
         this.shouldTick = true;
      }
   }

   private void executeCustomSound() {
      try {
         String soundRaw = this.properties.getProperty("customSound", String.class);
         if (soundRaw == null || soundRaw.isEmpty()) {
            return;
         }

         String[] soundData = soundRaw.replace(" ", "").split(";");
         if (soundData.length == 0) {
            return;
         }

         String sound = soundData[0].toLowerCase();
         float volume = soundData.length >= 2 ? Float.parseFloat(soundData[1]) : 1.0F;
         float pitch = soundData.length >= 3 ? Float.parseFloat(soundData[2]) : 1.0F;
         this.getAudience().forEach(player -> player.playSound(this.animationLocation, sound, volume, pitch));
      } catch (Exception ex) {
         Crates.getLogger().error("Error while processing animation custom sound", ex);
      }
   }

   @Override
   public final void baseTick() {
      if (this.waitingForAnimation) {
         if (!this.handler.getSession().getVirtualDisplayComponent().isPlayingAnimation(this.phase.getType())) {
            this.handler.nextPhase();
            this.waitingForAnimation = false;
         }
      } else if (this.shouldTick) {
         this.tick();
      }
   }

   @Override
   public final void baseUnload() {
      this.unload();
   }

   public abstract void load();

   public abstract void tick();

   public abstract void unload();

   @Override
   public void nextPhase() {
      if (this.handler.getSession().getVirtualDisplayComponent().isPlayingAnimation(this.phase.getType())) {
         this.waitingForAnimation = true;
      } else {
         this.handler.nextPhase();
      }
   }

   protected EulerAngle r(EulerAngle angle) {
      double facingYaw = this.getFacingYaw();
      return angle.add(0.0, 0.0, -facingYaw);
   }

   protected Location r(Location location) {
      double facingYaw = this.getFacingYaw();
      location.subtract(this.animationLocation);
      RotationUtil.applyYawRot(location, -facingYaw);
      location.add(this.animationLocation);
      return location;
   }

   protected Vector r(Vector vector) {
      double facingYaw = this.getFacingYaw();
      RotationUtil.applyYawRot(vector, -facingYaw);
      return vector;
   }

   protected double sin(double radAngle) {
      return MathUtil.sin(radAngle);
   }

   protected double cos(double radAngle) {
      return MathUtil.cos(radAngle);
   }

   protected double getFacingYaw() {
      return this.crate.getFacingDirection().getDirection();
   }

   public List<Player> getAudience() {
      if (this.phase.getEnvironment() == AnimationEnvironment.GUI) {
         return Lists.newArrayList(new Player[]{this.player});
      } else {
         return this.getCrate().getType().isSimultaneousOpeningsEnabled() ? Lists.newArrayList(new Player[]{this.player}) : this.crate.getNearbyPlayers();
      }
   }

   protected void playSound(Location location, XSound sound, float volume, float pitch) {
      if (!this.isSilent()) {
         PlayerUtil.playSound(location, this.getAudience(), sound, volume, pitch);
      }
   }

   @Deprecated
   protected void playSound(XSound sound, float volume, float pitch) {
      if (!this.isSilent()) {
         PlayerUtil.playSound(this.getAudience(), sound, volume, pitch);
      }
   }

   protected void openCrate() {
      this.handler.openCrate(this.isSilent());
   }

   protected void closeCrate() {
      this.handler.openCrate(this.isSilent());
   }

   protected boolean isSilent() {
      return this.properties.getPropertyOrDefault("silent", false);
   }

   @Generated
   public OpeningAnimationHandlerImpl getHandler() {
      return this.handler;
   }

   @Generated
   public AnimationPhaseImpl getPhase() {
      return this.phase;
   }

   @Generated
   @Override
   public AnimationProperties getProperties() {
      return this.properties;
   }

   @Generated
   public Crate getCrate() {
      return this.crate;
   }

   @Generated
   public CrateOpeningSession getSession() {
      return this.session;
   }

   @Generated
   public Location getAnimationLocation() {
      return this.animationLocation;
   }

   @Generated
   public Location getCenterLocation() {
      return this.centerLocation;
   }

   @Generated
   public Location getDisplayLocation() {
      return this.displayLocation;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public boolean isWaitingForAnimation() {
      return this.waitingForAnimation;
   }

   @Generated
   public boolean isShouldTick() {
      return this.shouldTick;
   }
}
