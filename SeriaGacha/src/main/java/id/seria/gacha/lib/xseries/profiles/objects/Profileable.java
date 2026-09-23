package id.seria.gacha.lib.xseries.profiles.objects;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;
import id.seria.gacha.lib.xseries.profiles.PlayerProfiles;
import id.seria.gacha.lib.xseries.profiles.PlayerUUIDs;
import id.seria.gacha.lib.xseries.profiles.exceptions.InvalidProfileException;
import id.seria.gacha.lib.xseries.profiles.exceptions.ProfileException;
import id.seria.gacha.lib.xseries.profiles.exceptions.UnknownPlayerException;
import id.seria.gacha.lib.xseries.profiles.gameprofile.MojangGameProfile;
import id.seria.gacha.lib.xseries.profiles.gameprofile.XGameProfile;
import id.seria.gacha.lib.xseries.profiles.mojang.MojangAPI;
import id.seria.gacha.lib.xseries.profiles.mojang.PlayerProfileFetcherThread;
import id.seria.gacha.lib.xseries.profiles.mojang.ProfileRequestConfiguration;
import id.seria.gacha.lib.xseries.profiles.objects.cache.TimedCacheableProfileable;
import id.seria.gacha.lib.xseries.profiles.objects.transformer.ProfileTransformer;
import id.seria.gacha.lib.xseries.profiles.objects.transformer.TransformableProfile;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftPackage;
import java.lang.invoke.MethodHandle;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface Profileable {
   @Nullable
   @Internal
   @Unmodifiable MojangGameProfile getProfile();

   @Contract(pure = true)
   boolean isReady();

   @Nullable
   @Contract("-> new")
   default ProfileException test() {
      try {
         this.getProfile();
         return null;
      } catch (ProfileException ex) {
         return ex;
      }
   }

   @Nullable
   @Internal
   @Contract("-> new")
   default MojangGameProfile getDisposableProfile() {
      MojangGameProfile profile = this.getProfile();
      return profile == null ? null : profile.copy();
   }

   @NotNull
   @Contract(value = "_ -> new", pure = true)
   default Profileable transform(@NotNull ProfileTransformer... transformers) {
      return new TransformableProfile(this, Arrays.asList(transformers));
   }

   @Nullable
   default String getProfileValue() {
      return PlayerProfiles.getOriginalValue(this.getProfile());
   }

   @NotNull
   @Contract("-> new")
   @Experimental
   default CompletableFuture<Profileable> prepare() {
      return this.isReady()
         ? CompletableFuture.completedFuture(this)
         : XReflection.stacktrace(CompletableFuture.supplyAsync(this::getProfile, PlayerProfileFetcherThread.EXECUTOR).thenApply(x -> this));
   }

   @NotNull
   @Contract("_ -> new")
   @Experimental
   static <C extends Collection<Profileable>> CompletableFuture<C> prepare(@NotNull C profileables) {
      return prepare(profileables, null, null);
   }

   @NotNull
   @Contract("_, _, _ -> new")
   @Experimental
   static <C extends Collection<Profileable>> CompletableFuture<C> prepare(
      @NotNull C profileables, @Nullable ProfileRequestConfiguration config, @Nullable Function<Throwable, Boolean> errorHandler
   ) {
      Objects.requireNonNull(profileables, "Profile list is null");
      if (profileables.isEmpty()) {
         return CompletableFuture.completedFuture(profileables);
      }

      CompletableFuture<Map<UUID, String>> initial = CompletableFuture.completedFuture(new HashMap<>());
      List<String> usernameRequests = new ArrayList<>();
      if (!PlayerUUIDs.isOnlineMode()) {
         for (Profileable profileable : profileables) {
            String username = null;
            if (profileable instanceof Profileable.UsernameProfileable) {
               username = ((Profileable.UsernameProfileable)profileable).username;
            } else if (profileable instanceof Profileable.PlayerProfileable) {
               username = ((Profileable.PlayerProfileable)profileable).username;
            } else if (profileable instanceof Profileable.StringProfileable
               && ((Profileable.StringProfileable)profileable).determineType().type == ProfileInputType.USERNAME) {
               username = ((Profileable.StringProfileable)profileable).string;
            }

            if (username != null) {
               usernameRequests.add(username);
            }
         }

         if (usernameRequests.size() > 1) {
            initial = CompletableFuture.supplyAsync(() -> MojangAPI.usernamesToUUIDs(usernameRequests, config), PlayerProfileFetcherThread.EXECUTOR);
         }
      }

      return XReflection.stacktrace(initial.<Void>thenCompose(a -> {
         List<CompletableFuture<MojangGameProfile>> profileTasks = new ArrayList<>(profileables.size());

         for (Profileable profileablex : profileables) {
            CompletableFuture<MojangGameProfile> profileTask;
            if (profileablex.isReady()) {
               profileTask = CompletableFuture.completedFuture(profileablex.getProfile());
            } else {
               profileTask = CompletableFuture.supplyAsync(profileablex::getProfile, PlayerProfileFetcherThread.EXECUTOR);
               if (errorHandler != null) {
                  profileTask = XReflection.stacktrace(profileTask).exceptionally(ex -> {
                     boolean rethrow = (Boolean)errorHandler.apply(ex);
                     if (rethrow) {
                        throw XReflection.throwCheckedException(ex);
                     } else {
                        return null;
                     }
                  });
               }
            }

            profileTasks.add(profileTask);
         }

         return CompletableFuture.allOf(profileTasks.toArray(new CompletableFuture[0]));
      }).thenApply(a -> profileables));
   }

   @NotNull
   @Contract(pure = true)
   static Profileable username(@NotNull String username) {
      return new Profileable.UsernameProfileable(username);
   }

   @NotNull
   @Contract(pure = true)
   static Profileable of(@NotNull UUID uuid) {
      return new Profileable.UUIDProfileable(uuid);
   }

   @NotNull
   @Contract(pure = true)
   static Profileable of(@NotNull GameProfile profile, boolean fetchTexturesIfNeeded) {
      return of(XGameProfile.of(profile), fetchTexturesIfNeeded);
   }

   @NotNull
   @Contract(pure = true)
   static Profileable of(@NotNull MojangGameProfile profile, boolean fetchTexturesIfNeeded) {
      return fetchTexturesIfNeeded ? new Profileable.RawGameProfileProfileable(profile) : new Profileable.DynamicGameProfileProfileable(profile);
   }

   @NotNull
   @Contract(pure = true)
   static Profileable of(@NotNull OfflinePlayer offlinePlayer) {
      return new Profileable.PlayerProfileable(offlinePlayer);
   }

   @NotNull
   @Contract(pure = true)
   static Profileable of(@NotNull BlockState blockState) {
      return new ProfileContainer.BlockStateProfileContainer((Skull)blockState);
   }

   @NotNull
   @Contract(pure = true)
   static Profileable of(@NotNull Block block) {
      return new ProfileContainer.BlockProfileContainer(block);
   }

   @NotNull
   @Contract(pure = true)
   static Profileable of(@NotNull ItemStack item) {
      return new ProfileContainer.ItemStackProfileContainer(item);
   }

   @NotNull
   @Contract(pure = true)
   static Profileable of(@NotNull ItemMeta meta) {
      return new ProfileContainer.ItemMetaProfileContainer((SkullMeta)meta);
   }

   @NotNull
   @Contract(pure = true)
   static Profileable detect(@NotNull String input) {
      return new Profileable.StringProfileable(input, null);
   }

   @NotNull
   @Contract(pure = true)
   static Profileable of(@NotNull ProfileInputType type, @NotNull String input) {
      Objects.requireNonNull(type, () -> "Cannot profile from a null input type: " + input);
      Objects.requireNonNull(input, () -> "Cannot profile from a null input: " + type);
      return new Profileable.StringProfileable(input, type);
   }

   @Internal
   final class DynamicGameProfileProfileable extends TimedCacheableProfileable {
      private final MojangGameProfile profile;

      public DynamicGameProfileProfileable(MojangGameProfile profile) {
         this.profile = Objects.requireNonNull(profile);
      }

      @Override
      protected MojangGameProfile cacheProfile() {
         return PlayerProfiles.hasTextures(this.profile)
            ? this.profile
            : (PlayerUUIDs.isOnlineMode() ? new Profileable.UUIDProfileable(this.profile.id()) : new Profileable.UsernameProfileable(this.profile.name()))
               .getProfile();
      }
   }

   @Internal
   final class PlayerProfileProfileable extends TimedCacheableProfileable {
      @NotNull
      private final PlayerProfile profile;
      @Nullable
      private PlayerProfile updated;
      private static final MethodHandle CraftPlayerProfile_buildGameProfile = XReflection.ofMinecraft()
         .inPackage(MinecraftPackage.CB, "profile")
         .named("CraftPlayerProfile")
         .method("public com.mojang.authlib.GameProfile buildGameProfile()")
         .reflectOrNull();

      public PlayerProfileProfileable(PlayerProfile profile) {
         this.profile = Objects.requireNonNull(profile);
      }

      @Override
      protected MojangGameProfile cacheProfile() {
         this.updated = (PlayerProfile)this.profile.update().join();
         if (CraftPlayerProfile_buildGameProfile == null) {
            MojangGameProfile gameProfile = XGameProfile.create(this.updated.getUniqueId(), this.updated.getName());
            String skinURL = this.updated.getTextures().getSkin().toString();
            String base64 = PlayerProfiles.encodeBase64("{\"textures\":{\"SKIN\":{\"url\":\"" + skinURL + "\"}}}");
            PlayerProfiles.setTexturesProperty(gameProfile, base64);
            return gameProfile;
         }

         try {
            return XGameProfile.of((GameProfile)CraftPlayerProfile_buildGameProfile.invoke((PlayerProfile)this.updated));
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }
   }

   @Internal
   final class PlayerProfileable extends TimedCacheableProfileable {
      @Nullable
      private final String username;
      @NotNull
      private final UUID id;

      public PlayerProfileable(OfflinePlayer player) {
         Objects.requireNonNull(player);
         this.username = player.getName();
         this.id = player.getUniqueId();
      }

      @Override
      public String getProfileValue() {
         return Strings.isNullOrEmpty(this.username) ? this.id.toString() : this.username;
      }

      @Override
      protected MojangGameProfile cacheProfile() {
         return Strings.isNullOrEmpty(this.username)
            ? new Profileable.UUIDProfileable(this.id).getProfile()
            : new Profileable.UsernameProfileable(this.username).getProfile();
      }
   }

   @Internal
   final class RawGameProfileProfileable implements Profileable {
      private final MojangGameProfile profile;

      public RawGameProfileProfileable(MojangGameProfile profile) {
         this.profile = Objects.requireNonNull(profile);
      }

      @Override
      public boolean isReady() {
         return true;
      }

      @NotNull
      @Override
      public @Unmodifiable MojangGameProfile getProfile() {
         return this.profile;
      }
   }

   @Internal
   final class StringProfileable extends TimedCacheableProfileable {
      private final String string;
      @Nullable
      private ProfileInputType type;

      public StringProfileable(String string, @Nullable ProfileInputType type) {
         this.string = Objects.requireNonNull(string, "Input string is null");
         this.type = type;
      }

      @Override
      public String getProfileValue() {
         return this.string;
      }

      @Override
      protected Duration expiresAfter() {
         this.determineType();
         if (this.type == null) {
            return Duration.ZERO;
         }

         switch (this.type) {
            case USERNAME:
            case UUID:
               return super.expiresAfter();
            default:
               return Duration.ZERO;
         }
      }

      private Profileable.StringProfileable determineType() {
         if (this.type == null) {
            this.type = ProfileInputType.typeOf(this.string);
         }

         return this;
      }

      @Override
      protected MojangGameProfile cacheProfile() {
         this.determineType();
         if (this.type == null) {
            throw new InvalidProfileException(this.string, "Unknown skull string value: " + this.string);
         } else {
            return this.type.getProfile(this.string);
         }
      }
   }

   @Internal
   final class UUIDProfileable extends TimedCacheableProfileable {
      private final UUID id;

      public UUIDProfileable(UUID id) {
         this.id = Objects.requireNonNull(id, "UUID cannot be null");
      }

      @Override
      public String getProfileValue() {
         return this.id.toString();
      }

      @Override
      protected MojangGameProfile cacheProfile() {
         MojangGameProfile profile = XGameProfile.of(MojangAPI.getCachedProfileByUUID(this.id));
         return PlayerProfiles.hasTextures(profile) ? profile : XGameProfile.of(MojangAPI.getOrFetchProfile(profile));
      }
   }

   @Internal
   final class UsernameProfileable extends TimedCacheableProfileable {
      private final String username;
      private Boolean valid;

      public UsernameProfileable(String username) {
         this.username = Objects.requireNonNull(username);
      }

      @Override
      public String getProfileValue() {
         return this.username;
      }

      @Override
      protected MojangGameProfile cacheProfile() {
         if (this.valid == null) {
            this.valid = ProfileInputType.USERNAME.pattern.matcher(this.username).matches();
         }

         if (!this.valid) {
            throw new InvalidProfileException(this.username, "Invalid username: '" + this.username + '\'');
         }

         Optional<GameProfile> profileOpt = MojangAPI.getMojangCachedProfileFromUsername(this.username);
         if (!profileOpt.isPresent()) {
            throw new UnknownPlayerException(this.username, "Cannot find player named '" + this.username + '\'');
         }

         MojangGameProfile profile = XGameProfile.of(profileOpt.get());
         return PlayerProfiles.hasTextures(profile) ? profile : XGameProfile.of(MojangAPI.getOrFetchProfile(profile));
      }
   }
}
