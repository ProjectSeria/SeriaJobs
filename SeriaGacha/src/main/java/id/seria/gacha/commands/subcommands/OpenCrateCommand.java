package id.seria.gacha.commands.subcommands;

import id.seria.gacha.Crates;
import id.seria.gacha.api.crate.events.CratePreOpenEvent;
import id.seria.gacha.api.crate.exceptions.CrateOpeningException;
import id.seria.gacha.api.crate.exceptions.CratePreviewException;
import id.seria.gacha.api.reward.RewardsMode;
import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.api.session.OpeningContext;
import id.seria.gacha.facades.CrateFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.session.CrateOpeningSession;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class OpenCrateCommand extends CrateCommandExecutable {
   public OpenCrateCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      if (!issuer.isPlayer()) {
         throw new CommandContextException(Translatable.literal(Translations.t("only-ingame-players")[0]));
      }

      Player player = (Player)issuer.raw();
      CrateType crateType = context.getComputedParameter("crate", CrateType.class);
      if (!crateType.isAllowOpenCommand()) {
         Translations.send(issuer, Translations.t("crate-open-command-disabled", "%crate%", crateType.getIdentifier()));
      } else {
         Crate crate = Crates.getCratesManager().getAnyCrateByType(crateType);
         if (crate == null) {
            Translations.send(issuer, Translations.t("crate-no-placed-instances", "%crate%", crateType.getIdentifier()));
         } else {
            try {
               CratePreOpenEvent preOpenEvent = new CratePreOpenEvent(crate, player);
               Bukkit.getPluginManager().callEvent(preOpenEvent);
               if (preOpenEvent.isCancelled()) {
                  return;
               }

               if (crateType.getRewardsMode() == RewardsMode.SELECTIVE) {
                  CrateFacade.openCrateWithMoneyConfirmation(crate, player, () -> CrateFacade.openRewardMenu(crate, player, true));
                  return;
               }

               CrateFacade.openCrateWithMoneyConfirmation(crate, player, () -> {
                  crate.openCrate(OpeningContext.random(player, false, 1));
                  if (!crateType.hasVirtualOpenPhase()) {
                     List<CrateOpeningSession> sessions = Crate.OPENING_SESSIONS.get(player.getUniqueId());
                     if (!sessions.isEmpty()) {
                        crate.unloadSession(sessions.get(sessions.size() - 1), true);
                     }
                  }
               });
            } catch (CrateOpeningException | CratePreviewException e) {
               Translations.send(issuer, e.getMessage());
            }
         }
      }
   }
}
