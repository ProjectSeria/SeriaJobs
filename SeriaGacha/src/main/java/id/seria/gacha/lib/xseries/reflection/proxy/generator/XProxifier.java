package id.seria.gacha.lib.xseries.reflection.proxy.generator;

import id.seria.gacha.lib.xseries.reflection.XAccessFlag;
import id.seria.gacha.lib.xseries.reflection.jvm.objects.ReflectedObject;
import id.seria.gacha.lib.xseries.reflection.proxy.ReflectiveProxyObject;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.Field;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.Final;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.Ignore;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.Private;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.Protected;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.Proxify;
import id.seria.gacha.lib.xseries.reflection.proxy.annotations.Static;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class XProxifier {
   private static final String MEMBER_SPACES = "    ";
   private final StringBuilder writer = new StringBuilder(1000);
   private final Set<String> imports = new HashSet<>(20);
   private final String proxifiedClassName;
   private final Class<?> clazz;
   private final boolean generateIntelliJAnnotations = true;
   private final boolean generateInaccessibleMembers = true;
   private final boolean copyAnnotations = true;
   private final boolean writeComments = true;
   private final boolean writeInfoAnnotationsAsComments = true;
   private boolean disableIDEFormatting;
   private Function<Class<?>, String> remapper;

   public XProxifier(Class<?> clazz) {
      this.clazz = clazz;
      this.proxifiedClassName = clazz.getSimpleName() + "Proxified";
      this.proxify();
   }

   private static Class<?> unwrapArrayType(Class<?> clazz) {
      while (clazz.isArray()) {
         clazz = clazz.getComponentType();
      }

      return clazz;
   }

   private void imports(Class<?> clazz) {
      clazz = unwrapArrayType(clazz);
      if (!clazz.isPrimitive() && !clazz.getPackage().getName().equals("java.lang")) {
         this.imports.add(clazz.getName().replace('$', '.'));
      }
   }

   private void writeComments(String... comments) {
      boolean multiLine = comments.length > 1;
      if (!multiLine) {
         this.writer.append("// ").append(comments[0]).append('\n');
      }

      this.writer.append("/**\n");

      for (String comment : comments) {
         this.writer.append(" * ");
         this.writer.append(comment);
         this.writer.append('\n');
      }

      this.writer.append(" */\n");
   }

   private void writeThrownExceptions(Class<?>[] exceptionTypes) {
      if (exceptionTypes != null && exceptionTypes.length != 0) {
         this.writer.append(" throws ");
         StringJoiner exs = new StringJoiner(", ");

         for (Class<?> ex : exceptionTypes) {
            this.imports(ex);
            exs.add(ex.getSimpleName());
         }

         this.writer.append(exs);
      }
   }

   private void writeMember(ReflectedObject jvm) {
      this.writeMember(jvm, false);
   }

   private void writeMember(ReflectedObject jvm, boolean generateGetterField) {
      this.writer.append(this.annotationsToString(true, true, jvm));
      Set<XAccessFlag> accessFlags = jvm.accessFlags();
      if (accessFlags.contains(XAccessFlag.PRIVATE)) {
         this.writeAnnotation(Private.class);
      }

      if (accessFlags.contains(XAccessFlag.PROTECTED)) {
         this.writeAnnotation(Protected.class);
      }

      if (accessFlags.contains(XAccessFlag.STATIC)) {
         this.writeAnnotation(Static.class);
      }

      if (accessFlags.contains(XAccessFlag.FINAL)) {
         this.writeAnnotation(Final.class);
      }

      switch (jvm.type()) {
         case CONSTRUCTOR:
            this.writeAnnotation(id.seria.gacha.lib.xseries.reflection.proxy.annotations.Constructor.class);
            this.writeAnnotation("NotNull");
            Constructor<?> ctor = (Constructor<?>)jvm.unreflect();
            String contractParams = Arrays.stream(ctor.getParameterTypes()).map(x -> "_").collect(Collectors.joining(", "));
            this.writeAnnotation("Contract", "value = \"" + contractParams + " -> new\"", "pure = true");
            break;
         case FIELD:
            this.writeAnnotation(Field.class);
            if (generateGetterField) {
               this.writeAnnotation("Contract", "pure = true");
            } else {
               this.writeAnnotation("Contract", "mutates = \"this\"");
            }
      }

      StringJoiner parameters = new StringJoiner(", ", "(", ")");
      Class<?>[] exceptionTypes = null;
      this.writer.append("    ");
      switch (jvm.type()) {
         case CONSTRUCTOR:
            Constructor<?> constructor = (Constructor<?>)jvm.unreflect();
            exceptionTypes = constructor.getExceptionTypes();
            this.writer.append(this.proxifiedClassName).append(' ').append("construct");
            this.writeParameters(parameters, constructor.getParameters());
            break;
         case FIELD:
            java.lang.reflect.Field field = (java.lang.reflect.Field)jvm.unreflect();
            this.imports(field.getType());
            if (generateGetterField) {
               this.writer.append(field.getType().getSimpleName());
            } else {
               this.writer.append("void");
               parameters.add(field.getType().getSimpleName() + " value");
            }

            this.writer.append(' ');
            this.writer.append(jvm.name());
            break;
         case METHOD:
            Method method = (Method)jvm.unreflect();
            exceptionTypes = method.getExceptionTypes();
            this.imports(method.getReturnType());
            this.writer.append(method.getReturnType().getSimpleName());
            this.writer.append(' ');
            this.writer.append(jvm.name());
            this.writeParameters(parameters, method.getParameters());
      }

      this.writer.append(parameters);
      this.writeThrownExceptions(exceptionTypes);
      this.writer.append(";\n\n");
   }

   private static Object[] getArray(Object val) {
      if (val instanceof Object[]) {
         return (Object[])val;
      }

      int arrlength = Array.getLength(val);
      Object[] outputArray = new Object[arrlength];

      for (int i = 0; i < arrlength; i++) {
         outputArray[i] = Array.get(val, i);
      }

      return outputArray;
   }

   private String constantToString(Object obj) {
      if (obj instanceof String) {
         return '"' + obj.toString() + '"';
      }

      if (obj instanceof Class) {
         Class<?> clazz = (Class<?>)obj;
         this.imports(clazz);
         return clazz.getSimpleName() + ".class";
      }

      if (obj instanceof Annotation) {
         Annotation annotation = (Annotation)obj;
         return this.annotationToString(annotation);
      }

      if (obj.getClass().isEnum()) {
         this.imports(obj.getClass());
         return obj.getClass().getSimpleName() + '.' + ((Enum)obj).name();
      }

      if (!obj.getClass().isArray()) {
         return obj.toString();
      }

      Object[] array = getArray(obj);
      if (array.length == 0) {
         return "{}";
      }

      StringJoiner builder;
      if (array.length == 1) {
         builder = new StringJoiner(", ");
      } else {
         builder = new StringJoiner(", ", "{", "}");
      }

      for (Object element : array) {
         builder.add(this.constantToString(element));
      }

      return builder.toString();
   }

   private String annotationsToString(boolean member, boolean newLine, AnnotatedElement annotatable) {
      StringJoiner builder = new StringJoiner((newLine ? '\n' : "") + (member ? "    " : ""), member ? "    " : "", newLine ? "\n" : "").setEmptyValue("");

      for (Annotation annotation : annotatable.getAnnotations()) {
         Annotation[] unwrapped = unwrapRepeatElement(annotation);
         if (unwrapped != null) {
            for (Annotation inner : unwrapped) {
               builder.add(this.annotationToString(inner));
            }
         } else {
            builder.add(this.annotationToString(annotation));
         }
      }

      return builder.toString();
   }

   private static Annotation[] unwrapRepeatElement(Annotation annotation) {
      try {
         Method method = annotation.annotationType().getDeclaredMethod("value");
         if (method.getReturnType().isArray()) {
            Class<?> rawReturn = unwrapArrayType(method.getReturnType());
            if (rawReturn.isAnnotation()) {
               Repeatable repeatable = rawReturn.getAnnotation(Repeatable.class);
               if (repeatable != null && repeatable.value() == annotation.annotationType()) {
                  try {
                     return (Annotation[])method.invoke(annotation);
                  } catch (IllegalAccessException | InvocationTargetException e) {
                     throw new IllegalArgumentException(e);
                  }
               }
            }
         }
      } catch (NoSuchMethodException var6) {
      }

      return null;
   }

   private String annotationToString(Annotation annotation) {
      List<String> builder = new ArrayList<>();
      boolean visitedValue = false;

      for (Method entry : annotation.annotationType().getDeclaredMethods()) {
         try {
            entry.setAccessible(true);
            String key = entry.getName();
            Object value = entry.invoke(annotation);

            try {
               Object defaultValue = entry.getDefaultValue();
               if (defaultValue != null
                  && (defaultValue.getClass().isArray() ? Arrays.equals(getArray(defaultValue), getArray(value)) : value.equals(defaultValue))) {
                  continue;
               }
            } catch (TypeNotPresentException var11) {
            }

            if (key.equals("value")) {
               visitedValue = true;
            }

            builder.add(key + " = " + this.constantToString(value));
         } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to get annotation value " + entry, e);
         }
      }

      this.imports(annotation.annotationType());
      String annotationValues;
      if (builder.isEmpty()) {
         annotationValues = "";
      } else if (builder.size() == 1 && visitedValue) {
         annotationValues = builder.get(0);
         int equalsSign = annotationValues.indexOf(61);
         annotationValues = '(' + annotationValues.substring(equalsSign + 2) + ')';
      } else {
         annotationValues = '(' + String.join(", ", builder) + ')';
      }

      return '@' + annotation.annotationType().getSimpleName() + annotationValues;
   }

   private StringJoiner writeParameters(StringJoiner joiner, Parameter[] parameters) {
      for (Parameter parameter : parameters) {
         this.imports(parameter.getType());
         String type;
         if (parameter.isVarArgs()) {
            type = parameter.getType().getSimpleName() + "... ";
         } else {
            type = parameter.getType().getSimpleName();
         }

         String annotations = this.annotationsToString(false, false, parameter);
         joiner.add(annotations + (annotations.isEmpty() ? "" : " ") + type + ' ' + parameter.getName());
      }

      return joiner;
   }

   private void writeAnnotation(Class<?> annotation, String... values) {
      this.writeAnnotation(true, annotation, values);
   }

   private void writeAnnotation(boolean member, Class<?> annotation, String... values) {
      this.imports(annotation);
      this.writeAnnotation(member, annotation.getSimpleName(), values);
   }

   private void writeAnnotation(String annotation, String... values) {
      this.writeAnnotation(true, annotation, values);
   }

   private void writeAnnotation(boolean member, String annotation, String... values) {
      if (member) {
         this.writer.append("    ");
      }

      this.writer.append('@').append(annotation);
      if (values.length != 0) {
         StringJoiner valueJoiner = new StringJoiner(", ", "(", ")");

         for (String value : values) {
            valueJoiner.add(value);
         }

         this.writer.append(valueJoiner);
      }

      this.writer.append('\n');
   }

   private void proxify() {
      if (this.disableIDEFormatting) {
         this.writer.append("// ").append("@formatter:").append("OFF").append('\n');
      }

      this.writeComments(
         "This is a generated proxified class for " + this.clazz.getSimpleName() + ". However, you might",
         "want to review each member and correct its annotations when needed.",
         "<p>",
         "It's also recommended to use your IDE's code formatter to adjust",
         "imports and spaces according to your settings.",
         "In IntelliJ, this can be done by with Ctrl+Alt+L",
         "<p>",
         "Full Target Class Path:",
         this.clazz.getName()
      );
      this.writer.append(this.annotationsToString(false, true, this.clazz));
      this.writeAnnotation(false, Proxify.class, "target = " + this.clazz.getSimpleName() + ".class");
      if (!XAccessFlag.PUBLIC.isSet(this.clazz.getModifiers())) {
         this.writeAnnotation(false, Private.class);
      }

      if (XAccessFlag.FINAL.isSet(this.clazz.getModifiers())) {
         this.writeAnnotation(false, Final.class);
         this.writeAnnotation(false, "ApiStatus.NonExtendable");
      }

      this.writer
         .append("public interface ")
         .append(this.proxifiedClassName)
         .append(" extends ")
         .append(ReflectiveProxyObject.class.getSimpleName())
         .append(" {\n");
      java.lang.reflect.Field[] declaredFields = this.clazz.getDeclaredFields();

      for (java.lang.reflect.Field field : declaredFields) {
         if (!field.isSynthetic()) {
            if (!XAccessFlag.FINAL.isSet(field.getModifiers())) {
               this.writeMember(ReflectedObject.of(field), false);
            }

            this.writeMember(ReflectedObject.of(field), true);
         }
      }

      if (declaredFields.length != 0) {
         this.writer.append('\n');
      }

      Constructor<?>[] declaredConstructors = this.clazz.getDeclaredConstructors();

      for (Constructor<?> constructor : declaredConstructors) {
         if (!constructor.isSynthetic()) {
            this.writeMember(ReflectedObject.of(constructor));
         }
      }

      if (declaredConstructors.length != 0) {
         this.writer.append('\n');
      }

      for (Method method : this.clazz.getDeclaredMethods()) {
         if (method.getDeclaringClass() != Object.class && !method.isSynthetic() && !method.isBridge()) {
            this.writeMember(ReflectedObject.of(method));
         }
      }

      this.writer.append('\n');
      this.writeAnnotation(Ignore.class);
      this.writeAnnotation("NotNull");
      this.writeAnnotation("ApiStatus.OverrideOnly");
      this.writeAnnotation("Contract", "value = \"_ -> new\"", "pure = true");
      this.writer.append("    ").append(this.proxifiedClassName).append(" bindTo(@NotNull Object instance);\n");
      this.writer.append("}\n");
      this.finalizeString();
   }

   private void finalizeString() {
      StringBuilder whole = new StringBuilder(this.writer.length() + this.imports.size() * 100);
      whole.append("import org.jetbrains.annotations.*;\n");
      List<String> sortedImports = new ArrayList<>(this.imports);
      sortedImports.sort(Comparator.naturalOrder());

      for (String anImport : sortedImports) {
         whole.append("import ").append(anImport).append(";\n");
      }

      whole.append('\n');
      this.writer.insert(0, whole);
      this.imports(ReflectiveProxyObject.class);
   }

   public String getString() {
      if (this.writer.length() == 0) {
         this.proxify();
      }

      return this.writer.toString();
   }

   public void writeTo(Path path) {
      if (Files.isDirectory(path)) {
         path = path.resolve(this.proxifiedClassName + ".java");
      }

      try {
         BufferedWriter writer = Files.newBufferedWriter(
            path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING
         );

         try {
            writer.write(this.getString());
         } catch (Throwable var6) {
            if (writer != null) {
               try {
                  writer.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (writer != null) {
            writer.close();
         }
      } catch (IOException e) {
         throw new IllegalStateException(e);
      }
   }
}
