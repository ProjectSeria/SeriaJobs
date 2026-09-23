package id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.complex;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickActionsFacade;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.ClickableComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.modifiers.ObjectValueModifier;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.objects.Pair;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Generated;
import lombok.NonNull;
import org.bukkit.entity.Player;

@Deprecated
public class EditablePair<T, U> extends ClickableComponent<EditablePair<T, U>> {
   private Translatable leftDisplayType;
   private Translatable rightDisplayType;
   private T leftValue;
   private U rightValue;
   protected BiConsumer<ContainerView, T> onLeftValueChange;
   protected BiConsumer<ContainerView, U> onRightValueChange;
   protected Function<T, Translatable> leftFormatter = Translatable::literal;
   protected Function<U, Translatable> rightFormatter = Translatable::literal;
   protected List<ObjectValueModifier<T>> leftModifiers = new ArrayList<>();
   protected List<ObjectValueModifier<U>> rightModifiers = new ArrayList<>();
   protected Map<ClickAction, BiConsumer<EditablePair<T, U>, ClickViewContext>> clickActions = new HashMap<>();

   public EditablePair(
      SlotCompound compound, ComponentData data, Translatable leftDisplayType, @NonNull T leftValue, Translatable rightDisplayType, @NonNull U rightValue
   ) {
      super(compound, data);
      if (leftValue == null) {
         throw new NullPointerException("leftValue is marked non-null but is null");
      }

      if (rightValue == null) {
         throw new NullPointerException("rightValue is marked non-null but is null");
      }

      if ((leftValue instanceof Boolean || leftValue instanceof String) && (rightValue instanceof Boolean || rightValue instanceof String)) {
         this.leftDisplayType = leftDisplayType;
         this.rightDisplayType = rightDisplayType;
         this.leftValue = leftValue;
         this.rightValue = rightValue;
         this.setupLeftValue();
         this.setupRightValue();
      } else {
         throw new IllegalArgumentException("Invalid types for left value and right value (Only allowed String or Boolean)");
      }
   }

   @Deprecated
   private void setupLeftValue() {
      if (this.leftValue.getClass() == String.class) {
         this.setLeftFormatter(value -> value != null && !((String)value).isEmpty() ? Translatable.literal(value) : Translatable.key("labels.empty"));
         this.setClickAction(ClickAction.CHANGE(ClickAction.ClickType.LEFT, this.leftDisplayType), context -> {
            Player player = context.getViewer();
            player.sendMessage(String.format("\u00a7eWrite the new %s in the chat.", this._t(this.leftDisplayType)[0].toLowerCase()));
            player.closeInventory();
            TextReader.askForInput(player, input -> {
               if (input != null) {
                  try {
                     this.setLeftValue(context.getContainerView(), (T)input);
                     player.sendMessage(String.format("\u00a7a%s updated successfully.", StringUtil.humanize(this._t(this.leftDisplayType)[0])));
                  } catch (Exception e) {
                     player.sendMessage(new String[]{"\u00a7cAn error has occurred:", e.getMessage()});
                     return;
                  }
               }

               context.getContainerView().updateView();
            });
         });
      } else if (this.leftValue.getClass() == Boolean.class) {
         this.setLeftFormatter(value -> (Boolean)value ? Translatable.key("labels.yes-word") : Translatable.key("labels.no-word"));
         this.setClickAction(new ClickAction(ClickAction.ClickType.LEFT, Translatable.key("labels.switch-state")), context -> {
            try {
               this.setLeftValue(context.getContainerView(), (T)Boolean.valueOf(!(Boolean)this.getLeftValue()));
            } catch (Exception e) {
               context.getViewer().sendMessage(new String[]{"\u00a7cAn error has occurred:", e.getMessage()});
            }

            context.getContainerView().updateView();
         });
      }
   }

