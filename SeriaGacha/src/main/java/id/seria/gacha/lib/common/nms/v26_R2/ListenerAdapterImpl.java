package id.seria.gacha.lib.common.nms.v26_R2;

import id.seria.gacha.lib.common.nms.interfaces.ListenerAdapter;
import id.seria.gacha.lib.common.nms.interfaces.events.BlockBreakEventCP;
import id.seria.gacha.lib.common.nms.interfaces.events.LegacyBlockExplodeEvent;
import id.seria.gacha.lib.common.nms.interfaces.events.LegacyEntityResurrectEvent;
import id.seria.gacha.lib.common.nms.interfaces.events.LegacyEntityToggleGlideEvent;
import id.seria.gacha.lib.common.nms.interfaces.events.LegacyPlayerInteractAtEntityEvent;
import id.seria.gacha.lib.common.nms.interfaces.events.LegacyPlayerInteractEvent;
import id.seria.gacha.lib.common.nms.interfaces.events.LegacyPlayerSwapHandItemsEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class ListenerAdapterImpl implements ListenerAdapter {
   public ListenerAdapterImpl() {
   }

   @EventHandler
   public void onBlockBreakEvent(BlockBreakEvent event) {
      BlockBreakEventCP cpEvent = new BlockBreakEventCP(event.getBlock(), event.getPlayer());
      cpEvent.setCancelled(event.isCancelled());
      cpEvent.setDropItems(event.isDropItems());
      Bukkit.getPluginManager().callEvent(cpEvent);
      event.setCancelled(cpEvent.isCancelled());
      event.setDropItems(cpEvent.isDropItems());
   }

   @EventHandler
   public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
      LegacyPlayerInteractAtEntityEvent legacyEvent = new LegacyPlayerInteractAtEntityEvent(
         event.getPlayer(), event.getRightClicked(), event.getClickedPosition(), event.getHand()
      );
      legacyEvent.setCancelled(event.isCancelled());
      Bukkit.getPluginManager().callEvent(legacyEvent);
      event.setCancelled(legacyEvent.isCancelled());
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      LegacyPlayerInteractEvent legacyEvent = new LegacyPlayerInteractEvent(
         event.getPlayer(),
         event.getItem(),
         event.getAction(),
         event.getClickedBlock(),
         event.getBlockFace(),
         event.useInteractedBlock(),
         event.useItemInHand(),
         event.getHand()
      );
      legacyEvent.setUseInteractedBlock(event.useInteractedBlock());
      legacyEvent.setUseItemInHand(event.useItemInHand());
      Bukkit.getPluginManager().callEvent(legacyEvent);
      event.setUseInteractedBlock(legacyEvent.getUseClickedBlock());
      event.setUseItemInHand(legacyEvent.getUseItemInHand());
   }

   @EventHandler
   public void onPlayerSwapHandItems(EntityResurrectEvent event) {
      LegacyEntityResurrectEvent legacyEvent = new LegacyEntityResurrectEvent(event.getEntity());
      legacyEvent.setCancelled(event.isCancelled());
      Bukkit.getPluginManager().callEvent(legacyEvent);
      event.setCancelled(legacyEvent.isCancelled());
   }

   @EventHandler
   public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
      LegacyPlayerSwapHandItemsEvent legacyEvent = new LegacyPlayerSwapHandItemsEvent(event.getPlayer(), event.getMainHandItem(), event.getOffHandItem());
      legacyEvent.setCancelled(event.isCancelled());
      Bukkit.getPluginManager().callEvent(legacyEvent);
      event.setCancelled(legacyEvent.isCancelled());
   }

   @EventHandler
   public void onEntityToggleGlide(EntityToggleGlideEvent event) {
      LegacyEntityToggleGlideEvent legacyEvent = new LegacyEntityToggleGlideEvent(event.getEntity(), event.isGliding());
      legacyEvent.setCancelled(event.isCancelled());
      Bukkit.getPluginManager().callEvent(legacyEvent);
      event.setCancelled(legacyEvent.isCancelled());
   }

   @EventHandler
   public void onBlockExplode(BlockExplodeEvent event) {
      LegacyBlockExplodeEvent legacyEvent = new LegacyBlockExplodeEvent(event.getBlock(), event.blockList(), event.getYield());
      legacyEvent.setCancelled(event.isCancelled());
      Bukkit.getPluginManager().callEvent(legacyEvent);
      event.setYield(legacyEvent.getYield());
      event.setCancelled(legacyEvent.isCancelled());
   }
}
