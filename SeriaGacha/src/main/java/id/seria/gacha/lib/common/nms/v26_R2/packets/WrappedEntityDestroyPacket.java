package id.seria.gacha.lib.common.nms.v26_R2.packets;

import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeEntityImpl;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;

public class WrappedEntityDestroyPacket extends WrappedPacket {
   private final Packet<?> packet;

   public WrappedEntityDestroyPacket(FakeEntityImpl entity) {
      this.packet = new ClientboundRemoveEntitiesPacket(new int[]{entity.getId()});
   }

   @Generated
   @Override
   public Packet<?> getPacket() {
      return this.packet;
   }
}
