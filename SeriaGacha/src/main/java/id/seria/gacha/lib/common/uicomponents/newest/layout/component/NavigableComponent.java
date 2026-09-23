package id.seria.gacha.lib.common.uicomponents.newest.layout.component;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.ContainerLayout;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.Generated;
import org.apache.commons.lang.Validate;

public class NavigableComponent extends ClickableComponent<NavigableComponent> {
   private final ContainerLayout layoutTarget;
   private List<NavigableComponent.LoreAttribute> attributes;
   private final Map<String, Object> metadata = new LinkedHashMap<>();

   @Deprecated
   public NavigableComponent(ComponentData data, ContainerLayout layoutTarget) {
      this(new SlotCompound(data.getSlot()), data, layoutTarget);
   }

   public NavigableComponent(SlotCompound slot, ComponentData data, ContainerLayout layoutTarget) {
      super(slot, data);
      this.attributes = new ArrayList<>();
      this.layoutTarget = layoutTarget;
      this.setClickAction(ClickAction.NAVIGATE, (component, context) -> {
         if (this.layoutTarget != null) {
            this.layoutTarget.withData(this.metadata).open(context.getViewer());
         }
      });
   }

   public NavigableComponent clearAttributes() {
      this.attributes.clear();
      return this;
   }

   public NavigableComponent addAttribute(Translatable type, Object value) {
      this.attributes.add(new NavigableComponent.LoreAttribute(type, value, false));
      return this;
   }

   public NavigableComponent addAttribute(Translatable type, Object value, boolean breakLine) {
      this.attributes.add(new NavigableComponent.LoreAttribute(type, value, breakLine));
      return this;
   }

   public NavigableComponent withMetadata(String key, Object value) {
      if (key != null && !key.trim().isEmpty()) {
         if (value == null) {
            this.metadata.remove(key);
         } else {
            this.metadata.put(key, value);
         }

         return this;
      } else {
         return this;
      }
   }

   public Object getMetadata(String key) {
      return this.metadata.get(key);
   }

   public <T> T getMetadata(String key, Class<T> clazz) {
      Object value = this.metadata.get(key);
      if (value == null) {
         return null;
      }

      Validate.isTrue(clazz.isInstance(value), String.format("Cannot cast %s to %s", value.getClass().getSimpleName(), clazz.getSimpleName()));
      return clazz.cast(value);
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext context) {
      if (this.attributes.isEmpty()) {
         return new ArrayList<>();
      }

      List<String> display = new ArrayList<>();

      for (NavigableComponent.LoreAttribute attr : this.attributes) {
         if (attr.breakLine) {
            display.add("");
         }

         if (attr.value instanceof Boolean) {
            display.add(
               this._t(
                  "layout.displays.state",
                  "%type%",
                  attr.type,
                  "%state%",
                  (Boolean)attr.value ? Translatable.key("labels.yes-word") : Translatable.key("labels.no-word")
               )[0]
            );
         } else {
            display.add(this._t("layout.displays.value", "%type%", attr.type, "%value%", attr.value, "%state%", attr.value)[0]);
         }
      }

      return display;
   }

   @Override
   public ClickableComponent setClickAction(ClickAction action, Consumer<ClickViewContext> consumer) {
      this.clickActions.put(action, (object, context) -> consumer.accept(context));
      return this;
   }

   @Override
   public ClickableComponent setClickAction(ClickAction action, BiConsumer<NavigableComponent, ClickViewContext> consumer) {
      this.clickActions.put(action, consumer);
      return this;
   }

   @Override
   public ClickableComponent removeClickAction(ClickAction.ClickType clickType) {
      this.clickActions.remove(new ClickAction(clickType, null));
      return this;
   }

   public ClickableComponent setDisplayAction(ClickAction action) {
      for (ClickAction original : this.clickActions.keySet()) {
         if (original.getClickType() == action.getClickType()) {
            original.setDisplayAction(action.getDisplayAction());
            break;
         }
      }

      return this;
   }

   @Generated
   public ContainerLayout getLayoutTarget() {
      return this.layoutTarget;
   }

   @Generated
   public List<NavigableComponent.LoreAttribute> getAttributes() {
      return this.attributes;
   }

   @Generated
   public Map<String, Object> getMetadata() {
      return this.metadata;
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
