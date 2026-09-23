package id.seria.gacha.api.managers;

import id.seria.gacha.api.key.Key;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public interface KeysManager {
   @NotNull
   List<? extends Key> getRegisteredKeys();

   @Range(from = 0L, to = 2147483647L) int countRegisteredKeys();

   @NotNull
   Set<String> getKeysIdentifier();

   @Nullable
   Key getKeyByIdentifier(String var1);
}
