package id.seria.gacha.lib.common.nms.interfaces.particles.data.texture;

import org.bukkit.Material;

public final class BlockTexture extends ParticleTexture {
   public BlockTexture(Material material) {
      super(material, (byte)0);
   }

   public BlockTexture(Material material, byte data) {
      super(material, data);
   }
}
