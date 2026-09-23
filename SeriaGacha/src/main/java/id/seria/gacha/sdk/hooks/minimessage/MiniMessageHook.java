package id.seria.gacha.sdk.hooks.minimessage;

import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import id.seria.gacha.sdk.platforms.server.internal.RichTextConverter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MiniMessageHook extends ServerHook {
   protected MiniMessageHook() {
   }

   @Override
   public void init() {
   }

   @Override
   public void load() {
   }

   public void sendRichMessage(CommandSender player, String message) {
      player.sendRichMessage(message);
   }

   public void sendPlayerListHeaderFooter(Player player, String header, String footer) {
      player.sendPlayerListHeaderAndFooter(RichTextConverter.deserialize(header), RichTextConverter.deserialize(footer));
   }

   public void setJoinMessage(PlayerJoinEvent event, String message) {
      event.joinMessage(RichTextConverter.deserialize(message));
   }

   public void setQuitMessage(PlayerQuitEvent event, String message) {
      event.quitMessage(RichTextConverter.deserialize(message));
   }

   @Override
   public void unload() {
   }

   @Override
   public void reload() {
      this.unload();
      this.load();
   }
}
