package id.seria.gacha.lib.common.utils.objects;

import org.apache.commons.lang.Validate;

public abstract class LazyObject<T> {
   private T instance;

   public LazyObject() {
   }

   protected abstract T initialize() throws Exception;

   public T get() throws Exception {
      if (this.instance == null) {
         this.instance = this.initialize();
         Validate.isTrue(this.instance != null, "Initialization is returning null");
      }

      return this.instance;
   }

   public boolean isInitialized() {
      return this.instance != null;
   }

   public void dispose() {
      this.instance = null;
   }

   public void setInstance(T object) {
      this.instance = object;
   }
}
