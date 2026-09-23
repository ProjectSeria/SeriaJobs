package id.seria.gacha.sdk.platforms.server.commands.objects;

import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.entity.Player;

public class Players extends ArrayList<Player> {
   public Players(Collection<? extends Player> players) {
      super(players);
   }
}
