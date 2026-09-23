package id.seria.gacha.lib.xseries.reflection.constraint;

import id.seria.gacha.lib.xseries.reflection.ReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.XAccessFlag;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public enum ClassTypeConstraint implements ReflectiveConstraint {
   INTERFACE {
      @Override
      protected boolean test(Class<?> clazz) {
         return clazz.isInterface();
      }
   },
   ABSTRACT {
      @Override
      protected boolean test(Class<?> clazz) {
         return XAccessFlag.ABSTRACT.isSet(clazz.getModifiers());
      }
   },
   ENUM {
      @Override
      protected boolean test(Class<?> clazz) {
         return clazz.isEnum();
      }
   },
   RECORD {
      private final MethodHandle isRecord;

      {
         MethodHandle isRecord0;
         try {
            isRecord0 = MethodHandles.lookup().findVirtual(Class.class, "isRecord", MethodType.methodType(boolean.class));
         } catch (NoSuchMethodException | IllegalAccessException ex) {
            isRecord0 = null;
         }

         this.isRecord = isRecord0;
      }

      @Override
      protected boolean test(Class<?> clazz) {
         try {
            return (boolean)this.isRecord.invoke((Class)clazz);
         } catch (Throwable e) {
            throw new IllegalStateException("Cannot use Class#isRecord", e);
         }
      }
   },
   ANNOTATION {
      @Override
      protected boolean test(Class<?> clazz) {
         return clazz.isAnnotation();
      }
   };

   ClassTypeConstraint() {
   }

   protected abstract boolean test(Class<?> var1);

   @Override
   public ReflectiveConstraint.Result appliesTo(ReflectiveHandle<?> handle, Object jvm) {
      return jvm instanceof Class ? ReflectiveConstraint.Result.of(this.test((Class<?>)jvm)) : ReflectiveConstraint.Result.INCOMPATIBLE;
   }

   @Override
   public String category() {
      return "ClassType";
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "::" + this.name();
   }
}
