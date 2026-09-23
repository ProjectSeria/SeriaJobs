package id.seria.gacha.lib.common.nms.interfaces.entity;

import org.bukkit.Material;

public interface FakeBlockDisplayEntity extends FakeDisplayEntity {
   String getBlockData();

   FakeBlockDisplayEntity setBlockData(String var1);

   FakeBlockDisplayEntity setBlock(Material var1);
}
