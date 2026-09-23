package id.seria.gacha.lib.common.utils.configuration.adapter.defaults;

import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import java.lang.reflect.Type;
import org.bukkit.potion.PotionEffectType;

public class BukkitPotionEffectTypeAdapter extends ConfigAdapter<PotionEffectType, String> {
   public BukkitPotionEffectTypeAdapter() {
   }

   public PotionEffectType deserialize(ConfigurationReader reader, Type type, Object object) throws ErrorException, WarningException {
      if (!(object instanceof String)) {
         throw new WarningException("Expected \"String\", founded \"" + object.getClass().getSimpleName() + "\"");
      } else {
         return PotionEffectType.getByName(object.toString());
      }
   }

   public String serialize(ConfigurationWriter writer, PotionEffectType effect) throws ErrorException {
      return effect.getName().toUpperCase();
   }
}
