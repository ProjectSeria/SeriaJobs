package id.seria.gacha.api.crate.cost;

import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public interface CostEngineType {
   int getOrdinal();

   String getName();
}
