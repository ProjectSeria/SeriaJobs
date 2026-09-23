package id.seria.gacha.limbo.playerdata;

import id.seria.gacha.Crates;
import id.seria.gacha.lib.common.services.services.database.context.DatabaseRepository;
import id.seria.gacha.lib.common.services.services.database.context.ModelIdentifier;
import id.seria.gacha.managers.players.PlayerData;
import java.lang.reflect.ParameterizedType;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;

public abstract class DataModule<T extends ModelIdentifier> {
   private static final ConcurrentHashMap<Class<?>, DatabaseRepository<?>> REPOSITORIES = new ConcurrentHashMap<>();
   protected final Object lock = new Object();
   protected final DatabaseRepository<T> repository;
   protected PlayerData playerData;
   private boolean dirty;

   public DataModule(PlayerData playerData) {
      Class<T> type = (Class<T>)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
      this.repository = (DatabaseRepository<T>)REPOSITORIES.computeIfAbsent(
         this.getClass(), cls -> new DatabaseRepository<>(Crates.getDatabaseProvider(), type)
      );
      this.playerData = playerData;
   }

   public abstract void onLoad();

   public abstract void onSave();

   public void markDirty() {
      this.dirty = true;
   }

   public void unmarkDirty() {
      this.dirty = false;
   }

   @Generated
   public Object getLock() {
      return this.lock;
   }

   @Generated
   public DatabaseRepository<T> getRepository() {
      return this.repository;
   }

   @Generated
   public PlayerData getPlayerData() {
      return this.playerData;
   }

   @Generated
   public boolean isDirty() {
      return this.dirty;
   }
}
