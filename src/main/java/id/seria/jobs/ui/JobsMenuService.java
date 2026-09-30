package id.seria.jobs.ui;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import id.seria.jobs.model.JobOrder;
import id.seria.jobs.model.JobProfile;
import id.seria.jobs.model.WorkOrder;
import id.seria.jobs.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Penyusun tampilan dan rendering visual GUI Jobs, slot order, dan tombol reroll dinamis.
 */
public class JobsMenuService {

    private final SeriaJobs plugin;

    // Slot positions for orders (row 2: 20, 21, 22, 23, 24)
    public static final int[] ORDER_SLOTS = {20, 21, 22, 23, 24};
    // Slot positions for reroll buttons directly below orders (row 4: 29, 30, 31, 32, 33)
    public static final int[] REROLL_SLOTS = {29, 30, 31, 32, 33};

    public JobsMenuService(@NotNull SeriaJobs plugin) {
        this.plugin = plugin;
    }

    /**
     * Membuka menu utama jobs untuk pemain.
     */
    public void openMainMenu(@NotNull Player player) {
        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
        String jobId = "";
        if (!profile.getActiveJobs().isEmpty()) {
            jobId = profile.getActiveJobs().iterator().next();
        } else if (!plugin.getJobRegistry().getJobs().isEmpty()) {
            jobId = plugin.getJobRegistry().getJobs().iterator().next().getId();
        }
        openJobMenu(player, jobId);
    }

