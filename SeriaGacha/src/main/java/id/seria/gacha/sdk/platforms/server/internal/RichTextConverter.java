package id.seria.gacha.sdk.platforms.server.internal;

import id.seria.gacha.sdk.utilities.Cache;
import id.seria.gacha.sdk.utilities.Colors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;

public class RichTextConverter {
   private static final String LEGACY_HEX_DIGITS = "0123456789abcdefABCDEF";
   private static final boolean ADVENTURE_SUPPORTED = Cache.fetch("adventure-minimessage-supported", () -> {
      try {
         Class.forName("net.kyori.adventure.text.Component");
         Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
         Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
         return true;
      } catch (ClassNotFoundException e) {
         return false;
      }
   });

   private RichTextConverter() {
   }

   public static boolean isAdventureSupported() {
      return ADVENTURE_SUPPORTED;
   }

   public static Component deserialize(String plainText) {
      String normalizedText = normalizeEscapedMiniMessageTags(plainText);
      if (MiniMessageCheck.isSyntaxCompatible(normalizedText)) {
         String miniMessageText = convertLegacyToMiniMessage(normalizedText);
         return RichTextConverter.Adventure.MINI_MESSAGE.deserialize(miniMessageText).decoration(TextDecoration.ITALIC, false);
      } else {
         String legacyFormatted = Colors.replaceColorCodes(normalizedText);
         return legacyFormatted.indexOf(167) >= 0
            ? RichTextConverter.Adventure.LEGACY_SECTION.deserialize(legacyFormatted).decoration(TextDecoration.ITALIC, false)
            : Component.text(normalizedText).decoration(TextDecoration.ITALIC, false);
      }
   }

   public static void applyRichText(Entity entity, String plainText) {
      if (!(entity instanceof TextDisplay)) {
         throw new IllegalArgumentException("Expected a TextDisplay entity but got " + (entity == null ? "null" : entity.getClass().getName()));
      }

      TextDisplay textDisplay = (TextDisplay)entity;
      if (ADVENTURE_SUPPORTED && isTextDisplayComponentSupported()) {
         textDisplay.text(deserialize(plainText));
      } else {
         textDisplay.setText(toLegacyText(plainText));
      }
   }

   private static boolean isTextDisplayComponentSupported() {
      return Cache.fetch("paper-text-display-component-supported", () -> {
         try {
            TextDisplay.class.getMethod("text", Component.class);
            return true;
         } catch (NoSuchMethodException e) {
            return false;
         }
      });
   }

   public static String toLegacyText(String plainText) {
      if (plainText == null || plainText.isEmpty()) {
         return plainText;
      } else {
         return ADVENTURE_SUPPORTED && MiniMessageCheck.isSyntaxCompatible(normalizeEscapedMiniMessageTags(plainText))
            ? RichTextConverter.Adventure.LEGACY_SECTION.serialize(deserialize(plainText))
            : Colors.replaceColorCodes(plainText);
      }
   }

   private static String normalizeEscapedMiniMessageTags(String text) {
      if (text != null && !text.isEmpty() && text.indexOf("\\<") >= 0) {
         StringBuilder output = new StringBuilder(text.length());

         for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == '\\' && i + 1 < text.length() && text.charAt(i + 1) == '<' && i + 2 < text.length()) {
               char tagStart = text.charAt(i + 2);
               if (Character.isLetter(tagStart) || tagStart == '/' || tagStart == '!' || tagStart == '#') {
                  output.append('<');
                  i++;
                  continue;
               }
            }

            output.append(current);
         }

