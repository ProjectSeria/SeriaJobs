package id.seria.gacha;

import id.seria.gacha.api.CratesPlugin;
import id.seria.gacha.api.PhoenixCratesAPI;
import id.seria.gacha.api.sdk.config.DatabaseConfiguration;
import id.seria.gacha.api.sdk.locales.Locale;
import id.seria.gacha.api.sdk.plugin.extensions.Extension;
import id.seria.gacha.commands.CrateCommandResolvers;
import id.seria.gacha.commands.CratesCommand;
import id.seria.gacha.configurations.SettingsConfiguration;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.BundledAddonsInstaller;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.Files;
import id.seria.gacha.internal.StringKeyStore;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.internal.language.Language;
import id.seria.gacha.lib.bstats.bukkit.Metrics;
import id.seria.gacha.lib.bstats.charts.SimplePie;
import id.seria.gacha.lib.common.services.Services;
import id.seria.gacha.lib.common.services.services.database.DatabaseProvider;
import id.seria.gacha.lib.common.uicomponents.newest.UiComponents;
import id.seria.gacha.lib.common.utils.api.ProductVersion;
import id.seria.gacha.lib.common.utils.configuration.objects.ComplexMaterial;
import id.seria.gacha.lib.common.utils.configuration.workers.AsyncQueueConfigurationWorker;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.ReflectiveClasses;
import id.seria.gacha.limbo.hooks.ModelEngineHook;
import id.seria.gacha.limbo.hooks.placeholders.PlaceholdersExpansion;
import id.seria.gacha.limbo.item.ItemStorage;
import id.seria.gacha.managers.CratesManager;
import id.seria.gacha.managers.KeysManager;
import id.seria.gacha.managers.PlayersManager;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.menus.MenuFactory;
import id.seria.gacha.patches.CratesPatches;
import id.seria.gacha.registries.AnimationsRegistry;
import id.seria.gacha.sdk.hooks.bettermodel.BetterModelHookProvider;
import id.seria.gacha.sdk.hooks.craftengine.CraftEngineHookProvider;
import id.seria.gacha.sdk.hooks.excellenteconomy.ExcellentEconomyHookProvider;
import id.seria.gacha.sdk.hooks.executableitems.ExecutableItemsHookProvider;
import id.seria.gacha.sdk.hooks.headdatabase.HeadDatabaseHookProvider;
import id.seria.gacha.sdk.hooks.itemsadder.ItemsAdderHookProvider;
import id.seria.gacha.sdk.hooks.minimessage.MiniMessageHookProvider;
import id.seria.gacha.sdk.hooks.mmoitems.MMOItemsHookProvider;
import id.seria.gacha.sdk.hooks.mythicitems.MythicItemsHookProvider;
import id.seria.gacha.sdk.hooks.mythicitems.MythicItemsProvider;
import id.seria.gacha.sdk.hooks.nexo.NexoHookProvider;
import id.seria.gacha.sdk.hooks.placeholderapi.PlaceholderHookProvider;
import id.seria.gacha.sdk.hooks.playerpoints.PlayerPointsHookProvider;
import id.seria.gacha.sdk.hooks.vault.VaultHookProvider;
import id.seria.gacha.sdk.hooks.votingplugin.VotingPluginHookProvider;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PhoenixCrates extends ServerPlugin implements CratesPlugin {
   private static PhoenixCrates INSTANCE;
   private final BukkitPlatform platform;
   private ItemStorage itemStorage;
   private final PlayersManager playersManager;
   private final KeysManager keysManager;
   private final CratesManager cratesManager;
   private DatabaseProvider databaseProvider;
   private boolean papiInstalled;
   private boolean modelEngineInstalled;
   private boolean modelEngineAvailable;
   private boolean betterModelInstalled;
   private boolean betterModelAvailable;

   @Deprecated
   public static PhoenixCrates getLegacyInstance() {
      return INSTANCE;
   }

   public Edition getEdition() {
      return "PhoenixCratesLite".equalsIgnoreCase(this.getDescription().getName()) ? Edition.LITE : Edition.PREMIUM;
   }

   public PhoenixCrates(BukkitPlatform platform) throws Exception {
      super(platform);
      this.platform = platform;
      INSTANCE = this;
      PhoenixCratesAPI.unsafe().setPluginInstance(this);
      PhoenixCratesAPI.unsafe().setAnimationRegistry(AnimationsRegistry.getInstance());
      if (this.getEdition() == Edition.LITE) {
         this.initializeLegacyLiteDataFolder();
      } else {
         BundledAddonsInstaller.ensurePresent(this);
      }

      this.itemStorage = new ItemStorage(this);
      if (this.getEdition() == Edition.PREMIUM) {
         this.getExtensionRegistry().registerExtensions(this.newPremiumOnlyExtension("id.seria.gacha.extensions.logger.LoggerExtension"));
         ReflectiveClasses.invokeStaticNoArgs("id.seria.gacha.editor.PremiumEditorBootstrap", "registerEditors");
      }

      this.playersManager = new PlayersManager(this);
      this.keysManager = new KeysManager(this);
      this.cratesManager = new CratesManager(this);
   }

   private Extension newPremiumOnlyExtension(String className) {
      try {
         Class<?> clazz = Class.forName(className);
         return (Extension)clazz.getDeclaredConstructor(ServerPlugin.class).newInstance(this);
      } catch (ReflectiveOperationException e) {
         throw new IllegalStateException("Could not instantiate " + className, e);
      }
   }

   private void initializeLegacyLiteDataFolder() {
      File dataFolder = this.getDataFolder();
      if (!dataFolder.exists()) {
         File pluginsFolder = dataFolder.getParentFile();
         File phoenixCratesLiteFolder = new File(pluginsFolder, "PhoenixCratesLite");
         if (phoenixCratesLiteFolder.exists()) {
            try {
               Files.cloneDirectory(phoenixCratesLiteFolder.toPath(), dataFolder.toPath());
               this.getLogger().info("Cloned PhoenixCratesLite to plugin data folder.");
            } catch (IOException e) {
               this.getLogger().error("Failed to clone PhoenixCratesLite", e);
            }
         } else {
            dataFolder.mkdirs();
            this.getLogger().info("Created plugin data folder.");
         }
      }
   }

   @Override
   public void onInit() throws Exception {
      ComplexMaterial.registerHookResolver(this.getHookRegistry()::resolveNamespacedMaterial);
      UiComponents.enable(this);
      Services.enable(this);
      this.getCommandManager().setResolvers(new CrateCommandResolvers(this.getCommandManager()));
      this.getHookRegistry().registerProvider(new MiniMessageHookProvider());
      this.getHookRegistry().registerProvider(new VaultHookProvider());
      this.getHookRegistry().registerProvider(new PlayerPointsHookProvider());
      this.getHookRegistry().registerProvider(new VotingPluginHookProvider());
      this.getHookRegistry().registerProvider(new ExcellentEconomyHookProvider());
      this.getHookRegistry().registerProvider(new BetterModelHookProvider());
      this.getHookRegistry().registerProvider(new PlaceholderHookProvider(new PlaceholdersExpansion(this)));
      this.getHookRegistry().registerProvider(new NexoHookProvider());
      this.getHookRegistry().registerProvider(new ItemsAdderHookProvider());
      this.getHookRegistry().registerProvider(new MythicItemsHookProvider());
      this.getHookRegistry().registerProvider(new MMOItemsHookProvider());
      this.getHookRegistry().registerProvider(new CraftEngineHookProvider());
      this.getHookRegistry().registerProvider(new HeadDatabaseHookProvider());
      this.getHookRegistry().registerProvider(new ExecutableItemsHookProvider());
      this.getHookRegistry()
         .onLoad(
            () -> {
               if (this.getHookRegistry().isHookEnabled(MythicItemsHookProvider.class)) {
                  this.getHookRegistry()
                     .getHook(MythicItemsProvider.class)
                     .registerItemDrop(
                        "pcrates:key", args -> Optional.ofNullable(this.keysManager.getKeyByIdentifier(args[0])).map(Key::createItemStack).orElse(null)
                     );
               }
            }
         );
      if (this.getEdition() == Edition.LITE) {
         StringKeyStore.initialize(this);
      }
   }

   @Override
   public void onLoad() throws Exception {
      this.sendConsoleMessage("\u00a7eChecking for internal patchs...");
      new CratesPatches(this).load();
      this.papiInstalled = Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
      if (this.papiInstalled) {
         this.sendConsoleMessage("\u00a77Placeholder API founded. Preparing placeholders...");
      } else {
         this.sendConsoleMessage("\u00a77PlaceholderAPI is not installed. If you want to support more placeholders in this plugin install it.");
         this.sendConsoleMessage("\u00a77-> See more at: https://www.spigotmc.org/resources/placeholderapi.6245/");
      }

      Plugin modelEnginePlugin = Bukkit.getServer().getPluginManager().getPlugin("ModelEngine");
      this.modelEngineInstalled = modelEnginePlugin != null && modelEnginePlugin.isEnabled();
      if (modelEnginePlugin != null) {
         if (modelEnginePlugin.isEnabled()) {
            this.sendConsoleMessage("\u00a77Model Engine founded. Preparing model engine...");
            this.modelEngineAvailable = true;

            try {
               ModelEngineHook.initialize(this);
            } catch (IllegalStateException e) {
               this.sendConsoleMessage("\u00a7c" + e.getMessage());
            }
         } else {
            this.sendConsoleMessage("\u00a77ModelEngine founded but didn't loaded correctly. (ModelEngine disabled)");
         }
      } else {
         this.sendConsoleMessage("\u00a77Model Engine is not installed. If you want to support custom models in this plugin install it.");
         this.sendConsoleMessage(
            "\u00a77-> (Meg 4) See more at: https://mythiccraft.io/index.php?resources/20-off-model-engine%E2%80%94ultimate-entity-model-manager-1-19-4-1-20-1.1213/"
         );
      }

      Plugin betterModelPlugin = Bukkit.getServer().getPluginManager().getPlugin("BetterModel");
      this.betterModelInstalled = betterModelPlugin != null;
      if (betterModelPlugin != null) {
         if (betterModelPlugin.isEnabled()) {
            this.sendConsoleMessage("\u00a77BetterModel founded. Preparing BetterModel engine...");
            this.betterModelAvailable = true;
         } else {
            this.sendConsoleMessage("\u00a77BetterModel founded but didn't loaded correctly. (BetterModel disabled)");
         }
      } else {
         this.sendConsoleMessage("\u00a77BetterModel is not installed. If you want to support custom models in this plugin install it.");
         this.sendConsoleMessage("\u00a77-> See more at: https://modrinth.com/plugin/bettermodel");
      }

      this.sendConsoleMessage("\u00a7eLoading components...");
      Constants.load();
      Translations.register(this);
      EditorFacade.initialize(this);
      this.sendConsoleMessage("\u00a7eLoading database...");
      DatabaseConfiguration databaseConfig = this.getEdition() == Edition.LITE ? DatabaseConfiguration.sqlite() : this.getConfiguration().getDatabase();
      this.sendConsoleMessage("\u00a7eInitializing " + databaseConfig.getType().toLowerCase() + " database provider...");
      this.databaseProvider = Services.getDatabaseService().createProvider(this, databaseConfig);
      this.sendConsoleMessage("\u00a7eLoading menus...");
      MenuFactory.initialize(this);
      this.sendConsoleMessage("\u00a7eLoading players data...");
      this.playersManager.load();
      this.sendConsoleMessage("\u00a7eLoading keys...");
      this.keysManager.load();
      this.sendConsoleMessage("\u00a77Loaded " + this.keysManager.getRegisteredKeys().size() + " keys.");
      this.sendConsoleMessage("\u00a7eLoading crates...");
      this.cratesManager.load();
      this.sendConsoleMessage("\u00a77Loaded " + this.cratesManager.getRegisteredTypes().size() + " types.");
      this.sendConsoleMessage("\u00a77Loaded " + this.cratesManager.getCrates().size() + " crates.");
      this.sendConsoleMessage("\u00a7eLoading commands...");
      this.getCommandManager().registerCommand(new CratesCommand(), true);
      System.setProperty("bstats.relocatecheck", "false");
      Metrics metrics = new Metrics(this.original, 15278);
      metrics.addCustomChart(new SimplePie("premium_vs_lite", () -> this.getEdition() == Edition.LITE ? "Lite" : "Premium"));
   }

   @Override
   public void onUnload() throws Exception {
      if (this.cratesManager != null) {
         this.cratesManager.unload();
      }

      if (this.keysManager != null) {
         this.keysManager.unload();
      }

      if (this.playersManager != null) {
         this.playersManager.unload();
      }

      if (this.itemStorage != null) {
         this.itemStorage.unload();
      }

      MenuFactory.dispose();
      Services.disable();
      UiComponents.disable();
   }

   @Override
   public void onReload() throws Exception {
      this.getConfigurationWorker().close();
      this.getConfigurationWorker().open();
      this.getConfigurationWorker().validate();
      Translations.reload();
      this.keysManager.reload();
      this.cratesManager.reload();
      this.playersManager.reload();
      MenuFactory.reload();
   }

   @Override
   public boolean isDebugEnabled() {
      return this.getConfiguration().isDebug();
   }

   public void switchLanguage(Language language) throws Exception {
      Translations.getProvider().changeTranslation(new Locale(language.getSymbol(), language.getLocalName()));
   }

   private void sendConsoleMessage(String message) {
      Bukkit.getConsoleSender().sendMessage(message);
   }

   public SettingsConfiguration getConfiguration() {
      return this.platform.getConfiguration();
   }

   public AsyncQueueConfigurationWorker getConfigurationWorker() {
      return this.platform.getConfigurationWorker();
   }

   public boolean isPluginFolderCreatedOnEnable() {
      return this.platform.isPluginFolderCreatedOnEnable();
   }

   public boolean needUpdate() {
      return this.platform.needUpdate();
   }

   public ProductVersion getCurrentVersion() {
      return this.platform.getCurrentVersion();
   }

   public ProductVersion getAvailableVersion() {
      return this.platform.getAvailableVersion();
   }

   @Generated
   public BukkitPlatform getPlatform() {
      return this.platform;
   }

   @Generated
   public ItemStorage getItemStorage() {
      return this.itemStorage;
   }

   @Generated
   public PlayersManager getPlayersManager() {
      return this.playersManager;
   }

   @Generated
   public KeysManager getKeysManager() {
      return this.keysManager;
   }

   @Generated
   public CratesManager getCratesManager() {
      return this.cratesManager;
   }

   @Generated
   public DatabaseProvider getDatabaseProvider() {
      return this.databaseProvider;
   }

   @Generated
   public boolean isPapiInstalled() {
      return this.papiInstalled;
   }

   @Generated
   public boolean isModelEngineInstalled() {
      return this.modelEngineInstalled;
   }

   @Generated
   public boolean isModelEngineAvailable() {
      return this.modelEngineAvailable;
   }

   @Generated
   public boolean isBetterModelInstalled() {
      return this.betterModelInstalled;
   }

   @Generated
   public boolean isBetterModelAvailable() {
      return this.betterModelAvailable;
   }
}
