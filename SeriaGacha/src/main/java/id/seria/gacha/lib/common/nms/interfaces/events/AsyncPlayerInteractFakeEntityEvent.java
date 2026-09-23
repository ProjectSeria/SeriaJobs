package id.seria.gacha.lib.common.nms.interfaces.events;

import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncPlayerInteractFakeEntityEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final int entityId;
   private final AsyncPlayerInteractFakeEntityEvent.FakeInteract interact;
   private final AsyncPlayerInteractFakeEntityEvent.FakeHand hand;

   public AsyncPlayerInteractFakeEntityEvent(
      Player player, int entityId, AsyncPlayerInteractFakeEntityEvent.FakeInteract interact, AsyncPlayerInteractFakeEntityEvent.FakeHand hand
   ) {
      super(true);
      this.player = player;
      this.entityId = entityId;
      this.interact = interact;
      this.hand = hand;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public void callEvent(ServerPlugin plugin) {
      if (plugin.isEnabled()) {
         if (Bukkit.isPrimaryThread()) {
            if (this.isAsynchronous()) {
               plugin.getScheduler().runAsync(() -> {
                  if (plugin.isEnabled()) {
                     Bukkit.getPluginManager().callEvent(this);
                  }
               });
            } else {
               Bukkit.getPluginManager().callEvent(this);
            }
         } else if (this.isAsynchronous()) {
            Bukkit.getPluginManager().callEvent(this);
         } else {
            plugin.getScheduler().runSync(() -> {
               if (plugin.isEnabled()) {
                  Bukkit.getPluginManager().callEvent(this);
               }
            });
         }
      }
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public int getEntityId() {
      return this.entityId;
   }

   @Generated
   public AsyncPlayerInteractFakeEntityEvent.FakeInteract getInteract() {
      return this.interact;
   }

   @Generated
   public AsyncPlayerInteractFakeEntityEvent.FakeHand getHand() {
      return this.hand;
   }

   public enum FakeHand {
      MAIN_HAND,
      OFF_HAND;

      FakeHand() {
      }
   }

   public enum FakeInteract {
      ATTACK,
      INTERACT,
      INTERACT_AT;

      FakeInteract() {
      }
   }
}
