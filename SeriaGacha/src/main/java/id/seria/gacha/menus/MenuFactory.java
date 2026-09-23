package id.seria.gacha.menus;

import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.ContainerConfiguration;
import id.seria.gacha.lib.common.utils.configuration.ConfigFile;
import id.seria.gacha.lib.common.utils.configuration.workers.ConfigurationWorker;
import id.seria.gacha.lib.common.utils.utility.FileUtil;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.menus.crates.ConfirmationMenu;
import id.seria.gacha.menus.crates.CratePreviewMenu;
import id.seria.gacha.menus.crates.CrateRerollMenu;
import id.seria.gacha.menus.crates.CrateSelectRewardMenu;
import id.seria.gacha.menus.keys.VirtualKeysMenu;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import lombok.Generated;

public class MenuFactory {
   private static MenuFactory INSTANCE;
   private final PhoenixCrates plugin;
   private final Map<String, ContainerConfiguration> configurations = new LinkedHashMap<>();
   private final Map<String, Supplier<? extends ContainerConfiguration>> typeSuppliers = new LinkedHashMap<>();

   public static MenuFactory getInstance() {
      Objects.requireNonNull(INSTANCE, "MenuFactory was not initialized.");
      return INSTANCE;
   }

   public static void initialize(PhoenixCrates plugin) throws Exception {
      if (INSTANCE == null) {
         INSTANCE = new MenuFactory(plugin);
         INSTANCE.load();
      }
   }

   public static void dispose() {
      if (INSTANCE != null) {
         INSTANCE.unload();
         INSTANCE = null;
      }
   }

   public static void reload() throws Exception {
      Objects.requireNonNull(INSTANCE, "MenuFactory was not initialized.");
      INSTANCE.unload();
      INSTANCE.load();
   }

   public MenuFactory(PhoenixCrates plugin) {
      this.plugin = plugin;
   }

   public void load() throws Exception {
      this.registerBuiltInTypes();
      File menusFolder = new File(this.plugin.getDataFolder(), "menus");
      if (!menusFolder.exists()) {
         menusFolder.mkdirs();
      }

      boolean premium = Crates.getPlugin().getEdition() == Edition.PREMIUM;
      this.loadDefaultRewardsPreviewConfiguration();
      this.loadCrateSelectRewardConfiguration();
      this.loadConfirmationMenuConfiguration();
      if (premium) {
         this.loadCrateRerollConfiguration();
         this.loadVirtualKeysConfiguration();
      }

      for (File file : menusFolder.listFiles()) {
         if (file.isFile() && FileUtil.getExtensionWithoutName(file.getName()).equalsIgnoreCase("yml")) {
            String key = FileUtil.getNameWithoutExtension(file.getName());
            if (!this.configurations.containsKey(key)) {
               ConfigFile configFile = new ConfigFile(this.plugin, "menus/" + file.getName());
               Object rawType = configFile.get("settings.type");
               String type = rawType == null ? "UNKNOWN" : String.valueOf(rawType);
               Supplier<? extends ContainerConfiguration> supplier = this.typeSuppliers.get(type.toUpperCase());
               if (supplier != null) {
                  ContainerConfiguration configuration = supplier.get();
                  new ConfigurationWorker(this.plugin, configuration, null, "menus/" + file.getName()).load();
                  this.configurations.put(key, configuration);
               }
            }
         }
      }
   }

   private void registerBuiltInTypes() {
      this.registerType("REWARDS_PREVIEW", CratePreviewMenu.CratePreviewMenuConfiguration::new);
      this.registerType("SELECT_REWARD", CrateSelectRewardMenu.CrateSelectRewardMenuConfiguration::new);
      this.registerType("CONFIRMATION", ConfirmationMenu.ConfirmationMenuConfiguration::new);
      if (Crates.getPlugin().getEdition() == Edition.PREMIUM) {
         this.registerType("CRATE_REROLL", CrateRerollMenu.CrateRerollMenuConfiguration::new);
         this.registerType("VIRTUAL_KEYS", VirtualKeysMenu.VirtualKeysMenuConfiguration::new);
      }
   }

