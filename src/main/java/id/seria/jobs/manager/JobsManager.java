package id.seria.jobs.manager;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.api.OrderCompleteEvent;
import id.seria.jobs.hook.AuraSkillsHook;
import id.seria.jobs.model.Job;
import id.seria.jobs.model.JobOrder;
import id.seria.jobs.model.JobProfile;
import id.seria.jobs.model.WorkOrder;
import id.seria.jobs.model.reward.Reward;
import id.seria.jobs.service.ActionRunnerService;
import id.seria.jobs.service.EconomyService;
import id.seria.jobs.service.OrderGenerationService;
import id.seria.jobs.service.ReputationBridgeService;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Pusat eksekusi alur penyetoran barang (turn-in) dan aksi reroll pasar.
 */
public class JobsManager {

    private final SeriaJobs plugin;
    private final EconomyService economyService;
    private final AuraSkillsHook auraSkillsHook;
    private final ActionRunnerService actionRunnerService;
    private final ReputationBridgeService reputationBridgeService;
    private final PlayerDataManager dataManager;
    private final OrderGenerationService orderGenerationService;

    public JobsManager(@NotNull SeriaJobs plugin,
                       @NotNull EconomyService economyService,
                       @Nullable AuraSkillsHook auraSkillsHook,
                       @NotNull ActionRunnerService actionRunnerService,
                       @NotNull ReputationBridgeService reputationBridgeService,
                       @NotNull PlayerDataManager dataManager,
                       @NotNull OrderGenerationService orderGenerationService) {
        this.plugin = plugin;
        this.economyService = economyService;
        this.auraSkillsHook = auraSkillsHook;
        this.actionRunnerService = actionRunnerService;
        this.reputationBridgeService = reputationBridgeService;
        this.dataManager = dataManager;
        this.orderGenerationService = orderGenerationService;
    }

    /**
     * Eksekusi penyetoran barang (Turn-In) untuk slot pesanan pemain.
     */
    public boolean executeTurnIn(@NotNull Player player, int slotIndex) {
        JobProfile profile = dataManager.getProfile(player.getUniqueId());
        if (profile.getActiveJobs().isEmpty()) return false;
        String firstJobId = profile.getActiveJobs().iterator().next();
        return executeTurnIn(player, firstJobId, slotIndex);
    }

    /**
     * Eksekusi penyetoran barang (Turn-In) spesifik untuk suatu job dan slot.
     */
    public boolean executeTurnIn(@NotNull Player player, @NotNull String jobId, int slotIndex) {
        JobProfile profile = dataManager.getProfile(player.getUniqueId());
        Job job = plugin.getJobRegistry().getJob(jobId);
        if (job == null) return false;

        WorkOrder order = profile.getActiveOrder(slotIndex);
        if (order == null) {
            // Fallback: check OrderPoolManager active orders if not yet instantiated into profile
            List<JobOrder> activeOrders = plugin.getOrderPoolManager().getActiveOrders(jobId);
            if (slotIndex >= 1 && slotIndex <= activeOrders.size()) {
                JobOrder jo = activeOrders.get(slotIndex - 1);
                int jobLevel = profile.getJobLevel(jobId);
                double orderMult = plugin.getReputationBridgeService() != null
                        ? plugin.getReputationBridgeService().getOrderMultiplier(player)
                        : 1.0;
                order = orderGenerationService.generateOrder(jo, jobLevel, orderMult);
                profile.setActiveOrder(slotIndex, order);
            }
        }

        if (order == null || order.isCompleted()) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.already-completed"));
            return false;
        }

