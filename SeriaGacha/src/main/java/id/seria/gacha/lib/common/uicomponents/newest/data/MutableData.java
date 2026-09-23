package id.seria.gacha.lib.common.uicomponents.newest.data;

import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import lombok.Generated;

public class MutableData<T> {
   private final String key;
   private T data;

   public static <T> MutableData<T> syncData(ContainerView view, String key, T data) {
      return new MutableData<T>(key, data).sync(view);
   }

   @Deprecated
   public MutableData(String key, T data) {
      this.key = key;
      this.data = data;
   }

   public T get() {
      return this.data;
   }

   public void set(T data) {
      this.data = data;
   }

   public MutableData<T> sync(ContainerView view) {
      String metadataKey = "components.value." + this.key;
      Object metadata = view.getMetadata(metadataKey);
      if (metadata != null) {
         this.data = (T)metadata;
      } else if (this.data != null) {
         view.setMetadata("components.value." + this.key, this.data, true);
      }

      return this;
   }

   @Generated
   public MutableData(String key) {
      this.key = key;
   }

   @Generated
   public String getKey() {
      return this.key;
   }
}
