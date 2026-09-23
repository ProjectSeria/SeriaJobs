package id.seria.gacha.lib.xseries.profiles.gameprofile;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import id.seria.gacha.lib.xseries.AbstractReferencedClass;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public abstract class MojangGameProfile extends AbstractReferencedClass<GameProfile> {
   protected final GameProfile object;

   protected MojangGameProfile(GameProfile object) {
      this.object = object;
   }

   public final GameProfile object() {
      return this.object;
   }

   public abstract UUID id();

   public abstract String name();

   public abstract PropertyMap properties();

   public final MojangGameProfile copy() {
      return this.copy(null);
   }

   public abstract MojangGameProfile copy(@Nullable Consumer<PropertyModifier> var1);

   @Nullable
   public Property getProperty(String name) {
      return (Property)Iterables.getFirst(this.properties().get(name), null);
   }
}
