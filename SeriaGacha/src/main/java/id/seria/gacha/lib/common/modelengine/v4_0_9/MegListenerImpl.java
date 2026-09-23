package id.seria.gacha.lib.common.modelengine.v4_0_9;

import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegBaseEntity;
import id.seria.gacha.lib.common.modelengine.interfaces.event.MegBaseEntityInteractEvent;
import id.seria.gacha.lib.common.modelengine.interfaces.event.MegListener;
import id.seria.gacha.lib.common.modelengine.interfaces.event.MegModelRegistrationEvent;
import id.seria.gacha.lib.common.modelengine.interfaces.model.MegActiveModel;
import id.seria.gacha.lib.common.modelengine.v4_0_9.entity.MegBaseEntityImpl;
import id.seria.gacha.lib.common.modelengine.v4_0_9.model.MegActiveModelImpl;
import com.ticxo.modelengine.api.events.BaseEntityInteractEvent;
import com.ticxo.modelengine.api.events.ModelRegistrationEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

public class MegListenerImpl extends MegListener {
   public MegListenerImpl() {
   }

   @EventHandler
   public void onInteractBaseEntity(BaseEntityInteractEvent event) {
      MegBaseEntity baseEntity = new MegBaseEntityImpl(event.getBaseEntity());
      MegActiveModel activeModel = new MegActiveModelImpl(event.getModel());
      MegBaseEntityInteractEvent custom = new MegBaseEntityInteractEvent(
         event.getPlayer(),
         baseEntity,
         activeModel,
         MegBaseEntityInteractEvent.Action.valueOf(event.getAction().name()),
         event.getSlot(),
         event.isSecondary(),
         event.getItem(),
         event.getClickedPosition()
      );
      Bukkit.getPluginManager().callEvent(custom);
   }

   @EventHandler
   public void onModelRegistrationEvent(ModelRegistrationEvent event) {
      MegModelRegistrationEvent.Phase phase = null;
      switch (event.getPhase()) {
         case PRE_ZIPPING:
            phase = MegModelRegistrationEvent.Phase.PRE;
            break;
         case POST_ZIPPING:
            phase = MegModelRegistrationEvent.Phase.POST;
            break;
         case FINISHED:
            phase = MegModelRegistrationEvent.Phase.FINAL;
            break;
         default:
            return;
      }

      MegModelRegistrationEvent custom = new MegModelRegistrationEvent(phase);
      Bukkit.getPluginManager().callEvent(custom);
   }
}
