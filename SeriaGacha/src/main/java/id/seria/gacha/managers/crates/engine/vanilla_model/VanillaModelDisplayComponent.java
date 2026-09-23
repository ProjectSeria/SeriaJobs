package id.seria.gacha.managers.crates.engine.vanilla_model;

import id.seria.gacha.Crates;
import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.lib.common.nms.interfaces.entity.DisplayTransformation;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeItemDisplayEntity;
import id.seria.gacha.lib.common.services.Services;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.RotationUtil;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.engine.EngineDisplayComponent;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class VanillaModelDisplayComponent extends EngineDisplayComponent {
   public static final boolean ITEM_MODEL_SUPPORTED = ItemBuilder.of(Material.STONE).isItemModelSupported();
   private static final double LEGACY_ARMOR_STAND_HELMET_HEIGHT = 1.44;
   private static final double BLOCK_CENTER = 0.5;
   protected FakeItemDisplayEntity entity;
   protected VanillaModelEngineData data;

   public VanillaModelDisplayComponent(Crate crate, VanillaModelEngineData data) {
      super(crate);
      this.data = data;
   }

   @Override
   public void create(boolean updateBlock) {
      if (updateBlock || !this.crate.getBlock().getType().equals(Material.BARRIER)) {
         this.crate.getBlock().setType(Material.BARRIER, false);
      }

      Location loc = this.crate.getLocation().clone();
      loc.setX(loc.getBlockX() + 0.5);
      loc.setY(loc.getBlockY() + 0.5);
      loc.setZ(loc.getBlockZ() + 0.5);
      this.entity = Utilities.getNMSFactory().createFakeItemDisplay(this.crate.getPlugin(), loc);
      boolean hasItemModel = ITEM_MODEL_SUPPORTED && this.data.getItemModel() != null && !this.data.getItemModel().isEmpty();
      ItemBuilder builder = ItemBuilder.of(this.data.getMaterial()).setCustomModelData(hasItemModel ? 0 : this.data.getCustomModelData());
      if (hasItemModel) {
         try {
            builder.setItemModel(this.data.getItemModel());
         } catch (Exception e) {
            Crates.getLogger().warn("Failed to apply item model '" + this.data.getItemModel() + "' to crate display:");
            e.printStackTrace();
         }
      }

      ItemStack item = builder.build();
      this.entity.setItemStack(item);
      this.entity.setItemDisplayTransform(FakeItemDisplayEntity.ItemDisplayTransform.HEAD);
      this.entity.setGravity(false);
      Vector v = new Vector(this.data.getOffsetX(), this.data.getOffsetY(), this.data.getOffsetZ());
      RotationUtil.applyYawRot(v, -this.crate.getFacingDirection().getDirection());
      float scale = (float)this.data.getScale();
      this.entity
         .setTransformation(
            DisplayTransformation.identity()
               .setScale(scale, scale, scale)
               .setLeftRotationAxisAngle(Math.toRadians(-this.data.getRotationDegrees()), 0.0, 1.0, 0.0)
               .setTranslation((float)v.getX(), (float)(v.getY() + 1.44 - 0.5), (float)v.getZ())
         );
      this.entity.setRenderDistance(Crates.getConfiguration().getCratesRenderRadius());
      Services.getWatcherService().startWatching(this.entity, true);
   }

   @Override
   public void destroy(boolean updateBlock) {
      if (updateBlock) {
         this.crate.getBlock().setType(Material.AIR, false);
      }

      Services.getWatcherService().stopWatching(this.entity);
   }

   @Override
   public void update() {
   }

   @Override
   public void show(List<Player> players) {
   }

   @Override
   public void hide(List<Player> players) {
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
      return 1.0;
   }
}
