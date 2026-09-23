package id.seria.gacha.lib.common.uicomponents.newest.actions;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import id.seria.gacha.lib.common.uicomponents.newest.Ui;
import id.seria.gacha.lib.common.uicomponents.newest.layout.ContainerLayout;
import id.seria.gacha.lib.common.utils.Utilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClickActionsFacade {
   private static final Pattern COLOR_CODES = Pattern.compile("(?i)[\u00a7&](#[0-9a-f]{6}|[0-9a-fk-or])");

   public ClickActionsFacade() {
   }

   public static List<String> applyActions(ContainerLayout layout, List<String> description, ClickAction... actions) {
      return applyActions(layout, description, Lists.newArrayList(actions));
   }

   public static List<String> applyActions(ContainerLayout layout, List<String> description, Set<ClickAction> actions) {
      return applyActions(layout, description, Lists.newArrayList(actions));
   }

   public static List<String> applyActions(ContainerLayout layout, List<String> description, List<ClickAction> actions) {
      if (actions.isEmpty()) {
         return description;
      }

      List<ClickAction> normalizedActions = normalizeActions(actions);
      List<String> copy = stripRenderedActions(layout, description);
      copy.add("");

      for (ClickAction action : normalizedActions) {
         copy.add(Ui.smallCaps(layout.getTranslationProvider().translate(action.format())[0]));
      }

      return copy;
   }

   private static List<String> stripRenderedActions(ContainerLayout layout, List<String> description) {
      Set<String> prefixes = collectActionPrefixes(layout);
      if (prefixes.isEmpty()) {
         return new ArrayList<>(description);
      }

      List<String> cleaned = new ArrayList<>(description.size());

      for (String line : description) {
         if (line == null || !isActionLine(line, prefixes)) {
            cleaned.add(line);
         }
      }

      while (!cleaned.isEmpty() && isBlank(cleaned.get(cleaned.size() - 1))) {
         cleaned.remove(cleaned.size() - 1);
      }

      return cleaned;
   }

   private static Set<String> collectActionPrefixes(ContainerLayout layout) {
      Set<String> prefixes = new HashSet<>();

      for (ClickAction.ClickType clickType : ClickAction.ClickType.values()) {
         String[] translated = layout.getTranslationProvider().translate(clickType.getFormat());
         if (translated != null && translated.length != 0 && translated[0] != null) {
            int placeholder = translated[0].indexOf("%action%");
            if (placeholder > 0) {
               String prefix = translated[0].substring(0, placeholder);
               addPrefix(prefixes, prefix);
               addPrefix(prefixes, Ui.smallCaps(prefix));
            }
         }
      }

      return prefixes;
   }

   private static void addPrefix(Set<String> prefixes, String prefix) {
      String normalized = normalize(prefix);
      if (!normalized.isEmpty()) {
         prefixes.add(normalized);
      }
   }

   private static boolean isActionLine(String line, Set<String> prefixes) {
      String normalized = normalize(line);
      if (normalized.isEmpty()) {
         return false;
      }

      for (String prefix : prefixes) {
         if (normalized.startsWith(prefix)) {
            return true;
         }
      }

      return false;
   }

   private static boolean isBlank(String value) {
      return value == null || normalize(value).isEmpty();
   }

   private static String normalize(String value) {
      return COLOR_CODES.matcher(value).replaceAll("").trim().toLowerCase(Locale.ROOT);
   }

   private static List<ClickAction> normalizeActions(List<ClickAction> actions) {
      List<ClickAction> normalized = new ArrayList<>();
      Set<ClickAction.ClickType> usedClickTypes = new HashSet<>();

      for (ClickAction action : actions) {
         if (usedClickTypes.contains(action.getClickType())) {
            Utilities.getPluginInstance().getLogger().warn("Extra \"" + action.getClickType() + "\" action founded, ignoring");
         } else {
            usedClickTypes.add(action.getClickType());
            normalized.add(action);
         }
      }

      Collections.sort(normalized, Comparator.comparingInt(a -> a.getClickType().ordinal()));
      return normalized;
   }

   public static List<ClickAction> filterActions(List<ClickAction> actions, ClickAction.ClickType... types) {
      HashSet<ClickAction.ClickType> set = Sets.newHashSet(types);
      return actions.stream().filter(a -> set.contains(a.getClickType())).collect(Collectors.toList());
   }

   public static List<String> getActions(ContainerLayout layout, ClickAction... actions) {
      return applyActions(layout, Lists.newArrayList(), actions);
   }
}
