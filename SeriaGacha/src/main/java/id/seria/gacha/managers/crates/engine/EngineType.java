package id.seria.gacha.managers.crates.engine;

import com.google.common.collect.ImmutableList;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.engine.bettermodel.BetterModelEngine;
import id.seria.gacha.managers.crates.engine.modelengine.ModelEngineEngine;
import id.seria.gacha.managers.crates.engine.vanilla_block.VanillaBlockEngine;
import id.seria.gacha.managers.crates.engine.vanilla_model.VanillaModelEngine;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

public abstract class EngineType implements id.seria.gacha.api.crate.engine.EngineType {
   private static int NEXT_ORDINAL;
   private static final Map<String, EngineType> VALUES = new LinkedHashMap<>();
   public static final EngineType VANILLA_BLOCK = init(new VanillaBlockEngine(NEXT_ORDINAL++));
   public static final EngineType VANILLA_MODEL = init(new VanillaModelEngine(NEXT_ORDINAL++));
   public static final EngineType MODEL_ENGINE = init(new ModelEngineEngine(NEXT_ORDINAL++));
   public static final EngineType BETTER_MODEL = init(new BetterModelEngine(NEXT_ORDINAL++));
   private final int ordinal;

   private static EngineType init(EngineType type) {
      VALUES.put(type.getName(), type);
      return type;
   }

   public static List<EngineType> values() {
      return ImmutableList.copyOf(VALUES.values());
   }

   public static EngineType valueOf(@NonNull String typeName) {
      if (typeName == null) {
         throw new NullPointerException("typeName is marked non-null but is null");
      } else {
         EngineType type = VALUES.get(typeName);
         if (type == null) {
            throw new IllegalArgumentException("No engine type " + typeName + ".");
         } else {
            return type;
         }
      }
   }

   protected EngineType(int ordinal) {
      this.ordinal = ordinal;
   }

   @Override
   public abstract String getName();

   @Override
   public abstract ItemStack getDisplayItem();

   public abstract EngineData createEmptyData();

   public abstract EngineDisplayComponent createDisplayComponent(Crate var1);

   public abstract EngineDisplayComponent createDisplayComponent(Crate var1, Player var2);

   public abstract boolean isInstalled();

   public abstract boolean isAvailable();

   @Deprecated
   @ScheduledForRemoval
   public boolean isSoon() {
      return false;
   }

   @Override
   protected final Object clone() throws CloneNotSupportedException {
      throw new CloneNotSupportedException();
   }

   @Override
   public String toString() {
      return this.getName();
   }

   @Generated
   @Override
   public int getOrdinal() {
      return this.ordinal;
   }
}
