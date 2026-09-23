package id.seria.gacha.lib.xseries.reflection.constraint;

import id.seria.gacha.lib.xseries.reflection.ReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.XAccessFlag;

public class ReflectiveConstraintException extends RuntimeException {
   private final ReflectiveConstraint constraint;
   private final ReflectiveConstraint.Result result;

   private ReflectiveConstraintException(ReflectiveConstraint constraint, ReflectiveConstraint.Result result, String message) {
      super(message);
      this.constraint = constraint;
      this.result = result;
   }

   public ReflectiveConstraint getConstraint() {
      return this.constraint;
   }

   public ReflectiveConstraint.Result getResult() {
      return this.result;
   }

   public static ReflectiveConstraintException create(
      ReflectiveConstraint constraint, ReflectiveConstraint.Result result, ReflectiveHandle<?> handle, Object jvm
   ) {
      String message;
      switch (result) {
         case MATCHED:
            throw new IllegalArgumentException("Cannot create an exception if results are successful: " + constraint + " -> MATCHED");
         case INCOMPATIBLE:
            message = "The constraint " + constraint + " cannot be applied to " + handle;
            break;
         case NOT_MATCHED:
            message = "Found "
               + handle
               + " with JVM "
               + jvm
               + ", however it doesn't match the constraint: "
               + constraint
               + " - "
               + XAccessFlag.getModifiers(jvm).map(XAccessFlag::toString).orElse("[NO MODIFIER]");
            break;
         default:
            throw new AssertionError("Unknown reflective constraint result: " + result);
      }

      return new ReflectiveConstraintException(constraint, result, message);
   }
}
