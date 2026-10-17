package id.seria.armorset.command;

import id.seria.armorset.SeriaArmorSetPlugin;
import id.seria.armorset.config.ArmorSetConfig;
import id.seria.armorset.hook.MMOItemsHook;
import id.seria.armorset.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SetTelepathyCommand implements CommandExecutor, TabCompleter {

    private final SeriaArmorSetPlugin plugin;

    public SetTelepathyCommand(SeriaArmorSetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ArmorSetConfig config = plugin.getConfiguration();

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("reload")) {
            if (!sender.hasPermission("settelepathy.reload") && !sender.hasPermission("seriaarmorset.reload")) {
                TextUtil.sendPrefixMessage(sender, config.getMessageNoPermission());
                return true;
            }
            plugin.reload();
            TextUtil.sendPrefixMessage(sender, config.getMessageReload());
            return true;
        }

        if (sub.equals("status")) {
            if (!sender.hasPermission("settelepathy.status") && !sender.hasPermission("settelepathy.reload")
                    && !sender.hasPermission("seriaarmorset.reload")) {
                TextUtil.sendPrefixMessage(sender, config.getMessageNoPermission());
                return true;
            }

            Player target;
            if (args.length >= 2) {
                target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    TextUtil.sendPrefixMessage(sender, config.getMessagePlayerNotFound().replace("{player}", args[1]));
                    return true;
                }
            } else if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage("Usage from console: /settelepathy status <player>");
                return true;
            }

            MMOItemsHook hook = plugin.getMmoItemsHook();
            String activeSet = hook.getActiveSet(target);

            if (activeSet != null) {
                int pieces = hook.getPieceCount(target, activeSet);
                int required = config.getRequiredPieces(activeSet);

                String msg = config.getMessageStatusWearing()
                        .replace("{player}", target.getName())
                        .replace("{set}", activeSet)
                        .replace("{pieces}", String.valueOf(pieces))
                        .replace("{required}", String.valueOf(required));
                TextUtil.sendPrefixMessage(sender, msg);
            } else {
                String msg = config.getMessageStatusNotWearing()
                        .replace("{player}", target.getName());
                TextUtil.sendPrefixMessage(sender, msg);
            }
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        ArmorSetConfig config = plugin.getConfiguration();
        for (String line : config.getMessageHelp()) {
            TextUtil.sendMessage(sender, line.replace("{version}", plugin.getDescription().getVersion()));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String prefix = args[0].toLowerCase(Locale.ROOT);
            for (String sub : List.of("reload", "status", "help")) {
                if (sub.startsWith(prefix)) {
                    completions.add(sub);
                }
            }
            return completions;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("status")) {
            List<String> names = new ArrayList<>();
            String prefix = args[1].toLowerCase(Locale.ROOT);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    names.add(player.getName());
                }
            }
            return names;
        }
        return Collections.emptyList();
    }
}