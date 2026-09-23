package id.seria.gacha.managers.crates.hologram;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import id.seria.gacha.api.crate.hologram.HologramType;
import id.seria.gacha.lib.common.nms.interfaces.entity.FakeDisplayEntity;
import id.seria.gacha.lib.common.uicomponents.newest.Ui;
import id.seria.gacha.lib.common.utils.configuration.adapter.Adaptable;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.hologram.adapter.HologramPropertiesAdapter;
import id.seria.gacha.managers.crates.hologram.builtin.BuiltinHologram;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Adaptable(adapter = HologramPropertiesAdapter.class)
public class HologramProperties implements id.seria.gacha.api.crate.hologram.HologramProperties, Cloneable {
   public static final String LINE_GAP = "line-gap";
   public static final String BILLBOARD = "billboard";
   public static final String TEXT_OPACITY = "text-opacity";
   public static final String SEE_THROUGH = "see-through";
   public static final String SHADOW = "shadow";
   public static final String BACKGROUND_COLOR = "background-color";
   public static final String SCALE = "scale";
   public static final double DEFAULT_LINE_GAP = 0.3;
   public static final FakeDisplayEntity.Billboard DEFAULT_BILLBOARD = FakeDisplayEntity.Billboard.CENTER;
   public static final int DEFAULT_TEXT_OPACITY = 255;
   public static final boolean DEFAULT_SEE_THROUGH = false;
   public static final boolean DEFAULT_SHADOW = true;
   public static final String DEFAULT_BACKGROUND_COLOR = "0,0,0,0";
   public static final double DEFAULT_SCALE = 1.0;
   private static final Color FALLBACK_BACKGROUND_COLOR = new Color(0, 0, 0, 64);
   private static final String[] STYLE_KEYS = new String[]{"line-gap", "billboard", "text-opacity", "see-through", "shadow", "background-color", "scale"};
   private boolean sealed = true;
   private final Map<String, Object> properties;

   public HologramProperties() {
      this.properties = new LinkedHashMap() {
         @Override
         public Object remove(Object key) {
            if (HologramProperties.this.sealed) {
               throw new UnsupportedOperationException("Properties can't be erased");
            } else {
               return super.remove(key);
            }
         }

         @Override
         public void clear() {
            if (HologramProperties.this.sealed) {
               throw new UnsupportedOperationException("Properties can't be erased");
            }

            super.clear();
         }
      };
      this.setType(HologramType.BUILTIN);
   }

   private HologramProperties(Map<String, Object> properties) {
      this.properties = Maps.newLinkedHashMap(properties);
   }

   @Override
   public HologramType getType() {
      return HologramType.BUILTIN;
   }

   @Override
   public void setType(HologramType type) {
      this.sealed = false;
      double offset = this.getOffset();
      Map<String, Object> style = new LinkedHashMap<>();

      for (String key : STYLE_KEYS) {
         Object value = this.properties.get(key);
         if (value != null) {
            style.put(key, value);
         }
      }

      this.properties.clear();
      this.properties.put("type", type);
      this.properties.put("offset", offset);
      this.properties.putAll(style);
      if (HologramType.BUILTIN.equals(type)) {
         this.properties
            .put(
               "lines", Lists.newArrayList(new String[]{"&e&l%name%", "", Ui.smallCaps("&aRight &7Click to open"), Ui.smallCaps("&cLeft &7Click to preview")})
            );
      }

      this.sealed = true;
   }

   public double getLineGap() {
      return toDouble(this.get("line-gap"), 0.3);
   }

   public void setLineGap(double lineGap) {
      this.set("line-gap", lineGap);
   }

