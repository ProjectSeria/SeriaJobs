package id.seria.gacha.lib.common.uicomponents.newest.container;

import id.seria.gacha.lib.common.utils.utility.MathUtil;
import lombok.Generated;

public enum ContainerType {
   CONTAINER_9X1(1),
   CONTAINER_9X2(2),
   CONTAINER_9X3(3),
   CONTAINER_9X4(4),
   CONTAINER_9X5(5),
   CONTAINER_9X6(6);

   private int rows;

   public int getSize() {
      return this.rows * 9;
   }

   public static ContainerType fromRows(int rows) {
      return valueOf("CONTAINER_9X" + MathUtil.clamp(rows, 1, 6));
   }

   @Generated
   public int getRows() {
      return this.rows;
   }

   @Generated
   ContainerType(final int rows) {
      this.rows = rows;
   }
}
