/*
 * Decompiled with CFR 0.152.
 */
package id.seria.gacha.lib.xseries.profiles.objects;

import id.seria.gacha.lib.xseries.profiles.PlayerProfiles;
import id.seria.gacha.lib.xseries.profiles.exceptions.InvalidProfileException;
import id.seria.gacha.lib.xseries.profiles.gameprofile.MojangGameProfile;
import id.seria.gacha.lib.xseries.profiles.objects.Profileable;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ProfileInputType {
    TEXTURE_HASH(Pattern.compile("[0-9a-z]{55,70}")){

        @Override
        public MojangGameProfile getProfile(String textureHash) {
            String base64 = PlayerProfiles.encodeBase64("{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + textureHash + "\"}}}");
            return PlayerProfiles.profileFromHashAndBase64(textureHash, base64);
        }
    }
    ,
    TEXTURE_URL(Pattern.compile("(?:https?://)?(?:textures\\.)?minecraft\\.net/texture/(?<hash>" + ProfileInputType.TEXTURE_HASH.pattern + ')', 2)){

        @Override
        public MojangGameProfile getProfile(String textureUrl) {
            String hash = ProfileInputType.extractTextureHash(textureUrl);
            return TEXTURE_HASH.getProfile(hash);
        }
    }
    ,
    BASE64(Pattern.compile("[-A-Za-z0-9+/]{100,}={0,3}")){

        @Override
        public MojangGameProfile getProfile(String base64) {
            String decodedBase64 = PlayerProfiles.decodeBase64(base64);
            if (decodedBase64 == null) {
                throw new InvalidProfileException(base64, "Not a base64 string: " + base64);
            }
            String textureHash = ProfileInputType.extractTextureHash(decodedBase64);
            if (textureHash == null) {
                throw new InvalidProfileException(decodedBase64, "Can't extract texture hash from base64: " + decodedBase64);
            }
            return PlayerProfiles.profileFromHashAndBase64(textureHash, base64);
        }
    }
    ,
    UUID(Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")){

        @Override
        public MojangGameProfile getProfile(String uuidString) {
            UUID uuid;
            try {
                uuid = java.util.UUID.fromString(uuidString);
            }
            catch (IllegalArgumentException ex) {
                throw new InvalidProfileException(uuidString, "Invalid UUID string: " + uuidString, ex);
            }
            return Profileable.of(uuid).getProfile();
        }
    }
    ,
    USERNAME(Pattern.compile("[A-Za-z0-9_]{1,16}")){

        @Override
        public MojangGameProfile getProfile(String username) {
            return Profileable.username(username).getProfile();
        }
    };

    @ApiStatus.Internal
    public final Pattern pattern;
    private static final ProfileInputType[] VALUES;

    private ProfileInputType(Pattern pattern) {
        this.pattern = pattern;
    }

    public abstract MojangGameProfile getProfile(String var1);

    @Nullable
    public static ProfileInputType typeOf(@NotNull String identifier) {
        Objects.requireNonNull(identifier, "Identifier cannot be null");
        return Arrays.stream(VALUES).filter(value -> value.pattern.matcher(identifier).matches()).findFirst().orElse(null);
    }

    @Nullable
    private static String extractTextureHash(String input) {
        Matcher matcher = ProfileInputType.TEXTURE_HASH.pattern.matcher(input);
        return matcher.find() ? matcher.group() : null;
    }

    static {
        VALUES = ProfileInputType.values();
    }
}

