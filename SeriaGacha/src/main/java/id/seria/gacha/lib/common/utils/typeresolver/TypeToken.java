package id.seria.gacha.lib.common.utils.typeresolver;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Objects;

public class TypeToken {
   public TypeToken() {
   }

   public static boolean isAssignableFrom(Type type1, Type type2) {
      Objects.requireNonNull(type1, "type1 must not be null");
      Objects.requireNonNull(type2, "type2 must not be null");
      if (type1.equals(type2)) {
         return true;
      }

      if (type1 instanceof Class) {
         Class<?> classType1 = (Class<?>)type1;
         if (type2 instanceof Class) {
            Class<?> classType2 = (Class<?>)type2;
            return classType1.isAssignableFrom(classType2);
         } else if (type2 instanceof ParameterizedType) {
            ParameterizedType parameterizedType2 = (ParameterizedType)type2;
            return classType1.isAssignableFrom((Class<?>)parameterizedType2.getRawType());
         } else {
            return false;
         }
      } else if (type1 instanceof ParameterizedType) {
         ParameterizedType parameterizedType1 = (ParameterizedType)type1;
         if (type2 instanceof Class) {
            return ((Class)parameterizedType1.getRawType()).isAssignableFrom((Class<?>)type2);
         }

         if (type2 instanceof ParameterizedType) {
            ParameterizedType parameterizedType2 = (ParameterizedType)type2;
            if (!isAssignableFrom(parameterizedType1.getRawType(), parameterizedType2.getRawType())) {
               return false;
            }

            Type[] typeArgs1 = parameterizedType1.getActualTypeArguments();
            Type[] typeArgs2 = parameterizedType2.getActualTypeArguments();
            if (typeArgs1.length != typeArgs2.length) {
               return false;
            }

            for (int i = 0; i < typeArgs1.length; i++) {
               if (!isAssignableFrom(typeArgs1[i], typeArgs2[i])) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else if (type1 instanceof WildcardType) {
         WildcardType wildcardType1 = (WildcardType)type1;
         Type[] upperBounds1 = wildcardType1.getUpperBounds();
         Type[] lowerBounds1 = wildcardType1.getLowerBounds();
         if (upperBounds1.length == 1 && lowerBounds1.length == 0) {
            return isAssignableFrom(upperBounds1[0], type2);
         }

         if (upperBounds1.length != 0 || lowerBounds1.length != 1) {
            return false;
         }

         if (type2 instanceof Class) {
            return isAssignableFrom(lowerBounds1[0], type2);
         }

         if (type2 instanceof ParameterizedType) {
            return isAssignableFrom(lowerBounds1[0], ((ParameterizedType)type2).getRawType());
         }

         if (type2 instanceof WildcardType) {
            WildcardType wildcardType2 = (WildcardType)type2;
            Type[] upperBounds2 = wildcardType2.getUpperBounds();
            Type[] lowerBounds2 = wildcardType2.getLowerBounds();
            if (upperBounds2.length == 1 && lowerBounds2.length == 0) {
               return isAssignableFrom(lowerBounds1[0], upperBounds2[0]);
            } else {
               return upperBounds2.length == 0 && lowerBounds2.length == 1 ? isAssignableFrom(lowerBounds1[0], lowerBounds2[0]) : false;
            }
         } else {
            return false;
         }
      } else if (type1 instanceof ParameterizedType) {
         if (type2 instanceof Class) {
            ParameterizedType parameterizedType1 = (ParameterizedType)type1;
            Class<?> rawType1 = (Class<?>)parameterizedType1.getRawType();
            return rawType1.isAssignableFrom((Class<?>)type2);
         }

         if (type2 instanceof ParameterizedType) {
            ParameterizedType parameterizedType1 = (ParameterizedType)type1;
            ParameterizedType parameterizedType2 = (ParameterizedType)type2;
            Type[] actualTypeArguments1 = parameterizedType1.getActualTypeArguments();
            Type[] actualTypeArguments2 = parameterizedType2.getActualTypeArguments();
            Type rawType1 = parameterizedType1.getRawType();
            Type rawType2 = parameterizedType2.getRawType();
            if (isAssignableFrom(rawType1, rawType2)) {
               TypeVariable<?>[] typeParameters1 = ((Class)rawType1).getTypeParameters();
               if (actualTypeArguments1.length == actualTypeArguments2.length && typeParameters1.length == actualTypeArguments1.length) {
                  for (int i = 0; i < actualTypeArguments1.length; i++) {
                     Type actualTypeArgument1 = actualTypeArguments1[i];
                     Type actualTypeArgument2 = actualTypeArguments2[i];
                     TypeVariable<?> typeParameter1 = typeParameters1[i];
                     if (actualTypeArgument1 instanceof TypeVariable) {
                        if (!isAssignableFrom(resolveTypeVariable(parameterizedType1, (TypeVariable<?>)actualTypeArgument1), actualTypeArgument2)) {
                           return false;
                        }
                     } else if (!isAssignableFrom(actualTypeArgument1, actualTypeArgument2)) {
                        return false;
                     }
                  }

                  return true;
               }
            }

            return false;
         } else if (type2 instanceof WildcardType) {
            WildcardType wildcardType2 = (WildcardType)type2;
            if (isAssignableFrom(type1, wildcardType2.getUpperBounds()[0])) {
               return true;
            }

            Type[] lowerBounds2 = wildcardType2.getLowerBounds();
            return lowerBounds2.length == 1 && isAssignableFrom(type1, lowerBounds2[0]);
         } else {
            return false;
         }
      } else if (type1 instanceof GenericArrayType) {
         if (type2 instanceof GenericArrayType) {
            GenericArrayType genericArrayType1 = (GenericArrayType)type1;
            GenericArrayType genericArrayType2 = (GenericArrayType)type2;
            return isAssignableFrom(genericArrayType1.getGenericComponentType(), genericArrayType2.getGenericComponentType());
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static Type resolveTypeVariable(ParameterizedType parameterizedType, TypeVariable<?> typeVariable) {
      Class<?> rawType = (Class<?>)parameterizedType.getRawType();
      TypeVariable<?>[] typeParameters = rawType.getTypeParameters();

      for (int i = 0; i < typeParameters.length; i++) {
         if (typeParameters[i].equals(typeVariable)) {
            return parameterizedType.getActualTypeArguments()[i];
         }
      }

      return typeVariable;
   }
}
