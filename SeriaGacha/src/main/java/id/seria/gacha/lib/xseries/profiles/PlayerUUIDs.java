package id.seria.gacha.lib.xseries.profiles;

import com.google.common.base.Strings;
import id.seria.gacha.lib.xseries.profiles.lock.KeyedLock;
import id.seria.gacha.lib.xseries.profiles.lock.MojangRequestQueue;
import id.seria.gacha.lib.xseries.profiles.mojang.MojangAPI;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class PlayerUUIDs {
   public static final UUID IDENTITY_UUID = new UUID(0L, 0L);
   private static final Pattern UUID_NO_DASHES = Pattern.compile("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})");
   public static final Map<UUID, UUID> OFFLINE_TO_ONLINE = new HashMap<>();
   public static final Map<UUID, UUID> ONLINE_TO_OFFLINE = new HashMap<>();
   public static final Map<String, UUID> USERNAME_TO_ONLINE = new HashMap<>();

   public PlayerUUIDs() {
   }

   public static UUID UUIDFromDashlessString(String dashlessUUIDString) {
      Matcher matcher = UUID_NO_DASHES.matcher(dashlessUUIDString);

      try {
         return UUID.fromString(matcher.replaceFirst("$1-$2-$3-$4-$5"));
      } catch (IllegalArgumentException ex) {
         throw new IllegalArgumentException("Cannot convert from dashless UUID: " + dashlessUUIDString, ex);
      }
   }

   public static String toUndashedUUID(UUID id) {
      return id.toString().replace("-", "");
   }

   @NotNull
   public static UUID getOfflineUUID(@NotNull String username) {
      return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
   }

   public static boolean isOnlineMode() {
      return Bukkit.getOnlineMode();
   }

   @Nullable
   public static UUID getRealUUIDOfPlayer(@NotNull String username) {
      if (Strings.isNullOrEmpty(username)) {
         throw new IllegalArgumentException("Username is null or empty: " + username);
      }

      UUID offlineUUID = getOfflineUUID(username);

      UUID realUUID;
      boolean cached;
      try {
         KeyedLock<String, UUID> lock = MojangRequestQueue.USERNAME_REQUESTS.lock(username, USERNAME_TO_ONLINE::get);

         try {
            realUUID = lock.getOrRetryValue();
            cached = realUUID != null;
            if (realUUID == null) {
               realUUID = MojangAPI.requestUsernameToUUID(username);
               if (realUUID == null) {
                  ProfileLogger.debug("Caching null for {} ({}) because it doesn't exist.", username, offlineUUID);
                  realUUID = IDENTITY_UUID;
               } else {
                  ONLINE_TO_OFFLINE.put(realUUID, offlineUUID);
               }

               OFFLINE_TO_ONLINE.put(offlineUUID, realUUID);
               USERNAME_TO_ONLINE.put(username, realUUID);
            }
         } catch (Throwable var8) {
            if (lock != null) {
               try {
                  lock.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (lock != null) {
            lock.close();
         }
      } catch (Exception e) {
         throw new IllegalStateException("Error while getting real UUID of player: " + username, e);
      }

      if (realUUID == IDENTITY_UUID) {
         ProfileLogger.debug("Providing null UUID for {} because it doesn't exist.", username);
         realUUID = null;
      } else {
         ProfileLogger.debug((cached ? "Cached " : "") + "Real UUID for {} ({}) is {}", username, offlineUUID, realUUID);
      }

      return realUUID;
   }

   @Nullable
   public static UUID getRealUUIDOfPlayer(@NotNull String username, @NotNull UUID uuid) {
      Objects.requireNonNull(uuid);
      if (Strings.isNullOrEmpty(username)) {
         throw new IllegalArgumentException("Username is null or empty: " + username);
      }

      if (isOnlineMode()) {
         return uuid;
      }

      UUID realUUID;
      boolean cached;
      try {
         KeyedLock<String, UUID> lock = MojangRequestQueue.USERNAME_REQUESTS.lock(username, () -> OFFLINE_TO_ONLINE.get(uuid));

         try {
            realUUID = lock.getOrRetryValue();
            cached = realUUID != null;
            if (realUUID == null) {
               realUUID = MojangAPI.requestUsernameToUUID(username);
               if (realUUID == null) {
                  ProfileLogger.debug("Caching null for {} ({}) because it doesn't exist.", username, uuid);
                  realUUID = IDENTITY_UUID;
               } else {
                  ONLINE_TO_OFFLINE.put(realUUID, uuid);
               }

               OFFLINE_TO_ONLINE.put(uuid, realUUID);
               USERNAME_TO_ONLINE.put(username, realUUID);
            }
         } catch (Throwable var8) {
            if (lock != null) {
               try {
                  lock.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (lock != null) {
            lock.close();
         }
      } catch (IOException e) {
         throw new IllegalStateException("Error while getting real UUID of player: " + username + " (" + uuid + ')', e);
      }

      if (realUUID == IDENTITY_UUID) {
         ProfileLogger.debug("Providing null UUID for {} ({}) because it doesn't exist.", username, uuid);
         realUUID = null;
      } else {
         ProfileLogger.debug((cached ? "Cached " : "") + "Real UUID for {} ({}) is {}", username, uuid, realUUID);
      }

      UUID offlineUUID = getOfflineUUID(username);
      if (!uuid.equals(offlineUUID) && !uuid.equals(realUUID)) {
         throw new IllegalArgumentException(
            "The provided UUID (" + uuid + ") for '" + username + "' doesn't match the offline UUID (" + offlineUUID + ") or the real UUID (" + realUUID + ')'
         );
      } else {
         return realUUID;
      }
   }
}
