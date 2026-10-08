package id.seria.mmoae.listener;

import id.seria.mmoae.hook.AEHook;
import id.seria.mmoae.service.AEBridgeService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ItemPipelineListener implements Listener {

    private final AEBridgeService bridgeService;
    private final boolean onPickup;
    private final boolean onCraft;
    private final boolean onInventoryClick;
    private final boolean onItemHeld;
    private final boolean onJoin;

    public ItemPipelineListener(
            AEBridgeService bridgeService,
            boolean onPickup,
            boolean onCraft,
            boolean onInventoryClick,
            boolean onItemHeld,
            boolean onJoin
    ) {
        this.bridgeService = bridgeService;
        this.onPickup = onPickup;
        this.onCraft = onCraft;
        this.onInventoryClick = onInventoryClick;
        this.onItemHeld = onItemHeld;
        this.onJoin = onJoin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!onPickup || !AEHook.isAvailable()) return;

        ItemStack current = event.getItem().getItemStack();
        if (current == null || current.getType().isAir()) return;

        if (!bridgeService.isFullyEnchanted(current)) {
            ItemStack updated = bridgeService.processItem(current);
            if (updated != null && updated != current) {
                event.getItem().setItemStack(updated);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!onCraft || !AEHook.isAvailable()) return;

        ItemStack result = event.getCurrentItem();
        if (result == null || result.getType().isAir()) return;

        if (!bridgeService.isFullyEnchanted(result)) {
            ItemStack updated = bridgeService.processItem(result);
            if (updated != null && updated != result) {
                event.setCurrentItem(updated);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!onInventoryClick || !AEHook.isAvailable()) return;

        ItemStack current = event.getCurrentItem();
        if (current != null && !current.getType().isAir() && !bridgeService.isFullyEnchanted(current)) {
            ItemStack updated = bridgeService.processItem(current);
            if (updated != null && updated != current) {
                event.setCurrentItem(updated);
            }
        }

        ItemStack cursor = event.getCursor();
        if (cursor != null && !cursor.getType().isAir() && !bridgeService.isFullyEnchanted(cursor)) {
            ItemStack updated = bridgeService.processItem(cursor);
            if (updated != null && updated != cursor) {
                event.getView().setCursor(updated);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        if (!onItemHeld || !AEHook.isAvailable()) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item != null && !item.getType().isAir() && !bridgeService.isFullyEnchanted(item)) {
            ItemStack updated = bridgeService.processItem(item);
            if (updated != null && updated != item) {
                player.getInventory().setItem(event.getNewSlot(), updated);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!onJoin || !AEHook.isAvailable()) return;

        Player player = event.getPlayer();
        PlayerInventory inv = player.getInventory();

        // Scan equipment and contents
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack it = inv.getItem(i);
            if (it != null && !it.getType().isAir() && !bridgeService.isFullyEnchanted(it)) {
                ItemStack updated = bridgeService.processItem(it);
                if (updated != null && updated != it) {
                    inv.setItem(i, updated);
                }
            }
        }
    }
}
