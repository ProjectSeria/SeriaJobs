package id.seria.gacha.api.player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerDataLayer {
   CompletableFuture<? extends PlayerDbModel> fetchByUniqueId(UUID var1);

   <T extends PlayerDbModel> CompletableFuture<T> insert(T var1);

   <T extends PlayerDbModel> CompletableFuture<Void> update(T var1);

   void setNextLayer(PlayerDataLayer var1);

   PlayerDataLayer getNextLayer();

   default CompletableFuture<Void> dispose(UUID uniqueId, PlayerDbModel model, boolean isDirty) {
      return isDirty && model != null ? this.update(model) : CompletableFuture.completedFuture(null);
   }
}
