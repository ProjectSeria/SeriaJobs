package id.seria.gacha.lib.common.services.services.hologram;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface PacketHologram {
   Hologram getHologram();

   Player getPlayer();

   Location getLocation();

   void setLines(String... var1);

   void setLines(List<String> var1);

   void refresh();

   void spawn();

   void teleport(Location var1);

   void despawn();

   boolean canViewerSee();
}
