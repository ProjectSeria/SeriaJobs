package id.seria.gacha.lib.common.modelengine.interfaces.model;

import java.util.Set;

public interface MegModelRegistry {
   Set<String> getAllBlueprintId();

   MegModelBlueprint getBlueprint(String var1);
}
