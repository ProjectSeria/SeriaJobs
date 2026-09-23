package id.seria.gacha.editor.layouts.crate.rewards.reward.alternative;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.utils.inventory.CustomInventory;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomReward;
import java.util.List;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Deprecated
public class EditorCustomAlternativeRewardCommandsMenu extends EditorFacade.LegacyEditorLayout {
   private final CrateType crateType;
   @Deprecated
   private final CustomReward reward;
   private final EditorFacade.EditorLayout parent;

   @Deprecated
   public EditorCustomAlternativeRewardCommandsMenu(CrateType crateType, CustomReward reward, EditorFacade.EditorLayout parent) {
      super(null, 4);
      this.setTitle(this.t("editor.titles.crate.reward-commands")[0]);
      this.crateType = crateType;
      this.reward = reward;
      this.parent = parent;
   }

   @Override
   public void setup(Player player, Inventory inventory) {
      ItemStack glass = ItemBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE).setDisplayName("\u00a72").build();
      this.fillBottomBorder(glass);
      CustomInventory.PaginationWrapper holder = (CustomInventory.PaginationWrapper)inventory.getHolder();
      List<String> records = Lists.newArrayList(this.reward.getAlternative().getWinCommands());
      int maxItemsPerPage = 7;
      int maxPages = (int)Math.ceil((records.size() + 1) / 7.0);
      if (holder.getCurrentPage() != 1) {
         int previousPage = holder.getCurrentPage() - 1;
         this.setItem(
            1,
            2,
            ItemBuilder.of(XMaterial.ARROW)
               .setDisplayName(this.t("layout.extras.previous-page.title", "%page%", previousPage)[0])
               .setLore(this.t("layout.extras.previous-page.description")),
            __ -> {
               holder.setCurrentPage(holder.getCurrentPage() - 1);
               this.updateInventorySync(player);
            }
         );
      }

      if (holder.getCurrentPage() != maxPages && maxPages != 1) {
         int nextPage = holder.getCurrentPage() + 1;
         this.setItem(
            9,
            2,
            ItemBuilder.of(XMaterial.ARROW)
               .setDisplayName(this.t("layout.extras.next-page.title", "%page%", nextPage)[0])
               .setLore(this.t("layout.extras.next-page.description")),
            __ -> {
               holder.setCurrentPage(holder.getCurrentPage() + 1);
               this.updateInventorySync(player);
            }
         );
      }

      int i = (holder.getCurrentPage() - 1) * 7;

      for (int x = 2; x <= 8; x++) {
         if (i >= records.size()) {
            if (i == records.size()) {
               this.setItem(
                  x,
                  2,
                  ItemBuilder.of(XMaterial.LIME_STAINED_GLASS_PANE)
                     .setDisplayName(this.t("editor.reward-commands.add.title")[0])
                     .setLore(this.t("editor.reward-commands.add.description")),
                  __ -> {
                     Translations.send(player, "\u00a7aWrite the command on the chat without \"/\"");
                     Translations.send(player, "\u00a7aUse \u00a7e%player% \u00a7ato specific the player field");
                     player.closeInventory();
                     TextReader.askForInput(player, commandx -> {
                        if (commandx.startsWith("/")) {
                           commandx = commandx.substring(1);
                        } else if (commandx.startsWith("*/")) {
                           commandx = "*" + commandx.substring(2);
                        }

                        if (commandx != null) {
                           this.reward.getAlternative().getWinCommands().add(commandx);
                           this.save(player);
                        } else {
                           this.open(player);
                        }
                     });
                  }
               );
            }
            break;
         }

         String command = records.get(i);
         int pos = i + 1;
         String displayCommand;
         if (command.startsWith("*")) {
            displayCommand = "\u00a7a*\u00a77/" + command.substring(1);
         } else {
            displayCommand = "\u00a77/" + command;
         }

         this.setItem(
            x,
            2,
            ItemBuilder.of(XMaterial.NAME_TAG)
               .setDisplayName(this.t("editor.reward-commands.command.title", "%id%", pos)[0])
               .setLore(this.t("editor.reward-commands.command.description", "%command%", displayCommand)),
            __ -> {
               this.reward.getAlternative().getWinCommands().remove(command);
               this.save(player);
            }
         );
         i++;
      }

      this.setItem(5, 4, ItemBuilder.of(XMaterial.FEATHER).setDisplayName("\u00a76" + this.t("labels.go-back")[0]), __ -> this.parent.open(player));
   }

   public void save(Player player) {
      try {
         Crates.getCratesManager().saveCrateType(this.crateType);
         if (this.getViewers().contains(player)) {
            this.updateInventorySync(player);
         } else {
            this.open(player);
         }
      } catch (Exception e) {
         Translations.error(player, e);
         player.closeInventory();
      }
   }

   @Override
   public void open(Player player) {
      this.open(player, new EditorFacade.EditorPaginationWrapper(this));
   }

   @Generated
   public CrateType getCrateType() {
      return this.crateType;
   }

   @Deprecated
   @Generated
   public CustomReward getReward() {
      return this.reward;
   }

   @Generated
   public EditorFacade.EditorLayout getParent() {
      return this.parent;
   }
}
