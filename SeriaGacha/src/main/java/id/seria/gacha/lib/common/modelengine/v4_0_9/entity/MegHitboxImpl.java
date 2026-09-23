package id.seria.gacha.lib.common.modelengine.v4_0_9.entity;

import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegHitbox;
import com.ticxo.modelengine.api.entity.Hitbox;
import java.util.Objects;
import lombok.Generated;

public class MegHitboxImpl implements MegHitbox {
   private final Hitbox object;

   public MegHitboxImpl(Hitbox object) {
      this.object = Objects.requireNonNull(object);
   }

   @Override
   public double getEyeHeight() {
      return this.object.getEyeHeight();
   }

   @Override
   public double getWidth() {
      return this.object.getWidth();
   }

   @Override
   public double getHeight() {
      return this.object.getHeight();
   }

   @Generated
   public Hitbox getObject() {
      return this.object;
   }
}
