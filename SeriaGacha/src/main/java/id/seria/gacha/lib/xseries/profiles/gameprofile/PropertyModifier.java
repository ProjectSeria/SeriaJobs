package id.seria.gacha.lib.xseries.profiles.gameprofile;

import com.google.common.collect.Multimap;
import com.mojang.authlib.properties.Property;

public final class PropertyModifier {
   private final Multimap<String, Property> properties;

   public PropertyModifier(Multimap<String, Property> properties) {
      this.properties = properties;
   }

   public void setProperty(String name, String value) {
      this.properties.removeAll(name);
      this.properties.put(name, new Property(name, value));
   }

   public void setProperty(String name, Property value) {
      this.properties.removeAll(name);
      this.properties.put(name, value);
   }

   public void removeProperty(String name) {
      this.properties.removeAll(name);
   }

   public Multimap<String, Property> getProperties() {
      return this.properties;
   }
}
