package id.seria.gacha.lib.common.nms.interfaces.particles.data.texture;

import id.seria.gacha.lib.common.nms.interfaces.particles.data.ParticleData;
import org.bukkit.Material;

public class ParticleTexture extends ParticleData {
   private final Material material;
   private final byte data;

   ParticleTexture(Material material, byte data) {
      this.material = material;
      this.data = data;
   }

   public Material getMaterial() {
      return this.material;
   }

   public byte getData() {
      return this.data;
   }
}
