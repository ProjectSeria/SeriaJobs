package id.seria.gacha.lib.xseries.reflection.jvm.objects;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

final class ReflectedObjectMethod extends AbstractMemberReflectedObject {
   private final Method delegate;

   ReflectedObjectMethod(Method delegate) {
      this.delegate = delegate;
   }

   @Override
   public ReflectedObject.Type type() {
      return ReflectedObject.Type.METHOD;
   }

   public Method unreflect() {
      return this.delegate;
   }

   @Override
   protected Member member() {
      return this.delegate;
   }
}
