package id.seria.gacha.lib.xseries.reflection.aggregate;

import id.seria.gacha.lib.xseries.reflection.ReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

@Experimental
public class AggregateReflectiveSupplier<H extends ReflectiveHandle<?>, O> {
   private final List<AggregateReflectiveSupplier<H, O>.ReflectivePair> handles = new ArrayList<>();
   private Consumer<H> handleModifier;

   @Internal
   public AggregateReflectiveSupplier() {
   }

   public AggregateReflectiveSupplier<H, O> or(@NotNull H handle, O object) {
      return this.or(() -> handle, object);
   }

   public AggregateReflectiveSupplier<H, O> or(@NotNull Callable<H> handle, O object) {
      return this.or(handle, () -> object);
   }

   public AggregateReflectiveSupplier<H, O> or(@NotNull H handle, Supplier<O> object) {
      return this.or(() -> handle, object);
   }

   public AggregateReflectiveSupplier<H, O> or(@NotNull Callable<H> handle, Supplier<O> object) {
      this.handles.add(new AggregateReflectiveSupplier.ReflectivePair(handle, object));
      return this;
   }

   public AggregateReflectiveSupplier<H, O> modify(@Nullable Consumer<H> handleModifier) {
      this.handleModifier = handleModifier;
      return this;
   }

   public O get() {
      ClassNotFoundException errors = null;

      for (AggregateReflectiveSupplier<H, O>.ReflectivePair pair : this.handles) {
         try {
            H handled = pair.handle.call();
            if (this.handleModifier != null) {
               this.handleModifier.accept(handled);
            }

            if (!handled.exists()) {
               handled.reflect();
            }

            return pair.object.get();
         } catch (Throwable ex) {
            if (errors == null) {
               errors = new ClassNotFoundException("None of the aggregate handles were successful");
            }

            errors.addSuppressed(ex);
         }
      }

      throw XReflection.throwCheckedException(XReflection.relativizeSuppressedExceptions(errors));
   }

   private final class ReflectivePair {
      private final Callable<H> handle;
      private final Supplier<O> object;

      private ReflectivePair(Callable<H> handle, Supplier<O> object) {
         this.handle = handle;
         this.object = object;
      }
   }
}
