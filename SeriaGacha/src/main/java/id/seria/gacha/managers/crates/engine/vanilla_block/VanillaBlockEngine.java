package id.seria.gacha.managers.crates.engine.vanilla_block;

import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.engine.EngineDisplayComponent;
import id.seria.gacha.managers.crates.engine.EngineType;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class VanillaBlockEngine extends EngineType {
   public VanillaBlockEngine(int ordinal) {
      super(ordinal);
   }

   @Override
   public String getName() {
      return "VANILLA_BLOCK";
   }

   @Override
   public ItemStack getDisplayItem() {
      return XMaterial.GRASS_BLOCK.parseItem();
   }

   public VanillaBlockEngineData createEmptyData() {
      return new VanillaBlockEngineData();
   }

   @Override
   public EngineDisplayComponent createDisplayComponent(Crate crate) {
      Validate.isTrue(crate.getType().getEngineData() instanceof VanillaBlockEngineData, "Expected Vanilla Block Data", new Object[0]);
      return new VanillaBlockDisplayComponent(crate, null);
   }

   @Override
   public EngineDisplayComponent createDisplayComponent(Crate crate, Player viewer) {
      Validate.isTrue(crate.getType().getEngineData() instanceof VanillaBlockEngineData, "Expected Vanilla Block Data", new Object[0]);
      return new VanillaBlockDisplayComponent(crate, viewer);
   }

   @Override
   public boolean isInstalled() {
      return true;
   }

   @Override
   public boolean isAvailable() {
      return true;
   }
}
