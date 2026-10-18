package id.seria.bossevent.config;

import id.seria.bossevent.SeriaBossEventPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class ConfigManager {

    private final SeriaBossEventPlugin plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    private boolean debug;
    private int dataSaveInterval;

    public ConfigManager(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();
        this.debug = config.getBoolean("settings.debug", false);
        this.dataSaveInterval = config.getInt("settings.data-save-interval", 60);

        loadMessages();
    }

    public void loadMessages() {
        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        InputStream defMsgStream = plugin.getResource("messages.yml");
        if (defMsgStream != null) {
            YamlConfiguration defMsgConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defMsgStream, StandardCharsets.UTF_8));
            messagesConfig.setDefaults(defMsgConfig);
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public int getDataSaveInterval() {
        return dataSaveInterval;
    }

    public String getPrefix() {
        return getMessage("prefix", "<sc:prefix> <dark_gray>»</dark_gray> ");
    }

    public String getMessage(String path, String def) {
        if (messagesConfig == null) return def;
        return messagesConfig.getString(path, def);
    }

    public List<String> getMessageList(String path) {
        if (messagesConfig == null) return Collections.emptyList();
        return messagesConfig.getStringList(path);
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
}
