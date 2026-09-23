package id.seria.gacha.lib.common.utils.typeresolver;

import com.google.common.primitives.Primitives;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.AccessController;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import sun.misc.Unsafe;

public final class TypeResolver {
   private static final Map<Class<?>, Reference<Map<TypeVariable<?>, Type>>> TYPE_VARIABLE_CACHE = Collections.synchronizedMap(new WeakHashMap<>());
   private static volatile boolean CACHE_ENABLED = true;
   private static boolean RESOLVES_LAMBDAS;
   private static Object JAVA_LANG_ACCESS;
   private static Method GET_CONSTANT_POOL;
   private static Method GET_CONSTANT_POOL_SIZE;
   private static Method GET_CONSTANT_POOL_METHOD_AT;
   private static final Map<String, Method> OBJECT_METHODS = new HashMap<>();
   private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS;
   private static final Double JAVA_VERSION = Double.parseDouble(System.getProperty("java.specification.version", "0"));

   private TypeResolver() {
   }

   public static void enableCache() {
      CACHE_ENABLED = true;
   }

   public static void disableCache() {
      TYPE_VARIABLE_CACHE.clear();
      CACHE_ENABLED = false;
   }

   public static Class<?> resolveClass(Type type) {
      if (type instanceof Class) {
         return Primitives.wrap((Class)type);
      }

      if (type instanceof ParameterizedType) {
         return resolveClass(((ParameterizedType)type).getRawType());
      }

      if (type instanceof WildcardType) {
         Type[] upperBounds = ((WildcardType)type).getUpperBounds();
         if (upperBounds.length == 1) {
            return resolveClass(upperBounds[0]);
         }
      }

      return null;
   }

   public static <T, S extends T> Class<?> resolveRawArgument(Class<T> type, Class<S> subType) {
      return resolveRawArgument(resolveGenericType(type, subType), subType);
   }

   public static Class<?> resolveRawArgument(Type genericType, Class<?> subType) {
      Class<?>[] arguments = resolveRawArguments(genericType, subType);
      if (arguments == null) {
         return TypeResolver.Unknown.class;
      } else if (arguments.length != 1) {
         throw new IllegalArgumentException("Expected 1 argument for generic type " + genericType + " but found " + arguments.length);
      } else {
         return arguments[0];
      }
   }

   public static <T, S extends T> Class<?>[] resolveRawArguments(Class<T> type, Class<S> subType) {
      return resolveRawArguments(resolveGenericType(type, subType), subType);
   }

   public static <T, S extends T> Type reify(Class<T> type, Class<S> context) {
      return reify(resolveGenericType(type, context), getTypeVariableMap(context, null));
   }

   public static Type reify(Type type, Class<?> context) {
      return reify(type, getTypeVariableMap(context, null));
   }

   public static Type reify(Type type) {
      return reify(type, new HashMap<>(0));
   }

   public static Class<?>[] resolveRawArguments(Type genericType, Class<?> subType) {
      Class<?>[] result = null;
      Class<?> functionalInterface = null;
      if (RESOLVES_LAMBDAS && subType.isSynthetic()) {
         Class<?> fi = genericType instanceof ParameterizedType && ((ParameterizedType)genericType).getRawType() instanceof Class
            ? (Class)((ParameterizedType)genericType).getRawType()
            : (genericType instanceof Class ? (Class)genericType : null);
         if (fi != null && fi.isInterface()) {
            functionalInterface = fi;
         }
      }

      if (genericType instanceof ParameterizedType) {
         ParameterizedType paramType = (ParameterizedType)genericType;
         Type[] arguments = paramType.getActualTypeArguments();
         result = new Class[arguments.length];

         for (int i = 0; i < arguments.length; i++) {
            result[i] = resolveRawClass(arguments[i], subType, functionalInterface);
         }
      } else if (genericType instanceof TypeVariable) {
         result = new Class[]{resolveRawClass(genericType, subType, functionalInterface)};
      } else if (genericType instanceof Class) {
         TypeVariable<?>[] typeParams = ((Class)genericType).getTypeParameters();
         result = new Class[typeParams.length];

         for (int i = 0; i < typeParams.length; i++) {
            result[i] = resolveRawClass(typeParams[i], subType, functionalInterface);
         }
      }

      return result;
   }

