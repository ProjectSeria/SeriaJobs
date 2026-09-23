package id.seria.gacha.sdk.platforms.server.components;

import id.seria.gacha.api.sdk.logger.Logger;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.logging.Level;

public class ServerLogger implements Logger {
   private ServerPlugin plugin;

   public ServerLogger(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public void info(String message) {
      this.plugin.getOriginal().getLogger().info(message);
   }

   @Override
   public void warn(String message) {
      this.plugin.getOriginal().getLogger().warning(message);
   }

   @Override
   public void error(String message) {
      this.plugin.getOriginal().getLogger().severe(message);
   }

   @Override
   public void info(String message, Throwable t) {
      this.plugin.getOriginal().getLogger().log(Level.INFO, message, t);
   }

   @Override
   public void warn(String message, Throwable t) {
      this.plugin.getOriginal().getLogger().log(Level.WARNING, message, t);
   }

   @Override
   public void error(String message, Throwable t) {
      this.plugin.getOriginal().getLogger().log(Level.SEVERE, message, t);
   }

   @Override
   public void debug(String message) {
      if (this.plugin.isDebugEnabled()) {
         this.plugin.getOriginal().getLogger().log(Level.WARNING, "[DEBUG]" + message);
      }
   }

   @Override
   public void debug(String message, Throwable t) {
      if (this.plugin.isDebugEnabled()) {
         this.plugin.getOriginal().getLogger().log(Level.WARNING, "[DEBUG]" + message, t);
      }
   }
}
