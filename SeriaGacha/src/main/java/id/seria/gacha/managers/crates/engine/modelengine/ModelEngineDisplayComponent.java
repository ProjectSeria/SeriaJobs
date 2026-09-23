package id.seria.gacha.managers.crates.engine.modelengine;

import id.seria.gacha.Crates;
import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.lib.common.modelengine.interfaces.animation.MegAnimationProperty;
import id.seria.gacha.lib.common.modelengine.interfaces.animation.MegBlueprintAnimation;
import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegEntityCompound;
import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegModeledEntity;
import id.seria.gacha.lib.common.modelengine.interfaces.enums.MegLoopMode;
import id.seria.gacha.lib.common.modelengine.interfaces.model.MegActiveModel;
import id.seria.gacha.lib.common.modelengine.interfaces.model.MegModelBlueprint;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeArmorStandEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.services.Services;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.limbo.hooks.ModelEngineHook;
import id.seria.gacha.limbo.hooks.modelengine.ModelEngine;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.engine.EngineDisplayComponent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ModelEngineDisplayComponent extends EngineDisplayComponent {
   public static final Map<UUID, Crate> LOOKUP_MAP = new ConcurrentHashMap<>();
   private final String modelId;
   private final String idleAnimation;
   private final String closeAnimation;
   private final double scale;
   protected final Map<PhaseType, String> phaseAnimations;
   protected final Set<PhaseType> playingPhaseAnimations;
   protected FakeArmorStandEntity tempHologram;
   protected MegEntityCompound entityCompound;
   protected MegActiveModel activeModel;
   protected MegModeledEntity modeledEntity;
   protected Player viewer;
   protected boolean playIdleAnimation = true;
   private final BukkitTask t;

   public ModelEngineDisplayComponent(Crate crate, ModelEngineEngineData data, Player viewer) {
      super(crate);
      this.phaseAnimations = new HashMap<>();
      this.playingPhaseAnimations = ConcurrentHashMap.newKeySet();
      this.t = null;
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
      if (this.viewer == null) {
         if (updateBlock || !this.crate.getBlock().getType().equals(Material.BARRIER)) {
            this.crate.getBlock().setType(Material.BARRIER, false);
         }

         if (this.modelId.equalsIgnoreCase("empty")) {
            this.initWaitingHologram("\u00a7cSetup the model");
            return;
         }

         if (!ModelEngineHook.areModelsLoaded()) {
            this.initWaitingHologram("\u00a7cNo models found");
         }
      } else if (this.modelId.equalsIgnoreCase("empty") || !ModelEngineHook.areModelsLoaded()) {
         return;
      }

      ModelEngineHook.whenModelsAvailable(() -> Crates.getScheduler().execute(this.crate.getLocation(), () -> {
         if (this.tempHologram != null) {
            Services.getWatcherService().stopWatching(this.tempHologram);
            this.tempHologram = null;
         }

         Location loc = this.crate.getLocation().clone();
         loc.setX(loc.getBlockX() + 0.5);
         loc.setY(loc.getBlockY());
         loc.setZ(loc.getBlockZ() + 0.5);
         loc.setYaw((float)this.crate.getFacingDirection().getDirection());
         MegModelBlueprint blueprint = ModelEngine.getApi().getModelRegistry().getBlueprint(this.modelId);
         if (blueprint == null) {
            this.initWaitingHologram("\u00a7cInvalid blueprint \"" + this.modelId + "\"");
         } else {
            this.entityCompound = ModelEngine.getApi().createStaticEntity(this.modelId, loc, this.viewer);
            this.activeModel = this.entityCompound.getActiveModel();
            if (this.activeModel == null) {
               this.initWaitingHologram("\u00a7cFailed to load model \"" + this.modelId + "\"");
            } else {
               this.modeledEntity = this.entityCompound.getModeledEntity();
               this.modeledEntity.setRenderRadius(Crates.getConfiguration().getCratesRenderRadius());
               this.activeModel.setScale(this.scale);
               LOOKUP_MAP.put(this.modeledEntity.getBase().getUniqueId(), this.getCrate());
               if (this.viewer != null) {
                  Crates.getScheduler().runSync(this.viewer, () -> {
                     this.modeledEntity.forceSpawn(this.viewer);
                     if (this.idleAnimation != null) {
                        this.activeModel.getAnimationHandler().stopAnimation(this.idleAnimation);
                     }
                  }, 2L);
               } else {
                  this.crate.getHologram().moveTo(this.crate.getHologramLocation());
                  this.playIdleAnimation();
               }
            }
         }
      }));
   }

   private void playIdleAnimation() {
      if (this.activeModel != null && this.idleAnimation != null && !this.idleAnimation.equalsIgnoreCase("none")) {
         MegBlueprintAnimation animation = this.activeModel.getBlueprint().getAnimations().get(this.idleAnimation);
         if (animation != null) {
            MegAnimationProperty propery = ModelEngine.getApi().createAnimationProperty(this.activeModel, animation);
            propery.setForceLoopMode(MegLoopMode.LOOP);
            propery.setForceOverride(true);
            this.activeModel.getAnimationHandler().playAnimation(propery, true);
         }
      }
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

      if (this.entityCompound != null) {
         this.entityCompound.release();
      }

      if (this.activeModel != null) {
         this.activeModel.destroy();
         this.activeModel = null;
      }

      if (this.modeledEntity != null) {
         if (this.viewer != null) {
            this.modeledEntity.removePlayer(this.viewer);
         }

         this.modeledEntity.destroy();
         LOOKUP_MAP.remove(this.modeledEntity.getBase().getUniqueId());
         this.modeledEntity = null;
      }

      this.playingPhaseAnimations.clear();
   }

   @Override
   public void update() {
      if (this.viewer != null) {
         this.modeledEntity.removePlayer(this.viewer);
         this.modeledEntity.forceSpawn(this.viewer);
      }
   }

   @Override
   public void playAnimation(PhaseType phase) {
      if (this.activeModel != null) {
         String animationId = this.phaseAnimations.get(phase);
         if (animationId != null && !animationId.equalsIgnoreCase("none")) {
            this.playingPhaseAnimations.add(phase);
            this.activeModel.getAnimationHandler().forceStopAllAnimations();
            this.activeModel.getAnimationHandler().playAnimation(animationId, 0.0, 0.0, 1.0, true);
            ModelEngineHook.awaitAnimationEnd(this.activeModel, this.crate.getLocation(), animationId, () -> this.playingPhaseAnimations.remove(phase));
         }
      }
   }

   @Override
   public boolean isPlayingAnimation(PhaseType phase) {
      return this.playingPhaseAnimations.contains(phase);
   }

   @Override
   public void show(List<Player> players) {
      if (this.activeModel != null && this.viewer == null) {
         for (Player player : players) {
            this.activeModel.showToPlayer(player);
         }
      }
   }

   @Override
   public void hide(List<Player> players) {
      if (this.activeModel != null && this.viewer == null) {
         for (Player player : players) {
            this.activeModel.hideFromPlayer(player);
         }
      }
   }

   @Override
   public double getDisplayHeight() {
      return this.activeModel != null && this.activeModel.getBlueprint().getMainHitbox() != null
         ? this.activeModel.getBlueprint().getMainHitbox().getEyeHeight() * this.scale
         : 1.1;
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
   public MegEntityCompound getEntityCompound() {
      return this.entityCompound;
   }

   @Generated
   public MegActiveModel getActiveModel() {
      return this.activeModel;
   }

   @Generated
   public MegModeledEntity getModeledEntity() {
      return this.modeledEntity;
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
   public BukkitTask getT() {
      return this.t;
   }

   @Generated
   public void setPlayIdleAnimation(boolean playIdleAnimation) {
      this.playIdleAnimation = playIdleAnimation;
   }
}
