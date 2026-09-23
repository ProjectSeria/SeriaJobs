package id.seria.gacha.lib.common.utils.actions.common;

import id.seria.gacha.lib.common.utils.actions.Action;
import id.seria.gacha.lib.common.utils.actions.ActionExecutor;
import id.seria.gacha.lib.common.utils.actions.exceptions.ActionExecutionException;
import id.seria.gacha.lib.xseries.XSound;
import java.util.Optional;

public class SoundAction extends Action {
   public SoundAction(String[] arguments) {
      super("SOUND", arguments);
   }

   @Override
   public void execute(ActionExecutor executor) throws ActionExecutionException {
      Optional<XSound> sound = XSound.matchXSound(this.getArgument(0).toUpperCase());
      if (sound.isPresent()) {
         sound.get().play(executor.getPlayer(), Float.valueOf(this.getArgument(1)), Float.valueOf(this.getArgument(2)));
      }
   }

   @Override
   public String getArgumentsSyntax() {
      return "<sound>; <volume>; <pitch>";
   }
}
