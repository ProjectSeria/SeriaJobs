package id.seria.gacha.editor.layouts.crate.animations.opening;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.api.crate.animation.AnimationPhase;
import id.seria.gacha.api.crate.animation.AnimationProperties;
import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.editor.EditorFacade;
import id.seria.gacha.editor.layouts.crate.EditorCrateMainSettingsLayout;
import id.seria.gacha.editor.layouts.crate.animations.opening.data.ColoredPhaseDataBuilderMenu;
import id.seria.gacha.editor.layouts.crate.animations.opening.data.ColoredSkullPhaseDataBuilderMenu;
import id.seria.gacha.editor.layouts.crate.animations.opening.data.KeybasedPhaseDataBuilderMenu;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.LoreBuilder;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.actions.ClickAction;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.inventory.CustomInventory;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.xseries.XEnchantment;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.animations.opening.OpeningPhase;
import id.seria.gacha.managers.crates.animations.opening.data.ColoredOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.data.ColoredSkullOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.data.EmptyOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.data.KeybasedOpeningPhaseData;
import id.seria.gacha.managers.crates.type.list.OpeningAnimation;
import id.seria.gacha.registries.AnimationsRegistry;
import id.seria.gacha.registries.animation.AddonAnimationPhaseImpl;
import id.seria.gacha.registries.animation.AddonLockedAnimationPhaseImpl;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

@Deprecated
public class EditorCrateCreateOpeningAnimation extends CustomInventory {
   private static final ItemStack SAVE_SUCCESS_SKULL = Utilities.getNMSFactory()
      .createTexturedSkullFromUrl("http://textures.minecraft.net/texture/a92e31ffb59c90ab08fc9dc1fe26802035a3a47c42fee63423bcdb4262ecb9b6");
   private final CrateType crateType;
   private OpeningAnimation animation;
   private final EditorFacade.EditorLayout parent;

   public EditorCrateCreateOpeningAnimation(PhoenixCrates plugin, CrateType crateType, EditorFacade.EditorLayout parent) {
      super(plugin, Translations.t("editor.titles.crate.opening-animation", "%crate%", StringUtil.humanize(crateType.getIdentifier()))[0], 6);
      this.crateType = crateType;
      this.parent = parent;
   }

   public EditorCrateCreateOpeningAnimation(PhoenixCrates plugin, CrateType crateType, EditorFacade.EditorLayout parent, OpeningAnimation animation) {
      super(plugin, Translations.t("editor.titles.crate.opening-animation", "%crate%", StringUtil.humanize(crateType.getIdentifier()))[0], 6);
      this.crateType = crateType;
      this.parent = parent;
      this.animation = animation;
   }

   @Override
   public void setup(Player player, Inventory inventory) {
      EditorCrateCreateOpeningAnimation.EditorOpeningPhasesHolder holder = (EditorCrateCreateOpeningAnimation.EditorOpeningPhasesHolder)inventory.getHolder();
      List<AnimationPhase> records = new ArrayList<>(AnimationsRegistry.getInstance().getPhases(holder.getCurrentPhaseType()));
      int maxItemsPerPage = 14;
      int maxPages = (int)Math.ceil(records.size() / 14.0);
      ItemStack glass = ItemBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE).setDisplayName("\u00a72").build();
      this.fillTopBorder(glass);
      this.fillBottomBorder(glass);
      this.setItem(
         4,
         1,
         ItemBuilder.of(holder.firstPhase == null ? XMaterial.IRON_BARS.parseItem() : holder.firstPhase.getPhase().getDisplayItem())
            .setDisplayName(
               Translations.t("editor.edit-opening-animation.phase-navbar.title", "%phase%", Translations.t("labels.first"))[0]
                  + (holder.currentPhaseType == PhaseType.PRE_OPENING ? " \u00a7e(" + Translations.t("labels.current")[0] + ")" : "")
            )
            .setLore(
               Translations.t(
                  "editor.edit-opening-animation.phase-navbar.description",
                  "%name%",
                  holder.firstPhase == null ? Translations.t("labels.to-choose")[0] : holder.firstPhase.getPhase().getDisplayName()
               )
            ),
         __ -> {
            holder.setCurrentPhaseType(PhaseType.PRE_OPENING);
            holder.setCurrentPage(1);
            this.updateInventorySync(player);
         }
      );
      this.setItem(
         5,
         1,
         ItemBuilder.of(holder.secondPhase == null ? XMaterial.IRON_BARS.parseItem() : holder.secondPhase.getPhase().getDisplayItem())
            .setDisplayName(
               Translations.t("editor.edit-opening-animation.phase-navbar.title", "%phase%", Translations.t("labels.second"))[0]
                  + (holder.currentPhaseType == PhaseType.OPENING ? " \u00a7e(" + Translations.t("labels.current")[0] + ")" : "")
            )
            .setLore(
               Translations.t(
                  "editor.edit-opening-animation.phase-navbar.description",
                  "%name%",
                  holder.secondPhase == null ? Translations.t("labels.to-choose")[0] : holder.secondPhase.getPhase().getDisplayName()
               )
            ),
         __ -> {
            holder.setCurrentPhaseType(PhaseType.OPENING);
            holder.setCurrentPage(1);
            this.updateInventorySync(player);
         }
      );
      this.setItem(
         6,
         1,
         ItemBuilder.of(holder.thirdPhase == null ? XMaterial.IRON_BARS.parseItem() : holder.thirdPhase.getPhase().getDisplayItem())
            .setDisplayName(
               Translations.t("editor.edit-opening-animation.phase-navbar.title", "%phase%", Translations.t("labels.third"))[0]
                  + (holder.currentPhaseType == PhaseType.DISPLAYING ? " \u00a7e(" + Translations.t("labels.current")[0] + ")" : "")
            )
            .setLore(
               Translations.t(
                  "editor.edit-opening-animation.phase-navbar.description",
                  "%name%",
                  holder.thirdPhase == null ? Translations.t("labels.to-choose")[0] : holder.thirdPhase.getPhase().getDisplayName()
               )
            ),
         __ -> {
            holder.setCurrentPhaseType(PhaseType.DISPLAYING);
            holder.setCurrentPage(1);
            this.updateInventorySync(player);
         }
      );
      int i = (holder.getCurrentPage() - 1) * 14;

