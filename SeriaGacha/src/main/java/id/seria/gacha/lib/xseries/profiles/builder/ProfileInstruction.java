package id.seria.gacha.lib.xseries.profiles.builder;

import id.seria.gacha.lib.xseries.profiles.ProfileLogger;
import id.seria.gacha.lib.xseries.profiles.exceptions.InvalidProfileException;
import id.seria.gacha.lib.xseries.profiles.exceptions.ProfileChangeException;
import id.seria.gacha.lib.xseries.profiles.exceptions.ProfileException;
import id.seria.gacha.lib.xseries.profiles.gameprofile.MojangGameProfile;
import id.seria.gacha.lib.xseries.profiles.mojang.PlayerProfileFetcherThread;
import id.seria.gacha.lib.xseries.profiles.mojang.ProfileRequestConfiguration;
import id.seria.gacha.lib.xseries.profiles.objects.DelegateProfileable;
import id.seria.gacha.lib.xseries.profiles.objects.ProfileContainer;
import id.seria.gacha.lib.xseries.profiles.objects.Profileable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class ProfileInstruction<T> implements DelegateProfileable {
   private final ProfileContainer<T> profileContainer;
   private Profileable profileable;
   private final List<Profileable> fallbacks = new ArrayList<>();
   private Consumer<ProfileFallback<T>> onFallback;
   private ProfileRequestConfiguration profileRequestConfiguration;
   private boolean lenient = false;

   protected ProfileInstruction(ProfileContainer<T> profileContainer) {
      this.profileContainer = profileContainer;
   }

   @NotNull
   @Contract(mutates = "this")
   public T removeProfile() {
      this.profileContainer.setProfile(null);
      return this.profileContainer.getObject();
   }

   @NotNull
   @Contract(value = "_ -> this", mutates = "this")
   @Experimental
   public ProfileInstruction<T> profileRequestConfiguration(ProfileRequestConfiguration config) {
      this.profileRequestConfiguration = config;
      return this;
   }

   @NotNull
   @Contract(value = "-> this", mutates = "this")
   public ProfileInstruction<T> lenient() {
      this.lenient = true;
      return this;
   }

   @Nullable
   @Internal
   @Override
   public MojangGameProfile getProfile() {
      return this.profileContainer.getProfile();
   }

   @Contract(pure = true)
   @Internal
   @Override
   public Profileable getDelegateProfile() {
      return this.profileContainer;
   }

   @NotNull
   @Contract(value = "_ -> this", mutates = "this")
   public ProfileInstruction<T> profile(@NotNull Profileable profileable) {
      this.profileable = Objects.requireNonNull(profileable, "Profileable is null");
      return this;
   }

   @NotNull
   @Contract(value = "_ -> this", mutates = "this")
   public ProfileInstruction<T> fallback(@NotNull Profileable... fallbacks) {
      Objects.requireNonNull(fallbacks, "fallbacks array is null");
      this.fallbacks.addAll(Arrays.asList(fallbacks));
      return this;
   }

   @NotNull
   @Contract(value = "_ -> this", mutates = "this")
   public ProfileInstruction<T> onFallback(@Nullable Consumer<ProfileFallback<T>> onFallback) {
      this.onFallback = onFallback;
      return this;
   }

   @NotNull
   @Contract(value = "_ -> this", mutates = "this")
   public ProfileInstruction<T> onFallback(@NotNull Runnable onFallback) {
      Objects.requireNonNull(onFallback, "onFallback runnable is null");
      this.onFallback = fallback -> onFallback.run();
      return this;
   }

   @NotNull
   public T apply() {
      Objects.requireNonNull(this.profileable, "No profile was set");
      ProfileChangeException exception = null;
      List<Profileable> tries = new ArrayList<>(2 + this.fallbacks.size());
      tries.add(this.profileable);
      tries.addAll(this.fallbacks);
      if (this.lenient) {
         tries.add(XSkull.getDefaultProfile());
      }

      boolean success = false;
      boolean tryingFallbacks = false;

      for (Profileable profileable : tries) {
         try {
            MojangGameProfile gameProfile = profileable.getDisposableProfile();
            if (gameProfile != null) {
               this.profileContainer.setProfile(gameProfile);
               success = true;
               break;
            }

            if (exception == null) {
               exception = new ProfileChangeException("Could not set the profile for " + this.profileContainer);
            }

            exception.addSuppressed(new InvalidProfileException(profileable.toString(), "Profile doesn't have a value: " + profileable));
            tryingFallbacks = true;
         } catch (ProfileException ex) {
            if (exception == null) {
               exception = new ProfileChangeException("Could not set the profile for " + this.profileContainer);
            }

            exception.addSuppressed(ex);
            tryingFallbacks = true;
         }
      }

      if (exception != null) {
         if (!success && !this.lenient) {
            throw exception;
         }

         ProfileLogger.debug("apply() silenced exception {}", exception);
      }

      T object = this.profileContainer.getObject();
      if (tryingFallbacks && this.onFallback != null) {
         ProfileFallback<T> fallback = new ProfileFallback<>(this, object, exception);
         this.onFallback.accept(fallback);
         object = fallback.getObject();
      }

      return object;
   }

   @NotNull
   public CompletableFuture<T> applyAsync() {
      return CompletableFuture.supplyAsync(this::apply, PlayerProfileFetcherThread.EXECUTOR);
   }
}
