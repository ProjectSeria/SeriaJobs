package id.seria.gacha.lib.xseries.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public enum XAccessFlag {
   PUBLIC(1, true, XAccessFlag.JVMLocation.CLASS, XAccessFlag.JVMLocation.FIELD, XAccessFlag.JVMLocation.METHOD, XAccessFlag.JVMLocation.INNER_CLASS),
   PRIVATE(2, true, XAccessFlag.JVMLocation.FIELD, XAccessFlag.JVMLocation.METHOD, XAccessFlag.JVMLocation.INNER_CLASS),
   PROTECTED(4, true, XAccessFlag.JVMLocation.FIELD, XAccessFlag.JVMLocation.METHOD, XAccessFlag.JVMLocation.INNER_CLASS),
   STATIC(8, true, XAccessFlag.JVMLocation.FIELD, XAccessFlag.JVMLocation.METHOD, XAccessFlag.JVMLocation.INNER_CLASS),
   FINAL(
      16,
      true,
      XAccessFlag.JVMLocation.CLASS,
      XAccessFlag.JVMLocation.FIELD,
      XAccessFlag.JVMLocation.METHOD,
      XAccessFlag.JVMLocation.INNER_CLASS,
      XAccessFlag.JVMLocation.METHOD_PARAMETER
   ),
   SUPER(32, false, XAccessFlag.JVMLocation.CLASS),
   OPEN(32, false, XAccessFlag.JVMLocation.MODULE),
   TRANSITIVE(32, false, XAccessFlag.JVMLocation.MODULE_REQUIRES),
   SYNCHRONIZED(32, true, XAccessFlag.JVMLocation.METHOD),
   STATIC_PHASE(64, false, XAccessFlag.JVMLocation.MODULE_REQUIRES),
   VOLATILE(64, true, XAccessFlag.JVMLocation.FIELD),
   BRIDGE(getPrivateMod("BRIDGE"), false, XAccessFlag.JVMLocation.METHOD),
   TRANSIENT(128, true, XAccessFlag.JVMLocation.FIELD),
   VARARGS(getPrivateMod("VARARGS"), false, XAccessFlag.JVMLocation.METHOD),
   NATIVE(256, true, XAccessFlag.JVMLocation.METHOD),
   INTERFACE(512, false, XAccessFlag.JVMLocation.CLASS, XAccessFlag.JVMLocation.INNER_CLASS),
   ABSTRACT(1024, true, XAccessFlag.JVMLocation.CLASS, XAccessFlag.JVMLocation.METHOD, XAccessFlag.JVMLocation.INNER_CLASS),
   STRICT(2048, true),
   SYNTHETIC(
      getPrivateMod("SYNTHETIC"),
      false,
      XAccessFlag.JVMLocation.CLASS,
      XAccessFlag.JVMLocation.FIELD,
      XAccessFlag.JVMLocation.METHOD,
      XAccessFlag.JVMLocation.INNER_CLASS,
      XAccessFlag.JVMLocation.METHOD_PARAMETER,
      XAccessFlag.JVMLocation.MODULE,
      XAccessFlag.JVMLocation.MODULE_REQUIRES,
      XAccessFlag.JVMLocation.MODULE_EXPORTS,
      XAccessFlag.JVMLocation.MODULE_OPENS
   ),
   ANNOTATION(getPrivateMod("ANNOTATION"), false, XAccessFlag.JVMLocation.CLASS, XAccessFlag.JVMLocation.INNER_CLASS),
   ENUM(getPrivateMod("ENUM"), false, XAccessFlag.JVMLocation.CLASS, XAccessFlag.JVMLocation.FIELD, XAccessFlag.JVMLocation.INNER_CLASS),
   MANDATED(
      getPrivateMod("MANDATED"),
      false,
      XAccessFlag.JVMLocation.METHOD_PARAMETER,
      XAccessFlag.JVMLocation.MODULE,
      XAccessFlag.JVMLocation.MODULE_REQUIRES,
      XAccessFlag.JVMLocation.MODULE_EXPORTS,
      XAccessFlag.JVMLocation.MODULE_OPENS
   ),
   MODULE(32768, false, XAccessFlag.JVMLocation.CLASS);

   private static final XAccessFlag[] VALUES = values();
   private final int mask;
   private final boolean sourceModifier;
   private final Set<XAccessFlag.JVMLocation> locations;

   XAccessFlag(int mask, boolean sourceModifier, XAccessFlag.JVMLocation... locations) {
      this.mask = mask;
      this.sourceModifier = sourceModifier;
      this.locations = Collections.unmodifiableSet(
         locations.length == 0 ? EnumSet.noneOf(XAccessFlag.JVMLocation.class) : EnumSet.copyOf(Arrays.asList(locations))
      );
   }

   private static int getPrivateMod(String name) {
      try {
         Class<?> AccessFlag = Class.forName("java.lang.reflect.AccessFlag");
         Field modField = AccessFlag.getDeclaredField(name);
         Object mod = modField.get(null);
         Method mask = AccessFlag.getDeclaredMethod("mask");
         return (Integer)mask.invoke(mod);
      } catch (Throwable ex) {
         try {
            return (Integer)Modifier.class.getDeclaredField(name).get(null);
         } catch (NoSuchFieldException | IllegalAccessException ex2) {
            return 0;
         }
      }
   }

   @Contract(pure = true)
   public int mask() {
      return this.mask;
   }

   @Contract(pure = true)
   public boolean sourceModifier() {
      return this.sourceModifier;
   }

   @NotNull
   @Contract(pure = true)
   public @Unmodifiable Set<XAccessFlag.JVMLocation> locations() {
      return this.locations;
   }

   @Contract(pure = true)
   public static Set<XAccessFlag> of(int mod) {
      Set<XAccessFlag> accessFlags = EnumSet.noneOf(XAccessFlag.class);

      for (XAccessFlag flag : VALUES) {
         if (flag.isSet(mod)) {
            accessFlags.add(flag);
         }
      }

      return accessFlags;
   }

   @Contract(pure = true)
   public int add(int mod) {
      return mod | this.mask;
   }

   @Contract(pure = true)
   public int remove(int mod) {
      return mod & ~this.mask;
   }

   @Contract(pure = true)
   public boolean isSet(int mod) {
      return (mod & this.mask) != 0;
   }

   @Contract(pure = true)
   public static boolean isSet(int mod, XAccessFlag... flags) {
      for (XAccessFlag modifier : flags) {
         if (!modifier.isSet(mod)) {
            return false;
         }
      }

      return true;
   }

   @Contract(pure = true)
   public static Optional<Integer> getModifiers(Object jvm) {
      if (jvm instanceof Class) {
         return Optional.of(((Class)jvm).getModifiers());
      } else {
         return jvm instanceof Member ? Optional.of(((Member)jvm).getModifiers()) : Optional.empty();
      }
   }

   @Contract(pure = true)
   public static String toString(int mod) {
      StringJoiner flags = new StringJoiner(" ", "Flags::" + mod + '(', ")");

      for (XAccessFlag accessFlag : VALUES) {
         if (accessFlag.isSet(mod)) {
            flags.add(accessFlag.name().toLowerCase(Locale.ENGLISH));
         }
      }

      return flags.toString();
   }

   public enum JVMLocation {
      CLASS,
      FIELD,
      METHOD,
      INNER_CLASS,
      METHOD_PARAMETER,
      MODULE,
      MODULE_REQUIRES,
      MODULE_EXPORTS,
      MODULE_OPENS;

      JVMLocation() {
      }
   }
}
