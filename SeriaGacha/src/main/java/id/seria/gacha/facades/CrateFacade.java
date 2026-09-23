package id.seria.gacha.facades;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.actions.Actions;
import id.seria.gacha.api.crate.animation.PhaseType;
import id.seria.gacha.api.crate.events.CratePreOpenEvent;
import id.seria.gacha.api.crate.events.CratePreviewOpenEvent;
import id.seria.gacha.api.crate.exceptions.CrateOpeningException;
import id.seria.gacha.api.crate.exceptions.CratePreviewException;
import id.seria.gacha.api.events.items.ItemDeliverySource;
import id.seria.gacha.api.reward.RewardsMode;
import id.seria.gacha.api.session.OpeningContext;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.ContainerConfiguration;
import id.seria.gacha.lib.common.utils.ParticleEffect;
import id.seria.gacha.lib.common.utils.actions.ActionsFacade;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import id.seria.gacha.lib.common.utils.utility.Validate;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.animations.idle.IdleEffect;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectData;
import id.seria.gacha.managers.crates.animations.idle.IdleEffectType;
import id.seria.gacha.managers.crates.animations.opening.OpeningPhase;
import id.seria.gacha.managers.crates.animations.opening.data.ColoredOpeningPhaseData;
import id.seria.gacha.managers.crates.animations.opening.data.EmptyOpeningPhaseData;
import id.seria.gacha.managers.crates.cost.CostEngineType;
import id.seria.gacha.managers.crates.type.list.OpeningAnimationWrapper;
import id.seria.gacha.managers.players.PlayerData;
import id.seria.gacha.menus.MenuFactory;
import id.seria.gacha.menus.crates.ConfirmationMenu;
import id.seria.gacha.menus.crates.CratePreviewMenu;
import id.seria.gacha.menus.crates.CrateSelectRewardMenu;
import id.seria.gacha.registries.AnimationsRegistry;
import java.awt.Color;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CrateFacade {
   public CrateFacade() {
   }

   public static CrateType buildDefault(String identifier) throws Exception {
      boolean generatedIdentifier = identifier == null;
      if (generatedIdentifier) {
         identifier = "crate_" + System.currentTimeMillis();
      }

      List<IdleEffect> idleEffects = Lists.newArrayList();
      idleEffects.add(new IdleEffect(IdleEffectType.DEFAULT, new IdleEffectData(ParticleEffect.REDSTONE, Color.RED, 0.0, 0.0, 0.0, 1.0, 0.1, 2)));
      idleEffects.add(new IdleEffect(IdleEffectType.DEFAULT, new IdleEffectData(ParticleEffect.FLAME, Color.WHITE, 0.0, 0.0, 0.0, 2.0, 0.1, 2)));
      idleEffects.add(new IdleEffect(IdleEffectType.STAR, new IdleEffectData(ParticleEffect.SMOKE_NORMAL, Color.WHITE, 0.0, 0.0, 0.0, 0.0, 0.0, 2)));
      OpeningAnimationWrapper openingAnimations = new OpeningAnimationWrapper(
         new OpeningPhase(AnimationsRegistry.getInstance().getPhaseById(PhaseType.PRE_OPENING, "DEFAULT").get(), new EmptyOpeningPhaseData()),
         new OpeningPhase(AnimationsRegistry.getInstance().getPhaseById(PhaseType.OPENING, "NONE").get(), new EmptyOpeningPhaseData()),
         new OpeningPhase(AnimationsRegistry.getInstance().getPhaseById(PhaseType.DISPLAYING, "DEFAULT").get(), new ColoredOpeningPhaseData(Color.WHITE))
      );
      CrateType crateType = new CrateType();
      crateType.setIdentifier(identifier);
      crateType.setDisplayName(generatedIdentifier ? crateType.getItem().getCustomName() : StringUtil.capitalize(identifier));
      crateType.setPermission("phoenixcrates.crate." + identifier);
      crateType.setIdleEffects(idleEffects);
      crateType.setOpeningAnimations(openingAnimations);
      return crateType;
   }

   public static void openPreviewMenu(Crate crate, Player player) throws CratePreviewException, CrateOpeningException {
      if (Crates.getPlayersManager().getPlayerIfCached(player, true) == null) {
         throw new CratePreviewException(Translations.t("crate.data-not-available")[0]);
      }

      CrateType type = crate.getType();
      if (type.hasPreviewActions()) {
         ActionsFacade.executeSerializedActions(Actions.getFactory(), Crates.getPlugin(), type.getPreviewActions(), player);
      }

      if (type.getRewardsMode() == RewardsMode.SELECTIVE) {
         openRewardMenu(crate, player);
      } else if (type.getRewardsMenuName().isEmpty()) {
         if (!type.hasPreviewActions()) {
            throw new CratePreviewException("\u00a7cThis crate don't have preview menu enabled.");
         }
      } else {
         ContainerConfiguration configuration = MenuFactory.getInstance().getConfigurationOrNull(type.getRewardsMenuName());
         if (configuration == null) {
            throw new CratePreviewException("\u00a7cNo menu with the name \"" + type.getRewardsMenuName() + "\" was found.");
         }

         if (configuration instanceof CratePreviewMenu.CratePreviewMenuConfiguration
            && configuration.getSettings().getType().equalsIgnoreCase("REWARDS_PREVIEW")) {
            CratePreviewOpenEvent event = new CratePreviewOpenEvent(type, type.getRewardsMenuName(), player);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
               new CratePreviewMenu((CratePreviewMenu.CratePreviewMenuConfiguration)configuration, crate).open(player);
            }
         } else {
            throw new CratePreviewException("\u00a7cThe menu \"" + type.getRewardsMenuName() + "\" is not a rewards preview menu.");
         }
      }
   }

   public static void openPreviewMenu(CrateType type, Player player) throws CratePreviewException {
      if (Crates.getPlayersManager().getPlayerIfCached(player, true) == null) {
         throw new CratePreviewException(Translations.t("crate.data-not-available")[0]);
      }

      if (type.hasPreviewActions()) {
         ActionsFacade.executeSerializedActions(Actions.getFactory(), Crates.getPlugin(), type.getPreviewActions(), player);
      }

      if (type.getRewardsMenuName().isEmpty()) {
         if (!type.hasPreviewActions()) {
            throw new CratePreviewException("\u00a7cThis crate don't have preview menu enabled.");
         }
      } else {
         ContainerConfiguration configuration = MenuFactory.getInstance().getConfigurationOrNull(type.getRewardsMenuName());
         if (configuration == null) {
            throw new CratePreviewException("\u00a7cNo menu with the name \"" + type.getRewardsMenuName() + "\" was found.");
         }

         if (configuration instanceof CratePreviewMenu.CratePreviewMenuConfiguration
            && configuration.getSettings().getType().equalsIgnoreCase("REWARDS_PREVIEW")) {
            CratePreviewOpenEvent event = new CratePreviewOpenEvent(type, type.getRewardsMenuName(), player);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
               new CratePreviewMenu((CratePreviewMenu.CratePreviewMenuConfiguration)configuration, type).open(player);
            }
         } else {
            throw new CratePreviewException("\u00a7cThe menu \"" + type.getRewardsMenuName() + "\" is not a rewards preview menu.");
         }
      }
   }

   public static void openRewardMenu(Crate crate, Player player) throws CratePreviewException {
      openRewardMenu(crate, player, false);
   }

   public static void openRewardMenu(Crate crate, Player player, boolean virtual) throws CratePreviewException {
      Validate.isTrue(crate.getType().getRewardsMode() == RewardsMode.SELECTIVE, "Rewards Selective Mode only allowed");
      if (Crates.getPlayersManager().getPlayerIfCached(player, true) == null) {
         throw new CratePreviewException(Translations.t("crate.data-not-available")[0]);
      }

      ContainerConfiguration menuConfiguration = MenuFactory.getInstance().getConfigurationOrNull(crate.getType().getSelectRewardMenuName());
      if (menuConfiguration == null) {
         throw new CratePreviewException("\u00a7cUnknown menu.");
      }

      if (!(menuConfiguration instanceof CrateSelectRewardMenu.CrateSelectRewardMenuConfiguration)) {
         throw new CratePreviewException("\u00a7cNot a selective reward menu.");
      }

      new CrateSelectRewardMenu((CrateSelectRewardMenu.CrateSelectRewardMenuConfiguration)menuConfiguration, crate, null, virtual).open(player);
   }

   @Deprecated
   public static int countInventoryKeys(CrateType type, Player player) {
      int i = 0;

      for (ItemStack content : player.getInventory().getContents()) {
         if (content != null && content.hasItemMeta()) {
            String crateKey = KeyFacade.extractKey(content);
            if (crateKey != null && crateKey.equals(type.getIdentifier())) {
               i += content.getAmount();
            }
         }
      }

      return i;
   }

   public static void openCrate(Crate crate, Player player) throws CrateOpeningException, CratePreviewException {
      CratePreOpenEvent preOpenEvent = new CratePreOpenEvent(crate, player);
      Bukkit.getPluginManager().callEvent(preOpenEvent);
      if (!preOpenEvent.isCancelled()) {
         switch (crate.getType().getRewardsMode()) {
            case RANDOM:
               if (!crate.getType().isKeyRequired()) {
                  crate.openCrate(OpeningContext.random(player, false, 1));
               } else {
                  crate.openCrate(OpeningContext.random(player, Crates.getConfiguration().isMainHandKey(), 1));
               }
               break;
            case SELECTIVE:
               try {
                  if (crate.getType().hasPreviewActions()) {
                     ActionsFacade.executeSerializedActions(Actions.getFactory(), Crates.getPlugin(), crate.getType().getPreviewActions(), player);
                  }

                  openRewardMenu(crate, player);
                  break;
               } catch (CratePreviewException ex) {
                  throw new CrateOpeningException(ex.getMessage());
               }
            default:
               throw new NotImplementedException();
         }
      }
   }

   public static void openCrateWithMoneyConfirmation(Crate crate, Player player, CrateFacade.CrateOpenAction action) throws CrateOpeningException, CratePreviewException {
      CostEngineType costEngine = crate.getType().getOpenCostEngine();
      String costCurrency = crate.getType().getOpenCostCurrency();
      if (!crate.hasActiveSession()
         && costEngine.isInstalled()
         && crate.getType().getOpenMoneyCost() > 0.0
         && costEngine.hasEnough(player, crate.getType().getOpenMoneyCost(), costCurrency)
         && !Crates.getConfiguration().isDisableConfirmationMenus()) {
         ConfirmationMenu.ConfirmationMenuConfiguration config = (ConfirmationMenu.ConfirmationMenuConfiguration)MenuFactory.getInstance()
            .getConfiguration("confirmation_menu");
         ItemStack currencyItem = ItemBuilder.of(XMaterial.GOLD_INGOT.parseItem())
            .setDisplayName("&a" + costEngine.format(crate.getType().getOpenMoneyCost(), costCurrency))
            .build();
         new ConfirmationMenu(config, () -> {
            try {
               action.run();
            } catch (CrateOpeningException e) {
               Translations.send(player, e.getMessage());
            } catch (CratePreviewException e) {
               throw new RuntimeException(e);
            }
         }, () -> {}, currencyItem, "&a" + costEngine.format(crate.getType().getOpenMoneyCost(), costCurrency)).open(player);
      } else {
         action.run();
      }
   }

   public static void giveCrate(@NonNull CrateType crateType, @NonNull Player player, int amount, boolean silent) {
      if (crateType == null) {
         throw new NullPointerException("crateType is marked non-null but is null");
      }

      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      ItemStack item = crateType.createItemStack();
      item.setAmount(amount);
      PlayerData playerData = Crates.getPlayersManager().getCachedDataNow(player);
      ItemDeliveryFacade.DeliveryResult result = ItemDeliveryFacade.deliver(playerData, player, Collections.singletonList(item), ItemDeliverySource.CRATE_GRANT);
      if (result.hasDroppedItems()) {
         Translations.send(player, Translations.t("editor.errors.inventory-full-crate"));
      }

      if (!silent) {
         Translations.send(player, Translations.t("crate-received", "%display_name%", crateType.getFormattedDisplayName(player), "%amount%", amount)[0]);
      }
   }

   @FunctionalInterface
   public interface CrateOpenAction {
      void run() throws CrateOpeningException, CratePreviewException;
   }
}
