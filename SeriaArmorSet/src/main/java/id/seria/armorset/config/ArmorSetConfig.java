package id.seria.armorset.config;

import id.seria.armorset.SeriaArmorSetPlugin;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ArmorSetConfig {

    private final SeriaArmorSetPlugin plugin;

    private boolean debug = false;
    private final Map<String, Integer> sets = new HashMap<>();

    private boolean blocksEnabled = true;
    private boolean mobsEnabled = true;
    private boolean xpEnabled = true;
    private boolean playSound = true;
    private Sound sound = Sound.ENTITY_ITEM_PICKUP;
    private float soundVolume = 0.5f;
    private float soundPitch = 1.2f;
    private boolean armorSlotsOnly = true;

    private String messagePrefix = "<gradient:#FF8A00:#E52E71><bold>SeriaSet</bold></gradient> <dark_gray>»</dark_gray> ";
    private String messageReload = "<green>Konfigurasi SeriaArmorSet berhasil dimuat ulang!</green>";
    private String messageNoPermission = "<red>Kamu tidak memiliki izin untuk menggunakan perintah ini.</red>";
    private String messagePlayerNotFound = "<red>Pemain <yellow>{player}</yellow> tidak ditemukan atau sedang offline.</red>";
    private String messageStatusWearing = "<gray>Pemain <white>{player}</white> sedang mengenakan set <gold><bold>{set}</bold></gold> (<green>{pieces}/{required}</green> bagian). Efek Telepathy: <green><bold>AKTIF</bold></green>.</gray>";
    private String messageStatusNotWearing = "<gray>Pemain <white>{player}</white> <red>tidak mengenakan</red> set armor telepathy yang aktif.</gray>";
    private List<String> messageHelp = new ArrayList<>();

    public ArmorSetConfig(SeriaArmorSetPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.debug = config.getBoolean("debug", false);

        // Load sets
        sets.clear();
        ConfigurationSection setsSection = config.getConfigurationSection("sets");
        if (setsSection != null) {
            for (String key : setsSection.getKeys(false)) {
                int required = setsSection.getInt(key + ".pieces-required", 4);
                sets.put(key.toUpperCase(Locale.ROOT), Math.max(1, required));
            }
        }

        // Options
        this.blocksEnabled = config.getBoolean("options.blocks", true);
        this.mobsEnabled = config.getBoolean("options.mobs", true);
        this.xpEnabled = config.getBoolean("options.xp", true);
        this.playSound = config.getBoolean("options.play-sound", true);
        this.armorSlotsOnly = config.getBoolean("options.armor-slots-only", true);

        // Sound settings
        String soundKey = config.getString("options.sound-name", "ENTITY_ITEM_PICKUP");
        try {
            this.sound = Sound.valueOf(soundKey.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound-name '" + soundKey + "' in config.yml, defaulting to ENTITY_ITEM_PICKUP");
            this.sound = Sound.ENTITY_ITEM_PICKUP;
        }
        this.soundVolume = (float) config.getDouble("options.sound-volume", 0.5);
        this.soundPitch = (float) config.getDouble("options.sound-pitch", 1.2);

        // Messages
        this.messagePrefix = config.getString("messages.prefix", messagePrefix);
        this.messageReload = config.getString("messages.reload", messageReload);
        this.messageNoPermission = config.getString("messages.no-permission", messageNoPermission);
        this.messagePlayerNotFound = config.getString("messages.player-not-found", messagePlayerNotFound);
        this.messageStatusWearing = config.getString("messages.status-wearing", messageStatusWearing);
        this.messageStatusNotWearing = config.getString("messages.status-not-wearing", messageStatusNotWearing);
        this.messageHelp = config.getStringList("messages.help");
        if (this.messageHelp.isEmpty()) {
            this.messageHelp = List.of(
                    "<gradient:#FF8A00:#E52E71><bold>SeriaArmorSet (SetTelepathy)</bold></gradient> <gray>v{version}</gray>",
                    "<gray>/settelepathy reload <dark_gray>-</dark_gray> <white>Muat ulang konfigurasi plugin</white></gray>",
                    "<gray>/settelepathy status [player] <dark_gray>-</dark_gray> <white>Periksa status armor set pemain</white></gray>"
            );
        }

        plugin.getLogger().info("Loaded " + sets.size() + " telepathy armor set(s) from config: " + sets.keySet());
    }

    public boolean isDebug() {
        return debug;
    }

    public Map<String, Integer> getSets() {
        return Collections.unmodifiableMap(sets);
    }

    public int getRequiredPieces(String setId) {
        if (setId == null) return -1;
        return sets.getOrDefault(setId.toUpperCase(Locale.ROOT), -1);
    }

    public boolean isConfiguredSet(String setId) {
        if (setId == null) return false;
        return sets.containsKey(setId.toUpperCase(Locale.ROOT));
    }

    public boolean isBlocksEnabled() {
        return blocksEnabled;
    }

    public boolean isMobsEnabled() {
        return mobsEnabled;
    }

    public boolean isXpEnabled() {
        return xpEnabled;
    }

    public boolean isPlaySound() {
        return playSound;
    }

    public Sound getSound() {
        return sound;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public float getSoundPitch() {
        return soundPitch;
    }

    public boolean isArmorSlotsOnly() {
        return armorSlotsOnly;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public String getMessageReload() {
        return messageReload;
    }

    public String getMessageNoPermission() {
        return messageNoPermission;
    }

    public String getMessagePlayerNotFound() {
        return messagePlayerNotFound;
    }

    public String getMessageStatusWearing() {
        return messageStatusWearing;
    }

    public String getMessageStatusNotWearing() {
        return messageStatusNotWearing;
    }

    public List<String> getMessageHelp() {
        return messageHelp;
    }
}