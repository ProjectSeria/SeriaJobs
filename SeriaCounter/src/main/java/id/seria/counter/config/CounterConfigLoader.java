package id.seria.counter.config;

import id.seria.counter.SeriaCounterPlugin;
import id.seria.counter.model.Counter;
import id.seria.counter.model.CounterStep;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CounterConfigLoader {

    private final SeriaCounterPlugin plugin;
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    public CounterConfigLoader(SeriaCounterPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadCounters() {
        File folder = new File(plugin.getDataFolder(), "counters");
        if (!folder.exists()) {
            folder.mkdirs();
            plugin.saveResource("counters/mini_boss_hollow_isle.yml", false);
        }

        // Cache existing counters state before reload
        Map<String, RuntimeState> existingStates = new HashMap<>();
        for (Counter c : counters.values()) {
            existingStates.put(c.getId(), new RuntimeState(c.getCurrentValue(), c.getCurrentStepIndex(), c.getExecutedOnceSteps()));
        }

        counters.clear();
        loadFromDirectory(folder, existingStates);

        plugin.getLogger().info("[SeriaCounter] Loaded " + counters.size() + " counters successfully.");
    }

    private void loadFromDirectory(File dir, Map<String, RuntimeState> existingStates) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                loadFromDirectory(file, existingStates);
            } else if (file.getName().endsWith(".yml") || file.getName().endsWith(".yaml")) {
                loadCounterFile(file, existingStates);
            }
        }
    }

    private void loadCounterFile(File file, Map<String, RuntimeState> existingStates) {
        String fileName = file.getName();
        String id = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase().trim();

        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            String displayName = yaml.getString("name", id);

            List<Map<?, ?>> rawSteps = yaml.getMapList("steps");
            if (rawSteps.isEmpty()) {
                plugin.getLogger().warning("[SeriaCounter] Skipping '" + fileName + "': No steps defined in config.");
                return;
            }

            List<CounterStep> steps = new ArrayList<>();
            for (int i = 0; i < rawSteps.size(); i++) {
                Map<?, ?> stepMap = rawSteps.get(i);
                String stepName = stepMap.containsKey("name") ? String.valueOf(stepMap.get("name")) : "Step " + (i + 1);
                String stepDesc = stepMap.containsKey("description") ? String.valueOf(stepMap.get("description")) : "";

                long goal = 0;
                Object rawGoal = stepMap.get("goal");
                if (rawGoal instanceof Number num) {
                    goal = num.longValue();
                } else if (rawGoal != null) {
                    try {
                        goal = Long.parseLong(rawGoal.toString().trim());
                    } catch (NumberFormatException ignored) {}
                }

                if (goal <= 0) {
                    plugin.getLogger().warning("[SeriaCounter] Skipping step " + (i + 1) + " in '" + fileName + "': Goal must be a positive number (> 0).");
                    continue;
                }

                List<String> actions = getListFromMap(stepMap, "actions");
                List<String> onceActions = getListFromMap(stepMap, "once-actions");

                steps.add(new CounterStep(i, stepName, stepDesc, goal, actions, onceActions));
            }

            if (steps.isEmpty()) {
                plugin.getLogger().warning("[SeriaCounter] Skipping '" + fileName + "': No valid steps with positive goals.");
                return;
            }

            Counter counter = new Counter(id, displayName, steps);

            // Restore state if was previously loaded
            if (existingStates.containsKey(id)) {
                RuntimeState state = existingStates.get(id);
                counter.loadState(state.value, state.stepIndex, state.executedOnce);
            }

            counters.put(id, counter);
        } catch (Exception e) {
            plugin.getLogger().severe("[SeriaCounter] Failed to load counter file '" + fileName + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getListFromMap(Map<?, ?> map, String key) {
        Object val = map.get(key);
        if (val instanceof List<?> list) {
            List<String> res = new ArrayList<>();
            for (Object obj : list) {
                if (obj != null) res.add(String.valueOf(obj));
            }
            return res;
        }
        return Collections.emptyList();
    }

    public Counter getCounter(String id) {
        if (id == null) return null;
        return counters.get(id.toLowerCase().trim());
    }

    public Map<String, Counter> getCounters() {
        return Collections.unmodifiableMap(counters);
    }

    public record RuntimeState(long value, int stepIndex, Set<Integer> executedOnce) {}
}
