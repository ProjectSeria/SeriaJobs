package io.lumine.mythic.api;
import io.lumine.mythic.core.items.MythicItem;
import org.bukkit.inventory.ItemStack;
import java.util.Optional;
public class MythicProvider {
    public static MythicProvider get() { return null; }
    public ItemManager getItemManager() { return null; }
    public interface ItemManager {
        Optional<MythicItem> getItem(String id);
        String getMythicTypeFromItem(ItemStack item);
    }
}
