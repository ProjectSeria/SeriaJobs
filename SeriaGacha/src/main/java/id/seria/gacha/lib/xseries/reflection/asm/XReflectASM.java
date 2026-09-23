package id.seria.gacha.lib.xseries.reflection.asm;

import com.google.common.collect.Streams;
import id.seria.gacha.lib.xseries.reflection.XAccessFlag;
import id.seria.gacha.lib.xseries.reflection.jvm.FieldMemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.objects.ReflectedObject;
import id.seria.gacha.lib.xseries.reflection.proxy.ClassOverloadedMethods;
import id.seria.gacha.lib.xseries.reflection.proxy.OverloadedMethod;
import id.seria.gacha.lib.xseries.reflection.proxy.ReflectiveProxyObject;
import id.seria.gacha.lib.xseries.reflection.proxy.processors.MappedType;
import id.seria.gacha.lib.xseries.reflection.proxy.processors.ProxyMethodInfo;
import id.seria.gacha.lib.xseries.reflection.proxy.processors.ReflectiveAnnotationProcessor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

@Internal
public final class XReflectASM<T extends ReflectiveProxyObject> extends ClassVisitor {
   private static final int JAVA_VERSION = ASMVersion.USED_JAVA_FILE_FORMAT;
   private static final int ASM_VERSION = ASMVersion.USED_ASM_OPCODE_VERSION;
   private static final String CONSTRUCTOR_NAME = "<init>";
   private static final String STATIC_BLOCK = "<clinit>";
   private static final String INSTANCE_FIELD = "instance";
   private static final String METHOD_HANDLE_PREFIX = "H_";
   private static final String XSERIES_ANNOTATIONS = 'L' + "id.seria.gacha.lib.xseries.reflection.proxy.annotations".replace('.', '/');
   private static final String GENERATED_CLASS_PACKAGE_PREFIX = "generated";
   private static final String GENERATED_CLASS_SUFFIX = "_XSeriesGen_" + ASM_VERSION + '_' + JAVA_VERSION;
   private static final String MAGIC_ACCESSOR_IMPL;
   private static final String SUPER_CLASS;
   private static final ASMClassLoader CLASS_LOADER;
   private static final Map<Class<?>, XReflectASM<?>> PROCESSED;
   private final ClassWriter classWriter;
   private final ClassReader classReader;
   private final Class<T> templateClass;
   private final Class<?> targetClass;
   private final Type templateClassType;
   private final Type targetClassType;
   private final Type generatedClassType;
   private final String generatedClassName;
   private final String generatedClassPath;
   private Class<?> loaded;
   private byte[] bytecode;
   private final ClassOverloadedMethods<XReflectASM.ASMProxyInfo> mapped;

   private static String getGeneratedClassPath(Class<?> clazz) {
      return clazz.getPackage().getName() + '.' + "generated" + '.' + clazz.getSimpleName() + GENERATED_CLASS_SUFFIX;
   }

   private static String descriptorProcessor(ProxyMethodInfo info) {
      Type rType = Type.getType(info.rType.synthetic);
      Type[] pTypes = Arrays.stream(info.pTypes).map(x -> Type.getType(x.synthetic)).toArray(Type[]::new);
      return Type.getMethodDescriptor(rType, pTypes);
   }

   public static <T extends ReflectiveProxyObject> XReflectASM<T> proxify(Class<T> interfaceClass) {
      XReflectASM<?> cache = PROCESSED.get(interfaceClass);
      if (cache != null) {
         return (XReflectASM<T>)cache;
      }

      ReflectiveAnnotationProcessor processor = new ReflectiveAnnotationProcessor(interfaceClass);
      processor.process(XReflectASM::descriptorProcessor);
      XReflectASM<T> asm = new XReflectASM<>(interfaceClass, processor.getTargetClass(), processor.getMapped());
      PROCESSED.put(interfaceClass, asm);
      processor.loadDependencies(PROCESSED::containsKey);
      asm.generate();
      return asm;
   }

   @NotNull
   public T create() {
      Class<?> proxified = this.loadClass();

      try {
         Optional<Constructor<?>> ctor = Arrays.stream(proxified.getDeclaredConstructors())
            .filter(x -> XAccessFlag.PUBLIC.isSet(x.getModifiers()) && x.getParameterCount() == 1)
            .findFirst();
         if (!ctor.isPresent()) {
            throw new IllegalStateException("Cannot find appropriate constructor for " + Arrays.toString(proxified.getDeclaredConstructors()));
         } else {
            return (T)ctor.get().newInstance(null);
         }
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
         throw new IllegalStateException("Couldn't initialize proxified ASM class: " + this.templateClass + " -> " + proxified, e);
      }
   }

