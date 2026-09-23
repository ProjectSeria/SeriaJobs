package id.seria.gacha.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReflectionNavigator {
   private Object instance;

   private ReflectionNavigator(Object instance) {
      this.instance = instance;
   }

   public static ReflectionNavigator of(Object instance) {
      return new ReflectionNavigator(instance);
   }

   public ReflectionNavigator method(String[] methodNames, Object... args) {
      Exception lastException = null;

      for (String methodName : methodNames) {
         try {
            Method method = findMethod(this.instance.getClass(), methodName, args);
            method.setAccessible(true);
            this.instance = method.invoke(this.instance, args);
            return this;
         } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            lastException = e;
         }
      }

      throw new RuntimeException(
         "Error invoking methods: " + String.join(", ", methodNames) + " for object " + (this.instance == null ? null : this.instance.getClass()),
         lastException
      );
   }

   public ReflectionNavigator method(String methodName, Object... args) {
      return this.method(new String[]{methodName}, args);
   }

   public ReflectionNavigator field(String fieldName) {
      try {
         Field field = this.instance.getClass().getDeclaredField(fieldName);
         field.setAccessible(true);
         this.instance = field.get(this.instance);
         return this;
      } catch (NoSuchFieldException | IllegalAccessException e) {
         throw new RuntimeException("Error accessing field: " + fieldName + " for object " + (this.instance == null ? null : this.instance.getClass()), e);
      }
   }

   public ReflectionNavigator setField(String fieldName, Object value) {
      try {
         Field field = this.instance.getClass().getDeclaredField(fieldName);
         field.setAccessible(true);
         field.set(this.instance, value);
         return this;
      } catch (NoSuchFieldException | IllegalAccessException e) {
         throw new RuntimeException("Error setting field: " + fieldName + " for object " + (this.instance == null ? null : this.instance.getClass()), e);
      }
   }

   public Object get() {
      return this.instance;
   }

   public int getInteger() {
      return (Integer)this.instance;
   }

   public double getDouble() {
      return (Double)this.instance;
   }

   public String getString() {
      return (String)this.instance;
   }

   public boolean getBoolean() {
      return (Boolean)this.instance;
   }

   public List list() {
      return this.instance instanceof Set ? new ArrayList((Set)this.instance) : (List)this.instance;
   }

   private static Method findMethod(Class<?> clazz, String methodName, Object... args) throws NoSuchMethodException {
      while (clazz != null) {
         for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == args.length) {
               return method;
            }
         }

         clazz = clazz.getSuperclass();
      }

      throw new NoSuchMethodException("Method not found: " + methodName);
   }

   public Iterable<?> iterable() {
      return (Iterable<?>)this.instance;
   }
}
