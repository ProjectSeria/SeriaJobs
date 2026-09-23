package id.seria.gacha.thirdparty;

import id.seria.gacha.Crates;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.keys.Key;
import org.bukkit.entity.Player;

public abstract class Migration {
   private static final int MAX_LITE_CRATE_TYPES = 5;
   private static final int MAX_LITE_KEYS = 5;
   private int registeredCrates = 0;
   private int registeredKeys = 0;

   public Migration() {
   }

   public abstract String getPluginName();

   public abstract boolean isAvailable();

   public abstract void convert(Player var1) throws Exception;

   public void registerCrate(CrateType type) throws Exception {
      if (Crates.getPlugin().getEdition() == Edition.LITE && Crates.getCratesManager().countRegisteredTypes() >= 5) {
         throw new IllegalStateException("\u00a7cOnly " + this.registeredCrates + " crate(s) were loaded. Purchase the Premium version to migrate more.");
      }

      Crates.getCratesManager().registerCrateType(type, true);
      this.registeredCrates++;
   }

   public void registerKey(Key key) throws Exception {
      if (Crates.getPlugin().getEdition() == Edition.LITE && Crates.getKeysManager().countRegisteredKeys() >= 5) {
         throw new IllegalStateException("\u00a7cOnly " + this.registeredKeys + " key(s) were loaded. Purchase the Premium version to migrate more.");
      }

      Crates.getKeysManager().registerKey(key, true);
      this.registeredKeys++;
   }
}
