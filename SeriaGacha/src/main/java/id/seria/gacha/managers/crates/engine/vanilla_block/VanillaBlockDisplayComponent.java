package id.seria.gacha.managers.crates.engine.vanilla_block;

import id.seria.gacha.Crates;
import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.engine.EngineDisplayComponent;
import id.seria.gacha.managers.crates.type.CrateBlockType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;

public class VanillaBlockDisplayComponent extends EngineDisplayComponent {
   private static final String LOOTBOX_METADATA_KEY = "lootbox";
   private final Player viewer;

   public VanillaBlockDisplayComponent(Crate crate, Player viewer) {
      super(crate);
      this.viewer = viewer;
   }

   @Override
   public void create(boolean updateBlock) {
      if (this.viewer == null || this.crate.getType().hasAnyWorldAnimationPhase()) {
         CrateBlockType blockType = this.crate.getType().getBlockType();
         if (this.viewer != null) {
            boolean isLootbox = Boolean.TRUE.equals(this.crate.getMetadataOrNull("lootbox", Boolean.class));
            BlockFace blockFace = this.crate.getFacingDirection().toBukkit(this.crate.getType().getBlockMaterial());
            if (blockType.isDataBase64()) {
               Utilities.getNMSFactory().sendTexturedBlockUpdatePacket(Arrays.asList(this.viewer), this.crate.getLocation(), blockType.getTexture(), blockFace);
            } else {
               byte blockData = blockType.isDataByte() ? (Byte)blockType.getData() : 0;
               Utilities.getNMSFactory()
                  .sendBlockUpdatePacket(Arrays.asList(this.viewer), this.crate.getLocation(), blockType.getBukkitMaterial(), blockData, blockFace);
            }

            if (isLootbox) {
               Utilities.getNMSFactory()
                  .sendBlockUpdatePacket(
                     Collections.singletonList(this.viewer), this.crate.getLocation().clone().add(0.0, 2.0, 0.0), Material.BARRIER, (byte)0, BlockFace.UP
                  );
            }
         } else {
            Block block = this.crate.getBlock();
            if (updateBlock) {
               block.setType(blockType.getBukkitMaterial(), false);
               Crates.getScheduler().runSync(block, () -> {
                  if (blockType.isDataBase64() && block.getState() instanceof Skull) {
                     Utilities.getNMSFactory().setBlockTexture(block, blockType.getTexture());
                  }

                  Utilities.getNMSFactory().setBlockFace(block, this.crate.getFacingDirection().toBukkit(blockType.getMaterial()));
               }, 1L);
            }
         }
      }
   }

   @Override
   public void destroy(boolean updateBlock) {
      if (this.viewer == null || this.crate.getType().hasAnyWorldAnimationPhase()) {
         Runnable task = () -> {
            if (this.viewer != null) {
               Block realBlock = this.crate.getBlock();
               Utilities.getNMSFactory()
                  .sendBlockUpdatePacket(
                     Arrays.asList(this.viewer), this.crate.getLocation(), realBlock.getType(), (byte)0, Utilities.getNMSFactory().getBlockFace(realBlock)
                  );
               if (Boolean.TRUE.equals(this.crate.getMetadataOrNull("lootbox", Boolean.class))) {
                  Utilities.getNMSFactory()
                     .sendBlockUpdatePacket(
                        Collections.singletonList(this.viewer), this.crate.getLocation().clone().add(0.0, 2.0, 0.0), Material.AIR, (byte)0, BlockFace.UP
                     );
               }
            } else {
               if (updateBlock) {
                  this.crate.getBlock().setType(Material.AIR, false);
               }
            }
         };
         if (Crates.getPlugin().isEnabled()) {
            Crates.getScheduler().runSync(this.crate.getBlock(), task);
         } else {
            task.run();
         }
      }
   }

   @Override
   public void update() {
      if (this.viewer == null || this.crate.getType().hasAnyWorldAnimationPhase()) {
         Block block = this.crate.getBlock();
         Utilities.getNMSFactory().setBlockFace(block, this.crate.getFacingDirection().toBukkit(this.crate.getType().getBlockType().getMaterial()));
      }
   }

   @Override
   public void show(List<Player> players) {
      if (this.viewer == null || this.crate.getType().hasAnyWorldAnimationPhase()) {
         BlockFace blockFace = this.crate.getFacingDirection().toBukkit(this.crate.getType().getBlockMaterial());
         if (this.crate.getType().getBlockType().isDataBase64()) {
            Utilities.getNMSFactory()
               .sendTexturedBlockUpdatePacket(players, this.crate.getLocation(), this.crate.getType().getBlockType().getTexture(), blockFace);
         } else {
            Utilities.getNMSFactory()
               .sendBlockUpdatePacket(players, this.crate.getLocation(), this.crate.getType().getBlockType().getBukkitMaterial(), (byte)0, blockFace);
         }
      }
   }

   @Override
   public void hide(List<Player> players) {
      if (this.viewer == null || this.crate.getType().hasAnyWorldAnimationPhase()) {
         Utilities.getNMSFactory().sendBlockUpdatePacket(players, this.crate.getLocation(), Material.AIR, (byte)0, BlockFace.NORTH);
      }
   }

   @Override
   public void playAnimation(PhaseType type) {
   }

   @Override
   public boolean isPlayingAnimation(PhaseType type) {
      return false;
   }

   @Override
   public double getDisplayHeight() {
      switch (this.crate.getType().getBlockType().getMaterial()) {
         case CHEST:
         case ENDER_CHEST:
            return 0.875;
         default:
            return 1.0;
      }
   }
}
