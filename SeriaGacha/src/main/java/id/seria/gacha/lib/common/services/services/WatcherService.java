package id.seria.gacha.lib.common.services.services;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEquipableEntity;
import id.seria.gacha.lib.common.services.factory.ServicesFactory;
import id.seria.gacha.lib.common.services.services.watcher.WatcherEntry;
import id.seria.gacha.lib.common.utils.objects.SetMap;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WatcherService implements ServicesFactory.ServiceLifecycle, Listener {
   public static final SetMap<UUID, FakeEntity> PLAYERS_LOOKUP_MAP = new SetMap<>();
   private final ServerPlugin plugin;
   private final Multimap<JavaPlugin, WatcherEntry> watchers = ArrayListMultimap.create();
   private int delta;

   public WatcherService(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public void load() {
      this.plugin.getEventBus().registerListener(this);
      this.plugin.getScheduler().scheduleAsync(this::tick, 0L, 0L);
   }

   private void tick() {
      List<FakeEntity> toRemove = Lists.newArrayList();
      this.delta++;
      synchronized (this.watchers) {
         for (WatcherEntry entry : this.watchers.values()) {
            FakeEntity entity = entry.getEntity();
            if (entity.isDead()) {
               toRemove.add(entity);
            } else {
               if (entry.isAutoUpdateAudience() && this.delta % 20 == 0) {
                  entry.refreshAudience(entity.getRenderDistance());
               }

               this.updateEntity(entity);
            }
         }
      }

      synchronized (this.watchers) {
         for (FakeEntity entity : toRemove) {
            entity.setBeingWatched(false);
            entity.prepareDestroyPacket().sendPacket(entity.getAudience());
            this.watchers.remove(entity.getPlugin(), entity);
         }
      }
   }

   @Override
   public void unload() {
      synchronized (this.watchers) {
         for (WatcherEntry entry : this.watchers.values()) {
            FakeEntity entity = entry.getEntity();
            entity.setBeingWatched(false);
            entity.prepareDestroyPacket().sendPacket(entity.getAudience());
         }

         this.watchers.clear();
      }

      this.plugin.getEventBus().unregisterListener(this);
   }

   public void startWatching(FakeEntity entity, boolean autoUpdateAudience) {
      if (entity != null && !entity.isBeingWatched()) {
         entity.setBeingWatched(true);
         WatcherEntry entry = new WatcherEntry(entity, autoUpdateAudience);
         if (autoUpdateAudience) {
            List var4 = entry.refreshAudience(entity.getRenderDistance());
         }

         if (entity.getAudience().size() > 0) {
            entity.prepareBundlePacket().sendPacket(entity.getAudience());
            entity.setMetadataChanged(false);
            entity.setEquipmentChanged(false);
            entity.setPositionChanged(false);
            entity.setPassengerChanged(false);
         }

         synchronized (this.watchers) {
            this.watchers.put(entity.getPlugin(), entry);
         }
      }
   }

   public void stopWatching(FakeEntity entity) {
      if (entity != null) {
         WatcherEntry entry = this.getEntryByEntity(entity);
         if (entity.isBeingWatched()) {
            entity.setBeingWatched(false);
            entity.prepareDestroyPacket().sendPacket(entity.getAudience());
            synchronized (this.watchers) {
               this.watchers.remove(entity.getPlugin(), entry);
            }
         }
      }
   }

   private WatcherEntry getEntryByEntity(FakeEntity entity) {
      synchronized (this.watchers) {
         for (WatcherEntry entry : this.watchers.get(entity.getPlugin())) {
            if (entry.getEntity().equals(entity)) {
               return entry;
            }
         }

         return null;
      }
   }

   private void updateEntity(FakeEntity entity) {
      if (entity.hasMetadataChanged()) {
         entity.prepareMetadataPacket().sendPacket(entity.getAudience());
         entity.setMetadataChanged(false);
      }

      if (entity.hasEquipmentChanged() && entity instanceof FakeEquipableEntity) {
         ((FakeEquipableEntity)entity).prepareEquipmentPacket().sendPacket(entity.getAudience());
         entity.setEquipmentChanged(false);
      }

      if (entity.hasPositionChanged()) {
         entity.prepareTeleportPacket().sendPacket(entity.getAudience());
         entity.setPositionChanged(false);
      }

      if (entity.hasVelocityChanged()) {
         entity.prepareVelocityPacket().sendPacket(entity.getAudience());
         entity.setVelocityChanged(false);
      }

      if (entity.hasPassengerChanged()) {
         entity.prepareSetPassengersPacket().sendPacket(entity.getAudience());
         entity.setPassengerChanged(false);
      }
   }

   @EventHandler
   protected void onPluginDisable(PluginDisableEvent event) {
      if (event.getPlugin() instanceof JavaPlugin) {
         JavaPlugin plugin = (JavaPlugin)event.getPlugin();
         synchronized (this.watchers) {
            for (WatcherEntry entry : this.watchers.removeAll(plugin)) {
               this.stopWatching(entry.getEntity());
            }
         }
      }
   }

   @EventHandler
   protected void onPlayerRespawn(PlayerRespawnEvent event) {
      Player player = event.getPlayer();
      this.plugin.getScheduler().runSync(() -> {
         for (FakeEntity entity : PLAYERS_LOOKUP_MAP.get(player.getUniqueId())) {
            if (entity.getAudience().contains(event.getPlayer())) {
               entity.prepareBundlePacket().sendPacket(player);
            }
         }
      }, 20L);
   }
}
