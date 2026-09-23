package id.seria.gacha.lib.common.uicomponents.newest.container.holders;

import id.seria.gacha.lib.common.uicomponents.newest.container.PaginatedContainerFrame;
import id.seria.gacha.lib.common.utils.utility.MathUtil;
import lombok.Generated;
import org.bukkit.entity.Player;

@Deprecated
public class PaginatedContainerView extends ContainerView {
   private int currentPage = 1;

   public PaginatedContainerView(PaginatedContainerFrame<?> container, Player viewer) {
      super(container, viewer);
   }

   public final int getPreviousPage() {
      return Math.max(1, this.currentPage - 1);
   }

   public final int getNextPage() {
      return Math.min(this.getContainer().getMaxPages(this), this.currentPage + 1);
   }

   public final boolean isFirstPage() {
      return this.currentPage == 1;
   }

   public final boolean isLastPage() {
      return this.currentPage == this.getContainer().getMaxPages(this);
   }

   public final void switchTo(int page) {
      this.currentPage = MathUtil.clamp(page, 1, this.getContainer().getMaxPages(this));
      this.getContainer().updateView(this.viewer);
   }

   public final boolean switchToPreviousPage() {
      if (this.isFirstPage()) {
         return false;
      }

      this.switchTo(this.currentPage - 1);
      return true;
   }

   public final boolean switchToNextPage() {
      if (this.isLastPage()) {
         return false;
      }

      this.switchTo(this.currentPage + 1);
      return true;
   }

   public PaginatedContainerFrame<?> getContainer() {
      return (PaginatedContainerFrame<?>)this.container;
   }

   @Generated
   public int getCurrentPage() {
      return this.currentPage;
   }
}
