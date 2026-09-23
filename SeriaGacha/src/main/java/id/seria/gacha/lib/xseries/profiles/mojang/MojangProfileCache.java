package id.seria.gacha.lib.xseries.profiles.mojang;

import com.google.common.base.Strings;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileActionType;
import com.mojang.authlib.yggdrasil.ProfileResult;
import id.seria.gacha.lib.xseries.profiles.PlayerProfiles;
import id.seria.gacha.lib.xseries.profiles.gameprofile.MojangGameProfile;
import id.seria.gacha.lib.xseries.profiles.gameprofile.XGameProfile;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
abstract class MojangProfileCache {
   MojangProfileCache() {
   }

   abstract void cache(PlayerProfile var1);

   @Nullable
   abstract Optional<GameProfile> get(UUID var1, GameProfile var2);

   protected static final class GameProfileCache extends MojangProfileCache {
      private final LoadingCache<GameProfile, GameProfile> insecureProfiles;

      GameProfileCache(LoadingCache<?, ?> insecureProfiles) {
         this.insecureProfiles = (LoadingCache<GameProfile, GameProfile>)insecureProfiles;
      }

      @Override
      void cache(PlayerProfile playerProfile) {
         if (playerProfile.exists()) {
            this.insecureProfiles.put(playerProfile.requestedGameProfile, playerProfile.fetchedGameProfile);
         } else {
            this.insecureProfiles.put(playerProfile.requestedGameProfile, PlayerProfiles.NIL);
         }
      }

      @Nullable
      @Override
      Optional<GameProfile> get(UUID realId, GameProfile gameProfile) {
         MojangGameProfile profile = XGameProfile.of(gameProfile);
         String profileName = profile.name();
         if (!Strings.isNullOrEmpty(profileName) && !profileName.equals("XSeries")) {
            GameProfile cache = (GameProfile)this.insecureProfiles.getIfPresent(XGameProfile.create(realId, profile.name()).object());
            if (cache == PlayerProfiles.NIL) {
               return Optional.empty();
            } else {
               return cache == null ? null : Optional.of(cache);
            }
         } else {
            return null;
         }
      }
   }

   protected static final class ProfileResultCache extends MojangProfileCache {
      private final LoadingCache<UUID, Optional<ProfileResult>> insecureProfiles;

      ProfileResultCache(LoadingCache<?, ?> insecureProfiles) {
         this.insecureProfiles = (LoadingCache<UUID, Optional<ProfileResult>>)insecureProfiles;
      }

      @Override
      void cache(PlayerProfile playerProfile) {
         if (playerProfile.exists()) {
            ProfileResult profileResult = new ProfileResult(playerProfile.fetchedGameProfile, playerProfile.profileActions.stream().map(x -> {
               try {
                  return ProfileActionType.valueOf(x);
               } catch (IllegalArgumentException ex) {
                  return null;
               }
            }).filter(Objects::nonNull).collect(Collectors.toSet()));
            this.insecureProfiles.put(playerProfile.realUUID, Optional.of(profileResult));
         } else {
            this.insecureProfiles.put(playerProfile.realUUID, Optional.empty());
         }
      }

      @Override
      Optional<GameProfile> get(UUID realId, GameProfile gameProfile) {
         Optional<ProfileResult> cache = (Optional<ProfileResult>)this.insecureProfiles.getIfPresent(realId);
         return cache == null ? null : cache.map(ProfileResult::profile);
      }
   }
}
