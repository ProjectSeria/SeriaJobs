package id.seria.gacha.lib.xseries.reflection.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

final class ArrayInsnGenerator {
   private final GeneratorAdapter mv;
   private final int length;
   private int index = 0;
   private final int storeInsn;

   public ArrayInsnGenerator(GeneratorAdapter mv, Class<?> type, int length) {
      if (type.getComponentType() != null) {
         throw new IllegalArgumentException("The raw array element type must be given, not the array type itself: " + type);
      }

      this.mv = mv;
      this.length = length;
      Type asmType = Type.getType(type);
      this.storeInsn = type == Object.class ? -1 : asmType.getOpcode(79);
      mv.push(length);
      mv.newArray(asmType);
   }

   private boolean isDynamicStoreInsn() {
      return this.storeInsn == -1;
   }

   public void add(Runnable instruction) {
      if (this.isDynamicStoreInsn()) {
         throw new IllegalStateException("Must provide the type of stored object since this is a dynamic type array");
      }

      this.add(instruction, this.storeInsn);
   }

   public void add(Type elementType, Runnable instruction) {
      this.add(instruction, elementType.getOpcode(79));
   }

   private void add(Runnable instruction, int storeInsn) {
      if (this.index >= this.length) {
         throw new IllegalStateException("Array is already full, at index " + this.index);
      }

      this.mv.visitInsn(89);
      this.mv.push(this.index++);
      instruction.run();
      this.mv.visitInsn(storeInsn);
   }
}
