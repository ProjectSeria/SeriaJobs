package id.seria.mmoae.listener;

import id.seria.mmoae.hook.AEHook;
import id.seria.mmoae.service.AEBridgeService;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeEvent;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeFinishEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MMOItemReforgeListener implements Listener {

    private final AEBridgeService bridgeService;
    private final boolean preservePlayerEnchants;
    private final boolean enabled;

    // Temporary cache of old player-enchanted AE enchants keyed by thread or item hash
    private final Map<Integer, Map<String, Integer>> previousEnchantsCache = new ConcurrentHashMap<>();

    public MMOItemReforgeListener(AEBridgeService bridgeService, boolean enabled, boolean preservePlayerEnchants) {
        this.bridgeService = bridgeService;
        this.enabled = enabled;
        this.preservePlayerEnchants = preservePlayerEnchants;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onReforgeStart(MMOItemReforgeEvent event) {
        if (!enabled || !preservePlayerEnchants || !AEHook.isAvailable()) return;

        try {
            // Check if reforger has target item
            if (event.getReforger() != null) {
                // If there's an original item, store its existing AE enchants
                ItemStack original = null;
                try {
                    java.lang.reflect.Method getTargetMethod = event.getReforger().getClass().getMethod("getTargetItem");
                    original = (ItemStack) getTargetMethod.invoke(event.getReforger());
                } catch (Throwable ignored) {}

                if (original != null) {
                    Map<String, Integer> oldEnchants = AEHook.getEnchantments(original);
                    if (!oldEnchants.isEmpty()) {
                        previousEnchantsCache.put(System.identityHashCode(event.getReforger()), oldEnchants);
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onReforgeFinish(MMOItemReforgeFinishEvent event) {
        if (!enabled || !AEHook.isAvailable()) return;

        try {
            ItemStack finished = event.getFinishedItem();
            if (finished == null) return;

            // 1. Apply baseline template AE enchants
            ItemStack updated = bridgeService.processItem(finished);

            // 2. Re-apply any extra player-installed enchants from before the reforge
            if (preservePlayerEnchants && event.getReforger() != null) {
                int key = System.identityHashCode(event.getReforger());
                Map<String, Integer> cached = previousEnchantsCache.remove(key);
                if (cached != null && !cached.isEmpty()) {
                    for (Map.Entry<String, Integer> entry : cached.entrySet()) {
                        String enchantName = entry.getKey();
                        int lvl = entry.getValue();
                        if (updated != null && (!AEHook.hasEnchant(updated, enchantName) || AEHook.getEnchantments(updated).getOrDefault(enchantName.toLowerCase(), 0) < lvl)) {
                            ItemStack reApplied = AEHook.applyEnchant(updated, enchantName, lvl);
                            if (reApplied != null) {
                                updated = reApplied;
                            }
                        }
                    }
                }
            }

            if (updated != null && updated != finished) {
                event.setFinishedItem(updated);
            }
        } catch (Throwable t) {
            // Fail-safe to avoid breaking the reforge pipeline
            t.printStackTrace();
        }
    }
}
