package id.seria.gacha.managers.players;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import id.seria.gacha.api.player.PlayerDbModel;
import id.seria.gacha.lib.common.services.services.database.context.ModelIdentifier;
import id.seria.gacha.lib.common.services.services.database.context.holder.DatabaseModelHolder;
import id.seria.gacha.lib.common.utils.objects.sealed.SealedHashMap;
import id.seria.gacha.limbo.playerdata.DataModule;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Function;
import lombok.Generated;
import org.bukkit.entity.Player;

public class PlayerData implements DatabaseModelHolder<PlayerDatabaseModelImpl>, id.seria.gacha.api.player.PlayerData {
   private static final Gson GSON = new GsonBuilder().create();
   private static final Map<Class<? extends DataModule<?>>, Function<PlayerData, DataModule<?>>> REGISTRY = new HashMap<>();
   private final Map<Class<? extends DataModule<?>>, DataModule<?>> modules = new HashMap<>();
   private final Object databaseLock = new Object();
   private int databaseId;
   private final UUID uniqueId;
   private String playerName;
   private SealedHashMap<String, Integer> openedCratesAmount = new SealedHashMap<>();
   private SealedHashMap<String, Long> cratesCooldown = new SealedHashMap<>();
   private SealedHashMap<String, Integer> rewardsWinAmount = new SealedHashMap<>();
   private SealedHashMap<String, Long> rewardsCooldown = new SealedHashMap<>();
   private SealedHashMap<String, Integer> virtualKeys = new SealedHashMap<>();
   private SealedHashMap<String, Integer> rerolls = new SealedHashMap<>();
   private boolean dirty;

   public static <T extends DataModule<?>> void registerModule(Class<T> clazz, Function<PlayerData, T> factory) {
      REGISTRY.put(clazz, factory::apply);
   }

   public static PlayerData from(PlayerDbModel model) {
      SealedHashMap<String, Integer> openedCratesAmount = new SealedHashMap<>(
         (Map<String, Integer>)GSON.fromJson(protectRawMap(model.getOpenedCratesAmount()), (new TypeToken<HashMap<String, Integer>>() {}).getType())
      );
      SealedHashMap<String, Long> cratesCooldown = new SealedHashMap<>(
         (Map<String, Long>)GSON.fromJson(protectRawMap(model.getCratesCooldown()), (new TypeToken<HashMap<String, Long>>() {}).getType())
      );
      SealedHashMap<String, Integer> rewardsWinAmount = new SealedHashMap<>(
         (Map<String, Integer>)GSON.fromJson(protectRawMap(model.getRewardsWinAmount()), (new TypeToken<HashMap<String, Integer>>() {}).getType())
      );
      SealedHashMap<String, Long> rewardsCooldown = new SealedHashMap<>(
         (Map<String, Long>)GSON.fromJson(protectRawMap(model.getRewardsCooldown()), (new TypeToken<HashMap<String, Long>>() {}).getType())
      );
      SealedHashMap<String, Integer> virtualKeys = new SealedHashMap<>(
         (Map<String, Integer>)GSON.fromJson(protectRawMap(model.getVirtualKeys()), (new TypeToken<HashMap<String, Integer>>() {}).getType())
      );
      SealedHashMap<String, Integer> rerolls = new SealedHashMap<>(
         (Map<String, Integer>)GSON.fromJson(protectRawMap(model.getRerolls()), (new TypeToken<HashMap<String, Integer>>() {}).getType())
      );
      return new PlayerData(
         model.getId(),
         UUID.fromString(model.getUniqueId()),
         model.getPlayerName(),
         openedCratesAmount,
         cratesCooldown,
         rewardsWinAmount,
         rewardsCooldown,
         virtualKeys,
         rerolls
      );
   }

   private static String protectRawMap(String rawMap) {
      return rawMap == null ? "{}" : rawMap;
   }

   public PlayerData(int databaseId, UUID uniqueId, String playerName) {
      this.databaseId = databaseId;
      this.uniqueId = uniqueId;
      this.setPlayerName(playerName);
      this.initializeModules();
   }

   private void initializeModules() {
      for (Entry<Class<? extends DataModule<?>>, Function<PlayerData, DataModule<?>>> entry : REGISTRY.entrySet()) {
         this.modules.put(entry.getKey(), entry.getValue().apply(this));
      }
   }

