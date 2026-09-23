package id.seria.gacha.lib.common.modelengine.interfaces.entity;

import java.util.UUID;

public interface MegBaseEntity {
   UUID getUniqueId();

   Object getOriginal();
}
