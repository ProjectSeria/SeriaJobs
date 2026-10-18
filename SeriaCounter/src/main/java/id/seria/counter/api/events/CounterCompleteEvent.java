package id.seria.counter.api.events;

import id.seria.counter.model.Counter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CounterCompleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Counter counter;
    private final int totalSteps;
    private final long finalValue;

    public CounterCompleteEvent(@NotNull Counter counter, int totalSteps, long finalValue) {
        this.counter = counter;
        this.totalSteps = totalSteps;
        this.finalValue = finalValue;
    }

    public @NotNull Counter getCounter() {
        return counter;
    }

    public @NotNull String getId() {
        return counter.getId();
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public long getFinalValue() {
        return finalValue;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
