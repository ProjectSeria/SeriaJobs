package id.seria.gacha.lib.common.uicomponents.newest.configurable;

import id.seria.gacha.api.sdk.scheduler.Task;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.ContainerConfiguration;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.ContainerItemsConfiguration;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.PaginatedContainerConfiguration;
import id.seria.gacha.lib.common.uicomponents.newest.container.ContainerType;
import id.seria.gacha.lib.common.uicomponents.newest.container.PaginatedContainerFrame;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.OpenViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.actions.ActionsFacade;
import id.seria.gacha.lib.common.utils.actions.ActionsFactory;
import id.seria.gacha.sdk.core.locales.providers.PluginTranslationsProvider;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.Generated;

@Deprecated
public abstract class ConfigurablePaginatedLayout<T> extends PaginatedContainerFrame<T> {
   private final PaginatedContainerConfiguration configuration;
   private final Supplier<List<T>> itemsSupplier;
   private final ActionsFactory actionsFactory;
   private Task task;

   public ConfigurablePaginatedLayout(
      ServerPlugin plugin,
      PluginTranslationsProvider translationProvider,
      PaginatedContainerConfiguration configuration,
      Supplier<List<T>> itemsSupplier,
      ActionsFactory actionsFactory
   ) {
      super(
         plugin,
         translationProvider,
         new PaginationSettings(
            configuration.getPagination().getSlots(), configuration.getPagination().getAlignItems(), configuration.getPagination().getDirectionItems()
         )
      );
      this.configuration = configuration;
      this.itemsSupplier = itemsSupplier;
      this.actionsFactory = actionsFactory;
      PaginatedContainerConfiguration.PaginationConfiguration pagination = configuration.getPagination();
      ContainerItemsConfiguration.NonSlotItemConfiguration fallbackItem = pagination.getFillItem();
      ContainerItemsConfiguration.ItemConfiguration previousItem = pagination.getPreviousItem();
      ContainerItemsConfiguration.ItemConfiguration nextItem = pagination.getNextItem();
      this.setFallbackItem(
         (ctx, item) -> {
            item.withItem(fallbackItem.getMaterial())
               .withCustomModelData(fallbackItem.getCustomModelData())
               .withItemModel(fallbackItem.getItemModel())
               .withDisplayName(fallbackItem.getDisplayName())
               .withLore(fallbackItem.getLore());
            if (fallbackItem.isGlow()) {
               item.withGlow();
            }
         }
      );
      this.setPreviousPageItem(
         (ctx, item) -> {
            item.withSlots(previousItem.getSlot().asArray())
               .withItem(previousItem.getMaterial())
               .withCustomModelData(previousItem.getCustomModelData())
               .withItemModel(previousItem.getItemModel())
               .withDisplayName(previousItem.getDisplayName())
               .withLore(previousItem.getLore())
               .whenClick(__ -> ActionsFacade.executeSerializedActions(this.actionsFactory, this.plugin, previousItem.getActions(), ctx.getViewer()));
            if (previousItem.isGlow()) {
               item.withGlow();
            }
         }
      );
      this.setNextPageItem(
         (ctx, item) -> {
            item.withSlots(nextItem.getSlot().asArray())
               .withItem(nextItem.getMaterial())
               .withCustomModelData(nextItem.getCustomModelData())
               .withItemModel(nextItem.getItemModel())
               .withDisplayName(nextItem.getDisplayName())
               .withLore(nextItem.getLore())
               .whenClick(__ -> ActionsFacade.executeSerializedActions(this.actionsFactory, this.plugin, nextItem.getActions(), ctx.getViewer()));
            if (nextItem.isGlow()) {
               item.withGlow();
            }
         }
      );
      this.setItems(this.itemsSupplier.get());
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
         this.task = null;
      }
   }

   public void tick() {
      this.setItems(this.itemsSupplier.get());
   }

   @Override
   public void onOpen(OpenViewContext context) {
      context.setContainerTitle(Utilities.getNMSFactory().translateColorCodes(this.configuration.getSettings().getTitle()));
      context.setContainerType(ContainerType.fromRows(this.configuration.getSettings().getRows()));
      ActionsFacade.executeSerializedActions(this.actionsFactory, this.plugin, this.configuration.getSettings().getOnOpenActions(), context.getViewer());
   }

   @Override
   protected int getNeededRows(ContainerView view) {
      return this.configuration.getSettings().getRows();
   }

   @Override
   public void onRender(RenderViewContext context) {
      for (ContainerItemsConfiguration.ItemConfiguration item : this.configuration.getItems().values()) {
         Consumer<ClickViewContext> consumer = ctx -> ActionsFacade.executeSerializedActions(
            this.actionsFactory, this.plugin, item.getActions(), ctx.getViewer()
         );
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
                  .withLore(lore);
               if (item.isGlow()) {
                  virtualItem.withGlow();
               }

               context.setSlot(i, virtualItem.getItemBuilder(), consumer);
            }
         } else {
            VirtualItem virtualItem = new VirtualItem()
               .withSlots(item.getSlot().asArray())
               .withItem(item.getMaterial())
               .withAmount(item.getAmount())
               .withCustomModelData(item.getCustomModelData())
               .withItemModel(item.getItemModel())
               .withDisplayName(displayName)
               .withLore(lore)
               .whenClick(consumer);
            if (item.isGlow()) {
               virtualItem.withGlow();
            }

            virtualItem.render(context);
         }
      }

      super.onRender(context);
   }

   @Override
   public abstract void onRenderItem(ContainerView var1, int var2, VirtualItem var3, T var4);

   @Generated
   public PaginatedContainerConfiguration getConfiguration() {
      return this.configuration;
   }

   @Generated
   public Supplier<List<T>> getItemsSupplier() {
      return this.itemsSupplier;
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
