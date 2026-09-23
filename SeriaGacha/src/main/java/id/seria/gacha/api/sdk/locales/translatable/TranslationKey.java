package id.seria.gacha.api.sdk.locales.translatable;

import id.seria.gacha.sdk.utilities.Colors;
import java.util.Arrays;
import java.util.function.Function;
import lombok.Generated;

public class TranslationKey extends Translatable {
   private final String key;
   private Object[] arguments;
   private Function<String, String> postProcessor;

   protected TranslationKey(String key, Object... arguments) {
      this.key = key;
      this.arguments = arguments;
      this.postProcessor = str -> str;
   }

   protected TranslationKey(String key, Function<String, String> postParser, Object... arguments) {
      this.key = key;
      this.arguments = arguments;
      this.postProcessor = postParser;
   }

   @Override
   public boolean isEmpty() {
      return this.key == null;
   }

   public TranslationKey postProcessor(Function<String, String> parser) {
      return new TranslationKey(this.key, parser, this.arguments);
   }

   public TranslationKey arguments(Object... arguments) {
      return new TranslationKey(this.key, this.postProcessor, arguments);
   }

   public TranslationKey colorless() {
      return new TranslationKey(this.key, str -> Colors.stripColor(this.postProcessor.apply(str)), this.arguments);
   }

   public TranslationKey lowercase() {
      return new TranslationKey(this.key, str -> this.postProcessor.apply(str).toLowerCase(), this.arguments);
   }

   @Override
   public String asString() {
      return this.key;
   }

   public TranslationKey clone() {
      return new TranslationKey(this.key, this.postProcessor, Arrays.copyOf(this.arguments, this.arguments.length));
   }

   @Generated
   public String getKey() {
      return this.key;
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
      return "TranslationKey(key=" + this.getKey() + ", arguments=" + Arrays.deepToString(this.getArguments()) + ")";
   }
}
