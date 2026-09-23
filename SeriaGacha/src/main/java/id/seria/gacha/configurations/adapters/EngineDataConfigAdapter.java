package id.seria.gacha.configurations.adapters;

import id.seria.gacha.lib.common.utils.configuration.ConfigurationTokenizer;
import id.seria.gacha.lib.common.utils.configuration.ConfigurationUtilities;
import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import id.seria.gacha.managers.crates.engine.EngineData;
import id.seria.gacha.managers.crates.engine.bettermodel.BetterModelEngineData;
import id.seria.gacha.managers.crates.engine.modelengine.ModelEngineEngineData;
import id.seria.gacha.managers.crates.engine.vanilla_block.VanillaBlockEngineData;
import id.seria.gacha.managers.crates.engine.vanilla_model.VanillaModelEngineData;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EngineDataConfigAdapter extends ConfigAdapter<EngineData, String> {
   private static final int VANILLA_MODEL_ITEM_MODEL_INDEX = 2;
   private static final int VANILLA_MODEL_TOKENS_WITH_ITEM_MODEL = 7;

   public EngineDataConfigAdapter() {
   }

   public EngineData deserialize(ConfigurationReader reader, Type type, Object object) throws ErrorException, WarningException {
      if (!(object instanceof String)) {
         throw new WarningException("Expected \"String\", founded \"" + object.getClass().getSimpleName() + "\"");
      }

      String value = object.toString();
      int separatorIndex = value.indexOf(59);
      String rawType = (separatorIndex == -1 ? value : value.substring(0, separatorIndex)).replace(" ", "_");
      switch (rawType.toUpperCase()) {
         case "VANILLA":
         case "VANILLA_BLOCK": {
            if (separatorIndex == -1) {
               return new VanillaBlockEngineData();
            }

            String data = value.substring(separatorIndex + 1);
            ConfigurationTokenizer tokenizer = new ConfigurationTokenizer(new ConfigurationTokenizer(data, ";").nextTokenAsObject(), ";");
            return (VanillaBlockEngineData)reader.unserializeObject(ConfigurationUtilities.GENERIC_SERIALIZATION, VanillaBlockEngineData.class, tokenizer);
         }
         case "VANILLA_MODEL": {
            if (separatorIndex == -1) {
               throw new WarningException("Expected separator \";\"");
            }

            String data = value.substring(separatorIndex + 1);
            ConfigurationTokenizer tokenizer = new ConfigurationTokenizer(
               migrateLegacyVanillaModelData(new ConfigurationTokenizer(data, ";").nextTokenAsObject()), ";"
            );
            return (VanillaModelEngineData)reader.unserializeObject(ConfigurationUtilities.GENERIC_SERIALIZATION, VanillaModelEngineData.class, tokenizer);
         }
         case "MODEL_ENGINE": {
            if (separatorIndex == -1) {
               throw new WarningException("Expected separator \";\"");
            }

            String data = value.substring(separatorIndex + 1);
            ConfigurationTokenizer tokenizer = new ConfigurationTokenizer(new ConfigurationTokenizer(data, ";").nextTokenAsObject(), ";");
            return (ModelEngineEngineData)reader.unserializeObject(ConfigurationUtilities.GENERIC_SERIALIZATION, ModelEngineEngineData.class, tokenizer);
         }
         case "BETTER_MODEL": {
            if (separatorIndex == -1) {
               throw new WarningException("Expected separator \";\"");
            }

            String data = value.substring(separatorIndex + 1);
            ConfigurationTokenizer tokenizer = new ConfigurationTokenizer(new ConfigurationTokenizer(data, ";").nextTokenAsObject(), ";");
            return (BetterModelEngineData)reader.unserializeObject(ConfigurationUtilities.GENERIC_SERIALIZATION, BetterModelEngineData.class, tokenizer);
         }
         default:
            throw new WarningException("Unknown engine \"" + rawType + "\"");
      }
   }

   private static String migrateLegacyVanillaModelData(String data) {
      String[] tokens = data.split(";", -1);
      if (tokens.length < 7 && tokens.length > 2) {
         List<String> migrated = new ArrayList<>(Arrays.asList(tokens));
         migrated.add(2, "");
         return String.join(";", migrated);
      } else {
         return data;
      }
   }

   public String serialize(ConfigurationWriter writer, EngineData data) throws ErrorException {
      try {
         switch (data.getType().getName()) {
            case "VANILLA":
            case "VANILLA_BLOCK":
               return String.format(
                  "%s;{%s}", "VANILLA_BLOCK", writer.serializeObject(ConfigurationUtilities.GENERIC_SERIALIZATION, data.getClass(), data, false, true)
               );
            case "VANILLA_MODEL":
               return String.format(
                  "%s;{%s}", "VANILLA_MODEL", writer.serializeObject(ConfigurationUtilities.GENERIC_SERIALIZATION, data.getClass(), data, false, true)
               );
            case "MODEL_ENGINE":
               return String.format(
                  "%s;{%s}", "MODEL_ENGINE", writer.serializeObject(ConfigurationUtilities.GENERIC_SERIALIZATION, data.getClass(), data, false, true)
               );
            case "BETTER_MODEL":
               return String.format(
                  "%s;{%s}", "BETTER_MODEL", writer.serializeObject(ConfigurationUtilities.GENERIC_SERIALIZATION, data.getClass(), data, false, true)
               );
            default:
               throw new ErrorException("Unknown engine \"" + data.getType().getName() + "\"");
         }
      } catch (IllegalArgumentException | IllegalAccessException e) {
         throw new ErrorException(e.getMessage());
      }
   }
}
