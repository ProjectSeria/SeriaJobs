package id.seria.gacha.lib.common.utils.configuration.logger;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

public class BasicLogger {
   private final List<BasicLogger.YamlLog> logs = new ArrayList<>();
   private int flushedCount;
   private boolean silent;

   public BasicLogger() {
   }

   private void push(BasicLogger.LogLevel level, String message) {
      synchronized (this.logs) {
         this.logs.add(new BasicLogger.YamlLog(level, message));
      }
   }

   public void pushAndFlush(BasicLogger.LogLevel level, String message) {
      this.push(level, message);
      this.flush();
   }

   private void info(String message) {
      synchronized (this.logs) {
         this.logs.add(new BasicLogger.YamlLog(BasicLogger.LogLevel.INFO, message));
      }
   }

   public void infoAndFlush(String message) {
      this.info(message);
      this.flush();
   }

   private void warning(String message) {
      synchronized (this.logs) {
         this.logs.add(new BasicLogger.YamlLog(BasicLogger.LogLevel.WARNING, message));
      }
   }

   public void warningAndFlush(String message) {
      this.warning(message);
      this.flush();
   }

   private void error(String message) {
      synchronized (this.logs) {
         this.logs.add(new BasicLogger.YamlLog(BasicLogger.LogLevel.ERROR, message));
      }
   }

   public void errorAndFlush(String message) {
      this.error(message);
      this.flush();
   }

   private void pop() {
      synchronized (this.logs) {
         this.logs.remove(this.logs.size() - 1);
      }
   }

   private void flush() {
      synchronized (this.logs) {
         for (BasicLogger.YamlLog log : this.logs) {
            this.log(log);
         }

         this.logs.clear();
      }
   }

   public void log(BasicLogger.YamlLog log) {
      this.flushedCount++;
      if (!this.silent) {
         Bukkit.getConsoleSender().sendMessage(log.getLevel().getDefaultColor() + this.formatMesssage(log.getLevel(), log.getMessage()));
      }
   }

   private String formatMesssage(BasicLogger.LogLevel level, String message) {
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
      INFO(ChatColor.AQUA, ChatColor.DARK_AQUA),
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
      private BasicLogger.LogLevel level;
      private String message;

      @Generated
      public void setLevel(BasicLogger.LogLevel level) {
         this.level = level;
      }

      @Generated
      public void setMessage(String message) {
         this.message = message;
      }

      @Generated
      public BasicLogger.LogLevel getLevel() {
         return this.level;
      }

      @Generated
      public String getMessage() {
         return this.message;
      }

      @Generated
      public YamlLog(BasicLogger.LogLevel level, String message) {
         this.level = level;
         this.message = message;
      }
   }
}
