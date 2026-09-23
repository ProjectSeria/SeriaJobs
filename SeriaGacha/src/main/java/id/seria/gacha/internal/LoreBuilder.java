package id.seria.gacha.internal;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickActionsFacade;
import id.seria.gacha.lib.common.uicomponents.newest.layout.ContainerLayout;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import lombok.Generated;

public class LoreBuilder {
   private final ContainerLayout layout;
   private Translatable description = Translatable.literal(new ArrayList());
   private List<LoreBuilder.LoreAttribute> attributes = new ArrayList<>();
   private List<ClickAction> actions = new ArrayList<>();
   private Map<Supplier<Boolean>, Translatable> warnings = new LinkedHashMap<>();
   private Map<Supplier<Boolean>, Translatable> errors = new LinkedHashMap<>();

   public static LoreBuilder builder(ContainerLayout layout) {
      return new LoreBuilder(layout);
   }

   public LoreBuilder description(Translatable description) {
      this.description = description;
      return this;
   }

   public LoreBuilder description(String... description) {
      this.description = Translatable.literal(Arrays.asList(description));
      return this;
   }

   public LoreBuilder description(Collection<String> description) {
      this.description = Translatable.literal(new ArrayList<>(description));
      return this;
   }

   public LoreBuilder attribute(Translatable type, Object value) {
      this.attributes.add(new LoreBuilder.LoreAttribute(type, value, false));
      return this;
   }

   public LoreBuilder attribute(Translatable type, Object value, boolean breakLine) {
      this.attributes.add(new LoreBuilder.LoreAttribute(type, value, breakLine));
      return this;
   }

   public LoreBuilder actions(List<ClickAction> actions) {
      this.actions = actions;
      return this;
   }

   public LoreBuilder actions(ClickAction... actions) {
      this.actions = Arrays.asList(actions);
      return this;
   }

   public LoreBuilder error(Supplier<Boolean> conditionForError, Translatable message) {
      this.errors.put(conditionForError, Translatable.concatenate("\u00a7c", message));
      return this;
   }

   public LoreBuilder warning(Translatable message) {
      this.warnings.put(() -> true, Translatable.concatenate("\u00a7e\u00a7n", message));
      return this;
   }

   public LoreBuilder warning(Supplier<Boolean> conditionForError, Translatable message) {
      this.warnings.put(conditionForError, Translatable.concatenate("\u00a7e\u00a7n", message));
      return this;
   }

   public Translatable literal() {
      return Translatable.literal(this.build());
   }

   public List<String> build() {
      String[] desc = Translations.t(this.description);
      List<String> lore = new ArrayList<>(Arrays.asList(desc));
      if (!this.attributes.isEmpty()) {
         lore.add(" ");
      }

      for (LoreBuilder.LoreAttribute attr : this.attributes) {
         if (attr.breakLine) {
            lore.add("");
         }

         if (attr.value instanceof Boolean) {
            lore.add(
               Translations.t(
                  "layout.displays.state",
                  "%type%",
                  attr.type,
                  "%state%",
                  (Boolean)attr.value ? Translatable.key("labels.yes-word") : Translatable.key("labels.no-word")
               )[0]
            );
         } else {
            lore.add(
               Translations.t("layout.displays.value", "%type%", attr.type.postProcessor(StringUtil::humanize), "%value%", attr.value, "%state%", attr.value)[0]
            );
         }
      }

      for (Entry<Supplier<Boolean>, Translatable> entry : this.warnings.entrySet()) {
         if (entry.getKey().get()) {
            lore.addAll(Arrays.asList(Translations.t(entry.getValue())));
         }
      }

      if (!this.actions.isEmpty()) {
         lore = ClickActionsFacade.applyActions(this.layout, lore, this.actions);
      }

      for (Entry<Supplier<Boolean>, Translatable> entry : this.errors.entrySet()) {
         if (entry.getKey().get()) {
            lore.addAll(Arrays.asList(Translations.t(entry.getValue())));
         }
      }

      return lore;
   }

   @Generated
   private LoreBuilder(ContainerLayout layout) {
      this.layout = layout;
   }

   public static class LoreAttribute {
      private Translatable type;
      private Object value;
      private boolean breakLine;

      @Generated
      public Translatable getType() {
         return this.type;
      }

      @Generated
      public Object getValue() {
         return this.value;
      }

      @Generated
      public boolean isBreakLine() {
         return this.breakLine;
      }

      @Generated
      public LoreAttribute(Translatable type, Object value, boolean breakLine) {
         this.type = type;
         this.value = value;
         this.breakLine = breakLine;
      }
   }
}
