package id.seria.gacha.lib.xseries;

public abstract class AbstractReferencedClass<T> {
   public AbstractReferencedClass() {
   }

   public abstract T object();

   protected final Object toVirtual() {
      return this.object();
   }

   @Override
   public final boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else {
         return this.getClass().isInstance(obj.getClass()) ? this.toVirtual().equals(((AbstractReferencedClass)obj).toVirtual()) : this.object().equals(obj);
      }
   }

   @Override
   public final int hashCode() {
      return this.toVirtual().hashCode();
   }

   @Override
   public final String toString() {
      return this.getClass().getSimpleName() + '(' + this.toVirtual().toString() + ')';
   }
}
