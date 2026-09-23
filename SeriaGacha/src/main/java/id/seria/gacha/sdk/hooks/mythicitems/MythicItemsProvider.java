package id.seria.gacha.sdk.hooks.mythicitems;

import id.seria.gacha.sdk.platforms.server.hooks.NamespacedItemHook;
import id.seria.gacha.sdk.platforms.server.hooks.ServerHook;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.adapters.AbstractItemStack;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.drops.DropMetadata;
import io.lumine.mythic.api.drops.IItemDrop;
import io.lumine.mythic.bukkit.adapters.BukkitItemStack;
import io.lumine.mythic.bukkit.adapters.item.ItemComponentBukkitItemStack;
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import io.lumine.mythic.core.drops.droppables.ItemDrop;
import io.lumine.mythic.core.items.MythicItem;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MythicItemsProvider extends ServerHook implements Listener, NamespacedItemHook {
   private final Map<String, Function<String[], ItemStack>> registeredDrops = new LinkedHashMap<>();

   protected MythicItemsProvider() {
   }

   @Override
   public void init() {
   }

   @Override
   public void load() {
      this.getHookRegistry().getPlugin().getEventBus().registerListener(this);
   }

   @Override
   public void unload() {
      this.getHookRegistry().getPlugin().getEventBus().unregisterListener(this);
   }

   @Override
   public void reload() {
   }

   @EventHandler
   public final void onMythicDropLoadEvent(MythicDropLoadEvent event) {
      for (Entry<String, Function<String[], ItemStack>> entry : this.registeredDrops.entrySet()) {
         String key = entry.getKey();
         String line = event.getContainer().getLine().toLowerCase();
         if (line.startsWith(key.toLowerCase())) {
            String rest = line.substring(key.length()).trim();
            String[] args = rest.split("[: ]+");
            if (args.length != 0) {
               event.register(new MythicItemsProvider.MythicDrop(entry.getValue(), args, event.getContainer().getLine(), event.getConfig()));
               break;
            }
         }
      }
   }

   public void registerItemDrop(String namespace, Function<String[], ItemStack> itemFunction) {
      this.registeredDrops.put(namespace, itemFunction);
   }

   @Override
   public String getNamespace() {
      return "mythicitem";
   }

   @Nullable
   @Override
   public String getIdFromItem(ItemStack item) {
      return MythicProvider.get().getItemManager().getMythicTypeFromItem(item);
   }

   @Nullable
   @Override
   public ItemStack getItemFromId(String id) {
      MythicItem mythicItem = (MythicItem)MythicProvider.get().getItemManager().getItem(id).orElse(null);
      if (mythicItem == null) {
         return null;
      }

      BukkitItemStack bukkitItemStack = (BukkitItemStack)mythicItem.generateItemStack(1);
      return bukkitItemStack != null ? bukkitItemStack.getItemStack() : null;
   }

   private class MythicDrop extends ItemDrop implements IItemDrop {
      @NonNull
      private final String[] args;
      @NotNull
      private final Function<String[], ItemStack> function;

      public MythicDrop(
         @NotNull Function<String[], ItemStack> function, @NonNull String[] args, @NotNull String mythicLine, @NotNull MythicLineConfig mythicConfig
      ) {
         super(mythicLine, mythicConfig);
         if (args == null) {
            throw new NullPointerException("args is marked non-null but is null");
         }

         this.args = args;
         this.function = function;
      }

      @Nullable
      public AbstractItemStack getDrop(@NotNull DropMetadata meta, double amount) {
         ItemStack stack = this.function.apply(this.args);
         if (stack == null) {
            MythicItemsProvider.this.getHookRegistry()
               .getPlugin()
               .getLogger()
               .error("Invalid drop item generated for MythicDrop with args: " + Arrays.toString(this.args));
            return null;
         } else {
            ItemComponentBukkitItemStack item = new ItemComponentBukkitItemStack(stack);
            int finalAmount = this.rollBonuses(meta, (int)Math.floor(item.getAmount() * amount));
            return item.copy().amount(finalAmount);
         }
      }
   }
}