      for (int y = 3; y <= 4; y++) {
         for (int x = 2; x <= 8; x++) {
            if (i >= records.size()) {
               this.setItem(x, y, ItemBuilder.of(XMaterial.GRAY_STAINED_GLASS_PANE).setDisplayName("\u00a77"));
            } else {
               AnimationPhase type = records.get(i);
               AddonAnimationPhaseImpl addonPhase = type instanceof AddonAnimationPhaseImpl ? (AddonAnimationPhaseImpl)type : null;
               AddonLockedAnimationPhaseImpl addonLockedPhase = type instanceof AddonLockedAnimationPhaseImpl ? (AddonLockedAnimationPhaseImpl)type : null;
               boolean haveRequiredVersion = type.isVersionCompatible();
               ItemBuilder displayItem = ItemBuilder.of(type.getDisplayItem());
               if (holder.isCurrentPhaseFilled() && holder.getCurrentPhase().getPhase().equals(type)) {
                  displayItem.addEnchant(XEnchantment.UNBREAKING.getEnchant(), 1).addItemFlag(ItemFlag.HIDE_ENCHANTS);
               }

               List<String> lore = Lists.newArrayList(
                  Translations.t(
                     "editor.edit-opening-animation.select-phase.description",
                     "%required_version%",
                     type.getRequiredVersion() + "+ " + (haveRequiredVersion ? "\u00a7a\u2714" : "\u00a7c\u274c")
                  )
               );
               if (addonLockedPhase != null) {
                  lore = LoreBuilder.builder(this.parent)
                     .description(
                        "\u00a7fRequired Version: \u00a77" + type.getRequiredVersion() + "+ " + (haveRequiredVersion ? "\u00a7a\u2714" : "\u00a7c\u274c")
                     )
                     .actions(ClickAction.legacy(ClickAction.ClickType.LEFT, "Check Addon"))
                     .error(() -> true, Translatable.key("unlock-addon", "%addon%", addonLockedPhase.getAddonName()))
                     .build();
               }

               String phaseDisplayName = addonPhase == null ? type.getDisplayName() : Translations.t(Constants.addon(type.getDisplayName()))[0];
               this.setItem(
                  x,
                  y,
                  displayItem.setDisplayName(Translations.t("editor.edit-opening-animation.select-phase.title", "%name%", phaseDisplayName)[0]).setLore(lore),
                  event -> {
                     if (haveRequiredVersion) {
                        if (addonLockedPhase != null && event.isLeftClick()) {
                           addonLockedPhase.sendUnlockMessage(player);
                        } else {
                           if (event.isLeftClick()) {
                              if (holder.isCurrentPhaseFilled() && holder.getCurrentPhase().getPhase().equals(type)) {
                                 return;
                              }

                              if (event.isShiftClick()) {
                                 holder.setCurrentPhase(new OpeningPhase(type, AnimationProperties.empty()));
                                 this.updateInventorySync(player);
                                 return;
                              }

                              if (type.getLegacyDataClass().equals(ColoredOpeningPhaseData.class)) {
                                 new ColoredPhaseDataBuilderMenu(
                                       PhoenixCrates.getLegacyInstance(), this.crateType, null, target -> this.open(target, holder), data -> {
                                          holder.setCurrentPhase(new OpeningPhase(type, data));
                                          if (holder.getCurrentPhaseType().equals(PhaseType.DISPLAYING)) {
                                             this.saveAndLeave(player, holder);
                                          } else {
                                             this.open(player, holder);
                                          }
                                       }
                                    )
                                    .open(player);
                                 return;
                              }

                              if (type.getLegacyDataClass().equals(ColoredSkullOpeningPhaseData.class)) {
                                 new ColoredSkullPhaseDataBuilderMenu(
                                       PhoenixCrates.getLegacyInstance(), this.crateType, null, target -> this.open(target, holder), data -> {
                                          holder.setCurrentPhase(new OpeningPhase(type, data));
                                          if (holder.getCurrentPhaseType().equals(PhaseType.DISPLAYING)) {
                                             this.saveAndLeave(player, holder);
                                          } else {
                                             this.open(player, holder);
                                          }
                                       }
                                    )
                                    .open(player);
                                 return;
                              }

                              if (type.getLegacyDataClass().equals(KeybasedOpeningPhaseData.class)) {
                                 new KeybasedPhaseDataBuilderMenu(
                                       PhoenixCrates.getLegacyInstance(), this.crateType, null, target -> this.open(target, holder), data -> {
                                          holder.setCurrentPhase(new OpeningPhase(type, data));
                                          if (holder.getCurrentPhaseType().equals(PhaseType.DISPLAYING)) {
                                             this.saveAndLeave(player, holder);
                                          } else {
                                             this.open(player, holder);
                                          }
                                       }
                                    )
                                    .open(player);
                                 return;
                              }

                              if (type.getLegacyDataClass().equals(EmptyOpeningPhaseData.class)) {
                                 holder.setCurrentPhase(new OpeningPhase(type, new EmptyOpeningPhaseData()));
                                 this.updateInventorySync(player);
                                 return;
                              }

                              Translations.send(player, "Opening Phase Data Editor not configured for \"" + type.getLegacyDataClass().getSimpleName() + "\".");
                           }
                        }
                     }
                  }
               );
               i++;
            }
         }
      }