    /**
     * Membuka menu pasar & order untuk job tertentu dengan slot 1-5 dan tombol reroll.
     */
    public void openJobMenu(@NotNull Player player, @NotNull String jobId) {
        Job job = plugin.getJobRegistry().getJob(jobId);
        if (job == null && !plugin.getJobRegistry().getJobs().isEmpty()) {
            job = plugin.getJobRegistry().getJobs().iterator().next();
        }
        if (job == null) return;

        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
        int rep = plugin.getReputationBridgeService() != null
                ? plugin.getReputationBridgeService().getPlayerReputation(player)
                : 0;

        int thresholdSlot4 = plugin.getConfig().getInt("reputation.slot4-threshold", 500);
        String donorPerm = plugin.getConfig().getString("slots.paid-rank-permission", "seriajobs.rank.paid");
        boolean hasPaidRank = player.hasPermission(donorPerm)
                || player.hasPermission("seriajobs.donor")
                || player.hasPermission("seriajobs.vip")
                || player.isOp();

        int maxAllowedSlots = profile.getMaxAllowedSlots(rep, thresholdSlot4, hasPaidRank);

        // Buat inventory 54 slot dengan holder JobsMenuHolder
        JobsMenuHolder holder = new JobsMenuHolder(player.getUniqueId(), job.getId());
        Component titleComp = TextUtil.parse("<gradient:#2c3e50:#3498db>Bursa Kerja: " + job.getDisplayName() + "</gradient>");
        Inventory inv = Bukkit.createInventory(holder, 54, titleComp);
        holder.setInventory(inv);

        // Fill background
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, filler);
        }

        // Header: Info Pemain
        int jobLevel = profile.getJobLevel(job.getId());
        int jobXp = profile.getJobXp(job.getId());
        int reqXp = job.getRequiredXp(jobLevel);
        List<String> playerInfoLore = List.of(
                "<gray>Profesi: <white>" + job.getDisplayName() + "</white>",
                "<gray>Level: <gold>" + jobLevel + "</gold>",
                "<gray>XP: <yellow>" + jobXp + " / " + reqXp + "</yellow>",
                "<gray>Reputasi: <aqua>" + rep + "</aqua>",
                "<gray>Kapasitas Slot Terbuka: <green>" + maxAllowedSlots + " / 5</green>",
                "<gray>Sisa Kuota Reroll: <yellow>" + (10 - profile.getRerollCount()) + " / 10</yellow>",
                "<gray>Reset Berikutnya: <gold>" + plugin.getOrderPoolManager().getFormattedCountdown() + "</gold>"
        );
        inv.setItem(4, createItem(Material.PLAYER_HEAD, "<green><bold>" + player.getName() + " - Profil Pekerja</bold></green>", playerInfoLore));

        // Render 5 Order Slots & Tombol Reroll
        List<JobOrder> activeTemplates = plugin.getOrderPoolManager().getActiveOrders(job.getId());

        for (int i = 0; i < 5; i++) {
            int slotNumber = i + 1; // 1 to 5
            int orderInvSlot = ORDER_SLOTS[i];
            int rerollInvSlot = REROLL_SLOTS[i];

            boolean isSlotUnlocked = slotNumber <= maxAllowedSlots;

            if (!isSlotUnlocked) {
                // Slot Terkunci
                if (slotNumber == 4) {
                    List<String> lockLore = List.of(
                            "<red>Slot ke-4 terkunci!</red>",
                            "<gray>Dibutuhkan minimal <aqua>" + thresholdSlot4 + " Reputasi</aqua>.",
                            "<gray>Reputasi saat ini: <yellow>" + rep + "</yellow>",
                            "",
                            "<dark_gray>Selesaikan pesanan pasar untuk menambah reputasi.</dark_gray>"
                    );
                    inv.setItem(orderInvSlot, createItem(Material.IRON_BARS, "<red><bold>Slot 4: Terkunci (Reputasi)</bold></red>", lockLore));
                } else {
                    List<String> lockLore = List.of(
                            "<red>Slot ke-5 terkunci!</red>",
                            "<gray>Slot ini khusus untuk pemain dengan status <gold>Donatur / Paid Rank</gold>.",
                            "",
                            "<yellow>Beli rank atau paket donatur untuk membuka 5 slot penuh!</yellow>"
                    );
                    inv.setItem(orderInvSlot, createItem(Material.GOLD_NUGGET, "<gold><bold>Slot 5: Terkunci (Rank Donatur)</bold></gold>", lockLore));
                }

                // Locked Reroll Button
                inv.setItem(rerollInvSlot, createItem(Material.BARRIER, "<red>Reroll Terkunci</red>", List.of("<gray>Slot pesanan ini belum terbuka.</gray>")));
                continue;
            }

            // Slot Terbuka: Ambil atau generate WorkOrder
            WorkOrder workOrder = profile.getActiveOrder(slotNumber);
            if (workOrder == null && i < activeTemplates.size()) {
                JobOrder jo = activeTemplates.get(i);
                double orderMult = plugin.getReputationBridgeService() != null
                        ? plugin.getReputationBridgeService().getOrderMultiplier(player)
                        : 1.0;
                workOrder = plugin.getOrderGenerationService().generateOrder(jo, jobLevel, orderMult);
                profile.setActiveOrder(slotNumber, workOrder);
            }

            if (workOrder != null) {
                // Render Order Card
                ItemStack orderCard = renderOrderCard(player, job, workOrder, slotNumber);
                inv.setItem(orderInvSlot, orderCard);

                // Render Tombol Reroll
                ItemStack rerollButton = renderRerollButton(profile, slotNumber);
                inv.setItem(rerollInvSlot, rerollButton);
            } else {
                inv.setItem(orderInvSlot, createItem(Material.OAK_SIGN, "<gray>Tidak ada pesanan pasar aktif</gray>", null));
                inv.setItem(rerollInvSlot, createItem(Material.BARRIER, "<gray>Tidak dapat reroll</gray>", null));
            }
        }

        // Navigation Buttons
        inv.setItem(48, createItem(Material.EXPERIENCE_BOTTLE, "<gold><bold>Level Rewards Roadmap</bold></gold>", List.of("<gray>Klik untuk melihat hadiah kenaikan level.</gray>")));
        inv.setItem(49, createItem(Material.CLOCK, "<aqua><bold>Refresh Tampilan</bold></aqua>", List.of("<gray>Klik untuk memperbarui tampilan pesanan.</gray>")));
        inv.setItem(50, createItem(Material.BARRIER, "<red><bold>Tutup Menu</bold></red>", null));

        player.openInventory(inv);
    }

    private ItemStack renderOrderCard(Player player, Job job, WorkOrder workOrder, int slotIndex) {
        boolean completed = workOrder.isCompleted();
        Material mat = completed ? Material.EMERALD_BLOCK : workOrder.getTargetItem().getType();
        if (mat == Material.AIR) mat = Material.PAPER;

        String name = completed
                ? "<green><bold>[SELESAI] Slot #" + slotIndex + ": " + workOrder.getTemplate().getTemplateId() + "</bold></green>"
                : "<yellow><bold>Slot #" + slotIndex + ": " + workOrder.getTemplate().getTemplateId() + "</bold></yellow>";

        List<String> lore = new ArrayList<>();
        lore.add("<gray>Target: <white>" + workOrder.getTemplate().getTargetAmount() + "x " + formatMaterialName(workOrder.getTargetItem().getType()) + "</white>");
        if (workOrder.getGeneratedReputationAmount() > 0) {
            lore.add("<gray>Reputasi Hadiah: <aqua>+" + workOrder.getGeneratedReputationAmount() + " Reputasi</aqua>");
        }
        lore.add("");
        lore.add("<yellow><bold>Hadiah Kontrak:</bold></yellow>");

        double rewardMult = plugin.getReputationBridgeService() != null
                ? plugin.getReputationBridgeService().getRewardMultiplier(player)
                : 1.0;

        if (workOrder.getTemplate().getOriginalJobOrder() != null && workOrder.getTemplate().getOriginalJobOrder().hasCustomRewardsDisplay()) {
            lore.addAll(workOrder.getTemplate().getOriginalJobOrder().getCustomRewardsDisplay());
        } else if (workOrder.getTemplate().getOriginalJobOrder() != null && !workOrder.getTemplate().getOriginalJobOrder().getRewards().isEmpty()) {
            for (id.seria.jobs.model.reward.Reward r : workOrder.getTemplate().getOriginalJobOrder().getRewards()) {
                String disp = r.getFormattedDisplay(rewardMult);
                if (!disp.startsWith(" ") && !disp.startsWith("▪")) {
                    disp = "  ▪ " + disp;
                }
                lore.add(disp);
            }
        } else {
            if (workOrder.getRewardGins() > 0) {
                lore.add("  ▪ <gold>" + String.format(java.util.Locale.US, "%,.0f", workOrder.getRewardGins() * rewardMult) + " Gins</gold>");
            }
            if (workOrder.getRewardJobXp() > 0) {
                lore.add("  ▪ <green>+" + (int) Math.round(workOrder.getRewardJobXp() * rewardMult) + " Job XP</green>");
            }
            if (workOrder.getRewardAuraXp() > 0) {
                lore.add("  ▪ <light_purple>+" + (int) Math.round(workOrder.getRewardAuraXp() * rewardMult) + " AuraSkills XP</light_purple>");
            }
        }

        if (workOrder.getRareDropChance() > 0) {
            lore.add("");
            lore.add("<gray>Peluang Bonus Langka: <yellow>" + (int)(workOrder.getRareDropChance() * 100) + "%</yellow>");
        }
        lore.add("");

        if (completed) {
            lore.add("<green>✔ Pesanan ini telah berhasil kamu selesaikan!</green>");
            lore.add("<dark_gray>Gunakan tombol Reroll di bawah jika ingin pesanan baru di slot ini.</dark_gray>");
        } else {
            boolean canFulfill = workOrder.canFulfill(player);
            if (canFulfill) {
                lore.add("<green><bold>▶ KLIK KIRI UNTUK SETOR (TURN-IN)</bold></green>");
                lore.add("<gray>Barang di inventarismu mencukupi.</gray>");
            } else {
                lore.add("<red>✖ Barang belum mencukupi di inventaris.</red>");
                lore.add("<gray>Kumpulkan barang yang diminta lalu klik untuk setor.</gray>");
            }
            lore.add("<dark_gray>• Shift + Klik Kanan: Reroll cepat pesanan ini</dark_gray>");
        }

        return createItem(mat, name, lore);
    }

    private ItemStack renderRerollButton(JobProfile profile, int slotIndex) {
        boolean canReroll = profile.canReroll();
        int currentReroll = profile.getRerollCount();
        int remaining = Math.max(0, 10 - currentReroll);

        if (canReroll) {
            int nextCostGins = plugin.getOrderGenerationService().calculateRerollCost(currentReroll);
            List<String> lore = List.of(
                    "<gray>Counter Reroll: <yellow>" + currentReroll + " / 10</yellow>",
                    "<gray>Sisa Kuota Reroll: <green>" + remaining + "x</green>",
                    "",
                    "<gray>Biaya Reroll Gins: <gold>" + String.format(java.util.Locale.US, "%,d", nextCostGins) + " Gins</gold>",
                    "<gray>Biaya Alternatif: <aqua>1 Serium</aqua>",
                    "",
                    "<yellow>[Klik Kiri]</yellow> <white>Reroll dengan Gins</white>",
                    "<aqua>[Klik Kanan]</aqua> <white>Reroll dengan 1 Serium</white>"
            );
            return createItem(Material.EMERALD, "<green><bold>Reroll Pesanan Slot #" + slotIndex + "</bold></green>", lore);
        } else {
            List<String> lore = List.of(
                    "<red>Status: Batas maksimal reroll siklus ini telah tercapai (10/10)!</red>",
                    "<gray>Sisa kuota: <red>0x</red>",
                    "",
                    "<gray>Tunggu siklus rotasi pasar berikutnya (4–6 jam) untuk mereset kuota reroll.</gray>"
            );
            return createItem(Material.BARRIER, "<red><bold>Reroll Terkunci (Maksimal 10/10)</bold></red>", lore);
        }
    }

    private ItemStack createItem(Material mat, String name, @Nullable List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.displayName(TextUtil.parse(name));
            }
            if (lore != null && !lore.isEmpty()) {
                List<Component> compLore = new ArrayList<>();
                for (String line : lore) {
                    compLore.add(TextUtil.parse(line));
                }
                meta.lore(compLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatMaterialName(Material mat) {
        if (mat == null) return "Barang";
        String[] parts = mat.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
