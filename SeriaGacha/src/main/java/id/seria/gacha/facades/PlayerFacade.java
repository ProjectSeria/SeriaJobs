package id.seria.gacha.facades;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.managers.players.PlayerData;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class PlayerFacade {
   private PlayerFacade() {
   }

   private static void runOnPlayerRegion(Player player, Runnable task) {
      if (player != null && player.isOnline()) {
         Crates.getScheduler().runSync(player, task);
      } else {
         Crates.getScheduler().runSync(task);
      }
   }

   public static void processPlayerData(String playerName, Consumer<PlayerData> onFetch) {
      processPlayerData(playerName, onFetch, e -> Crates.getLogger().error("An error occurred while processing player data", e));
   }

   public static void processPlayerData(Player player, Consumer<PlayerData> onFetch) {
      processPlayerData(player, onFetch, e -> Crates.getLogger().error("An error occurred while processing player data", e));
   }

   public static void processPlayerData(String playerName, Consumer<PlayerData> onFetch, Consumer<Exception> onFailure) {
      Crates.getScheduler().runAsync(() -> {
         try {
            Player online = Bukkit.getPlayer(playerName);
            boolean shouldDisposeAfter = online == null;
            PlayerData data;
            if (online == null) {
               UUID resolvedUuid = Crates.getPlayersManager().fetchUuidByPlayerName(playerName);
               if (resolvedUuid == null) {
                  throw new CommandContextException(Translatable.literal("\u00a7cPlayer \u00a7e" + playerName + "\u00a7c not found!"));
               }

               OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(resolvedUuid);
               data = Crates.getPlayersManager().fetchPlayerDataAsync(offlinePlayer).get();
            } else {
               data = Crates.getPlayersManager().fetchPlayerDataAsync(online).get();
            }

            if (data == null) {
               throw new CommandContextException(Translatable.literal("\u00a7cPlayer \u00a7e" + playerName + "\u00a7c not found!"));
            }

            runOnPlayerRegion(online, () -> {
               try {
                  onFetch.accept(data);
                  if (data.isDirty()) {
                     Crates.getPlayersManager().updatePlayerData(data);
                  }

                  if (shouldDisposeAfter) {
                     Crates.getPlayersManager().disposeCachedPlayerData(data.getUniqueId());
                  }
               } catch (Exception e) {
                  onFailure.accept(e);
               }
            });
         } catch (Exception e) {
            Crates.getScheduler().runSync(() -> onFailure.accept(e));
         }
      });
   }

   public static void processPlayerData(Player player, Consumer<PlayerData> onFetch, Consumer<Exception> onFailure) {
      Crates.getScheduler().runAsync(() -> {
         try {
            PlayerData data = Crates.getPlayersManager().fetchPlayerDataAsync(player).get();
            if (data == null) {
               throw new CommandContextException(Translatable.literal("\u00a7cPlayer \u00a7e" + player.getName() + "\u00a7c not found!"));
            }

            runOnPlayerRegion(player, () -> {
               try {
                  onFetch.accept(data);
                  if (data.isDirty()) {
                     Crates.getPlayersManager().updatePlayerData(data);
                  }
               } catch (Exception ex) {
                  onFailure.accept(ex);
               }
            });
         } catch (Exception e) {
            Crates.getScheduler().runSync(() -> onFailure.accept(e));
         }
      });
   }
}
