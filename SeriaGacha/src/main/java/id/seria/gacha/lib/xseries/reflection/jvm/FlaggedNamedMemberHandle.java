package id.seria.gacha.lib.xseries.reflection.jvm;

import id.seria.gacha.lib.xseries.reflection.XAccessFlag;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.ClassHandle;

public abstract class FlaggedNamedMemberHandle extends NamedMemberHandle {
   protected ClassHandle returnType;

   protected FlaggedNamedMemberHandle(ClassHandle clazz) {
      super(clazz);
   }

   public FlaggedNamedMemberHandle asStatic() {
      this.accessFlags.add(XAccessFlag.STATIC);
      return this;
   }

   public FlaggedNamedMemberHandle returns(Class<?> clazz) {
      this.returnType = XReflection.of(clazz);
      return this;
   }

   public FlaggedNamedMemberHandle returns(ClassHandle clazz) {
      this.returnType = clazz;
      return this;
   }

   public static Class<?>[] getParameters(Object owner, ClassHandle[] parameterTypes) {
      Class<?>[] classes = new Class[parameterTypes.length];
      int i = 0;

      for (ClassHandle parameterType : parameterTypes) {
         try {
            classes[i++] = parameterType.unreflect();
         } catch (Throwable ex) {
            throw XReflection.throwCheckedException(new ReflectiveOperationException("Unknown parameter " + parameterType + " for " + owner, ex));
         }
      }

      return classes;
   }

   protected Class<?> getReturnType() {
      try {
         return this.returnType.unreflect();
      } catch (Throwable ex) {
         throw XReflection.throwCheckedException(new ReflectiveOperationException("Unknown return type " + this.returnType + " for " + this));
      }
   }
}
