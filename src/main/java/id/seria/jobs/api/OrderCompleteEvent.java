package id.seria.jobs.api;

import id.seria.jobs.model.Job;
import id.seria.jobs.model.JobOrder;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class OrderCompleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Job job;
    private final JobOrder order;

    public OrderCompleteEvent(@NotNull Player player, @NotNull Job job, @NotNull JobOrder order) {
        this.player = player;
        this.job = job;
        this.order = order;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public Job getJob() {
        return job;
    }

    @NotNull
    public JobOrder getOrder() {
        return order;
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
