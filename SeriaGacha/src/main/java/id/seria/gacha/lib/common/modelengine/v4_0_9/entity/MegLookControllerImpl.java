package id.seria.gacha.lib.common.modelengine.v4_0_9.entity;

import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegLookController;
import com.ticxo.modelengine.api.nms.entity.wrapper.LookController;
import java.util.Objects;

public class MegLookControllerImpl implements MegLookController {
   private final LookController object;

   public MegLookControllerImpl(LookController object) {
      this.object = Objects.requireNonNull(object);
   }

   @Override
   public void lookAt(double x, double y, double z) {
      this.object.lookAt(x, y, z);
   }

   @Override
   public void setPitch(float pitch) {
      this.object.setPitch(pitch);
   }

   @Override
   public void setBodyYaw(float yaw) {
      this.object.setBodyYaw(yaw);
   }

   @Override
   public void setHeadYaw(float yaw) {
      this.object.setHeadYaw(yaw);
   }
}
