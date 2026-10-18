package id.seria.jobs.hook;

import id.seria.jobs.SeriaJobs;
import org.bukkit.Bukkit;

public class HookManager {

    private final SeriaJobs plugin;

    private VaultHook vaultHook;
    private ExcellentEconomyHook excellentEconomyHook;
    private LuckPermsHook luckPermsHook;
    private AuraSkillsHook auraSkillsHook;
    private MMOItemsHook mmoItemsHook;
    private MythicMobsHook mythicMobsHook;
    private CustomFishingHook customFishingHook;
    private SeriaMagicBundleHook seriaMagicBundleHook;
    private SeriaCraftingHook seriaCraftingHook;
    private SeriaCoreHook seriaCoreHook;
    private SeriaTranslatorHook seriaTranslatorHook;
    private PAPIExpansion papiExpansion;
    private JobsPlaceholderExpansion jobsOrderExpansion;

    private boolean vaultLoaded = false;
    private boolean excellentEconomyLoaded = false;
    private boolean luckPermsLoaded = false;
    private boolean auraSkillsLoaded = false;
    private boolean mmoItemsLoaded = false;
    private boolean mythicMobsLoaded = false;
    private boolean customFishingLoaded = false;
    private boolean seriaMagicBundleLoaded = false;
    private boolean seriaCraftingLoaded = false;
    private boolean seriaCoreLoaded = false;
    private boolean seriaTranslatorLoaded = false;
    private boolean papiLoaded = false;

    public HookManager(SeriaJobs plugin) {
        this.plugin = plugin;
    }

    public void init() {
        // Vault Hook
        if (plugin.getConfigManager().isHookEnabled("vault")) {
            vaultHook = new VaultHook();
            if (vaultHook.setup()) {
                vaultLoaded = true;
                plugin.getLogger().info("Successfully hooked into Vault Economy.");
            }
        }

        // ExcellentEconomy Hook
        if (plugin.getConfigManager().isHookEnabled("excellenteconomy")) {
            excellentEconomyHook = new ExcellentEconomyHook();
            if (excellentEconomyHook.setup()) {
                excellentEconomyLoaded = true;
                plugin.getLogger().info("Successfully hooked into ExcellentEconomy API.");
            }
        }

        // LuckPerms Hook
        if (plugin.getConfigManager().isHookEnabled("luckperms")) {
            luckPermsHook = new LuckPermsHook();
            if (luckPermsHook.setup()) {
                luckPermsLoaded = true;
                plugin.getLogger().info("Successfully hooked into LuckPerms API.");
            }
        }

        // AuraSkills Hook
        if (plugin.getConfigManager().isHookEnabled("auraskills")) {
            auraSkillsHook = new AuraSkillsHook();
            if (auraSkillsHook.setup()) {
                auraSkillsLoaded = true;
                plugin.getLogger().info("Successfully hooked into AuraSkills API.");
            }
        }

        // MMOItems Hook
        if (plugin.getConfigManager().isHookEnabled("mmoitems")) {
            mmoItemsHook = new MMOItemsHook();
            if (mmoItemsHook.setup()) {
                mmoItemsLoaded = true;
                plugin.getLogger().info("Successfully hooked into MMOItems & MythicLib.");
            }
        }

        // MythicMobs Hook
        if (plugin.getConfigManager().isHookEnabled("mythicmobs")) {
            mythicMobsHook = new MythicMobsHook();
            if (mythicMobsHook.setup()) {
                mythicMobsLoaded = true;
                plugin.getLogger().info("Successfully hooked into MythicMobs item engine.");
            }
        }

        // CustomFishing Hook
        if (plugin.getConfigManager().isHookEnabled("customfishing")) {
            customFishingHook = new CustomFishingHook();
            if (customFishingHook.setup()) {
                customFishingLoaded = true;
                plugin.getLogger().info("Successfully hooked into Custom-Fishing by XiaoMoMi.");
            }
        }

        // SeriaMagicBundle Hook
        if (plugin.getConfigManager().isHookEnabled("seriamagicbundle") && Bukkit.getPluginManager().getPlugin("SeriaMagicBundle") != null) {
            try {
                seriaMagicBundleHook = new SeriaMagicBundleHook();
                if (seriaMagicBundleHook.setup()) {
                    seriaMagicBundleLoaded = true;
                    plugin.getLogger().info("Successfully hooked into SeriaMagicBundle.");
                }
            } catch (Throwable t) {
                plugin.getLogger().warning("Could not hook into SeriaMagicBundle: " + t.getMessage());
            }
        }

        // SeriaCrafting Hook
        if (plugin.getConfigManager().isHookEnabled("seriacrafting") && Bukkit.getPluginManager().getPlugin("SeriaCrafting") != null) {
            try {
                seriaCraftingHook = new SeriaCraftingHook();
                if (seriaCraftingHook.setup()) {
                    seriaCraftingLoaded = true;
                    plugin.getLogger().info("Successfully hooked into SeriaCrafting.");
                }
            } catch (Throwable t) {
                plugin.getLogger().warning("Could not hook into SeriaCrafting: " + t.getMessage());
            }
        }

        // SeriaCore Hook (Backend)
        if (plugin.getConfigManager().isHookEnabled("seriacore.enabled") && Bukkit.getPluginManager().getPlugin("SeriaCore") != null) {
            try {
                seriaCoreHook = new SeriaCoreHook();
                if (seriaCoreHook.setup()) {
                    seriaCoreLoaded = true;
                    plugin.getLogger().info("Successfully hooked into SeriaCore backbone.");
                }
            } catch (Throwable t) {
                plugin.getLogger().warning("Could not hook into SeriaCore: " + t.getMessage());
            }
        }

        // SeriaTranslator Hook (Localization)
        if (plugin.getConfigManager().isHookEnabled("seriatranslator.enabled") && Bukkit.getPluginManager().getPlugin("SeriaTranslator") != null) {
            try {
                seriaTranslatorHook = new SeriaTranslatorHook();
                if (seriaTranslatorHook.setup()) {
                    seriaTranslatorLoaded = true;
                    plugin.getLogger().info("Successfully hooked into SeriaTranslator / TritonAPI.");
                }
            } catch (Throwable t) {
                plugin.getLogger().warning("Could not hook into SeriaTranslator: " + t.getMessage());
            }
        }

        // PlaceholderAPI Hook & SeriaCore Internal Provider
        papiExpansion = new PAPIExpansion(plugin);
        jobsOrderExpansion = new JobsPlaceholderExpansion(plugin);
        if (plugin.getConfigManager().isHookEnabled("placeholderapi") && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            if (papiExpansion.register()) {
                papiLoaded = true;
                plugin.getLogger().info("Registered %seriajobs% placeholders to PlaceholderAPI.");
            }
            if (jobsOrderExpansion.register()) {
                plugin.getLogger().info("Registered %jobsorder% placeholders to PlaceholderAPI.");
            }
        }

        // Connect to SeriaCore hierarchy (internal placeholders and custom folder)
        if (seriaCoreLoaded && seriaCoreHook != null) {
            seriaCoreHook.registerToSeriaCore(plugin, papiExpansion);
        }
    }

