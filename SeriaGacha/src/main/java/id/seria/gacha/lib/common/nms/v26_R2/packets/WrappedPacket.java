package id.seria.gacha.lib.common.nms.v26_R2.packets;

import id.seria.gacha.lib.common.nms.interfaces.packets.FakePacket;
import net.minecraft.network.protocol.Packet;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public abstract class WrappedPacket implements FakePacket {
   public WrappedPacket() {
   }

   public abstract Packet<?> getPacket();

   @Override
   public void sendPacket(Player player) {
      ((CraftPlayer)player).getHandle().connection.send(this.getPacket());
   }
}
