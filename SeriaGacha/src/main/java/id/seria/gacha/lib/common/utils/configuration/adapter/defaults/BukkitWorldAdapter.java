package id.seria.gacha.lib.common.utils.configuration.adapter.defaults;

import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import java.lang.reflect.Type;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class BukkitWorldAdapter extends ConfigAdapter<World, String> {
   public BukkitWorldAdapter() {
   }

   public World deserialize(ConfigurationReader reader, Type type, Object object) throws ErrorException, WarningException {
      if (!(object instanceof String)) {
         throw new WarningException("Expected \"String\", founded \"" + object.getClass().getSimpleName() + "\"");
      } else {
         World bukkitWorld = Bukkit.getWorld((String)object);
         if (bukkitWorld == null) {
            throw new WarningException("World \"" + (String)object + "\" does not exist.");
         } else {
            return bukkitWorld;
         }
      }
   }

   public String serialize(ConfigurationWriter writer, World world) {
      return world.getName();
   }
}
