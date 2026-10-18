package id.seria.bossevent.leaderboard;

import id.seria.bossevent.SeriaBossEventPlugin;
import id.seria.bossevent.config.EventConfig;
import id.seria.bossevent.display.CenteredMessageBuilder;
import id.seria.bossevent.tracker.FightTracker;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class LeaderboardService {

    private final SeriaBossEventPlugin plugin;

    public LeaderboardService(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
    }

    public FightResult buildFightResult(EventConfig eventConfig, FightTracker tracker, @Nullable Player killer) {
        String eventName = tracker.getEventName();
        String mobName = tracker.getMobName();
        String displayName = eventConfig.getDisplayName();
        if (displayName == null || displayName.isBlank()) {
            displayName = mobName.replace('_', ' ').toUpperCase();
        }

        // 1. Calculate total pool
        double sum = 0.0;
        for (double dmg : tracker.getDamage().values()) {
            if (dmg > 0) sum += dmg;
        }
        double totalPool = sum > 0 ? sum : 1.0;

        // 2. Sort entries descending
        List<Map.Entry<UUID, Double>> sorted = new ArrayList<>(tracker.getDamage().entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // 3. Build rankings with percentage
        List<PlayerContribution> rankings = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<UUID, Double> entry : sorted) {
            UUID uuid = entry.getKey();
            double dmg = entry.getValue();
            double rawPercent = (dmg / totalPool) * 100.0;
            double percent = Math.round(rawPercent * 10.0) / 10.0; // 1 decimal place

            String name = tracker.getNames().get(uuid);
            if (name == null) {
                name = plugin.getPlayerInfoService().getPlayerName(uuid);
            }
            Component prefix = tracker.getPrefixes().get(uuid);
            if (prefix == null) {
                prefix = plugin.getPlayerInfoService().getPrefix(uuid);
            }

            rankings.add(new PlayerContribution(rank++, uuid, name, prefix, dmg, percent));
        }

        // 4. Killer / Last Hit
        UUID killerUuid = null;
        String killerName = "Unknown";
        Component killerPrefix = Component.empty();

        if (killer != null) {
            killerUuid = killer.getUniqueId();
            killerName = killer.getName();
            killerPrefix = plugin.getPlayerInfoService().getPrefix(killer);
        } else if (tracker.getLastDamager() != null) {
            killerUuid = tracker.getLastDamager();
            killerName = tracker.getNames().getOrDefault(killerUuid, plugin.getPlayerInfoService().getPlayerName(killerUuid));
            killerPrefix = tracker.getPrefixes().getOrDefault(killerUuid, plugin.getPlayerInfoService().getPrefix(killerUuid));
        }

        // 5. Fight duration format mm:ss
        long seconds = 0;
        if (tracker.getStartTime() != null) {
            seconds = Math.max(0, Duration.between(tracker.getStartTime(), Instant.now()).getSeconds());
        }
        long mm = seconds / 60;
        long ss = seconds % 60;
        String durationFormatted = String.format("%02d:%02d", mm, ss);

        return new FightResult(
                eventName,
                mobName,
                displayName,
                killerUuid,
                killerName,
                killerPrefix,
                rankings,
                durationFormatted,
                totalPool,
                Instant.now()
        );
    }

    public String formatNumber(double number, String localeTag) {
        try {
            Locale locale = Locale.forLanguageTag(localeTag != null ? localeTag : "id-ID");
            NumberFormat nf = NumberFormat.getInstance(locale);
            nf.setMaximumFractionDigits(0);
            return nf.format(Math.round(number));
        } catch (Exception e) {
            return String.valueOf(Math.round(number));
        }
    }

    public void displayLeaderboard(EventConfig eventConfig, FightResult result, Map<UUID, List<String>> playerRewardMessages) {
        boolean centered = eventConfig.isDisplayCentered();
        int topLimit = eventConfig.getDisplayTop();
        int minPlayersForPercent = eventConfig.getMinPlayersForPercent();
        String locale = eventConfig.getNumberFormatLocale();
        String scope = eventConfig.getDisplayScope();

        // Prepare Top entries
        List<PlayerContribution> allRanks = result.getRankings();
        int totalPlayers = allRanks.size();

        // Header component
        Map<String, String> headerMap = Map.of("boss", result.getDisplayName());
        String rawHeader = plugin.getMessageService().resolveMessageTemplate(eventConfig, "leaderboard.header", "        <gold><bold>{boss} TELAH DIKALAHKAN!</bold></gold>");
        Component headerComp = plugin.getMessageService().parse(rawHeader, headerMap);
        if (centered) headerComp = CenteredMessageBuilder.center(headerComp);

        // Last hit component
        Component lastHitComp = Component.empty();
        if (eventConfig.isShowLastHit()) {
            String rawLastHit = plugin.getMessageService().resolveMessageTemplate(eventConfig, "leaderboard.last-hit", "   <gray>Last hit dilancarkan oleh</gray> {killer_prefix}{killer_name}");
            Map<String, String> lastHitMap = Map.of(
                    "killer_name", result.getKillerName(),
                    "killer_prefix", ""
            );
            Component prefixPart = result.getKillerPrefix() != null && !result.getKillerPrefix().equals(Component.empty())
                    ? result.getKillerPrefix().append(Component.text(" "))
                    : Component.empty();

            Component parsedLastHit = plugin.getMessageService().parse(rawLastHit, lastHitMap);
            // Insert prefix before killer name if needed
            Component fullLastHit = Component.text("   ")
                    .append(plugin.getMessageService().parse("<gray>Last hit dilancarkan oleh </gray>"))
                    .append(prefixPart)
                    .append(Component.text(result.getKillerName()));

            lastHitComp = centered ? CenteredMessageBuilder.center(fullLastHit) : fullLastHit;
        }

        // Duration component
        Component durationComp = Component.empty();
        if (eventConfig.isShowFightDuration()) {
            String rawDur = plugin.getMessageService().resolveMessageTemplate(eventConfig, "leaderboard.fight-duration", "   <white>Lama pertarungan:</white> <aqua>{duration}</aqua>");
            durationComp = plugin.getMessageService().parse(rawDur, Map.of("duration", result.getDurationFormatted()));
            if (centered) durationComp = CenteredMessageBuilder.center(durationComp);
        }

        // Send to online players based on scope
        Collection<? extends Player> recipients = getRecipients(scope, eventConfig);

        for (Player viewer : recipients) {
            UUID viewerUuid = viewer.getUniqueId();
            viewer.sendMessage(Component.empty());
            viewer.sendMessage(headerComp);
            viewer.sendMessage(Component.empty());

            if (eventConfig.isShowLastHit()) {
                viewer.sendMessage(lastHitComp);
                viewer.sendMessage(Component.empty());
            }

            // Top rankings
            int count = Math.min(topLimit, allRanks.size());
            for (int i = 0; i < count; i++) {
                PlayerContribution pc = allRanks.get(i);
                boolean isViewer = pc.getUuid().equals(viewerUuid);

                String rankColor = getRankColor(pc.getRank(), isViewer);
                String percentText = totalPlayers >= minPlayersForPercent
                        ? " <gray>(" + pc.getPercent() + "%)</gray>"
                        : "";

                Component prefixComp = pc.getPrefix() != null && !pc.getPrefix().equals(Component.empty())
                        ? pc.getPrefix().append(Component.text(" "))
                        : Component.empty();

                Component rankLine = Component.text("   ")
                        .append(plugin.getMessageService().parse(rankColor + "#" + pc.getRank() + ". "))
                        .append(prefixComp)
                        .append(Component.text(pc.getName()))
                        .append(plugin.getMessageService().parse(" <gray>-</gray> "))
                        .append(Component.text(formatNumber(pc.getDamage(), locale)))
                        .append(Component.text(" damage"))
                        .append(plugin.getMessageService().parse(percentText));

                if (centered) {
                    rankLine = CenteredMessageBuilder.center(rankLine);
                }
                viewer.sendMessage(rankLine);
            }

            viewer.sendMessage(Component.empty());

            // Personal contribution line
            if (eventConfig.isShowPersonal()) {
                PlayerContribution viewerContribution = null;
                for (PlayerContribution pc : allRanks) {
                    if (pc.getUuid().equals(viewerUuid)) {
                        viewerContribution = pc;
                        break;
                    }
                }

                if (viewerContribution != null) {
                    Map<String, String> personalMap = Map.of(
                            "damage", formatNumber(viewerContribution.getDamage(), locale),
                            "percent", String.valueOf(viewerContribution.getPercent()),
                            "rank", String.valueOf(viewerContribution.getRank()),
                            "total_players", String.valueOf(totalPlayers)
                    );
                    String rawPersonal = plugin.getMessageService().resolveMessageTemplate(
                            eventConfig,
                            "leaderboard.personal-contribution",
                            "   <white>Kontribusi Damage:</white> <yellow>{damage}</yellow> <gray>({percent}%)</gray> <gold>(#{rank}/{total_players})</gold>"
                    );
                    Component personalComp = plugin.getMessageService().parse(rawPersonal, personalMap);
                    if (centered) personalComp = CenteredMessageBuilder.center(personalComp);
                    viewer.sendMessage(personalComp);
                }
            }

            if (eventConfig.isShowFightDuration()) {
                viewer.sendMessage(durationComp);
            }

            // Reward messages for viewer
            if (playerRewardMessages != null) {
                List<String> rewardMsgs = playerRewardMessages.get(viewerUuid);
                if (rewardMsgs != null && !rewardMsgs.isEmpty()) {
                    for (String msg : rewardMsgs) {
                        Component rComp = plugin.getMessageService().parse("   " + msg);
                        if (centered) rComp = CenteredMessageBuilder.center(rComp);
                        viewer.sendMessage(rComp);
                    }
                }
            }

            viewer.sendMessage(Component.empty());
        }

        // Also output clean version to console
        Bukkit.getConsoleSender().sendMessage(headerComp);
        if (eventConfig.isShowLastHit()) Bukkit.getConsoleSender().sendMessage(lastHitComp);
        for (int i = 0; i < Math.min(topLimit, allRanks.size()); i++) {
            PlayerContribution pc = allRanks.get(i);
            String percentText = totalPlayers >= minPlayersForPercent ? " (" + pc.getPercent() + "%)" : "";
            Bukkit.getConsoleSender().sendMessage(Component.text("   #" + pc.getRank() + ". " + pc.getName() + " - " + formatNumber(pc.getDamage(), locale) + " damage" + percentText));
        }
    }

    private String getRankColor(int rank, boolean isSelf) {
        if (isSelf) return "<green>";
        return switch (rank) {
            case 1 -> "<yellow>";
            case 2 -> "<gold>";
            case 3 -> "<red>";
            default -> "<white>";
        };
    }

    private Collection<? extends Player> getRecipients(String scope, EventConfig eventConfig) {
        if ("world".equalsIgnoreCase(scope) && !eventConfig.getBosses().isEmpty()) {
            String worldName = eventConfig.getBosses().get(0).getWorld();
            List<Player> inWorld = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().getName().equalsIgnoreCase(worldName)) {
                    inWorld.add(p);
                }
            }
            return inWorld;
        }
        return Bukkit.getOnlinePlayers();
    }
}
