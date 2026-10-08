package id.seria.mmoae;

import id.seria.mmoae.command.MMOAECommand;
import id.seria.mmoae.hook.AEHook;
import id.seria.mmoae.hook.MMOItemsHook;
import id.seria.mmoae.listener.ItemPipelineListener;
import id.seria.mmoae.listener.MMOItemReforgeListener;
import id.seria.mmoae.service.AEBridgeService;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class SeriaMMOItemsAEPlugin extends JavaPlugin {

    private static SeriaMMOItemsAEPlugin instance;
    private AEBridgeService bridgeService;

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
        HandlerList.unregisterAll(this);
        getLogger().info("SeriaMMOItemsAE disabled.");
    }

    public void reloadPluginConfig() {
        reloadConfig();
        AEHook.initialize();
        reloadServicesAndListeners();
    }

    private void reloadServicesAndListeners() {
        HandlerList.unregisterAll(this);

        boolean debug = getConfig().getBoolean("debug", false);
        this.bridgeService = new AEBridgeService(debug);

        boolean onPickup = getConfig().getBoolean("auto-apply.on-pickup", true);
        boolean onCraft = getConfig().getBoolean("auto-apply.on-craft", true);
        boolean onInventoryClick = getConfig().getBoolean("auto-apply.on-inventory-click", true);
        boolean onItemHeld = getConfig().getBoolean("auto-apply.on-item-held", true);
        boolean onJoin = getConfig().getBoolean("auto-apply.on-join", true);
        boolean onReforge = getConfig().getBoolean("auto-apply.on-reforge", true);
        boolean preservePlayerEnchants = getConfig().getBoolean("preserve-player-enchants-on-reforge", true);

        // Register Listeners
        Bukkit.getPluginManager().registerEvents(
                new ItemPipelineListener(bridgeService, onPickup, onCraft, onInventoryClick, onItemHeld, onJoin),
                this
        );

        Bukkit.getPluginManager().registerEvents(
                new MMOItemReforgeListener(bridgeService, onReforge, preservePlayerEnchants),
                this
        );
    }

    public static SeriaMMOItemsAEPlugin getInstance() {
        return instance;
    }

    public AEBridgeService getBridgeService() {
        return bridgeService;
    }
}