         return output.toString();
      } else {
         return text;
      }
   }

   private static String convertLegacyToMiniMessage(String plainText) {
      if (plainText != null && !plainText.isEmpty()) {
         if (plainText.indexOf(38) < 0 && plainText.indexOf(167) < 0) {
            return plainText;
         }

         StringBuilder result = new StringBuilder(plainText.length() + 16);

         for (int i = 0; i < plainText.length(); i++) {
            char current = plainText.charAt(i);
            if ((current == '&' || current == 167) && i + 1 < plainText.length()) {
               char code = plainText.charAt(i + 1);
               if (code == '#') {
                  if (i + 7 < plainText.length()) {
                     String hex = plainText.substring(i + 2, i + 8);
                     if (isHex(hex)) {
                        result.append("<#").append(hex.toUpperCase()).append('>');
                        i += 7;
                        continue;
                     }
                  }
               } else {
                  if ((code == 'x' || code == 'X') && i + 13 < plainText.length() && isSectionHexColor(plainText, i)) {
                     String hex = new StringBuilder(6)
                        .append(plainText.charAt(i + 3))
                        .append(plainText.charAt(i + 5))
                        .append(plainText.charAt(i + 7))
                        .append(plainText.charAt(i + 9))
                        .append(plainText.charAt(i + 11))
                        .append(plainText.charAt(i + 13))
                        .toString();
                     result.append("<#").append(hex.toUpperCase()).append('>');
                     i += 13;
                     continue;
                  }

                  String tag = legacyCodeToMiniMessageTag(code);
                  if (tag != null) {
                     result.append(tag);
                     i++;
                     continue;
                  }
               }
            }

            result.append(current);
         }

         return result.toString();
      } else {
         return plainText;
      }
   }

   private static String legacyCodeToMiniMessageTag(char code) {
      switch (Character.toLowerCase(code)) {
         case '0':
            return "<black>";
         case '1':
            return "<dark_blue>";
         case '2':
            return "<dark_green>";
         case '3':
            return "<dark_aqua>";
         case '4':
            return "<dark_red>";
         case '5':
            return "<dark_purple>";
         case '6':
            return "<gold>";
         case '7':
            return "<gray>";
         case '8':
            return "<dark_gray>";
         case '9':
            return "<blue>";
         case ':':
         case ';':
         case '<':
         case '=':
         case '>':
         case '?':
         case '@':
         case 'A':
         case 'B':
         case 'C':
         case 'D':
         case 'E':
         case 'F':
         case 'G':
         case 'H':
         case 'I':
         case 'J':
         case 'K':
         case 'L':
         case 'M':
         case 'N':
         case 'O':
         case 'P':
         case 'Q':
         case 'R':
         case 'S':
         case 'T':
         case 'U':
         case 'V':
         case 'W':
         case 'X':
         case 'Y':
         case 'Z':
         case '[':
         case '\\':
         case ']':
         case '^':
         case '_':
         case '`':
         case 'g':
         case 'h':
         case 'i':
         case 'j':
         case 'p':
         case 'q':
         default:
            return null;
         case 'a':
            return "<green>";
         case 'b':
            return "<aqua>";
         case 'c':
            return "<red>";
         case 'd':
            return "<light_purple>";
         case 'e':
            return "<yellow>";
         case 'f':
            return "<white>";
         case 'k':
            return "<obfuscated>";
         case 'l':
            return "<bold>";
         case 'm':
            return "<strikethrough>";
         case 'n':
            return "<underlined>";
         case 'o':
            return "<italic>";
         case 'r':
            return "<reset>";
      }
   }

   private static boolean isHex(String value) {
      if (value != null && value.length() == 6) {
         for (int i = 0; i < value.length(); i++) {
            if ("0123456789abcdefABCDEF".indexOf(value.charAt(i)) < 0) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean isSectionHexColor(String text, int index) {
      if (index + 13 >= text.length()) {
         return false;
      } else {
         return text.charAt(index + 2) == 167
               && text.charAt(index + 4) == 167
               && text.charAt(index + 6) == 167
               && text.charAt(index + 8) == 167
               && text.charAt(index + 10) == 167
               && text.charAt(index + 12) == 167
            ? isHex(
               new StringBuilder(6)
                  .append(text.charAt(index + 3))
                  .append(text.charAt(index + 5))
                  .append(text.charAt(index + 7))
                  .append(text.charAt(index + 9))
                  .append(text.charAt(index + 11))
                  .append(text.charAt(index + 13))
                  .toString()
            )
            : false;
      }
   }

   private static final class Adventure {
      static final TagResolver UNKNOWN_TAG_PASSTHROUGH = new TagResolver() {
         public Tag resolve(String name, ArgumentQueue arguments, Context ctx) {
            StringBuilder raw = new StringBuilder("<").append(name);

            while (arguments.hasNext()) {
               raw.append(':').append(arguments.pop().value());
            }

            raw.append('>');
            return Tag.selfClosingInserting(Component.text(raw.toString()));
         }

         public boolean has(String name) {
            return true;
         }
      };
      static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
         .tags(TagResolver.resolver(new TagResolver[]{UNKNOWN_TAG_PASSTHROUGH, TagResolver.standard()}))
         .build();
      static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();

      private Adventure() {
      }
   }
}
