package id.seria.gacha.editor.layouts.crate.rewards.reward;

import com.google.common.collect.Lists;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.EditableObject;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableNumber;
import id.seria.gacha.lib.common.uicomponents.newest.layout.modifiers.ObjectModifiers;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomReward;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.Generated;
import org.jetbrains.annotations.Nullable;

public class EditorCrateRewardCooldownDurationLayout extends EditorFacade.EditorLayout {
   private static final long SECONDS_PER_MINUTE = 60L;
   private static final long SECONDS_PER_HOUR = 3600L;
   private static final long SECONDS_PER_DAY = 86400L;
   private static final long SECONDS_PER_WEEK = 604800L;
   private final CrateType crateType;
   private final CustomReward reward;
   private final Function<CustomReward, Long> getter;
   private final BiConsumer<CustomReward, Long> setter;
   @Nullable
   private final Runnable afterApply;
   private EditableObject<Number> weeksComponent;
   private EditableObject<Number> daysComponent;
   private EditableObject<Number> hoursComponent;
   private EditableObject<Number> minutesComponent;

   public EditorCrateRewardCooldownDurationLayout(
      CrateType crateType,
      CustomReward reward,
      EditorFacade.EditorLayout parent,
      Function<CustomReward, Long> getter,
      BiConsumer<CustomReward, Long> setter,
      @Nullable Runnable afterApply
   ) {
      super(Translatable.key("editor.titles.crate.reward-cooldown-duration"), parent);
      this.crateType = crateType;
      this.reward = reward;
      this.getter = getter;
      this.setter = setter;
      this.afterApply = afterApply;
      this.setViewPersistentOnGoBack(true);
   }

   @Override
   public void onPrepare(ContainerView view) {
      long remaining = Math.max(0L, this.getter.apply(this.reward));
      long weeks = remaining / 604800L;
      remaining %= 604800L;
      long days = remaining / 86400L;
      remaining %= 86400L;
      long hours = remaining / 3600L;
      remaining %= 3600L;
      long minutes = remaining / 60L;
      this.weeksComponent = new EditableNumber(
            SlotCompound.from(3, 2),
            this.createData(
               XMaterial.CLOCK,
               Translatable.key("editor.crate.reward.cooldowns.duration.weeks.title"),
               Translatable.key("editor.crate.reward.cooldowns.duration.weeks.description")
            ),
            Translatable.key("time.weeks"),
            (int)weeks
         )
         .addModifier(ObjectModifiers.POSITIVE_NUMBER)
         .setOnValueChange(value -> this.applyDuration(view));
      this.daysComponent = new EditableNumber(
            SlotCompound.from(4, 2),
            this.createData(
               XMaterial.CLOCK,
               Translatable.key("editor.crate.reward.cooldowns.duration.days.title"),
               Translatable.key("editor.crate.reward.cooldowns.duration.days.description")
            ),
            Translatable.key("time.days"),
            (int)days
         )
         .addModifier(ObjectModifiers.POSITIVE_NUMBER)
         .setOnValueChange(value -> this.applyDuration(view));
      this.hoursComponent = new EditableNumber(
            SlotCompound.from(5, 2),
            this.createData(
               XMaterial.CLOCK,
               Translatable.key("editor.crate.reward.cooldowns.duration.hours.title"),
               Translatable.key("editor.crate.reward.cooldowns.duration.hours.description")
            ),
            Translatable.key("time.hours"),
            (int)hours
         )
         .addModifier(ObjectModifiers.POSITIVE_NUMBER)
         .setOnValueChange(value -> this.applyDuration(view));
      this.minutesComponent = new EditableNumber(
            SlotCompound.from(6, 2),
            this.createData(
               XMaterial.CLOCK,
               Translatable.key("editor.crate.reward.cooldowns.duration.minutes.title"),
               Translatable.key("editor.crate.reward.cooldowns.duration.minutes.description")
            ),
            Translatable.key("time.minutes"),
            (int)minutes
         )
         .addModifier(ObjectModifiers.POSITIVE_NUMBER)
         .setOnValueChange(value -> this.applyDuration(view));
      this.addComponent(view, this.weeksComponent);
      this.addComponent(view, this.daysComponent);
      this.addComponent(view, this.hoursComponent);
      this.addComponent(view, this.minutesComponent);
      this.addComponent(
         view,
         this.createComponent(
            SlotCompound.from(7, 2),
            this.createData(
               XMaterial.BARRIER,
               Translatable.key("editor.crate.reward.cooldowns.duration.disable.title"),
               Translatable.key("editor.crate.reward.cooldowns.duration.disable.description")
            ),
            Lists.newArrayList(new ClickAction[]{ClickAction.SET(ClickAction.ClickType.LEFT, this.label("disabled"))}),
            context -> {
               this.weeksComponent.safeSetValue(view, 0, false, false);
               this.daysComponent.safeSetValue(view, 0, false, false);
               this.hoursComponent.safeSetValue(view, 0, false, false);
               this.minutesComponent.safeSetValue(view, 0, false, false);
               this.setter.accept(this.reward, 0L);
               if (this.afterApply != null) {
                  this.afterApply.run();
               }

               EditorFacade.saveCrate(view, this.crateType, this.reward);
               this.updateView(view);
            }
         )
      );
   }

   private void applyDuration(ContainerView view) {
      long total = this.weeksComponent.getValue().get().longValue() * 604800L
         + this.daysComponent.getValue().get().longValue() * 86400L
         + this.hoursComponent.getValue().get().longValue() * 3600L
         + this.minutesComponent.getValue().get().longValue() * 60L;
      this.setter.accept(this.reward, total);
      if (this.afterApply != null) {
         this.afterApply.run();
      }

      EditorFacade.saveCrate(view, this.crateType, this.reward);
      this.updateView(view);
   }

   @Generated
   public CrateType getCrateType() {
      return this.crateType;
   }

   @Generated
   public CustomReward getReward() {
      return this.reward;
   }

   @Generated
   public Function<CustomReward, Long> getGetter() {
      return this.getter;
   }

   @Generated
   public BiConsumer<CustomReward, Long> getSetter() {
      return this.setter;
   }

   @Nullable
   @Generated
   public Runnable getAfterApply() {
      return this.afterApply;
   }

   @Generated
   public EditableObject<Number> getWeeksComponent() {
      return this.weeksComponent;
   }

   @Generated
   public EditableObject<Number> getDaysComponent() {
      return this.daysComponent;
   }

   @Generated
   public EditableObject<Number> getHoursComponent() {
      return this.hoursComponent;
   }

   @Generated
   public EditableObject<Number> getMinutesComponent() {
      return this.minutesComponent;
   }
}
