package id.seria.gacha.lib.xseries.reflection.jvm.classes;

import id.seria.gacha.lib.xseries.reflection.ReflectiveNamespace;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class StaticClassHandle extends ClassHandle {
   @NotNull
   protected Class<?> clazz;

   public StaticClassHandle(ReflectiveNamespace namespace, @NotNull Class<?> clazz) {
      super(namespace);
      this.clazz = Objects.requireNonNull(clazz);
   }

   private Class<?> purifyClass() {
      Class<?> pureClazz = this.clazz;

      while (true) {
         Class<?> component = pureClazz.getComponentType();
         if (component == null) {
            return Objects.requireNonNull(pureClazz);
         }

         pureClazz = component;
      }
   }

   public StaticClassHandle asArray(int dimension) {
      Class<?> arrayClass = this.purifyClass();
      if (dimension > 0) {
         for (int i = 0; i < dimension; i++) {
            arrayClass = Array.newInstance(arrayClass, 0).getClass();
         }
      }

      this.clazz = arrayClass;
      return this;
   }

   public Class<?> reflect() throws ClassNotFoundException {
      return this.checkConstraints(this.clazz);
   }

   @Override
   public boolean isArray() {
      return this.clazz.isArray();
   }

   @Override
   public Set<String> getPossibleNames() {
      return Collections.singleton(this.clazz.getSimpleName());
   }

   public StaticClassHandle copy() {
      return new StaticClassHandle(this.namespace, this.clazz);
   }

   @Override
   public String toString() {
      return "StaticClassHandle(" + this.clazz + ')';
   }
}
