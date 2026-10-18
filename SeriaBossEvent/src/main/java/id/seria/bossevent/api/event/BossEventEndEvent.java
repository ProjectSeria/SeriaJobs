package id.seria.bossevent.api.event;

import id.seria.bossevent.leaderboard.FightResult;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class BossEventEndEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final String eventName;
    private final EndReason endReason;
    private final FightResult fightResult;

    public BossEventEndEvent(String eventName, EndReason endReason, @Nullable FightResult fightResult) {
        this.eventName = eventName;
        this.endReason = endReason;
        this.fightResult = fightResult;
    }

    public String getEventName() {
        return eventName;
    }

    public EndReason getEndReason() {
        return endReason;
    }

    @Nullable
    public FightResult getFightResult() {
        return fightResult;
    }

    public Set<UUID> getParticipants() {
        if (fightResult != null) {
            return fightResult.getParticipantUuids();
        }
        return Collections.emptySet();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
