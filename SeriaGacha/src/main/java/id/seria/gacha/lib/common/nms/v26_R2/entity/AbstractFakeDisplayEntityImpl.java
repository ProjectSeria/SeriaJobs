package id.seria.gacha.lib.common.nms.v26_R2.entity;

import id.seria.gacha.lib.common.nms.interfaces.entity.DisplayTransformation;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeDisplayEntity;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.nms.interfaces.packets.FakePacket;
import id.seria.gacha.lib.common.nms.v26_R2.packets.WrappedEntityTeleportPacket;
import net.minecraft.world.entity.Display;
import org.bukkit.Color;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class AbstractFakeDisplayEntityImpl extends FakeEntityImpl implements FakeDisplayEntity {
   protected AbstractFakeDisplayEntityImpl(JavaPlugin plugin, FakeEntityType type, Display nmsEntity) {
      super(plugin, type, nmsEntity);
   }

   public Display getNmsEntity() {
      return (Display)this.nmsEntity;
   }

   public org.bukkit.entity.Display getBukkitEntity() {
      return (org.bukkit.entity.Display)this.nmsEntity.getBukkitEntity();
   }

   @Override
   public FakePacket prepareTeleportPacket() {
      return new WrappedEntityTeleportPacket(this);
   }

   @Override
   public int getInterpolationDuration() {
      return this.getBukkitEntity().getInterpolationDuration();
   }

   @Override
   public void setInterpolationDuration(int duration) {
      this.getBukkitEntity().setInterpolationDuration(duration);
      this.metadataChanged = true;
   }

   @Override
   public int getTeleportDuration() {
      return this.getBukkitEntity().getTeleportDuration();
   }

   @Override
   public void setTeleportDuration(int duration) {
      this.getBukkitEntity().setTeleportDuration(duration);
      this.metadataChanged = true;
   }

   @Override
   public float getViewRange() {
      return this.getBukkitEntity().getViewRange();
   }

   @Override
   public void setViewRange(float range) {
      this.getBukkitEntity().setViewRange(range);
      this.metadataChanged = true;
   }

   @Override
   public float getShadowRadius() {
      return this.getBukkitEntity().getShadowRadius();
   }

   @Override
   public void setShadowRadius(float radius) {
      this.getBukkitEntity().setShadowRadius(radius);
      this.metadataChanged = true;
   }

   @Override
   public float getShadowStrength() {
      return this.getBukkitEntity().getShadowStrength();
   }

   @Override
   public void setShadowStrength(float strength) {
      this.getBukkitEntity().setShadowStrength(strength);
      this.metadataChanged = true;
   }

   @Override
   public float getDisplayWidth() {
      return this.getBukkitEntity().getDisplayWidth();
   }

   @Override
   public void setDisplayWidth(float width) {
      this.getBukkitEntity().setDisplayWidth(width);
      this.metadataChanged = true;
   }

   @Override
   public float getDisplayHeight() {
      return this.getBukkitEntity().getDisplayHeight();
   }

   @Override
   public void setDisplayHeight(float height) {
      this.getBukkitEntity().setDisplayHeight(height);
      this.metadataChanged = true;
   }

   @Override
   public int getInterpolationDelay() {
      return this.getBukkitEntity().getInterpolationDelay();
   }

   @Override
   public void setInterpolationDelay(int delay) {
      this.getBukkitEntity().setInterpolationDelay(delay);
      this.metadataChanged = true;
   }

   @Override
   public FakeDisplayEntity.Billboard getBillboard() {
      org.bukkit.entity.Display.Billboard billboard = this.getBukkitEntity().getBillboard();
      if (billboard == null) {
         return FakeDisplayEntity.Billboard.FIXED;
      }

      switch (billboard) {
         case VERTICAL:
            return FakeDisplayEntity.Billboard.VERTICAL;
         case HORIZONTAL:
            return FakeDisplayEntity.Billboard.HORIZONTAL;
         case CENTER:
            return FakeDisplayEntity.Billboard.CENTER;
         case FIXED:
         default:
            return FakeDisplayEntity.Billboard.FIXED;
      }
   }

   @Override
   public void setBillboard(FakeDisplayEntity.Billboard billboard) {
      this.getBukkitEntity().setBillboard(switch (billboard) {
         case VERTICAL -> org.bukkit.entity.Display.Billboard.VERTICAL;
         case HORIZONTAL -> org.bukkit.entity.Display.Billboard.HORIZONTAL;
         case CENTER -> org.bukkit.entity.Display.Billboard.CENTER;
         default -> org.bukkit.entity.Display.Billboard.FIXED;
      });
      this.metadataChanged = true;
   }

   @Override
   public Color getGlowColorOverride() {
      return this.getBukkitEntity().getGlowColorOverride();
   }

   @Override
   public void setGlowColorOverride(Color color) {
      this.getBukkitEntity().setGlowColorOverride(color);
      this.metadataChanged = true;
   }

   @Override
   public FakeDisplayEntity.Brightness getBrightness() {
      org.bukkit.entity.Display.Brightness brightness = this.getBukkitEntity().getBrightness();
      return brightness == null ? null : new FakeDisplayEntity.Brightness(brightness.getBlockLight(), brightness.getSkyLight());
   }

   @Override
   public void setBrightness(FakeDisplayEntity.Brightness brightness) {
      if (brightness == null) {
         this.getBukkitEntity().setBrightness(null);
      } else {
         this.getBukkitEntity().setBrightness(new org.bukkit.entity.Display.Brightness(brightness.getBlockLight(), brightness.getSkyLight()));
      }

      this.metadataChanged = true;
   }

   @Override
   public DisplayTransformation getTransformation() {
      Transformation transformation = this.getBukkitEntity().getTransformation();
      Vector3f translation = transformation.getTranslation();
      Vector3f scale = transformation.getScale();
      Quaternionf left = transformation.getLeftRotation();
      Quaternionf right = transformation.getRightRotation();
      return new DisplayTransformation()
         .setTranslation(translation.x(), translation.y(), translation.z())
         .setScale(scale.x(), scale.y(), scale.z())
         .setLeftRotation(left.x(), left.y(), left.z(), left.w())
         .setRightRotation(right.x(), right.y(), right.z(), right.w());
   }

   @Override
   public void setTransformation(DisplayTransformation transformation) {
      this.getBukkitEntity()
         .setTransformation(
            new Transformation(
               new Vector3f(transformation.getTranslationX(), transformation.getTranslationY(), transformation.getTranslationZ()),
               new Quaternionf(
                  transformation.getLeftRotationX(), transformation.getLeftRotationY(), transformation.getLeftRotationZ(), transformation.getLeftRotationW()
               ),
               new Vector3f(transformation.getScaleX(), transformation.getScaleY(), transformation.getScaleZ()),
               new Quaternionf(
                  transformation.getRightRotationX(),
                  transformation.getRightRotationY(),
                  transformation.getRightRotationZ(),
                  transformation.getRightRotationW()
               )
            )
         );
      this.metadataChanged = true;
   }
}
