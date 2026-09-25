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

            clickActions.put(slot, e -> {
                e.setCancelled(true);
                handleDepositOrder(order, profile, shiftId);
            });
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
