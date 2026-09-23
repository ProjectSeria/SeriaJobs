package id.seria.gacha.managers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.api.reward.GuaranteedRewardType;
import id.seria.gacha.configurations.adapters.EngineDataConfigAdapter;
import id.seria.gacha.configurations.adapters.OpeningAnimationConfigAdapter;
import id.seria.gacha.configurations.crates.CrateLocation;
import id.seria.gacha.configurations.crates.CratesLocationsConfiguration;
import id.seria.gacha.facades.CrateFacade;
import id.seria.gacha.facades.RewardFacade;
import id.seria.gacha.internal.FacingDirection;
import id.seria.gacha.lib.common.uicomponents.newest.Ui;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.workers.AsyncQueueConfigurationWorker;
import id.seria.gacha.lib.common.utils.configuration.workers.AsyncQueueMultiWorker;
import id.seria.gacha.lib.common.utils.configuration.workers.ConfigurationWorker;
import id.seria.gacha.lib.common.utils.utility.FileUtil;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.item.adapter.BaseItemAdapter;
import id.seria.gacha.limbo.item.storage.ItemStorageScope;
import id.seria.gacha.listeners.CratesListener;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.animations.idle.IdleEffect;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectData;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectType;
import id.seria.gacha.managers.crates.animations.opening.OpeningPhase;
import id.seria.gacha.managers.crates.animations.opening.data.ColoredOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.data.EmptyOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.data.KeybasedOpeningPhaseData;
import id.seria.gacha.managers.crates.rewards.GlobalRewardStatsTracker;
import id.seria.gacha.managers.crates.session.CrateOpeningSession;
import id.seria.gacha.managers.crates.type.CachedCrateType;
import id.seria.gacha.managers.crates.type.list.OpeningAnimationWrapper;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.managers.tasks.LocationTaskBus;
import id.seria.gacha.managers.tasks.TaskSubscriber;
import id.seria.gacha.registries.AnimationsRegistry;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Generated;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class CratesManager implements id.seria.gacha.api.managers.CratesManager {
   private static final int MAX_LITE_CRATE_TYPES = 5;
   private final PhoenixCrates plugin;
   private final Set<CachedCrateType> types = new LinkedHashSet<>();
   private final Map<CrateLocation, Crate> crates = new ConcurrentHashMap<>();
   private final Set<CrateOpeningSession> openingSessions = ConcurrentHashMap.newKeySet();
   private final LocationTaskBus locationTaskBus = new LocationTaskBus();
   private final GlobalRewardStatsTracker globalRewardStatsTracker;
   private final AsyncQueueMultiWorker filesWorker;
   private final CratesLocationsConfiguration locationsConfiguration;
   private final AsyncQueueConfigurationWorker locationsWorker;

   public CratesManager(PhoenixCrates plugin) {
      this.plugin = plugin;
      this.filesWorker = new AsyncQueueMultiWorker(plugin);
      this.locationsConfiguration = new CratesLocationsConfiguration();
      this.locationsWorker = new AsyncQueueConfigurationWorker(plugin, this.locationsConfiguration, null, "locations.yml");
      this.globalRewardStatsTracker = new GlobalRewardStatsTracker(plugin);
   }

   public void load() throws Exception {
      this.filesWorker.open();
      this.locationsWorker.open();
      this.locationsWorker.validate();
      this.globalRewardStatsTracker.load();
      this.loadTypesFromFile();
      this.loadCratesFromFile();
      this.plugin.getEventBus().registerListener(new CratesListener(this));
   }

   public void reload() throws Exception {
      this.unload();
      this.filesWorker.open();
      this.locationsWorker.open();
      this.locationsWorker.validate();
      this.globalRewardStatsTracker.load();
      this.loadTypesFromFile();
      this.loadCratesFromFile();
   }

   public void unload() throws Exception {
      for (Crate crate : this.crates.values()) {
         crate.unload(false);
      }

      this.locationTaskBus.shutdown();
      this.locationsWorker.close();
      this.filesWorker.close();
      this.globalRewardStatsTracker.unload();
      this.crates.clear();
      this.types.clear();
      this.openingSessions.clear();
   }

   @Override
   public List<CrateType> getRegisteredTypes() {
      return this.types.stream().map(CachedCrateType::getType).collect(Collectors.toList());
   }

   @Override
   public int countRegisteredTypes() {
      return this.types.size();
   }

   @Override
   public List<CrateType> getCrateTypes() {
      return this.types.stream().map(CachedCrateType::getType).filter(CrateType::isEnabled).collect(Collectors.toList());
   }

   @Override
   public Set<String> getCratesIdentifier() {
      return this.types.stream().map(c -> c.getType().getIdentifier()).collect(Collectors.toSet());
   }

   public CrateType getTypeByIdentifier(String key) {
      return this.types.stream().map(CachedCrateType::getType).filter(t -> t.getIdentifier().equalsIgnoreCase(key)).findFirst().orElse(null);
   }

   public Crate getCrateByBlock(Block block) {
      return this.crates.values().stream().filter(c -> c.getBlock().equals(block)).findFirst().orElse(null);
   }

   @Override
   public List<CrateOpeningSession> getPlayerOpeningSessions(Player player) {
      return this.openingSessions.stream().filter(session -> session.getContext().getPlayerId().equals(player.getUniqueId())).collect(Collectors.toList());
   }

   public void subscribeTask(TaskSubscriber subscriber) {
      this.locationTaskBus.subscribe(subscriber);
   }

   public void subscribeTask(TaskSubscriber subscriber, Runnable onRelease) {
      this.locationTaskBus.subscribe(subscriber, onRelease);
   }

   public void unsubscribeTask(TaskSubscriber subscriber) {
      this.locationTaskBus.unsubscribe(subscriber);
   }

   public void registerOpeningSession(CrateOpeningSession session) {
      if (session != null) {
         this.openingSessions.add(session);
         Crate.OPENING_SESSIONS.add(session.getContext().getPlayerId(), session);
         this.subscribeTask(session, () -> this.releaseOpeningSession(session));
      }
   }

   public void unregisterOpeningSession(CrateOpeningSession session) {
      this.unregisterOpeningSession(session, false);
   }

   public void unregisterOpeningSession(CrateOpeningSession session, boolean forceUnload) {
      if (session != null) {
         if (forceUnload && !session.isUnloaded()) {
            session.unload(true);
         }

         this.unsubscribeTask(session);
      }
   }

   private void releaseOpeningSession(CrateOpeningSession session) {
      UUID playerId = session.getContext().getPlayerId();
      session.getCrate().getCurrentSessions().remove(playerId, session);
      Crate.OPENING_SESSIONS.remove(playerId, session);
      this.openingSessions.remove(session);
   }

   public List<Crate> getCratesByType(CrateType type) {
      return this.crates.values().stream().filter(c -> c.getType().equals(type)).collect(Collectors.toList());
   }

   public Crate getAnyCrateByType(CrateType type) {
      return this.crates.values().stream().filter(c -> c.getType().equals(type)).findFirst().orElse(null);
   }

   public void saveCrateType(CrateType type) throws Exception {
      if (type.isDirtyIdentifier()) {
         this.locationsConfiguration.getLocations().remove(type.getOldIdentifier());

         for (Crate crate : this.getCratesByType(type)) {
            this.addCrateFromConfiguration(crate, false);
         }

         type.setDirtyIdentifier(false);
      }

      if (type.isEnabled()) {
         this.reloadCratesByType(type);
      }

      this.filesWorker.queueSave(this.getCachedCrateTypeOrNull(type.getIdentifier()).getConfigurationWorker());
      this.locationsWorker.save();
   }

   public CachedCrateType getCachedCrateTypeOrNull(String identifier) {
      synchronized (this.types) {
         return this.types.stream().filter(c -> c.getType().getIdentifier().equalsIgnoreCase(identifier)).findFirst().orElse(null);
      }
   }

   public void registerCrateType(CrateType crateType, boolean createFile) throws Exception {
      if (this.getCachedCrateTypeOrNull(crateType.getIdentifier()) == null) {
         this.types.add(this.createCache(crateType, crateType.getIdentifier()));
         if (createFile) {
            this.saveCrateType(crateType);
         }

         crateType.markRegistered();
      }
   }

   public CrateType getTypeByStorageScopeId(String scopeId) {
      if (scopeId != null && !scopeId.trim().isEmpty()) {
         synchronized (this.types) {
            return this.types
               .stream()
               .map(CachedCrateType::getType)
               .filter(type -> type instanceof ItemStorageScope)
               .filter(type -> scopeId.equalsIgnoreCase(type.getStorageScopeId()))
               .findFirst()
               .orElse(null);
         }
      } else {
         return null;
      }
   }

   @Deprecated
   private CachedCrateType createCache(CrateType crateType, String fileName) throws ErrorException {
      ConfigurationWorker configurationWorker = new ConfigurationWorker(this.plugin, crateType, "crate.yml", "crates/" + fileName + ".yml");
      configurationWorker.registerAdapter(new EngineDataConfigAdapter());
      configurationWorker.registerAdapter(new BaseItemAdapter());
      configurationWorker.registerAdapter(new OpeningAnimationConfigAdapter());
      return new CachedCrateType(configurationWorker, crateType);
   }

   public void unregisterCrateType(CrateType crateType, boolean eraseFile) {
      CachedCrateType cache = this.getCachedCrateTypeOrNull(crateType.getIdentifier());
      if (cache != null) {
         crateType.unmarkRegistered();
         this.types.remove(cache);

         for (Crate crate : new ArrayList<>(this.crates.values())) {
            if (crate.getType().equals(crateType)) {
               crate.unload(true);
               this.crates.remove(CrateLocation.from(crate.getLocation()), crate);
            }
         }

         this.locationsConfiguration.getLocations().remove(crateType.getIdentifier());
         if (eraseFile) {
            cache.getConfigurationWorker().getConfigurationFile().delete();
         }
      }
   }

   public void createCrate(Crate crate, boolean updateBlock) throws Exception {
      Validate.isTrue(Bukkit.isPrimaryThread(), "This function should be called in the main thread", new Object[0]);
      CrateLocation location = CrateLocation.from(crate.getLocation());
      Crate previous = this.crates.remove(location);
      if (previous != null) {
         previous.unload(false);
         this.removeCrateFromConfiguration(previous, false);
      }

      crate.load(updateBlock);
      this.crates.put(location, crate);
      this.addCrateFromConfiguration(crate, true);
   }

   public void destroyCrate(Crate crate) throws Exception {
      Validate.isTrue(Bukkit.isPrimaryThread(), "This function should be called in the main thread", new Object[0]);
      crate.unload(true);
      this.crates.remove(CrateLocation.from(crate.getLocation()), crate);
      this.removeCrateFromConfiguration(crate, true);
   }

   private void addCrateFromConfiguration(Crate crate, boolean save) throws Exception {
      String typeIdentifier = crate.getType().getIdentifier();
      List<CrateLocation> locations = this.locationsConfiguration.getLocations().get(typeIdentifier);
      CrateLocation location = this.toConfiguration(crate.getLocation(), crate.getFacingDirection());
      if (locations == null) {
         locations = Lists.newArrayList(new CrateLocation[]{location});
      } else if (!locations.contains(location)) {
         locations.add(location);
      }

      this.locationsConfiguration.getLocations().put(typeIdentifier, locations);
      if (save) {
         this.locationsWorker.queueSave();
      }
   }

   private void removeCrateFromConfiguration(Crate crate, boolean save) throws Exception {
      String typeIdentifier = crate.getType().getIdentifier();
      List<CrateLocation> locations = this.locationsConfiguration.getLocations().get(typeIdentifier);
      if (locations != null) {
         locations.remove(this.toConfiguration(crate.getLocation(), crate.getFacingDirection()));
      }

      if (save) {
         this.locationsWorker.queueSave();
      }
   }

   public CrateLocation toConfiguration(Location location, FacingDirection face) {
      return new CrateLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), face);
   }

   public void reloadCratesByType(CrateType type) {
      try {
         for (Crate crate : this.getCratesByType(type)) {
            crate.reload();
         }
      } catch (Exception e) {
         Crates.getLogger().error("Error reloading crates by type", e);
      }
   }

   public void recreateCrates() {
      for (Crate crate : this.crates.values()) {
         crate.unload(false);
      }

      this.loadCratesFromFile();
   }

   private synchronized void loadTypesFromFile() throws Exception {
      this.types.clear();
      File cratesFolder = new File(this.plugin.getDataFolder(), "crates");
      if (cratesFolder.exists() && cratesFolder.listFiles().length != 0) {
         boolean limitCrateTypes = this.plugin.getEdition() == Edition.LITE;
         int loadedCount = 0;

         for (File file : Objects.requireNonNull(cratesFolder.listFiles())) {
            if (file.isFile() && FileUtil.getExtensionWithoutName(file.getName()).equalsIgnoreCase("yml")) {
               String name = FileUtil.getNameWithoutExtension(file.getName());
               if (limitCrateTypes) {
                  if (++loadedCount > 5) {
                     Bukkit.getConsoleSender()
                        .sendMessage(ChatColor.RED + "Crate \"" + name + "\" was deleted. Maximum amount (5) allowed exceeded for the lite version.");
                     file.delete();
                     continue;
                  }
               }

               CrateType crateType = new CrateType();
               CachedCrateType cachedCrateType = this.createCache(crateType, name);
               cachedCrateType.getConfigurationWorker().load();
               crateType.markRegistered();
               if (this.isStorageScopeIdDuplicated(crateType)) {
                  crateType.regenerateStorageScopeId();
                  this.filesWorker.queueSave(cachedCrateType.getConfigurationWorker());
               }

               this.types.add(cachedCrateType);
            }
         }
      } else {
         cratesFolder.mkdirs();
         CrateType example1 = CrateFacade.buildDefault("example1");
         example1.markRegistered();
         example1.setDisplayName("&eExample #1");
         example1.setPermission("phoenixcrates.crate.example1");
         example1.setLinkedKeysIds(Sets.newHashSet(new String[]{"example1"}));
         example1.setGuaranteedRewardType(GuaranteedRewardType.DISABLED);
         example1.setIdleEffects(
            Lists.newArrayList(
               new IdleEffect[]{
                  new IdleEffect(IdleEffectType.DEFAULT, new IdleEffectData(ParticleEffect.REDSTONE, Color.RED, 0.0, 0.0, 0.0, 1.0, 0.1, 2)),
                  new IdleEffect(IdleEffectType.DEFAULT, new IdleEffectData(ParticleEffect.FLAME, Color.WHITE, 0.0, 0.0, 0.0, 2.0, 0.1, 2)),
                  new IdleEffect(IdleEffectType.STAR, new IdleEffectData(ParticleEffect.SMOKE_NORMAL, Color.WHITE, 0.0, 0.0, 0.0, 0.0, 0.1, 2))
               }
            )
         );
         example1.setOpeningAnimations(
            new OpeningAnimationWrapper(
               new OpeningPhase(AnimationsRegistry.getInstance().getPhaseById(PhaseType.PRE_OPENING, "KEY_OPENER").get(), new KeybasedOpeningPhaseData()),
               new OpeningPhase(
                  AnimationsRegistry.getInstance().getPhaseById(PhaseType.OPENING, "SWIRL").get(), new ColoredOpeningPhaseData(Color.decode("#404040"))
               ),
               new OpeningPhase(
                  AnimationsRegistry.getInstance().getPhaseById(PhaseType.DISPLAYING, "DEFAULT").get(), new ColoredOpeningPhaseData(Color.decode("#404040"))
               )
            )
         );
         example1.setRegisteredRewards(Lists.newArrayList(new CustomReward[]{RewardFacade.buildDefault()}));
         this.registerCrateType(example1, true);
         CrateType example2 = CrateFacade.buildDefault("example2");
         example2.markRegistered();
         example2.setDisplayName(Ui.hex("&#CCFF00", "&e") + "Example #2");
         example2.setPermission("phoenixcrates.crate.example2");
         example2.setLinkedKeysIds(Sets.newHashSet(new String[]{"example2"}));
         example2.setGuaranteedRewardType(GuaranteedRewardType.DISABLED);
         example2.setIdleEffects(
            Lists.newArrayList(
               new IdleEffect[]{
                  new IdleEffect(IdleEffectType.DEFAULT, new IdleEffectData(ParticleEffect.REDSTONE, Color.WHITE, 0.0, 0.0, 0.0, 1.0, 0.1, 2)),
                  new IdleEffect(IdleEffectType.DEFAULT, new IdleEffectData(ParticleEffect.CLOUD, Color.WHITE, 0.0, 0.0, 0.0, 0.0, 0.1, 2)),
                  new IdleEffect(IdleEffectType.STAR, new IdleEffectData(ParticleEffect.SMOKE_NORMAL, Color.WHITE, 0.0, 3.0, 0.0, 0.0, 0.0, 1))
               }
            )
         );
         example2.setOpeningAnimations(
            new OpeningAnimationWrapper(
               new OpeningPhase(AnimationsRegistry.getInstance().getPhaseById(PhaseType.PRE_OPENING, "FIRE").get(), new EmptyOpeningPhaseData()),
               new OpeningPhase(AnimationsRegistry.getInstance().getPhaseById(PhaseType.OPENING, "FIRE").get(), new EmptyOpeningPhaseData()),
               new OpeningPhase(
                  AnimationsRegistry.getInstance().getPhaseById(PhaseType.DISPLAYING, "FIRE_SPIRAL").get(),
                  new ColoredOpeningPhaseData(Color.decode("#404040"))
               )
            )
         );
         example2.setRegisteredRewards(Lists.newArrayList(new CustomReward[]{RewardFacade.buildDefault()}));
         this.registerCrateType(example2, true);
         CrateType example3 = CrateFacade.buildDefault("example3");
         example3.markRegistered();
         example3.setDisplayName("&eExample #3");
         example3.setPermission("phoenixcrates.crate.example3");
         example3.setLinkedKeysIds(Sets.newHashSet(new String[]{"example3"}));
         example3.setGuaranteedRewardType(GuaranteedRewardType.DISABLED);
         example3.setIdleEffects(
            Lists.newArrayList(
               new IdleEffect[]{
                  new IdleEffect(IdleEffectType.DEFAULT, new IdleEffectData(ParticleEffect.REDSTONE, Color.WHITE, 0.0, 0.0, 0.0, 1.0, 0.1, 2)),
                  new IdleEffect(IdleEffectType.DEFAULT, new IdleEffectData(ParticleEffect.CLOUD, Color.WHITE, 0.0, 0.0, 0.0, 0.0, 0.1, 2)),
                  new IdleEffect(IdleEffectType.STAR, new IdleEffectData(ParticleEffect.SMOKE_NORMAL, Color.WHITE, 0.0, 3.0, 0.0, 0.0, 0.0, 1))
               }
            )
         );
         example3.setOpeningAnimations(
            new OpeningAnimationWrapper(
               new OpeningPhase(AnimationsRegistry.getInstance().getPhaseById(PhaseType.PRE_OPENING, "DEFAULT").get(), new EmptyOpeningPhaseData()),
               new OpeningPhase(
                  AnimationsRegistry.getInstance().getPhaseById(PhaseType.OPENING, "SWIRL").get(), new ColoredOpeningPhaseData(Color.decode("#404040"))
               ),
               new OpeningPhase(AnimationsRegistry.getInstance().getPhaseById(PhaseType.DISPLAYING, "HELIX").get(), new EmptyOpeningPhaseData())
            )
         );
         example3.setRegisteredRewards(Lists.newArrayList(new CustomReward[]{RewardFacade.buildDefault()}));
         this.registerCrateType(example3, true);
         CrateType example4 = CrateFacade.buildDefault("example4");
         example4.markRegistered();
         example4.setDisplayName(Ui.hex("&#CCFF00", "&e") + "Example #4");
         example4.setPermission("phoenixcrates.crate.example4");
         example4.setLinkedKeysIds(Sets.newHashSet(new String[]{"example4"}));
         example4.setGuaranteedRewardType(GuaranteedRewardType.DISABLED);
         example4.setIdleEffects(
            Lists.newArrayList(
               new IdleEffect[]{
                  new IdleEffect(IdleEffectType.SQUARE, new IdleEffectData(ParticleEffect.REDSTONE, Color.BLUE, 0.0, 3.0, 0.0, 0.0, 0.1, 2)),
                  new IdleEffect(IdleEffectType.DEFAULT, new IdleEffectData(ParticleEffect.CRIT, Color.WHITE, 0.0, 0.0, 0.0, 1.0, 0.1, 2)),
                  new IdleEffect(IdleEffectType.SQUARE, new IdleEffectData(ParticleEffect.DRIP_WATER, Color.WHITE, 0.0, 3.0, 0.0, 0.0, 0.0, 2))
               }
            )
         );
         example4.setOpeningAnimations(
            new OpeningAnimationWrapper(
               new OpeningPhase(AnimationsRegistry.getInstance().getPhaseById(PhaseType.PRE_OPENING, "CRACK").get(), new EmptyOpeningPhaseData()),
               new OpeningPhase(AnimationsRegistry.getInstance().getPhaseById(PhaseType.OPENING, "BALL").get(), new EmptyOpeningPhaseData()),
               new OpeningPhase(
                  AnimationsRegistry.getInstance().getPhaseById(PhaseType.DISPLAYING, "DEFAULT").get(), new ColoredOpeningPhaseData(Color.decode("#404040"))
               )
            )
         );
         example4.setRegisteredRewards(Lists.newArrayList(new CustomReward[]{RewardFacade.buildDefault()}));
         this.registerCrateType(example4, true);
      }
   }

   private boolean isStorageScopeIdDuplicated(CrateType crateType) {
      String scopeId = crateType.getStorageScopeId();
      return scopeId != null && !scopeId.trim().isEmpty()
         ? this.types.stream().map(CachedCrateType::getType).anyMatch(type -> scopeId.equalsIgnoreCase(type.getStorageScopeId()))
         : false;
   }

   private synchronized void loadCratesFromFile() {
      this.crates.clear();

      for (Entry<String, List<CrateLocation>> entry : this.locationsConfiguration.getLocations().entrySet()) {
         CrateType type = this.getTypeByIdentifier(entry.getKey());
         if (type == null) {
            Crates.getLogger().warn("There was an attempt to add crates with an invalid type: \"" + entry.getKey() + "\"!");
         } else if (type.isEnabled()) {
            try {
               for (CrateLocation serializedLocation : entry.getValue()) {
                  World world = Bukkit.getWorld(serializedLocation.getWorld());
                  if (world == null) {
                     Crates.getLogger().warn("There was an attempt to add a crate with an invalid world: \"" + serializedLocation.getWorld() + "\"!");
                  } else if (!this.crates.containsKey(serializedLocation)) {
                     Location location = new Location(world, serializedLocation.getX(), serializedLocation.getY(), serializedLocation.getZ());
                     Crate crate = new Crate(this, type, location, serializedLocation.getFace());
                     crate.load(false);
                     this.crates.put(serializedLocation, crate);
                  }
               }
            } catch (Exception ex) {
               Crates.getLogger().error("Error loading crate", ex);
            }
         }
      }
   }

   @Generated
   public PhoenixCrates getPlugin() {
      return this.plugin;
   }

   @Generated
   public Map<CrateLocation, Crate> getCrates() {
      return this.crates;
   }

   @Generated
   public Set<CrateOpeningSession> getOpeningSessions() {
      return this.openingSessions;
   }

   @Generated
   public LocationTaskBus getLocationTaskBus() {
      return this.locationTaskBus;
   }

   @Generated
   public GlobalRewardStatsTracker getGlobalRewardStatsTracker() {
      return this.globalRewardStatsTracker;
   }

   @Generated
   public AsyncQueueMultiWorker getFilesWorker() {
      return this.filesWorker;
   }

   @Generated
   public CratesLocationsConfiguration getLocationsConfiguration() {
      return this.locationsConfiguration;
   }

   @Generated
   public AsyncQueueConfigurationWorker getLocationsWorker() {
      return this.locationsWorker;
   }

   @Generated
   private Set<CachedCrateType> getTypes() {
      return this.types;
   }
}
