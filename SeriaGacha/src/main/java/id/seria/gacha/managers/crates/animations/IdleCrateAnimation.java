package id.seria.gacha.managers.crates.animations;

import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectAnimation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Generated;
import org.bukkit.entity.Player;

public class IdleCrateAnimation {
   protected final Crate crate;
   private final List<IdleEffectAnimation> effects = new ArrayList<>();
   private final List<Player> ignoredPlayers = new ArrayList<>();

   public IdleCrateAnimation(Crate crate) {
      this.crate = crate;
   }

   public void tick() {
      this.effects.forEach(IdleEffectAnimation::tick);
   }

   public void addEffect(IdleEffectAnimation effect) {
      this.effects.add(effect);
   }

   public void setIgnoredPlayers(Collection<Player> ignoredPlayers) {
      this.ignoredPlayers.clear();
      this.ignoredPlayers.addAll(ignoredPlayers);
   }

   public boolean isValid() {
      for (IdleEffectAnimation effect : this.effects) {
         if (effect.getParticle().getFieldName().equalsIgnoreCase("NONE")) {
            return false;
         }
      }

      return true;
   }

   @Generated
   public Crate getCrate() {
      return this.crate;
   }

   @Generated
   public List<IdleEffectAnimation> getEffects() {
      return this.effects;
   }

   @Generated
   public List<Player> getIgnoredPlayers() {
      return this.ignoredPlayers;
   }
}
