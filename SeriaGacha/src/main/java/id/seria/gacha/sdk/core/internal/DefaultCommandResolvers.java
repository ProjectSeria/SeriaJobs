package id.seria.gacha.sdk.core.internal;

import id.seria.gacha.api.sdk.exceptions.ParameterParsingException;
import id.seria.gacha.api.sdk.plugin.commands.CommandResolvers;
import id.seria.gacha.api.sdk.plugin.commands.components.ObjectResolver;
import id.seria.gacha.api.sdk.plugin.commands.contexts.InvocationContext;
import id.seria.gacha.sdk.core.commands.CommandTranslationKeys;
import id.seria.gacha.sdk.core.commands.PluginCommandManager;
import id.seria.gacha.sdk.core.commands.contexts.ParameterInvocationContext;
import id.seria.gacha.sdk.utilities.Objects;
import id.seria.gacha.sdk.utilities.Parsers;
import id.seria.gacha.sdk.utilities.Primitives;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

public class DefaultCommandResolvers implements CommandResolvers {
   protected final PluginCommandManager manager;
   protected final Map<Class<?>, ObjectResolver<? extends InvocationContext, ?>> resolverMap = new HashMap<>();

   @Override
   public Map<Class<?>, ObjectResolver<? extends InvocationContext, ?>> getResolverMap() {
      return Collections.unmodifiableMap(this.resolverMap);
   }

   public DefaultCommandResolvers(PluginCommandManager manager) {
      this.manager = manager;
      this.registerResolver(String.class, (c, stack) -> stack.pop());
      this.registerResolver(Short.class, (c, stack) -> {
         String number = stack.pop();

         try {
            return Parsers.parseNumber(number).shortValue();
         } catch (NumberFormatException e) {
            throw new ParameterParsingException(CommandTranslationKeys.MUST_BE_A_NUMBER, "%num%", number);
         }
      });
      this.registerResolver(Integer.class, (c, stack) -> {
         String number = stack.pop();

         try {
            return Parsers.parseNumber(number).intValue();
         } catch (NumberFormatException e) {
            throw new ParameterParsingException(CommandTranslationKeys.MUST_BE_A_NUMBER, "%num%", number);
         }
      });
      this.registerResolver(Long.class, (c, stack) -> {
         String number = stack.pop();

         try {
            return Parsers.parseNumber(number).longValue();
         } catch (NumberFormatException e) {
            throw new ParameterParsingException(CommandTranslationKeys.MUST_BE_A_NUMBER, "%num%", number);
         }
      });
      this.registerResolver(Float.class, (c, stack) -> {
         String number = stack.pop();

         try {
            return Parsers.parseNumber(number).floatValue();
         } catch (NumberFormatException e) {
            throw new ParameterParsingException(CommandTranslationKeys.MUST_BE_A_NUMBER, "%num%", number);
         }
      });
      this.registerResolver(Double.class, (c, stack) -> {
         String number = stack.pop();

         try {
            return Parsers.parseNumber(number).doubleValue();
         } catch (NumberFormatException e) {
            throw new ParameterParsingException(CommandTranslationKeys.MUST_BE_A_NUMBER, "%num%", number);
         }
      });
      this.registerResolver(Number.class, (c, stack) -> {
         String number = stack.pop();

         try {
            return Parsers.parseNumber(number);
         } catch (NumberFormatException e) {
            throw new ParameterParsingException(CommandTranslationKeys.MUST_BE_A_NUMBER, "%num%", number);
         }
      });
      this.registerResolver(BigDecimal.class, (c, stack) -> {
         String numberStr = stack.pop();

         try {
            return new BigDecimal(numberStr);
         } catch (NumberFormatException e) {
            throw new ParameterParsingException(CommandTranslationKeys.MUST_BE_A_NUMBER, "%num%", numberStr);
         }
      });
      this.registerResolver(BigInteger.class, (c, stack) -> {
         String numberStr = stack.pop();

         try {
            BigDecimal number = new BigDecimal(numberStr);
            return number.toBigIntegerExact();
         } catch (NumberFormatException e) {
            throw new ParameterParsingException(CommandTranslationKeys.MUST_BE_A_NUMBER, "%num%", numberStr);
         }
      });
      this.registerResolver(Boolean.class, (c, stack) -> Objects.isTruthy(stack.pop()));
   }

   @NotNull
   private static Number parseNumber(String number, InvocationContext c, Number minValue, Number maxValue) throws ParameterParsingException {
      return Parsers.parseNumber(number);
   }

   @Override
   public <T> ObjectResolver<ParameterInvocationContext, T> getClassResolver(Class<T> type) {
      Class<?> wrappedType = Primitives.wrap(type);

      for (Entry<Class<?>, ObjectResolver<?, ?>> entry : this.resolverMap.entrySet()) {
         if (entry.getKey().equals(wrappedType)) {
            return (ObjectResolver<ParameterInvocationContext, T>)entry.getValue();
         }
      }

      return null;
   }

   public <T> void registerResolver(@NonNull Class<T> context, @NonNull ObjectResolver<ParameterInvocationContext, T> resolver) {
      if (context == null) {
         throw new NullPointerException("context is marked non-null but is null");
      }

      if (resolver == null) {
         throw new NullPointerException("resolver is marked non-null but is null");
      }

      this.resolverMap.put(Primitives.wrap(context), resolver);
   }

   @Generated
   public PluginCommandManager getManager() {
      return this.manager;
   }
}
