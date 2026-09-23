package id.seria.gacha.lib.book.internals;

import id.seria.gacha.lib.book.UnsupportedVersionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class NmsUtil {
   public static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
   public static final int major;
   public static final int minor;
   private static final Method craftPlayerGetHandle;
   private static final Method nmsItemStackSave;
   private static final Constructor<?> nbtTagCompoundConstructor;
   private static final Method craftItemStackAsNMSCopy;

   public static String itemToJson(ItemStack item) {
      try {
         Object nmsItemStack = nmsCopy(item);
         Object emptyTag = nbtTagCompoundConstructor.newInstance();
         Object json = nmsItemStackSave.invoke(nmsItemStack, emptyTag);
         return json.toString();
      } catch (Exception e) {
         throw new UnsupportedVersionException(e);
      }
   }

   public static Object toNms(Player player) throws InvocationTargetException, IllegalAccessException {
      return craftPlayerGetHandle.invoke(player);
   }

   public static Object nmsCopy(ItemStack item) throws InvocationTargetException, IllegalAccessException {
      return craftItemStackAsNMSCopy.invoke(null, item);
   }

   public static Class<?> getNmsClass(String className, boolean required) {
      return getNms17PlusClass("server." + version + "." + className, required);
   }

   public static Class<?> getNmsClass(String className, String post17middlePackage, boolean required) {
      Class<?> pre = getNmsClass(className, false);
      return pre != null ? pre : getNms17PlusClass(post17middlePackage + "." + className, required);
   }

   private static Class<?> getNms17PlusClass(String className, boolean required) {
      try {
         return Class.forName("net.minecraft." + className);
      } catch (ClassNotFoundException e) {
         if (required) {
            throw new RuntimeException("Cannot find NMS class " + className, e);
         } else {
            return null;
         }
      }
   }

   public static Class<?> getCraftClass(String path) {
      try {
         return Class.forName("org.bukkit.craftbukkit." + version + "." + path);
      } catch (ClassNotFoundException e) {
         throw new RuntimeException("Cannot find CraftBukkit class at path: " + path, e);
      }
   }

   private NmsUtil() {
   }

   static {
      try {
         Pattern pattern = Pattern.compile("v([0-9]+)_([0-9]+)");
         Matcher m = pattern.matcher(version);
         if (m.find()) {
            major = Integer.parseInt(m.group(1));
            minor = Integer.parseInt(m.group(2));
            Class<?> craftPlayerClass = getCraftClass("entity.CraftPlayer");
            craftPlayerGetHandle = craftPlayerClass.getMethod("getHandle");
            Class<?> craftItemStackClass = getCraftClass("inventory.CraftItemStack");
            craftItemStackAsNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Class nbtTagCompoundClass = getNmsClass("NBTTagCompound", "nbt", true);
            Class itemStackClass = getNmsClass("ItemStack", "world.item", true);

            Method itemStackSave;
            try {
               itemStackSave = itemStackClass.getMethod("b", nbtTagCompoundClass);
            } catch (NoSuchMethodException e) {
               itemStackSave = itemStackClass.getMethod("save", nbtTagCompoundClass);
            }

            nmsItemStackSave = itemStackSave;
            nbtTagCompoundConstructor = nbtTagCompoundClass.getConstructor();
         } else {
            throw new IllegalStateException("Cannot parse version \"" + version + "\", make sure it follows \"v<major>_<minor>...\"");
         }
      } catch (Exception e) {
         throw new IllegalStateException("Cannot initiate reflections for " + version, e);
      }
   }
}
