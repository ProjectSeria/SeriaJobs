package id.seria.gacha.lib.common.uicomponents.newest.actions.builder.types;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionTypeBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.DisplayableComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableNumber;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableString;
import id.seria.gacha.lib.common.utils.actions.ActionType;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.function.IntConsumer;

public class TitleActionBuilderLayout extends ActionTypeBuilderLayout<TitleActionBuilderLayout.Holder> {
   public TitleActionBuilderLayout(ActionBuilderLayout builder, ActionType type) {
      super(builder, type);
   }

   public void onPrepareBuilder(ContainerView view, TitleActionBuilderLayout.Holder holder) {
      this.addComponent(
         view,
         new EditableString(
               SlotCompound.from(3, 2),
               this.createData(
                  XMaterial.PAPER,
                  Translatable.key("layout.actions.builder.fields.title-text.title"),
                  Translatable.key("layout.actions.builder.fields.title-text.description", "%type%", this.typeName())
               ),
               Translatable.key("layout.actions.builder.fields.title-text.label"),
               holder.title
            )
            .setOnValueChange(value -> holder.title = value)
      );
      this.addComponent(
         view,
         new EditableString(
               SlotCompound.from(4, 2),
               this.createData(
                  XMaterial.PAPER,
                  Translatable.key("layout.actions.builder.fields.subtitle.title"),
                  Translatable.key("layout.actions.builder.fields.subtitle.description", "%type%", this.typeName())
               ),
               Translatable.key("layout.actions.builder.fields.subtitle.label"),
               holder.subtitle
            )
            .setOnValueChange(value -> holder.subtitle = value)
      );
      this.addComponent(view, this.ticks(SlotCompound.from(5, 2), "fade-in", holder.fadeIn, value -> holder.fadeIn = value));
      this.addComponent(view, this.ticks(SlotCompound.from(6, 2), "stay", holder.stay, value -> holder.stay = value));
      this.addComponent(view, this.ticks(SlotCompound.from(7, 2), "fade-out", holder.fadeOut, value -> holder.fadeOut = value));
   }

   private DisplayableComponent ticks(SlotCompound slot, String field, int value, IntConsumer setter) {
      return new EditableNumber(
            slot,
            this.createData(
               XMaterial.CLOCK,
               Translatable.key("layout.actions.builder.fields." + field + ".title"),
               Translatable.key("layout.actions.builder.fields." + field + ".description", "%type%", this.typeName())
            ),
            Translatable.key("layout.actions.builder.fields." + field + ".label"),
            value
         )
         .addModifier(input -> Math.max(0, input.intValue()))
         .setOnValueChange(input -> setter.accept(input.intValue()));
   }

   public TitleActionBuilderLayout.Holder createHolder() {
      return new TitleActionBuilderLayout.Holder();
   }

   public boolean canCreate(TitleActionBuilderLayout.Holder holder) {
      return !holder.title.trim().isEmpty() && !holder.subtitle.trim().isEmpty();
   }

   protected String[] arguments(TitleActionBuilderLayout.Holder holder) {
      return new String[]{holder.title, holder.subtitle, String.valueOf(holder.fadeIn), String.valueOf(holder.stay), String.valueOf(holder.fadeOut)};
   }

   public static class Holder {
      private String title = "";
      private String subtitle = "";
      private int fadeIn = 10;
      private int stay = 20;
      private int fadeOut = 10;

      public Holder() {
      }
   }
}
