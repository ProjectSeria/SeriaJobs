package id.seria.bossevent.event;

import id.seria.bossevent.SeriaBossEventPlugin;
import id.seria.bossevent.config.EventConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CountdownTask {

    private final SeriaBossEventPlugin plugin;
    private final BossEvent bossEvent;
    private final EventConfig eventConfig;
    private final Runnable onComplete;

    private BukkitTask task;
    private Instant startTime;
    private int totalDurationSeconds;
    private final Set<Integer> notifiedSeconds = new HashSet<>();
    private volatile int currentSecondsRemaining;

    public CountdownTask(SeriaBossEventPlugin plugin, BossEvent bossEvent, EventConfig eventConfig, Runnable onComplete) {
        this.plugin = plugin;
        this.bossEvent = bossEvent;
        this.eventConfig = eventConfig;
        this.onComplete = onComplete;
        this.totalDurationSeconds = eventConfig.getCountdownDuration();
        this.currentSecondsRemaining = totalDurationSeconds;
    }

    public synchronized void start() {
        if (task != null) return;

        this.startTime = Instant.now();
        long periodMs = eventConfig.getTaskPeriod();
        long periodTicks = Math.max(1L, periodMs / 50L);

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (startTime == null) return;

                long elapsedMillis = java.time.Duration.between(startTime, Instant.now()).toMillis();
                long elapsedSeconds = elapsedMillis / 1000L;
                int remaining = (int) Math.max(0, totalDurationSeconds - elapsedSeconds);
                currentSecondsRemaining = remaining;

                // Check notify-at triggers
                if (eventConfig.getNotifyAt().contains(remaining) && !notifiedSeconds.contains(remaining)) {
                    notifiedSeconds.add(remaining);
                    triggerNotification(remaining);
                }

                if (remaining <= 0) {
                    cancel();
                    task = null;
                    onComplete.run();
                }
            }
        }.runTaskTimer(plugin, 0L, periodTicks);
    }

    private void triggerNotification(int seconds) {
        Map<String, String> rep = Map.of(
                "time", String.valueOf(seconds),
                "boss", eventConfig.getDisplayName(),
                "event", eventConfig.getName()
        );

        String msgTemplate = plugin.getMessageService().resolveMessageTemplate(
                eventConfig,
                "countdown.notify",
                "<sc:prefix> <yellow>Boss <gold><bold>{boss}</bold></gold> akan muncul dalam <aqua>{time}</aqua> detik!</yellow>"
        );
        Component msg = plugin.getMessageService().parse(msgTemplate, rep);
        plugin.getMessageService().broadcast(msg, eventConfig.getDisplayScope(), eventConfig);

        String soundName = plugin.getConfigManager().getMessage("countdown.sound", "BLOCK_NOTE_BLOCK_PLING");
        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getMessageService().playSound(p, soundName);
        }

        if (seconds <= 5) {
            String titleStr = plugin.getConfigManager().getMessage("countdown.title.title", "<gold><bold>{boss}</bold></gold>");
            String subStr = plugin.getConfigManager().getMessage("countdown.title.subtitle", "<yellow>Muncul dalam <aqua>{time}</aqua> detik</yellow>");
            Component titleComp = plugin.getMessageService().parse(titleStr, rep);
            Component subComp = plugin.getMessageService().parse(subStr, rep);
            for (Player p : Bukkit.getOnlinePlayers()) {
                plugin.getMessageService().showTitle(p, titleComp, subComp, 2, 16, 2);
            }
        }
    }

    public synchronized void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public int getCurrentSecondsRemaining() {
        return currentSecondsRemaining;
    }
}
