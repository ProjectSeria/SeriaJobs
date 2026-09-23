package id.seria.gacha.lib.common.uicomponents.newest.actions.builder.types;

import com.google.common.collect.Lists;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionTypeBuilderLayout;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableListSingleOption;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableStringList;
import id.seria.gacha.lib.common.utils.actions.Action;
import id.seria.gacha.lib.common.utils.actions.ActionType;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandActionBuilderLayout extends ActionTypeBuilderLayout<CommandActionBuilderLayout.Holder> {
   private static final List<String> TARGETS = Arrays.asList("console", "player");

   public CommandActionBuilderLayout(ActionBuilderLayout builder, ActionType type) {
      super(builder, type);
   }

   public void onPrepareBuilder(ContainerView view, CommandActionBuilderLayout.Holder holder) {
      this.addComponent(
         view,
         new EditableListSingleOption<>(
               SlotCompound.from(4, 2),
               this.createData(
                  XMaterial.SLIME_BALL,
                  Translatable.key("layout.actions.builder.fields.command-target.title"),
                  Translatable.key("layout.actions.builder.fields.command-target.description", "%type%", this.typeName())
               ),
               TARGETS,
               holder.target
            )
            .setItemFormatter(value -> Translatable.literal(StringUtil.humanize(value)))
            .setOnOptionChange(value -> holder.target = value)
      );
      this.addComponent(
         view,
         new EditableStringList(
               SlotCompound.from(6, 2),
               this.createData(
                  XMaterial.COMMAND_BLOCK,
                  Translatable.key("layout.actions.builder.fields.command.title"),
                  Translatable.key("layout.actions.builder.fields.command.description", "%type%", this.typeName())
               ),
               Translatable.key("layout.actions.builder.fields.command.label"),
               holder.commands
            )
            .setOnValueChange(value -> holder.commands = value)
      );
   }

   public CommandActionBuilderLayout.Holder createHolder() {
      return new CommandActionBuilderLayout.Holder();
   }

   public boolean canCreate(CommandActionBuilderLayout.Holder holder) {
      return !validCommands(holder).isEmpty();
   }

   protected String[] arguments(CommandActionBuilderLayout.Holder holder) {
      return new String[]{holder.target, validCommands(holder).get(0)};
   }

   protected List<Action> createActions(CommandActionBuilderLayout.Holder holder) {
      List<Action> actions = Lists.newArrayList();

      for (String command : validCommands(holder)) {
         actions.add(this.type.createAction(new String[]{holder.target, command}));
      }

      return actions;
   }

   private static List<String> validCommands(CommandActionBuilderLayout.Holder holder) {
      List<String> commands = Lists.newArrayList();

      for (String command : holder.commands) {
         if (!command.trim().isEmpty()) {
            commands.add(command);
         }
      }

      return commands;
   }

   public static class Holder {
      private String target = CommandActionBuilderLayout.TARGETS.get(0);
      private List<String> commands = new ArrayList<>();

      public Holder() {
      }
   }
}
