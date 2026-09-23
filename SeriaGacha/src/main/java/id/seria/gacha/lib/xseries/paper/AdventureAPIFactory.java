package id.seria.gacha.lib.xseries.paper;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class AdventureAPIFactory {
   private static final MiniMessage DEFAULT_MINI_MESSAGE = MiniMessage.miniMessage();

   public AdventureAPIFactory() {
   }

   public static void setDisplayNameFromString(ItemMeta meta, String miniMessageText) {
      Component displayName = DEFAULT_MINI_MESSAGE.deserialize(miniMessageText);
      meta.displayName(displayName);
   }

   public static void setLoreFromString(ItemMeta meta, List<String> miniMessageLoreLines) {
      List<Component> lore = miniMessageLoreLines.stream()
         .map(line -> (Component)(line.isEmpty() ? Component.empty() : DEFAULT_MINI_MESSAGE.deserialize(line)))
         .collect(Collectors.toList());
      meta.lore(lore);
   }

   public static void setDisplayName(ItemMeta meta, Component displayname) {
      meta.displayName(displayname);
   }

   public static void setLore(ItemMeta meta, List<? extends Component> lore) {
      meta.lore(lore);
   }

   public static String displayName(ItemMeta meta, Function<List<? extends Component>, List<String>> miniMessageHandler) {
      return miniMessageHandler.apply(Collections.singletonList(meta.displayName())).get(0);
   }

   public static List<String> lore(ItemMeta meta, Function<List<? extends Component>, List<String>> miniMessageHandler) {
      return miniMessageHandler.apply(meta.lore());
   }
}
