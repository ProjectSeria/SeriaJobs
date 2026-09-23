package id.seria.gacha.lib.xseries.reflection.parser;

import id.seria.gacha.lib.xseries.reflection.ReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.ReflectiveNamespace;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import id.seria.gacha.lib.xseries.reflection.jvm.ConstructorMemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.FieldMemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.FlaggedNamedMemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.MemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.MethodMemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.ClassHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.DynamicClassHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.PackageHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.StaticClassHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.UnknownClassHandle;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftPackage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class ReflectionParser {
   private static final String[] DEFAULT_CHECKED_PACKAGES = new String[]{"java.util", "java.util.function", "java.lang", "java.io"};
   private final String declaration;
   private Pattern pattern;
   private Matcher matcher;
   private ReflectiveNamespace namespace;
   private Map<String, Class<?>> cachedImports;
   private String[] checkedPackages = DEFAULT_CHECKED_PACKAGES;
   private final Set<ReflectionParser.Flag> flags = EnumSet.noneOf(ReflectionParser.Flag.class);
   private static final PackageHandle[] PACKAGE_HANDLES = MinecraftPackage.values();
   @Language("RegExp")
   private static final String GENERIC = "(?:\\s*<\\s*[.\\w<>\\[\\], ]+\\s*>)?";
   @Language("RegExp")
   private static final String ARRAY = "(?:(?:\\[])*)";
   @Language("RegExp")
   private static final String PACKAGE_REGEX = "(?:package\\s+(?<package>(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)\\s*;\\s*)?";
   @Language("RegExp")
   private static final String CLASS_TYPES = "(?<classType>class|interface|enum|record)";
   @Language("RegExp")
   private static final String PARAMETERS = "\\s*\\(\\s*(?<parameters>[\\w$_,.<?>\\[\\] ]+)?\\s*\\)";
   @Language("RegExp")
   private static final String THROWS = "(?:\\s*throws\\s+(?<throws>(?:" + type(null).array(false) + ")(?:\\s*,\\s*" + type(null).array(false) + ")*))?";
   @Language("RegExp")
   private static final String END_DECL = "\\s*;?\\s*";
   private static final Pattern CLASS = Pattern.compile(
      "(?:package\\s+(?<package>(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)\\s*;\\s*)?"
         + ReflectionParser.Flag.FLAGS_REGEX
         + "(?<classType>class|interface|enum|record)"
         + "\\s+"
         + type("className")
         + "(?:\\(\\))?(?:\\s+extends\\s+"
         + id("superclasses")
         + ")?(?:\\s+implements\\s+(?<interfaces>(?:"
         + type(null).array(false)
         + ")(?:\\s*,\\s*"
         + type(null).array(false)
         + ")*))?(?:\\s*\\{\\s*})?\\s*"
   );
   private static final Pattern METHOD = Pattern.compile(
      ReflectionParser.Flag.FLAGS_REGEX
         + type("methodReturnType")
         + "\\s+"
         + id("methodName")
         + "\\s*\\(\\s*(?<parameters>[\\w$_,.<?>\\[\\] ]+)?\\s*\\)"
         + THROWS
         + "\\s*;?\\s*"
   );
   private static final Pattern CONSTRUCTOR = Pattern.compile(
      ReflectionParser.Flag.FLAGS_REGEX + "\\s+" + id("className") + "\\s*\\(\\s*(?<parameters>[\\w$_,.<?>\\[\\] ]+)?\\s*\\)" + "\\s*;?\\s*"
   );
   private static final Pattern FIELD = Pattern.compile(ReflectionParser.Flag.FLAGS_REGEX + type("fieldType") + "\\s+" + id("fieldName") + "\\s*;?\\s*");
   private static final Map<String, Class<?>> PREDEFINED_TYPES = new HashMap<>();

   public ReflectionParser(@Language("Java") String declaration) {
      this.declaration = declaration;
   }

   public ReflectionParser checkedPackages(
      @org.intellij.lang.annotations.Pattern(
   "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*"
) String... checkedPackages
   ) {
      this.checkedPackages = checkedPackages;
      return this;
   }

   private static ReflectionParser.IDHandler id(@NotNull @Language("RegExp") String groupName) {
      return new ReflectionParser.IDHandler(groupName, false);
   }

   private static ReflectionParser.IDHandler type(@Language("RegExp") String groupName) {
      return new ReflectionParser.IDHandler(groupName, true).generic(true).array(true);
   }

   private ClassHandle[] parseTypes(String[] typeNames) {
      ClassHandle[] classes = new ClassHandle[typeNames.length];

      for (int i = 0; i < typeNames.length; i++) {
         String typeName = typeNames[i];
         typeName = typeName.trim().substring(0, typeName.lastIndexOf(32)).trim();
         classes[i] = this.parseType(typeName);
      }

      return classes;
   }

   private ClassHandle parseType(String typeName) {
      if (this.cachedImports == null && this.namespace != null) {
         this.cachedImports = this.namespace.getImports();
      }

      String firstTypeName = typeName;
      typeName = typeName.replace(" ", "");
      int arrayDimension = 0;
      if (typeName.endsWith("[]")) {
         String replaced = typeName.replace("[]", "");
         arrayDimension = (typeName.length() - replaced.length()) / 2;
         typeName = replaced;
      }

      if (typeName.endsWith(">")) {
         typeName = typeName.substring(0, typeName.indexOf(60));
      }

      Class<?> clazz = this.stringToClass(typeName);
      if (clazz == null) {
         return new UnknownClassHandle(this.getOrCreateNamespace(), firstTypeName + " -> " + typeName);
      }

      if (arrayDimension != 0) {
         clazz = XReflection.of(clazz).asArray(arrayDimension).unreflect();
      }

      return new StaticClassHandle(this.getOrCreateNamespace(), clazz);
   }

   @Nullable
   private Class<?> stringToClass(String typeName) {
      Class<?> clazz = null;
      if (!typeName.contains(".")) {
         if (this.cachedImports != null) {
            clazz = this.cachedImports.get(typeName);
         }

         if (clazz == null) {
            clazz = PREDEFINED_TYPES.get(typeName);
         }

         if (clazz == null && this.checkedPackages != null) {
            for (String checkedPackage : this.checkedPackages) {
               boolean inner = checkedPackage.endsWith("$");
               clazz = classNamed(checkedPackage + (inner ? "" : '.') + typeName);
               if (clazz != null) {
                  break;
               }
            }
         }
      }

      if (clazz == null) {
         clazz = classNamed(typeName);
      }

      return clazz;
   }

   private static Class<?> classNamed(String name) {
      try {
         return Class.forName(name);
      } catch (ClassNotFoundException ignored) {
         return null;
      }
   }

   private ReflectiveNamespace getOrCreateNamespace() {
      return this.namespace == null ? XReflection.namespaced() : this.namespace;
   }

   public ReflectionParser imports(ReflectiveNamespace namespace) {
      this.namespace = namespace;
      return this;
   }

   private void pattern(Pattern pattern, ReflectiveHandle<?> handle) {
      this.pattern = pattern;
      this.matcher = pattern.matcher(this.declaration);
      this.start(handle);
   }

   public <T extends DynamicClassHandle> T parseClass(T classHandle) {
      this.pattern(CLASS, classHandle);
      String packageName = this.group("package");
      if (packageName != null && !packageName.isEmpty()) {
         boolean found = false;

         for (PackageHandle pkgHandle : PACKAGE_HANDLES) {
            String targetPackageName = pkgHandle.packageId().toLowerCase(Locale.ENGLISH);
            if (packageName.startsWith(targetPackageName)) {
               if (packageName.indexOf(46) == -1) {
                  classHandle.inPackage(pkgHandle);
               } else {
                  classHandle.inPackage(pkgHandle, packageName.substring(targetPackageName.length() + 1));
               }

               found = true;
               break;
            }
         }

         if (!found) {
            classHandle.inPackage(packageName);
         }
      }

      String className = this.group("className");
      if (className.contains("<")) {
         className = className.substring(0, className.indexOf(60));
      }

      classHandle.named(className);
      return classHandle;
   }

   private void includeInnerClassOf(MemberHandle handle) {
      Class<?> clazz = handle.getClassHandle().reflectOrNull();
      if (clazz != null) {
         int len = DEFAULT_CHECKED_PACKAGES.length + 2;
         this.checkedPackages = Arrays.copyOf(DEFAULT_CHECKED_PACKAGES, len);
         this.checkedPackages[len - 1] = clazz.getName() + '$';
         this.checkedPackages[len - 2] = clazz.getPackage().getName();
      }
   }

   public <T extends ConstructorMemberHandle> T parseConstructor(T ctorHandle) {
      this.includeInnerClassOf(ctorHandle);
      this.pattern(CONSTRUCTOR, ctorHandle);
      if (this.has("className") && !ctorHandle.getClassHandle().getPossibleNames().contains(this.group("className"))) {
         this.error("Wrong class name associated to constructor, possible names: " + ctorHandle.getClassHandle().getPossibleNames());
      }

      if (this.has("parameters")) {
         ctorHandle.parameters(this.parseTypes(this.group("parameters").split(",")));
      }

      return ctorHandle;
   }

   public <T extends MethodMemberHandle> T parseMethod(T methodHandle) {
      this.includeInnerClassOf(methodHandle);
      this.pattern(METHOD, methodHandle);
      methodHandle.named(this.group("methodName").split("\\$"));
      methodHandle.returns(this.parseType(this.group("methodReturnType")));
      if (this.has("parameters")) {
         methodHandle.parameters(this.parseTypes(this.group("parameters").split(",")));
      }

      return methodHandle;
   }

   public <T extends FieldMemberHandle> T parseField(T fieldHandle) {
      this.includeInnerClassOf(fieldHandle);
      this.pattern(FIELD, fieldHandle);
      fieldHandle.named(this.group("fieldName").split("\\$"));
      fieldHandle.returns(this.parseType(this.group("fieldType")));
      return fieldHandle;
   }

   private String group(String groupName) {
      return this.matcher.group(groupName);
   }

   private boolean has(String groupName) {
      String group = this.group(groupName);
      return group != null && !group.isEmpty();
   }

   private void start(ReflectiveHandle<?> handle) {
      if (!this.matcher.matches()) {
         this.error("Not a " + handle + " declaration");
      }

      this.parseFlags();
      if (handle instanceof MemberHandle) {
         MemberHandle memberHandle = (MemberHandle)handle;
         if (!hasOneOf(this.flags, ReflectionParser.Flag.PUBLIC, ReflectionParser.Flag.PROTECTED, ReflectionParser.Flag.PRIVATE)) {
            Class<?> clazz = memberHandle.getClassHandle().reflectOrNull();
            if (clazz != null && !clazz.isInterface()) {
               memberHandle.makeAccessible();
            }
         } else if (hasOneOf(this.flags, ReflectionParser.Flag.PRIVATE, ReflectionParser.Flag.PROTECTED)) {
            memberHandle.makeAccessible();
         }

         if (handle instanceof FieldMemberHandle && this.flags.contains(ReflectionParser.Flag.FINAL)) {
            ((FieldMemberHandle)handle).asFinal();
         }

         if (handle instanceof FlaggedNamedMemberHandle && this.flags.contains(ReflectionParser.Flag.STATIC)) {
            ((FlaggedNamedMemberHandle)handle).asStatic();
         }
      }
   }

   private void parseFlags() {
      if (this.has("flags")) {
         String flagsStr = this.group("flags");

         for (String flag : flagsStr.split("\\s+")) {
            if (!this.flags.add(ReflectionParser.Flag.valueOf(flag.toUpperCase(Locale.ENGLISH)))) {
               this.error("Repeated flag: " + flag);
            }
         }

         if (containsDuplicates(this.flags, ReflectionParser.Flag.PUBLIC, ReflectionParser.Flag.PROTECTED, ReflectionParser.Flag.PRIVATE)) {
            this.error("Duplicate visibility flags");
         }
      }
   }

   @SafeVarargs
   private static <T> boolean containsDuplicates(Collection<T> collection, T... values) {
      boolean contained = false;

      for (T value : values) {
         if (collection.contains(value)) {
            if (contained) {
               return true;
            }

            contained = true;
         }
      }

      return false;
   }

   @SafeVarargs
   private static <T> boolean hasOneOf(Collection<T> collection, T... elements) {
      return Arrays.<Object>stream(elements).anyMatch(collection::contains);
   }

   private void error(String message) {
      throw new ReflectionParser.ReflectionParserException(
         message + " in: " + this.declaration + " (RegEx: " + this.pattern.pattern() + "), (Imports: " + this.cachedImports + ')'
      );
   }

   static {
      Arrays.asList(
            byte.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class,
            boolean.class,
            char.class,
            void.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            Boolean.class,
            Character.class,
            Void.class,
            Object.class,
            String.class,
            CharSequence.class,
            StringBuilder.class,
            StringBuffer.class,
            UUID.class,
            Optional.class,
            Map.class,
            HashMap.class,
            Date.class,
            Calendar.class,
            Duration.class,
            TimeUnit.class,
            Path.class,
            Files.class,
            ConcurrentHashMap.class,
            Callable.class,
            Future.class,
            CompletableFuture.class,
            Throwable.class,
            Error.class,
            Exception.class,
            IllegalArgumentException.class,
            IllegalStateException.class
         )
         .forEach(x -> PREDEFINED_TYPES.put(x.getSimpleName(), (Class<?>)x));
   }

   private enum Flag {
      PUBLIC,
      PROTECTED,
      PRIVATE,
      FINAL,
      TRANSIENT,
      ABSTRACT,
      STATIC,
      NATIVE,
      SYNCHRONIZED,
      STRICTFP,
      VOLATILE;

      private static final String FLAGS_REGEX = "(?<flags>(?:(?:"
         + Arrays.stream(values()).map(Enum::name).map(x -> x.toLowerCase(Locale.ENGLISH)).collect(Collectors.joining("|"))
         + ")\\s*)+)?";

      Flag() {
      }
   }

   private static final class IDHandler {
      private boolean generic;
      private boolean array;
      private final String groupName;
      private final boolean isFullyQualified;

      private IDHandler(String groupName, boolean isFullyQualified) {
         this.groupName = groupName;
         this.isFullyQualified = isFullyQualified;
      }

      public ReflectionParser.IDHandler generic(boolean generic) {
         this.generic = generic;
         return this;
      }

      public ReflectionParser.IDHandler array(boolean array) {
         this.array = array;
         return this;
      }

      @Override
      public String toString() {
         String type = (
               this.isFullyQualified
                  ? "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*"
                  : "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*"
            )
            + (this.generic ? "(?:\\s*<\\s*[.\\w<>\\[\\], ]+\\s*>)?" : "")
            + (this.array ? "(?:(?:\\[])*)" : "");
         return this.groupName == null ? "(?:" + type + ')' : "(?<" + this.groupName + '>' + type + ')';
      }
   }

   public static final class ReflectionParserException extends RuntimeException {
      public ReflectionParserException(String message) {
         super(message);
      }
   }
}
