package id.seria.gacha.lib.common.uicomponents.newest.pagination;

import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import lombok.Generated;

public class PaginationSettings {
   private final SlotCompound compound;
   private final PaginationSettings.ItemsAlign align;
   private final PaginationSettings.ItemsDirection direction;

   public PaginationSettings(SlotCompound compound, PaginationSettings.ItemsAlign align) {
      this.compound = compound;
      this.align = align;
      this.direction = PaginationSettings.ItemsDirection.HORIZONTAL;
   }

   @Generated
   public SlotCompound getCompound() {
      return this.compound;
   }

   @Generated
   public PaginationSettings.ItemsAlign getAlign() {
      return this.align;
   }

   @Generated
   public PaginationSettings.ItemsDirection getDirection() {
      return this.direction;
   }

   @Generated
   public PaginationSettings(SlotCompound compound, PaginationSettings.ItemsAlign align, PaginationSettings.ItemsDirection direction) {
      this.compound = compound;
      this.align = align;
      this.direction = direction;
   }

   public enum ItemsAlign {
      LEFT,
      CENTER,
      RIGHT;

      ItemsAlign() {
      }
   }

   public enum ItemsDirection {
      VERTICAL,
      HORIZONTAL;

      ItemsDirection() {
      }
   }
}
