package id.seria.gacha.lib.xseries.reflection.asm;

import java.lang.reflect.Field;
import org.objectweb.asm.Opcodes;

final class ASMVersion {
   protected static final int LATEST_ASM_OPCODE_VERSION;
   protected static final int USED_ASM_OPCODE_VERSION;
   protected static final int CURRENT_JAVA_VERSION = getJavaVersion();
   protected static final int CURRENT_JAVA_FILE_FORMAT = javaVersionToClassFileFormat(CURRENT_JAVA_VERSION);
   protected static final int USED_JAVA_FILE_FORMAT;
   protected static final int LATEST_SUPPORTED_JAVA_CLASS_FILE_FORMAT_VERSION;

   private ASMVersion() {
   }

   protected static int getASMOpcodeVersion(int asmVersion) {
      return asmVersion << 16 | 0;
   }

   protected static int getJavaVersion() {
      String version = System.getProperty("java.version");
      if (version.startsWith("1.")) {
         version = version.substring(2, 3);
      } else {
         int dot = version.indexOf(46);
         if (dot != -1) {
            version = version.substring(0, dot);
         }
      }

      return Integer.parseInt(version);
   }

   protected static int javaVersionToClassFileFormat(int version) {
      return 0 | 44 + version;
   }

   static {
      int latestAsm = 0;
      int latestJava = 0;

      try {
         for (Field field : Opcodes.class.getDeclaredFields()) {
            String name = field.getName();
            if (!name.contains("EXPERIMENTAL")) {
               if (name.startsWith("ASM")) {
                  latestAsm = Math.max(latestAsm, field.getInt(null));
               }

               if (name.startsWith("V") && name.length() <= 4) {
                  latestJava = Math.max(latestJava, field.getInt(null));
               }
            }
         }
      } catch (IllegalAccessException e) {
         throw new IllegalStateException(e);
      }

      if (latestAsm == 0) {
         latestAsm = 393216;
      }

      if (latestJava == 0) {
         latestJava = 52;
      }

      LATEST_ASM_OPCODE_VERSION = latestAsm;
      LATEST_SUPPORTED_JAVA_CLASS_FILE_FORMAT_VERSION = latestJava;
      int usedAsmVersion = latestAsm;
      int usedJavaVersion = Math.min(CURRENT_JAVA_FILE_FORMAT, LATEST_SUPPORTED_JAVA_CLASS_FILE_FORMAT_VERSION);

      try {
         String asmVer = System.getProperty("xseries.xreflection.asm.version");
         String javaVersion = System.getProperty("xseries.xreflection.asm.javaVersion");
         if (asmVer != null) {
            usedAsmVersion = Integer.parseInt(asmVer);
            System.out.println("[XSeries/XReflection] Using custom ASM version: " + usedAsmVersion);
         }

         if (javaVersion != null) {
            usedJavaVersion = Integer.parseInt(javaVersion);
            System.out.println("[XSeries/XReflection] Using custom ASM Java target version: " + usedJavaVersion);
         }
      } catch (SecurityException var7) {
      }

      USED_ASM_OPCODE_VERSION = usedAsmVersion;
      USED_JAVA_FILE_FORMAT = usedJavaVersion;
   }
}
