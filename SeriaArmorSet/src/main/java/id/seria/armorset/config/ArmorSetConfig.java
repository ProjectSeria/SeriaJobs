package id.seria.armorset.config;

import id.seria.armorset.SeriaArmorSetPlugin;
import id.seria.armorset.model.ArmorSetDefinition;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ArmorSetConfig {

    private final SeriaArmorSetPlugin plugin;

    private boolean debug = false;
    private final Map<String, ArmorSetDefinition> setDefinitions = new HashMap<>();

    private boolean blocksEnabled = true;
    private boolean mobsEnabled = true;
    private boolean xpEnabled = true;
    private boolean playSound = true;
    private Sound sound = Sound.ENTITY_ITEM_PICKUP;
    private float soundVolume = 0.5f;
    private float soundPitch = 1.2f;
    private boolean armorSlotsOnly = true;

    private String messagePrefix = "<gradient:#FF8A00:#E52E71><bold>SeriaArmorSet</bold></gradient> <dark_gray>»</dark_gray> ";
    private String messageReload = "<green>Konfigurasi SeriaArmorSet berhasil dimuat ulang!</green>";
    private String messageNoPermission = "<red>Kamu tidak memiliki izin untuk menggunakan perintah ini.</red>";
    private String messagePlayerNotFound = "<red>Pemain <yellow>{player}</yellow> tidak ditemukan atau sedang offline.</red>";
    private String messageStatusWearing = "<gray>Pemain <white>{player}</white> mengenakan set <gold><bold>{set}</bold></gold> (<green>{pieces}/{required}</green> bagian). Skills aktif: <green>{skills}</green>.</gray>";
    private String messageStatusNotWearing = "<gray>Pemain <white>{player}</white> <red>tidak mengenakan</red> set armor kustom yang aktif.</gray>";
    private List<String> messageHelp = new ArrayList<>();

    public ArmorSetConfig(SeriaArmorSetPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.debug = config.getBoolean("debug", false);

        // General settings
        this.armorSlotsOnly = config.getBoolean("settings.armor-slots-only",
                config.getBoolean("options.armor-slots-only", true));

        // Global fallback options
        this.blocksEnabled = config.getBoolean("options.blocks", true);
        this.mobsEnabled = config.getBoolean("options.mobs", true);
        this.xpEnabled = config.getBoolean("options.xp", true);
        this.playSound = config.getBoolean("options.play-sound", true);

        // Sound settings
        String soundKey = config.getString("options.sound-name", "ENTITY_ITEM_PICKUP");
        try {
            this.sound = Sound.valueOf(soundKey.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
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
                    "<gradient:#FF8A00:#E52E71><bold>SeriaArmorSet Engine</bold></gradient> <gray>v{version}</gray>",
                    "<gray>/armorset reload <dark_gray>-</dark_gray> <white>Muat ulang konfigurasi plugin</white></gray>",
                    "<gray>/armorset status [player] <dark_gray>-</dark_gray> <white>Periksa status armor set pemain</white></gray>",
                    "<gray>/armorset list <dark_gray>-</dark_gray> <white>Daftar seluruh armor set dan skill terdaftar</white></gray>"
            );
        }

        // Clear existing sets
        setDefinitions.clear();

        // 1. Load sets from config.yml
        ConfigurationSection setsSection = config.getConfigurationSection("sets");
        if (setsSection != null) {
            loadSetsFromSection(setsSection);
        }

        // 2. Load sets from sets/ directory
        loadSetsFromDirectory();

        plugin.getLogger().info("Loaded " + setDefinitions.size() + " custom armor set(s): " + setDefinitions.keySet());
    }

    private void loadSetsFromSection(ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            int required = section.getInt(key + ".pieces-required", 4);
            ArmorSetDefinition def = new ArmorSetDefinition(key, required);

            ConfigurationSection skillsSec = section.getConfigurationSection(key + ".skills");
            if (skillsSec != null) {
                for (String skillId : skillsSec.getKeys(false)) {
                    boolean enabled = skillsSec.getBoolean(skillId + ".enabled", true);
                    if (enabled) {
                        def.addSkill(skillId, skillsSec.getConfigurationSection(skillId));
                    }
                }
            } else {
                // Backwards compatibility: attach default telepathy skill
                ConfigurationSection telepathyConf = section.createSection("temp_telepathy");
                telepathyConf.set("blocks", blocksEnabled);
                telepathyConf.set("mobs", mobsEnabled);
                telepathyConf.set("xp", xpEnabled);
                telepathyConf.set("play-sound", playSound);
                def.addSkill("telepathy", telepathyConf);
            }

            setDefinitions.put(def.getId(), def);
        }
    }

    private void loadSetsFromDirectory() {
        File setsFolder = new File(plugin.getDataFolder(), "sets");
        if (!setsFolder.exists()) {
            setsFolder.mkdirs();
            // If default file doesn't exist, save it
            plugin.saveResource("sets/halloween_2026.yml", false);
        }

        File[] files = setsFolder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files == null) return;

        for (File file : files) {
            try {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                ConfigurationSection sec = yaml.getConfigurationSection("sets");
                if (sec != null) {
                    loadSetsFromSection(sec);
                } else {
                    // Check if the root itself is a map of sets
                    loadSetsFromSection(yaml);
                }
            } catch (Throwable t) {
                plugin.getLogger().warning("Failed loading armor sets from file: " + file.getName() + " - " + t.getMessage());
            }
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public Map<String, ArmorSetDefinition> getSetDefinitions() {
        return Collections.unmodifiableMap(setDefinitions);
    }

    public ArmorSetDefinition getSetDefinition(String setId) {
        if (setId == null) return null;
        return setDefinitions.get(setId.toUpperCase(Locale.ROOT));
    }

    public Map<String, Integer> getSets() {
        Map<String, Integer> map = new HashMap<>();
        for (ArmorSetDefinition def : setDefinitions.values()) {
            map.put(def.getId(), def.getPiecesRequired());
        }
        return map;
    }

    public int getRequiredPieces(String setId) {
        ArmorSetDefinition def = getSetDefinition(setId);
        return def != null ? def.getPiecesRequired() : -1;
    }

    public boolean isConfiguredSet(String setId) {
        return getSetDefinition(setId) != null;
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