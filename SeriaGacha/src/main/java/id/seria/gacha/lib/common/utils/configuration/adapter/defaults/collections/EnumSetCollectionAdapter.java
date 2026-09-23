package id.seria.gacha.lib.common.utils.configuration.adapter.defaults.collections;

import id.seria.gacha.lib.common.utils.configuration.ConfigurationTokenizer;
import id.seria.gacha.lib.common.utils.configuration.ConfigurationUtilities;
import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class EnumSetCollectionAdapter extends ConfigAdapter<EnumSet<?>, List<String>> {
   public EnumSetCollectionAdapter() {
   }

   public EnumSet<?> deserialize(ConfigurationReader reader, Type type, Object object) throws ErrorException, WarningException {
      if (!(object instanceof String) && !(object instanceof List)) {
         throw new WarningException("Expected \"String\" or \"List\", founded \"" + object.getClass().getSimpleName() + "\"");
      }

      if (!(type instanceof ParameterizedType)) {
         throw new WarningException("Expected \"ParameterizedType\", founded \"" + type.getTypeName() + "\"");
      }

      Type[] params = ((ParameterizedType)type).getActualTypeArguments();
      Type enumType = params[0];
      if (!(enumType instanceof Class)) {
         throw new WarningException("Expected \"Class\", founded \"" + enumType.getTypeName() + "\"");
      }

      Class enumClass = (Class)enumType;
      EnumSet enumSet = EnumSet.noneOf(enumClass);
      if (object instanceof String) {
         return this.deserializeLegacy(reader, enumSet, enumClass, object);
      }

      for (Object rawElement : (List)object) {
         String element = rawElement.toString();

         try {
            enumSet.add(Enum.valueOf(enumClass, element));
         } catch (IllegalArgumentException e) {
            throw new WarningException(e.getMessage());
         }
      }

      return enumSet;
   }

   public EnumSet<?> deserializeLegacy(ConfigurationReader reader, EnumSet enumSet, Class enumClass, Object object) throws ErrorException, WarningException {
      ConfigurationTokenizer tokenizer = new ConfigurationTokenizer(object.toString(), ConfigurationUtilities.GENERIC_SERIALIZATION.separator());

      while (tokenizer.hasToken()) {
         String token = tokenizer.nextToken();

         try {
            enumSet.add(Enum.valueOf(enumClass, token));
         } catch (IllegalArgumentException e) {
            throw new WarningException(e.getMessage());
         }
      }

      return enumSet;
   }

   public List<String> serialize(ConfigurationWriter writer, EnumSet<?> collection) throws ErrorException {
      return collection.stream().map(Enum::name).collect(Collectors.toList());
   }
}
