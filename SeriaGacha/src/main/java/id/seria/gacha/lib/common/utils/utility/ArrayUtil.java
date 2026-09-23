package id.seria.gacha.lib.common.utils.utility;

public class ArrayUtil {
   public static final int INDEX_NOT_FOUND = -1;

   public ArrayUtil() {
   }

   public static Object[] toObjectArray(int[] intArray) {
      Object[] objectArray = new Object[intArray.length];

      for (int i = 0; i < intArray.length; i++) {
         objectArray[i] = intArray[i];
      }

      return objectArray;
   }

   public static boolean contains(Object[] array, Object objectToFind) {
      return indexOf(array, objectToFind) != -1;
   }

   public static int indexOf(Object[] array, Object objectToFind) {
      return indexOf(array, objectToFind, 0);
   }

   public static int indexOf(Object[] array, Object objectToFind, int startIndex) {
      if (array == null) {
         return -1;
      }

      if (startIndex < 0) {
         startIndex = 0;
      }

      if (objectToFind == null) {
         for (int i = startIndex; i < array.length; i++) {
            if (array[i] == null) {
               return i;
            }
         }
      } else if (array.getClass().getComponentType().isInstance(objectToFind)) {
         for (int i = startIndex; i < array.length; i++) {
            if (objectToFind.equals(array[i])) {
               return i;
            }
         }
      }

      return -1;
   }
}
