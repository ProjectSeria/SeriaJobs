package id.seria.gacha.lib.xseries.reflection.jvm.objects;

import id.seria.gacha.lib.xseries.reflection.ReflectiveHandle;
import org.jetbrains.annotations.NotNull;

public final class ReflectedObjectHandle implements ReflectiveHandle<ReflectedObject> {
   private final ReflectedObjectHandle.ReflectiveOperation jvmGetter;

   public ReflectedObjectHandle(ReflectedObjectHandle.ReflectiveOperation jvmGetter) {
      this.jvmGetter = jvmGetter;
   }

   @Override
   public ReflectiveHandle<ReflectedObject> copy() {
      return new ReflectedObjectHandle(this.jvmGetter);
   }

   @NotNull
   public ReflectedObject reflect() throws ReflectiveOperationException {
      return this.jvmGetter.get();
   }

   @NotNull
   @Override
   public ReflectiveHandle<ReflectedObject> jvm() {
      return this;
   }

   @FunctionalInterface
   public interface ReflectiveOperation {
      ReflectedObject get() throws ReflectiveOperationException;
   }
}
