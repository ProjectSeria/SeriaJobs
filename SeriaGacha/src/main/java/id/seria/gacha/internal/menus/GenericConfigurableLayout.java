package id.seria.gacha.internal.menus;

import id.seria.gacha.PhoenixCrates;
import id.seria.gacha.actions.Actions;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.Placeholders;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.uicomponents.newest.configurable.ConfigurableLayout;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.ContainerConfiguration;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.ContainerItemsConfiguration;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.PaginatedContainerConfiguration;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.ClickViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.data.PagingData;
import id.seria.gacha.lib.common.uicomponents.newest.layout.component.basic.list.SimplePaginatedList;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.utils.actions.ActionsFacade;
import id.seria.gacha.lib.common.utils.actions.ActionsFactory;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

public abstract class GenericConfigurableLayout<T extends ContainerConfiguration> extends ConfigurableLayout {
   public GenericConfigurableLayout(T configuration) {
      this(configuration, Actions.getFactory());
   }

   public GenericConfigurableLayout(T configuration, ActionsFactory actionsFactory) {
      super(PhoenixCrates.getLegacyInstance(), Translations.getProvider(), configuration, actionsFactory);
   }

   @Override
   public void onRenderSlot(RenderViewContext context, VirtualItem item) {
      if (item.getDisplayName() != null) {
         item.withDisplayName(Placeholders.setPlaceholders(item.getDisplayName(), context.getViewer()));
      }

      if (item.getLore() != null) {
         item.withLore(item.getLore().stream().map(text -> Placeholders.setPlaceholders(text, context.getViewer())).collect(Collectors.toList()));
      }
   }

   public PhoenixCrates getPlugin() {
      return (PhoenixCrates)super.getPlugin();
   }

   @Override
   public T getConfiguration() {
      return (T)super.getConfiguration();
   }

   protected void setItem(RenderViewContext context, ContainerItemsConfiguration.ItemConfiguration item, Consumer<ClickViewContext> consumer) {
      this.prepareItem(item, consumer).render(context);
   }

   protected void setItem(RenderViewContext context, ContainerItemsConfiguration.NonActionsItemConfiguration item, Consumer<ClickViewContext> consumer) {
      this.prepareItem(item, consumer).render(context);
   }

   protected void setItem(
      RenderViewContext context, ContainerItemsConfiguration.NonActionsSingleSlotItemConfiguration item, Consumer<ClickViewContext> consumer
   ) {
      this.prepareItem(item, consumer).render(context);
   }

   protected VirtualItem prepareItem(ContainerItemsConfiguration.ItemConfiguration item, Consumer<ClickViewContext> consumer) {
      if (item.getSlot().length() != 0 && item.getSlot().getFirst() >= 0) {
         String displayName = item.getDisplayName().isEmpty() ? "\u00a7r" : item.getDisplayName();
         return VirtualItem.from(item.getMaterial())
            .withSlots(item.getSlot())
            .withCustomModelData(item.getCustomModelData())
            .withItemModel(item.getItemModel())
            .withDisplayName(displayName)
            .withLore(item.getLore())
            .withHideTooltip(item.getHideTooltip())
            .withTooltipStyle(item.getTooltipStyle())
            .withEnchantmentGlintOverride(item.getEnchantmentGlintOverride())
            .withArmorTrim(item.getTrimMaterial(), item.getTrimPattern())
            .withBannerMeta(item.getBannerMeta())
            .withPotionEffects(item.getPotionEffects())
            .withRgb(item.getRgb())
            .withFlag(Constants.ITEM_FLAGS)
            .whenClick(consumer);
      } else {
         return new VirtualItem();
      }
   }

   protected VirtualItem prepareItem(ContainerItemsConfiguration.NonSlotBasicItemConfiguration item, Consumer<ClickViewContext> consumer) {
      String displayName = item.getDisplayName().isEmpty() ? "\u00a7r" : item.getDisplayName();
      return VirtualItem.from(item.getMaterial())
         .withCustomModelData(item.getCustomModelData())
         .withItemModel(item.getItemModel())
         .withDisplayName(displayName)
         .withLore(item.getLore())
         .withHideTooltip(item.getHideTooltip())
         .withTooltipStyle(item.getTooltipStyle())
         .withEnchantmentGlintOverride(item.getEnchantmentGlintOverride())
         .withArmorTrim(item.getTrimMaterial(), item.getTrimPattern())
         .withBannerMeta(item.getBannerMeta())
         .withPotionEffects(item.getPotionEffects())
         .withRgb(item.getRgb())
         .withFlag(Constants.ITEM_FLAGS)
         .whenClick(consumer);
   }

