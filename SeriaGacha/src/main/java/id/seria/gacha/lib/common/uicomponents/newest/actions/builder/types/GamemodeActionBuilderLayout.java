package id.seria.gacha.lib.common.uicomponents.newest.actions.builder.types;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.actions.builder.ActionBuilderLayout;
import id.seria.gacha.lib.common.utils.actions.ActionType;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.GameMode;

public class GamemodeActionBuilderLayout extends ChoiceActionBuilderLayout {
   public GamemodeActionBuilderLayout(ActionBuilderLayout builder, ActionType type) {
      super(builder, type, XMaterial.SLIME_BALL, Translatable.key("layout.actions.builder.fields.gamemode.label"), getGamemodes());
   }

   private static List<String> getGamemodes() {
      List<String> gamemodes = new ArrayList<>();

      for (GameMode gamemode : GameMode.values()) {
         gamemodes.add(gamemode.name().toLowerCase());
      }

      return gamemodes;
   }
}
