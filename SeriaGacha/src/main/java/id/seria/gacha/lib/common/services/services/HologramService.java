package id.seria.gacha.lib.common.services.services;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import id.seria.gacha.lib.common.services.factory.ServicesFactory;
import id.seria.gacha.lib.common.services.services.hologram.Hologram;
import id.seria.gacha.lib.common.services.services.hologram.PacketHologram;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class HologramService implements ServicesFactory.ServiceLifecycle, Listener {
   private final ServerPlugin plugin;
   private final Multimap<ServerPlugin, Hologram> holograms = ArrayListMultimap.create();

   public HologramService(ServerPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public void load() {
      this.plugin.getEventBus().registerListener(this);
      this.plugin.getScheduler().scheduleAsync(this::tick, 10L, 10L);
   }

   private void tick() {
      synchronized (this.holograms) {
         for (Hologram hologram : this.holograms.values()) {
            if (hologram.isSpawned()) {
               hologram.getIgnoredPlayers().removeIf(p -> !p.isOnline());
               hologram.getPacketHolograms().values().removeIf(p -> !p.getPlayer().isOnline());

               for (PacketHologram h : hologram.getPacketHolograms().values()) {
                  if (!hologram.getIgnoredPlayers().contains(h.getPlayer())) {
                     if (h.canViewerSee()) {
                        h.spawn();
                     } else {
                        h.despawn();
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void unload() {
      synchronized (this.holograms) {
         this.holograms.values().forEach(Hologram::despawn);
         this.holograms.clear();
      }

      this.plugin.getEventBus().unregisterListener(this);
   }

   public void reload() throws Exception {
      synchronized (this.holograms) {
         for (Hologram holo : this.holograms.values()) {
            holo.refreshLines();
         }
      }
   }

   public Hologram createHologram(ServerPlugin plugin, Location location) {
      Hologram hologram = new Hologram(plugin);
      hologram.spawn(location);
      this.addHologram(hologram);
      return hologram;
   }

   public void addHologram(Hologram hologram) {
      if (!this.holograms.containsEntry(hologram.getPlugin(), hologram)) {
         synchronized (this.holograms) {
            this.holograms.put(hologram.getPlugin(), hologram);
         }
      }
   }

   public void removeHologram(Hologram hologram) {
      hologram.despawn();
      synchronized (this.holograms) {
         this.holograms.remove(hologram.getPlugin(), hologram);
      }
   }

   public List<Hologram> getHolograms() {
      synchronized (this.holograms) {
         return Collections.unmodifiableList(new ArrayList<>(this.holograms.values()));
      }
   }

   @EventHandler
   protected void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      synchronized (this.holograms) {
         this.holograms.values().stream().filter(h -> h.getLocation().getWorld().equals(player.getWorld()) && h.isSpawned()).forEach(h -> h.show(player));
      }
   }

   @EventHandler
   protected void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      synchronized (this.holograms) {
         for (Hologram hologram : this.holograms.values()) {
            hologram.getPacketHolograms().remove(player.getUniqueId());
         }
      }
   }

   @EventHandler
   protected void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
      Player player = event.getPlayer();
      synchronized (this.holograms) {
         for (Hologram hologram : this.holograms.values()) {
            if (hologram.isSpawned()) {
               World world = hologram.getLocation().getWorld();
               if (world.equals(event.getFrom())) {
                  hologram.hide(player);
               } else if (world.equals(player.getWorld())) {
                  hologram.show(player);
               }
            }
         }
      }
   }

   @EventHandler
   protected void onPluginDisable(PluginDisableEvent event) {
      if (event.getPlugin() instanceof JavaPlugin) {
         JavaPlugin plugin = (JavaPlugin)event.getPlugin();
         synchronized (this.holograms) {
            for (Hologram hologram : this.holograms.removeAll(plugin)) {
               if (hologram.isSpawned()) {
                  hologram.despawn();
               }
            }
         }
      }
   }

   @Generated
   public ServerPlugin getPlugin() {
      return this.plugin;
   }
}
