package id.seria.gacha.lib.xseries.profiles.gameprofile.property;

import com.mojang.authlib.properties.Property;
import id.seria.gacha.lib.xseries.AbstractReferencedClass;
import javax.annotation.Nullable;
import org.jspecify.annotations.NonNull;

public abstract class MojangProperty extends AbstractReferencedClass<Property> {
   protected final Property object;

   protected MojangProperty(Property object) {
      this.object = object;
   }

   public final Property object() {
      return this.object;
   }

   public abstract @NonNull String name();

   public abstract @NonNull String value();

   @Nullable
   public abstract String signature();

   public final @NonNull Property copy() {
      return new Property(this.name(), this.value(), this.signature());
   }
}
