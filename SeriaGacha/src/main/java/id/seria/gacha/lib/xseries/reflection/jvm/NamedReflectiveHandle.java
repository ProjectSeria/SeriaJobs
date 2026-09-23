package id.seria.gacha.lib.xseries.reflection.jvm;

import java.util.Set;
import org.jetbrains.annotations.NotNull;

public interface NamedReflectiveHandle {
   @NotNull
   Set<String> getPossibleNames();
}
