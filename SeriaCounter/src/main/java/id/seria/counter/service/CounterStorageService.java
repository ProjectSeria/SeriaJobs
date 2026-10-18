package id.seria.counter.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import id.seria.counter.SeriaCounterPlugin;
import id.seria.counter.model.Counter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class CounterStorageService {

    private final SeriaCounterPlugin plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File dataFile;
    private BukkitTask autoSaveTask;

    public CounterStorageService(SeriaCounterPlugin plugin) {
        this.plugin = plugin;
        File dataDir = new File(plugin.getDataFolder(), "data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        this.dataFile = new File(dataDir, "counters_data.json");
    }

    public void startAutoSave() {
        int intervalSeconds = Math.max(10, plugin.getConfigManager().getAutoSaveInterval());
        long intervalTicks = intervalSeconds * 20L;

        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }

        autoSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveDataSync, intervalTicks, intervalTicks);
    }

    public void stopAutoSave() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
            autoSaveTask = null;
        }
    }

    public void loadData() {
        if (!dataFile.exists()) return;

        try (Reader reader = new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, StoredCounterData>>() {}.getType();
            Map<String, StoredCounterData> dataMap = gson.fromJson(reader, type);
            if (dataMap == null) return;

            for (Map.Entry<String, StoredCounterData> entry : dataMap.entrySet()) {
                String id = entry.getKey().toLowerCase().trim();
                Counter counter = plugin.getCounterLoader().getCounter(id);
                if (counter != null && entry.getValue() != null) {
                    StoredCounterData d = entry.getValue();
                    counter.loadState(d.value, d.stepIndex, d.executedOnceSteps);
                }
            }
            plugin.getLogger().info("[SeriaCounter] Restored runtime data for " + dataMap.size() + " counters from disk.");
        } catch (Exception e) {
            plugin.getLogger().severe("[SeriaCounter] Failed to load data from " + dataFile.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized void saveDataSync() {
        Map<String, StoredCounterData> map = new HashMap<>();
        for (Counter c : plugin.getCounterLoader().getCounters().values()) {
            StoredCounterData d = new StoredCounterData();
            d.value = c.getCurrentValue();
            d.stepIndex = c.getCurrentStepIndex();
            d.executedOnceSteps = new HashSet<>(c.getExecutedOnceSteps());
            map.put(c.getId(), d);
        }

        File tempFile = new File(dataFile.getParentFile(), dataFile.getName() + ".tmp");
        try {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
                gson.toJson(map, writer);
            }
            Files.move(tempFile.toPath(), dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            plugin.getLogger().severe("[SeriaCounter] Failed to save counter data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static class StoredCounterData {
        public long value;
        public int stepIndex;
        public Set<Integer> executedOnceSteps = new HashSet<>();
    }
}
