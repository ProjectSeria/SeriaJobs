package id.seria.counter.api.events;

import id.seria.counter.model.Counter;
import id.seria.counter.model.CounterStep;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CounterStepCompleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Counter counter;
    private final CounterStep step;
    private final long currentValue;

    public CounterStepCompleteEvent(@NotNull Counter counter, @NotNull CounterStep step, long currentValue) {
        this.counter = counter;
        this.step = step;
        this.currentValue = currentValue;
    }

    public @NotNull Counter getCounter() {
        return counter;
    }

    public @NotNull String getId() {
        return counter.getId();
    }

    public @NotNull CounterStep getStep() {
        return step;
    }

    public int getStepNumber() {
        return step.getStepNumber();
    }

    public long getCurrentValue() {
        return currentValue;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
