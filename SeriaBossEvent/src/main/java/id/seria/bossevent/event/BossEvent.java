package id.seria.bossevent.event;

import id.seria.bossevent.SeriaBossEventPlugin;
import id.seria.bossevent.api.event.BossEventEndEvent;
import id.seria.bossevent.api.event.BossEventStartEvent;
import id.seria.bossevent.api.event.EndReason;
import id.seria.bossevent.config.EventConfig;
import id.seria.bossevent.leaderboard.FightResult;
import id.seria.bossevent.tracker.FightTracker;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BossEvent {

    private final SeriaBossEventPlugin plugin;
    private final EventConfig config;

    private volatile BossEventState state = BossEventState.IDLE;
    private CountdownTask countdownTask;
    private BukkitTask delayTask;
    private BukkitTask durationTask;

    private final List<FightTracker> trackers = Collections.synchronizedList(new ArrayList<>());
    private final Set<UUID> completedTrackers = ConcurrentHashMap.newKeySet();

    public BossEvent(SeriaBossEventPlugin plugin, EventConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public synchronized boolean start() {
        if (state != BossEventState.IDLE) {
            return false;
        }

        // Validate bosses and worlds
        if (!plugin.getBossSpawner().validateBosses(config)) {
            return false;
        }

        trackers.clear();
        completedTrackers.clear();

        // Fire Bukkit Event
        Bukkit.getPluginManager().callEvent(new BossEventStartEvent(config.getName()));

        int delaySec = config.getCountdownDelay();
        if (delaySec > 0) {
            state = BossEventState.WAITING;
            delayTask = Bukkit.getScheduler().runTaskLater(plugin, this::startCountdown, delaySec * 20L);
        } else {
            startCountdown();
        }

        return true;
    }

    private synchronized void startCountdown() {
        if (state == BossEventState.ENDED) return;

        if (!config.isCountdownEnabled()) {
            spawnBosses();
            return;
        }

        state = BossEventState.COUNTDOWN;
        countdownTask = new CountdownTask(plugin, this, config, this::spawnBosses);
        countdownTask.start();
    }

    private synchronized void spawnBosses() {
        if (state == BossEventState.ENDED) return;

        state = BossEventState.ACTIVE;
        plugin.getBossSpawner().spawnAllBosses(config).thenAccept(spawnedTrackers -> {
            trackers.addAll(spawnedTrackers);

            // Broadcast spawned message
            Map<String, String> rep = Map.of(
                    "boss", config.getDisplayName(),
                    "world", config.getBosses().isEmpty() ? "" : config.getBosses().get(0).getWorld(),
                    "event", config.getName()
            );

            String spawnedMsg = plugin.getMessageService().resolveMessageTemplate(
                    config,
                    "boss.spawned",
                    "<sc:prefix> <red><bold>PERHATIAN!</bold> Boss <gold><bold>{boss}</bold></gold> telah muncul di <yellow>{world}</yellow>!</red>"
            );
            Component msg = plugin.getMessageService().parse(spawnedMsg, rep);
            plugin.getMessageService().broadcast(msg, config.getDisplayScope(), config);

            String sound = plugin.getConfigManager().getMessage("boss.spawned-sound", "ENTITY_WITHER_SPAWN");
            for (Player p : Bukkit.getOnlinePlayers()) {
                plugin.getMessageService().playSound(p, sound);
            }

            // Start duration countdown if > 0
            if (config.getDuration() > 0) {
                durationTask = Bukkit.getScheduler().runTaskLater(plugin, this::handleDurationExpired, config.getDuration() * 20L);
            }
        });
    }

    public synchronized void onBossKilled(FightTracker tracker, @Nullable Player killer) {
        if (state != BossEventState.ACTIVE) return;

        completedTrackers.add(tracker.getBossUuid());

        // Check if all bosses are completed
        boolean allDone = true;
        for (FightTracker t : trackers) {
            if (!completedTrackers.contains(t.getBossUuid())) {
                allDone = false;
                break;
            }
        }

        if (allDone) {
            endEvent(EndReason.KILLED, tracker, killer);
        }
    }

    public synchronized void onBossDespawnFailed(FightTracker tracker) {
        if (state != BossEventState.ACTIVE) return;
        endEvent(EndReason.FAILED, tracker, null);
    }

    private synchronized void handleDurationExpired() {
        if (state != BossEventState.ACTIVE) return;

        if (config.isShutdownEventAfterDuration()) {
            Map<String, String> rep = Map.of(
                    "boss", config.getDisplayName(),
                    "event", config.getName()
            );
            String timeoutMsg = plugin.getMessageService().resolveMessageTemplate(
                    config,
                    "event.timeout",
                    "<sc:prefix> <red>Waktu pertarungan melawan <gold>{boss}</gold> telah habis!</red>"
            );
            plugin.getMessageService().broadcast(plugin.getMessageService().parse(timeoutMsg, rep), config.getDisplayScope(), config);

            FightTracker lastTracker = trackers.isEmpty() ? null : trackers.get(0);
            endEvent(EndReason.TIMEOUT, lastTracker, null);
        }
    }

    public synchronized void stop() {
        if (state == BossEventState.IDLE || state == BossEventState.ENDED) return;

        Map<String, String> rep = Map.of(
                "boss", config.getDisplayName(),
                "event", config.getName()
        );
        String stopMsg = plugin.getMessageService().resolveMessageTemplate(
                config,
                "event.stopped",
                "<sc:prefix> <yellow>Event boss <gold>{boss}</gold> telah dihentikan oleh admin.</yellow>"
        );
        plugin.getMessageService().broadcast(plugin.getMessageService().parse(stopMsg, rep), config.getDisplayScope(), config);

        endEvent(EndReason.STOPPED, trackers.isEmpty() ? null : trackers.get(0), null);
    }

    private synchronized void endEvent(EndReason reason, @Nullable FightTracker tracker, @Nullable Player killer) {
        state = BossEventState.ENDED;

        FightResult result = null;
        if (tracker != null) {
            double totalPool = 0.0;
            for (double dmg : tracker.getDamage().values()) {
                if (dmg > 0) totalPool += dmg;
            }

            boolean isUncontested = (totalPool <= 0);

            if (reason == EndReason.KILLED && !isUncontested) {
                // Build result
                result = plugin.getLeaderboardService().buildFightResult(config, tracker, killer);

                // Rewards
                Map<UUID, List<String>> rewardMsgs = plugin.getRewardService().processRewards(config, result);

                // Leaderboard broadcast
                if (config.isLeaderboardEnabled()) {
                    plugin.getLeaderboardService().displayLeaderboard(config, result, rewardMsgs);
                }

                // Discord embed
                if (config.isDiscordEnabled() && plugin.getDiscordHook() != null) {
                    plugin.getDiscordHook().sendLeaderboardEmbed(config, result);
                }

                // Save result
                plugin.getResultStore().saveFightResult(result, config.isGlobalScore());
            } else if (isUncontested && reason == EndReason.KILLED) {
                String uncontestedMsg = plugin.getMessageService().resolveMessageTemplate(
                        config,
                        "event.uncontested",
                        "<sc:prefix> <gray>Boss <gold>{boss}</gold> gugur tanpa perlawanan. Leaderboard dan reward dibatalkan.</gray>"
                );
                plugin.getMessageService().broadcast(plugin.getMessageService().parse(uncontestedMsg, Map.of("boss", config.getDisplayName())), config.getDisplayScope(), config);
            } else if (config.isShowOnFail()) {
                result = plugin.getLeaderboardService().buildFightResult(config, tracker, killer);
                plugin.getLeaderboardService().displayLeaderboard(config, result, Collections.emptyMap());
            }
        }

        // Fire BossEventEndEvent
        Bukkit.getPluginManager().callEvent(new BossEventEndEvent(config.getName(), reason, result));

        cleanup();
    }

    public synchronized void cleanup() {
        if (delayTask != null) {
            delayTask.cancel();
            delayTask = null;
        }
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        if (durationTask != null) {
            durationTask.cancel();
            durationTask = null;
        }

        plugin.getBossSpawner().cleanup(config.getName());
        trackers.clear();
        completedTrackers.clear();
        state = BossEventState.IDLE;
    }

    public BossEventState getState() {
        return state;
    }

    public EventConfig getConfig() {
        return config;
    }

    public int getCountdownRemaining() {
        return countdownTask != null ? countdownTask.getCurrentSecondsRemaining() : 0;
    }

    public List<FightTracker> getTrackers() {
        return trackers;
    }
}
