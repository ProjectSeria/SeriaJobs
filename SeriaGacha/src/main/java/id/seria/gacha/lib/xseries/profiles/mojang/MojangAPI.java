package id.seria.gacha.lib.xseries.profiles.mojang;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import id.seria.gacha.lib.xseries.profiles.PlayerProfiles;
import id.seria.gacha.lib.xseries.profiles.PlayerUUIDs;
import id.seria.gacha.lib.xseries.profiles.ProfileLogger;
import id.seria.gacha.lib.xseries.profiles.ProfilesCore;
import id.seria.gacha.lib.xseries.profiles.exceptions.UnknownPlayerException;
import id.seria.gacha.lib.xseries.profiles.gameprofile.MojangGameProfile;
import id.seria.gacha.lib.xseries.profiles.gameprofile.XGameProfile;
import id.seria.gacha.lib.xseries.profiles.gameprofile.property.XProperty;
import id.seria.gacha.lib.xseries.profiles.lock.KeyedLock;
import id.seria.gacha.lib.xseries.profiles.lock.MojangRequestQueue;
import id.seria.gacha.lib.xseries.profiles.objects.ProfileInputType;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.Obsolete;

@Internal
public final class MojangAPI {
   private static final MojangProfileCache MOJANG_PROFILE_CACHE = !ProfilesCore.NULLABILITY_RECORD_UPDATE
      ? new MojangProfileCache.GameProfileCache(ProfilesCore.YggdrasilMinecraftSessionService_insecureProfiles)
      : new MojangProfileCache.ProfileResultCache(ProfilesCore.YggdrasilMinecraftSessionService_insecureProfiles);
   private static final Cache<UUID, Optional<GameProfile>> INSECURE_PROFILES = CacheBuilder.newBuilder().expireAfterWrite(6L, TimeUnit.HOURS).build();
   private static final boolean REQUIRE_SECURE_PROFILES = false;
   private static final MinecraftClient USERNAME_TO_UUID = new MinecraftClient(
      "GET", "https://api.mojang.com/users/profiles/minecraft/", new RateLimiter(600, Duration.ofMinutes(10L))
   );
   private static final MinecraftClient USERNAMES_TO_UUIDS = new MinecraftClient(
      "POST", "https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname", new RateLimiter(600, Duration.ofMinutes(10L))
   );
   private static final MinecraftClient UUID_TO_PROFILE = new MinecraftClient(
      "GET", "https://sessionserver.mojang.com/session/minecraft/profile/", new RateLimiter(200, Duration.ofMinutes(1L))
   );

   public MojangAPI() {
   }

   @Nullable
   public static UUID requestUsernameToUUID(@NotNull String username) throws IOException {
      JsonElement requestElement = USERNAME_TO_UUID.session(null).append(username).request();
      if (requestElement == null) {
         return null;
      } else {
         JsonObject userJson = requestElement.getAsJsonObject();
         JsonElement idElement = userJson.get("id");
         if (idElement == null) {
            throw new IllegalArgumentException("No 'id' field for UUID request for '" + username + "': " + userJson);
         } else {
            return PlayerUUIDs.UUIDFromDashlessString(idElement.getAsString());
         }
      }
   }

   @Obsolete
   private static GameProfile getCachedProfileByUsername(String username) {
      try {
         Object profile = (Object)ProfilesCore.GameProfileCache_get$profileByName$.invoke((Object)ProfilesCore.USER_CACHE, (String)username);
         if (profile instanceof Optional) {
            profile = ((Optional)profile).orElse(null);
         }

         GameProfile gameProfile = profile == null
            ? PlayerProfiles.createGameProfile(PlayerUUIDs.IDENTITY_UUID, username).object()
            : PlayerProfiles.sanitizeProfile((GameProfile)profile);
         ProfileLogger.debug("The cached profile for {} -> {}", username, profile);
         return gameProfile;
      } catch (Throwable throwable) {
         ProfileLogger.LOGGER.error("Unable to get cached profile by username: {}", username, throwable);
         return null;
      }
   }

