package id.seria.gacha.lib.xseries.reflection;

import id.seria.gacha.lib.xseries.reflection.aggregate.AggregateReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.aggregate.AggregateReflectiveSupplier;
import id.seria.gacha.lib.xseries.reflection.aggregate.VersionHandle;
import id.seria.gacha.lib.xseries.reflection.asm.XReflectASM;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.DynamicClassHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.StaticClassHandle;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftClassHandle;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftMapping;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftPackage;
import id.seria.gacha.lib.xseries.reflection.proxy.ReflectiveProxy;
import id.seria.gacha.lib.xseries.reflection.proxy.ReflectiveProxyObject;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class XReflection {
   @Nullable
   @Internal
   public static final String NMS_VERSION = findNMSVersionString();
   @Internal
   public static final String XSERIES_VERSION = "13.6.0";
   @TestOnly
   public static final String DISABLE_MINECRAFT_CAPABILITIES_PROPERTY = "xseries.xreflection.disable.minecraft";
   @Internal
   public static final boolean SUPPORTS_ASM;
   public static final int MAJOR_NUMBER;
   public static final int MINOR_NUMBER;
   public static final int PATCH_NUMBER;
   @Internal
   public static final String CRAFTBUKKIT_PACKAGE;
   @Internal
   public static final String NMS_PACKAGE;
   @Internal
   @Experimental
   public static final @Unmodifiable Set<MinecraftMapping> SUPPORTED_MAPPINGS;
   private static final Map<Class<?>, ReflectiveProxyObject> PROXIFIED_CLASSES;

   @Nullable
   @Internal
   public static String findNMSVersionString() {
      String found = null;

      for (Package pack : Package.getPackages()) {
         String name = pack.getName();
         if (name.startsWith("org.bukkit.craftbukkit.v")) {
            found = pack.getName().split("\\.")[3];

            try {
               Class.forName("org.bukkit.craftbukkit." + found + ".entity.CraftPlayer");
               break;
            } catch (ClassNotFoundException e) {
               found = null;
            }
         }
      }

      return found;
   }

   private static String isMinecraftDisabled() {
      try {
         return System.getProperty("xseries.xreflection.disable.minecraft");
      } catch (SecurityException ignored) {
         return null;
      }
   }

   @NotNull
   @Contract(pure = true)
   public static String getVersionInformation() {
      return "(NMS: "
         + (NMS_VERSION == null ? "Unknown NMS" : NMS_VERSION)
         + " | Parsed: "
         + MAJOR_NUMBER
         + '.'
         + MINOR_NUMBER
         + '.'
         + PATCH_NUMBER
         + " | Minecraft: "
         + Bukkit.getVersion()
         + " | Bukkit: "
         + Bukkit.getBukkitVersion()
         + ')';
   }

   @Nullable
   @Contract(pure = true)
   public static Integer getLatestPatchNumberOf(int minorVersion) {
      if (minorVersion <= 0) {
         throw new IllegalArgumentException("Minor version must be positive: " + minorVersion);
      }

      int[] patches = new int[]{1, 5, 2, 7, 2, 4, 10, 8, 4, 2, 2, 2, 2, 4, 2, 5, 1, 2, 4, 6, 8};
      return minorVersion > patches.length ? null : patches[minorVersion - 1];
   }

   private XReflection() {
   }

   @NotNull
   @Contract(value = "_, _ -> new", pure = true)
   public static <T> VersionHandle<T> v(int version, T handle) {
      return new VersionHandle<>(version, handle);
   }

   @NotNull
   @Contract(value = "_, _, _ -> new", pure = true)
   public static <T> VersionHandle<T> v(int version, int patch, T handle) {
      return new VersionHandle<>(version, patch, handle);
   }

   @NotNull
   @Contract(value = "_, _ -> new", pure = true)
   public static <T> VersionHandle<T> v(int version, Callable<T> handle) {
      return new VersionHandle<>(version, handle);
   }

   @NotNull
   @Contract(value = "_, _, _ -> new", pure = true)
   public static <T> VersionHandle<T> v(int version, int patch, Callable<T> handle) {
      return new VersionHandle<>(version, patch, handle);
   }

   @Contract(pure = true)
   public static boolean supports(int minorNumber) {
      return supports(1, minorNumber, 0);
   }

   @Contract(pure = true)
   public static boolean supports(int majorNumber, int minorNumber, int patchNumber) {
      return MAJOR_NUMBER == majorNumber
         ? (MINOR_NUMBER == minorNumber ? PATCH_NUMBER >= patchNumber : MINOR_NUMBER > minorNumber)
         : MAJOR_NUMBER > majorNumber;
   }

   @Contract(pure = true)
   public static boolean supports(int minorNumber, int patchNumber) {
      return supports(1, minorNumber, patchNumber);
   }

   @Deprecated
   @Contract(pure = true)
   public static boolean supportsPatch(int patchNumber) {
      return PATCH_NUMBER >= patchNumber;
   }

   @Deprecated
   @NotNull
   public static Class<?> getNMSClass(@Nullable String packageName, @NotNull String name) {
      if (packageName != null && supports(17)) {
         name = packageName + '.' + name;
      }

      try {
         return Class.forName(NMS_PACKAGE + '.' + name);
      } catch (ClassNotFoundException ex) {
         throw new IllegalArgumentException(ex);
      }
   }

   @Deprecated
   @NotNull
   public static Class<?> getNMSClass(@NotNull String name) {
      return getNMSClass(null, name);
   }

   public static boolean isRecord(Class<?> clazz) {
      try {
         Method isRecord = Class.class.getDeclaredMethod("isRecord");
         return (Boolean)isRecord.invoke(clazz);
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
         return false;
      }
   }

   @Deprecated
   @NotNull
   public static Class<?> getCraftClass(@NotNull String name) {
      try {
         return Class.forName(CRAFTBUKKIT_PACKAGE + '.' + name);
      } catch (ClassNotFoundException ex) {
         throw new IllegalArgumentException(ex);
      }
   }

   @NotNull
   @Contract(pure = true)
   public static Class<?> toArrayClass(@NotNull Class<?> clazz) {
      Objects.requireNonNull(clazz, "Class is null");

      try {
         return Class.forName("[L" + clazz.getName() + ';');
      } catch (ClassNotFoundException ex) {
         throw new IllegalArgumentException("Cannot find array class for class: " + clazz, ex);
      }
   }

   @NotNull
   @Contract(value = "-> new", pure = true)
   public static MinecraftClassHandle ofMinecraft() {
      return new MinecraftClassHandle(new ReflectiveNamespace());
   }

   @NotNull
   @Contract(value = "-> new", pure = true)
   public static DynamicClassHandle classHandle() {
      return new DynamicClassHandle(new ReflectiveNamespace());
   }

   @NotNull
   @Contract(value = "_ -> new", pure = true)
   public static StaticClassHandle of(Class<?> clazz) {
      return new StaticClassHandle(new ReflectiveNamespace(), clazz);
   }

   @NotNull
   @Contract(value = "-> new", pure = true)
   public static ReflectiveNamespace namespaced() {
      return new ReflectiveNamespace();
   }

   @SafeVarargs
   @NotNull
   @Contract(value = "_ -> new", pure = true)
   public static <T, H extends ReflectiveHandle<T>> AggregateReflectiveHandle<T, H> any(H... handles) {
      return new AggregateReflectiveHandle(Arrays.stream(handles).<Callable<H>>map(x -> () -> x).collect(Collectors.toList()));
   }

   @SafeVarargs
   @NotNull
   @Contract(value = "_ -> new", pure = true)
   public static <T, H extends ReflectiveHandle<T>> AggregateReflectiveHandle<T, H> anyOf(Callable<H>... handles) {
      return new AggregateReflectiveHandle<>(Arrays.asList(handles));
   }

   @Experimental
   @NotNull
   @Contract(value = "_, _ -> new", pure = true)
   public static <H extends ReflectiveHandle<?>, O> AggregateReflectiveSupplier<H, O> supply(H handle, O object) {
      AggregateReflectiveSupplier<H, O> supplier = new AggregateReflectiveSupplier<>();
      supplier.or(handle, object);
      return supplier;
   }

   @Experimental
   @NotNull
   @Contract(value = "_, _ -> new", pure = true)
   public static <H extends ReflectiveHandle<?>, O> AggregateReflectiveSupplier<H, O> supply(H handle, Supplier<O> object) {
      AggregateReflectiveSupplier<H, O> supplier = new AggregateReflectiveSupplier<>();
      supplier.or(handle, object);
      return supplier;
   }

   @Experimental
   @NotNull
   @Contract(value = "_ -> param1", mutates = "param1")
   public static <T extends Throwable> T relativizeSuppressedExceptions(@NotNull T ex) {
      Objects.requireNonNull(ex, "Cannot relativize null exception");
      StackTraceElement[] EMPTY_STACK_TRACE_ARRAY = new StackTraceElement[0];
      StackTraceElement[] mainStackTrace = ex.getStackTrace();

      for (Throwable suppressed : ex.getSuppressed()) {
         StackTraceElement[] suppressedStackTrace = suppressed.getStackTrace();
         List<StackTraceElement> relativized = new ArrayList<>(10);

         for (int i = 0; i < suppressedStackTrace.length; i++) {
            if (mainStackTrace.length <= i) {
               relativized = null;
               break;
            }

            StackTraceElement mainTrace = mainStackTrace[i];
            StackTraceElement suppTrace = suppressedStackTrace[i];
            if (mainTrace.equals(suppTrace)) {
               break;
            }

            relativized.add(suppTrace);
         }

         if (relativized != null) {
            suppressed.setStackTrace(relativized.toArray(EMPTY_STACK_TRACE_ARRAY));
         }
      }

      return ex;
   }

   @SuppressWarnings("unchecked")
   @Contract(value = "_ -> fail", pure = true)
   private static <T extends Throwable> void throwException(Throwable exception) throws T {
      throw (T)exception;
   }

   @Contract(value = "_ -> fail", pure = true)
   public static RuntimeException throwCheckedException(@NotNull Throwable exception) {
      Objects.requireNonNull(exception, "Cannot throw null exception");
      throwException(exception);
      return null;
   }

   @Experimental
   @Contract(value = "_ -> new", mutates = "param1")
   public static <T> CompletableFuture<T> stacktrace(@NotNull CompletableFuture<T> completableFuture) {
      StackTraceElement[] currentStacktrace = new Throwable().getStackTrace();
      return completableFuture.whenComplete(
         (value, ex) -> {
            if (ex == null) {
               completableFuture.complete((T)value);
            } else {
               try {
                  StackTraceElement[] exStacktrace = ex.getStackTrace();
                  if (exStacktrace.length >= 3) {
                     List<StackTraceElement> clearStacktrace = new ArrayList<>(Arrays.asList(exStacktrace));
                     Collections.reverse(clearStacktrace);
                     Iterator<StackTraceElement> iter = clearStacktrace.iterator();
                     List<String> watchClassNames = Arrays.asList(
                        "java.util.concurrent.CompletableFuture",
                        "java.util.concurrent.ThreadPoolExecutor",
                        "java.util.concurrent.ForkJoinTask",
                        "java.util.concurrent.ForkJoinWorkerThread",
                        "java.util.concurrent.ForkJoinPool"
                     );
                     List<String> watchMethodNames = Arrays.asList(
                        "postComplete",
                        "encodeThrowable",
                        "completeThrowable",
                        "tryFire",
                        "run",
                        "runWorker",
                        "scan",
                        "exec",
                        "doExec",
                        "topLevelExec",
                        "uniWhenComplete"
                     );

                     while (iter.hasNext()) {
                        StackTraceElement stackTraceElement = iter.next();
                        String className = stackTraceElement.getClassName();
                        String methodName = stackTraceElement.getMethodName();
                        if (!className.equals(Thread.class.getName())) {
                           if (!watchClassNames.stream().anyMatch(className::startsWith) || !watchMethodNames.stream().anyMatch(methodName::equals)) {
                              break;
                           }

                           iter.remove();
                        }
                     }

                     Collections.reverse(clearStacktrace);
                     exStacktrace = clearStacktrace.toArray(new StackTraceElement[0]);
                  }

                  StackTraceElement[] finalCurrentStackTrace = Arrays.stream(currentStacktrace).skip(1L).toArray(StackTraceElement[]::new);
                  ex.setStackTrace(concatenate(exStacktrace, finalCurrentStackTrace));
               } catch (Throwable ex2) {
                  ex.addSuppressed(ex2);
               } finally {
                  completableFuture.completeExceptionally(ex);
               }
            }
         }
      );
   }

   @Internal
   @Contract(value = "_, _ -> new", pure = true)
   public static <T> T[] concatenate(T[] a, T[] b) {
      int aLen = a.length;
      int bLen = b.length;
      T[] c = (T[])Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
      System.arraycopy(a, 0, c, 0, aLen);
      System.arraycopy(b, 0, c, aLen, bLen);
      return c;
   }

   @Experimental
   @NotNull
   public static <T extends ReflectiveProxyObject> T proxify(@NotNull Class<T> interfaceClass) {
      ReflectiveProxy.checkInterfaceClass(interfaceClass);
      ReflectiveProxyObject loaded = PROXIFIED_CLASSES.get(interfaceClass);
      if (loaded != null) {
         return (T)loaded;
      }

      T proxified;
      if (SUPPORTS_ASM) {
         proxified = XReflectASM.proxify(interfaceClass).create();
      } else {
         proxified = ReflectiveProxy.proxify(interfaceClass).proxy();
      }

      PROXIFIED_CLASSES.put(interfaceClass, proxified);
      return proxified;
   }

   static {
      boolean supportsASM;
      try {
         Class.forName("org.objectweb.asm.ClassWriter");
         Class.forName("org.objectweb.asm.MethodVisitor");
         Class.forName("org.objectweb.asm.FieldVisitor");
         Class.forName("org.objectweb.asm.Constants");
         Class.forName("org.objectweb.asm.Opcodes");
         supportsASM = true;
      } catch (ClassNotFoundException e) {
         supportsASM = false;
      }

      SUPPORTS_ASM = supportsASM;
      String verProp = isMinecraftDisabled();
      if (verProp != null) {
         System.out
            .println("[XSeries/XReflection] Testing with hardcoded server version: " + (verProp.isEmpty() ? "Disabled Minecraft Capabilities" : verProp));
         if (verProp.isEmpty() || verProp.equals("true")) {
            verProp = "1.21.4-R0.1-SNAPSHOT";
         }
      }

      Matcher bukkitVer = Pattern.compile("^(?<major>\\d+)\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+))?")
         .matcher(verProp != null ? verProp : Bukkit.getBukkitVersion());
      if (!bukkitVer.find()) {
         throw new IllegalStateException("Cannot parse server version: \"" + Bukkit.getBukkitVersion() + '"');
      }

      try {
         String patch = bukkitVer.group("patch");
         MAJOR_NUMBER = Integer.parseInt(bukkitVer.group("major"));
         MINOR_NUMBER = Integer.parseInt(bukkitVer.group("minor"));
         PATCH_NUMBER = Integer.parseInt(patch != null && !patch.isEmpty() ? patch : "0");
      } catch (Throwable ex) {
         throw new IllegalStateException("Failed to parse minor number: " + bukkitVer + ' ' + getVersionInformation(), ex);
      }

      CRAFTBUKKIT_PACKAGE = isMinecraftDisabled() != null ? null : Bukkit.getServer().getClass().getPackage().getName();
      NMS_PACKAGE = v(17, "net.minecraft").orElse("net.minecraft.server." + NMS_VERSION);
      if (isMinecraftDisabled() != null) {
         SUPPORTED_MAPPINGS = Collections.unmodifiableSet(EnumSet.noneOf(MinecraftMapping.class));
      } else {
         SUPPORTED_MAPPINGS = Collections.unmodifiableSet(
            supply(
                  ofMinecraft().inPackage(MinecraftPackage.NMS, "server.level").map(MinecraftMapping.MOJANG, "ServerPlayer"),
                  () -> EnumSet.of(MinecraftMapping.MOJANG)
               )
               .or(
                  ofMinecraft().inPackage(MinecraftPackage.NMS, "server.level").map(MinecraftMapping.MOJANG, "EntityPlayer"),
                  () -> EnumSet.of(MinecraftMapping.SPIGOT, MinecraftMapping.OBFUSCATED)
               )
               .get()
         );
      }

      PROXIFIED_CLASSES = new IdentityHashMap<>();
   }
}
