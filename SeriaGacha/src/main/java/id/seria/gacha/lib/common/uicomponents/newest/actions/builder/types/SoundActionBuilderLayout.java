package id.seria.gacha.lib.common.uicomponents.newest.actions.builder.types;

import com.google.common.collect.Lists;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionTypeBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.DisplayableComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditablePopupList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableNumber;
import id.seria.gacha.lib.common.utils.actions.ActionType;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.lib.xseries.XSound;
import id.seria.gacha.lib.xseries.base.XModule;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SoundActionBuilderLayout extends ActionTypeBuilderLayout<SoundActionBuilderLayout.Holder> {
   public SoundActionBuilderLayout(ActionBuilderLayout builder, ActionType type) {
      super(builder, type);
   }

   public void onPrepareBuilder(ContainerView view, SoundActionBuilderLayout.Holder holder) {
      this.addComponent(
         view,
         new EditablePopupList<>(
               SlotCompound.from(3, 2),
               this.createData(
                  XMaterial.NOTE_BLOCK,
                  Translatable.key("layout.actions.builder.fields.sound.title"),
                  Translatable.key("layout.actions.builder.fields.sound.description", "%type%", this.typeName())
               ),
               Translatable.key("layout.actions.builder.fields.sound.label"),
               getAvailableSounds(),
               holder.sound
            )
            .setParser(sound -> new EditablePopupList.ListItem<>(sound, XMaterial.BOOK, StringUtil.humanize(sound.name()), new ArrayList<>()))
            .setFormatter(sound -> sound == null ? Translatable.key("labels.none") : Translatable.literal(StringUtil.humanize(sound.name())))
            .setOnValueChange(value -> holder.sound = value)
      );
      this.addComponent(view, this.level(SlotCompound.from(5, 2), XMaterial.GUNPOWDER, "volume", holder.volume, value -> holder.volume = value));
      this.addComponent(view, this.level(SlotCompound.from(7, 2), XMaterial.GLOWSTONE_DUST, "pitch", holder.pitch, value -> holder.pitch = value));
   }

   private DisplayableComponent level(SlotCompound slot, XMaterial icon, String field, float value, SoundActionBuilderLayout.FloatConsumer setter) {
      return new EditableNumber(
            slot,
            this.createData(
               icon,
               Translatable.key("layout.actions.builder.fields." + field + ".title"),
               Translatable.key("layout.actions.builder.fields." + field + ".description", "%type%", this.typeName())
            ),
            Translatable.key("layout.actions.builder.fields." + field + ".label"),
            value
         )
         .addModifier(input -> Math.max(0.0F, input.floatValue()))
         .setOnValueChange(input -> setter.accept(input.floatValue()));
   }

   private static List<XSound> getAvailableSounds() {
      List<XSound> sounds = Lists.newArrayList();

      for (XSound sound : XSound.getValues()) {
         if (sound.get() != null) {
            sounds.add(sound);
         }
      }

      sounds.sort(Comparator.comparing(XModule::name));
      return sounds;
   }

   public SoundActionBuilderLayout.Holder createHolder() {
      return new SoundActionBuilderLayout.Holder();
   }

   public boolean canCreate(SoundActionBuilderLayout.Holder holder) {
      return holder.sound != null;
   }

   protected String[] arguments(SoundActionBuilderLayout.Holder holder) {
      return new String[]{holder.sound.name(), String.valueOf(holder.volume), String.valueOf(holder.pitch)};
   }

   @FunctionalInterface
   private interface FloatConsumer {
      void accept(float var1);
   }

   public static class Holder {
      private XSound sound;
      private float volume = 1.0F;
      private float pitch = 1.0F;

      public Holder() {
      }
   }
}