   public static Optional<GameProfile> getMojangCachedProfileFromUsername(String username) {
      try {
         return getMojangCachedProfileFromUsername0(username);
      } catch (Throwable e) {
         throw XReflection.throwCheckedException(e);
      }
   }

   private static Optional<GameProfile> getMojangCachedProfileFromUsername0(String username) throws Throwable {
      String normalized = username.toLowerCase(Locale.ENGLISH);
      Object userCacheEntry = ProfilesCore.UserCache_profilesByName.get(normalized);
      Optional<GameProfile> optional;
      if (userCacheEntry != null) {
         if (ProfilesCore.GameProfileInfo_setLastAccess != null && ProfilesCore.UserCache_getNextOperation != null) {
            long nextOperation = (long)ProfilesCore.UserCache_getNextOperation.invoke((Object)ProfilesCore.USER_CACHE);
            ProfilesCore.GameProfileInfo_setLastAccess.invoke((Object)userCacheEntry, (long)nextOperation);
         }

         if (ProfilesCore.GameProfileInfo_getProfile != null) {
            optional = Optional.of((GameProfile)ProfilesCore.GameProfileInfo_getProfile.invoke((Object)userCacheEntry));
         } else {
            Object nameAndId = (Object)ProfilesCore.GameProfileInfo_nameAndId.invoke((Object)userCacheEntry);
            optional = Optional.of(
               XGameProfile.create((UUID)ProfilesCore.NameAndId_id.invoke((Object)nameAndId), (String)ProfilesCore.NameAndId_name.invoke((Object)nameAndId))
                  .object()
            );
         }
      } else {
         UUID realUUID = PlayerUUIDs.getRealUUIDOfPlayer(username);
         if (realUUID == null) {
            return Optional.empty();
         }

         GameProfile profile = PlayerProfiles.createGameProfile(PlayerUUIDs.isOnlineMode() ? realUUID : PlayerUUIDs.getOfflineUUID(username), username)
            .object();
         optional = Optional.of(profile);
         cacheProfile(profile);
      }

      return optional;
   }

   public static Map<UUID, String> usernamesToUUIDs(@NotNull Collection<String> usernames, @Nullable ProfileRequestConfiguration config) {
      if (usernames != null && !usernames.isEmpty()) {
         for (String username : usernames) {
            if (username == null || !ProfileInputType.USERNAME.pattern.matcher(username).matches()) {
               throw new IllegalArgumentException("One of the requested usernames is invalid: " + username + " in " + usernames);
            }
         }

         Map<UUID, String> mapped = new HashMap<>(usernames.size());
         Map<String, KeyedLock<String, UUID>> pendingUsernames = new HashMap<>(usernames.size());

         try {
            for (String username : usernames) {
               if (!pendingUsernames.containsKey(username)) {
                  KeyedLock<String, UUID> lock = MojangRequestQueue.USERNAME_REQUESTS.lock(username, PlayerUUIDs.USERNAME_TO_ONLINE::get);
                  UUID cached = lock.getOrRetryValue();
                  if (cached != null) {
                     mapped.put(cached, username);
                     lock.unlock();
                  } else {
                     pendingUsernames.put(username, lock);
                  }
               }
            }

            if (pendingUsernames.isEmpty()) {
               return mapped;
            }

            boolean onlineMode = PlayerUUIDs.isOnlineMode();

            for (List<String> batch : Iterables.partition(pendingUsernames.keySet(), 10)) {
               JsonArray response;
               try {
                  response = USERNAMES_TO_UUIDS.session(config).body(batch).request().getAsJsonArray();
               } catch (IOException ex) {
                  throw new IllegalStateException("Failed to request UUIDs for username batch: " + batch, ex);
               }

               for (JsonElement element : response) {
                  JsonObject obj = element.getAsJsonObject();
                  String name = obj.get("name").getAsString();
                  UUID realId = PlayerUUIDs.UUIDFromDashlessString(obj.get("id").getAsString());
                  UUID offlineId = PlayerUUIDs.getOfflineUUID(name);
                  PlayerUUIDs.USERNAME_TO_ONLINE.put(name, realId);
                  PlayerUUIDs.ONLINE_TO_OFFLINE.put(realId, offlineId);
                  PlayerUUIDs.OFFLINE_TO_ONLINE.put(offlineId, realId);
                  if (!ProfilesCore.UserCache_profilesByName.containsKey(name)) {
                     cacheProfile(PlayerProfiles.createGameProfile(onlineMode ? realId : offlineId, name).object());
                  }

                  String prev = mapped.put(realId, name);
                  if (prev != null) {
                     throw new IllegalArgumentException("Got duplicate usernames for UUID: " + realId + " (" + prev + " -> " + name + ')');
                  }
               }
            }
         } finally {
            for (KeyedLock<String, UUID> lock : pendingUsernames.values()) {
               lock.unlock();
            }
         }

         return mapped;
      } else {
         throw new IllegalArgumentException("Usernames are null or empty");
      }
   }

