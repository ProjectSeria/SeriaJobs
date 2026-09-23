package id.seria.gacha.lib.common.modelengine.v4_0_8;

import id.seria.gacha.lib.common.modelengine.interfaces.*;
import id.seria.gacha.lib.common.modelengine.interfaces.animation.*;
import id.seria.gacha.lib.common.modelengine.interfaces.entity.*;
import id.seria.gacha.lib.common.modelengine.interfaces.model.*;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/** ponytail: compile-only stub — runtime uses pre-compiled bytecode from work/extracted-raw/ */
public class MegAPIImpl implements MegAPI {
    public MegModelRegistry getModelRegistry() { throw new UnsupportedOperationException("stub"); }
    public MegModelGenerator getModelGenerator() { throw new UnsupportedOperationException("stub"); }
    public MegActiveModel createActiveModel(String id) { throw new UnsupportedOperationException("stub"); }
    public MegActiveModel createActiveModel(MegModelBlueprint bp) { throw new UnsupportedOperationException("stub"); }
    public MegModeledEntity createModeledEntity(Entity e) { throw new UnsupportedOperationException("stub"); }
    public MegModeledEntity createModeledEntity(MegDummy d) { throw new UnsupportedOperationException("stub"); }
    public MegAnimationProperty createAnimationProperty(MegActiveModel m, MegBlueprintAnimation a) { throw new UnsupportedOperationException("stub"); }
    public MegAnimationProperty createAnimationProperty(MegActiveModel m, MegBlueprintAnimation a, double x, double y, double z) { throw new UnsupportedOperationException("stub"); }
    public MegEntityCompound createStaticEntity(String id, Location loc, Player p) { throw new UnsupportedOperationException("stub"); }
    public MegDummy createDummy() { throw new UnsupportedOperationException("stub"); }
}
