package id.seria.gacha.api.crate;

import id.seria.gacha.api.crate.exceptions.CrateOpeningException;
import id.seria.gacha.api.session.OpeningContext;
import org.bukkit.Location;

public interface CrateInstance {
   CrateType getType();

   Location getLocation();

   void openCrate(OpeningContext var1) throws CrateOpeningException;
}
