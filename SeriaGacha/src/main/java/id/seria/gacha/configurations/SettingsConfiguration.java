package id.seria.gacha.configurations;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.internal.language.Language;
import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.plugin.PluginConfiguration;
import id.seria.gacha.limbo.Edition;
import java.util.List;
import lombok.Generated;

public class SettingsConfiguration extends PluginConfiguration implements id.seria.gacha.api.sdk.config.PluginConfiguration {
   @ConfigField
   private boolean joinReminder;
   @ConfigField
   private Language language;
   @ConfigField
   private boolean notifyIfNewVersion;
   @ConfigField
   private DatabaseConfigurationSettings database;
   @ConfigField(path = "performance.internal-pool-size")
   private int internalPoolSize;
   @ConfigField
   private boolean debug;
   @ConfigField(path = "crates.knockback")
   private double cratesKnockback;
   @ConfigField(path = "crates.quick-open")
   private boolean cratesQuickOpen;
   @ConfigField(path = "crates.disable-confirmation-menus")
   private boolean disableConfirmationMenus;
   @ConfigField(path = "crates.broadcast-message", compact = true)
   private List<String> broadcastMessage;
   @ConfigField(path = "performance.crate-render-radius", legacyPaths = {"crates.render-radius", "crates.effects-despawn-range"})
   private int cratesRenderRadius;
   @ConfigField(path = "performance.hologram-refresh-rate")
   private int hologramRefreshRate;
   @ConfigField(path = "key.mainhand")
   private boolean mainHandKey;
   @ConfigField(path = "commands.virtual-keys.enabled")
   private Boolean virtualKeysCommandEnabled;
   @ConfigField(path = "commands.virtual-keys.alternative")
   private String virtualKeysCommandAlternative;
   @ConfigField(path = "commands.pay-key.enabled")
   private Boolean payKeyCommandEnabled;
   @ConfigField(path = "commands.pay-key.alternative")
   private String payKeyCommandAlternative;

   public SettingsConfiguration() {
      this.productLicense = "TQ1HH-JVZKA-GJNAF-QSICM";
      this.language = Language.UNDEFINED;
      this.notifyIfNewVersion = true;
      this.database = new DatabaseConfigurationSettings();
      this.internalPoolSize = 5;
      this.debug = false;
      this.cratesKnockback = 0.6;
      this.cratesQuickOpen = true;
      this.disableConfirmationMenus = false;
      this.cratesRenderRadius = 15;
      this.hologramRefreshRate = 10;
      this.mainHandKey = true;
      if (Crates.getPlugin().getEdition() == Edition.PREMIUM) {
         this.productLicense = "TQ1HH-JVZKA-GJNAF-QSICM";
         this.virtualKeysCommandEnabled = true;
         this.virtualKeysCommandAlternative = "virtualkeys";
         this.payKeyCommandEnabled = false;
         this.payKeyCommandAlternative = "paykey";
      } else {
         this.productLicense = null;
      }

      if (Crates.getPlugin().getEdition() == Edition.LITE) {
         this.broadcastMessage = Lists.newArrayList(new String[]{"&ePlayer &a%player% &ejust got &6\"%reward%\" &ereward from the crate &6%crate%&e!"});
      } else {
         this.broadcastMessage = null;
      }
   }

   @Override
   public Configuration clone() {
      return new SettingsConfiguration();
   }

   public int getHologramRefreshRate() {
      return this.hologramRefreshRate < 10 ? (this.hologramRefreshRate = 10) : this.hologramRefreshRate;
   }

   @Generated
   public void setJoinReminder(boolean joinReminder) {
      this.joinReminder = joinReminder;
   }

   @Generated
   public void setLanguage(Language language) {
      this.language = language;
   }

   @Generated
   public void setNotifyIfNewVersion(boolean notifyIfNewVersion) {
      this.notifyIfNewVersion = notifyIfNewVersion;
   }

   @Generated
   public void setDatabase(DatabaseConfigurationSettings database) {
      this.database = database;
   }

   @Generated
   public void setInternalPoolSize(int internalPoolSize) {
      this.internalPoolSize = internalPoolSize;
   }

   @Generated
   public void setDebug(boolean debug) {
      this.debug = debug;
   }

   @Generated
   public void setCratesKnockback(double cratesKnockback) {
      this.cratesKnockback = cratesKnockback;
   }

   @Generated
   public void setCratesQuickOpen(boolean cratesQuickOpen) {
      this.cratesQuickOpen = cratesQuickOpen;
   }

   @Generated
   public void setDisableConfirmationMenus(boolean disableConfirmationMenus) {
      this.disableConfirmationMenus = disableConfirmationMenus;
   }

   @Generated
   public void setBroadcastMessage(List<String> broadcastMessage) {
      this.broadcastMessage = broadcastMessage;
   }

   @Generated
   public void setCratesRenderRadius(int cratesRenderRadius) {
      this.cratesRenderRadius = cratesRenderRadius;
   }

   @Generated
   public void setHologramRefreshRate(int hologramRefreshRate) {
      this.hologramRefreshRate = hologramRefreshRate;
   }

   @Generated
   public void setMainHandKey(boolean mainHandKey) {
      this.mainHandKey = mainHandKey;
   }

   @Generated
   public void setVirtualKeysCommandEnabled(Boolean virtualKeysCommandEnabled) {
      this.virtualKeysCommandEnabled = virtualKeysCommandEnabled;
   }

   @Generated
   public void setVirtualKeysCommandAlternative(String virtualKeysCommandAlternative) {
      this.virtualKeysCommandAlternative = virtualKeysCommandAlternative;
   }

   @Generated
   public void setPayKeyCommandEnabled(Boolean payKeyCommandEnabled) {
      this.payKeyCommandEnabled = payKeyCommandEnabled;
   }

   @Generated
   public void setPayKeyCommandAlternative(String payKeyCommandAlternative) {
      this.payKeyCommandAlternative = payKeyCommandAlternative;
   }

   @Generated
   public boolean isJoinReminder() {
      return this.joinReminder;
   }

   @Generated
   public Language getLanguage() {
      return this.language;
   }

   @Generated
   public boolean isNotifyIfNewVersion() {
      return this.notifyIfNewVersion;
   }

   @Generated
   public DatabaseConfigurationSettings getDatabase() {
      return this.database;
   }

   @Generated
   @Override
   public int getInternalPoolSize() {
      return this.internalPoolSize;
   }

   @Generated
   public boolean isDebug() {
      return this.debug;
   }

   @Generated
   public double getCratesKnockback() {
      return this.cratesKnockback;
   }

   @Generated
   public boolean isCratesQuickOpen() {
      return this.cratesQuickOpen;
   }

   @Generated
   public boolean isDisableConfirmationMenus() {
      return this.disableConfirmationMenus;
   }

   @Generated
   public List<String> getBroadcastMessage() {
      return this.broadcastMessage;
   }

   @Generated
   public int getCratesRenderRadius() {
      return this.cratesRenderRadius;
   }

   @Generated
   public boolean isMainHandKey() {
      return this.mainHandKey;
   }

   @Generated
   public Boolean getVirtualKeysCommandEnabled() {
      return this.virtualKeysCommandEnabled;
   }

   @Generated
   public String getVirtualKeysCommandAlternative() {
      return this.virtualKeysCommandAlternative;
   }

   @Generated
   public Boolean getPayKeyCommandEnabled() {
      return this.payKeyCommandEnabled;
   }

   @Generated
   public String getPayKeyCommandAlternative() {
      return this.payKeyCommandAlternative;
   }
}
