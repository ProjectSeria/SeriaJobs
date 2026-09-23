package com.willfp.eco.core.items;
import org.bukkit.inventory.ItemStack;
public class Items {
    public static CustomItem getCustomItem(ItemStack item) { return null; }
    public static TestLookup lookup(String id) { return null; }
    public interface TestLookup {
        ItemStack getItem();
    }
}
