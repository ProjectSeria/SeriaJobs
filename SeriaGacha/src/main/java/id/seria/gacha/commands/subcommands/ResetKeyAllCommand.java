package id.seria.gacha.commands.subcommands;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.managers.players.PlayerData;
import id.seria.gacha.managers.players.PlayerDatabaseModelImpl;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ResetKeyAllCommand extends CrateCommandExecutable {
   private final AtomicBoolean IS_PROCESSING = new AtomicBoolean(false);

   public ResetKeyAllCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      Key key = context.getComputedParameter("key", Key.class);
      if (this.IS_PROCESSING.get()) {
         Translations.send(issuer, Translatable.literal("Currently processing..."));
      } else {
         Crates.getScheduler().runAsync(() -> {
            try {
               if (!this.IS_PROCESSING.get()) {
                  this.IS_PROCESSING.set(true);
                  this.processResetKeyAll(issuer, key);
                  return;
               }
            } finally {
               this.IS_PROCESSING.set(false);
            }
         });
      }
   }

   private void processResetKeyAll(Actor issuer, Key key) {
      Translations.send(issuer, "\u00a77Loading player data...");
      int current = 1;
      long millis = System.currentTimeMillis();
      String keyDisplayName = key.getFormattedDisplayName(Bukkit.getConsoleSender() instanceof Player ? (Player)Bukkit.getConsoleSender() : null);
      List<PlayerData> data = Crates.getPlayersManager().getAllPlayerDataFromDatabase();
      List<PlayerDatabaseModelImpl> modelsToUpdate = new ArrayList<>(data.size());
      Translations.send(issuer, "\u00a7aData loaded (" + (System.currentTimeMillis() - millis) + "ms). \u00a72Processing now...");
      long progressMillis = System.currentTimeMillis();

      for (PlayerData playerData : data) {
         playerData.setVirtualKeys(key.getIdentifier(), 0);
         if (playerData.isDirty()) {
            modelsToUpdate.add(playerData.getDatabaseModel());
            playerData.unmarkDirty();
         }

         if (System.currentTimeMillis() - progressMillis >= 1000L) {
            Translations.send(issuer, "\u00a7eProcessing player data... \u00a76(" + current + "/" + data.size() + ")");
            progressMillis = System.currentTimeMillis();
         }

         current++;
      }

      long updateMillis = System.currentTimeMillis();
      Crates.getPlayersManager().getDatabaseRepository().updateAllLater(modelsToUpdate).join();
      Translations.send(issuer, " \u00a7aAll " + keyDisplayName + " have been cleared. \u00a77(" + (System.currentTimeMillis() - updateMillis) + "ms)");
   }
}
