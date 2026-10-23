package id.seria.jobs.ui;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import id.seria.jobs.model.JobOrder;
import id.seria.jobs.model.JobProfile;
import id.seria.jobs.model.reward.Reward;
import id.seria.jobs.model.WorkOrder;
import id.seria.jobs.utils.ItemMatcher;
import id.seria.jobs.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu aksi pesanan khusus untuk navigasi satu-klik (terutama pemain Bedrock Edition)
 * yang menyediakan tombol Setor Barang dan Reroll Kontrak secara terpisah.
 */
public class OrderActionMenu extends BaseMenu {

    private final Job job;
    private final int slotNumber;
    private final JobOrder order;

    public OrderActionMenu(@NotNull SeriaJobs plugin,
                           @NotNull Player player,
                           @NotNull Job job,
                           int slotNumber,
                           @Nullable JobOrder order) {
        super(plugin, player, "job_order_action_gui.yml");
        this.job = job;
        this.slotNumber = slotNumber;
        this.order = order;
    }

    public Job getJob() {
        return job;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public JobOrder getOrder() {
        return order;
    }

    @Override
    public void build() {
        int size = menuConfig != null ? menuConfig.getInt("size", 27) : 27;
        String rawTitle = menuConfig != null ? menuConfig.getString("title", "<sc:menu-prefix>Kontrak ⏵ {order_display_name}") : "<sc:menu-prefix>Kontrak ⏵ {order_display_name}";
        rawTitle = replacePlaceholders(rawTitle);

        createInventory(size, rawTitle);
        applyFillers();
        applyStaticItems();
        renderOrderPreview();
    }

    private void renderOrderPreview() {
        if (order == null || menuConfig == null) return;
        ConfigurationSection itemsSec = menuConfig.getConfigurationSection("items");
        if (itemsSec == null) return;

        ConfigurationSection previewSec = null;
        for (String key : itemsSec.getKeys(false)) {
            ConfigurationSection sec = itemsSec.getConfigurationSection(key);
            if (sec != null && (sec.contains("taints.order-preview") || sec.getBoolean("taints.order-preview", false) || key.equals("order_preview"))) {
                previewSec = sec;
                break;
            }
        }
        if (previewSec == null) return;

        int targetSlot = previewSec.getInt("slot", 11);
        if (targetSlot < 0 || targetSlot >= inventory.getSize()) return;

        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
        String shiftId = plugin.getOrderPoolManager().getCurrentShiftId();
        boolean isCompleted = profile != null && profile.isOrderCompleted(shiftId, job.getId(), order.getId());

        ItemStack cardItem = null;
        if (!isCompleted) {
            String src = order.getSource().toUpperCase();
            if (src.equals("MMOITEMS") && plugin.getHookManager().isMMOItemsLoaded()) {
                cardItem = plugin.getHookManager().getMMOItemsHook().getItem(order.getMmoItemType(), order.getMmoItemId());
            } else if ((src.equals("MYTHICMOBS") || src.equals("MYTHIC")) && plugin.getHookManager().isMythicMobsLoaded()) {
                cardItem = plugin.getHookManager().getMythicMobsHook().generateItem(order.getCustomItemId(), 1);
            } else if ((src.equals("CUSTOMFISHING") || src.equals("FISHING")) && plugin.getHookManager().isCustomFishingLoaded()) {
                cardItem = plugin.getHookManager().getCustomFishingHook().generateItem(order.getCustomItemId(), 1);
            } else if ((src.equals("SERIAMAGICBUNDLE") || src.equals("MAGICBUNDLE")) && plugin.getHookManager().isSeriaMagicBundleLoaded()) {
                cardItem = plugin.getHookManager().getSeriaMagicBundleHook().generateItem(order.getCustomItemId(), 1);
            } else if ((src.equals("SERIACRAFTING") || src.equals("CRAFTING")) && plugin.getHookManager().isSeriaCraftingLoaded()) {
                cardItem = plugin.getHookManager().getSeriaCraftingHook().generateItem(order.getCustomItemId(), 1);
            }
        }

        if (cardItem == null) {
            Material cardMat = null;
            if (previewSec.contains("material")) {
                cardMat = Material.matchMaterial(previewSec.getString("material", ""));
            }
            if (cardMat == null) {
                cardMat = order.getMaterial() != null ? order.getMaterial() : Material.CHEST;
            }
            cardItem = new ItemStack(cardMat);
        } else {
            cardItem = cardItem.clone();
        }

        ItemMeta meta = cardItem.getItemMeta();
        if (meta != null) {
            String nameFormat = previewSec.getString("name", order.getDisplayName());
            nameFormat = replacePlaceholders(nameFormat);
            meta.displayName(TextUtil.parseForPlayer(player, nameFormat));

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

            if (previewSec.contains("lore")) {
                List<String> rawLore = previewSec.getStringList("lore");
                List<Component> finalLore = new ArrayList<>();
                for (String line : rawLore) {
                    if (line.contains("{rewards_list}")) {
                        double rewardMult = plugin.getReputationBridgeService() != null
                                ? plugin.getReputationBridgeService().getRewardMultiplier(player)
                                : 1.0;
                        for (Reward r : order.getRewards()) {
                            String disp = r.getFormattedDisplay(rewardMult);
                            if (!disp.startsWith(" ") && !disp.startsWith("<lore-color>▪") && !disp.startsWith("▪")) {
                                disp = "  " + disp;
                            }
                            finalLore.add(TextUtil.parseForPlayer(player, disp));
                        }
                    } else {
                        finalLore.add(TextUtil.parseForPlayer(player, replacePlaceholders(line)));
                    }
                }
                meta.lore(finalLore);
            }
            cardItem.setItemMeta(meta);
        }

        inventory.setItem(targetSlot, cardItem);
    }

    @Override
    protected void handleActions(List<String> actions) {
        if (actions == null || actions.isEmpty()) return;
        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
        String shiftId = plugin.getOrderPoolManager().getCurrentShiftId();

        for (String act : actions) {
            if (act == null || act.isBlank()) continue;
            String trimmed = act.trim();

            if (trimmed.equalsIgnoreCase("[TURN_IN]") || trimmed.equalsIgnoreCase("[DEPOSIT]")) {
                if (order != null) {
                    OrderDepositMenu odm = new OrderDepositMenu(plugin, player, job);
                    odm.handleDepositOrder(order, profile, shiftId, slotNumber);
                    odm.open();
                }
                return;
            } else if (trimmed.equalsIgnoreCase("[jobs_reroll]") || trimmed.equalsIgnoreCase("[reroll]")) {
                if (slotNumber == 100 || plugin.getJobsManager().isSlotNonRerollable(player, job, profile, slotNumber) || (order != null && !order.isRerollable())) {
                    player.sendMessage(plugin.getConfigManager().getEventDenyRerollMessage());
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                if (plugin.getJobsManager().executeReroll(player, job.getId(), slotNumber)) {
                    new OrderDepositMenu(plugin, player, job).open();
                }
                return;
            } else if (trimmed.equalsIgnoreCase("[BACK]") || trimmed.equalsIgnoreCase("[RETURN]")) {
                new OrderDepositMenu(plugin, player, job).open();
                return;
            } else if (trimmed.equalsIgnoreCase("[REFRESH]")) {
                refresh();
                return;
            }
        }

        super.handleActions(actions);
    }

    @Override
    protected String replacePlaceholders(String text) {
        if (text == null) return "";
        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
        String shiftId = plugin.getOrderPoolManager().getCurrentShiftId();

        int jobLevel = profile != null ? profile.getJobLevel(job.getId()) : 1;
        double orderMult = plugin.getReputationBridgeService() != null
                ? plugin.getReputationBridgeService().getOrderMultiplier(player)
                : 1.0;
        double rewardMult = plugin.getReputationBridgeService() != null
                ? plugin.getReputationBridgeService().getRewardMultiplier(player)
                : 1.0;

        WorkOrder wo = (profile != null && slotNumber > 0) ? profile.getActiveOrder(slotNumber) : null;
        boolean isCompleted = profile != null && order != null && profile.isOrderCompleted(shiftId, job.getId(), order.getId());
        int progress = (profile != null && order != null) ? profile.getOrderProgress(shiftId, job.getId(), order.getId()) : 0;
        int required = (wo != null && wo.getTemplate() != null)
                ? wo.getTemplate().getTargetAmount()
                : (order != null ? order.getRequiredAmount(jobLevel, orderMult) : 1);
        int percentage = Math.min(100, (int) Math.round(((double) progress / required) * 100));

        boolean isEvent = slotNumber == 100 || (order != null && order.isEventOrder());
        boolean isRerollable = !isEvent && (order == null || order.isRerollable());

        String orderName = order != null ? order.getDisplayName() : "Pesanan";
        String itemName = order != null ? ItemMatcher.getItemDisplayName(order) : "";

        String replaced = text.replace("{order_display_name}", orderName)
                .replace("{order_id}", order != null ? order.getId() : "")
                .replace("{job_name}", job.getDisplayName())
                .replace("{job_id}", job.getId())
                .replace("{job_level}", String.valueOf(jobLevel))
                .replace("{progress}", String.valueOf(progress))
                .replace("{required_amount}", String.valueOf(required))
                .replace("{required}", String.valueOf(required))
                .replace("{percentage}", String.valueOf(percentage))
                .replace("{progress_bar}", createProgressBar(progress, required))
                .replace("{item_name}", itemName)
                .replace("{is_completed}", String.valueOf(isCompleted))
                .replace("{is_rerollable}", String.valueOf(isRerollable))
                .replace("{is_event}", String.valueOf(isEvent));

        if (!isRerollable && replaced.contains("%custom_reroll_status%")) {
            replaced = replaced.replace("%custom_reroll_status%", isEvent
                    ? "<sc:info-icon> <gray>Pesanan Khusus Event <dark_gray>(Non-Rerollable)"
                    : "<sc:info-icon> <gray>Pesanan Khusus <dark_gray>(Non-Rerollable)");
        }

        return super.replacePlaceholders(replaced);
    }

    private String createProgressBar(int progress, int required) {
        int totalBars = 10;
        int filled = (int) Math.min(totalBars, Math.round(((double) progress / Math.max(1, required)) * totalBars));
        StringBuilder sb = new StringBuilder("<green>");
        for (int i = 0; i < filled; i++) {
            sb.append("|");
        }
        sb.append("<gray>");
        for (int i = filled; i < totalBars; i++) {
            sb.append("|");
        }
        return sb.toString();
    }
}