   public <K extends ModelIdentifier, T extends DataModule<K>> T getModule(Class<T> clazz) {
      DataModule<?> module = this.modules.get(clazz);
      if (module == null) {
         Function<PlayerData, DataModule<?>> factory = REGISTRY.get(clazz);
         if (factory != null) {
            module = factory.apply(this);
            this.modules.put(clazz, module);
            module.onLoad();
         }
      }

      return Objects.requireNonNull(clazz.cast(module), "Module " + clazz.getSimpleName() + " not found");
   }

   public <T extends DataModule<?>> boolean hasModule(Class<T> clazz) {
      return this.modules.containsKey(clazz);
   }

   public PlayerData(
      int databaseId,
      UUID uniqueId,
      String playerName,
      SealedHashMap<String, Integer> openedCratesAmount,
      SealedHashMap<String, Long> cratesCooldown,
      SealedHashMap<String, Integer> rewardsWinAmount,
      SealedHashMap<String, Long> rewardsCooldown,
      SealedHashMap<String, Integer> virtualKeys,
      SealedHashMap<String, Integer> rerolls
   ) {
      this(databaseId, uniqueId, playerName);
      this.openedCratesAmount = openedCratesAmount;
      this.cratesCooldown = cratesCooldown;
      this.rewardsWinAmount = rewardsWinAmount;
      this.rewardsCooldown = rewardsCooldown;
      this.virtualKeys = virtualKeys;
      this.rerolls = rerolls;
   }

   public PlayerData(
      int databaseId,
      Player player,
      SealedHashMap<String, Integer> openedCratesAmount,
      SealedHashMap<String, Long> cratesCooldown,
      SealedHashMap<String, Integer> rewardsWinAmount,
      SealedHashMap<String, Long> rewardsCooldown,
      SealedHashMap<String, Integer> virtualKeys,
      SealedHashMap<String, Integer> rerolls
   ) {
      this(databaseId, player.getUniqueId(), player.getName());
      this.openedCratesAmount = openedCratesAmount;
      this.cratesCooldown = cratesCooldown;
      this.rewardsWinAmount = rewardsWinAmount;
      this.rewardsCooldown = rewardsCooldown;
      this.virtualKeys = virtualKeys;
      this.rerolls = rerolls;
   }

   @Override
   public int getDatabaseId() {
      synchronized (this.databaseLock) {
         return this.databaseId;
      }
   }

   @Override
   public void setDatabaseId(int newId) {
      synchronized (this.databaseLock) {
         this.databaseId = newId;
         this.markDirty();
      }
   }

   public void setPlayerName(String playerName) {
      if (playerName != null && (this.playerName == null || !this.playerName.equals(playerName))) {
         this.playerName = playerName;
         this.markDirty();
      }
   }

   public void addCrateCooldown(String crateIdentifier, Long millis) {
      this.cratesCooldown.setSealed(false);
      this.cratesCooldown.put(crateIdentifier, millis);
      this.cratesCooldown.setSealed(true);
      this.markDirty();
   }

   public void removeCrateCooldown(String crateIdentifier) {
      this.cratesCooldown.setSealed(false);
      this.cratesCooldown.remove(crateIdentifier);
      this.cratesCooldown.setSealed(true);
      this.markDirty();
   }

   public Long getCrateCooldown(String crateIdentifier) {
      return this.cratesCooldown.getOrDefault(crateIdentifier, 0L);
   }

   public void incrementRewardWinAmount(String rewardIdentifier) {
      this.rewardsWinAmount.setSealed(false);
      this.rewardsWinAmount.put(rewardIdentifier, this.getRewardWinAmount(rewardIdentifier) + 1);
      this.rewardsWinAmount.setSealed(true);
      this.markDirty();
   }

   public void resetRewardWinAmount(String rewardIdentifier) {
      this.rewardsWinAmount.setSealed(false);
      this.rewardsWinAmount.remove(rewardIdentifier);
      this.rewardsWinAmount.setSealed(true);
      this.markDirty();
   }

   public void incrementOpenedCratesAmount(String crateIdentifier, int amount) {
      this.openedCratesAmount.setSealed(false);
      this.openedCratesAmount.put(crateIdentifier, this.getOpenedCratesAmount(crateIdentifier) + amount);
      this.openedCratesAmount.setSealed(true);
      this.markDirty();
   }

   public int getOpenedCratesAmount(String crateIdentifier) {
      return this.openedCratesAmount.getOrDefault(crateIdentifier, 0);
   }

