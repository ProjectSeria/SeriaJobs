package id.seria.bossevent.service;

import id.seria.bossevent.SeriaBossEventPlugin;
import id.seria.bossevent.leaderboard.FightResult;
import id.seria.bossevent.leaderboard.PlayerContribution;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ResultStore {

    private final SeriaBossEventPlugin plugin;
    private final File dataFolder;

    // Cache of last fight results per event
    private final Map<String, FightResult> lastResults = new ConcurrentHashMap<>();

    // Pending rewards for offline players: UUID -> list of commands
    private final Map<UUID, List<String>> pendingRewards = new ConcurrentHashMap<>();

    public ResultStore(SeriaBossEventPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        loadAll();
    }

    public void loadAll() {
        loadPendingRewards();
        loadLastResults();
    }

    private void loadPendingRewards() {
        File file = new File(dataFolder, "pending-rewards.yml");
        if (!file.exists()) return;

        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            for (String key : yaml.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    List<String> cmds = yaml.getStringList(key);
                    if (!cmds.isEmpty()) {
                        pendingRewards.put(uuid, new ArrayList<>(cmds));
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (Exception e) {
            plugin.getLogger().severe("[SeriaBossEvent] Error reading pending-rewards.yml: " + e.getMessage());
        }
    }

    public synchronized void savePendingRewardsAsync() {
        Map<UUID, List<String>> copy = new HashMap<>();
        for (Map.Entry<UUID, List<String>> entry : pendingRewards.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            File file = new File(dataFolder, "pending-rewards.yml");
            YamlConfiguration yaml = new YamlConfiguration();
            for (Map.Entry<UUID, List<String>> entry : copy.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    yaml.set(entry.getKey().toString(), entry.getValue());
                }
            }
            try {
                yaml.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("[SeriaBossEvent] Failed to save pending-rewards.yml: " + e.getMessage());
            }
        });
    }

    public void addPendingReward(UUID uuid, List<String> commands) {
        if (uuid == null || commands == null || commands.isEmpty()) return;
        pendingRewards.computeIfAbsent(uuid, k -> Collections.synchronizedList(new ArrayList<>())).addAll(commands);
        savePendingRewardsAsync();
    }

    public List<String> pollPendingRewards(UUID uuid) {
        List<String> list = pendingRewards.remove(uuid);
        if (list != null && !list.isEmpty()) {
            savePendingRewardsAsync();
            return list;
        }
        return Collections.emptyList();
    }

    private void loadLastResults() {
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml") && !name.equals("pending-rewards.yml") && !name.endsWith("-global.yml"));
        if (files == null) return;

        for (File f : files) {
            String eventName = f.getName().substring(0, f.getName().lastIndexOf('.')).toLowerCase();
            try {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
                String mobName = yaml.getString("mob", "Unknown");
                String displayName = yaml.getString("display-name", mobName);
                String killerUuidStr = yaml.getString("killer.uuid");
                UUID killerUuid = killerUuidStr != null ? UUID.fromString(killerUuidStr) : null;
                String killerName = yaml.getString("killer.name", "Unknown");
                String duration = yaml.getString("duration", "00:00");
                double pool = yaml.getDouble("pool", 0.0);

                List<PlayerContribution> rankings = new ArrayList<>();
                List<Map<?, ?>> rawRanks = yaml.getMapList("rankings");
                int rank = 1;
                for (Map<?, ?> rm : rawRanks) {
                    try {
                        UUID uuid = UUID.fromString(String.valueOf(rm.get("uuid")));
                        String name = String.valueOf(rm.get("name"));
                        double dmg = ((Number) rm.get("damage")).doubleValue();
                        double pct = ((Number) rm.get("percent")).doubleValue();
                        rankings.add(new PlayerContribution(rank++, uuid, name, net.kyori.adventure.text.Component.empty(), dmg, pct));
                    } catch (Exception ignored) {}
                }

                FightResult result = new FightResult(
                        eventName,
                        mobName,
                        displayName,
                        killerUuid,
                        killerName,
                        net.kyori.adventure.text.Component.empty(),
                        rankings,
                        duration,
                        pool,
                        java.time.Instant.now()
                );
                lastResults.put(eventName, result);
            } catch (Exception e) {
                plugin.getLogger().warning("[SeriaBossEvent] Could not load saved result for event " + eventName + ": " + e.getMessage());
            }
        }
    }

    public void saveFightResult(FightResult result, boolean globalScore) {
        if (result == null) return;
        lastResults.put(result.getEventName().toLowerCase(), result);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // 1. Save data/<event>.yml
            File file = new File(dataFolder, result.getEventName().toLowerCase() + ".yml");
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.set("event", result.getEventName());
            yaml.set("mob", result.getMobName());
            yaml.set("display-name", result.getDisplayName());
            yaml.set("killer.name", result.getKillerName());
            if (result.getKillerUuid() != null) {
                yaml.set("killer.uuid", result.getKillerUuid().toString());
            }
            yaml.set("duration", result.getDurationFormatted());
            yaml.set("pool", result.getTotalPool());
            yaml.set("date", DateTimeFormatter.ISO_INSTANT.format(result.getEndTime()));

            List<Map<String, Object>> rankList = new ArrayList<>();
            for (PlayerContribution pc : result.getRankings()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("rank", pc.getRank());
                m.put("uuid", pc.getUuid().toString());
                m.put("name", pc.getName());
                m.put("damage", pc.getDamage());
                m.put("percent", pc.getPercent());
                rankList.add(m);
            }
            yaml.set("rankings", rankList);

            try {
                yaml.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("[SeriaBossEvent] Failed to save " + file.getName() + ": " + e.getMessage());
            }

            // 2. Global score accumulation if enabled
            if (globalScore) {
                File globalFile = new File(dataFolder, result.getEventName().toLowerCase() + "-global.yml");
                YamlConfiguration globalYaml = globalFile.exists() ? YamlConfiguration.loadConfiguration(globalFile) : new YamlConfiguration();
                for (PlayerContribution pc : result.getRankings()) {
                    String path = "scores." + pc.getUuid().toString();
                    double existing = globalYaml.getDouble(path + ".damage", 0.0);
                    globalYaml.set(path + ".name", pc.getName());
                    globalYaml.set(path + ".damage", existing + pc.getDamage());
                }
                try {
                    globalYaml.save(globalFile);
                } catch (IOException e) {
                    plugin.getLogger().severe("[SeriaBossEvent] Failed to save global scores for " + result.getEventName() + ": " + e.getMessage());
                }
            }
        });
    }

    public FightResult getLastResult(String eventName) {
        return lastResults.get(eventName.toLowerCase());
    }
}
