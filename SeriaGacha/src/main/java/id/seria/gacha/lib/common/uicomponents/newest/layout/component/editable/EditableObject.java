package id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.data.MutableData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.ClickableComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.modifiers.ObjectValueModifier;
import id.seria.gacha.lib.common.uicomponents.newest.limbo.ObjectValidationException;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Generated;

public abstract class EditableObject<T> extends ClickableComponent<EditableObject<T>> {
   private MutableData<T> value;
   protected Consumer<T> onValueChange;
   protected Function<T, Translatable> formatter = Translatable::literal;
   protected List<ObjectValueModifier<T>> modifiers = new ArrayList<>();
   protected final Translatable displayType;

   protected EditableObject(SlotCompound slot, ComponentData data, Translatable displayType, T value) {
      super(slot, data);
      this.displayType = displayType == null ? null : displayType.colorless();
      this.value = new MutableData<>(null, value);
   }

   protected EditableObject(SlotCompound slot, ComponentData data, Translatable displayType, MutableData<T> value) {
      super(slot, data);
      this.displayType = displayType == null ? null : displayType.colorless();
      this.value = value;
   }

   @Override
   public void onInitialize(ContainerView view) {
      if (this.value.getKey() != null) {
         T rawValue = (T)view.getMetadata("components.value." + this.value.getKey());
         if (rawValue == null && this.value.get() != null) {
            rawValue = this.value.get();
            this.getContainerView().setMetadata("components.value." + this.value.getKey(), rawValue, true);
         }

         if (rawValue != null || this.value.get() == null) {
            this.value.set(rawValue);
         }
      }
   }

   public void setRawValue(T value) {
      this.value.set(value);
      if (this.value.getKey() != null) {
         this.getContainerView().setMetadata("components.value." + this.value.getKey(), value, true);
      }
   }

   public void setValue(T object) throws Exception {
      this.setValue(object, true);
   }

   public void setValue(T object, boolean applyModifiers) throws Exception {
      T processedObject = applyModifiers ? this.applyValueModifiers(object) : object;
      if (this.value.get() == null || !this.value.get().equals(processedObject)) {
         this.setRawValue(processedObject);
         if (this.onValueChange != null) {
            this.onValueChange.accept(this.value.get());
         }
      }
   }

   public void safeSetValue(ContainerView view, T object, boolean updateView) {
      this.safeSetValue(view, object, updateView, true);
   }

   public void safeSetValue(ContainerView view, T object, boolean updateView, boolean applyModifiers) {
      try {
         this.setValue(object, applyModifiers);
         if (updateView) {
            view.updateView();
         }
      } catch (Exception ex) {
         ex.printStackTrace();
         view.getViewer().sendMessage(new String[]{"\u00a7cAn error has occurred:", ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage()});
      }
   }

   protected T applyValueModifiers(T value) {
      T copy = value;

      for (ObjectValueModifier<T> modifier : this.modifiers) {
         if (modifier != null) {
            copy = modifier.process(copy);
         }
      }

      return copy;
   }

   public EditableObject<T> addModifier(ObjectValueModifier<T> processor) {
      this.modifiers.add(processor);
      return this;
   }

   public EditableObject<T> addValidator(Function<T, Boolean> validation, Translatable error) {
      this.modifiers.add(value -> {
         boolean result = !validation.apply(value);
         if (result) {
            throw new ObjectValidationException(this._t(error)[0]);
         } else {
            return value;
         }
      });
      return this;
   }

   public EditableObject<T> swapClickAction(ClickAction.ClickType fromClickType, ClickAction.ClickType toClickType) {
      ClickAction from = this.getDisplayAction(fromClickType);
      BiConsumer<EditableObject<T>, ClickViewContext> consumerFrom = this.clickActions.get(new ClickAction(fromClickType, null));
      ClickAction to = this.getDisplayAction(toClickType);
      BiConsumer<EditableObject<T>, ClickViewContext> consumerTo = this.clickActions.get(new ClickAction(toClickType, null));
      if (to != null) {
         this.removeClickAction(toClickType);
      }

      if (from != null) {
         this.removeClickAction(fromClickType);
      }

      if (from != null) {
         from.setClickType(toClickType);
         this.clickActions.put(from, consumerFrom);
      }

      if (to != null) {
         to.setClickType(fromClickType);
         this.clickActions.put(to, consumerTo);
      }

      return this;
   }

   public EditableObject<T> setClickAction(ClickAction action) {
      this.removeClickAction(action.getClickType());
      this.clickActions.put(action, (object, context) -> {});
      return this;
   }

   public EditableObject<T> setClickAction(ClickAction action, Consumer<ClickViewContext> consumer) {
      this.removeClickAction(action.getClickType());
      if (consumer != null) {
         this.clickActions.put(action, (object, context) -> consumer.accept(context));
      }

      return this;
   }

   public EditableObject<T> setClickAction(ClickAction action, BiConsumer<EditableObject<T>, ClickViewContext> consumer) {
      this.removeClickAction(action.getClickType());
      if (consumer != null) {
         this.clickActions.put(action, consumer);
      }

      return this;
   }

   public EditableObject<T> removeClickAction(ClickAction.ClickType clickType) {
      this.clickActions.remove(new ClickAction(clickType, null));
      return this;
   }

   @Generated
   public MutableData<T> getValue() {
      return this.value;
   }

   @Generated
   public Consumer<T> getOnValueChange() {
      return this.onValueChange;
   }

   @Generated
   public Function<T, Translatable> getFormatter() {
      return this.formatter;
   }

   @Generated
   public List<ObjectValueModifier<T>> getModifiers() {
      return this.modifiers;
   }

   @Generated
   public Translatable getDisplayType() {
      return this.displayType;
   }

   @Generated
   public EditableObject<T> setOnValueChange(Consumer<T> onValueChange) {
      this.onValueChange = onValueChange;
      return this;
   }

   @Generated
   public EditableObject<T> setFormatter(Function<T, Translatable> formatter) {
      this.formatter = formatter;
      return this;
   }
}
