package id.seria.bossevent.command;

import id.seria.bossevent.SeriaBossEventPlugin;
import id.seria.bossevent.config.EventConfig;
import id.seria.bossevent.event.BossEvent;
import id.seria.bossevent.event.BossEventState;
import id.seria.bossevent.tracker.FightTracker;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SbeCommand implements CommandExecutor, TabCompleter {

    private final SeriaBossEventPlugin plugin;

    public SbeCommand(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "start" -> handleStart(sender, args);
            case "stop" -> handleStop(sender, args);
            case "list" -> handleList(sender);
            case "status" -> handleStatus(sender, args);
            case "reload" -> handleReload(sender);
            default -> sendHelp(sender, label);
        }

        return true;
    }

    private void handleStart(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "seriabossevent.admin.start")) return;

        if (args.length < 2) {
            plugin.getMessageService().sendMessageWithPrefix(sender, "<red>Penggunaan: /sbe start <event></red>", null);
            return;
        }

        String eventName = args[1].toLowerCase();
        BossEvent event = plugin.getEventManager().getEvent(eventName);

        if (event == null) {
            String notFound = plugin.getConfigManager().getMessage("error.event-not-found", "<sc:prefix> <red>Event '{event}' tidak ditemukan!</red>");
            plugin.getMessageService().sendMessage(sender, notFound, Map.of("event", eventName));
            return;
        }

        if (event.getState() != BossEventState.IDLE) {
            String alreadyRunning = plugin.getConfigManager().getMessage("error.event-already-running", "<sc:prefix> <red>Event '{event}' sedang berjalan!</red>");
            plugin.getMessageService().sendMessage(sender, alreadyRunning, Map.of("event", eventName));
            return;
        }

        boolean success = event.start();
        if (success) {
            String started = plugin.getConfigManager().getMessage("command.started", "<sc:prefix> <green>Event '{event}' berhasil dimulai.</green>");
            plugin.getMessageService().sendMessage(sender, started, Map.of("event", eventName));
        } else {
            plugin.getMessageService().sendMessageWithPrefix(sender, "<red>Gagal memulai event '{event}'. Periksa validasi world atau MythicMob di console.</red>", Map.of("event", eventName));
        }
    }

    private void handleStop(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "seriabossevent.admin.stop")) return;

        if (args.length < 2) {
            plugin.getMessageService().sendMessageWithPrefix(sender, "<red>Penggunaan: /sbe stop <event></red>", null);
            return;
        }

        String eventName = args[1].toLowerCase();
        BossEvent event = plugin.getEventManager().getEvent(eventName);

        if (event == null) {
            String notFound = plugin.getConfigManager().getMessage("error.event-not-found", "<sc:prefix> <red>Event '{event}' tidak ditemukan!</red>");
            plugin.getMessageService().sendMessage(sender, notFound, Map.of("event", eventName));
            return;
        }

        if (event.getState() == BossEventState.IDLE) {
            String notRunning = plugin.getConfigManager().getMessage("error.event-not-running", "<sc:prefix> <red>Event '{event}' sedang tidak aktif.</red>");
            plugin.getMessageService().sendMessage(sender, notRunning, Map.of("event", eventName));
            return;
        }

        event.stop();
        String stopped = plugin.getConfigManager().getMessage("command.stopped", "<sc:prefix> <yellow>Event '{event}' berhasil dihentikan.</yellow>");
        plugin.getMessageService().sendMessage(sender, stopped, Map.of("event", eventName));
    }

    private void handleList(CommandSender sender) {
        if (!hasPermission(sender, "seriabossevent.admin.list")) return;

        Collection<BossEvent> events = plugin.getEventManager().getAllEvents();
        plugin.getMessageService().sendMessageWithPrefix(sender, "<yellow>Daftar Boss Events (<gold>" + events.size() + "</gold>):</yellow>", null);

        for (BossEvent e : events) {
            String stateColor = switch (e.getState()) {
                case IDLE -> "<gray>";
                case WAITING, COUNTDOWN -> "<yellow>";
                case ACTIVE -> "<green>";
                case ENDED -> "<red>";
            };

            String line = "<gray> - </gray><white>" + e.getConfig().getName() + "</white> " +
                    "<dark_gray>(</dark_gray><gold>" + e.getConfig().getDisplayName() + "</gold><dark_gray>)</dark_gray>: " +
                    stateColor + e.getState().name() + "</state>";

            plugin.getMessageService().sendMessage(sender, line, null);
        }
    }

    private void handleStatus(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "seriabossevent.admin.status")) return;

        if (args.length < 2) {
            plugin.getMessageService().sendMessageWithPrefix(sender, "<red>Penggunaan: /sbe status <event></red>", null);
            return;
        }

        String eventName = args[1].toLowerCase();
        BossEvent event = plugin.getEventManager().getEvent(eventName);

        if (event == null) {
            String notFound = plugin.getConfigManager().getMessage("error.event-not-found", "<sc:prefix> <red>Event '{event}' tidak ditemukan!</red>");
            plugin.getMessageService().sendMessage(sender, notFound, Map.of("event", eventName));
            return;
        }

        EventConfig cfg = event.getConfig();
        plugin.getMessageService().sendMessageWithPrefix(sender, "<yellow>Status Event: <gold>" + cfg.getName() + "</gold></yellow>", null);
        sender.sendMessage(plugin.getMessageService().parse("<gray> - Display Name: <gold>" + cfg.getDisplayName() + "</gold></gray>"));
        sender.sendMessage(plugin.getMessageService().parse("<gray> - State: <aqua>" + event.getState().name() + "</aqua></gray>"));

        if (event.getState() == BossEventState.COUNTDOWN) {
            sender.sendMessage(plugin.getMessageService().parse("<gray> - Sisa Countdown: <yellow>" + event.getCountdownRemaining() + "s</yellow></gray>"));
        } else if (event.getState() == BossEventState.ACTIVE) {
            sender.sendMessage(plugin.getMessageService().parse("<gray> - Active Bosses: <green>" + event.getTrackers().size() + "</green></gray>"));
            for (FightTracker t : event.getTrackers()) {
                sender.sendMessage(plugin.getMessageService().parse("<gray>   * " + t.getMobName() + " (HP: " + Math.round(t.getLastHealth()) + ")</gray>"));
            }
        }
    }

    private void handleReload(CommandSender sender) {
        if (!hasPermission(sender, "seriabossevent.admin.reload")) return;

        plugin.getConfigManager().loadConfig();
        plugin.getEventConfigLoader().loadEvents();
        plugin.getEventManager().reload();

        String reloaded = plugin.getConfigManager().getMessage("command.reloaded", "<sc:prefix> <green>Konfigurasi SeriaBossEvent berhasil dimuat ulang!</green>");
        plugin.getMessageService().sendMessage(sender, reloaded, null);
    }

    private boolean hasPermission(CommandSender sender, String perm) {
        if (sender.hasPermission("seriabossevent.admin") || sender.hasPermission(perm)) {
            return true;
        }
        String noPerm = plugin.getConfigManager().getMessage("error.no-permission", "<sc:prefix> <red>Kamu tidak memiliki izin untuk menjalankan perintah ini!</red>");
        plugin.getMessageService().sendMessage(sender, noPerm, null);
        return false;
    }

    private void sendHelp(CommandSender sender, String label) {
        plugin.getMessageService().sendMessageWithPrefix(sender, "<gold><bold>SeriaBossEvent Commands:</bold></gold>", null);
        sender.sendMessage(plugin.getMessageService().parse("<yellow>/" + label + " start <event></yellow> <gray>- Mulai event boss</gray>"));
        sender.sendMessage(plugin.getMessageService().parse("<yellow>/" + label + " stop <event></yellow> <gray>- Hentikan event & despawn boss</gray>"));
        sender.sendMessage(plugin.getMessageService().parse("<yellow>/" + label + " list</yellow> <gray>- Lihat daftar event & status</gray>"));
        sender.sendMessage(plugin.getMessageService().parse("<yellow>/" + label + " status <event></yellow> <gray>- Lihat detail status event</gray>"));
        sender.sendMessage(plugin.getMessageService().parse("<yellow>/" + label + " reload</yellow> <gray>- Muat ulang config & messages</gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subs = Arrays.asList("start", "stop", "list", "status", "reload");
            return StringUtil.copyPartialMatches(args[0], subs, new ArrayList<>());
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("start") || sub.equals("stop") || sub.equals("status")) {
                List<String> eventNames = new ArrayList<>(plugin.getEventConfigLoader().getEvents().keySet());
                return StringUtil.copyPartialMatches(args[1], eventNames, new ArrayList<>());
            }
        }
        return Collections.emptyList();
    }
}
