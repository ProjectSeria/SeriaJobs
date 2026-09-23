package id.seria.gacha.api.sdk.plugin.extensions;

import id.seria.gacha.api.sdk.internal.LifecycleObject;
import java.util.UUID;
import lombok.Generated;

public abstract class Extension implements LifecycleObject {
   private final Extension.Unsafe unsafe = new Extension.Unsafe();
   private boolean enabled;
   private String name = "Extension-" + UUID.randomUUID();
   private String description = "Empty description";

   public Extension() {
   }

   public Extension.Unsafe unsafe() {
      return this.unsafe;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getDescription() {
      return this.description;
   }

   @Generated
   public void setName(String name) {
      this.name = name;
   }

   @Generated
   public void setDescription(String description) {
      this.description = description;
   }

   public class Unsafe {
      public Unsafe() {
      }

      public void setEnabled(boolean state) {
         Extension.this.enabled = state;
      }
   }
}
