package id.seria.gacha.commands.subcommands;

import id.seria.gacha.api.sdk.exceptions.CommandContextException;
import id.seria.gacha.api.sdk.internal.Actor;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.managers.keys.Key;
import id.seria.gacha.sdk.core.commands.contexts.NodeInvocationContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TakeKeyCommand extends CrateCommandExecutable {
   public TakeKeyCommand() {
   }

   @Override
   public void onExecute(NodeInvocationContext context) {
      Actor issuer = context.getIssuer();
      Key key = context.getComputedParameter("key", Key.class);
      String targetInput = context.getParameterInput("player");
      Player target = context.getComputedParameter("player", Player.class);
      Integer amount = context.getComputedParameter("amount", Integer.class);
      boolean silent = context.getComputedParameter("-s", String.class).equalsIgnoreCase("-s");
      boolean virtualKeyMode = key.isVirtual();
      if (virtualKeyMode) {
         if (target == null) {
            this.processPlayerData(issuer, targetInput, data -> {
               data.setVirtualKeys(key.getIdentifier(), Math.max(0, data.getVirtualKeys(key.getIdentifier()) - amount));
               Translations.send(issuer, Translations.t("key-taken", "%amount%", amount, "%player%", targetInput));
            });
         } else {
            this.processPlayerData(issuer, target, data -> {
               data.setVirtualKeys(key.getIdentifier(), Math.max(0, data.getVirtualKeys(key.getIdentifier()) - amount));
               if (!silent) {
                  Translations.send(target, Translations.t("virtual-key-lost", "%display_name%", key.getFormattedDisplayName(target), "%amount%", amount)[0]);
               }

               Translations.send(issuer, Translations.t("key-taken", "%amount%", amount, "%player%", targetInput));
            });
         }
      } else {
         if (target == null) {
            throw new CommandContextException(Translatable.key("player-offline", "%name%", targetInput));
         }

         ItemStack keyItem = this.prepareCrateKey(key, amount);
         this.removeItemFromPlayer(target, keyItem);
         if (!silent) {
            Translations.send(target, Translations.t("key-lost", "%display_name%", key.getFormattedDisplayName(target), "%amount%", amount)[0]);
         }

         Translations.send(issuer, Translations.t("key-taken", "%amount%", amount, "%player%", targetInput));
      }
   }

   private void removeItemFromPlayer(Player player, ItemStack item) {
      int remainingKeys = item.getAmount();
      ItemStack[] armor = player.getInventory().getArmorContents();

      for (int i = 0; i < armor.length && remainingKeys > 0; i++) {
         ItemStack piece = armor[i];
         if (piece != null && piece.getType() != Material.AIR && piece.isSimilar(item)) {
            if (piece.getAmount() > remainingKeys) {
               piece.setAmount(piece.getAmount() - remainingKeys);
               remainingKeys = 0;
            } else {
               remainingKeys -= piece.getAmount();
               armor[i] = null;
            }
         }
      }

      player.getInventory().setArmorContents(armor);
      if (remainingKeys > 0) {
         ItemStack offhand = Utilities.getNMSFactory().wrapPlayer(player).getItemInOffHand();
         if (offhand != null && offhand.getType() != Material.AIR && offhand.isSimilar(item)) {
            if (offhand.getAmount() > remainingKeys) {
               offhand.setAmount(offhand.getAmount() - remainingKeys);
               remainingKeys = 0;
            } else {
               remainingKeys -= offhand.getAmount();
               offhand.setAmount(0);
            }
         }
      }

      for (int i = 0; i < player.getInventory().getSize() && remainingKeys > 0; i++) {
         ItemStack slot = player.getInventory().getItem(i);
         if (slot != null && slot.getType() != Material.AIR && slot.isSimilar(item)) {
            if (slot.getAmount() > remainingKeys) {
               slot.setAmount(slot.getAmount() - remainingKeys);
               remainingKeys = 0;
            } else {
               remainingKeys -= slot.getAmount();
               player.getInventory().clear(i);
            }
         }
      }
   }

   private ItemStack prepareCrateKey(Key key, int amount) {
      ItemStack item = key.createItemStack();
      item.setAmount(amount);
      return item;
   }
}