   @Deprecated
   private void setupRightValue() {
      if (this.rightValue.getClass() == String.class) {
         this.setRightFormatter(value -> value != null && !((String)value).isEmpty() ? Translatable.literal(value) : Translatable.key("labels.empty"));
         this.setClickAction(ClickAction.CHANGE(ClickAction.ClickType.RIGHT, this.rightDisplayType), context -> {
            Player player = context.getViewer();
            player.sendMessage(String.format("\u00a7eWrite the new %s in the chat.", this._t(this.rightDisplayType)[0].toLowerCase()));
            player.closeInventory();
            TextReader.askForInput(player, input -> {
               if (input != null) {
                  try {
                     this.setRightValue(context.getContainerView(), (U)input);
                     player.sendMessage(String.format("\u00a7a%s updated successfully.", StringUtil.humanize(this._t(this.rightDisplayType)[0])));
                  } catch (Exception e) {
                     player.sendMessage(new String[]{"\u00a7cAn error has occurred:", e.getMessage()});
                     return;
                  }
               }

               context.getContainerView().updateView();
            });
         });
      } else if (this.rightValue.getClass() == Boolean.class) {
         this.setRightFormatter(value -> (Boolean)value ? Translatable.key("labels.yes-word") : Translatable.key("labels.no-word"));
         this.setClickAction(new ClickAction(ClickAction.ClickType.RIGHT, Translatable.key("labels.switch-state")), context -> {
            try {
               this.setRightValue(context.getContainerView(), (U)Boolean.valueOf(!(Boolean)this.getRightValue()));
            } catch (Exception e) {
               context.getViewer().sendMessage(new String[]{"\u00a7cAn error has occurred:", e.getMessage()});
            }

            context.getContainerView().updateView();
         });
      }
   }

   @Override
   public void onPrepare(ContainerView ignored) {
   }

   @Override
   public void onRender(RenderViewContext context) {
      VirtualItem item = new VirtualItem();
      item.withItem(this.getData().getItem()).withDisplayName("\u00a7a" + this._t(this.getData().getTitle())[0]);
      List<String> lore = new ArrayList<>();

      for (String line : this._t(this.getData().getDescription())) {
         lore.add("\u00a77" + line);
      }

      List<String> display = this.getRenderDisplay(context);
      if (!display.isEmpty()) {
         lore.add("");
         lore.addAll(display);
      }

      lore = ClickActionsFacade.applyActions(this.getContainerLayout(), lore, this.clickActions.keySet());
      if (this.warnings != null) {
         for (Pair<Supplier<Boolean>, Translatable> entry : this.warnings) {
            if (entry.first.get()) {
               lore.add(String.format("\u00a7c%s", this._t(entry.second)[0]));
            }
         }
      }

      item.withLore(lore);
      item.withSlots(this.getSlot());
      item.render(context);
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext context) {
      return Arrays.asList(
         this._t("layout.displays.value", "%type%", this.leftDisplayType, "%value%", this.leftFormatter.apply(this.leftValue))[0],
         this._t("layout.displays.value", "%type%", this.rightDisplayType, "%value%", this.rightFormatter.apply(this.rightValue))[0]
      );
   }

   @Override
   public final void onClick(ClickViewContext context) {
      BiConsumer<EditablePair<T, U>, ClickViewContext> consumer = this.clickActions.get(new ClickAction(ClickAction.ClickType.from(context.getClick())));
      if (consumer != null) {
         consumer.accept(this, context);
      }
   }

   public void setLeftValue(ContainerView view, T object) throws Exception {
      T processedObject = this.applyLeftValueModifiers(object);
      if (this.leftValue == null || !this.leftValue.equals(processedObject)) {
         this.leftValue = processedObject;
         if (this.onLeftValueChange != null) {
            this.onLeftValueChange.accept(view, this.leftValue);
         }
      }
   }

   private T applyLeftValueModifiers(T value) {
      T copy = value;

      for (ObjectValueModifier<T> modifier : this.leftModifiers) {
         if (modifier != null) {
            copy = modifier.process(copy);
         }
      }

      return copy;
   }

   public EditablePair<T, U> addLeftModifier(ObjectValueModifier<T> processor) {
      this.leftModifiers.add(processor);
      return this;
   }

   public EditablePair<T, U> addLeftValidator(Function<T, Boolean> validation, Translatable error) {
      this.leftModifiers.add(value -> {
         boolean result = !validation.apply(value);
         if (result) {
            throw new IllegalArgumentException(this._t(error)[0]);
         } else {
            return value;
         }
      });
      return this;
   }

