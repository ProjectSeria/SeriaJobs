package id.seria.jobs.ui;

import id.seria.core.SeriaCorePlugin;
import id.seria.core.gui.menu.BaseSeriaMenu;
import id.seria.core.item.ItemProviderRegistry;
import id.seria.core.placeholder.CustomPlaceholderManager;
import id.seria.jobs.SeriaJobs;
import id.seria.jobs.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Menu SeriaJobs di atas engine menu SeriaCore ({@link BaseSeriaMenu}).
 * Kelas ini hanya menambahkan bagian khusus SeriaJobs: sumber item plugin, render teks {@link TextUtil},
 * action ekonomi reroll dan navigasi ke menu Java SeriaJobs.
 */
public abstract class BaseMenu extends BaseSeriaMenu {

    // Menyembunyikan field Plugin milik BaseSeriaMenu agar subclass mendapat tipe SeriaJobs
    protected final SeriaJobs plugin;

    public BaseMenu(SeriaJobs plugin, Player player, String menuConfigName) {
        super(plugin, player, plugin.getMenuRegistry().getMenu(menuConfigName));
        this.plugin = plugin;
    }

    /**
     * Susun isi inventory: {@code createInventory}, {@code applyFillers}, {@code applyMenuItems} dan konten dinamis.
     */
    public abstract void build();

    @Override
    public void open() {
        build();
        player.openInventory(inventory);
    }

    @Override
    public void refresh() {
        if (inventory == null) {
            open();
            return;
        }
        clearActions();
        build();
        player.updateInventory();
    }

    @Override
    protected void applyCustomContent() {
        // Konten dinamis disusun oleh build() masing-masing menu
    }

    @Override
    protected String replacePlaceholders(String text) {
        return replacePlaceholders(text, null);
    }

    /**
     * Resolve placeholder lewat engine SeriaCore (%custom_*%, %seriajobs_*%, %jobsorder_*%, PAPI, $e(...)).
     * {@code context} berisi token literal (mis. "{xp}" -> "120") yang juga berlaku di custom placeholder bersarang.
     */
    protected String replacePlaceholders(String text, Map<String, String> context) {
        if (text == null || text.isEmpty())
            return "";
        if (text.indexOf('%') == -1 && text.indexOf('{') == -1 && !text.contains("$e(")) {
            return text;
        }
        return CustomPlaceholderManager.parse(player, text, context);
    }

    @Override
    protected @NotNull Component parseText(@Nullable String text) {
        return TextUtil.parseForPlayer(player, text);
    }

    @Override
    protected @NotNull ItemStack resolveBaseItem(@NotNull ConfigurationSection sec) {
        ItemProviderRegistry items = SeriaCorePlugin.getInstance().getPlaceholderManager().getItemProviderRegistry();

        // 1. MMOItems: "mmoitem: TYPE:ID" atau mmoitem-type + mmoitem-id
        if (sec.contains("mmoitem") || sec.contains("mmoitem-id")) {
            String mmoType = sec.getString("mmoitem-type");
            String mmoId = sec.getString("mmoitem-id");
            String val = sec.getString("mmoitem");
            if (val != null && val.contains(":")) {
                String[] parts = val.split(":", 2);
                mmoType = parts[0];
                mmoId = parts[1];
            } else if (val != null && mmoId == null) {
                mmoId = val;
            }
            if (mmoType != null && mmoId != null) {
                ItemStack item = items.resolveItem("mmoitems", mmoType, mmoId);
                if (item != null) return item;
            }
        }

        // 2. MythicMobs
        String mythicId = sec.getString("mythicitem", sec.getString("mythic-id"));
        if (mythicId != null) {
            ItemStack item = items.resolveItem("mythicmobs", mythicId);
            if (item != null) return item;
        }

        // 3. CustomFishing
        String fishingId = sec.getString("customfishing", sec.getString("fishing-id"));
        if (fishingId != null) {
            ItemStack item = items.resolveItem("customfishing", fishingId);
            if (item != null) return item;
        }

        // 4. Material vanilla (SeriaCore)
        return super.resolveBaseItem(sec);
    }

    /**
     * Titik masuk action SeriaJobs. Subclass menangani token miliknya lalu memanggil super untuk sisanya.
     */
    protected void handleActions(List<String> actions) {
        super.executeActionList(actions);
    }

    @Override
    public void executeActionList(List<String> actions) {
        handleActions(actions);
    }

    @Override
    protected void dispatchConsoleCommand(@NotNull String cmd) {
        // "serium take <player> <amount>" dipotong lewat API ekonomi bila memungkinkan
        if (cmd.toLowerCase().startsWith("serium take ")) {
            String[] parts = cmd.split("\\s+");
            int amount = parts.length > 3 ? (int) Double.parseDouble(parts[3]) : 1;
            if (plugin.getJobsManager().deductSerium(player, amount)) {
                plugin.getLogger().info("Deducted " + amount + " Serium via hook for " + player.getName());
                return;
            }
            cmd = "serium take " + player.getName() + " " + amount;
        }
        plugin.getLogger().info("Executing [console] action for " + player.getName() + ": /" + cmd);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }

