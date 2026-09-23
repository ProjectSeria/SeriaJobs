package id.seria.gacha.lib.common.modelengine.interfaces;

import id.seria.gacha.lib.common.modelengine.interfaces.animation.MegAnimationProperty;
import id.seria.gacha.lib.common.modelengine.interfaces.animation.MegBlueprintAnimation;
import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegDummy;
import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegEntityCompound;
import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegModeledEntity;
import id.seria.gacha.lib.common.modelengine.interfaces.model.MegActiveModel;
import id.seria.gacha.lib.common.modelengine.interfaces.model.MegModelBlueprint;
import id.seria.gacha.lib.common.modelengine.interfaces.model.MegModelGenerator;
import id.seria.gacha.lib.common.modelengine.interfaces.model.MegModelRegistry;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface MegAPI {
   MegModelRegistry getModelRegistry();

   MegModelGenerator getModelGenerator();

   MegActiveModel createActiveModel(String var1);

   MegActiveModel createActiveModel(MegModelBlueprint var1);

   MegModeledEntity createModeledEntity(Entity var1);

   MegModeledEntity createModeledEntity(MegDummy var1);

   MegAnimationProperty createAnimationProperty(MegActiveModel var1, MegBlueprintAnimation var2);

   MegAnimationProperty createAnimationProperty(MegActiveModel var1, MegBlueprintAnimation var2, double var3, double var5, double var7);

   MegEntityCompound createStaticEntity(@NonNull String var1, @NonNull Location var2, Player var3);

   MegDummy createDummy();
}
