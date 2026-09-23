package id.seria.gacha.lib.common.uicomponents.newest.actions.builder.types;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionTypeBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableListSingleOption;
import id.seria.gacha.lib.common.utils.actions.ActionType;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.ArrayList;
import java.util.List;

public class ChoiceActionBuilderLayout extends ActionTypeBuilderLayout<ChoiceActionBuilderLayout.Holder> {
   private final XMaterial icon;
   private final Translatable label;
   private final List<String> choices;

   public ChoiceActionBuilderLayout(ActionBuilderLayout builder, ActionType type, XMaterial icon, Translatable label, List<String> choices) {
      super(builder, type);
      this.icon = icon;
      this.label = label;
      this.choices = new ArrayList<>(choices);
   }

   public void onPrepareBuilder(ContainerView view, ChoiceActionBuilderLayout.Holder holder) {
      this.addComponent(
         view,
         new EditableListSingleOption<>(
               SlotCompound.from(5, 2),
               this.createData(
                  this.icon,
                  Translatable.key("layout.actions.builder.fields.choice.title", "%choice%", this.label),
                  Translatable.key("layout.actions.builder.fields.choice.description", "%type%", this.typeName(), "%choice%", this.label)
               ),
               this.choices,
               holder.choice
            )
            .setItemFormatter(value -> Translatable.literal(StringUtil.humanize(value)))
            .setOnOptionChange(value -> holder.choice = value)
      );
   }

   public ChoiceActionBuilderLayout.Holder createHolder() {
      return new ChoiceActionBuilderLayout.Holder(this.choices.get(0));
   }

   public boolean canCreate(ChoiceActionBuilderLayout.Holder holder) {
      return holder.choice != null;
   }

   protected String[] arguments(ChoiceActionBuilderLayout.Holder holder) {
      return new String[]{holder.choice};
   }

   public static class Holder {
      private String choice;

      public Holder(String choice) {
         this.choice = choice;
      }
   }
}
