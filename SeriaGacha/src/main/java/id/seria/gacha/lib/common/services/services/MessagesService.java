package id.seria.gacha.lib.common.services.services;

import id.seria.gacha.lib.common.services.factory.ServicesFactory;
import id.seria.gacha.lib.common.services.services.messages.PluginMessagesHolder;
import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

@Deprecated
public class MessagesService implements ServicesFactory.ServiceLifecycle, Listener {
   private final ServerPlugin plugin;
   private final Map<ServerPlugin, PluginMessagesHolder> holders = new HashMap<>();

   public MessagesService(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public void load() throws Exception {
      this.plugin.getEventBus().registerListener(this);
   }

   @Override
   public void unload() throws Exception {
      this.plugin.getEventBus().unregisterListener(this);
   }

   public PluginMessagesHolder registerPluginMessages(ServerPlugin plugin, Configuration messagesConfiguration) throws Exception {
      return this.registerPluginMessages(plugin, messagesConfiguration, "messages.yml", "messages.yml");
   }

   public PluginMessagesHolder registerPluginMessages(ServerPlugin plugin, Configuration messagesConfiguration, String fileName) throws Exception {
      return this.registerPluginMessages(plugin, messagesConfiguration, fileName, fileName);
   }

   public PluginMessagesHolder registerPluginMessages(ServerPlugin plugin, Configuration messagesConfiguration, String sourceFileName, String targetFileName) throws Exception {
      PluginMessagesHolder holder = new PluginMessagesHolder(plugin, messagesConfiguration, sourceFileName, targetFileName);
      holder.load();
      synchronized (this.holders) {
         this.holders.put(plugin, holder);
         return holder;
      }
   }

   public PluginMessagesHolder unregisterPluginMessages(JavaPlugin plugin) throws Exception {
      synchronized (this.holders) {
         PluginMessagesHolder holder = this.holders.remove(plugin);
         if (holder != null) {
            holder.unload();
         }

         return holder;
      }
   }

   @EventHandler
   protected void onPluginDisable(PluginDisableEvent event) {
      if (event.getPlugin() instanceof JavaPlugin) {
         JavaPlugin plugin = (JavaPlugin)event.getPlugin();
         synchronized (this.holders) {
            this.holders.remove(plugin);
         }
      }
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public Map<ServerPlugin, PluginMessagesHolder> getHolders() {
      return this.holders;
   }
}
