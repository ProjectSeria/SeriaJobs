package id.seria.counter.config;

import id.seria.counter.SeriaCounterPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;

public class ConfigManager {

    private final SeriaCounterPlugin plugin;
    private boolean allowNegative;
    private int autoSaveInterval;
    private boolean debug;
    private String prefix;

    public ConfigManager(SeriaCounterPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.allowNegative = config.getBoolean("settings.allow-negative", false);
        this.autoSaveInterval = config.getInt("settings.auto-save-interval", 60);
        this.debug = config.getBoolean("settings.debug", false);
        this.prefix = config.getString("messages.prefix", "<sc:prefix> <dark_gray>»</dark_gray> ");
    }

    public boolean isAllowNegative() {
        return allowNegative;
    }

    public int getAutoSaveInterval() {
        return autoSaveInterval;
    }

    public boolean isDebug() {
        return debug;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getMessage(String key, String def) {
        return plugin.getConfig().getString("messages." + key, def);
    }

    public List<String> getMessageList(String key) {
        List<String> list = plugin.getConfig().getStringList("messages." + key);
        return list != null ? list : Collections.emptyList();
    }
}
