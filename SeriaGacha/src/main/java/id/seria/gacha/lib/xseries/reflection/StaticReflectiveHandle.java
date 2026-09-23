package id.seria.gacha.lib.xseries.reflection;

import id.seria.gacha.lib.xseries.reflection.jvm.objects.ReflectedObject;
import org.jetbrains.annotations.NotNull;

public class StaticReflectiveHandle<T> implements ReflectiveHandle<T> {
   private final T reflected;
   private final ReflectiveHandle<ReflectedObject> jvm;

   public StaticReflectiveHandle(T reflected, ReflectedObject jvm) {
      this.reflected = reflected;
      this.jvm = new StaticReflectiveHandle<>(jvm);
   }

   private StaticReflectiveHandle(T reflected) {
      this.reflected = reflected;
      this.jvm = null;
   }

   @Override
   public ReflectiveHandle<T> copy() {
      return this;
   }

   @NotNull
   @Override
   public T reflect() throws ReflectiveOperationException {
      return this.reflected;
   }

   @NotNull
   @Override
   public ReflectiveHandle<ReflectedObject> jvm() {
      return this.jvm == null ? (ReflectiveHandle)this : this.jvm;
   }
}
