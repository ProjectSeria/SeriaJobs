package id.seria.gacha.lib.common.utils;

import id.seria.gacha.lib.common.utils.configuration.Configuration;
import id.seria.gacha.lib.common.utils.configuration.adapter.AdaptersFactory;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.annotations.Serializable;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import id.seria.gacha.lib.common.utils.configuration.logger.SystemYamlLogger;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.configuration.file.YamlConfiguration;

public class Main {
   public Main() {
   }

   public static void main(String[] args) throws IOException, ErrorException {
      read();
   }

   public static void write() throws IOException, ErrorException {
      new AdaptersFactory();
      Main.SoundsFactory config = new Main.SoundsFactory();
      File file = new File("D:\\servers\\spigotserver_v1.20.4\\plugins\\PhoenixDuels\\testing.yml");
      if (file.exists()) {
         file.createNewFile();
      }

      SystemYamlLogger logger = new SystemYamlLogger();
      ConfigurationWriter writer = new ConfigurationWriter(config);
      writer.setLogger(logger);
      YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
      writer.writeConfiguration(yaml);
      yaml.save(file);
      writer.writeConfiguration(yaml);
      yaml.save(file);
      System.out.println("Done");
   }

   public static Main.SoundsFactory read() throws IOException, ErrorException {
      AdaptersFactory factory = new AdaptersFactory();
      new Main.SoundsFactory();
      File file = new File("D:\\servers\\spigotserver_v1.20.4\\plugins\\PhoenixDuels\\testing.yml");
      if (file.exists()) {
         file.createNewFile();
      }

      SystemYamlLogger logger = new SystemYamlLogger();
      YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
      yaml.save(file);
      ConfigurationReader reader = new ConfigurationReader(factory);
      reader.setLogger(logger);
      Main.SoundsFactory fetched = reader.readConfiguration(yaml, Main.SoundsFactory.class);
      System.out.println(fetched);
      return fetched;
   }

   @Serializable
   public static class CustomSound {
      @ConfigField
      private String name = "none";
      @ConfigField
      private float volume = 1.0F;
      @ConfigField
      private float pitch = 1.0F;

      @Generated
      @Override
      public String toString() {
         return "Main.CustomSound(name=" + this.getName() + ", volume=" + this.getVolume() + ", pitch=" + this.getPitch() + ")";
      }

      @Generated
      public void setName(String name) {
         this.name = name;
      }

      @Generated
      public void setVolume(float volume) {
         this.volume = volume;
      }

      @Generated
      public void setPitch(float pitch) {
         this.pitch = pitch;
      }

      @Generated
      public String getName() {
         return this.name;
      }

      @Generated
      public float getVolume() {
         return this.volume;
      }

      @Generated
      public float getPitch() {
         return this.pitch;
      }

      @Generated
      public CustomSound(String name, float volume, float pitch) {
         this.name = name;
         this.volume = volume;
         this.pitch = pitch;
      }

      @Generated
      public CustomSound() {
      }
   }

   public enum SoundType {
      REQUEST,
      JOIN_QUEUE,
      LEAVE_QUEUE,
      START,
      DEFEAT,
      DRAW,
      WINNING;

      SoundType() {
      }
   }

   public static class SoundsFactory extends Configuration {
      @ConfigField
      private Map<String, Map<Integer, List<Main.CustomSound>>> sounds = new HashMap<>();

      @Override
      public Configuration clone() {
         return new Main.SoundsFactory();
      }

      @Generated
      public Map<String, Map<Integer, List<Main.CustomSound>>> getSounds() {
         return this.sounds;
      }

      @Generated
      public SoundsFactory() {
      }

      @Generated
      public SoundsFactory(Map<String, Map<Integer, List<Main.CustomSound>>> sounds) {
         this.sounds = sounds;
      }

      @Generated
      @Override
      public String toString() {
         return "Main.SoundsFactory(sounds=" + this.getSounds() + ")";
      }
   }
}
