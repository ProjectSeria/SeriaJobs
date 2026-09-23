package id.seria.gacha.lib.xseries.profiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class ProfileLogger {
   public static final Logger LOGGER = LogManager.getLogger("XSkull");

   public ProfileLogger() {
   }

   public static void debug(String mainMessage, Object... variables) {
      LOGGER.debug(mainMessage, variables);
   }
}
