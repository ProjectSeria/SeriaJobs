package id.seria.jobs.ui;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.api.OrderCompleteEvent;
import id.seria.jobs.model.Job;
import id.seria.jobs.model.JobOrder;
import id.seria.jobs.model.JobProfile;
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
        if (player != null && plugin.getPlayerDataManager() != null) {
            JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
            if (profile != null) {
                level = profile.getJobLevel(job.getId());
                xp = profile.getJobXp(job.getId());
                reqXp = job.getRequiredXp(level);
            }
        }
        return text.replace("{job_name}", job.getDisplayName())
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
                .replace("%req_xp%", String.valueOf(reqXp));
    }

    @Override
    public void build() {
        createInventory(45, "<gradient:#2c3e50:#3498db>Bursa Kerja: " + job.getDisplayName() + "</gradient>");
        applyFillers();
        applyStaticItems();

        List<Integer> slots = menuConfig != null
                ? menuConfig.getIntegerList("order-slots")
                : List.of(20, 22, 24);

        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
        String shiftId = plugin.getOrderPoolManager().getCurrentShiftId();
        List<JobOrder> activeOrders = plugin.getOrderPoolManager().getActiveOrders(job.getId());

        int jobLevel = profile != null ? profile.getJobLevel(job.getId()) : 1;
        double resourceMultiplier = plugin.getConfigManager().getResourceMultiplier(jobLevel);
        String multFormatted = String.format(java.util.Locale.US, "%.2fx", resourceMultiplier);

        int slotIdx = 0;
        for (JobOrder order : activeOrders) {
            if (slotIdx >= slots.size()) break;
            int slot = slots.get(slotIdx++);

            boolean isCompleted = profile != null && profile.isOrderCompleted(shiftId, job.getId(), order.getId());
            int progress = profile != null ? profile.getOrderProgress(shiftId, job.getId(), order.getId()) : 0;
            int required = order.getRequiredAmount(jobLevel);
            int percentage = Math.min(100, (int) Math.round(((double) progress / required) * 100));

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
                Material cardMat = order.getMaterial() != null ? order.getMaterial() : Material.CHEST;
                if (isCompleted) cardMat = Material.EMERALD_BLOCK;
                cardItem = new ItemStack(cardMat);
            } else {
                cardItem = cardItem.clone();
            }
            if (isCompleted) {
                cardItem = new ItemStack(Material.EMERALD_BLOCK);
            }

            ItemMeta meta = cardItem.getItemMeta();

            if (meta != null) {
                String statusSection = isCompleted ? "order-card.completed" : "order-card.incomplete";
                String nameFormat = menuConfig != null
                        ? menuConfig.getString(statusSection + ".name", order.getDisplayName())
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
                        .replace("{resource_multiplier}", multFormatted);

                meta.displayName(TextUtil.parseForPlayer(player, nameFormat));

                List<String> rawLore = menuConfig != null
                        ? menuConfig.getStringList(statusSection + ".lore")
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
                                    .replace("{resource_multiplier}", multFormatted);
                            finalLore.add(TextUtil.parseForPlayer(player, parsedOLine));
                        }
                    } else if (line.contains("{rewards_list}")) {
                        if (order.hasCustomRewardsDisplay()) {
                            for (String rLine : order.getCustomRewardsDisplay()) {
                                finalLore.add(TextUtil.parseForPlayer(player, rLine));
                            }
                        } else {
                            for (Reward r : order.getRewards()) {
                                String disp = r.getFormattedDisplay();
                                if (!disp.startsWith(" ") && !disp.startsWith("<lore-color>▪") && !disp.startsWith("▪")) {
                                    disp = "  " + disp;
                                }
                                finalLore.add(TextUtil.parseForPlayer(player, disp));
                            }
                        }
                    } else if (line.contains("{progress_bar}")) {
                        finalLore.add(TextUtil.parseForPlayer(player, createProgressBar(progress, required)));
                    } else {
                        String replaced = line.replace("{order_display_name}", order.getDisplayName())
                                .replace("{order_id}", order.getId())
                                .replace("{job_name}", job.getDisplayName())
                                .replace("{job_id}", job.getId())
                                .replace("{required_amount}", String.valueOf(required))
                                .replace("{required}", String.valueOf(required))
                                .replace("{item_name}", ItemMatcher.getItemDisplayName(order))
                                .replace("{progress}", String.valueOf(progress))
                                .replace("{percentage}", String.valueOf(percentage))
                                .replace("{multiplier}", multFormatted)
                                .replace("{resource_multiplier}", multFormatted);
                        finalLore.add(TextUtil.parseForPlayer(player, replaced));
                    }
                }
                meta.lore(finalLore);
                cardItem.setItemMeta(meta);
            }

            inventory.setItem(slot, cardItem);

            int slotNumber = slotIdx;
            bindOrderCardActions(slot, order, profile, shiftId, slotNumber);
        }
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
            }
        }
        super.handleActions(actions);
    }

    private void bindOrderCardActions(int slot, JobOrder order, JobProfile profile, String shiftId, int slotNumber) {
        org.bukkit.configuration.ConfigurationSection orderCardSec = menuConfig != null ? menuConfig.getConfigurationSection("order-card") : null;
        org.bukkit.configuration.ConfigurationSection incompleteSec = orderCardSec != null ? orderCardSec.getConfigurationSection("incomplete") : null;
        org.bukkit.configuration.ConfigurationSection globalSec = plugin.getConfig().getConfigurationSection("order-clicks");

        List<String> leftActions = getActionList(incompleteSec, orderCardSec, globalSec, "left-click", "left-click-actions");
        List<String> rightActions = getActionList(incompleteSec, orderCardSec, globalSec, "right-click", "right-click-actions");
        List<String> shiftRightActions = getActionList(incompleteSec, orderCardSec, globalSec, "shift-right-click", "shift-right-click-actions");
        List<String> shiftLeftActions = getActionList(incompleteSec, orderCardSec, globalSec, "shift-left-click", "shift-left-click-actions");
        List<String> middleActions = getActionList(incompleteSec, orderCardSec, globalSec, "middle-click", "middle-click-actions");
        List<String> generalActions = getActionList(incompleteSec, orderCardSec, globalSec, "click-actions", "actions");

        final List<String> fLeftActions = (leftActions.isEmpty() && generalActions.isEmpty()) ? List.of("[TURN_IN]") : leftActions;
        final List<String> fShiftRightActions = shiftRightActions.isEmpty() ? List.of("[REROLL_GINS]") : shiftRightActions;
        final List<String> fRightActions = rightActions;
        final List<String> fShiftLeftActions = shiftLeftActions;
        final List<String> fMiddleActions = middleActions;
        final List<String> fGeneralActions = generalActions;

        if (!fGeneralActions.isEmpty()) {
            clickActions.put(slot, e -> executeOrderActions(fGeneralActions, order, profile, shiftId, slotNumber));
        }
        if (!fLeftActions.isEmpty()) {
            leftClickActions.put(slot, e -> executeOrderActions(fLeftActions, order, profile, shiftId, slotNumber));
        }
        if (!fRightActions.isEmpty()) {
            rightClickActions.put(slot, e -> executeOrderActions(fRightActions, order, profile, shiftId, slotNumber));
        }
        if (!fShiftLeftActions.isEmpty()) {
            shiftLeftClickActions.put(slot, e -> executeOrderActions(fShiftLeftActions, order, profile, shiftId, slotNumber));
        }
        if (!fShiftRightActions.isEmpty()) {
            shiftRightClickActions.put(slot, e -> executeOrderActions(fShiftRightActions, order, profile, shiftId, slotNumber));
        }
        if (!fMiddleActions.isEmpty()) {
            middleClickActions.put(slot, e -> executeOrderActions(fMiddleActions, order, profile, shiftId, slotNumber));
        }
    }

    private List<String> getActionList(org.bukkit.configuration.ConfigurationSection s1,
                                       org.bukkit.configuration.ConfigurationSection s2,
                                       org.bukkit.configuration.ConfigurationSection s3,
                                       String k1, String k2) {
        if (s1 != null && s1.contains(k1)) return s1.getStringList(k1);
        if (s1 != null && s1.contains(k2)) return s1.getStringList(k2);
        if (s2 != null && s2.contains(k1)) return s2.getStringList(k1);
        if (s2 != null && s2.contains(k2)) return s2.getStringList(k2);
        if (s3 != null && s3.contains(k1)) return s3.getStringList(k1);
        if (s3 != null && s3.contains(k2)) return s3.getStringList(k2);
        return List.of();
    }

    private void executeOrderActions(List<String> actions, JobOrder order, JobProfile profile, String shiftId, int slotNumber) {
        if (actions == null || actions.isEmpty()) return;
        for (String act : actions) {
            String trimmed = act.trim();
            if (trimmed.equalsIgnoreCase("[TURN_IN]") || trimmed.equalsIgnoreCase("[DEPOSIT]")) {
                handleDepositOrder(order, profile, shiftId);
            } else if (trimmed.equalsIgnoreCase("[REROLL]") || trimmed.equalsIgnoreCase("[REROLL_GINS]")) {
                if (plugin.getJobsManager().executeRerollGins(player, job.getId(), slotNumber)) {
                    refresh();
                }
            } else if (trimmed.equalsIgnoreCase("[REROLL_SERIUM]")) {
                if (plugin.getJobsManager().executeRerollSerium(player, job.getId(), slotNumber)) {
                    refresh();
                }
            } else if (trimmed.equalsIgnoreCase("[DEPOSIT_ALL]")) {
                handleDepositAll();
            } else if (trimmed.equalsIgnoreCase("[REFRESH]")) {
                refresh();
            } else if (trimmed.equalsIgnoreCase("[CLOSE]")) {
                player.closeInventory();
            } else {
                handleActions(List.of(trimmed));
            }
        }
    }

    private void handleDepositOrder(JobOrder order, JobProfile profile, String shiftId) {
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
            Bukkit.getPluginManager().callEvent(new OrderCompleteEvent(player, job, order));

            // Execute all rewards
            for (Reward reward : order.getRewards()) {
                reward.apply(player, job);
            }

            // Execute reward actions if present
            if (plugin.getActionRunnerService() != null && !order.getRewardActions().isEmpty()) {
                int rep = order.getMinReputationAmount() > 0 ? order.getMinReputationAmount() : 10;
                plugin.getActionRunnerService().executeAllActions(player, order.getRewardActions(), rep);
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
        List<JobOrder> activeOrders = plugin.getOrderPoolManager().getActiveOrders(job.getId());

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
                    Bukkit.getPluginManager().callEvent(new OrderCompleteEvent(player, job, order));

                    for (Reward reward : order.getRewards()) {
                        reward.apply(player, job);
                    }

                    if (plugin.getActionRunnerService() != null && !order.getRewardActions().isEmpty()) {
                        int rep = order.getMinReputationAmount() > 0 ? order.getMinReputationAmount() : 10;
                        plugin.getActionRunnerService().executeAllActions(player, order.getRewardActions(), rep);
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
