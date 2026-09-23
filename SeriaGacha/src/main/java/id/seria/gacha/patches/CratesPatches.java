package id.seria.gacha.patches;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.reward.GuaranteedRewardType;
import id.seria.gacha.api.sdk.locales.Locale;
import id.seria.gacha.lib.common.utils.utility.BukkitUtil;
import id.seria.gacha.managers.crates.hologram.HologramProperties;
import id.seria.gacha.sdk.core.locales.resources.TranslatableFile;
import id.seria.gacha.sdk.core.patcher.PluginPatcher;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class CratesPatches extends PluginPatcher<PhoenixCrates> {
   public CratesPatches(PhoenixCrates plugin) {
      super(plugin);
   }

   @Override
   public void onLoad() {
      this.addPatch(1L, () -> {
         File messagesFile = new File(this.plugin.getDataFolder(), "messages.yml");
         if (messagesFile.exists()) {
            TranslatableFile file = new TranslatableFile(this.plugin, Locale.ENGLISH, "translations/translations.en.yml");
            NavigableMap<String, String[]> translations = file.fetchTranslations();
            File targetFile = new File(this.plugin.getDataFolder(), "translations/translations.en.yml");
            YamlConfiguration target = YamlConfiguration.loadConfiguration(targetFile);
            YamlConfiguration messages = YamlConfiguration.loadConfiguration(messagesFile);
            boolean updateTargetYaml = false;

            for (Entry<String, String[]> entry : translations.entrySet()) {
               if (!entry.getKey().startsWith("editor")) {
                  Object object = messages.get(entry.getKey());
                  if (object == null && entry.getKey().startsWith("labels")) {
                     object = messages.get(entry.getKey().replace("labels", "words"));
                  }

                  if (object != null) {
                     target.set("en." + entry.getKey(), object);
                     updateTargetYaml = true;
                  }
               }
            }

            if (updateTargetYaml) {
               target.save(targetFile);
            }

            messagesFile.renameTo(new File(this.plugin.getDataFolder(), "old_messages.yml"));
         }
      });
      this.addPatch(2L, () -> {
         File dataFolder = new File(this.plugin.getDataFolder(), "crates");
         if (dataFolder.exists()) {
            File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
               for (File file : files) {
                  YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                  if (config.contains("rewards")) {
                     for (String key : config.getConfigurationSection("rewards").getKeys(false)) {
                        if (config.getBoolean("rewards." + key + ".virtual")) {
                           config.set("rewards." + key + ".items", new ArrayList());
                        }
                     }

                     try {
                        config.save(file);
                     } catch (IOException e) {
                        Crates.getLogger().error("Error running patch", e);
                     }
                  }
               }
            }
         }
      });
      this.addPatch(3L, this.translationPatch((config, lang) -> {
         if (config.contains(lang + ".crate-key-taken")) {
            config.set(lang + ".crate-key-taken", null);
         }
      }));
      this.addPatch("2025.05.16-10:16", this.translationPatch((config, lang) -> {
         if (config.contains(lang + ".editor.crate.delete.description")) {
            config.set(lang + ".editor.crate.delete.description", null);
         }
      }));
      this.addPatch("2025.05.21-19:58", () -> {
         File menusFolder = new File(this.plugin.getDataFolder(), "menus");
         if (menusFolder.exists()) {
            File[] files = menusFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
               for (File file : files) {
                  YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                  config.set("reward.unlocked", null);
                  config.set("reward.locked", null);
                  config.save(file);
               }
            }
         }
      });
      this.addPatch("2025.05.22-16:06", () -> {
         File cratesFolder = new File(this.plugin.getDataFolder(), "crates");
         if (cratesFolder.exists()) {
            File[] files = cratesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
               for (File file : files) {
                  YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                  String milestoneType = config.getString("current-milestone");
                  if (milestoneType == null) {
                     milestoneType = "disabled";
                  }

                  GuaranteedRewardType type = GuaranteedRewardType.DISABLED;
                  switch (milestoneType.toLowerCase()) {
                     case "sequential":
                        type = GuaranteedRewardType.SEQUENTIAL;
                        break;
                     case "repetitive":
                        type = GuaranteedRewardType.REPETITIVE;
                  }

                  config.set("guaranteed-reward-type", type.name());
                  int counter = -1;
                  ConfigurationSection rewardsSection = config.getConfigurationSection("rewards");
                  if (rewardsSection != null) {
                     for (String key : rewardsSection.getKeys(false)) {
                        try {
                           counter = Math.max(counter, Integer.parseInt(key));
                        } catch (Exception var22) {
                        }
                     }
                  }

                  String milestoneRewardsKey = "milestones." + type.name().toLowerCase();
                  ConfigurationSection milestonesSection = config.getConfigurationSection(milestoneRewardsKey);
                  if (milestonesSection != null) {
                     for (String key : milestonesSection.getKeys(false)) {
                        String milestoneSectionKey = milestoneRewardsKey + "." + key;
                        int milestone = config.getInt(milestoneSectionKey + ".milestone");
                        if (milestone <= 0) {
                           milestone = -1;
                        }

                        ConfigurationSection oldSection = config.getConfigurationSection(milestoneSectionKey + ".reward");
                        if (oldSection != null) {
                           ConfigurationSection newSection = config.createSection("rewards." + ++counter);

                           for (String kk : oldSection.getKeys(true)) {
                              newSection.set(kk, oldSection.get(kk));
                           }

                           newSection.set("guaranteed-win", milestone);
                           newSection.set("percentage", 0);
                           config.set(milestoneSectionKey, null);
                        }
                     }
                  }

                  config.save(file);
               }
            }
         }
      });
      this.addPatch("2025.06.25-17:25", () -> {
         File cratesFolder = new File(this.plugin.getDataFolder(), "crates");
         File keysFolder = new File(this.plugin.getDataFolder(), "keys");
         if (!keysFolder.exists()) {
            keysFolder.mkdirs();
         }

         if (cratesFolder.exists()) {
            File[] files = cratesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
               for (File file : files) {
                  YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                  String id = config.getString("identifier");
                  ConfigurationSection keySection = config.getConfigurationSection("key");
                  File keyFile = new File(keysFolder, "key_" + id + ".yml");
                  YamlConfiguration keyConfig = YamlConfiguration.loadConfiguration(keyFile);
                  keyConfig.set("identifier", id);
                  keyConfig.set("enabled", keySection.getBoolean("enabled", true));
                  keyConfig.set("display-name", BukkitUtil.extractName(keySection.getItemStack("item", null)));
                  keyConfig.set("virtual", keySection.getBoolean("virtual", false));
                  keyConfig.set("glow", keySection.getBoolean("glow", true));
                  keyConfig.set("item", keySection.get("item"));
                  keyConfig.save(keyFile);
                  config.set("key", null);
                  config.set("key-required", keySection.getBoolean("enabled", true));
                  config.set("linked-keys-ids", Lists.newArrayList(new String[]{id}));
                  config.save(file);
               }
            }
         }
      });
      this.addPatch("2025.07.04-15:58", () -> this.plugin.getLogger().warn("Testing patch and backup system"));
      this.addPatch("2025.09.11-12:32", this.translationPatch((config, lang) -> {
         if (config.contains(lang + ".plans")) {
            config.set(lang + ".plans", null);
         }

         if (config.contains(lang + ".editor.edit-opening-animation.select-phase.description")) {
            config.set(lang + ".editor.edit-opening-animation.select-phase.description", null);
         }
      }));
      this.addPatch("2025.11.06-16:01", () -> {
         File cratesFolder = new File(this.plugin.getDataFolder(), "crates");
         File keysFolder = new File(this.plugin.getDataFolder(), "keys");
         if (!keysFolder.exists()) {
            keysFolder.mkdirs();
         }

         if (cratesFolder.exists()) {
            File[] files = cratesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
               for (File file : files) {
                  YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                  if (config.contains("hologram.lines")) {
                     List<String> lines = config.getStringList("hologram.lines");
                     config.set("hologram.lines", null);
                     config.set("hologram.data", "INTERNAL;{[" + String.join(";", lines) + "]}");
                     config.save(file);
                  }
               }
            }
         }
      });
      this.addPatch(
         "2026.08.25-18:20",
         () -> {
            File templatesFile = new File(this.plugin.getDataFolder(), "templates.yml");
            if (templatesFile.exists()) {
               YamlConfiguration templates = YamlConfiguration.loadConfiguration(templatesFile);
               Map<String, Object> style = new LinkedHashMap<>();
               style.put("line-gap", templates.getDouble("hologram.line-gap", 0.3));
               style.put("billboard", templates.getString("hologram.billboard", HologramProperties.DEFAULT_BILLBOARD.name()));
               style.put("text-opacity", templates.getInt("hologram.text-opacity", 255));
               style.put("see-through", templates.getBoolean("hologram.see-through", false));
               style.put("shadow", templates.getBoolean("hologram.shadow", true));
               style.put("background-color", templates.getString("hologram.background-color", "0,0,0,0"));
               style.put("scale", templates.getDouble("hologram.scale", 1.0));
               File cratesFolder = new File(this.plugin.getDataFolder(), "crates");
               int migrated = 0;
               if (cratesFolder.exists()) {
                  File[] files = cratesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
                  if (files != null) {
                     for (File file : files) {
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                        if (config.contains("hologram")) {
                           for (Entry<String, Object> entry : style.entrySet()) {
                              String path = "hologram." + entry.getKey();
                              if (!config.contains(path)) {
                                 config.set(path, entry.getValue());
                              }
                           }

                           config.save(file);
                           migrated++;
                        }
                     }
                  }
               }

               if (templatesFile.delete()) {
                  this.plugin
                     .getLogger()
                     .info(
                        "Hologram style moved from templates.yml into "
                           + migrated
                           + " crate(s), each crate now owns its own style in the editor. templates.yml was removed."
                     );
               } else {
                  this.plugin
                     .getLogger()
                     .warn(
                        "Hologram style moved from templates.yml into "
                           + migrated
                           + " crate(s), but templates.yml could not be deleted. It is no longer read and can be removed by hand."
                     );
               }
            }
         }
      );
   }

   private PluginPatcher.PatchRunner translationPatch(BiConsumer<YamlConfiguration, String> consumer) {
      return () -> {
         File translationsFolder = new File(this.plugin.getDataFolder(), "translations");
         if (translationsFolder.exists()) {
            File[] files = translationsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
               for (File file : files) {
                  YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                  String lang = file.getName().replace("translations.", "").replace(".yml", "");
                  consumer.accept(config, lang);
                  config.save(file);
               }
            }
         }
      };
   }
}
