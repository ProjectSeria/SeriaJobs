package id.seria.jobs.api;

import id.seria.jobs.model.Job;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class JobLevelUpEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Job job;
    private final int oldLevel;
    private final int newLevel;

    public JobLevelUpEvent(@NotNull Player player, @NotNull Job job, int oldLevel, int newLevel) {
        this.player = player;
        this.job = job;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public Job getJob() {
        return job;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
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
