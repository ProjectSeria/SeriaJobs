package id.seria.bossevent;

import id.seria.bossevent.command.SbeCommand;
import id.seria.bossevent.config.ConfigManager;
import id.seria.bossevent.config.EventConfigLoader;
import id.seria.bossevent.display.MessageService;
import id.seria.bossevent.event.BossSpawner;
import id.seria.bossevent.event.EventManager;
import id.seria.bossevent.hook.DiscordHook;
import id.seria.bossevent.hook.PlaceholderHook;
import id.seria.bossevent.leaderboard.LeaderboardService;
import id.seria.bossevent.listener.BossListener;
import id.seria.bossevent.listener.DamageListener;
import id.seria.bossevent.listener.PlayerJoinListener;
import id.seria.bossevent.reward.RewardService;
import id.seria.bossevent.service.PlayerInfoService;
import id.seria.bossevent.service.ResultStore;
import id.seria.bossevent.tracker.HpPollTask;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class SeriaBossEventPlugin extends JavaPlugin {

    private static SeriaBossEventPlugin instance;

    private ConfigManager configManager;
    private MessageService messageService;
    private PlayerInfoService playerInfoService;
    private ResultStore resultStore;
    private EventConfigLoader eventConfigLoader;
    private BossSpawner bossSpawner;
    private HpPollTask hpPollTask;
    private LeaderboardService leaderboardService;
    private RewardService rewardService;
    private EventManager eventManager;

    private DiscordHook discordHook;
    private PlaceholderHook placeholderHook;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Core configurations and utilities
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();

        this.messageService = new MessageService(this);
        this.playerInfoService = new PlayerInfoService(this);
        this.resultStore = new ResultStore(this);

        // 2. Logic services
        this.leaderboardService = new LeaderboardService(this);
        this.rewardService = new RewardService(this);
        this.bossSpawner = new BossSpawner(this);
        this.hpPollTask = new HpPollTask(this);
        this.hpPollTask.start();

        // 3. Load events
        this.eventConfigLoader = new EventConfigLoader(this);
        this.eventConfigLoader.loadEvents();

        this.eventManager = new EventManager(this);
        this.eventManager.initEvents();

        // 4. Register Bukkit listeners
        Bukkit.getPluginManager().registerEvents(new DamageListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BossListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // 5. Register command
        PluginCommand cmd = getCommand("seriabossevent");
        if (cmd != null) {
            SbeCommand executor = new SbeCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        // 6. Optional hooks
        if (DiscordHook.isAvailable()) {
            this.discordHook = new DiscordHook(this);
            getLogger().info("[SeriaBossEvent] Hooked into DiscordSRV successfully.");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.placeholderHook = new PlaceholderHook(this);
            this.placeholderHook.register();
            getLogger().info("[SeriaBossEvent] Hooked into PlaceholderAPI (%seriabossevent_*%).");
        }

        getLogger().info("[SeriaBossEvent] Enabled successfully with " + eventConfigLoader.getEvents().size() + " configured boss events.");
    }

    @Override
    public void onDisable() {
        // 1. Cleanly stop and cleanup all active events
        if (eventManager != null) {
            eventManager.stopAll();
        }

        // 2. Stop HP poll task
        if (hpPollTask != null) {
            hpPollTask.stop();
        }

        // 3. Save pending rewards sync
        if (resultStore != null) {
            resultStore.savePendingRewardsAsync();
        }

        // 4. Unregister PlaceholderHook
        if (placeholderHook != null) {
            try {
                placeholderHook.unregister();
            } catch (Throwable ignored) {}
        }

        getLogger().info("[SeriaBossEvent] Disabled cleanly. All entities and tasks released.");
    }

    public static SeriaBossEventPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public PlayerInfoService getPlayerInfoService() {
        return playerInfoService;
    }

    public ResultStore getResultStore() {
        return resultStore;
    }

    public EventConfigLoader getEventConfigLoader() {
        return eventConfigLoader;
    }

    public BossSpawner getBossSpawner() {
        return bossSpawner;
    }

    public HpPollTask getHpPollTask() {
        return hpPollTask;
    }

    public LeaderboardService getLeaderboardService() {
        return leaderboardService;
    }

    public RewardService getRewardService() {
        return rewardService;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public DiscordHook getDiscordHook() {
        return discordHook;
    }

    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }
}
