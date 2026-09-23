package id.seria.gacha.editor.layouts.crate.rewards.reward;

import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.LoreBuilder;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.CloseViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.dialog.DialogUtil;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.SimplePaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableNumber;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.managers.CratesManager;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomReward;
import java.util.function.Consumer;
import lombok.Generated;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class EditorCrateRewardCommandsMenu extends EditorFacade.EditorLayout {
   private final CrateType crateType;
   private final CustomReward reward;

   public EditorCrateRewardCommandsMenu(CrateType crateType, CustomReward reward, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.reward-commands"), parent);
      this.crateType = crateType;
      this.reward = reward;
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(view, view.computeMetadataIfAbsent("commands-list", () -> new EditorCrateRewardCommandsMenu.CommandsListComponent()));
      int cmdsMax = this.reward.getWinCommands().size();
      int cmdAmountNormalized = cmdsMax == 0 ? -1 : Math.min(Math.max(-1, this.reward.getMaxWinCommandAmount()), cmdsMax);
      this.reward.setMaxWinCommandAmount(cmdAmountNormalized);
      boolean lite = Crates.getPlugin().getEdition() == Edition.LITE;
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(2, 6),
               this.createData(
                  XMaterial.COMMAND_BLOCK,
                  Translatable.key("editor.crate.reward.max-win-command-amount.title"),
                  Translatable.key("editor.crate.reward.max-win-command-amount.description")
               ),
               this.label("amount"),
               cmdAmountNormalized
            )
            .setFormatter(value -> Translatable.literal(value.intValue() < 0 ? "\u221e" : value.intValue()))
            .setClickAction(ClickAction.REMOVE(ClickAction.ClickType.RIGHT, Translatable.literal("1")), (editable, context) -> {
               int current = editable.getValue().get().intValue();
               if (current > -1) {
                  editable.safeSetValue(context.getContainerView(), current == 1 ? -1 : current - 1, true, false);
               }
            })
            .setClickAction(ClickAction.ADD(ClickAction.ClickType.LEFT, Translatable.literal("1")), (editable, context) -> {
               int current = editable.getValue().get().intValue();
               int max = this.reward.getWinCommands().size();
               if (current < max) {
                  editable.safeSetValue(context.getContainerView(), current < 0 ? 1 : current + 1, true, false);
               }
            })
            .setClickAction(
               ClickAction.SET(ClickAction.ClickType.SHIFT_LEFT, this.label("unlimited")),
               (editable, context) -> editable.safeSetValue(context.getContainerView(), -1, true, false)
            )
            .removeClickAction(ClickAction.ClickType.SHIFT_RIGHT)
            .setOnValueChange(value -> {
               int max = this.reward.getWinCommands().size();
               int clamped = max == 0 ? -1 : Math.min(Math.max(-1, value.intValue()), max);
               this.reward.setMaxWinCommandAmount(clamped);
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
            .addWarning(() -> lite, Translatable.key("plans.unlock-premium"))
            .addWarning(() -> lite, Translatable.key("plans.upgrade-at"))
            .setLocked(value -> lite)
      );
   }

   @Override
   public void onClose(CloseViewContext context) {
      EditorFacade.saveCrate(context.getHolder(), this.crateType, this.reward);
   }

   private void save(ContainerView view) {
      Player player = view.getViewer();

      try {
         if (!this.reward.isRegistered()) {
            this.crateType.addReward(this.reward);
         }

         CratesManager manager = Crates.getCratesManager();
         if (this.crateType.isRegistered()) {
            manager.saveCrateType(this.crateType);
         } else {
            manager.registerCrateType(this.crateType, true);
         }
      } catch (Exception e) {
         Translations.error(player, e);
         player.closeInventory();
      }
   }

   private void saveAndOpen(Player player) {
      try {
         if (!this.reward.isRegistered()) {
            this.crateType.addReward(this.reward);
         }

         CratesManager manager = Crates.getCratesManager();
         if (this.crateType.isRegistered()) {
            manager.saveCrateType(this.crateType);
         } else {
            manager.registerCrateType(this.crateType, true);
         }

         this.open(player);
      } catch (Exception e) {
         Translations.error(player, e);
         player.closeInventory();
      }
   }

   @Generated
   public CrateType getCrateType() {
      return this.crateType;
   }

   @Generated
   public CustomReward getReward() {
      return this.reward;
   }

   private class CommandsListComponent extends SimplePaginatedList<String> {
      public CommandsListComponent() {
         super(
            SlotCompound.fromGrid(10, 3, 7),
            new PagingData(PaginationSettings.ItemsAlign.LEFT, SlotCompound.from(1, 3), SlotCompound.from(9, 3)),
            EditorCrateRewardCommandsMenu.this.reward.getWinCommands()
         );
         this.setOnItemRender(
            (context, index, item, command) -> {
               String displayCommand = command.startsWith("*") ? "\u00a7a*\u00a77/" + command.substring(1) : "\u00a77/" + command;
               item.withItem(ItemBuilder.of(XMaterial.NAME_TAG).build())
                  .withDisplayName(EditorCrateRewardCommandsMenu.this.t("editor.reward-commands.command.title", "%id%", index + 1)[0])
                  .withLore(
                     LoreBuilder.builder(EditorCrateRewardCommandsMenu.this)
                        .description(Translatable.literal(displayCommand))
                        .actions(ClickAction.REMOVE(ClickAction.ClickType.LEFT, EditorCrateRewardCommandsMenu.this.label("command")))
                        .build()
                  );
            }
         );
         this.setOnItemClick((context, index, command) -> {
            if (context.isLeftClick()) {
               EditorCrateRewardCommandsMenu.this.reward.getWinCommands().remove(command);
               EditorCrateRewardCommandsMenu.this.save(context.getContainerView());
               context.getContainerView().updateView();
            }
         });
      }

      @Override
      public VirtualItem onPrepareAddItem(RenderViewContext context) {
         return VirtualItem.from(XMaterial.LIME_STAINED_GLASS_PANE)
            .withDisplayName(EditorCrateRewardCommandsMenu.this.t("editor.reward-commands.add.title")[0])
            .withLore(EditorCrateRewardCommandsMenu.this.t("editor.reward-commands.add.description"))
            .whenClick(
               click -> {
                  Player viewer = click.getViewer();
                  viewer.closeInventory();
                  Consumer<String> handler = input -> {
                     if (input != null) {
                        String cmd = ChatColor.stripColor(input);
                        if (cmd.startsWith("*/")) {
                           cmd = "*" + cmd.substring(2);
                        } else if (cmd.startsWith("/")) {
                           cmd = cmd.substring(1);
                        }

                        EditorCrateRewardCommandsMenu.this.reward.getWinCommands().add(cmd);
                        EditorCrateRewardCommandsMenu.this.saveAndOpen(viewer);
                     } else {
                        EditorCrateRewardCommandsMenu.this.open(viewer);
                     }
                  };

                  try {
                     DialogUtil.askForInput(
                        viewer,
                        EditorCrateRewardCommandsMenu.this.t(Translatable.key("editor.titles.crate.reward-commands"))[0],
                        EditorCrateRewardCommandsMenu.this.t(EditorCrateRewardCommandsMenu.this.label("command"))[0],
                        "<white>Enter the <green>command<white> to run when this reward is won.",
                        null,
                        handler
                     );
                  } catch (Throwable ignored) {
                     TextReader.askForInput(Translations.getProvider(), viewer, handler);
                  }
               }
            );
      }
   }
}
