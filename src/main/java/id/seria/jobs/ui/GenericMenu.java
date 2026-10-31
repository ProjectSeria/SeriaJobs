package id.seria.jobs.ui;

import id.seria.jobs.SeriaJobs;
import org.bukkit.entity.Player;

/**
 * Concrete generic menu implementation capable of loading and displaying
 * any custom or secondary menu configuration YAML registered in MenuRegistry.
 */
public class GenericMenu extends BaseMenu {

    public GenericMenu(SeriaJobs plugin, Player player, String menuConfigName) {
        super(plugin, player, menuConfigName);
    }

    @Override
    public void build() {
        int size = menuConfig != null ? menuConfig.getInt("size", 54) : 54;
        String rawTitle = menuConfig != null ? menuConfig.getString("title", "Menu") : "Menu";
        createInventory(size, rawTitle);
        applyFillers();
        applyMenuItems();
    }
}
