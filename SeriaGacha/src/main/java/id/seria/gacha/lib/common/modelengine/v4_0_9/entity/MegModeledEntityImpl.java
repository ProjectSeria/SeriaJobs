package id.seria.gacha.lib.common.modelengine.v4_0_9.entity;

import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegBaseEntity;
import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegLookController;
import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegModeledEntity;
import id.seria.gacha.lib.common.modelengine.interfaces.model.MegActiveModel;
import id.seria.gacha.lib.common.modelengine.v4_0_9.model.MegActiveModelImpl;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.utils.ticker.DualTicker;
import java.util.Objects;
import lombok.Generated;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

public class MegModeledEntityImpl implements MegModeledEntity {
   private final ModeledEntity object;

   public MegModeledEntityImpl(ModeledEntity object) {
      this.object = Objects.requireNonNull(object);
   }

   @Override
   public MegBaseEntity getBase() {
      return new MegBaseEntityImpl(this.object.getBase());
   }

   @Override
   public MegLookController getLookController() {
      return new MegLookControllerImpl(this.object.getBase().getLookController());
   }

   @Override
   public void setRenderRadius(int radius) {
      this.object.getBase().setRenderRadius(radius);
   }

   @Override
   public void forceSpawn(Player player) {
      BaseEntity<?> base = this.object.getBase();
      Validate.isTrue(base instanceof Dummy, "Method only available to Dummy entities");
      ((Dummy)base).setForceViewing(player, true);
   }

   @Override
   public void removePlayer(Player player) {
      BaseEntity<?> base = this.object.getBase();
      Validate.isTrue(base instanceof Dummy, "Method only available to Dummy entities");
      ((Dummy)base).setForceHidden(player, true);
   }

   @Override
   public void addModel(MegActiveModel model, boolean overrideHitbox) {
      this.object.addModel(((MegActiveModelImpl)model).getObject(), overrideHitbox);
   }

   @Override
   public void destroy() {
      DualTicker.queueDelayedSyncTask(this.object::destroy, 1);
   }

   @Generated
   public ModeledEntity getObject() {
      return this.object;
   }
}
