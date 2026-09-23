package id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.dialog.DialogUtil;
import id.seria.gacha.lib.common.uicomponents.newest.layout.ContainerLayout;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.EditableObject;
import id.seria.gacha.lib.common.uicomponents.newest.limbo.ObjectValidationException;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.common.utils.version.ServerVersion;
import id.seria.gacha.lib.common.utils.version.VersionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public class EditableStringList extends EditableObject<List<String>> {
   @Deprecated
   public EditableStringList(ComponentData data, Supplier<List<String>> initialList) {
      this(new SlotCompound(data.getSlot()), data, initialList.get());
   }

   public EditableStringList(SlotCompound slot, ComponentData data, List<String> initialList) {
      this(slot, data, null, initialList);
   }

   public EditableStringList(SlotCompound slot, ComponentData data, Translatable displayType, List<String> initialList) {
      super(slot, data, displayType, Collections.synchronizedList(initialList));
      this.setFormatter(
         v -> v.isEmpty()
            ? Translatable.key("labels.empty")
            : Translatable.literal(v.stream().map(x -> String.format("\u00a78- \u00a77%s", x)).collect(Collectors.toList()))
      );
      if (VersionUtils.isPaperServer() && ServerVersion.isGreaterEqualThan(ServerVersion.v1_21_R7)) {
         this.removeClickAction(ClickAction.ClickType.LEFT);
         this.removeClickAction(ClickAction.ClickType.RIGHT);
         this.removeClickAction(ClickAction.ClickType.SHIFT_LEFT);
         this.removeClickAction(ClickAction.ClickType.SHIFT_RIGHT);
         this.setClickAction(
            ClickAction.ADD(ClickAction.ClickType.LEFT, displayType),
            context -> {
               Player player = context.getViewer();
               player.closeInventory();
               String dataTitle = this._t(this.getData().getTitle().colorless())[0];
               DialogUtil.askForMultiInput(
                  player,
                  "<color:#99969e>"
                     + this._t(((ContainerLayout)context.getContainerView().getContainer()).getTitle().colorless())[0]
                     + " > "
                     + StringUtil.humanize(dataTitle),
                  dataTitle,
                  "<white>Enter the <green>" + dataTitle.toLowerCase() + "<white>.",
                  this.getValue().get(),
                  input -> {
                     if (input != null) {
                        try {
                           this.setValue(input);
                           player.sendMessage(String.format("\u00a7a%s successfully updated!", dataTitle));
                        } catch (ObjectValidationException e) {
                           player.sendMessage(e.getMessage());
                        } catch (Exception e) {
                           player.sendMessage(new String[]{"\u00a7cAn error has occurred:", e.getMessage()});
                           return;
                        }
                     }

                     context.getContainerView().updateView();
                  }
               );
            }
         );
      } else {
         this.setClickAction(ClickAction.ADD(ClickAction.ClickType.LEFT, displayType), context -> {
            Player viewer = context.getViewer();
            viewer.sendMessage(String.format("\u00a7eWrite the new %s in the chat.", this._t(this.displayType)[0].toLowerCase()));
            viewer.closeInventory();
            TextReader.askForInput(viewer, input -> {
               if (input != null) {
                  try {
                     List<String> list = new ArrayList<>(this.getValue().get());
                     list.add(input);
                     this.setValue(list);
                     viewer.sendMessage(String.format("\u00a7a%s successfully added!", StringUtil.capitalize(this._t(this.displayType)[0])));
                  } catch (Exception e) {
                     viewer.sendMessage(new String[]{"\u00a7cAn error has occurred:", e.getMessage()});
                     return;
                  }
               }

               context.getContainerView().updateView();
            });
         });
         this.setClickAction(ClickAction.REMOVE(ClickAction.ClickType.RIGHT, Translatable.key("labels.last-one")), context -> {
            Player viewer = context.getViewer();
            if (!this.getValue().get().isEmpty()) {
               List<String> list = new ArrayList<>(this.getValue().get());
               list.remove(list.size() - 1);
               this.safeSetValue(context.getContainerView(), list, true);
            }
         });
         this.setClickAction(
            ClickAction.CLEAR(ClickAction.ClickType.SHIFT_RIGHT, Translatable.key("labels.all")),
            context -> this.safeSetValue(this.getContainerView(), new ArrayList<>(), true)
         );
      }
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext context) {
      List<String> lore = new ArrayList<>();
      lore.add("\u00a7f" + this._t(this.getData().getTitle().colorless())[0]);
      lore.addAll(Arrays.asList(this._t(this.getFormatter().apply(this.getValue().get()))));
      return lore;
   }
}
