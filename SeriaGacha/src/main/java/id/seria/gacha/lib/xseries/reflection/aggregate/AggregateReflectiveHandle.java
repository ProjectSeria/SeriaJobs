package id.seria.gacha.lib.xseries.reflection.aggregate;

import id.seria.gacha.lib.xseries.reflection.ReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import id.seria.gacha.lib.xseries.reflection.jvm.objects.ReflectedObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class AggregateReflectiveHandle<T, H extends ReflectiveHandle<T>> implements ReflectiveHandle<T> {
   private final List<Callable<H>> handles;
   private Consumer<H> handleModifier;

   @Internal
   public AggregateReflectiveHandle(Collection<Callable<H>> handles) {
      this.handles = new ArrayList<>(handles.size());
      this.handles.addAll(handles);
   }

   public AggregateReflectiveHandle<T, H> or(@NotNull H handle) {
      return this.or(() -> handle);
   }

   public AggregateReflectiveHandle<T, H> or(@NotNull Callable<H> handle) {
      this.handles.add(handle);
      return this;
   }

   public AggregateReflectiveHandle<T, H> modify(@Nullable Consumer<H> handleModifier) {
      this.handleModifier = handleModifier;
      return this;
   }

   public H getHandle() {
      ClassNotFoundException errors = null;

      for (Callable<H> handle : this.handles) {
         try {
            H handled = handle.call();
            if (this.handleModifier != null) {
               this.handleModifier.accept(handled);
            }

            if (!handled.exists()) {
               handled.reflect();
            }

            return handled;
         } catch (Throwable ex) {
            if (errors == null) {
               errors = new ClassNotFoundException("None of the aggregate handles were successful");
            }

            errors.addSuppressed(ex);
         }
      }

      throw XReflection.throwCheckedException(XReflection.relativizeSuppressedExceptions(errors));
   }

   public AggregateReflectiveHandle<T, H> copy() {
      AggregateReflectiveHandle<T, H> handle = new AggregateReflectiveHandle<>(new ArrayList<>(this.handles));
      handle.handleModifier = this.handleModifier;
      return handle;
   }

   @NotNull
   @Override
   public ReflectiveHandle<ReflectedObject> jvm() {
      return this.getHandle().jvm();
   }

   @Override
   public T reflect() throws ReflectiveOperationException {
      ClassNotFoundException errors = null;

      for (Callable<H> handle : this.handles) {
         H handled;
         try {
            handled = handle.call();
            if (this.handleModifier != null) {
               this.handleModifier.accept(handled);
            }
         } catch (Throwable ex) {
            if (errors == null) {
               errors = new ClassNotFoundException("None of the aggregate handles were successful");
            }

            errors.addSuppressed(ex);
            continue;
         }

         try {
            return handled.reflect();
         } catch (Throwable ex) {
            if (errors == null) {
               errors = new ClassNotFoundException("None of the aggregate handles were successful");
            }

            errors.addSuppressed(ex);
         }
      }

      throw (ClassNotFoundException)XReflection.relativizeSuppressedExceptions(errors);
   }
}
