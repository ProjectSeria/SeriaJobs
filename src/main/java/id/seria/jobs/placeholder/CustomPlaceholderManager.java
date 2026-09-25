package id.seria.jobs.placeholder;

import id.seria.jobs.SeriaJobs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class CustomPlaceholderManager {

    private final SeriaJobs plugin;
    private final Map<String, CustomPlaceholderEntry> placeholderRegistry = new ConcurrentHashMap<>();
    private final Map<String, CustomPlaceholderEntry> fallbackRegistry = new ConcurrentHashMap<>();
    private CustomPAPIExpansion customExpansion;

    public CustomPlaceholderManager(SeriaJobs plugin) {
        this.plugin = plugin;
    }

    public void init() {
        saveDefaultPlaceholders();
        load();
        registerExpansion();
    }

    public void saveDefaultPlaceholders() {
        File folder = new File(plugin.getDataFolder(), "placeholder");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Check if folder is empty, copy default playerlevel.yml
        File defaultFile = new File(folder, "playerlevel.yml");
        if (!defaultFile.exists()) {
            try (InputStream in = plugin.getResource("placeholder/playerlevel.yml")) {
                if (in != null) {
                    Files.copy(in, defaultFile.toPath());
                    plugin.getLogger().info("Created default placeholder template: placeholder/playerlevel.yml");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Could not save default playerlevel.yml", e);
            }
        }
    }

    public synchronized void load() {
        placeholderRegistry.clear();
        fallbackRegistry.clear();

        List<File> foldersToScan = new ArrayList<>();
        File primaryFolder = new File(plugin.getDataFolder(), "placeholder");
        File secondaryFolder = new File(plugin.getDataFolder(), "placeholders");

        if (primaryFolder.exists() && primaryFolder.isDirectory()) {
            foldersToScan.add(primaryFolder);
        }
        if (secondaryFolder.exists() && secondaryFolder.isDirectory() && !secondaryFolder.equals(primaryFolder)) {
            foldersToScan.add(secondaryFolder);
        }

        int count = 0;
        for (File dir : foldersToScan) {
            count += loadFolderRecursively(dir, "");
        }

        plugin.getLogger().info("Loaded " + count + " custom placeholder definition(s) from placeholder/ folder.");
    }

    private int loadFolderRecursively(File dir, String prefix) {
        File[] files = dir.listFiles();
        if (files == null) return 0;

        int loaded = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                String subPrefix = prefix.isEmpty() ? file.getName() : prefix + "_" + file.getName();
                loaded += loadFolderRecursively(file, subPrefix);
            } else if (file.getName().toLowerCase().endsWith(".yml") || file.getName().toLowerCase().endsWith(".yaml")) {
                loaded += loadPlaceholderFile(file, prefix);
            }
        }
        return loaded;
    }

    private int loadPlaceholderFile(File file, String prefix) {
        String baseName = file.getName();
        int dotIdx = baseName.lastIndexOf('.');
        if (dotIdx > 0) {
            baseName = baseName.substring(0, dotIdx);
        }

        String group = prefix.isEmpty() ? baseName : prefix + "_" + baseName;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        int fileCount = 0;

        for (String key : config.getKeys(false)) {
            if (!config.isConfigurationSection(key)) continue;

            ConfigurationSection sec = config.getConfigurationSection(key);
            if (sec == null) continue;

            String type = sec.getString("type", "function");
            String function = sec.getString("function", "short-track");
            String input = sec.getString("input", "");

            Map<String, String> tracks = new LinkedHashMap<>();
            ConfigurationSection tracksSec = sec.getConfigurationSection("tracks");
            if (tracksSec != null) {
                for (String tKey : tracksSec.getKeys(false)) {
                    tracks.put(tKey, tracksSec.getString(tKey, ""));
                }
            }

            CustomPlaceholderEntry entry = new CustomPlaceholderEntry(key, group, type, function, input, tracks);

            // Register various formats for accessibility
            // 1. Full group + key: e.g. playerlevel_math-message-next
            String fullKey = (group + "_" + key).toLowerCase();
            placeholderRegistry.put(fullKey, entry);

            // Also hyphenated: e.g. playerlevel-math-message-next
            String hyphenKey = (group + "-" + key).toLowerCase();
            placeholderRegistry.put(hyphenKey, entry);

            // 2. Direct key fallback: e.g. math-message-next
            fallbackRegistry.putIfAbsent(key.toLowerCase(), entry);

            fileCount++;
        }

        return fileCount;
    }

    public void registerExpansion() {
        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                if (customExpansion != null) {
                    customExpansion.unregister();
                }
                customExpansion = new CustomPAPIExpansion(this);
                customExpansion.register();
                plugin.getLogger().info("Hooked into PlaceholderAPI for '%custom_<file>_<name>%' placeholders.");
            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "Failed to register custom PAPI expansion", t);
            }
        }
    }

    public void unregisterExpansion() {
        if (customExpansion != null) {
            try {
                customExpansion.unregister();
            } catch (Throwable ignored) {}
            customExpansion = null;
        }
    }

    @Nullable
    public String evaluate(@Nullable Player player, @NotNull String params) {
        String clean = params.trim();
        String lower = clean.toLowerCase();

        // 1. Direct match in registry (e.g. playerlevel_math-message-next)
        CustomPlaceholderEntry entry = placeholderRegistry.get(lower);
        if (entry != null) {
            return entry.evaluate(player);
        }

        // 2. Check fallback registry by key alone (e.g. math-message-next)
        entry = fallbackRegistry.get(lower);
        if (entry != null) {
            return entry.evaluate(player);
        }

        // 3. Strip prefixes if present: custom_, jobs_, seriajobs_
        if (lower.startsWith("custom_")) {
            String stripped = lower.substring(7);
            entry = placeholderRegistry.get(stripped);
            if (entry == null) entry = fallbackRegistry.get(stripped);
            if (entry != null) return entry.evaluate(player);
        }

        if (lower.startsWith("seriajobs_")) {
            String stripped = lower.substring(10);
            entry = placeholderRegistry.get(stripped);
            if (entry == null) entry = fallbackRegistry.get(stripped);
            if (entry != null) return entry.evaluate(player);
        }

        return null;
    }

    public Map<String, CustomPlaceholderEntry> getPlaceholderRegistry() {
        return Collections.unmodifiableMap(placeholderRegistry);
    }
}
