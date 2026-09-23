package id.seria.gacha.lib.common.utils.objects;

import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang.Validate;

public abstract class AsyncLazyObject<T> {
   private CompletableFuture<T> instance;

   public AsyncLazyObject() {
   }

   protected abstract CompletableFuture<T> initialize() throws Exception;

   public T getNow() throws Exception {
      return this.get().get();
   }

   public CompletableFuture<T> get() throws Exception {
      synchronized (this) {
         if (this.instance == null) {
            this.instance = this.initialize();
            Validate.isTrue(this.instance != null, "Initialization is returning null");
         }

         return this.instance;
      }
   }

   public boolean isInitialized() {
      synchronized (this) {
         return this.instance != null;
      }
   }

   public void dispose() {
      synchronized (this) {
         this.instance = null;
      }
   }

   public void setInstance(T object) {
      synchronized (this) {
         this.instance = CompletableFuture.completedFuture(object);
      }
   }
}
