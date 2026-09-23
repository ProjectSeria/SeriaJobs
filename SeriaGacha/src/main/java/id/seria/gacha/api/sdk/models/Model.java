package id.seria.gacha.api.sdk.models;

import org.bukkit.entity.Player;

public interface Model {
   void showToPlayer(Player var1);

   void hideFromPlayer(Player var1);

   boolean playAnimation(String var1, boolean var2);

   boolean playAnimation(Animation var1, boolean var2);

   boolean playAnimation(String var1, double var2, double var4, double var6, boolean var8);

   boolean stopAnimation(String var1);

   Animation getAnimation(String var1);
}
