package id.seria.jobs.ui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof BaseMenu menu) {
            menu.handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof BaseMenu) {
            event.setCancelled(true);
        }
    }
}
