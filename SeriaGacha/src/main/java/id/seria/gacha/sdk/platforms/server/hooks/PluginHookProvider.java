package id.seria.gacha.sdk.platforms.server.hooks;

import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class PluginHookProvider extends AbstractHookProvider {
   private final JavaPlugin plugin;

   public PluginHookProvider(String pluginName) {
      super(pluginName);
      this.plugin = (JavaPlugin)Bukkit.getPluginManager().getPlugin(pluginName);
   }

   @Override
   public boolean isAvailable() {
      return this.plugin != null;
   }

   @Override
   public boolean isEnabled() {
      return this.plugin != null && Bukkit.getPluginManager().isPluginEnabled(this.name);
   }

   @Generated
   public JavaPlugin getPlugin() {
      return this.plugin;
   }
}
