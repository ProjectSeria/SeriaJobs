package id.seria.gacha.api.sdk.internal;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public interface Actor {
   Object raw();

   boolean isPlayer();

   @NotNull
   UUID getUniqueId();

   boolean hasPermission(String var1);

   void sendMessage(String... var1);
}
