package id.seria.gacha.lib.common.nms.interfaces.entity.attributes;

import java.util.List;

public interface Attribute {
   double getBaseValue();

   void setBaseValue(double var1);

   List<AttributeModifier> getModifiers();

   void addModifier(AttributeModifier var1);

   void removeModifier(AttributeModifier var1);

   double getValue();

   boolean isValid();
}
