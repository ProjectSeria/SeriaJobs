package id.seria.gacha.actions.executors;

import id.seria.gacha.lib.common.utils.actions.Action;
import id.seria.gacha.lib.common.utils.actions.ActionExecutor;
import id.seria.gacha.lib.common.utils.actions.exceptions.ActionExecutionException;
import id.seria.gacha.lib.xseries.XPotion;
import java.util.Optional;
import org.bukkit.potion.PotionEffect;

public class PotionEffectAction extends Action {
   public PotionEffectAction(String[] arguments) {
      super("POTION_EFFECT", arguments);
   }

   @Override
   public void execute(ActionExecutor executor) throws ActionExecutionException {
      int duration = Integer.valueOf(this.getArgument(1));
      if (duration < 0) {
         duration = Integer.MAX_VALUE;
      } else {
         duration *= 20;
      }

      int amplifier = Integer.valueOf(this.getArgument(2));
      Optional<XPotion> optional = XPotion.matchXPotion(this.getArgument(0));
      if (optional.isPresent()) {
         PotionEffect effect = new PotionEffect(optional.get().getPotionEffectType(), duration, amplifier, false, false);
         executor.getPlayer().addPotionEffect(effect);
      }
   }

   @Override
   public String getArgumentsSyntax() {
      return "<effect>; <duration>; <amplifier>";
   }
}
