package id.seria.gacha.lib.xseries.reflection.asm;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class ASMClassLoader extends ClassLoader {
   private static final String DEFINE_CLASS = "defineClass";

   protected ASMClassLoader() {
   }

   protected Class<?> defineClass(String name, byte[] bytes) {
      return super.defineClass(asmTypeToBinary(name), bytes, 0, bytes.length);
   }

   private static Class<?> defineClassLombockTransplanter(String className, byte[] bytecode, ClassLoader classLoader) {
      try {
         Class<?> methodHandles = Class.forName("java.lang.invoke.MethodHandles");
         Class<?> methodHandle = Class.forName("java.lang.invoke.MethodHandle");
         Class<?> methodType = Class.forName("java.lang.invoke.MethodType");
         Class<?> methodHandlesLookup = Class.forName("java.lang.invoke.MethodHandles$Lookup");
         Method lookupMethod = methodHandles.getDeclaredMethod("lookup");
         Method methodTypeMethod = methodType.getDeclaredMethod("methodType", Class.class, Class[].class);
         Method findVirtualMethod = methodHandlesLookup.getDeclaredMethod("findVirtual", Class.class, String.class, methodType);
         Method invokeMethod = methodHandle.getDeclaredMethod("invokeWithArguments", Object[].class);
         Object lookup = lookupMethod.invoke(null);
         Object type = methodTypeMethod.invoke(null, Class.class, new Class[]{String.class, byte[].class, int.class, int.class});
         Object method = findVirtualMethod.invoke(lookup, classLoader.getClass(), "defineClass", type);
         return (Class<?>)invokeMethod.invoke(method, (Object)new Object[]{classLoader, className, bytecode, 0, bytecode.length});
      } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
         throw new IllegalStateException(e);
      }
   }

   private static Class<?> defineClassJLA(String generatedClassName, byte[] bytecode) {
      try {
         Class<?> ClassDefiner = Class.forName("jdk.internal.reflect.ClassDefiner");
         Method meth = ClassDefiner.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class);
         return (Class<?>)meth.invoke(null, generatedClassName, bytecode, 0, bytecode.length, XReflectASM.class.getClassLoader());
      } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
         throw new IllegalStateException(e);
      }
   }

   private static String asmTypeToBinary(String dirPath) {
      return dirPath.replace('/', '.');
   }

   private static Class<?> methodHandleLoadClass(byte[] bytecode) {
      Lookup lookup = MethodHandles.lookup();

      try {
         MethodHandle defineClass = lookup.findVirtual(Lookup.class, "defineClass", MethodType.methodType(Class.class, byte[].class));
         return (Class)defineClass.invoke((Lookup)lookup, (byte[])bytecode);
      } catch (Throwable e) {
         throw new IllegalStateException(e);
      }
   }

   private static Class<?> loadClass(String className, byte[] bytecode) {
      try {
         ClassLoader loader = ClassLoader.getSystemClassLoader();
         Class<?> cls = Class.forName("java.lang.ClassLoader");
         Method method = cls.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
         method.setAccessible(true);

         Class<?> clazz;
         try {
            Object[] args = new Object[]{className, bytecode, 0, bytecode.length};
            clazz = (Class<?>)method.invoke(loader, args);
         } finally {
            method.setAccessible(false);
         }

         return clazz;
      } catch (Exception e) {
         throw new IllegalStateException("Failed to load class " + className + " into the system class loader", e);
      }
   }
}
