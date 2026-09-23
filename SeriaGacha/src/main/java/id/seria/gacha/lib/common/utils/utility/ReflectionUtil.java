package id.seria.gacha.lib.common.utils.utility;

import id.seria.gacha.lib.common.utils.version.ServerVersion;
import id.seria.gacha.lib.common.utils.version.VersionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ReflectionUtil {
   public static final String NMS_PATH = getNMSPackageName();
   public static final String CB_PATH = getCBPackageName();

   public ReflectionUtil() {
   }

   public static String getNMSPackageName() {
      try {
         return VersionUtils.isPaperServer() && ServerVersion.isGreaterEqualThan(ServerVersion.v1_20_R6)
            ? "net.minecraft.server"
            : "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
      } catch (ArrayIndexOutOfBoundsException ex) {
         Bukkit.getLogger().warning("[Phoenix Plugins] Error on getting nms package name. trying using paper remap");
         return "net.minecraft.server";
      }
   }

   public static String getCBPackageName() {
      try {
         return VersionUtils.isPaperServer() && ServerVersion.isGreaterEqualThan(ServerVersion.v1_20_R6)
            ? "org.bukkit.craftbukkit"
            : "org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
      } catch (ArrayIndexOutOfBoundsException ex) {
         Bukkit.getLogger().warning("[Phoenix Plugins] Error on getting cb package name. trying using paper remap");
         return "org.bukkit.craftbukkit";
      }
   }

   public static Class<?> getClass(String name) {
      try {
         return Class.forName(name);
      } catch (ClassNotFoundException var2) {
         return null;
      }
   }

   public static Class<?> getNMSClass(String className) {
      return getClass(NMS_PATH + "." + className);
   }

   public static Class<?> getCBClass(String className) {
      return getClass(CB_PATH + "." + className);
   }

   public static Field getField(Class<?> clazz, String fieldName) {
      try {
         Field field = clazz.getDeclaredField(fieldName);
         if (!field.isAccessible()) {
            field.setAccessible(true);
         }

         return field;
      } catch (NoSuchFieldException e) {
         e.printStackTrace();
         return null;
      }
   }

   public static <T> T getField(Class<?> clazz, String fieldName, Object instance) {
      try {
         return (T)getField(clazz, fieldName).get(instance);
      } catch (IllegalAccessException e) {
         e.printStackTrace();
         return null;
      }
   }

   public static void setField(Class<?> clazz, String fieldName, Object instance, Object value) {
      try {
         getField(clazz, fieldName).set(instance, value);
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      }
   }

   public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
      try {
         Method method = clazz.getDeclaredMethod(methodName, params);
         if (!method.isAccessible()) {
            method.setAccessible(true);
         }

         return method;
      } catch (NoSuchMethodException e) {
         e.printStackTrace();
         return null;
      }
   }

   public static <T> T invokeMethod(Method method, Object instance, Object... args) {
      try {
         return (T)method.invoke(instance, args);
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (InvocationTargetException e) {
         e.printStackTrace();
      }

      return null;
   }

   public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... params) {
      try {
         Constructor<?> constructor = clazz.getConstructor(params);
         if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
         }

         return constructor;
      } catch (NoSuchMethodException e) {
         e.printStackTrace();
         return null;
      }
   }

   public static <T> T invokeConstructor(Constructor<?> constructor, Object... args) {
      try {
         return (T)constructor.newInstance(args);
      } catch (InstantiationException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (InvocationTargetException e) {
         e.printStackTrace();
      }

      return null;
   }

   public static Object getBlockPosition(Player player) {
      try {
         Object handle = player.getClass().getMethod("getHandle").invoke(player);
         Constructor<?> constructor = getNMSClass("BlockPosition").getConstructor(getNMSClass("Entity"));
         return constructor.newInstance(handle);
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public static void sendPacket(Player player, Object packet) {
      try {
         Object handle = player.getClass().getMethod("getHandle").invoke(player);
         Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
         playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
