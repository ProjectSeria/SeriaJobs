package id.seria.gacha.lib.common.modelengine.v4_0_9.entity;

import id.seria.gacha.lib.common.modelengine.interfaces.entity.MegBaseEntity;
import com.ticxo.modelengine.api.entity.BaseEntity;
import java.util.Objects;
import java.util.UUID;
import lombok.Generated;

public class MegBaseEntityImpl implements MegBaseEntity {
   private final BaseEntity<?> object;

   public MegBaseEntityImpl(BaseEntity<?> object) {
      this.object = Objects.requireNonNull(object);
   }

   @Override
   public UUID getUniqueId() {
      return this.object.getUUID();
   }

   @Override
   public Object getOriginal() {
      return this.object.getOriginal();
   }

   @Generated
   public BaseEntity<?> getObject() {
      return this.object;
   }
}
