package id.seria.gacha.sdk.hooks.playerpoints;

import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import org.apache.commons.lang.Validate;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

public class PlayerPointsHook extends ServerHook {
   private PlayerPointsAPI api;

   protected PlayerPointsHook() {
   }

   @Override
   public void init() {
   }

   @Override
   public void load() {
      Plugin plugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");
      this.api = plugin instanceof PlayerPoints ? ((PlayerPoints)plugin).getAPI() : null;
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

   public PlayerPointsHookProvider getProvider() {
      return (PlayerPointsHookProvider)super.getProvider();
   }

   public int look(OfflinePlayer player) {
      Validate.isTrue(this.api != null, "PlayerPoints not found or initialized.");
      return this.api.look(player.getUniqueId());
   }

   public boolean give(OfflinePlayer player, int amount) {
      Validate.isTrue(this.api != null, "PlayerPoints not found or initialized.");
      return this.api.give(player.getUniqueId(), amount);
   }

   public boolean take(OfflinePlayer player, int amount) {
      Validate.isTrue(this.api != null, "PlayerPoints not found or initialized.");
      return this.api.take(player.getUniqueId(), amount);
   }

   public boolean has(OfflinePlayer player, int amount) {
      return this.look(player) >= amount;
   }

   public String format(int amount) {
      return String.valueOf(amount);
   }
}
