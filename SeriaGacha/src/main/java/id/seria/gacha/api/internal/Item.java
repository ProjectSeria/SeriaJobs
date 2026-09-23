package id.seria.gacha.api.internal;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Item {
   protected Item() {
   }

   public abstract String getNamespace();

   @NotNull
   public abstract ItemStack build();

   @Nullable
   public abstract String getDisplayName();

   @NotNull
   public abstract List<String> getLore();

   public abstract Material getType();

   public abstract int getAmount();

   public abstract Item clone();
}
