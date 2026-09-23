package id.seria.gacha.registries.rewards;

@FunctionalInterface
public interface Pipeline<T> {
   double apply(PipelineContext<T> var1, double var2);
}
