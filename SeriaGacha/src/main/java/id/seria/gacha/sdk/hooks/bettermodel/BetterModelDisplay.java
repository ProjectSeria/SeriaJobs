package id.seria.gacha.sdk.hooks.bettermodel;

import java.util.UUID;
import java.util.function.Consumer;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.animation.AnimationModifier;
import kr.toxicity.model.api.animation.AnimationIterator.Type;
import kr.toxicity.model.api.bone.RenderedBone;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.event.ModelEventListener;
import kr.toxicity.model.api.event.hitbox.HitBoxInteractAtEvent;
import kr.toxicity.model.api.nms.ModelDisplay;
import kr.toxicity.model.api.nms.ModelInteractionHand;
import kr.toxicity.model.api.tracker.EntityTracker;
import kr.toxicity.model.api.tracker.EntityTrackerRegistry;
import kr.toxicity.model.api.tracker.ModelScaler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class BetterModelDisplay {
   @Nullable
   private final Entity backingEntity;
   private final EntityTracker tracker;
   private final boolean ownsEntity;
   @Nullable
   private ModelEventListener interactListener;

   BetterModelDisplay(Entity backingEntity, EntityTracker tracker) {
      this.backingEntity = backingEntity;
      this.tracker = tracker;
      this.ownsEntity = true;
   }

   BetterModelDisplay(EntityTracker tracker) {
      this.backingEntity = null;
      this.tracker = tracker;
      this.ownsEntity = false;
   }

   public void spawnForViewer(@Nullable Player viewer) {
      if (viewer != null) {
         this.tracker.markPlayerForSpawn(BukkitAdapter.adapt(viewer));
      }
   }

   public void show(Player player) {
      this.tracker.show(BukkitAdapter.adapt(player));
   }

   public void hide(Player player) {
      this.tracker.hide(BukkitAdapter.adapt(player));
   }

   public void remove(Player player) {
      this.tracker.remove(BukkitAdapter.adapt(player));
   }

   public void stopAnimation(@Nullable String animationId) {
      if (animationId != null) {
         this.tracker.stopAnimation(animationId);
      }
   }

   public void playLoopAnimation(@Nullable String animationId) {
      if (animationId != null && !animationId.equalsIgnoreCase("none")) {
         AnimationModifier modifier = AnimationModifier.builder().type(Type.LOOP).override(true).build();
         this.tracker.animate(animationId, modifier);
      }
   }

   public void playAnimationOnce(@Nullable String animationId, @Nullable Runnable whenFinish) {
      if (animationId != null) {
         AnimationModifier modifier = AnimationModifier.builder().type(Type.PLAY_ONCE).build();
         this.tracker.animate(animationId, modifier, whenFinish);
      }
   }

   public double height() {
      return this.tracker.height();
   }

   public void scale(float scale) {
      this.tracker.scaler(ModelScaler.value(scale));
   }

   public void glow(boolean enabled, int rgb) {
      for (RenderedBone bone : this.tracker.bones()) {
         ModelDisplay display = bone.getDisplay();
         if (display != null) {
            display.glow(enabled);
            display.glowColor(rgb);
         }
      }
   }

   public void rotation(float yaw, float pitch) {
      if (this.backingEntity != null) {
         this.backingEntity.setRotation(yaw, pitch);
      }
   }

   public void viewRange(float viewRange) {
      for (RenderedBone bone : this.tracker.bones()) {
         ModelDisplay display = bone.getDisplay();
         if (display != null) {
            display.viewRange(viewRange);
         }
      }
   }

   public void onInteract(Consumer<Player> onLeftClick, Consumer<Player> onRightClick) {
      if (this.interactListener != null) {
         this.interactListener.unregister();
      }

      UUID trackerRegistryId = this.tracker.registry().uuid();
      this.interactListener = BetterModel.eventBus().subscribe(() -> true, HitBoxInteractAtEvent.class, event -> {
         EntityTrackerRegistry registry = (EntityTrackerRegistry)event.getHitBox().registry().orElse(null);
         if (registry != null && registry.uuid().equals(trackerRegistryId)) {
            Player player = Bukkit.getPlayer(event.getWho().uuid());
            if (player != null) {
               if (event.getHand() == ModelInteractionHand.LEFT) {
                  onLeftClick.accept(player);
               } else {
                  onRightClick.accept(player);
               }
            }
         }
      });
   }

   public void close() {
      if (this.interactListener != null) {
         this.interactListener.unregister();
         this.interactListener = null;
      }

      if (this.ownsEntity) {
         this.tracker.close();
         if (this.backingEntity != null) {
            this.backingEntity.remove();
         }
      }
   }
}