   public void verify(boolean silent) {
      this.generate();
      PrintWriter pw = new PrintWriter(silent ? System.err : System.out);
      ASMAnalyzer.verify(new ClassReader(this.bytecode), XReflectASM.class.getClassLoader(), !silent, pw);
   }

   public void writeToFile(Path folder) {
      this.generate();

      try {
         Files.write(
            folder.resolve(this.generatedClassName + ".class"),
            this.bytecode,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
         );
      } catch (IOException e) {
         throw new IllegalStateException("Cannot write generated file", e);
      }
   }

   public XReflectASM(Class<T> templateClass, Class<?> targetClass, ClassOverloadedMethods<ProxyMethodInfo> mapped) {
      super(ASM_VERSION);
      this.mapped = mapTypes(mapped);

      try {
         this.classReader = new ClassReader(templateClass.getName());
      } catch (IOException e) {
         throw new IllegalStateException("Unable to read class: " + templateClass, e);
      }

      this.cv = this.classWriter = new ClassWriter(this.classReader, 3);
      this.templateClass = templateClass;
      this.templateClassType = Type.getType(templateClass);
      this.targetClass = targetClass;
      this.targetClassType = Type.getType(targetClass);
      this.generatedClassName = templateClass.getSimpleName() + GENERATED_CLASS_SUFFIX;
      this.generatedClassPath = getGeneratedClassPath(templateClass);
      this.generatedClassType = Type.getType('L' + this.generatedClassPath.replace('.', '/') + ';');
   }

   public void generate() {
      if (this.bytecode == null) {
         this.classReader.accept(this, 0);
         this.bytecode = this.classWriter.toByteArray();
      }
   }

   public byte[] getBytecode() {
      return this.bytecode;
   }

   private static boolean shouldRemoveAnnotation(String descriptor) {
      return descriptor.startsWith(XSERIES_ANNOTATIONS);
   }

   private static ClassOverloadedMethods<XReflectASM.ASMProxyInfo> mapTypes(ClassOverloadedMethods<ProxyMethodInfo> mapped) {
      OverloadedMethod.Builder<XReflectASM.ASMProxyInfo> asmMapped = new OverloadedMethod.Builder<>(x -> descriptorProcessor(x.info));

      for (Entry<String, OverloadedMethod<ProxyMethodInfo>> overloads : mapped.mappings().entrySet()) {
         Collection<ProxyMethodInfo> overloaded = overloads.getValue().getOverloads();
         int overloadIndex = 0;

         for (ProxyMethodInfo overload : overloaded) {
            ReflectedObject jvm = overload.handle.jvm().unreflect();
            if (!jvm.accessFlags().contains(XAccessFlag.PUBLIC)) {
               String name;
               switch (jvm.type()) {
                  case CONSTRUCTOR:
                     name = "$init$" + (overloaded.size() == 1 ? "" : "_" + overloadIndex++);
                     break;
                  case FIELD:
                     FieldMemberHandle field = (FieldMemberHandle)overload.handle.unwrap();
                     name = jvm.name() + '_' + (field.isGetter() ? "getter" : "setter");
                     break;
                  case METHOD:
                     name = jvm.name() + (overloaded.size() == 1 ? "" : "_" + overloadIndex++);
                     break;
                  default:
                     throw new IllegalStateException("Unexpected JVM type: " + jvm);
               }

               asmMapped.add(new XReflectASM.ASMProxyInfo(overload, name), overloads.getKey());
            } else {
               asmMapped.add(new XReflectASM.ASMProxyInfo(overload, null), overloads.getKey());
            }
         }
      }

      return asmMapped.build();
   }

