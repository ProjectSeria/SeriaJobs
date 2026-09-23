package id.seria.gacha.lib.xseries.profiles.gameprofile.property;

import com.mojang.authlib.properties.Property;

public final class NewProperty extends MojangProperty {
   protected NewProperty(Property object) {
      super(object);
   }

   @Override
   public String name() {
      return this.object.name();
   }

   @Override
   public String value() {
      return this.object.value();
   }

   @Override
   public String signature() {
      return this.object.signature();
   }
}
