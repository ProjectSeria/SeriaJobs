package id.seria.gacha.lib.common.utils.actions;

import id.seria.gacha.lib.common.utils.actions.exceptions.ActionExecutionArgumentException;
import id.seria.gacha.lib.common.utils.actions.exceptions.ActionExecutionException;
import id.seria.gacha.lib.common.utils.actions.internal.DelayAction;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ActionsFacade {
   public ActionsFacade() {
   }

   public static void executeSerializedActions(ActionsFactory factory, ServerPlugin plugin, Collection<String> serializedActions, Player player) {
      if (player.isOnline()) {
         List<Action> actions = new ArrayList<>();

         for (String rawAction : serializedActions) {
            actions.add(factory.unserialize(rawAction));
         }

         executeActions(plugin, actions, player);
      }
   }

   public static void executeSerializedActions(ActionsFactory factory, ServerPlugin plugin, Collection<String> serializedActions, OfflinePlayer player) {
      if (player.isOnline()) {
         List<Action> actions = new ArrayList<>();

         for (String rawAction : serializedActions) {
            actions.add(factory.unserialize(rawAction));
         }

         executeActions(plugin, actions, player);
      }
   }

   public static void executeActions(ServerPlugin plugin, Collection<Action> actions, OfflinePlayer player) {
      if (player.isOnline()) {
         List<Action> copy = new ArrayList<>(actions);

         for (Action action : actions) {
            copy.remove(action);
            if (action instanceof DelayAction) {
               double delay = ((DelayAction)action).getDelaySeconds();
               int delayTicks = (int)(delay * 20.0);
               if (delayTicks > 0) {
                  plugin.getScheduler().runSync(() -> executeActions(plugin, copy, player), delayTicks);
                  return;
               }
            } else {
               Player online = player.getPlayer();

               try {
                  action.execute(new ActionExecutor(action, plugin, online));
               } catch (ActionExecutionArgumentException e) {
                  online.sendMessage("\u00a7cAn error occurred while processing an action:");
                  online.sendMessage("\u00a77Invalid arguments for action \"" + action.getName() + "\". Expected syntax: " + action.getArgumentsSyntax());
                  plugin.getLogger().warn("Invalid arguments for action \"" + action.getName() + "\". Expected syntax: " + action.getArgumentsSyntax());
               } catch (ActionExecutionException e) {
                  throw new RuntimeException(e);
               }
            }
         }
      }
   }
}
