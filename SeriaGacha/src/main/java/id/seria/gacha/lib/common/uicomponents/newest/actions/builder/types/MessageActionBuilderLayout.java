package id.seria.gacha.lib.common.uicomponents.newest.actions.builder.types;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionTypeBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableString;
import id.seria.gacha.lib.common.utils.actions.ActionType;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;

public class MessageActionBuilderLayout extends ActionTypeBuilderLayout<MessageActionBuilderLayout.Holder> {
   private static final Translatable LABEL = Translatable.key("layout.actions.builder.fields.message.label");

   public MessageActionBuilderLayout(ActionBuilderLayout builder, ActionType type) {
      super(builder, type);
   }

   public void onPrepareBuilder(ContainerView view, MessageActionBuilderLayout.Holder holder) {
      this.addComponent(
         view,
         new EditableString(
               SlotCompound.from(5, 2),
               this.createData(
                  XMaterial.PAPER,
                  Translatable.key("layout.actions.builder.fields.message.title"),
                  Translatable.key("layout.actions.builder.fields.message.description", "%type%", this.typeName())
               ),
               LABEL,
               holder.message
            )
            .setOnValueChange(value -> holder.message = value)
      );
   }

   public MessageActionBuilderLayout.Holder createHolder() {
      return new MessageActionBuilderLayout.Holder();
   }

   public boolean canCreate(MessageActionBuilderLayout.Holder holder) {
      return !holder.message.trim().isEmpty();
   }

   protected String[] arguments(MessageActionBuilderLayout.Holder holder) {
      return new String[]{holder.message};
   }

   public static class Holder {
      private String message = "";

      public Holder() {
      }
   }
}