      if (holder.getCurrentPage() != 1) {
         int previousPage = holder.getCurrentPage() - 1;
         this.setItem(
            1,
            3,
            ItemBuilder.of(XMaterial.ARROW)
               .setDisplayName(Translations.t("layout.extras.previous-page.title", "%page%", previousPage)[0])
               .setLore(Translations.t("layout.extras.previous-page.description")),
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
            3,
            ItemBuilder.of(XMaterial.ARROW)
               .setDisplayName(Translations.t("layout.extras.next-page.title", "%page%", nextPage)[0])
               .setLore(Translations.t("layout.extras.next-page.description")),
            __ -> {
               holder.setCurrentPage(holder.getCurrentPage() + 1);
               this.updateInventorySync(player);
            }
         );
      }

      this.setItem(5, 6, ItemBuilder.of(XMaterial.FEATHER).setDisplayName("\u00a76" + Translations.t("labels.go-back")[0]), __ -> this.parent.open(player));
      if (holder.getCurrentPhaseType() != PhaseType.PRE_OPENING) {
         this.setItem(
            3, 6, ItemBuilder.of(EditorCrateMainSettingsLayout.ARROW_LEFT).setDisplayName("\u00a7a" + Translations.t("labels.previous-phase")[0]), __ -> {
               holder.previousPhase();
               this.updateInventorySync(player);
            }
         );
      } else {
         this.setItem(3, 6, ItemBuilder.of(XMaterial.BLACK_STAINED_GLASS_PANE).setDisplayName("\u00a72"));
      }

