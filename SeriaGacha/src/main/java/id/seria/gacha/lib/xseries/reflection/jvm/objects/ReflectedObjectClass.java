package id.seria.gacha.lib.xseries.reflection.jvm.objects;

final class ReflectedObjectClass extends AbstractReflectedObject {
   private final Class<?> delegate;

   ReflectedObjectClass(Class<?> delegate) {
      this.delegate = delegate;
   }

   @Override
   public ReflectedObject.Type type() {
      return ReflectedObject.Type.CLASS;
   }

   public Class<?> unreflect() {
      return this.delegate;
   }

   @Override
   public String name() {
      return this.delegate.getSimpleName();
   }

   @Override
   public Class<?> getDeclaringClass() {
      return this.delegate.getDeclaringClass();
   }

   @Override
   public int getModifiers() {
      return this.delegate.getModifiers();
   }
}
