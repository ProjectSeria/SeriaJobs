package id.seria.gacha.lib.common.utils.configuration.adapter.defaults;

import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import id.seria.gacha.lib.common.utils.configuration.objects.ConfigLocation;
import java.lang.reflect.Type;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class BukkitLocationAdapter extends ConfigAdapter<Location, ConfigLocation> {
   public BukkitLocationAdapter() {
   }

   public Location deserialize(ConfigurationReader reader, Type type, Object object) throws ErrorException, WarningException {
      ConfigLocation section = (ConfigLocation)reader.toComplexObject(ConfigLocation.class, null, object);
      World bukkitWorld = Bukkit.getWorld(section.getWorld());
      if (bukkitWorld == null) {
         throw new WarningException("World \"" + section.getWorld() + "\" does not exist.");
      } else {
         return new Location(bukkitWorld, section.getX(), section.getY(), section.getZ(), section.getYaw(), section.getPitch());
      }
   }

   public ConfigLocation serialize(ConfigurationWriter writer, Location location) {
      return new ConfigLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
   }
}
