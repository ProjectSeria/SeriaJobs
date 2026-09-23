package id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.EditableObject;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Generated;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

public class EditableListMultiOption<T> extends EditableObject<List<Integer>> {
   private static final String SELECTED_INDEX_KEY = "components.%s.selected-index";
   protected final List<T> options;
   protected Consumer<List<T>> onOptionChange;
   protected Function<T, Translatable> itemFormatter = value -> Translatable.literal(value.toString());
   protected Function<T, Boolean> itemSelection = value -> true;

   public EditableListMultiOption(SlotCompound slot, ComponentData data, List<T> options, Collection<T> defaultOptions) {
      super(slot, data, null, defaultOptions.stream().map(options::indexOf).distinct().collect(Collectors.toList()));
      Validate.isTrue(!options.isEmpty(), "Options need to atleast have 1 element");
      this.options = new ArrayList<>(options);
      this.setClickAction(new ClickAction(ClickAction.ClickType.LEFT, Translatable.key("labels.swap-option")), context -> {
         int selectedIndex = this.getSelectedIndex(context.getContainerView());
         int i = 0;

         int index;
         for (index = this.normalizeIndex(selectedIndex + 1); i < this.options.size() && !this.itemSelection.apply(this.options.get(index)); i++) {
            index = this.normalizeIndex(index + 1);
         }

         if (i != 10) {
            this.setSelectedIndex(context.getContainerView(), index);
            context.getContainerView().updateView();
         }
      });
      this.setClickAction(ClickAction.SELECT(ClickAction.ClickType.RIGHT, Translatable.key("labels.option")), context -> {
         Integer selectedIndex = this.normalizeIndex(this.getSelectedIndex(context.getContainerView()));
         List<Integer> value = new ArrayList<>(this.getValue().get());
         if (value.contains(selectedIndex)) {
            value.remove(selectedIndex);
         } else {
            value.add(selectedIndex);
         }

         this.setValue(value);
         context.getContainerView().updateView();
      });
   }

   public int getSelectedIndex(ContainerView view) {
      Integer index = view.getMetadataOrNull(String.format("components.%s.selected-index", this.id), Integer.class);
      return index == null ? 0 : index;
   }

   public void setSelectedIndex(ContainerView view, Integer selectedIndex) {
      view.setMetadata(String.format("components.%s.selected-index", this.id), selectedIndex, true);
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

   public List<T> getCurrentOption() {
      return this.getValue().get().stream().map(o -> this.options.get(MathUtil.clamp(o, 0, this.options.size() - 1))).collect(Collectors.toList());
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext context) {
      List<String> lore = new ArrayList<>();
      int selectedIndex = this.normalizeIndex(this.getSelectedIndex(context.getContainerView()));
      int i = 0;

      for (T target : this.options) {
         if (this.itemSelection.apply(target)) {
            if (selectedIndex == i) {
               if (this.getValue().get().contains(i)) {
                  lore.add(String.format("\u00a7a\u2714 %s", this.getFormattedOption(target, true)));
               } else {
                  lore.add(String.format("\u00a7c\u274c %s", this.getFormattedOption(target, false)));
               }
            } else if (this.getValue().get().contains(i)) {
               lore.add(String.format("\u00a72\u2714 %s", this.getFormattedOption(target, true)));
            } else {
               lore.add(String.format("\u00a74\u274c %s", this.getFormattedOption(target, false)));
            }
         } else if (this.getValue().get().contains(i)) {
            lore.add(String.format("\u00a78\u2714 %s", this.getFormattedOption(target, true)));
         } else {
            lore.add(String.format("\u00a78\u274c %s", this.getFormattedOption(target, false)));
         }

         i++;
      }

      return lore;
   }

   protected String getFormattedOption(T object, boolean selected) {
      return selected
         ? this._t(this.itemFormatter.apply(object))[0]
         : this._t(this.itemFormatter.apply(object))[0].replace("\u00a77", "\u00a78").replace("\u00a7a", "\u00a72");
   }

   public void setValue(List<Integer> object) {
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
   public Function<List<Integer>, Translatable> getFormatter() {
      throw new NotImplementedException("Not supported");
   }

   public EditableListMultiOption<T> setFormatter(Function<List<Integer>, Translatable> ignored) {
      throw new NotImplementedException("Not supported");
   }

   @Generated
   public List<T> getOptions() {
      return this.options;
   }

   @Generated
   public Consumer<List<T>> getOnOptionChange() {
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
   public EditableListMultiOption<T> setOnOptionChange(Consumer<List<T>> onOptionChange) {
      this.onOptionChange = onOptionChange;
      return this;
   }

   @Generated
   public EditableListMultiOption<T> setItemFormatter(Function<T, Translatable> itemFormatter) {
      this.itemFormatter = itemFormatter;
      return this;
   }

   @Generated
   public EditableListMultiOption<T> setItemSelection(Function<T, Boolean> itemSelection) {
      this.itemSelection = itemSelection;
      return this;
   }
}
