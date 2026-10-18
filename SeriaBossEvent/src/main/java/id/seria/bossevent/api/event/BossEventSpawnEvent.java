package id.seria.bossevent.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BossEventSpawnEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final String eventName;
    private final Entity bossEntity;
    private final String mobName;

    public BossEventSpawnEvent(String eventName, Entity bossEntity, String mobName) {
        this.eventName = eventName;
        this.bossEntity = bossEntity;
        this.mobName = mobName;
    }

    public String getEventName() {
        return eventName;
    }

    public Entity getBossEntity() {
        return bossEntity;
    }

    public String getMobName() {
        return mobName;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
