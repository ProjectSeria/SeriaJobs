package id.seria.gacha.lib.common.uicomponents.newest.configuration;

import com.google.common.collect.Lists;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.objects.ComplexMaterial;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;
import lombok.Generated;

public abstract class PaginatedContainerConfiguration extends ContainerConfiguration {
   @ConfigField
   protected PaginatedContainerConfiguration.PaginationConfiguration pagination = new PaginatedContainerConfiguration.PaginationConfiguration();

   public PaginatedContainerConfiguration() {
   }

   @Generated
   public void setPagination(PaginatedContainerConfiguration.PaginationConfiguration pagination) {
      this.pagination = pagination;
   }

   @Generated
   public PaginatedContainerConfiguration.PaginationConfiguration getPagination() {
      return this.pagination;
   }

   public static class PaginationConfiguration {
      @ConfigField
      protected PaginationSettings.ItemsDirection directionItems = PaginationSettings.ItemsDirection.HORIZONTAL;
      @ConfigField
      protected PaginationSettings.ItemsAlign alignItems = PaginationSettings.ItemsAlign.LEFT;
      @ConfigField
      private SlotCompound slots = new SlotCompound(10, 16);
      @ConfigField
      protected ContainerItemsConfiguration.NonSlotItemConfiguration fillItem = new ContainerItemsConfiguration.NonSlotItemConfiguration(
         new ComplexMaterial(XMaterial.AIR), 0, "", false, "", Lists.newArrayList(), Lists.newArrayList()
      );
      @ConfigField
      protected ContainerItemsConfiguration.ItemConfiguration previousItem = new ContainerItemsConfiguration.ItemConfiguration(
         new SlotCompound(9),
         new ComplexMaterial(XMaterial.ARROW),
         1,
         0,
         null,
         false,
         "&aPrevious page - Page %page%",
         Lists.newArrayList(new String[]{"&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<"}),
         Lists.newArrayList()
      );
      @ConfigField
      protected ContainerItemsConfiguration.ItemConfiguration nextItem = new ContainerItemsConfiguration.ItemConfiguration(
         new SlotCompound(17),
         new ComplexMaterial(XMaterial.ARROW),
         1,
         0,
         null,
         false,
         "&aNext page - Page %page%",
         Lists.newArrayList(new String[]{"&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>"}),
         Lists.newArrayList()
      );

      public PaginationConfiguration(
         PaginationSettings.ItemsAlign alignItems,
         SlotCompound slots,
         ContainerItemsConfiguration.NonSlotItemConfiguration fillItem,
         ContainerItemsConfiguration.ItemConfiguration previousItem,
         ContainerItemsConfiguration.ItemConfiguration nextItem
      ) {
         this.directionItems = PaginationSettings.ItemsDirection.HORIZONTAL;
         this.alignItems = PaginationSettings.ItemsAlign.LEFT;
         this.slots = new SlotCompound(10, 16);
         this.fillItem = new ContainerItemsConfiguration.NonSlotItemConfiguration(
            new ComplexMaterial(XMaterial.AIR), 0, "", false, "", Lists.newArrayList(), Lists.newArrayList()
         );
         this.previousItem = new ContainerItemsConfiguration.ItemConfiguration(
            new SlotCompound(9),
            new ComplexMaterial(XMaterial.ARROW),
            1,
            0,
            null,
            false,
            "&aPrevious page - Page %page%",
            Lists.newArrayList(new String[]{"&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<&a<&2<"}),
            Lists.newArrayList()
         );
         this.nextItem = new ContainerItemsConfiguration.ItemConfiguration(
            new SlotCompound(17),
            new ComplexMaterial(XMaterial.ARROW),
            1,
            0,
            null,
            false,
            "&aNext page - Page %page%",
            Lists.newArrayList(new String[]{"&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>&a>&2>"}),
            Lists.newArrayList()
         );
         this.directionItems = this.directionItems;
         this.alignItems = alignItems;
         this.slots = slots;
         this.fillItem = fillItem;
         this.previousItem = previousItem;
         this.nextItem = nextItem;
      }

      @Generated
      public void setDirectionItems(PaginationSettings.ItemsDirection directionItems) {
         this.directionItems = directionItems;
      }

      @Generated
      public void setAlignItems(PaginationSettings.ItemsAlign alignItems) {
         this.alignItems = alignItems;
      }

      @Generated
      public void setSlots(SlotCompound slots) {
         this.slots = slots;
      }

      @Generated
      public void setFillItem(ContainerItemsConfiguration.NonSlotItemConfiguration fillItem) {
         this.fillItem = fillItem;
      }

      @Generated
      public void setPreviousItem(ContainerItemsConfiguration.ItemConfiguration previousItem) {
         this.previousItem = previousItem;
      }

      @Generated
      public void setNextItem(ContainerItemsConfiguration.ItemConfiguration nextItem) {
         this.nextItem = nextItem;
      }

      @Generated
      public PaginationSettings.ItemsDirection getDirectionItems() {
         return this.directionItems;
      }

      @Generated
      public PaginationSettings.ItemsAlign getAlignItems() {
         return this.alignItems;
      }

      @Generated
      public SlotCompound getSlots() {
         return this.slots;
      }

      @Generated
      public ContainerItemsConfiguration.NonSlotItemConfiguration getFillItem() {
         return this.fillItem;
      }

      @Generated
      public ContainerItemsConfiguration.ItemConfiguration getPreviousItem() {
         return this.previousItem;
      }

      @Generated
      public ContainerItemsConfiguration.ItemConfiguration getNextItem() {
         return this.nextItem;
      }

      @Generated
      public PaginationConfiguration() {
      }

      @Generated
      public PaginationConfiguration(
         PaginationSettings.ItemsDirection directionItems,
         PaginationSettings.ItemsAlign alignItems,
         SlotCompound slots,
         ContainerItemsConfiguration.NonSlotItemConfiguration fillItem,
         ContainerItemsConfiguration.ItemConfiguration previousItem,
         ContainerItemsConfiguration.ItemConfiguration nextItem
      ) {
         this.directionItems = directionItems;
         this.alignItems = alignItems;
         this.slots = slots;
         this.fillItem = fillItem;
         this.previousItem = previousItem;
         this.nextItem = nextItem;
      }
   }
}
