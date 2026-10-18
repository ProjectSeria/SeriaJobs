package id.seria.bossevent.event;

import id.seria.bossevent.SeriaBossEventPlugin;
import id.seria.bossevent.config.EventConfig;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager {

    private final SeriaBossEventPlugin plugin;
    private final Map<String, BossEvent> activeEvents = new ConcurrentHashMap<>();

    public EventManager(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
    }

    public void initEvents() {
        for (EventConfig config : plugin.getEventConfigLoader().getEvents().values()) {
            activeEvents.put(config.getName().toLowerCase(), new BossEvent(plugin, config));
        }
    }

    public synchronized void reload() {
        // Keep active events intact, update/add idle events
        Map<String, EventConfig> newConfigs = plugin.getEventConfigLoader().getEvents();

        for (Map.Entry<String, EventConfig> entry : newConfigs.entrySet()) {
            String name = entry.getKey().toLowerCase();
            BossEvent current = activeEvents.get(name);
            if (current == null || current.getState() == BossEventState.IDLE) {
                activeEvents.put(name, new BossEvent(plugin, entry.getValue()));
            }
        }
    }

    public BossEvent getEvent(String name) {
        if (name == null) return null;
        return activeEvents.get(name.toLowerCase());
    }

    public Collection<BossEvent> getAllEvents() {
        return Collections.unmodifiableCollection(activeEvents.values());
    }

    public void stopAll() {
        for (BossEvent event : activeEvents.values()) {
            try {
                event.cleanup();
            } catch (Throwable t) {
                plugin.getLogger().warning("[SeriaBossEvent] Error stopping event " + event.getConfig().getName() + ": " + t.getMessage());
            }
        }
        activeEvents.clear();
    }
}
