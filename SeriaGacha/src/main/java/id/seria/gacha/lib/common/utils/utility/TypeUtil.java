package id.seria.gacha.lib.common.utils.utility;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeUtil {
   public TypeUtil() {
   }

   public static boolean isAssignable(Type type1, Type type2) {
      if (type1 == null || type2 == null) {
         return false;
      } else if (type1.equals(type2)) {
         return true;
      } else if (type1 instanceof Class) {
         return isClassAssignable((Class<?>)type1, type2);
      } else {
         return type1 instanceof ParameterizedType ? isParameterizedTypeAssignable((ParameterizedType)type1, type2) : false;
      }
   }

   private static boolean isClassAssignable(Class<?> classType, Type type) {
      return type instanceof Class ? classType.isAssignableFrom((Class<?>)type) : false;
   }

   private static boolean isParameterizedTypeAssignable(ParameterizedType parameterizedType, Type type) {
      if (type instanceof ParameterizedType) {
         ParameterizedType otherParameterizedType = (ParameterizedType)type;
         Type[] typeArguments = parameterizedType.getActualTypeArguments();
         Type[] otherTypeArguments = otherParameterizedType.getActualTypeArguments();
         if (typeArguments.length != otherTypeArguments.length) {
            return false;
         }

         for (int i = 0; i < typeArguments.length; i++) {
            if (!isAssignable(typeArguments[i], otherTypeArguments[i])) {
               return false;
            }
         }

         return isClassAssignable((Class<?>)parameterizedType.getRawType(), otherParameterizedType.getRawType());
      } else {
         return false;
      }
   }
}
