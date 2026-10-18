package id.seria.bossevent.hook;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import id.seria.bossevent.SeriaBossEventPlugin;
import id.seria.bossevent.config.EventConfig;
import id.seria.bossevent.leaderboard.FightResult;
import id.seria.bossevent.leaderboard.PlayerContribution;
import org.bukkit.Bukkit;

import java.awt.Color;
import java.util.List;

public class DiscordHook {

    private final SeriaBossEventPlugin plugin;
    private boolean warnedChannelNotFound = false;

    public DiscordHook(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
    }

    public static boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("DiscordSRV");
    }

    public void sendLeaderboardEmbed(EventConfig eventConfig, FightResult result) {
        if (!isAvailable()) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String channelName = eventConfig.getDiscordChannel();
                TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channelName);

                if (channel == null) {
                    if (!warnedChannelNotFound) {
                        plugin.getLogger().warning("[SeriaBossEvent] Discord channel '" + channelName + "' not found in DiscordSRV configuration.");
                        warnedChannelNotFound = true;
                    }
                    return;
                }

                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("⚔ " + stripFormat(result.getDisplayName()) + " TELAH DIKALAHKAN!");

                Color embedColor;
                try {
                    embedColor = Color.decode(eventConfig.getDiscordColor());
                } catch (Exception e) {
                    embedColor = new Color(255, 170, 0); // Gold
                }
                eb.setColor(embedColor);

                StringBuilder desc = new StringBuilder();
                desc.append("Last hit dilancarkan oleh: **").append(stripFormat(result.getKillerName())).append("**\n\n");
                desc.append("🏆 **Top Kontribusi:**\n");

                List<PlayerContribution> rankings = result.getRankings();
                int maxEntries = Math.min(eventConfig.getDiscordMaxEntries(), rankings.size());
                int minPlayersForPercent = eventConfig.getMinPlayersForPercent();

                for (int i = 0; i < maxEntries; i++) {
                    PlayerContribution pc = rankings.get(i);
                    String percentStr = rankings.size() >= minPlayersForPercent ? " (" + pc.getPercent() + "%)" : "";
                    desc.append("#").append(pc.getRank()).append(". ")
                            .append(stripFormat(pc.getName())).append(" - ")
                            .append(plugin.getLeaderboardService().formatNumber(pc.getDamage(), eventConfig.getNumberFormatLocale()))
                            .append(" damage").append(percentStr).append("\n");
                }

                eb.setDescription(desc.toString());
                eb.setFooter("⏱ Lama pertarungan: " + result.getDurationFormatted());

                channel.sendMessageEmbeds(eb.build()).queue(
                        success -> {},
                        error -> plugin.getLogger().warning("[SeriaBossEvent] Failed to send Discord embed: " + error.getMessage())
                );
            } catch (Throwable t) {
                plugin.getLogger().warning("[SeriaBossEvent] Error dispatching DiscordSRV embed: " + t.getMessage());
            }
        });
    }

    private String stripFormat(String input) {
        if (input == null) return "";
        // Strip legacy color codes and hex codes
        String stripped = input.replaceAll("(?i)&[0-9a-fk-orx]", "")
                .replaceAll("(?i)§[0-9a-fk-orx]", "")
                .replaceAll("<[^>]*>", ""); // strip MiniMessage tags
        return stripped.trim();
    }
}
