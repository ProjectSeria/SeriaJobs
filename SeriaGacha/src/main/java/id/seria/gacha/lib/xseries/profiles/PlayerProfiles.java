package id.seria.gacha.lib.xseries.profiles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import id.seria.gacha.lib.xseries.profiles.gameprofile.MojangGameProfile;
import id.seria.gacha.lib.xseries.profiles.gameprofile.XGameProfile;
import id.seria.gacha.lib.xseries.profiles.gameprofile.property.XProperty;
import id.seria.gacha.lib.xseries.profiles.objects.transformer.ProfileTransformer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class PlayerProfiles {
   public static final String XSERIES_SIG = "XSeries";
   private static final Property XSERIES_GAMEPROFILE_SIGNATURE = XProperty.create("XSeries", "13.6.0").object();
   private static final String TEXTURES_PROPERTY = "textures";
   public static final GameProfile NIL = createGameProfile(PlayerUUIDs.IDENTITY_UUID, "XSeries").object();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   public static final String TEXTURES_NBT_PROPERTY_PREFIX = "{\"textures\":{\"SKIN\":{\"url\":\"";
   public static final String TEXTURES_BASE_URL = "http://textures.minecraft.net/texture/";

   public PlayerProfiles() {
   }

   public static Optional<Property> getTextureProperty(MojangGameProfile profile) {
      return Optional.ofNullable(profile.getProperty("textures"));
   }

   @Nullable
   public static String getTextureValue(@NotNull MojangGameProfile profile) {
      Objects.requireNonNull(profile, "Game profile cannot be null");
      return getTextureProperty(profile).map(PlayerProfiles::getPropertyValue).orElse(null);
   }

   @Nullable
   public static String getOriginalValue(@Nullable MojangGameProfile profile) {
      if (profile == null) {
         return null;
      }

      String original = ProfileTransformer.IncludeOriginalValue.getOriginalValue(profile);
      return original != null ? original : getTextureValue(profile);
   }

   @NotNull
   public static String getPropertyValue(@NotNull Property property) {
      if (ProfilesCore.NULLABILITY_RECORD_UPDATE) {
         return property.value();
      }

      try {
         return (String)ProfilesCore.Property_getValue.invoke((Property)property);
      } catch (Throwable throwable) {
         throw new IllegalArgumentException("Unable to get a property value: " + property, throwable);
      }
   }

   public static boolean hasTextures(MojangGameProfile profile) {
      return getTextureProperty(profile).isPresent();
   }

   @NotNull
   public static MojangGameProfile profileFromHashAndBase64(String hash, String base64) {
      UUID uuid = UUID.nameUUIDFromBytes(hash.getBytes(StandardCharsets.UTF_8));
      MojangGameProfile profile = createNamelessGameProfile(uuid);
      return setTexturesProperty(profile, base64);
   }

   public static void removeTimestamp(MojangGameProfile profile) {
      JsonObject jsonObject = Optional.ofNullable(getTextureValue(profile))
         .map(PlayerProfiles::decodeBase64)
         .map(decoded -> new JsonParser().parse(decoded).getAsJsonObject())
         .orElse(null);
      if (jsonObject != null && jsonObject.has("timestamp")) {
         jsonObject.remove("timestamp");
         setTexturesProperty(profile, encodeBase64(GSON.toJson(jsonObject)));
      }
   }

   @Nullable
   public static MojangGameProfile fromResolvableProfile(@Nullable Object profile) throws Throwable {
      if (profile == null) {
         return null;
      }

      if (!(profile instanceof GameProfile) && ProfilesCore.ResolvableProfile_gameProfile != null) {
         profile = (Object)ProfilesCore.ResolvableProfile_gameProfile.invoke((Object)profile);
      }

      return XGameProfile.of((GameProfile)profile);
   }

   @Nullable
   public static Object toResolvableProfile(@Nullable MojangGameProfile profile) throws Throwable {
      if (profile == null) {
         return null;
      } else {
         return ProfilesCore.ResolvableProfile$bukkitSupports
            ? (Object)ProfilesCore.ResolvableProfile$constructor.invoke((GameProfile)profile.object())
            : profile.object();
      }
   }

   public static GameProfile sanitizeProfile(GameProfile gameProfile) {
      if (PlayerUUIDs.isOnlineMode()) {
         return gameProfile;
      }

      MojangGameProfile profile = XGameProfile.of(gameProfile);
      UUID offlineId = PlayerUUIDs.getOfflineUUID(profile.name());
      PlayerUUIDs.ONLINE_TO_OFFLINE.put(profile.id(), offlineId);
      MojangGameProfile clone = createGameProfile(offlineId, profile.name(), profile.properties());
      return clone.object();
   }

   @Contract(pure = true)
   public static MojangGameProfile setTexturesProperty(MojangGameProfile profile, String texture) {
      return profile.copy(x -> x.setProperty("textures", texture));
   }

   @Contract(pure = true)
   public static String encodeBase64(String str) {
      return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
   }

   @Nullable
   @Contract(pure = true)
   public static String decodeBase64(String base64) {
      Objects.requireNonNull(base64, "Cannot decode null string");

      try {
         byte[] bytes = Base64.getDecoder().decode(base64);
         return new String(bytes, StandardCharsets.UTF_8);
      } catch (IllegalArgumentException exception) {
         return null;
      }
   }

   @Contract(pure = true)
   public static MojangGameProfile createGameProfile(UUID uuid, String username) {
      return signXSeries(XGameProfile.create(uuid, username));
   }

   @Contract(pure = true)
   public static MojangGameProfile createGameProfile(UUID uuid, String username, PropertyMap properties) {
      return signXSeries(XGameProfile.create(uuid, username, properties));
   }

   @Contract(pure = true)
   public static MojangGameProfile signXSeries(MojangGameProfile profile) {
      return profile.copy(x -> x.setProperty("XSeries", XSERIES_GAMEPROFILE_SIGNATURE));
   }

   @Contract(pure = true)
   public static MojangGameProfile createNamelessGameProfile(UUID id) {
      return createGameProfile(id, "XSeries");
   }
}
