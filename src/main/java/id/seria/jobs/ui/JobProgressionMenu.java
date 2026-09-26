package id.seria.jobs.ui;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import id.seria.jobs.model.JobProfile;
import id.seria.jobs.model.reward.Reward;
import id.seria.jobs.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class JobProgressionMenu extends BaseMenu {

    private final Job job;
    private int page;

    public Job getJob() {
        return job;
    }

    public JobProgressionMenu(SeriaJobs plugin, Player player, Job job) {
        this(plugin, player, job, 1);
    }

    public JobProgressionMenu(SeriaJobs plugin, Player player, Job job, int page) {
        super(plugin, player, "job_progression_gui.yml");
        this.job = job;
        this.page = Math.max(1, page);
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
                .replace("{page}", String.valueOf(page))
                .replace("%page%", String.valueOf(page));
        return super.replacePlaceholders(text);
    }

    @Override
    public void build() {
        createInventory(54, "<gradient:#f59e0b:#d97706>Roadmap Hadiah: " + job.getDisplayName() + "</gradient>");
        applyFillers();
        applyCustomStaticItems();

        List<Integer> slots = menuConfig != null && menuConfig.isList("level-slots")
                ? menuConfig.getIntegerList("level-slots")
                : List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);

        int perPage = slots.size();
        int maxLevel = job.getMaxLevel();
        int maxPage = Math.max(1, (int) Math.ceil((double) maxLevel / perPage));
        if (page > maxPage) page = maxPage;

        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
        int currentLevel = profile.getJobLevel(job.getId());
        int currentXp = profile.getJobXp(job.getId());

        int startLevel = (page - 1) * perPage + 1;

        for (int i = 0; i < perPage; i++) {
            int slot = slots.get(i);
            int level = startLevel + i;
            if (level > maxLevel) {
                // Empty remaining slots on last page
                inventory.setItem(slot, null);
                clickActions.remove(slot);
                continue;
            }

            boolean isReached = level <= currentLevel;
            boolean isNext = level == currentLevel + 1;
            int requiredXp = job.getRequiredXp(level);

            ItemStack item = createLevelItem(level, isReached, isNext, requiredXp, currentXp);
            inventory.setItem(slot, item);
            clickActions.put(slot, e -> e.setCancelled(true));
        }

        // Apply page info to pagination buttons if configured
        int finalMaxPage = maxPage;
        if (menuConfig != null) {
            int prevSlot = menuConfig.getInt("items.previous_page.slot", 48);
            int nextSlot = menuConfig.getInt("items.next_page.slot", 50);

            if (page > 1) {
                clickActions.put(prevSlot, e -> {
                    e.setCancelled(true);
                    page--;
                    refresh();
                });
            } else {
                clickActions.put(prevSlot, e -> e.setCancelled(true));
            }

            if (page < finalMaxPage) {
                clickActions.put(nextSlot, e -> {
                    e.setCancelled(true);
                    page++;
                    refresh();
                });
            } else {
                clickActions.put(nextSlot, e -> e.setCancelled(true));
            }
        }
    }

    private ItemStack createLevelItem(int level, boolean isReached, boolean isNext, int requiredXp, int currentXp) {
        int needed = Math.max(0, requiredXp - currentXp);
        boolean hasMilestone = job.hasRewardsForLevel(level);

        // Pre-build milestone reward display lines
        List<String> rewardLines = new ArrayList<>();
        List<String> displayRewards = job.getDisplayRewardsForLevel(level);
        if (displayRewards != null && !displayRewards.isEmpty()) {
            for (String d : displayRewards) {
                rewardLines.add("  " + d);
            }
        } else {
            List<Reward> rewards = job.getRewardsForLevel(level);
            if (rewards != null && !rewards.isEmpty()) {
                for (Reward r : rewards) {
                    rewardLines.add("  " + r.getFormattedDisplay());
                }
            }
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{level}", String.valueOf(level));
        placeholders.put("{job_name}", job.getDisplayName());
        placeholders.put("{job_id}", job.getId());
        placeholders.put("{required_xp}", String.valueOf(level == 1 ? 0 : requiredXp));
        placeholders.put("{current_xp}", String.valueOf(currentXp));
        placeholders.put("{needed_xp}", String.valueOf(needed));
        placeholders.put("{status}", isReached ? "TERCAPAI" : (isNext ? "LEVEL BERIKUTNYA" : "TERKUNCI"));
        placeholders.put("{status_color}", isReached ? "<green>" : (isNext ? "<yellow>" : "<gray>"));

        // 1. Check if specific custom icon/item is configured for this level in job config
        org.bukkit.configuration.ConfigurationSection customIconSec = job.getLevelIcon(level);
        if (customIconSec != null) {
            return buildConfiguredLevelItem(customIconSec, placeholders, rewardLines, isReached, isNext);
        }

        // 2. Check if menuConfig defines level-display templates
        String state = isReached ? "reached" : (isNext ? "next" : "locked");
        org.bukkit.configuration.ConfigurationSection displaySec = null;
        if (menuConfig != null) {
            if (hasMilestone && menuConfig.isConfigurationSection("level-display." + state + "-milestone")) {
                displaySec = menuConfig.getConfigurationSection("level-display." + state + "-milestone");
            } else if (menuConfig.isConfigurationSection("level-display." + state)) {
                displaySec = menuConfig.getConfigurationSection("level-display." + state);
            }
        }

        if (displaySec != null) {
            return buildConfiguredLevelItem(displaySec, placeholders, rewardLines, isReached, isNext);
        }

        // 3. Fallback default item (amount is 1, not level, avoiding 64+ stack glitch)
        Material mat;
        if (isReached) {
            mat = hasMilestone ? Material.EMERALD : Material.LIME_STAINED_GLASS_PANE;
        } else if (isNext) {
            mat = hasMilestone ? Material.GOLD_BLOCK : Material.YELLOW_STAINED_GLASS_PANE;
        } else {
            mat = hasMilestone ? Material.CHEST : Material.GRAY_STAINED_GLASS_PANE;
        }

        ItemStack item = new ItemStack(mat, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String title;
            if (isReached) {
                title = "<green><bold>Level " + level + "</bold></green> <dark_gray>[TERCAPAI]</dark_gray>";
            } else if (isNext) {
                title = "<yellow><bold>Level " + level + "</bold></yellow> <gold>[LEVEL BERIKUTNYA]</gold>";
            } else {
                title = "<gray>Level " + level + "</gray> <dark_gray>[TERKUNCI]</dark_gray>";
            }
            meta.displayName(TextUtil.parseForPlayer(player, title));

            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.parseForPlayer(player, "<gray>Kebutuhan XP: <white>" + (level == 1 ? 0 : requiredXp) + " XP</white></gray>"));

            if (isNext) {
                lore.add(TextUtil.parseForPlayer(player, "<gray>Progres Anda: <yellow>" + currentXp + " / " + requiredXp + " XP</yellow> <dark_gray>(Sisa: " + needed + " XP)</dark_gray>"));
            }

            lore.add(Component.empty().decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
            if (hasMilestone) {
                lore.add(TextUtil.parseForPlayer(player, "<gold><bold>Hadiah Milestone Khusus:</bold></gold>"));
                if (!rewardLines.isEmpty()) {
                    for (String rLine : rewardLines) {
                        lore.add(TextUtil.parseForPlayer(player, rLine));
                    }
                } else {
                    lore.add(TextUtil.parseForPlayer(player, "  <dark_gray>Tidak ada rincian hadiah.</dark_gray>"));
                }
            } else {
                lore.add(TextUtil.parseForPlayer(player, "<dark_gray>Tidak ada reward khusus di level ini.</dark_gray>"));
            }

            lore.add(Component.empty().decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
            if (isReached) {
                lore.add(TextUtil.parseForPlayer(player, "<green>✔ Anda sudah mencapai level ini!</green>"));
            } else if (isNext) {
                lore.add(TextUtil.parseForPlayer(player, "<yellow>▶ Selesaikan kontrak kerja untuk naik ke level ini!</yellow>"));
            } else {
                lore.add(TextUtil.parseForPlayer(player, "<red>✖ Capai level sebelumnya terlebih dahulu.</red>"));
            }

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack buildConfiguredLevelItem(org.bukkit.configuration.ConfigurationSection sec,
                                              Map<String, String> placeholders,
                                              List<String> rewardLines,
                                              boolean isReached,
                                              boolean isNext) {
        ItemStack item = resolveBaseItem(sec, player);
        int amount = sec.getInt("amount", 1);
        item.setAmount(Math.max(1, amount));

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (sec.contains("custom-model-data")) {
                meta.setCustomModelData(sec.getInt("custom-model-data"));
            }

            if (sec.getBoolean("glow", false)) {
                try {
                    meta.setEnchantmentGlintOverride(true);
                } catch (Throwable t) {
                    meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
                    meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                }
            }

            if (sec.contains("name")) {
                String rawName = sec.getString("name");
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    rawName = rawName.replace(entry.getKey(), entry.getValue());
                }
                rawName = replacePlaceholders(rawName);
                meta.displayName(TextUtil.parseForPlayer(player, rawName));
            }

            if (sec.contains("lore")) {
                List<String> rawLore = sec.getStringList("lore");
                List<Component> lore = new ArrayList<>();
                for (String line : rawLore) {
                    if (line.contains("{rewards_list}") || line.contains("{milestone_info}")) {
                        if (!rewardLines.isEmpty()) {
                            for (String rLine : rewardLines) {
                                lore.add(TextUtil.parseForPlayer(player, rLine));
                            }
                        } else {
                            lore.add(TextUtil.parseForPlayer(player, "<dark_gray>Tidak ada reward khusus di level ini.</dark_gray>"));
                        }
                        continue;
                    }

                    String replaced = line;
                    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                        replaced = replaced.replace(entry.getKey(), entry.getValue());
                    }
                    replaced = replacePlaceholders(replaced);
                    lore.add(TextUtil.parseForPlayer(player, replaced));
                }
                meta.lore(lore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    private void applyCustomStaticItems() {
        applyStaticItems();

        if (menuConfig != null && menuConfig.isConfigurationSection("items")) {
            for (String key : menuConfig.getConfigurationSection("items").getKeys(false)) {
                int slot = menuConfig.getInt("items." + key + ".slot", -1);
                List<String> actions = menuConfig.getStringList("items." + key + ".actions");

                if (actions.contains("[BACK]")) {
                    clickActions.put(slot, e -> {
                        e.setCancelled(true);
                        new OrderDepositMenu(plugin, player, job).open();
                    });
                }
            }
        }
    }

    @Override
    protected void handleActions(List<String> actions) {
        if (actions == null) return;
        for (String act : actions) {
            String trimmed = act.trim();
            if (trimmed.equalsIgnoreCase("[BACK]")) {
                new OrderDepositMenu(plugin, player, job).open();
                return;
            } else if (trimmed.equalsIgnoreCase("[PREV_PAGE]")) {
                if (page > 1) {
                    page--;
                    refresh();
                }
                return;
            } else if (trimmed.equalsIgnoreCase("[NEXT_PAGE]")) {
                int perPage = menuConfig != null && menuConfig.isList("level-slots")
                        ? menuConfig.getIntegerList("level-slots").size() : 21;
                int maxPage = Math.max(1, (int) Math.ceil((double) job.getMaxLevel() / perPage));
                if (page < maxPage) {
                    page++;
                    refresh();
                }
                return;
            }
        }
        super.handleActions(actions);
    }
}
