package id.seria.jobs.config;

import id.seria.jobs.SeriaJobs;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class ConfigManager {

    private final SeriaJobs plugin;
    private FileConfiguration config;

    public ConfigManager(SeriaJobs plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String getLanguageFileName() {
        return config.getString("settings.language", "messages_id.yml");
    }

    public String getStorageType() {
        return config.getString("settings.storage.type", "SQLITE");
    }

    public String getMySqlHost() {
        return config.getString("settings.storage.mysql.host", "localhost");
    }

    public int getMySqlPort() {
        return config.getInt("settings.storage.mysql.port", 3306);
    }

    public String getMySqlDatabase() {
        return config.getString("settings.storage.mysql.database", "seria_network");
    }

    public String getMySqlUser() {
        return config.getString("settings.storage.mysql.user", "root");
    }

    public String getMySqlPassword() {
        return config.getString("settings.storage.mysql.password", "");
    }

    public int getMySqlPoolSize() {
        return config.getInt("settings.storage.mysql.pool-size", 10);
    }

    public int getDefaultSlots() {
        return config.getInt("job-slots.default-slots", 1);
    }

    public boolean isPermissionSlotsEnabled() {
        return config.getBoolean("job-slots.enable-permission-slots", true);
    }

    public boolean isLeaveJobEnabled() {
        return config.getBoolean("job-slots.leave-job.enabled", true);
    }

    public int getLeaveCooldownHours() {
        return config.getInt("job-slots.leave-job.cooldown-hours", 24);
    }

    public boolean isResetProgressOnLeave() {
        return config.getBoolean("job-slots.leave-job.reset-progress-on-leave", false);
    }

    public int getMarketShiftIntervalSeconds() {
        return config.getInt("market-shifts.interval-seconds", 21600);
    }

    public boolean isUseFixedTimes() {
        return config.getBoolean("market-shifts.use-fixed-times", true);
    }

    public List<String> getFixedResetTimes() {
        return config.getStringList("market-shifts.fixed-reset-times");
    }

    public boolean isHookEnabled(String hookPath) {
        if (config.isBoolean("hooks." + hookPath)) {
            return config.getBoolean("hooks." + hookPath);
        }
        return config.getBoolean("hooks." + hookPath, true);
    }

    public boolean isMultiplyResourceEnabled() {
        if (config.contains("multiply-resource.enabled")) {
            return config.getBoolean("multiply-resource.enabled", true);
        }
        if (config.contains("Multiply Resource.Enabled")) {
            return config.getBoolean("Multiply Resource.Enabled", true);
        }
        if (config.contains("Multiply Resource.enabled")) {
            return config.getBoolean("Multiply Resource.enabled", true);
        }
        if (config.isConfigurationSection("multiply-resource") || config.isConfigurationSection("Multiply Resource")) {
            return true;
        }
        return false;
    }

    public int getMultiplyResourceLevel() {
        int lvl = config.getInt("multiply-resource.level", 0);
        if (lvl <= 0) lvl = config.getInt("Multiply Resource.Level", 0);
        if (lvl <= 0) lvl = config.getInt("multiply-resource.Level", 0);
        if (lvl <= 0) lvl = config.getInt("Multiply Resource.level", 0);
        return lvl > 0 ? lvl : 5;
    }

    public double getMultiplyResourceMultiplier() {
        double mult = config.getDouble("multiply-resource.multiplier", 0.0);
        if (mult <= 0.0) mult = config.getDouble("Multiply Resource.Multiplier", 0.0);
        if (mult <= 0.0) mult = config.getDouble("multiply-resource.Multiplier", 0.0);
        if (mult <= 0.0) mult = config.getDouble("Multiply Resource.multiplier", 0.0);
        return mult > 0.0 ? mult : 1.25;
    }

    public String getMultiplyResourceIntervalMode() {
        String mode = config.getString("multiply-resource.interval-mode", null);
        if (mode == null) mode = config.getString("Multiply Resource.Interval-Mode", null);
        if (mode == null) mode = config.getString("Multiply Resource.interval-mode", null);
        return mode != null ? mode.toUpperCase() : "AFTER_LEVEL";
    }

    public String getMultiplyResourceScalingType() {
        String type = config.getString("multiply-resource.scaling-type", null);
        if (type == null) type = config.getString("Multiply Resource.Scaling-Type", null);
        if (type == null) type = config.getString("Multiply Resource.scaling-type", null);
        return type != null ? type.toUpperCase() : "COMPOUND";
    }

    public String getMultiplyResourceRounding() {
        String round = config.getString("multiply-resource.rounding", null);
        if (round == null) round = config.getString("Multiply Resource.Rounding", null);
        if (round == null) round = config.getString("Multiply Resource.rounding", null);
        return round != null ? round.toUpperCase() : "CEIL";
    }

    public double getResourceMultiplier(int jobLevel) {
        if (!isMultiplyResourceEnabled()) {
            return 1.0;
        }
        int interval = getMultiplyResourceLevel();
        if (interval <= 0) return 1.0;

        int tier;
        if ("AT_LEVEL".equalsIgnoreCase(getMultiplyResourceIntervalMode())) {
            tier = jobLevel / interval;
        } else {
            // Default: AFTER_LEVEL (Lv 1-5 = tier 0, Lv 6-10 = tier 1, etc.)
            tier = Math.max(0, (jobLevel - 1) / interval);
        }

        if (tier <= 0) {
            return 1.0;
        }

        double multiplier = getMultiplyResourceMultiplier();
        if (multiplier <= 0.0) return 1.0;

        if ("LINEAR".equalsIgnoreCase(getMultiplyResourceScalingType())) {
            return 1.0 + ((multiplier - 1.0) * tier);
        } else {
            // Default: COMPOUND
            return Math.pow(multiplier, tier);
        }
    }

    public int calculateScaledAmount(int baseAmount, int jobLevel) {
        if (baseAmount <= 0) return baseAmount;
        double mult = getResourceMultiplier(jobLevel);
        if (mult <= 1.0) {
            return baseAmount;
        }
        double raw = baseAmount * mult;
        String rounding = getMultiplyResourceRounding();
        if ("FLOOR".equalsIgnoreCase(rounding)) {
            return Math.max(1, (int) Math.floor(raw));
        } else if ("ROUND".equalsIgnoreCase(rounding)) {
            return Math.max(1, (int) Math.round(raw));
        } else {
            // Default: CEIL
            return Math.max(1, (int) Math.ceil(raw));
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String getCurrentEvent() {
        return config != null ? config.getString("events.current-event", "none").trim().toLowerCase() : "none";
    }

    public boolean isEventActive() {
        String ev = getCurrentEvent();
        return !ev.isEmpty() && !ev.equalsIgnoreCase("none");
    }

    public int getEventSlot() {
        if (config != null) {
            if (config.contains("events.event-slot")) {
                return config.getInt("events.event-slot");
            }
            if (config.contains("events.inventory-slot")) {
                return config.getInt("events.inventory-slot");
            }
        }
        return 8;
    }

    public String getEventDenyRerollMessage() {
        return config != null ? config.getString("events.deny-reroll-message",
                "<red>Pesanan khusus event tidak dapat di-reroll! Tunggu rotasi pasar berikutnya.</red>")
                : "<red>Pesanan khusus event tidak dapat di-reroll! Tunggu rotasi pasar berikutnya.</red>";
    }

    public void setCurrentEvent(String eventName) {
        if (config != null) {
            config.set("events.current-event", eventName != null ? eventName.trim().toLowerCase() : "none");
            plugin.saveConfig();
        }
    }
}
