package id.seria.counter.api.events;

import id.seria.counter.model.Counter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CounterChangeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    private final Counter counter;
    private final long oldValue;
    private long newValue;
    private final CommandSender source;

    public CounterChangeEvent(@NotNull Counter counter, long oldValue, long newValue, @Nullable CommandSender source) {
        this.counter = counter;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.source = source;
    }

    public @NotNull Counter getCounter() {
        return counter;
    }

    public @NotNull String getId() {
        return counter.getId();
    }

    public long getOldValue() {
        return oldValue;
    }

    public long getNewValue() {
        return newValue;
    }

    public void setNewValue(long newValue) {
        this.newValue = newValue;
    }

    public long getDelta() {
        return newValue - oldValue;
    }

    public @Nullable CommandSender getSource() {
        return source;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
