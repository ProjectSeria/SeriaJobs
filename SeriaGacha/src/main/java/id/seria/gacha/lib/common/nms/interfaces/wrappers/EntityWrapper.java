package id.seria.gacha.lib.common.nms.interfaces.wrappers;

import id.seria.gacha.lib.common.nms.interfaces.NMSFactory;
import org.bukkit.entity.Entity;

public interface EntityWrapper {
   NMSFactory getNMSFactory();

   Entity getBukkit();

   void setCollidable(boolean var1);
}
