package id.seria.gacha.thirdparty;

import id.seria.gacha.thirdparty.impl.AdvancedCratesMigration;
import id.seria.gacha.thirdparty.impl.AquaticCratesMigration;
import id.seria.gacha.thirdparty.impl.CrazyCratesMigration;
import id.seria.gacha.thirdparty.impl.CustomCratesMigration;
import id.seria.gacha.thirdparty.impl.ExcellentCratesMigration;
import id.seria.gacha.thirdparty.impl.SpecializedCratesMigration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.entity.Player;

public class MigrationFactory {
   private static final Map<String, Migration> BY_ID = new HashMap<>();

   public MigrationFactory() {
   }

   public static void registerMigration(Migration migration) {
      BY_ID.put(migration.getPluginName().toLowerCase(), migration);
   }

   public static void unregisterMigration(String pluginName) {
      BY_ID.remove(pluginName.toLowerCase());
   }

   public static Migration getById(String pluginName) {
      return BY_ID.get(pluginName.toLowerCase());
   }

   public static List<Migration> values() {
      return new ArrayList<>(BY_ID.values());
   }

   public static void migrate(String pluginName, Player owner) throws Exception {
      Migration migration = Objects.requireNonNull(getById(pluginName), "Converter \"" + pluginName + "\" doesn't exist");
      if (!migration.isAvailable()) {
         throw new IllegalStateException("Plugin " + pluginName + " not installed or available");
      }

      migration.convert(owner);
   }

   static {
      registerMigration(new CustomCratesMigration());
      registerMigration(new CrazyCratesMigration());
      registerMigration(new ExcellentCratesMigration());
      registerMigration(new SpecializedCratesMigration());
      registerMigration(new AdvancedCratesMigration());
      registerMigration(new AquaticCratesMigration());
   }
}
