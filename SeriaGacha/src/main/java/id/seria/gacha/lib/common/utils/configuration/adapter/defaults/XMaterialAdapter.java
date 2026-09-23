package id.seria.gacha.lib.common.utils.configuration.adapter.defaults;

import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import id.seria.gacha.lib.xseries.XMaterial;
import java.lang.reflect.Type;

public class XMaterialAdapter extends ConfigAdapter<XMaterial, String> {
   public XMaterialAdapter() {
   }

   public XMaterial deserialize(ConfigurationReader reader, Type type, Object object) throws ErrorException, WarningException {
      if (!(object instanceof String)) {
         throw new WarningException("Expected \"String\", founded \"" + object.getClass().getSimpleName() + "\"");
      } else {
         return XMaterial.matchXMaterial(object.toString()).orElseThrow(() -> new WarningException("Unknown material \"" + object.toString() + "\""));
      }
   }

   public String serialize(ConfigurationWriter writer, XMaterial material) throws ErrorException {
      if (XMaterial.supports(1, 13)) {
         return material.name();
      } else {
         return material.getData() == 0 ? writer.simplifyObject(material.parseMaterial()).toString() : material.parseMaterial() + ":" + material.getData();
      }
   }
}
