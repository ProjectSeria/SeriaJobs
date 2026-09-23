package id.seria.gacha.extensions.logger.configurations;

import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import java.awt.Color;
import lombok.Generated;

public class LoggerConfiguration extends Configuration {
   @ConfigField(path = "log-format.open-crate")
   private final String openCrateLogFormat = "%player% opened a crate %crate_name% (%crate_id%) at %location%.";
   @ConfigField(path = "log-format.won-reward")
   private final String wonRewardLogFormat = "%player% got the reward %reward_name% (%reward_id%) on crate %crate_name% (%crate_id%).";
   @ConfigField(path = "discord-webhook.enabled")
   private final boolean discordWebhookEnabled = false;
   @ConfigField(path = "discord-webhook.url")
   private final String discordWebhookUrl = "";
   @ConfigField(path = "discord-webhook.avatar-url")
   private final String discordWebhookAvatarUrl = "";
   @ConfigField(path = "discord-webhook.username")
   private final String discordWebhookUsername = "Phoenix Crates - Logger";
   @ConfigField(path = "discord-webhook.embed")
   private final LoggerConfiguration.DiscordEmbed discordWebhookEmbed = new LoggerConfiguration.DiscordEmbed();

   public LoggerConfiguration() {
   }

   public LoggerConfiguration clone() {
      return new LoggerConfiguration();
   }

   @Generated
   public String getOpenCrateLogFormat() {
      return this.openCrateLogFormat;
   }

   @Generated
   public String getWonRewardLogFormat() {
      return this.wonRewardLogFormat;
   }

   @Generated
   public boolean isDiscordWebhookEnabled() {
      return this.discordWebhookEnabled;
   }

   @Generated
   public String getDiscordWebhookUrl() {
      return this.discordWebhookUrl;
   }

   @Generated
   public String getDiscordWebhookAvatarUrl() {
      return this.discordWebhookAvatarUrl;
   }

   @Generated
   public String getDiscordWebhookUsername() {
      return this.discordWebhookUsername;
   }

   @Generated
   public LoggerConfiguration.DiscordEmbed getDiscordWebhookEmbed() {
      return this.discordWebhookEmbed;
   }

   public static class DiscordEmbed {
      @ConfigField
      private String title = "Action Log";
      @ConfigField
      private String description = "%message%";
      @ConfigField
      private Color color = new Color(74, 32, 255);

      @Generated
      public String getTitle() {
         return this.title;
      }

      @Generated
      public String getDescription() {
         return this.description;
      }

      @Generated
      public Color getColor() {
         return this.color;
      }

      @Generated
      public DiscordEmbed() {
      }

      @Generated
      public DiscordEmbed(String title, String description, Color color) {
         this.title = title;
         this.description = description;
         this.color = color;
      }
   }
}