    @Override
    protected void handleCustomAction(String action) {
        String lower = action.toLowerCase();
        if (lower.startsWith("[open]")) {
            openMenuByName(action.substring("[open]".length()).trim());
        } else if (lower.startsWith("[deduct_gins]") || lower.startsWith("[deduct-gins]")) {
            String param = afterTag(action);
            int amount = !param.isEmpty()
                    ? (int) Double.parseDouble(param)
                    : plugin.getOrderGenerationService().calculateRerollCost(plugin.getPlayerDataManager().getProfile(player.getUniqueId()).getRerollCount());
            plugin.getEconomyService().withdraw(player, amount);
            plugin.getLogger().info("Deducted " + amount + " Gins directly via Vault for " + player.getName());
        } else if (lower.startsWith("[deduct_serium]") || lower.startsWith("[deduct-serium]")) {
            String param = afterTag(action);
            int amount = !param.isEmpty() ? (int) Double.parseDouble(param) : plugin.getConfig().getInt("reroll.serium-cost", 1);
            plugin.getJobsManager().deductSerium(player, amount);
            plugin.getLogger().info("Deducted " + amount + " Serium directly for " + player.getName());
        }
    }

    @Override
    protected void handleOpenMenuAction(String menuTarget) {
        openMenuByName(menuTarget);
    }

    /**
     * Apakah daftar action menjalankan reroll order ([jobs_reroll], [reroll], dan varian gins/serium/auto).
     */
    protected static boolean isRerollList(List<String> actions) {
        for (String act : actions) {
            String lower = act == null ? "" : act.trim().toLowerCase();
            if (lower.startsWith("[jobs_reroll") || lower.startsWith("[reroll")) return true;
        }
        return false;
    }

    private static String afterTag(String action) {
        int closing = action.indexOf(']');
        return closing != -1 && closing + 1 < action.length() ? action.substring(closing + 1).trim() : "";
    }

    public void openMenuByName(String menuTarget) {
        if (menuTarget == null || menuTarget.isBlank()) return;
        menuTarget = menuTarget.trim();

        String[] tokens = menuTarget.split("\\s+");
        String rawTarget = tokens[0].trim();
        String param = tokens.length > 1 ? tokens[1].trim() : null;

        String clean = rawTarget.toLowerCase();
        if (clean.endsWith(".yml")) {
            clean = clean.substring(0, clean.length() - 4);
        } else if (clean.endsWith(".yaml")) {
            clean = clean.substring(0, clean.length() - 5);
        }

        // 1. Main Jobs Menu aliases
        if (clean.equals("main_job_menu") || clean.equals("main_jobs_menu")
                || clean.equals("main_job") || clean.equals("main_jobs")
                || clean.equals("main") || clean.equals("jobs")
                || clean.equals("jobs_menu") || clean.equals("jobs-menu")
                || clean.equals("bursa")) {
            new MainJobsMenu(plugin, player).open();
            return;
        }

        // 2. Orders GUI aliases
        if (clean.equals("job_order_gui") || clean.equals("job_orders_gui")
                || clean.equals("job_order") || clean.equals("job_orders")
                || clean.equals("order") || clean.equals("orders")
                || clean.equals("order_deposit_menu") || clean.equals("orders_gui")) {
            id.seria.jobs.model.Job targetJob = resolveJob(param);
            if (targetJob != null) {
                new OrderDepositMenu(plugin, player, targetJob).open();
            } else {
                new MainJobsMenu(plugin, player).open();
            }
            return;
        }

        // 3. Progression GUI aliases
        if (clean.equals("job_progression_gui") || clean.equals("job_progression")
                || clean.equals("progression") || clean.equals("roadmap")
                || clean.equals("rewards_roadmap")) {
            id.seria.jobs.model.Job targetJob = resolveJob(param);
            if (targetJob != null) {
                new JobProgressionMenu(plugin, player, targetJob).open();
            } else {
                new MainJobsMenu(plugin, player).open();
            }
            return;
        }

        // 4. Registry-based GUI (GenericMenu fallback for any registered custom YAML)
        if (plugin.getMenuRegistry() != null) {
            String ymlName = clean + ".yml";
            if (plugin.getMenuRegistry().getMenu(ymlName) != null) {
                new GenericMenu(plugin, player, ymlName).open();
                return;
            }
            if (plugin.getMenuRegistry().getMenu(rawTarget) != null) {
                new GenericMenu(plugin, player, rawTarget).open();
                return;
            }
        }

        plugin.getLogger().warning("Could not find menu to open for target: '" + menuTarget + "'");
    }

    protected id.seria.jobs.model.Job resolveJob(String param) {
        if (param != null && !param.isBlank()) {
            id.seria.jobs.model.Job j = plugin.getJobRegistry().getJob(param.trim());
            if (j != null) return j;
        }
        if (this instanceof OrderDepositMenu orderMenu) {
            return orderMenu.getJob();
        }
        if (this instanceof OrderActionMenu actionMenu) {
            return actionMenu.getJob();
        }
        if (this instanceof JobProgressionMenu progMenu) {
            return progMenu.getJob();
        }
        if (player != null && plugin.getPlayerDataManager() != null) {
            id.seria.jobs.model.JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
            if (profile != null && !profile.getActiveJobs().isEmpty()) {
                String jobId = profile.getActiveJobs().iterator().next();
                id.seria.jobs.model.Job j = plugin.getJobRegistry().getJob(jobId);
                if (j != null) return j;
            }
        }
        if (plugin.getJobRegistry() != null && !plugin.getJobRegistry().getJobs().isEmpty()) {
            return plugin.getJobRegistry().getJobs().iterator().next();
        }
        return null;
    }
}
