package id.seria.gacha.lib.common.utils.configuration.logger;

import id.seria.gacha.lib.common.utils.utility.StringUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.md_5.bungee.api.ChatColor;

public abstract class ConfigYamlLogger {
   private final List<ConfigYamlLogger.YamlLog> logs = new ArrayList<>();
   private int flushedCount;
   private boolean silent;
   private String lastPathFlushed;
   private int errorCount;

   public ConfigYamlLogger() {
   }

   private void push(ConfigYamlLogger.LogLevel level, String path, String message) {
      synchronized (this.logs) {
         this.logs.add(new ConfigYamlLogger.YamlLog(level, path, message));
      }
   }

   public void pushAndFlush(ConfigYamlLogger.LogLevel level, String path, String message) {
      this.push(level, path, message);
      this.flush();
   }

   private void info(String path, String message) {
      synchronized (this.logs) {
         this.logs.add(new ConfigYamlLogger.YamlLog(ConfigYamlLogger.LogLevel.INFO, path, message));
      }
   }

   public void infoAndFlush(String path, String message) {
      this.info(path, message);
      this.flush();
   }

   private void warning(String path, String message) {
      synchronized (this.logs) {
         this.logs.add(new ConfigYamlLogger.YamlLog(ConfigYamlLogger.LogLevel.WARNING, path, message));
      }
   }

   public void warningAndFlush(String path, String message) {
      this.warning(path, message);
      this.flush();
   }

   private void error(String path, String message) {
      synchronized (this.logs) {
         this.logs.add(new ConfigYamlLogger.YamlLog(ConfigYamlLogger.LogLevel.ERROR, path, message));
      }

      this.errorCount++;
   }

   public void errorAndFlush(String path, String message) {
      this.error(path, message);
      this.flush();
   }

   private void pop() {
      synchronized (this.logs) {
         this.logs.remove(this.logs.size() - 1);
      }
   }

   public void flush() {
      synchronized (this.logs) {
         for (ConfigYamlLogger.YamlLog log : this.logs) {
            this.log(log);
         }

         this.logs.clear();
      }
   }

   public void log(ConfigYamlLogger.YamlLog log) {
      this.flushedCount++;
      if (!this.silent) {
         if (log.getPath() != null && !log.getPath().equalsIgnoreCase(this.lastPathFlushed) && !log.getPath().isEmpty()) {
            this.flushRaw(ChatColor.AQUA + "-> Path: " + ChatColor.DARK_AQUA + "[" + log.getPath() + "]");
         }

         String displayName = StringUtil.capitalize(log.getLevel().name().toLowerCase());
         this.flushRaw(log.getLevel().getDefaultColor() + "--> " + displayName + ": " + this.formatMesssage(log.getLevel(), log.getMessage()));
         this.lastPathFlushed = log.getPath();
      }
   }

   private String formatMesssage(ConfigYamlLogger.LogLevel level, String message) {
      StringBuilder builder = new StringBuilder();
      boolean openedMarker = false;

      for (char character : message.toCharArray()) {
         if (character == '"') {
            if (openedMarker) {
               builder.append(character).append(level.getDefaultColor());
            } else {
               builder.append(level.getHighlightColor()).append(character);
            }

            openedMarker = !openedMarker;
         } else {
            builder.append(character);
         }
      }

      return builder.toString();
   }

   public int countQueue() {
      synchronized (this.logs) {
         return this.logs.size();
      }
   }

   public int countFlushed() {
      synchronized (this.logs) {
         return this.flushedCount;
      }
   }

   public void clear() {
      synchronized (this.logs) {
         this.logs.clear();
      }
   }

   public void reset() {
      synchronized (this.logs) {
         this.clear();
         this.flushedCount = 0;
         this.errorCount = 0;
      }
   }

   public abstract void flushRaw(String var1);

   @Generated
   public boolean isSilent() {
      return this.silent;
   }

   @Generated
   public void setSilent(boolean silent) {
      this.silent = silent;
   }

   @Generated
   public int getErrorCount() {
      return this.errorCount;
   }

   public enum LogLevel {
      INFO(ChatColor.GREEN, ChatColor.DARK_GREEN),
      WARNING(ChatColor.YELLOW, ChatColor.GOLD),
      ERROR(ChatColor.RED, ChatColor.DARK_RED);

      private final ChatColor defaultColor;
      private final ChatColor highlightColor;

      @Generated
      public ChatColor getDefaultColor() {
         return this.defaultColor;
      }

      @Generated
      public ChatColor getHighlightColor() {
         return this.highlightColor;
      }

      @Generated
      LogLevel(final ChatColor defaultColor, final ChatColor highlightColor) {
         this.defaultColor = defaultColor;
         this.highlightColor = highlightColor;
      }
   }

   private static class YamlLog {
      private ConfigYamlLogger.LogLevel level;
      private String path;
      private String message;

      @Generated
      public void setLevel(ConfigYamlLogger.LogLevel level) {
         this.level = level;
      }

      @Generated
      public void setPath(String path) {
         this.path = path;
      }

      @Generated
      public void setMessage(String message) {
         this.message = message;
      }

      @Generated
      public ConfigYamlLogger.LogLevel getLevel() {
         return this.level;
      }

      @Generated
      public String getPath() {
         return this.path;
      }

      @Generated
      public String getMessage() {
         return this.message;
      }

      @Generated
      public YamlLog(ConfigYamlLogger.LogLevel level, String path, String message) {
         this.level = level;
         this.path = path;
         this.message = message;
      }
   }
}
