package id.seria.jobs;

import id.seria.core.SeriaCorePlugin;
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
import id.seria.jobs.utils.TranslationExporter;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

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

    // Blueprint Services & Managers
    private id.seria.jobs.service.EconomyService economyService;
    private id.seria.jobs.service.ActionRunnerService actionRunnerService;
    private id.seria.jobs.service.ReputationBridgeService reputationBridgeService;
    private id.seria.jobs.service.OrderGenerationService orderGenerationService;
    private id.seria.jobs.manager.JobsManager jobsManager;

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

        // 3. Hooks (AuraSkills, MMOItems, SeriaTranslator, PAPI) + registrasi placeholder ke SeriaCore
        saveDefaultPlaceholders();
        this.hookManager = new HookManager(this);
        this.hookManager.init();

        // 4. Core Blueprint Services
        this.economyService = new id.seria.jobs.service.EconomyService();
        this.actionRunnerService = new id.seria.jobs.service.ActionRunnerService();
        this.reputationBridgeService = new id.seria.jobs.service.ReputationBridgeService(this);
        this.orderGenerationService = new id.seria.jobs.service.OrderGenerationService(this);

        // 5. Managers
        this.playerDataManager = new PlayerDataManager(this);
        this.playerDataManager.init();

        this.jobRegistry = new JobRegistry(this);
        this.jobRegistry.load();

        this.slotPermissionManager = new SlotPermissionManager(this);

        this.orderPoolManager = new OrderPoolManager(this);
        this.orderPoolManager.init();

        this.leaderboardManager = new LeaderboardManager(this);
        this.leaderboardManager.init();

        this.jobsManager = new id.seria.jobs.manager.JobsManager(
                this,
                this.economyService,
                this.hookManager.getAuraSkillsHook(),
                this.actionRunnerService,
                this.reputationBridgeService,
                this.playerDataManager,
                this.orderGenerationService
        );

        // 6. Register Listeners (menu clicks are handled by SeriaCore's SeriaMenuListener)
        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(this), this);

        // 7. Register Commands
        JobsCommand jobsCommand = new JobsCommand(this);
        PluginCommand cmd = getCommand("seriajobs");
        if (cmd != null) {
            cmd.setExecutor(jobsCommand);
            cmd.setTabCompleter(jobsCommand);
        }

        // 8. Load profiles for online players if reloaded
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
        SeriaCorePlugin.getInstance().getPlaceholderManager().load();
        if (reputationBridgeService != null) {
            reputationBridgeService.reload();
        }
        TranslationExporter.setupTranslationFolders(this);
        getLogger().info("SeriaJobs configurations and job registries reloaded.");
    }

    /**
     * Salin template placeholder/ bawaan bila belum ada. Isinya dimuat dan dievaluasi oleh engine SeriaCore.
     */
    private void saveDefaultPlaceholders() {
        for (String name : new String[]{"playerlevel.yml", "reroll.yml", "progressbar.yml"}) {
            if (!new File(getDataFolder(), "placeholder/" + name).exists() && getResource("placeholder/" + name) != null) {
                saveResource("placeholder/" + name, false);
            }
        }
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

    public id.seria.jobs.service.EconomyService getEconomyService() {
        return economyService;
    }

    public id.seria.jobs.service.ActionRunnerService getActionRunnerService() {
        return actionRunnerService;
    }

    public id.seria.jobs.service.ReputationBridgeService getReputationBridgeService() {
        return reputationBridgeService;
    }

    public id.seria.jobs.service.OrderGenerationService getOrderGenerationService() {
        return orderGenerationService;
    }

    public id.seria.jobs.manager.JobsManager getJobsManager() {
        return jobsManager;
    }
}
