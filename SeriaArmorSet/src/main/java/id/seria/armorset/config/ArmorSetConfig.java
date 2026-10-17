package id.seria.armorset.config;

import id.seria.armorset.SeriaArmorSetPlugin;
import id.seria.armorset.model.ArmorSetDefinition;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemSet;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
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
    private final Map<String, String> abilityMapping = new HashMap<>();

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

        // Ability Mappings (MMOItems item-sets.yml ability type -> SeriaArmorSet internal skill)
        abilityMapping.clear();
        abilityMapping.put("HALLOWEEN_DREAD_DECOY", "dread_decoy");
        abilityMapping.put("HALLOWEEN_ECLIPSE_VACUUM", "telepathy");
        abilityMapping.put("TELEPATHY", "telepathy");
        abilityMapping.put("SERIA_TELEPATHY", "telepathy");

        ConfigurationSection mappingSec = config.getConfigurationSection("ability-mapping");
        if (mappingSec != null) {
            for (String key : mappingSec.getKeys(false)) {
                abilityMapping.put(key.toUpperCase(Locale.ROOT), mappingSec.getString(key));
            }
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

        // 3. Auto-hook & sync directly from MMOItems item-sets.yml
        syncFromMMOItems();

        plugin.getLogger().info("Loaded " + setDefinitions.size() + " custom armor set(s): " + setDefinitions.keySet());
    }

    private void loadSetsFromSection(ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            int required = section.getInt(key + ".pieces-required", 4);
            ArmorSetDefinition def = setDefinitions.computeIfAbsent(key.toUpperCase(Locale.ROOT), k -> new ArmorSetDefinition(k, required));

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
                ConfigurationSection telepathyConf = new MemoryConfiguration();
                telepathyConf.set("blocks", blocksEnabled);
                telepathyConf.set("mobs", mobsEnabled);
                telepathyConf.set("xp", xpEnabled);
                telepathyConf.set("play-sound", playSound);
                def.addSkill("telepathy", telepathyConf);
            }
        }
    }

    private void loadSetsFromDirectory() {
        File setsFolder = new File(plugin.getDataFolder(), "sets");
        if (!setsFolder.exists()) {
            setsFolder.mkdirs();
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
                    loadSetsFromSection(yaml);
                }
            } catch (Throwable t) {
                plugin.getLogger().warning("Failed loading armor sets from file: " + file.getName() + " - " + t.getMessage());
            }
        }
    }

    /**
     * Automatically inspects MMOItems ItemSets and imports abilities defined under bonuses directly from item-sets.yml!
     */
    public void syncFromMMOItems() {
        if (!Bukkit.getPluginManager().isPluginEnabled("MMOItems")) return;

        try {
            if (MMOItems.plugin == null || MMOItems.plugin.getSets() == null) return;

            for (ItemSet itemSet : MMOItems.plugin.getSets().getAll()) {
                String setId = itemSet.getId().toUpperCase(Locale.ROOT);

                // Check tiers from 2 to 10
                for (int tier = 2; tier <= 10; tier++) {
                    ItemSet.SetBonuses bonuses = itemSet.getBonuses(tier);
                    if (bonuses == null) continue;

                    List<AbilityData> abilities = bonuses.getAbilities();
                    if (abilities == null || abilities.isEmpty()) continue;

                    for (AbilityData ability : abilities) {
                        if (ability == null || ability.getHandler() == null) continue;

                        String handlerId = ability.getHandler().getId().toUpperCase(Locale.ROOT);
                        String mappedSkill = getMappedSkill(handlerId);

                        // If mapped or matches a known skill in SeriaArmorSet
                        if (plugin.getSkillManager().getSkill(mappedSkill) != null) {
                            final int requiredPieces = tier;
                            ArmorSetDefinition def = setDefinitions.computeIfAbsent(setId, k -> new ArmorSetDefinition(k, requiredPieces));

                            ConfigurationSection abilityConf = new MemoryConfiguration();
                            abilityConf.set("mmoitems-ability", handlerId);
                            abilityConf.set("mode", ability.getTrigger() != null ? ability.getTrigger().name() : "DEFAULT");

                            for (String modifier : ability.getModifiers()) {
                                abilityConf.set(modifier, ability.getModifier(modifier));
                            }

                            // If telepathy/vacuum, also ensure block & mob drops flags are active
                            if (mappedSkill.equalsIgnoreCase("telepathy") || mappedSkill.contains("vacuum")) {
                                abilityConf.set("blocks", true);
                                abilityConf.set("mobs", true);
                                abilityConf.set("xp", true);
                            }

                            def.addSkill(mappedSkill, abilityConf);

                            if (debug) {
                                plugin.getLogger().info("[Debug] Auto-hooked ability '" + handlerId + "' from MMOItems set '"
                                        + setId + "' (" + tier + " pieces) -> Skill: " + mappedSkill);
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            if (debug) {
                plugin.getLogger().warning("[Debug] Could not sync abilities directly from MMOItems: " + t.getMessage());
            }
        }
    }

    public String getMappedSkill(String abilityId) {
        String mapped = abilityMapping.get(abilityId.toUpperCase(Locale.ROOT));
        return mapped != null ? mapped : abilityId.toLowerCase(Locale.ROOT);
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