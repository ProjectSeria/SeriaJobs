package id.seria.gacha.lib.common.utils.configuration.adapter.defaults;

import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import id.seria.gacha.lib.xseries.XMaterial;
import java.lang.reflect.Type;
import org.bukkit.inventory.ItemStack;

public class BukkitItemStackAdapter extends ConfigAdapter<ItemStack, ItemStack> {
   public BukkitItemStackAdapter() {
   }

   public ItemStack deserialize(ConfigurationReader reader, Type type, Object object) throws ErrorException, WarningException {
      if (!(object instanceof String) && !(object instanceof ItemStack)) {
         throw new WarningException("Expected \"String\" or \"ItemStack\", founded \"" + object.getClass().getSimpleName() + "\"");
      }

      if (object instanceof String) {
         XMaterial xMaterial = XMaterial.matchXMaterial((String)object).orElse(null);
         if (xMaterial == null) {
            throw new WarningException("Material \"" + object.toString() + "\" does not exist.");
         } else {
            return xMaterial.parseItem();
         }
      } else {
         return (ItemStack)object;
      }
   }

   public ItemStack serialize(ConfigurationWriter writer, ItemStack item) {
      return item;
   }
}
