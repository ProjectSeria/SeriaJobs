package id.seria.gacha.api.crate.animation;

import id.seria.gacha.api.crate.CrateInstance;
import id.seria.gacha.api.session.OpeningSession;
import java.util.List;
import org.bukkit.entity.Player;

public interface OpeningAnimationHandler {
   Player getPlayer();

   <T extends OpeningSession> T getSession();

   CrateInstance getCrate();

   int getCurrentPhaseIndex();

   PhaseRunner getCurrentPhase();

   List<PhaseRunner> getPhases();

   void nextPhase();
}
