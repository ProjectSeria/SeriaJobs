package id.seria.gacha.lib.common.uicomponents.newest.layout.component;

import com.google.common.collect.Lists;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.Ui;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickActionsFacade;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.bukkit.EditableItem;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.objects.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

public abstract class ClickableComponent<T extends ClickableComponent<T>> extends DisplayableComponent {
   @Deprecated
   protected Map<ClickAction, BiConsumer<T, ClickViewContext>> clickActions = new HashMap<>();
   protected Predicate<T> locked = obj -> false;
   protected List<Pair<Supplier<Boolean>, Translatable>> warnings = new ArrayList<>();
   protected List<Pair<Supplier<Boolean>, Translatable>> errors = new ArrayList<>();
   protected final Map<String, Supplier<String>> placeholders = new LinkedHashMap<>();
   private boolean showAttributes = true;
   private boolean showClickActions = true;

   protected ClickableComponent(SlotCompound slot, ComponentData data) {
      super(slot, data);
   }

   @Override
   public void onInitialize(ContainerView ignored) {
   }

   @Override
   public void onPrepare(ContainerView ignored) {
   }

   @Override
   public void onRender(RenderViewContext context) {
      VirtualItem item = new VirtualItem();
      item.withFlag(ItemFlag.HIDE_ATTRIBUTES).withItem(this.getData().getItem()).withDisplayName("\u00a7a" + this._t(this.getData().getTitle())[0]);
      List<String> lore = new ArrayList<>();

      for (String line : this._t(this.getData().getDescription())) {
         String temp = line;

         for (Entry<String, Supplier<String>> entry : this.placeholders.entrySet()) {
            temp.replace("%" + entry.getKey() + "%", entry.getValue().get());
         }

         lore.add("\u00a77" + temp);
      }

      if (this.showAttributes) {
         List<String> display = this.getRenderDisplay(context);
         if (!display.isEmpty()) {
            lore.add("");
            lore.addAll(display);
         }
      }

      if (this.showClickActions) {
         lore = ClickActionsFacade.applyActions(this.getContainerLayout(), lore, this.clickActions.keySet());
      }

      for (Pair<Supplier<Boolean>, Translatable> entry : this.errors) {
         if (entry.first.get()) {
            for (String line : this._t(entry.second)) {
               lore.add("\u00a7c" + Ui.smallCaps(line));
            }
            break;
         }
      }

      for (Pair<Supplier<Boolean>, Translatable> entry : this.warnings) {
         if (entry.first.get()) {
            for (String line : this._t(entry.second)) {
               lore.add("\u00a7c" + Ui.smallCaps(line));
            }
         }
      }

      item.withLore(lore);
      item.withSlots(this.getSlot());
      item.render(context);
   }

   public abstract List<String> getRenderDisplay(RenderViewContext var1);

   @Override
   public List<ClickAction> getDisplayActions() {
      return Lists.newArrayList(this.clickActions.keySet());
   }

   public void onClick(ClickViewContext context) {
      if (!this.locked.test((T)this)) {
         BiConsumer<T, ClickViewContext> consumer = this.clickActions.get(new ClickAction(ClickAction.ClickType.DRAG_DROP));
         if (consumer != null) {
            if (!(this instanceof EditableItem)) {
               if (context.getCursor() != null && context.getCursor().getType() != Material.AIR) {
                  consumer.accept((T)this, context);
                  return;
               }

               return;
            }

            boolean ignoreEmptyHand = ((EditableItem)this).isIgnoreEmptyHand();
            if (!ignoreEmptyHand || context.getCursor() != null && context.getCursor().getType() != Material.AIR) {
               consumer.accept((T)this, context);
               return;
            }
         }

         consumer = this.clickActions.get(new ClickAction(ClickAction.ClickType.LEFT_AND_RIGHT));
         if (consumer == null) {
            consumer = this.clickActions.get(new ClickAction(ClickAction.ClickType.from(context.getClick())));
         }

         if (consumer != null) {
            consumer.accept((T)this, context);
         }
      }
   }

   public boolean isClicking(ClickViewContext context) {
      return this.getSlot().contains(context.getRawSlot());
   }

   public ClickableComponent<T> addError(Translatable message) {
      return this.addError(() -> true, message);
   }

   public ClickableComponent<T> addError(String message) {
      return this.addError(() -> true, message);
   }

   public ClickableComponent<T> addError(boolean condition, Translatable message) {
      return this.addError(() -> condition, message);
   }

   public ClickableComponent<T> addError(boolean condition, String message) {
      return this.addError(() -> condition, message);
   }

   public ClickableComponent<T> addError(Supplier<Boolean> condition, String message) {
      return this.addError(condition, Translatable.literal(message));
   }

