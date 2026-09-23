package id.seria.gacha.internal;

import java.util.Locale;

public final class Key {
   private final String id;

   private Key(String id) {
      this.id = id.toUpperCase(Locale.ROOT);
   }

   public static Key of(String id) {
      return new Key(id);
   }

   @Override
   public String toString() {
      return this.id;
   }

   @Override
   public boolean equals(Object o) {
      return o instanceof Key && this.id.equals(((Key)o).id);
   }

   @Override
   public int hashCode() {
      return this.id.hashCode();
   }
}
