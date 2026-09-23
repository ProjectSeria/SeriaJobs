package id.seria.gacha.lib.common.modelengine.interfaces.entity;

import id.seria.gacha.lib.common.modelengine.interfaces.model.MegActiveModel;
import org.bukkit.entity.Player;

public interface MegModeledEntity {
   MegBaseEntity getBase();

   MegLookController getLookController();

   void setRenderRadius(int var1);

   void forceSpawn(Player var1);

   void removePlayer(Player var1);

   void addModel(MegActiveModel var1, boolean var2);

   void destroy();
}
