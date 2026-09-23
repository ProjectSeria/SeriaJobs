package id.seria.gacha.lib.xseries.reflection;

import id.seria.gacha.lib.xseries.reflection.jvm.objects.ReflectedObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
class CachedReflectiveHandle<T> implements ReflectiveHandle<T> {
   private final ReflectiveHandle<T> delegate;
   private T cache;
   private CachedReflectiveHandle<ReflectedObject> jvm;

   CachedReflectiveHandle(ReflectiveHandle<T> delegate) {
      this.delegate = delegate;
   }

   public ReflectiveHandle<T> getDelegate() {
      return this.delegate;
   }

   @Override
   public ReflectiveHandle<T> copy() {
      return this.delegate.copy();
   }

   @NotNull
   @Override
   public T reflect() throws ReflectiveOperationException {
      if (this.cache == null) {
         this.cache = this.delegate.reflect();
         return this.cache;
      } else {
         return this.cache;
      }
   }

   @NotNull
   @Override
   public ReflectiveHandle<ReflectedObject> jvm() {
      if (this.jvm == null) {
         this.jvm = new CachedReflectiveHandle<>(this.delegate.jvm());
         return this.jvm;
      } else {
         return this.jvm;
      }
   }
}
