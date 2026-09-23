package id.seria.gacha.lib.common.utils.configuration.logger;

import org.bukkit.Bukkit;

public class BukkitYamlLogger extends ConfigYamlLogger {
   public BukkitYamlLogger() {
   }

   @Override
   public void flushRaw(String message) {
      Bukkit.getConsoleSender().sendMessage(message);
   }
}
