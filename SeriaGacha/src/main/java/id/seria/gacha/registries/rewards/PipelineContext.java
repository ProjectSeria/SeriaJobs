package id.seria.gacha.registries.rewards;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class PipelineContext<T> {
   private final T target;
   private final Map<String, Object> metadata;

   private PipelineContext(T target) {
      this.target = target;
      this.metadata = new LinkedHashMap<>();
   }

   public static <T> PipelineContext<T> of(T target) {
      return new PipelineContext<>(target);
   }

   public T getTarget() {
      return this.target;
   }

   public PipelineContext<T> put(String key, Object value) {
      this.metadata.put(Objects.requireNonNull(key, "key"), value);
      return this;
   }

   public boolean has(String key) {
      return this.metadata.containsKey(key);
   }

   public Object get(String key) {
      String checkedKey = Objects.requireNonNull(key, "key");
      if (!this.metadata.containsKey(checkedKey)) {
         throw new IllegalStateException("Pipeline metadata key '" + checkedKey + "' is not populated");
      } else {
         return this.metadata.get(checkedKey);
      }
   }

   public <V> V get(String key, Class<V> type) {
      String checkedKey = Objects.requireNonNull(key, "key");
      Class<V> checkedType = Objects.requireNonNull(type, "type");
      if (!this.metadata.containsKey(checkedKey)) {
         throw new IllegalStateException("Pipeline metadata key '" + checkedKey + "' is not populated");
      } else {
         Object value = this.metadata.get(checkedKey);
         if (value == null) {
            throw new IllegalStateException("Pipeline metadata key '" + checkedKey + "' is null");
         } else if (!checkedType.isInstance(value)) {
            throw new IllegalStateException(
               "Pipeline metadata key '" + checkedKey + "' is not of type " + checkedType.getName() + " (was " + value.getClass().getName() + ")"
            );
         } else {
            return checkedType.cast(value);
         }
      }
   }

   public Map<String, Object> metadata() {
      return Collections.unmodifiableMap(this.metadata);
   }
}
