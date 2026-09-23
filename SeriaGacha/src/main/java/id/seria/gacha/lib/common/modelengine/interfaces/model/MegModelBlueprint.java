package id.seria.gacha.lib.common.modelengine.interfaces.model;

import id.seria.gacha.lib.common.modelengine.interfaces.animation.MegBlueprintAnimation;
import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegHitbox;
import java.util.Map;

public interface MegModelBlueprint {
   Map<String, MegBlueprintAnimation> getAnimations();

   MegHitbox getMainHitbox();
}
