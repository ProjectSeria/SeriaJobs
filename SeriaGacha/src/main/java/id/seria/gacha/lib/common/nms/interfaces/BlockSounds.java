package id.seria.gacha.lib.common.nms.interfaces;

import org.bukkit.Sound;

public interface BlockSounds {
   Sound getBreakSound();

   Sound getHitSound();

   Sound getStepSound();

   Sound getPlaceSound();

   Sound getFallSound();

   float getVolume();

   float getPitch();
}
