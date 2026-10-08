package id.seria.mmoae.command;

import id.seria.mmoae.SeriaMMOItemsAEPlugin;
import id.seria.mmoae.hook.AEHook;
import id.seria.mmoae.hook.MMOItemsHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MMOAECommand implements CommandExecutor, TabCompleter {

    private final SeriaMMOItemsAEPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MMOAECommand(SeriaMMOItemsAEPlugin plugin) {
        this.plugin = plugin;
    }

    private void sendMessage(CommandSender sender, String msg) {
        if (msg == null || msg.isEmpty()) return;
        String prefix = plugin.getConfig().getString("messages.prefix", "<gold><bold>MMO-AE</bold></gold> <dark_gray>»</dark_gray> ");
        Component component = miniMessage.deserialize(prefix + msg);
        sender.sendMessage(component);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("seriammoae.admin")) {
            sendMessage(sender, plugin.getConfig().getString("messages.no-permission", "<red>Kamu tidak memiliki izin untuk perintah ini.</red>"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendMessage(sender, "<yellow>Daftar perintah <gold>SeriaMMOItemsAE</gold>:</yellow>");
            sender.sendMessage(miniMessage.deserialize("<gold>» </gold><yellow>/" + label + " reload</yellow> <gray>- Memuat ulang konfigurasi plugin.</gray>"));
            sender.sendMessage(miniMessage.deserialize("<gold>» </gold><yellow>/" + label + " apply</yellow> <gray>- Terapkan enchant AE pada item di tangan.</gray>"));
            sender.sendMessage(miniMessage.deserialize("<gold>» </gold><yellow>/" + label + " info</yellow> <gray>- Cek status integrasi MMOItems & AE.</gray>"));
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            plugin.reloadPluginConfig();
            sendMessage(sender, plugin.getConfig().getString("messages.reload", "<green>Konfigurasi berhasil dimuat ulang!</green>"));
            return true;
        }

        if (sub.equals("info")) {
            sendMessage(sender, "<gold><bold>Status Integrasi Bridge:</bold></gold>");
            sender.sendMessage(miniMessage.deserialize("<gray>MMOItems Hook: " + (MMOItemsHook.isAvailable() ? "<green>Tersedia</green>" : "<red>Tidak Terdeteksi</red>")));
            sender.sendMessage(miniMessage.deserialize("<gray>AdvancedEnchantments Hook: " + (AEHook.isAvailable() ? "<green>Tersedia</green>" : "<red>Tidak Terdeteksi</red>")));
            return true;
        }

        if (sub.equals("apply")) {
            if (!(sender instanceof Player player)) {
                sendMessage(sender, "<red>Perintah ini hanya dapat dijalankan oleh pemain.</red>");
                return true;
            }

            if (!AEHook.isAvailable()) {
                sendMessage(player, plugin.getConfig().getString("messages.ae-not-loaded", "<red>AdvancedEnchantments belum siap.</red>"));
                return true;
            }

            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType().isAir()) {
                sendMessage(player, plugin.getConfig().getString("messages.no-item", "<red>Pegang item di tangan utama!</red>"));
                return true;
            }

            if (!MMOItemsHook.isMMOItem(hand)) {
                sendMessage(player, plugin.getConfig().getString("messages.not-mmoitem", "<red>Item bukan item MMOItems!</red>"));
                return true;
            }

            List<String> enchants = MMOItemsHook.getAEEnchants(hand);
            if (enchants.isEmpty()) {
                sendMessage(player, plugin.getConfig().getString("messages.no-enchants", "<yellow>Item tidak memiliki konfigurasi 'ae-enchants'.</yellow>"));
                return true;
            }

            if (plugin.getBridgeService().isFullyEnchanted(hand)) {
                sendMessage(player, plugin.getConfig().getString("messages.already-applied", "<yellow>Semua enchant AE sudah terpasang.</yellow>"));
                return true;
            }

            ItemStack updated = plugin.getBridgeService().processItem(hand);
            if (updated != null) {
                player.getInventory().setItemInMainHand(updated);
                sendMessage(player, plugin.getConfig().getString("messages.applied-success", "<green>Berhasil menerapkan enchant AE pada item!</green>"));
            }
            return true;
        }

        sendMessage(sender, "<red>Argumen tidak dikenali. Gunakan /" + label + " help</red>");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("reload", "apply", "info", "help");
            List<String> result = new ArrayList<>();
            for (String s : subcommands) {
                if (s.startsWith(args[0].toLowerCase())) {
                    result.add(s);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }
}