   public int getRewardWinAmount(String rewardIdentifier) {
      return this.rewardsWinAmount.getOrDefault(rewardIdentifier, 0);
   }

   public long getRewardCooldown(String rewardIdentifier) {
      return this.rewardsCooldown.getOrDefault(rewardIdentifier, 0L);
   }

   public void setRewardCooldown(String rewardIdentifier, long timestamp) {
      this.rewardsCooldown.setSealed(false);
      this.rewardsCooldown.put(rewardIdentifier, timestamp);
      this.rewardsCooldown.setSealed(true);
      this.markDirty();
   }

   public int getVirtualKeys(String crateIdentifier) {
      return Math.max(0, this.virtualKeys.getOrDefault(crateIdentifier, 0));
   }

   public void setVirtualKeys(String crateIdentifier, int amount) {
      this.virtualKeys.setSealed(false);
      this.virtualKeys.put(crateIdentifier, Math.max(0, amount));
      this.virtualKeys.setSealed(true);
      this.markDirty();
   }

   public int getCrateRerolls(String crateIdentifier) {
      return Math.max(0, this.rerolls.getOrDefault(crateIdentifier, 0));
   }

   public void setCrateRerolls(String crateIdentifier, int amount) {
      this.rerolls.setSealed(false);
      this.rerolls.put(crateIdentifier, Math.max(0, amount));
      this.rerolls.setSealed(true);
      this.markDirty();
   }

   @Override
   public void markDirty() {
      this.dirty = true;
   }

   @Override
   public void unmarkDirty() {
      this.dirty = false;
   }

   @Override
   public boolean isDirty() {
      return this.dirty;
   }

   public PlayerDatabaseModelImpl getDatabaseModel() {
      synchronized (this.databaseLock) {
         return new PlayerDatabaseModelImpl(
            this.databaseLock,
            this.databaseId,
            this.uniqueId.toString(),
            this.playerName.toLowerCase(),
            GSON.toJson(this.openedCratesAmount),
            "{}",
            GSON.toJson(this.cratesCooldown),
            GSON.toJson(this.rewardsWinAmount),
            GSON.toJson(this.rewardsCooldown),
            GSON.toJson(this.virtualKeys),
            GSON.toJson(this.rerolls)
         );
      }
   }

   @Generated
   public Map<Class<? extends DataModule<?>>, DataModule<?>> getModules() {
      return this.modules;
   }

   @Generated
   public Object getDatabaseLock() {
      return this.databaseLock;
   }

   @Generated
   public UUID getUniqueId() {
      return this.uniqueId;
   }

   @Generated
   public String getPlayerName() {
      return this.playerName;
   }

   @Generated
   public SealedHashMap<String, Integer> getOpenedCratesAmount() {
      return this.openedCratesAmount;
   }

   @Generated
   public SealedHashMap<String, Long> getCratesCooldown() {
      return this.cratesCooldown;
   }

   @Generated
   public SealedHashMap<String, Integer> getRewardsWinAmount() {
      return this.rewardsWinAmount;
   }

   @Generated
   public SealedHashMap<String, Long> getRewardsCooldown() {
      return this.rewardsCooldown;
   }

   @Generated
   public SealedHashMap<String, Integer> getVirtualKeys() {
      return this.virtualKeys;
   }

   @Generated
   public SealedHashMap<String, Integer> getRerolls() {
      return this.rerolls;
   }

   @Generated
   public void setOpenedCratesAmount(SealedHashMap<String, Integer> openedCratesAmount) {
      this.openedCratesAmount = openedCratesAmount;
   }

   @Generated
   public void setCratesCooldown(SealedHashMap<String, Long> cratesCooldown) {
      this.cratesCooldown = cratesCooldown;
   }

   @Generated
   public void setRewardsWinAmount(SealedHashMap<String, Integer> rewardsWinAmount) {
      this.rewardsWinAmount = rewardsWinAmount;
   }

   @Generated
   public void setRewardsCooldown(SealedHashMap<String, Long> rewardsCooldown) {
      this.rewardsCooldown = rewardsCooldown;
   }

   @Generated
   public void setVirtualKeys(SealedHashMap<String, Integer> virtualKeys) {
      this.virtualKeys = virtualKeys;
   }

   @Generated
   public void setRerolls(SealedHashMap<String, Integer> rerolls) {
      this.rerolls = rerolls;
   }
}
