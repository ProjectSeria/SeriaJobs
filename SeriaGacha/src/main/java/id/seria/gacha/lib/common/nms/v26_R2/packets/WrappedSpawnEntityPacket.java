package id.seria.gacha.lib.common.nms.v26_R2.packets;

import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeEntityImpl;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;

public class WrappedSpawnEntityPacket extends WrappedPacket {
   private final Packet<?> packet;

   public WrappedSpawnEntityPacket(FakeEntityImpl entity) {
      Entity nms = entity.getNmsEntity();
      this.packet = new ClientboundAddEntityPacket(
         nms.getId(),
         nms.getUUID(),
         nms.getX(),
         nms.getY(),
         nms.getZ(),
         nms.getXRot(),
         nms.getYRot(),
         nms.getType(),
         0,
         nms.getDeltaMovement(),
         nms.getYHeadRot()
      );
   }

   @Generated
   @Override
   public Packet<?> getPacket() {
      return this.packet;
   }
}
