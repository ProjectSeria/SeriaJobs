package id.seria.gacha.lib.common.modelengine.v4_0_9.entity;

import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegDummy;
import com.ticxo.modelengine.api.entity.Dummy;
import java.util.Objects;
import lombok.Generated;
import org.bukkit.Location;

public class MegDummyImpl implements MegDummy {
   private final Dummy<?> object;

   public MegDummyImpl(Dummy<?> object) {
      this.object = Objects.requireNonNull(object);
   }

   @Override
   public void setLocation(Location location) {
      this.object.setLocation(location);
   }

   @Override
   public void setYBodyRot(float rotation) {
      this.object.setYBodyRot(rotation);
   }

   @Override
   public void setYHeadRot(float rotation) {
      this.object.setYHeadRot(rotation);
   }

   @Override
   public void setXHeadRot(float rotation) {
      this.object.setXHeadRot(rotation);
   }

   @Generated
   public Dummy<?> getObject() {
      return this.object;
   }
}
