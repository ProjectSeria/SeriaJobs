package id.seria.gacha.limbo.hooks;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.sdk.scheduler.Task;
import id.seria.gacha.lib.common.modelengine.interfaces.animation.MegAnimationProperty;
import id.seria.gacha.lib.common.modelengine.interfaces.event.MegModelRegistrationEvent;
import id.seria.gacha.lib.common.modelengine.interfaces.model.MegActiveModel;
import id.seria.gacha.limbo.hooks.modelengine.ModelEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Deprecated
public class ModelEngineHook {
   private static PhoenixCrates PLUGIN;
   private static final List<Runnable> QUEUED_REQUESTS = new ArrayList<>();
   private static boolean MODELS_LOADED;

   public ModelEngineHook() {
   }

   public static void initialize(final PhoenixCrates plugin) throws ClassNotFoundException {
      PLUGIN = plugin;
      ModelEngine.initialize(plugin.getOriginal());
      plugin.getEventBus().registerListener(new Listener() {
         @EventHandler
         public void onModelsLoad(MegModelRegistrationEvent event) {
            if (event.getPhase() == MegModelRegistrationEvent.Phase.FINAL) {
               plugin.getScheduler().runSync(() -> {
                  synchronized (ModelEngineHook.QUEUED_REQUESTS) {
                     ModelEngineHook.QUEUED_REQUESTS.forEach(Runnable::run);
                     ModelEngineHook.QUEUED_REQUESTS.clear();
                  }
               });
               ModelEngineHook.MODELS_LOADED = true;
            }
         }
      });
      if (ModelEngine.getApi().getModelGenerator() != null && ModelEngine.getApi().getModelGenerator().isInitialized()) {
         MODELS_LOADED = true;
      }
   }

   public static void playAnimationOnce(MegActiveModel activeModel, Location location, String animationId, Runnable whenFinish) {
      if (activeModel != null && animationId != null) {
         Validate.isTrue(PLUGIN.isEnabled(), "Plugin is not enabled", new Object[0]);
         activeModel.getAnimationHandler().forceStopAllAnimations();
         activeModel.getAnimationHandler().playAnimation(animationId, 0.0, 1.0, 1.0, true);
         awaitAnimationEnd(activeModel, location, animationId, whenFinish);
      }
   }

   public static void awaitAnimationEnd(MegActiveModel activeModel, Location location, String animationId, Runnable whenFinish) {
      if (activeModel != null && animationId != null) {
         AtomicReference<Task> taskReference = new AtomicReference<>();
         AtomicBoolean finished = new AtomicBoolean();
         AtomicReference<Double> lastTime = new AtomicReference<>();
         taskReference.set(PLUGIN.getScheduler().scheduleSync(location, () -> {
            if (finished.get()) {
               cancelTask(taskReference);
            } else {
               MegAnimationProperty property = activeModel.getAnimationHandler().getAnimation(animationId);
               if (property == null) {
                  finishAnimation(taskReference, finished, whenFinish);
               } else if (property.isSimpleProperty()) {
                  Double previousTime = lastTime.getAndSet(property.getTime());
                  if (property.isFinished() || previousTime != null && property.getTime() < previousTime) {
                     finishAnimation(taskReference, finished, whenFinish);
                  }
               }
            }
         }, 1L, 1L));
         if (finished.get()) {
            cancelTask(taskReference);
         }
      }
   }

   private static void finishAnimation(AtomicReference<Task> taskReference, AtomicBoolean finished, Runnable whenFinish) {
      if (finished.compareAndSet(false, true)) {
         cancelTask(taskReference);
         if (whenFinish != null) {
            whenFinish.run();
         }
      }
   }

   private static void cancelTask(AtomicReference<Task> taskReference) {
      Task task = taskReference.get();
      if (task != null) {
         task.cancel();
      }
   }

   public static void whenModelsAvailable(Runnable runnable) {
      Validate.isTrue(isModelEngineAvailable(), "Model Engine not available", new Object[0]);
      if (MODELS_LOADED) {
         runnable.run();
      } else {
         synchronized (QUEUED_REQUESTS) {
            QUEUED_REQUESTS.add(runnable);
         }
      }
   }

   private static boolean isModelEngineAvailable() {
      Validate.isTrue(PLUGIN != null, "Hook not initialized", new Object[0]);
      return PLUGIN.isModelEngineAvailable();
   }

   public static boolean areModelsLoaded() {
      return MODELS_LOADED;
   }
}
