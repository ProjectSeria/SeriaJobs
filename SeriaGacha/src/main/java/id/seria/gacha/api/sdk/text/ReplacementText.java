package id.seria.gacha.api.sdk.text;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;

public class ReplacementText implements Text {
   private final Text source;
   private final Map<String, Text> replacements;

   public ReplacementText(Text source, String regex, Text replacement) {
      this.source = source;
      this.replacements = new LinkedHashMap<>();
      this.replacements.put(regex, replacement);
   }

   private ReplacementText(Text source, Map<String, Text> replacements) {
      this.source = source;
      this.replacements = new LinkedHashMap<>(replacements);
   }

   @Override
   public Text replaceText(String target, Text replacement) {
      Map<String, Text> newReplacements = new LinkedHashMap<>(this.replacements);
      newReplacements.put(target, replacement);
      return new ReplacementText(this.source, newReplacements);
   }

   @Override
   public String toPlainText() {
      String result = this.source.toPlainText();

      for (Entry<String, Text> r : this.replacements.entrySet()) {
         result = result.replaceAll(r.getKey(), r.getValue().toPlainText());
      }

      return result;
   }

   @Generated
   public Text getSource() {
      return this.source;
   }

   @Generated
   public Map<String, Text> getReplacements() {
      return this.replacements;
   }
}
