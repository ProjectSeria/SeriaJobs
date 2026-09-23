package id.seria.gacha.managers.crates.hologram;

import id.seria.gacha.api.crate.hologram.Hologram;
import id.seria.gacha.managers.crates.Crate;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class CrateHologram implements Hologram {
   protected final Crate crate;

   public CrateHologram(Crate crate) {
      this.crate = crate;
   }

   @Override
   public abstract void show(Player var1);

   @Override
   public abstract void hide(Player var1);

   @Override
   public abstract void moveTo(Location var1);

   public abstract void applyStyle();

   @Override
   public abstract void refresh();

   @Override
   public abstract void despawn();

   @Generated
   public Crate getCrate() {
      return this.crate;
   }
}
