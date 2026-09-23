package id.seria.gacha.lib.xseries.profiles.gameprofile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import java.util.UUID;
import javax.annotation.Nullable;

public class XGameProfile {
   private static final boolean USE_RECORDS = XReflection.isRecord(GameProfile.class);

   private XGameProfile() {
   }

   @Nullable
   public static MojangGameProfile of(GameProfile gameProfile) {
      if (gameProfile == null) {
         return null;
      } else {
         return USE_RECORDS ? new NewGameProfile(gameProfile) : new OldGameProfile(gameProfile);
      }
   }

   public static MojangGameProfile create(UUID id, String name, PropertyMap properties) {
      GameProfile gameProfile = new GameProfile(id, name);
      MojangGameProfile converted = of(gameProfile);
      if (properties != null) {
         converted.properties().putAll(properties);
      }
      return converted;
   }

   public static MojangGameProfile create(UUID id, String name) {
      return of(new GameProfile(id, name));
   }
}
