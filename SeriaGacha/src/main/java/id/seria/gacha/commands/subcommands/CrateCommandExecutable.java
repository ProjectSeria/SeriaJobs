package id.seria.gacha.commands.subcommands;

import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.exceptions.ParameterParsingException;
import id.seria.gacha.api.sdk.exceptions.ParameterValidationException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.plugin.commands.components.Executable;
import id.seria.gacha.facades.PlayerFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.players.PlayerData;
import id.seria.gacha.sdk.core.commands.CommandTranslationKeys;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import java.util.function.Consumer;
import org.bukkit.entity.Player;

public abstract class CrateCommandExecutable implements Executable<NodeInvocationContext> {
   public CrateCommandExecutable() {
   }

   public void execute(NodeInvocationContext context) {
      try {
         this.onExecute(context);
      } catch (CommandContextException | ParameterValidationException | ParameterParsingException e) {
         throw e;
      } catch (Exception e) {
         Translations.error(context.getIssuer(), e);
      }
   }

   public abstract void onExecute(NodeInvocationContext var1) throws Exception;

   protected final void processPlayerData(Actor sender, String playerName, Consumer<PlayerData> onFetch) {
      this.processPlayerData(sender, playerName, onFetch, initial -> {
         try {
            throw initial;
         } catch (ParameterParsingException e) {
            sender.sendMessage(Translations.t(CommandTranslationKeys.error(e.getTranslation())));
         } catch (ParameterValidationException e) {
            sender.sendMessage(Translations.t(CommandTranslationKeys.error(e.getTranslation())));
         } catch (CommandContextException e) {
            sender.sendMessage(Translations.t(e.getTranslation()));
         } catch (Exception e) {
            Translations.error(sender, e);
         }
      });
   }

   protected final void processPlayerData(Actor sender, Player playerName, Consumer<PlayerData> onFetch) {
      this.processPlayerData(sender, playerName, onFetch, initial -> {
         try {
            throw initial;
         } catch (ParameterParsingException e) {
            sender.sendMessage(Translations.t(CommandTranslationKeys.error(e.getTranslation())));
         } catch (ParameterValidationException e) {
            sender.sendMessage(Translations.t(CommandTranslationKeys.error(e.getTranslation())));
         } catch (CommandContextException e) {
            sender.sendMessage(Translations.t(e.getTranslation()));
         } catch (Exception e) {
            Translations.error(sender, e);
         }
      });
   }

   protected final void processPlayerData(Actor sender, String playerName, Consumer<PlayerData> onFetch, Consumer<Exception> onFailure) {
      PlayerFacade.processPlayerData(playerName, onFetch, onFailure);
   }

   protected final void processPlayerData(Actor sender, Player player, Consumer<PlayerData> onFetch, Consumer<Exception> onFailure) {
      PlayerFacade.processPlayerData(player, onFetch, onFailure);
   }
}
