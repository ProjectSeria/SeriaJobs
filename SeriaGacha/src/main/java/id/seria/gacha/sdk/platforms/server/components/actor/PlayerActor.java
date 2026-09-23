package id.seria.gacha.sdk.platforms.server.components.actor;

import id.seria.gacha.api.sdk.internal.Actor;
import java.util.UUID;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerActor implements Actor {
   private final Player player;

   public static PlayerActor from(Player player) {
      return new PlayerActor(player);
   }

   private PlayerActor(@NonNull Player player) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      this.player = player;
   }

   public Player raw() {
      return this.player;
   }

   @Override
   public boolean isPlayer() {
      return true;
   }

   @NotNull
   @Override
   public UUID getUniqueId() {
      return this.player.getUniqueId();
   }

   @Override
   public boolean hasPermission(String permission) {
      return this.player.hasPermission(permission);
   }

   @Override
   public void sendMessage(String... message) {
      this.player.sendMessage(message);
   }
}
