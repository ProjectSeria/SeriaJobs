package id.seria.gacha.api.managers;

import id.seria.gacha.api.crate.CrateInstance;
import id.seria.gacha.api.crate.CrateType;
import id.seria.gacha.api.session.OpeningSession;
import java.util.List;
import java.util.Set;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface CratesManager {
   List<? extends CrateType> getRegisteredTypes();

   int countRegisteredTypes();

   List<? extends CrateType> getCrateTypes();

   Set<String> getCratesIdentifier();

   CrateType getTypeByIdentifier(String var1);

   CrateInstance getCrateByBlock(Block var1);

   List<? extends OpeningSession> getPlayerOpeningSessions(Player var1);
}
