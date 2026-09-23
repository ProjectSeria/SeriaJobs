package id.seria.gacha.sdk.platforms.server.internal;

import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.lib.xseries.profiles.builder.XSkull;
import id.seria.gacha.lib.xseries.profiles.objects.Profileable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

public class Skulls {
   private static final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

   public Skulls() {
   }

   public static ItemStack getPlayerHead(OfflinePlayer player) {
      return cache.computeIfAbsent(player.getUniqueId().toString(), id -> {
         try {
            return XSkull.createItem().lenient().profile(Profileable.of(player)).apply();
         } catch (Exception ignored) {
            return XMaterial.PLAYER_HEAD.parseItem();
         }
      }).clone();
   }

   public static ItemStack getPlayerHead(String playerName) {
      return cache.computeIfAbsent(playerName, id -> {
         try {
            return XSkull.createItem().lenient().profile(Profileable.username(playerName)).apply();
         } catch (Exception ignored) {
            return XMaterial.PLAYER_HEAD.parseItem();
         }
      }).clone();
   }
}
