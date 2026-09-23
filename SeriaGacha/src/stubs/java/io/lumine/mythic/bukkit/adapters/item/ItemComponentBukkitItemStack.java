package io.lumine.mythic.bukkit.adapters.item;
import io.lumine.mythic.api.adapters.AbstractItemStack;
import org.bukkit.inventory.ItemStack;
public class ItemComponentBukkitItemStack implements AbstractItemStack {
    public ItemComponentBukkitItemStack(ItemStack is) {}
    public int getAmount() { return 1; }
    public AbstractItemStack copy() { return this; }
    public AbstractItemStack amount(int a) { return this; }
}