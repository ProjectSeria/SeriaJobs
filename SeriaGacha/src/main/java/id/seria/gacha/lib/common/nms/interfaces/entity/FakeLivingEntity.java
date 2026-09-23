package id.seria.gacha.lib.common.nms.interfaces.entity;

import id.seria.gacha.lib.common.nms.interfaces.entity.attributes.Attribute;
import id.seria.gacha.lib.common.nms.interfaces.entity.attributes.AttributeType;
import id.seria.gacha.lib.common.nms.interfaces.packets.FakePacket;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.CustomPathfinder;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.Pathfinder;
import id.seria.gacha.lib.common.nms.interfaces.pathfinding.WrappedPathfinder;
import java.util.Collection;
import java.util.Set;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.entity.LivingEntity;

public interface FakeLivingEntity extends FakeEntity {
   FakeLivingEntity setHeadYaw(float var1);

   default Attribute getAttribute(AttributeType type) {
      throw new NotImplementedException("Not implemented on this version");
   }

   default FakePacket prepareAttributesPacket() {
      return player -> {};
   }

   default FakeLivingEntity setAI(boolean ai) {
      throw new NotImplementedException("Not implemented on this version");
   }

   LivingEntity getBukkitEntity();

   default void addPathfinder(Pathfinder b, int priority, boolean target) {
      throw new NotImplementedException("Not implemented on this version");
   }

   default void removePathfinder(Pathfinder b, boolean target) {
      throw new NotImplementedException("Not implemented on this version");
   }

   default void clearPathfinders(boolean target) {
      throw new NotImplementedException("Not implemented on this version");
   }

   default void addCustomPathfinder(CustomPathfinder p, int priority, boolean target) {
      throw new NotImplementedException("Not implemented on this version");
   }

   default Set<WrappedPathfinder> getGoals(boolean target) {
      throw new NotImplementedException("Not implemented on this version");
   }

   default Collection<WrappedPathfinder> getRunningGoals(boolean target) {
      throw new NotImplementedException("Not implemented on this version");
   }

   default void setFlag(Pathfinder.PathfinderFlag flag, boolean target, boolean value) {
      throw new NotImplementedException("Not implemented on this version");
   }
}
