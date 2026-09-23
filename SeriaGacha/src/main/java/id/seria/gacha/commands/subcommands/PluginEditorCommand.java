package id.seria.gacha.commands.subcommands;

import id.seria.gacha.Crates;
import id.seria.gacha.commands.validations.CrateValidations;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.StringKeyStore;
import id.seria.gacha.lib.book.BookUtil;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PluginEditorCommand extends CrateCommandExecutable {
   public PluginEditorCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      CrateValidations.requirePlayerSender(context);
      Player player = (Player)context.getIssuer().raw();
      if (Crates.getPlugin().getEdition() == Edition.LITE && !StringKeyStore.contains("welcome-message")) {
         StringKeyStore.add("welcome-message");
         ItemStack book = BookUtil.writtenBook()
            .author("PhoenixPlugins")
            .title("Introducing Phoenix Crates")
            .pages(
               new BookUtil.PageBuilder()
                  .add(new TextComponent(ChatColor.DARK_GREEN + "Thank you for downloading Phoenix Crates!"))
                  .add(new TextComponent(ChatColor.RESET + " We hope you enjoy the plugin."))
                  .newLine()
                  .newLine()
                  .add(new TextComponent("You are using the Lite version, consider "))
                  .add(
                     BookUtil.TextBuilder.of("purchasing the premium on our website")
                        .color(ChatColor.DARK_PURPLE)
                        .onClick(BookUtil.ClickAction.openUrl("https://www.phoenixplugins.com/products/phoenix-crates"))
                        .onHover(BookUtil.HoverAction.showText("Open website"))
                        .build()
                  )
                  .add(new TextComponent(" for additional features."))
                  .newLine()
                  .newLine()
                  .add(
                     BookUtil.TextBuilder.of("Click here to get started.")
                        .color(ChatColor.DARK_AQUA)
                        .style(ChatColor.BOLD)
                        .onClick(BookUtil.ClickAction.runCommand("/pcrates editor"))
                        .onHover(BookUtil.HoverAction.showText("Open editor"))
                        .build()
                  )
                  .build()
            )
            .build();
         BookUtil.openPlayer(player, book);
      } else {
         EditorFacade.openEditor(player);
      }
   }
}
