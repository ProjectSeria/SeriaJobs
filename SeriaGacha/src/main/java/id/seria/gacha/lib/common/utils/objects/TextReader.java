package id.seria.gacha.lib.common.utils.objects;

import com.google.common.collect.Maps;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.sdk.core.locales.providers.PluginTranslationsProvider;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TextReader {
   private static final Map<UUID, Consumer<String>> sessions = Maps.newConcurrentMap();

   public TextReader() {
   }

   public static void register(final ServerPlugin plugin) {
      plugin.getEventBus().registerListener(new Listener() {
         @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
         public void onPlayerChat(AsyncPlayerChatEvent event) {
            Player player = event.getPlayer();
            Consumer<String> consumer = TextReader.sessions.remove(player.getUniqueId());
            if (consumer != null) {
               boolean cancelled = event.getMessage().equalsIgnoreCase("cancel");
               if (cancelled) {
                  player.sendMessage("\u00a7eSession canceled.");
               }

               plugin.getScheduler().runSync(() -> {
                  Utilities.getNMSFactory().sendTitlePacket(Arrays.asList(player), "\u00a7r", "\u00a7r", 0, 1, 10);
                  consumer.accept(cancelled ? null : event.getMessage());
               });
               event.setCancelled(true);
            }
         }

         @EventHandler
         public void onPlayerQuit(PlayerQuitEvent event) {
            TextReader.sessions.remove(event.getPlayer().getUniqueId());
         }
      });
   }

   @Deprecated
   public static void askForInput(Player player, Consumer<String> consumer) {
      player.sendMessage("\u00a7aTo exit this session type \u00a7e\"cancel\"\u00a7a.");
      Utilities.getNMSFactory().sendTitlePacket(Arrays.asList(player), "\u00a7r", "\u00a7aWrite in the chat.", 10, 10000, 0);
      sessions.put(player.getUniqueId(), consumer);
   }

   public static void cancel(UUID uuid) {
      sessions.remove(uuid);
   }

   public static void askForInput(PluginTranslationsProvider translation, Player player, Consumer<String> consumer) {
      player.sendMessage(translation.translate(Translatable.key("readers.exit-session"))[0]);
      Utilities.getNMSFactory()
         .sendTitlePacket(Arrays.asList(player), "\u00a7r", translation.translate(Translatable.key("readers.write-chat"))[0], 10, 10000, 0);
      sessions.put(player.getUniqueId(), consumer);
   }
}
