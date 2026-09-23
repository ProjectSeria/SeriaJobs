package id.seria.gacha.lib.xseries.profiles.mojang;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
final class PlayerProfile {
   public final UUID realUUID;
   public final GameProfile requestedGameProfile;
   public final GameProfile fetchedGameProfile;
   public final List<String> profileActions;

   PlayerProfile(UUID realUUID, GameProfile requestedGameProfile, GameProfile fetchedGameProfile, List<String> profileActions) {
      this.realUUID = realUUID;
      this.requestedGameProfile = requestedGameProfile;
      this.fetchedGameProfile = fetchedGameProfile;
      this.profileActions = profileActions;
   }

   boolean exists() {
      return this.fetchedGameProfile != null;
   }
}
