package id.seria.gacha.api.sdk.text;

import java.util.List;
import java.util.stream.Collectors;

public interface Text {
   static Text concat(String first, Text second) {
      return new CompositeText(plain(first), second);
   }

   static Text empty() {
      return new PlainText("");
   }

   static Text plain(Object text) {
      return new PlainText(text.toString());
   }

   static TextCompound plain(List<?> text) {
      return new TextCompound(text.stream().map(Object::toString).map(Text::plain).collect(Collectors.toList()));
   }

   static Text join(String delimiter, Text... components) {
      if (components != null && components.length != 0) {
         Text result = components[0];

         for (int i = 1; i < components.length; i++) {
            result = result.append(plain(delimiter)).append(components[i]);
         }

         return result;
      } else {
         return plain("");
      }
   }

   default Text append(Text component) {
      return new CompositeText(this, component);
   }

   default Text append(Text other, boolean withSpace) {
      return withSpace ? this.append(plain(" ")).append(other) : this.append(other);
   }

   default Text replaceText(String regex, Text component) {
      return new ReplacementText(this, regex, component);
   }

   default Text replaceText(String regex, Object text) {
      return new ReplacementText(this, regex, plain(text.toString()));
   }

   String toPlainText();
}
