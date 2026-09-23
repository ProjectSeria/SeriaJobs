package id.seria.gacha.lib.common.utils.inventory;

import com.google.common.collect.Lists;
import id.seria.gacha.api.sdk.text.Text;
import id.seria.gacha.lib.xseries.XEnchantment;
import id.seria.gacha.lib.xseries.XItemFlag;
import id.seria.gacha.lib.xseries.XPotion;
import id.seria.gacha.sdk.platforms.server.rich.RItemStack;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class ItemStackUtil {
   private static final Logger LOGGER = Logger.getLogger(ItemStackUtil.class.getName());
   public static final List<XItemFlag> DEFAULT_HIDE_FLAGS = Collections.unmodifiableList(
      Arrays.asList(
         XItemFlag.HIDE_ENCHANTS,
         XItemFlag.HIDE_ATTRIBUTES,
         XItemFlag.HIDE_UNBREAKABLE,
         XItemFlag.HIDE_CAN_PLACE_ON,
         XItemFlag.HIDE_CAN_BREAK,
         XItemFlag.HIDE_DESTROYS,
         XItemFlag.HIDE_ARMOR_TRIM,
         XItemFlag.HIDE_PROVIDES_TRIM_MATERIAL,
         XItemFlag.HIDE_TRIM,
         XItemFlag.HIDE_POTION_CONTENTS,
         XItemFlag.HIDE_ATTRIBUTE_MODIFIERS,
         XItemFlag.HIDE_BUNDLE_CONTENTS,
         XItemFlag.HIDE_ADDITIONAL_TOOLTIP,
         XItemFlag.HIDE_DYE
      )
   );

   public ItemStackUtil() {
   }

   public static List<String> extractLore(ItemStack item) {
      return item.hasItemMeta() && item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : Lists.newArrayList();
   }

   public static List<String> extractRawLore(ItemStack item) {
      return RItemStack.of(item).lore().stream().map(Text::toPlainText).collect(Collectors.toList());
   }

   public static List<ItemStack> deserializeItems(String base64) {
      if (base64 != null && !base64.trim().isEmpty()) {
         try {
            ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(base64));

            List var14;
            label72: {
               try {
                  label80: {
                     BukkitObjectInputStream bois = new BukkitObjectInputStream(bais);

                     label68: {
                        try {
                           label81: {
                              int size;
                              try {
                                 size = bois.readInt();
                              } catch (Exception ignored) {
                                 var14 = new ArrayList();
                                 break label81;
                              }

                              List<ItemStack> items = new ArrayList<>();

                              for (int i = 0; i < size; i++) {
                                 Object obj = bois.readObject();
                                 if (obj instanceof ItemStack) {
                                    items.add((ItemStack)obj);
                                 }
                              }

                              var14 = items;
                              break label68;
                           }
                        } catch (Throwable var10) {
                           try {
                              bois.close();
                           } catch (Throwable var8) {
                              var10.addSuppressed(var8);
                           }

                           throw var10;
                        }

                        bois.close();
                        break label80;
                     }

                     bois.close();
                     break label72;
                  }
               } catch (Throwable var11) {
                  try {
                     bais.close();
                  } catch (Throwable var7) {
                     var11.addSuppressed(var7);
                  }

                  throw var11;
               }

               bais.close();
               return var14;
            }

            bais.close();
            return var14;
         } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deserializing ItemStacks", e);
            return new ArrayList<>();
         }
      } else {
         return Lists.newArrayList();
      }
   }

   public static String serializeItems(List<ItemStack> items) {
      if (items != null && !items.isEmpty()) {
         try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            String var10;
            try {
               BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos);

               try {
                  boos.writeInt(items.size());

                  for (ItemStack item : items) {
                     boos.writeObject(item);
                  }

                  var10 = Base64.getEncoder().encodeToString(baos.toByteArray());
               } catch (Throwable var7) {
                  try {
                     boos.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }

                  throw var7;
               }

               boos.close();
            } catch (Throwable var8) {
               try {
                  baos.close();
               } catch (Throwable var5) {
                  var8.addSuppressed(var5);
               }

               throw var8;
            }

            baos.close();
            return var10;
         } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error serializing ItemStacks", e);
            return "";
         }
      } else {
         return "";
      }
   }

   public static String rgbToString(Color color) {
      return color == null ? "" : color.getRed() + ", " + color.getGreen() + ", " + color.getBlue();
   }

   public static Color parseRgb(String value) {
      if (value != null && !value.isEmpty()) {
         String[] parts = value.split(",");
         if (parts.length != 3) {
            return null;
         }

         try {
            return Color.fromRGB(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()));
         } catch (IllegalArgumentException ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static List<String> flagsToStrings(List<XItemFlag> flags) {
      return flags == null ? new ArrayList<>() : flags.stream().map(Enum::name).collect(Collectors.toList());
   }

   public static List<XItemFlag> parseFlags(List<String> values) {
      if (values != null && !values.isEmpty()) {
         List<XItemFlag> flags = new ArrayList<>();

         for (String s : values) {
            try {
               flags.add(XItemFlag.valueOf(s.trim().toUpperCase().replace(" ", "_")));
            } catch (IllegalArgumentException var5) {
            }
         }

         return flags.isEmpty() ? null : flags;
      } else {
         return null;
      }
   }

   public static List<String> bannerPatternsToStrings(List<Pattern> patterns) {
      return patterns == null ? new ArrayList<>() : patterns.stream().map(p -> p.getColor().name() + ";" + p.getPattern().name()).collect(Collectors.toList());
   }

   public static List<Pattern> parseBannerPatterns(List<String> values) {
      if (values != null && !values.isEmpty()) {
         List<Pattern> patterns = new ArrayList<>();

         for (String s : values) {
            String[] parts = s.split(";", 2);
            if (parts.length == 2) {
               try {
                  patterns.add(new Pattern(DyeColor.valueOf(parts[0].trim().toUpperCase()), PatternType.valueOf(parts[1].trim().toUpperCase())));
               } catch (IllegalArgumentException var6) {
               }
            }
         }

         return patterns.isEmpty() ? null : patterns;
      } else {
         return null;
      }
   }

   public static List<String> enchantsToStrings(Map<XEnchantment, Integer> enchants) {
      return enchants == null ? new ArrayList<>() : enchants.entrySet().stream().map(e -> e.getKey().name() + ":" + e.getValue()).collect(Collectors.toList());
   }

   public static Map<XEnchantment, Integer> parseEnchants(List<String> values) {
      if (values != null && !values.isEmpty()) {
         Map<XEnchantment, Integer> enchants = new HashMap<>();

         for (String s : values) {
            String[] parts = s.split(":", 2);

            try {
               XEnchantment enchant = XEnchantment.of(parts[0].trim()).orElse(null);
               if (enchant != null) {
                  enchants.put(enchant, parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 1);
               }
            } catch (NumberFormatException var6) {
            }
         }

         return enchants.isEmpty() ? null : enchants;
      } else {
         return null;
      }
   }

   public static List<String> potionEffectsToStrings(List<PotionEffect> effects) {
      return effects == null
         ? new ArrayList<>()
         : effects.stream().map(e -> e.getType().getKey().getKey() + ";" + e.getDuration() + ";" + e.getAmplifier()).collect(Collectors.toList());
   }

   public static List<PotionEffect> parsePotionEffects(List<String> values) {
      if (values != null && !values.isEmpty()) {
         List<PotionEffect> effects = new ArrayList<>();

         for (String s : values) {
            String[] parts = s.split(";", 3);
            if (parts.length == 3) {
               try {
                  PotionEffectType type = XPotion.matchXPotion(parts[0].trim()).map(XPotion::get).orElse(null);
                  if (type != null) {
                     effects.add(new PotionEffect(type, Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim())));
                  }
               } catch (NumberFormatException var6) {
               }
            }
         }

         return effects.isEmpty() ? null : effects;
      } else {
         return null;
      }
   }

   public static NamespacedKey parseNamespacedKey(String value) {
      if (value != null && !value.isEmpty()) {
         String[] parts = value.split(":", 2);
         return parts.length == 2 ? new NamespacedKey(parts[0], parts[1]) : NamespacedKey.minecraft(value);
      } else {
         return null;
      }
   }

   public static ArmorTrimMaterial parseTrimMaterial(String value) {
      if (value != null && !value.isEmpty()) {
         try {
            return ArmorTrimMaterial.valueOf(value.trim().toUpperCase());
         } catch (IllegalArgumentException ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static ArmorTrimPattern parseTrimPattern(String value) {
      if (value != null && !value.isEmpty()) {
         try {
            return ArmorTrimPattern.valueOf(value.trim().toUpperCase());
         } catch (IllegalArgumentException ignored) {
            return null;
         }
      } else {
         return null;
      }
   }
}
