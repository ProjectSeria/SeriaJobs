package io.lumine.mythic.bukkit.events;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import io.lumine.mythic.api.drops.IItemDrop;
import io.lumine.mythic.api.config.MythicLineConfig;
public class MythicDropLoadEvent extends Event {
    public static HandlerList getHandlerList() { return null; }
    public HandlerList getHandlers() { return null; }
    public String getDropName() { return null; }
    public MythicLineConfig getConfig() { return null; }
    public Container getContainer() { return null; }
    public void register(IItemDrop drop) {}
    public interface Container {
        String getLine();
    }
}
