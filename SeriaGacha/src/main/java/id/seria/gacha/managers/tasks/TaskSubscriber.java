package id.seria.gacha.managers.tasks;

import org.bukkit.Location;

public interface TaskSubscriber {
   Location getLocation();

   void tick();

   boolean isFinished();
}
