package id.seria.gacha;

import com.google.common.collect.Lists;
import id.seria.gacha.api.sdk.Platform;
import id.seria.gacha.configurations.SettingsConfiguration;
import id.seria.gacha.internal.Files;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.api.PhoenixPluginsAPI;
import id.seria.gacha.lib.common.utils.api.ProductVersion;
import id.seria.gacha.lib.common.utils.api.authentication.AuthenticationHandler;
import id.seria.gacha.lib.common.utils.configuration.workers.AsyncQueueConfigurationWorker;
import id.seria.gacha.lib.common.utils.utility.TimeUtil;
import id.seria.gacha.limbo.Edition;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import lombok.Generated;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlatform extends JavaPlugin implements Platform {
   private static BukkitPlatform INSTANCE;
   private PhoenixCrates plugin;
   private String productId;
   private String planId;
   protected SettingsConfiguration configuration;
   protected AsyncQueueConfigurationWorker configurationWorker;
   private AuthenticationHandler authenticator;
   protected List<String> header;
   protected boolean licenseActivated;
   protected ProductVersion currentVersion;
   protected ProductVersion availableVersion;
   protected int errorCode;
   protected Exception errorException;
   protected Listener invalidLicenseListener;
   private boolean pluginFolderCreatedOnEnable;

   public static BukkitPlatform getInstance() {
      return INSTANCE;
   }

   public BukkitPlatform() {
      try {
         INSTANCE = this;
         Edition edition = this.detectEdition();
         if (edition == Edition.PREMIUM) {
            this.initializeDataFolder();
         }

         this.plugin = new PhoenixCrates(this);
         this.initialize("phoenix-crates", edition == Edition.PREMIUM ? "premium" : "lite", new SettingsConfiguration());
         String productName = edition == Edition.PREMIUM ? "Phoenix Crates" : "Phoenix Crates Lite";
         this.header = Lists.newArrayList(
            new String[]{
               "\u00a7b  ____  \u00a79  ____                               ",
               "\u00a7b |  _ \\ \u00a79 / ___|                              ",
               "\u00a7b | |_) |\u00a79| |       \u00a72" + productName + " \u00a7bv" + this.currentVersion + "      ",
               "\u00a7b |  __/ \u00a79| |___    \u00a78https://phoenixplugins.com/",
               "\u00a7b |_|    \u00a79 \\____|                             ",
               "                                                 "
            }
         );
      } catch (Exception ex) {
         this.getLogger().log(Level.SEVERE, "Error loading plugin", ex);
      }
   }

   private Edition detectEdition() {
      return "PhoenixCratesLite".equalsIgnoreCase(this.getDescription().getName()) ? Edition.LITE : Edition.PREMIUM;
   }

   private void initializeDataFolder() {
      File dataFolder = this.getDataFolder();
      if (!dataFolder.exists()) {
         File pluginsFolder = dataFolder.getParentFile();
         File phoenixCratesProFolder = new File(pluginsFolder, "PhoenixCratesPro");
         File phoenixCratesLiteFolder = new File(pluginsFolder, "PhoenixCratesLite");

         try {
            if (phoenixCratesProFolder.exists()) {
               Files.cloneDirectory(phoenixCratesProFolder.toPath(), dataFolder.toPath());
               this.getLogger().log(Level.SEVERE, "Cloned PhoenixCrates (Pro Plan) to plugin data folder.");
            } else if (phoenixCratesLiteFolder.exists()) {
               Files.cloneDirectory(phoenixCratesLiteFolder.toPath(), dataFolder.toPath());
               this.getLogger().log(Level.SEVERE, "Cloned PhoenixCrates (Lite Plan) to plugin data folder.");
            } else {
               dataFolder.mkdirs();
               this.getLogger().log(Level.SEVERE, "Created plugin data folder.");
            }
         } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Failed to clone plugin data folder", e);
         }
      }
   }

   private void stripLiteOnlyConfigKeys() {
      try {
         File file = this.configurationWorker.getConfigurationFile();
         if (!file.exists()) {
            return;
         }

         YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
         yaml.set("product-license", null);
         yaml.set("commands", null);
         yaml.save(file);
      } catch (IOException e) {
         this.getLogger().log(Level.WARNING, "Failed to strip Lite-only keys from config.yml", e);
      }
   }

   protected void initialize(String productId, String planId, SettingsConfiguration configuration) {
      this.productId = productId;
      this.planId = planId;
      this.currentVersion = new ProductVersion(this.getDescription().getVersion());
      this.availableVersion = this.currentVersion;
      this.configuration = configuration;
      this.configurationWorker = new AsyncQueueConfigurationWorker(this.plugin, this.configuration, "config.yml");
   }

   public void onEnable() {
      if (this.plugin != null) {
         try {
            this.pluginFolderCreatedOnEnable = !this.getDataFolder().exists();
            Utilities.enable(this.plugin);
            this.header.forEach(this::sendConsoleMessage);
            this.sendConsoleMessage("\u00a7eLoading settings...");
            this.configurationWorker.validate();
            if (this.plugin.getEdition() != Edition.PREMIUM) {
               this.stripLiteOnlyConfigKeys();
            }

            this.configurationWorker.open();
            if (this.plugin.getEdition() != Edition.PREMIUM) {
               this.plugin.init();
               this.plugin.load();
               Files.pruneEmptyDirectories(this.getDataFolder());
               this.sendConsoleMessage("");
               this.sendConsoleMessage("\u00a7aPlugin loaded successfully!");
               this.sendConsoleMessage("\u00a77[Any questions or problems that may arise, contact us through");
               this.sendConsoleMessage("\u00a77the website or our discord: https://phoenixplugins.com/media/discord]");
               this.sendConsoleMessage("");
               return;
            }

            this.sendConsoleMessage("\u00a7eValidating license...");
            this.authenticator = new AuthenticationHandler(
               this.plugin,
               this.productId,
               this.planId,
               this.configuration.getProductLicense(),
               ex -> {
                  this.errorCode = ex instanceof PhoenixPluginsAPI.ApiResponseException ? ((PhoenixPluginsAPI.ApiResponseException)ex).getErrorCode() : -1;
                  this.errorException = ex;
                  if (this.licenseActivated) {
                     this.sendConsoleMessage(String.format("\u00a74An error occurred while trying to renew authentication for %s:", this.getName()));
                  } else {
                     this.sendConsoleMessage(String.format("\u00a74An error occurred while trying to authenticate %s:", this.getName()));
                  }

                  if (this.errorCode == 2) {
                     this.sendConsoleMessage("\u00a7cThere was an error validating your license.");
                     this.sendConsoleMessage("\u00a7cIt appears that it does not exist for this product. Make sure it is spelled correctly.");
                  } else {
                     if (this.errorCode == 3) {
                        this.sendConsoleMessage("\u00a7cOops! Maximum concurrent sessions allowed reached.");
                        this.sendConsoleMessage(
                           "\u00a7cIf you have a reason for why you want to increase your number of concurrent sessions, contact us by our Discord."
                        );
                     } else {
                        this.sendConsoleMessage("\u00a7c" + ex.getMessage());
                     }

                     if (this.invalidLicenseListener == null) {
                        this.invalidLicenseListener = new Listener() {
                           @EventHandler
                           public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
                              String[] command = event.getMessage().substring(1).split(" ");
                              if (BukkitPlatform.this.plugin.getCommandManager().getCommandNames().contains(command[0].toLowerCase())) {
                                 event.setCancelled(true);
                                 Player player = event.getPlayer();
                                 if (BukkitPlatform.this.errorCode == 2) {
                                    player.sendMessage("\u00a7cThis plugin is disabled. There was an error validating your license.");
                                    player.sendMessage(
                                       "\u00a7cIt appears that your license does not exist for this product. Make sure it is spelled correctly (config.yml)."
                                    );
                                 } else if (BukkitPlatform.this.errorCode == 3) {
                                    player.sendMessage("\u00a7cThis plugin is disabled. Maximum concurrent sessions allowed reached.");
                                    player.sendMessage(
                                       "\u00a7cIf you have a reason for why you want to increase your number of concurrent sessions, contact us by our Discord."
                                    );
                                 } else {
                                    player.sendMessage("\u00a7cThis plugin is disabled. An error occurred while trying to authenticate.");
                                    player.sendMessage("\u00a7cMessage: " + ex.getMessage());
                                 }
                              }
                           }
                        };
                        Bukkit.getPluginManager().registerEvents(this.invalidLicenseListener, this);
                     }

                     if (this.licenseActivated) {
                        try {
                           if (this.plugin != null) {
                              this.plugin.unload();
                           }
                        } catch (Exception e) {
                           e.printStackTrace();
                        }

                        this.licenseActivated = false;
                     }
                  }
               },
               auth -> {
                  if (this.invalidLicenseListener != null) {
                     HandlerList.unregisterAll(this.invalidLicenseListener);
                     this.invalidLicenseListener = null;
                  }

                  if (auth.getException() != null) {
                     long diff = auth.getGracePeriodExpiry() - System.currentTimeMillis();
                     int secondsLeft = (int)(diff <= 0L ? 0.0 : diff / 1000.0);
                     this.sendConsoleMessage(String.format("\u00a74An error occurred while trying to authenticate %s:", this.getName()));
                     this.sendConsoleMessage(
                        "\u00a7eOh no! Apparently there was an error contacting our server. But don't worry, the grace period has been activated."
                     );
                     this.sendConsoleMessage(
                        "\u00a7cGrace period will expire in " + TimeUtil.formatSecondsShort(secondsLeft) + ", so contact us before that happens!"
                     );
                     this.sendConsoleMessage("\u00a7cError Message: " + auth.getException());
                  } else {
                     this.sendConsoleMessage(
                        "\u00a7aGreat news! Your license is valid and active. Enjoy full access to all the features and benefits of this plugin."
                     );
                     if (auth.getCurrentVersion() == null) {
                        synchronized (this.availableVersion) {
                           this.availableVersion = new ProductVersion(auth.getCurrentVersion().get());
                        }
                     }

                     switch (this.availableVersion.compareTo(this.currentVersion)) {
                        case -1:
                           this.sendConsoleMessage("\u00a7dYou have a version that is not listed.");
                           break;
                        case 0:
                           this.sendConsoleMessage("No new version available!");
                           break;
                        case 1:
                           this.sendConsoleMessage(
                              "\u00a76New version found! Your current version is "
                                 + this.currentVersion
                                 + ", you can upgrade it to version "
                                 + this.availableVersion
                           );
                     }
                  }

                  if (!this.licenseActivated) {
                     try {
                        Validate.notNull(this.plugin);
                        this.plugin.init();
                        this.plugin.load();
                        Files.pruneEmptyDirectories(this.getDataFolder());
                        this.sendConsoleMessage("");
                        this.sendConsoleMessage("\u00a7aPlugin loaded successfully!");
                        this.sendConsoleMessage("\u00a77[Any questions or problems that may arise, contact us through");
                        this.sendConsoleMessage("\u00a77the website or our discord: https://phoenixplugins.com/discord]");
                        this.sendConsoleMessage("");
                     } catch (Exception e) {
                        e.printStackTrace();
                        Bukkit.getPluginManager().disablePlugin(this);
                     }

                     this.licenseActivated = true;
                  }
               }
            );
            this.authenticator.login();
         } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
         }
      }
   }

   public void onDisable() {
      if (this.authenticator != null) {
         this.authenticator.logout();
      }

      if (this.plugin != null) {
         try {
            this.plugin.unload();
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      this.configurationWorker.close();
      Utilities.disable();
   }

   public boolean needUpdate() {
      synchronized (this.availableVersion) {
         return this.availableVersion.compareTo(this.currentVersion) == 1;
      }
   }

   protected void sendConsoleMessage(String message) {
      Bukkit.getConsoleSender().sendMessage(message);
   }

   @Generated
   public PhoenixCrates getPlugin() {
      return this.plugin;
   }

   @Generated
   public String getProductId() {
      return this.productId;
   }

   @Generated
   public String getPlanId() {
      return this.planId;
   }

   @Generated
   public SettingsConfiguration getConfiguration() {
      return this.configuration;
   }

   @Generated
   public AsyncQueueConfigurationWorker getConfigurationWorker() {
      return this.configurationWorker;
   }

   @Generated
   public AuthenticationHandler getAuthenticator() {
      return this.authenticator;
   }

   @Generated
   public List<String> getHeader() {
      return this.header;
   }

   @Generated
   public boolean isLicenseActivated() {
      return this.licenseActivated;
   }

   @Generated
   public ProductVersion getCurrentVersion() {
      return this.currentVersion;
   }

   @Generated
   public ProductVersion getAvailableVersion() {
      return this.availableVersion;
   }

   @Generated
   public int getErrorCode() {
      return this.errorCode;
   }

   @Generated
   public Exception getErrorException() {
      return this.errorException;
   }

   @Generated
   public Listener getInvalidLicenseListener() {
      return this.invalidLicenseListener;
   }

   @Generated
   public boolean isPluginFolderCreatedOnEnable() {
      return this.pluginFolderCreatedOnEnable;
   }

   @Generated
   public void setHeader(List<String> header) {
      this.header = header;
   }
}
