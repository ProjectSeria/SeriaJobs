package id.seria.gacha.lib.common.nms.interfaces.packets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.bukkit.entity.Player;

public interface FakePacket {
   void sendPacket(Player var1);

   default void sendPacket(Player... players) {
      for (Player player : Arrays.asList(players)) {
         this.sendPacket(player);
      }
   }

   default void sendPacket(Collection<Player> players) {
      for (Player player : new ArrayList<>(players)) {
         this.sendPacket(player);
      }
   }
}
