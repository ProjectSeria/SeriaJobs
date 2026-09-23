package id.seria.gacha.api.sdk.plugin.hooks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HookProvider {
   Hook createHook();

   String getName();

   @NotNull
   String getDescription();

   void setDescription(@NotNull String var1);

   @NotNull
   String getUrl();

   void setUrl(@NotNull String var1);

   boolean isEnabled();

   boolean isAvailable();

   boolean isCompatible();

   @Nullable
   HookRegistry getRegistry();

   void setRegistry(@NotNull HookRegistry var1);
}
