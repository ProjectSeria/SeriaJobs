package id.seria.gacha.lib.common.uicomponents.newest.dialog;

import com.google.common.collect.Lists;
import id.seria.gacha.sdk.platforms.server.internal.MiniMessageCheck;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry.Builder;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput.MultilineOptions;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback.Options;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class DialogUtil {
   private static final int MIN_MAX_LENGTH = 256;
   private static final int MIN_MAX_LINES = 10;

   private DialogUtil() {
   }

   private static int maxLengthFor(String initial) {
      return initial == null ? 256 : Math.max(256, initial.length());
   }

   public static void askForInput(Player player, String title, String field, String prompt, @Nullable String initial, Consumer<String> consumer) {
      String initialValue = initial != null && !initial.trim().isEmpty() ? initial : "";
      Dialog dialog = Dialog.create(
         builder -> ((Builder)builder.empty())
            .base(
               DialogBase.builder(deserializeKyori(title))
                  .body(Lists.newArrayList(new PlainMessageDialogBody[]{DialogBody.plainMessage(deserializeKyori(prompt))}))
                  .inputs(
                     Lists.newArrayList(
                        new TextDialogInput[]{DialogInput.text("input", 200, deserializeKyori(field), true, initialValue, maxLengthFor(initialValue), null)}
                     )
                  )
                  .build()
            )
            .type(
               DialogType.multiAction(
                  Lists.newArrayList(
                     new ActionButton[]{
                        ActionButton.builder(deserializeKyori("<green>\u2714 <white>Save")).action(DialogAction.customClick((view, audience) -> {
                           String input = view.getText("input");
                           if (input != null) {
                              consumer.accept(input);
                           }
                        }, (Options)Options.builder().uses(1).build())).build()
                     }
                  ),
                  ActionButton.builder(deserializeKyori("<color:#e0741b>\u2b05 <white>Go Back"))
                     .action(DialogAction.customClick((view, audience) -> consumer.accept(null), (Options)Options.builder().uses(1).build()))
                     .build(),
                  2
               )
            )
      );

      try {
         Method showDialogMethod = player.getClass().getMethod("showDialog", DialogLike.class);
         showDialogMethod.invoke(player, dialog);
      } catch (ReflectiveOperationException e) {
         throw new RuntimeException("Failed to show dialog via reflection", e);
      }
   }

   public static void askForMultiInput(
      Player player, String title, String field, String prompt, @Nullable List<String> initial, Consumer<List<String>> consumer
   ) {
      String initialValue = initial != null && !initial.isEmpty() ? String.join("\n", initial) : "";
      int maxLines = Math.max(10, initial == null ? 0 : initial.size());
      Dialog dialog = Dialog.create(
         builder -> ((Builder)builder.empty())
            .base(
               DialogBase.builder(deserializeKyori(title))
                  .body(Lists.newArrayList(new PlainMessageDialogBody[]{DialogBody.plainMessage(deserializeKyori(prompt))}))
                  .inputs(
                     Lists.newArrayList(
                        new TextDialogInput[]{
                           DialogInput.text(
                              "input", 200, deserializeKyori(field), true, initialValue, maxLengthFor(initialValue), MultilineOptions.create(maxLines, 150)
                           )
                        }
                     )
                  )
                  .build()
            )
            .type(
               DialogType.multiAction(
                  Lists.newArrayList(
                     new ActionButton[]{
                        ActionButton.builder(deserializeKyori("<green>\u2714 <white>Save")).action(DialogAction.customClick((view, audience) -> {
                           String input = view.getText("input");
                           if (input != null) {
                              List<String> temp = Arrays.stream(input.split("\n")).map(String::trim).collect(Collectors.toList());
                              if (temp.size() == 1 && temp.get(0).isEmpty()) {
                                 consumer.accept(new ArrayList<>());
                              } else {
                                 consumer.accept(temp);
                              }
                           }
                        }, (Options)Options.builder().uses(1).build())).build()
                     }
                  ),
                  ActionButton.builder(deserializeKyori("<color:#e0741b>\u2b05 <white>Go Back"))
                     .action(DialogAction.customClick((view, audience) -> consumer.accept(null), (Options)Options.builder().uses(1).build()))
                     .build(),
                  2
               )
            )
      );

      try {
         Method showDialogMethod = player.getClass().getMethod("showDialog", DialogLike.class);
         showDialogMethod.invoke(player, dialog);
      } catch (ReflectiveOperationException e) {
         throw new RuntimeException("Failed to show dialog via reflection", e);
      }
   }

   public static void askForInputs(
      Player player, String title, String prompt, List<String> fieldIds, List<String> fieldLabels, List<String> initialValues, Consumer<List<String>> consumer
   ) {
      List<DialogInput> inputs = new ArrayList<>();

      for (int i = 0; i < fieldIds.size(); i++) {
         String initialValue = i < initialValues.size() && initialValues.get(i) != null ? initialValues.get(i) : "";
         inputs.add(DialogInput.text(fieldIds.get(i), 200, deserializeKyori(fieldLabels.get(i)), true, initialValue, maxLengthFor(initialValue), null));
      }

      Dialog dialog = Dialog.create(
         builder -> ((Builder)builder.empty())
            .base(
               DialogBase.builder(deserializeKyori(title))
                  .body(Lists.newArrayList(new PlainMessageDialogBody[]{DialogBody.plainMessage(deserializeKyori(prompt))}))
                  .inputs(inputs)
                  .build()
            )
            .type(
               DialogType.multiAction(
                  Lists.newArrayList(
                     new ActionButton[]{
                        ActionButton.builder(deserializeKyori("<green>\u2714 <white>Save")).action(DialogAction.customClick((view, audience) -> {
                           List<String> results = new ArrayList<>();

                           for (String id : fieldIds) {
                              String val = view.getText(id);
                              results.add(val != null ? val : "");
                           }

                           consumer.accept(results);
                        }, (Options)Options.builder().uses(1).build())).build()
                     }
                  ),
                  ActionButton.builder(deserializeKyori("<color:#e0741b>\u2b05 <white>Go Back"))
                     .action(DialogAction.customClick((view, audience) -> consumer.accept(null), (Options)Options.builder().uses(1).build()))
                     .build(),
                  2
               )
            )
      );

      try {
         Method showDialogMethod = player.getClass().getMethod("showDialog", DialogLike.class);
         showDialogMethod.invoke(player, dialog);
      } catch (ReflectiveOperationException e) {
         throw new RuntimeException("Failed to show dialog via reflection", e);
      }
   }

   private static Component deserializeKyori(String plainText) {
      return (Component)(MiniMessageCheck.isSyntaxCompatible(plainText)
         ? MiniMessage.miniMessage().deserialize(plainText)
         : LegacyComponentSerializer.legacyAmpersand().deserialize(plainText));
   }
}
