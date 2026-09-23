package id.seria.gacha.lib.common.uicomponents.newest.actions.builder.types;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionTypeBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableNumber;
import id.seria.gacha.lib.common.utils.actions.ActionType;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;

public class DelayActionBuilderLayout extends ActionTypeBuilderLayout<DelayActionBuilderLayout.Holder> {
   private static final Translatable LABEL = Translatable.key("layout.actions.builder.fields.delay.label");

   public DelayActionBuilderLayout(ActionBuilderLayout builder, ActionType type) {
      super(builder, type);
   }

   public void onPrepareBuilder(ContainerView view, DelayActionBuilderLayout.Holder holder) {
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(5, 2),
               this.createData(
                  XMaterial.CLOCK,
                  Translatable.key("layout.actions.builder.fields.delay.title"),
                  Translatable.key("layout.actions.builder.fields.delay.description", "%type%", this.typeName())
               ),
               LABEL,
               holder.seconds
            )
            .addModifier(value -> Math.max(0.0, value.doubleValue()))
            .setOnValueChange(value -> holder.seconds = value.doubleValue())
      );
   }

   public DelayActionBuilderLayout.Holder createHolder() {
      return new DelayActionBuilderLayout.Holder();
   }

   public boolean canCreate(DelayActionBuilderLayout.Holder holder) {
      return holder.seconds > 0.0;
   }

   protected String[] arguments(DelayActionBuilderLayout.Holder holder) {
      return new String[]{String.valueOf(holder.seconds)};
   }

   public static class Holder {
      private double seconds = 1.0;

      public Holder() {
      }
   }
}
