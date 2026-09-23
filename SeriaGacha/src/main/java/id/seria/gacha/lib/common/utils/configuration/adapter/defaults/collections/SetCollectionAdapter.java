package id.seria.gacha.lib.common.utils.configuration.adapter.defaults.collections;

import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetCollectionAdapter extends ConfigAdapter<Set<?>, ArrayList<?>> {
   public SetCollectionAdapter() {
   }

   public Set<?> deserialize(ConfigurationReader reader, Type type, Object object) throws ErrorException, WarningException {
      if (!(object instanceof List)) {
         throw new WarningException("Expected \"List\", founded \"" + object.getClass().getSimpleName() + "\"");
      } else {
         return new HashSet((List)object);
      }
   }

   public ArrayList<?> serialize(ConfigurationWriter writer, Set<?> collection) {
      return new ArrayList(collection);
   }
}
