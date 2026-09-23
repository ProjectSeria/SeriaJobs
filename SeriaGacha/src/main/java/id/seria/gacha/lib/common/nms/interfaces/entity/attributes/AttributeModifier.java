package id.seria.gacha.lib.common.nms.interfaces.entity.attributes;

import java.util.UUID;
import lombok.Generated;

public class AttributeModifier {
   private final UUID uniqueId;
   private final String name;
   private final double value;
   private final AttributeModifier.Operation operation;

   public AttributeModifier(UUID uniqueId, String name, double value, AttributeModifier.Operation operation) {
      this.uniqueId = uniqueId;
      this.name = name;
      this.value = value;
      this.operation = operation;
   }

   @Generated
   public UUID getUniqueId() {
      return this.uniqueId;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public double getValue() {
      return this.value;
   }

   @Generated
   public AttributeModifier.Operation getOperation() {
      return this.operation;
   }

   public enum Operation {
      ADD_NUMBER,
      ADD_SCALAR,
      MULTIPLY_SCALAR_1;

      Operation() {
      }
   }
}
