package id.seria.bossevent.config;

import id.seria.bossevent.SeriaBossEventPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventConfigLoader {

    private final SeriaBossEventPlugin plugin;
    private final Map<String, EventConfig> events = new ConcurrentHashMap<>();

    public EventConfigLoader(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadEvents() {
        File folder = new File(plugin.getDataFolder(), "events");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] existing = folder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (existing == null || existing.length == 0) {
            plugin.saveResource("events/mini_boss_hollow_isle.yml", false);
        }

        events.clear();
        loadFromDirectory(folder);

        plugin.getLogger().info("[SeriaBossEvent] Loaded " + events.size() + " boss event configs successfully.");
    }

    private void loadFromDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                loadFromDirectory(file);
            } else if (file.getName().endsWith(".yml") || file.getName().endsWith(".yaml")) {
                loadEventFile(file);
            }
        }
    }

    public EventConfig loadEventFile(File file) {
        String fileName = file.getName();
        String defaultName = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase().trim();

        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            String name = yaml.getString("name", defaultName).toLowerCase().trim();

            // Game section
            String displayName = yaml.getString("game.display-name", name);
            long duration = yaml.getLong("game.duration", -1);
            boolean overrideMessages = yaml.getBoolean("game.override-messages", false);
            boolean shutdownEventAfterDuration = yaml.getBoolean("game.shutdown-event-after-duration", false);
            boolean globalScore = yaml.getBoolean("game.global-score", false);

            // Countdown section
            boolean countdownEnabled = yaml.getBoolean("countdown.enabled", true);
            int countdownDuration = yaml.getInt("countdown.duration", 60);
            if (countdownDuration <= 0) {
                plugin.getLogger().warning("[SeriaBossEvent] countdown.duration in '" + fileName + "' must be > 0. Falling back to 60s.");
                countdownDuration = 60;
            }

            List<Integer> rawNotifyAt = yaml.getIntegerList("countdown.notify-at");
            if (rawNotifyAt.isEmpty() && yaml.contains("countdown.notify-at")) {
                // In case it was written as single line or array
                for (Object obj : yaml.getList("countdown.notify-at", Collections.emptyList())) {
                    if (obj instanceof Number num) {
                        rawNotifyAt.add(num.intValue());
                    }
                }
            }
            if (rawNotifyAt.isEmpty()) {
                rawNotifyAt = Arrays.asList(60, 30, 10, 5, 4, 3, 2, 1);
            }

            List<Integer> notifyAt = new ArrayList<>();
            for (int val : rawNotifyAt) {
                if (val <= countdownDuration && !notifyAt.contains(val)) {
                    notifyAt.add(val);
                }
            }
            notifyAt.sort(Collections.reverseOrder());

            int countdownDelay = yaml.getInt("countdown.delay", 0);

            // Task section
            boolean taskEnabled = yaml.getBoolean("task.enabled", true);
            long taskPeriod = yaml.getLong("task.period", 1000);
            long taskDelay = yaml.getLong("task.delay", 0);

            // Bosses section: supports either single object or list of objects
            List<BossConfig> bosses = parseBosses(yaml, fileName);
            if (bosses.isEmpty()) {
                plugin.getLogger().warning("[SeriaBossEvent] Skipping '" + fileName + "': No valid bosses configured.");
                return null;
            }

            // Leaderboard section
            boolean leaderboardEnabled = yaml.getBoolean("leaderboard.enabled", true);
            String trackingMode = yaml.getString("leaderboard.tracking.mode", "hp-delta");
            long pollInterval = yaml.getLong("leaderboard.tracking.poll-interval", 1);
            boolean attributeUnownedDamage = yaml.getBoolean("leaderboard.tracking.attribute-unowned-damage", false);

            int displayTop = yaml.getInt("leaderboard.display.top", 3);
            boolean displayCentered = yaml.getBoolean("leaderboard.display.centered", true);
            int minPlayersForPercent = yaml.getInt("leaderboard.display.min-players-for-percent", 2);
            boolean showLastHit = yaml.getBoolean("leaderboard.display.show-last-hit", true);
            boolean showPersonal = yaml.getBoolean("leaderboard.display.show-personal", true);
            boolean showFightDuration = yaml.getBoolean("leaderboard.display.show-fight-duration", true);
            boolean showOnFail = yaml.getBoolean("leaderboard.display.show-on-fail", false);
            String displayScope = yaml.getString("leaderboard.display.scope", "server");

            boolean discordEnabled = yaml.getBoolean("leaderboard.discord.enabled", true);
            String discordChannel = yaml.getString("leaderboard.discord.channel", "broadcasts");
            int discordMaxEntries = yaml.getInt("leaderboard.discord.max-entries", 25);
            String discordColor = yaml.getString("leaderboard.discord.color", "#FFAA00");

            String numberFormatLocale = yaml.getString("leaderboard.number-format.locale", "id-ID");

            // Rewards section
            List<RewardConfig> rewards = parseRewards(yaml);

            // Custom messages override map
            Map<String, Object> customMessages = new HashMap<>();
            ConfigurationSection msgSection = yaml.getConfigurationSection("messages");
            if (msgSection != null) {
                for (String key : msgSection.getKeys(true)) {
                    if (!msgSection.isConfigurationSection(key)) {
                        customMessages.put(key, msgSection.get(key));
                    }
                }
            }

            EventConfig config = new EventConfig(
                    name,
                    displayName,
                    duration,
                    overrideMessages,
                    shutdownEventAfterDuration,
                    globalScore,
                    countdownEnabled,
                    countdownDuration,
                    notifyAt,
                    countdownDelay,
                    taskEnabled,
                    taskPeriod,
                    taskDelay,
                    bosses,
                    leaderboardEnabled,
                    trackingMode,
                    pollInterval,
                    attributeUnownedDamage,
                    displayTop,
                    displayCentered,
                    minPlayersForPercent,
                    showLastHit,
                    showPersonal,
                    showFightDuration,
                    showOnFail,
                    displayScope,
                    discordEnabled,
                    discordChannel,
                    discordMaxEntries,
                    discordColor,
                    numberFormatLocale,
                    rewards,
                    customMessages
            );

            events.put(name, config);
            return config;
        } catch (Exception e) {
            plugin.getLogger().severe("[SeriaBossEvent] Error parsing event file '" + fileName + "': " + e.getMessage());
            return null;
        }
    }

    private List<BossConfig> parseBosses(YamlConfiguration yaml, String fileName) {
        List<BossConfig> list = new ArrayList<>();
        if (yaml.isList("bosses")) {
            List<Map<?, ?>> bossMaps = yaml.getMapList("bosses");
            for (Map<?, ?> map : bossMaps) {
                BossConfig boss = parseBossMap(map, fileName);
                if (boss != null) list.add(boss);
            }
        } else if (yaml.isConfigurationSection("bosses")) {
            ConfigurationSection sec = yaml.getConfigurationSection("bosses");
            if (sec != null) {
                BossConfig boss = parseBossSection(sec, fileName);
                if (boss != null) list.add(boss);
            }
        }
        return list;
    }

    private BossConfig parseBossMap(Map<?, ?> map, String fileName) {
        if (!map.containsKey("mob") || !map.containsKey("world")) return null;
        String mob = String.valueOf(map.get("mob")).trim();
        String world = String.valueOf(map.get("world")).trim();
        double x = getDouble(map.get("x"));
        double y = getDouble(map.get("y"));
        double z = getDouble(map.get("z"));
        float yaw = (float) getDouble(map.get("yaw"));
        float pitch = (float) getDouble(map.get("pitch"));
        return new BossConfig(mob, world, x, y, z, yaw, pitch);
    }

    private BossConfig parseBossSection(ConfigurationSection sec, String fileName) {
        String mob = sec.getString("mob");
        String world = sec.getString("world");
        if (mob == null || world == null) return null;
        double x = sec.getDouble("x");
        double y = sec.getDouble("y");
        double z = sec.getDouble("z");
        float yaw = (float) sec.getDouble("yaw", 0.0);
        float pitch = (float) sec.getDouble("pitch", 0.0);
        return new BossConfig(mob, world, x, y, z, yaw, pitch);
    }

    private List<RewardConfig> parseRewards(YamlConfiguration yaml) {
        List<RewardConfig> rewards = new ArrayList<>();
        List<Map<?, ?>> list = yaml.getMapList("rewards");
        for (Map<?, ?> map : list) {
            String id = map.containsKey("id") ? String.valueOf(map.get("id")) : UUID.randomUUID().toString();
            List<String> onlyMobs = new ArrayList<>();
            if (map.containsKey("only-mobs") && map.get("only-mobs") instanceof List<?> mobList) {
                for (Object o : mobList) onlyMobs.add(String.valueOf(o));
            }
            double minDmgPercent = map.containsKey("min-damage-percent") ? getDouble(map.get("min-damage-percent")) : 0;
            List<String> commands = new ArrayList<>();
            if (map.containsKey("commands") && map.get("commands") instanceof List<?> cmdList) {
                for (Object o : cmdList) commands.add(String.valueOf(o));
            }

            int amountMin = 1;
            int amountMax = 1;
            if (map.containsKey("amount")) {
                Object amtObj = map.get("amount");
                if (amtObj instanceof Number num) {
                    amountMin = num.intValue();
                    amountMax = num.intValue();
                } else if (amtObj instanceof Map<?, ?> amtMap) {
                    amountMin = amtMap.containsKey("min") ? ((Number) amtMap.get("min")).intValue() : 1;
                    amountMax = amtMap.containsKey("max") ? ((Number) amtMap.get("max")).intValue() : amountMin;
                }
            }

            Map<String, Integer> groupAmount = new HashMap<>();
            if (map.containsKey("group-amount") && map.get("group-amount") instanceof Map<?, ?> gMap) {
                for (Map.Entry<?, ?> entry : gMap.entrySet()) {
                    if (entry.getValue() instanceof Number n) {
                        groupAmount.put(String.valueOf(entry.getKey()), n.intValue());
                    }
                }
            }

            int topDpsMin = 0;
            int topDpsMax = 0;
            int lastHitMin = 0;
            int lastHitMax = 0;
            if (map.containsKey("bonus") && map.get("bonus") instanceof Map<?, ?> bMap) {
                if (bMap.containsKey("top-dps") && bMap.get("top-dps") instanceof Map<?, ?> tMap) {
                    topDpsMin = tMap.containsKey("min") ? ((Number) tMap.get("min")).intValue() : 0;
                    topDpsMax = tMap.containsKey("max") ? ((Number) tMap.get("max")).intValue() : topDpsMin;
                }
                if (bMap.containsKey("last-hit") && bMap.get("last-hit") instanceof Map<?, ?> lMap) {
                    lastHitMin = lMap.containsKey("min") ? ((Number) lMap.get("min")).intValue() : 0;
                    lastHitMax = lMap.containsKey("max") ? ((Number) lMap.get("max")).intValue() : lastHitMin;
                }
            }

            String message = map.containsKey("message") ? String.valueOf(map.get("message")) : "";
            String groupBonusMessage = map.containsKey("group-bonus-message") ? String.valueOf(map.get("group-bonus-message")) : "";
            String defaultBonusMessage = map.containsKey("default-bonus-message") ? String.valueOf(map.get("default-bonus-message")) : "";

            rewards.add(new RewardConfig(
                    id,
                    onlyMobs,
                    minDmgPercent,
                    commands,
                    amountMin,
                    amountMax,
                    groupAmount,
                    topDpsMin,
                    topDpsMax,
                    lastHitMin,
                    lastHitMax,
                    message,
                    groupBonusMessage,
                    defaultBonusMessage
            ));
        }
        return rewards;
    }

    private double getDouble(Object obj) {
        if (obj instanceof Number num) return num.doubleValue();
        if (obj != null) {
            try {
                return Double.parseDouble(obj.toString().trim());
            } catch (NumberFormatException ignored) {}
        }
        return 0.0;
    }

    public Map<String, EventConfig> getEvents() {
        return Collections.unmodifiableMap(events);
    }

    public EventConfig getEvent(String name) {
        return events.get(name.toLowerCase());
    }
}