   public void setRightValue(ContainerView view, U object) throws Exception {
      U processedObject = this.applyRightValueModifiers(object);
      if (this.rightValue == null || !this.rightValue.equals(processedObject)) {
         this.rightValue = processedObject;
         if (this.onRightValueChange != null) {
            this.onRightValueChange.accept(view, this.rightValue);
         }
      }
   }

   private U applyRightValueModifiers(U value) {
      U copy = value;

      for (ObjectValueModifier<U> modifier : this.rightModifiers) {
         if (modifier != null) {
            copy = modifier.process(copy);
         }
      }

      return copy;
   }

   public EditablePair<T, U> addRightModifier(ObjectValueModifier<U> processor) {
      this.rightModifiers.add(processor);
      return this;
   }

   public EditablePair<T, U> addRightValidator(Function<U, Boolean> validation, Translatable error) {
      this.rightModifiers.add(value -> {
         boolean result = !validation.apply(value);
         if (result) {
            throw new IllegalArgumentException(this._t(error)[0]);
         } else {
            return value;
         }
      });
      return this;
   }

   public EditablePair<T, U> setClickAction(ClickAction action, Consumer<ClickViewContext> consumer) {
      this.clickActions.put(action, (object, context) -> consumer.accept(context));
      return this;
   }

   public EditablePair<T, U> setClickAction(ClickAction action, BiConsumer<EditablePair<T, U>, ClickViewContext> consumer) {
      this.clickActions.put(action, consumer);
      return this;
   }

   public EditablePair<T, U> setDisplayAction(ClickAction action) {
      for (ClickAction original : this.clickActions.keySet()) {
         if (original.getClickType() == action.getClickType()) {
            original.setDisplayAction(action.getDisplayAction());
            break;
         }
      }

      return this;
   }

   @Generated
   public Translatable getLeftDisplayType() {
      return this.leftDisplayType;
   }

   @Generated
   public Translatable getRightDisplayType() {
      return this.rightDisplayType;
   }

   @Generated
   public T getLeftValue() {
      return this.leftValue;
   }

   @Generated
   public U getRightValue() {
      return this.rightValue;
   }

   @Generated
   public BiConsumer<ContainerView, T> getOnLeftValueChange() {
      return this.onLeftValueChange;
   }

   @Generated
   public BiConsumer<ContainerView, U> getOnRightValueChange() {
      return this.onRightValueChange;
   }

   @Generated
   public Function<T, Translatable> getLeftFormatter() {
      return this.leftFormatter;
   }

   @Generated
   public Function<U, Translatable> getRightFormatter() {
      return this.rightFormatter;
   }

   @Generated
   public List<ObjectValueModifier<T>> getLeftModifiers() {
      return this.leftModifiers;
   }

   @Generated
   public List<ObjectValueModifier<U>> getRightModifiers() {
      return this.rightModifiers;
   }

   @Generated
   @Override
   public Map<ClickAction, BiConsumer<EditablePair<T, U>, ClickViewContext>> getClickActions() {
      return this.clickActions;
   }

   @Generated
   public EditablePair<T, U> setLeftDisplayType(Translatable leftDisplayType) {
      this.leftDisplayType = leftDisplayType;
      return this;
   }

   @Generated
   public EditablePair<T, U> setRightDisplayType(Translatable rightDisplayType) {
      this.rightDisplayType = rightDisplayType;
      return this;
   }

   @Generated
   public EditablePair<T, U> setOnLeftValueChange(BiConsumer<ContainerView, T> onLeftValueChange) {
      this.onLeftValueChange = onLeftValueChange;
      return this;
   }

   @Generated
   public EditablePair<T, U> setOnRightValueChange(BiConsumer<ContainerView, U> onRightValueChange) {
      this.onRightValueChange = onRightValueChange;
      return this;
   }

   @Generated
   public EditablePair<T, U> setLeftFormatter(Function<T, Translatable> leftFormatter) {
      this.leftFormatter = leftFormatter;
      return this;
   }

   @Generated
   public EditablePair<T, U> setRightFormatter(Function<U, Translatable> rightFormatter) {
      this.rightFormatter = rightFormatter;
      return this;
   }
}
