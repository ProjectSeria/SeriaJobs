package id.seria.gacha.lib.common.uicomponents.newest.layout.component;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import lombok.Generated;
import org.apache.commons.lang.Validate;

public class LinkedComponent extends ClickableComponent<LinkedComponent> {
   private static final Map<String, BiFunction<ContainerView, LinkedComponent, DisplayableComponent>> PROVIDERS = new ConcurrentHashMap<>();
   private final String linkedKey;
   private List<LinkedComponent.LoreAttribute> attributes = new ArrayList<>();
   private final Map<ContainerView, DisplayableComponent> resolvedPerView = Collections.synchronizedMap(new WeakHashMap<>());
   private final Map<String, Object> metadata = new LinkedHashMap<>();

   public LinkedComponent(SlotCompound slot, ComponentData data, String linkedKey) {
      super(slot, data);
      Validate.notNull(linkedKey, "linkedKey cannot be null");
      this.linkedKey = normalize(linkedKey);
      this.setLocked(value -> !hasProvider(this.linkedKey));
   }

   public static void register(String key, BiFunction<ContainerView, LinkedComponent, DisplayableComponent> provider) {
      Validate.notNull(key, "key cannot be null");
      Validate.notNull(provider, "provider cannot be null");
      PROVIDERS.put(normalize(key), provider);
   }

   public static void unregister(String key) {
      if (key != null) {
         PROVIDERS.remove(normalize(key));
      }
   }

   public static boolean hasProvider(String key) {
      return key == null ? false : PROVIDERS.containsKey(normalize(key));
   }

   public static void clearProviders() {
      PROVIDERS.clear();
   }

   public LinkedComponent setFallbackError(Translatable message) {
      this.addError(() -> message != null && !hasProvider(this.linkedKey), message);
      return this;
   }

   public LinkedComponent withMetadata(String key, Object value) {
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

   public LinkedComponent clearAttributes() {
      this.attributes.clear();
      return this;
   }

   public LinkedComponent addAttribute(Translatable type, Object value) {
      this.attributes.add(new LinkedComponent.LoreAttribute(type, value, false));
      return this;
   }

   public LinkedComponent addAttribute(Translatable type, Object value, boolean breakLine) {
      this.attributes.add(new LinkedComponent.LoreAttribute(type, value, breakLine));
      return this;
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
   public void onInitialize(ContainerView view) {
      DisplayableComponent resolved = this.resolveAndLink(view);
      if (resolved != this) {
         resolved.onInitialize(view);
      }
   }

   @Override
   public void onPrepare(ContainerView view) {
      DisplayableComponent resolved = this.resolveAndLink(view);
      if (resolved != this) {
         resolved.onPrepare(view);
      }
   }

   @Override
   public void onPreRender(RenderViewContext context) {
      DisplayableComponent resolved = this.getResolved(context.getContainerView());
      if (resolved != this) {
         this.ensureLinkedSlot(resolved);
         resolved.onPreRender(context);
      }
   }

   @Override
   public void onRender(RenderViewContext context) {
      DisplayableComponent resolved = this.getResolved(context.getContainerView());
      if (resolved == this) {
         super.onRender(context);
      } else {
         this.ensureLinkedSlot(resolved);
         resolved.onRender(context);
      }
   }

   @Override
   public void onClick(ClickViewContext context) {
      DisplayableComponent resolved = this.getResolved(context.getContainerView());
      if (resolved == this) {
         super.onClick(context);
      } else {
         this.ensureLinkedSlot(resolved);
         if (resolved instanceof ClickableComponent) {
            ((ClickableComponent)resolved).onClick(context);
         }
      }
   }

   @Override
   public boolean isClicking(ClickViewContext context) {
      return this.getSlot().contains(context.getRawSlot());
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext context) {
      if (this.attributes.isEmpty()) {
         return new ArrayList<>();
      }

      List<String> display = new ArrayList<>();

      for (LinkedComponent.LoreAttribute attr : this.attributes) {
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

   private DisplayableComponent getResolved(ContainerView view) {
      DisplayableComponent resolved = this.resolvedPerView.get(view);
      if (resolved == null) {
         resolved = this.resolveAndLink(view);
      }

      return resolved;
   }

   private DisplayableComponent resolveAndLink(ContainerView view) {
      DisplayableComponent resolved = this.resolveDelegate(view);
      this.ensureLinkedView(view, resolved);
      this.ensureLinkedSlot(resolved);
      this.resolvedPerView.put(view, resolved);
      return resolved;
   }

   private DisplayableComponent resolveDelegate(ContainerView view) {
      BiFunction<ContainerView, LinkedComponent, DisplayableComponent> provider = PROVIDERS.get(this.linkedKey);
      if (provider == null) {
         return this;
      }

      try {
         DisplayableComponent resolved = provider.apply(view, this);
         return resolved == null ? this : resolved;
      } catch (Exception ignored) {
         return this;
      }
   }

   private void ensureLinkedView(ContainerView view, DisplayableComponent resolved) {
      if (resolved != null && resolved != this) {
         try {
            resolved.getContainerView();
         } catch (Exception ignored) {
            resolved.linkView(view, this.getId() + ".linked");
         }
      }
   }

   private void ensureLinkedSlot(DisplayableComponent resolved) {
      if (resolved != null && resolved != this) {
         resolved.setSlot(this.getSlot());
      }
   }

   private static String normalize(String key) {
      return key.trim().toLowerCase();
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
