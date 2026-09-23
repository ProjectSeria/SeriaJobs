package id.seria.gacha.registries.rewards;

import id.seria.gacha.api.reward.BaseReward;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PipelineRegistry {
   private static final String MAIN_PIPELINE_ID = "main";
   private static final String DEFAULT_TARGET_ID = "default";
   private static final String REWARD_CHANCE_TARGET_ID = "reward:calculate-chance";
   private static final Map<Class<?>, Map<String, PipelineRegistry.PipelineBucket<?>>> BUCKETS = new LinkedHashMap<>();

   private PipelineRegistry() {
   }

   public static String getMainPipelineId() {
      return "main";
   }

   public static String getDefaultTargetId() {
      return "default";
   }

   public static String getRewardChanceTargetId() {
      return "reward:calculate-chance";
   }

   @Deprecated
   public static synchronized <T> void register(Class<T> targetClass, String id, Pipeline<T> pipeline) {
      register(targetClass, "default", id, pipeline);
   }

   public static synchronized <T> void register(Class<T> targetClass, String targetId, String id, Pipeline<T> pipeline) {
      Class<T> checkedTargetClass = Objects.requireNonNull(targetClass, "targetClass");
      String checkedTargetId = requireTargetId(targetId);
      String checkedId = Objects.requireNonNull(id, "id");
      Pipeline<T> checkedPipeline = Objects.requireNonNull(pipeline, "pipeline");
      PipelineRegistry.PipelineBucket<T> bucket = getBucket(checkedTargetClass, checkedTargetId);
      if (bucket.pipelines.containsKey(checkedId)) {
         bucket.order.remove(checkedId);
      }

      bucket.pipelines.put(checkedId, checkedPipeline);
      bucket.order.add(checkedId);
   }

   @Deprecated
   public static synchronized <T> void registerAfter(Class<T> targetClass, String id, String afterId, Pipeline<T> pipeline) {
      registerAfter(targetClass, "default", id, afterId, pipeline);
   }

   public static synchronized <T> void registerAfter(Class<T> targetClass, String targetId, String id, String afterId, Pipeline<T> pipeline) {
      Class<T> checkedTargetClass = Objects.requireNonNull(targetClass, "targetClass");
      String checkedTargetId = requireTargetId(targetId);
      String checkedId = Objects.requireNonNull(id, "id");
      String checkedAfterId = Objects.requireNonNull(afterId, "afterId");
      Pipeline<T> checkedPipeline = Objects.requireNonNull(pipeline, "pipeline");
      PipelineRegistry.PipelineBucket<T> bucket = getBucket(checkedTargetClass, checkedTargetId);
      if (!bucket.pipelines.containsKey(checkedAfterId)) {
         register(checkedTargetClass, checkedTargetId, checkedId, checkedPipeline);
      } else {
         bucket.pipelines.put(checkedId, checkedPipeline);
         bucket.order.remove(checkedId);
         int index = bucket.order.indexOf(checkedAfterId);
         if (index >= 0 && index + 1 < bucket.order.size()) {
            bucket.order.add(index + 1, checkedId);
         } else {
            bucket.order.add(checkedId);
         }
      }
   }

   @Deprecated
   public static synchronized void unregister(Class<?> targetClass, String id) {
      unregister(targetClass, "default", id);
   }

   public static synchronized void unregister(Class<?> targetClass, String targetId, String id) {
      Class<?> checkedTargetClass = Objects.requireNonNull(targetClass, "targetClass");
      String checkedTargetId = requireTargetId(targetId);
      if (id != null && !"main".equalsIgnoreCase(id)) {
         Map<String, PipelineRegistry.PipelineBucket<?>> targetBuckets = BUCKETS.get(checkedTargetClass);
         if (targetBuckets != null) {
            PipelineRegistry.PipelineBucket<?> bucket = targetBuckets.get(checkedTargetId);
            if (bucket != null) {
               bucket.pipelines.remove(id);
               bucket.order.remove(id);
               if (bucket.pipelines.isEmpty()) {
                  targetBuckets.remove(checkedTargetId);
               }

               if (targetBuckets.isEmpty()) {
                  BUCKETS.remove(checkedTargetClass);
               }
            }
         }
      }
   }

   @Deprecated
   public static synchronized <T> double calculate(Class<T> targetClass, PipelineContext<T> context) {
      return calculate(targetClass, "default", context);
   }

   public static synchronized <T> double calculate(Class<T> targetClass, String targetId, PipelineContext<T> context) {
      Class<T> checkedTargetClass = Objects.requireNonNull(targetClass, "targetClass");
      String checkedTargetId = requireTargetId(targetId);
      PipelineRegistry.PipelineBucket<T> bucket = getBucket(checkedTargetClass, checkedTargetId);
      double current = 0.0;

      for (String pipelineId : bucket.order) {
         Pipeline<T> pipeline = bucket.pipelines.get(pipelineId);
         if (pipeline != null) {
            current = pipeline.apply(context, current);
         }
      }

      return current;
   }

   private static synchronized <T> PipelineRegistry.PipelineBucket<T> getBucket(Class<T> targetClass, String targetId) {
      Map<String, PipelineRegistry.PipelineBucket<?>> targetBuckets = BUCKETS.computeIfAbsent(targetClass, k -> new LinkedHashMap<>());
      return (PipelineRegistry.PipelineBucket<T>)targetBuckets.computeIfAbsent(targetId, k -> new PipelineRegistry.PipelineBucket());
   }

   private static String requireTargetId(String targetId) {
      String checkedTargetId = Objects.requireNonNull(targetId, "targetId").trim();
      if (checkedTargetId.isEmpty()) {
         throw new IllegalArgumentException("targetId cannot be blank");
      } else {
         return checkedTargetId;
      }
   }

   static {
      register(BaseReward.class, "reward:calculate-chance", "main", (context, currentValue) -> {
         if (context != null && context.getTarget() != null) {
            BaseReward reward = context.getTarget();
            List<BaseReward> rewards = context.get("availableRewards", List.class);
            if (rewards.isEmpty()) {
               return 0.0;
            }

            double totalWeight = 0.0;

            for (BaseReward r : rewards) {
               totalWeight += Math.max(0.0, r.getWeight());
            }

            return totalWeight <= 0.0 ? 0.0 : Math.max(0.0, reward.getWeight()) / totalWeight * 100.0;
         } else {
            return 0.0;
         }
      });
   }

   private static final class PipelineBucket<T> {
      private final Map<String, Pipeline<T>> pipelines = new LinkedHashMap<>();
      private final List<String> order = new LinkedList<>();

      private PipelineBucket() {
      }
   }
}
