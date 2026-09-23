package id.seria.gacha.lib.common.uicomponents.newest.configuration;

import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;

public abstract class ContainerConfiguration extends ContainerItemsConfiguration {
   @ConfigField
   protected ContainerConfiguration.SettingsConfigurationMenu settings = new ContainerConfiguration.SettingsConfigurationMenu();

   public ContainerConfiguration() {
   }

   @Generated
   public void setSettings(ContainerConfiguration.SettingsConfigurationMenu settings) {
      this.settings = settings;
   }

   @Generated
   public ContainerConfiguration.SettingsConfigurationMenu getSettings() {
      return this.settings;
   }

   public static class DummyContainerConfiguration extends ContainerConfiguration {
      public DummyContainerConfiguration() {
      }

      public ContainerConfiguration.DummyContainerConfiguration clone() {
         return new ContainerConfiguration.DummyContainerConfiguration();
      }
   }

   public static class SettingsConfigurationMenu {
      @ConfigField
      protected String type = "CUSTOM";
      @ConfigField
      protected String title = "Custom Menu";
      @ConfigField
      protected int rows = 3;
      @ConfigField(path = "refresh.enabled")
      protected boolean refreshEnabled = false;
      @ConfigField(path = "refresh.rate")
      protected int refreshRate = 40;
      @ConfigField
      protected List<String> onOpenActions = new ArrayList<>();

      @Generated
      public void setType(String type) {
         this.type = type;
      }

      @Generated
      public void setTitle(String title) {
         this.title = title;
      }

      @Generated
      public void setRows(int rows) {
         this.rows = rows;
      }

      @Generated
      public void setRefreshEnabled(boolean refreshEnabled) {
         this.refreshEnabled = refreshEnabled;
      }

      @Generated
      public void setRefreshRate(int refreshRate) {
         this.refreshRate = refreshRate;
      }

      @Generated
      public void setOnOpenActions(List<String> onOpenActions) {
         this.onOpenActions = onOpenActions;
      }

      @Generated
      public String getType() {
         return this.type;
      }

      @Generated
      public String getTitle() {
         return this.title;
      }

      @Generated
      public int getRows() {
         return this.rows;
      }

      @Generated
      public boolean isRefreshEnabled() {
         return this.refreshEnabled;
      }

      @Generated
      public int getRefreshRate() {
         return this.refreshRate;
      }

      @Generated
      public List<String> getOnOpenActions() {
         return this.onOpenActions;
      }

      @Generated
      public SettingsConfigurationMenu() {
      }

      @Generated
      public SettingsConfigurationMenu(String type, String title, int rows, boolean refreshEnabled, int refreshRate, List<String> onOpenActions) {
         this.type = type;
         this.title = title;
         this.rows = rows;
         this.refreshEnabled = refreshEnabled;
         this.refreshRate = refreshRate;
         this.onOpenActions = onOpenActions;
      }
   }
}
