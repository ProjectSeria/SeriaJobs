package id.seria.jobs.api;

import id.seria.jobs.model.Job;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class JobLeaveEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Job job;
    private boolean cancelled = false;

    public JobLeaveEvent(@NotNull Player player, @NotNull Job job) {
        this.player = player;
        this.job = job;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public Job getJob() {
        return job;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
