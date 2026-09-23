package id.seria.gacha.managers;

import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.player.PlayerDataLayer;
import id.seria.gacha.api.player.PlayerDbModel;
import id.seria.gacha.lib.common.services.services.database.context.DatabaseRepository;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.limbo.playerdata.DataModule;
import id.seria.gacha.listeners.PlayersListener;
import id.seria.gacha.managers.players.DirectDatabaseLayer;
import id.seria.gacha.managers.players.PlayerData;
import id.seria.gacha.managers.players.PlayerDatabaseModelImpl;
import id.seria.gacha.sdk.utilities.objects.ExpiringHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayersManager implements id.seria.gacha.api.managers.PlayersManager {
   private final PhoenixCrates plugin;
   private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();
   private DatabaseRepository<PlayerDatabaseModelImpl> databaseRepository;
   private PlayerDataLayer playerDataLayer;
   private final ExpiringHashMap<UUID, CompletableFuture<PlayerData>> fetchRequestQueue = new ExpiringHashMap<>(5000L);
   private final Map<UUID, Long> disposalTimestamps = new ConcurrentHashMap<>();

   public PlayersManager(PhoenixCrates plugin) {
      this.plugin = plugin;
   }

   public void load() throws Exception {
      this.databaseRepository = new DatabaseRepository<>(this.plugin.getDatabaseProvider(), PlayerDatabaseModelImpl.class);
      this.playerDataLayer = new DirectDatabaseLayer(this.databaseRepository);
      this.plugin.getEventBus().registerListener(new PlayersListener(this));
      Crates.getScheduler().scheduleAsync(this::updateRecords, 100L, 100L);
   }

   private void updateRecords() {
      long currentTime = System.currentTimeMillis();

      for (Entry<UUID, PlayerData> entry : this.players.entrySet()) {
         UUID uniqueId = entry.getKey();
         PlayerData record = entry.getValue();
         Long scheduledTime = this.disposalTimestamps.get(uniqueId);
         if (scheduledTime != null && currentTime >= scheduledTime) {
            this.players.remove(uniqueId);
            this.disposalTimestamps.remove(uniqueId);
            this.disposePlayerData(record);
         } else {
            this.updatePlayerData(record);
         }
      }
   }

   private void disposePlayerData(PlayerData data) {
      boolean isDirty = data.isDirty();
      PlayerDatabaseModelImpl model = data.getDatabaseModel();
      this.playerDataLayer.dispose(data.getUniqueId(), model, isDirty);
      if (isDirty) {
         data.unmarkDirty();
      }

      for (DataModule<?> module : data.getModules().values()) {
         module.onSave();
      }
   }

   public void updatePlayerData(PlayerData data) {
      if (data.isDirty()) {
         this.playerDataLayer.update(data.getDatabaseModel());
         data.unmarkDirty();
      }

      for (DataModule<?> module : data.getModules().values()) {
         module.onSave();
      }
   }

   public void reload() throws Exception {
      this.unload();
   }

   public void unload() throws Exception {
      this.updateRecords();
   }

   @Override
   public CompletableFuture<PlayerData> fetchPlayerDataAsync(OfflinePlayer player) {
      UUID uniqueId = player.getUniqueId();
      PlayerData cached = this.players.get(uniqueId);
      return cached != null ? CompletableFuture.completedFuture(cached) : this.fetchRequestQueue.computeIfAbsent(uniqueId, key -> {
         CompletableFuture<PlayerData> future = this.playerDataLayer.fetchByUniqueId(uniqueId).thenApply(databaseModel -> {
            if (databaseModel == null) {
               PlayerData newData = new PlayerData(-1, uniqueId, player.getName());
               PlayerDatabaseModelImpl model = newData.getDatabaseModel();
               this.playerDataLayer.insert(model).whenComplete((inserted, __) -> newData.setDatabaseId(model.getId()));
               return newData;
            }

            PlayerData data = PlayerData.from(databaseModel);
            data.setPlayerName(player.getName());

            for (DataModule<?> module : data.getModules().values()) {
               module.onLoad();
            }

            return data;
         }).thenApply(data -> {
            this.players.put(key, data);
            return (PlayerData)data;
         });
         long fetchTimeout = Crates.getConfiguration().getDatabase().getFetchTimeout();
         Utilities.getSchedulerService().schedule(() -> {
            if (!future.isDone()) {
               Crates.getLogger().warn(uniqueId + ": Data fetch is taking longer than " + fetchTimeout + "ms - still waiting on the database, not retrying.");
            }
         }, fetchTimeout, TimeUnit.MILLISECONDS);
         future.whenComplete((result, throwable) -> {
            if (throwable != null) {
               Crates.getLogger().error("Error fetching player information", throwable);
            }

            this.fetchRequestQueue.remove(key);
         });
         return future;
      });
   }

   public PlayerData getPlayerIfCached(OfflinePlayer player, boolean requestCache) {
      UUID uniqueId = player.getUniqueId();
      this.disposalTimestamps.remove(uniqueId);
      PlayerData data = this.players.get(uniqueId);
      if (data == null && requestCache) {
         this.fetchPlayerDataAsync(player);
      }

      return data;
   }

   public PlayerData getCachedDataNow(Player player) {
      PlayerData data = this.getPlayerIfCached(player, true);
      if (data == null) {
         throw new IllegalStateException("Player Data is not available");
      } else {
         return data;
      }
   }

   @Override
   public boolean isDataCached(OfflinePlayer player) {
      return this.players.containsKey(player.getUniqueId());
   }

   @Override
   public void setPlayerDataLayer(PlayerDataLayer layer) {
      if (layer != null) {
         layer.setNextLayer(this.playerDataLayer);
         this.playerDataLayer = layer;
      } else {
         this.playerDataLayer = new DirectDatabaseLayer(this.databaseRepository);
      }
   }

   @Deprecated
   private PlayerData getPlayerFromDatabaseSync(OfflinePlayer player) {
      PlayerDbModel databaseModel = this.databaseRepository.fetchNowBy("UniqueId", player.getUniqueId().toString());
      PlayerData record;
      if (databaseModel == null) {
         record = new PlayerData(-1, player.getUniqueId(), player.getName());
         PlayerDbModel model = record.getDatabaseModel();
         this.databaseRepository.insertLater((PlayerDatabaseModelImpl)model).whenComplete((output, __) -> record.setDatabaseId(model.getId()));
      } else {
         record = PlayerData.from(databaseModel);

         for (DataModule<?> module : record.getModules().values()) {
            module.onLoad();
         }
      }

      record.setPlayerName(player.getName());
      return record;
   }

   @Deprecated
   public PlayerData getPlayerFromDatabaseAsyncByName(String playerName) {
      PlayerDbModel databaseModel = this.databaseRepository.fetchNowBy("PlayerName", playerName.toLowerCase());
      if (databaseModel != null) {
         PlayerData data = PlayerData.from(databaseModel);
         data.setPlayerName(playerName);
         return data;
      } else {
         return null;
      }
   }

   public UUID fetchUuidByPlayerName(String playerName) {
      PlayerDbModel databaseModel = this.databaseRepository.fetchNowBy("PlayerName", playerName.toLowerCase());
      return databaseModel != null && databaseModel.getUniqueId() != null ? UUID.fromString(databaseModel.getUniqueId()) : null;
   }

   public void disposeCachedPlayerData(OfflinePlayer player) {
      this.disposeCachedPlayerData(player.getUniqueId());
   }

   public void disposeCachedPlayerData(UUID uniqueId) {
      PlayerData cached = this.players.get(uniqueId);
      if (cached != null) {
         if (this.plugin.getAddonRegistry().isAddonEnabled("Redis")) {
            this.players.remove(uniqueId);
            this.disposePlayerData(cached);
            return;
         }

         this.disposalTimestamps.put(uniqueId, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5L));
      }
   }

   @Deprecated
   public List<PlayerData> getAllPlayerDataFromDatabase() {
      List<PlayerDatabaseModelImpl> models = this.databaseRepository.fetchAllNow();
      List<PlayerData> playersData = new ArrayList<>();

      for (PlayerDbModel model : models) {
         UUID uniqueId = UUID.fromString(model.getUniqueId());
         PlayerData data = this.players.get(uniqueId);
         if (data == null) {
            data = PlayerData.from(model);
         }

         playersData.add(data);
      }

      return playersData;
   }

   @Generated
   public PhoenixCrates getPlugin() {
      return this.plugin;
   }

   @Generated
   public Map<UUID, PlayerData> getPlayers() {
      return this.players;
   }

   @Generated
   public DatabaseRepository<PlayerDatabaseModelImpl> getDatabaseRepository() {
      return this.databaseRepository;
   }

   @Generated
   public PlayerDataLayer getPlayerDataLayer() {
      return this.playerDataLayer;
   }

   @Generated
   public ExpiringHashMap<UUID, CompletableFuture<PlayerData>> getFetchRequestQueue() {
      return this.fetchRequestQueue;
   }

   @Generated
   public Map<UUID, Long> getDisposalTimestamps() {
      return this.disposalTimestamps;
   }
}
