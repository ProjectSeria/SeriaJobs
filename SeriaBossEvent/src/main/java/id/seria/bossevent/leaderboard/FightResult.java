package id.seria.bossevent.leaderboard;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;

public class FightResult {

    private final String eventName;
    private final String mobName;
    private final String displayName;
    private final UUID killerUuid;
    private final String killerName;
    private final Component killerPrefix;
    private final List<PlayerContribution> rankings;
    private final String durationFormatted;
    private final double totalPool;
    private final Instant endTime;

    public FightResult(String eventName,
                       String mobName,
                       String displayName,
                       @Nullable UUID killerUuid,
                       String killerName,
                       Component killerPrefix,
                       List<PlayerContribution> rankings,
                       String durationFormatted,
                       double totalPool,
                       Instant endTime) {
        this.eventName = eventName;
        this.mobName = mobName;
        this.displayName = displayName;
        this.killerUuid = killerUuid;
        this.killerName = killerName;
        this.killerPrefix = killerPrefix;
        this.rankings = rankings != null ? Collections.unmodifiableList(rankings) : Collections.emptyList();
        this.durationFormatted = durationFormatted;
        this.totalPool = totalPool;
        this.endTime = endTime;
    }

    public String getEventName() {
        return eventName;
    }

    public String getMobName() {
        return mobName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Nullable
    public UUID getKillerUuid() {
        return killerUuid;
    }

    public String getKillerName() {
        return killerName;
    }

    public Component getKillerPrefix() {
        return killerPrefix;
    }

    public List<PlayerContribution> getRankings() {
        return rankings;
    }

    public String getDurationFormatted() {
        return durationFormatted;
    }

    public double getTotalPool() {
        return totalPool;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public Set<UUID> getParticipantUuids() {
        Set<UUID> set = new LinkedHashSet<>();
        for (PlayerContribution contribution : rankings) {
            set.add(contribution.getUuid());
        }
        return Collections.unmodifiableSet(set);
    }
}
