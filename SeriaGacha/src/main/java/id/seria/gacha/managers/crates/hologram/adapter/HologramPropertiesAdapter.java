package id.seria.gacha.managers.crates.hologram.adapter;

import id.seria.gacha.api.crate.hologram.HologramType;
import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import id.seria.gacha.managers.crates.hologram.HologramProperties;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public class HologramPropertiesAdapter extends ConfigAdapter<HologramProperties, Map<String, Object>> {
   public HologramPropertiesAdapter() {
   }

   public HologramProperties deserialize(ConfigurationReader reader, Type rawType, Object object) throws WarningException {
      if (!(object instanceof ConfigurationSection section)) {
         throw new WarningException("Expected \"Yaml Section\", found \"" + object.getClass().getSimpleName() + "\"");
      } else {
         double offset = this.mapDouble(section.get("offset"));
         HologramProperties properties = new HologramProperties();
         properties.set("offset", offset);
         properties.setType(HologramType.BUILTIN);
         List<String> lines = this.mapList(section.get("lines"));
         if (lines != null) {
            properties.set("lines", lines);
         }

         if (section.isSet("line-gap")) {
            properties.setLineGap(section.getDouble("line-gap"));
         }

         if (section.isSet("billboard")) {
            properties.set("billboard", section.getString("billboard"));
         }

         if (section.isSet("text-opacity")) {
            properties.setTextOpacity(section.getInt("text-opacity"));
         }

         if (section.isSet("see-through")) {
            properties.setSeeThrough(section.getBoolean("see-through"));
         }

         if (section.isSet("shadow")) {
            properties.setShadow(section.getBoolean("shadow"));
         }

         if (section.isSet("background-color")) {
            properties.setRawBackgroundColor(section.getString("background-color"));
         }

         if (section.isSet("scale")) {
            properties.setScale(section.getDouble("scale"));
         }

         return properties;
      }
   }

   public Map<String, Object> serialize(ConfigurationWriter writer, HologramProperties properties) throws ErrorException {
      HologramType type = properties.getType();
      Map<String, Object> values = new LinkedHashMap<>();
      values.put("type", type.name());
      values.put("offset", properties.getOffset());
      List<String> lines = properties.get("lines") == null ? new ArrayList<>() : (List)properties.get("lines");
      values.put("lines", lines);
      values.put("line-gap", properties.getLineGap());
      values.put("billboard", properties.getBillboard().name());
      values.put("text-opacity", properties.getTextOpacity());
      values.put("see-through", properties.isSeeThrough());
      values.put("shadow", properties.isShadow());
      values.put("background-color", properties.getRawBackgroundColor());
      values.put("scale", properties.getScale());
      return values;
   }

   private double mapDouble(Object o) {
      return o instanceof Number ? ((Number)o).doubleValue() : 0.0;
   }

   @Nullable
   private List<String> mapList(Object obj) {
      return obj instanceof List ? (List)obj : null;
   }
}
