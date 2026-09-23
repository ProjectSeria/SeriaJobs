package id.seria.gacha.lib.common.uicomponents.newest.actions.builder;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableStringList;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import java.util.ArrayList;
import java.util.List;

public class ActionListComponent extends EditableStringList {
   public ActionListComponent(SlotCompound slot, ComponentData data, ActionBuilderRegistry registry, List<String> actions) {
      super(slot, data, ActionTypeBuilderLayout.ACTION_LABEL, actions);
      this.setFormatter(value -> value.isEmpty() ? Translatable.key("labels.empty") : Translatable.literal(format(value)));
      this.swapClickAction(ClickAction.ClickType.LEFT, ClickAction.ClickType.SHIFT_LEFT);
      this.setClickAction(
         ClickAction.CREATE(ClickAction.ClickType.LEFT, ActionTypeBuilderLayout.ACTION_LABEL),
         context -> new ActionBuilderLayout(this.getContainerLayout(), registry, action -> {
            List<String> list = new ArrayList<>(this.getValue().get());
            list.add(registry.getFactory().serialize(action));
            this.safeSetValue(this.getContainerView(), list, false);
         }).open(context.getViewer())
      );
      this.setClickAction(ClickAction.REMOVE(ClickAction.ClickType.RIGHT, Translatable.key("labels.last-one")), context -> {
         if (!this.getValue().get().isEmpty()) {
            List<String> list = new ArrayList<>(this.getValue().get());
            list.remove(list.size() - 1);
            this.safeSetValue(context.getContainerView(), list, true);
         }
      });
      this.setClickAction(
         ClickAction.CLEAR(ClickAction.ClickType.SHIFT_RIGHT, Translatable.key("labels.all")),
         context -> this.safeSetValue(context.getContainerView(), new ArrayList<>(), true)
      );
   }

   private static List<String> format(List<String> serializedActions) {
      List<String> display = new ArrayList<>();

      for (String action : serializedActions) {
         int typeEnd = action.indexOf(93);
         if (typeEnd == -1) {
            display.add("\u00a78- \u00a7f" + action);
         } else {
            display.add("\u00a78- \u00a7a" + action.substring(0, typeEnd + 1) + "\u00a7f" + action.substring(typeEnd + 1));
         }
      }

      return display;
   }
}
