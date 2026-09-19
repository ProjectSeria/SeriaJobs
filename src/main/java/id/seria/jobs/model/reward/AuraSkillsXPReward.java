package id.seria.jobs.model.reward;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AuraSkillsXPReward implements Reward {

    private final String skillName;
    private final double xp;

    public AuraSkillsXPReward(String skillName, double xp) {
        this.skillName = skillName;
        this.xp = xp;
    }

    public String getSkillName() {
        return skillName;
    }

    public double getXp() {
        return xp;
    }

    @Override
    public void apply(@NotNull Player player, @NotNull Job job) {
        if (SeriaJobs.getInstance().getHookManager().isAuraSkillsLoaded()) {
            SeriaJobs.getInstance().getHookManager().getAuraSkillsHook().addSkillXp(player, skillName, xp);
        }
    }

    @NotNull
    @Override
    public String getFormattedDisplay() {
        return "<aqua>+ " + (int) xp + " " + skillName.substring(0, 1).toUpperCase() + skillName.substring(1) + " Skill XP</aqua>";
    }
}
