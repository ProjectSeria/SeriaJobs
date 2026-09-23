package id.seria.gacha.editor.layouts.crate.rerolls;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.LoreBuilder;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickActionsFacade;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.SimplePaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableBoolean;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableNumber;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.objects.TextReader;
import id.seria.gacha.lib.common.utils.utility.InventoryUtil;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.CratesManager;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.rerolling.RerollGroup;
import org.bukkit.entity.Player;

public class EditorCrateRerollingLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;

   public EditorCrateRerollingLayout(CrateType crateType, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.rerolling"), parent);
      this.crateType = crateType;
   }

   @Override
   public void onPrepare(ContainerView view) {
      this.addComponent(view, new EditorCrateRerollingLayout.PermissionsRollsComponent());
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(2, 4),
               this.createData(
                  XMaterial.GRAY_DYE,
                  Translatable.literal("&aEnabled"),
                  Translatable.literal("&7By clicking here you will be able", "&7to turn on/off rerolls on this crate.")
               ),
               this.label("enabled"),
               this.crateType.getRerolling().isEnabled()
            )
            .setOnValueChange(value -> {
               this.crateType.getRerolling().setEnabled(value);
               this.save(view);
            })
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(3, 4),
               this.createData(
                  XMaterial.SLIME_BALL,
                  Translatable.literal("&aFree Rerolls"),
                  Translatable.literal("&7By clicking here you will be able", "&7to define how many rerolls a common", "&7player have when opening this crate.")
               ),
               this.label("rerolls"),
               this.crateType.getRerolling().getFreeRolls()
            )
            .setOnValueChange(value -> {
               this.crateType.getRerolling().setFreeRolls(value.intValue());
               this.save(view);
            })
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(7, 4),
               this.createData(
                  XMaterial.CLOCK,
                  Translatable.literal("&aReplay Animation"),
                  Translatable.literal(
                     "&7By clicking here you will be able",
                     "&7to turn on/off replaying the opening",
                     "&7animation when the player rerolls.",
                     "&7The reroll menu will re-open once",
                     "&7the animation is done."
                  )
               ),
               this.label("enabled"),
               this.crateType.getRerolling().isReplayAnimation()
            )
            .setOnValueChange(value -> {
               this.crateType.getRerolling().setReplayAnimation(value);
               this.save(view);
            })
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(8, 4),
               this.createData(
                  XMaterial.HOPPER,
                  Translatable.literal("&aSkip Same Reward"),
                  Translatable.literal(
                     "&7By clicking here you will be able",
                     "&7to turn on/off giving the player a",
                     "&7reward they have already won while",
                     "&7rerolling on this opening."
                  )
               ),
               this.label("enabled"),
               this.crateType.getRerolling().isSkipSameReward()
            )
            .setOnValueChange(value -> {
               this.crateType.getRerolling().setSkipSameReward(value);
               this.save(view);
            })
            .setFixedPosition(true)
      );
   }

   private void save(ContainerView view) {
      Player player = view.getViewer();

      try {
         CratesManager manager = Crates.getCratesManager();
         if (!this.crateType.isRegistered()) {
            manager.registerCrateType(this.crateType, true);
         } else {
            manager.saveCrateType(this.crateType);
         }
      } catch (Exception e) {
         Translations.error(player, e);
         player.closeInventory();
      }
   }

   private class PermissionsRollsComponent extends SimplePaginatedList<RerollGroup> {
      public PermissionsRollsComponent() {
         super(
            SlotCompound.fromGrid(10, 1, 7),
            new PagingData(PaginationSettings.ItemsAlign.LEFT, InventoryUtil.getPositionRaw(1, 2), InventoryUtil.getPositionRaw(9, 2)),
            EditorCrateRerollingLayout.this.crateType.getRerolling().getGroups()
         );
         this.id = "permissions-rolls";
         this.setOnItemRender(
               (context, index, item, rerollGroup) -> item.withItem(XMaterial.PAPER)
                  .withDisplayName("&aGroup &e\"" + StringUtil.capitalize(rerollGroup.getNamespace()) + "\"")
                  .withLore(
                     ClickActionsFacade.applyActions(
                        EditorCrateRerollingLayout.this,
                        Lists.newArrayList(
                           new String[]{"", "\u00a7fPermission: &7" + rerollGroup.getPermission(), "\u00a7fRerolls: \u00a7e" + rerollGroup.getRerolls()}
                        ),
                        ClickAction.EDIT(ClickAction.ClickType.LEFT, EditorCrateRerollingLayout.this.label("group")),
                        ClickAction.REMOVE(ClickAction.ClickType.SHIFT_LEFT, EditorCrateRerollingLayout.this.label("group"))
                     )
                  )
            )
            .setOnItemClick(
               (context, index, rerollGroup) -> {
                  if (context.isLeftClick()) {
                     if (context.isShiftClick()) {
                        EditorCrateRerollingLayout.this.crateType.getRerolling().getGroups().remove(rerollGroup);
                        EditorCrateRerollingLayout.this.save(context.getContainerView());
                        context.getContainerView().updateView();
                        return;
                     }

                     new EditorCrateRerollGroupLayout(EditorCrateRerollingLayout.this.crateType, rerollGroup, EditorCrateRerollingLayout.this)
                        .open(context.getViewer());
                  }
               }
            )
            .setAddItemSlot(
               VirtualItem.from(XMaterial.LIME_STAINED_GLASS_PANE)
                  .withDisplayName("\u00a7aAdd Group")
                  .withLore(
                     LoreBuilder.builder(EditorCrateRerollingLayout.this)
                        .description(
                           "\u00a77By clicking here you will be able to",
                           "\u00a77add a group. A group is based on a permission",
                           "\u00a77that allows you to have extra rerolls."
                        )
                        .actions(ClickAction.ADD(ClickAction.ClickType.LEFT, EditorCrateRerollingLayout.this.label("group")))
                        .build()
                  )
                  .withFlag(Constants.ITEM_FLAGS)
                  .whenClick(
                     context -> {
                        Player player = context.getViewer();
                        Translations.send(player, "\u00a7eWrite the new group namespace in the chat.");
                        player.closeInventory();
                        TextReader.askForInput(
                           player,
                           input -> {
                              if (input != null) {
                                 try {
                                    RerollGroup rerollGroup = new RerollGroup(input.replace(" ", "_").toLowerCase(), 1);
                                    EditorCrateRerollingLayout.this.crateType.getRerolling().getGroups().add(rerollGroup);
                                    EditorCrateRerollingLayout.this.save(context.getContainerView());
                                    new EditorCrateRerollGroupLayout(EditorCrateRerollingLayout.this.crateType, rerollGroup, EditorCrateRerollingLayout.this)
                                       .open(context.getViewer());
                                 } catch (Exception var5) {
                                    Translations.send(player, "\u00a7cAn error has occurred:", var5.getMessage());
                                    return;
                                 }
                              }
                           }
                        );
                     }
                  )
            );
      }
   }
}
