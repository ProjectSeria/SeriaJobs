package id.seria.gacha.lib.common.nms.interfaces.pathfinding;

import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

public interface Conditional<T> {
   @NotNull
   Predicate<T> getCondition();

   void setCondition(@NotNull Predicate<T> var1);
}
