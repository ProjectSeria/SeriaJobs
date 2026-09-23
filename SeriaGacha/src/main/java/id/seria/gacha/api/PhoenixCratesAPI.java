package id.seria.gacha.api;

import id.seria.gacha.api.managers.CratesManager;
import id.seria.gacha.api.managers.KeysManager;
import id.seria.gacha.api.managers.PlayersManager;
import id.seria.gacha.api.registries.AnimationsRegistry;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class PhoenixCratesAPI {
   private static final PhoenixCratesAPI.Unsafe UNSAFE = new PhoenixCratesAPI.Unsafe();
   private static CratesPlugin PLUGIN_INSTANCE;
   private static CratesPlugin PROVIDER;
   private static AnimationsRegistry ANIMATION_REGISTRY;

   @Internal
   public static PhoenixCratesAPI.Unsafe unsafe() {
      return UNSAFE;
   }

   public static AnimationsRegistry getAnimationsRegistry() {
      return Objects.requireNonNull(ANIMATION_REGISTRY, "AnimationRegistry has not been initialized.");
   }

   public static CratesPlugin getPluginInstance() {
      return Objects.requireNonNull(PLUGIN_INSTANCE, "API not initialized");
   }

   public static CratesManager getCratesManager() {
      return Objects.requireNonNull(PROVIDER, "CratesPlugin provider has not been initialized.").getCratesManager();
   }

   public static KeysManager getKeysManager() {
      return Objects.requireNonNull(PROVIDER, "CratesPlugin provider has not been initialized.").getKeysManager();
   }

   public static PlayersManager getPlayersManager() {
      return Objects.requireNonNull(PROVIDER, "CratesPlugin provider has not been initialized.").getPlayersManager();
   }

   private PhoenixCratesAPI() {
   }

   @Internal
   public static class Unsafe {
      public Unsafe() {
      }

      public void setPluginInstance(CratesPlugin newProvider) {
         if (PhoenixCratesAPI.PROVIDER != null) {
            throw new IllegalStateException("Provider is already set and cannot be changed.");
         }

         PhoenixCratesAPI.PROVIDER = newProvider;
      }

      public void setAnimationRegistry(AnimationsRegistry registry) {
         if (PhoenixCratesAPI.ANIMATION_REGISTRY != null) {
            throw new IllegalStateException("AnimationRegistry is already set and cannot be changed.");
         }

         PhoenixCratesAPI.ANIMATION_REGISTRY = registry;
      }
   }
}
