package id.seria.gacha.api.managers;

import id.seria.gacha.api.player.PlayerData;
import id.seria.gacha.api.player.PlayerDataLayer;
import java.util.concurrent.CompletableFuture;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface PlayersManager {
   CompletableFuture<? extends PlayerData> fetchPlayerDataAsync(OfflinePlayer var1);

   PlayerData getPlayerIfCached(OfflinePlayer var1, boolean var2);

   PlayerData getCachedDataNow(Player var1);

   boolean isDataCached(OfflinePlayer var1);

   void setPlayerDataLayer(PlayerDataLayer var1);
}
