package id.seria.jobs.model.reward;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JobXPReward implements Reward {

    private final int xp;

    public JobXPReward(int xp) {
        this.xp = xp;
    }

    public int getXp() {
        return xp;
    }

    @Override
    public void apply(@NotNull Player player, @NotNull Job job) {
        SeriaJobs.getInstance().getPlayerDataManager().addJobXp(player, job.getId(), xp);
    }

    @NotNull
    @Override
    public String getFormattedDisplay() {
        return "<yellow>+ " + xp + " Job XP</yellow>";
    }
}
