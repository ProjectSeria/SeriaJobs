package id.seria.gacha.api.key;

import id.seria.gacha.api.internal.Item;

public interface Key {
   boolean isEnabled();

   boolean isVirtual();

   boolean isGlowing();

   Item getPlainItem();

   String getDisplayName();
}
