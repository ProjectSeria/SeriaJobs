package id.seria.gacha.api.sdk.text;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;

class CompositeText implements Text {
   private final List<Text> components;

   public CompositeText(Text first, Text second) {
      this.components = new ArrayList<>();
      this.components.add(first);
      this.components.add(second);
   }

   private CompositeText(List<? extends Text> components) {
      this.components = new ArrayList<>(components);
   }

   @Override
   public Text append(Text other) {
      List<Text> newComponents = new ArrayList<>(this.components);
      if (other instanceof CompositeText) {
         newComponents.addAll(((CompositeText)other).components);
      } else {
         newComponents.add(other);
      }

      return new CompositeText(newComponents);
   }

   @Override
   public String toPlainText() {
      StringBuilder builder = new StringBuilder();

      for (Text c : this.components) {
         builder.append(c.toPlainText());
      }

      return builder.toString();
   }

   @Generated
   public List<Text> getComponents() {
      return this.components;
   }
}
