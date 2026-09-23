package id.seria.gacha.lib.common.nms.v26_R2.packets;

import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeEntityImpl;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;

public class WrappedEntitySetPassengersPacket extends WrappedPacket {
   private final Packet<?> packet;

   public WrappedEntitySetPassengersPacket(FakeEntityImpl entity) {
      this.packet = new ClientboundSetPassengersPacket(entity.getNmsEntity());
   }

   @Generated
   @Override
   public Packet<?> getPacket() {
      return this.packet;
   }
}
