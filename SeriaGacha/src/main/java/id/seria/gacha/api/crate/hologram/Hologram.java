package id.seria.gacha.api.crate.hologram;

import id.seria.gacha.api.crate.CrateInstance;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface Hologram {
   CrateInstance getCrate();

   void show(Player var1);

   void hide(Player var1);

   void moveTo(Location var1);

   void refresh();

   void despawn();
}
