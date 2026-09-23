package id.seria.gacha.lib.common.nms.interfaces.wrappers;

import id.seria.gacha.lib.common.nms.interfaces.NMSFactory;
import id.seria.gacha.lib.common.nms.interfaces.particles.Particle;
import id.seria.gacha.lib.common.nms.interfaces.particles.data.ParticleData;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface PlayerWrapper extends EntityWrapper {
   @Override
   NMSFactory getNMSFactory();

   Player getBukkit();

   boolean consumeTotemOfUndying();

   int getTotemCooldownTicks();

   void setCooldown(Material var1, int var2);

   ItemStack getItemInMainHand();

   boolean setItemInMainHand(ItemStack var1);

   ItemStack getItemInOffHand();

   boolean setItemInOffHand(ItemStack var1);

   void setArrowsInBody(int var1);

   void stopSound(Sound var1);

   int getPing();

   double getBaseAttributeValue(PlayerWrapper.Attribute var1);

   void openBook(ItemStack var1);

   default boolean spawnParticle(
      Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, ParticleData data
   ) {
      throw new NotImplementedException();
   }

   enum Attribute {
      GENERIC_MAX_HEALTH;

      Attribute() {
      }
   }
}