   public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
      return shouldRemoveAnnotation(descriptor) ? null : super.visitAnnotation(descriptor, visible);
   }

   public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
      return shouldRemoveAnnotation(descriptor) ? null : super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
   }

   public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
      this.classWriter
         .visit(JAVA_VERSION, 49, this.generatedClassType.getInternalName(), null, SUPER_CLASS, new String[]{this.templateClassType.getInternalName()});
   }

   public void visitSource(String source, String debug) {
      this.classWriter.visitSource(this.generatedClassName + ".java", null);
      boolean needsStaticInit = false;

      for (OverloadedMethod<XReflectASM.ASMProxyInfo> method : this.mapped.mappings().values()) {
         for (XReflectASM.ASMProxyInfo overload : method.getOverloads()) {
            if (overload.isInaccessible()) {
               needsStaticInit = true;
               this.writeMethodHandleField(overload.methodHandleName);
            }
         }
      }

      this.writePrivateFinalField(false, "instance", this.targetClass);
      if (needsStaticInit) {
         this.initStaticFields();
      }

      this.writeConstructor();
   }

   public void visitEnd() {
      this.generateGetTargetClass();
      this.generateIsInstance();
      this.generateNewArraySingleDim();
      this.generateNewArrayMultiDim();
      this.generateInstance();
      this.generateBindTo();
      this.generateEquals();
      this.generateHashCode();
      this.generateToString();
      super.visitEnd();
   }

   public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
      throw new UnsupportedOperationException("Raw fields are not supported");
   }

   public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
      if (!"<init>".equals(name) && !"<clinit>".equals(name)) {
         XReflectASM.ASMProxyInfo handle = this.mapped.get(name, () -> descriptor, true);
         return handle == null ? null : new XReflectASM.MethodRewriter(handle, access, name, descriptor, signature, exceptions);
      } else {
         return null;
      }
   }

   private static int magicMaxs(String descriptor, boolean staticMethod) {
      return Type.getArgumentsAndReturnSizes(descriptor) >> 2 + (staticMethod ? -1 : 0);
   }

   public static Type getType(String className) {
      return Type.getType('L' + className.replace('.', '/') + ';');
   }

   private static Type[] convert(MappedType[] pTypes) {
      return Arrays.stream(pTypes).map(x -> Type.getType(x.real)).toArray(Type[]::new);
   }

   private void getInstance(MethodVisitor mv) {
      mv.visitFieldInsn(180, this.generatedClassType.getInternalName(), "instance", this.targetClassType.getDescriptor());
   }

   private void writeConstructor() {
      GeneratorAdapter mv = this.createMethod(1, "<init>", Type.getMethodDescriptor(Type.getType(void.class), new Type[]{this.targetClassType}));
      Label label0 = new Label();
      mv.visitLabel(label0);
      mv.loadThis();
      mv.visitMethodInsn(183, SUPER_CLASS, "<init>", "()V", false);
      mv.loadThis();
      mv.loadArg(0);
      mv.putField(this.generatedClassType, "instance", this.targetClassType);
      mv.returnValue();
      Label label1 = new Label();
      mv.visitLabel(label1);
      this.visitThis(mv, label0, label1);
      mv.visitLocalVariable("instance", this.targetClassType.getDescriptor(), null, label0, label1, 1);
      mv.visitMaxs(2, 2);
      mv.visitEnd();
   }

   private void writeMethodHandleField(String name) {
      this.writePrivateFinalField(true, "H_" + name, MethodHandle.class);
   }

   private void writePrivateFinalField(boolean asStatic, String name, Class<?> type) {
      int access = 18;
      if (asStatic) {
         access |= 8;
      }

      FieldVisitor fv = this.classWriter.visitField(access, name, Type.getDescriptor(type), null, null);
      fv.visitEnd();
   }

   private void initStaticFields() {
      GeneratorAdapter mv = this.createMethod(8, "<clinit>", "()V");
      mv.visitCode();
      Label start = new Label();
      Label end = new Label();
      Label catchException = new Label();
      mv.visitTryCatchBlock(start, end, catchException, "java/lang/Throwable");
      mv.visitLabel(start);
      int targetClass = mv.newLocal(Type.getType(Class.class));
      mv.visitLdcInsn(this.targetClass.getName());
      mv.invokeStatic(Type.getType(Class.class), Method.getMethod("Class forName(String)"));
      mv.storeLocal(targetClass);
      Type ASMPrivateLookup = Type.getType(ASMPrivateLookup.class);
      int lookup = mv.newLocal(ASMPrivateLookup);
      mv.newInstance(ASMPrivateLookup);
      mv.dup();
      mv.loadLocal(targetClass);
      mv.invokeConstructor(ASMPrivateLookup, Method.getMethod("void <init>(Class)"));
      mv.storeLocal(lookup);

      label51:
      for (OverloadedMethod<XReflectASM.ASMProxyInfo> method : this.mapped.mappings().values()) {
         Iterator label6 = method.getOverloads().iterator();

         while (true) {
            XReflectASM.ASMProxyInfo overload;
            label31:
            while (true) {
               if (!label6.hasNext()) {
                  continue label51;
               }

               overload = (XReflectASM.ASMProxyInfo)label6.next();
               if (overload.isInaccessible()) {
                  ReflectedObject jvm = overload.info.handle.jvm().unreflect();
                  Label unitLabel = new Label();
                  mv.visitLabel(unitLabel);
                  switch (jvm.type()) {
                     case CONSTRUCTOR: {
                        mv.loadLocal(lookup);
                        ArrayInsnGenerator pTypes = new ArrayInsnGenerator(mv, Class.class, overload.info.pTypes.length);

                        for (MappedType pType : overload.info.pTypes) {
                           pTypes.add(() -> mv.push(Type.getType(pType.real)));
                        }

                        mv.invokeVirtual(ASMPrivateLookup, Method.getMethod("java.lang.invoke.MethodHandle findConstructor(Class[])"));
                        break label31;
                     }
                     case FIELD:
                        FieldMemberHandle field = (FieldMemberHandle)overload.info.handle.unwrap();
                        mv.loadLocal(lookup);
                        mv.push(jvm.name());
                        mv.push(Type.getType(overload.info.rType.real));
                        mv.push(field.isGetter());
                        mv.invokeVirtual(ASMPrivateLookup, Method.getMethod("java.lang.invoke.MethodHandle findField(String, Class, boolean)"));
                        break label31;
                     case METHOD: {
                        mv.loadLocal(lookup);
                        mv.push(jvm.name());
                        mv.push(Type.getType(overload.info.rType.real));
                        ArrayInsnGenerator pTypes = new ArrayInsnGenerator(mv, Class.class, overload.info.pTypes.length);

                        for (MappedType pType : overload.info.pTypes) {
                           pTypes.add(() -> mv.push(Type.getType(pType.real)));
                        }

                        mv.invokeVirtual(ASMPrivateLookup, Method.getMethod("java.lang.invoke.MethodHandle findMethod(String, Class, Class[])"));
                        break label31;
                     }
                     default:
                        throw new IllegalStateException("Unknown ReflectedObject type: " + jvm);
                  }
               }
            }

            mv.visitFieldInsn(179, this.generatedClassType.getInternalName(), "H_" + overload.methodHandleName, Type.getDescriptor(MethodHandle.class));
         }
      }

      mv.visitLabel(end);
      Label noExceptionThrown = new Label();
      mv.visitJumpInsn(167, noExceptionThrown);
      mv.visitLabel(catchException);
      int ex = mv.newLocal(Type.getType(Throwable.class));
      mv.storeLocal(ex);
      Label label6 = new Label();
      mv.visitLabel(label6);
      mv.visitTypeInsn(187, "java/lang/RuntimeException");
      mv.visitInsn(89);
      String StringBuilder = "java/lang/StringBuilder";
      mv.visitTypeInsn(187, StringBuilder);
      mv.visitInsn(89);
      mv.visitMethodInsn(183, StringBuilder, "<init>", "()V", false);
      mv.visitLdcInsn("Failed to get inaccessible members for ");
      mv.visitMethodInsn(182, StringBuilder, "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      mv.visitLdcInsn(this.generatedClassType);
      mv.visitMethodInsn(182, "java/lang/Class", "getName", "()Ljava/lang/String;", false);
      mv.visitMethodInsn(182, StringBuilder, "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
      mv.visitMethodInsn(182, StringBuilder, "toString", "()Ljava/lang/String;", false);
      mv.loadLocal(ex);
      mv.visitMethodInsn(183, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V", false);
      mv.visitInsn(191);
      mv.visitLabel(noExceptionThrown);
      mv.visitInsn(177);
      mv.visitLocalVariable("targetClass", Type.getDescriptor(Class.class), "Ljava/lang/Class<*>;", start, noExceptionThrown, targetClass);
      mv.visitLocalVariable("lookup", Type.getDescriptor(ASMPrivateLookup.class), null, start, noExceptionThrown, lookup);
      mv.visitLocalVariable("ex", Type.getDescriptor(Throwable.class), null, label6, noExceptionThrown, ex);
      mv.visitMaxs(-1, -1);
      mv.visitEnd();
   }

   private void generateInstance() {
      GeneratorAdapter mv = this.createMethod(1, "instance", Type.getMethodDescriptor(Type.getType(Object.class), new Type[0]));
      Label label0 = new Label();
      mv.visitLabel(label0);
      mv.visitLineNumber(33, label0);
      mv.loadThis();
      this.getInstance(mv);
      mv.returnValue();
      Label label1 = new Label();
      mv.visitLabel(label1);
      this.visitThis(mv, label0, label1);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
   }

   private void generateBindTo() {
      GeneratorAdapter mv = this.createMethod(1, "bindTo", Type.getMethodDescriptor(this.templateClassType, new Type[]{Type.getType(Object.class)}));
      Label label0 = mv.newLabel();
      mv.visitLabel(label0);
      Label label1 = new Label();
      mv.loadThis();
      this.getInstance(mv);
      mv.visitJumpInsn(198, label1);
      mv.throwException(Type.getType(UnsupportedOperationException.class), "bindTo() must be called from the factory object, not on an instance");
      mv.visitLabel(label1);
      mv.newInstance(this.generatedClassType);
      mv.dup();
      mv.loadArg(0);
      mv.checkCast(this.targetClassType);
      mv.invokeConstructor(this.generatedClassType, new Method("<init>", Type.VOID_TYPE, new Type[]{this.targetClassType}));
      mv.returnValue();
      Label label3 = new Label();
      mv.visitLabel(label3);
      this.visitThis(mv, label0, label3);
      mv.visitLocalVariable("instance", this.targetClassType.getDescriptor(), null, label0, label3, 1);
      mv.visitMaxs(3, 2);
      mv.visitEnd();
   }

   private void generateHashCode() {
      GeneratorAdapter mv = this.createMethod(1, "hashCode", "()I");
      Label label0 = mv.newLabel();
      mv.visitLabel(label0);
      mv.loadThis();
      this.getInstance(mv);
      Label label1 = mv.newLabel();
      mv.ifNonNull(label1);
      mv.loadThis();
      mv.invokeVirtual(this.generatedClassType, Method.getMethod("int hashCode()"));
      mv.returnValue();
      mv.visitLabel(label1);
      mv.loadThis();
      this.getInstance(mv);
      mv.invokeVirtual(this.targetClassType, Method.getMethod("int hashCode()"));
      mv.returnValue();
      mv.visitMaxs(1, 1);
      mv.visitEnd();
   }

   private void generateEquals() {
      GeneratorAdapter mv = this.createMethod(1, "equals", "(Ljava/lang/Object;)Z");
      Label label0 = new Label();
      mv.visitLabel(label0);
      mv.loadThis();
      mv.loadArg(0);
      Label label1 = new Label();
      mv.visitJumpInsn(166, label1);
      mv.visitInsn(4);
      mv.visitInsn(172);
      mv.visitLabel(label1);
      mv.loadThis();
      this.getInstance(mv);
      Label label2 = new Label();
      mv.visitJumpInsn(199, label2);
      mv.visitInsn(3);
      mv.visitInsn(172);
      mv.visitLabel(label2);
      mv.loadArg(0);
      Label label3 = new Label();
      mv.visitJumpInsn(199, label3);
      mv.visitInsn(3);
      mv.visitInsn(172);
      mv.visitLabel(label3);
      mv.loadArg(0);
      mv.instanceOf(this.templateClassType);
      Label label4 = new Label();
      mv.visitJumpInsn(153, label4);
      Label label5 = new Label();
      mv.visitLabel(label5);
      mv.loadThis();
      this.getInstance(mv);
      mv.loadArg(0);
      mv.checkCast(this.templateClassType);
      mv.invokeInterface(this.templateClassType, Method.getMethod("Object instance();"));
      mv.invokeVirtual(this.targetClassType, Method.getMethod("boolean equals(Object);"));
      mv.visitInsn(172);
      mv.visitLabel(label4);
      mv.loadArg(0);
      mv.instanceOf(this.targetClassType);
      Label label6 = new Label();
      mv.visitJumpInsn(153, label6);
      Label label7 = new Label();
      mv.visitLabel(label7);
      mv.loadThis();
      this.getInstance(mv);
      mv.loadArg(0);
      mv.invokeVirtual(this.targetClassType, Method.getMethod("boolean equals(Object);"));
      mv.visitInsn(172);
      mv.visitLabel(label6);
      mv.visitInsn(3);
      mv.visitInsn(172);
      Label label8 = new Label();
      mv.visitLabel(label8);
      this.visitThis(mv, label0, label8);
      mv.visitLocalVariable("obj", "Ljava/lang/Object;", null, label0, label8, 1);
      mv.visitMaxs(2, 2);
      mv.visitEnd();
   }

   private void generateGetTargetClass() {
      GeneratorAdapter mv = this.createMethod(1, "Class getTargetClass()");
      mv.push(this.targetClassType);
      mv.returnValue();
      mv.visitMaxs(1, 0);
      mv.visitEnd();
   }

   private void generateIsInstance() {
      GeneratorAdapter mv = this.createMethod(1, "boolean isInstance(Object)");
      mv.loadArg(0);
      mv.instanceOf(this.targetClassType);
      mv.returnValue();
      mv.visitMaxs(1, 1);
      mv.visitEnd();
   }

   private void generateNewArraySingleDim() {
      GeneratorAdapter mv = this.createMethod(1, "Object[] newArray(int)");
      Label startLabel = new Label();
      mv.visitLabel(startLabel);
      mv.loadArg(0);
      mv.newArray(this.targetClassType);
      mv.visitInsn(176);
      Label endLabel = new Label();
      mv.visitLabel(endLabel);
      this.visitThis(mv, startLabel, endLabel);
      mv.visitLocalVariable("length", "I", null, startLabel, endLabel, 1);
      mv.visitMaxs(1, 2);
      mv.visitEnd();
   }

   private void generateNewArrayMultiDim() {
      GeneratorAdapter mv = this.createMethod(129, "Object[] newArray(int[])");
      Label label0 = new Label();
      mv.visitLabel(label0);
      mv.loadArg(0);
      mv.arrayLength();
      Label case1 = new Label();
      Label case2 = new Label();
      Label case3 = new Label();
      Label defaultCase = new Label();
      mv.visitTableSwitchInsn(1, 3, defaultCase, new Label[]{case1, case2, case3});
      mv.visitLabel(case1);
      mv.loadArg(0);
      mv.visitInsn(3);
      mv.visitInsn(46);
      mv.newArray(this.targetClassType);
      mv.visitInsn(176);
      mv.visitLabel(case2);
      mv.loadArg(0);
      mv.visitInsn(3);
      mv.visitInsn(46);
      mv.loadArg(0);
      mv.visitInsn(4);
      mv.visitInsn(46);
      mv.visitMultiANewArrayInsn("[[" + this.targetClassType.getDescriptor(), 2);
      mv.visitInsn(176);
      mv.visitLabel(case3);
      mv.loadArg(0);
      mv.visitInsn(3);
      mv.visitInsn(46);
      mv.loadArg(0);
      mv.visitInsn(4);
      mv.visitInsn(46);
      mv.loadArg(0);
      mv.visitInsn(5);
      mv.visitInsn(46);
      mv.visitMultiANewArrayInsn("[[[" + this.targetClassType.getDescriptor(), 3);
      mv.visitInsn(176);
      mv.visitLabel(defaultCase);
      mv.push(this.targetClassType);
      mv.loadArg(0);
      mv.visitMethodInsn(184, "java/lang/reflect/Array", "newInstance", "(Ljava/lang/Class;[I)Ljava/lang/Object;", false);
      mv.checkCast(Type.getType(Object[].class));
      mv.visitInsn(176);
      Label endLabel = new Label();
      mv.visitLabel(endLabel);
      this.visitThis(mv, label0, endLabel);
      mv.visitLocalVariable("dimensions", "[I", null, label0, endLabel, 1);
      mv.visitMaxs(4, 2);
      mv.visitEnd();
   }

   private void generateToString() {
      Type StringBuilder = Type.getType(StringBuilder.class);
      GeneratorAdapter mv = this.createMethod(1, "String toString()");
      Label start = mv.newLabel();
      mv.visitLabel(start);
      mv.newInstance(StringBuilder);
      mv.dup();
      mv.invokeConstructor(StringBuilder, Method.getMethod("void <init>()"));
      mv.loadThis();
      mv.invokeVirtual(Type.getType(Object.class), Method.getMethod("Class getClass()"));
      mv.invokeVirtual(Type.getType(Class.class), Method.getMethod("String getSimpleName()"));
      mv.invokeVirtual(StringBuilder, Method.getMethod("StringBuilder append(String)"));
      mv.push("(instance=");
      mv.invokeVirtual(StringBuilder, Method.getMethod("StringBuilder append(String)"));
      mv.loadThis();
      this.getInstance(mv);
      mv.invokeVirtual(StringBuilder, Method.getMethod("StringBuilder append(Object)"));
      mv.push(41);
      mv.invokeVirtual(StringBuilder, Method.getMethod("StringBuilder append(char)"));
      mv.invokeVirtual(StringBuilder, Method.getMethod("String toString()"));
      mv.returnValue();
      Label end = new Label();
      mv.visitLabel(end);
      this.visitThis(mv, start, end);
      mv.visitMaxs(2, 1);
      mv.visitEnd();
   }

   private GeneratorAdapter createMethod(int access, String descriptor) {
      Method desc = Method.getMethod(descriptor);
      return this.createMethod(access, desc.getName(), desc.getDescriptor());
   }

   private GeneratorAdapter createMethod(
      int access, @Pattern("(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)|(<init>)|(<clinit>)") String name, String descriptor
   ) {
      GeneratorAdapter method = new GeneratorAdapter(this.classWriter.visitMethod(access, name, descriptor, null, null), access, name, descriptor);
      method.visitCode();
      return method;
   }

   private void visitThis(MethodVisitor mv, Label start, Label end) {
      mv.visitLocalVariable("this", this.generatedClassType.getDescriptor(), null, start, end, 0);
   }

   @NotNull
   public Class<?> loadClass() {
      if (this.loaded != null) {
         return this.loaded;
      }

      this.generate();
      this.verify(true);
      return this.loaded = CLASS_LOADER.defineClass(this.generatedClassPath, this.bytecode);
   }

   static {
      String magicAccessor;
      try {
         Class.forName("sun.reflect.MagicAccessorImpl");
         magicAccessor = "sun/reflect/MagicAccessorImpl";
      } catch (ClassNotFoundException e) {
         try {
            Class.forName("jdk.internal.reflect.MagicAccessorImpl");
            magicAccessor = "jdk/internal/reflect/MagicAccessorImpl";
         } catch (ClassNotFoundException ex) {
            magicAccessor = null;
         }
      }

      MAGIC_ACCESSOR_IMPL = magicAccessor;
      SUPER_CLASS = Type.getInternalName(Object.class);
      CLASS_LOADER = new ASMClassLoader();
      PROCESSED = new IdentityHashMap<>();
   }

   private static final class ASMProxyInfo {
      private final ProxyMethodInfo info;
      private final String methodHandleName;

      private ASMProxyInfo(ProxyMethodInfo info, String methodHandleName) {
         this.info = info;
         this.methodHandleName = methodHandleName;
      }

      private boolean isInaccessible() {
         return this.methodHandleName != null;
      }
   }

   private final class MethodRewriter extends MethodVisitor {
      private final XReflectASM.ASMProxyInfo handle;
      private final GeneratorAdapter adapter;
      private final String descriptor;

      MethodRewriter(XReflectASM.ASMProxyInfo handle, int access, String name, String descriptor, String signature, String[] exceptions) {
         super(XReflectASM.ASM_VERSION, XReflectASM.super.visitMethod(XAccessFlag.ABSTRACT.remove(access), name, descriptor, signature, exceptions));
         this.handle = handle;
         this.adapter = new GeneratorAdapter(this.mv, access, name, descriptor);
         this.descriptor = descriptor;
         this.generateCode();
      }

      private void generateCode() {
         this.adapter.visitCode();
         boolean isInterface = XReflectASM.this.targetClass.isInterface();

         ReflectedObject.Type type;
         boolean isStatic;
         String name;
         try {
            ReflectedObject obj = this.handle.info.handle.jvm().reflect();
            type = obj.type();
            name = obj.name();
            isStatic = obj.accessFlags().contains(XAccessFlag.STATIC);
         } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
         }

         Label label0 = new Label();
         this.adapter.visitLabel(label0);
         if (type == ReflectedObject.Type.CONSTRUCTOR) {
            this.adapter.loadThis();
            XReflectASM.this.getInstance(this.adapter);
            Label label1 = new Label();
            this.adapter.ifNull(label1);
            this.adapter
               .throwException(
                  Type.getType(UnsupportedOperationException.class), "Constructor method must be called from the factory object, not on an instance"
               );
            this.adapter.visitLabel(label1);
         }

         MappedType rType = this.handle.info.rType;
         Type realReturnType;
         boolean needsConversion;
         Type syntheticReturnType;
         if (rType.isDifferent()) {
            if (rType.synthetic.isAssignableFrom(rType.real)) {
               realReturnType = null;
               syntheticReturnType = null;
               needsConversion = false;
            } else {
               if (!ReflectiveProxyObject.class.isAssignableFrom(rType.synthetic)) {
                  throw new VerifyError(
                     "Cannot convert return type " + rType.synthetic + " to " + rType.real + " in proxy method " + this.handle.info.interfaceMethod
                  );
               }

               needsConversion = true;
               realReturnType = Type.getType(rType.real);
               syntheticReturnType = XReflectASM.getType(XReflectASM.getGeneratedClassPath(rType.synthetic));
               this.adapter.newInstance(syntheticReturnType);
               this.adapter.dup();
            }
         } else {
            realReturnType = null;
            syntheticReturnType = null;
            needsConversion = false;
         }

         if (this.handle.isInaccessible()) {
            this.adapter.getStatic(XReflectASM.this.generatedClassType, "H_" + this.handle.methodHandleName, Type.getType(MethodHandle.class));
         }

         if (type == ReflectedObject.Type.CONSTRUCTOR) {
            if (!this.handle.isInaccessible()) {
               this.adapter.newInstance(XReflectASM.this.targetClassType);
               this.adapter.dup();
            }
         } else if (!isStatic) {
            this.adapter.loadThis();
            XReflectASM.this.getInstance(this.adapter);
         }

         int operandIndex = 1;
         Type[] argumentTypes = this.adapter.getArgumentTypes();

         for (int i = 0; i < argumentTypes.length; i++) {
            Type argumentType = argumentTypes[i];
            MappedType pType = this.handle.info.pTypes[i];
            if (pType.isDifferent()) {
               if (!ReflectiveProxyObject.class.isAssignableFrom(pType.synthetic)) {
                  throw new VerifyError(
                     "Cannot convert parameter type " + pType.synthetic + " to " + pType.real + " in proxy method " + this.handle.info.interfaceMethod
                  );
               }

               this.adapter.visitVarInsn(argumentType.getOpcode(21), operandIndex);
               this.adapter.invokeInterface(Type.getType(ReflectiveProxyObject.class), Method.getMethod("Object instance()"));
               this.adapter.checkCast(Type.getType(pType.real));
            } else {
               this.adapter.visitVarInsn(argumentType.getOpcode(21), operandIndex);
            }

            operandIndex += argumentType.getSize();
         }

         String invokeExact = "invokeExact";
         switch (type) {
            case CONSTRUCTOR:
               if (this.handle.isInaccessible()) {
                  this.adapter
                     .invokeVirtual(
                        Type.getType(MethodHandle.class),
                        new Method(invokeExact, XReflectASM.this.targetClassType, XReflectASM.convert(this.handle.info.pTypes))
                     );
               } else {
                  this.adapter
                     .visitMethodInsn(
                        183,
                        XReflectASM.this.targetClassType.getInternalName(),
                        "<init>",
                        Type.getMethodDescriptor(Type.getType(void.class), argumentTypes),
                        false
                     );
               }
               break;
            case FIELD:
               boolean isSetter = argumentTypes.length != 0;
               Type fieldDescriptor;
               if (argumentTypes.length != 0) {
                  fieldDescriptor = this.adapter.getArgumentTypes()[0];
               } else {
                  fieldDescriptor = this.adapter.getReturnType();
               }

               if (this.handle.isInaccessible()) {
                  List<Type> parameters = new ArrayList<>(3);
                  if (!isStatic) {
                     parameters.add(XReflectASM.this.targetClassType);
                  }

                  if (isSetter) {
                     parameters.add(Type.getType(this.handle.info.pTypes[0].real));
                  }

                  this.adapter
                     .invokeVirtual(
                        Type.getType(MethodHandle.class), new Method(invokeExact, Type.getType(this.handle.info.rType.real), parameters.toArray(new Type[0]))
                     );
               } else {
                  int fieldCode;
                  if (isSetter) {
                     if (isStatic) {
                        fieldCode = 179;
                     } else {
                        fieldCode = 181;
                     }
                  } else if (isStatic) {
                     fieldCode = 178;
                  } else {
                     fieldCode = 180;
                  }

                  this.adapter.visitFieldInsn(fieldCode, XReflectASM.this.targetClassType.getInternalName(), name, fieldDescriptor.getDescriptor());
               }
               break;
            case METHOD:
               if (this.handle.isInaccessible()) {
                  this.adapter
                     .invokeVirtual(
                        Type.getType(MethodHandle.class),
                        new Method(
                           invokeExact,
                           Type.getType(this.handle.info.rType.real),
                           (Type[])Streams.concat(
                                 new Stream[]{
                                    isStatic ? Stream.of() : Stream.of(XReflectASM.this.targetClassType),
                                    Arrays.stream(this.handle.info.pTypes).map(x -> Type.getType(x.real))
                                 }
                              )
                              .toArray(Type[]::new)
                        )
                     );
               } else {
                  this.adapter
                     .visitMethodInsn(
                        isStatic ? 184 : (isInterface ? 185 : 182),
                        XReflectASM.this.targetClassType.getInternalName(),
                        name,
                        Type.getMethodDescriptor(Type.getType(rType.real), XReflectASM.convert(this.handle.info.pTypes)),
                        isInterface
                     );
               }
               break;
            default:
               throw new IllegalStateException("Unknown ReflectedObject type: " + type);
         }

         if (needsConversion) {
            this.adapter.invokeConstructor(syntheticReturnType, new Method("<init>", Type.VOID_TYPE, new Type[]{realReturnType}));
         }

         this.adapter.returnValue();
         Label label1 = new Label();
         this.adapter.visitLabel(label1);
         if (!isStatic && type != ReflectedObject.Type.CONSTRUCTOR) {
            XReflectASM.this.visitThis(this.adapter, label0, label1);
         }

         int magicMaxs = XReflectASM.magicMaxs(this.descriptor, isStatic);
         this.adapter.visitMaxs(magicMaxs, magicMaxs);
         this.adapter.visitEnd();
      }

      public void visitCode() {
      }

      public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
         return XReflectASM.shouldRemoveAnnotation(descriptor) ? null : super.visitParameterAnnotation(parameter, descriptor, visible);
      }

      public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
         return XReflectASM.shouldRemoveAnnotation(descriptor) ? null : super.visitAnnotation(descriptor, visible);
      }

      public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
         return XReflectASM.shouldRemoveAnnotation(descriptor) ? null : super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
      }
   }
}
