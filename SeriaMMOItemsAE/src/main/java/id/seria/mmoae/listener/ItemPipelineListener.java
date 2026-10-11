package id.seria.mmoae.listener;

import id.seria.mmoae.hook.AEHook;
import id.seria.mmoae.service.AEBridgeService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ItemPipelineListener implements Listener {

    private final AEBridgeService bridgeService;
    private final boolean onPickup;
    private final boolean onCraft;
    private final boolean onInventoryClick;
    private final boolean onItemHeld;
    private final boolean onJoin;
    private final boolean onInteract;
    private final boolean onAttack;

    public ItemPipelineListener(
            AEBridgeService bridgeService,
            boolean onPickup,
            boolean onCraft,
            boolean onInventoryClick,
            boolean onItemHeld,
            boolean onJoin,
            boolean onInteract,
            boolean onAttack
    ) {
        this.bridgeService = bridgeService;
        this.onPickup = onPickup;
        this.onCraft = onCraft;
        this.onInventoryClick = onInventoryClick;
        this.onItemHeld = onItemHeld;
        this.onJoin = onJoin;
        this.onInteract = onInteract;
        this.onAttack = onAttack;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemBuild(net.Indyuce.mmoitems.api.event.ItemBuildEvent event) {
        if (!AEHook.isAvailable()) return;
        ItemStack current = event.getItemStack();
        if (current == null || current.getType().isAir()) return;

        if (!bridgeService.isFullyEnchanted(current)) {
            ItemStack updated = bridgeService.processItem(current);
            if (updated != null && updated != current) {
                event.setItemStack(updated);
            }
        }
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

    @EventHandler(priority = EventPriority.LOW)
    public void onPreInventoryClick(InventoryClickEvent event) {
        if (!onInventoryClick || !AEHook.isAvailable()) return;
        ItemStack scrollItem = event.getCursor();
        ItemStack targetItem = event.getCurrentItem();
        if (scrollItem != null && !scrollItem.getType().isAir() && targetItem != null && !targetItem.getType().isAir()) {
            if (AEHook.isBlackScroll(scrollItem) && id.seria.mmoae.hook.MMOItemsHook.isMMOItem(targetItem)) {
                if (targetItem.hasItemMeta()) {
                    org.bukkit.inventory.meta.ItemMeta m = targetItem.getItemMeta();
                    m.getPersistentDataContainer().set(AEBridgeService.KEY_DISENCHANTED,
                            org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
                    m.getPersistentDataContainer().set(AEBridgeService.KEY_INITIALIZED,
                            org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
                    targetItem.setItemMeta(m);
                    try {
                        io.lumine.mythic.lib.api.item.NBTItem nbt = io.lumine.mythic.lib.api.item.NBTItem.get(targetItem);
                        targetItem = nbt.addTag(new io.lumine.mythic.lib.api.item.ItemTag("MMOAE_DISENCHANTED", true))
                                        .addTag(new io.lumine.mythic.lib.api.item.ItemTag("MMOAE_INITIALIZED", true))
                                        .toItem();
                    } catch (Throwable ignored) {}
                    event.setCurrentItem(targetItem);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!onInventoryClick || !AEHook.isAvailable()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check if player applied a Black Scroll on an MMOItem
        ItemStack scrollItem = event.getCursor();
        ItemStack targetItem = event.getCurrentItem();
        if (scrollItem != null && !scrollItem.getType().isAir() && targetItem != null && !targetItem.getType().isAir()) {
            if (AEHook.isBlackScroll(scrollItem) && id.seria.mmoae.hook.MMOItemsHook.isMMOItem(targetItem)) {
                if (targetItem.hasItemMeta()) {
                    org.bukkit.inventory.meta.ItemMeta m = targetItem.getItemMeta();
                    m.getPersistentDataContainer().set(AEBridgeService.KEY_DISENCHANTED,
                            org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
                    m.getPersistentDataContainer().set(AEBridgeService.KEY_INITIALIZED,
                            org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
                    targetItem.setItemMeta(m);
                    try {
                        io.lumine.mythic.lib.api.item.NBTItem nbt = io.lumine.mythic.lib.api.item.NBTItem.get(targetItem);
                        targetItem = nbt.addTag(new io.lumine.mythic.lib.api.item.ItemTag("MMOAE_DISENCHANTED", true))
                                        .addTag(new io.lumine.mythic.lib.api.item.ItemTag("MMOAE_INITIALIZED", true))
                                        .toItem();
                    } catch (Throwable ignored) {}
                    event.setCurrentItem(targetItem);
                }
            }
        }

        // Schedule 1-tick delay: allows Bukkit & AdvancedEnchantments to finish item transfer,
        // book consumption, and slot mutations completely before checking.
        // Guarantees zero cursor conflict and ZERO item duplication!
        org.bukkit.Bukkit.getScheduler().runTask(id.seria.mmoae.SeriaMMOItemsAEPlugin.getInstance(), () -> {
            if (!player.isOnline()) return;

            // Check cursor
            ItemStack cursor = player.getItemOnCursor();
            if (cursor != null && !cursor.getType().isAir() && !bridgeService.isFullyEnchanted(cursor)) {
                ItemStack updated = bridgeService.processItem(cursor);
                if (updated != null && updated != cursor) {
                    player.setItemOnCursor(updated);
                }
            }

            // Check clicked slot if view is valid
            try {
                if (player.getOpenInventory() != null) {
                    int rawSlot = event.getRawSlot();
                    if (rawSlot >= 0 && rawSlot < player.getOpenInventory().countSlots()) {
                        ItemStack slotItem = player.getOpenInventory().getItem(rawSlot);
                        if (slotItem != null && !slotItem.getType().isAir() && !bridgeService.isFullyEnchanted(slotItem)) {
                            ItemStack updated = bridgeService.processItem(slotItem);
                            if (updated != null && updated != slotItem) {
                                player.getOpenInventory().setItem(rawSlot, updated);
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {}

            // Check clicked inventory directly if present
            try {
                if (event.getClickedInventory() != null) {
                    int slot = event.getSlot();
                    if (slot >= 0 && slot < event.getClickedInventory().getSize()) {
                        ItemStack clicked = event.getClickedInventory().getItem(slot);
                        if (clicked != null && !clicked.getType().isAir() && !bridgeService.isFullyEnchanted(clicked)) {
                            ItemStack updated = bridgeService.processItem(clicked);
                            if (updated != null && updated != clicked) {
                                event.getClickedInventory().setItem(slot, updated);
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {}

            checkAndFixPlayerEquipment(player);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!onInventoryClick || !AEHook.isAvailable()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        org.bukkit.Bukkit.getScheduler().runTask(id.seria.mmoae.SeriaMMOItemsAEPlugin.getInstance(), () -> {
            if (!player.isOnline()) return;
            checkAndFixPlayerEquipment(player);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!onInventoryClick || !AEHook.isAvailable()) return;
        if (event.getPlayer() instanceof Player player) {
            checkAndFixPlayerEquipment(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!onInventoryClick || !AEHook.isAvailable()) return;
        if (event.getPlayer() instanceof Player player) {
            checkAndFixPlayerEquipment(player);
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (!onInteract || !AEHook.isAvailable()) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand != null && !mainHand.getType().isAir() && !bridgeService.isFullyEnchanted(mainHand)) {
            ItemStack updated = bridgeService.processItem(mainHand);
            if (updated != null && updated != mainHand) {
                player.getInventory().setItemInMainHand(updated);
            }
        }

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand != null && !offHand.getType().isAir() && !bridgeService.isFullyEnchanted(offHand)) {
            ItemStack updated = bridgeService.processItem(offHand);
            if (updated != null && updated != offHand) {
                player.getInventory().setItemInOffHand(updated);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!onAttack || !AEHook.isAvailable()) return;

        if (event.getDamager() instanceof Player attacker) {
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            if (weapon != null && !weapon.getType().isAir() && !bridgeService.isFullyEnchanted(weapon)) {
                ItemStack updated = bridgeService.processItem(weapon);
                if (updated != null && updated != weapon) {
                    attacker.getInventory().setItemInMainHand(updated);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        if (!onItemHeld || !AEHook.isAvailable()) return;

        ItemStack main = event.getMainHandItem();
        if (main != null && !main.getType().isAir() && !bridgeService.isFullyEnchanted(main)) {
            ItemStack updated = bridgeService.processItem(main);
            if (updated != null && updated != main) {
                event.setMainHandItem(updated);
            }
        }

        ItemStack off = event.getOffHandItem();
        if (off != null && !off.getType().isAir() && !bridgeService.isFullyEnchanted(off)) {
            ItemStack updated = bridgeService.processItem(off);
            if (updated != null && updated != off) {
                event.setOffHandItem(updated);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (!onPickup || !AEHook.isAvailable()) return;

        ItemStack current = event.getItemDrop().getItemStack();
        if (current != null && !current.getType().isAir() && !bridgeService.isFullyEnchanted(current)) {
            ItemStack updated = bridgeService.processItem(current);
            if (updated != null && updated != current) {
                event.getItemDrop().setItemStack(updated);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!onJoin || !AEHook.isAvailable()) return;
        checkAndFixPlayerEquipment(event.getPlayer());
    }

    public void checkAndFixPlayerEquipment(Player player) {
        if (player == null || !player.isOnline()) return;
        PlayerInventory inv = player.getInventory();

        // Check armor
        ItemStack[] armor = inv.getArmorContents();
        boolean armorChanged = false;
        for (int i = 0; i < armor.length; i++) {
            ItemStack it = armor[i];
            if (it != null && !it.getType().isAir() && !bridgeService.isFullyEnchanted(it)) {
                ItemStack updated = bridgeService.processItem(it);
                if (updated != null && updated != it) {
                    armor[i] = updated;
                    armorChanged = true;
                }
            }
        }
        if (armorChanged) {
            inv.setArmorContents(armor);
        }

        // Check main & off hand
        ItemStack main = inv.getItemInMainHand();
        if (main != null && !main.getType().isAir() && !bridgeService.isFullyEnchanted(main)) {
            ItemStack updated = bridgeService.processItem(main);
            if (updated != null && updated != main) {
                inv.setItemInMainHand(updated);
            }
        }

        ItemStack off = inv.getItemInOffHand();
        if (off != null && !off.getType().isAir() && !bridgeService.isFullyEnchanted(off)) {
            ItemStack updated = bridgeService.processItem(off);
            if (updated != null && updated != off) {
                inv.setItemInOffHand(updated);
            }
        }

        // Check full storage inventory (slots 0 to 35)
        ItemStack[] storage = inv.getStorageContents();
        boolean storageChanged = false;
        for (int i = 0; i < storage.length; i++) {
            ItemStack it = storage[i];
            if (it != null && !it.getType().isAir() && !bridgeService.isFullyEnchanted(it)) {
                ItemStack updated = bridgeService.processItem(it);
                if (updated != null && updated != it) {
                    storage[i] = updated;
                    storageChanged = true;
                }
            }
        }
        if (storageChanged) {
            inv.setStorageContents(storage);
        }
    }
}