   public ClickableComponent<T> addError(Supplier<Boolean> condition, Translatable message) {
      this.errors.add(Pair.of(condition, message));
      this.mergeLockedCondition(condition);
      return this;
   }

   public ClickableComponent<T> addWarning(Translatable message) {
      this.warnings.add(Pair.of(() -> true, message));
      return this;
   }

   public ClickableComponent<T> addWarning(String message) {
      this.warnings.add(Pair.of(() -> true, Translatable.literal(message)));
      return this;
   }

   public ClickableComponent<T> addWarning(boolean condition, Translatable message) {
      return this.addWarning(() -> condition, message);
   }

   public ClickableComponent<T> addWarning(boolean condition, String message) {
      return this.addWarning(() -> condition, message);
   }

   public ClickableComponent<T> addWarning(Supplier<Boolean> condition, Translatable message) {
      this.warnings.add(Pair.of(condition, message));
      return this;
   }

   public ClickableComponent<T> addWarning(Supplier<Boolean> condition, String message) {
      this.warnings.add(Pair.of(condition, Translatable.literal(message)));
      return this;
   }

   private void mergeLockedCondition(Supplier<Boolean> condition) {
      Predicate<T> currentLocked = this.locked == null ? obj -> false : this.locked;
      this.setLocked(obj -> currentLocked.test(obj) || condition.get());
   }

   public ClickableComponent<T> removeClickAction(ClickAction.ClickType clickType) {
      this.clickActions.remove(new ClickAction(clickType, null));
      return this;
   }

   public ClickableComponent<T> setClickAction(ClickAction action) {
      this.removeClickAction(action.getClickType());
      this.clickActions.put(action, null);
      return this;
   }

   public ClickableComponent<T> setClickAction(ClickAction action, Consumer<ClickViewContext> consumer) {
      this.removeClickAction(action.getClickType());
      if (consumer != null) {
         this.clickActions.put(action, (object, context) -> consumer.accept(context));
      }

      return this;
   }

   public ClickableComponent<T> setClickAction(ClickAction action, BiConsumer<T, ClickViewContext> consumer) {
      this.removeClickAction(action.getClickType());
      if (consumer != null) {
         this.clickActions.put(action, consumer);
      }

      return this;
   }

   public ClickableComponent<T> replaceDisplayAction(ClickAction.ClickType clickType, Translatable displayAction) {
      ClickAction action = this.getDisplayAction(clickType);
      if (action != null) {
         action.setDisplayAction(displayAction);
      }

      return this;
   }

   public ClickableComponent<T> replaceDisplayAction(ClickAction action) {
      ClickAction original = this.getDisplayAction(action.getClickType());
      if (original != null) {
         action.setDisplayAction(action.getDisplayAction());
      }

      return this;
   }

   protected ClickAction getDisplayAction(ClickAction.ClickType clickType) {
      for (ClickAction original : this.clickActions.keySet()) {
         if (original.getClickType() == clickType) {
            return original;
         }
      }

      return null;
   }

   public ClickableComponent<T> addPlaceholder(String placeholder, Supplier<String> supplier) {
      this.placeholders.put(placeholder, supplier);
      return this;
   }

   @Deprecated
   @Generated
   public Map<ClickAction, BiConsumer<T, ClickViewContext>> getClickActions() {
      return this.clickActions;
   }

   @Generated
   public Predicate<T> getLocked() {
      return this.locked;
   }

   @Generated
   public List<Pair<Supplier<Boolean>, Translatable>> getWarnings() {
      return this.warnings;
   }

   @Generated
   public List<Pair<Supplier<Boolean>, Translatable>> getErrors() {
      return this.errors;
   }

   @Generated
   public Map<String, Supplier<String>> getPlaceholders() {
      return this.placeholders;
   }

   @Generated
   public boolean isShowAttributes() {
      return this.showAttributes;
   }

   @Generated
   public boolean isShowClickActions() {
      return this.showClickActions;
   }

   @Generated
   public ClickableComponent<T> setLocked(Predicate<T> locked) {
      this.locked = locked;
      return this;
   }

   @Generated
   public ClickableComponent<T> setWarnings(List<Pair<Supplier<Boolean>, Translatable>> warnings) {
      this.warnings = warnings;
      return this;
   }

   @Generated
   public ClickableComponent<T> setErrors(List<Pair<Supplier<Boolean>, Translatable>> errors) {
      this.errors = errors;
      return this;
   }

   @Generated
   public ClickableComponent<T> setShowAttributes(boolean showAttributes) {
      this.showAttributes = showAttributes;
      return this;
   }

   @Generated
   public ClickableComponent<T> setShowClickActions(boolean showClickActions) {
      this.showClickActions = showClickActions;
      return this;
   }
}