        // 1. Validasi order.canFulfill(player)
        if (!order.canFulfill(player)) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.deposit-none"));
            return false;
        }

        // 2. Tarik barang via order.consumeItems(player)
        order.consumeItems(player);

        // 3. Berikan Gins via EconomyService.deposit
        double gins = order.getRewardGins();
        if (gins > 0) {
            economyService.deposit(player, gins);
        }

        // 4. Jalankan dispatch action dengan parameter order.getGeneratedReputationAmount()
        int repAmount = order.getGeneratedReputationAmount();
        List<String> actions = order.getActions();
        if (actions != null && !actions.isEmpty()) {
            actionRunnerService.executeAllActions(player, actions, repAmount);
        } else {
            // Fallback default action from config if none configured in template
            String defAction = plugin.getConfig().getString(
                    "reputation.default-action",
                    "[console] excelenteconomy give %player% reputasi %amount%"
            );
            if (defAction != null && !defAction.isBlank() && repAmount > 0) {
                actionRunnerService.executeAction(player, defAction, repAmount);
            }
        }

        // 5. Eksekusi semua hadiah yang didefinisikan secara dinamis di file YAML
        boolean hasAuraReward = false;
        boolean hasVaultReward = false;
        boolean hasJobXpReward = false;

        if (order.getTemplate().getOriginalJobOrder() != null) {
            JobOrder orig = order.getTemplate().getOriginalJobOrder();
            for (Reward r : orig.getRewards()) {
                if (r instanceof id.seria.jobs.model.reward.AuraSkillsXPReward) hasAuraReward = true;
                if (r instanceof id.seria.jobs.model.reward.VaultReward) hasVaultReward = true;
                if (r instanceof id.seria.jobs.model.reward.JobXPReward) hasJobXpReward = true;
                r.apply(player, job);
            }
        }

        // Fallback untuk instance WorkOrder yang tidak memiliki YAML reward list
        if (!hasVaultReward && gins > 0) {
            economyService.deposit(player, gins);
        }
        if (!hasJobXpReward && order.getRewardJobXp() > 0) {
            dataManager.addJobXp(player, job.getId(), order.getRewardJobXp());
        }
        if (!hasAuraReward && order.getRewardAuraXp() > 0 && auraSkillsHook != null) {
            String skillName = job.getAuraSkillName();
            auraSkillsHook.addSkillXp(player, skillName, order.getRewardAuraXp());
        }

        // 6. Roll probabilitas rare reward (mendukung rare-drop-chance dari order atau default-chance di config)
        double rareChance = order.getRareDropChance();
        if (rareChance <= 0.0) {
            double defChance = plugin.getConfig().getDouble("rare-drop.default-chance", 0.0);
            if (defChance > 1.0) defChance /= 100.0;
            rareChance = defChance;
        } else if (rareChance > 1.0) {
            rareChance /= 100.0;
        }

        if (rareChance > 0.0 && ThreadLocalRandom.current().nextDouble() < rareChance) {
            triggerRareReward(player, job, order.getGeneratedReputationAmount());
        }

        // 8. Set order.setCompleted(true)
        order.setCompleted(true);
        String shiftId = plugin.getOrderPoolManager().getCurrentShiftId();
        profile.markOrderCompleted(shiftId, job.getId(), order.getTemplate().getTemplateId());
        profile.incrementOrdersCompleted();

        // Increment leaderboard completed orders
        dataManager.recordLeaderboardOrderCompletion(player.getUniqueId(), job.getId());

        // Call Bukkit event
        if (order.getTemplate().getOriginalJobOrder() != null) {
            Bukkit.getPluginManager().callEvent(new OrderCompleteEvent(player, job, order.getTemplate().getOriginalJobOrder()));
        }

        // Send completion message & sound
        player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.order-completed",
                "{order}", order.getTemplate().getOriginalJobOrder() != null ? order.getTemplate().getOriginalJobOrder().getDisplayName() : order.getTemplate().getTemplateId()));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);

        dataManager.saveProfileAsync(profile);
        return true;
    }

    /**
     * Eksekusi reroll order murni tanpa pemotongan ekonomi otomatis di Java.
     * Logika ekonomi (Gins, Serium, saldo, token) dapat sepenuhnya diatur melalui
     * click-requirement dan click-actions pada file menu YAML.
     */
    public boolean executeReroll(@NotNull Player player, int slotIndex) {
        JobProfile profile = dataManager.getProfile(player.getUniqueId());
        if (profile.getActiveJobs().isEmpty()) return false;
        String firstJobId = profile.getActiveJobs().iterator().next();
        return executeReroll(player, firstJobId, slotIndex);
    }

    public boolean executeReroll(@NotNull Player player, @NotNull String jobId, int slotIndex) {
        JobProfile profile = dataManager.getProfile(player.getUniqueId());
        Job job = plugin.getJobRegistry().getJob(jobId);
        if (job == null) return false;

        // 0. Cek apakah pesanan pada slot ini sudah selesai disetor di shift saat ini
        if (isSlotOrderCompleted(player, job, profile, slotIndex)) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.already-completed",
                    "<red>Pesanan ini sudah selesai dan tidak dapat di-reroll!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        // 1. Validasi kuota reroll (kuota Gins; Serium dapat digunakan setelahnya)
        int maxReroll = plugin.getConfig().getInt("reroll.max-count", 10);
        boolean allowSeriumAfterMax = plugin.getConfig().getBoolean("reroll.allow-serium-after-max", true);
        if (!allowSeriumAfterMax && !profile.canReroll(maxReroll)) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.reroll-max-reached",
                    "<red>Batas maksimal reroll (" + maxReroll + "x) periode ini sudah habis!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        // 2. Generate order baru untuk slot target
        rollNewOrderForSlot(player, job, profile, slotIndex);

        // 3. Increment counter reroll
        profile.incrementReroll();

        // 4. Sound & Save
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);

        dataManager.saveProfileAsync(profile);
        return true;
    }

    /**
     * Eksekusi reroll order menggunakan Gins (mata uang utama Vault).
     */
    public boolean executeRerollGins(@NotNull Player player, int slotIndex) {
        JobProfile profile = dataManager.getProfile(player.getUniqueId());
        if (profile.getActiveJobs().isEmpty()) return false;
        String firstJobId = profile.getActiveJobs().iterator().next();
        return executeRerollGins(player, firstJobId, slotIndex);
    }

    public boolean executeRerollGins(@NotNull Player player, @NotNull String jobId, int slotIndex) {
        JobProfile profile = dataManager.getProfile(player.getUniqueId());
        Job job = plugin.getJobRegistry().getJob(jobId);
        if (job == null) return false;

        // 0. Cek apakah pesanan pada slot ini sudah selesai disetor di shift saat ini
        if (isSlotOrderCompleted(player, job, profile, slotIndex)) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.already-completed",
                    "<red>Pesanan ini sudah selesai dan tidak dapat di-reroll!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        // 1. Validasi playerData.canReroll()
        if (!profile.canReroll()) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.reroll-max-reached",
                    "<red>Batas maksimal reroll (10x) periode ini sudah habis!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        // 2. Hitung biaya lewat OrderGenerationService
        int cost = orderGenerationService.calculateRerollCost(profile.getRerollCount());

        // 3. Validasi & potong saldo Gins via Vault
        if (!economyService.has(player, cost)) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.reroll-insufficient-gins",
                    "<red>Saldo Gins tidak mencukupi untuk reroll! Dibutuhkan: " + cost + " Gins.</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        economyService.withdraw(player, cost);

        // 4. Generate order baru untuk slot target
        rollNewOrderForSlot(player, job, profile, slotIndex);

        // 5. Jalankan playerData.incrementReroll()
        profile.incrementReroll();

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        dataManager.saveProfileAsync(profile);
        return true;
    }

    /**
     * Eksekusi reroll order menggunakan 1 Serium (mata uang premium).
     */
    public boolean executeRerollSerium(@NotNull Player player, int slotIndex) {
        JobProfile profile = dataManager.getProfile(player.getUniqueId());
        if (profile.getActiveJobs().isEmpty()) return false;
        String firstJobId = profile.getActiveJobs().iterator().next();
        return executeRerollSerium(player, firstJobId, slotIndex);
    }

    public boolean executeRerollSerium(@NotNull Player player, @NotNull String jobId, int slotIndex) {
        JobProfile profile = dataManager.getProfile(player.getUniqueId());
        Job job = plugin.getJobRegistry().getJob(jobId);
        if (job == null) return false;

        // 0. Cek apakah pesanan pada slot ini sudah selesai disetor di shift saat ini
        if (isSlotOrderCompleted(player, job, profile, slotIndex)) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.already-completed",
                    "<red>Pesanan ini sudah selesai dan tidak dapat di-reroll!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        // 1. Validasi playerData.canReroll()
        if (!profile.canReroll()) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.reroll-max-reached",
                    "<red>Batas maksimal reroll (10x) periode ini sudah habis!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        // 2. Potong biaya 1 Serium
        int seriumCost = plugin.getConfig().getInt("reroll.serium-cost", 1);
        if (!deductSerium(player, seriumCost)) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "orders.reroll-insufficient-serium",
                    "<red>Saldo Serium tidak mencukupi untuk reroll! Dibutuhkan: " + seriumCost + " Serium.</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        // 3. Generate order baru untuk slot target
        rollNewOrderForSlot(player, job, profile, slotIndex);

        // 4. Jalankan playerData.incrementReroll()
        profile.incrementReroll();

        // 5. Sound & Save
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);

        dataManager.saveProfileAsync(profile);
        return true;
    }

    public boolean isSlotOrderCompleted(@NotNull Player player, @NotNull Job job, @NotNull JobProfile profile, int slotIndex) {
        if (plugin.getOrderPoolManager() == null) return false;
        String shiftId = plugin.getOrderPoolManager().getCurrentShiftId();
        WorkOrder wo = profile.getActiveOrder(slotIndex);
        String orderId = (wo != null && wo.getTemplate() != null) ? wo.getTemplate().getTemplateId() : null;
        if (orderId == null) {
            List<JobOrder> activeOrders = plugin.getOrderPoolManager().getActiveOrders(job.getId());
            if (slotIndex > 0 && slotIndex <= activeOrders.size()) {
                orderId = activeOrders.get(slotIndex - 1).getId();
            }
        }
        return orderId != null && profile.isOrderCompleted(shiftId, job.getId(), orderId);
    }

    private void rollNewOrderForSlot(Player player, Job job, JobProfile profile, int slotIndex) {
        List<JobOrder> pool = new ArrayList<>(job.getOrdersPool().values());
        if (pool.isEmpty()) return;

        // Filter out currently active orders on other slots to ensure variety
        WorkOrder current = profile.getActiveOrder(slotIndex);
        String currentId = current != null ? current.getTemplate().getTemplateId() : null;

        List<JobOrder> available = new ArrayList<>();
        for (JobOrder jo : pool) {
            if (!jo.getId().equalsIgnoreCase(currentId)) {
                available.add(jo);
            }
        }
        if (available.isEmpty()) available = pool;

        JobOrder chosen = available.get(ThreadLocalRandom.current().nextInt(available.size()));
        int jobLevel = profile.getJobLevel(job.getId());
        double orderMult = plugin.getReputationBridgeService() != null
                ? plugin.getReputationBridgeService().getOrderMultiplier(player)
                : 1.0;
        WorkOrder newWorkOrder = orderGenerationService.generateOrder(chosen, jobLevel, orderMult);
        profile.setActiveOrder(slotIndex, newWorkOrder);
    }

    public boolean deductSerium(Player player, int amount) {
        // 1. Check ExcellentEconomy / CoinsEngine
        if (plugin.getHookManager() != null && plugin.getHookManager().isExcellentEconomyLoaded()) {
            double current = plugin.getHookManager().getExcellentEconomyHook().getBalance(player, "serium");
            if (current >= amount) {
                return plugin.getHookManager().getExcellentEconomyHook().withdraw(player, "serium", amount);
            }
        }

        // 2. Custom Command fallback from config
        String seriumCmd = plugin.getConfig().getString("reroll.serium-command");
        if (seriumCmd != null && !seriumCmd.isBlank()) {
            String formatted = seriumCmd.replace("%player%", player.getName()).replace("%amount%", String.valueOf(amount));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatted);
            return true;
        }

        // 3. Fallback: if economy service supports it or vault
        return false;
    }

    public void checkAndTriggerRareReward(@NotNull Player player, @NotNull Job job, @NotNull JobOrder order) {
        double rareChance = order.getRareDropChance();
        if (rareChance <= 0.0) {
            double defChance = plugin.getConfig().getDouble("rare-drop.default-chance", 0.0);
            if (defChance > 1.0) defChance /= 100.0;
            rareChance = defChance;
        } else if (rareChance > 1.0) {
            rareChance /= 100.0;
        }

        if (rareChance > 0.0 && ThreadLocalRandom.current().nextDouble() < rareChance) {
            int rep = order.getMinReputationAmount() > 0 ? order.getMinReputationAmount() : 10;
            triggerRareReward(player, job, rep);
        }
    }

    public void triggerRareReward(@NotNull Player player, @NotNull Job job, int reputationAmount) {
        org.bukkit.configuration.ConfigurationSection rewardsSec = plugin.getConfig().getConfigurationSection("rare-drop.rewards");

        // 1. Weight-based drop table evaluation
        if (rewardsSec != null && !rewardsSec.getKeys(false).isEmpty()) {
            int totalWeight = 0;
            List<String> entryKeys = new ArrayList<>();
            List<Integer> cumulativeWeights = new ArrayList<>();

            for (String key : rewardsSec.getKeys(false)) {
                int w = rewardsSec.getInt(key + ".weight", 1);
                if (w > 0) {
                    totalWeight += w;
                    entryKeys.add(key);
                    cumulativeWeights.add(totalWeight);
                }
            }

            if (totalWeight > 0) {
                int roll = ThreadLocalRandom.current().nextInt(totalWeight);
                String selectedKey = entryKeys.get(0);
                for (int i = 0; i < cumulativeWeights.size(); i++) {
                    if (roll < cumulativeWeights.get(i)) {
                        selectedKey = entryKeys.get(i);
                        break;
                    }
                }

                org.bukkit.configuration.ConfigurationSection chosenSec = rewardsSec.getConfigurationSection(selectedKey);
                if (chosenSec != null) {
                    // Mendukung entry "none" / "empty" / "zonk" jika admin ingin probabilitas kosong langsung di tabel weight
                    if (selectedKey.equalsIgnoreCase("none") || selectedKey.equalsIgnoreCase("empty") || selectedKey.equalsIgnoreCase("zonk")) {
                        return;
                    }

                    // Send item/pool specific message or fallback to general
                    String msg = chosenSec.getString("message", plugin.getConfig().getString("rare-drop.message"));
                    if (msg != null && !msg.isBlank()) {
                        player.sendMessage(id.seria.jobs.utils.TextUtil.parseForPlayer(player, msg));
                    }

                    // Play specific sound or fallback
                    String soundKey = chosenSec.getString("sound", plugin.getConfig().getString("rare-drop.sound", "UI_TOAST_CHALLENGE_COMPLETE"));
                    if (soundKey != null && !soundKey.isBlank()) {
                        actionRunnerService.executeAction(player, "[sound] " + soundKey, reputationAmount);
                    }

                    // Execute reward actions
                    List<String> actions = chosenSec.getStringList("actions");
                    if (!actions.isEmpty()) {
                        actionRunnerService.executeAllActions(player, actions, reputationAmount);
                    }
                    return;
                }
            }
        }

        // 2. Global Fallback if no rewards table defined
        String rareMsg = plugin.getConfig().getString(
                "rare-drop.message",
                "<gold><bold>[BONUS LANGKA!]</bold> <yellow>Kamu mendapatkan drop langka dari pesanan pasar ini!</yellow>"
        );
        if (rareMsg != null && !rareMsg.isBlank()) {
            player.sendMessage(id.seria.jobs.utils.TextUtil.parseForPlayer(player, rareMsg));
        }

        List<String> rareActions = plugin.getConfig().getStringList("rare-drop.actions");
        if (!rareActions.isEmpty()) {
            actionRunnerService.executeAllActions(player, rareActions, reputationAmount);
        }
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    public EconomyService getEconomyService() {
        return economyService;
    }

    public AuraSkillsHook getAuraSkillsHook() {
        return auraSkillsHook;
    }

    public ActionRunnerService getActionRunnerService() {
        return actionRunnerService;
    }

    public ReputationBridgeService getReputationBridgeService() {
        return reputationBridgeService;
    }

    public PlayerDataManager getDataManager() {
        return dataManager;
    }

    public OrderGenerationService getOrderGenerationService() {
        return orderGenerationService;
    }
}
