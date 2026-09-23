package id.seria.gacha.lib.common.utils.configuration.adapter.defaults.collections;

import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class LinkedHashSetCollectionAdapter extends ConfigAdapter<LinkedHashSet<?>, ArrayList<?>> {
   public LinkedHashSetCollectionAdapter() {
   }

   public LinkedHashSet<?> deserialize(ConfigurationReader reader, Type type, Object object) throws ErrorException, WarningException {
      if (!(object instanceof List)) {
         throw new WarningException("Expected \"List\", founded \"" + object.getClass().getSimpleName() + "\"");
      } else {
         return new LinkedHashSet((List)object);
      }
   }

   public ArrayList<?> serialize(ConfigurationWriter writer, LinkedHashSet<?> collection) {
      return new ArrayList(collection);
   }
}
