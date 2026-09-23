package id.seria.gacha.lib.common.utils.configuration.adapter.defaults;

import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import id.seria.gacha.lib.common.utils.configuration.objects.ConfigVector;
import java.lang.reflect.Type;
import org.bukkit.util.Vector;

public class BukkitVectorAdapter extends ConfigAdapter<Vector, ConfigVector> {
   public BukkitVectorAdapter() {
   }

   public Vector deserialize(ConfigurationReader reader, Type type, Object object) throws ErrorException, WarningException {
      ConfigVector section = (ConfigVector)reader.toComplexObject(ConfigVector.class, null, object);
      return new Vector(section.getX(), section.getY(), section.getZ());
   }

   public ConfigVector serialize(ConfigurationWriter writer, Vector location) {
      return new ConfigVector(location.getX(), location.getY(), location.getZ());
   }
}
