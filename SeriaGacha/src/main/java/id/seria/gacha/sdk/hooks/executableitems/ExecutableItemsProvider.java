package id.seria.gacha.sdk.hooks.executableitems;

import id.seria.gacha.sdk.platforms.server.hooks.NamespacedItemHook;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import com.ssomar.score.api.executableitems.ExecutableItemsAPI;
import com.ssomar.score.api.executableitems.config.ExecutableItemInterface;
import java.util.Optional;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ExecutableItemsProvider extends ServerHook implements NamespacedItemHook {
   protected ExecutableItemsProvider() {
   }

   @Override
   public void init() {
   }

   @Override
   public void load() {
   }

   @Override
   public void unload() {
   }

   @Override
   public void reload() {
      this.unload();
      this.load();
   }

   @Override
   public String getNamespace() {
      return "executableitem";
   }

   @Nullable
   @Override
   public String getIdFromItem(ItemStack itemStack) {
      Optional<ExecutableItemInterface> optional = ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(itemStack);
      if (!optional.isPresent()) {
         return null;
      }

      ExecutableItemInterface item = optional.get();
      return item.getId();
   }

   @Nullable
   public ItemStack getItemFromId(@NonNull Player player, @NonNull String id) {
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }

      if (id == null) {
         throw new NullPointerException("id is marked non-null but is null");
      }

      Optional<ExecutableItemInterface> optional = ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(id);
      if (!optional.isPresent()) {
         return null;
      }

      ExecutableItemInterface item = optional.get();
      return item.buildItem(1, Optional.empty(), Optional.of(player));
   }

   @Nullable
   @Override
   public ItemStack getItemFromId(@NonNull String id) {
      if (id == null) {
         throw new NullPointerException("id is marked non-null but is null");
      }

      Optional<ExecutableItemInterface> optional = ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(id);
      if (!optional.isPresent()) {
         return null;
      }

      ExecutableItemInterface item = optional.get();
      return item.buildItem(1, Optional.empty(), Optional.empty());
   }
}
