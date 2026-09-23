package id.seria.gacha.lib.xseries.profiles.lock;

import java.util.UUID;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class MojangRequestQueue {
   public static final KeyedLockMap<String> USERNAME_REQUESTS = new KeyedLockMap<>();
   public static final KeyedLockMap<UUID> UUID_REQUESTS = new KeyedLockMap<>();

   public MojangRequestQueue() {
   }
}