   public synchronized void registerType(String type, Supplier<? extends ContainerConfiguration> supplier) {
      Objects.requireNonNull(type, "type");
      Objects.requireNonNull(supplier, "supplier");
      this.typeSuppliers.put(type.toUpperCase(), supplier);
   }

   public synchronized <T extends ContainerConfiguration> T registerConfiguration(String key, T configuration, String path, String forcedType) throws Exception {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(configuration, "configuration");
      Objects.requireNonNull(path, "path");
      new ConfigurationWorker(this.plugin, configuration, null, path).load();
      if (forcedType != null && !forcedType.trim().isEmpty()) {
         configuration.getSettings().setType(forcedType);
      }

      this.configurations.put(key, configuration);
      return configuration;
   }

   public synchronized <T extends ContainerConfiguration> T registerConfiguration(String key, T configuration, String path) throws Exception {
      return this.registerConfiguration(key, configuration, path, null);
   }

   private void loadDefaultRewardsPreviewConfiguration() throws Exception {
      CratePreviewMenu.CratePreviewMenuConfiguration configuration = new CratePreviewMenu.CratePreviewMenuConfiguration();
      new ConfigurationWorker(this.plugin, configuration, null, "menus/default_rewards_preview.yml").load();
      configuration.getSettings().setType("REWARDS_PREVIEW");
      this.configurations.put("default_rewards_preview", configuration);
   }

   private void loadCrateRerollConfiguration() throws Exception {
      CrateRerollMenu.CrateRerollMenuConfiguration configuration = new CrateRerollMenu.CrateRerollMenuConfiguration();
      new ConfigurationWorker(this.plugin, configuration, null, "menus/default_crate_reroll.yml").load();
      configuration.getSettings().setType("CRATE_REROLL");
      this.configurations.put("default_crate_reroll", configuration);
   }

   private void loadCrateSelectRewardConfiguration() throws Exception {
      CrateSelectRewardMenu.CrateSelectRewardMenuConfiguration configuration = new CrateSelectRewardMenu.CrateSelectRewardMenuConfiguration();
      new ConfigurationWorker(this.plugin, configuration, null, "menus/default_select_reward.yml").load();
      configuration.getSettings().setType("SELECT_REWARD");
      this.configurations.put("default_select_reward", configuration);
   }

   private void loadConfirmationMenuConfiguration() throws Exception {
      ConfirmationMenu.ConfirmationMenuConfiguration configuration = new ConfirmationMenu.ConfirmationMenuConfiguration();
      new ConfigurationWorker(this.plugin, configuration, null, "menus/confirmation_menu.yml").load();
      configuration.getSettings().setType("CONFIRMATION");
      this.configurations.put("confirmation_menu", configuration);
   }

   private void loadVirtualKeysConfiguration() throws Exception {
      VirtualKeysMenu.VirtualKeysMenuConfiguration configuration = new VirtualKeysMenu.VirtualKeysMenuConfiguration();
      new ConfigurationWorker(this.plugin, configuration, null, "menus/virtual_keys.yml").load();
      configuration.getSettings().setType("VIRTUAL_KEYS");
      this.configurations.put("virtual_keys", configuration);
   }

   public void unload() {
      this.configurations.clear();
   }

   @Deprecated
   public ContainerConfiguration getConfigurationOrNull(String key) {
      return this.configurations.get(key);
   }

   public ContainerConfiguration getConfiguration(String key) {
      return Objects.requireNonNull(this.configurations.get(key), "Configuration with key \"" + key + "\" not found");
   }

   @Generated
   public PhoenixCrates getPlugin() {
      return this.plugin;
   }

   @Generated
   public Map<String, ContainerConfiguration> getConfigurations() {
      return this.configurations;
   }

   @Generated
   public Map<String, Supplier<? extends ContainerConfiguration>> getTypeSuppliers() {
      return this.typeSuppliers;
   }
}
