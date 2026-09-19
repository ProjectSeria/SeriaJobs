package id.seria.jobs;

import id.seria.jobs.command.JobsCommand;
import id.seria.jobs.config.ConfigManager;
import id.seria.jobs.config.LanguageManager;
import id.seria.jobs.config.MenuRegistry;
import id.seria.jobs.hook.HookManager;
import id.seria.jobs.manager.JobRegistry;
import id.seria.jobs.manager.LeaderboardManager;
import id.seria.jobs.manager.OrderPoolManager;
import id.seria.jobs.manager.PlayerConnectionListener;
import id.seria.jobs.manager.PlayerDataManager;
import id.seria.jobs.manager.SlotPermissionManager;
import id.seria.jobs.ui.MenuListener;
import id.seria.jobs.utils.TranslationExporter;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SeriaJobs extends JavaPlugin {

    private static SeriaJobs instance;

    private ConfigManager configManager;
    private LanguageManager languageManager;
    private MenuRegistry menuRegistry;
    private HookManager hookManager;
    private JobRegistry jobRegistry;
    private OrderPoolManager orderPoolManager;
    private PlayerDataManager playerDataManager;
    private SlotPermissionManager slotPermissionManager;
    private LeaderboardManager leaderboardManager;

    @Override
    public void onEnable() {
        instance = this;
        long startTime = System.currentTimeMillis();

        getLogger().info("Initializing SeriaJobs core systems...");

        // 1. Config and Languages
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        this.languageManager = new LanguageManager(this);
        this.languageManager.load();

        this.menuRegistry = new MenuRegistry(this);
        this.menuRegistry.load();

        // 2. Export translation folders (cache/, translator/, translations/)
        TranslationExporter.setupTranslationFolders(this);

        // 3. Hooks (Vault, AuraSkills, MMOItems, SeriaCore, SeriaTranslator, PAPI)
        this.hookManager = new HookManager(this);
        this.hookManager.init();

        // 4. Managers
        this.playerDataManager = new PlayerDataManager(this);
        this.playerDataManager.init();

        this.jobRegistry = new JobRegistry(this);
        this.jobRegistry.load();

        this.slotPermissionManager = new SlotPermissionManager(this);

        this.orderPoolManager = new OrderPoolManager(this);
        this.orderPoolManager.init();

        this.leaderboardManager = new LeaderboardManager(this);
        this.leaderboardManager.init();

        // 5. Register Listeners
        Bukkit.getPluginManager().registerEvents(new MenuListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(this), this);

        // 6. Register Commands
        JobsCommand jobsCommand = new JobsCommand(this);
        PluginCommand cmd = getCommand("seriajobs");
        if (cmd != null) {
            cmd.setExecutor(jobsCommand);
            cmd.setTabCompleter(jobsCommand);
        }

        // 7. Load profiles for online players if reloaded
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            playerDataManager.loadProfileAsync(onlinePlayer.getUniqueId(), onlinePlayer.getName(), null);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        getLogger().info("SeriaJobs enabled successfully in " + elapsed + "ms!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling SeriaJobs...");

        if (leaderboardManager != null) {
            leaderboardManager.shutdown();
        }

        if (hookManager != null) {
            hookManager.unregister();
        }

        if (orderPoolManager != null) {
            orderPoolManager.shutdown();
        }

        if (playerDataManager != null) {
            playerDataManager.shutdown();
        }

        getLogger().info("SeriaJobs disabled cleanly.");
    }

    public void reload() {
        configManager.load();
        languageManager.load();
        menuRegistry.load();
        jobRegistry.load();
        TranslationExporter.setupTranslationFolders(this);
        getLogger().info("SeriaJobs configurations and job registries reloaded.");
    }

    public static SeriaJobs getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public MenuRegistry getMenuRegistry() {
        return menuRegistry;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public JobRegistry getJobRegistry() {
        return jobRegistry;
    }

    public OrderPoolManager getOrderPoolManager() {
        return orderPoolManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public SlotPermissionManager getSlotPermissionManager() {
        return slotPermissionManager;
    }

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }
}
