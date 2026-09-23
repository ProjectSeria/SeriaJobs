package id.seria.gacha.menus.crates;

import com.google.common.collect.Lists;
import id.seria.gacha.internal.Placeholders;
import id.seria.gacha.internal.menus.GenericConfigurableLayout;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.ContainerConfiguration;
import id.seria.gacha.lib.common.uicomponents.newest.configuration.ContainerItemsConfiguration;
import id.seria.gacha.lib.common.uicomponents.newest.container.ContainerType;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.CloseViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.OpenViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.context.RenderViewContext;
import id.seria.gacha.lib.common.uicomponents.newest.container.holders.ContainerView;
import id.seria.gacha.lib.common.uicomponents.newest.others.VirtualItem;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.objects.ComplexMaterial;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.PlaceholderUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import lombok.Generated;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

public class ConfirmationMenu extends GenericConfigurableLayout<ConfirmationMenu.ConfirmationMenuConfiguration> {
   private final Runnable onConfirm;
   private final Runnable onCancel;
   private final ItemStack displayItem;
   private final String displayName;
   private boolean responded = false;

   public ConfirmationMenu(
      ConfirmationMenu.ConfirmationMenuConfiguration configuration,
      @NonNull Runnable onConfirm,
      @NonNull Runnable onCancel,
      ItemStack displayItem,
      String displayName
   ) {
      super(configuration);
      if (onConfirm == null) {
         throw new NullPointerException("onConfirm is marked non-null but is null");
      }

      if (onCancel == null) {
         throw new NullPointerException("onCancel is marked non-null but is null");
      }

      this.onConfirm = onConfirm;
      this.onCancel = onCancel;
      this.displayItem = displayItem;
      this.displayName = displayName;
   }

   @Override
   public void onPrepare(ContainerView view) {
   }

   @Override
   public void onOpen(OpenViewContext context) {
      super.onOpen(context);
      context.setContainerTitle(
         Placeholders.setPlaceholders(
            PlaceholderUtil.replacePlaceholdersAndColors(this.getPlugin(), this.getConfiguration().getSettings().getTitle()), context.getViewer()
         )
      );
      context.setContainerType(ContainerType.fromRows(this.getConfiguration().getSettings().getRows()));
   }

   @Override
   public void onRender(RenderViewContext context) {
      super.onRender(context);
      this.setItem(context, this.getConfiguration().getConfirmItem(), click -> {
         this.responded = true;
         click.getViewer().closeInventory();
         this.onConfirm.run();
      });
      this.setItem(context, this.getConfiguration().getCancelItem(), click -> {
         this.responded = true;
         click.getViewer().closeInventory();
         this.onCancel.run();
      });
      if (this.displayItem != null) {
         ItemStack item = ItemBuilder.of(this.displayItem).setDisplayName(this.displayName != null ? this.displayName : "").build();
         new VirtualItem().withSlots(this.getConfiguration().getItemSlot()).withItem(item).whenClick(click -> {}).render(context);
      }
   }

   @Override
   public void onClose(CloseViewContext context) {
      super.onClose(context);
      if (!this.responded) {
         this.responded = true;
         this.onCancel.run();
      }
   }

   @Generated
   public Runnable getOnConfirm() {
      return this.onConfirm;
   }

   @Generated
   public Runnable getOnCancel() {
      return this.onCancel;
   }

   @Generated
   public ItemStack getDisplayItem() {
      return this.displayItem;
   }

   @Generated
   public String getDisplayName() {
      return this.displayName;
   }

   @Generated
   public boolean isResponded() {
      return this.responded;
   }

   public static class ConfirmationMenuConfiguration extends ContainerConfiguration {
      @ConfigField(path = "confirm")
      private final ContainerItemsConfiguration.NonActionsItemConfiguration confirmItem;
      @ConfigField(path = "cancel")
      private final ContainerItemsConfiguration.NonActionsItemConfiguration cancelItem;
      @ConfigField(path = "item-slot")
      private SlotCompound itemSlot;

      public ConfirmationMenuConfiguration() {
         this.settings.setType("CONFIRMATION");
         this.settings.setTitle("Are you sure?");
         this.settings.setRows(3);
         this.settings.setRefreshEnabled(false);
         this.settings.setRefreshRate(20);
         this.confirmItem = new ContainerItemsConfiguration.NonActionsItemConfiguration(
            new SlotCompound(10, 11),
            new ComplexMaterial(XMaterial.GREEN_STAINED_GLASS_PANE),
            1,
            0,
            "",
            false,
            "&aConfirm",
            Lists.newArrayList(new String[]{"&7Click to confirm."})
         );
         this.cancelItem = new ContainerItemsConfiguration.NonActionsItemConfiguration(
            new SlotCompound(15, 16),
            new ComplexMaterial(XMaterial.RED_STAINED_GLASS_PANE),
            1,
            0,
            "",
            false,
            "&cCancel",
            Lists.newArrayList(new String[]{"&7Click to cancel."})
         );
         this.itemSlot = SlotCompound.from(5, 2);
         ContainerItemsConfiguration.ItemConfiguration filler = new ContainerItemsConfiguration.ItemConfiguration(
            SlotCompound.fromGrid(0, 3, 9),
            new ComplexMaterial(XMaterial.BLACK_STAINED_GLASS_PANE),
            1,
            0,
            "",
            false,
            "&r",
            Lists.newArrayList(),
            Lists.newArrayList()
         );
         filler.setHideTooltip(true);
         this.items.put("filler", filler);
      }

      public ConfirmationMenu.ConfirmationMenuConfiguration clone() {
         return new ConfirmationMenu.ConfirmationMenuConfiguration();
      }

      @Generated
      public ContainerItemsConfiguration.NonActionsItemConfiguration getConfirmItem() {
         return this.confirmItem;
      }

      @Generated
      public ContainerItemsConfiguration.NonActionsItemConfiguration getCancelItem() {
         return this.cancelItem;
      }

      @Generated
      public SlotCompound getItemSlot() {
         return this.itemSlot;
      }
   }
}