   public FakeDisplayEntity.Billboard getBillboard() {
      Object value = this.get("billboard");
      if (value instanceof FakeDisplayEntity.Billboard) {
         return (FakeDisplayEntity.Billboard)value;
      }

      if (value instanceof String) {
         try {
            return FakeDisplayEntity.Billboard.valueOf(((String)value).toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var3) {
         }
      }

      return DEFAULT_BILLBOARD;
   }

   public void setBillboard(FakeDisplayEntity.Billboard billboard) {
      this.set("billboard", (billboard == null ? DEFAULT_BILLBOARD : billboard).name());
   }

   public int getTextOpacity() {
      return MathUtil.clamp((int)toDouble(this.get("text-opacity"), 255.0), 0, 255);
   }

   public void setTextOpacity(int textOpacity) {
      this.set("text-opacity", MathUtil.clamp(textOpacity, 0, 255));
   }

   public boolean isSeeThrough() {
      return toBoolean(this.get("see-through"), false);
   }

   public void setSeeThrough(boolean seeThrough) {
      this.set("see-through", seeThrough);
   }

   public boolean isShadow() {
      return toBoolean(this.get("shadow"), true);
   }

   public void setShadow(boolean shadow) {
      this.set("shadow", shadow);
   }

   public String getRawBackgroundColor() {
      Object value = this.get("background-color");
      return value instanceof String ? (String)value : "0,0,0,0";
   }

   public void setRawBackgroundColor(String rawBackgroundColor) {
      this.set("background-color", rawBackgroundColor == null ? "0,0,0,0" : rawBackgroundColor);
   }

   public double getScale() {
      return toDouble(this.get("scale"), 1.0);
   }

   public void setScale(double scale) {
      this.set("scale", scale);
   }

   public Color getBackgroundColor() {
      return parseColor(this.getRawBackgroundColor());
   }

   public static Color parseColor(String rawColor) {
      if (rawColor != null && !rawColor.trim().isEmpty()) {
         String raw = rawColor.trim();
         if (raw.contains(",")) {
            String[] parts = raw.split(",", -1);
            if (parts.length >= 4) {
               try {
                  int a = MathUtil.clamp(Integer.parseInt(parts[0].trim()), 0, 255);
                  int r = MathUtil.clamp(Integer.parseInt(parts[1].trim()), 0, 255);
                  int g = MathUtil.clamp(Integer.parseInt(parts[2].trim()), 0, 255);
                  int b = MathUtil.clamp(Integer.parseInt(parts[3].trim()), 0, 255);
                  return new Color(r, g, b, a);
               } catch (NumberFormatException var8) {
               }
            }
         }

         if (raw.startsWith("#")) {
            try {
               String hex = raw.substring(1);
               if (hex.length() == 8) {
                  int a = Integer.parseInt(hex.substring(0, 2), 16);
                  int r = Integer.parseInt(hex.substring(2, 4), 16);
                  int g = Integer.parseInt(hex.substring(4, 6), 16);
                  int b = Integer.parseInt(hex.substring(6, 8), 16);
                  return new Color(r, g, b, a);
               }

               if (hex.length() == 6) {
                  return new Color(
                     Integer.parseInt(hex.substring(0, 2), 16), Integer.parseInt(hex.substring(2, 4), 16), Integer.parseInt(hex.substring(4, 6), 16)
                  );
               }
            } catch (NumberFormatException | StringIndexOutOfBoundsException var7) {
            }
         }

         return FALLBACK_BACKGROUND_COLOR;
      } else {
         return FALLBACK_BACKGROUND_COLOR;
      }
   }

   private static double toDouble(Object value, double fallback) {
      return value instanceof Number ? ((Number)value).doubleValue() : fallback;
   }

   private static boolean toBoolean(Object value, boolean fallback) {
      if (value instanceof Boolean) {
         return (Boolean)value;
      } else {
         return value instanceof String ? Boolean.parseBoolean((String)value) : fallback;
      }
   }

   @Override
   public double getOffset() {
      return this.getOrSetDefault("offset", 1.0);
   }

   @Override
   public void setOffset(double offset) {
      this.set("offset", offset);
   }

   @Override
   public Object get(String key) {
      return this.properties.get(key);
   }

   public <T> T get(String key, Class<T> type) {
      Object value = this.get(key);
      if (value == null) {
         return null;
      } else if (!type.isInstance(value)) {
         throw new ClassCastException("Property '" + key + "' is not of type " + type.getSimpleName());
      } else {
         return (T)value;
      }
   }

   public <T> T getOrSetDefault(String key, T fallback) {
      Object value = this.get(key);
      if (value == null) {
         this.set(key, fallback);
         return fallback;
      }

      try {
         return (T)value;
      } catch (ClassCastException e) {
         throw new ClassCastException("Property '" + key + "' is not of expected type: " + fallback.getClass().getSimpleName());
      }
   }

   @Override
   public void set(String key, Object value) {
      this.properties.put(key, value);
   }

   public CrateHologram createHologram(Crate crate) {
      return new BuiltinHologram(crate);
   }

   public HologramProperties clone() {
      return new HologramProperties(this.properties);
   }
}
