package id.seria.gacha.lib.common.nms.interfaces.events;

import id.seria.gacha.lib.common.nms.interfaces.wrappers.enums.EquipmentSlotWrapper;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class LegacyPlayerInteractEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   protected ItemStack item;
   protected Action action;
   protected Block blockClicked;
   protected BlockFace blockFace;
   private Result useClickedBlock;
   private Result useItemInHand;
   private final EquipmentSlot hand;

   public LegacyPlayerInteractEvent(
      Player player, ItemStack item, Action action, Block blockClicked, BlockFace blockFace, Result useClickedBlock, Result useItemInHand, EquipmentSlot hand
   ) {
      this.player = player;
      this.item = item;
      this.action = action;
      this.blockClicked = blockClicked;
      this.blockFace = blockFace;
      this.useClickedBlock = useClickedBlock;
      this.useItemInHand = useItemInHand;
      this.hand = hand;
   }

   public Action getAction() {
      return this.action;
   }

   @Deprecated
   public boolean isCancelled() {
      return this.useInteractedBlock() == Result.DENY;
   }

   public void setCancelled(boolean cancel) {
      this.setUseInteractedBlock(cancel ? Result.DENY : (this.useInteractedBlock() == Result.DENY ? Result.DEFAULT : this.useInteractedBlock()));
      this.setUseItemInHand(cancel ? Result.DENY : (this.useItemInHand() == Result.DENY ? Result.DEFAULT : this.useItemInHand()));
   }

   public ItemStack getItem() {
      return this.item;
   }

   public Material getMaterial() {
      return !this.hasItem() ? Material.AIR : this.item.getType();
   }

   public boolean hasBlock() {
      return this.blockClicked != null;
   }

   public boolean hasItem() {
      return this.item != null;
   }

   public boolean isBlockInHand() {
      return !this.hasItem() ? false : this.item.getType().isBlock();
   }

   public Block getClickedBlock() {
      return this.blockClicked;
   }

   public BlockFace getBlockFace() {
      return this.blockFace;
   }

   public Result useInteractedBlock() {
      return this.useClickedBlock;
   }

   public void setUseInteractedBlock(Result useInteractedBlock) {
      this.useClickedBlock = useInteractedBlock;
   }

   public Result useItemInHand() {
      return this.useItemInHand;
   }

   public void setUseItemInHand(Result useItemInHand) {
      this.useItemInHand = useItemInHand;
   }

   public EquipmentSlot getHand() {
      return this.hand;
   }

   public EquipmentSlotWrapper getLegacyHand() {
      return EquipmentSlotWrapper.valueOf(this.hand.name());
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public Block getBlockClicked() {
      return this.blockClicked;
   }

   @Generated
   public Result getUseClickedBlock() {
      return this.useClickedBlock;
   }

   @Generated
   public Result getUseItemInHand() {
      return this.useItemInHand;
   }
}
