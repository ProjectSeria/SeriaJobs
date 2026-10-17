package id.seria.armorset;

import id.seria.armorset.command.ArmorSetCommand;
import id.seria.armorset.config.ArmorSetConfig;
import id.seria.armorset.hook.MMOItemsHook;
import id.seria.armorset.hook.MythicMobsHook;
import id.seria.armorset.hook.SeriaCoreHook;
import id.seria.armorset.listener.BlockDropListener;
import id.seria.armorset.listener.EntityDeathListener;
import id.seria.armorset.listener.PlayerCombatListener;
import id.seria.armorset.listener.PlayerEquipmentListener;
import id.seria.armorset.skill.SkillManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class SeriaArmorSetPlugin extends JavaPlugin {

    private static SeriaArmorSetPlugin instance;

    private ArmorSetConfig configuration;
    private SkillManager skillManager;
    private MMOItemsHook mmoItemsHook;
    private MythicMobsHook mythicMobsHook;
    private SeriaCoreHook seriaCoreHook;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // 1. Initialize Skill Manager & Registry
        skillManager = new SkillManager(this);
        skillManager.initialize();

        // 2. Initialize Configuration & Sets
        configuration = new ArmorSetConfig(this);
        configuration.reload();

        // 3. Initialize Hooks
        mmoItemsHook = new MMOItemsHook(this);
        seriaCoreHook = new SeriaCoreHook(this);
        seriaCoreHook.initialize();

        mythicMobsHook = new MythicMobsHook(this);
        mythicMobsHook.initialize();

        // 4. Register Event Listeners
        getServer().getPluginManager().registerEvents(new PlayerEquipmentListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockDropListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerCombatListener(this), this);

        // 5. Register Commands
        ArmorSetCommand commandHandler = new ArmorSetCommand(this);
        registerCommand("armorset", commandHandler);
        registerCommand("settelepathy", commandHandler);

        getLogger().info("=================================================");
        getLogger().info(" SeriaArmorSet Skill Engine v" + getDescription().getVersion() + " Enabled!");
        getLogger().info(" Registered Sets: " + configuration.getSetDefinitions().keySet());
        getLogger().info(" Registered Skills: " + skillManager.getRegisteredSkills().size());
        getLogger().info(" SeriaCore: " + (seriaCoreHook.isAvailable() ? "HOOKED" : "NOT FOUND"));
        getLogger().info(" MythicMobs: " + (mythicMobsHook.isAvailable() ? "HOOKED" : "NOT FOUND"));
        getLogger().info(" Performance Cache: ACTIVE (O(1) in-memory resolution)");
        getLogger().info("=================================================");
    }

    private void registerCommand(String name, ArmorSetCommand handler) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }
    }

    @Override
    public void onDisable() {
        if (skillManager != null) {
            skillManager.unregisterAll();
        }
        if (seriaCoreHook != null) {
            seriaCoreHook.unregister();
        }
        if (mmoItemsHook != null) {
            mmoItemsHook.invalidateAll();
        }
        getLogger().info("SeriaArmorSet disabled.");
    }

    public void reload() {
        if (mmoItemsHook != null) {
            mmoItemsHook.invalidateAll();
        }
        configuration.reload();
    }

    public static SeriaArmorSetPlugin getInstance() {
        return instance;
    }

    public ArmorSetConfig getConfiguration() {
        return configuration;
    }

    public SkillManager getSkillManager() {
        return skillManager;
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