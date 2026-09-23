package id.seria.gacha.sdk.hooks.bettermodel;

import id.seria.gacha.api.sdk.scheduler.Scheduler;
import id.seria.gacha.api.sdk.scheduler.Task;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.data.renderer.ModelRenderer;
import kr.toxicity.model.api.tracker.EntityTracker;
import kr.toxicity.model.api.tracker.EntityTrackerRegistry;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.jetbrains.annotations.Nullable;

public class BetterModelProvider extends ServerHook {
   private final List<Runnable> queuedRequests = new ArrayList<>();
   private boolean modelsLoaded = false;
   private Task modelLoadPollTask;
   private static BetterModelProvider BETTER_MODEL;

   protected BetterModelProvider() {
   }

   @Override
   public void init() {
      if (!BetterModel.modelKeys().isEmpty()) {
         this.markModelsLoaded();
      } else {
         this.modelLoadPollTask = this.getHookRegistry().getPlugin().getScheduler().scheduleSync(() -> {
            if (!BetterModel.modelKeys().isEmpty()) {
               this.markModelsLoaded();
               this.cancelModelLoadPollTask();
            }
         }, 20L, 20L);
      }
   }

   @Override
   public void load() {
   }

   @Override
   public void unload() {
      this.cancelModelLoadPollTask();
      synchronized (this.queuedRequests) {
         this.queuedRequests.clear();
         this.modelsLoaded = false;
      }
   }

   private void cancelModelLoadPollTask() {
      if (this.modelLoadPollTask != null) {
         this.modelLoadPollTask.cancel();
         this.modelLoadPollTask = null;
      }
   }

   @Override
   public void reload() {
      this.unload();
      this.load();
   }

   public BetterModelHookProvider getProvider() {
      return (BetterModelHookProvider)super.getProvider();
   }

   public List<String> modelKeys() {
      return BetterModel.modelKeys().stream().sorted(String::compareToIgnoreCase).collect(Collectors.toList());
   }

   public List<String> animationKeys(String modelId) {
      Optional<ModelRenderer> model = BetterModel.model(modelId);
      return model.<List<String>>map(
            modelRenderer -> modelRenderer.animations().keySet().stream().sorted(String::compareToIgnoreCase).collect(Collectors.toList())
         )
         .orElse(Collections.emptyList());
   }

   @Nullable
   public BetterModelDisplay findDisplay(UUID entityUuid) {
      EntityTracker tracker = EntityTrackerRegistry.registry(entityUuid).first();
      return tracker == null ? null : new BetterModelDisplay(tracker);
   }

   @Nullable
   public BetterModelDisplay createDisplay(String modelId, Location location) {
      ModelRenderer renderer = this.getModelRenderer(modelId);
      if (renderer == null) {
         return null;
      } else {
         ArmorStand backingEntity = (ArmorStand)location.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setInvulnerable(true);
            stand.setGravity(false);
            stand.setSilent(true);
            stand.setPersistent(false);
            stand.setCustomNameVisible(false);
         });
         EntityTracker tracker = renderer.create(BukkitAdapter.adapt(backingEntity));
         if (tracker == null) {
            backingEntity.remove();
            return null;
         } else {
            return new BetterModelDisplay(backingEntity, tracker);
         }
      }
   }

   public void whenModelsAvailable(Runnable runnable) {
      synchronized (this.queuedRequests) {
         if (!this.modelsLoaded) {
            this.queuedRequests.add(runnable);
            return;
         }
      }

      runnable.run();
   }

   public boolean areModelsLoaded() {
      return this.modelsLoaded;
   }

   private void markModelsLoaded() {
      List<Runnable> tasksToRun;
      synchronized (this.queuedRequests) {
         if (this.modelsLoaded) {
            return;
         }

         this.modelsLoaded = true;
         tasksToRun = new ArrayList<>(this.queuedRequests);
         this.queuedRequests.clear();
      }

      Scheduler scheduler = this.getHookRegistry().getPlugin().getScheduler();

      for (Runnable task : tasksToRun) {
         scheduler.runSync(task);
      }
   }

   @Nullable
   public ModelRenderer getModelRenderer(String modelId) {
      return BetterModel.modelOrNull(modelId);
   }
}
