package id.seria.jobs.config;

import id.seria.jobs.SeriaJobs;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuRegistry {

    private final SeriaJobs plugin;
    private final Map<String, FileConfiguration> menuCache = new HashMap<>();

    public MenuRegistry(SeriaJobs plugin) {
        this.plugin = plugin;
    }

    public void load() {
        menuCache.clear();
        File menuFolder = new File(plugin.getDataFolder(), "menus");
        if (!menuFolder.exists()) {
            menuFolder.mkdirs();
        }

        // Save default menus
        saveDefaultMenu("main_jobs_menu.yml");
        saveDefaultMenu("job_orders_gui.yml");
        saveDefaultMenu("job_progression_gui.yml");

        scanMenuDirectory(menuFolder, menuFolder);
        plugin.getLogger().info("Loaded " + menuCache.size() + " GUI menu configuration key(s) from menus/ directory and subfolders.");
    }

    private void scanMenuDirectory(File root, File current) {
        File[] files = current.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanMenuDirectory(root, file);
            } else if (file.getName().endsWith(".yml") || file.getName().endsWith(".yaml")) {
                try {
                    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                    // Store by simple filename (e.g. "main_jobs_menu.yml")
                    menuCache.put(file.getName().toLowerCase(), config);

                    // Store by relative path (e.g. "jobs/custom_menu.yml")
                    String relative = root.toURI().relativize(file.toURI()).getPath().toLowerCase().replace('\\', '/');
                    menuCache.put(relative, config);

                    // Dynamic open-commands registration
                    List<String> openCmds = config.getStringList("open-commands.register");
                    if (!openCmds.isEmpty()) {
                        String menuFile = file.getName().toLowerCase();
                        id.seria.core.gui.menu.DynamicCommandRegistrar.registerMenuCommands(plugin, openCmds, player -> {
                            if (menuFile.equals("main_jobs_menu.yml")) {
                                new id.seria.jobs.ui.MainJobsMenu(plugin, player).open();
                            } else {
                                new id.seria.jobs.ui.MainJobsMenu(plugin, player).open();
                            }
                        });
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load menu file " + file.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    private void saveDefaultMenu(String name) {
        File target = new File(plugin.getDataFolder(), "menus/" + name);
        if (!target.exists()) {
            plugin.saveResource("menus/" + name, false);
        }
    }

    public FileConfiguration getMenu(String name) {
        if (name == null) return null;
        String normalized = name.toLowerCase().trim().replace('\\', '/');
        return menuCache.get(normalized);
    }

    public Map<String, FileConfiguration> getMenuCache() {
        return menuCache;
    }
}
