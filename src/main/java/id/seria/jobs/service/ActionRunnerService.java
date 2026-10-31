package id.seria.jobs.service;

import id.seria.jobs.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Parser dan dispatcher untuk daftar reward action berformat click-action string.
 */
public class ActionRunnerService {

    /**
     * Parsing token placeholder dan eksekusi action berdasarkan prefix:
     * - [console] <command>
     * - [player] <command>
     * - [message] <text>
     * - [sound] <sound_key>
     */
    public void executeAction(@NotNull Player player, @Nullable String rawAction, int reputationAmount) {
        if (rawAction == null || rawAction.isBlank()) return;

        String formatted = rawAction
                .replace("%player%", player.getName())
                .replace("{player}", player.getName())
                .replace("%amount%", String.valueOf(reputationAmount))
                .replace("{amount}", String.valueOf(reputationAmount));

        String trimmed = formatted.trim();

        if (trimmed.regionMatches(true, 0, "[console]", 0, 9)) {
            String cmd = trimmed.substring(9).trim();
            if (!cmd.isEmpty()) {
                String lower = cmd.toLowerCase();
                EconomyService economy = id.seria.jobs.SeriaJobs.getInstance().getEconomyService();
                if (lower.startsWith("reputasi give ") || lower.startsWith("reputation give ")) {
                    String[] parts = cmd.split("\\s+");
                    double amt = parts.length > 3 ? parseDoubleSafe(parts[3], reputationAmount) : reputationAmount;
                    if (economy.isCurrencyEngineAvailable() && economy.deposit(player, "reputasi", amt)) {
                        return;
                    }
                } else if (lower.startsWith("serium give ")) {
                    String[] parts = cmd.split("\\s+");
                    double amt = parts.length > 3 ? parseDoubleSafe(parts[3], 1.0) : 1.0;
                    if (economy.isCurrencyEngineAvailable() && economy.deposit(player, "serium", amt)) {
                        return;
                    }
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        } else if (trimmed.regionMatches(true, 0, "[player]", 0, 8)) {
            String cmd = trimmed.substring(8).trim();
            if (!cmd.isEmpty()) {
                player.performCommand(cmd);
            }
        } else if (trimmed.regionMatches(true, 0, "[message]", 0, 9)) {
            String msg = trimmed.substring(9).trim();
            if (!msg.isEmpty()) {
                player.sendMessage(TextUtil.parseForPlayer(player, msg));
            }
        } else if (trimmed.regionMatches(true, 0, "[sound]", 0, 7)) {
            String soundKey = trimmed.substring(7).trim();
            if (!soundKey.isEmpty()) {
                playSound(player, soundKey);
            }
        } else {
            // Default fallback: if it starts with /, dispatch as player command; otherwise console
            if (trimmed.startsWith("/")) {
                player.performCommand(trimmed.substring(1));
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), trimmed);
            }
        }
    }

    /**
     * Mengiterasi seluruh list command/action template untuk slot order yang disetor.
     */
    public void executeAllActions(@NotNull Player player, @Nullable List<String> actions, int reputationAmount) {
        if (actions == null || actions.isEmpty()) return;
        for (String action : actions) {
            executeAction(player, action, reputationAmount);
        }
    }

    private void playSound(@NotNull Player player, @NotNull String soundKey) {
        try {
            Sound sound = Sound.valueOf(soundKey.toUpperCase().replace(".", "_"));
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            try {
                player.playSound(player.getLocation(), soundKey.toLowerCase(), 1.0f, 1.0f);
            } catch (Throwable ignored) {}
        }
    }

    private double parseDoubleSafe(String s, double fallback) {
        try {
            return Double.parseDouble(s);
        } catch (Throwable t) {
            return fallback;
        }
    }
}
