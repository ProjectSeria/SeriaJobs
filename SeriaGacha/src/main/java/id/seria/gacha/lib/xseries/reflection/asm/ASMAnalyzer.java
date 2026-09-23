package id.seria.gacha.lib.xseries.reflection.asm;

import java.io.PrintWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.SimpleVerifier;
import org.objectweb.asm.util.CheckClassAdapter;

final class ASMAnalyzer {
   private static final MethodHandle CheckClassAdapter_printAnalyzerResult;

   private ASMAnalyzer() {
   }

   protected static Throwable findCause(Throwable exception, Predicate<Throwable> findCause) {
      Set<Throwable> circularCause = Collections.newSetFromMap(new IdentityHashMap<>(5));
      Throwable cause = exception;

      while (circularCause.add(cause)) {
         if (findCause.test(cause)) {
            return cause;
         }

         cause = cause.getCause();
         if (cause == null) {
            return null;
         }
      }

      return null;
   }

   protected static void verify(ClassReader classReader, ClassLoader loader, boolean printResults, PrintWriter printWriter) {
      ClassNode classNode = new ClassNode();
      classReader.accept(new CheckClassAdapter(ASMVersion.LATEST_ASM_OPCODE_VERSION, classNode, false) {}, 2);
      Type syperType = classNode.superName == null ? null : Type.getObjectType(classNode.superName);
      List<MethodNode> methods = classNode.methods;
      List<Type> interfaces = new ArrayList<>();

      for (String interfaceName : classNode.interfaces) {
         interfaces.add(Type.getObjectType(interfaceName));
      }

      for (MethodNode method : methods) {
         SimpleVerifier verifier = new SimpleVerifier(Type.getObjectType(classNode.name), syperType, interfaces, (classNode.access & 512) != 0);
         Analyzer<BasicValue> analyzer = new Analyzer(verifier);
         if (loader != null) {
            verifier.setClassLoader(loader);
         }

         boolean hasError;
         try {
            analyzer.analyze(classNode.name, method);
            hasError = false;
         } catch (AnalyzerException e) {
            ClassNotFoundException cls = (ClassNotFoundException)findCause(e, cause -> cause instanceof ClassNotFoundException);
            if (cls != null && cls.getMessage() != null && cls.getMessage().contains("XSeriesGen")) {
               hasError = false;
            } else {
               hasError = true;
               e.printStackTrace(printWriter);
            }
         }

         if (printResults || hasError) {
            try {
               CheckClassAdapter_printAnalyzerResult.invokeExact((MethodNode)method, (Analyzer)analyzer, (PrintWriter)printWriter);
            } catch (Throwable e) {
               throw new IllegalStateException("Cannot write bytecode instructions: ", e);
            }
         }
      }

      printWriter.flush();
   }

   static {
      Lookup lookup = MethodHandles.lookup();

      MethodHandle printAnalyzerResult;
      try {
         Method printer = CheckClassAdapter.class.getDeclaredMethod("printAnalyzerResult", MethodNode.class, Analyzer.class, PrintWriter.class);
         printer.setAccessible(true);
         printAnalyzerResult = lookup.unreflect(printer);
      } catch (NoSuchMethodException | IllegalAccessException e) {
         printAnalyzerResult = null;
      }

      CheckClassAdapter_printAnalyzerResult = printAnalyzerResult;
   }
}