   protected VirtualItem prepareItem(ContainerItemsConfiguration.NonActionsItemConfiguration item, Consumer<ClickViewContext> consumer) {
      String displayName = item.getDisplayName().isEmpty() ? "\u00a7r" : item.getDisplayName();
      return VirtualItem.from(item.getMaterial())
         .withSlots(item.getSlot())
         .withCustomModelData(item.getCustomModelData())
         .withItemModel(item.getItemModel())
         .withDisplayName(displayName)
         .withLore(item.getLore())
         .withHideTooltip(item.getHideTooltip())
         .withTooltipStyle(item.getTooltipStyle())
         .withEnchantmentGlintOverride(item.getEnchantmentGlintOverride())
         .withArmorTrim(item.getTrimMaterial(), item.getTrimPattern())
         .withBannerMeta(item.getBannerMeta())
         .withPotionEffects(item.getPotionEffects())
         .withRgb(item.getRgb())
         .withFlag(Constants.ITEM_FLAGS)
         .whenClick(consumer);
   }

   protected VirtualItem prepareItem(ContainerItemsConfiguration.NonActionsSingleSlotItemConfiguration item, Consumer<ClickViewContext> consumer) {
      String displayName = item.getDisplayName().isEmpty() ? "\u00a7r" : item.getDisplayName();
      return VirtualItem.from(item.getMaterial())
         .withSlot(item.getSlot())
         .withCustomModelData(item.getCustomModelData())
         .withItemModel(item.getItemModel())
         .withDisplayName(displayName)
         .withLore(item.getLore())
         .withHideTooltip(item.getHideTooltip())
         .withTooltipStyle(item.getTooltipStyle())
         .withEnchantmentGlintOverride(item.getEnchantmentGlintOverride())
         .withArmorTrim(item.getTrimMaterial(), item.getTrimPattern())
         .withBannerMeta(item.getBannerMeta())
         .withPotionEffects(item.getPotionEffects())
         .withRgb(item.getRgb())
         .withFlag(Constants.ITEM_FLAGS)
         .whenClick(consumer);
   }

   public <X> SimplePaginatedList<X> createSimplePagination(
      PaginatedContainerConfiguration configuration, Class<X> clazz, Function<ContainerView, List<X>> itemsSupplier
   ) {
      PaginatedContainerConfiguration.PaginationConfiguration paging = configuration.getPagination();
      ContainerItemsConfiguration.ItemConfiguration previousItem = paging.getPreviousItem();
      ContainerItemsConfiguration.ItemConfiguration nextItem = paging.getNextItem();
      SimplePaginatedList<X> pagination = new SimplePaginatedList<>(
         paging.getSlots(),
         new PagingData(
            paging.getAlignItems(),
            new PagingData.PaginationController(
               previousItem.getSlot(),
               previousItem.getMaterial(),
               previousItem.getCustomModelData(),
               previousItem.getItemModel(),
               previousItem.getDisplayName(),
               previousItem.getLore()
            ),
            new PagingData.PaginationController(
               nextItem.getSlot(),
               nextItem.getMaterial(),
               nextItem.getCustomModelData(),
               nextItem.getItemModel(),
               nextItem.getDisplayName(),
               nextItem.getLore()
            )
         ),
         itemsSupplier
      );
      pagination.setFallbackItem(
         context -> VirtualItem.from(paging.getFillItem().getMaterial())
            .withDisplayName(paging.getFillItem().getDisplayName())
            .withLore(paging.getFillItem().getLore())
            .withCustomModelData(paging.getFillItem().getCustomModelData())
            .withItemModel(paging.getFillItem().getItemModel())
            .withHideTooltip(paging.getFillItem().getHideTooltip())
            .withTooltipStyle(paging.getFillItem().getTooltipStyle())
            .withEnchantmentGlintOverride(paging.getFillItem().getEnchantmentGlintOverride())
            .withArmorTrim(paging.getFillItem().getTrimMaterial(), paging.getFillItem().getTrimPattern())
            .withBannerMeta(paging.getFillItem().getBannerMeta())
            .withPotionEffects(paging.getFillItem().getPotionEffects())
            .withRgb(paging.getFillItem().getRgb())
            .whenClick(
               c -> ActionsFacade.executeSerializedActions(this.getActionsFactory(), this.getPlugin(), paging.getFillItem().getActions(), context.getViewer())
            )
      );
      return pagination;
   }

   protected ItemStack getPlayerHead(OfflinePlayer player) {
      return ItemBuilder.of(XMaterial.PLAYER_HEAD).setHeadOwner(player).build();
   }

   protected ContainerItemsConfiguration.ItemConfiguration r(ContainerItemsConfiguration.ItemConfiguration item, Object... values) {
      ContainerItemsConfiguration.ItemConfiguration clone = item.clone();
      clone.setLore(Translations.r(item.getLore(), values));
      return clone;
   }

   protected String f(boolean state) {
      return state ? String.format("&a%s", this.t("labels.yes-word")[0]) : String.format("&c%s", this.t("labels.no-word")[0]);
   }

   protected String r(String rawMessage, Object... values) {
      return Translations.r(rawMessage, values);
   }

   protected List<String> r(List<String> rawMessage, Object... values) {
      return Translations.r(rawMessage, values);
   }
}
