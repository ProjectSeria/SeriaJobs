package id.seria.gacha.lib.common.services.services.watcher;

import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntity;
import id.seria.gacha.lib.common.utils.utility.BukkitUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WatcherEntry {
   private final FakeEntity entity;
   private final boolean autoUpdateAudience;

   public List<Player> refreshAudience(double distance) {
      Location location = this.entity.getLocation();
      List<Player> nearbyPlayers = new ArrayList<>();
      nearbyPlayers.addAll(BukkitUtil.getNearbyPlayers(location, distance));
      nearbyPlayers.sort((a, b) -> Double.compare(a.getLocation().distanceSquared(location), b.getLocation().distanceSquared(location)));
      this.entity.setAudience(nearbyPlayers);
      return nearbyPlayers;
   }

   @Generated
   public FakeEntity getEntity() {
      return this.entity;
   }

   @Generated
   public boolean isAutoUpdateAudience() {
      return this.autoUpdateAudience;
   }

   @Generated
   public WatcherEntry(FakeEntity entity, boolean autoUpdateAudience) {
      this.entity = entity;
      this.autoUpdateAudience = autoUpdateAudience;
   }
}
