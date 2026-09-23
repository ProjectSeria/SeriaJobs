package id.seria.gacha.lib.common.nms.v26_R2.entity.attributes;

import id.seria.gacha.lib.common.nms.interfaces.entity.attributes.Attribute;
import id.seria.gacha.lib.common.nms.interfaces.entity.attributes.AttributeModifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier.Operation;

public class AttributeImpl implements Attribute {
   private static final List<AttributeModifier> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<>());
   private final AttributeInstance instance;

   public AttributeImpl(AttributeInstance instance) {
      this.instance = instance;
   }

   @Override
   public double getBaseValue() {
      return this.instance == null ? 0.0 : this.instance.getBaseValue();
   }

   @Override
   public void setBaseValue(double baseValue) {
      if (this.instance != null) {
         this.instance.setBaseValue(baseValue);
      }
   }

   @Override
   public List<AttributeModifier> getModifiers() {
      return this.instance == null
         ? EMPTY_LIST
         : this.instance
            .getModifiers()
            .stream()
            .map(a -> new AttributeModifier(a.getUniqueId(), a.getName(), a.getAmount(), AttributeModifier.Operation.valueOf(a.getOperation().name())))
            .collect(Collectors.toList());
   }

   @Override
   public void addModifier(AttributeModifier modifier) {
      if (this.instance != null) {
         this.instance
            .addModifier(
               new org.bukkit.attribute.AttributeModifier(
                  modifier.getUniqueId(), modifier.getName(), modifier.getValue(), Operation.valueOf(modifier.getOperation().name())
               )
            );
      }
   }

   @Override
   public void removeModifier(AttributeModifier modifier) {
      if (this.instance != null) {
         org.bukkit.attribute.AttributeModifier bukkitModifier = this.instance
            .getModifiers()
            .stream()
            .filter(t -> t.getUniqueId().equals(modifier.getUniqueId()))
            .findFirst()
            .orElse(null);
         if (bukkitModifier != null) {
            this.instance.removeModifier(bukkitModifier);
         }
      }
   }

   @Override
   public double getValue() {
      return this.instance == null ? 0.0 : this.instance.getValue();
   }

   @Override
   public boolean isValid() {
      return this.instance != null;
   }

   @Generated
   public AttributeInstance getInstance() {
      return this.instance;
   }
}
