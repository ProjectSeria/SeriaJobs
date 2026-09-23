package id.seria.gacha.extensions.logger;

import id.seria.gacha.api.crate.CrateInstance;
import id.seria.gacha.api.crate.events.CrateOpenEvent;
import id.seria.gacha.api.crate.events.CrateRewardPlayerEvent;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.api.sdk.plugin.extensions.Extension;
import id.seria.gacha.extensions.logger.configurations.LoggerConfiguration;
import id.seria.gacha.extensions.logger.internal.DiscordWebhook;
import id.seria.gacha.extensions.logger.internal.LogHandler;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.configuration.workers.AsyncQueueConfigurationWorker;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.Validate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LoggerExtension extends Extension {
   private final ServerPlugin plugin;
   private LoggerConfiguration configuration;
   private AsyncQueueConfigurationWorker configurationWorker;
   private LogHandler logHandler;
   private Listener registeredListener;

   public LoggerExtension(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public void init() {
      this.configuration = new LoggerConfiguration();
      this.logHandler = new LogHandler();
      this.configurationWorker = new AsyncQueueConfigurationWorker(this.plugin, this.configuration, "logger.yml");
      this.registeredListener = new Listener() {
         @EventHandler
         public void onCrateOpen(CrateOpenEvent event) {
            if (!LoggerExtension.this.configuration.getOpenCrateLogFormat().isEmpty()) {
               CrateInstance crate = event.getCrate();
               String serializedLocation = String.format(
                  "%s, %d, %d, %d",
                  crate.getLocation().getWorld().getName(),
                  (int)crate.getLocation().getX(),
                  (int)crate.getLocation().getY(),
                  (int)crate.getLocation().getZ()
               );
               String message = LoggerExtension.this.configuration
                  .getOpenCrateLogFormat()
                  .replace("%player%", event.getPlayer().getName())
                  .replace(
                     "%crate_name%",
                     ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', StringUtil.trimToEmpty(crate.getType().getDisplayName())))
                  )
                  .replace("%crate_id%", crate.getType().getIdentifier())
                  .replace("%location%", serializedLocation);
               LoggerExtension.this.logHandler.log(Level.INFO, message);
               if (LoggerExtension.this.configuration.isDiscordWebhookEnabled()) {
                  LoggerExtension.this.notifyDiscordByWebhook(message);
               }
            }
         }

         @EventHandler
         public void onPlayerReceiveReward(CrateRewardPlayerEvent event) {
            if (!LoggerExtension.this.configuration.getWonRewardLogFormat().isEmpty()) {
               CrateInstance crate = event.getCrate();
               BaseReward reward = event.getReward();
               String message = LoggerExtension.this.configuration
                  .getWonRewardLogFormat()
                  .replace("%player%", event.getPlayer().getName())
                  .replace(
                     "%reward_name%",
                     ChatColor.stripColor(
                        ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', ItemBuilder.of(reward.getDisplayItem().build()).getDisplayName()))
                     )
                  )
                  .replace("%reward_id%", reward.getIdentifier())
                  .replace("%reward_amount%", String.valueOf(reward.getDisplayItem().getAmount()))
                  .replace(
                     "%crate_name%",
                     ChatColor.stripColor(
                        ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', StringUtil.trimToEmpty(crate.getType().getDisplayName())))
                     )
                  )
                  .replace("%crate_id%", crate.getType().getIdentifier());
               LoggerExtension.this.logHandler.log(Level.INFO, message);
               if (LoggerExtension.this.configuration.isDiscordWebhookEnabled()) {
                  LoggerExtension.this.notifyDiscordByWebhook(message);
               }
            }
         }
      };
   }

   @Override
   public void load() throws Exception {
      this.configurationWorker.validate();
      this.logHandler.init(new File(this.plugin.getDataFolder(), "logs"));
      this.plugin.getEventBus().registerListener(this.registeredListener);
   }

   @Override
   public void unload() {
      if (this.logHandler != null) {
         this.logHandler.close();
      }

      if (this.configurationWorker != null) {
         this.configurationWorker.close();
      }

      if (this.registeredListener != null) {
         this.plugin.getEventBus().unregisterListener(this.registeredListener);
      }
   }

   @Override
   public void reload() throws Exception {
   }

   private void notifyDiscordByWebhook(String message) {
      if (!this.configuration.getDiscordWebhookUrl().isEmpty()) {
         try {
            Validate.isTrue(isValidURL(this.configuration.getDiscordWebhookUrl()), "Discord Webhook Url is invalid.");
            boolean avatarUrlAbsent = this.configuration.getDiscordWebhookAvatarUrl().isEmpty();
            Validate.isTrue(avatarUrlAbsent || isValidURL(this.configuration.getDiscordWebhookAvatarUrl()), "Discord Webhook Avatar Url is invalid.");
            DiscordWebhook webhook = new DiscordWebhook(this.configuration.getDiscordWebhookUrl());
            webhook.setUsername(this.configuration.getDiscordWebhookUsername());
            if (!avatarUrlAbsent) {
               webhook.setAvatarUrl(this.configuration.getDiscordWebhookAvatarUrl());
            }

            DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
            embed.setTitle(this.configuration.getDiscordWebhookEmbed().getTitle());
            embed.setDescription(this.configuration.getDiscordWebhookEmbed().getDescription().replace("%message%", message));
            embed.setColor(this.configuration.getDiscordWebhookEmbed().getColor());
            webhook.addEmbed(embed);
            Utilities.getAsyncService().execute(() -> {
               try {
                  webhook.execute();
               } catch (IOException e) {
                  this.plugin.getLogger().error("Error executing discord webhook", e);
               }
            });
         } catch (Exception e) {
            this.plugin.getLogger().error("Error saving discord webhook", e);
         }
      }
   }

   public static boolean isValidURL(String urlString) {
      try {
         URL url = new URL(urlString);
         url.toURI();
         return true;
      } catch (Exception e) {
         return false;
      }
   }
}
