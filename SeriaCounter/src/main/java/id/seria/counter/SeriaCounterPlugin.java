package id.seria.counter;

import id.seria.counter.command.CounterCommand;
import id.seria.counter.config.ConfigManager;
import id.seria.counter.config.CounterConfigLoader;
import id.seria.counter.placeholder.CounterPAPIExpansion;
import id.seria.counter.service.CounterActionService;
import id.seria.counter.service.CounterStorageService;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class SeriaCounterPlugin extends JavaPlugin {

    private static SeriaCounterPlugin instance;

    private ConfigManager configManager;
    private CounterConfigLoader counterLoader;
    private CounterActionService actionService;
    private CounterStorageService storageService;
    private CounterPAPIExpansion papiExpansion;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Initialize configuration & services
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();

        this.actionService = new CounterActionService(this);
        this.counterLoader = new CounterConfigLoader(this);
        this.counterLoader.loadCounters();

        this.storageService = new CounterStorageService(this);
        this.storageService.loadData();
        this.storageService.startAutoSave();

        // 2. Register command
        PluginCommand cmd = getCommand("counter");
        if (cmd != null) {
            CounterCommand executor = new CounterCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        // 3. Hook PlaceholderAPI if present
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.papiExpansion = new CounterPAPIExpansion(this);
            this.papiExpansion.register();
            getLogger().info("[SeriaCounter] Hooked into PlaceholderAPI (%seriacounter_<id>_<property>%).");
        }

        getLogger().info("[SeriaCounter] Enabled successfully with " + counterLoader.getCounters().size() + " active counters.");
    }

    @Override
    public void onDisable() {
        if (storageService != null) {
            storageService.stopAutoSave();
            storageService.saveDataSync();
        }

        if (papiExpansion != null) {
            try {
                papiExpansion.unregister();
            } catch (Throwable ignored) {}
        }

        getLogger().info("[SeriaCounter] Disabled and saved runtime data safely.");
    }

    public static SeriaCounterPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CounterConfigLoader getCounterLoader() {
        return counterLoader;
    }

    public CounterActionService getActionService() {
        return actionService;
    }

    public CounterStorageService getStorageService() {
        return storageService;
    }
}
