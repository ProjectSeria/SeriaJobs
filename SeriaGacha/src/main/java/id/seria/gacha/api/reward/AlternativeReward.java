package id.seria.gacha.api.reward;

import com.google.common.collect.Lists;
import id.seria.gacha.api.internal.Item;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.NotImplementedException;

public interface AlternativeReward extends BaseReward {
   boolean isEnabled();

   @Deprecated
   Item getItem();

   @Deprecated
   boolean isVirtual();

   @Override
   default Item getDisplayItem() {
      return this.getItem();
   }

   @Override
   default List<? extends Item> getWinItems() {
      return this.isVirtual() ? new ArrayList<>() : Lists.newArrayList(new Item[]{this.getItem()});
   }

   @Override
   default boolean isAlternative() {
      return true;
   }

   @Override
   default String getIdentifier() {
      throw new NotImplementedException("Not compatible");
   }

   @Override
   default double getWeight() {
      throw new NotImplementedException("Not compatible");
   }
}
