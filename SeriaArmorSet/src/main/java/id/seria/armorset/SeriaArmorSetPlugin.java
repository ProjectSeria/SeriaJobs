package id.seria.armorset;

import id.seria.armorset.command.SetTelepathyCommand;
import id.seria.armorset.config.ArmorSetConfig;
import id.seria.armorset.hook.MMOItemsHook;
import id.seria.armorset.hook.MythicMobsHook;
import id.seria.armorset.hook.SeriaCoreHook;
import id.seria.armorset.listener.BlockDropListener;
import id.seria.armorset.listener.EntityDeathListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class SeriaArmorSetPlugin extends JavaPlugin {

    private static SeriaArmorSetPlugin instance;

    private ArmorSetConfig configuration;
    private MMOItemsHook mmoItemsHook;
    private MythicMobsHook mythicMobsHook;
    private SeriaCoreHook seriaCoreHook;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // 1. Initialize Configuration
        configuration = new ArmorSetConfig(this);
        configuration.reload();

        // 2. Initialize Hooks
        mmoItemsHook = new MMOItemsHook(this);
        seriaCoreHook = new SeriaCoreHook(this);
        seriaCoreHook.initialize();

        mythicMobsHook = new MythicMobsHook(this);
        mythicMobsHook.initialize();

        // 3. Register Event Listeners
        getServer().getPluginManager().registerEvents(new BlockDropListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDeathListener(this), this);

        // 4. Register Commands
        PluginCommand cmd = getCommand("settelepathy");
        if (cmd != null) {
            SetTelepathyCommand handler = new SetTelepathyCommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }

        getLogger().info("=================================================");
        getLogger().info(" SeriaArmorSet (SetTelepathy) v" + getDescription().getVersion() + " Enabled!");
        getLogger().info(" Active Sets: " + configuration.getSets().keySet());
        getLogger().info(" SeriaCore: " + (seriaCoreHook.isAvailable() ? "HOOKED" : "NOT FOUND"));
        getLogger().info(" MythicMobs: " + (mythicMobsHook.isAvailable() ? "HOOKED" : "NOT FOUND"));
        getLogger().info("=================================================");
    }

    @Override
    public void onDisable() {
        if (seriaCoreHook != null) {
            seriaCoreHook.unregister();
        }
        getLogger().info("SeriaArmorSet disabled.");
    }

    public void reload() {
        configuration.reload();
    }

    public static SeriaArmorSetPlugin getInstance() {
        return instance;
    }

    public ArmorSetConfig getConfiguration() {
        return configuration;
    }

    public MMOItemsHook getMmoItemsHook() {
        return mmoItemsHook;
    }

    public MythicMobsHook getMythicMobsHook() {
        return mythicMobsHook;
    }

    public SeriaCoreHook getSeriaCoreHook() {
        return seriaCoreHook;
    }
}