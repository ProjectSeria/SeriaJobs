package id.seria.gacha.managers.crates.engine;

import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.managers.crates.Crate;
import java.util.List;
import lombok.Generated;
import org.bukkit.entity.Player;

public abstract class EngineDisplayComponent {
   protected final Crate crate;
   protected boolean loaded;

   public EngineDisplayComponent(Crate crate) {
      this.crate = crate;
   }

   public abstract void create(boolean var1);

   public abstract void destroy(boolean var1);

   public abstract void update();

   public abstract void show(List<Player> var1);

   public abstract void hide(List<Player> var1);

   public abstract void playAnimation(PhaseType var1);

   public abstract boolean isPlayingAnimation(PhaseType var1);

   public abstract double getDisplayHeight();

   @Generated
   public Crate getCrate() {
      return this.crate;
   }

   @Generated
   public boolean isLoaded() {
      return this.loaded;
   }
}