   @NotNull
   public static GameProfile getCachedProfileByUUID(UUID uuid) {
      uuid = PlayerUUIDs.isOnlineMode() ? uuid : PlayerUUIDs.ONLINE_TO_OFFLINE.getOrDefault(uuid, uuid);

      try {
         Object profile = (Object)ProfilesCore.GameProfileCache_get$profileByUUID$.invoke((Object)ProfilesCore.USER_CACHE, (UUID)uuid);
         if (profile instanceof Optional) {
            profile = ((Optional)profile).orElse(null);
         }

         if (ProfilesCore.NameAndId_name != null && profile != null) {
            String name = (String)ProfilesCore.NameAndId_name.invoke((Object)profile);
            UUID id = (UUID)ProfilesCore.NameAndId_id.invoke((Object)profile);
            profile = XGameProfile.create(id, name).object();
         }

         ProfileLogger.debug("The cached profile for {} -> {}", uuid, profile);
         return profile == null ? PlayerProfiles.createNamelessGameProfile(uuid).object() : PlayerProfiles.sanitizeProfile((GameProfile)profile);
      } catch (Throwable throwable) {
         ProfileLogger.LOGGER.error("Unable to get cached profile by UUID: {}", uuid, throwable);
         return PlayerProfiles.createNamelessGameProfile(uuid).object();
      }
   }

   private static void cacheProfile(GameProfile profile) {
      try {
         if (ProfilesCore.NameAndId$ctor_GameProfile == null) {
            ProfilesCore.CachedUserNameToIdResolver_add.invoke((Object)ProfilesCore.USER_CACHE, (GameProfile)profile);
         } else {
            Object nameAndId = (Object)ProfilesCore.NameAndId$ctor_GameProfile.invoke((GameProfile)profile);
            ProfilesCore.CachedUserNameToIdResolver_add.invoke((Object)ProfilesCore.USER_CACHE, (Object)nameAndId);
         }

         ProfileLogger.debug("Profile is now cached: {}", profile);
      } catch (Throwable throwable) {
         ProfileLogger.LOGGER.error("Unable to cache profile {}", profile);
         throwable.printStackTrace();
      }
   }

