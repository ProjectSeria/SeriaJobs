package id.seria.gacha.lib.common.utils.configuration.adapter.defaults;

import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import java.lang.reflect.Type;
import org.bukkit.Color;

public class BukkitColorAdapter extends ConfigAdapter<Color, String> {
   public BukkitColorAdapter() {
   }

   public Color deserialize(ConfigurationReader reader, Type type, Object object) throws ErrorException, WarningException {
      if (!(object instanceof String)) {
         throw new WarningException("Expected \"String\", founded \"" + object.getClass().getSimpleName() + "\"");
      }

      java.awt.Color javaColor = java.awt.Color.decode(object.toString());
      return Color.fromRGB(javaColor.getRed(), javaColor.getGreen(), javaColor.getBlue());
   }

   public String serialize(ConfigurationWriter writer, Color color) {
      return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
   }
}
