package id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.EditableObject;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Generated;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

public class EditableListSingleOption<T> extends EditableObject<Integer> {
   protected List<T> options;
   protected Consumer<T> onOptionChange;
   protected Function<T, Translatable> itemFormatter = value -> Translatable.literal(value.toString());
   protected Function<T, Boolean> itemSelection = value -> true;

   @Deprecated
   public EditableListSingleOption(ComponentData data, List<T> options, Supplier<T> defaultOption) {
      this(new SlotCompound(data.getSlot()), data, options, defaultOption.get());
   }

   public EditableListSingleOption(SlotCompound slot, ComponentData data, List<T> options, T defaultOption) {
      super(slot, data, null, Math.max(0, options.indexOf(defaultOption)));
      Validate.isTrue(!options.isEmpty(), "Options need to atleast have 1 element");
      this.options = new ArrayList<>(options);
      this.setClickAction(new ClickAction(ClickAction.ClickType.LEFT_AND_RIGHT, Translatable.key("labels.swap-option")), context -> {
         int inc = context.isLeftClick() ? 1 : -1;
         int i = 0;

         int index;
         for (index = this.normalizeIndex(this.getValue().get() + inc); i < this.options.size() && !this.itemSelection.apply(this.options.get(index)); i++) {
            index = this.normalizeIndex(index + inc);
         }

         if (i != 10) {
            this.setValue(index);
            context.getContainerView().updateView();
         }
      });
   }

   protected int normalizeIndex(int index) {
      while (index < 0) {
         index += this.options.size();
      }

      while (index > this.options.size() - 1) {
         index -= this.options.size();
      }

      return index;
   }

   public T getCurrentOption() {
      return this.options.get(MathUtil.clamp(this.getValue().get(), 0, this.options.size() - 1));
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext context) {
      List<String> lore = new ArrayList<>();
      int currentValueIndex = this.getValue().get();
      if (this.options.size() >= 15) {
         int minRange = currentValueIndex - 5;
         if (minRange < 0) {
            minRange = 0;
         }

         if (minRange > 0) {
            lore.add("\u00a78...");
         }

         int maxRange = minRange + 15;
         if (maxRange > this.options.size()) {
            maxRange = this.options.size();
            minRange = this.options.size() - 15;
         }

         int i = minRange;

         for (T target : this.options.subList(minRange, maxRange)) {
            if (this.itemSelection.apply(target)) {
               if (i == currentValueIndex) {
                  lore.add(String.format("\u00a7a\u25aa %s", this.getFormattedOption(target, true)));
               } else {
                  lore.add(String.format("\u00a77\u25aa %s", this.getFormattedOption(target, false)));
               }
            } else if (i == currentValueIndex) {
               lore.add(String.format("\u00a78\u25aa %s", this.getFormattedOption(target, true)));
            } else {
               lore.add(String.format("\u00a78\u25aa %s", this.getFormattedOption(target, false)));
            }

            i++;
         }

         if (maxRange < this.options.size()) {
            lore.add("\u00a78...");
         }
      } else {
         int i = 0;

         for (T target : this.options) {
            if (this.itemSelection.apply(target)) {
               if (i == currentValueIndex) {
                  lore.add(String.format("\u00a7a\u25aa %s", this.getFormattedOption(target, true)));
               } else {
                  lore.add(String.format("\u00a77\u25aa %s", this.getFormattedOption(target, false)));
               }
            } else if (i == currentValueIndex) {
               lore.add(String.format("\u00a78\u25aa %s", this.getFormattedOption(target, true)));
            } else {
               lore.add(String.format("\u00a78\u25aa %s", this.getFormattedOption(target, false)));
            }

            i++;
         }
      }

      return lore;
   }

   protected String getFormattedOption(T object, boolean selected) {
      return selected
         ? this._t(this.itemFormatter.apply(object))[0]
         : this._t(this.itemFormatter.apply(object))[0].replace("\u00a77", "\u00a78").replace("\u00a7a", "\u00a72");
   }

   public void setValue(Integer object) {
      if (this.getValue().get() == null || !this.getValue().get().equals(object)) {
         this.setRawValue(object);
         if (this.onValueChange != null) {
            this.onValueChange.accept(this.getValue().get());
         }

         if (this.onOptionChange != null) {
            this.onOptionChange.accept(this.getCurrentOption());
         }
      }
   }

   @Override
   public Function<Integer, Translatable> getFormatter() {
      throw new NotImplementedException("Not supported");
   }

   public EditableListSingleOption<Integer> setFormatter(Function<Integer, Translatable> ignored) {
      throw new NotImplementedException("Not supported");
   }

   @Generated
   public List<T> getOptions() {
      return this.options;
   }

   @Generated
   public Consumer<T> getOnOptionChange() {
      return this.onOptionChange;
   }

   @Generated
   public Function<T, Translatable> getItemFormatter() {
      return this.itemFormatter;
   }

   @Generated
   public Function<T, Boolean> getItemSelection() {
      return this.itemSelection;
   }

   @Generated
   public EditableListSingleOption<T> setOnOptionChange(Consumer<T> onOptionChange) {
      this.onOptionChange = onOptionChange;
      return this;
   }

   @Generated
   public EditableListSingleOption<T> setItemFormatter(Function<T, Translatable> itemFormatter) {
      this.itemFormatter = itemFormatter;
      return this;
   }

   @Generated
   public EditableListSingleOption<T> setItemSelection(Function<T, Boolean> itemSelection) {
      this.itemSelection = itemSelection;
      return this;
   }
}
