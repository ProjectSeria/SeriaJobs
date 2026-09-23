package id.seria.gacha.lib.common.utils.configuration.logger;

import id.seria.gacha.lib.common.utils.utility.StringUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

public class BasicYamlLogger {
   private final List<BasicYamlLogger.YamlLog> logs = new ArrayList<>();
   private int flushedCount;
   private boolean silent;
   private String lastPathFlushed;

   public BasicYamlLogger() {
   }

   private void push(BasicYamlLogger.LogLevel level, String path, String message) {
      synchronized (this.logs) {
         this.logs.add(new BasicYamlLogger.YamlLog(level, path, message));
      }
   }

   public void pushAndFlush(BasicYamlLogger.LogLevel level, String path, String message) {
      this.push(level, path, message);
      this.flush();
   }

   private void info(String path, String message) {
      synchronized (this.logs) {
         this.logs.add(new BasicYamlLogger.YamlLog(BasicYamlLogger.LogLevel.INFO, path, message));
      }
   }

   public void infoAndFlush(String path, String message) {
      this.info(path, message);
      this.flush();
   }

   private void warning(String path, String message) {
      synchronized (this.logs) {
         this.logs.add(new BasicYamlLogger.YamlLog(BasicYamlLogger.LogLevel.WARNING, path, message));
      }
   }

   public void warningAndFlush(String path, String message) {
      this.warning(path, message);
      this.flush();
   }

   private void error(String path, String message) {
      synchronized (this.logs) {
         this.logs.add(new BasicYamlLogger.YamlLog(BasicYamlLogger.LogLevel.ERROR, path, message));
      }
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

   private void flush() {
      synchronized (this.logs) {
         for (BasicYamlLogger.YamlLog log : this.logs) {
            this.log(log);
         }

         this.logs.clear();
      }
   }

   public void log(BasicYamlLogger.YamlLog log) {
      this.flushedCount++;
      if (!this.silent) {
         if (!log.getPath().equalsIgnoreCase(this.lastPathFlushed) && log.getPath().length() > 0) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "-> Path: " + ChatColor.DARK_AQUA + "[" + log.getPath() + "]");
         }

         String displayName = StringUtil.capitalize(log.getLevel().name().toLowerCase());
         Bukkit.getConsoleSender()
            .sendMessage(log.getLevel().getDefaultColor() + "--> " + displayName + ": " + this.formatMesssage(log.getLevel(), log.getMessage()));
         this.lastPathFlushed = log.getPath();
      }
   }

   private String formatMesssage(BasicYamlLogger.LogLevel level, String message) {
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
      }
   }

   @Generated
   public boolean isSilent() {
      return this.silent;
   }

   @Generated
   public void setSilent(boolean silent) {
      this.silent = silent;
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
      private BasicYamlLogger.LogLevel level;
      private String path;
      private String message;

      @Generated
      public void setLevel(BasicYamlLogger.LogLevel level) {
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
      public BasicYamlLogger.LogLevel getLevel() {
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
      public YamlLog(BasicYamlLogger.LogLevel level, String path, String message) {
         this.level = level;
         this.path = path;
         this.message = message;
      }
   }
}
