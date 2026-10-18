package id.seria.bossevent.hook;

import id.seria.bossevent.SeriaBossEventPlugin;
import id.seria.bossevent.event.BossEvent;
import id.seria.bossevent.leaderboard.FightResult;
import id.seria.bossevent.leaderboard.PlayerContribution;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderHook extends PlaceholderExpansion {

    private final SeriaBossEventPlugin plugin;
    private static final Pattern RANK_PATTERN = Pattern.compile("^rank(\\d+)_(.+)_(name|damage)$");

    public PlaceholderHook(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "seriabossevent";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Seria";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String lower = params.toLowerCase();

        // 1. Status: %seriabossevent_status_<event>%
        if (lower.startsWith("status_")) {
            String eventName = lower.substring("status_".length());
            BossEvent event = plugin.getEventManager().getEvent(eventName);
            return event != null ? event.getState().name() : "IDLE";
        }

        // 2. Countdown: %seriabossevent_countdown_<event>%
        if (lower.startsWith("countdown_")) {
            String eventName = lower.substring("countdown_".length());
            BossEvent event = plugin.getEventManager().getEvent(eventName);
            return event != null ? String.valueOf(event.getCountdownRemaining()) : "0";
        }

        // 3. Top raw values:
        // %seriabossevent_top_<event>_name%
        // %seriabossevent_top_<event>_damage%
        // %seriabossevent_top_<event>_percent%
        if (lower.startsWith("top_")) {
            String sub = lower.substring("top_".length());
            if (sub.endsWith("_name")) {
                String eventName = sub.substring(0, sub.length() - "_name".length());
                FightResult res = plugin.getResultStore().getLastResult(eventName);
                if (res != null && !res.getRankings().isEmpty()) {
                    return res.getRankings().get(0).getName();
                }
                return "Belum ada";
            }
            if (sub.endsWith("_damage")) {
                String eventName = sub.substring(0, sub.length() - "_damage".length());
                FightResult res = plugin.getResultStore().getLastResult(eventName);
                if (res != null && !res.getRankings().isEmpty()) {
                    return plugin.getLeaderboardService().formatNumber(res.getRankings().get(0).getDamage(), "id-ID");
                }
                return "0";
            }
            if (sub.endsWith("_percent")) {
                String eventName = sub.substring(0, sub.length() - "_percent".length());
                FightResult res = plugin.getResultStore().getLastResult(eventName);
                if (res != null && !res.getRankings().isEmpty()) {
                    return String.valueOf(res.getRankings().get(0).getPercent());
                }
                return "0";
            }

            // Default %seriabossevent_top_<event>%
            String eventName = sub;
            FightResult res = plugin.getResultStore().getLastResult(eventName);
            if (res != null && !res.getRankings().isEmpty()) {
                PlayerContribution top = res.getRankings().get(0);
                String formattedDmg = plugin.getLeaderboardService().formatNumber(top.getDamage(), "id-ID");
                return "Top DPS: " + top.getName() + " (" + formattedDmg + ") (" + top.getPercent() + "%)";
            }
            return "Top DPS: Belum ada";
        }

        // 4. Rank N: %seriabossevent_rank<n>_<event>_name% or damage
        Matcher matcher = RANK_PATTERN.matcher(lower);
        if (matcher.matches()) {
            int rankNum = Integer.parseInt(matcher.group(1));
            String eventName = matcher.group(2);
            String field = matcher.group(3);

            FightResult res = plugin.getResultStore().getLastResult(eventName);
            if (res != null && rankNum > 0 && rankNum <= res.getRankings().size()) {
                PlayerContribution pc = res.getRankings().get(rankNum - 1);
                if ("name".equalsIgnoreCase(field)) {
                    return pc.getName();
                }
                if ("damage".equalsIgnoreCase(field)) {
                    return plugin.getLeaderboardService().formatNumber(pc.getDamage(), "id-ID");
                }
            }
            return field.equals("name") ? "Unknown" : "0";
        }

        return null;
    }
}
