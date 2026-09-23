package id.seria.gacha.api.sdk.text;

import id.seria.gacha.api.sdk.utilities.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.Generated;

public class TextCompound extends ArrayList<Text> implements Text {
   public static TextCompound of(Text... text) {
      return new TextCompound(Lists.newArrayList(text));
   }

   public TextCompound(Collection<? extends Text> c) {
      super(c);
   }

   public boolean add(String text) {
      return super.add(Text.plain(text));
   }

   public TextCompound replaceText(String regex, Text component) {
      return new TextCompound(this.stream().map(t -> t.replaceText(regex, component)).collect(Collectors.toList()));
   }

   public TextCompound replaceText(String regex, Object text) {
      return this.replaceText(regex, Text.plain(text.toString()));
   }

   @Override
   public String toPlainText() {
      StringBuilder builder = new StringBuilder();

      for (Text t : this) {
         builder.append(t.toPlainText());
      }

      return builder.toString();
   }

   @Generated
   public TextCompound() {
   }
}
