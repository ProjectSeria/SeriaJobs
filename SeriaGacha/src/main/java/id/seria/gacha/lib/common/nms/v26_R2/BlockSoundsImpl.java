package id.seria.gacha.lib.common.nms.v26_R2;

import id.seria.gacha.lib.common.nms.interfaces.BlockSounds;
import lombok.Generated;
import org.bukkit.Sound;

public class BlockSoundsImpl implements BlockSounds {
   private Sound breakSound;
   private Sound hitSound;
   private Sound stepSound;
   private Sound placeSound;
   private Sound fallSound;
   private float volume;
   private float pitch;

   @Generated
   @Override
   public Sound getBreakSound() {
      return this.breakSound;
   }

   @Generated
   @Override
   public Sound getHitSound() {
      return this.hitSound;
   }

   @Generated
   @Override
   public Sound getStepSound() {
      return this.stepSound;
   }

   @Generated
   @Override
   public Sound getPlaceSound() {
      return this.placeSound;
   }

   @Generated
   @Override
   public Sound getFallSound() {
      return this.fallSound;
   }

   @Generated
   @Override
   public float getVolume() {
      return this.volume;
   }

   @Generated
   @Override
   public float getPitch() {
      return this.pitch;
   }

   @Generated
   public void setBreakSound(Sound breakSound) {
      this.breakSound = breakSound;
   }

   @Generated
   public void setHitSound(Sound hitSound) {
      this.hitSound = hitSound;
   }

   @Generated
   public void setStepSound(Sound stepSound) {
      this.stepSound = stepSound;
   }

   @Generated
   public void setPlaceSound(Sound placeSound) {
      this.placeSound = placeSound;
   }

   @Generated
   public void setFallSound(Sound fallSound) {
      this.fallSound = fallSound;
   }

   @Generated
   public void setVolume(float volume) {
      this.volume = volume;
   }

   @Generated
   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   @Generated
   public BlockSoundsImpl() {
   }
}
