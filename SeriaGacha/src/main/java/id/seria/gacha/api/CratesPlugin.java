package id.seria.gacha.api;

import id.seria.gacha.api.managers.CratesManager;
import id.seria.gacha.api.managers.KeysManager;
import id.seria.gacha.api.managers.PlayersManager;

public interface CratesPlugin {
   CratesManager getCratesManager();

   KeysManager getKeysManager();

   PlayersManager getPlayersManager();
}
