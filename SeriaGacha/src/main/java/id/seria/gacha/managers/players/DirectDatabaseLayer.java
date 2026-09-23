package id.seria.gacha.managers.players;

import id.seria.gacha.api.player.PlayerDataLayer;
import id.seria.gacha.api.player.PlayerDbModel;
import id.seria.gacha.lib.common.services.services.database.context.DatabaseRepository;
import id.seria.gacha.lib.common.utils.Utilities;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;

public class DirectDatabaseLayer implements PlayerDataLayer {
   private final DatabaseRepository<PlayerDatabaseModelImpl> databaseRepository;
   private PlayerDataLayer nextLayer;

   public DirectDatabaseLayer(DatabaseRepository<PlayerDatabaseModelImpl> databaseRepository) {
      this.databaseRepository = databaseRepository;
   }

   @Override
   public CompletableFuture<PlayerDbModel> insert(PlayerDbModel model) {
      return this.databaseRepository.insertLater((PlayerDatabaseModelImpl)model).thenApply(result -> model);
   }

   @Override
   public CompletableFuture<Void> update(PlayerDbModel model) {
      return this.databaseRepository.updateLater((PlayerDatabaseModelImpl)model).thenApply(result -> null);
   }

   @Override
   public CompletableFuture<PlayerDbModel> fetchByUniqueId(UUID uniqueId) {
      return CompletableFuture.supplyAsync(() -> this.databaseRepository.fetchNowBy("UniqueId", uniqueId.toString()), Utilities.getAsyncService());
   }

   @Generated
   public DatabaseRepository<PlayerDatabaseModelImpl> getDatabaseRepository() {
      return this.databaseRepository;
   }

   @Generated
   @Override
   public PlayerDataLayer getNextLayer() {
      return this.nextLayer;
   }

   @Generated
   @Override
   public void setNextLayer(PlayerDataLayer nextLayer) {
      this.nextLayer = nextLayer;
   }
}
