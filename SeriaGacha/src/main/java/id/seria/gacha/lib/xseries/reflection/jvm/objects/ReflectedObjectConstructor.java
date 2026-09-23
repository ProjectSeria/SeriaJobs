package id.seria.gacha.lib.xseries.reflection.jvm.objects;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;

final class ReflectedObjectConstructor extends AbstractMemberReflectedObject {
   private final Constructor<?> delegate;

   ReflectedObjectConstructor(Constructor<?> delegate) {
      this.delegate = delegate;
   }

   @Override
   public ReflectedObject.Type type() {
      return ReflectedObject.Type.CONSTRUCTOR;
   }

   @Override
   public String name() {
      return "<init>";
   }

   public Constructor<?> unreflect() {
      return this.delegate;
   }

   @Override
   protected Member member() {
      return this.delegate;
   }
}
