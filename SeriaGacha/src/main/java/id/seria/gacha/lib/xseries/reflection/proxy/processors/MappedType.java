package id.seria.gacha.lib.xseries.reflection.proxy.processors;

import id.seria.gacha.lib.xseries.reflection.proxy.ReflectiveProxyObject;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

public final class MappedType {
   public static final Map<Class<? extends ReflectiveProxyObject>, Class<?>> LOOK_AHEAD = new IdentityHashMap<>();
   public final Class<?> synthetic;
   public final Class<?> real;

   public MappedType(Class<?> synthetic, Class<?> real) {
      this.synthetic = synthetic;
      this.real = real;
   }

   public boolean isDifferent() {
      return this.synthetic != this.real;
   }

   public static Class<?>[] getRealTypes(MappedType[] types) {
      return Arrays.stream(types).map(x -> x.real).toArray(Class[]::new);
   }

   public static Class<?> getMappedTypeOrCreate(Class<? extends ReflectiveProxyObject> synthetic) {
      Class<?> real = LOOK_AHEAD.get(synthetic);
      if (real == null) {
         ReflectiveAnnotationProcessor processor = new ReflectiveAnnotationProcessor(synthetic);
         processor.processTargetClass();
         real = processor.getTargetClass();
         if (real == null) {
            throw new IllegalStateException("Look ahead type " + synthetic + " could not be processed.");
         }
      }

      return real;
   }

   @Override
   public String toString() {
      return "MappedType[synthetic: " + this.synthetic + ", real: " + this.real + ']';
   }
}
