package id.seria.gacha.lib.common.nms.v26_R2.packets;

import id.seria.gacha.lib.common.nms.v26_R2.entity.FakeEntityImpl;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;

public class WrappedEntityMetadataPacket extends WrappedPacket {
   private final Packet<?> packet;

   public WrappedEntityMetadataPacket(FakeEntityImpl entity, boolean packAll) {
      this.packet = new ClientboundSetEntityDataPacket(
         entity.getId(), packAll ? entity.getDataWatcher().getNonDefaultValues() : entity.getDataWatcher().packDirty()
      );
   }

   @Generated
   @Override
   public Packet<?> getPacket() {
      return this.packet;
   }
}
