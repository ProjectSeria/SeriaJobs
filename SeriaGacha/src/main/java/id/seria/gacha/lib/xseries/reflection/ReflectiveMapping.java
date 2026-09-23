package id.seria.gacha.lib.xseries.reflection;

import id.seria.gacha.lib.xseries.reflection.jvm.NamedReflectiveHandle;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public interface ReflectiveMapping {
   boolean shouldBeChecked();

   String category();

   String name();

   String process(NamedReflectiveHandle var1, String var2);
}
