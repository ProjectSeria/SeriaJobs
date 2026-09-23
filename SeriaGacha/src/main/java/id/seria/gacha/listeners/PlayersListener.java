package id.seria.gacha.listeners;

import id.seria.gacha.Crates;
import id.seria.gacha.commands.subcommands.SetLanguageCommand;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.internal.language.Language;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.managers.PlayersManager;
import id.seria.gacha.managers.crates.Crate;
import java.util.Collections;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayersListener implements Listener {
   private final PlayersManager manager;

   public PlayersListener(PlayersManager manager) {
      this.manager = manager;
   }

   @EventHandler(priority = EventPriority.LOWEST)
   protected void onPlayerJoin1(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      this.manager.fetchPlayerDataAsync(player);
   }

   @EventHandler(priority = EventPriority.LOWEST)
   protected void onRemindPlayer(PlayerJoinEvent event) {
      if (Crates.getPlugin().getEdition() == Edition.LITE) {
         Player player = event.getPlayer();
         if (this.manager.getPlugin().getConfiguration().isJoinReminder()) {
            this.manager
               .getPlugin()
               .getScheduler()
               .runSync(
                  player,
                  () -> {
                     TextComponent welcome = new TextComponent("Phoenix Crates Lite:\n\n");
                     TextComponent liteInfo = new TextComponent(ChatColor.GRAY + "You're currently using the Lite version of our plugin. ");
                     TextComponent premiumLink = new TextComponent(
                        "For enhanced features and stunning animations, don't miss out on our "
                           + ChatColor.YELLOW
                           + "premium version"
                           + ChatColor.GRAY
                           + "!\n\n"
                     );
                     premiumLink.setColor(ChatColor.GRAY);
                     premiumLink.setHoverEvent(
                        new HoverEvent(Action.SHOW_TEXT, new TextComponent[]{new TextComponent("Click to visit our premium version website")})
                     );
                     premiumLink.setClickEvent(
                        new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL, "https://www.phoenixplugins.com/products/phoenix-crates")
                     );
                     TextComponent discordLink = new TextComponent("Join our friendly Discord community for support, giveaways, and more.");
                     discordLink.setColor(ChatColor.DARK_PURPLE);
                     discordLink.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new TextComponent[]{new TextComponent("Click to join our Discord")}));
                     discordLink.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL, "https://phoenixplugins.com/media/discord"));
                     TextComponent fullMessage = new TextComponent(welcome);
                     fullMessage.addExtra(liteInfo);
                     fullMessage.addExtra(premiumLink);
                     fullMessage.addExtra(discordLink);
                     fullMessage.addExtra(ChatColor.DARK_PURPLE + " Enjoy the plugin!");
                     player.sendMessage(" ");
                     player.spigot().sendMessage(fullMessage);
                     player.sendMessage(ChatColor.DARK_GRAY + "Change this message at config.yml `join-reminder`)");
                     player.sendMessage(" ");
                  },
                  50L
               );
         }
      }
   }

   @EventHandler
   protected void onPlayerJoin2(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      if (Crates.getConfiguration().isNotifyIfNewVersion() && this.manager.getPlugin().needUpdate() && player.hasPermission("phoenixcrates.admin")) {
         Crates.getScheduler()
            .runSync(
               player,
               () -> {
                  Translations.send(player, "");
                  Translations.send(player, "\u00a7aNew version available for Phoenix Crates!");
                  Translations.send(
                     player,
                     "\u00a77Your current version is "
                        + this.manager.getPlugin().getCurrentVersion()
                        + ", you can upgrade it to version "
                        + this.manager.getPlugin().getAvailableVersion()
                  );
                  Translations.send(player, "\u00a77-> Update at: https://www.phoenixplugins.com/products/phoenix-crates");
                  Translations.send(player, "");
               },
               20L
            );
      }
   }

   @EventHandler
   protected void onPlayerJoin3(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      if (Crates.getConfiguration().getLanguage() == Language.UNDEFINED && player.hasPermission("phoenixcrates.admin")) {
         Language.fetchLanguageByHostAddress()
            .whenComplete(
               (language, throwable) -> {
                  if (SetLanguageCommand.Languages.isAvailable(language)) {
                     Crates.getScheduler()
                        .runSync(
                           player,
                           () -> {
                              player.sendMessage(" ");
                              player.sendMessage("\u00a7eNo default language defined for Phoenix Crates.");
                              player.spigot()
                                 .sendMessage(
                                    new ComponentBuilder("\u00a7aLuckily we found one here that suits your profile.")
                                       .event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("\u00a7dClick to change language!").create()))
                                       .event(
                                          new ClickEvent(
                                             net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND,
                                             "/phoenixcrates setLanguage " + language.name().toLowerCase()
                                          )
                                       )
                                       .create()
                                 );
                              player.spigot()
                                 .sendMessage(
                                    new ComponentBuilder("\u00a7aClick here to define your language to \u00a72" + language.getDisplayName() + "\u00a7a.")
                                       .event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("\u00a7dClick to change language!").create()))
                                       .event(
                                          new ClickEvent(
                                             net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND,
                                             "/phoenixcrates setLanguage " + language.name().toLowerCase()
                                          )
                                       )
                                       .create()
                                 );
                              player.sendMessage(" ");
                           },
                           20L
                        );
                  }
               }
            );
      }
   }

   @EventHandler
   protected void onCrateVisibilityRefresh(PlayerJoinEvent event) {
      Player player = event.getPlayer();

      for (Crate crate : Crates.getCratesManager().getCrates().values()) {
         this.manager.getPlugin().getScheduler().runSync(crate.getLocation(), () -> {
            if (player.isOnline()) {
               try {
                  crate.getDisplayComponent().show(Collections.singletonList(player));
               } catch (Exception e) {
                  Crates.getLogger().error("Could not show crate display at " + crate.getLocation() + " to " + player.getName(), e);
               }
            }
         }, 20L);
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   protected void onPlayerQuit(PlayerQuitEvent event) {
      this.manager.disposeCachedPlayerData(event.getPlayer());
   }
}
