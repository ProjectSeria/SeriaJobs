package id.seria.counter.command;

import id.seria.counter.SeriaCounterPlugin;
import id.seria.counter.model.Counter;
import id.seria.counter.model.CounterStep;
import id.seria.counter.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CounterCommand implements CommandExecutor, TabCompleter {

    private final SeriaCounterPlugin plugin;

    public CounterCommand(SeriaCounterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase().trim();

        switch (sub) {
            case "reload":
                handleReload(sender);
                break;
            case "add":
                handleAdd(sender, args);
                break;
            case "set":
                handleSet(sender, args);
                break;
            case "get":
                handleGet(sender, args);
                break;
            case "reset":
                handleReset(sender, args);
                break;
            default:
                sendHelp(sender, label);
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("seria.counter.reload")) {
            sendMsg(sender, "no-permission", Map.of());
            return;
        }

        plugin.getConfigManager().loadConfig();
        plugin.getStorageService().saveDataSync();
        plugin.getCounterLoader().loadCounters();
        plugin.getStorageService().loadData();

        int count = plugin.getCounterLoader().getCounters().size();
        sendMsg(sender, "reload-success", Map.of("count", String.valueOf(count)));
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("seria.counter.add")) {
            sendMsg(sender, "no-permission", Map.of());
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(TextUtil.parse("<sc:error-color>Penggunaan: /counter add <id> <amount></sc:error-color>"));
            return;
        }

        String id = args[1].toLowerCase().trim();
        Counter counter = plugin.getCounterLoader().getCounter(id);
        if (counter == null) {
            sendMsg(sender, "counter-not-found", Map.of("id", id));
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            sendMsg(sender, "invalid-amount", Map.of());
            return;
        }

        if (amount < 0 && !plugin.getConfigManager().isAllowNegative()) {
            sendMsg(sender, "negative-not-allowed", Map.of());
            return;
        }

        boolean success = counter.add(amount, sender);
        if (success) {
            CounterStep step = counter.getCurrentStep();
            Map<String, String> rep = new HashMap<>();
            rep.put("id", counter.getId());
            rep.put("name", counter.getDisplayName());
            rep.put("amount", String.valueOf(amount));
            rep.put("value", String.valueOf(counter.getCurrentValue()));
            rep.put("goal", String.valueOf(counter.getGoal()));
            rep.put("step", String.valueOf(step != null ? step.getStepNumber() : 1));
            rep.put("step_name", step != null ? step.getName() : "");
            sendMsg(sender, "add-success", rep);
        }
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("seria.counter.set")) {
            sendMsg(sender, "no-permission", Map.of());
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(TextUtil.parse("<sc:error-color>Penggunaan: /counter set <id> <value></sc:error-color>"));
            return;
        }

        String id = args[1].toLowerCase().trim();
        Counter counter = plugin.getCounterLoader().getCounter(id);
        if (counter == null) {
            sendMsg(sender, "counter-not-found", Map.of("id", id));
            return;
        }

        long value;
        try {
            value = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            sendMsg(sender, "invalid-amount", Map.of());
            return;
        }

        if (value < 0 && !plugin.getConfigManager().isAllowNegative()) {
            sendMsg(sender, "negative-not-allowed", Map.of());
            return;
        }

        boolean success = counter.set(value, sender);
        if (success) {
            Map<String, String> rep = new HashMap<>();
            rep.put("id", counter.getId());
            rep.put("name", counter.getDisplayName());
            rep.put("value", String.valueOf(counter.getCurrentValue()));
            sendMsg(sender, "set-success", rep);
        }
    }

    private void handleGet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("seria.counter.get")) {
            sendMsg(sender, "no-permission", Map.of());
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(TextUtil.parse("<sc:error-color>Penggunaan: /counter get <id></sc:error-color>"));
            return;
        }

        String id = args[1].toLowerCase().trim();
        Counter counter = plugin.getCounterLoader().getCounter(id);
        if (counter == null) {
            sendMsg(sender, "counter-not-found", Map.of("id", id));
            return;
        }

        CounterStep step = counter.getCurrentStep();
        Map<String, String> rep = new HashMap<>();
        rep.put("id", counter.getId());
        rep.put("name", counter.getDisplayName());
        rep.put("value", String.valueOf(counter.getCurrentValue()));
        rep.put("goal", String.valueOf(counter.getGoal()));
        rep.put("remaining", String.valueOf(counter.getRemaining()));
        rep.put("progress", String.format("%.1f", counter.getProgress()));
        rep.put("step", String.valueOf(step != null ? step.getStepNumber() : 1));
        rep.put("step_name", step != null ? step.getName() : "");
        rep.put("total_steps", String.valueOf(counter.getTotalSteps()));

        List<String> lines = plugin.getConfigManager().getMessageList("get-info");
        if (!lines.isEmpty()) {
            TextUtil.sendList(sender, lines, rep);
        } else {
            sender.sendMessage(TextUtil.parse("<gray>Counter <aqua>{name}</aqua> ({id}): <yellow>{value}</yellow>/<gold>{goal}</gold> (Step {step}: {step_name})</gray>", rep));
        }
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("seria.counter.reset")) {
            sendMsg(sender, "no-permission", Map.of());
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(TextUtil.parse("<sc:error-color>Penggunaan: /counter reset <id></sc:error-color>"));
            return;
        }

        String id = args[1].toLowerCase().trim();
        Counter counter = plugin.getCounterLoader().getCounter(id);
        if (counter == null) {
            sendMsg(sender, "counter-not-found", Map.of("id", id));
            return;
        }

        counter.reset();
        Map<String, String> rep = Map.of("id", counter.getId(), "name", counter.getDisplayName());
        sendMsg(sender, "reset-success", rep);
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(TextUtil.parse("<sc:prefix> <gray>Bantuan Perintah <aqua>SeriaCounter</aqua>:</gray>"));
        if (sender.hasPermission("seria.counter.add")) {
            sender.sendMessage(TextUtil.parse(" <dark_gray>•</dark_gray> <yellow>/" + label + " add <id> <amount></yellow> <gray>- Tambah nilai counter</gray>"));
        }
        if (sender.hasPermission("seria.counter.set")) {
            sender.sendMessage(TextUtil.parse(" <dark_gray>•</dark_gray> <yellow>/" + label + " set <id> <value></yellow> <gray>- Atur nilai counter</gray>"));
        }
        if (sender.hasPermission("seria.counter.get")) {
            sender.sendMessage(TextUtil.parse(" <dark_gray>•</dark_gray> <yellow>/" + label + " get <id></yellow> <gray>- Lihat status dan progress counter</gray>"));
        }
        if (sender.hasPermission("seria.counter.reset")) {
            sender.sendMessage(TextUtil.parse(" <dark_gray>•</dark_gray> <yellow>/" + label + " reset <id></yellow> <gray>- Reset counter ke 0 dan step 1</gray>"));
        }
        if (sender.hasPermission("seria.counter.reload")) {
            sender.sendMessage(TextUtil.parse(" <dark_gray>•</dark_gray> <yellow>/" + label + " reload</yellow> <gray>- Memuat ulang konfigurasi</gray>"));
        }
    }

    private void sendMsg(CommandSender sender, String key, Map<String, String> rep) {
        String raw = plugin.getConfigManager().getMessage(key, "");
        if (raw.isBlank()) return;
        TextUtil.sendMessage(sender, plugin.getConfigManager().getPrefix(), raw, rep);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            if (sender.hasPermission("seria.counter.add")) subs.add("add");
            if (sender.hasPermission("seria.counter.set")) subs.add("set");
            if (sender.hasPermission("seria.counter.get")) subs.add("get");
            if (sender.hasPermission("seria.counter.reset")) subs.add("reset");
            if (sender.hasPermission("seria.counter.reload")) subs.add("reload");
            return filterPrefix(subs, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("add") || sub.equals("set") || sub.equals("get") || sub.equals("reset")) {
                return filterPrefix(plugin.getCounterLoader().getCounters().keySet(), args[1]);
            }
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("add")) {
                return List.of("1", "10", "100", "500", "1000");
            } else if (sub.equals("set")) {
                return List.of("0", "100", "500", "1000");
            }
        }

        return Collections.emptyList();
    }

    private List<String> filterPrefix(Collection<String> items, String prefix) {
        String lower = prefix.toLowerCase();
        List<String> res = new ArrayList<>();
        for (String item : items) {
            if (item.toLowerCase().startsWith(lower)) {
                res.add(item);
            }
        }
        Collections.sort(res);
        return res;
    }
}
