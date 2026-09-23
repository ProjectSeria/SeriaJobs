package id.seria.gacha.api.sdk.locales.translatable;

import id.seria.gacha.sdk.utilities.Colors;
import java.util.Arrays;
import java.util.function.Function;
import lombok.Generated;

public class TranslationLiteral extends Translatable {
   private final String[] literal;
   private Object[] arguments;
   private Function<String, String> postProcessor;

   protected TranslationLiteral(String[] literal, Object... arguments) {
      this.literal = literal;
      this.arguments = arguments;
      this.postProcessor = str -> str;
   }

   protected TranslationLiteral(String[] literal, Function<String, String> postParser, Object... arguments) {
      this.literal = literal;
      this.arguments = arguments;
      this.postProcessor = postParser;
   }

   @Override
   public boolean isEmpty() {
      return this.literal == null;
   }

   public TranslationLiteral postProcessor(Function<String, String> parser) {
      return new TranslationLiteral(this.literal, parser, this.arguments);
   }

   public TranslationLiteral arguments(Object... arguments) {
      return new TranslationLiteral(this.literal, this.postProcessor, arguments);
   }

   public TranslationLiteral colorless() {
      return new TranslationLiteral(this.literal, str -> Colors.stripColor(this.postProcessor.apply(str)), this.arguments);
   }

   public TranslationLiteral lowercase() {
      return new TranslationLiteral(this.literal, str -> this.postProcessor.apply(str).toLowerCase(), this.arguments);
   }

   @Override
   public String asString() {
      return String.join(", ", this.literal);
   }

   public TranslationLiteral clone() {
      return new TranslationLiteral(this.literal, this.postProcessor, Arrays.copyOf(this.arguments, this.arguments.length));
   }

   @Generated
   public String[] getLiteral() {
      return this.literal;
   }

   @Generated
   public Object[] getArguments() {
      return this.arguments;
   }

   @Generated
   public Function<String, String> getPostProcessor() {
      return this.postProcessor;
   }

   @Generated
   public void setArguments(Object[] arguments) {
      this.arguments = arguments;
   }

   @Generated
   public void setPostProcessor(Function<String, String> postProcessor) {
      this.postProcessor = postProcessor;
   }

   @Generated
   @Override
   public String toString() {
      return "TranslationLiteral(literal=" + Arrays.deepToString(this.getLiteral()) + ", arguments=" + Arrays.deepToString(this.getArguments()) + ")";
   }
}
