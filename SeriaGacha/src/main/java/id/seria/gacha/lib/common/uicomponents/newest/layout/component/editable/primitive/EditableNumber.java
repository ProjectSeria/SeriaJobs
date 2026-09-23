package id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.data.MutableData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.EditableObject;
import id.seria.gacha.lib.common.uicomponents.newest.limbo.ObjectValidationException;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import id.seria.gacha.lib.common.utils.utility.Validate;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Generated;
import org.bukkit.entity.Player;

public class EditableNumber extends EditableObject<Number> {
   private static DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("#.##");
   private Function<EditableNumber, Number> normalStep;
   private Function<EditableNumber, Number> shiftStep;
   private Runnable inputTypeSetup;
   private boolean chatBasedInput;

   @Deprecated
   public EditableNumber(ComponentData data, Supplier<Number> object) {
      this(new SlotCompound(data.getSlot()), data, data.getTitle(), object.get());
   }

   public EditableNumber(SlotCompound slot, ComponentData data, Number object) {
      this(slot, data, new MutableData<>(null, object));
   }

   public EditableNumber(SlotCompound slot, ComponentData data, Translatable displayType, Number object) {
      this(slot, data, displayType, new MutableData<>(null, object));
   }

   public EditableNumber(SlotCompound slot, ComponentData data, MutableData<Number> mutableData) {
      this(slot, data, data.getTitle(), mutableData);
   }

   public EditableNumber(SlotCompound slot, ComponentData data, Translatable displayType, MutableData<Number> object) {
      super(slot, data, displayType, object);
      this.defaultInput();
      if (this.getValue().get() == null) {
         object.set(0);
      }

      this.setFormatter(this::formatNumber);
      if (this.isClassDecimal(this.getValue().get())) {
         this.normalStep = dummy -> 0.1;
         this.shiftStep = dummy -> 1.0;
      } else {
         this.normalStep = dummy -> 1.0;
         this.shiftStep = dummy -> 10.0;
      }
   }

   public EditableNumber defaultInput() {
      this.chatBasedInput = false;
      return this;
   }

   public EditableNumber chatInput() {
      this.chatBasedInput = true;
      return this;
   }

   @Override
   public void onPrepare(ContainerView view) {
      super.onPrepare(view);
      this.createClickActions();
   }

   public EditableNumber setNormalStep(Function<EditableNumber, Number> normalStep) {
      Validate.isTrue(!this.chatBasedInput, "Chat base input doesn't support steps");
      this.normalStep = normalStep;
      Number computedStep = normalStep.apply(this);
      this.replaceDisplayAction(ClickAction.ADD(ClickAction.ClickType.LEFT, this.formatNumber(computedStep)));
      this.replaceDisplayAction(ClickAction.REMOVE(ClickAction.ClickType.RIGHT, this.formatNumber(computedStep)));
      return this;
   }

   public EditableNumber setShiftStep(Function<EditableNumber, Number> shiftStep) {
      Validate.isTrue(!this.chatBasedInput, "Chat base input doesn't support steps");
      this.shiftStep = shiftStep;
      Number computedStep = shiftStep.apply(this);
      this.replaceDisplayAction(ClickAction.ADD(ClickAction.ClickType.SHIFT_LEFT, this.formatNumber(computedStep)));
      this.replaceDisplayAction(ClickAction.REMOVE(ClickAction.ClickType.SHIFT_RIGHT, this.formatNumber(computedStep)));
      return this;
   }

