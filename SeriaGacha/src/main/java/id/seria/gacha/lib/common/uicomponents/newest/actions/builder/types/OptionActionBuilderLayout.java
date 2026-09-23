package id.seria.gacha.lib.common.uicomponents.newest.actions.builder.types;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionTypeBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditablePopupList;
import id.seria.gacha.lib.common.utils.actions.ActionType;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OptionActionBuilderLayout extends ActionTypeBuilderLayout<OptionActionBuilderLayout.Holder> {
   private final XMaterial icon;
   private final Translatable label;
   private final Supplier<List<String>> options;

   public OptionActionBuilderLayout(ActionBuilderLayout builder, ActionType type, XMaterial icon, Translatable label, Supplier<List<String>> options) {
      super(builder, type);
      this.icon = icon;
      this.label = label;
      this.options = options;
   }

   public void onPrepareBuilder(ContainerView view, OptionActionBuilderLayout.Holder holder) {
      this.addComponent(
         view,
         new EditablePopupList<>(
               SlotCompound.from(5, 2),
               this.createData(
                  this.icon,
                  Translatable.key("layout.actions.builder.fields.option.title", "%option%", this.label),
                  Translatable.key("layout.actions.builder.fields.option.description", "%type%", this.typeName(), "%option%", this.label)
               ),
               this.label,
               new ArrayList<>(this.options.get()),
               holder.option
            )
            .setParser(option -> new EditablePopupList.ListItem<>(option, this.icon, StringUtil.humanize(option), new ArrayList<>()))
            .setFormatter(option -> option != null && !option.isEmpty() ? Translatable.literal(option) : Translatable.key("labels.none"))
            .setOnValueChange(value -> holder.option = value)
      );
   }

   public OptionActionBuilderLayout.Holder createHolder() {
      return new OptionActionBuilderLayout.Holder();
   }

   public boolean canCreate(OptionActionBuilderLayout.Holder holder) {
      return holder.option != null && !holder.option.isEmpty();
   }

   protected String[] arguments(OptionActionBuilderLayout.Holder holder) {
      return new String[]{holder.option};
   }

   public static class Holder {
      private String option;

      public Holder() {
      }
   }
}
