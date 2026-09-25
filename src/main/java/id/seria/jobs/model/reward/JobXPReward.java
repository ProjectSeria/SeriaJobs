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
        SeriaJobs.getInstance().getPlayerDataManager().addJobXp(player, job.getId(), xp);
    }

    @NotNull
    @Override
    public String getFormattedDisplay() {
        if (customDisplay != null && !customDisplay.trim().isEmpty()) {
            return customDisplay
                    .replace("{amount}", String.valueOf(xp))
                    .replace("{xp}", String.valueOf(xp));
        }
        return "<yellow>+ " + xp + " Job XP</yellow>";
    }
}
