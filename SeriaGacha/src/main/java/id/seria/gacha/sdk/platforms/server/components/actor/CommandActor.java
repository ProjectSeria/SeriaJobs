package id.seria.gacha.sdk.platforms.server.components.actor;

import id.seria.gacha.api.sdk.internal.Actor;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandActor implements Actor {
   private CommandSender sender;

   public CommandActor(CommandSender sender) {
      this.sender = sender;
   }

   public CommandSender raw() {
      return this.sender;
   }

   @Override
   public boolean isPlayer() {
      return this.sender instanceof Player;
   }

   @NotNull
   @Override
   public UUID getUniqueId() {
      return this.isPlayer() ? ((Player)this.sender).getUniqueId() : UUID.nameUUIDFromBytes(this.sender.getName().getBytes(StandardCharsets.UTF_8));
   }

   @Override
   public boolean hasPermission(String permission) {
      return this.sender.hasPermission(permission);
   }

   @Override
   public void sendMessage(String... message) {
      this.sender.sendMessage(message);
   }
}
