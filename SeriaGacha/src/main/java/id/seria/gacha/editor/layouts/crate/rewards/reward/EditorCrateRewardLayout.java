package id.seria.gacha.editor.layouts.crate.rewards.reward;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.api.internal.Item;
import id.seria.gacha.api.reward.GuaranteedRewardType;
import id.seria.gacha.api.reward.RewardsMode;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.editor.layouts.crate.rewards.reward.alternative.EditorCustomAlternativeRewardItemLayout;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.CloseViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.LinkedComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.NavigableComponent;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.bukkit.EditableItem;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.list.EditableListOption;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableBoolean;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableNumber;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.editable.primitive.EditableString;
import id.seria.gacha.lib.common.uicomponents.newest.layout.modifiers.ObjectModifiers;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XItemFlag;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.limbo.Edition;
import id.seria.gacha.limbo.Operations;
import id.seria.gacha.limbo.item.BaseItem;
import id.seria.gacha.limbo.menus.ConfirmationMenu;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.reward.CustomReward;
import id.seria.gacha.managers.reward.RewardMessageType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EditorCrateRewardLayout extends EditorFacade.EditorLayout {
   private final CrateType crateType;
   private final CustomReward reward;

   public EditorCrateRewardLayout(CrateType crateType, CustomReward reward, EditorFacade.EditorLayout parent) {
      super(Translatable.key("editor.titles.crate.reward"), parent);
      this.crateType = crateType;
      this.reward = reward;
      this.setViewPersistentOnGoBack(true);
   }

   @Override
   public void onPrepare(ContainerView view) {
      boolean lite = Crates.getPlugin().getEdition() == Edition.LITE;
      this.addComponent(
         view,
         new EditableItem(
               SlotCompound.from(5, 1),
               this.createData(
                  XMaterial.AIR, Translatable.key("editor.crate.reward.display-item.title"), Translatable.key("editor.crate.reward.display-item.description")
               ),
               Operations.o(this.reward.getDisplayItem()).map(Item::build).orElse(XMaterial.AIR.parseItem())
            )
            .setClickAction(
               ClickAction.EDIT(ClickAction.ClickType.LEFT, Translatable.key("labels.item")),
               (object, context) -> new EditorCrateRewardDisplayItemLayout(this.crateType, this.reward, this).open(context.getViewer())
            )
            .setClickAction(
               ClickAction.RECEIVE(ClickAction.ClickType.RIGHT, Translatable.key("labels.item")),
               (object, context) -> context.getViewer().getInventory().addItem(new ItemStack[]{Objects.requireNonNull(this.reward.getDisplayItem()).build()})
            )
            .setOnValueChange(value -> {
               this.crateType.invalidateRewardsCache();
               boolean wasPresent = this.reward.getDisplayItem() != null;
               this.reward.setDisplayItem(BaseItem.from(value));
               if (!wasPresent) {
                  Translations.send(view.getViewer(), "\u00a77Added display item as win items as well.");
                  this.reward.getWinItems().add(BaseItem.from(value));
               }

               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
      );
      this.addComponent(
         view,
         new NavigableComponent(
            SlotCompound.from(2, 3),
            this.createData(
               XMaterial.ORANGE_SHULKER_BOX.or(XMaterial.MINECART),
               Translatable.key("editor.crate.reward.win-items.title"),
               Translatable.key("editor.crate.reward.win-items.description")
            ),
            new EditorCrateRewardItemsLayout(this.crateType, this.reward, this)
         )
      );
      this.addComponent(
         view,
         new NavigableComponent(
            SlotCompound.from(3, 3),
            this.createData(
               XMaterial.COMMAND_BLOCK,
               Translatable.key("editor.crate.reward.win-commands.title"),
               Translatable.key("editor.crate.reward.win-commands.description")
            ),
            new EditorCrateRewardCommandsMenu(this.crateType, this.reward, this)
         )
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(4, 3),
               this.createData(
                  XMaterial.GUNPOWDER, Translatable.key("editor.crate.reward.win-limit.title"), Translatable.key("editor.crate.reward.win-limit.description")
               ),
               this.label("win-limit"),
               this.reward.getWinLimits()
            )
            .setFormatter(value -> Translatable.literal(value.intValue() < 0 ? "\u221e" : value.intValue()))
            .setClickAction(ClickAction.REMOVE(ClickAction.ClickType.RIGHT, Translatable.literal("1")), (editable, context) -> {
               int current = editable.getValue().get().intValue();
               editable.safeSetValue(context.getContainerView(), current == 1 ? -1 : current - 1, true, false);
            })
            .setClickAction(ClickAction.ADD(ClickAction.ClickType.LEFT, Translatable.literal("1")), (editable, context) -> {
               int current = editable.getValue().get().intValue();
               editable.safeSetValue(context.getContainerView(), current < 0 ? 1 : current + 1, true, false);
            })
            .setClickAction(
               ClickAction.SET(ClickAction.ClickType.SHIFT_LEFT, this.label("unlimited")),
               (editable, context) -> editable.safeSetValue(context.getContainerView(), -1, true, false)
            )
            .removeClickAction(ClickAction.ClickType.SHIFT_RIGHT)
            .setOnValueChange(value -> {
               this.reward.setWinLimits(value.intValue());
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
      );
      this.addComponent(
         view,
         new LinkedComponent(
               SlotCompound.from(6, 3),
               this.createData(
                  XMaterial.DIAMOND, Constants.addon("Rarity"), Translatable.literal("By clicking here you can select", "the rarity of the reward.")
               ),
               "crate.reward.rarity.editor"
            )
            .addAttribute(this.label("rarity"), "\u00a7cNot Set")
            .withMetadata("crateType", this.crateType)
            .withMetadata("reward", this.reward)
            .setFallbackError(Translatable.key("unlock-addon", "%addon%", "Rarity"))
            .setClickAction(ClickAction.CHOOSE(ClickAction.ClickType.LEFT, this.label("rarity")))
      );
      this.addComponent(
         view,
         (new EditableNumber(
               SlotCompound.from(5, 3),
               this.createData(
                  XMaterial.NETHER_STAR,
                  Translatable.literal("\u00a7aGuaranteed Win"),
                  Translatable.literal("By using this option you can", "guarantee that this reward will be won", "after/every X openings.")
               ),
               this.label("openings"),
               this.reward.getGuaranteedWin()
            ) {
               @Override
               public List<String> getRenderDisplay(RenderViewContext context) {
                  List<String> baseDisplay = new ArrayList<>();
                  if (!EditorCrateRewardLayout.this.crateType.getGuaranteedRewardType().equals(GuaranteedRewardType.DISABLED)) {
                     baseDisplay.add("\u00a7fMode: \u00a77" + StringUtil.humanize(EditorCrateRewardLayout.this.crateType.getGuaranteedRewardType()));
                  }

                  baseDisplay.addAll(super.getRenderDisplay(context));
                  return baseDisplay;
               }
            })
            .chatInput()
            .setFormatter(value -> Translatable.literal(value.intValue() <= 0 ? "Not Enabled" : value.intValue()))
            .setClickAction(
               ClickAction.legacy(ClickAction.ClickType.RIGHT, "Disable"), (obj, context) -> obj.safeSetValue(context.getContainerView(), -1, true)
            )
            .setOnValueChange(value -> {
               this.reward.setGuaranteedWin(value.intValue());
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
            .addError(this.crateType.getGuaranteedRewardType() == GuaranteedRewardType.DISABLED, "&cGuaranteed Reward not enabled on this crate.")
            .addError(this.crateType.getRewardsMode() == RewardsMode.SELECTIVE, "&cGuaranteed Reward not supported on selective mode.")
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(7, 3),
               this.createData(
                  XMaterial.OAK_SIGN,
                  Translatable.key("editor.crate.reward.allow-win-message.title"),
                  Translatable.key("editor.crate.reward.allow-win-message.description")
               ),
               this.label("enabled"),
               this.reward.isAllowWinMessage()
            )
            .setOnValueChange(value -> {
               this.reward.setAllowWinMessage(value);
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
            .addError(lite, Translatable.key("plans.unlock-premium"))
            .addError(lite, Translatable.key("plans.upgrade-at"))
            .setLocked(ctx -> lite)
      );
      if (this.crateType.getRewardsMode() == RewardsMode.RANDOM) {
         this.addComponent(
            view,
            new EditableNumber(
                  SlotCompound.from(2, 4),
                  this.createData(
                     XMaterial.HOPPER,
                     Translatable.concatenate(Translatable.key("editor.crate.reward.weight.title"), " \u00a78(Random Mode)"),
                     Translatable.key("editor.crate.reward.weight.description")
                  ),
                  this.label("weight"),
                  this.reward.getWeight()
               )
               .setNormalStep(ctx -> ctx.getValue().get().doubleValue() <= 1.0 ? 0.05 : 1.0)
               .setShiftStep(ctx -> ctx.getValue().get().doubleValue() <= 1.0 ? 0.1 : 10.0)
               .addModifier(ObjectModifiers.CLAMP(0, 100))
               .setOnValueChange(value -> {
                  this.reward.setWeight(value.doubleValue());
                  EditorFacade.saveCrate(view, this.crateType, this.reward);
                  this.updateView(view);
               })
               .addWarning(() -> !this.crateType.getRewardsMode().equals(RewardsMode.RANDOM), "&cOnly available with rewards \u00a7lrandom mode\u00a7c.")
               .setLocked(value -> !this.crateType.getRewardsMode().equals(RewardsMode.RANDOM))
         );
      } else {
         this.addComponent(
            view,
            new EditableNumber(
                  SlotCompound.from(2, 4),
                  this.createData(
                     XMaterial.TRIPWIRE_HOOK,
                     Translatable.literal("&aKey Requirement \u00a78(Selective Mode)"),
                     Translatable.literal("&7The keys required for the player", "&7to redeem this reward on select mode.")
                  ),
                  Translatable.literal("Required Keys"),
                  this.reward.getRequiredKeys()
               )
               .setFormatter(value -> Translatable.literal(value.intValue() < 0 ? "\u221e" : value.intValue()))
               .addModifier(ObjectModifiers.GREATHER_THEN_ONE)
               .setOnValueChange(value -> {
                  this.reward.setRequiredKeys(value.intValue());
                  EditorFacade.saveCrate(view, this.crateType, this.reward);
               })
               .addWarning(() -> !this.crateType.getRewardsMode().equals(RewardsMode.SELECTIVE), "&cOnly available with rewards \u00a7lselective mode\u00a7c.")
               .setLocked(ctx -> !this.crateType.getRewardsMode().equals(RewardsMode.SELECTIVE))
         );
      }

      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(3, 4),
               this.createData(
                  ItemBuilder.of(XMaterial.GOAT_HORN.or(XMaterial.NETHER_STAR).parseItem()).addItemFlags(Constants.ITEM_FLAGS),
                  Translatable.key("editor.crate.reward.broadcast.title"),
                  Translatable.key("editor.crate.reward.broadcast.description")
               ),
               this.label("broadcast"),
               this.reward.isBroadcast()
            )
            .setOnValueChange(value -> {
               this.reward.setBroadcast(value);
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(4, 4),
               this.createData(
                  XMaterial.EMERALD,
                  Translatable.key("editor.crate.reward.share-players-online.title"),
                  Translatable.key("editor.crate.reward.share-players-online.description")
               ),
               this.label("sharing"),
               this.reward.isSharePlayersOnline()
            )
            .setOnValueChange(value -> {
               this.reward.setSharePlayersOnline(value);
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
            .addError(lite, Translatable.key("plans.unlock-premium"))
            .addError(lite, Translatable.key("plans.upgrade-at"))
            .setLocked(ctx -> lite)
      );
      this.addComponent(
         view,
         new NavigableComponent(
            SlotCompound.from(5, 4),
            this.createData(
               XMaterial.BOOK, Translatable.key("editor.crate.reward.requirements.title"), Translatable.key("editor.crate.reward.requirements.description")
            ),
            new EditorCrateRewardRequirementsLayout(this.crateType, this.reward, this)
         )
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(6, 4),
               this.createData(
                  XMaterial.BLAZE_ROD,
                  Translatable.key("editor.crate.reward.alternative-reward.title"),
                  Translatable.key("editor.crate.reward.alternative-reward.description")
               ),
               this.label("enabled"),
               this.reward.getAlternative().isEnabled()
            )
            .swapClickAction(ClickAction.ClickType.LEFT, ClickAction.ClickType.RIGHT)
            .setClickAction(
               ClickAction.NAVIGATE,
               (editable, context) -> new EditorCustomAlternativeRewardItemLayout(this.crateType, this.reward, this).open(context.getViewer())
            )
            .setOnValueChange(value -> this.reward.getAlternative().setEnabled(value))
            .addWarning(() -> !this.crateType.getRewardsMode().equals(RewardsMode.RANDOM), "&cOnly available with rewards \u00a7lrandom mode\u00a7c.")
            .setLocked(value -> !this.crateType.getRewardsMode().equals(RewardsMode.RANDOM))
      );
      this.addComponent(
         view,
         new EditableString(
               SlotCompound.from(3, 6),
               this.createData(
                  XMaterial.NAME_TAG, Translatable.key("editor.crate.reward.identifier.title"), Translatable.key("editor.crate.reward.identifier.description")
               ),
               this.reward.getIdentifier()
            )
            .addValidator(input -> !input.replace(" ", "").isEmpty(), Translatable.key("editor.errors.invalid-input"))
            .addValidator(input -> !input.contains(" "), Translatable.key("editor.errors.no-spaces"))
            .addModifier(input -> ObjectModifiers.ID_NORMALIZER.process(input))
            .addValidator(input -> this.getRewardByIdentifier(input) == null, Translatable.key("editor.errors.identifier-already-in-use"))
            .setOnValueChange(value -> {
               this.reward.setIdentifier(value);
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         this.createComponent(
               SlotCompound.from(7, 6),
               this.createData(
                  XMaterial.BARRIER, Translatable.key("editor.crate.reward.delete.title"), Translatable.key("editor.crate.reward.delete.description")
               ),
               Lists.newArrayList(new ClickAction[]{ClickAction.DELETE(ClickAction.ClickType.LEFT, this.label("reward"))}),
               context -> {
                  if (this.reward.isRegistered()) {
                     Player player = context.getViewer();
                     ConfirmationMenu confirmation = new ConfirmationMenu(this.plugin, "Do you want to delete this reward?", () -> {
                        try {
                           this.crateType.removeReward(this.reward);
                           Crates.getCratesManager().saveCrateType(this.crateType);
                           this.getParent().open(player);
                        } catch (Exception e) {
                           Translations.error(player, e);
                           player.closeInventory();
                        }
                     }, () -> this.open(player));
                     confirmation.open(player);
                  }
               }
            )
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         new EditableNumber(
               SlotCompound.from(7, 4),
               this.createData(
                  XMaterial.NETHERITE_INGOT,
                  Translatable.key("editor.crate.reward.global-win-limit.title"),
                  Translatable.key("editor.crate.reward.global-win-limit.description")
               ),
               this.label("global-win-limit"),
               this.reward.getGlobalWinLimit()
            )
            .setFormatter(value -> Translatable.literal(value.intValue() < 0 ? "\u221e" : value.intValue()))
            .setClickAction(ClickAction.REMOVE(ClickAction.ClickType.RIGHT, Translatable.literal("1")), (editable, context) -> {
               int current = editable.getValue().get().intValue();
               editable.safeSetValue(context.getContainerView(), current == 1 ? -1 : current - 1, true, false);
            })
            .setClickAction(ClickAction.ADD(ClickAction.ClickType.LEFT, Translatable.literal("1")), (editable, context) -> {
               int current = editable.getValue().get().intValue();
               editable.safeSetValue(context.getContainerView(), current < 0 ? 1 : current + 1, true, false);
            })
            .setClickAction(
               ClickAction.SET(ClickAction.ClickType.SHIFT_LEFT, this.label("unlimited")),
               (editable, context) -> editable.safeSetValue(context.getContainerView(), -1, true, false)
            )
            .removeClickAction(ClickAction.ClickType.SHIFT_RIGHT)
            .setOnValueChange(value -> {
               this.reward.setGlobalWinLimit(value.intValue());
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
      );
      this.addComponent(
         view,
         new NavigableComponent(
               SlotCompound.from(8, 4),
               this.createData(
                  XMaterial.CLOCK, Translatable.key("editor.crate.reward.cooldowns.title"), Translatable.key("editor.crate.reward.cooldowns.description")
               ),
               new EditorCrateRewardCooldownsLayout(this.crateType, this.reward, this)
            )
            .addError(lite, Translatable.key("plans.unlock-premium"))
            .addError(lite, Translatable.key("plans.upgrade-at"))
            .setLocked(ctx -> lite)
      );
      this.addComponent(
         view,
         new EditableListOption<>(
               SlotCompound.from(8, 3),
               this.createData(
                  ItemBuilder.of(XMaterial.MUSIC_DISC_CAT.or(XMaterial.JUKEBOX)).addItemFlags(List.of(XItemFlag.HIDE_ADDITIONAL_TOOLTIP)),
                  Translatable.key("editor.crate.reward.notification-types.title"),
                  Translatable.key("editor.crate.reward.notification-types.description")
               ),
               Arrays.asList(RewardMessageType.values()),
               this.reward.getRewardMessageType()
            )
            .setItemFormatter(rewardMessageType -> Translatable.literal(StringUtil.humanize(rewardMessageType)))
            .setOnOptionChange(value -> {
               this.reward.setRewardMessageType(value);
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
            .addError(lite, Translatable.key("plans.unlock-premium"))
            .addError(lite, Translatable.key("plans.upgrade-at"))
            .setLocked(ctx -> lite)
            .setFixedPosition(true)
      );
      this.addComponent(
         view,
         new EditableBoolean(
               SlotCompound.from(2, 6),
               this.createData(
                  XMaterial.LIME_DYE, Translatable.key("editor.crate.reward.enabled.title"), Translatable.key("editor.crate.reward.enabled.description")
               ),
               this.label("enabled"),
               this.reward.isEnabled()
            )
            .setOnValueChange(value -> {
               this.crateType.invalidateRewardsCache();
               this.reward.setEnabled(value);
               EditorFacade.saveCrate(view, this.crateType, this.reward);
            })
            .setFixedPosition(true)
      );
   }

   private CustomReward getRewardByIdentifier(String identifier) {
      return this.crateType.getRegisteredRewards().stream().filter(r -> r.getIdentifier().equalsIgnoreCase(identifier)).findFirst().orElse(null);
   }

   @Override
   public void onClose(CloseViewContext context) {
      EditorFacade.saveCrate(context.getHolder(), this.crateType, this.reward);
   }

   @Generated
   public CrateType getCrateType() {
      return this.crateType;
   }

   @Generated
   public CustomReward getReward() {
      return this.reward;
   }
}
