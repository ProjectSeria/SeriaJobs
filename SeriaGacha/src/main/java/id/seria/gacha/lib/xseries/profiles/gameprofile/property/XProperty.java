package id.seria.gacha.lib.xseries.profiles.gameprofile.property;

import com.google.common.collect.Multimap;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import java.lang.invoke.MethodHandle;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class XProperty {
   private static final boolean USE_RECORDS = XReflection.isRecord(Property.class);
   private static final MethodHandle PropertyMap$ctor = XReflection.of(PropertyMap.class).constructor().reflectOrNull();

   private XProperty() {
   }

   @NotNull
   public static MojangProperty of(Property property) {
      Objects.requireNonNull(property, "Property is null");
      return USE_RECORDS ? new NewProperty(property) : new OldProperty(property);
   }

   public static MojangProperty create(String name, String value) {
      return of(new Property(name, value));
   }

   public static MojangProperty create(String name, String value, String signature) {
      return of(new Property(name, value, signature));
   }

   public static PropertyMap createPropertyMap(Multimap<String, Property> map) {
      PropertyMap propertyMap = new PropertyMap();
      if (map != null) {
         propertyMap.putAll(map);
      }
      return propertyMap;
   }
}
