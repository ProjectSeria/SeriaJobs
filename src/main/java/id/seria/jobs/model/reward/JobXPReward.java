package id.seria.jobs.model.reward;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JobXPReward implements Reward {

    private final int xp;
    private final String customDisplay;

    public JobXPReward(int xp) {
        this(xp, null);
    }

    public JobXPReward(int xp, String customDisplay) {
        this.xp = xp;
        this.customDisplay = customDisplay;
    }

    public int getXp() {
        return xp;
    }

    @Override
    public String getCustomDisplay() {
        return customDisplay;
    }

    @Override
    public void apply(@NotNull Player player, @NotNull Job job) {
        apply(player, job, 1.0);
    }

    @Override
    public void apply(@NotNull Player player, @NotNull Job job, double multiplier) {
        int effective = multiplier > 0.0 ? Math.max(1, (int) Math.round(xp * multiplier)) : xp;
        SeriaJobs.getInstance().getPlayerDataManager().addJobXp(player, job.getId(), effective);
    }

    @NotNull
    @Override
    public String getFormattedDisplay() {
        return getFormattedDisplay(1.0);
    }

    @NotNull
    @Override
    public String getFormattedDisplay(double multiplier) {
        int effective = multiplier > 0.0 ? Math.max(1, (int) Math.round(xp * multiplier)) : xp;
        if (customDisplay != null && !customDisplay.trim().isEmpty()) {
            return customDisplay
                    .replace("{amount}", String.valueOf(effective))
                    .replace("{xp}", String.valueOf(effective));
        }
        return "<yellow>+ " + effective + " Job XP</yellow>";
    }
}
