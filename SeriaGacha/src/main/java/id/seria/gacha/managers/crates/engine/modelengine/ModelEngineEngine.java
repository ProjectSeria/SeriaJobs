package id.seria.gacha.managers.crates.engine.modelengine;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.engine.EngineDisplayComponent;
import id.seria.gacha.managers.crates.engine.EngineType;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ModelEngineEngine extends EngineType {
   private static final ItemStack CACHED_DISPLAY_ITEM = Utilities.getNMSFactory()
      .createTexturedSkullFromUrl("https://textures.minecraft.net/texture/6060cc52fb3c01f2ac0d8d8d14f775a62b5f7501b74d32024cf27fe5be03b52a");

   public ModelEngineEngine(int ordinal) {
      super(ordinal);
   }

   @Override
   public String getName() {
      return "MODEL_ENGINE";
   }

   @Override
   public ItemStack getDisplayItem() {
      return CACHED_DISPLAY_ITEM.clone();
   }

   public ModelEngineEngineData createEmptyData() {
      return new ModelEngineEngineData();
   }

   @Override
   public EngineDisplayComponent createDisplayComponent(Crate crate) {
      Validate.isTrue(this.isAvailable(), "Model Engine is not available", new Object[0]);
      Validate.isTrue(crate.getType().getEngineData() instanceof ModelEngineEngineData, "Expected Model Engine Data", new Object[0]);
      return new ModelEngineDisplayComponent(crate, (ModelEngineEngineData)crate.getType().getEngineData(), null);
   }

   @Override
   public EngineDisplayComponent createDisplayComponent(Crate crate, Player viewer) {
      Validate.isTrue(this.isAvailable(), "Model Engine is not available", new Object[0]);
      Validate.isTrue(crate.getType().getEngineData() instanceof ModelEngineEngineData, "Expected Model Engine Data", new Object[0]);
      return new ModelEngineDisplayComponent(crate, (ModelEngineEngineData)crate.getType().getEngineData(), viewer);
   }

   @Override
   public boolean isInstalled() {
      return PhoenixCrates.getLegacyInstance().isModelEngineInstalled();
   }

   @Override
   public boolean isAvailable() {
      return PhoenixCrates.getLegacyInstance().isModelEngineAvailable();
   }
}
