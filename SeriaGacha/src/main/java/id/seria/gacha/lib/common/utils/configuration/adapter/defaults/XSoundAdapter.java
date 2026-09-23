package id.seria.gacha.lib.common.utils.configuration.adapter.defaults;

import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import id.seria.gacha.lib.xseries.XSound;
import java.lang.reflect.Type;
import org.bukkit.Sound;

public class XSoundAdapter extends ConfigAdapter<XSound, String> {
   public XSoundAdapter() {
   }

   public XSound deserialize(ConfigurationReader reader, Type type, Object object) throws ErrorException, WarningException {
      if (!(object instanceof String)) {
         throw new WarningException("Expected \"String\", founded \"" + object.getClass().getSimpleName() + "\"");
      } else {
         return XSound.matchXSound(object.toString()).orElseThrow(() -> new WarningException("Unknown material \"" + object.toString() + "\""));
      }
   }

   public String serialize(ConfigurationWriter writer, XSound sound) throws ErrorException {
      Sound parsedSound = sound.parseSound();
      if (parsedSound == null) {
         throw new ErrorException("Sound \"" + sound.name() + "\" not supported");
      } else {
         return parsedSound.name();
      }
   }
}