   public static Type resolveGenericType(Class<?> type, Type subType) {
      Class<?> rawType;
      if (subType instanceof ParameterizedType) {
         rawType = (Class<?>)((ParameterizedType)subType).getRawType();
      } else {
         rawType = (Class<?>)subType;
      }

      if (type.equals(rawType)) {
         return subType;
      }

      if (type.isInterface()) {
         for (Type superInterface : rawType.getGenericInterfaces()) {
            Type result;
            if (superInterface != null && !superInterface.equals(Object.class) && (result = resolveGenericType(type, superInterface)) != null) {
               return result;
            }
         }
      }

      Type superClass = rawType.getGenericSuperclass();
      Type result;
      return superClass != null && !superClass.equals(Object.class) && (result = resolveGenericType(type, superClass)) != null ? result : null;
   }

   public static Class<?> resolveRawClass(Type genericType, Class<?> subType) {
      return resolveRawClass(genericType, subType, null);
   }

   private static Class<?> resolveRawClass(Type genericType, Class<?> subType, Class<?> functionalInterface) {
      if (genericType instanceof Class) {
         return (Class<?>)genericType;
      }

      if (genericType instanceof ParameterizedType) {
         return resolveRawClass(((ParameterizedType)genericType).getRawType(), subType, functionalInterface);
      }

      if (genericType instanceof GenericArrayType) {
         GenericArrayType arrayType = (GenericArrayType)genericType;
         Class<?> component = resolveRawClass(arrayType.getGenericComponentType(), subType, functionalInterface);
         return Array.newInstance(component, 0).getClass();
      }

      if (genericType instanceof TypeVariable) {
         TypeVariable<?> variable = (TypeVariable<?>)genericType;
         genericType = getTypeVariableMap(subType, functionalInterface).get(variable);
         genericType = genericType == null ? resolveBound(variable) : resolveRawClass(genericType, subType, functionalInterface);
      }

      return genericType instanceof Class ? (Class)genericType : TypeResolver.Unknown.class;
   }

   private static Type reify(Type genericType, Map<TypeVariable<?>, Type> typeVariableTypeMap) {
      if (genericType == null) {
         return null;
      } else {
         return genericType instanceof Class ? genericType : reify(genericType, typeVariableTypeMap, new HashMap<>());
      }
   }

