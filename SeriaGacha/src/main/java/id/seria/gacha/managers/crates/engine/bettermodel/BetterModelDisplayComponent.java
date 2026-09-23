package id.seria.gacha.managers.crates.engine.bettermodel;

import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeArmorStandEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.services.Services;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.engine.EngineDisplayComponent;
import id.seria.gacha.sdk.hooks.bettermodel.BetterModelDisplay;
import id.seria.gacha.sdk.hooks.bettermodel.BetterModelProvider;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BetterModelDisplayComponent extends EngineDisplayComponent {
   public static final Map<UUID, Crate> LOOKUP_MAP = new ConcurrentHashMap<>();
   private final String modelId;
   private final String idleAnimation;
   private final String closeAnimation;
   private final double scale;
   protected final Map<PhaseType, String> phaseAnimations;
   protected final Set<PhaseType> playingPhaseAnimations;
   protected FakeArmorStandEntity tempHologram;
   protected BetterModelDisplay activeModel;
   private final Player viewer;
   protected boolean playIdleAnimation = true;
   protected boolean playCloseAnimation;

   public BetterModelDisplayComponent(Crate crate, BetterModelEngineData data, Player viewer) {
      super(crate);
      this.phaseAnimations = new HashMap<>();
      this.playingPhaseAnimations = new HashSet<>();
      this.playCloseAnimation = true;
      this.modelId = data.getModelId();
      this.idleAnimation = data.getIdleAnimation();
      this.closeAnimation = data.getCloseAnimation();
      this.scale = data.getScale();
      this.phaseAnimations.put(PhaseType.PRE_OPENING, data.getFirstPhaseAnimation());
      this.phaseAnimations.put(PhaseType.OPENING, data.getSecondPhaseAnimation());
      this.phaseAnimations.put(PhaseType.DISPLAYING, data.getThirdPhaseAnimation());
      this.viewer = viewer;
   }

   @Override
   public void create(boolean updateBlock) {
      BetterModelProvider pluginHook;
      try {
         pluginHook = PhoenixCrates.getLegacyInstance().getHookRegistry().getHook(BetterModelProvider.class);
      } catch (IllegalStateException e) {
         if (PhoenixCrates.getLegacyInstance().isBetterModelAvailable()) {
            Crates.getScheduler().runSync(this.crate.getLocation(), () -> this.create(updateBlock), 1L);
         }

         return;
      }

      if (this.viewer == null) {
         if (updateBlock || !this.crate.getBlock().getType().equals(Material.BARRIER)) {
            this.crate.getBlock().setType(Material.BARRIER, false);
         }

         if (this.modelId.equalsIgnoreCase("empty")) {
            this.initWaitingHologram("\u00a7cSetup the model!");
            return;
         }

         if (!pluginHook.areModelsLoaded()) {
            this.initWaitingHologram("\u00a7cNo models found");
         }
      } else if (this.modelId.equalsIgnoreCase("empty") || !pluginHook.areModelsLoaded()) {
         return;
      }

      pluginHook.whenModelsAvailable(() -> Crates.getScheduler().execute(this.crate.getLocation(), () -> {
         if (this.tempHologram != null) {
            Services.getWatcherService().stopWatching(this.tempHologram);
            this.tempHologram = null;
         }

         Location loc = this.crate.getLocation().clone();
         loc.setX(loc.getBlockX() + 0.5);
         loc.setY(loc.getBlockY());
         loc.setZ(loc.getBlockZ() + 0.5);
         loc.setYaw((float)this.crate.getFacingDirection().getDirection());
         this.activeModel = pluginHook.createDisplay(this.modelId, loc);
         if (this.activeModel == null) {
            this.initWaitingHologram("\u00a7cInvalid blueprint \"" + this.modelId + "\"");
         } else {
            this.activeModel.scale((float)this.scale);
            if (this.viewer != null) {
               Crates.getScheduler().runSync(this.viewer, () -> {
                  this.activeModel.spawnForViewer(this.viewer);
                  this.activeModel.stopAnimation(this.idleAnimation);
               }, 2L);
            } else {
               this.crate.getHologram().moveTo(this.crate.getHologramLocation());
               this.activeModel.playLoopAnimation(this.idleAnimation);
            }
         }
      }));
   }

   private void initWaitingHologram(String name) {
      if (this.viewer == null) {
         if (this.tempHologram == null) {
            Location loc = this.crate.getLocation().clone();
            loc.setX(loc.getBlockX() + 0.5);
            loc.setY(loc.getBlockY() + 0.2);
            loc.setZ(loc.getBlockZ() + 0.5);
            this.tempHologram = (FakeArmorStandEntity)Utilities.getNMSFactory()
               .createFakeEntity(this.crate.getPlugin().getOriginal(), FakeEntityType.ARMOR_STAND, loc);
            this.tempHologram.setMarker(true);
            this.tempHologram.setInvisible(true);
            this.tempHologram.setSmall(true);
            Services.getWatcherService().startWatching(this.tempHologram, true);
         }

         this.tempHologram.setCustomNameVisible(true);
         this.tempHologram.setCustomName(name);
      }
   }

   @Override
   public void destroy(boolean updateBlock) {
      if (this.viewer == null && updateBlock) {
         this.crate.getBlock().setType(Material.AIR, false);
      }

      if (this.tempHologram != null) {
         Services.getWatcherService().stopWatching(this.tempHologram);
         this.tempHologram = null;
      }

      if (this.activeModel != null) {
         if (this.viewer != null) {
            this.activeModel.remove(this.viewer);
         }

         this.activeModel.close();
         this.activeModel = null;
      }

      this.playingPhaseAnimations.clear();
   }

   @Override
   public void update() {
      if (this.viewer != null && this.activeModel != null) {
         this.activeModel.remove(this.viewer);
         this.activeModel.spawnForViewer(this.viewer);
      }
   }

   @Override
   public void show(List<Player> players) {
      if (this.activeModel != null && this.viewer == null) {
         for (Player player : players) {
            this.activeModel.spawnForViewer(player);
            this.activeModel.show(player);
         }
      }
   }

   @Override
   public void hide(List<Player> players) {
      if (this.activeModel != null && this.viewer == null) {
         for (Player player : players) {
            this.activeModel.hide(player);
         }
      }
   }

   @Override
   public void playAnimation(PhaseType phase) {
      if (this.activeModel != null) {
         String animationId = this.phaseAnimations.get(phase);
         if (animationId != null && !animationId.equalsIgnoreCase("none")) {
            this.playingPhaseAnimations.add(phase);
            this.activeModel.playAnimationOnce(animationId, () -> this.playingPhaseAnimations.remove(phase));
         }
      }
   }

   @Override
   public boolean isPlayingAnimation(PhaseType phase) {
      return this.playingPhaseAnimations.contains(phase);
   }

   @Override
   public double getDisplayHeight() {
      return this.activeModel == null ? 1.1 : this.activeModel.height();
   }

   public void playCloseAnimation(Runnable whenFinish) {
      if (this.activeModel != null && this.closeAnimation != null && !this.closeAnimation.equalsIgnoreCase("none")) {
         this.activeModel.playAnimationOnce(this.closeAnimation, whenFinish);
      } else {
         whenFinish.run();
      }
   }

   @Generated
   public String getModelId() {
      return this.modelId;
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
   public Map<PhaseType, String> getPhaseAnimations() {
      return this.phaseAnimations;
   }

   @Generated
   public Set<PhaseType> getPlayingPhaseAnimations() {
      return this.playingPhaseAnimations;
   }

   @Generated
   public FakeArmorStandEntity getTempHologram() {
      return this.tempHologram;
   }

   @Generated
   public BetterModelDisplay getActiveModel() {
      return this.activeModel;
   }

   @Generated
   public Player getViewer() {
      return this.viewer;
   }

   @Generated
   public boolean isPlayIdleAnimation() {
      return this.playIdleAnimation;
   }

   @Generated
   public boolean isPlayCloseAnimation() {
      return this.playCloseAnimation;
   }

   @Generated
   public void setPlayIdleAnimation(boolean playIdleAnimation) {
      this.playIdleAnimation = playIdleAnimation;
   }

   @Generated
   public void setPlayCloseAnimation(boolean playCloseAnimation) {
      this.playCloseAnimation = playCloseAnimation;
   }
}
