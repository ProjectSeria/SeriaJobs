package id.seria.gacha.lib.xseries.reflection.constraint;

import id.seria.gacha.lib.xseries.reflection.ReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.XAccessFlag;
import java.lang.reflect.Member;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

@Experimental
public enum VisibilityConstraint implements ReflectiveConstraint {
   PUBLIC(XAccessFlag.PUBLIC),
   PRIVATE(XAccessFlag.PRIVATE),
   PROTECTED(XAccessFlag.PROTECTED);

   private final XAccessFlag accessFlag;

   VisibilityConstraint(XAccessFlag accessFlag) {
      this.accessFlag = accessFlag;
   }

   @Internal
   public XAccessFlag getAccessFlag() {
      return this.accessFlag;
   }

   @Override
   public ReflectiveConstraint.Result appliesTo(ReflectiveHandle<?> handle, Object jvm) {
      int mods;
      if (jvm instanceof Class) {
         mods = ((Class)jvm).getModifiers();
         if (this == PRIVATE) {
            return ReflectiveConstraint.Result.INCOMPATIBLE;
         }

         if (this == PROTECTED) {
            return ReflectiveConstraint.Result.of(!XAccessFlag.PUBLIC.isSet(mods));
         }
      } else {
         if (!(jvm instanceof Member)) {
            return ReflectiveConstraint.Result.INCOMPATIBLE;
         }

         mods = ((Member)jvm).getModifiers();
      }

      return ReflectiveConstraint.Result.of(this.accessFlag.isSet(mods));
   }

   @Override
   public String category() {
      return "Visibility";
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "::" + this.name();
   }
}
