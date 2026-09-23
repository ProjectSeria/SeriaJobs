package id.seria.gacha.lib.common.uicomponents.newest.data;

import id.seria.gacha.api.sdk.locales.translatable.Translatable;
import id.seria.gacha.lib.common.uicomponents.newest.pagination.PaginationSettings;
import id.seria.gacha.lib.common.utils.configuration.objects.ComplexMaterial;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.xseries.XMaterial;
import java.util.List;
import lombok.Generated;

public class PagingData {
   private PaginationSettings.ItemsAlign itemsAlign = PaginationSettings.ItemsAlign.LEFT;
   private PaginationSettings.ItemsDirection itemsDirection = PaginationSettings.ItemsDirection.HORIZONTAL;
   private PagingData.PaginationController previousPageController = new PagingData.PaginationController(
      new SlotCompound(),
      new ComplexMaterial(XMaterial.ARROW),
      0,
      Translatable.key("layout.extras.previous-page.title"),
      Translatable.key("layout.extras.previous-page.description")
   );
   private PagingData.PaginationController nextPageController = new PagingData.PaginationController(
      new SlotCompound(),
      new ComplexMaterial(XMaterial.ARROW),
      0,
      Translatable.key("layout.extras.next-page.title"),
      Translatable.key("layout.extras.next-page.description")
   );

   public PagingData(PaginationSettings.ItemsAlign align, int previousPageControllerSlot, int nextPageControllerSlot) {
      this.itemsAlign = align;
      this.previousPageController.slotsCompound = new SlotCompound(previousPageControllerSlot);
      this.nextPageController.slotsCompound = new SlotCompound(nextPageControllerSlot);
   }

   public PagingData(PaginationSettings.ItemsAlign align, SlotCompound previousPageControllerSlot, SlotCompound nextPageControllerSlot) {
      this.itemsAlign = align;
      this.previousPageController.slotsCompound = previousPageControllerSlot;
      this.nextPageController.slotsCompound = nextPageControllerSlot;
   }

   public PagingData(
      PaginationSettings.ItemsAlign align, PagingData.PaginationController previousPageController, PagingData.PaginationController nextPageController
   ) {
      this.itemsAlign = align;
      this.previousPageController = previousPageController;
      this.nextPageController = nextPageController;
   }

   public PagingData(
      PaginationSettings.ItemsAlign align,
      PaginationSettings.ItemsDirection direction,
      SlotCompound previousPageControllerSlot,
      SlotCompound nextPageControllerSlot
   ) {
      this.itemsAlign = align;
      this.itemsDirection = direction;
      this.previousPageController.slotsCompound = previousPageControllerSlot;
      this.nextPageController.slotsCompound = nextPageControllerSlot;
   }

   @Generated
   public PaginationSettings.ItemsAlign getItemsAlign() {
      return this.itemsAlign;
   }

   @Generated
   public PaginationSettings.ItemsDirection getItemsDirection() {
      return this.itemsDirection;
   }

   @Generated
   public PagingData.PaginationController getPreviousPageController() {
      return this.previousPageController;
   }

   @Generated
   public PagingData.PaginationController getNextPageController() {
      return this.nextPageController;
   }

   @Generated
   public PagingData() {
   }

   @Generated
   public PagingData(
      PaginationSettings.ItemsAlign itemsAlign,
      PaginationSettings.ItemsDirection itemsDirection,
      PagingData.PaginationController previousPageController,
      PagingData.PaginationController nextPageController
   ) {
      this.itemsAlign = itemsAlign;
      this.itemsDirection = itemsDirection;
      this.previousPageController = previousPageController;
      this.nextPageController = nextPageController;
   }

   public static class PaginationController {
      private SlotCompound slotsCompound;
      private final ComplexMaterial material;
      private final int customModelData;
      private final String itemModel;
      private final Translatable name;
      private final Translatable lore;

      @Deprecated
      public PaginationController(SlotCompound slots, ComplexMaterial material, int customModelData, String itemModel, String name, List<String> lore) {
         this.slotsCompound = slots;
         this.material = material;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.name = Translatable.literal(name);
         this.lore = Translatable.literal(lore);
      }

      @Deprecated
      public PaginationController(SlotCompound slots, ComplexMaterial material, int customModelData, String name, List<String> lore) {
         this.slotsCompound = slots;
         this.material = material;
         this.customModelData = customModelData;
         this.itemModel = null;
         this.name = Translatable.literal(name);
         this.lore = Translatable.literal(lore);
      }

      @Deprecated
      public PaginationController(SlotCompound slots, ComplexMaterial material, int customModelData, Translatable name, Translatable lore) {
         this.slotsCompound = slots;
         this.material = material;
         this.customModelData = customModelData;
         this.itemModel = null;
         this.name = name;
         this.lore = lore;
      }

      @Generated
      public SlotCompound getSlotsCompound() {
         return this.slotsCompound;
      }

      @Generated
      public ComplexMaterial getMaterial() {
         return this.material;
      }

      @Generated
      public int getCustomModelData() {
         return this.customModelData;
      }

      @Generated
      public String getItemModel() {
         return this.itemModel;
      }

      @Generated
      public Translatable getName() {
         return this.name;
      }

      @Generated
      public Translatable getLore() {
         return this.lore;
      }

      @Generated
      public PaginationController(
         SlotCompound slotsCompound, ComplexMaterial material, int customModelData, String itemModel, Translatable name, Translatable lore
      ) {
         this.slotsCompound = slotsCompound;
         this.material = material;
         this.customModelData = customModelData;
         this.itemModel = itemModel;
         this.name = name;
         this.lore = lore;
      }
   }
}
