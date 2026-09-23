package id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.data.ComponentData;
import id.seria.gacha.lib.common.uicomponents.newest.dialog.DialogUtil;
import id.seria.gacha.lib.common.uicomponents.newest.layout.ContainerLayout;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.EditableObject;
import id.seria.gacha.lib.common.uicomponents.newest.layout.modifiers.ObjectValueModifier;
import id.seria.gacha.lib.common.uicomponents.newest.limbo.ObjectValidationException;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.common.utils.version.ServerVersion;
import id.seria.gacha.lib.common.utils.version.VersionUtils;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import org.bukkit.entity.Player;

public class EditableString extends EditableObject<String> {
   @Deprecated
   public EditableString(ComponentData data, Supplier<String> object) {
      this(new SlotCompound(data.getSlot()), data, data.getTitle(), object.get());
   }

   public EditableString(SlotCompound slot, ComponentData data, String object) {
      this(slot, data, data.getTitle(), object);
   }

   public EditableString(SlotCompound slot, ComponentData data, Translatable displayType, String object) {
      super(slot, data, displayType, object);
      this.setFormatter(value -> value != null && !value.isEmpty() ? Translatable.literal(value) : Translatable.key("labels.empty"));
      this.setClickAction(
         ClickAction.CHANGE(ClickAction.ClickType.LEFT, this.displayType),
         context -> {
            Player player = context.getViewer();
            if (VersionUtils.isPaperServer() && ServerVersion.isGreaterEqualThan(ServerVersion.v1_21_R7)) {
               player.closeInventory();
               String dataTitle = this._t(this.getData().getTitle().colorless())[0];
               DialogUtil.askForInput(
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
                           player.sendMessage(
                              String.format("\u00a7a%s successfully updated to \"%s\u00a7a\"!", this._t(this.getData().getTitle().colorless())[0], input)
                           );
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
            } else {
               player.sendMessage(String.format("\u00a7eWrite the new %s\u00a7e in the chat.", this._t(this.getData().getTitle())[0].toLowerCase()));
               player.closeInventory();
               TextReader.askForInput(
                  player,
                  input -> {
                     if (input != null) {
                        try {
                           this.setValue(input);
                           player.sendMessage(
                              String.format("\u00a7a%s successfully updated to \"%s\u00a7a\"!", this._t(this.getData().getTitle().colorless())[0], input)
                           );
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
         }
      );
   }

   @Override
   public List<String> getRenderDisplay(RenderViewContext context) {
      return Arrays.asList(this._t("layout.displays.value", "%type%", this.displayType, "%value%", this.formatter.apply(this.getValue().get())));
   }

   public EditableString addModifier(ObjectValueModifier<String> processor) {
      this.modifiers.add(processor);
      return this;
   }
}
