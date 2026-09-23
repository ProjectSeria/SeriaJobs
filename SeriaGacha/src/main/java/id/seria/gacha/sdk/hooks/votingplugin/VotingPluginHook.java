package id.seria.gacha.sdk.hooks.votingplugin;

import com.bencodez.votingplugin.VotingPluginMain;
import com.bencodez.votingplugin.user.VotingPluginUser;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

public class VotingPluginHook extends ServerHook {
   private VotingPluginMain api;

   protected VotingPluginHook() {
   }

   @Override
   public void init() {
   }

   @Override
   public void load() {
      Plugin plugin = Bukkit.getPluginManager().getPlugin("VotingPlugin");
      this.api = plugin instanceof VotingPluginMain ? (VotingPluginMain)plugin : null;
   }

   @Override
   public void unload() {
      if (this.api != null) {
         this.api = null;
      }
   }

   @Override
   public void reload() {
      this.unload();
      this.load();
   }

   public VotingPluginHookProvider getProvider() {
      return (VotingPluginHookProvider)super.getProvider();
   }

   private VotingPluginUser getUser(OfflinePlayer player) {
      Validate.isTrue(this.api != null, "VotingPlugin not found or initialized.");
      return this.api.getUser(player.getUniqueId());
   }

   public int getPoints(OfflinePlayer player) {
      return this.getUser(player).getPoints();
   }

   public int addPoints(OfflinePlayer player, int amount) {
      return this.getUser(player).addPoints(amount);
   }

   public boolean removePoints(OfflinePlayer player, int amount) {
      return this.getUser(player).removePoints(amount);
   }

   public void setPoints(OfflinePlayer player, int amount) {
      this.getUser(player).setPoints(amount);
   }

   public boolean has(OfflinePlayer player, int amount) {
      return this.getPoints(player) >= amount;
   }

   public String format(int amount) {
      return String.valueOf(amount);
   }
}
