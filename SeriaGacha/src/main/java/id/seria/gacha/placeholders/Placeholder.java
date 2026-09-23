package id.seria.gacha.placeholders;

import id.seria.gacha.Crates;
import id.seria.gacha.api.reward.BaseReward;
import id.seria.gacha.api.reward.Reward;
import id.seria.gacha.api.sdk.text.Text;
import id.seria.gacha.facades.RewardFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.players.PlayerData;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public final class Placeholder {
   private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
   private static final Map<Class<?>, Map<String, Placeholder.Resolver<?>>> REGISTRY = new ConcurrentHashMap<>();

   private Placeholder() {
   }

   public static <T> void register(Class<T> type, String key, Placeholder.Resolver<T> resolver) {
      String checkedKey = normalizeKey(key);
      REGISTRY.computeIfAbsent(type, ignored -> new ConcurrentHashMap<>()).put(checkedKey, resolver);
   }

   public static void unregister(Class<?> type, String key) {
      Map<String, Placeholder.Resolver<?>> resolvers = REGISTRY.get(type);
      if (resolvers != null) {
         resolvers.remove(normalizeKey(key));
      }
   }

   public static <T> Placeholder.Context<T> context(T source) {
      Placeholder.Context<T> context = new Placeholder.Context<>(source);
      if (source instanceof BaseReward reward) {
         RItemStack rItem = RItemStack.of(reward.getDisplayItem() == null ? CustomReward.INVALID_REWARD : reward.getDisplayItem().build());
         context.with("reward_name", rItem.displayName().toPlainText());
         context.with("reward_lore", rItem.lore().stream().map(Text::toPlainText).collect(Collectors.toList()));
      }

      return context;
   }

   private static <T> String resolve(Placeholder.Resolver<?> resolver, T source, Placeholder.Context<T> context) {
      return stringify(((Placeholder.Resolver<T>)resolver).resolve(source, context));
   }

   private static Map<String, Placeholder.Resolver<?>> resolversForSource(Object source) {
      Map<String, Placeholder.Resolver<?>> output = new LinkedHashMap<>();
      if (source == null) {
         return output;
      }

      for (Entry<Class<?>, Map<String, Placeholder.Resolver<?>>> entry : REGISTRY.entrySet()) {
         if (entry.getKey().isInstance(source)) {
            output.putAll(entry.getValue());
         }
      }

      return output;
   }

   private static String stringify(Object value) {
      return value == null ? "" : String.valueOf(value);
   }

   private static String normalizeKey(String key) {
      return key == null ? "" : key.trim().toLowerCase();
   }

   private static String token(String key) {
      return "%" + normalizeKey(key) + "%";
   }

   static {
      register(BaseReward.class, "percentage", (reward, context) -> DECIMAL_FORMAT.format(Math.max(0.0, reward.getWeight())));
      register(
         BaseReward.class,
         "required_keys",
         (reward, context) -> !(reward instanceof Reward) ? "0" : String.valueOf(Math.max(0, ((Reward)reward).getRequiredKeys()))
      );
      register(BaseReward.class, "amount_limit", (reward, context) -> {
         if (!(reward instanceof CustomReward customReward)) {
            return "\u221e";
         } else {
            if (customReward.getWinLimits() < 0) {
               return "\u221e";
            }

            Player viewer = context.viewer();
            if (viewer == null) {
               return String.valueOf(customReward.getWinLimits());
            }

            PlayerData playerData = Crates.getPlayersManager().getCachedDataNow(viewer);
            int winAmount = playerData.getRewardWinAmount(customReward.getIdentifier());
            return String.valueOf(Math.max(0, customReward.getWinLimits() - winAmount));
         }
      });
      register(BaseReward.class, "global_amount_limit", (reward, context) -> {
         if (!(reward instanceof CustomReward customReward)) {
            return "\u221e";
         } else {
            if (customReward.getGlobalWinLimit() < 0) {
               return "\u221e";
            }

            String crateId = context.get("crate", String.class);
            if (crateId == null) {
               return String.valueOf(customReward.getGlobalWinLimit());
            }

            int globalWinAmount = Crates.getGlobalRewardStatsTracker().getWinAmount(crateId, customReward.getIdentifier());
            return String.valueOf(Math.max(0, customReward.getGlobalWinLimit() - globalWinAmount));
         }
      });
      register(BaseReward.class, "chance", (reward, context) -> {
         List<? extends BaseReward> rewards = context.get("rewards", List.class);
         if (rewards != null && !rewards.isEmpty()) {
            Player viewer = context.get("viewer", Player.class);
            return DECIMAL_FORMAT.format(Math.max(0.0, RewardFacade.calculateChance(reward, rewards, viewer)));
         } else {
            return "0";
         }
      });
   }

   public static final class Context<T> {
      private final T source;
      private final Map<String, Object> values = new LinkedHashMap<>();

      private Context(T source) {
         this.source = source;
      }

      public Placeholder.Context<T> with(String key, Object value) {
         this.values.put(Placeholder.normalizeKey(key), value);
         return this;
      }

      public String parse(String template) {
         return template == null ? null : Translations.r(template, this.replacementValues());
      }

      public List<String> parse(List<String> templates) {
         return templates == null ? Collections.emptyList() : Translations.r(templates, this.replacementValues());
      }

      public Map<String, String> resolvedTokens() {
         Map<String, String> resolved = new LinkedHashMap<>();
         Object[] pairs = this.replacementValues();

         for (int i = 0; i < pairs.length; i += 2) {
            Object value = pairs[i + 1];
            if (!(value instanceof Collection)) {
               resolved.put((String)pairs[i], String.valueOf(value));
            }
         }

         return resolved;
      }

      private Object[] replacementValues() {
         List<Object> replacements = new ArrayList<>();

         for (Entry<String, Object> valueEntry : this.values.entrySet()) {
            replacements.add(Placeholder.token(valueEntry.getKey()));
            Object val = valueEntry.getValue();
            replacements.add(val instanceof Collection ? val : Placeholder.stringify(val));
         }

         for (Entry<String, Placeholder.Resolver<?>> entry : Placeholder.resolversForSource(this.source).entrySet()) {
            if (!this.values.containsKey(entry.getKey())) {
               replacements.add(Placeholder.token(entry.getKey()));
               replacements.add(Placeholder.resolve(entry.getValue(), this.source, this));
            }
         }

         return replacements.toArray(new Object[0]);
      }

      public Player viewer() {
         return this.get("viewer", Player.class);
      }

      public <V> V get(String key, Class<V> type) {
         Object value = this.values.get(Placeholder.normalizeKey(key));
         return (V)(value != null && type.isInstance(value) ? value : null);
      }
   }

   @FunctionalInterface
   public interface Resolver<T> {
      Object resolve(T var1, Placeholder.Context<T> var2);
   }
}
