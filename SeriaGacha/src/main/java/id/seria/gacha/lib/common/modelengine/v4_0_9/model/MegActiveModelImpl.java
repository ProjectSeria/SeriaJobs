package id.seria.gacha.lib.common.modelengine.v4_0_9.model;

import id.seria.gacha.lib.common.modelengine.interfaces.model.MegActiveModel;
import id.seria.gacha.lib.common.modelengine.v4_0_9.animation.MegAnimationHandlerImpl;
import com.ticxo.modelengine.api.entity.data.BukkitEntityData;
import com.ticxo.modelengine.api.entity.data.DummyEntityData;
import com.ticxo.modelengine.api.entity.data.IEntityData;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.utils.ticker.DualTicker;
import java.util.Objects;
import lombok.Generated;
import org.bukkit.entity.Player;

public class MegActiveModelImpl implements MegActiveModel {
   private final ActiveModel object;

   public MegActiveModelImpl(ActiveModel object) {
      this.object = Objects.requireNonNull(object);
   }

   public MegModelBlueprintImpl getBlueprint() {
      return new MegModelBlueprintImpl(this.object.getBlueprint());
   }

   public MegAnimationHandlerImpl getAnimationHandler() {
      return new MegAnimationHandlerImpl(this.object.getAnimationHandler());
   }

   @Override
   public void setHitboxVisible(boolean visible) {
      this.object.setHitboxVisible(false);
   }

   @Override
   public void setScale(double scale) {
      this.object.setScale(scale);
   }

   @Override
   public void setGlow(boolean glowing, int rgb) {
      this.object.setGlowing(glowing);
      this.object.setGlowColor(rgb);
   }

   @Override
   public void showToPlayer(Player player) {
      IEntityData data = this.object.getModeledEntity().getBase().getData();
      if (data instanceof BukkitEntityData) {
         ((BukkitEntityData)data).getTracked().removeForcedHidden(player.getUniqueId());
      } else {
         if (!(data instanceof DummyEntityData)) {
            throw new IllegalStateException("Not implemented for " + data.getClass().getSimpleName());
         }

         ((DummyEntityData)data).getTracked().removeForcedHidden(player.getUniqueId());
      }
   }

   @Override
   public void hideFromPlayer(Player player) {
      IEntityData data = this.object.getModeledEntity().getBase().getData();
      if (data instanceof BukkitEntityData) {
         ((BukkitEntityData)data).getTracked().addForcedHidden(player.getUniqueId());
      } else {
         if (!(data instanceof DummyEntityData)) {
            throw new IllegalStateException("Not implemented for " + data.getClass().getSimpleName());
         }

         ((DummyEntityData)data).getTracked().addForcedHidden(player.getUniqueId());
      }
   }

   @Override
   public void destroy() {
      DualTicker.queueDelayedSyncTask(() -> this.object.destroy(), 1);
   }

   @Generated
   public ActiveModel getObject() {
      return this.object;
   }
}
