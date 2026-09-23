package id.seria.gacha.lib.xseries.reflection.jvm.classes;

import id.seria.gacha.lib.xseries.reflection.ReflectiveNamespace;
import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class UnknownClassHandle extends ClassHandle {
   private final String name;

   public UnknownClassHandle(ReflectiveNamespace namespace, String name) {
      super(namespace);
      this.name = name;
   }

   @Override
   public Set<String> getPossibleNames() {
      return Collections.singleton(this.name);
   }

   public UnknownClassHandle asArray(int dimensions) {
      return new UnknownClassHandle(this.namespace, this.name + "[]");
   }

   @Override
   public boolean isArray() {
      return this.name.endsWith("[]");
   }

   public UnknownClassHandle copy() {
      return new UnknownClassHandle(this.namespace, this.name);
   }

   @NotNull
   public Class<?> reflect() throws ReflectiveOperationException {
      throw new ReflectiveOperationException("Unknown class: " + this.name);
   }

   @Override
   public String toString() {
      return "UnknownClassHandle(" + this.name + ')';
   }
}
