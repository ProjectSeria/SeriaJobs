package id.seria.bossevent.event;

import id.seria.bossevent.SeriaBossEventPlugin;
import id.seria.bossevent.api.event.BossEventSpawnEvent;
import id.seria.bossevent.config.BossConfig;
import id.seria.bossevent.config.EventConfig;
import id.seria.bossevent.tracker.FightTracker;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BossSpawner {

    private final SeriaBossEventPlugin plugin;

    // Track chunk tickets added by event: eventName -> list of chunks
    private final Map<String, Set<ChunkPos>> heldChunkTickets = new ConcurrentHashMap<>();

    // Track spawned active mobs: eventName -> list of ActiveMob/UUIDs
    private final Map<String, List<UUID>> spawnedBosses = new ConcurrentHashMap<>();

    public BossSpawner(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean validateBosses(EventConfig eventConfig) {
        if (eventConfig.getBosses().isEmpty()) {
            plugin.getLogger().warning("[SeriaBossEvent] Event " + eventConfig.getName() + " has no bosses configured.");
            return false;
        }

        for (BossConfig bc : eventConfig.getBosses()) {
            World world = Bukkit.getWorld(bc.getWorld());
            if (world == null) {
                plugin.getLogger().warning("[SeriaBossEvent] World '" + bc.getWorld() + "' for event " + eventConfig.getName() + " is not loaded!");
                return false;
            }

            Optional<MythicMob> mythicMob = MythicBukkit.inst().getMobManager().getMythicMob(bc.getMob());
            if (mythicMob.isEmpty()) {
                plugin.getLogger().warning("[SeriaBossEvent] MythicMob '" + bc.getMob() + "' for event " + eventConfig.getName() + " is not registered in MythicMobs!");
                return false;
            }
        }
        return true;
    }

    public CompletableFuture<List<FightTracker>> spawnAllBosses(EventConfig eventConfig) {
        CompletableFuture<List<FightTracker>> future = new CompletableFuture<>();
        List<BossConfig> configs = eventConfig.getBosses();
        List<FightTracker> trackers = Collections.synchronizedList(new ArrayList<>());
        List<UUID> spawnedUuids = Collections.synchronizedList(new ArrayList<>());

        List<CompletableFuture<Void>> chunkLoads = new ArrayList<>();

        for (BossConfig bc : configs) {
            World world = Bukkit.getWorld(bc.getWorld());
            if (world == null) continue;

            Location loc = new Location(world, bc.getX(), bc.getY(), bc.getZ(), bc.getYaw(), bc.getPitch());
            int chunkX = loc.getBlockX() >> 4;
            int chunkZ = loc.getBlockZ() >> 4;

            // Load chunk asynchronously
            CompletableFuture<Chunk> chunkFuture = world.getChunkAtAsync(chunkX, chunkZ);
            chunkLoads.add(chunkFuture.thenAccept(chunk -> {
                // Keep chunk loaded with plugin ticket
                world.addPluginChunkTicket(chunkX, chunkZ, plugin);
                heldChunkTickets.computeIfAbsent(eventConfig.getName().toLowerCase(), k -> ConcurrentHashMap.newKeySet())
                        .add(new ChunkPos(world.getName(), chunkX, chunkZ));
            }));
        }

        CompletableFuture.allOf(chunkLoads.toArray(new CompletableFuture[0])).thenRun(() -> {
            // Spawn on main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    for (BossConfig bc : configs) {
                        World world = Bukkit.getWorld(bc.getWorld());
                        if (world == null) continue;

                        Location loc = new Location(world, bc.getX(), bc.getY(), bc.getZ(), bc.getYaw(), bc.getPitch());
                        ActiveMob activeMob = MythicBukkit.inst().getMobManager().spawnMob(bc.getMob(), loc);

                        if (activeMob != null && activeMob.getEntity() != null) {
                            Entity entity = activeMob.getEntity().getBukkitEntity();
                            UUID uuid = entity.getUniqueId();
                            spawnedUuids.add(uuid);

                            double hp = 100.0;
                            if (entity instanceof LivingEntity living) {
                                hp = living.getHealth();
                            }

                            FightTracker tracker = new FightTracker(uuid, bc.getMob(), eventConfig.getName(), hp);
                            trackers.add(tracker);
                            plugin.getHpPollTask().registerTracker(tracker);

                            // Fire custom Bukkit event
                            Bukkit.getPluginManager().callEvent(new BossEventSpawnEvent(eventConfig.getName(), entity, bc.getMob()));
                        } else {
                            plugin.getLogger().severe("[SeriaBossEvent] Failed to spawn MythicMob '" + bc.getMob() + "' at " + loc);
                        }
                    }

                    spawnedBosses.put(eventConfig.getName().toLowerCase(), spawnedUuids);
                    future.complete(trackers);
                } catch (Exception e) {
                    plugin.getLogger().severe("[SeriaBossEvent] Error spawning bosses for " + eventConfig.getName() + ": " + e.getMessage());
                    future.completeExceptionally(e);
                }
            });
        });

        return future;
    }

    public void cleanup(String eventName) {
        String key = eventName.toLowerCase();

        // 1. Despawn any remaining bosses
        List<UUID> uuids = spawnedBosses.remove(key);
        if (uuids != null) {
            for (UUID uuid : uuids) {
                plugin.getHpPollTask().unregisterTracker(uuid);
                ActiveMob activeMob = MythicBukkit.inst().getMobManager().getActiveMob(uuid).orElse(null);
                if (activeMob != null) {
                    try {
                        activeMob.despawn();
                    } catch (Throwable ignored) {}
                }
                Entity entity = Bukkit.getEntity(uuid);
                if (entity != null && entity.isValid() && !entity.isDead()) {
                    entity.remove();
                }
            }
        }

        // 2. Release chunk tickets
        Set<ChunkPos> tickets = heldChunkTickets.remove(key);
        if (tickets != null) {
            for (ChunkPos cp : tickets) {
                World w = Bukkit.getWorld(cp.world());
                if (w != null) {
                    try {
                        w.removePluginChunkTicket(cp.x(), cp.z(), plugin);
                    } catch (Throwable ignored) {}
                }
            }
        }
    }

    public record ChunkPos(String world, int x, int z) {}
}
