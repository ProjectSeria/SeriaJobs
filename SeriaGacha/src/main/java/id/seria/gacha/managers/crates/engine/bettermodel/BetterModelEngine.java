package id.seria.gacha.managers.crates.engine.bettermodel;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.engine.EngineDisplayComponent;
import id.seria.gacha.managers.crates.engine.EngineType;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BetterModelEngine extends EngineType {
   private static final ItemStack CACHED_DISPLAY_ITEM = Utilities.getNMSFactory()
      .createTexturedSkullFromBase64(
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTZjMzhjOTllMWRkNDJkNDEzYTFjYzY5MmE1YTJjOTZiYzQ3MzkzNDI1NDFmNGVkZmU0ZDMxZTk2YjNlY2Q4In19fQ=="
      );

   public BetterModelEngine(int ordinal) {
      super(ordinal);
   }

   @Override
   public String getName() {
      return "BETTER_MODEL";
   }

   @Override
   public ItemStack getDisplayItem() {
      return CACHED_DISPLAY_ITEM.clone();
   }

   public BetterModelEngineData createEmptyData() {
      return new BetterModelEngineData();
   }

   @Override
   public EngineDisplayComponent createDisplayComponent(Crate crate) {
      Validate.isTrue(this.isAvailable(), "BetterModel is not available", new Object[0]);
      Validate.isTrue(crate.getType().getEngineData() instanceof BetterModelEngineData, "Expected Better Model Engine Data", new Object[0]);
      return new BetterModelDisplayComponent(crate, (BetterModelEngineData)crate.getType().getEngineData(), null);
   }

   @Override
   public EngineDisplayComponent createDisplayComponent(Crate crate, Player viewer) {
      Validate.isTrue(this.isAvailable(), "BetterModel is not available", new Object[0]);
      Validate.isTrue(crate.getType().getEngineData() instanceof BetterModelEngineData, "Expected Better Model Engine Data", new Object[0]);
      return new BetterModelDisplayComponent(crate, (BetterModelEngineData)crate.getType().getEngineData(), viewer);
   }

   @Override
   public boolean isInstalled() {
      return PhoenixCrates.getLegacyInstance().isBetterModelAvailable();
   }

   @Override
   public boolean isAvailable() {
      return PhoenixCrates.getLegacyInstance().isBetterModelAvailable();
   }
}