   @NotNull
   public static GameProfile getOrFetchProfile(@NotNull MojangGameProfile profile) throws UnknownPlayerException {
      UUID realUUID;
      if (profile.name().equals("XSeries")) {
         realUUID = profile.id();
      } else {
         realUUID = PlayerUUIDs.getRealUUIDOfPlayer(profile.name(), profile.id());
         if (realUUID == null) {
            throw new UnknownPlayerException(profile.name(), "Player with the given properties not found: " + profile);
         }
      }

      try {
         KeyedLock<UUID, GameProfile> lock = MojangRequestQueue.UUID_REQUESTS.lock(realUUID, () -> handleCache(profile.object(), realUUID));

         GameProfile var12;
         label55: {
            GameProfile var8;
            try {
               GameProfile cached = lock.getOrRetryValue();
               if (cached != null) {
                  var12 = cached;
                  break label55;
               }

               JsonElement request = requestProfile(profile.object(), realUUID);
               JsonObject profileData = request.getAsJsonObject();
               List<String> profileActions = new ArrayList<>();
               GameProfile fetchedProfile = createGameProfile(profileData, profileActions);
               fetchedProfile = PlayerProfiles.sanitizeProfile(fetchedProfile);
               cacheProfile(fetchedProfile);
               INSECURE_PROFILES.put(realUUID, Optional.of(fetchedProfile));
               MOJANG_PROFILE_CACHE.cache(new PlayerProfile(realUUID, profile.object(), fetchedProfile, profileActions));
               var8 = fetchedProfile;
            } catch (Throwable var10) {
               if (lock != null) {
                  try {
                     lock.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (lock != null) {
               lock.close();
            }

            return var8;
         }

         if (lock != null) {
            lock.close();
         }

         return var12;
      } catch (Throwable ex) {
         throw new IllegalStateException("Failed to fetch profile for " + profile, ex);
      }
   }

   @Nullable
   private static GameProfile handleCache(@NotNull GameProfile profile, UUID realUUID) {
      Optional<GameProfile> cached = (Optional<GameProfile>)INSECURE_PROFILES.getIfPresent(realUUID);
      if (cached != null) {
         ProfileLogger.debug("Found cached profile from UUID ({}): {} -> {}", realUUID, profile, cached);
         if (cached.isPresent()) {
            return cached.get();
         } else {
            throw new UnknownPlayerException(realUUID, "Player with the given properties not found: " + profile);
         }
      } else {
         Optional<GameProfile> mojangCache = MOJANG_PROFILE_CACHE.get(realUUID, profile);
         if (mojangCache != null) {
            INSECURE_PROFILES.put(realUUID, mojangCache);
            if (mojangCache.isPresent()) {
               return mojangCache.get();
            } else {
               throw new UnknownPlayerException(realUUID, "Player with the given properties not found: " + profile);
            }
         } else {
            return null;
         }
      }
   }

   @NotNull
   private static JsonElement requestProfile(@NotNull GameProfile profile, UUID realUUID) {
      JsonElement request;
      try {
         request = UUID_TO_PROFILE.session(null).append(PlayerUUIDs.toUndashedUUID(realUUID) + "?unsigned=" + true).request();
      } catch (IOException e) {
         throw new IllegalStateException("Failed to request profile: " + profile + " with real UUID: " + realUUID, e);
      }

      if (request == null) {
         INSECURE_PROFILES.put(realUUID, Optional.empty());
         MOJANG_PROFILE_CACHE.cache(new PlayerProfile(realUUID, profile, null, null));
         throw new UnknownPlayerException(realUUID, "Player with the given properties not found: " + profile);
      } else {
         return request;
      }
   }

   @NotNull
   private static GameProfile createGameProfile(JsonObject profileData, List<String> profileActions) {
      UUID id = PlayerUUIDs.UUIDFromDashlessString(profileData.get("id").getAsString());
      String name = profileData.get("name").getAsString();
      ListMultimap<String, Property> propertyMap = MultimapBuilder.hashKeys().arrayListValues().build();
      JsonElement propertiesEle = profileData.get("properties");
      if (propertiesEle != null) {
         for (JsonElement prop : propertiesEle.getAsJsonArray()) {
            JsonObject obj = prop.getAsJsonObject();
            String propName = obj.get("name").getAsString();
            String propValue = obj.get("value").getAsString();
            JsonElement sig = obj.get("signature");
            Property property;
            if (sig != null) {
               property = new Property(propName, propValue, sig.getAsString());
            } else {
               property = new Property(propName, propValue);
            }

            propertyMap.put(propName, property);
         }
      }

      JsonElement profileActionsElement = profileData.get("profileActions");
      if (profileActionsElement != null) {
         for (JsonElement action : profileActionsElement.getAsJsonArray()) {
            profileActions.add(action.getAsString());
         }
      }

      PropertyMap properties = XProperty.createPropertyMap(propertyMap);
      return PlayerProfiles.createGameProfile(id, name, properties).object();
   }
}
