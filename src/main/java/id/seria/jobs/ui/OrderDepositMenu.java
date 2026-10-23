package id.seria.jobs.ui;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.api.OrderCompleteEvent;
import id.seria.jobs.model.Job;
import id.seria.jobs.model.JobOrder;
import id.seria.jobs.model.JobProfile;
import id.seria.jobs.model.WorkOrder;
import id.seria.jobs.model.reward.Reward;
import id.seria.jobs.utils.ItemMatcher;
import id.seria.jobs.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class OrderDepositMenu extends BaseMenu {

    private final Job job;

    public Job getJob() {
        return job;
    }

    public OrderDepositMenu(SeriaJobs plugin, Player player, Job job) {
        super(plugin, player, "job_orders_gui.yml");
        this.job = job;
    }

    @Override
    protected String replacePlaceholders(String text) {
        if (text == null) return null;
        if (job == null) return text;
        int level = 1;
        int xp = 0;
        int reqXp = 100;
        int ordersCompleted = 0;
        if (player != null && plugin.getPlayerDataManager() != null) {
            JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
            if (profile != null) {
                level = profile.getJobLevel(job.getId());
                xp = profile.getJobXp(job.getId());
                reqXp = job.getRequiredXp(level);
                ordersCompleted = profile.getOrdersCompleted();
            }
        }
        String ordersCompletedStr = String.valueOf(ordersCompleted);
        text = text.replace("{job_name}", job.getDisplayName())
                .replace("%job_name%", job.getDisplayName())
                .replace("{job}", job.getDisplayName())
                .replace("%job%", job.getDisplayName())
                .replace("{job_title}", job.getDisplayName())
                .replace("%job_title%", job.getDisplayName())
                .replace("{job_id}", job.getId())
                .replace("%job_id%", job.getId())
                .replace("<job_id>", job.getId())
                .replace("{level}", String.valueOf(level))
                .replace("%level%", String.valueOf(level))
                .replace("{job_level}", String.valueOf(level))
                .replace("%job_level%", String.valueOf(level))
                .replace("{xp}", String.valueOf(xp))
                .replace("%xp%", String.valueOf(xp))
                .replace("{job_xp}", String.valueOf(xp))
                .replace("%job_xp%", String.valueOf(xp))
                .replace("{required_xp}", String.valueOf(reqXp))
                .replace("%required_xp%", String.valueOf(reqXp))
                .replace("{req_xp}", String.valueOf(reqXp))
                .replace("%req_xp%", String.valueOf(reqXp))
                .replace("{seriajobs_order_completed}%", ordersCompletedStr)
                .replace("{seriajobs_order_completed}", ordersCompletedStr)
                .replace("%seriajobs_order_completed%", ordersCompletedStr)
                .replace("{seriajobs_orders_completed}", ordersCompletedStr)
                .replace("%seriajobs_orders_completed%", ordersCompletedStr)
                .replace("{order_completed}", ordersCompletedStr)
                .replace("%order_completed%", ordersCompletedStr)
                .replace("{orders_completed}", ordersCompletedStr)
                .replace("%orders_completed%", ordersCompletedStr);
        return super.replacePlaceholders(text);
    }

    public List<Integer> getOrderSlots() {
        if (menuConfig != null) {
            if (menuConfig.isList("layout.order-slots.slots")) {
                return menuConfig.getIntegerList("layout.order-slots.slots");
            }
            if (menuConfig.isList("layout.orders.slots")) {
                return menuConfig.getIntegerList("layout.orders.slots");
            }
            if (menuConfig.isList("order-slots")) {
                return menuConfig.getIntegerList("order-slots");
            }
        }
        return List.of(20, 22, 24, 30, 32);
    }

    public int getEventInventorySlot() {
        if (menuConfig != null) {
            if (menuConfig.contains("layout.event-slot.slot")) {
                return menuConfig.getInt("layout.event-slot.slot");
            }
            if (menuConfig.isList("layout.event-slot.slots")) {
                List<Integer> s = menuConfig.getIntegerList("layout.event-slot.slots");
                if (!s.isEmpty()) return s.get(0);
            }
            if (menuConfig.contains("layout.event-slot")) {
                return menuConfig.getInt("layout.event-slot");
            }
            if (menuConfig.contains("events.inventory-slot")) {
                return menuConfig.getInt("events.inventory-slot");
            }
            if (menuConfig.contains("events.event-slot")) {
                return menuConfig.getInt("events.event-slot");
            }
        }
        return plugin.getConfigManager().getEventSlot();
    }

    public org.bukkit.configuration.ConfigurationSection getOrderCardSection(boolean isCompleted) {
        return getOrderCardSection(isCompleted, false);
    }

    public org.bukkit.configuration.ConfigurationSection getOrderCardSection(boolean isCompleted, boolean isEvent) {
        if (menuConfig == null) return null;

        // Dynamic candidate evaluation from items: with priority and view-requirement
        org.bukkit.configuration.ConfigurationSection itemsSec = menuConfig.getConfigurationSection("items");
        if (itemsSec != null) {
            List<org.bukkit.configuration.ConfigurationSection> candidates = new ArrayList<>();
            for (String key : itemsSec.getKeys(false)) {
                if (!itemsSec.isConfigurationSection(key)) continue;
                org.bukkit.configuration.ConfigurationSection sec = itemsSec.getConfigurationSection(key);
                if (sec == null) continue;

                boolean matchesTaint;
                if (isEvent) {
                    matchesTaint = sec.contains("taints.event-order") || sec.contains("taints.event-item")
                            || sec.getBoolean("taints.event", false) || key.startsWith("event_");
                } else {
                    matchesTaint = sec.contains("taints.orders-item") || sec.contains("taints.order-item")
                            || sec.getBoolean("taints.order", false) || key.startsWith("incomplete") || key.startsWith("completed");
                }
                if (!matchesTaint) continue;

                boolean candidateCompleted = key.contains("completed") || sec.getBoolean("completed", false);
                if (candidateCompleted != isCompleted) continue;

                candidates.add(sec);
            }

            if (!candidates.isEmpty()) {
                candidates.sort((a, b) -> Integer.compare(b.getInt("priority", 1), a.getInt("priority", 1)));
                for (org.bukkit.configuration.ConfigurationSection candidate : candidates) {
                    if (checkCandidateViewRequirement(candidate)) {
                        return candidate;
                    }
                }
            }
        }

        if (isEvent) {
            if (isCompleted) {
                if (menuConfig.isConfigurationSection("items.event_completed")) {
                    return menuConfig.getConfigurationSection("items.event_completed");
                }
            } else {
                if (menuConfig.isConfigurationSection("items.event_order")) {
                    return menuConfig.getConfigurationSection("items.event_order");
                }
                if (menuConfig.isConfigurationSection("items.event_incomplete")) {
                    return menuConfig.getConfigurationSection("items.event_incomplete");
                }
            }
        }
        if (isCompleted) {
            if (menuConfig.isConfigurationSection("items.completed")) {
                return menuConfig.getConfigurationSection("items.completed");
            }
            if (menuConfig.isConfigurationSection("items.order_completed")) {
                return menuConfig.getConfigurationSection("items.order_completed");
            }
            return menuConfig.getConfigurationSection("order-card.completed");
        } else {
            if (menuConfig.isConfigurationSection("items.incomplete")) {
                return menuConfig.getConfigurationSection("items.incomplete");
            }
            if (menuConfig.isConfigurationSection("items.order_incomplete")) {
                return menuConfig.getConfigurationSection("items.order_incomplete");
            }
            return menuConfig.getConfigurationSection("order-card.incomplete");
        }
    }

    @Override
    public void build() {
        createInventory(54, "<gradient:#2c3e50:#3498db>Bursa Kerja: " + job.getDisplayName() + "</gradient>");
        applyFillers();
        applyStaticItems();

        List<Integer> slots = getOrderSlots();

        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
        String shiftId = plugin.getOrderPoolManager().getCurrentShiftId();

        for (int i = 0; i < slots.size(); i++) {
            renderOrderCard(slots.get(i), i + 1, profile, shiftId, false);
        }

        if (plugin.getConfigManager().isEventActive()) {
            int eventInvSlot = getEventInventorySlot();
            if (eventInvSlot >= 0 && eventInvSlot < inventory.getSize()) {
                renderOrderCard(eventInvSlot, 100, profile, shiftId, true);
            }
        }
    }

    @Override
    protected boolean refreshCustomSlot(int slot) {
        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
        String shiftId = plugin.getOrderPoolManager().getCurrentShiftId();

        if (plugin.getConfigManager().isEventActive() && slot == getEventInventorySlot()) {
            renderOrderCard(slot, 100, profile, shiftId, true);
            return true;
        }

        List<Integer> slots = getOrderSlots();
        int idx = slots.indexOf(slot);
        if (idx != -1) {
            renderOrderCard(slot, idx + 1, profile, shiftId, false);
            return true;
        }
        return false;
    }

    private void renderOrderCard(int slot, int slotNumber, JobProfile profile, String shiftId) {
        renderOrderCard(slot, slotNumber, profile, shiftId, slotNumber == 100);
    }

    private void renderOrderCard(int slot, int slotNumber, JobProfile profile, String shiftId, boolean isEventCard) {
        if (job == null) return;

        JobOrder order = null;
        id.seria.jobs.model.WorkOrder wo = null;

        if (isEventCard || slotNumber == 100) {
            if (profile != null && profile.getActiveOrder(100) != null) {
                wo = profile.getActiveOrder(100);
                order = job.getOrder(wo.getTemplate().getTemplateId());
            }
            if (order == null) {
                order = plugin.getOrderPoolManager().getActiveEventOrder(job.getId());
            }
        } else {
            List<JobOrder> activeOrders = plugin.getOrderPoolManager().getActiveOrders(job.getId());
            if (profile != null && profile.getActiveOrder(slotNumber) != null) {
                wo = profile.getActiveOrder(slotNumber);
                order = job.getOrder(wo.getTemplate().getTemplateId());
            }
            if (order == null && (slotNumber - 1) < activeOrders.size()) {
                order = activeOrders.get(slotNumber - 1);
            }
        }

        if (order == null) return;

        int jobLevel = profile != null ? profile.getJobLevel(job.getId()) : 1;
        double resourceMultiplier = plugin.getConfigManager().getResourceMultiplier(jobLevel);
        String multFormatted = String.format(java.util.Locale.US, "%.2fx", resourceMultiplier);

        double rewardMult = plugin.getReputationBridgeService() != null
                ? plugin.getReputationBridgeService().getRewardMultiplier(player)
                : 1.0;
        double orderMult = plugin.getReputationBridgeService() != null
                ? plugin.getReputationBridgeService().getOrderMultiplier(player)
                : 1.0;
        id.seria.jobs.model.ReputationTier repTier = plugin.getReputationBridgeService() != null
                ? plugin.getReputationBridgeService().getPlayerTier(player)
                : null;
        String repTierName = repTier != null ? repTier.getDisplayName() : "Default";
        String rewardMultFormatted = String.format(java.util.Locale.US, "%.2fx", rewardMult);
        String orderMultFormatted = String.format(java.util.Locale.US, "%.2fx", orderMult);

        boolean isCompleted = profile != null && profile.isOrderCompleted(shiftId, job.getId(), order.getId());
        int progress = profile != null ? profile.getOrderProgress(shiftId, job.getId(), order.getId()) : 0;
        int required = (wo != null && wo.getTemplate() != null)
                ? wo.getTemplate().getTargetAmount()
                : order.getRequiredAmount(jobLevel, orderMult);
        int percentage = Math.min(100, (int) Math.round(((double) progress / required) * 100));

        int currentRep = (wo != null && wo.getGeneratedReputationAmount() > 0)
                ? wo.getGeneratedReputationAmount()
                : (order.getMinReputationAmount() > 0 ? order.getMinReputationAmount() : 0);
        String repStr = String.valueOf(currentRep);
        String repRange = (order.getMinReputationAmount() > 0 && order.getMaxReputationAmount() > order.getMinReputationAmount())
                ? (order.getMinReputationAmount() + "-" + order.getMaxReputationAmount())
                : repStr;
        String activeRepDisplay = (wo != null && wo.getGeneratedReputationAmount() > 0) ? repStr : repRange;

        boolean isEventOrder = isEventCard || order.isEventOrder() || (wo != null && wo.isEventOrder());
        boolean isRerollable = !isEventOrder && order.isRerollable() && (wo == null || wo.isRerollable());
        String eventName = order.getEvent() != null ? order.getEvent() : (wo != null ? wo.getEvent() : "");

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
        org.bukkit.configuration.ConfigurationSection cardSec = getOrderCardSection(isCompleted, isEventOrder);

        if (cardItem == null) {
            Material cardMat = null;
            if (cardSec != null && cardSec.contains("material")) {
                cardMat = Material.matchMaterial(cardSec.getString("material", ""));
            }
            if (cardMat == null) {
                if (isCompleted) {
                    cardMat = Material.EMERALD_BLOCK;
                } else {
                    cardMat = order.getMaterial() != null ? order.getMaterial() : Material.CHEST;
                }
            }
            cardItem = new ItemStack(cardMat);
        } else {
            cardItem = cardItem.clone();
            if (isCompleted && cardSec != null && cardSec.contains("material")) {
                Material secMat = Material.matchMaterial(cardSec.getString("material", ""));
                if (secMat != null) {
                    cardItem = new ItemStack(secMat);
                }
            }
        }

        ItemMeta meta = cardItem.getItemMeta();
        if (meta != null) {
            String nameFormat = cardSec != null
                    ? cardSec.getString("name", order.getDisplayName())
                    : order.getDisplayName();

            nameFormat = nameFormat.replace("{order_display_name}", order.getDisplayName())
                    .replace("{order_id}", order.getId())
                    .replace("{job_name}", job.getDisplayName())
                    .replace("{job_id}", job.getId())
                    .replace("{required_amount}", String.valueOf(required))
                    .replace("{required}", String.valueOf(required))
                    .replace("{item_name}", ItemMatcher.getItemDisplayName(order))
                    .replace("{progress}", String.valueOf(progress))
                    .replace("{percentage}", String.valueOf(percentage))
                    .replace("{multiplier}", multFormatted)
                    .replace("{resource_multiplier}", multFormatted)
                    .replace("{reward_multiplier}", rewardMultFormatted)
                    .replace("{order_multiplier}", orderMultFormatted)
                    .replace("{reputation_tier}", repTierName)
                    .replace("{tier_name}", repTierName)
                    .replace("{reputation}", activeRepDisplay)
                    .replace("{order_reputation}", activeRepDisplay)
                    .replace("{min_reputation}", String.valueOf(order.getMinReputationAmount()))
                    .replace("{max_reputation}", String.valueOf(order.getMaxReputationAmount()))
                    .replace("{reputation_range}", repRange)
                    .replace("{is_event}", String.valueOf(isEventOrder))
                    .replace("{is_rerollable}", String.valueOf(isRerollable))
                    .replace("{event_name}", eventName)
                    .replace("{event}", eventName);

            meta.displayName(TextUtil.parseForPlayer(player, nameFormat));

            List<String> rawLore = cardSec != null
                    ? cardSec.getStringList("lore")
                    : new ArrayList<>();

            List<Component> finalLore = new ArrayList<>();
            for (String line : rawLore) {
                if (line.contains("{order_lore}")) {
                    for (String oLine : order.getLore()) {
                        String parsedOLine = oLine.replace("{progress}", String.valueOf(progress))
                                .replace("{required}", String.valueOf(required))
                                .replace("{required_amount}", String.valueOf(required))
                                .replace("{percentage}", String.valueOf(percentage))
                                .replace("{item_name}", ItemMatcher.getItemDisplayName(order))
                                .replace("{order_display_name}", order.getDisplayName())
                                .replace("{order_id}", order.getId())
                                .replace("{job_name}", job.getDisplayName())
                                .replace("{job_id}", job.getId())
                                .replace("{multiplier}", multFormatted)
                                .replace("{resource_multiplier}", multFormatted)
                                .replace("{reward_multiplier}", rewardMultFormatted)
                                .replace("{order_multiplier}", orderMultFormatted)
                                .replace("{reputation_tier}", repTierName)
                                .replace("{tier_name}", repTierName)
                                .replace("{reputation}", activeRepDisplay)
                                .replace("{order_reputation}", activeRepDisplay)
                                .replace("{min_reputation}", String.valueOf(order.getMinReputationAmount()))
                                .replace("{max_reputation}", String.valueOf(order.getMaxReputationAmount()))
                                .replace("{reputation_range}", repRange)
                                .replace("{is_event}", String.valueOf(isEventOrder))
                                .replace("{is_rerollable}", String.valueOf(isRerollable))
                                .replace("{event_name}", eventName)
                                .replace("{event}", eventName);
                        finalLore.add(TextUtil.parseForPlayer(player, parsedOLine));
                    }
                } else if (line.contains("{rewards_list}")) {
                    if (order.hasCustomRewardsDisplay()) {
                        for (String rLine : order.getCustomRewardsDisplay()) {
                            String parsedRLine = rLine.replace("{reputation}", activeRepDisplay)
                                    .replace("{order_reputation}", activeRepDisplay)
                                    .replace("{min_reputation}", String.valueOf(order.getMinReputationAmount()))
                                    .replace("{max_reputation}", String.valueOf(order.getMaxReputationAmount()))
                                    .replace("{reputation_range}", repRange)
                                    .replace("{multiplier}", multFormatted)
                                    .replace("{resource_multiplier}", multFormatted)
                                    .replace("{reward_multiplier}", rewardMultFormatted)
                                    .replace("{order_multiplier}", orderMultFormatted)
                                    .replace("{reputation_tier}", repTierName)
                                    .replace("{tier_name}", repTierName)
                                    .replace("{is_event}", String.valueOf(isEventOrder))
                                    .replace("{is_rerollable}", String.valueOf(isRerollable))
                                    .replace("{event_name}", eventName)
                                    .replace("{event}", eventName);
                            finalLore.add(TextUtil.parseForPlayer(player, parsedRLine));
                        }
                    } else {
                        for (Reward r : order.getRewards()) {
                            String disp = r.getFormattedDisplay(rewardMult);
                            if (!disp.startsWith(" ") && !disp.startsWith("<lore-color>▪") && !disp.startsWith("▪")) {
                                disp = "  " + disp;
                            }
                            finalLore.add(TextUtil.parseForPlayer(player, disp));
                        }
                        if (currentRep > 0 || order.getMaxReputationAmount() > 0) {
                            finalLore.add(TextUtil.parseForPlayer(player, "  <lore-color>▪ <aqua>+" + activeRepDisplay + " Reputasi</aqua>"));
                        }
                    }
                } else if (line.contains("{progress_bar}")) {
                    finalLore.add(TextUtil.parseForPlayer(player, createProgressBar(progress, required)));
                } else {
                    String replaced = line;
                    if (!isRerollable && replaced.contains("%custom_reroll_status%")) {
                        replaced = replaced.replace("%custom_reroll_status%", isEventOrder
                                ? "<sc:info-icon> <gray>Pesanan Khusus Event <dark_gray>(Non-Rerollable)"
                                : "<sc:info-icon> <gray>Pesanan Khusus <dark_gray>(Non-Rerollable)");
                    }
                    replaced = replaced.replace("{order_display_name}", order.getDisplayName())
                            .replace("{order_id}", order.getId())
                            .replace("{job_name}", job.getDisplayName())
                            .replace("{job_id}", job.getId())
                            .replace("{required_amount}", String.valueOf(required))
                            .replace("{required}", String.valueOf(required))
                            .replace("{item_name}", ItemMatcher.getItemDisplayName(order))
                            .replace("{progress}", String.valueOf(progress))
                            .replace("{percentage}", String.valueOf(percentage))
                            .replace("{multiplier}", multFormatted)
                            .replace("{resource_multiplier}", multFormatted)
                            .replace("{reward_multiplier}", rewardMultFormatted)
                            .replace("{order_multiplier}", orderMultFormatted)
                            .replace("{reputation_tier}", repTierName)
                            .replace("{tier_name}", repTierName)
                            .replace("{reputation}", activeRepDisplay)
                            .replace("{order_reputation}", activeRepDisplay)
                            .replace("{min_reputation}", String.valueOf(order.getMinReputationAmount()))
                            .replace("{max_reputation}", String.valueOf(order.getMaxReputationAmount()))
                            .replace("{reputation_range}", repRange)
                            .replace("{is_event}", String.valueOf(isEventOrder))
                            .replace("{is_rerollable}", String.valueOf(isRerollable))
                            .replace("{event_name}", eventName)
                            .replace("{event}", eventName);
                    finalLore.add(TextUtil.parseForPlayer(player, replaced));
                }
            }
            meta.lore(finalLore);
            cardItem.setItemMeta(meta);
        }

        inventory.setItem(slot, cardItem);
        bindOrderCardActions(slot, order, profile, shiftId, slotNumber, isCompleted);
    }

    @Override
    protected void handleActions(List<String> actions) {
        if (actions == null) return;
        for (String act : actions) {
            String trimmed = act.trim();
            if (trimmed.equalsIgnoreCase("[DEPOSIT_ALL]")) {
                handleDepositAll();
            } else if (trimmed.equalsIgnoreCase("[OPEN_PROGRESSION]")) {
                new JobProgressionMenu(plugin, player, job).open();
                return;
            } else if (trimmed.equalsIgnoreCase("[BACK]")) {
                new MainJobsMenu(plugin, player).open();
                return;
            } else if (trimmed.equalsIgnoreCase("[RESIGN]") || trimmed.equalsIgnoreCase("[LEAVE_JOB]")) {
                handleLeaveJob();
                return;
            }
        }
        super.handleActions(actions);
    }

    private void handleLeaveJob() {
        if (!plugin.getConfigManager().isLeaveJobEnabled()) {
            return;
        }

        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
        if (profile == null) return;

        if (profile.isLeaveOnCooldown(job.getId())) {
            long remainingMillis = profile.getLeaveCooldown(job.getId()) - System.currentTimeMillis();
            long hours = remainingMillis / (1000 * 60 * 60);
            long minutes = (remainingMillis % (1000 * 60 * 60)) / (1000 * 60);
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "jobs.leave-cooldown",
                    "{time}", hours + " jam " + minutes + " menit"));
            return;
        }

        id.seria.jobs.api.JobLeaveEvent event = new id.seria.jobs.api.JobLeaveEvent(player, job);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        profile.removeActiveJob(job.getId());
        long cooldownHours = plugin.getConfigManager().getLeaveCooldownHours();
        profile.setLeaveCooldown(job.getId(), System.currentTimeMillis() + (cooldownHours * 3600 * 1000L));

        if (plugin.getConfigManager().isResetProgressOnLeave()) {
            profile.setJobLevel(job.getId(), 1);
            profile.setJobXp(job.getId(), 0);
        }

        plugin.getPlayerDataManager().saveProfileAsync(profile);
        player.sendMessage(plugin.getLanguageManager().getMessage(player, "jobs.leave-success", "{job}", job.getDisplayName()));
        player.closeInventory();
    }

    private void bindOrderCardActions(int slot, JobOrder order, JobProfile profile, String shiftId, int slotNumber, boolean isCompleted) {
        org.bukkit.configuration.ConfigurationSection orderCardSec = menuConfig != null ? menuConfig.getConfigurationSection("order-card") : null;
        org.bukkit.configuration.ConfigurationSection cardSec = getOrderCardSection(isCompleted);
        org.bukkit.configuration.ConfigurationSection globalSec = plugin.getConfig().getConfigurationSection("order-clicks");

        List<String> leftActions = getActionList(cardSec, orderCardSec, globalSec, "left-click");
        List<String> rightActions = getActionList(cardSec, orderCardSec, globalSec, "right-click");
        List<String> shiftRightActions = getActionList(cardSec, orderCardSec, globalSec, "shift-right-click");
        List<String> shiftLeftActions = getActionList(cardSec, orderCardSec, globalSec, "shift-left-click");
        List<String> middleActions = getActionList(cardSec, orderCardSec, globalSec, "middle-click");
        List<String> generalActions = getActionList(cardSec, orderCardSec, globalSec, "click-actions");
        if (generalActions.isEmpty()) generalActions = getActionList(cardSec, orderCardSec, globalSec, "actions");

        final List<String> fLeftActions = (!isCompleted && leftActions.isEmpty() && generalActions.isEmpty()) ? List.of("[TURN_IN]") : leftActions;
        final List<String> fShiftRightActions = shiftRightActions;
        final List<String> fRightActions = rightActions;
        final List<String> fShiftLeftActions = shiftLeftActions;
        final List<String> fMiddleActions = middleActions;
        final List<String> fGeneralActions = generalActions;

        org.bukkit.configuration.ConfigurationSection generalReq = getSection(cardSec, orderCardSec, "click-requirement", "click-requirements", "requirement", "requirements");
        org.bukkit.configuration.ConfigurationSection leftReq = getSection(cardSec, orderCardSec, "left-click-requirement", "left-click-requirements");
        org.bukkit.configuration.ConfigurationSection rightReq = getSection(cardSec, orderCardSec, "right-click-requirement", "right-click-requirements");
        org.bukkit.configuration.ConfigurationSection shiftLeftReq = getSection(cardSec, orderCardSec, "shift-left-click-requirement", "shift-left-click-requirements");
        org.bukkit.configuration.ConfigurationSection shiftRightReq = getSection(cardSec, orderCardSec, "shift-right-click-requirement", "shift-right-click-requirements");
        org.bukkit.configuration.ConfigurationSection middleReq = getSection(cardSec, orderCardSec, "middle-click-requirement", "middle-click-requirements");

        if (!fGeneralActions.isEmpty()) {
            clickActions.put(slot, e -> {
                if (checkClickRequirement(generalReq)) {
                    executeOrderActions(fGeneralActions, order, profile, shiftId, slotNumber, slot);
                }
            });
        }
        if (!fLeftActions.isEmpty()) {
            leftClickActions.put(slot, e -> {
                if (checkClickRequirement(leftReq != null ? leftReq : generalReq)) {
                    executeOrderActions(fLeftActions, order, profile, shiftId, slotNumber, slot);
                }
            });
        }
        if (!fRightActions.isEmpty()) {
            rightClickActions.put(slot, e -> {
                if (checkClickRequirement(rightReq != null ? rightReq : generalReq)) {
                    executeOrderActions(fRightActions, order, profile, shiftId, slotNumber, slot);
                }
            });
        }
        if (!fShiftLeftActions.isEmpty()) {
            shiftLeftClickActions.put(slot, e -> {
                if (checkClickRequirement(shiftLeftReq != null ? shiftLeftReq : generalReq)) {
                    executeOrderActions(fShiftLeftActions, order, profile, shiftId, slotNumber, slot);
                }
            });
        }
        if (!fShiftRightActions.isEmpty()) {
            shiftRightClickActions.put(slot, e -> {
                if (checkClickRequirement(shiftRightReq != null ? shiftRightReq : generalReq)) {
                    executeOrderActions(fShiftRightActions, order, profile, shiftId, slotNumber, slot);
                }
            });
        }
        if (!fMiddleActions.isEmpty()) {
            middleClickActions.put(slot, e -> {
                if (checkClickRequirement(middleReq != null ? middleReq : generalReq)) {
                    executeOrderActions(fMiddleActions, order, profile, shiftId, slotNumber, slot);
                }
            });
        }
    }

    private org.bukkit.configuration.ConfigurationSection getSection(org.bukkit.configuration.ConfigurationSection s1,
                                                                     org.bukkit.configuration.ConfigurationSection s2,
                                                                     String... keys) {
        if (s1 != null) {
            for (String k : keys) {
                if (s1.isConfigurationSection(k)) return s1.getConfigurationSection(k);
            }
        }
        if (s2 != null) {
            for (String k : keys) {
                if (s2.isConfigurationSection(k)) return s2.getConfigurationSection(k);
            }
        }
        return null;
    }

    private boolean checkClickRequirement(org.bukkit.configuration.ConfigurationSection reqSec) {
        if (reqSec == null) return true;
        if (!id.seria.core.gui.menu.MenuRequirementEvaluator.checkRequirement(reqSec, player)) {
            String denyMsg = reqSec.getString("deny-message", reqSec.getString("deny_message"));
            if (denyMsg != null && !denyMsg.isBlank()) {
                player.sendMessage(TextUtil.parseForPlayer(player, denyMsg));
            }
            List<String> denyActs = reqSec.getStringList("deny-actions");
            if (denyActs.isEmpty()) denyActs = reqSec.getStringList("deny_actions");
            if (!denyActs.isEmpty()) {
                handleActions(denyActs);
            }
            return false;
        }
        return true;
    }

    private List<String> getActionList(org.bukkit.configuration.ConfigurationSection s1,
                                       org.bukkit.configuration.ConfigurationSection s2,
                                       org.bukkit.configuration.ConfigurationSection s3,
                                       String baseKey) {
        String[] keys = {baseKey, baseKey + "-action", baseKey + "-actions", baseKey + "_action", baseKey + "_actions"};
        if (s1 != null) {
            for (String k : keys) {
                if (s1.contains(k)) return s1.getStringList(k);
            }
        }
        if (s2 != null) {
            for (String k : keys) {
                if (s2.contains(k)) return s2.getStringList(k);
            }
        }
        if (s3 != null) {
            for (String k : keys) {
                if (s3.contains(k)) return s3.getStringList(k);
            }
        }
        return List.of();
    }

    private void executeOrderActions(List<String> actions, JobOrder order, JobProfile profile, String shiftId, int slotNumber, int slot) {
        if (actions == null || actions.isEmpty()) return;
        boolean hasRefresh = actions.stream().anyMatch(a -> a.trim().toLowerCase().startsWith("[refresh"));
        for (String act : actions) {
            String trimmed = act.trim();
            if (trimmed.equalsIgnoreCase("[TURN_IN]") || trimmed.equalsIgnoreCase("[DEPOSIT]")) {
                handleDepositOrder(order, profile, shiftId, slotNumber);
            } else if (trimmed.equalsIgnoreCase("[OPEN_ORDER_ACTION]") || trimmed.equalsIgnoreCase("[ORDER_ACTION]")) {
                new OrderActionMenu(plugin, player, job, slotNumber, order).open();
                return;
            } else if (trimmed.equalsIgnoreCase("[jobs_reroll]") || trimmed.equalsIgnoreCase("[reroll]")) {
                if (slotNumber == 100 || plugin.getJobsManager().isSlotNonRerollable(player, job, profile, slotNumber) || (order != null && !order.isRerollable())) {
                    player.sendMessage(plugin.getConfigManager().getEventDenyRerollMessage());
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                if (profile != null && order != null && profile.isOrderCompleted(shiftId, job.getId(), order.getId())) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.already-completed",
                            "<red>Pesanan ini sudah selesai dan tidak dapat di-reroll!</red>"));
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                if (plugin.getJobsManager().executeReroll(player, job.getId(), slotNumber)) {
                    if (!hasRefresh) {
                        refreshSlot(slot);
                    }
                }
            } else if (trimmed.equalsIgnoreCase("[jobs_reroll_auto]") || trimmed.equalsIgnoreCase("[reroll_auto]")) {
                if (slotNumber == 100 || plugin.getJobsManager().isSlotNonRerollable(player, job, profile, slotNumber) || (order != null && !order.isRerollable())) {
                    player.sendMessage(plugin.getConfigManager().getEventDenyRerollMessage());
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                if (profile != null && order != null && profile.isOrderCompleted(shiftId, job.getId(), order.getId())) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.already-completed",
                            "<red>Pesanan ini sudah selesai dan tidak dapat di-reroll!</red>"));
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                handleAutoReroll(profile, slotNumber, slot, hasRefresh);
            } else if (trimmed.equalsIgnoreCase("[jobs_reroll_gins]") || trimmed.equalsIgnoreCase("[reroll_gins]")) {
                if (slotNumber == 100 || plugin.getJobsManager().isSlotNonRerollable(player, job, profile, slotNumber) || (order != null && !order.isRerollable())) {
                    player.sendMessage(plugin.getConfigManager().getEventDenyRerollMessage());
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                if (profile != null && order != null && profile.isOrderCompleted(shiftId, job.getId(), order.getId())) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.already-completed",
                            "<red>Pesanan ini sudah selesai dan tidak dapat di-reroll!</red>"));
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                if (plugin.getJobsManager().executeRerollGins(player, job.getId(), slotNumber)) {
                    if (!hasRefresh) {
                        refreshSlot(slot);
                    }
                }
            } else if (trimmed.equalsIgnoreCase("[jobs_reroll_serium]") || trimmed.equalsIgnoreCase("[reroll_serium]")) {
                if (slotNumber == 100 || plugin.getJobsManager().isSlotNonRerollable(player, job, profile, slotNumber) || (order != null && !order.isRerollable())) {
                    player.sendMessage(plugin.getConfigManager().getEventDenyRerollMessage());
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                if (profile != null && order != null && profile.isOrderCompleted(shiftId, job.getId(), order.getId())) {
                    player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.already-completed",
                            "<red>Pesanan ini sudah selesai dan tidak dapat di-reroll!</red>"));
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                if (plugin.getJobsManager().executeRerollSerium(player, job.getId(), slotNumber)) {
                    if (!hasRefresh) {
                        refreshSlot(slot);
                    }
                }
            } else if (trimmed.equalsIgnoreCase("[DEPOSIT_ALL]")) {
                handleDepositAll();
            } else if (trimmed.equalsIgnoreCase("[CLOSE]")) {
                player.closeInventory();
            } else {
                handleActions(List.of(trimmed));
            }
        }
    }

    private void handleAutoReroll(JobProfile profile, int slotNumber, int slot, boolean hasRefresh) {
        int rerollCount = profile.getRerollCount();
        int maxGinsRolls = plugin.getConfig().getInt("reroll.max-count", 10);
        boolean success;
        if (rerollCount <= 0 && plugin.getConfig().getBoolean("reroll.first-roll-free", true)) {
            success = plugin.getJobsManager().executeReroll(player, job.getId(), slotNumber);
        } else if (rerollCount < maxGinsRolls) {
            success = plugin.getJobsManager().executeRerollGins(player, job.getId(), slotNumber);
        } else {
            success = plugin.getJobsManager().executeRerollSerium(player, job.getId(), slotNumber);
        }
        if (success && !hasRefresh) {
            refreshSlot(slot);
        }
    }

    public void handleDepositOrder(JobOrder order, JobProfile profile, String shiftId, int slotNumber) {
        if (profile.isOrderCompleted(shiftId, job.getId(), order.getId())) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.already-completed"));
            return;
        }

        int jobLevel = profile.getJobLevel(job.getId());
        int required = order.getRequiredAmount(jobLevel);
        int currentProgress = profile.getOrderProgress(shiftId, job.getId(), order.getId());
        int remainingNeeded = required - currentProgress;
        if (remainingNeeded <= 0) return;

        int count = ItemMatcher.countMatchingItems(player, order);
        if (count <= 0) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.deposit-none"));
            return;
        }

        int toRemove = Math.min(count, remainingNeeded);
        int removed = ItemMatcher.removeMatchingItems(player, order, toRemove);
        if (removed <= 0) return;

        profile.addOrderProgress(shiftId, job.getId(), order.getId(), removed);
        int newProgress = profile.getOrderProgress(shiftId, job.getId(), order.getId());

        player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.deposit-success",
                "{amount}", String.valueOf(removed),
                "{item}", ItemMatcher.getItemDisplayName(order),
                "{order}", order.getDisplayName()));

        if (newProgress >= required) {
            profile.markOrderCompleted(shiftId, job.getId(), order.getId());
            profile.incrementOrdersCompleted();
            Bukkit.getPluginManager().callEvent(new OrderCompleteEvent(player, job, order));

            // Execute all rewards
            double rewardMult = plugin.getReputationBridgeService() != null
                    ? plugin.getReputationBridgeService().getRewardMultiplier(player)
                    : 1.0;
            for (Reward reward : order.getRewards()) {
                reward.apply(player, job, rewardMult);
            }

            // Execute reward actions if present
            if (plugin.getActionRunnerService() != null) {
                WorkOrder wo = (profile != null && slotNumber > 0) ? profile.getActiveOrder(slotNumber) : null;
                int rep = (wo != null && wo.getGeneratedReputationAmount() > 0)
                        ? wo.getGeneratedReputationAmount()
                        : (order.getMinReputationAmount() > 0 ? order.getMinReputationAmount() : 10);

                if (!order.getRewardActions().isEmpty()) {
                    plugin.getActionRunnerService().executeAllActions(player, order.getRewardActions(), rep);
                } else if (rep > 0) {
                    String defAction = plugin.getConfig().getString(
                            "reputation.default-action",
                            "[console] reputasi give %player% %amount%"
                    );
                    if (defAction != null && !defAction.isBlank()) {
                        plugin.getActionRunnerService().executeAction(player, defAction, rep);
                    }
                }
            }

            // Check and trigger weight-based rare drop
            if (plugin.getJobsManager() != null) {
                plugin.getJobsManager().checkAndTriggerRareReward(player, job, order);
            }

            player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.order-completed",
                    "{order}", order.getDisplayName()));
        }

        plugin.getPlayerDataManager().saveProfileAsync(profile);

        // Refresh UI
        refresh();
    }

    private void handleDepositAll() {
        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
        String shiftId = plugin.getOrderPoolManager().getCurrentShiftId();
        List<JobOrder> activeOrders = new ArrayList<>(plugin.getOrderPoolManager().getActiveOrders(job.getId()));
        if (plugin.getConfigManager().isEventActive()) {
            JobOrder evOrder = plugin.getOrderPoolManager().getActiveEventOrder(job.getId());
            if (evOrder != null && !activeOrders.contains(evOrder)) {
                activeOrders.add(evOrder);
            }
        }

        int jobLevel = profile != null ? profile.getJobLevel(job.getId()) : 1;
        int totalItemsDeposited = 0;
        for (JobOrder order : activeOrders) {
            if (profile != null && profile.isOrderCompleted(shiftId, job.getId(), order.getId())) {
                continue;
            }

            int required = order.getRequiredAmount(jobLevel);
            int currentProgress = profile != null ? profile.getOrderProgress(shiftId, job.getId(), order.getId()) : 0;
            int remainingNeeded = required - currentProgress;
            if (remainingNeeded <= 0) continue;

            int count = ItemMatcher.countMatchingItems(player, order);
            if (count <= 0) continue;

            int toRemove = Math.min(count, remainingNeeded);
            int removed = ItemMatcher.removeMatchingItems(player, order, toRemove);
            if (removed <= 0) continue;

            totalItemsDeposited += removed;
            if (profile != null) {
                profile.addOrderProgress(shiftId, job.getId(), order.getId(), removed);
                int newProgress = profile.getOrderProgress(shiftId, job.getId(), order.getId());

                if (newProgress >= required) {
                    profile.markOrderCompleted(shiftId, job.getId(), order.getId());
                    profile.incrementOrdersCompleted();
                    Bukkit.getPluginManager().callEvent(new OrderCompleteEvent(player, job, order));

                    double rewardMult = plugin.getReputationBridgeService() != null
                            ? plugin.getReputationBridgeService().getRewardMultiplier(player)
                            : 1.0;
                    for (Reward reward : order.getRewards()) {
                        reward.apply(player, job, rewardMult);
                    }

                    if (plugin.getActionRunnerService() != null) {
                        WorkOrder wo = null;
                        for (WorkOrder w : profile.getActiveOrders().values()) {
                            if (w != null && w.getTemplate() != null && w.getTemplate().getTemplateId().equalsIgnoreCase(order.getId())) {
                                wo = w;
                                break;
                            }
                        }
                        int rep = (wo != null && wo.getGeneratedReputationAmount() > 0)
                                ? wo.getGeneratedReputationAmount()
                                : (order.getMinReputationAmount() > 0 ? order.getMinReputationAmount() : 10);

                        if (!order.getRewardActions().isEmpty()) {
                            plugin.getActionRunnerService().executeAllActions(player, order.getRewardActions(), rep);
                        } else if (rep > 0) {
                            String defAction = plugin.getConfig().getString(
                                    "reputation.default-action",
                                    "[console] reputasi give %player% %amount%"
                            );
                            if (defAction != null && !defAction.isBlank()) {
                                plugin.getActionRunnerService().executeAction(player, defAction, rep);
                            }
                        }
                    }

                    if (plugin.getJobsManager() != null) {
                        plugin.getJobsManager().checkAndTriggerRareReward(player, job, order);
                    }

                    player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.order-completed",
                            "{order}", order.getDisplayName()));
                }
            }
        }

        if (totalItemsDeposited > 0) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.deposit-all-success"));
            plugin.getPlayerDataManager().saveProfileAsync(profile);
            refresh();
        } else {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.deposit-none"));
        }
    }

    private String createProgressBar(int progress, int total) {
        int barLength = 20;
        double percentage = (double) progress / Math.max(1, total);
        int filled = (int) Math.round(percentage * barLength);
        filled = Math.min(barLength, Math.max(0, filled));

        StringBuilder sb = new StringBuilder("<dark_gray>[</dark_gray><green>");
        for (int i = 0; i < filled; i++) {
            sb.append("■");
        }
        sb.append("</green><gray>");
        for (int i = filled; i < barLength; i++) {
            sb.append("■");
        }
        sb.append("</gray><dark_gray>]</dark_gray>");
        return sb.toString();
    }
}
