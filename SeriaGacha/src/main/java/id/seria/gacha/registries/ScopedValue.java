package id.seria.gacha.registries;

import id.seria.gacha.internal.Key;
import lombok.Generated;
import org.jetbrains.annotations.Nullable;

public final class ScopedValue {
   private final Key action;
   private final Object scope;
   private final Object value;

   private ScopedValue(Key action, Object scope, Object value) {
      this.action = action;
      this.scope = scope;
      this.value = value;
   }

   public static ScopedValue of(Key action, @Nullable Object scope, Object value) {
      return new ScopedValue(action, scope, value);
   }

   @Generated
   public Key getAction() {
      return this.action;
   }

   @Generated
   public Object getScope() {
      return this.scope;
   }

   @Generated
   public Object getValue() {
      return this.value;
   }
}