    public void unregister() {
        if (papiExpansion != null && papiLoaded) {
            papiExpansion.unregister();
        }
        if (jobsOrderExpansion != null) {
            jobsOrderExpansion.unregister();
        }
    }

    public boolean isVaultLoaded() {
        return vaultLoaded;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public boolean isExcellentEconomyLoaded() {
        return excellentEconomyLoaded && (excellentEconomyHook != null && excellentEconomyHook.isLoaded());
    }

    public ExcellentEconomyHook getExcellentEconomyHook() {
        return excellentEconomyHook;
    }

    public boolean isLuckPermsLoaded() {
        return luckPermsLoaded && (luckPermsHook != null && luckPermsHook.isLoaded());
    }

    public LuckPermsHook getLuckPermsHook() {
        return luckPermsHook;
    }

    public boolean isAuraSkillsLoaded() {
        return auraSkillsLoaded;
    }

    public AuraSkillsHook getAuraSkillsHook() {
        return auraSkillsHook;
    }

    public boolean isMmoItemsLoaded() {
        return mmoItemsLoaded;
    }

    public boolean isMMOItemsLoaded() {
        return mmoItemsLoaded;
    }

    public MMOItemsHook getMmoItemsHook() {
        return mmoItemsHook;
    }

    public MMOItemsHook getMMOItemsHook() {
        return mmoItemsHook;
    }

    public boolean isMythicMobsLoaded() {
        return mythicMobsLoaded;
    }

    public MythicMobsHook getMythicMobsHook() {
        return mythicMobsHook;
    }

    public boolean isCustomFishingLoaded() {
        return customFishingLoaded;
    }

    public CustomFishingHook getCustomFishingHook() {
        return customFishingHook;
    }

    public boolean isSeriaMagicBundleLoaded() {
        return seriaMagicBundleLoaded;
    }

    public SeriaMagicBundleHook getSeriaMagicBundleHook() {
        return seriaMagicBundleHook;
    }

    public boolean isSeriaCraftingLoaded() {
        return seriaCraftingLoaded;
    }

    public SeriaCraftingHook getSeriaCraftingHook() {
        return seriaCraftingHook;
    }

    public boolean isSeriaCoreLoaded() {
        return seriaCoreLoaded;
    }

    public SeriaCoreHook getSeriaCoreHook() {
        return seriaCoreHook;
    }

    public boolean isSeriaTranslatorLoaded() {
        return seriaTranslatorLoaded;
    }

    public SeriaTranslatorHook getSeriaTranslatorHook() {
        return seriaTranslatorHook;
    }

    public boolean isPapiLoaded() {
        return papiLoaded;
    }

    public JobsPlaceholderExpansion getJobsOrderExpansion() {
        return jobsOrderExpansion;
    }

    public PAPIExpansion getPapiExpansion() {
        return papiExpansion;
    }
}
