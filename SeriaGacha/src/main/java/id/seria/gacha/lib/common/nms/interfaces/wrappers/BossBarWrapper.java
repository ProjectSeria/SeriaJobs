package id.seria.gacha.lib.common.nms.interfaces.wrappers;

import id.seria.gacha.lib.common.nms.interfaces.NMSFactory;
import id.seria.gacha.lib.common.nms.interfaces.wrappers.enums.BarColorWrapper;
import id.seria.gacha.lib.common.nms.interfaces.wrappers.enums.BarStyleWrapper;
import java.util.List;
import org.bukkit.entity.Player;

public interface BossBarWrapper {
   NMSFactory getNMSFactory();

   String getTitle();

   void setTitle(String var1);

   BarColorWrapper getColor();

   void setColor(BarColorWrapper var1);

   BarStyleWrapper getStyle();

   void setStyle(BarStyleWrapper var1);

   void setProgress(double var1);

   double getProgress();

   void addPlayer(Player var1);

   void removePlayer(Player var1);

   void removeAll();

   List<Player> getPlayers();

   void setVisible(boolean var1);

   boolean isVisible();
}
