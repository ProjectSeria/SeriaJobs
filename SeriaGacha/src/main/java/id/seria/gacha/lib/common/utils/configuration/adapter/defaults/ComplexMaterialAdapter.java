package id.seria.gacha.lib.common.utils.configuration.adapter.defaults;

import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import id.seria.gacha.lib.common.utils.configuration.objects.ComplexMaterial;
import id.seria.gacha.lib.common.utils.objects.Base64;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import java.lang.reflect.Type;
import java.util.Optional;

public class ComplexMaterialAdapter extends ConfigAdapter<ComplexMaterial, String> {
   public ComplexMaterialAdapter() {
   }

   public ComplexMaterial deserialize(ConfigurationReader reader, Type type, Object object) throws ErrorException, WarningException {
      if (!(object instanceof String)) {
         throw new WarningException("Expected \"String\", founded \"" + object.getClass().getSimpleName() + "\"");
      }

      String stringObject = object.toString();
      String rawObject = object.toString().replace(" ", "_");
      String[] splitted = rawObject.split(":");
      if (splitted.length == 0) {
         throw new ErrorException("Value missing");
      }

      Optional<XMaterial> xMaterialOpt = XMaterial.matchXMaterial(splitted[0]);
      if (!xMaterialOpt.isPresent()) {
         if (splitted.length >= 2) {
            ComplexMaterial cm = new ComplexMaterial(XMaterial.BARRIER, null);
            cm.setHookMaterial(rawObject);
            return cm;
         } else {
            throw new WarningException("Unknown material \"" + stringObject + "\"");
         }
      } else {
         XMaterial xMaterial = xMaterialOpt.get();
         if (splitted.length == 1) {
            return new ComplexMaterial(xMaterial, null);
         }

         String data = splitted[1];
         if (StringUtil.isInteger(data)) {
            XMaterial xMaterialWithData = XMaterial.matchXMaterial(splitted[0] + ":" + data)
               .orElseThrow(() -> new WarningException("Unknown material \"" + stringObject + "\""));
            return new ComplexMaterial(xMaterialWithData, null);
         }

         if (Base64.isValid(data)) {
            if (!splitted[0].contains("SKULL") && !splitted[0].contains("HEAD")) {
               throw new WarningException("Expected material \"Player Head\" when parsing data as \"Base 64\"");
            } else {
               return new ComplexMaterial(XMaterial.PLAYER_HEAD, data);
            }
         } else {
            return new ComplexMaterial(XMaterial.PLAYER_HEAD, data);
         }
      }
   }

   public String serialize(ConfigurationWriter writer, ComplexMaterial materialCompound) throws ErrorException {
      if (materialCompound.getHookMaterial() != null) {
         return materialCompound.getHookMaterial();
      } else {
         return !materialCompound.isTextureValid()
            ? writer.simplifyObject(materialCompound.getMaterial()).toString()
            : XMaterial.PLAYER_HEAD.parseMaterial().name() + ":" + materialCompound.getTexture();
      }
   }
}