   private static Type reify(Type genericType, Map<TypeVariable<?>, Type> typeVariableMap, Map<ParameterizedType, ReifiedParameterizedType> partial) {
      if (genericType instanceof Class) {
         return genericType;
      }

      if (genericType instanceof ParameterizedType) {
         ParameterizedType parameterizedType = (ParameterizedType)genericType;
         if (partial.containsKey(parameterizedType)) {
            ReifiedParameterizedType res = partial.get(genericType);
            res.addReifiedTypeArgument(res);
            return res;
         }

         Type[] genericTypeArguments = parameterizedType.getActualTypeArguments();
         ReifiedParameterizedType result = new ReifiedParameterizedType(parameterizedType);
         partial.put(parameterizedType, result);

         for (Type genericTypeArgument : genericTypeArguments) {
            Type reified = reify(genericTypeArgument, typeVariableMap, partial);
            if (reified != result) {
               result.addReifiedTypeArgument(reified);
            }
         }

         return result;
      } else if (genericType instanceof GenericArrayType) {
         GenericArrayType genericArrayType = (GenericArrayType)genericType;
         Type genericComponentType = genericArrayType.getGenericComponentType();
         Type reifiedComponentType = reify(genericArrayType.getGenericComponentType(), typeVariableMap, partial);
         if (genericComponentType == reifiedComponentType) {
            return genericComponentType;
         } else if (reifiedComponentType instanceof Class) {
            return Array.newInstance((Class<?>)reifiedComponentType, 0).getClass();
         } else {
            throw new UnsupportedOperationException(
               "Attempted to reify generic array type, whose generic component type could not be reified to some Class<?>. Handling for this case is not implemented"
            );
         }
      } else {
         if (genericType instanceof TypeVariable) {
            TypeVariable<?> typeVariable = (TypeVariable<?>)genericType;
            Type mapping = typeVariableMap.get(typeVariable);
            return mapping != null ? reify(mapping, typeVariableMap, partial) : reify(typeVariable.getBounds()[0], typeVariableMap, partial);
         }

         if (genericType instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType)genericType;
            Type[] upperBounds = wildcardType.getUpperBounds();
            Type[] lowerBounds = wildcardType.getLowerBounds();
            if (upperBounds.length == 1 && lowerBounds.length == 0) {
               return reify(upperBounds[0], typeVariableMap, partial);
            } else {
               throw new UnsupportedOperationException(
                  "Attempted to reify wildcard type with name '"
                     + wildcardType.getTypeName()
                     + "' which has "
                     + upperBounds.length
                     + " upper bounds and "
                     + lowerBounds.length
                     + " lower bounds. Reification of wildcard types is only supported for the trivial case of exactly 1 upper bound and 0 lower bounds."
               );
            }
         } else {
            throw new UnsupportedOperationException(
               "Reification of type with name '"
                  + genericType.getTypeName()
                  + "' and class name '"
                  + genericType.getClass().getName()
                  + "' is not implemented."
            );
         }
      }
   }

   private static Map<TypeVariable<?>, Type> getTypeVariableMap(Class<?> targetType, Class<?> functionalInterface) {
      Reference<Map<TypeVariable<?>, Type>> ref = TYPE_VARIABLE_CACHE.get(targetType);
      Map<TypeVariable<?>, Type> map = ref != null ? ref.get() : null;
      if (map == null) {
         map = new HashMap<>();
         if (functionalInterface != null) {
            populateLambdaArgs(functionalInterface, targetType, map);
         }

         populateSuperTypeArgs(targetType.getGenericInterfaces(), map, functionalInterface != null);
         Type genericType = targetType.getGenericSuperclass();

         for (Class<?> type = targetType.getSuperclass(); type != null && !Object.class.equals(type); type = type.getSuperclass()) {
            if (genericType instanceof ParameterizedType) {
               populateTypeArgs((ParameterizedType)genericType, map, false);
            }

            populateSuperTypeArgs(type.getGenericInterfaces(), map, false);
            genericType = type.getGenericSuperclass();
         }

         for (Class<?> var7 = targetType; var7.isMemberClass(); var7 = var7.getEnclosingClass()) {
            genericType = var7.getGenericSuperclass();
            if (genericType instanceof ParameterizedType) {
               populateTypeArgs((ParameterizedType)genericType, map, functionalInterface != null);
            }
         }

         if (CACHE_ENABLED) {
            TYPE_VARIABLE_CACHE.put(targetType, new WeakReference<>(map));
         }
      }

      return map;
   }

   private static void populateSuperTypeArgs(Type[] types, Map<TypeVariable<?>, Type> map, boolean depthFirst) {
      for (Type type : types) {
         if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            if (!depthFirst) {
               populateTypeArgs(parameterizedType, map, depthFirst);
            }

            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class) {
               populateSuperTypeArgs(((Class)rawType).getGenericInterfaces(), map, depthFirst);
            }

            if (depthFirst) {
               populateTypeArgs(parameterizedType, map, depthFirst);
            }
         } else if (type instanceof Class) {
            populateSuperTypeArgs(((Class)type).getGenericInterfaces(), map, depthFirst);
         }
      }
   }

   private static void populateTypeArgs(ParameterizedType type, Map<TypeVariable<?>, Type> map, boolean depthFirst) {
      if (type.getRawType() instanceof Class) {
         TypeVariable<?>[] typeVariables = ((Class)type.getRawType()).getTypeParameters();
         Type[] typeArguments = type.getActualTypeArguments();
         if (type.getOwnerType() != null) {
            Type owner = type.getOwnerType();
            if (owner instanceof ParameterizedType) {
               populateTypeArgs((ParameterizedType)owner, map, depthFirst);
            }
         }

         for (int i = 0; i < typeArguments.length; i++) {
            TypeVariable<?> variable = typeVariables[i];
            Type typeArgument = typeArguments[i];
            if (typeArgument instanceof Class) {
               map.put(variable, typeArgument);
            } else if (typeArgument instanceof GenericArrayType) {
               map.put(variable, typeArgument);
            } else if (typeArgument instanceof ParameterizedType) {
               map.put(variable, typeArgument);
            } else if (typeArgument instanceof TypeVariable) {
               TypeVariable<?> typeVariableArgument = (TypeVariable<?>)typeArgument;
               if (depthFirst) {
                  Type existingType = map.get(variable);
                  if (existingType != null) {
                     map.put(typeVariableArgument, existingType);
                     continue;
                  }
               }

               Type resolvedType = map.get(typeVariableArgument);
               if (resolvedType == null) {
                  resolvedType = resolveBound(typeVariableArgument);
               }

               map.put(variable, resolvedType);
            }
         }
      }
   }

   public static Type resolveBound(TypeVariable<?> typeVariable) {
      Type[] bounds = typeVariable.getBounds();
      if (bounds.length == 0) {
         return TypeResolver.Unknown.class;
      }

      Type bound = bounds[0];
      if (bound instanceof TypeVariable) {
         bound = resolveBound((TypeVariable<?>)bound);
      }

      return bound == Object.class ? TypeResolver.Unknown.class : bound;
   }

   private static void populateLambdaArgs(Class<?> functionalInterface, Class<?> lambdaType, Map<TypeVariable<?>, Type> map) {
      if (RESOLVES_LAMBDAS) {
         for (Method m : functionalInterface.getMethods()) {
            if (!isDefaultMethod(m) && !Modifier.isStatic(m.getModifiers()) && !m.isBridge()) {
               Method objectMethod = OBJECT_METHODS.get(m.getName());
               if (objectMethod == null || !Arrays.equals(m.getTypeParameters(), objectMethod.getTypeParameters())) {
                  Type returnTypeVar = m.getGenericReturnType();
                  Type[] paramTypeVars = m.getGenericParameterTypes();
                  Member member = getMemberRef(lambdaType);
                  if (member == null) {
                     return;
                  }

                  if (returnTypeVar instanceof TypeVariable) {
                     Class<?> returnType = member instanceof Method ? ((Method)member).getReturnType() : ((Constructor)member).getDeclaringClass();
                     returnType = wrapPrimitives(returnType);
                     if (!returnType.equals(Void.class)) {
                        map.put((TypeVariable<?>)returnTypeVar, returnType);
                     }
                  }

                  Class<?>[] arguments = member instanceof Method ? ((Method)member).getParameterTypes() : ((Constructor)member).getParameterTypes();
                  int paramOffset = 0;
                  if (paramTypeVars.length > 0 && paramTypeVars[0] instanceof TypeVariable && paramTypeVars.length == arguments.length + 1) {
                     Class<?> instanceType = member.getDeclaringClass();
                     map.put((TypeVariable<?>)paramTypeVars[0], instanceType);
                     paramOffset = 1;
                  }

                  int argOffset = 0;
                  if (paramTypeVars.length < arguments.length) {
                     argOffset = arguments.length - paramTypeVars.length;
                  }

                  for (int i = 0; i + argOffset < arguments.length; i++) {
                     if (paramTypeVars[i] instanceof TypeVariable) {
                        map.put((TypeVariable<?>)paramTypeVars[i + paramOffset], wrapPrimitives(arguments[i + argOffset]));
                     }
                  }

                  return;
               }
            }
         }
      }
   }

   private static boolean isDefaultMethod(Method m) {
      return JAVA_VERSION >= 1.8 && m.isDefault();
   }

   private static Member getMemberRef(Class<?> type) {
      Object constantPool;
      try {
         constantPool = GET_CONSTANT_POOL.invoke(JAVA_LANG_ACCESS, type);
      } catch (Exception ignore) {
         return null;
      }

      Member result = null;

      for (int i = getConstantPoolSize(constantPool) - 1; i >= 0; i--) {
         Member member = getConstantPoolMethodAt(constantPool, i);
         if (member != null
            && (!(member instanceof Constructor) || !member.getDeclaringClass().getName().equals("java.lang.invoke.SerializedLambda"))
            && !member.getDeclaringClass().isAssignableFrom(type)) {
            result = member;
            if (!(member instanceof Method) || !isAutoBoxingMethod((Method)member)) {
               break;
            }
         }
      }

      return result;
   }

   private static boolean isAutoBoxingMethod(Method method) {
      Class<?>[] parameters = method.getParameterTypes();
      return method.getName().equals("valueOf")
         && parameters.length == 1
         && parameters[0].isPrimitive()
         && wrapPrimitives(parameters[0]).equals(method.getDeclaringClass());
   }

   private static Class<?> wrapPrimitives(Class<?> clazz) {
      return clazz.isPrimitive() ? PRIMITIVE_WRAPPERS.get(clazz) : clazz;
   }

   private static int getConstantPoolSize(Object constantPool) {
      try {
         return (Integer)GET_CONSTANT_POOL_SIZE.invoke(constantPool);
      } catch (Exception ignore) {
         return 0;
      }
   }

   private static Member getConstantPoolMethodAt(Object constantPool, int i) {
      try {
         return (Member)GET_CONSTANT_POOL_METHOD_AT.invoke(constantPool, i);
      } catch (Exception ignore) {
         return null;
      }
   }

   static {
      try {
         Unsafe unsafe = AccessController.doPrivileged((java.security.PrivilegedExceptionAction<Unsafe>) () -> {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe)f.get(null);
         });
         Class<?> sharedSecretsClass;
         TypeResolver.AccessMaker accessSetter;
         if (JAVA_VERSION < 9.0) {
            sharedSecretsClass = Class.forName("sun.misc.SharedSecrets");
            accessSetter = accessibleObject -> accessibleObject.setAccessible(true);
         } else if (JAVA_VERSION < 12.0) {
            try {
               sharedSecretsClass = Class.forName("jdk.internal.misc.SharedSecrets");
            } catch (ClassNotFoundException e) {
               sharedSecretsClass = Class.forName("jdk.internal.access.SharedSecrets");
            }

            Field overrideField = AccessibleObject.class.getDeclaredField("override");
            long overrideFieldOffset = unsafe.objectFieldOffset(overrideField);
            accessSetter = accessibleObject -> unsafe.putBoolean(accessibleObject, overrideFieldOffset, true);
         } else {
            sharedSecretsClass = Class.forName("jdk.internal.access.SharedSecrets");
            Field implLookupField = Lookup.class.getDeclaredField("IMPL_LOOKUP");
            long implLookupFieldOffset = unsafe.staticFieldOffset(implLookupField);
            Object lookupStaticFieldBase = unsafe.staticFieldBase(implLookupField);
            Lookup implLookup = (Lookup)unsafe.getObject(lookupStaticFieldBase, implLookupFieldOffset);
            MethodHandle overrideSetter = implLookup.findSetter(AccessibleObject.class, "override", boolean.class);
            accessSetter = object -> overrideSetter.invokeWithArguments(object, true);
         }

         Method javaLangAccessGetter = sharedSecretsClass.getMethod("getJavaLangAccess");
         accessSetter.makeAccessible(javaLangAccessGetter);
         JAVA_LANG_ACCESS = javaLangAccessGetter.invoke(null);
         GET_CONSTANT_POOL = JAVA_LANG_ACCESS.getClass().getMethod("getConstantPool", Class.class);
         String constantPoolName = JAVA_VERSION < 9.0 ? "sun.reflect.ConstantPool" : "jdk.internal.reflect.ConstantPool";
         Class<?> constantPoolClass = Class.forName(constantPoolName);
         GET_CONSTANT_POOL_SIZE = constantPoolClass.getDeclaredMethod("getSize");
         GET_CONSTANT_POOL_METHOD_AT = constantPoolClass.getDeclaredMethod("getMethodAt", int.class);
         accessSetter.makeAccessible(GET_CONSTANT_POOL);
         accessSetter.makeAccessible(GET_CONSTANT_POOL_SIZE);
         accessSetter.makeAccessible(GET_CONSTANT_POOL_METHOD_AT);
         Object constantPool = GET_CONSTANT_POOL.invoke(JAVA_LANG_ACCESS, Object.class);
         GET_CONSTANT_POOL_SIZE.invoke(constantPool);

         for (Method method : Object.class.getDeclaredMethods()) {
            OBJECT_METHODS.put(method.getName(), method);
         }

         RESOLVES_LAMBDAS = true;
      } catch (Throwable var12) {
      }

      Map<Class<?>, Class<?>> types = new HashMap<>();
      types.put(boolean.class, Boolean.class);
      types.put(byte.class, Byte.class);
      types.put(char.class, Character.class);
      types.put(double.class, Double.class);
      types.put(float.class, Float.class);
      types.put(int.class, Integer.class);
      types.put(long.class, Long.class);
      types.put(short.class, Short.class);
      types.put(void.class, Void.class);
      PRIMITIVE_WRAPPERS = Collections.unmodifiableMap(types);
   }

   private interface AccessMaker {
      void makeAccessible(AccessibleObject var1) throws Throwable;
   }

   public static final class Unknown {
      private Unknown() {
      }
   }
}
