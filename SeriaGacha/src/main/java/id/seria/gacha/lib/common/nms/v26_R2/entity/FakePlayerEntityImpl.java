package id.seria.gacha.lib.common.nms.v26_R2.entity;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakePlayerEntity;
import id.seria.gacha.lib.common.nms.interfaces.packets.FakePacket;
import id.seria.gacha.lib.common.nms.v26_R2.packets.WrappedPlayerInfoAddPacket;
import id.seria.gacha.lib.common.nms.v26_R2.packets.WrappedPlayerInfoRemovePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.plugin.java.JavaPlugin;

public class FakePlayerEntityImpl extends FakeEntityImpl implements FakePlayerEntity {
   public FakePlayerEntityImpl(JavaPlugin plugin, ServerPlayer nmsEntity) {
      super(plugin, FakeEntityType.PLAYER, nmsEntity);
   }

   public ServerPlayer getNmsEntity() {
      return (ServerPlayer)this.nmsEntity;
   }

   @Override
   public FakePacket preparePlayerInfoAddPacket() {
      return new WrappedPlayerInfoAddPacket(this.getNmsEntity());
   }

   @Override
   public FakePacket preparePlayerInfoRemovePacket() {
      return new WrappedPlayerInfoRemovePacket(this.getNmsEntity().getUUID());
   }
}
