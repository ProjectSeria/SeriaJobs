package id.seria.gacha.lib.common.uicomponents.newest.actions.builder.types;

import com.google.common.collect.Lists;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionTypeBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditablePopupList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableNumber;
import id.seria.gacha.lib.common.utils.actions.ActionType;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.lib.xseries.XPotion;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PotionEffectActionBuilderLayout extends ActionTypeBuilderLayout<PotionEffectActionBuilderLayout.Holder> {
   public PotionEffectActionBuilderLayout(ActionBuilderLayout builder, ActionType type) {
      super(builder, type);
   }

   public void onPrepareBuilder(ContainerView view, PotionEffectActionBuilderLayout.Holder holder) {
      this.addComponent(
         view,
         new EditablePopupList<>(
               SlotCompound.from(3, 2),
               this.createData(
                  XMaterial.POTION,
                  Translatable.key("layout.actions.builder.fields.potion.title"),
                  Translatable.key("layout.actions.builder.fields.potion.description", "%type%", this.typeName())
               ),
               Translatable.key("layout.actions.builder.fields.potion.label"),
               getAvailablePotions(),
               holder.potion
            )
            .setParser(potion -> new EditablePopupList.ListItem<>(potion, XMaterial.BOOK, StringUtil.humanize(potion.name()), new ArrayList<>()))
            .setFormatter(potion -> potion == null ? Translatable.key("labels.none") : Translatable.literal(StringUtil.humanize(potion.name())))
            .setOnValueChange(value -> holder.potion = value)
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(5, 2),
               this.createData(
                  XMaterial.CLOCK,
                  Translatable.key("layout.actions.builder.fields.duration.title"),
                  Translatable.key("layout.actions.builder.fields.duration.description", "%type%", this.typeName())
               ),
               Translatable.key("layout.actions.builder.fields.duration.label"),
               holder.duration
            )
            .setFormatter(
               value -> value.intValue() < 0
                  ? Translatable.key("layout.actions.builder.fields.duration.infinite")
                  : Translatable.literal(value.intValue() + "s")
            )
            .addModifier(value -> Math.max(-1, value.intValue()))
            .setOnValueChange(value -> holder.duration = value.intValue())
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(7, 2),
               this.createData(
                  XMaterial.GUNPOWDER,
                  Translatable.key("layout.actions.builder.fields.amplifier.title"),
                  Translatable.key("layout.actions.builder.fields.amplifier.description", "%type%", this.typeName())
               ),
               Translatable.key("layout.actions.builder.fields.amplifier.label"),
               holder.amplifier
            )
            .addModifier(value -> Math.max(0, value.intValue()))
            .setOnValueChange(value -> holder.amplifier = value.intValue())
      );
   }

   private static List<XPotion> getAvailablePotions() {
      List<XPotion> potions = Lists.newArrayList();

      for (XPotion potion : XPotion.values()) {
         if (potion.getPotionEffectType() != null) {
            potions.add(potion);
         }
      }

      potions.sort(Comparator.comparing(Enum::name));
      return potions;
   }

   public PotionEffectActionBuilderLayout.Holder createHolder() {
      return new PotionEffectActionBuilderLayout.Holder();
   }

   public boolean canCreate(PotionEffectActionBuilderLayout.Holder holder) {
      return holder.potion != null;
   }

   protected String[] arguments(PotionEffectActionBuilderLayout.Holder holder) {
      return new String[]{holder.potion.name(), String.valueOf(holder.duration), String.valueOf(holder.amplifier)};
   }

   public static class Holder {
      private XPotion potion;
      private int duration = 60;
      private int amplifier = 1;

      public Holder() {
      }
   }
}
