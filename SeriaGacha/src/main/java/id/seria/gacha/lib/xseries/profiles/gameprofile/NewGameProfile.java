package id.seria.gacha.lib.xseries.profiles.gameprofile;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public final class NewGameProfile extends MojangGameProfile {
   protected NewGameProfile(GameProfile object) {
      super(object);
   }

   @Override
   public UUID id() {
      return this.object.getId();
   }

   @Override
   public String name() {
      return this.object.getName();
   }

   @Override
   public PropertyMap properties() {
      return this.object.getProperties();
   }

   @Override
   public MojangGameProfile copy(@Nullable Consumer<PropertyModifier> propertyModifier) {
      PropertyMap properties = this.properties();
      ListMultimap<String, Property> copiedProperties = MultimapBuilder.hashKeys(properties.size()).arrayListValues(1).build();
      copiedProperties.putAll(properties);
      if (propertyModifier != null) {
         propertyModifier.accept(new PropertyModifier(copiedProperties));
      }

      GameProfile clone = new GameProfile(this.id(), this.name());
      clone.getProperties().putAll(copiedProperties);
      return new NewGameProfile(clone);
   }
}