   private void createClickActions() {
      if (this.chatBasedInput) {
         this.setClickAction(
            ClickAction.CHANGE(ClickAction.ClickType.LEFT, this.displayType),
            context -> {
               Player player = context.getViewer();
               player.sendMessage(String.format("\u00a7eWrite the new %s in the chat.", this._t(this.getData().getTitle())[0].toLowerCase()));
               player.closeInventory();
               TextReader.askForInput(
                  player,
                  input -> {
                     if (input != null) {
                        try {
                           try {
                              Number oldValue = this.getValue().get();
                              Double newValue = Double.parseDouble(input);
                              if (oldValue.getClass().equals(Integer.class)) {
                                 this.setValue(newValue.intValue());
                              } else if (oldValue.getClass().equals(Long.class)) {
                                 this.setValue(newValue.longValue());
                              } else if (oldValue.getClass().equals(Double.class)) {
                                 this.setValue(newValue);
                              } else {
                                 if (!oldValue.getClass().equals(Float.class)) {
                                    throw new IllegalStateException("Unknown field type");
                                 }

                                 this.setValue(newValue.floatValue());
                              }

                              player.sendMessage(
                                 String.format("\u00a7a%s successfully updated to \"%s\u00a7a\"!", this._t(this.getData().getTitle().colorless())[0], input)
                              );
                           } catch (NumberFormatException ex) {
                              throw new ObjectValidationException("\u00a7cInvalid number!");
                           }
                        } catch (ObjectValidationException e) {
                           player.sendMessage(e.getMessage());
                        } catch (Exception e) {
                           player.sendMessage(new String[]{"\u00a7cAn error has occurred:", e.getMessage()});
                           return;
                        }
                     }

                     context.getContainerView().updateView();
                  }
               );
            }
         );
      } else {
         Consumer<ClickViewContext> consumer = context -> {
            Number normalStepx = this.normalStep.apply(this);
            Number shiftStepx = this.shiftStep.apply(this);
            Number a = context.isShiftClick() ? shiftStepx : normalStepx;
            if (context.isLeftClick()) {
               this.updateValue(context.getContainerView(), normalize(sum(this.getValue().get(), a)));
            } else if (context.isRightClick()) {
               this.updateValue(context.getContainerView(), normalize(subtract(this.getValue().get(), a)));
            }

            context.getContainerView().updateView();
         };
         Number normalStep = this.normalStep.apply(this);
         Number shiftStep = this.shiftStep.apply(this);
         this.setClickAction(ClickAction.ADD(ClickAction.ClickType.LEFT, this.formatNumber(normalStep)), consumer);
         this.setClickAction(ClickAction.REMOVE(ClickAction.ClickType.RIGHT, this.formatNumber(normalStep)), consumer);
         this.setClickAction(ClickAction.ADD(ClickAction.ClickType.SHIFT_LEFT, this.formatNumber(shiftStep)), consumer);
         this.setClickAction(ClickAction.REMOVE(ClickAction.ClickType.SHIFT_RIGHT, this.formatNumber(shiftStep)), consumer);
      }
   }

   private void updateValue(ContainerView view, Number newValue) {
      try {
         if (newValue.getClass().equals(Integer.class)) {
            this.setValue(newValue.intValue());
         } else if (newValue.getClass().equals(Long.class)) {
            this.setValue(newValue.longValue());
         } else if (newValue.getClass().equals(Double.class)) {
            this.setValue(newValue.doubleValue());
         } else {
            if (!newValue.getClass().equals(Float.class)) {
               throw new IllegalStateException("Unknown field type");
            }

            this.setValue(newValue.floatValue());
         }
      } catch (Exception e) {
         view.getViewer().sendMessage(new String[]{"\u00a7cAn error has occurred:", e.getMessage()});
      }
   }

   @Override
   public MutableData<Number> getValue() {
      if (super.getValue().get() == null) {
         super.getValue().set(0);
      }

      return super.getValue();
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext context) {
      return Arrays.asList(
         this._t(Translatable.key("layout.displays.value", "%type%", this.displayType, "%value%", this.formatter.apply(this.getValue().get())))
      );
   }

   private boolean isDecimal(Number value) {
      return value != null && (this.isClassDecimal(value) || value.doubleValue() % 1.0 != 0.0);
   }

   private boolean isClassDecimal(Number value) {
      return value != null && (value.getClass().equals(Double.class) || value.getClass().equals(Float.class));
   }

   private Translatable formatNumber(Number value) {
      return value == null ? Translatable.literal("0") : Translatable.literal(this.isDecimal(value) ? DECIMAL_FORMATTER.format(value) : value.toString());
   }

   private static Number sum(Number main, Number addition) {
      if (main instanceof Double) {
         return main.doubleValue() + addition.doubleValue();
      } else if (main instanceof Float) {
         return main.floatValue() + addition.floatValue();
      } else {
         return main instanceof Long ? main.longValue() + addition.longValue() : main.intValue() + addition.intValue();
      }
   }

   private static Number subtract(Number main, Number subtraction) {
      if (main instanceof Double) {
         return main.doubleValue() - subtraction.doubleValue();
      } else if (main instanceof Float) {
         return main.floatValue() - subtraction.floatValue();
      } else {
         return main instanceof Long ? main.longValue() - subtraction.longValue() : main.intValue() - subtraction.intValue();
      }
   }

   private static Number normalize(Number n) {
      if (n instanceof Double) {
         double d = Math.round(n.doubleValue() * 1.0E10) / 1.0E10;
         if (Math.abs(d) < 1.0E-10) {
            d = 0.0;
         }

         return d;
      } else if (n instanceof Float) {
         float f = Math.round(n.floatValue() * 1000000.0F) / 1000000.0F;
         if (Math.abs(f) < 1.0E-6F) {
            f = 0.0F;
         }

         return f;
      } else {
         return n;
      }
   }

   @Generated
   public Function<EditableNumber, Number> getNormalStep() {
      return this.normalStep;
   }

   @Generated
   public Function<EditableNumber, Number> getShiftStep() {
      return this.shiftStep;
   }

   @Generated
   public Runnable getInputTypeSetup() {
      return this.inputTypeSetup;
   }

   @Generated
   public boolean isChatBasedInput() {
      return this.chatBasedInput;
   }
}
