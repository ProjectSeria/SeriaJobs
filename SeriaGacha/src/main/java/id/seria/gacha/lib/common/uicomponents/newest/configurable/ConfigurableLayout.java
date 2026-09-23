package id.seria.gacha.lib.common.uicomponents.newest.configurable;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.api.sdk.scheduler.Task;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.ContainerConfiguration;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.ContainerItemsConfiguration;
import id.seria.gacha.lib.common.uicomponents.newest.container.ContainerType;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.OpenViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.layout.ContainerLayout;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.actions.ActionsFacade;
import id.seria.gacha.lib.common.utils.actions.ActionsFactory;
import id.seria.gacha.lib.xseries.XItemFlag;
import id.seria.gacha.sdk.core.locales.providers.PluginTranslationsProvider;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class ConfigurableLayout extends ContainerLayout {
   protected final ContainerConfiguration configuration;
   protected ActionsFactory actionsFactory;
   private Task task;

   public ConfigurableLayout(
      ServerPlugin plugin, PluginTranslationsProvider translationProvider, ContainerConfiguration configuration, ActionsFactory actionsFactory
   ) {
      super(plugin, translationProvider, Translatable.literal(configuration.getSettings().getTitle()), null);
      this.configuration = configuration;
      this.actionsFactory = actionsFactory;
   }

   public void load() {
      ContainerConfiguration.SettingsConfigurationMenu settings = this.configuration.getSettings();
      if (this.configuration.getSettings().isRefreshEnabled()) {
         this.task = this.getPlugin().getScheduler().scheduleSync(this::tick, settings.getRefreshRate(), settings.getRefreshRate());
      }
   }

   public void unload() {
      if (this.task != null) {
         this.task.cancel();
      }
   }

   protected void tick() {
      this.getViewersContext().values().removeIf(Objects::isNull);

      for (ContainerView view : new ArrayList<>(this.getViewersContext().values())) {
         if (view != null) {
            Player viewer = view.getViewer();
            if (!Utilities.isPlayerOnline(viewer)) {
               this.getViewersContext().remove(viewer.getUniqueId());
            } else {
               Inventory topInv = viewer.getOpenInventory().getTopInventory();
               InventoryHolder holder = topInv.getHolder();
               if (holder instanceof ContainerView && holder.equals(view)) {
                  try {
                     this.updateView(view);
                  } catch (Exception e) {
                     this.getPlugin().getLogger().error("Error while updating ContainerView in tick", e);
                  }
               } else {
                  this.getViewersContext().remove(viewer.getUniqueId());
               }
            }
         }
      }
   }

   @Override
   public abstract void onPrepare(ContainerView var1);

   @Override
   public void onOpen(OpenViewContext context) {
      context.setContainerTitle(Utilities.getNMSFactory().translateColorCodes(this.configuration.getSettings().getTitle()));
      context.setContainerType(ContainerType.fromRows(this.configuration.getSettings().getRows()));
      ActionsFacade.executeSerializedActions(this.actionsFactory, this.plugin, this.configuration.getSettings().getOnOpenActions(), context.getViewer());
   }

   @Override
   public void onRender(RenderViewContext context) {
      Player viewer = context.getViewer();

      for (ContainerItemsConfiguration.ItemConfiguration item : this.configuration.getItems().values()) {
         Consumer<ClickViewContext> consumer = ctx -> ActionsFacade.executeSerializedActions(this.actionsFactory, this.plugin, item.getActions(), viewer);
         String displayName = item.getDisplayName();
         List<String> lore = item.getLore();
         if (displayName.length() == 0) {
            displayName = "\u00a7r";
         }

         if (item.getSlot().getFirst() < 0) {
            for (int i = 0; i < context.getContainerType().getSize(); i++) {
               VirtualItem virtualItem = new VirtualItem()
                  .withItem(item.getMaterial())
                  .withAmount(item.getAmount())
                  .withCustomModelData(item.getCustomModelData())
                  .withItemModel(item.getItemModel())
                  .withDisplayName(displayName)
                  .withLore(lore)
                  .withHideTooltip(item.getHideTooltip())
                  .withTooltipStyle(item.getTooltipStyle())
                  .withEnchantmentGlintOverride(item.getEnchantmentGlintOverride())
                  .withArmorTrim(item.getTrimMaterial(), item.getTrimPattern())
                  .withBannerMeta(item.getBannerMeta())
                  .withPotionEffects(item.getPotionEffects())
                  .withRgb(item.getRgb());
               if (item.isGlow()) {
                  virtualItem.withGlow();
               }

               if (item.isHideAttributes()) {
                  virtualItem.withFlag(
                     XItemFlag.HIDE_ATTRIBUTES,
                     XItemFlag.HIDE_ENCHANTS,
                     XItemFlag.HIDE_ATTRIBUTES,
                     XItemFlag.HIDE_UNBREAKABLE,
                     XItemFlag.HIDE_CAN_PLACE_ON,
                     XItemFlag.HIDE_CAN_BREAK,
                     XItemFlag.HIDE_POTION_CONTENTS,
                     XItemFlag.HIDE_ATTRIBUTE_MODIFIERS
                  );
               }

               context.setSlot(i, virtualItem.getItemBuilder(), consumer);
            }
         } else {
            for (int slot : item.getSlot().asArray()) {
               VirtualItem virtualItem = new VirtualItem()
                  .withItem(item.getMaterial())
                  .withAmount(item.getAmount())
                  .withCustomModelData(item.getCustomModelData())
                  .withItemModel(item.getItemModel())
                  .withDisplayName(displayName)
                  .withLore(lore)
                  .withHideTooltip(item.getHideTooltip())
                  .withTooltipStyle(item.getTooltipStyle())
                  .withEnchantmentGlintOverride(item.getEnchantmentGlintOverride())
                  .withArmorTrim(item.getTrimMaterial(), item.getTrimPattern())
                  .withBannerMeta(item.getBannerMeta())
                  .withPotionEffects(item.getPotionEffects())
                  .withRgb(item.getRgb());
               if (item.isGlow()) {
                  virtualItem.withGlow();
               }

               if (item.isHideAttributes()) {
                  virtualItem.withFlag(
                     XItemFlag.HIDE_ATTRIBUTES,
                     XItemFlag.HIDE_ENCHANTS,
                     XItemFlag.HIDE_ATTRIBUTES,
                     XItemFlag.HIDE_UNBREAKABLE,
                     XItemFlag.HIDE_CAN_PLACE_ON,
                     XItemFlag.HIDE_CAN_BREAK,
                     XItemFlag.HIDE_POTION_CONTENTS,
                     XItemFlag.HIDE_ATTRIBUTE_MODIFIERS
                  );
               }

               context.setSlot(slot, virtualItem.getItemBuilder(), consumer);
            }
         }
      }

      super.onRender(context);
   }

   @Generated
   public ContainerConfiguration getConfiguration() {
      return this.configuration;
   }

   @Generated
   public ActionsFactory getActionsFactory() {
      return this.actionsFactory;
   }

   @Generated
   public Task getTask() {
      return this.task;
   }
}
