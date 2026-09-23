package id.seria.gacha.lib.xseries.reflection.jvm.objects;

import java.lang.reflect.Field;
import java.lang.reflect.Member;

final class ReflectedObjectField extends AbstractMemberReflectedObject {
   private final Field delegate;

   ReflectedObjectField(Field delegate) {
      this.delegate = delegate;
   }

   @Override
   public ReflectedObject.Type type() {
      return ReflectedObject.Type.FIELD;
   }

   public Field unreflect() {
      return this.delegate;
   }

   @Override
   protected Member member() {
      return this.delegate;
   }
}
