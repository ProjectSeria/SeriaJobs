package id.seria.jobs.hook;

import id.seria.core.SeriaCorePlugin;
import id.seria.core.placeholder.CustomPlaceholderManager;
import id.seria.core.placeholder.SeriaPlaceholderProvider;
import id.seria.jobs.SeriaJobs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class HookManager {

    private final SeriaJobs plugin;

    private LuckPermsHook luckPermsHook;
    private AuraSkillsHook auraSkillsHook;
    private CustomFishingHook customFishingHook;
    private SeriaMagicBundleHook seriaMagicBundleHook;
    private SeriaCraftingHook seriaCraftingHook;
    private SeriaTranslatorHook seriaTranslatorHook;
    private PAPIExpansion papiExpansion;
    private JobsPlaceholderExpansion jobsOrderExpansion;

    private boolean luckPermsLoaded = false;
    private boolean auraSkillsLoaded = false;
    private boolean customFishingLoaded = false;
    private boolean seriaMagicBundleLoaded = false;
    private boolean seriaCraftingLoaded = false;
    private boolean seriaTranslatorLoaded = false;
    private boolean papiLoaded = false;

    public HookManager(SeriaJobs plugin) {
        this.plugin = plugin;
    }

    public void init() {
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

        registerToSeriaCore();
    }

    /**
     * Daftarkan provider %seriajobs_*% / %jobsorder_*% dan folder placeholder/ SeriaJobs ke engine SeriaCore.
     */
    private void registerToSeriaCore() {
        CustomPlaceholderManager placeholders = SeriaCorePlugin.getInstance().getPlaceholderManager();
        placeholders.registerProvider(new SeriaPlaceholderProvider() {
            @Override
            public @NotNull String getPrefix() {
                return "seriajobs";
            }

            @Override
            public @Nullable String onRequest(@Nullable Player player, @NotNull String params) {
                return papiExpansion.onRequest(player, params);
            }
        });
        placeholders.registerProvider(new SeriaPlaceholderProvider() {
            @Override
            public @NotNull String getPrefix() {
                return "jobsorder";
            }

            @Override
            public @Nullable String onRequest(@Nullable Player player, @NotNull String params) {
                return jobsOrderExpansion.onRequest(player, params);
            }
        });

        File folder = new File(plugin.getDataFolder(), "placeholder");
        if (folder.isDirectory()) {
            placeholders.registerFolder(folder);
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
