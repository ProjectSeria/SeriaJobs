package id.seria.gacha.lib.common.nms.v26_R2.packets;

import java.util.Collections;
import java.util.UUID;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;

public class WrappedPlayerInfoRemovePacket extends WrappedPacket {
   private final Packet<?> packet;

   public WrappedPlayerInfoRemovePacket(UUID uuid) {
      this.packet = new ClientboundPlayerInfoRemovePacket(Collections.singletonList(uuid));
   }

   @Generated
   @Override
   public Packet<?> getPacket() {
      return this.packet;
   }
}