      if (holder.getCurrentPhaseType() == PhaseType.DISPLAYING) {
         this.setItem(
            7,
            6,
            ItemBuilder.of(holder.isCurrentPhaseFilled() ? SAVE_SUCCESS_SKULL : XMaterial.GRAY_DYE.parseItem())
               .setDisplayName((holder.isCurrentPhaseFilled() ? "\u00a7d" : "\u00a7c") + Translations.t("labels.save")[0]),
            __ -> {
               if (holder.isCurrentPhaseFilled()) {
                  this.saveAndLeave(player, holder);
               }
            }
         );
      } else {
         this.setItem(
            7,
            6,
            ItemBuilder.of(holder.isCurrentPhaseFilled() ? EditorCrateMainSettingsLayout.ARROW_RIGHT : XMaterial.GRAY_DYE.parseItem())
               .setDisplayName((holder.isCurrentPhaseFilled() ? "\u00a7a" : "\u00a7c") + Translations.t("labels.next-phase")[0]),
            __ -> {
               if (holder.isCurrentPhaseFilled()) {
                  holder.nextPhase();
                  this.updateInventorySync(player);
               }
            }
         );
      }
   }

   public void saveAndLeave(Player player, EditorCrateCreateOpeningAnimation.EditorOpeningPhasesHolder holder) {
      try {
         if (!holder.isFilled()) {
            Translations.send(player, "\u00a7cSome phases need to be selected.");
            return;
         }

         if (this.animation == null) {
            this.animation = OpeningAnimation.defaultAnimation();
            this.crateType.getOpeningAnimations().add(this.animation);
         }

         this.animation.setPreOpenPhase(holder.firstPhase);
         this.animation.setOpenPhase(holder.secondPhase);
         this.animation.setDisplayPhase(holder.thirdPhase);
         Crates.getCratesManager().saveCrateType(this.crateType);
         Translations.send(player, "\u00a7aOpening Animation successfully changed.");
         this.parent.open(player);
      } catch (Exception e) {
         Translations.error(player, e);
         player.closeInventory();
      }
   }

   @Override
   public void open(Player player) {
      this.open(player, new EditorCrateCreateOpeningAnimation.EditorOpeningPhasesHolder(this));
   }

   public static class EditorOpeningPhasesHolder extends CustomInventory.PaginationWrapper {
      private PhaseType currentPhaseType = PhaseType.PRE_OPENING;
      private OpeningPhase firstPhase;
      private OpeningPhase secondPhase;
      private OpeningPhase thirdPhase;

      public EditorOpeningPhasesHolder(EditorCrateCreateOpeningAnimation container) {
         super(container);
         if (container.animation != null) {
            this.firstPhase = container.animation.getPreOpenPhase();
            this.secondPhase = container.animation.getOpenPhase();
            this.thirdPhase = container.animation.getDisplayPhase();
         }
      }

      public OpeningPhase getCurrentPhase() {
         switch (this.currentPhaseType) {
            case PRE_OPENING:
               return this.firstPhase;
            case OPENING:
               return this.secondPhase;
            case DISPLAYING:
               return this.thirdPhase;
            default:
               return null;
         }
      }

      public void setCurrentPhase(OpeningPhase newPhase) {
         switch (this.currentPhaseType) {
            case PRE_OPENING:
               this.firstPhase = newPhase;
               break;
            case OPENING:
               this.secondPhase = newPhase;
               break;
            case DISPLAYING:
               this.thirdPhase = newPhase;
         }
      }

      public boolean isCurrentPhaseFilled() {
         switch (this.currentPhaseType) {
            case PRE_OPENING:
               return this.firstPhase != null;
            case OPENING:
               return this.secondPhase != null;
            case DISPLAYING:
               return this.thirdPhase != null;
            default:
               return false;
         }
      }

      public void previousPhase() {
         switch (this.currentPhaseType) {
            case PRE_OPENING:
            default:
               break;
            case OPENING:
               this.currentPhaseType = PhaseType.PRE_OPENING;
               break;
            case DISPLAYING:
               this.currentPhaseType = PhaseType.OPENING;
         }
      }

      public void nextPhase() {
         switch (this.currentPhaseType) {
            case PRE_OPENING:
               this.currentPhaseType = PhaseType.OPENING;
               break;
            case OPENING:
               this.currentPhaseType = PhaseType.DISPLAYING;
            case DISPLAYING:
         }
      }

      public boolean isFilled() {
         return this.firstPhase != null && this.secondPhase != null && this.thirdPhase != null;
      }

      @Generated
      public PhaseType getCurrentPhaseType() {
         return this.currentPhaseType;
      }

      @Generated
      public OpeningPhase getFirstPhase() {
         return this.firstPhase;
      }

      @Generated
      public OpeningPhase getSecondPhase() {
         return this.secondPhase;
      }

      @Generated
      public OpeningPhase getThirdPhase() {
         return this.thirdPhase;
      }

      @Generated
      public void setCurrentPhaseType(PhaseType currentPhaseType) {
         this.currentPhaseType = currentPhaseType;
      }

      @Generated
      public void setFirstPhase(OpeningPhase firstPhase) {
         this.firstPhase = firstPhase;
      }

      @Generated
      public void setSecondPhase(OpeningPhase secondPhase) {
         this.secondPhase = secondPhase;
      }

      @Generated
      public void setThirdPhase(OpeningPhase thirdPhase) {
         this.thirdPhase = thirdPhase;
      }
   }
}
