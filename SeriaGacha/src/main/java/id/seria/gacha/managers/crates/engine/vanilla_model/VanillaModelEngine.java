package id.seria.gacha.managers.crates.engine.vanilla_model;

import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.engine.EngineDisplayComponent;
import id.seria.gacha.managers.crates.engine.EngineType;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class VanillaModelEngine extends EngineType {
   public VanillaModelEngine(int ordinal) {
      super(ordinal);
   }

   @Override
   public String getName() {
      return "VANILLA_MODEL";
   }

   @Override
   public ItemStack getDisplayItem() {
      return XMaterial.EMERALD_BLOCK.parseItem();
   }

   public VanillaModelEngineData createEmptyData() {
      return new VanillaModelEngineData();
   }

   @Override
   public EngineDisplayComponent createDisplayComponent(Crate crate) {
      Validate.isTrue(crate.getType().getEngineData() instanceof VanillaModelEngineData, "Expected Vanilla Model Entity Data", new Object[0]);
      return new VanillaModelDisplayComponent(crate, (VanillaModelEngineData)crate.getType().getEngineData());
   }

   @Override
   public EngineDisplayComponent createDisplayComponent(Crate crate, Player viewer) {
      Validate.isTrue(crate.getType().getEngineData() instanceof VanillaModelEngineData, "Expected Vanilla Model Entity Data", new Object[0]);
      return new VanillaModelDisplayComponent(crate, (VanillaModelEngineData)crate.getType().getEngineData());
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
