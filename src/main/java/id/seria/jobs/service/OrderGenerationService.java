package id.seria.jobs.service;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.JobOrder;
import id.seria.jobs.model.OrderTemplate;
import id.seria.jobs.model.WorkOrder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Menangani kalkulasi pengacakan order, nilai reputasi, dan penentuan biaya reroll.
 */
public class OrderGenerationService {

    private final SeriaJobs plugin;

    public OrderGenerationService(@NotNull SeriaJobs plugin) {
        this.plugin = plugin;
    }

    /**
     * Membangun instance WorkOrder baru dengan nilai reputasi acak antara min dan max.
     */
    @NotNull
    public WorkOrder generateOrder(@NotNull OrderTemplate template) {
        int min = template.getMinReputationAmount();
        int max = template.getMaxReputationAmount();

        int generatedReputation;
        if (min <= 0 && max <= 0) {
            generatedReputation = 0;
        } else if (min >= max) {
            generatedReputation = min;
        } else {
            generatedReputation = ThreadLocalRandom.current().nextInt(min, max + 1);
        }

        ItemStack item = null;
        if (template.getOriginalJobOrder() != null) {
            JobOrder orig = template.getOriginalJobOrder();
            if (orig.getMaterial() != null) {
                item = new ItemStack(orig.getMaterial(), template.getTargetAmount());
            }
        }
        if (item == null) {
            Material mat = template.getTargetMaterial() != null ? template.getTargetMaterial() : Material.PAPER;
            item = new ItemStack(mat, Math.max(1, template.getTargetAmount()));
        }

        return new WorkOrder(
                UUID.randomUUID(),
                template,
                item,
                generatedReputation,
                template.getRewardGins(),
                template.getRewardJobXp(),
                template.getRewardAuraXp(),
                template.getRareDropChance(),
                template.getRewardActions(),
                false
        );
    }

    /**
     * Helper to generate a WorkOrder directly from a JobOrder and job level.
     */
    @NotNull
    public WorkOrder generateOrder(@NotNull JobOrder order, int jobLevel) {
        return generateOrder(order, jobLevel, 1.0);
    }

    /**
     * Helper to generate a WorkOrder directly from a JobOrder, job level, and reputation order multiplier.
     */
    @NotNull
    public WorkOrder generateOrder(@NotNull JobOrder order, int jobLevel, double orderMultiplier) {
        int scaledAmount = order.getRequiredAmount(jobLevel, orderMultiplier);
        OrderTemplate template = order.toTemplate(scaledAmount);
        return generateOrder(template);
    }

    /**
     * Menerapkan formula linear bertahap untuk biaya reroll:
     * Biaya = min(Base * (1 + currentRollCount * 0.5), Cap)
     * Base = 3.000 Gins, Kenaikan = +1.500 Gins per roll, Cap = 15.000 Gins.
     */
    public int calculateRerollCost(int currentRollCount) {
        if (currentRollCount <= 0 && plugin.getConfig().getBoolean("reroll.first-roll-free", true)) {
            return 0;
        }
        int effectiveRoll = Math.max(0, currentRollCount - 1);
        double base = plugin.getConfig().getDouble("reroll.formula.base", 3000.0);
        double cap = plugin.getConfig().getDouble("reroll.formula.cap", 15000.0);
        double cost = base * (1.0 + (effectiveRoll * 0.5));
        return (int) Math.round(Math.min(cost, cap));
    }
}
