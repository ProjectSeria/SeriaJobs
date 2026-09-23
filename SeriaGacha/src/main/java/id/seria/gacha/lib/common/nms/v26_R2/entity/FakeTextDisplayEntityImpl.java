package id.seria.gacha.lib.common.nms.v26_R2.entity;

import id.seria.gacha.lib.common.nms.interfaces.entity.DisplayTransformation;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeEntityType;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeTextDisplayEntity;
import java.awt.Color;
import net.minecraft.world.entity.Display.TextDisplay;
import org.bukkit.plugin.java.JavaPlugin;

public class FakeTextDisplayEntityImpl extends AbstractFakeDisplayEntityImpl implements FakeTextDisplayEntity {
   public FakeTextDisplayEntityImpl(JavaPlugin plugin, TextDisplay nmsEntity) {
      super(plugin, FakeEntityType.TEXT_DISPLAY, nmsEntity);
   }

   public TextDisplay getNmsEntity() {
      return (TextDisplay)this.nmsEntity;
   }

   public org.bukkit.entity.TextDisplay getBukkitEntity() {
      return (org.bukkit.entity.TextDisplay)this.nmsEntity.getBukkitEntity();
   }

   @Override
   public FakeTextDisplayEntity.TextAlignment getAlignment() {
      org.bukkit.entity.TextDisplay.TextAlignment a = this.getBukkitEntity().getAlignment();
      switch (a) {
         case LEFT:
            return FakeTextDisplayEntity.TextAlignment.LEFT;
         case RIGHT:
            return FakeTextDisplayEntity.TextAlignment.RIGHT;
         case CENTER:
         default:
            return FakeTextDisplayEntity.TextAlignment.CENTER;
      }
   }

   @Override
   public Color getBackgroundColor() {
      org.bukkit.Color bukkit = this.getBukkitEntity().getBackgroundColor();
      return new Color(bukkit.getRed(), bukkit.getGreen(), bukkit.getBlue(), bukkit.getAlpha());
   }

   @Override
   public int getLineWidth() {
      return this.getBukkitEntity().getLineWidth();
   }

   @Override
   public String getText() {
      return this.getBukkitEntity().getText();
   }

   @Override
   public byte getTextOpacity() {
      return this.getBukkitEntity().getTextOpacity();
   }

   @Override
   public boolean isDefaultBackground() {
      return this.getBukkitEntity().isDefaultBackground();
   }

   @Override
   public boolean isSeeThrough() {
      return this.getBukkitEntity().isSeeThrough();
   }

   @Override
   public boolean isShadowed() {
      return this.getBukkitEntity().isShadowed();
   }

   @Override
   public FakeTextDisplayEntity setAlignment(FakeTextDisplayEntity.TextAlignment alignment) {
      switch (alignment) {
         case LEFT:
            this.getBukkitEntity().setAlignment(org.bukkit.entity.TextDisplay.TextAlignment.LEFT);
            break;
         case RIGHT:
            this.getBukkitEntity().setAlignment(org.bukkit.entity.TextDisplay.TextAlignment.RIGHT);
            break;
         case CENTER:
         default:
            this.getBukkitEntity().setAlignment(org.bukkit.entity.TextDisplay.TextAlignment.CENTER);
      }

      this.metadataChanged = true;
      return this;
   }

   @Override
   public FakeTextDisplayEntity setBackgroundColor(Color color) {
      org.bukkit.Color bukkit = org.bukkit.Color.fromARGB(color.getRGB());
      this.getBukkitEntity().setBackgroundColor(bukkit);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public FakeTextDisplayEntity setDefaultBackground(boolean defaultBackground) {
      this.getBukkitEntity().setDefaultBackground(defaultBackground);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public FakeTextDisplayEntity setLineWidth(int width) {
      this.getBukkitEntity().setLineWidth(width);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public FakeTextDisplayEntity setSeeThrough(boolean seeThrough) {
      this.getBukkitEntity().setSeeThrough(seeThrough);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public FakeTextDisplayEntity setShadowed(boolean shadow) {
      this.getBukkitEntity().setShadowed(shadow);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public FakeTextDisplayEntity setText(String text) {
      this.getBukkitEntity().setText(text);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public FakeTextDisplayEntity setTextOpacity(byte opacity) {
      this.getBukkitEntity().setTextOpacity(opacity);
      this.metadataChanged = true;
      return this;
   }

   @Override
   public FakeTextDisplayEntity setScale(float scale) {
      this.setTransformation(DisplayTransformation.ofScale(scale));
      return this;
   }
}
