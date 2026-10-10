package id.seria.mmoae;

import id.seria.mmoae.command.MMOAECommand;
import id.seria.mmoae.hook.AEHook;
import id.seria.mmoae.hook.MMOItemsHook;
import id.seria.mmoae.listener.ItemPipelineListener;
import id.seria.mmoae.listener.MMOItemReforgeListener;
import id.seria.mmoae.service.AEBridgeService;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class SeriaMMOItemsAEPlugin extends JavaPlugin {

    private static SeriaMMOItemsAEPlugin instance;
    private AEBridgeService bridgeService;
    private ItemPipelineListener pipelineListener;
    private BukkitTask scanTask;

    @Override
    public void onLoad() {
        instance = this;
        // Register custom stat early during plugin load cycle
        MMOItemsHook.initialize();
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Initialize hooks
        if (!MMOItemsHook.isAvailable()) {
            MMOItemsHook.initialize();
        }
        AEHook.initialize();

        reloadServicesAndListeners();

        // Pre-process MMOItems templates to resolve placeholders in names & lore
        if (MMOItemsHook.isAvailable()) {
            MMOItemsHook.preProcessAllTemplates();
            // Also run a short delayed task to catch any templates loaded asynchronously
            Bukkit.getScheduler().runTaskLater(this, MMOItemsHook::preProcessAllTemplates, 20L);
        }

        // Register commands
        PluginCommand cmd = getCommand("mmoae");
        if (cmd != null) {
            MMOAECommand handler = new MMOAECommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }

        getLogger().info("=========================================");
        getLogger().info(" SeriaMMOItemsAE v" + getDescription().getVersion() + " Enabled!");
        getLogger().info(" MMOItems Status: " + (MMOItemsHook.isAvailable() ? "HOOKED" : "NOT FOUND"));
        getLogger().info(" AdvancedEnchantments Status: " + (AEHook.isAvailable() ? "HOOKED" : "NOT FOUND"));
        getLogger().info("=========================================");
    }

    @Override
    public void onDisable() {
        if (scanTask != null) {
            scanTask.cancel();
            scanTask = null;
        }
        HandlerList.unregisterAll(this);
        getLogger().info("SeriaMMOItemsAE disabled.");
    }

    public void reloadPluginConfig() {
        reloadConfig();
        MMOItemsHook.clearCache();
        AEHook.initialize();
        reloadServicesAndListeners();
        if (MMOItemsHook.isAvailable()) {
            MMOItemsHook.preProcessAllTemplates();
        }
    }

    private void reloadServicesAndListeners() {
        if (scanTask != null) {
            scanTask.cancel();
            scanTask = null;
        }
        HandlerList.unregisterAll(this);

        boolean debug = getConfig().getBoolean("debug", false);
        this.bridgeService = new AEBridgeService(debug);

        boolean onPickup = getConfig().getBoolean("auto-apply.on-pickup", true);
        boolean onCraft = getConfig().getBoolean("auto-apply.on-craft", true);
        boolean onInventoryClick = getConfig().getBoolean("auto-apply.on-inventory-click", true);
        boolean onItemHeld = getConfig().getBoolean("auto-apply.on-item-held", true);
        boolean onJoin = getConfig().getBoolean("auto-apply.on-join", true);
        boolean onInteract = getConfig().getBoolean("auto-apply.on-interact", false);
        boolean onAttack = getConfig().getBoolean("auto-apply.on-attack", false);
        boolean onReforge = getConfig().getBoolean("auto-apply.on-reforge", true);
        boolean periodicScan = getConfig().getBoolean("auto-apply.periodic-inventory-scan", false);
        boolean preservePlayerEnchants = getConfig().getBoolean("preserve-player-enchants-on-reforge", true);

        // Register Listeners
        this.pipelineListener = new ItemPipelineListener(
                bridgeService,
                onPickup,
                onCraft,
                onInventoryClick,
                onItemHeld,
                onJoin,
                onInteract,
                onAttack
        );
        Bukkit.getPluginManager().registerEvents(pipelineListener, this);

        Bukkit.getPluginManager().registerEvents(
                new MMOItemReforgeListener(bridgeService, onReforge, preservePlayerEnchants),
                this
        );

        // Register MMOItems reload detector
        Bukkit.getPluginManager().registerEvents(
                new id.seria.mmoae.listener.MMOItemsReloadListener(this),
                this
        );

        // Periodic scanner for online players to guarantee live item enchantment updates
        if (periodicScan) {
            scanTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
                if (!AEHook.isAvailable()) return;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (pipelineListener != null) {
                        pipelineListener.checkAndFixPlayerEquipment(player);
                    }
                }
            }, 20L, 20L); // every 1 second
        }
    }

    public static SeriaMMOItemsAEPlugin getInstance() {
        return instance;
    }

    public AEBridgeService getBridgeService() {
        return bridgeService;
    }
}
